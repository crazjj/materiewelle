#!/usr/bin/env python3
"""Build actual, transparent Typst math SVGs for the static Kobweb DOM UI."""
from pathlib import Path
import argparse
import json
import os
import subprocess

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]
CACHE = Path(os.environ.get("XDG_CACHE_HOME", Path.home() / ".cache"))
TYPST_PYTHON = os.environ.get("TYPST_PYTHON", str(Path(os.environ.get("TASK_TOOLS_ROOT", CACHE / "materiewelle-tools")) / "typst/bin/python"))
DEFAULT_OUTPUT = ROOT / "apps/web/site/build/generated/materiewelle-product/public/math"

# The pinned binding runs in its own small environment; the normal interpreter
# only needs Pillow when explicitly requesting a visual comparison board.
WORKER = r'''
from pathlib import Path
import hashlib,json,re,sys,xml.etree.ElementTree as ET
import typst
here,root,out,preview = Path(sys.argv[1]),Path(sys.argv[2]),Path(sys.argv[3]),sys.argv[4]
source=here/'inline.typ'; palette=root/'content/typst/vorlage.typ'; catalog_path=here/'formulas.json';catalog=json.loads(catalog_path.read_text())
font_directory=root/'apps/web/site/src/jsMain/resources/public/fonts'
assert len(catalog)==len({x['id'] for x in catalog})
assert all(re.fullmatch('[a-z][a-z0-9-]*',x['id']) and x['alt'].strip() and x['source'].strip() for x in catalog)
out.mkdir(parents=True,exist_ok=True)
if preview:Path(preview).mkdir(parents=True,exist_ok=True)
manifest={'schema_version':1,'compiler':f'Typst 0.14.0 (Python binding {typst.__version__})','font':'Latin Modern Math','font_size_pt':22,'transparent':True,'dimension_unit':'CSS px','annotations':[
 {'id':'energy','label':'Energie','light':'#875600','dark':'#efc16a'},
 {'id':'momentum','label':'Impuls','light':'#6d48a4','dark':'#c6a4f4'},
 {'id':'wave','label':'Wellenlänge','light':'#006c80','dark':'#59d8ee'},
 {'id':'geometry-focus','label':'Ausgewählter geometrischer Bezug','light':'#875600','dark':'#efc16a'}], 'formulas':[]}
all_ids=set()
for item in catalog:
 entry={'id':item['id'],'alt':item['alt'],'roles':item['roles'],'source_sha256':hashlib.sha256(source.read_bytes()+palette.read_bytes()+json.dumps(item,ensure_ascii=False,sort_keys=True).encode()).hexdigest()}
 dimensions=[]
 for theme in ['light','dark']:
  inputs={'formula':item['id'],'theme':theme}
  svg,warnings=typst.compile_with_warnings(str(source),root=str(root),font_paths=[str(font_directory)],format='svg',sys_inputs=inputs)
  assert not warnings,(item['id'],theme,warnings)
  assert isinstance(svg,bytes),'Each formula must compile to exactly one page'
  text=svg.decode();prefix=f"formula-{item['id']}-{theme}-"
  old_ids=re.findall(r'\bid="([^"]+)"',text)
  assert len(old_ids)==len(set(old_ids))
  for old in old_ids:
   new=prefix+old
   text=text.replace(f'id="{old}"',f'id="{new}"').replace(f'="#{old}"',f'="#{new}"').replace(f'url(#{old})',f'url(#{new})')
  from xml.sax.saxutils import escape
  title_id=prefix+'title'
  text=text.replace('<svg ','<svg role="img" aria-labelledby="'+title_id+'" ',1)
  at=text.index('>')+1
  text=text[:at]+'<title id="'+title_id+'">'+escape(item['alt'])+'</title>'+text[at:]
  document=ET.fromstring(text)
  ids={x.attrib['id'] for x in document.iter() if 'id' in x.attrib}
  assert not ids.intersection(all_ids),(item['id'],theme,'cross-resource ID collision')
  all_ids.update(ids)
  assert not any(x.tag.endswith(('script','foreignObject','image')) for x in document.iter())
  for x in document.iter():
   for name,value in x.attrib.items():
    if name.endswith('href'):assert value.startswith('#') and value[1:] in ids,(item['id'],name,value)
  _,_,width,height=map(float,document.attrib['viewBox'].split())
  width,height=width*4/3,height*4/3
  assert 0<width<=600 and 0<height<=130,(item['id'],width,height)
  dimensions.append((width,height))
  filename=f"{item['id']}-{theme}.svg"
  (out/filename).write_text(text)
  entry[theme]='math/'+filename
  entry[theme+'_sha256']=hashlib.sha256(text.encode()).hexdigest()
  if preview:
   png,warnings=typst.compile_with_warnings(str(source),root=str(root),font_paths=[str(font_directory)],format='png',ppi=144,sys_inputs=inputs)
   assert not warnings and isinstance(png,bytes)
   (Path(preview)/f"{item['id']}-{theme}.png").write_bytes(png)
 assert dimensions[0]==dimensions[1],(item['id'],'theme layout differs')
 entry['width'],entry['height']=[round(x,4) for x in dimensions[0]]
 manifest['formulas'].append(entry)
# A highlight changes only paint, never formula layout.
by_id={entry['id']:entry for entry in manifest['formulas']}
for item in catalog:
 if 'layout_base' in item:
  highlighted,base=by_id[item['id']],by_id[item['layout_base']]
  assert (highlighted['width'],highlighted['height'])==(base['width'],base['height']),(item['id'],'highlight layout differs')
(out/'manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n')
print(f'Built {len(catalog)*2} real Typst SVGs, no warnings, transparent, theme dimensions equal, unique IDs.')
'''

def preview_board(output):
    from PIL import Image, ImageDraw, ImageFont
    manifest = json.loads((output / 'manifest.json').read_text())
    font_path = subprocess.check_output(['fc-match', '-f', '%{file}', 'DejaVu Sans'], text=True).strip()
    label_font = ImageFont.truetype(font_path, 15)
    width,row_height = 1360,150
    board = Image.new('RGB',(width,len(manifest['formulas'])*row_height),'white')
    draw = ImageDraw.Draw(board)
    for index,item in enumerate(manifest['formulas']):
        y=index*row_height
        for theme,x,background,ink in [('light',0,'#ffffff','#182830'),('dark',680,'#171e24','#eaf1f5')]:
            draw.rectangle((x,y,x+679,y+row_height-1),fill=background)
            draw.text((x+14,y+10),item['id']+' · '+theme,font=label_font,fill=ink)
            im=Image.open(output/'previews'/f"{item['id']}-{theme}.png").convert('RGBA')
            im.thumbnail((640,100),Image.Resampling.LANCZOS)
            board.paste(im,(x+14,y+38),im)
    target=output/'previews/typst-formeln-hell-dunkel.png'
    board.save(target)
    print(target)

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--output-dir',type=Path,default=DEFAULT_OUTPUT)
    parser.add_argument('--previews',action='store_true')
    args=parser.parse_args()
    subprocess.run([TYPST_PYTHON,'-c',WORKER,str(HERE),str(ROOT),str(args.output_dir),str(args.output_dir/'previews') if args.previews else ''],check=True)
    if args.previews:preview_board(args.output_dir)

if __name__=='__main__':main()
