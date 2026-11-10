#!/usr/bin/env python3
"""Download real personal reports and distinguish requested handout access from delivery.

Uses the established static-artifact/browser harness. All own work below is an
explicit synthetic QA fixture, not evidence of a pupil's learning or authorship.
"""
from __future__ import annotations

from contextlib import contextmanager
from functools import partial
from http.server import ThreadingHTTPServer
import importlib.util
import threading
import json
from pathlib import Path
from urllib.parse import urljoin, urlsplit

from playwright.sync_api import expect
from browser_assets import reload_with_images
from browser_evidence import QuietHandler

_spec = importlib.util.spec_from_file_location('personal_report_base', Path(__file__).with_name('test-run-two.py'))
base = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(base)


class HandoutFailure:
    active = False
    requests = []


class FixtureHandler(QuietHandler):
    def do_GET(self):
        if HandoutFailure.active and urlsplit(self.path).path.endswith('/handout/materiewellen.pdf'):
            HandoutFailure.requests.append(self.path)
            body = b'QA fixture: handout unavailable'
            self.send_response(503)
            self.send_header('Content-Type', 'text/plain; charset=utf-8')
            self.send_header('Content-Length', str(len(body)))
            self.end_headers()
            self.wfile.write(body)
        else:
            super().do_GET()


@contextmanager
def served_with_failure_fixture(directory):
    # Download requests can bypass Playwright page/context routing. Fulfill the
    # real HTTP failure at the local test origin, without modifying the artifact.
    server = ThreadingHTTPServer(('127.0.0.1', 0), partial(FixtureHandler, directory=str(directory)))
    worker = threading.Thread(target=server.serve_forever, daemon=True)
    worker.start()
    try:
        yield f'http://127.0.0.1:{server.server_port}/'
    finally:
        HandoutFailure.active = False
        server.shutdown()
        server.server_close()
        worker.join()

OWN_DG = {
    'prediction': 'QA-SYNTHETISCH: 999 nm; diese falsche Vorhersage muss unverändert bleiben.',
    'numericAnswer': 'QA-SYNTHETISCH: mein geometrischer Rechenweg ergibt 0,165 nm.',
    'note': 'QA-SYNTHETISCH: Der Winkelbezug ist noch zu prüfen; kein automatisches Urteil.',
    'solutionViewed': True,
}
OWN_REASON = 'QA-SYNTHETISCH: vier gewählte Bildpositionen; Maximalwahl nicht automatisch bestätigt.'
OWN_CONCLUSION = 'QA-SYNTHETISCH: Mein Schluss bleibt vorläufig. Ein unabhängiger Bildmaßstab fehlt.'
OWN_MARKS = [551, 564, 578, 592]
EMPTY_MEASUREMENT = 'Noch keine eigene Bildauswahl oder Begründung eingetragen.'


