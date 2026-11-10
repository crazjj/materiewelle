#!/usr/bin/env python3
"""Current four-chapter browser contract with independent shared browser helpers.

Browser plugin unavailable: Python Playwright/system Chromium. Synthetic fixtures,
not human learning evidence; viewport/touch emulation is not an iPad/Safari test.
"""
from __future__ import annotations
import argparse
from datetime import datetime, timezone
import io
import json
import math
import os
from pathlib import Path
import re
import subprocess
import traceback
from urllib.parse import urljoin
import zipfile
from playwright.sync_api import expect, sync_playwright
from browser_assets import settle_images, reload_with_images
from browser_theme import set_theme
from browser_evidence import sha

PROJECT=Path(__file__).resolve().parents[1]
import browser_harness as browser_base
from browser_article_checks import ArticleChecks
CONTRACT='run-two-2026-11-04'
SCENARIOS=('articles','dg','measurement','state','tutor','handout','viewports','data_failure')
SECTIONS={'debroglie':'#debroglie-article','davisson-germer':'#dg-article','joensson':'#joensson-article','fazit':'#conclusion-article'}
STORAGE=browser_base.STORAGE
NOTE='QA-SYNTHETISCH: getrennte ältere Antwort.'
OWN_WORK_EXCLUDED='Deine Frage, die Aufgabe und das Fachwissen stehen in der Nachricht. Weitere Angaben aus den Arbeitsfeldern werden nur aufgenommen, wenn du sie hier auswählst.'
DEFAULT_EXPLANATION_QUESTION='Erkläre mir den Kern dieses Themas und wie die einzelnen Schritte zusammenhängen.'
LEGACY_STATIONS=(('S01_OBSERVATION','ausgangsfrage','article-einstieg'),('S02_CLASSICAL','klassische-bilder','article-einstieg'),('S03_LIGHT','lichtquanten','article-geschichte'),('S04_HYPOTHESIS','de-broglie','article-hypothese'),('S05_DERIVATION','spannung-zur-vorhersage','article-herleitung'),('S06_CALCULATION','150-volt','article-rechnung'),('S07_RECEPTION','einstein-1924','article-rezeption'),('S08_DG_SETUP','dg-aufbau','dg-aufbau'),('S09_CRYSTAL','nickel-kristall','dg-beobachtung'),('S10_GEOMETRY','beugungsgeometrie','dg-bragg'),('S11_DG_EVALUATION','dg-auswertung','dg-vergleich'),('S12_JONSSON_SETUP','joensson-aufbau','joensson-aufbau'),('S13_INTERFERENCE','lichtvergleich','joensson-lehrmodell'),('S14_MEASUREMENT','bildmessung','joensson-messung'),('S15_LIMITS','modellgrenzen','joensson-grenzen'),('S16_CONCLUSION','fazit','conclusion-title'))

def nr_pm(voltage):return 6.62607015e-34/math.sqrt(2*9.1093837139e-31*1.602176634e-19*voltage)*1e12

def handoff(page):
    # Read the actual human-readable message; do not inspect a hidden JSON payload.
    text=page.locator('#compact-tutor-preview').text_content()
    titles={'De Broglie':'debroglie','Davisson–Germer':'davisson-germer','Jönsson':'joensson','Fazit':'fazit'}
    section=titles[text.split('Kapitel: ',1)[1].splitlines()[0]]
    question=text.split('## Meine Frage\n',1)[1].split('\n\n## Gewünschte Hilfe: ',1)[0]
    own=text.split('## Meine freigegebenen Angaben\n',1)[1].split('\n\n## Fachlicher Kontext und Hinweise für die Antwort',1)[0]
    return text,{'section':section,'question':question,
                 'ownWork':None if own.startswith('Keine zusätzlichen Angaben aus den Arbeitsfeldern beigefügt.') else own}

