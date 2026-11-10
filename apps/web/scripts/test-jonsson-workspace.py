#!/usr/bin/env python3
"""Jönsson workspace regressions on the built app, not source snapshots.

Browser plugin not available: Playwright/system Chromium. iPad 9 viewport and
synthetic work only; this does not establish Safari or pupil learning quality.
"""
from pathlib import Path
import importlib.util
from playwright.sync_api import expect
from browser_theme import set_theme
from simulation_browser_checks import simulation_extensions

HERE = Path(__file__).resolve().parent
spec = importlib.util.spec_from_file_location('workspace_base', HERE / 'test-run-two.py')
two = importlib.util.module_from_spec(spec)
spec.loader.exec_module(two)


class WorkspaceChecks(two.Checks):
    def show_workspace(self, page, selector):
        # Position the complete work surface below the real sticky bar, not behind it.
        page.locator(selector).evaluate("e => { const header=document.querySelector('header').getBoundingClientRect(); window.scrollBy(0,e.getBoundingClientRect().top-header.bottom-16); }")
        page.wait_for_function("s => document.querySelector(s).getBoundingClientRect().top >= document.querySelector('header').getBoundingClientRect().bottom+10", arg=selector)

    def compact_model(self):
        for name, viewport, touch in [('laptop', {'width': 1440, 'height': 900}, False),
                                       ('ipad9', {'width': 1080, 'height': 810}, True),
                                       ('ipad-short', {'width': 1024, 'height': 700}, True)]:
            page = self.page(viewport=viewport, has_touch=touch)
            self.route(page, 'joensson')
            for theme in ('dark', 'light'):
                set_theme(page, theme)
                page.locator('#course-slit-save').click()
                aperture = page.locator('#slit-aperture svg').get_attribute('aria-label')
                page.locator('#course-model-g').fill('4')
                expect(page.locator('#course-model-g')).to_have_value('4')
                expect(page.locator('#slit-aperture svg')).not_to_have_attribute('aria-label', aperture)
                self.check('Real openings respond to the spacing control '+name+' '+theme, True)
                self.show_workspace(page, '#slit-workspace')
                boxes = {key: page.locator(selector).bounding_box() for key, selector in (
                    ('workspace', '#slit-workspace'), ('aperture', '#slit-aperture'),
                    ('profile', '#slit-profile'), ('controls', '#slit-controls'))}
                header = page.locator('header').first.bounding_box()
                self.report.setdefault('workspace_geometry', []).append({'device': name, 'theme': theme, **boxes})
                self.check('Aperture, full saved-comparison profile and controls fit together '+name+' '+theme,
                           boxes['workspace']['height'] < viewport['height'] - header['height'] - 24)
                self.check('Settings and plot are side by side '+name+' '+theme,
                           boxes['controls']['x'] >= boxes['profile']['x'] + boxes['profile']['width'])
                for selector in ('#course-model-g', '#course-model-b', '#course-slit-save'):
                    bounds = page.locator(selector).bounding_box()
                    self.check('Touch target remains at least 44 pixels '+selector+' '+name+' '+theme,
                               bounds['height'] >= 44)
                labels = page.locator('#slit-aperture [data-typst-label]').evaluate_all(
                    'nodes => nodes.map(e=>({text:e.getAttribute("aria-label"),height:e.getBoundingClientRect().height}))')
                self.check('Aperture labels remain readable at compact scale '+name+' '+theme,
                           bool(labels) and all(item['height'] >= 12 for item in labels))
                self.check('No page-level horizontal overflow '+name+' '+theme,
                           page.evaluate('document.documentElement.scrollWidth <= innerWidth+1'))
                self.shot(page, 'coupled-model-'+name+'-'+theme+'.png')
                changed_aperture = page.locator('#slit-aperture svg').get_attribute('aria-label')
                page.locator('#course-model-g').fill('2')
                expect(page.locator('#slit-aperture svg')).not_to_have_attribute('aria-label', changed_aperture)
            page.context.close()
        simulation_extensions(self)

    def live_label_colors(self, page, theme):
        colors = page.evaluate('() => {\n            const marker=document.querySelector(\'#jonsson-native-image [data-typst-label][aria-label="1"] > g\');\n            const line=document.querySelector(\'#jonsson-native-image [data-own-marker="0"]\');\n            const plate=document.querySelector(\'#jonsson-native-image .measurement-label-plate\');\n            const tick=document.querySelector(\'#jonsson-gray-profile [data-typst-label]\');\n            return {foreground:getComputedStyle(marker).color, line:getComputedStyle(line).stroke,\n                background:getComputedStyle(plate).fill,\n                tickForeground:getComputedStyle(tick.firstElementChild).color, tickFill:getComputedStyle(tick).fill};\n        }')
        self.check('Existing marker and profile glyphs follow the changed theme '+theme,
                   colors['foreground'] == colors['line'] and colors['tickForeground'] == colors['tickFill'])
        def luminance(css):
            import re
            rgb = list(map(float, re.findall(r'[\d.]+', css)))[:3]
            channels = [c / 255 for c in rgb]
            linear = [c / 12.92 if c <= .04045 else ((c + .055) / 1.055) ** 2.4 for c in channels]
            return sum(c * w for c, w in zip(linear, (.2126, .7152, .0722)))
        foreground, background = luminance(colors['foreground']), luminance(colors['background'])
        ratio = (max(foreground, background) + .05) / (min(foreground, background) + .05)
        self.report.setdefault('marker_label_contrast', []).append({'theme': theme, 'ratio': ratio, **colors})
        self.check('Marker glyph against its actual plate reaches4.5:1 '+theme, ratio >= 4.5)

    def result_is_in_view(self, page, selector, label):
        page.wait_for_function('s => { const e=document.querySelector(s).getBoundingClientRect(); const h=document.querySelector("header").getBoundingClientRect(); return e.top >= h.bottom+10 && e.top+Math.min(180,e.height) <= innerHeight-10; }', arg=selector)
        self.check(label + ' below the sticky header without a manual scroll', True)

    def first_selection(self):
        page = self.page(viewport={'width': 1080, 'height': 810}, has_touch=True,
                         permissions=['clipboard-read', 'clipboard-write'])
        self.route(page, 'joensson/messung')
        expect(page.locator('#joensson-messung')).to_contain_text('Markiere zunächst vier benachbarte helle Streifenmaxima.')
        expect(page.locator('#joensson-messung')).to_contain_text('Die vier Marken ergeben drei Abstände;')
        first = page.locator('#measurement-first-workspace')
        for selector in ('#jonsson-native-image', '#measurement-profile', '#jonsson-spacing',
                         '#jonsson-justification', '#jonsson-seal-selection'):
            expect(first.locator(selector)).to_have_count(1)
        self.check('First workspace contains image, band, own result, reason and save', True)
        for selector in ('#jonsson-show-reference', '#jonsson-compare-save'):
            expect(first.locator(selector)).to_have_count(0)
            expect(page.locator('#measurement-checks').locator(selector)).to_have_count(1)
            expect(page.locator(selector)).to_be_disabled()
        self.check('Reference and comparison management come after the own selection', True)
        expect(page.locator('#jonsson-export-json,#jonsson-export-text')).to_have_count(0)
        expect(page.locator('#measurement-protocol-copy')).to_have_count(1)
        expect(page.locator('#measurement-case-a-card,#measurement-case-b-card')).to_have_count(0)
        self.check('Empty comparison stays compact and measurement uses readable copy instead of files', True)
        expect(page.locator('#measurement-optional-comparison')).not_to_have_attribute('open', '')
        expect(page.locator('#jonsson-compare-save')).not_to_be_visible()
        self.check('A/B is explicitly optional and closed before the first own selection', True)
        for viewport in ({'width': 1080, 'height': 810}, {'width': 1024, 'height': 700}):
            page.set_viewport_size(viewport)
            self.show_workspace(page, '#measurement-first-workspace')
            photo = page.locator('#jonsson-image-window').bounding_box()
            profile = page.locator('#jonsson-gray-profile').bounding_box()
            reason = page.locator('#jonsson-justification').bounding_box()
            self.check('Photo and profile share the left column, reason is alongside '+str(viewport['width']),
                       abs(photo['x'] - profile['x']) < 3
                       and profile['y'] >= photo['y'] + photo['height']
                       and reason['x'] >= photo['x'] + photo['width'])
            self.check('Reason field is visible with the photograph in landscape '+str(viewport['width']),
                       reason['y'] + reason['height'] <= viewport['height'] - 8)
            self.check('Measurement workspace does not overflow the page '+str(viewport['width']),
                       page.evaluate('document.documentElement.scrollWidth <= innerWidth+1'))
            self.shot(page, 'compact-measurement-'+str(viewport['width'])+'.png')
        page.set_viewport_size({'width': 1080, 'height': 810})
        self.show_workspace(page, '#measurement-first-workspace')
        self.shot(page, 'first-measurement-ipad9-dark.png')
        expect(page.locator('#measurement-coordinate-tools')).not_to_have_attribute('open', '')
        expect(page.locator('#marker-new-x')).not_to_be_visible()
        self.check('First measurement hides optional numerical precision controls', True)
        # Synthetic candidate coordinates exercise the contract; they are not certified maxima.
        for count, x in enumerate((551, 565, 580, 595), 1):
            self.open_marker_coordinates(page)
            page.locator('#marker-new-x').fill(str(x)); page.locator('#marker-add').click()
            expect(page.locator('[data-own-marker]')).to_have_count(count)
        accepted = self.snapshot(page)['measurement']['markers']
        page.locator('#marker-new-x').fill('551')
        expect(page.locator('#marker-add')).to_be_disabled()
        expect(page.locator('#measurement-new-coordinate-status')).to_contain_text('liegt bereits eine Marke')
        expect(page.locator('#marker-new-x')).to_have_attribute('aria-invalid', 'true')
        page.locator('#marker-new-x').fill('703')
        expect(page.locator('#measurement-new-coordinate-status')).to_contain_text('von 490 bis 702')
        self.check('Rejected new coordinates explain the reason without changing accepted markers',
                   self.snapshot(page)['measurement']['markers'] == accepted)
        page.locator('#marker-new-x').fill('')
        expect(page.locator('#jonsson-count')).to_contain_text('4 Marken → 3 Abstände')
        self.show_workspace(page, '#measurement-first-workspace')
        image_box = page.locator('#jonsson-image-window').bounding_box()
        nudge_box = page.locator('#marker-plus').bounding_box()
        self.check('Selected marker nudge is beside the photo rather than below the workspace',
                   nudge_box['y'] < image_box['y'] + image_box['height']
                   and nudge_box['y'] + nudge_box['height'] > image_box['y'])
        self.check('Marker action labels fit inside their touch targets',
                   page.locator('#marker-minus,#marker-plus,#marker-remove').evaluate_all(
                       'buttons => buttons.every(e => e.scrollWidth <= e.clientWidth + 1 && e.getBoundingClientRect().height >= 44)'))
        expect(page.locator('#measurement-detail-disclosure')).not_to_have_attribute('open', '')
        page.locator('#measurement-detail-disclosure > summary').click()
        expect(page.locator('#measurement-native-detail')).to_be_visible()
        page.locator('#measurement-detail-disclosure > summary').click()
        page.locator('#jonsson-justification').fill('QA: vier selbst gesetzte Kandidaten; Maximalzuordnung hier nicht bewertet.')
        page.locator('#jonsson-seal-selection').click()
        sealed = self.snapshot(page)['measurement']['initialSelection']
        page.locator('#measurement-protocol-copy').click()
        expect(page.locator('#measurement-protocol-copy-status')).to_have_text('Messprotokoll kopiert.')
        copied = page.evaluate('navigator.clipboard.readText()')
        self.check('Copied protocol contains native candidates, calculated intervals and own reason',
                   'Marken x / Originalpixel: 551, 565, 580, 595' in copied
                   and 'Abstände / Originalpixel: 14, 15, 15' in copied
                   and 'QA: vier selbst gesetzte Kandidaten; Maximalzuordnung hier nicht bewertet.' in copied)
        self.check('Copied protocol preserves the measurement limitation',
                   'Grauwert ist keine kalibrierte Elektronenintensität.' in copied
                   and 'keine absolute Wellenlänge aus Pixeln' in copied)
        page.locator('#image-zoom').fill('2')
        self.check('Zoom does not alter the first saved native measurement',
                   self.snapshot(page)['measurement']['initialSelection'] == sealed)
        page.locator('#measurement-countercheck > summary').click()
        expect(page.locator('#measurement-countercheck')).to_contain_text('doppelt')
        self.check('Counterexample is explanatory and changes no own answer',
                   self.snapshot(page)['measurement']['initialSelection'] == sealed)
        # Explicit compare actions must reveal their actual result, not merely mutate
        # scientific state elsewhere on this long reading page.
        work_before_compare = self.snapshot(page)['measurement']
        self.open_disclosures(page, '#jonsson-compare-save')
        page.locator('#jonsson-compare-save').focus()
        page.keyboard.press('Enter')
        expect(page.locator('#measurement-comparison-view')).to_be_focused()
        expect(page.locator('#measurement-comparison-saved')).to_contain_text('A gespeichert: 4 Marken')
        self.result_is_in_view(page, '#measurement-comparison-view', 'Saving A reveals its comparison')
        saved_comparison = self.snapshot(page)['measurement']['comparison']
        self.check('Saving comparison does not replace own marks or first selection',
                   saved_comparison['markers'] == work_before_compare['markers']
                   and self.snapshot(page)['measurement']['markers'] == work_before_compare['markers']
                   and self.snapshot(page)['measurement']['initialSelection'] == sealed)
        expect(page.locator('#measurement-case-a-card')).to_be_visible()
        expect(page.locator('#measurement-case-b-card')).to_be_visible()
        self.shot(page, 'comparison-saved-visible-ipad9.png')
        page.locator('#measurement-comparison-edit').click()
        expect(page.locator('#jonsson-interactive-image')).to_be_focused()
        self.result_is_in_view(page, '#jonsson-image-window', 'Comparison returns directly to the image')
        self.open_disclosures(page, '#show-markers')
        page.locator('#show-markers').uncheck()
        page.locator('#jonsson-show-reference').tap()
        expect(page.locator('#measurement-reference-result')).to_be_focused()
        self.result_is_in_view(page, '#measurement-reference-result', 'Reference action reveals its own numeric comparison')
        expect(page.locator('#measurement-reference-marks')).to_contain_text('551, 564, 578, 592, 606, 620, 634, 647')
        expect(page.locator('#measurement-reference-result')).to_contain_text('keine historische Sollvorgabe')
        expect(page.locator('#show-markers')).to_be_checked()
        expect(page.locator('[data-reference-marker]')).to_have_count(8)
        after_reference = self.snapshot(page)['measurement']
        self.check('Reference is a visible comparison, not a replacement of the learner measurement',
                   after_reference['markers'] == work_before_compare['markers']
                   and after_reference['initialSelection'] == sealed
                   and after_reference['comparison'] == saved_comparison
                   and after_reference['referenceViewed'] is True)
        self.shot(page, 'reference-comparison-visible-ipad9.png')
        page.locator('#measurement-reference-image').click()
        expect(page.locator('#jonsson-interactive-image')).to_be_focused()
        self.result_is_in_view(page, '#jonsson-image-window', 'Reference has an explicit image jump')
        expect(page.locator('#image-zoom')).to_have_value('2')
        page.locator('#measurement-reference-return').click()
        expect(page.locator('#measurement-reference-result')).to_be_focused()
        self.result_is_in_view(page, '#measurement-reference-result', 'Image returns to the numeric reference result')
        page.locator('#jonsson-show-reference').focus(); page.keyboard.press('Enter')
        expect(page.locator('#measurement-reference-result')).to_be_focused()
        self.check('Already revealed reference can be revisited by keyboard', True)
        self.open_disclosures(page, '#jonsson-compare-clear')
        page.locator('#jonsson-compare-clear').click()
        expect(page.locator('#measurement-case-a-card')).to_have_count(0)
        self.check('Clearing A retains the first selection and reference exposure',
                   self.snapshot(page)['measurement']['initialSelection'] == sealed
                   and self.snapshot(page)['measurement']['referenceViewed'] is True)
        self.open_disclosures(page, '#marker-reset')
        page.locator('#marker-reset').click()
        for x in (552, 580):
            self.open_marker_coordinates(page)
            page.locator('#marker-new-x').fill(str(x)); page.locator('#marker-add').click()
        page.locator('#jonsson-justification').fill('QA: zwei Kandidaten ergeben einen Abstand.')
        expect(page.locator('#jonsson-compare-save')).to_be_enabled()
        self.check('Four-mark instruction is not a new validation gate', True)
        page.locator('#measurement-profile').select_option('oben')
        expect(page.locator('#measurement-justification-status')).to_be_visible()
        expect(page.locator('#measurement-justification-status')).to_contain_text('Band „Oberes Band“')
        expect(page.locator('#jonsson-compare-save')).to_be_disabled()
        self.check('Band change still invalidates the old justification association',
                   self.snapshot(page)['measurement']['initialSelection'] == sealed)
        set_theme(page, 'light')
        self.live_label_colors(page, 'light')
        set_theme(page, 'dark')
        self.live_label_colors(page, 'dark')
        set_theme(page, 'light')
        self.show_workspace(page, '#measurement-first-workspace')
        self.shot(page, 'first-measurement-ipad9-light.png')
        for viewport in ({'width': 1024, 'height': 700}, {'width': 1080, 'height': 810},
                         {'width': 1280, 'height': 800}, {'width': 1440, 'height': 900}):
            page.set_viewport_size(viewport)
            for theme in ('dark', 'light'):
                set_theme(page, theme)
                self.show_workspace(page, '#jonsson-gray-profile')
                plot = page.locator('#jonsson-gray-profile').bounding_box()
                profile_labels = page.locator('#jonsson-gray-profile [data-typst-label]').evaluate_all(
                    'nodes => nodes.map(e => ({label:e.getAttribute("aria-label"),...e.getBoundingClientRect().toJSON()}))')
                self.report.setdefault('profile_label_geometry', []).append(
                    {'viewport': viewport, 'theme': theme, 'plot': plot, 'labels': profile_labels})
                label = str(viewport['width']) + ' ' + theme
                self.check('Grayscale tick glyphs remain at least 14 CSS pixels: ' + label,
                           len(profile_labels) == 10 and all(item['height'] >= 14 for item in profile_labels))
                self.check('Grayscale labels stay inside their SVG without overlapping: ' + label,
                           all(plot['x'] <= a['left'] and a['right'] <= plot['x'] + plot['width']
                               and plot['y'] <= a['top'] and a['bottom'] <= plot['y'] + plot['height']
                               for a in profile_labels)
                           and all(a['right'] <= b['left'] or b['right'] <= a['left']
                                   or a['bottom'] <= b['top'] or b['bottom'] <= a['top']
                                   for i, a in enumerate(profile_labels) for b in profile_labels[i + 1:]))
                self.shot(page, 'grayscale-profile-' + str(viewport['width']) + '-' + theme + '.png')

        self.comparison_guidance()

    def comparison_guidance(self):
        page = self.page(viewport={'width': 1080, 'height': 810}, has_touch=True)
        self.route(page, 'joensson/messung')
        # The keyboard route remains available without manipulating DOM visibility.
        summary = page.locator('#measurement-coordinate-tools > summary')
        summary.focus(); page.keyboard.press('Enter')
        expect(page.locator('#marker-new-x')).to_be_visible()
        for x in (550, 577, 604, 631):
            page.locator('#marker-new-x').fill(str(x)); page.locator('#marker-add').click()
        self.check('Four current marks calculate their actual 27 px mean',
                   float(page.locator('#jonsson-spacing').get_attribute('data-spacing-px')) == 27.0)
        expect(page.locator('#measurement-result-method')).to_contain_text('27,0')
        expect(page.locator('#measurement-intermediate-maxima')).to_have_count(0)
        self.check('Result explains actual endpoint calculation without leaking the hidden reference', True)
        page.locator('#jonsson-justification').fill('QA: grobe Testauswahl, Zwischenmaxima sind noch zu prüfen.')
        page.locator('#jonsson-seal-selection').click()
        before = self.snapshot(page)['measurement']
        page.locator('#jonsson-show-reference').click()
        expect(page.locator('#measurement-intermediate-maxima')).to_be_visible()
        expect(page.locator('#measurement-intermediate-maxima')).to_contain_text('zwischen zwei Marken')
        after = self.snapshot(page)['measurement']
        self.check('Doubled-spacing prompt follows explicit reference opening and keeps both own selections',
                   after['markers'] == before['markers'] and after['initialSelection'] == before['initialSelection'])
        self.shot(page, 'reference-check-intermediate-maxima-ipad9.png')
        page.locator('#measurement-reference-image').click()
        expect(page.locator('#jonsson-interactive-image')).to_be_focused()
        self.open_disclosures(page, '#marker-reset')
        page.locator('#marker-reset').click()
        self.open_marker_coordinates(page)
        for x in (551, 564, 578, 592):
            page.locator('#marker-new-x').fill(str(x)); page.locator('#marker-add').click()
        expect(page.locator('#measurement-intermediate-maxima')).to_have_count(0)
        self.check('A different current spacing removes the prompt without replacing the first selection',
                   self.snapshot(page)['measurement']['initialSelection'] == before['initialSelection'])


if __name__ == '__main__':
    two.main(scenarios=('compact_model', 'first_selection'), suite_type=WorkspaceChecks,
             harness_path=Path(__file__))
