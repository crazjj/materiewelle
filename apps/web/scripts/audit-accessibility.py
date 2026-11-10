from pathlib import Path
import argparse,sys,json,hashlib
from playwright.sync_api import sync_playwright,expect
scripts=Path(__file__).resolve().parent;root=scripts.parents[2];sys.path.insert(0,str(scripts))
from browser_evidence import served
from browser_assets import settle_images
from browser_theme import set_theme
p=argparse.ArgumentParser(description='Chromium/axe and keyboard audit; manual SVG and screenreader checks remain separate.');p.add_argument('--dist',type=Path,required=True);p.add_argument('--output',type=Path,required=True);p.add_argument('--axe',type=Path,required=True);p.add_argument('--chromium',default='/usr/bin/chromium');p.add_argument('--dg-steps',action='store_true',help='Include all six focused DG lesson steps.');a=p.parse_args();a.output.mkdir(exist_ok=True,parents=True)
if any(a.output.iterdir()):p.error('Choose a fresh evidence directory.')
report={'method':'Chromium/axe-core 4.10.3, actual DOM plus tab walk; not a screenreader or human audit','axe_sha256':hashlib.sha256(a.axe.read_bytes()).hexdigest(),'test_harness_sha256':hashlib.sha256(Path(__file__).read_bytes()).hexdigest(),'artifact_manifest_sha256':hashlib.sha256((a.dist/'manifest.json').read_bytes()).hexdigest(),'views':[],'errors':[]}
with served(a.dist) as origin,sync_playwright() as pw:
 b=pw.chromium.launch(executable_path=a.chromium,args=['--no-sandbox'])
 page=b.new_page(viewport={'width':1280,'height':800},reduced_motion='reduce');page.on('pageerror',lambda e:report['errors'].append(str(e)))
 page.goto(origin,wait_until='networkidle')
 for theme in ['dark','light']:
  set_theme(page,theme)
  topics=['debroglie','davisson-germer','joensson','fazit']
  if a.dg_steps: topics += ['davisson-germer/'+key for key in ['versuch','befund','interferenz','geometrie','rechnen','einordnen']]
  for topic in topics:
   page.evaluate('s=>location.hash=s',topic);page.wait_for_timeout(200);settle_images(page)
   page.add_script_tag(path=str(a.axe))
   result=page.evaluate('''async () => {const r=await axe.run(document,{runOnly:{type:'tag',values:['wcag2a','wcag2aa','wcag21aa']}});return {violations:r.violations.map(v=>({id:v.id,impact:v.impact,description:v.description,nodes:v.nodes.map(n=>({target:n.target,summary:n.failureSummary}))})),incomplete:r.incomplete.map(v=>({id:v.id,nodes:v.nodes.map(n=>({target:n.target,summary:n.failureSummary}))})),passes:r.passes.map(p=>p.id)}}''')
   visited=[];seen=set();page.locator('body').click(position={'x':1,'y':1});page.keyboard.press('Control+Home')
   for i in range(250):
    page.keyboard.press('Tab')
    item=page.evaluate('''() => {const e=document.activeElement,s=getComputedStyle(e);return {identity:(e.id||e.tagName)+'|'+(e.getAttribute('href')||'')+'|'+(e.getAttribute('aria-label')||e.textContent||'').slice(0,90),tag:e.tagName,id:e.id,outline:s.outlineStyle+' '+s.outlineWidth,shadow:s.boxShadow,focusVisible:e.matches(':focus-visible'),rects:e.getClientRects().length}}''')
    if item['identity'] in seen:break
    seen.add(item['identity']);visited.append(item)
   report['views'].append({'theme':theme,'topic':topic,'axe':result,'tab_sequence':visited})
   print(topic,theme,'violations',len(result['violations']),'tabStops',len(visited),flush=True)
 b.close()
(a.output/'audit.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n')
print('Total violations',sum(len(v['axe']['violations']) for v in report['views']))

if report['errors'] or any(view['axe']['violations'] for view in report['views']): raise SystemExit(1)