class ReportChecks(base.Checks):
    def conclusion_synthesis(self):
        # A conclusion first explains the argument, then invites personal work,
        # followed by optional resources. Exercise both resource actions on the
        # actual rendered landscape page; do not replace the existing provenance tests.
        for width, height, theme in ((1080, 810, 'dark'), (1024, 700, 'light')):
            page = self.page(viewport={'width': width, 'height': height}, has_touch=True)
            base.set_theme(page, theme)
            self.section(page, 'fazit')
            expect(page.locator('#conclusion-title')).to_be_focused()
            synthesis = page.locator('#conclusion-synthesis')
            expect(synthesis).to_be_visible()
            expect(synthesis.locator('li')).to_have_count(3)
            positions = page.evaluate('''() => Object.fromEntries(
                ['conclusion-synthesis','conclusion-resources','own-work-summary','conclusion-draft']
                .map(id => {const r=document.getElementById(id).getBoundingClientRect();
                    return [id, {top:r.top+scrollY,bottom:r.bottom+scrollY}]}))''')
            self.report.setdefault('conclusion_layout', []).append({'width': width, 'height': height, 'theme': theme, 'positions': positions})
            self.check(f'Conclusion argument precedes personal work and optional resources {width} {theme}',
                       positions['conclusion-synthesis']['bottom'] <= positions['own-work-summary']['top']
                       and positions['own-work-summary']['bottom'] <= positions['conclusion-draft']['top']
                       and positions['conclusion-draft']['bottom'] <= positions['conclusion-resources']['top'])
            expect(page.locator('#conclusion-draft')).to_have_value('')
            self.check(f'Authored synthesis does not fill a personal conclusion {width}', True)
            self.shot(page, f'conclusion-synthesis-{width}-{theme}.png')
            page.locator('#conclusion-draft').fill(OWN_CONCLUSION)
            resources = page.locator('#conclusion-resources')
            resources.evaluate('e => e.scrollIntoView({block:"center",behavior:"instant"})')
            for selector in ('#handout-download', '#tutor-help-conclusion'):
                bounds = page.locator(selector).evaluate('''e => {
                    const r=e.getBoundingClientRect(), h=document.querySelector('header').getBoundingClientRect();
                    const hit=document.elementFromPoint(r.x+r.width/2,r.y+r.height/2);
                    return {width:r.width,height:r.height,visible:r.top>=h.bottom && r.bottom<=innerHeight
                        && r.left>=0 && r.right<=innerWidth && !!hit && e.contains(hit)};
                }''')
                self.check(f'Conclusion resource is visible and touch-sized {selector} {width}',
                           bounds['visible'] and bounds['width'] >= 44 and bounds['height'] >= 44)
            self.shot(page, f'conclusion-resources-{width}-{theme}.png')
            raw = self.download(page, '#handout-download', f'conclusion-handout-{width}.pdf')
            self.check(f'Visible conclusion handout action downloads an actual PDF {width}', raw.startswith(b'%PDF-'))
            expect(page.locator('#conclusion-draft')).to_have_value(OWN_CONCLUSION)
            page.locator('#tutor-help-conclusion').tap()
            expect(page.locator('#compact-tutor-question')).to_be_focused()
            question = 'QA-SYNTHETISCH: Wie hängen meine Rechnung und die beiden Versuche zusammen?'
            page.locator('#compact-tutor-question').fill(question)
            page.get_by_role('button', name='Zurück zum Fazit', exact=True).click()
            expect(page.locator('#compact-tutor-panel')).to_have_count(0)
            expect(page.locator('#tutor-help-conclusion')).to_be_focused()
            expect(page.locator('#conclusion-draft')).to_have_value(OWN_CONCLUSION)
            page.locator('#tutor-help-conclusion').tap()
            expect(page.locator('#compact-tutor-question')).to_have_value(question)
            page.get_by_role('button', name='Zurück zum Fazit', exact=True).click()
            self.check(f'Conclusion tutor action preserves own text, question and return focus {width}', True)

    def prepared_work(self, name):
        page = self.page(viewport={'width': 1080, 'height': 810}, has_touch=True)
        self.section(page, 'debroglie')
        page.locator('#article-notes').fill('QA-SYNTHETISCH: Fixture initialisieren.')
        page.wait_for_function('key => localStorage.getItem(key) !== null', arg=base.STORAGE)
        fixture = self.snapshot(page)
        fixture['work'] = {
            'S06_CALCULATION': {'numericAnswer': '', 'solutionViewed': True},
            'S11_DG_EVALUATION': OWN_DG,
            'S16_CONCLUSION': {'note': OWN_CONCLUSION},
        }
        fixture['measurement']['markers'] = [{'xPx': x} for x in OWN_MARKS]
        fixture['measurement']['justification'] = OWN_REASON
        page.evaluate('([key, value]) => localStorage.setItem(key, value)', [base.STORAGE, json.dumps(fixture)])
        reload_with_images(page)
        self.route(page, 'joensson/messung')
        expect(page.locator('#jonsson-seal-selection')).to_be_enabled()
        return page

    def read_report(self, page, filename, expected_conclusion=OWN_CONCLUSION, include_technical=False):
        self.section(page, 'fazit')
        expect(page.locator('#conclusion-draft')).to_have_value(expected_conclusion)
        disclosure = page.get_by_text('Deinen Arbeitsbericht lesen und kopieren', exact=True)
        if not page.locator('#personal-evidence-download').is_visible():
            disclosure.click()
        page.get_by_label('Technischen Anhang mitkopieren und herunterladen', exact=True).set_checked(include_technical)
        return self.download(page, '#personal-evidence-download', filename).decode('utf-8')

    def personal_report(self):
        self.conclusion_synthesis()
        page = self.prepared_work('report')
        page.locator('#jonsson-seal-selection').click()
        first = self.snapshot(page)['measurement']['initialSelection']
        self.check('First choice is made through the actual application', first is not None and first['markers'] == [{'xPx': x} for x in OWN_MARKS])
        standard = self.read_report(page, 'personal-evidence-standard.txt')
        self.check('Standard report omits the optional technical appendix while retaining own work and bibliography',
                   'TECHNISCHER ANHANG' not in standard and 'SHA-256' not in standard
                   and OWN_REASON in standard and 'Diffraction of Electrons by a Crystal of Nickel' in standard)
        report = self.read_report(page, 'personal-evidence.txt', include_technical=True)
        self.check('Downloaded report is actual readable text', report.startswith('MATERIEWELLE') and '\x00' not in report)
        self.check('Each own DG route and interpretation survives exactly once', all(report.count(value) == 1 for value in OWN_DG.values() if isinstance(value, str)))
        self.check('Own conclusion and measurement reasoning are retained', report.count(OWN_CONCLUSION) == 1 and OWN_REASON in report)
        self.check('Actual native coordinates and interval result reach the download', '551, 564, 578, 592' in report and '13, 14, 14' in report)
        self.check('Sealed first selection without a comparison never gets the empty-measurement message',
                   self.snapshot(page)['measurement']['comparison'] is None and EMPTY_MEASUREMENT not in report)
        main, appendix = report.split('TECHNISCHER ANHANG', 1)
        de_broglie = main.split('2 · DAVISSON', 1)[0]
        self.check('Opened control never fills the missing own 150 V result', 'Eingabe: noch offen' in de_broglie and '100,137' not in de_broglie and '100.137' not in de_broglie)
        self.check('Both bibliographic titles are visible before technical diagnostics', all(title in main for title in (
            'Diffraction of Electrons by a Crystal of Nickel',
            'A proper understanding of the Davisson and Germer experiments',
            'Electron Diffraction at Multiple Slits',
        )))
        self.check('D1, D2 and J1 have independent readable citations', all('\n' + key + ' · ' in main for key in ('D1', 'D2', 'J1')))
        self.check('Technical hashes, private project paths and HTTP diagnostics stay out of main report', 'SHA-256' not in main and 'QUELLENAKTE' not in main and 'HTTP 403' not in main)
        self.check('Scientific limitations remain next to results', 'Das Original D1 wurde nicht eingesehen' in main and 'keine absolute Wellenlängenmessung' in main)
        data = page.request.get(urljoin(self.url, 'versuchsdaten.json')).json()
        self.check('Imported provenance is retained exactly in technical appendix', all(source['status'] in appendix for source in data['source_ids'].values()))
        self.check('Saved source raster identity remains recoverable', first['imageSha256'] in appendix)
        self.check('Export leaves wrong/empty own answers and first choice unchanged', self.snapshot(page)['work']['S11_DG_EVALUATION']['prediction'] == OWN_DG['prediction'] and self.snapshot(page)['work']['S06_CALCULATION']['numericAnswer'] == '' and self.snapshot(page)['measurement']['initialSelection'] == first)
        page.context.grant_permissions(['clipboard-read', 'clipboard-write'])
        page.locator('#personal-evidence-copy').click()
        expect(page.locator('#personal-evidence-copy-status')).to_have_text('Arbeitsbericht kopiert.')
        self.check('Actual clipboard carries the same complete report as the optional TXT download',
                   page.evaluate('navigator.clipboard.readText()') == report)
        page.evaluate('''() => {
            window.__reportClipboardWrite = navigator.clipboard.writeText.bind(navigator.clipboard);
            Object.defineProperty(navigator.clipboard, 'writeText', {configurable:true,
                value:() => Promise.reject(new DOMException('QA permission refusal', 'NotAllowedError'))});
        }''')
        page.locator('#personal-evidence-copy').click()
        expect(page.locator('#personal-evidence-copy-status')).to_contain_text('Automatisches Kopieren ist hier nicht möglich.')
        expect(page.locator('#personal-evidence-copy-preview')).to_be_visible()
        page.locator('#personal-evidence-copy-select').click()
        preview = page.locator('#personal-evidence-copy-preview').text_content()
        selection = page.evaluate('window.getSelection().toString()')
        self.report['manual_copy_observation'] = {'expected_length': len(report), 'preview_length': len(preview),
            'selection_length': len(selection), 'expected_tail': report[-50:], 'selection_tail': selection[-50:]}
        # Chromium's rendered Selection omits the final line break of a <pre>.
        # Accept only that one terminal difference; do not trim or alter own text.
        self.check('Clipboard refusal exposes exact text and selects all content (optional terminal line break)',
                   preview == report and (selection == report or selection + '\n' == report))
        page.evaluate('''() => Object.defineProperty(navigator.clipboard, 'writeText',
            {configurable:true, value:window.__reportClipboardWrite})''')
        page.locator('#conclusion-draft').scroll_into_view_if_needed()
        self.shot(page, 'personal-conclusion-ipad-dark.png')
        expect(page.locator('#personal-evidence-calculations')).to_be_visible()
        page.locator('#personal-evidence-calculations').scroll_into_view_if_needed()
        self.shot(page, 'personal-report-ipad-dark.png')
        page.locator('#personal-evidence-sources').scroll_into_view_if_needed()
        self.shot(page, 'personal-report-sources-ipad-dark.png')
        self.optional_claims_report(page)
        self.measurement_report_variants()
        self.presence_summary()

    def optional_claims_report(self, page):
        # Keep the preceding control-only regression intact, then complete the
        # missing calculation through the UI. The prepared DG, measurement and
        # conclusion now form the four-present visitor state from the review.
        own_answer = '100 pm'
        own_note = 'QA-SYNTHETISCH: Meine Rechnung fuer 150 V, selbst eingetragene Testnotiz.'
        page.locator('#own-work-calculation-link').click()
        expect(page.locator('#course-task-answer')).to_be_focused()
        page.locator('#course-task-answer').fill(own_answer)
        page.locator('#article-notes').fill(own_note)
        self.section(page, 'fazit')
        for key in ('calculation', 'dg', 'measurement', 'conclusion'):
            expect(page.locator('#own-work-' + key + '-status')).to_have_text('Angaben vorhanden')
        expect(page.get_by_text('Freiwillig: Aussagen mit Belegen ergänzen', exact=True)).to_be_visible()
        blank_state = self.snapshot(page)
        self.check('Four present visitor results do not require filling optional claim rows',
                   all(not value.strip() for row in blank_state['inquiry']['evidenceRows']
                       for value in row.values()))

        blank_report = self.read_report(page, 'personal-report-optional-claims-blank.txt')
        expect(page.locator('#personal-evidence-claims')).to_have_count(0)
        self.check('Unused optional claims are absent from the actual TXT, with visitor results retained',
                   'Freiwillige Aussagen mit Belegen' not in blank_report
                   and all(f'Aussage {number}:' not in blank_report for number in (1, 2, 3))
                   and all(value in blank_report for value in
                       (own_answer, own_note, OWN_DG['prediction'], OWN_DG['numericAnswer'],
                        OWN_DG['note'], OWN_REASON, OWN_CONCLUSION))
                   and 'Marken im Originalraster: 551, 564, 578, 592' in blank_report)
        page.locator('#personal-evidence-copy').click()
        expect(page.locator('#personal-evidence-copy-status')).to_have_text('Arbeitsbericht kopiert.')
        self.check('Actual clipboard also omits unused optional rows and matches the blank-claims TXT',
                   page.evaluate('navigator.clipboard.readText()') == blank_report
                   and self.snapshot(page) == blank_state)
        page.locator('#personal-evidence-conclusion').scroll_into_view_if_needed()
        self.shot(page, 'personal-report-optional-claims-blank.png')

        # Enter only the second row's evidence field. This must stay row 2 and
        # remain visible even without an assertion or limitation; no data seed.
        own_evidence = ('QA-SYNTHETISCH: Mein Beleg nur in Zeile 2.\n'
                        'Die Aussage und Grenze sind noch nicht eingetragen.')
        page.get_by_text('Freiwillig: Aussagen mit Belegen ergänzen', exact=True).click()
        expect(page.get_by_role('heading', name='Freiwillige Belegtabelle', exact=True)).to_be_visible()
        page.locator('#station-evidence-2-evidence').fill(own_evidence)
        expect(page.locator('#station-evidence-2-evidence')).to_have_value(own_evidence)
        for row in (1, 3):
            for field in ('claim', 'evidence', 'limitation'):
                expect(page.locator(f'#station-evidence-{row}-{field}')).to_have_value('')
        expect(page.locator('#station-evidence-2-claim')).to_have_value('')
        expect(page.locator('#station-evidence-2-limitation')).to_have_value('')
        edited_state = self.snapshot(page)
        expected_rows = json.loads(json.dumps(blank_state['inquiry']['evidenceRows']))
        expected_rows[1]['evidence'] = own_evidence
        self.check('Actual partial-row edit preserves empty rows and every other visitor result',
                   edited_state['inquiry']['evidenceRows'] == expected_rows
                   and edited_state['work'] == blank_state['work']
                   and edited_state['measurement'] == blank_state['measurement'])

        partial_report = self.read_report(page, 'personal-report-optional-claims-partial.txt')
        claims = page.locator('#personal-evidence-claims')
        expect(claims).to_be_visible()
        expect(claims.get_by_role('heading', name='Freiwillige Aussagen mit Belegen', exact=True)).to_be_visible()
        expect(claims.get_by_role('heading', name='Aussage 2', exact=True)).to_have_count(1)
        for number in (1, 3):
            expect(claims.get_by_role('heading', name=f'Aussage {number}', exact=True)).to_have_count(0)
        expect(page.locator('#personal-report-evidence-1')).to_have_text(own_evidence)
        expect(claims).to_contain_text('Behauptung: (nicht ausgefüllt).')
        expect(claims).to_contain_text('Annahme oder Grenze: (nicht ausgefüllt).')
        expect(claims).not_to_contain_text('noch offen')
        self.check('Actual TXT keeps partial row 2 verbatim and labels empty optional fields neutrally',
                   'Freiwillige Aussagen mit Belegen' in partial_report
                   and 'Aussage 2: (nicht ausgefüllt)' in partial_report
                   and f'Beleg: {own_evidence}\n' in partial_report
                   and partial_report.count(own_evidence) == 1
                   and 'Annahme/Grenze: (nicht ausgefüllt)' in partial_report
                   and all(f'Aussage {number}:' not in partial_report for number in (1, 3)))
        page.locator('#personal-evidence-copy').click()
        expect(page.locator('#personal-evidence-copy-status')).to_have_text('Arbeitsbericht kopiert.')
        self.check('Actual clipboard includes the edited row and matches the fresh TXT without changing data',
                   page.evaluate('navigator.clipboard.readText()') == partial_report
                   and self.snapshot(page) == edited_state)
        claims.scroll_into_view_if_needed()
        self.shot(page, 'personal-report-optional-claims-partial.png')

    def measurement_report_variants(self):
        # Exercise the actual UI from an empty session, not just imported data.
        page = self.page(viewport={'width': 1080, 'height': 810}, has_touch=True)
        report = self.read_report(page, 'report-empty-measurement.txt', expected_conclusion='')
        self.check('An empty session reports the missing measurement exactly once',
                   report.count(EMPTY_MEASUREMENT) == 1 and 'Marken im Originalraster:' not in report)
        page.locator('#conclusion-draft').fill(OWN_CONCLUSION)
        self.route(page, 'joensson/messung')
        for x in OWN_MARKS:
            self.open_marker_coordinates(page)
            page.locator('#marker-new-x').fill(str(x))
            page.locator('#marker-add').click()
        page.locator('#jonsson-justification').fill(OWN_REASON)
        page.locator('#jonsson-seal-selection').click()
        self.section(page, 'fazit')
        saved = self.snapshot(page)
        self.check('Four markers and a first selection are saved through visible controls without a comparison',
                   saved['measurement']['initialSelection']['markers'] == [{'xPx': x} for x in OWN_MARKS]
                   and saved['measurement']['comparison'] is None)
        report = self.read_report(page, 'report-first-selection-no-comparison.txt')
        self.check('Actual own selection reaches TXT with its intervals and no contradictory empty state',
                   'Marken im Originalraster: 551, 564, 578, 592' in report
                   and '13, 14, 14' in report and 'Gesicherte erste Auswahl' in report
                   and OWN_REASON in report and EMPTY_MEASUREMENT not in report
                   and 'Gespeicherter eigener Vergleich' not in report)
        self.check('Downloading the first selection does not create a comparison or alter work',
                   self.snapshot(page) == saved)
        self.route(page, 'joensson/messung')
        self.open_disclosures(page, '#jonsson-compare-save')
        page.locator('#jonsson-compare-save').click()
        self.section(page, 'fazit')
        saved = self.snapshot(page)
        report = self.read_report(page, 'report-with-own-comparison.txt')
        self.check('Adding an explicit comparison keeps both measurement records without the empty state',
                   saved['measurement']['comparison']['markers'] == saved['measurement']['initialSelection']['markers']
                   and 'Gesicherte erste Auswahl' in report and 'Gespeicherter eigener Vergleich' in report
                   and EMPTY_MEASUREMENT not in report and self.snapshot(page) == saved)

    def presence_summary(self):
        page = self.page(viewport={'width': 1080, 'height': 810}, has_touch=True)
        self.section(page, 'fazit')
        root = page.locator('#own-work-summary')
        expect(root).to_contain_text('Freie Antworten werden nicht automatisch bewertet.')
        for key in ('calculation', 'dg', 'measurement', 'conclusion'):
            expect(page.locator('#own-work-' + key + '-status')).to_have_text('Noch offen')
        self.check('Empty presence summary reports four open own-work fields, never a grade', True)
        # These are actual UI links, including keyboard activation and real focus.
        page.locator('#own-work-calculation-link').focus(); page.keyboard.press('Enter')
        expect(page.locator('#course-task-answer')).to_be_focused()
        page.go_back()
        expect(page.locator('#conclusion-title')).to_be_focused()
        page.go_forward()
        expect(page.locator('#article-rechnung')).to_be_focused()
        self.check('Browser history restores ordinary section focus, not stale presence-link input focus', True)
        self.route(page, 'lernen/fazit')
        expect(page.locator('#conclusion-title')).to_be_focused()
        page.locator('#own-work-conclusion-link').click()
        expect(page.locator('#conclusion-draft')).to_be_focused()
        page.locator('#nav-debroglie').click()
        expect(page.locator('#debroglie-article h1')).to_be_focused()
        page.locator('#course-nav').click()
        expect(page.locator('#conclusion-title')).to_be_focused()
        self.check('Both conclusion aliases and ordinary chapter navigation consume one-time input focus', True)
        page.locator('#own-work-calculation-link').click()
        expect(page.locator('#course-task-answer')).to_be_focused()
        page.locator('#course-numeric-solution').click()
        self.section(page, 'fazit')
        expect(page.locator('#own-work-calculation-status')).to_have_text('Noch offen')
        self.check('Opening a correct solution never counts as an own calculation', True)
        page.locator('#own-work-calculation-link').tap()
        expect(page.locator('#course-task-answer')).to_be_focused()
        page.locator('#course-task-answer').fill('999 pm')
        page.locator('#course-numeric-check').click()
        self.section(page, 'fazit')
        expect(page.locator('#own-work-calculation-status')).to_have_text('Teilweise eingetragen')
        page.locator('#own-work-calculation-link').click()
        page.locator('#article-notes').fill('QA: vollständig eingetragen, rechnerisch aber noch falsch.')
        self.section(page, 'fazit')
        expect(page.locator('#own-work-calculation-status')).to_have_text('Angaben vorhanden')
        expect(page.locator('#own-work-calculation-details')).to_contain_text('Zahlenprüfung:')
        self.check('Complete but wrong work remains present with its numeric failure visible',
                   self.snapshot(page)['work']['S06_CALCULATION']['numericAnswer'] == '999 pm')
        page.locator('#own-work-calculation-link').click()
        page.locator('#course-task-answer').fill('100,137 pm')
        page.locator('#course-numeric-check').click()
        self.section(page, 'fazit')
        expect(page.locator('#own-work-calculation-status')).to_have_text('Angaben vorhanden')
        expect(page.locator('#own-work-calculation-details')).not_to_contain_text('Zahlenprüfung:')
        self.check('Corrected value changes numeric feedback but not the presence meaning', True)
        page.locator('#own-work-dg-link').focus(); page.keyboard.press('Enter')
        expect(page.locator('#dg-prediction-input')).to_be_focused()
        page.locator('#dg-prediction-input').fill(OWN_DG['prediction'])
        self.section(page, 'fazit')
        expect(page.locator('#own-work-dg-status')).to_have_text('Teilweise eingetragen')
        page.locator('#own-work-dg-link').click()
        page.locator('#dg-geometry-input').fill(OWN_DG['numericAnswer'])
        self.route(page, 'davisson-germer/einordnen')
        page.locator('#dg-conclusion-note').fill(OWN_DG['note'])
        self.section(page, 'fazit')
        expect(page.locator('#own-work-dg-status')).to_have_text('Angaben vorhanden')
        page.locator('#own-work-measurement-link').tap()
        expect(page.locator('#jonsson-interactive-image')).to_be_focused()
        self.open_marker_coordinates(page)
        page.locator('#marker-new-x').fill('551'); page.locator('#marker-add').click()
        self.section(page, 'fazit')
        expect(page.locator('#own-work-measurement-status')).to_have_text('Teilweise eingetragen')
        expect(page.locator('#own-work-measurement-details')).to_contain_text('Eine zweite Marke')
        page.locator('#own-work-measurement-link').click()
        self.open_marker_coordinates(page)
        page.locator('#marker-new-x').fill('578'); page.locator('#marker-add').click()
        self.section(page, 'fazit')
        expect(page.locator('#own-work-measurement-details')).to_contain_text('Begründung zum aktuellen Band')
        page.locator('#own-work-measurement-link').click()
        expect(page.locator('#jonsson-justification')).to_be_focused()
        self.check('An existing pair of marks with a missing reason leads directly to the reason field', True)
        page.locator('#jonsson-justification').fill(OWN_REASON)
        self.section(page, 'fazit')
        expect(page.locator('#own-work-measurement-status')).to_have_text('Angaben vorhanden')
        expect(page.locator('#own-work-measurement-details')).to_contain_text('Auswahl ist ein Entwurf')
        self.check('Two own markers suffice; an unsealed first choice has an explicit independent notice', True)
        page.locator('#own-work-measurement-link').click()
        page.locator('#measurement-profile').select_option('oben')
        self.section(page, 'fazit')
        expect(page.locator('#own-work-measurement-status')).to_have_text('Teilweise eingetragen')
        expect(page.locator('#own-work-measurement-details')).to_contain_text('früheren Band')
        page.locator('#own-work-measurement-link').click()
        expect(page.locator('#jonsson-justification')).to_be_focused()
        page.locator('#measurement-profile').select_option('basis')
        self.section(page, 'fazit')
        page.locator('#own-work-conclusion-link').focus(); page.keyboard.press('Enter')
        expect(page.locator('#conclusion-draft')).to_be_focused()
        page.locator('#conclusion-draft').fill(OWN_CONCLUSION)
        expect(page.locator('#own-work-conclusion-status')).to_have_text('Angaben vorhanden')
        before = self.snapshot(page)
        root.scroll_into_view_if_needed(); self.shot(page, 'own-work-presence-ipad.png')
        report = self.read_report(page, 'presence-personal-report.txt')
        self.check('Presence and downloaded report preserve the same actual own entries',
                   all(value in report for value in (OWN_CONCLUSION, OWN_REASON, OWN_DG['prediction']))
                   and self.snapshot(page) == before)
        self.check('An unsealed current measurement is also present without the empty-measurement message',
                   before['measurement']['initialSelection'] is None
                   and before['measurement']['comparison'] is None and EMPTY_MEASUREMENT not in report)
        reload_with_images(page)
        for key in ('calculation', 'dg', 'measurement', 'conclusion'):
            expect(page.locator('#own-work-' + key + '-status')).to_have_text('Angaben vorhanden')
        self.check('Presence derives again after reload without altering personal work', self.snapshot(page) == before)
        saved = page.evaluate('key => localStorage.getItem(key)', base.STORAGE)
        second = page.context.new_page(); self.pages.append(second); self.current = second; self.observe(second)
        second.goto(self.url + '#fazit', wait_until='networkidle')
        expect(second.locator('#own-work-conclusion-status')).to_have_text('Angaben vorhanden')
        expect(second.locator('#conclusion-draft')).to_be_disabled()
        expect(second.locator('#conclusion-draft')).to_have_value(OWN_CONCLUSION)
        self.open_storage(second)
        second.locator('#session-status').get_by_text('Kopiertext ansehen', exact=True).click()
        expect(second.locator('#session-work-preview')).to_contain_text(OWN_CONCLUSION)
        work_copy = second.locator('#session-work-preview').text_content()
        self.check('Device copy shares loaded report bibliography and image verification',
                   report in work_copy and 'TECHNISCHER ANHANG' not in work_copy)
        expect(second.locator('#course-export,#course-import-file')).to_have_count(0)
        self.check('Read-only second-tab presence retains the owner work and exposes a readable copy',
                   self.snapshot(second) == before
                   and page.evaluate('key => localStorage.getItem(key)', base.STORAGE) == saved)
        second.close(); self.current = page
        # A 200 malformed scientific-data response is a real loader failure without
        # browser transport errors. Stored own work remains valid and exportable.
        page.route('**/versuchsdaten.json', lambda route: route.fulfill(
            status=200, content_type='application/json', body='{QA-invalid-json'))
        reload_with_images(page)
        expect(page.locator('#own-work-measurement-status')).to_have_text('Teilweise eingetragen')
        expect(page.locator('#own-work-measurement-details')).to_contain_text('Bildzuordnung ist noch ungeprüft')
        expect(page.locator('#conclusion-draft')).to_have_value(OWN_CONCLUSION)
        self.open_storage(page)
        page.locator('#session-status').get_by_text('Kopiertext ansehen', exact=True).click()
        copied_text = page.locator('#session-work-preview').text_content()
        self.check('Unavailable source data honestly omits current image verification and keeps bibliography',
                   'kein Abgleich mit einer aktuell geladenen Datei' in copied_text
                   and 'Diffraction of Electrons by a Crystal of Nickel' in copied_text)
        self.check('Missing source data labels identity unchecked but preserves a readable copy of all own work',
                   self.snapshot(page) == before and all(value in copied_text for value in
                   (OWN_CONCLUSION, OWN_REASON, OWN_DG['prediction'], OWN_DG['numericAnswer'], OWN_DG['note'])))
        self.shot(page, 'own-work-data-unavailable.png')
        # Valid source JSON can intentionally declare that no historical image is
        # available. This is Ready data, distinct from a failed repository load.
        page.unroute('**/versuchsdaten.json')
        no_image = page.request.get(urljoin(self.url, 'versuchsdaten.json')).json()
        no_image['jonsson']['image']['available'] = False
        page.route('**/versuchsdaten.json', lambda route: route.fulfill(
            status=200, content_type='application/json', body=json.dumps(no_image)))
        reload_with_images(page)
        # Explicit source withdrawal follows the existing reconciliation contract:
        # unlike a load failure, it clears incompatible raster work and records why.
        expect(page.locator('#own-work-measurement-status')).to_have_text('Noch offen')
        withdrawn = self.snapshot(page)
        self.check('Explicitly unavailable source preserves the existing image reconciliation contract',
                   withdrawn['imageIdentity'] is None
                   and withdrawn['measurement']['markers'] == []
                   and withdrawn['measurementResetReason'] == 'IMAGE_UNAVAILABLE'
                   and withdrawn['work'] == before['work'])
        page.locator('#own-work-measurement-link').click()
        expect(page.locator('#joensson-messung')).to_be_focused()
        expect(page.locator('#jonsson-native-image')).to_have_count(0)
        self.check('Valid data without a historical image focuses the available measurement explanation', True)
        self.route(page, 'davisson-germer/rechnen')
        expect(page.locator('#dg-prediction-input')).to_be_visible()
        self.section(page, 'fazit')
        expect(page.locator('#conclusion-title')).to_be_focused()
        self.check('No-image fallback leaves other science and ordinary navigation usable', self.snapshot(page) == withdrawn)
        self.shot(page, 'own-work-ready-without-image.png')

    def request_failed_handout(self, page, name):
        self.section(page, 'fazit')
        href = page.locator('#handout-download').get_attribute('href')
        target = urljoin(page.url, href)
        expected_console = []
        def fixture_console(message):
            if message.location.get('url') == target and '503' in message.text:
                expected_console.append({'type': message.type, 'text': message.text})
        page.on('console', fixture_console)
        HandoutFailure.requests.clear()
        HandoutFailure.active = True
        page.locator('#handout-download').click()
        page.wait_for_function('key => JSON.parse(localStorage.getItem(key)).measurement.referenceMaterialsOpened.includes("HANDOUT_REQUESTED")', arg=base.STORAGE)
        # Browser download responses may not surface as page Response events. The
        # actual server request and returned 503 are the fixture evidence.
        for _ in range(20):
            if HandoutFailure.requests:
                break
            page.wait_for_timeout(50)
        served = list(HandoutFailure.requests)
        HandoutFailure.active = False
        self.report.setdefault('handout_request_observations', []).append({'name': name, 'target': target, 'served_503_paths': served})
        self.check(name + ': real click requested the deliberately unavailable PDF', served == [urlsplit(target).path])
        expect(page.locator('#conclusion-article')).to_contain_text('Ob die PDF heruntergeladen oder gelesen wurde, wird nicht erfasst.')
        self.check(name + ': failed download records request without opening laboratory reference', not self.snapshot(page)['measurement']['referenceViewed'])
        self.report.setdefault('expected_http_fixtures', []).append({'url': target, 'status': 503, 'purpose': name})
        # Only diagnostics belonging to the intentionally failed PDF are expected;
        # unrelated failures still fail the shared harness at the end.
        for key in ('http_errors', 'request_failures'):
            self.report.setdefault('expected_' + key, []).extend(item for item in self.report[key] if item['url'] == target)
            self.report[key] = [item for item in self.report[key] if item['url'] != target]
        for item in expected_console:
            if item in self.report['console_messages']:
                self.report['console_messages'].remove(item)
                self.report.setdefault('expected_console_messages', []).append(dict(item, url=target))
        page.remove_listener('console', fixture_console)

    def handout_request_provenance(self):
        for timing in ('before-first-selection', 'after-first-selection'):
            page = self.prepared_work(timing)
            before = self.snapshot(page)['measurement']
            self.check(timing + ': no synthetic prior handout contact', 'HANDOUT_REQUESTED' not in before['referenceMaterialsOpened'])
            if timing == 'after-first-selection':
                page.locator('#jonsson-seal-selection').click()
            preserved = self.snapshot(page)['measurement']['initialSelection']
            self.request_failed_handout(page, timing)
            after = self.snapshot(page)['measurement']
            self.check(timing + ': request does not rewrite an existing first selection', after['initialSelection'] == preserved)
            if timing == 'before-first-selection':
                self.route(page, 'joensson/messung')
                page.locator('#jonsson-seal-selection').click()
                first = self.snapshot(page)['measurement']['initialSelection']
                self.check('Requested access before capture is included even when download failed', 'HANDOUT_REQUESTED' in first['priorReferenceMaterials'])
            else:
                self.check('Later access is not retroactively assigned to first choice', 'HANDOUT_REQUESTED' not in after['initialSelection']['priorReferenceMaterials'])
            report = self.read_report(page, timing + '-report.txt', include_technical=True)
            self.check(timing + ': report identifies request and limits of observation', 'Handout-Zugriff angefordert' in report and 'weder einen erfolgreichen Download noch Lesen oder Verständnis' in report and 'Direkte PDF-Aufrufe' in report)


if __name__ == '__main__':
    base.browser_base.served = served_with_failure_fixture
    base.main(scenarios=('personal_report', 'handout_request_provenance'), suite_type=ReportChecks, harness_path=Path(__file__))