class Checks(browser_base.Checks, ArticleChecks):
    def page(self, **options):
        init_script=options.pop('init_script',None)
        context=self.browser.new_context(**(dict(viewport={'width':1440,'height':900},locale='de-DE',reduced_motion='reduce',accept_downloads=True)|options))
        page=context.new_page();self.pages.append(page);self.current=page;self.observe(page)
        if init_script:page.add_init_script(init_script)
        response=page.goto(self.url,wait_until='networkidle')
        expect(page).to_have_title(re.compile('Materiewelle'));expect(page.locator('main')).to_be_visible()
        self.check('Entry HTTP response succeeds',response is not None and response.status==200)
        self.check('No framework overlay',page.locator('#webpack-dev-server-client-overlay,vite-error-overlay,nextjs-portal').count()==0)
        return page

    def open_tutor(self,page):
        if page.locator('#compact-tutor-open').get_attribute('aria-pressed')!='true':page.locator('#compact-tutor-open').click()
        expect(page.locator('#compact-tutor-copy')).to_be_visible()

    def route(self,page,fragment):
        settle_images(page)
        page.evaluate('f=>location.hash=f',fragment)
        page.wait_for_function("f=>location.hash==='#'+f",arg=fragment)
        root=SECTIONS.get(fragment.split('/')[0])
        if root:expect(page.locator(root)).to_be_visible()
        page.wait_for_load_state('networkidle')

    def section(self,page,key):
        self.route(page,key)
        expect(page.locator(SECTIONS[key])).to_be_visible()
        return page.locator(SECTIONS[key])

    def viewport_target(self,page,selector):
        expect(page.locator(selector)).to_be_visible()
        page.wait_for_function("s=>{const e=document.querySelector(s),h=document.querySelector('header');if(!e)return false;const r=e.getBoundingClientRect(),hr=h?.getBoundingClientRect();return r.top>=Math.max(0,hr?.bottom??0)-2 && r.top<innerHeight*.75}",arg=selector)
        self.report.setdefault('target_bounds',[]).append(page.locator(selector).evaluate('(e)=>{const r=e.getBoundingClientRect(),h=document.querySelector("header").getBoundingClientRect();return{selector:"#"+e.id,targetTop:r.top,targetBottom:r.bottom,headerBottom:h.bottom,url:location.href,viewport:{width:innerWidth,height:innerHeight}}}'))
        self.check('Reading target is actually below sticky navigation: '+selector,True)

    def touch_targets(self,page,label):
        for selector in ('#course-model-g','#course-model-b','#image-zoom','#profile-sample-x'):
            expect(page.locator(selector)).to_be_visible()
            bounds=page.locator(selector).evaluate('e=>{const r=e.getBoundingClientRect();return{width:r.width,height:r.height}}')
            self.report.setdefault('touch_target_bounds',[]).append({'selector':selector,'context':label,**bounds})
            self.check('D05 main input target is at least 44px '+selector+' '+label,bounds['height']>=44 and bounds['width']>=44)
        self.open_disclosures(page, '#show-markers')
        checkbox=page.locator('#show-markers')
        expect(checkbox).to_be_visible()
        bounds=checkbox.evaluate('e=>{const r=e.closest("label").getBoundingClientRect();return{width:r.width,height:r.height}}')
        self.report.setdefault('touch_target_bounds',[]).append({'selector':'#show-markers closest label','context':label,**bounds})
        self.check('D05 checkbox has a full clickable label target '+label,bounds['height']>=44 and bounds['width']>=44)

    def open_model(self,page):
        if not page.locator('#article-voltage').is_visible():page.locator('#article-model-disclosure > summary').click()

    def open_disclosures(self, page, selector):
        # Use the visible summary controls; do not force hidden DOM state open.
        for disclosure in page.locator(selector).locator('xpath=ancestor::details[not(@open)]').all():
            disclosure.locator(':scope > summary').click()
        expect(page.locator(selector)).to_be_visible()

    def open_marker_coordinates(self, page):
        # Precision editing remains an explicit learner action, not a hidden DOM override.
        disclosure = page.locator('#measurement-coordinate-tools')
        if disclosure.get_attribute('open') is None:
            disclosure.locator('summary').first.click()
        expect(page.locator('#marker-new-x')).to_be_visible()

    def open_storage(self,page):
        if not page.locator('#session-work-copy').is_visible():page.get_by_text('Arbeit auf diesem Gerät',exact=True).click()

    def seed(self,page):
        self.section(page,'debroglie')
        # Visiting alone need not create a personal-work record. Persist an actual
        # synthetic answer through the same input path a learner uses first.
        page.locator('#article-notes').fill('QA-SYNTHETISCH: Fixture initialisieren.')
        page.wait_for_function('k=>localStorage.getItem(k)!==null',arg=STORAGE)
        fixture=self.snapshot(page)
        for key,_,_ in LEGACY_STATIONS:
            fixture['work'][key]={'note':NOTE+' '+key}
        fixture['measurement']['markers']=[{'xPx':551},{'xPx':578}]
        fixture['measurement']['justification']='QA-SYNTHETISCH: eigene Auswahl, kein historisches Maximum nachgewiesen.'
        fixture['deBroglie'].update(raw='375',lastValidVoltage=375.0)
        page.evaluate('([k,v])=>localStorage.setItem(k,v)',[STORAGE,json.dumps(fixture)])
        reload_with_images(page)
        return fixture

    def articles(self):
        page=self.page()
        expect(page.locator('#overview-title')).to_be_visible()
        self.check('Entry explains four chapters and the own task before opening an article',page.locator('#overview-chapters a').count()==4)
        self.shot(page,'overview-dark.png')
        page.locator('#overview-start').click()
        expect(page.locator('#debroglie-article')).to_be_visible()
        self.check('Exactly four main destinations',page.locator('header nav').first.locator('button,a').count()==4)
        for key in SECTIONS:
            article=self.section(page,key)
            expect(article.locator('h1')).to_have_count(1)
            self.check('No old station chrome in '+key,article.locator('.course-topline,.station-frame,.argument-rail').count()==0)
            self.check('Article has visible authored content '+key,bool(article.locator('p').first.inner_text().strip()))
            self.shot(page,key+'-dark.png')
        self.route(page,'debroglie')
        self.article_checks(page)
        # Real typography copied from a formula must work as well as ASCII ^.
        unicode_answer='1,0 × 10⁻¹⁰ m'
        page.locator('#course-task-answer').fill(unicode_answer)
        expect(page.locator('#course-input-preview')).to_have_text('Deine Eingabe in Pikometern: 100 pm · gerundete Vorschau, noch keine Bewertung.')
        page.locator('#course-numeric-check').click()
        expect(page.locator('#course-numeric-feedback')).to_have_attribute('data-answer-status','CORRECT')
        expect(page.locator('#course-numeric-feedback')).to_have_text(re.compile(r'^Dein Zahlenwert passt zur Modellrechnung\.'))
        self.check('150 V accepts a Unicode superscript exponent without rewriting the own answer',
                   self.snapshot(page)['work']['S06_CALCULATION']['numericAnswer']==unicode_answer)
        reload_with_images(page)
        expect(page.locator('#course-task-answer')).to_have_value(unicode_answer)
        self.check('Unicode exponent answer survives a real browser reload',True)
        nav=page.locator('header nav').first.locator('button')
        nav.nth(1).focus();page.keyboard.press('Enter');expect(page.locator('#dg-article')).to_be_visible()
        self.check('Primary destination responds to keyboard activation',True)
        for fragment,target in [('lernen/dg-aufbau','#dg-aufbau'),('lernen/joensson-aufbau','#joensson-aufbau'),('lernen/fazit','#conclusion-title'),('joensson/messung','#marker-workshop')]:
            self.route(page,fragment)
            self.viewport_target(page,target)
        for _,key,target in LEGACY_STATIONS:
            self.route(page,'lernen/'+key);expect(page.locator('#'+target)).to_be_focused()
            self.viewport_target(page,'#'+target)
        self.section(page,'debroglie');self.section(page,'davisson-germer')
        page.go_back();expect(page.locator('#debroglie-article')).to_be_visible()
        page.go_forward();expect(page.locator('#dg-article')).to_be_visible()
        self.check('Browser back and forward restore the selected article',True)
        page.evaluate("location.hash='not-an-authored-route'")
        expect(page.locator('main')).to_contain_text('Dieser Abschnitt wurde nicht gefunden')
        self.check('Unknown route is explicitly explained without data loss',True)
        css=page.request.get(urljoin(self.url,'app.css')).text()
        self.check('Retired motion-joke-arrive rule is absent from current CSS','motion-joke-arrive' not in css)
        self.check('Retired joke component is not mounted',page.locator('.motion-joke-arrive').count()==0)

    def dg(self):
        page=self.page();article=self.section(page,'davisson-germer')
        expect(page.locator('[data-dg-lesson-step]')).to_have_count(1)
        self.check('DG starts with one readable step',True)
        self.shot(page,'dg-guided-setup.png')
        self.route(page,'davisson-germer/geometrie')
        self.shot(page,'dg-guided-geometry.png')
        page.locator('#dg-lesson-all').click()
        expect(page.locator('[data-dg-lesson-step]')).to_have_count(6)
        page.locator('#dg-model-variation > summary').click()
        expect(article).to_contain_text(re.compile('Bragg'))
        expect(article).to_contain_text(re.compile('Sekundär|sekundär'))
        geo=.352*math.sqrt(3/8)*math.sin(math.radians(50))
        predicted=nr_pm(54)/1000
        for sel,want in [('#dg-article-geometric',geo),('#dg-article-predicted',predicted)]:
            expect(page.locator(sel)).to_be_visible()
            actual=float(page.locator(sel).get_attribute('data-value-nm'))
            self.check('DG independent numeric control '+sel,abs(actual-want)<1e-9)
        signed=(geo-predicted)/predicted*100
        self.check('DG signed difference uses theory denominator',abs(float(page.locator('#dg-article-difference').get_attribute('data-value-percent'))-signed)<1e-8)
        before=page.locator('#dg-article-geometric').get_attribute('data-value-nm')
        page.locator('#dg-article-angle').fill('40')
        expect(page.locator('#dg-article-angle')).to_have_value('40')
        page.wait_for_function("want=>Math.abs(Number(document.querySelector('#dg-article-geometric').dataset.valueNm)-want)<1e-9",arg=.352*math.sqrt(3/8)*math.sin(math.radians(40)))
        self.check('DG angle changes a computed value',page.locator('#dg-article-geometric').get_attribute('data-value-nm')!=before)
        valid=page.locator('#dg-article-geometric').get_attribute('data-value-nm')
        page.locator('#dg-article-angle').fill('nicht numerisch')
        expect(page.locator('#dg-article-angle')).to_have_attribute('aria-invalid','true')
        self.check('Invalid DG edit retains last valid result',page.locator('#dg-article-geometric').get_attribute('data-value-nm')==valid)
        page.locator('#dg-article-reset').click()
        page.locator('#dg-article-voltage').fill('20')
        expect(page.locator('#dg-article-model-limit')).to_be_visible()
        page.locator('#dg-article-voltage').fill('1001')
        expect(page.locator('#dg-article-voltage')).to_have_attribute('aria-invalid','true')
        expect(page.locator('#dg-article-model-limit')).to_be_visible()
        self.check('DG warns when no real first-order surface direction exists and retains last valid model',abs(float(page.locator('#dg-article-predicted').get_attribute('data-value-nm'))-nr_pm(20)/1000)<1e-9)
        page.locator('#dg-article-reset').click()
        expect(page.locator('#dg-article-model-limit')).to_have_count(0)
        page.locator('#dg-article-save').click()
        expect(page.locator('#dg-article-saved')).to_be_attached()
        page.locator('#dg-article-angle').fill('40')
        self.section(page,'debroglie');self.section(page,'davisson-germer')
        expect(page.locator('#dg-article-angle')).to_have_value('40')
        self.check('DG model and saved comparison survive a real article remount',self.snapshot(page)['dg']['comparisonAngle']==50)
        page.locator('#dg-model-variation > summary').click()
        page.locator('#dg-article-saved > summary').click();page.locator('#dg-article-clear').click()
        expect(page.locator('#dg-article-saved')).to_have_count(0)
        expect(page.locator('#dg-bragg .article-formula')).to_have_count(2)
        self.check('Whole chapter shows both Bragg paths without advancing guided geometry', self.snapshot(page)['dg']['geometryStep']==0)
        page.locator('#dg-lesson-all').click()
        self.route(page,'davisson-germer/interferenz')
        for step in (1,2):
            page.locator('#dg-lesson-next').click()
            self.check('Bragg construction retains cumulative geometry step '+str(step),self.snapshot(page)['dg']['geometryStep']==step)
        page.locator('#dg-bragg').scroll_into_view_if_needed()
        self.shot(page,'dg-bragg-two-paths.png')
        page.locator('#dg-lesson-next').click()
        page.locator('#dg-oberflaeche').scroll_into_view_if_needed()
        self.shot(page,'dg-geometry-and-data.png')

    def measurement(self):
        for dpr in (1,2):
            page=self.page(has_touch=True,device_scale_factor=dpr)
            if dpr == 1:
                self.route(page,'joensson/messung')
                self.shot(page,'measurement-entry.png')
            self.seed(page);self.route(page,'joensson/messung')
            original=self.snapshot(page)['measurement']
            expect(page.locator('#image-zoom')).to_be_visible()
            for zoom in (1,2):
                page.locator('#image-zoom').fill(str(zoom));expect(page.locator('#image-zoom')).to_have_value(str(zoom))
                self.check(f'Zoom {zoom} DPR{dpr} preserves native measurement',self.snapshot(page)['measurement']==original)
            page.locator('[data-marker-index="0"]').tap();page.locator('#marker-plus').tap()
            expect(page.locator('[data-own-marker="0"]')).to_have_attribute('data-native-x','552')
            page.locator('#marker-minus').click();expect(page.locator('[data-own-marker="0"]')).to_have_attribute('data-native-x','551')
            self.check(f'Touch then mouse on same device DPR{dpr}',self.snapshot(page)['measurement']==original)
            page.locator('#jonsson-interactive-image').focus();page.keyboard.press('ArrowRight')
            expect(page.locator('[data-own-marker="0"]')).to_have_attribute('data-native-x','552')
            page.keyboard.press('ArrowLeft');expect(page.locator('[data-own-marker="0"]')).to_have_attribute('data-native-x','551')
            self.check(f'Keyboard image movement remains one native pixel DPR{dpr}',self.snapshot(page)['measurement']==original)
            page.locator('#image-zoom').fill('1')
            page.locator('#jonsson-native-image').scroll_into_view_if_needed()
            def point(x,y):return page.locator('#jonsson-native-image').evaluate('(e,p)=>{const q=new DOMPoint(...p).matrixTransform(e.getScreenCTM());return{x:q.x,y:q.y}}',[x,y])
            a,b=point(551,160),point(555,160)
            page.mouse.move(a['x'],a['y']);page.mouse.down();page.mouse.move(b['x'],b['y'],steps=8);page.mouse.up()
            expect(page.locator('[data-own-marker="0"]')).to_have_attribute('data-native-x','555')
            self.check(f'Actual pointer drag maps to native pixels DPR{dpr}',self.snapshot(page)['measurement']['markers'][0]['xPx']==555)
            self.open_marker_coordinates(page)
            page.locator('#marker-x').fill('551');expect(page.locator('[data-own-marker="0"]')).to_have_attribute('data-native-x','551')
            page.locator('#jonsson-native-image').scroll_into_view_if_needed()
            touch=page.context.new_cdp_session(page)
            start,finish=point(551,160),point(554,160)
            touch.send('Input.dispatchTouchEvent',{'type':'touchStart','touchPoints':[{'x':start['x'],'y':start['y'],'id':1}]})
            for step in range(1,5):
                touch.send('Input.dispatchTouchEvent',{'type':'touchMove','touchPoints':[{'x':start['x']+(finish['x']-start['x'])*step/4,'y':start['y'],'id':1}]})
            touch.send('Input.dispatchTouchEvent',{'type':'touchEnd','touchPoints':[]});touch.detach()
            expect(page.locator('[data-own-marker="0"]')).to_have_attribute('data-native-x','554')
            self.check(f'Actual touch pointer drag maps to native pixels DPR{dpr}',self.snapshot(page)['measurement']['markers'][0]['xPx']==554)
            self.open_marker_coordinates(page)
            page.locator('#marker-x').fill('551');expect(page.locator('[data-own-marker="0"]')).to_have_attribute('data-native-x','551')
            page.locator('#image-zoom').fill('2')
            self.open_marker_coordinates(page)
            page.locator('#marker-new-x').fill('600')
            page.locator('#jonsson-image-window').evaluate('e=>{e.scrollLeft=87;e.dispatchEvent(new Event("scroll"))}')
            pan=page.locator('#jonsson-image-window').evaluate('e=>e.scrollLeft')
            self.route(page,'debroglie');self.route(page,'joensson/messung')
            expect(page.locator('#image-zoom')).to_have_value('2')
            expect(page.locator('#marker-new-x')).to_have_value('600')
            page.wait_for_function('want=>Math.abs(document.querySelector("#jonsson-image-window").scrollLeft-want)<1',arg=pan)
            self.check(f'Unmount/remount preserves zoom and native work DPR{dpr}',self.snapshot(page)['measurement']==original)
            self.check(f'Unmount/remount preserves independent mark draft and image pan DPR{dpr}',True)
            page.locator('#jonsson-justification').fill('QA: Ich möchte diese gewählten Positionen vergleichen; ihre Maximalzuordnung wird hier nicht automatisch bestätigt.')
            page.locator('#jonsson-seal-selection').click()
            expect(page.locator('#jonsson-show-reference')).to_be_enabled()
            sealed=self.snapshot(page)['measurement']
            page.locator('#jonsson-show-reference').click()
            expect(page.locator('#jonsson-reference-status')).to_contain_text(re.compile('betrachtet|eingeblendet|Referenz'))
            viewed=self.snapshot(page)['measurement']
            self.check('Viewing reference never imports its marks',viewed['markers']==sealed['markers'] and viewed['initialSelection']==sealed['initialSelection'])
            self.open_disclosures(page, '#jonsson-compare-save')
            page.locator('#jonsson-compare-save').click()
            compared=self.snapshot(page)['measurement']['comparison']
            self.check('Saved comparison preserves actual native selection and provenance',compared['markers']==sealed['markers'] and compared['referenceViewedAtCapture'] is True)
            page.locator('#measurement-profile').select_option('oben')
            self.check('Band changes retain native own positions',self.snapshot(page)['measurement']['markers']==viewed['markers'])
            expect(page.locator('#measurement-justification-status')).to_be_visible()
            expect(page.locator('#jonsson-compare-save')).to_be_disabled()
            self.check('Changed band never silently revalidates the prior justification',self.snapshot(page)['measurement']['comparison']==compared)
            page.locator('#profile-sample-x').fill('600')
            expect(page.locator('#profile-sample')).to_have_attribute('data-native-x','600')
            from PIL import Image
            image_bytes=page.request.get(urljoin(self.url,'jonsson-page6-xref33.png')).body()
            raster=Image.open(io.BytesIO(image_bytes)).convert('L')
            independent_gray=sum(raster.getpixel((600,y)) for y in range(20,45))/25
            self.check('Displayed profile is independently computed from served raster and selected band',abs(float(page.locator('#profile-sample').get_attribute('data-gray-value'))-independent_gray)<1e-9)

            self.open_marker_coordinates(page)
            page.locator('#marker-new-x').fill('600');page.locator('#marker-add').click()
            expect(page.locator('[data-own-marker]')).to_have_count(3)
            self.check('Additional mark is native integer',self.snapshot(page)['measurement']['markers'][-1]['xPx']==600)
            page.locator('#jonsson-native-image').scroll_into_view_if_needed()
            self.shot(page,f'measurement-native-dpr{dpr}.png')
            self.open_disclosures(page, '#marker-reset')
            page.locator('#marker-reset').click()
            expect(page.locator('[data-own-marker]')).to_have_count(0)
            self.check('Reset clears current marks without fabricated result',self.snapshot(page)['measurement']['markers']==[])
            self.open_marker_coordinates(page)
            page.locator('#marker-new-x').fill('551');page.locator('#marker-add').click()
            expect(page.locator('[data-own-marker]')).to_have_count(1)
            self.check('Single native mark does not fabricate multiple markers',len(self.snapshot(page)['measurement']['markers'])==1)
            expect(page.locator('#jonsson-mean-equation')).to_contain_text('Setze mindestens zwei Marken')

    def state(self):
        page=self.page(has_touch=True);fixture=self.seed(page)
        for key in SECTIONS:self.section(page,key)
        restored=self.snapshot(page)
        self.check('All sixteen legacy answers remain individually intact',all(restored['work'][k]['note']==fixture['work'][k]['note'] for k,_,_ in LEGACY_STATIONS))
        self.section(page,'fazit');expect(page.locator('#conclusion-draft')).to_have_value(NOTE+' S16_CONCLUSION')
        page.locator('#conclusion-draft').fill('QA: echter lokaler Entwurf, kein automatischer Schluss.')
        reload_with_images(page);expect(page.locator('#conclusion-draft')).to_have_value('QA: echter lokaler Entwurf, kein automatischer Schluss.')
        self.route(page,'joensson/messung');reload_with_images(page)
        page.evaluate("location.hash='lernen'");expect(page.locator('#section-resume')).to_be_visible()
        page.locator('#section-resume').click();expect(page.locator('#joensson-messung')).to_be_focused()
        self.check('Only an actually saved reading position is offered and restored',True)
        self.section(page,'debroglie')
        self.check('Personal JSON file controls are absent',page.locator('#course-export,#session-import-file,#session-draft-export,#session-stored-export').count()==0)
        original=page.evaluate('k=>localStorage.getItem(k)',STORAGE)
        other=page.context.new_page();self.pages.append(other);other.goto(self.url,wait_until='networkidle')
        changed=json.loads(original);changed['work']['S01_OBSERVATION']['note']='QA: second tab newer record'
        newer=json.dumps(changed);other.evaluate('([k,v])=>localStorage.setItem(k,v)',[STORAGE,newer])
        own='QA: aktuelle Arbeit im Konflikttab bleibt kopierbar.'
        page.locator('#article-notes').fill(own)
        expect(page.locator('#course-storage-status')).to_contain_text(re.compile('anderer Tab|inzwischen|geändert'))
        self.check('Conflicting edit cannot overwrite newer other-tab storage',page.evaluate('k=>localStorage.getItem(k)',STORAGE)==newer)
        expect(page.locator('#session-work-copy')).to_be_visible()
        expect(page.locator('#session-work-preview')).to_contain_text(own)
        self.check('Current conflicting work remains readable without a JSON export',True)
        bad=self.page(init_script="""const get=Storage.prototype.getItem,set=Storage.prototype.setItem;
          Storage.prototype.getItem=function(k){if(k==='materiewelle.course.session.v1')throw new DOMException('QA denied','SecurityError');return get.call(this,k)};
          Storage.prototype.setItem=function(k,v){if(k==='materiewelle.course.session.v1')throw new DOMException('QA denied','SecurityError');return set.call(this,k,v)};""")
        expect(bad.locator('main')).to_contain_text(re.compile('Speicher|Speichern'))
        self.section(bad,'debroglie')
        bad.locator('#course-task-answer').fill('100 pm');bad.locator('#course-numeric-check').click()
        expect(bad.locator('#course-numeric-feedback')).to_have_attribute('data-answer-status','CORRECT')
        self.check('Storage denial leaves current physics usable',True)

    def local_tutor_help(self):
        page=self.page(viewport={'width':1024,'height':768},has_touch=True)
        self.seed(page)
        # The legacy fixture omits defaults. A real save serializes them explicitly.
        defaults={'note':'','numericAnswer':'','checked':False,'transferAnswer':'','transferChecked':False,
                  'explanationStep':0,'hintsOpened':0,'markedWorked':False,'learningPhase':'PREDICT',
                  'prediction':'','solutionViewed':False,'transferSolutionViewed':False,'explanationFurthestStep':0}
        def normalized(work):return {station:defaults|fields for station,fields in work.items()}
        entries=(('debroglie','#tutor-help-calculation','Zurück zur Rechnung','De Broglie · Deine Rechnung bei 150 Volt'),
                 ('davisson-germer','#tutor-help-dg-comparison','Zurück zum Vergleich','Davisson–Germer · Vorhersage und Befund vergleichen'),
                 ('joensson','#tutor-help-image-measurement','Zurück zur Messung','Jönsson · Deine Bildmessung'),
                 ('fazit','#tutor-help-conclusion','Zurück zum Fazit','Fazit · Deine Argumentation'))
        for index,(key,selector,back_label,heading) in enumerate(entries):
            self.section(page,key)
            if key=='davisson-germer': self.route(page,'davisson-germer/rechnen')
            before=page.evaluate('k=>JSON.parse(localStorage.getItem(k))',STORAGE)
            trigger=page.locator(selector)
            trigger.evaluate('e=>e.scrollIntoView({block:"center"})')
            origin_scroll=page.evaluate('scrollY')
            original_hash=page.evaluate('location.hash')
            entry_label=trigger.inner_text()
            if index==1:
                trigger.focus();page.keyboard.press('Enter')
            elif index==2:trigger.tap()
            else:trigger.click()
            expect(page.locator('#compact-tutor-panel')).to_have_count(1)
            expect(page.locator('#compact-tutor-question')).to_be_focused()
            expect(page.locator('#compact-tutor-close')).to_have_text(back_label)
            expect(page.locator('#compact-tutor-close')).to_have_accessible_name(back_label)
            self.check('Local help is mounted in the current article '+key,page.locator(SECTIONS[key]+' #compact-tutor-panel').count()==1)
            expect(page.locator('#compact-tutor-copy')).to_have_text('Nachricht kopieren')
            return_action={
                'debroglie':'Prüfe danach deinen Rechenweg und ergänze ihn hier in deinen eigenen Worten.',
                'davisson-germer':'Prüfe danach beide Rechenwege und ergänze hier deine Einordnung des Vergleichs.',
                'joensson':'Prüfe danach hier deine Marken am Bild und ergänze deine Auswahlbegründung.',
                'fazit':'Prüfe danach die Belege und Grenzen deiner Aussage und überarbeite hier dein eigenes Fazit.',
            }
            self.check('Local help explains the copy route and field roles '+key,
                entry_label=='Hilfe mit deinem KI-Chat'
                and page.locator('#compact-tutor-title').inner_text()=='Hilfe mit deinem KI-Chat'
                and page.locator('#compact-tutor-scope').inner_text()==heading
                and 'Kopiere die ganze Nachricht, einschließlich Aufgabe und Fachwissen.' in page.locator('#compact-tutor-panel').inner_text()
                and 'Füge die Nachricht ein und sende sie ab.' in page.locator('#compact-tutor-panel').inner_text()
                and return_action[key] in page.locator('#compact-tutor-panel').inner_text()
                and 'Die Website sendet nichts automatisch und erhält keine Chatantwort.' in page.locator('#compact-tutor-how').inner_text())
            expect(page.locator('#compact-tutor-question')).to_have_accessible_name('Deine Frage (optional)')
            expect(page.locator('#compact-tutor-explanation')).to_have_attribute('aria-pressed','true')
            expect(page.locator('#compact-tutor-own-summary')).to_have_text(OWN_WORK_EXCLUDED)
            expect(page.locator('#compact-tutor-copy')).to_have_text('Nachricht kopieren')
            self.check('Empty local question receives the selected explanation request '+key,
                handoff(page)[1]['question']==DEFAULT_EXPLANATION_QUESTION and handoff(page)[1]['ownWork'] is None)
            if key=='joensson':
                self.viewport_target(page,'#compact-tutor-panel')
                self.check('Wide measurement help is outside the narrow result column',page.locator('#jonsson-explanation #compact-tutor-panel').count()==0)
            page.wait_for_function("""() => {
                const r=document.querySelector('#compact-tutor-question').getBoundingClientRect();
                const h=document.querySelector('header');
                const sticky=h&&['sticky','fixed'].includes(getComputedStyle(h).position);
                return r.top>=Math.max(0,sticky?h.getBoundingClientRect().bottom:0)-2 && r.bottom<=innerHeight;
            }""")
            self.check('Opening local help brings the focused question fully into view '+key,True)
            self.check('Local help does not replace chapter route '+key,page.evaluate('location.hash')==original_hash)
            expect(page.locator('#compact-tutor-own')).to_have_attribute('type','checkbox')
            expect(page.locator('#compact-tutor-own')).to_have_accessible_name('Meine Angaben zu dieser Aufgabe in die Nachricht aufnehmen')
            expect(page.locator('#compact-tutor-own')).not_to_be_checked()
            draft='QA: Meine eigene Frage zu '+key+'.'
            page.locator('#compact-tutor-question').scroll_into_view_if_needed()
            scroll_before=page.evaluate('scrollY')
            page.locator('#compact-tutor-question').fill(draft)
            self.check('Editing a question never triggers another help jump '+key,abs(page.evaluate('scrollY')-scroll_before)<2)
            examples=page.locator('#compact-tutor-examples button')
            expect(examples).to_have_count(2)
            expect(page.locator('#compact-tutor-examples')).not_to_be_visible()
            page.get_by_text('Eine Beispielfrage ergänzen',exact=True).click()
            expect(page.locator('#compact-tutor-examples')).to_be_visible()
            self.check('Local help and both examples meet touch target size '+key,all(e.evaluate('e=>{const r=e.getBoundingClientRect();return r.width>=44&&r.height>=44}') for e in [trigger,*examples.all()]))
            first=examples.first.inner_text()
            examples.first.click()
            expect(page.locator('#compact-tutor-question')).to_be_focused()
            expect(page.locator('#compact-tutor-question')).to_have_value(draft+'\n\n'+first)
            examples.first.click()
            expect(page.locator('#compact-tutor-question')).to_have_value(draft+'\n\n'+first)
            self.check('Sample question retains own text and is not duplicated '+key,True)
            page.get_by_text('Vollständige Nachricht ansehen',exact=True).click()
            _,data=handoff(page)
            self.check('Sample is an editable question with no own answers shared '+key,data['section']==key and data['ownWork'] is None and data['question'].startswith(draft))
            after=page.evaluate('k=>JSON.parse(localStorage.getItem(k))',STORAGE)
            self.report.setdefault('local_tutor_work',[]).append({'section':key,'before_work':before['work'],'after_work':after['work'],'before_markers':before['measurement']['markers'],'after_markers':after['measurement']['markers']})
            self.check('Sample questions never become scientific work '+key,normalized(before['work'])==normalized(after['work']) and before['measurement']['markers']==after['measurement']['markers'])
            page.get_by_text('Vollständige Nachricht ansehen',exact=True).click()
            if key=='joensson':
                self.check('Method help does not change measurement or reference contacts',before['measurement']==after['measurement'])
                text,_=handoff(page)
                self.check('Method help contains no documented reference coordinates or spacing','551,564,578' not in text and '96/7' not in text and '13,7 native' not in text and '13,7 Originalpixel' not in text)
                page.locator('#compact-tutor-panel').evaluate('e=>e.scrollIntoView({block:"center"})')
                self.shot(page,'tutor-local-question-ipad.png')
            if key=='debroglie':
                page.locator('#compact-tutor-question').fill('x'*2000)
                for button in examples.all():expect(button).to_be_disabled()
                expect(page.locator('#compact-tutor-panel')).to_contain_text('Kürze deinen Text, um noch eine Beispielfrage zu ergänzen.')
                page.locator('#compact-tutor-question').fill(draft+'\n\n'+first)
                self.check('Examples refuse overflow without truncating an existing draft',True)
            page.locator('#compact-tutor-own').click()
            expect(page.locator('#compact-tutor-own')).to_be_checked()
            if key=='debroglie':
                expect(page.locator('#compact-tutor-preview')).to_contain_text(NOTE+' S06_CALCULATION')
                own=handoff(page)[1]['ownWork']
                self.check('Local calculation shares only the fixed 150-V task',
                    '### Meine feste Aufgabe bei 150 V' in own and NOTE+' S06_CALCULATION' in own
                    and '### Aktuelle Einstellung des Spannungsmodells' not in own and NOTE+' S05_DERIVATION' not in own)
            if key=='davisson-germer':
                expect(page.locator('#compact-tutor-preview')).to_contain_text(NOTE+' S11_DG_EVALUATION')
                own=handoff(page)[1]['ownWork']
                self.check('Local DG comparison shares source-case work without free model settings',
                    'Mein Vergleich des dokumentierten Davisson–Germer-Zahlenfalls' in own and NOTE+' S11_DG_EVALUATION' in own
                    and '### Aktuelle Einstellung des Davisson–Germer-Modells' not in own and NOTE+' S10_GEOMETRY' not in own)
            page.locator('#compact-tutor-question').focus();page.keyboard.press('Escape')
            expect(page.locator('#compact-tutor-panel')).to_have_count(0)
            expect(trigger).to_be_focused()
            self.check('Closing local help restores original viewport '+key,abs(page.evaluate('scrollY')-origin_scroll)<2)
            self.viewport_target(page,selector)
            self.check('Escape returns to the local help trigger '+key,True)
            trigger.click();expect(page.locator('#compact-tutor-question')).to_have_value(draft+'\n\n'+first)
            expect(page.locator('#compact-tutor-own')).not_to_be_checked()
            page.locator('#compact-tutor-close').click();expect(trigger).to_be_focused()
            self.check('Reopening preserves question but requires renewed data selection '+key,True)
        page.locator('#tutor-help-conclusion').click();expect(page.locator('#compact-tutor-question')).to_be_focused()
        self.section(page,'debroglie');expect(page.locator('#debroglie-article h1')).to_be_focused()
        self.viewport_target(page,'#article-title')
        self.check('Chapter navigation is never hijacked by an already open help panel',True)
        self.open_tutor(page)
        page.locator('#compact-tutor-close').click();expect(page.locator('#compact-tutor-open')).to_be_focused()
        page.route('**/tutor/START-debroglie.md',lambda route:route.fulfill(status=200,content_type='text/plain',body='invalid checked start text'))
        page.locator('#tutor-help-calculation').click();expect(page.locator('#compact-tutor-error')).to_be_visible()
        page.locator('#compact-tutor-close').click();expect(page.locator('#tutor-help-calculation')).to_be_focused()
        self.check('A failed local help package still returns to its origin',True)
        page.unroute('**/tutor/START-debroglie.md')
        page.set_viewport_size({'width':1280,'height':800});set_theme(page,'light')
        page.locator('#tutor-help-calculation').click();expect(page.locator('#compact-tutor-question')).to_be_focused()
        self.check('Local helper layout fits laptop in light theme',page.evaluate('document.documentElement.scrollWidth<=innerWidth+1'))
        self.shot(page,'tutor-local-question-laptop-light.png')

    def wide_measurement_tutor(self):
        page=self.page(viewport={'width':1024,'height':680},has_touch=True,init_script="""Object.defineProperty(navigator,'clipboard',{value:{writeText:async text=>{if(window.__denyClipboard)throw new Error('QA denied');window.__copiedTutorText=text}},configurable:true});""")
        self.seed(page);self.route(page,'joensson/messung')
        before=self.snapshot(page)
        # Keep the underlying layout constant for the absolute scroll-return check.
        # Precision fields now live in an explicit disclosure.
        self.open_marker_coordinates(page)
        trigger=page.locator('#tutor-help-image-measurement')
        trigger.evaluate('e=>e.scrollIntoView({block:"center"})')
        origin_scroll=page.evaluate('scrollY')
        trigger.tap();expect(page.locator('#compact-tutor-question')).to_be_focused()
        self.viewport_target(page,'#compact-tutor-panel')
        panel=page.locator('#compact-tutor-panel')
        geometry=panel.evaluate("e=>{const r=e.getBoundingClientRect(),w=document.querySelector('#measurement-first-workspace').getBoundingClientRect();return {panelWidth:r.width,workspaceWidth:w.width,below:r.top>=w.bottom-1}}")
        self.report.setdefault('wide_tutor_bounds',[]).append(geometry)
        self.check('Wide help spans the measured workspace and is rendered below it',geometry['below'] and geometry['panelWidth']>=geometry['workspaceWidth']-2 and geometry['panelWidth']>800)
        self.check('Opening wide help changes no native measurement or source contact',before['measurement']==self.snapshot(page)['measurement'])
        expect(page.locator('#compact-tutor-title')).to_have_text('Hilfe mit deinem KI-Chat')
        expect(page.locator('#compact-tutor-scope')).to_have_text('Jönsson · Deine Bildmessung')
        expect(page.locator('#compact-tutor-close')).to_have_text('Zurück zur Messung')
        expect(page.locator('#compact-tutor-own')).not_to_be_checked()
        question='QA-SYNTHETISCH: Wie begründe ich meine eigene Markenauswahl?'
        page.locator('#compact-tutor-question').fill(question)
        page.locator('#compact-tutor-copy').click()
        expect(page.locator('#compact-tutor-status')).to_contain_text('Nachricht kopiert.')
        self.check('Wide tutor copies the actual preview without own data by default',page.evaluate('window.__copiedTutorText')==handoff(page)[0] and handoff(page)[1]['ownWork'] is None)
        self.shot(page,'tutor-wide-tablet-short-dark.png')
        page.locator('#compact-tutor-own').click()
        expect(page.locator('#compact-tutor-own')).to_be_checked()
        expect(page.locator('#compact-tutor-own-summary')).to_contain_text('aktuelles Bildband 100–220 px und 2 aktuelle Marken')
        expect(page.locator('#compact-tutor-own-summary')).to_contain_text('Das Bild selbst wird nicht mitkopiert.')
        # Move the first marker without crossing the neighbouring mark at 578.
        page.locator('[data-marker-index="0"]').click()
        self.open_marker_coordinates(page)
        page.locator('#marker-x').fill('552')
        expect(page.locator('[data-own-marker="0"]')).to_have_attribute('data-native-x','552')
        expect(page.locator('#compact-tutor-own')).not_to_be_checked()
        expect(page.locator('#compact-tutor-own-summary')).to_have_text(OWN_WORK_EXCLUDED)
        expect(page.locator('#compact-tutor-question')).to_have_value(question)
        self.check('Actual marker edit revokes old consent while preserving the wide tutor question',handoff(page)[1]['ownWork'] is None)
        page.evaluate('window.__denyClipboard=true')
        page.locator('#compact-tutor-copy').click()
        expect(page.locator('#compact-tutor-status')).to_contain_text('Automatisches Kopieren ist hier nicht möglich.')
        expect(page.locator('#compact-tutor-status')).to_contain_text('Gesamten Text markieren')
        expect(page.locator('#compact-tutor-preview')).to_be_visible()
        page.locator('#compact-tutor-select').click()
        expect(page.locator('#compact-tutor-preview')).to_be_focused()
        self.check('Wide tutor clipboard refusal keeps the complete current message selectable',page.evaluate('window.getSelection().toString()')==handoff(page)[0])
        page.locator('#compact-tutor-close').click()
        expect(trigger).to_be_focused()
        self.check('Wide tutor explicit return restores the original reading position',abs(page.evaluate('scrollY')-origin_scroll)<2)
        # If the learner changes the underlying layout while help is open,
        # returning must preserve the trigger's reading position, not the old document offset.
        anchor_top=trigger.bounding_box()['y']
        trigger.click();expect(page.locator('#compact-tutor-question')).to_be_focused()
        page.locator('#measurement-coordinate-tools > summary').click()
        expect(page.locator('#measurement-coordinate-tools')).to_have_js_property('open',False)
        page.locator('#compact-tutor-close').click();expect(trigger).to_be_focused()
        self.check('Wide tutor return keeps the reading anchor after a precision-panel layout change',
                   abs(trigger.bounding_box()['y']-anchor_top)<2)
        page.set_viewport_size({'width':1080,'height':810});set_theme(page,'light')
        trigger.click();expect(page.locator('#compact-tutor-question')).to_have_value(question)
        self.check('Wide tutor remains inside the iPad landscape width',page.evaluate('document.documentElement.scrollWidth<=innerWidth+1'))
        self.shot(page,'tutor-wide-ipad9-light.png')
        self.section(page,'debroglie');self.route(page,'joensson/messung')
        trigger.click();expect(page.locator('#compact-tutor-question')).to_have_value(question)
        expect(page.locator('#compact-tutor-panel')).to_have_count(1)
        self.check('Chapter return reuses the one question owner without duplicate tutor panels',True)
        page.locator('#compact-tutor-close').click()
        page.route('**/tutor/START-joensson.md',lambda route:route.fulfill(status=200,content_type='text/plain',body='QA invalid verified tutor start'))
        trigger.click();expect(page.locator('#compact-tutor-error')).to_be_visible()
        self.viewport_target(page,'#compact-tutor-panel')
        self.shot(page,'tutor-wide-invalid-package.png')
        page.locator('#compact-tutor-close').click();expect(trigger).to_be_focused()
        self.check('Invalid wide tutor package has a visible error and an intact return to measurement',True)
        page.unroute('**/tutor/START-joensson.md')

    def deferred_tutor_ready_does_not_move_focus(self):
        page=self.page()
        self.section(page,'debroglie')
        guide=page.request.get(urljoin(self.url,'tutor/START-debroglie.md')).body()
        pending=[]
        page.route('**/tutor/START-debroglie.md',lambda route:pending.append(route))
        with page.expect_request('**/tutor/START-debroglie.md'):
            page.locator('#compact-tutor-open').click()
        expect(page.locator('#compact-tutor-question')).to_be_focused()
        expect(page.locator('#compact-tutor-panel')).to_contain_text('Hilfe wird vorbereitet')
        expect(page.locator('#compact-tutor-copy')).to_have_count(0)
        question='QA: Frage schon während der Fachtext lädt.'
        note='QA: Ich schreibe inzwischen wieder meinen eigenen Rechenweg.'
        page.locator('#compact-tutor-question').fill(question)
        page.locator('#article-notes').fill(note)
        expect(page.locator('#article-notes')).to_be_focused()
        expect(page.locator('#article-notes')).to_have_value(note)
        # Retain the real response until the learner has deliberately moved on.
        # No sleep, retry or readiness assertion replaces this interaction.
        assert len(pending)==1,'Expected one held guide request'
        page.evaluate("window.__lateTutorFocus=[];document.addEventListener('focusin',event=>window.__lateTutorFocus.push(event.target.id))")
        pending.pop().fulfill(status=200,content_type='text/markdown; charset=utf-8',body=guide)
        expect(page.locator('#compact-tutor-copy')).to_be_visible()
        # Flush the same render frames in which a formerly load-dependent focus ran.
        page.evaluate('() => new Promise(resolve=>requestAnimationFrame(()=>requestAnimationFrame(resolve)))')
        expect(page.locator('#article-notes')).to_be_focused()
        expect(page.locator('#article-notes')).to_have_value(note)
        expect(page.locator('#compact-tutor-question')).to_have_value(question)
        self.check('A late tutor guide never redirects focus or text from the selected calculation field',
            page.evaluate('window.__lateTutorFocus')==[]
            and self.snapshot(page)['work']['S06_CALCULATION']['note']==note)
        self.check('Tutor question is directly usable before its guide finishes loading',True)
        self.shot(page,'tutor-delayed-guide-focus.png')
        page.unroute('**/tutor/START-debroglie.md')

    def new_tutor_flows(self):
        page=self.page(init_script="""Object.defineProperty(navigator,'clipboard',{value:{writeText:async text=>{window.__copiedTutorText=text}},configurable:true});""")
        self.seed(page)

        def copied_message():
            # Read after the caller's composed-text expectation; a checkbox alone
            # can update one Compose frame before its accompanying message.
            preview,data=handoff(page)
            page.evaluate('window.__copiedTutorText=null')
            page.locator('#compact-tutor-copy').click()
            page.wait_for_function('expected=>window.__copiedTutorText===expected',arg=preview)
            expect(page.locator('#compact-tutor-status')).to_contain_text('Nachricht kopiert.')
            return preview,data

        for visit in range(2):
            # Exercise actual navigation gestures, including a second entry into
            # the standalone helper after returning to the remembered chapter.
            page.locator('#nav-joensson').click()
            expect(page.locator('#joensson-article')).to_be_visible()
            page.locator('header').get_by_role('link',name='MATERIEWELLE',exact=True).click()
            expect(page.locator('#overview-title')).to_be_visible()
            page.get_by_role('link',name='Hilfe mit deinem KI-Chat',exact=True).click()
            expect(page.locator('#compact-tutor-panel')).to_have_count(1)
            expect(page.locator('#compact-tutor-question')).to_be_focused()
            expect(page.locator('#compact-tutor-scope')).to_have_text('Jönsson · Fragen zum Kapitel')
            expect(page.locator('#compact-tutor-copy')).to_be_visible()
            self.check('Landing tutor retains Jönsson after real header and brand navigation '+str(visit+1),handoff(page)[1]['section']=='joensson')
            if visit==1:
                modes=(('hint','Hinweis','Hilf mir mit einem ersten Schritt bei dieser Aufgabe.','ohne die fertige Lösung oder den Ergebniswert vorwegzunehmen.'),
                       ('explanation','Erklärung',DEFAULT_EXPLANATION_QUESTION,'physikalischen Bedeutung'),
                       ('check','Ansatz prüfen','Prüfe meinen Ansatz und zeige mir, was ich als Nächstes verbessern kann.','Prüfe meine tatsächlich beigefügten Angaben.'))
                for mode,label,question,instruction in modes:
                    page.locator('#compact-tutor-'+mode).click()
                    expect(page.locator('#compact-tutor-'+mode)).to_have_attribute('aria-pressed','true')
                    expect(page.locator('#compact-tutor-question')).to_have_value('')
                    expect(page.locator('#compact-tutor-preview')).to_contain_text('## Meine Frage\n'+question)
                    expect(page.locator('#compact-tutor-preview')).to_contain_text('## Gewünschte Hilfe: '+label)
                    text,data=copied_message()
                    request=text.split('## Gewünschte Hilfe: ',1)[1].split('\n\n## Meine freigegebenen Angaben',1)[0]
                    self.check('Actual copied '+label+' uses its default question and selected response instruction',
                        data['question']==question and data['ownWork'] is None
                        and text.count('## Gewünschte Hilfe: ')==1 and instruction in request)
                expect(page.locator('#compact-tutor-summary')).to_contain_text('Wähle deine Angaben aus oder beschreibe oben deinen Ansatz')
            page.locator('#compact-tutor-close').click()
            expect(page.locator('#compact-tutor-panel')).to_have_count(0)
            expect(page.locator('#joensson-article')).to_be_visible()
            expect(page).to_have_url(re.compile(r'#joensson$'))
            self.check('Closing landing tutor returns to Jönsson on visit '+str(visit+1),True)

        page.locator('#course-nav').click()
        draft='QA-SYNTHETISCH: Fazit für die erneute Freigabe früherer Ergebnisse.'
        page.locator('#conclusion-draft').fill(draft)
        page.locator('#tutor-help-conclusion').click()
        page.locator('#compact-tutor-own').check()
        page.locator('#compact-tutor-earlier').check()
        expect(page.locator('#compact-tutor-preview')).to_contain_text('### Zusätzlich freigegebene frühere Ergebnisse')
        expect(page.locator('#compact-tutor-preview')).to_contain_text(NOTE+' S06_CALCULATION')
        _,included=copied_message()
        self.check('Fazit explicitly selected earlier work reaches the actual copied message',
            draft in included['ownWork'] and NOTE+' S06_CALCULATION' in included['ownWork']
            and '### Meine aktuelle Bildauswahl' in included['ownWork'])
        page.locator('#compact-tutor-own').uncheck()
        expect(page.locator('#compact-tutor-earlier')).to_have_count(0)
        expect(page.locator('#compact-tutor-preview')).not_to_contain_text(draft)
        _,withdrawn=copied_message()
        self.check('Withdrawing main Fazit consent removes all selected work from the copy',withdrawn['ownWork'] is None)
        page.locator('#compact-tutor-own').check()
        expect(page.locator('#compact-tutor-earlier')).not_to_be_checked()
        expect(page.locator('#compact-tutor-preview')).to_contain_text(draft)
        expect(page.locator('#compact-tutor-preview')).not_to_contain_text('### Zusätzlich freigegebene frühere Ergebnisse')
        _,renewed=copied_message()
        self.check('Renewed main Fazit consent requires a fresh selection of earlier results',
            draft in renewed['ownWork'] and NOTE+' S06_CALCULATION' not in renewed['ownWork']
            and '### Meine aktuelle Bildauswahl' not in renewed['ownWork'])
        page.locator('#compact-tutor-close').click()

        page.locator('#nav-joensson').click()
        page.locator('#slit-lab-configuration').select_option('BOTH_COHERENT')
        page.locator('#course-model-g').fill('3.2')
        page.locator('#course-model-b').fill('0.7')
        page.locator('#course-slit-save').click()
        page.locator('#course-model-g').fill('4.4')
        page.locator('#slit-lab-configuration').select_option('LEFT_ONLY')
        page.get_by_text('Mit einer Vorhersage beginnen',exact=True).click()
        page.locator('#slit-investigate-spacing').click()
        prediction='QA-SYNTHETISCH: Größeres g verändert im kohärenten Fall den Streifenabstand.'
        reason='QA-SYNTHETISCH: Aktuell ist nur der linke Spalt offen; A war der kohärente Zweispaltfall.'
        page.locator('#slit-investigation-prediction').fill(prediction)
        page.locator('#slit-investigation-reason').fill(reason)
        page.locator('#tutor-help-slit-model').click()
        expect(page.locator('#compact-tutor-scope')).to_have_text('Jönsson · Spaltmodell untersuchen')
        page.locator('#compact-tutor-own').check()
        expect(page.locator('#compact-tutor-preview')).to_contain_text(reason)
        _,slit=copied_message()
        self.check('Local slit-model copy contains actual B, remembered A, configuration and both notes',
            'Aktuelle Einstellung B: g = 4,4 µm; b = 0,7 µm; U = 150 V; L = 10 m.' in slit['ownWork']
            and 'Gemerkte Einstellung A: g = 3,2 µm; b = 0,7 µm; U = 150 V; L = 10 m.' in slit['ownWork']
            and 'Aktuelle Spaltkonfiguration: nur linker Spalt.' in slit['ownWork']
            and prediction in slit['ownWork'] and reason in slit['ownWork']
            and '### Meine aktuelle Bildauswahl' not in slit['ownWork'])

    def tutor(self):
        self.new_tutor_flows()
        self.deferred_tutor_ready_does_not_move_focus()
        self.local_tutor_help()
        self.wide_measurement_tutor()
        page=self.page(init_script="""Object.defineProperty(navigator,'clipboard',{value:{writeText:async text=>{if(window.__denyClipboard)throw new Error('QA denied');window.__copiedTutorText=text}},configurable:true});""")
        self.seed(page)
        self.section(page,'debroglie')
        note_question='QA-SYNTHETISCH: Warum ziehe ich hier die Wurzel? Diese Frage steht zunächst nur in meinen Rechennotizen.'
        page.locator('#article-notes').fill(note_question)
        self.open_tutor(page)
        expect(page.locator('#compact-tutor-question')).to_have_value('')
        expect(page.locator('#compact-tutor-explanation')).to_have_attribute('aria-pressed','true')
        expect(page.locator('#compact-tutor-own-summary')).to_have_text(OWN_WORK_EXCLUDED)
        expect(page.locator('#compact-tutor-own')).not_to_be_checked()
        expect(page.locator('#compact-tutor-copy')).to_have_text('Nachricht kopieren')
        page.locator('#compact-tutor-copy').click()
        expect(page.locator('#compact-tutor-status')).to_contain_text('Nachricht kopiert.')
        _,initial_handoff=handoff(page)
        self.check('Question written in calculation notes is retained and visibly excluded until deliberately selected',
            self.snapshot(page)['work']['S06_CALCULATION']['note']==note_question
            and initial_handoff['question']==DEFAULT_EXPLANATION_QUESTION and initial_handoff['ownWork'] is None
            and note_question not in page.evaluate('window.__copiedTutorText'))
        for key in SECTIONS:
            self.section(page,key)
            self.open_tutor(page)
            expect(page.locator('#compact-tutor-own')).not_to_be_checked()
            expect(page.locator('#compact-tutor-own')).to_have_accessible_name('Meine Angaben aus diesem Kapitel in die Nachricht aufnehmen')
            question='QA: Bitte nur einen kleinen Hinweis zu '+key
            page.locator('#compact-tutor-question').fill(question)
            expect(page.locator('#compact-tutor-copy')).to_have_text('Nachricht kopieren')
            expect(page.locator('#compact-tutor-own-summary')).to_have_text(OWN_WORK_EXCLUDED)
            page.get_by_text('Vollständige Nachricht ansehen',exact=True).click()
            expect(page.locator('#compact-tutor-preview')).to_be_visible()
            expect(page.locator('#compact-tutor-preview')).to_contain_text(question)
            preview,data=handoff(page)
            self.check('Correct curated context with no own work: '+key,data['section']==key and data['question']==question and data['ownWork'] is None and 'Fachlicher Kontext und Hinweise für die Antwort' in preview)
            page.locator('#compact-tutor-copy').click();expect(page.locator('#compact-tutor-status')).to_contain_text('Nachricht kopiert.')
            self.check('Preview equals copied text: '+key,page.evaluate('window.__copiedTutorText')==preview)
            self.check('Tutor needs no downloads: '+key,page.locator('#compact-tutor-panel a[download]').count()==0)
            page.locator('#compact-tutor-own').click();expect(page.locator('#compact-tutor-own')).to_be_checked()
            expect(page.locator('#compact-tutor-own-summary')).to_contain_text('Enthalten:')
            own_summaries={'debroglie':('deine 150-V-Rechnung','Werte des freien Modells: aktuell 375 V'),
                           'davisson-germer':('deine Angaben zum DG-Vergleich','aktuelle Modellwerte 54 V / 50°'),
                           'joensson':('Spaltmodell aktuell g=2 µm, b=0,5 µm','aktuelles Bildband 100–220 px und 2 aktuelle Marken','deine aktuelle Begründung','Das Bild selbst wird nicht mitkopiert.'),
                           'fazit':('dein Fazit',)}
            for text in own_summaries[key]:expect(page.locator('#compact-tutor-own-summary')).to_contain_text(text)
            own_details={'debroglie':('Rechenweg: '+note_question,'Spannungsmodell B: 375 V; Eingabetext: 375'),
                         'davisson-germer':('DG-Modell B: 54 V / 50° zur Normalen','Mein Vergleich des dokumentierten Davisson–Germer-Zahlenfalls · Notiz: '+NOTE+' S11_DG_EVALUATION'),
                         'joensson':('Aktuelle Bildmarken x / px: 551, 578','Aktuelle Begründung: '+self.snapshot(page)['measurement']['justification']),
                         'fazit':('Fazit: '+NOTE+' S16_CONCLUSION',)}
            for text in own_details[key]:expect(page.locator('#compact-tutor-summary')).to_contain_text(text)
            own=handoff(page)[1]['ownWork']
            roles={'debroglie':'### Meine feste Aufgabe bei 150 V','davisson-germer':'### Aktuelle Einstellung des Davisson–Germer-Modells','joensson':'### Meine aktuelle Bildauswahl','fazit':'### Mein Fazitentwurf'}
            self.check('Opt-in contains only current topic role: '+key,roles[key] in own and all(role not in own for topic,role in roles.items() if topic!=key))
            full_message=handoff(page)[0]
            self.check('Copied tutor message has no package, installation or JSON contract: '+key,
                all(hidden not in full_message for hidden in ('Paketversion:', 'Quellenrevision:', 'schemaVersion', 'packageVersion', 'ownWork', '```json', 'references/', 'Client Dateien', 'native Installation')))
            if key=='joensson':
                self.check('Image handoff is readable, native and explicitly uncalibrated',
                    '703 × 242 Originalpixel' in own and 'keine absolute Wellenlänge' in own
                    and self.snapshot(page)['measurement']['justification'] in own
                    and 'Das Bild selbst ist nicht beigefügt' in own)
            if key=='fazit':self.check('Conclusion handoff uses actual personal draft', 'Mein Text:\n  '+NOTE+' S16_CONCLUSION' in own)
        self.section(page,'debroglie');self.open_tutor(page);expect(page.locator('#compact-tutor-own')).not_to_be_checked()
        self.open_model(page);page.locator('#article-voltage').fill('600')
        page.locator('#course-task-answer').fill('100 pm')
        page.locator('#article-notes').fill('QA: eigener Rechenweg zur festen 150-V-Aufgabe')
        page.locator('#compact-tutor-own').click()
        expect(page.locator('#compact-tutor-own')).to_be_checked()
        # The checkbox changes before Compose commits the accompanying message.
        # Await actual selected work, not numbers already present in the guide.
        page.wait_for_function("""() => {
            const text = document.querySelector('#compact-tutor-preview')?.textContent;
            if (!text) return false;
            const own = (text.split('## Meine freigegebenen Angaben\\n')[1] || '').split('\\n\\n## Fachlicher Kontext und Hinweise für die Antwort')[0];
            return own.includes('Mein Ergebnis:\\n  100 pm')
                && own.includes('Aktuelle Einstellung B · im Modell verwendet: 600 V.');
        }""")
        preview,data=handoff(page)
        own=data['ownWork']
        task=own.split('### Meine feste Aufgabe bei 150 V',1)[1].split('### Aktuelle Einstellung des Spannungsmodells',1)[0]
        model=own.split('### Aktuelle Einstellung des Spannungsmodells',1)[1]
        self.check('Readable tutor message separates the fixed 150-V task from the 600-V model',
            'Mein Ergebnis:\n  100 pm' in task and '100,137 pm' in task and '600 V' not in task
            and 'Aktuelle Einstellung B · im Modell verwendet: 600 V.' in model and '50,069 pm' in model
            and 'Eingabetext des Spannungsfelds:\n  600' in model
            and 'ändert die feste 150-V-Aufgabe nicht' in model)
        page.locator('#compact-tutor-copy').click();expect(page.locator('#compact-tutor-status')).to_contain_text('Nachricht kopiert.')
        self.check('Actually copied context preserves the fixed-task and free-model separation',page.evaluate('window.__copiedTutorText')==preview)
        self.shot(page,'tutor-task-model-separation.png')
        page.locator('#article-voltage').fill('375');expect(page.locator('#compact-tutor-own')).not_to_be_checked()
        page.locator('#compact-tutor-own').click();expect(page.locator('#compact-tutor-own')).to_be_checked()
        page.locator('#article-voltage').fill('400');expect(page.locator('#compact-tutor-own')).not_to_be_checked()
        self.check('Changed own input invalidates previous consent',handoff(page)[1]['ownWork'] is None)
        expect(page.locator('#compact-tutor-own-summary')).to_have_text(OWN_WORK_EXCLUDED)
        page.evaluate('window.__denyClipboard=true');page.locator('#compact-tutor-copy').click()
        expect(page.locator('#compact-tutor-status')).to_contain_text('Automatisches Kopieren ist hier nicht möglich.')
        expect(page.locator('#compact-tutor-status')).to_contain_text('Gesamten Text markieren')
        expect(page.locator('#compact-tutor-preview')).to_be_visible()
        self.shot(page,'tutor-preview-fallback.png')
        page.locator('#compact-tutor-select').click()
        expect(page.locator('#compact-tutor-preview')).to_be_focused()
        self.check('Manual fallback selects the complete current payload',page.evaluate('window.getSelection().toString()')==handoff(page)[0])
        expect(page.locator('#compact-tutor-status')).to_contain_text('Text markiert')
        # Legacy packages stay available for existing links; the current UX is copy-only.
        archive=page.request.get(urljoin(self.url,'tutor/materiewellen-fachtutor.zip')).body()
        with zipfile.ZipFile(io.BytesIO(archive)) as z:
            self.check('Preserved legacy ZIP contains the core skill and topic references',z.testzip() is None and sum('/references/' in n and n.endswith('.md') for n in z.namelist())>=4)
        manifest=page.request.get(urljoin(self.url,'tutor/manifest.json')).json()
        item=next(x for x in manifest['files'] if x['path'].endswith('.zip'))
        self.check('Legacy ZIP matches checked manifest',sha(archive)==item['sha256'])
        page.locator('#compact-tutor-question').focus();page.keyboard.press('Escape')
        expect(page.locator('#compact-tutor-panel')).to_have_count(0);expect(page.locator('#compact-tutor-open')).to_be_focused()
        page.route('**/tutor/manifest.json',lambda r:r.fulfill(status=200,content_type='application/json',body='{"schemaVersion":"invalid"}'))
        page.locator('#compact-tutor-open').click();expect(page.locator('#compact-tutor-error')).to_be_visible()
        page.unroute('**/tutor/manifest.json');page.locator('#compact-tutor-retry').click();expect(page.locator('#compact-tutor-copy')).to_be_visible()
        self.check('Package failure has explicit recoverable state',True)

    def handout(self):
        page=self.page();self.section(page,'fazit')
        before = self.snapshot(page).get('measurement', {})
        self.check('No invented handout contact before requesting it', 'HANDOUT_REQUESTED' not in before.get('referenceMaterialsOpened', []))
        raw=self.download(page,'#handout-download','materiewellen.pdf')
        after = self.snapshot(page)['measurement']
        self.check('Handout activation records only requested access', 'HANDOUT_REQUESTED' in after['referenceMaterialsOpened'] and after.get('initialSelection') == before.get('initialSelection'))
        page.get_by_text('Was beim Handout-Aufruf gespeichert wird', exact=True).click()
        expect(page.get_by_text('Der Arbeitsstand vermerkt den Klick auf den Handout-Link.', exact=False)).to_be_visible()
        self.check('Download contact is distinguished from reading in the UI', 'Ob die PDF heruntergeladen oder gelesen wurde, wird nicht erfasst.' in page.locator('#conclusion-article').inner_text())
        self.check('Handout is a real PDF',raw.startswith(b'%PDF-'))
        manifest=page.request.get(urljoin(self.url,'manifest.json')).json()
        expected=manifest['files']['handout/materiewellen.pdf']
        self.check('Downloaded PDF matches shipped file hash',sha(raw)==expected['sha256'])
        path=self.output/'materiewellen.pdf'
        metadata=subprocess.check_output(['pdfinfo',str(path)],text=True)
        self.check('Handout has exactly three pages',re.search(r'^Pages:\s+3$',metadata,re.M) is not None)
        text=subprocess.check_output(['pdftotext',str(path),'-'],text=True)
        self.check('PDF contains all three experiments and sources',all(x in text for x in ('Broglie','Davisson','Jönsson')) and re.search('Quellen|Literatur',text) is not None)

    def viewports(self):
        for width,height,touch in ((1280,800,False),(1440,900,False),(1024,768,True),(1180,820,True)):
            page=self.page(viewport={'width':width,'height':height},has_touch=touch)
            for theme in ('dark','light'):
                set_theme(page,theme)
                self.route(page,'lernen')
                expect(page.locator('#overview-title')).to_be_visible()
                self.check(f'Overview fits landscape {width} {theme}',page.evaluate('document.documentElement.scrollWidth<=innerWidth+1'))
                if width==1024:self.shot(page,f'overview-ipad-{theme}.png')
                for key in SECTIONS:
                    article=self.section(page,key)
                    self.check(f'No horizontal page overflow {key} {width} {theme}',page.evaluate('document.documentElement.scrollWidth<=innerWidth+1'))
                    self.viewport_target(page,article.locator('h1').get_attribute('id') and '#'+article.locator('h1').get_attribute('id') or SECTIONS[key])
                    self.check('Rendered SVG/image resources decode '+key,page.evaluate('Array.from(document.images).every(i=>i.complete&&i.naturalWidth>0)'))
                    if key=='joensson':self.touch_targets(page,f'{width}x{height} {theme}')
                    if width==1024 and theme=='dark':self.shot(page,f'ipad-1024-{key}-dark.png')
                if width==1280:
                    self.section(page,'debroglie')
                    for selector in ('.article-lead','.article-source','#article-rechnung > p'):self.contrast(page,selector,theme)
            if touch:
                page.locator('#theme-preference').tap();expect(page.locator('html')).to_have_attribute('data-theme','dark')
                page.locator('#theme-preference').click();expect(page.locator('html')).to_have_attribute('data-theme','light')
                self.check('Touch and mouse alternate on same iPad viewport '+str(width),True)
            self.shot(page,f'ipad-or-laptop-{width}-light.png')
            if width==1024:
                self.route(page,'joensson/messung');page.locator('#jonsson-native-image').scroll_into_view_if_needed()
                self.shot(page,'ipad-1024-measurement-light.png')
            page.evaluate("document.documentElement.style.fontSize='200%'")
            for key in SECTIONS:
                self.section(page,key);self.check('200% text enlargement retains document width '+key,page.evaluate('document.documentElement.scrollWidth<=innerWidth+1'))
        normal=self.page(reduced_motion='no-preference');self.section(normal,'debroglie')
        expect(normal.locator('#motion-hero')).to_be_visible()
        self.check('Introductory box contains only a schematic wave',normal.locator('#motion-hero .motion-wave-trace').count()==1 and normal.locator('#motion-hero').inner_text().strip()=='' and normal.locator('#motion-hero .motion-head,#motion-hero .motion-status,#motion-hero .motion-mascot,#motion-hero .motion-spark').count()==0)
        normal.wait_for_function('getComputedStyle(document.querySelector("#motion-hero .motion-wave-trace")).animationName!=="none"')
        transform=normal.locator('#motion-hero .motion-wave-trace').evaluate('e=>getComputedStyle(e).transform')
        normal.wait_for_function('before=>getComputedStyle(document.querySelector("#motion-hero .motion-wave-trace")).transform!==before',arg=transform)
        self.check('Visible introductory wave actually animates',True)
        normal.emulate_media(reduced_motion='reduce')
        normal.wait_for_function('getComputedStyle(document.querySelector("#motion-hero .motion-wave-trace")).animationName==="none"')
        self.check('Reduced motion stops the decorative wave',True)
        normal.emulate_media(reduced_motion='no-preference')
        self.route(normal,'lernen/spannung-zur-vorhersage')
        expect(normal.locator('#article-reading-surface')).to_have_class(re.compile('article-reading-enhanced'))
        expect(normal.locator('[id^="article-line-"]')).to_have_count(6)
        normal.locator('#article-reading-choice-3').click()
        expect(normal.locator('#article-line-3')).to_be_focused()
        normal.emulate_media(reduced_motion='reduce')
        expect(normal.locator('#article-reading-surface')).not_to_have_class(re.compile('article-reading-enhanced'))
        expect(normal.locator('#article-line-5')).to_be_attached()
        self.check('Scroll derivation retains all six steps with a normal-flow reduced-motion alternative',True)



    def data_failure(self):
        # Exercise the active article UI. Fault responses are deliberately injected;
        # only their precisely matched browser events are separated from real errors.
        source_url=urljoin(self.url,'versuchsdaten.json')
        probe=self.browser.new_page()
        canonical=probe.request.get(source_url).json()
        wrong_order=json.loads(json.dumps(canonical))
        wrong_order['davisson_germer']['geometry']['diffraction_order']=2
        profile_url=urljoin(self.url,'joensson_sensitivitaet.json')
        wrong_profile=probe.request.get(profile_url).json()
        wrong_profile['source_image_sha256']='0'*64
        probe.close()

        def page_for_failure(pattern,body=None,status=200,abort=False):
            page=self.browser.new_page(viewport={'width':1280,'height':800})
            self.pages.append(page);self.current=page;self.observe(page)
            if abort:page.route(pattern,lambda route:route.abort('failed'))
            else:page.route(pattern,lambda route:route.fulfill(status=status,content_type='application/json',body=body))
            return page

        def expected_fixture_events(name,url,status=None,aborted=False):
            expected={'case':name,'url':url,'http':[],'requests':[],'console':[]}
            if status is not None:
                expected['http']=[item for item in self.report['http_errors'] if item['url']==url and item['status']==status]
                self.check('Injected HTTP failure is observed exactly once: '+name,len(expected['http'])==1)
                console_pattern=re.compile(r'^Failed to load resource: the server responded with a status of '+str(status)+r' \([^\n]*\)$')
            elif aborted:
                expected['requests']=[item for item in self.report['request_failures'] if item['url']==url and item['failure']=='net::ERR_FAILED']
                self.check('Injected transport abort is observed exactly once: '+name,len(expected['requests'])==1)
                console_pattern=re.compile(r'^Failed to load resource: net::ERR_FAILED$')
            else:return
            expected['console']=[item for item in self.report['console_messages'] if item['type']=='error' and console_pattern.fullmatch(item['text'])]
            self.check('Only the one expected browser resource error is classified: '+name,len(expected['console'])==1)
            for field,items in (('http_errors',expected['http']),('request_failures',expected['requests']),('console_messages',expected['console'])):
                for item in items:self.report[field].remove(item)
            self.report.setdefault('expected_fixture_errors',[]).append(expected)

        cases=(
            ('http503',503,'{}','Die Datendatei ist nicht verfügbar (HTTP 503).',False),
            ('transport',None,None,'Die Datendatei konnte nicht übertragen werden.',True),
            ('malformed-json',200,'{','Die Datei hat kein lesbares Datenformat.',False),
            ('unsupported-schema',200,'{"schema_version":"invalid"}','Die Datei passt nicht zur geprüften Datenspezifikation.',False),
            ('invalid-diffraction-order',200,json.dumps(wrong_order),'Die Datei passt nicht zur geprüften Datenspezifikation.',False),
        )
        for name,status,body,message,aborted in cases:
            page=page_for_failure('**/versuchsdaten.json',body,status,aborted)
            page.goto(self.url+'#davisson-germer',wait_until='networkidle')
            failure=page.locator('[data-load-state="failed"]')
            expect(failure).to_be_visible();expect(failure).to_contain_text(message)
            expect(page.locator('#dg-article-geometric,#dg-article-predicted')).to_have_count(0)
            page.evaluate("location.hash='joensson'")
            expect(failure).to_be_visible();expect(failure).to_contain_text(message)
            expect(page.locator('#jonsson-native-image')).to_have_count(0)
            self.check('Typed failure stays out of both scientific article results: '+name,True)
            self.section(page,'debroglie')
            page.locator('#course-task-answer').fill('100 pm');page.locator('#course-numeric-check').click()
            expect(page.locator('#course-numeric-feedback')).to_have_attribute('data-answer-status','CORRECT')
            self.check('Independent 150-V calculation remains available: '+name,True)
            if name=='invalid-diffraction-order':
                page.evaluate("location.hash='davisson-germer'");expect(failure).to_be_visible()
                self.shot(page,'invalid-scientific-data.png')
            page.close()
            if status==503:expected_fixture_events(name,source_url,status=status)
            elif aborted:expected_fixture_events(name,source_url,aborted=True)

        page=page_for_failure('**/joensson_sensitivitaet.json',json.dumps(wrong_profile))
        page.goto(self.url+'#joensson/messung',wait_until='networkidle')
        expect(page.locator('#jonsson-native-image')).to_be_visible()
        expect(page.locator('#gray-profile-status')).to_contain_text('Die Datei passt nicht zur geprüften Datenspezifikation.')
        expect(page.locator('#jonsson-gray-profile')).to_have_count(0)
        for x in (551,578):
            self.open_marker_coordinates(page)
            page.locator('#marker-new-x').fill(str(x));page.locator('#marker-add').click()
        page.locator('#jonsson-justification').fill('QA-SYNTHETISCH: zwei eigene Bildkoordinaten trotz fehlender Profilreferenz.')
        self.check('Own pixel mean remains independent of failed profile',float(page.locator('#jonsson-spacing').get_attribute('data-spacing-px'))==27)
        page.locator('#jonsson-seal-selection').click()
        expect(page.locator('#jonsson-show-reference')).to_be_disabled()
        expect(page.locator('#measurement-intermediate-maxima')).to_have_count(0)
        saved=self.snapshot(page)
        self.check('Rejected profile cannot fabricate reference marks or remove own measurement',
                   saved['measurement']['markers']==[{'xPx':551},{'xPx':578}]
                   and saved['measurement']['initialSelection']['markers']==[{'xPx':551},{'xPx':578}]
                   and saved['measurement']['referenceViewed'] is False)
        page.locator('#gray-profile-status').scroll_into_view_if_needed();self.shot(page,'profile-integrity-fallback.png')

