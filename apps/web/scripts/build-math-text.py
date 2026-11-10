#!/usr/bin/env python3
"""Typeset owned inline notation at build time; no runtime compiler or text fallback."""
from pathlib import Path
import hashlib
import html
import fcntl
import json
import os
import re
import subprocess
import sys
import xml.etree.ElementTree as ET

PROJECT = Path(__file__).resolve().parents[1]
ROOT = PROJECT.parents[1]
OUT = PROJECT / 'site/build/generated/materiewelle-product/public/math-text'
FONT_SIZE = 16
FONT_DIRECTORY = PROJECT / 'site/src/jsMain/resources/public/fonts'
NUMERIC_FONT = 'Latin Modern Mono'
FONT_INPUTS = sorted(FONT_DIRECTORY.glob('*.otf'))
# Full identifiers first. A source ID such as A1/D2 is never an isolated variable.
NAMES = ['E_kin,0', 'E_kin,1', 'E_kin,2', 'Eₖᵢₙ', 'E_kin', 'm_e', 'λNR', 'λrel', 'λgeo', 'λdB', 'λneu', 'λalt', 'λ₀', 'λA', 'λB', 'K₀', 'd₁₁₁', 'd111',
         'AP', 'AQ', 'PB', 'BR', 'AB', 'AR', 'P', 'Q', 'R',
         'θᴮ', 'θB', 'Δs', 'Δx', 'Δy', 'AΣ', 'u₁', 'u₂', 'Eγ', 'Eₙ', 'mₑ', 'N_F',
         'xᵢ₊₁', 'xᵢ', 'xN', 'xₙ', 'pγ', 'x₁', 'x₀', 'p₀', 'nλ', 'eU', 'hf', 'nhf', 'λp', 'λL', 'Lλ',
         'λ', 'θ', 'φ', 'α', 'β', 'γ', 'π', 'τ', 'δ', 'Σ', 'Δ',
         'A', 'B', 'K', 'E', 'U', 'V', 'L', 'W', 'D', 'F', 'G', 'I', 'T', 'N',
         'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'm', 'n', 'p', 's', 't', 'v', 'x', 'y', 'z']
UNITS = ['kg', 'kV', 'eV', 'µm', 'μm', 'pm', 'nm', 'mm', 'rad', 'px', 'Å', 'Å', 'J', 'C', 'V', 'm', 's', '%', '°']
# Greek/subscript identifiers may adjoin another mathematical factor (mₑv²,
# 2πΔs); ASCII identifiers must never be a prefix of prose or source IDs.
NAME = re.compile(r'(?:' + '|'.join(re.escape(n) + (r'(?![A-Za-z0-9_])' if n.isascii() else '')
                                  for n in sorted(NAMES, key=len, reverse=True)) + r')')
NUMBER = re.compile(r'\d+(?:[,.]\d+)?(?:[eE][+−-]?\d+)?')
UNIT = re.compile(r'(?:' + '|'.join(map(re.escape, sorted(UNITS, key=len, reverse=True))) + r')(?![A-Za-z0-9Α-ω_₀-₉ₐ-₟])')
FUNCTION = re.compile(r'(?:sin|cos|tan|sqrt)(?![a-z])')
OP = set('½=≈≠∝<>≤≥≪≫+−-×·*/^√→↔⇒…±²³⁻⁰¹⁴⁵⁶⁷⁸⁹₀₁₂₃₄₅₆₇₈₉()[]|')


def spans(text):
    """Conservative mathematical runs; German words and IDs are hard boundaries."""
    i = 0
    while i < len(text):
        if i and (text[i-1].isalnum() or text[i-1] == '_'):
            i += 1; continue
        first = NAME.match(text, i) or NUMBER.match(text, i)
        if first is None and text[i] not in '√±½':
            i += 1; continue
        start = i; j = i; meaningful = False; brackets = []
        while j < len(text):
            if text[j].isspace(): j += 1; continue
            token = FUNCTION.match(text, j) or NAME.match(text, j) or UNIT.match(text, j) or NUMBER.match(text, j)
            if token:
                meaningful |= bool(NAME.fullmatch(token.group()) or UNIT.fullmatch(token.group()))
                j = token.end(); continue
            ch = text[j]
            if ch in OP:
                if ch in '([': brackets.append(ch)
                elif ch in ')]':
                    if not brackets: break
                    brackets.pop()
                meaningful = True; j += 1; continue
            break
        candidate = text[start:j].strip()
        # Strip open punctuation from a prose boundary, never invent a closed root.
        while candidate and candidate[-1] in '([·×/+-−→': candidate = candidate[:-1].rstrip()
        if candidate and meaningful and not brackets:
            yield candidate
        i = max(i + 1, j)


def kotlin_strings(source):
    """Read text literals while treating interpolation as a dynamic boundary."""
    found = []
    def read(i, triple=False):
        quote = '"""' if triple else '"'; i += len(quote); parts = []
        while i < len(source):
            if source.startswith(quote, i): found.append(''.join(parts)); return i + len(quote)
            if not triple and source[i] == '\\' and i + 1 < len(source):
                escape = source[i+1]; parts.append({'n':'\n','t':'\t','"':'"','\\':'\\'}.get(escape, escape)); i += 2; continue
            if source.startswith('${', i):
                parts.append(' ¤ '); depth = 1; i += 2
                while i < len(source) and depth:
                    if source[i] == '"': i = read(i, source.startswith('"""', i)); continue
                    if source[i] == '{': depth += 1
                    if source[i] == '}': depth -= 1
                    i += 1
                continue
            if source[i] == '$' and i + 1 < len(source) and (source[i+1].isalpha() or source[i+1] == '_'):
                parts.append(' ¤ '); i += 2
                while i < len(source) and (source[i].isalnum() or source[i] == '_'): i += 1
                continue
            parts.append(source[i]); i += 1
        return i
    i = 0
    while i < len(source):
        if source[i] == '"': i = read(i, source.startswith('"""', i))
        else: i += 1
    return found


def notation(source):
    """Translate the project's bounded, reviewed notation to actual Typst math."""
    # Dates and prose compounds such as "150-V-Fall" retain literal hyphens.
    # Treating these as subtraction (or a caption slash as a fraction) changes
    # their meaning. They still consist of genuine Typst paths.
    if (re.fullmatch(r'\d{4}-\d{2}-\d{2}', source)
            or re.fullmatch(r'\d{4}/\d{2}(?:\d{2})?', source)
            or (re.search(r'\d-(?:kg|kV|eV|µm|μm|pm|nm|mm|V|m|s)\b', source)
                and not re.search(r'[=≈≠∝≤≥]', source))):
        return 'upright('+json.dumps(source,ensure_ascii=False)+')'
    quantity_source=source
    source = re.sub(r'([0-9]+(?:[,.][0-9]+)?)[eE]([+−-]?[0-9]+)', lambda m:m[1]+' dot 10^('+m[2].replace('−','-')+')', source)
    if source.startswith('unit:'): return 'upright('+json.dumps(source[5:],ensure_ascii=False)+')'
    s = source.replace('θᴮ','θB').replace('d₁₁₁','d111')
    for index, alias in enumerate(('EkinZero', 'EkinOne', 'EkinTwo')):
        s = s.replace('E_kin,' + str(index), alias)
    s = s.replace('Eₖᵢₙ','Ekin').replace('E_kin','Ekin').replace('m_e','mₑ')
    # A displacement such as Δs is one quantity, although its Typst spelling
    # contains two atoms. Protect it on either side of a fraction; otherwise
    # Typst puts only s over the bar, or reads λ/Δs as (λ/Δ) times s. Keep
    # standalone quantities unparenthesized, including their A/B qualifiers.
    displacement = r'Δ[sxy](?:\([AB]\))?'
    s = re.sub(r'(' + displacement + r')(?=\s*/)', r'(\1)', s)
    s = re.sub(r'(/\s*)(' + displacement + r')', r'\1(\2)', s)
    names = {'AP':'A P', 'AQ':'A Q', 'PB':'P B', 'BR':'B R', 'AB':'A B', 'AR':'A R',
             'λNR':'lambda_"NR"','λrel':'lambda_"rel"','λgeo':'lambda_"geo"','λdB':'lambda_"dB"',
             'λA':'lambda_A','λB':'lambda_B','λneu':'lambda_"neu"','λalt':'lambda_"alt"','K₀':'K_0','d111':'d_(111)','θB':'theta_B','AΣ':'A_Sigma',
             'EkinZero':'E_("kin",0)', 'EkinOne':'E_("kin",1)', 'EkinTwo':'E_("kin",2)',
             'Ekin':'E_"kin"','Eγ':'E_gamma','Eₙ':'E_n','mₑ':'m_e',
             'xᵢ₊₁':'x_(i+1)','xᵢ':'x_i','xN':'x_N','xₙ':'x_n','pγ':'p_gamma','u₁':'u_1','u₂':'u_2','λ₀':'lambda_0','p₀':'p_0','x₁':'x_1','x₀':'x_0',
             'N_F':'N_F','Δs':'Delta s','Δx':'Delta x','Δy':'Delta y'}
    for old in sorted(names,key=len,reverse=True): s=s.replace(old,' '+names[old]+' ')
    for old,new in {'λ':'lambda','θ':'theta','φ':'phi','α':'alpha','β':'beta','γ':'gamma','π':'pi','τ':'tau','Σ':'Sigma','δ':'delta','Δ':'Delta'}.items(): s=s.replace(old,' '+new+' ')
    s=s.replace('−','-').replace('×',' dot ').replace('·',' dot ').replace('√',' sqrt').replace('½','frac(1, 2) ').replace('°',' degree ')
    # Typst [..] is a content block, not a mathematical radicand.
    s=re.sub(r'sqrt\[([^\[\]]*)\]',r'sqrt(\1)',s)
    if source in ('²','³'): return '{}^2' if source == '²' else '{}^3'
    if source == '√': return 'sqrt("")'
    if source in ('(',')','[',']','/','*','^'): return '"'+source+'"'
    s=re.sub(r'([⁻⁰¹²³⁴⁵⁶⁷⁸⁹]+)',lambda m:'^('+m.group().translate(str.maketrans('⁻⁰¹²³⁴⁵⁶⁷⁸⁹','-0123456789'))+')',s)
    # A root without parentheses binds the next atom, not the entire expression.
    # The subscript belongs inside the root too: √E_kin is sqrt(E_"kin"),
    # not sqrt(E)_"kin". Explicit parenthesized radicands stay untouched.
    atom = r'(?:\d+|[A-Za-z]+(?:_(?:"[^"]+"|[A-Za-z0-9]+|\([^)]*\)))?)'
    s=re.sub(r'sqrt\s*(' + atom + r')(?![A-Za-z_(])',r'sqrt(\1)',s)
    for old,new in [('eU','e U'),('Llambda','L lambda'),('lambdaL','lambda L'),('lambda p','lambda p'),('nhf','n h f'),('hf','h f')]: s=s.replace(old,new)
    # Bind trigonometric arguments explicitly before any division: sin θ / λ
    # means sin(θ)/λ, not sin(θ/λ). Parentheses also protect I(0) as denominator.
    s=re.sub(r'\b(sin|cos|tan)(\^\([^)]*\))?\s+((?:alpha|beta|theta|phi)(?:_[A-Za-z0-9]+)?|[0-9]+(?:\s+degree)?)',
             lambda m:m[1]+(m[2] or '')+'('+m[3]+')',s)
    s=re.sub(r'I\s*/\s*I\(0\)',r'I / (I(0))',s)
    s=s.replace('m s^-1','upright("m") thin upright("s")^(-1)')
    # Words in math are allowed only from the closed symbol/function/unit set.
    unit_pattern=r'(?<![A-Za-z_"])('+'|'.join(map(re.escape,sorted(set(UNITS)-{'m','s','V','%','°'},key=len,reverse=True)))+r')(?![A-Za-z_"])'
    s=re.sub(unit_pattern,lambda m:'upright("'+m.group()+'")',s)
    s=s.replace('upright("J") s','upright("J s")')
    # m inside an equation denotes mass. Only a whole quantity has a trailing unit.
    if re.fullmatch(r'[0-9.,+−eE×·⁻⁰¹²³⁴⁵⁶⁷⁸⁹\s-]+(m|s|V)', quantity_source):
        s=re.sub(r'(m|s|V)$',lambda m:' upright("'+m.group(1)+'")',s)
    s=s.replace('°',' degree ').replace('%',' percent ')
    s=re.sub(r'(?<=\d),(?=\d)', '","', s)
    return s.strip()


def collect():
    values=set(NAMES+UNITS+['(',')','[',']','/','*','^','·','×','=', '≈', '≠', '∝', '≤', '≥', '≪', '≫', '<', '>', '+', '−', '±', '→', '↔', '⇒', '…', '²', '³', '√', '½', 'π', 'sin', 'cos', 'tan'])
    inputs=[]
    for directory in [PROJECT/'site/src/jsMain/kotlin', PROJECT/'browser-design/src/commonMain/kotlin',
                      ROOT/'packages/physics-core/src/commonMain/kotlin']:
        for path in sorted(directory.rglob('*.kt')):
            if path.name in {'TypstText.kt','TypstSemantics.kt','TypstSvgLabels.kt'}: continue
            inputs.append(path)
            for value in kotlin_strings(path.read_text()):
                if '<svg' in value or '<text' in value:
                    labels=re.findall(r'<text\b[^>]*>(.*?)</text>',value,re.S)
                elif re.search(r'<(?:path|line|rect|g|circle)\b',value): continue
                else: labels=[value]
                for label in labels:
                    # Dynamic boundaries also make both sides independently typesettable.
                    for segment in html.unescape(label).split('¤'):
                        values.update(spans(segment))
    for path in [ROOT/'content/data/versuchsdaten.json',ROOT/'content/typst/math/formulas.json']:
        inputs.append(path)
        def walk(v):
            if isinstance(v,str): values.update(spans(v))
            elif isinstance(v,list):
                for x in v:walk(x)
            elif isinstance(v,dict):
                for x in v.values():walk(x)
        walk(json.loads(path.read_text()))
    # Standalone aliases used inside assembled expressions must also exist.
    values.update('unit:'+unit for unit in UNITS)
    values.update(['a/√2','a/√3','a√(3/8)','D sin φ','d sin θB','2d sin θB','g sin θ','b sin θ',
                   'λ/λ₀','p/p₀','Δs/λ','t/T','I/I(0)','λ=h/p','λ∝1/√U','p²/(2mₑ)',
                   'W²/(λL)','(g+b)²/(λL)','sin α / α','cos² β','sin θ','sin 60°'])
    return sorted(values),inputs


def compile_svg(typst, expression, theme, key):
    ink='eaf1f5' if theme=='dark' else '182830'
    source=f'''#set document(date:none)
#set page(width:auto,height:auto,margin:0pt,fill:none)
#set text(font:"Latin Modern Math",size:{FONT_SIZE}pt,fill:rgb("{ink}"))
#set par(leading:0pt)
#box(width:0.01pt,height:0.01pt,baseline:0pt,fill:rgb("ff00fe"))${expression}$'''
    svg,warnings=typst.compile_with_warnings(source.encode(),format='svg',
        font_paths=[str(FONT_DIRECTORY)], ignore_system_fonts=True)
    if warnings or not isinstance(svg,bytes): raise ValueError((expression,warnings))
    ET.register_namespace('', 'http://www.w3.org/2000/svg');ET.register_namespace('xlink','http://www.w3.org/1999/xlink')
    tree=ET.fromstring(svg);baseline=None
    def inspect(node,ty=0.0):
        nonlocal baseline
        transform=node.get('transform','');m=re.fullmatch(r'matrix\(1 0 0 1 ([^ ]+) ([^)]+)\)',transform)
        if m:ty+=float(m[2])
        for child in list(node):
            if child.get('fill')=='#ff00fe':baseline=ty+.01;node.remove(child)
            else:inspect(child,ty)
    inspect(tree);assert baseline is not None
    _,_,width,height=map(float,tree.get('viewBox').split())
    raw=ET.tostring(tree,encoding='unicode')
    for old in re.findall(r'\bid="([^"]+)"',raw):raw=raw.replace('id="'+old+'"','id="'+key+'-'+old+'"').replace('="#'+old+'"','="#'+key+'-'+old+'"')
    assert '<text' not in raw and '<script' not in raw and '<foreignObject' not in raw
    return raw,width,height,baseline


def main():
    try:import typst
    except ImportError:
        cache_root = Path(os.environ.get('XDG_CACHE_HOME', Path.home() / '.cache'))
        tools_root = Path(os.environ.get('TASK_TOOLS_ROOT', cache_root / 'materiewelle-tools'))
        python = tools_root / 'typst/bin/python'
        subprocess.run([str(python),str(Path(__file__).resolve()),*sys.argv[1:]],check=True);return
    values,inputs=collect()
    recipe=Path(__file__).resolve()
    inputs.append(recipe)
    inputs.extend(FONT_INPUTS)
    font_hash = hashlib.sha256(b''.join(path.read_bytes() for path in FONT_INPUTS)).hexdigest()
    recipe_hash=hashlib.sha256(recipe.read_bytes()).hexdigest()
    if '--list' in sys.argv:
        print(json.dumps(values,ensure_ascii=False,indent=2));return
    OUT.mkdir(parents=True,exist_ok=True)
    lock=(PROJECT/'site/build/math-text.lock').open('a')
    fcntl.flock(lock,fcntl.LOCK_EX)
    entries=[];failures=[]
    cache_path=PROJECT/'site/build/math-text-cache.json'
    try: cache=json.loads(cache_path.read_text()) if cache_path.exists() else {}
    except (json.JSONDecodeError, OSError): cache={}
    for value in values:
        key=hashlib.sha256(value.encode()).hexdigest()[:20]
        try:
            expression=notation(value)
            variants={}
            for theme in ['light','dark']:
                path=f'{key}-{theme}.svg'
                digest=hashlib.sha256((expression+theme+str(FONT_SIZE)+typst.__version__+recipe_hash+font_hash).encode()).hexdigest()
                cached=cache.get(path)
                if (cached and cached['digest']==digest and (OUT/path).exists()
                        and cached.get('sha256')==hashlib.sha256((OUT/path).read_bytes()).hexdigest()):
                    width,height,baseline=cached['metrics']
                else:
                    svg,width,height,baseline=compile_svg(typst,expression,theme,key+'-'+theme)
                    (OUT/path).write_text(svg)
                    cache[path]={'digest':digest,'metrics':[width,height,baseline],
                                 'sha256':hashlib.sha256(svg.encode()).hexdigest()}
                variants[theme]=f'math-text/{path}'
            entries.append(dict(source=value,**variants,width=width/FONT_SIZE,height=height/FONT_SIZE,baseline=baseline/FONT_SIZE))
        except Exception as error:failures.append({'source':value,'typst':expression,'error':str(error)})
    cache_path.write_text(json.dumps(cache))
    if failures:
        Path('/tmp/materiewelle-math-errors.json').write_text(json.dumps(failures,ensure_ascii=False,indent=2))
        raise RuntimeError(f'{len(failures)} notation errors; see /tmp/materiewelle-math-errors.json')
    glyphs=[]
    for ch in '0123456789.,−-+eE×%° ':
        key='typst-number-'+str(ord(ch))
        # Tabular measurement numerals use the same monospaced family as the UI,
        # still compiled by Typst into actual paths rather than browser text.
        expr='text(font: '+json.dumps(NUMERIC_FONT)+', "'+ch+'")'
        svg,width,height,baseline=compile_svg(typst,expr,'light',key)
        tree=ET.fromstring(svg);body=''.join(ET.tostring(x,encoding='unicode') for x in tree)
        body=body.replace('#182830','currentColor')
        glyphs.append(dict(character=ch,id=key,body=body,width=width/FONT_SIZE,height=height/FONT_SIZE,baseline=baseline/FONT_SIZE))
    digit_widths = [glyph['width'] for glyph in glyphs if glyph['character'].isdigit()]
    if max(digit_widths) - min(digit_widths) > 1e-8:
        raise RuntimeError('The measurement digit font must retain equal advances for 0–9.')
    catalog={'schemaVersion':1,'compiler':'Typst '+typst.__version__,'entries':entries,'glyphs':glyphs,
             'inputs':{str(p.relative_to(ROOT)):hashlib.sha256(p.read_bytes()).hexdigest() for p in inputs}}
    # This directory is owned by the generator. Warm and clean builds must
    # ship the same expression set, without stale, unreferenced old formulae.
    expected={Path(entry[theme]).name for entry in entries for theme in ('light','dark')}
    for old in OUT.glob('*.svg'):
        if re.fullmatch(r'[a-f0-9]{20}-(light|dark)\.svg',old.name) and old.name not in expected: old.unlink()
    (OUT/'catalog.json').write_text(json.dumps(catalog,ensure_ascii=False,separators=(',',':')))
    print(f'Typst inline catalogue: {len(entries)} expressions, {len(glyphs)} numeric glyphs; all real paths, both themes, measured baselines.')

if __name__=='__main__':main()
