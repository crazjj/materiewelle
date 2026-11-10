#!/usr/bin/env python3
"""Actual DG lesson flow. Browser plugin not available; Playwright/system Chromium.

Synthetic QA work only. Does not establish human learning or real Safari usability.
"""
from pathlib import Path
import importlib.util
import math
from playwright.sync_api import expect
from browser_assets import reload_with_images, settle_images, settle_fonts
from browser_theme import set_theme

HERE = Path(__file__).resolve().parent
spec = importlib.util.spec_from_file_location('dg_path_base', HERE / 'test-run-two.py')
base = importlib.util.module_from_spec(spec)
spec.loader.exec_module(base)
STEPS = [('versuch', 'dg-aufbau'), ('befund', 'dg-beobachtung'), ('interferenz', 'dg-bragg'),
         ('geometrie', 'dg-oberflaeche'), ('rechnen', 'dg-vergleich'), ('einordnen', 'dg-quellen')]


class LessonChecks(base.Checks):
    def go(self, page, key):
        self.route(page, 'davisson-germer/' + key)
        target = dict(STEPS)[key]
        expect(page.locator('#' + target)).to_be_focused()
        expect(page.locator('[data-dg-lesson-step]')).to_have_count(1)
        expect(page.locator('#' + target)).to_have_attribute('data-dg-lesson-step', key)

    def lesson(self):
        page = self.page()
        self.go(page, 'versuch')
        before = self.snapshot(page)
        expect(page.locator('#dg-lesson-next')).to_have_text('Nächster Bildschritt: Kristall')
        expect(page.locator('#dg-aufbau figure #dg-lesson-next')).to_be_visible()
        self.check('The setup action belongs to the drawing it changes', True)
        page.locator('#dg-lesson-next').click()
        expect(page.locator('#dg-lesson-next')).to_have_text('Nächster Bildschritt: Detektor')
        page.locator('#dg-lesson-next').click()
        expect(page.locator('#dg-aufbau')).to_be_visible()
        expect(page.locator('#dg-aufbau figure')).to_have_attribute('data-step', '2')
        expect(page.locator('#dg-aufbau')).to_contain_text('3 · Verschiedene Richtungen')
        self.check('Setup advances in the same drawing without writing scientific work', self.snapshot(page).get('work', {}) == before.get('work', {}))
        expect(page.locator('#dg-lesson-footer-next')).to_have_text('Weiter zum Abschnitt: Befund')
        page.locator('#dg-lesson-footer-next').click()
        expect(page.locator('#dg-beobachtung')).to_be_focused()
        expect(page.locator('#dg-observation-note')).to_have_value('')
        page.locator('#dg-observation-note').fill('QA: Richtungsabhängige Streuung; die Deutung ist noch zu prüfen.')
        page.locator('#dg-lesson-next').click()
        expect(page.locator('#dg-bragg')).to_be_focused()
        expect(page.locator('#dg-bragg .article-formula')).to_have_count(0)
        expect(page.locator('#dg-lesson-next')).to_have_text('Nächster Bildschritt: erster Zusatzweg')
        expect(page.locator('#dg-bragg figure #dg-lesson-next')).to_be_visible()
        self.check('The Bragg action belongs to the drawing it changes', True)
        page.locator('#dg-lesson-next').click()
        expect(page.locator('#dg-bragg .article-formula')).to_have_count(1)
        expect(page.locator('#dg-lesson-next')).to_have_text('Nächster Bildschritt: zweiter Zusatzweg')
        before_help = self.snapshot(page)
        sine_summary = page.get_by_text('Sinus im Dreieck auffrischen', exact=True)
        sine_help = sine_summary.locator('xpath=..')
        self.check('Trigonometry recall starts as an optional closed disclosure',
                   not sine_help.evaluate('e => e.open'))
        sine_summary.focus(); page.keyboard.press('Enter')
        sine_formula = sine_help.locator('img[src$="math/bragg-sine-dark.svg"]')
        expect(sine_formula).to_be_visible()
        settle_images(page)
        self.check('Optional sine explanation uses loaded Typst mathematics without changing own work',
                   sine_formula.evaluate('e => e.complete && e.naturalWidth > 0')
                   and self.snapshot(page) == before_help)
        symbol_sizes = sine_help.locator('.inline-formula-text-symbol img').evaluate_all('''images => images.map(image => ({
            loaded: image.complete && image.naturalWidth > 0,
            height: image.getBoundingClientRect().height,
            text: parseFloat(getComputedStyle(image.closest('p')).fontSize)
        }))''')
        self.check('Point and segment SVGs use prose dimensions rather than display-equation size',
                   len(symbol_sizes) == 5 and all(item['loaded'] and .8 <= item['height'] / item['text'] <= 1.05
                                                for item in symbol_sizes))
        sine_summary.scroll_into_view_if_needed()
        self.shot(page, 'dg-sine-recall.png')
        sine_summary.focus(); page.keyboard.press('Space')
        expect(sine_formula).not_to_be_visible()
        page.locator('#dg-lesson-next').click()
        expect(page.locator('#dg-bragg .article-formula')).to_have_count(2)
        page.locator('#dg-bragg-replay').click()
        expect(page.locator('#dg-bragg .article-formula')).to_have_count(0)
        self.check('Bragg replay preserves the furthest scientific geometry step', self.snapshot(page)['dg']['geometryStep'] == 2)
        page.locator('#dg-bragg-all').click()
        expect(page.locator('#dg-bragg .article-formula')).to_have_count(2)
        settle_images(page)
        page.wait_for_function('''() => {
            const figure = document.querySelector('#dg-bragg figure').getBoundingClientRect();
            const nav = document.querySelector('#dg-lesson-navigation').getBoundingClientRect();
            return figure.y >= nav.bottom - 2 && figure.bottom <= innerHeight;
        }''')
        drawing = page.locator('#dg-bragg figure').bounding_box()
        navigation = page.locator('#dg-lesson-navigation').bounding_box()
        self.report['bragg_visible_bounds'] = {'drawing': drawing, 'navigation': navigation, 'viewport': page.viewport_size}
        self.check('Bragg drawing stays visible beside the expanded explanation',
                   drawing['y'] >= navigation['y'] + navigation['height'] - 2
                   and drawing['y'] + drawing['height'] <= page.viewport_size['height'])
        self.shot(page, 'dg-path-bragg.png')
        expect(page.locator('#dg-lesson-footer-next')).to_have_text('Weiter zum Abschnitt: Geometrie')
        page.locator('#dg-lesson-footer-next').click()
        expect(page.locator('#dg-oberflaeche')).to_be_focused()
        expect(page.locator('#dg-oberflaeche figure svg').first).to_have_attribute('viewBox', '392 40 355 325')
        self.shot(page, 'dg-path-geometry.png')
        page.locator('#dg-lesson-next').click()
        expect(page.locator('#dg-vergleich')).to_be_focused()
        expect(page.locator('#dg-prediction-result,#dg-geometry-result')).to_have_count(0)
        expect(page.locator('#dg-prediction-input')).to_have_value('')
        expect(page.locator('#dg-geometry-input')).to_have_value('')
        page.locator('#dg-prediction-input').fill('QA: Eigener Rechenweg; etwa 0,167 nm.')
        page.locator('#dg-geometry-input').fill('0,165 nm')
        page.locator('#dg-show-prediction').click()
        expect(page.locator('#dg-prediction-result')).to_be_visible()
        expect(page.locator('#dg-geometry-result')).to_have_count(0)
        self.check('Two source-case controls are independently revealed', True)
        page.locator('#dg-show-geometry').click()
        predicted = base.nr_pm(54) / 1000
        geometric = .352 * math.sqrt(3/8) * math.sin(math.radians(50))
        self.check('Source-case prediction is actually calculated', abs(float(page.locator('#dg-prediction-result').get_attribute('data-value-nm')) - predicted) < 1e-10)
        self.check('Source-case geometry is actually calculated', abs(float(page.locator('#dg-geometry-result').get_attribute('data-value-nm')) - geometric) < 1e-10)
        self.check('Signed comparison retains the theory denominator', abs(float(page.locator('#dg-source-comparison').get_attribute('data-value-percent')) - (geometric-predicted)/predicted*100) < 1e-8)
        page.locator('#dg-vergleich').scroll_into_view_if_needed(); self.shot(page, 'dg-path-calculation.png')
        page.locator('#tutor-help-dg-comparison').click()
        expect(page.locator('#compact-tutor-own')).not_to_be_checked()
        page.locator('#compact-tutor-own').click()
        page.get_by_text('Vollständige Nachricht ansehen', exact=True).click()
        own = base.handoff(page)[1]['ownWork']
        self.check('Explicit readable tutor message contains both actual own source-case answers',
                   'Mein Vergleich des dokumentierten Davisson–Germer-Zahlenfalls:' in own and 'Mein Ergebnis:\n  0,165 nm' in own
                   and 'Meine Vorhersage oder mein Rechenansatz:\n  QA: Eigener Rechenweg; etwa 0,167 nm.' in own)
        page.locator('#compact-tutor-close').click()
        page.locator('#dg-lesson-next').click()
        expect(page.locator('#dg-conclusion-note')).to_have_value('')
        page.locator('#dg-conclusion-note').fill('QA: Zwei unabhängige Rechenwege stimmen näherungsweise überein; historische Unsicherheiten fehlen.')
        page.locator('#dg-lesson-next').click()
        expect(page.locator('#joensson-article')).to_be_visible()
        page.go_back(); expect(page.locator('#dg-quellen')).to_be_focused()
        reload_with_images(page)
        expect(page.locator('#dg-conclusion-note')).to_have_value('QA: Zwei unabhängige Rechenwege stimmen näherungsweise überein; historische Unsicherheiten fehlen.')
        self.go(page, 'rechnen')
        expect(page.locator('#dg-prediction-input')).to_have_value('QA: Eigener Rechenweg; etwa 0,167 nm.')
        expect(page.locator('#dg-geometry-input')).to_have_value('0,165 nm')
        self.check('Navigation, browser back and reload retain independent own work', True)

    def complete_reading(self):
        page = self.page()
        self.go(page, 'versuch')
        before = self.snapshot(page)
        expect(page.locator('#dg-aufbau h3')).to_have_count(1)
        expect(page.locator('#dg-setup-next')).to_have_count(0)
        page.locator('#dg-lesson-all').click()
        expect(page.locator('#dg-aufbau h3')).to_have_count(3)
        for explanation in ['1 · Elektronen beschleunigen', '2 · Auf eine regelmäßige Struktur treffen', '3 · Verschiedene Richtungen untersuchen']:
            expect(page.locator('#dg-aufbau')).to_contain_text(explanation)
        expect(page.locator('#dg-bragg .article-formula')).to_have_count(2)
        expect(page.locator('#dg-bragg')).to_contain_text('zweite, gleich lange Strecke')
        expect(page.locator('#dg-bragg')).to_contain_text('Bragg-Bedingung')
        expect(page.locator('#dg-prediction-result,#dg-geometry-result')).to_have_count(0)
        expect(page.locator('#dg-lesson-next,#dg-lesson-footer-next,#dg-bragg-next')).to_have_count(0)
        after = self.snapshot(page)
        self.check('Complete reading exposes every setup explanation and both paths, without help or answer writes',
                   after.get('work', {}) == before.get('work', {}) and after.get('dg', {}) == before.get('dg', {}))
        self.shot(page, 'dg-complete-reading.png')
        page.locator('#dg-lesson-all').click()
        expect(page.locator('#dg-aufbau figure')).to_have_attribute('data-step', '0')
        expect(page.locator('#dg-aufbau h3')).to_have_count(1)
        self.go(page, 'interferenz')
        expect(page.locator('#dg-bragg .article-formula')).to_have_count(0)
        self.check('Returning from complete reading preserves the unfinished guided stages', True)
        self.go(page, 'rechnen')
        wrong_reason = 'QA: 0,167 nm, weil die Wellenlänge mit der Spannung wächst.'
        page.locator('#dg-prediction-input').fill(wrong_reason)
        expect(page.locator('#dg-show-prediction')).to_have_text('Ergebnis anzeigen')
        expect(page.locator('#dg-vergleich')).to_contain_text('Die Website bewertet deinen eingegebenen Rechenweg nicht automatisch.')
        page.locator('#dg-show-prediction').click()
        expect(page.locator('#dg-prediction-result')).to_be_visible()
        expect(page.locator('#dg-prediction-input')).to_have_value(wrong_reason)
        self.check('Opening a correct control value never corrects or grades a wrong free explanation',
                   self.snapshot(page)['work']['S11_DG_EVALUATION']['prediction'] == wrong_reason)

    def views(self):
        page = self.page()
        self.seed(page)
        self.go(page, 'rechnen')
        original = self.snapshot(page)
        expect(page.locator('#dg-geometry-input')).to_have_value(original['work']['S11_DG_EVALUATION'].get('numericAnswer', ''))
        page.locator('#dg-show-prediction').click(); page.locator('#dg-show-geometry').click()
        fixed = page.locator('#dg-geometry-result').get_attribute('data-value-nm')
        page.locator('#dg-model-variation > summary').click()
        page.locator('#dg-article-angle').fill('40')
        expect(page.locator('#dg-article-angle')).to_have_value('40')
        self.check('Model variation never rewrites the fixed source-case calculation', page.locator('#dg-geometry-result').get_attribute('data-value-nm') == fixed)
        self.go(page, 'geometrie'); self.go(page, 'rechnen')
        expect(page.locator('#dg-geometry-result')).to_be_visible()
        page.locator('#dg-model-variation > summary').click()
        expect(page.locator('#dg-article-angle')).to_have_value('40')
        page.locator('#dg-lesson-all').click()
        expect(page.locator('[data-dg-lesson-step]')).to_have_count(6)
        ids = page.locator('[id]').evaluate_all('es=>es.map(e=>e.id)')
        self.check('Whole chapter contains no duplicate element identities', len(ids) == len(set(ids)))
        self.check('Changing presentation preserves all imported work fields', {k: v['note'] for k, v in original['work'].items()} == {k: v['note'] for k, v in self.snapshot(page)['work'].items()})
        page.locator('#dg-lesson-all').click()
        expect(page.locator('[data-dg-lesson-step]')).to_have_count(1)
        reload_with_images(page)
        expect(page.locator('#dg-geometry-result')).to_be_visible()
        page.locator('#dg-model-variation > summary').click()
        expect(page.locator('#dg-article-angle')).to_have_value('40')
        self.check('View controls and model input survive remount and actual reload', True)
        for fragment, target in [('lernen/dg-aufbau', 'dg-aufbau'), ('davisson-germer/beobachtung', 'dg-beobachtung'), ('lernen/beugungsgeometrie', 'dg-bragg'), ('davisson-germer/auswertung', 'dg-vergleich')]:
            self.route(page, fragment); expect(page.locator('#' + target)).to_be_focused()
        self.check('Existing station and public chapter links still reach their content', True)
        self.geometry_comparison()

    def geometry_comparison(self):
        page = self.page(viewport={'width': 1080, 'height': 810}, has_touch=True)
        self.seed(page)
        self.go(page, 'geometrie')
        before = self.snapshot(page)
        comparison = page.locator('#dg-geometry-comparison')
        comparison.locator('summary').focus(); page.keyboard.press('Enter')
        for theme in ('dark', 'light'):
            set_theme(page, theme)
            for key in ('reference', 'distance', 'path'):
                choice = page.locator('#dg-comparison-' + key)
                if choice.get_attribute('aria-pressed') != 'true':
                    choice.tap()
                expect(choice).to_have_attribute('aria-pressed', 'true')
                for model in ('bragg', 'surface'):
                    figure = page.locator('#dg-comparison-' + model)
                    expect(figure).to_be_visible()
                    expect(figure.locator('[data-typst-source]')).not_to_have_count(0)
                    expect(page.locator('#dg-comparison-' + model + '-highlight')).to_be_visible()
                    feature_asset = {'reference': 'angle', 'distance': 'spacing', 'path': 'path'}[key]
                    expect(page.locator('#dg-comparison-' + model + '-formula img')).to_have_attribute(
                        'src', 'math/' + model + '-focus-' + feature_asset + '-' + theme + '.svg')
                    bounds = figure.bounding_box()
                    self.check('Comparison figure fits tablet without shrinking document width: ' + theme + ' ' + key + ' ' + model,
                               bounds['x'] >= 0 and bounds['x'] + bounds['width'] <= page.viewport_size['width'] + 1)
                self.check('Both geometric models retain a persistent touch selection: ' + theme + ' ' + key, True)
            page.locator('#dg-comparison-reference').focus(); page.keyboard.press('Space')
            expect(page.locator('#dg-comparison-reference')).to_have_attribute('aria-pressed', 'true')
            self.check('Geometry focus is visible with keyboard: ' + theme,
                       page.locator('#dg-comparison-reference').evaluate('e => getComputedStyle(e).outlineStyle !== "none"'))
            comparison.scroll_into_view_if_needed(); self.shot(page, 'dg-comparison-' + theme + '.png')
        captions = {model: page.locator('#dg-comparison-' + model + ' figcaption [data-typst-source]').evaluate_all(
            'es => es.map(e => e.getAttribute("data-typst-source")).join(" ")') for model in ('bragg', 'surface')}
        self.check('Bragg and source-case angles retain independent 30- and 50-degree labels',
                   '30' in captions['bragg'] and '50' in captions['surface'])
        expect(page.locator('#dg-comparison-bragg figcaption')).to_contain_text('zur Netzebene')
        expect(page.locator('#dg-comparison-surface figcaption')).to_contain_text('zur äußeren Normalen')
        self.check('Looking at geometry changes no physical inputs or historical answers', self.snapshot(page) == before)
        page.locator('#dg-trial-start').tap()
        trial = page.locator('#dg-geometry-trial')
        expect(trial).to_be_visible()
        expect(page.locator('#dg-trial-title')).to_be_focused()
        expect(page.locator('#dg-comparison-bragg')).not_to_be_visible()
        expect(page.locator('#dg-comparison-surface')).not_to_be_visible()
        expect(page.locator('#dg-trial-feedback')).not_to_be_visible()
        expect(page.locator('#dg-trial-angle')).to_have_value('unanswered')
        expect(page.locator('#dg-trial-length')).to_have_value('unanswered')
        own = 'QA: Diese freie Begründung bleibt auch bei einer falschen Zuordnung unverändert.'
        page.locator('#dg-trial-note').fill(own)
        page.locator('#dg-trial-angle').select_option('plane')
        page.locator('#dg-trial-length').select_option('planes')
        page.locator('#dg-trial-compare').focus(); page.keyboard.press('Enter')
        expect(page.locator('#dg-trial-feedback')).to_contain_text('Beim Winkel korrigieren')
        expect(page.locator('#dg-trial-feedback')).to_contain_text('Bei der Länge korrigieren')
        expect(page.locator('#dg-trial-feedback')).to_contain_text('freie Begründung wird nicht automatisch bewertet')
        expect(page.locator('#dg-trial-note')).to_have_value(own)
        self.shot(page, 'dg-trial-wrong-assignment.png')
        page.locator('#dg-trial-angle').select_option('normal')
        page.locator('#dg-trial-length').select_option('rows')
        page.locator('#dg-trial-compare').tap()
        expect(page.locator('#dg-trial-feedback')).to_contain_text('Die Zuordnung passt zu diesem Oberflächenmodell.')
        expect(page.locator('#dg-trial-note')).to_have_value(own)
        self.check('Separate trial compares only explicit geometric choices and retains free reasoning', True)
        self.route(page, 'joensson'); self.go(page, 'geometrie')
        if not trial.is_visible():
            comparison.locator('summary').click()
        expect(page.locator('#dg-trial-note')).to_have_value(own)
        expect(page.locator('#dg-trial-angle')).to_have_value('normal')
        expect(page.locator('#dg-trial-length')).to_have_value('rows')
        self.check('Temporary trial survives chapter changes without scientific mutation', self.snapshot(page) == before)
        page.locator('#dg-trial-close').tap()
        expect(page.locator('#dg-trial-start')).to_be_focused()
        expect(page.locator('#dg-comparison-bragg')).to_be_visible()
        expect(page.locator('#dg-comparison-surface')).to_be_visible()
        page.locator('#dg-trial-start').tap()
        expect(page.locator('#dg-trial-note')).to_have_value(own)
        page.locator('#dg-trial-reset').tap()
        expect(page.locator('#dg-trial-note')).to_have_value('')
        expect(page.locator('#dg-trial-angle')).to_have_value('unanswered')
        page.locator('#dg-trial-note').fill(own)
        reload_with_images(page)
        comparison.locator('summary').click()
        page.locator('#dg-trial-start').click()
        expect(page.locator('#dg-trial-note')).to_have_value('')
        expect(page.locator('#dg-trial-angle')).to_have_value('unanswered')
        self.check('Explicit reset and reload clear only the temporary trial', self.snapshot(page) == before)
        page.locator('#dg-trial-close').click()
        page.evaluate("document.documentElement.style.fontSize='200%'")
        self.check('Comparison at 200% text enlargement has no document overflow',
                   page.evaluate('document.documentElement.scrollWidth<=innerWidth+1'))
        for model in ('bragg', 'surface'):
            expect(page.locator('#dg-comparison-' + model)).to_be_visible()
        self.shot(page, 'dg-comparison-enlarged-text.png')

    def surface_annotations(self):
        page = self.page(viewport={'width': 1080, 'height': 810})
        self.go(page, 'geometrie')
        page.get_by_text('Woher kommt der Reihenabstand?', exact=True).click()
        diagram = page.locator('svg[aria-label^="Ni111-Oberflächenmodell"]')
        expect(diagram).to_be_visible()
        for theme in ('dark', 'light'):
            set_theme(page, theme)
            settle_images(page)
            settle_fonts(page)
            for prefix in ('AC =', 'Erste Ordnung:'):
                label = diagram.locator('[data-typst-label][aria-label^="' + prefix + '"]')
                expect(label).to_have_count(1)
                self.check('Surface formula clauses have a visible punctuation separator: ' + theme + ' ' + prefix,
                           ';' in label.text_content())
                runs = label.locator('[data-typst-source]').evaluate_all(
                    'nodes => nodes.map(node => node.getAttribute("data-typst-source"))')
                self.check('Typeset surface clauses remain separate mathematical statements: ' + theme + ' ' + prefix,
                           bool(runs) and not any('λ' in run and 'd₁₁₁' in run for run in runs)
                           and not any('sin φ' in run and run.count('φ') > 1 for run in runs))
            clearance = diagram.evaluate('''svg => {
                const label = svg.querySelector('[data-typst-label][aria-label="gewählter Azimut"]');
                const bounds = label.getBoundingClientRect();
                const overlaps = node => {
                    const other = node.getBoundingClientRect();
                    return bounds.left < other.right + 2 && bounds.right > other.left - 2
                        && bounds.top < other.bottom + 2 && bounds.bottom > other.top - 2;
                };
                const points = [...svg.querySelectorAll('circle')];
                const rows = [...svg.querySelectorAll('line[stroke-dasharray]')];
                return {label: {x: bounds.x, y: bounds.y, width: bounds.width, height: bounds.height},
                    points: points.length, rows: rows.length,
                    pointCollisions: points.filter(overlaps).length,
                    rowCollisions: rows.filter(overlaps).length};
            }''')
            self.report.setdefault('dg_surface_annotation_clearance', []).append({'theme': theme, **clearance})
            self.check('Azimuth label clears the rendered lattice points and row lines: ' + theme,
                       clearance['points'] > 0 and clearance['rows'] > 0
                       and clearance['pointCollisions'] == 0 and clearance['rowCollisions'] == 0)

    def stable_controls(self):
        for width, height in [(1080, 810), (1024, 700)]:
            for motion in ('reduce', 'no-preference'):
                page = self.page(viewport={'width': width, 'height': height}, has_touch=True, reduced_motion=motion)
                for key, panel_id, step_ids, replay_id, next_key in [
                    ('versuch', 'dg-setup-explanation', ('dg-setup-source-text', 'dg-setup-crystal-text', 'dg-setup-detector-text'), 'dg-setup-replay', 'befund'),
                    ('interferenz', 'dg-bragg-explanation', ('dg-bragg-reference-text', 'dg-bragg-incoming-text', 'dg-bragg-outgoing-text'), 'dg-bragg-replay', 'geometrie'),
                ]:
                    self.go(page, key)
                    settle_images(page)
                    next_button = page.locator('#dg-lesson-next')
                    replay = page.locator('#' + replay_id)
                    panel = page.locator('#' + panel_id)
                    footer_next = page.locator('#dg-lesson-footer-next')
                    expect(replay).to_be_disabled()
                    expect(footer_next).to_have_count(0)
                    self.check(f'Initial explanation is fully laid out without a clipped inner scroll area {width}x{height} {motion} {key}',
                               panel.evaluate('''e => {
                                   const bounds=e.getBoundingClientRect();
                                   const last=e.querySelector('section:last-child > :last-child').getBoundingClientRect();
                                   return e.scrollHeight <= e.clientHeight+1 && getComputedStyle(e).overflowY === 'visible'
                                       && last.bottom <= bounds.bottom+1;
                               }'''))
                    if key == 'interferenz' and motion == 'no-preference':
                        self.shot(page, f'dg-bragg-initial-readable-{width}x{height}.png')
                    next_button.focus()
                    origin = next_button.bounding_box()
                    document_y = page.evaluate('scrollY')
                    original_text = page.locator('#' + step_ids[0]).inner_text()
                    samples = [origin]
                    for stage in (1, 2):
                        page.keyboard.press('Enter')
                        target = page.locator('#' + step_ids[stage])
                        expect(target).to_be_attached()
                        settle_images(page)
                        self.check(f'Every revealed paragraph fits the natural explanation height {width}x{height} {motion} {key} {stage}',
                                   panel.evaluate('''e => {
                                       const bounds=e.getBoundingClientRect();
                                       return e.scrollHeight <= e.clientHeight+1 && e.scrollTop === 0
                                           && Array.from(e.children).every(child => child.getBoundingClientRect().bottom <= bounds.bottom+1);
                                   }'''))
                        expect(next_button).to_be_focused()
                        position = next_button.bounding_box()
                        samples.append(position)
                        self.check(f'Next button remains at exactly the same drawing position {width}x{height} {motion} {key} {stage}',
                                   all(abs(position[prop] - origin[prop]) <= 1 for prop in ('x', 'y', 'width', 'height')))
                        self.check(f'Revealing the next picture step does not scroll the page or replace previous text {width}x{height} {motion} {key} {stage}',
                                   abs(page.evaluate('scrollY') - document_y) <= 1
                                   and page.locator('#' + step_ids[0]).inner_text() == original_text)
                        if stage == 1:
                            expect(footer_next).to_have_count(0)
                    expect(footer_next).to_have_count(1)
                    expect(next_button).to_have_count(1)
                    expect(footer_next).to_have_text(next_button.inner_text())
                    self.report.setdefault('dg_stable_controls', []).append({
                        'viewport': page.viewport_size, 'motion': motion, 'step': key, 'positions': samples,
                        'explanation_extent': panel.evaluate('e => ({height:e.clientHeight, content:e.scrollHeight, scrollTop:e.scrollTop})'),
                    })
                    self.check(f'Explanation remains a named region without an unnecessary scrolling focus stop {width} {motion} {key}',
                               panel.get_attribute('role') == 'region' and bool(panel.get_attribute('aria-label'))
                               and panel.get_attribute('tabindex') is None)
                    replay.tap()
                    expect(replay).to_be_disabled()
                    expect(footer_next).to_have_count(0)
                    expect(page.locator('#' + step_ids[1])).to_have_count(0)
                    expect(page.locator('#' + step_ids[0])).to_be_attached()
                    replay_position = next_button.bounding_box()
                    self.check(f'Replay keeps the control bar fixed {width} {motion} {key}',
                               all(abs(replay_position[prop] - origin[prop]) <= 1 for prop in ('x', 'y', 'width', 'height')))
                    next_button.tap(); next_button.tap()
                    expect(page.locator('#' + step_ids[2])).to_be_attached()
                    expect(footer_next).to_be_attached()
                    settle_images(page)
                    # A wheel over the explanation must scroll the document, not a concealed inner pane.
                    panel_bounds = panel.bounding_box()
                    page.mouse.move(panel_bounds['x'] + 20, panel_bounds['y'] + 20)
                    before_reading = page.evaluate('scrollY')
                    page.mouse.wheel(0, 240)
                    page.wait_for_function('before => scrollY > before+100', arg=before_reading)
                    self.check(f'Reading over the explanation uses ordinary document scrolling {width} {motion} {key}',
                               panel.evaluate('e => e.scrollTop') == 0)
                    navigation = page.locator('#dg-lesson-navigation').bounding_box()
                    drawing = page.locator('#' + dict(STEPS)[key] + ' figure').bounding_box()
                    controls = next_button.bounding_box()
                    self.check(f'Drawing and picture-step controls stay below navigation while reading {width} {motion} {key}',
                               drawing['y'] >= navigation['y'] + navigation['height'] - 1
                               and controls['y'] >= drawing['y']
                               and controls['y'] + controls['height'] <= height)
                    last_paragraph = page.locator('#' + step_ids[2] + ' > :last-child')
                    last_paragraph.scroll_into_view_if_needed()
                    expect(last_paragraph).to_be_in_viewport()
                    footer_next.scroll_into_view_if_needed()
                    expect(footer_next).to_be_in_viewport()
                    self.check(f'Next-section action follows all revealed explanation text {width} {motion} {key}',
                               footer_next.bounding_box()['y'] >= last_paragraph.bounding_box()['y'] + last_paragraph.bounding_box()['height']
                               and footer_next.bounding_box()['height'] >= 44)
                    self.shot(page, f'dg-reading-footer-{key}-{width}x{height}-{motion}.png')
                    footer_next.tap()
                    expect(page.locator('#' + dict(STEPS)[next_key])).to_be_focused()
                    self.check(f'Footer continuation reaches the next section by touch {width} {motion} {key}', True)
                self.check(f'Guided explanations do not create horizontal document overflow {width} {motion}',
                           page.evaluate('document.documentElement.scrollWidth <= innerWidth + 1'))

    def layouts(self):
        self.stable_controls()
        for width, height, touch in [(1440, 900, False), (1080, 810, True), (1024, 768, True), (1024, 700, True)]:
            page = self.page(viewport={'width': width, 'height': height}, has_touch=touch)
            for theme in ['dark', 'light']:
                set_theme(page, theme)
                for key, target in STEPS:
                    self.go(page, key)
                    self.check(f'No document overflow {width} {theme} {key}', page.evaluate('document.documentElement.scrollWidth<=innerWidth+1'))
                    bounds = page.locator('#' + target).bounding_box()
                    nav = page.locator('#dg-lesson-navigation').bounding_box()
                    header = page.locator('#site-navigation-header').bounding_box()
                    self.check(f'Chapter navigation clears the actual header including wrapped labels {width} {theme} {key}',
                               nav['y'] >= header['y'] + header['height'] - 1)
                    if key in ('versuch', 'befund', 'interferenz', 'geometrie'):
                        figure = page.locator('#' + target + ' figure').first.bounding_box()
                        self.check(f'Drawing remains below the chapter bar {width} {theme} {key}',
                                   figure['y'] >= nav['y'] + nav['height'] - 1)
                    self.check(f'Step begins below the visible chapter navigation {width} {theme} {key}', nav['y'] >= 0 and bounds['y'] >= nav['y'] + nav['height'] - 2)
                    self.check(f'Six touch-sized step links {width} {theme} {key}', page.locator('#dg-lesson-navigation a').evaluate_all('es=>es.length===6&&es.every(e=>e.getBoundingClientRect().height>=44)'))
                    self.check(f'Explicit next-action label fits a touch-sized button {width} {theme} {key}',
                               page.locator('#dg-lesson-next').evaluate('e => { const r=e.getBoundingClientRect(); return r.height>=44 && r.left>=0 && r.right<=innerWidth+1 && e.scrollWidth<=e.clientWidth+1; }'))
                    if key in ('versuch', 'befund', 'interferenz', 'geometrie'):
                        labels = page.locator('#' + target + ' figure').first.locator('[data-typst-label]').evaluate_all(
                            'nodes => nodes.map(n => {const m=n.getScreenCTM(); return {text:n.getAttribute("data-typst-source"),pixels:Number(n.getAttribute("font-size")) * Math.hypot(m.a,m.b)};})')
                        self.report.setdefault('dg_label_sizes', []).append({'viewport': page.viewport_size, 'theme': theme, 'step': key, 'labels': labels})
                        self.check(f'Drawing labels are at least 14 CSS pixels at {width}x{height} {theme} {key}',
                                   len(labels) > 0 and min(label['pixels'] for label in labels) >= 14)
                    if key == 'geometrie':
                        page.locator('#dg-geometry-comparison > summary').click()
                        bragg = page.locator('#dg-comparison-bragg .dg-comparison-drawing svg').first.bounding_box()
                        surface = page.locator('#dg-comparison-surface .dg-comparison-drawing svg').first.bounding_box()
                        self.check(f'Geometries remain side by side at {width} {theme}',
                                   bragg['x'] + bragg['width'] < surface['x'] and abs(bragg['y'] - surface['y']) < 3)
                        self.check(f'Comparison drawings have matching heights at {width} {theme}', abs(bragg['height'] - surface['height']) < 3)
                        labels = page.locator('#dg-geometry-comparison .dg-comparison-drawing [data-typst-label]').evaluate_all(
                            'nodes => nodes.map(n => {const m=n.getScreenCTM(); return Number(n.getAttribute("font-size")) * Math.hypot(m.a,m.b);})')
                        self.report.setdefault('dg_comparison_label_sizes', []).append({'viewport': page.viewport_size, 'theme': theme, 'minimum': min(labels), 'maximum': max(labels)})
                        self.check(f'Comparison labels remain readable at {width} {theme}', min(labels) >= 14)
                        grid_name = f'dg-comparison-grid-{width}x{height}-{theme}.png'
                        grid_path = self.output / grid_name
                        page.locator('.dg-comparison-figures').screenshot(path=str(grid_path))
                        self.report['screenshots'].append({'file': grid_name, 'sha256': base.browser_base.sha(grid_path.read_bytes()),
                            'viewport': page.viewport_size, 'url': page.url, 'theme': theme,
                            'scope': 'Both geometry figures, element screenshot', 'fixture': 'Synthetic QA inputs, not personal learner work'})
                        if theme == 'light': self.shot(page, f'dg-path-{width}x{height}-geometry-light.png')
                    if key == 'versuch' and theme == 'dark': self.shot(page, f'dg-path-{width}x{height}-setup-dark.png')
            self.go(page, 'versuch')
            link = page.locator('a[href="#davisson-germer/interferenz"]')
            link.focus(); page.keyboard.press('Enter'); expect(page.locator('#dg-bragg')).to_be_focused()
            page.locator('#dg-bragg-all').click()
            page.locator('#dg-lesson-next').tap() if touch else page.locator('#dg-lesson-next').click()
            expect(page.locator('#dg-oberflaeche')).to_be_focused()
            self.check(f'Keyboard and pointer navigation work on the same viewport {width}', True)


if __name__ == '__main__':
    base.main(scenarios=('lesson', 'complete_reading', 'views', 'surface_annotations', 'layouts'), suite_type=LessonChecks, harness_path=Path(__file__))