def main(*, scenarios=SCENARIOS, suite_type=Checks, harness_path=Path(__file__)):
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument('--url');p.add_argument('--dist',type=Path,default=PROJECT/'dist-run-two');p.add_argument('--base-path',default='/')
    p.add_argument('--output',type=Path,default=Path('/tmp/materiewelle-run-two'));p.add_argument('--chromium',default=os.environ.get('SCREENSHOT_CHROMIUM','/usr/bin/chromium'))
    p.add_argument('--only',nargs='+',choices=scenarios);a=p.parse_args()
    a.output.mkdir(parents=True,exist_ok=True)
    if any(a.output.iterdir()):p.error('Choose a fresh output directory; previous evidence is preserved.')
    report={'recorded_at':datetime.now(timezone.utc).isoformat(),'method':'Browser plugin not available; Python Playwright/system Chromium','scope':CONTRACT,'contract':CONTRACT,'test_harness_sha256':sha(harness_path.read_bytes()),'base_harness_sha256':sha(Path(__file__).read_bytes()),'selected_scenarios':a.only or list(scenarios),'not_tested':['real iPad/Safari','real touch hardware','screenreader','human learning outcomes','native AI-provider skill import','public deployment'],'checks':[],'scenarios':[],'screenshots':[],'downloads':[],'contrast_samples':[],'page_errors':[],'console_messages':[],'request_failures':[],'http_errors':[],'forbidden_requests':[],'outbound_requests':[]}
    def run(url):
        report['url']=url
        with sync_playwright() as pw:
            browser=pw.chromium.launch(executable_path=a.chromium,args=['--no-sandbox']);report['browser']=browser.version
            suite=suite_type(browser,url,a.output,report)
            try:
                page=suite.page();r=page.request.get(urljoin(url,'manifest.json'));manifest=r.json();js=page.request.get(urljoin(url,'materiewellen.js'))
                suite.check('New product contract matches artifact',manifest.get('product_contract')==CONTRACT)
                suite.check('Served JS matches actual manifest',js.ok and sha(js.body())==manifest['files']['materiewellen.js']['sha256'])
                suite.check('Only curated handout PDF enters manifest',{n for n in manifest['files'] if n.endswith('.pdf')}=={'handout/materiewellen.pdf'})
                suite.check('Internal derivatives absent',not any('learning-docs' in n or 'arbeitsfassung' in n or 'praesentation' in n for n in manifest['files']))
                report['release']={'deployment_base':manifest['deployment_base'],'mode':manifest['mode'],'javascript_sha256':sha(js.body()),'manifest_sha256':sha(r.body())}
                for scenario in a.only or scenarios:
                    entry={'name':scenario,'passed':False};report['scenarios'].append(entry)
                    try:getattr(suite,scenario)();entry['passed']=True
                    except Exception as e:
                        entry.update(failure=type(e).__name__+': '+str(e),traceback=traceback.format_exc())
                        if suite.current:
                            try:suite.shot(suite.current,scenario+'-failure.png')
                            except Exception as capture:entry['capture_failure']=str(capture)
                    finally:
                        for page in suite.pages:
                            if not page.is_closed():settle_images(page);page.close()
                        suite.pages=[]
                        print(json.dumps({'scenario':entry},ensure_ascii=False),flush=True)
                        (a.output/'report.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n')
                for key in ('page_errors','console_messages','request_failures','http_errors','forbidden_requests','outbound_requests'):suite.check('No '+key,not report[key])
                suite.check('All selected scenarios passed',all(x['passed'] for x in report['scenarios']))
            finally:browser.close()
    try:
        if a.url:run(a.url)
        else:
            with browser_base.served(a.dist) as origin:run(urljoin(origin,a.base_path.strip('/')+'/' if a.base_path.strip('/') else ''))
        report['status']='passed'
    except Exception as e:report.update(status='failed',failure=type(e).__name__+': '+str(e));raise
    finally:
        (a.output/'report.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n')
        print(json.dumps({'status':report.get('status'),'scenarios':report['scenarios'],'report':str(a.output/'report.json')}))
if __name__=='__main__':main()
