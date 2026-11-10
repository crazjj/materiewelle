#!/usr/bin/env python3
"""Build two review-only handouts, leaving the canonical handout untouched.

Run with the pinned Typst environment:
  /workspace/.materiewelle-tools/typst/bin/python content/typst/varianten/build.py

Portable setup: pip install -r content/typst/varianten/requirements.txt
Requires Poppler (pdfinfo, pdffonts, pdftotext, pdftoppm).
"""
from __future__ import annotations

import argparse
import hashlib
import importlib.metadata
import json
import math
from pathlib import Path
import re
import shutil
import subprocess
import tempfile
import xml.etree.ElementTree as ET

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]
DEFAULT_OUTPUT = ROOT / 'docs/handout-varianten'
FONTS = ROOT / 'apps/web/site/src/jsMain/resources/public/fonts'
PROTECTED = (ROOT / 'content/typst/handout.typ', ROOT / 'docs/handout.pdf')
VARIANTS = ('A', 'B')
PPI = 140


def digest(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def tool(name: str, *args: str) -> str:
    if shutil.which(name) is None:
        raise RuntimeError(f'Missing tool: {name}; install poppler-utils.')
    return subprocess.check_output([name, *args], text=True, encoding='utf-8')


def controls(data: dict) -> dict:
    constants = data['constants']
    h, e, me, c = (constants[k]['value'] for k in ('h', 'e', 'electron_mass', 'c'))
    nr = lambda u: h / math.sqrt(2 * me * e * u)
    rel = lambda u: h * c / math.sqrt((e * u) * (e * u + 2 * me * c * c))
    row = data['davisson_germer']['rows'][0]
    d = data['davisson_germer']['geometry']['lattice_constant_m'] * math.sqrt(3 / 8)
    geo = d * math.sin(math.radians(row['phi_deg']))
    marks = data['jonsson']['image']['markers_px']
    return {
        'lambda_150V_pm': (nr(150) * 1e12, 3),
        'lambda_600V_pm': (nr(600) * 1e12, 3),
        'D_nm': (d * 1e9, 4),
        'DG_geo_nm': (geo * 1e9, 3),
        'DG_NR_nm': (nr(row['voltage_V']) * 1e9, 3),
        'DG_signed_difference_percent': ((geo / nr(row['voltage_V']) - 1) * 100, 1),
        'image_period_px': ((marks[-1] - marks[0]) / (len(marks) - 1), 1),
        'lambda_50kV_NR_pm': (nr(50_000) * 1e12, 3),
        'lambda_50kV_rel_pm': (rel(50_000) * 1e12, 3),
        'NR_over_rel_percent': ((nr(50_000) / rel(50_000) - 1) * 100, 2),
    }


def validate(path: Path, expected: dict) -> dict:
    info = tool('pdfinfo', str(path))
    pages = int(re.search(r'^Pages:\s+(\d+)', info, re.M)[1])
    match = re.search(r'^Page size:\s+([\d.]+) x ([\d.]+) pts', info, re.M)
    size = list(map(float, match.groups()))
    if pages != 3 or abs(size[0] - 595.276) > .05 or abs(size[1] - 841.89) > .05:
        raise RuntimeError(f'{path.name}: expected 3 A4 pages; got {pages}, {size}')
    fonts = []
    for row in tool('pdffonts', str(path)).splitlines()[2:]:
        fields = row.split()
        if fields:
            if fields[-5] != 'yes':
                raise RuntimeError(f'{path.name}: unembedded font: {row}')
            fonts.append(fields[0].split('+')[-1])
    if not any('LMRoman' in f for f in fonts) or not any('Math' in f for f in fonts):
        raise RuntimeError(f'{path.name}: Latin Modern Roman/Math fonts missing')
    text = tool('pdftotext', '-layout', str(path), '-')
    normalized = re.sub(r'\s+', ' ', text.replace('−', '-').replace('\u00ad', ''))
    if '�' in text:
        raise RuntimeError(f'{path.name}: replacement character in PDF text')
    rendered_values = {}
    for name, (value, precision) in expected.items():
        rendered = f'{value:.{precision}f}'.replace('.', ',')
        if rendered not in normalized:
            raise RuntimeError(f'{path.name}: expected {name} = {rendered} in PDF')
        rendered_values[name] = rendered
    for term in ('Langevin', 'Bragg', '54', '50', '150', '600', 'H1', 'H3', 'D1', 'D2', 'J1',
                 'Sekundäranalyse', '1961', '1974', '40', '703', '242', '551', '647',
                 'Grauwert', 'keine absolute', 'KI'):
        if term not in normalized:
            raise RuntimeError(f'{path.name}: required shared fact/status missing: {term}')
    bbox = ET.fromstring(tool('pdftotext', '-bbox', str(path), '-'))
    count = 0
    for page in bbox.iter('{http://www.w3.org/1999/xhtml}page'):
        width, height = float(page.attrib['width']), float(page.attrib['height'])
        for word in page.iter('{http://www.w3.org/1999/xhtml}word'):
            x0, y0, x1, y1 = (float(word.attrib[k]) for k in ('xMin', 'yMin', 'xMax', 'yMax'))
            if not (0 <= x0 <= x1 <= width and 0 <= y0 <= y1 <= height):
                raise RuntimeError(f'{path.name}: text outside page: {word.text}')
            count += 1
    if count < 500:
        raise RuntimeError(f'{path.name}: unexpectedly sparse/nonextractable PDF')
    return {'pages': pages, 'paper': 'A4', 'page_size_pt': size,
            'fonts_embedded': fonts, 'text_inside_page': True,
            'extractable_words': count, 'displayed_control_values': rendered_values,
            'compiler_warnings': 0,
            'note': 'Page bounds checked automatically; inspect PNG previews for collisions and reading order.'}


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--output-dir', type=Path, default=DEFAULT_OUTPUT)
    args = parser.parse_args()
    output = args.output_dir.resolve()
    if output in (ROOT, HERE, ROOT / 'docs', ROOT / 'content/typst') or output.is_symlink():
        raise RuntimeError('Choose the preview directory or a separate empty review directory.')
    import typst
    from PIL import Image
    version = importlib.metadata.version('typst')
    if version != '0.14.2':
        raise RuntimeError(f'Use pinned typst==0.14.2, found {version}')
    before = {str(path.relative_to(ROOT)): digest(path) for path in PROTECTED}
    data = json.loads((ROOT / 'content/data/versuchsdaten.json').read_text())
    expected = controls(data)
    manifest = {'kind': 'materiewellen-handout-preview-variants', 'canonical_selection': None,
                'typst_python_version': version, 'preview_ppi': PPI,
                'canonical_files_before': before, 'variants': {}}
    with tempfile.TemporaryDirectory(prefix='materiewelle-handout-variants-') as temp:
        temporary = Path(temp)
        for variant in VARIANTS:
            source = HERE / f'{variant}.typ'
            pdf = temporary / f'{variant}.pdf'
            _, warnings = typst.compile_with_warnings(str(source), output=str(pdf), root=str(ROOT), font_paths=[str(FONTS)])
            if warnings:
                raise RuntimeError(f'{variant}: Typst warnings: {warnings}')
            validation = validate(pdf, expected)
            # Byte-for-byte repeat compilation tests date-free PDF reproducibility.
            repeat = temporary / f'{variant}-repeat.pdf'
            _, warnings = typst.compile_with_warnings(str(source), output=str(repeat), root=str(ROOT), font_paths=[str(FONTS)])
            if warnings or digest(repeat) != digest(pdf):
                raise RuntimeError(f'{variant}: repeated compilation was not identical')
            repeat.unlink()
            tool('pdftoppm', '-r', str(PPI), '-png', '-singlefile', '-f', '1', str(pdf), str(temporary / f'{variant}-seite-1'))
            for page in (2, 3):
                tool('pdftoppm', '-r', str(PPI), '-png', '-singlefile', '-f', str(page), str(pdf), str(temporary / f'{variant}-seite-{page}'))
            previews = [temporary / f'{variant}-seite-{i}.png' for i in (1, 2, 3)]
            thumbnail_width = 360
            thumbs = []
            for path in previews:
                with Image.open(path) as image:
                    thumbs.append(image.convert('RGB').resize((thumbnail_width, round(image.height * thumbnail_width / image.width)), Image.Resampling.LANCZOS))
            gap = 18
            sheet = Image.new('RGB', (thumbnail_width * 3 + gap * 4, thumbs[0].height + gap * 2), '#e9e9e7')
            for i, thumb in enumerate(thumbs):
                sheet.paste(thumb, (gap + i * (thumbnail_width + gap), gap))
            overview = temporary / f'{variant}-uebersicht.png'
            sheet.save(overview, optimize=True)
            manifest['variants'][variant] = {'source': str(source.relative_to(ROOT)), 'pdf': pdf.name,
                'sha256': digest(pdf), 'bytes': pdf.stat().st_size, 'validation': validation,
                'repeat_compile_identical': True,
                'page_previews': [p.name for p in previews], 'overview': overview.name}
        after = {str(path.relative_to(ROOT)): digest(path) for path in PROTECTED}
        if before != after:
            raise RuntimeError('Canonical handout changed during preview build; do not publish these previews.')
        manifest['canonical_files_after'] = after
        manifest['canonical_files_unchanged'] = True
        sources = [HERE / 'build.py', HERE / 'requirements.txt', HERE / 'A.typ', HERE / 'B.typ',
                   ROOT / 'content/typst/fachwerte.typ', ROOT / 'content/typst/vorlage.typ',
                   ROOT / 'content/typst/auswahl.json', ROOT / 'content/typst/handoutassets/bragg-summary.svg',
                   ROOT / 'content/data/versuchsdaten.json']
        sources.extend(sorted(HERE.glob('*-assets/*')))
        manifest['source_inputs'] = [{'path': str(p.relative_to(ROOT)), 'sha256': digest(p)} for p in sources if p.is_file()]
        (temporary / 'manifest.json').write_text(json.dumps(manifest, indent=2, ensure_ascii=False, sort_keys=True) + '\n')
        output.mkdir(parents=True, exist_ok=True)
        # Only named generated files are written; README and unrelated files are preserved.
        for path in temporary.iterdir():
            shutil.copyfile(path, output / path.name)
    print(f'Preview A and B: 3 A4 pages each, embedded fonts, checked values, deterministic PDFs.\n{output}')
    print('Canonical handout source and PDF unchanged; no production selection made.')


if __name__ == '__main__':
    main()
