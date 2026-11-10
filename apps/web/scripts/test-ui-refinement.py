#!/usr/bin/env python3
"""Additional actual-browser flows; reuses the existing artifact/console/download harness.

Browser plugin not available: Playwright/system Chromium. Synthetic local work;
tablet emulation does not establish real Safari usability or human learning.
"""
from pathlib import Path
import importlib.util
import json
import re
from playwright.sync_api import expect
from browser_assets import settle_images, reload_with_images
from browser_theme import set_theme

HERE = Path(__file__).resolve().parent
spec = importlib.util.spec_from_file_location('refinement_base', HERE / 'test-run-two.py')
two = importlib.util.module_from_spec(spec)
spec.loader.exec_module(two)


class RefinementChecks(two.Checks):
    def presentation(self):
        for device, viewport, touch in [('laptop', {'width': 1440, 'height': 900}, False),
                                        ('tablet', {'width': 1024, 'height': 768}, True)]:
            page = self.page(viewport=viewport, has_touch=touch)
            github = page.locator('#project-github')
            expect(github).to_have_attribute('href', 'https://github.com/crazjj/materiewelle')
            expect(github).to_have_accessible_name('Quelltext auf GitHub (neuer Tab)')
            expect(github.locator('svg path')).to_have_count(1)
            bounds = github.bounding_box()
            self.check('GitHub target fits header and touch: '+device,
                       bounds['width'] >= 44 and bounds['height'] >= 44 and
                       bounds['x'] + bounds['width'] <= viewport['width'])
            # Tab to the link so :focus-visible is exercised, not merely programmatic focus.
            page.locator('#theme-preference').focus(); page.keyboard.press('Tab')
            expect(github).to_be_focused()
            self.check('GitHub keyboard focus is visible: '+device,
                       github.evaluate('e => getComputedStyle(e).outlineStyle !== "none"'))
            self.route(page, 'joensson')
            for theme in ('dark', 'light'):
                if page.locator('html').get_attribute('data-theme') != theme:
                    page.locator('#theme-preference').click()
                diagram = page.locator('#course-optics-diagram')
                diagram.scroll_into_view_if_needed()
                expect(diagram).to_be_visible()
                bounds = diagram.bounding_box()
                self.check('Whole optics drawing fits the horizontal viewport: '+device+' '+theme,
                           bounds['x'] >= 0 and bounds['x'] + bounds['width'] <= viewport['width']+1)
                for letter in ('b', 'g'):
                    label = diagram.locator('[data-typst-label][aria-label="'+letter+'"]')
                    expect(label).to_have_count(1)
                    expect(label.locator('[data-typst-source]')).not_to_have_count(0)
                    box = label.bounding_box()
                    self.check('Dimension label is enlarged and separated from the ray field: '+letter+' '+device+' '+theme,
                               box['height'] >= 12 and box['y'] > bounds['y'] + bounds['height']*.63)
                    # Follow the actual glyph color after a theme switch, not only its parent style.
                    self.contrast(page, '#course-optics-diagram [aria-label="'+letter+'"] > g', theme)
                self.shot(page, 'jonsson-optics-'+device+'-'+theme+'.png')
                self.check('No horizontal document overflow: '+device+' '+theme,
                           page.evaluate('document.documentElement.scrollWidth <= innerWidth+1'))
            self.jonsson_setup_steps(page, device)
            page.get_by_role('button', name='3 · Elektronenoptische Abbildung', exact=True).click()
            expect(page.locator('#joensson-aufbau')).to_contain_text('nicht aus Glas')
            page.get_by_text('Was verändert eine Elektronenlinse – und was nicht?', exact=True).click()
            expect(page.locator('#joensson-aufbau')).to_contain_text('ohne Arbeit')
            self.route(page, 'joensson/messung')
            expect(page.locator('#joensson-messung')).to_contain_text('Historische Aufnahme aus Jönssons Veröffentlichung')
            self.check('Unclear first-decision jargon is absent', 'Erstentscheidung' not in page.locator('#joensson-article').inner_text())
            expect(page.locator('#joensson-messung')).to_contain_text('Hier: Doppelspaltausschnitt')
            self.shot(page, 'jonsson-image-'+device+'.png')
            page.get_by_text('Bildansicht und Bedienung', exact=True).click()
            page.get_by_role('button', name='Gesamtbild zeigen', exact=True).click()
            expect(page.locator('#joensson-messung')).to_contain_text('Gesamtbild: links Einspalt')
            page.get_by_role('button', name='Doppelspaltausschnitt zeigen', exact=True).click()
            expect(page.locator('#joensson-messung')).to_contain_text('Hier: Doppelspaltausschnitt')
            self.check('Historical caption follows the actual image viewport: '+device, True)

    def jonsson_setup_steps(self, page, device):
        setup = page.locator('#jonsson-setup')
        next_button = page.locator('#jonsson-setup-next')
        previous = page.locator('#jonsson-setup-previous')
        read = page.locator('#jonsson-setup-read-explanation')
        explanation = page.locator('#jonsson-setup-explanation')
        title = page.locator('#jonsson-setup-explanation-title')
        original_viewport = page.viewport_size
        viewports = [(device, original_viewport)]
        if device == 'tablet':
            viewports += [('ipad9', {'width': 1080, 'height': 810}),
                          ('tablet-short', {'width': 1024, 'height': 700})]
        steps = [('SOURCE', 'Elektronenquelle und Kondensor', 'Quelle liefert'),
                 ('SLITS', 'Zwei sehr feine Spalte', 'Breite'),
                 ('OPTICS', 'Elektronenoptische Abbildung', 'nicht aus Glas'),
                 ('REPRODUCTION', 'Beobachtung und Reproduktion', 'historischen Aufnahme')]
        touch = device == 'tablet'

        def activate(control):
            if touch:
                # Real screen coordinates avoid Playwright's implicit locator scrolling.
                box = control.bounding_box()
                page.touchscreen.tap(box['x'] + box['width']/2, box['y'] + box['height']/2)
            else:
                control.focus()
                page.keyboard.press('Enter')

        def position_controls(control):
            # Reproduce a reader who has reached the controls below the large drawing.
            control.evaluate('e => e.scrollIntoView({block:"end",behavior:"instant"})')
            page.evaluate('scrollBy(0,50); document.activeElement?.blur()')

        for viewport_name, viewport in viewports:
            page.set_viewport_size(viewport)
            for motion in ('reduce', 'no-preference'):
                page.emulate_media(reduced_motion=motion)
                setup.get_by_role('button', name='1 · '+steps[0][1], exact=True).click()
                expect(read).to_have_accessible_name('Erklärung lesen')
                expect(read).to_have_attribute('aria-controls', 'jonsson-setup-explanation')
                position_controls(next_button)
                before = {'next': next_button.bounding_box(), 'previous': previous.bounding_box()}
                scroll_y = page.evaluate('scrollY')
                expect(previous).to_be_disabled()
                navigation = [(i, next_button, 'next') for i in (1, 2, 3)]
                navigation += [(i, previous, 'previous') for i in (2, 1, 0)]
                for index, control, direction in navigation:
                    activate(control)
                    stage, _, meaning = steps[index]
                    expect(setup).to_have_attribute('data-optics-stage', stage)
                    expect(page.locator('#jonsson-setup-progress')).to_have_text(f'Aufbau · Schritt {index+1} von 4')
                    expect(explanation).to_contain_text(meaning)
                    after = {'next': next_button.bounding_box(), 'previous': previous.bounding_box()}
                    self.check(f'Jönsson previous/next stay in place without scrolling: {viewport_name} {motion} {direction} step {index+1}',
                               all(abs(before[name][key]-after[name][key]) < 1.1
                                   for name in before for key in ('x','y','width','height'))
                               and abs(page.evaluate('scrollY')-scroll_y) < 1.1)
                    if index == 3:
                        expect(next_button).to_be_disabled()
                expect(previous).to_be_disabled()

                for index, (stage, heading, meaning) in enumerate(steps):
                    setup.get_by_role('button', name=f'{index+1} · {heading}', exact=True).click()
                    expect(setup).to_have_attribute('data-optics-stage', stage)
                    position_controls(read)
                    before_work = self.snapshot(page)
                    before_url = page.url
                    activate(read)
                    expect(title).to_be_focused()
                    expect(title).to_have_text(heading)
                    expect(explanation).to_contain_text(meaning)
                    page.wait_for_function('''() => {
                        const e=document.querySelector('#jonsson-setup-explanation').getBoundingClientRect();
                        const h=document.querySelector('#site-navigation-header').getBoundingClientRect();
                        return e.top >= h.bottom+12 && e.bottom <= innerHeight-12;
                    }''')
                    self.check(f'Explicit reading reveals the entire current explanation below the header: {viewport_name} {motion} step {index+1}', True)
                    self.check(f'Reading keeps stage, route and personal work unchanged: {viewport_name} {motion} step {index+1}',
                               setup.get_attribute('data-optics-stage') == stage
                               and page.url == before_url and self.snapshot(page) == before_work)
                    if not touch:
                        self.check(f'Explanation heading has visible keyboard focus: {viewport_name} {motion} step {index+1}',
                                   title.evaluate('e => getComputedStyle(e).outlineStyle !== "none"'))
                    if index == 2:
                        self.shot(page, f'jonsson-explanation-{viewport_name}-{motion}.png')
            setup.get_by_role('button', name='3 · '+steps[2][1], exact=True).click()
            self.route(page, 'fazit')
            self.route(page, 'joensson')
            expect(setup).to_have_attribute('data-optics-stage', 'OPTICS')
            self.check('Jönsson setup step survives chapter changes: '+viewport_name, True)
        page.set_viewport_size(original_viewport)
        self.shot(page, 'jonsson-setup-step-optics-'+device+'.png')

    def reading(self):
        page = self.page()
        self.route(page, 'debroglie')
        self.route(page, 'lernen/spannung-zur-vorhersage')
        expect(page.locator('#article-herleitung')).to_be_focused()
        expect(page.locator('[id^="article-line-"]')).to_have_count(6)
        expect(page.locator('#article-play,#article-follow,#article-next-line,#article-all-lines')).to_have_count(0)
        for index in range(6):
            expect(page.locator('#article-line-' + str(index) + ' [data-typst-source],#article-line-' + str(index) + ' img')).not_to_have_count(0)
        page.locator('#article-reading-choice-5').click()
        expect(page.locator('#article-line-5')).to_be_focused()
        stored = self.snapshot(page)['work']['S05_DERIVATION']
        page.locator('#article-reading-choice-1').click()
        expect(page.locator('#article-line-1')).to_be_focused()
        self.check('Direct backward reading preserves all six explanations and the furthest recorded step', self.snapshot(page)['work']['S05_DERIVATION'] == stored)
        reload_with_images(page)
        expect(page.locator('#article-line-1')).to_have_class(re.compile('article-reading-current'))
        self.check('Reading position survives reload independently from cumulative scientific progress', True)
        page.locator('#article-herleitung').scroll_into_view_if_needed()
        self.shot(page, 'derivation-normal-reading.png')

        normal = self.page(reduced_motion='no-preference')
        self.route(normal, 'debroglie')
        self.route(normal, 'lernen/spannung-zur-vorhersage')
        expect(normal.locator('#article-reading-surface')).to_have_class(re.compile('article-reading-enhanced'))
        # Route positioning is asynchronous; begin the passive-scroll check only after it finished.
        expect(normal.locator('#article-herleitung')).to_be_focused()
        original_focus = normal.evaluate('document.activeElement.id')
        viewport_height = normal.viewport_size['height']
        header = normal.locator('#site-navigation-header').bounding_box()
        reading_top = header['y'] + header['height'] + 24
        def scroll_line(index, screen_y):
            element_id = 'article-line-' + str(index)
            target = normal.locator('#' + element_id).bounding_box()
            normal.mouse.wheel(0, target['y'] - screen_y)
            normal.wait_for_function('([id, y]) => Math.abs(document.getElementById(id).getBoundingClientRect().top - y) < 1.5',
                                     arg=[element_id, screen_y])
        expect(normal.locator('#article-line-0')).to_have_class(re.compile('article-reading-current'))
        scroll_line(1, viewport_height - 12)
        expect(normal.locator('#article-line-0')).to_have_class(re.compile('article-reading-current'))
        self.check('Seeing a later heading at the viewport bottom does not skip the explanation being read',
                   normal.locator('#article-line-1').bounding_box()['y'] > reading_top + 100)
        scroll_line(1, reading_top + 12)
        expect(normal.locator('#article-line-0')).to_have_class(re.compile('article-reading-current'))
        scroll_line(1, reading_top - 12)
        expect(normal.locator('#article-line-1')).to_have_class(re.compile('article-reading-current'))
        expect(normal.locator('#article-reading-formulas .article-reading-formula-row')).to_have_count(2)
        self.check('Explanation and formula advance together at the reading line below the header',
                   normal.locator('#article-line-1').bounding_box()['y'] <= reading_top)
        self.check('User scrolling selects the matching step without moving keyboard focus', normal.evaluate('document.activeElement.id') == original_focus)
        self.shot(normal, 'derivation-reading-line.png')
        scroll_line(4, reading_top - 12)
        expect(normal.locator('#article-line-4')).to_have_class(re.compile('article-reading-current'))
        expect(normal.locator('#article-reading-formulas .article-reading-formula-row')).to_have_count(5)
        stable = self.snapshot(normal)
        normal.wait_for_timeout(5300)
        expect(normal.locator('#article-reading-formulas .article-reading-formula-row')).to_have_count(5)
        self.check('Reading has no timed advancement or hidden work writes', self.snapshot(normal) == stable)
        scroll_line(4, reading_top + 12)
        expect(normal.locator('#article-line-3')).to_have_class(re.compile('article-reading-current'))
        self.check('Backward reading follows the same reading line rather than the viewport bottom',
                   normal.locator('#article-line-4').bounding_box()['y'] > reading_top)
        scroll_line(0, reading_top - 12)
        expect(normal.locator('#article-line-0')).to_have_class(re.compile('article-reading-current'))
        expect(normal.locator('#article-reading-formulas .article-reading-formula-row')).to_have_count(5)
        self.check('Backward scroll changes the current step without removing previously reached formulas', self.snapshot(normal) == stable)
        self.shot(normal, 'derivation-scroll-cumulative.png')
        # Large scroll jumps can begin and end in paragraph gaps. No section then
        # intersects the narrow reading band, so observer notifications alone fail.
        for previous_index, next_index in ((1, 2), (3, 4), (1, 2)):
            previous = normal.locator('#article-line-' + str(previous_index)).bounding_box()
            following = normal.locator('#article-line-' + str(next_index)).bounding_box()
            gap_midpoint = (previous['y'] + previous['height'] + following['y']) / 2
            self.check('The large-jump probe really targets a gap between explanations',
                       previous['y'] + previous['height'] < gap_midpoint < following['y'])
            normal.mouse.wheel(0, gap_midpoint - reading_top)
            normal.wait_for_function('([a, b, y]) => Math.abs((document.getElementById(a).getBoundingClientRect().bottom + document.getElementById(b).getBoundingClientRect().top) / 2 - y) < 1.5',
                                     arg=['article-line-' + str(previous_index), 'article-line-' + str(next_index), reading_top])
            expect(normal.locator('#article-line-' + str(previous_index))).to_have_class(re.compile('article-reading-current'))
        self.check('Forward and backward gap-to-gap jumps keep the explanation and formula aligned', True)
        normal.locator('#article-reading-choice-1').click()
        expect(normal.locator('#article-line-1')).to_be_focused()
        expect(normal.locator('#article-line-1')).to_have_class(re.compile('article-reading-current'))
        normal.wait_for_timeout(250)
        expect(normal.locator('#article-line-1')).to_have_class(re.compile('article-reading-current'))
        self.check('A deliberate step selection keeps its highlight even with the following explanation visible',
                   normal.locator('#article-line-2').bounding_box()['y'] < viewport_height)
        self.route(page, 'davisson-germer/geometrie')
        page.get_by_text('Beide Geometrien gegenüberstellen', exact=True).click()
        table = page.locator('.article-reference-table')
        expect(table).to_contain_text('Netzebenen')
        expect(table).to_contain_text('Oberflächenreihen')
        expect(table.locator('[data-typst-source]')).not_to_have_count(0)
        self.check('DG geometry roles are visible before the calculation and math is Typst', True)
        table.scroll_into_view_if_needed(); self.shot(page, 'dg-geometry-bridge.png')
        self.motion_contract()
        self.derivation_end()
        self.derivation_practice()

    def motion_contract(self):
        for mode in ('system-reduced', 'explicit-still', 'short-viewport', 'missing-observer', 'missing-resize-observer'):
            options = {'reduced_motion': 'reduce' if mode == 'system-reduced' else 'no-preference'}
            if mode == 'short-viewport': options['viewport'] = {'width': 1080, 'height': 600}
            if mode == 'missing-observer': options['init_script'] = "Object.defineProperty(window, 'IntersectionObserver', {value: undefined});"
            if mode == 'missing-resize-observer': options['init_script'] = "Object.defineProperty(window, 'ResizeObserver', {value: undefined});"
            page = self.page(**options)
            self.route(page, 'debroglie')
            if mode == 'explicit-still': page.locator('#motion-toggle').click()
            self.route(page, 'lernen/spannung-zur-vorhersage')
            # The composed content exists before the route's rAF positions its focus.
            expect(page.locator('#article-herleitung')).to_be_focused()
            expect(page.locator('#article-reading-surface')).not_to_have_class(re.compile('article-reading-enhanced'))
            expect(page.locator('[id^="article-line-"]')).to_have_count(6)
            expect(page.locator('#article-reading-formulas')).not_to_be_visible()
            page.locator('#article-reading-choice-2').focus()
            expect(page.locator('#article-reading-choice-2')).to_be_focused()
            page.keyboard.press('Enter')
            expect(page.locator('#article-line-2')).to_be_focused()
            self.check('Normal reading fallback retains all content and keyboard step selection: ' + mode, True)
            self.shot(page, 'derivation-fallback-' + mode + '.png')
        page = self.page(reduced_motion='no-preference')
        self.route(page, 'debroglie')
        self.route(page, 'lernen/spannung-zur-vorhersage')
        expect(page.locator('#article-herleitung')).to_be_focused()
        expect(page.locator('#article-reading-surface')).to_have_class(re.compile('article-reading-enhanced'))
        page.set_viewport_size({'width':1080,'height':600})
        expect(page.locator('#article-reading-surface')).not_to_have_class(re.compile('article-reading-enhanced'))
        expect(page.locator('#article-line-5 .article-reading-inline-formula')).to_be_visible()
        page.set_viewport_size({'width':1080,'height':810})
        expect(page.locator('#article-reading-surface')).to_have_class(re.compile('article-reading-enhanced'))
        motion = page.locator('#motion-toggle')
        expect(motion).to_be_enabled()
        expect(motion).to_have_attribute('aria-pressed', 'true')
        page.emulate_media(reduced_motion='reduce')
        expect(page.locator('#article-reading-surface')).not_to_have_class(re.compile('article-reading-enhanced'))
        expect(motion).to_be_disabled()
        expect(motion).to_have_attribute('aria-pressed', 'false')
        expect(motion).to_contain_text('System')
        page.emulate_media(reduced_motion='no-preference')
        expect(motion).to_be_enabled()
        expect(motion).to_have_attribute('aria-pressed', 'true')
        self.check('System motion reduction is shown honestly and restores the unchanged preference', True)
        self.check('Resize and motion changes preserve a complete ordinary reading alternative', True)

        for width, height in ((1080, 810), (1024, 700)):
            page = self.page(viewport={'width': width, 'height': height}, has_touch=True,
                             reduced_motion='no-preference')
            self.route(page, 'debroglie')
            self.route(page, 'lernen/spannung-zur-vorhersage')
            expect(page.locator('#article-herleitung')).to_be_focused()
            expect(page.locator('#article-reading-surface')).to_have_class(re.compile('article-reading-enhanced'))
            expect(page.locator('#article-line-0')).to_have_class(re.compile('article-reading-current'))
            self.check('Landscape entry highlights the first explanation, not a later visible heading: ' + str(width), True)
            buttons = page.locator('#article-reading-navigation button')
            self.check('Derivation step targets remain touch-sized: ' + str(width), all(
                b['width'] >= 44 and b['height'] >= 44 for b in buttons.evaluate_all(
                    'buttons => buttons.map(e => { const b=e.getBoundingClientRect(); return {width:b.width,height:b.height}; })')))
            page.locator('#article-reading-choice-5').tap()
            page.locator('#article-reading-choice-1').tap()
            expect(page.locator('#article-reading-formulas .article-reading-formula-row')).to_have_count(6)
            board = page.locator('#article-reading-formulas').bounding_box()
            header = page.locator('#site-navigation-header').bounding_box()
            self.check('Complete sticky formula board fits landscape viewport below header: ' + str(width),
                       board['y'] >= header['y'] + header['height'] and
                       board['y'] + board['height'] <= height and board['x'] + board['width'] <= width)
            self.check('Landscape derivation does not introduce horizontal overflow: ' + str(width),
                       page.evaluate('document.documentElement.scrollWidth <= innerWidth + 1'))
            self.shot(page, 'derivation-landscape-' + str(width) + '-' + str(height) + '.png')
            if width == 1024:
                set_theme(page, 'light')
                self.shot(page, 'derivation-landscape-1024-700-light.png')
            # Increased text size should restore normal reading, not squeeze the formula board.
            stable = self.snapshot(page)
            page.evaluate("document.documentElement.style.fontSize='24px'; window.dispatchEvent(new Event('resize'))")
            expect(page.locator('#article-reading-surface')).not_to_have_class(re.compile('article-reading-enhanced'))
            expect(page.locator('#article-line-5 .article-reading-inline-formula')).to_be_visible()
            self.check('Enlarged text restores all inline formulas and preserves learner work: ' + str(width),
                       self.snapshot(page) == stable)

    def derivation_end(self):
        # A sticky board can fit the viewport yet be pushed above it by the end
        # of its own container. Test the actual final step, not a jump back to 2.
        for width, height in ((1080, 810), (1024, 700), (1440, 900)):
            label = str(width) + 'x' + str(height)
            page = self.page(viewport={'width': width, 'height': height}, has_touch=True,
                             reduced_motion='no-preference')
            self.route(page, 'debroglie')
            self.route(page, 'lernen/spannung-zur-vorhersage')
            expect(page.locator('#article-herleitung')).to_be_focused()
            expect(page.locator('#article-reading-surface')).to_have_class(re.compile('article-reading-enhanced'))
            header = page.locator('#site-navigation-header').bounding_box()
            reading_top = header['y'] + header['height'] + 24

            def end_geometry(action):
                expect(page.locator('#article-line-5')).to_have_class(re.compile('article-reading-current'))
                expect(page.locator('#article-reading-formulas .article-reading-formula-row')).to_have_count(6)
                page.wait_for_function('''() => {
                    const board=document.getElementById('article-reading-formulas').getBoundingClientRect();
                    const header=document.getElementById('site-navigation-header').getBoundingClientRect();
                    return board.top >= header.bottom && board.bottom <= innerHeight + 1;
                }''')
                geometry = page.evaluate('''() => {
                    const last=document.getElementById('article-line-5');
                    const box=last.getBoundingClientRect(), style=getComputedStyle(last);
                    const explanation=last.querySelector('.article-reading-step-content');
                    return {step:box.toJSON(), board:document.getElementById('article-reading-formulas').getBoundingClientRect().toJSON(),
                        explanation:explanation.getBoundingClientRect().toJSON(),
                        reserveColor:style.backgroundColor, explanationColor:getComputedStyle(explanation).backgroundColor,
                        surface:document.getElementById('article-reading-surface').getBoundingClientRect().toJSON(),
                        calculation:document.getElementById('article-rechnung').getBoundingClientRect().toJSON(),
                        naturalHeight:last.lastElementChild.getBoundingClientRect().bottom-box.top+
                            parseFloat(style.paddingBottom)+parseFloat(style.borderBottomWidth),
                        focus:document.activeElement.id, scrollY};
                }''')
                self.report.setdefault('derivation_end_geometry', []).append({
                    'viewport': {'width': width, 'height': height}, 'action': action, **geometry,
                })
                board = geometry['board']; step = geometry['step']
                self.check('Final explanation highlights its text while the formula-height reserve stays neutral: '+label+' '+action,
                           geometry['reserveColor'] == 'rgba(0, 0, 0, 0)' and
                           geometry['explanationColor'] != 'rgba(0, 0, 0, 0)' and
                           geometry['explanation']['height'] < step['height'] - 24)
                self.check('All six formulas remain visible at the final reading step: '+label+' '+action,
                           board['top'] >= reading_top - 1 and board['bottom'] <= height + 1 and
                           board['left'] >= 0 and board['right'] <= width + 1)
                self.check('Final row reserves only the height its actual content or board needs: '+label+' '+action,
                           abs(step['height'] - max(board['height'], geometry['naturalHeight'])) <= 2)
                self.check('Formula board stays inside its reading section before the 150-V task: '+label+' '+action,
                           board['bottom'] <= geometry['surface']['bottom'] + 1 and
                           geometry['calculation']['top'] >= geometry['surface']['bottom'])
                self.check('Final-step layout has no horizontal document overflow: '+label+' '+action,
                           page.evaluate('document.documentElement.scrollWidth <= innerWidth + 1'))

            page.locator('#article-reading-choice-5').tap()
            expect(page.locator('#article-line-5')).to_be_focused()
            self.viewport_target(page, '#article-line-5')
            end_geometry('direct-touch')
            self.shot(page, 'derivation-last-step-'+label+'.png')
            page.locator('#article-reading-choice-0').focus(); page.keyboard.press('Enter')
            page.locator('#article-reading-choice-5').focus(); page.keyboard.press('Enter')
            expect(page.locator('#article-line-5')).to_be_focused()
            self.viewport_target(page, '#article-line-5')
            end_geometry('keyboard')

            page.locator('#article-reading-choice-0').focus(); page.keyboard.press('Enter')
            expect(page.locator('#article-line-0')).to_be_focused()
            focus = page.evaluate('document.activeElement.id')
            page.evaluate('''() => {window.__readingScrollCalls=0; const scroll=window.scrollTo.bind(window);
                window.scrollTo=(...args) => {window.__readingScrollCalls++; return scroll(...args);};}''')
            for index in range(1, 6):
                target = '#article-line-' + str(index)
                page.mouse.wheel(0, page.locator(target).bounding_box()['y'] - (reading_top - 8))
                page.wait_for_function('([id,y]) => Math.abs(document.querySelector(id).getBoundingClientRect().top-y)<1.5',
                                       arg=[target, reading_top - 8])
                expect(page.locator(target)).to_have_class(re.compile('article-reading-current'))
            end_geometry('normal-reading')
            self.check('Reading to the end changes neither focus nor scroll position programmatically: '+label,
                       page.evaluate('document.activeElement.id') == focus and page.evaluate('window.__readingScrollCalls') == 0)
            self.shot(page, 'derivation-last-step-reading-'+label+'.png')
            page.locator('#article-rechnung').scroll_into_view_if_needed()
            self.check('The next task is never covered by the former sticky board: '+label,
                       page.locator('#article-reading-formulas').bounding_box()['y'] +
                       page.locator('#article-reading-formulas').bounding_box()['height'] <=
                       page.locator('#article-rechnung').bounding_box()['y'])
            page.context.close()

            fallback = self.page(viewport={'width': width, 'height': height}, reduced_motion='reduce')
            self.route(fallback, 'debroglie')
            self.route(fallback, 'lernen/spannung-zur-vorhersage')
            expect(fallback.locator('#article-herleitung')).to_be_focused()
            fallback.locator('#article-reading-choice-5').focus(); fallback.keyboard.press('Enter')
            expect(fallback.locator('#article-line-5')).to_be_focused()
            self.viewport_target(fallback, '#article-line-5')
            expect(fallback.locator('#article-line-5 .article-reading-inline-formula')).to_be_visible()
            expect(fallback.locator('#article-reading-formulas')).not_to_be_visible()
            self.check('Reduced-motion reading has no formula-board space reservation: '+label,
                       fallback.locator('#article-line-5').evaluate('e => getComputedStyle(e).minHeight') == '0px')
            self.shot(fallback, 'derivation-last-step-reduced-'+label+'.png')
            fallback.context.close()

    def derivation_practice(self):
        page = self.page(viewport={'width': 1080, 'height': 810}, has_touch=True)
        self.route(page, 'debroglie')
        self.route(page, 'lernen/150-volt')
        page.locator('#course-task-answer').fill('QA: 999 nm, bewusst falscher eigener Wert')
        page.locator('#article-notes').fill('QA: Diese ursprüngliche 150-V-Rechnung bleibt unverändert.')
        self.route(page, 'lernen/spannung-zur-vorhersage')
        expect(page.locator('#article-herleitung')).to_be_focused()
        page.locator('#article-reading-choice-1').click()
        before = self.snapshot(page)
        opener = page.locator('#derivation-practice-open')
        opener.focus(); page.keyboard.press('Enter')
        panel = page.locator('#derivation-practice-panel')
        expect(panel).to_be_visible()
        expect(page.locator('#derivation-practice-temporary')).to_contain_text('bis zum Neuladen')
        expect(page.locator('[id^="article-line-"]').first).not_to_be_visible()
        expect(page.locator('#derivation-practice-choices-panel')).not_to_be_visible()
        own = 'QA: Erst die Energiebeziehung umstellen; diese Erklärung wird nicht automatisch benotet.'
        hypothesis = 'QA: Die Zuordnung einer Wellenlänge ist die zusätzliche Hypothese.'
        page.locator('#derivation-practice-answer').fill(own)
        page.locator('#derivation-practice-hypothesis').fill(hypothesis)
        page.locator('#derivation-practice-hint').tap()
        expect(page.locator('#derivation-practice-hint-one')).to_be_visible()
        expect(page.locator('#derivation-practice-hint-two')).not_to_be_visible()
        page.locator('#derivation-practice-hint').tap()
        expect(page.locator('#derivation-practice-hint-one')).to_be_visible()
        expect(page.locator('#derivation-practice-hint-two')).to_be_visible()
        page.locator('#derivation-practice-choices').tap()
        for key, asset in (('momentum', 'momentum'), ('energy', 'kinetic-momentum'), ('hypothesis', 'de-broglie')):
            image = page.locator('#derivation-practice-choice-' + key + ' img')
            expect(image).to_have_attribute('src', re.compile(r'math/' + asset + r'-(?:dark|light)\.svg$'))
            self.check('Choice is a loaded real Typst SVG: ' + key, image.evaluate('e => e.complete && e.naturalWidth > 0'))
        page.locator('#derivation-practice-choice-energy').tap()
        expect(page.locator('#derivation-practice-choice-feedback')).to_contain_text('Energiebeziehung gilt')
        page.locator('#derivation-practice-choice-momentum').focus(); page.keyboard.press('Space')
        expect(page.locator('#derivation-practice-choice-feedback')).to_contain_text('Impulsbetrag direkt')
        page.locator('#derivation-practice-compare').tap()
        expect(page.locator('#derivation-practice-comparison')).to_contain_text('Dein freier Text wird nicht automatisch bewertet')
        expect(page.locator('#derivation-practice-comparison')).to_contain_text('andere algebraisch gültige Reihenfolge')
        expect(page.locator('#derivation-practice-answer')).to_have_value(own)
        expect(page.locator('#derivation-practice-hypothesis')).to_have_value(hypothesis)
        self.check('Hints and deliberate comparison retain both own ungraded explanations', True)
        page.locator('#derivation-practice-example').tap()
        expect(page.locator('#derivation-practice-example-panel')).to_be_visible()
        expect(page.locator('[id^="article-line-"]')).to_have_count(6)
        self.check('Explicit example access keeps the complete example and prior reading position', True)
        self.shot(page, 'derivation-practice-tablet.png')
        page.locator('#derivation-practice-revise').focus(); page.keyboard.press('Enter')
        expect(page.locator('#derivation-practice-answer')).to_be_focused()
        expect(page.locator('#derivation-practice-comparison')).not_to_be_visible()
        expect(page.locator('#derivation-practice-example-panel')).not_to_be_visible()
        expect(page.locator('#derivation-practice-answer')).to_have_value(own)
        expect(page.locator('#derivation-practice-hypothesis')).to_have_value(hypothesis)
        self.check('Returning to own explanation closes both aids, preserves drafts and leaves scientific work unchanged',
                   self.snapshot(page) == before)
        self.route(page, 'joensson')
        self.route(page, 'lernen/spannung-zur-vorhersage')
        expect(page.locator('#derivation-practice-answer')).to_have_value(own)
        expect(page.locator('#derivation-practice-hypothesis')).to_have_value(hypothesis)
        self.check('Temporary practice survives chapter changes without modifying scientific work', self.snapshot(page) == before)
        page.locator('#derivation-practice-close').click()
        expect(page.locator('#article-line-1')).to_be_focused()
        self.viewport_target(page, '#article-line-1')
        expect(page.locator('[id^="article-line-"]')).to_have_count(6)
        page.locator('#derivation-practice-open').click()
        expect(page.locator('#derivation-practice-answer')).to_have_value(own)
        self.check('Closing and reopening the rehearsal retains its answer and complete example', self.snapshot(page) == before)
        reload_with_images(page)
        page.locator('#derivation-practice-open').click()
        expect(page.locator('#derivation-practice-answer')).to_have_value('')
        expect(page.locator('#derivation-practice-hypothesis')).to_have_value('')
        self.check('Reload clears only the announced temporary practice', self.snapshot(page) == before)
        self.derivation_return()

    def derivation_return(self):
        # Returning from the exercise must land in the derivation, not at its opener
        # below all six steps beside the next 150-V task. Exercise both remount layouts.
        for motion in ('reduce', 'no-preference'):
            page = self.page(viewport={'width': 1080, 'height': 810}, has_touch=True, reduced_motion=motion)
            self.route(page, 'debroglie')
            self.route(page, 'lernen/spannung-zur-vorhersage')
            expect(page.locator('#article-herleitung')).to_be_focused()
            set_theme(page, 'light')
            page.locator('#article-reading-choice-2').tap()
            page.locator('#derivation-practice-open').tap()
            expect(page.locator('#derivation-practice-title')).to_be_focused()
            selected = page.evaluate('JSON.parse(localStorage.getItem("materiewelle.ui-drafts.v1")).derivationStep')
            before = self.snapshot(page)
            own = 'QA: Mein Ansatz bleibt beim Rückweg erhalten.'
            page.locator('#derivation-practice-answer').fill(own)
            page.locator('#derivation-practice-close').tap()
            target = '#article-line-' + str(selected)
            expect(page.locator(target)).to_be_focused()
            self.viewport_target(page, target)
            expect(page.locator(target)).to_have_class(re.compile('article-reading-current'))
            if motion == 'no-preference':
                expect(page.locator('#article-reading-surface')).to_have_class(re.compile('article-reading-enhanced'))
            else:
                expect(page.locator('#article-reading-surface')).not_to_have_class(re.compile('article-reading-enhanced'))
            self.check('Explicit exercise return restores the selected reading step and scientific work: ' + motion,
                       self.snapshot(page) == before)
            self.shot(page, 'derivation-return-' + motion + '.png')
            page.locator('#derivation-practice-open').tap()
            expect(page.locator('#derivation-practice-answer')).to_have_value(own)

    def measurement_entry(self):
        # The initial task and source role must leave a genuinely usable image area.
        # Keep the original 1024×768 top/size threshold; add a real touch action
        # without locator autoscroll and a shorter viewport for browser chrome.
        for label, viewport in (
            ('tablet', {'width': 1024, 'height': 768}),
            ('ipad9', {'width': 1080, 'height': 810}),
            ('tablet-short', {'width': 1024, 'height': 680}),
        ):
            page = self.page(has_touch=True, viewport=viewport)
            self.route(page, 'joensson/messung')
            expect(page.locator('#joensson-messung')).to_be_focused()
            image = page.locator('#jonsson-native-image').bounding_box()
            header = page.locator('header').first.bounding_box()
            visible_height = min(image['y'] + image['height'], viewport['height']) - max(image['y'], header['y'] + header['height'])
            self.report.setdefault('measurement_entry_bounds', []).append({
                'viewport': viewport, 'image': image, 'visible_height': visible_height,
            })
            self.check('At least 200px of the actual image remain usable without scrolling: '+label,
                       image['height'] > 200 and visible_height >= 200 and image['x'] >= 0 and image['x'] + image['width'] <= viewport['width'])
            if label == 'tablet':
                self.check('Original tablet first-image contract remains unchanged', image['y'] < 768 * .5)
            section = page.locator('#joensson-messung')
            expect(section).to_contain_text('Markiere zunächst vier benachbarte helle Streifenmaxima.')
            expect(section).to_contain_text('Die vier Marken ergeben drei Abstände')
            expect(section).to_contain_text('dazwischen darf kein weiteres Maximum liegen.')
            expect(section).to_contain_text('Historische Aufnahme aus Jönssons Veröffentlichung')
            expect(section).to_contain_text('Reproduktion, keine Simulation')
            point = page.locator('#jonsson-native-image').evaluate('e => { const p=new DOMPoint(551,160).matrixTransform(e.getScreenCTM()); return {x:p.x,y:p.y}; }')
            self.check('Native first-touch target is inside the initial visible image: '+label,
                       image['x'] <= point['x'] <= image['x'] + image['width'] and max(image['y'], header['y'] + header['height']) <= point['y'] < viewport['height'])
            before_scroll = page.evaluate('scrollY')
            before_focus = page.evaluate('document.activeElement?.id')
            page.touchscreen.tap(point['x'], point['y'])
            expect(page.locator('[data-own-marker]')).to_have_count(1)
            expect(page.locator('[data-own-marker="0"]')).to_have_attribute('data-native-x', '551')
            after_scroll = page.evaluate('scrollY')
            self.report.setdefault('measurement_entry_interactions', []).append({
                'viewport': viewport, 'tap': point, 'scroll_before': before_scroll,
                'scroll_after': after_scroll, 'focus_before': before_focus,
                'focus_after': page.evaluate('document.activeElement?.id'),
                'image_after': page.locator('#jonsson-native-image').bounding_box(),
            })
            self.check('A first native marker is set by touch without scrolling: '+label,
                       abs(after_scroll - before_scroll) < 1)
            expect(page.locator('#jonsson-interactive-image')).to_be_focused()
            page.keyboard.press('ArrowRight')
            expect(page.locator('[data-own-marker="0"]')).to_have_attribute('data-native-x', '552')
            page.keyboard.press('ArrowLeft')
            expect(page.locator('[data-own-marker="0"]')).to_have_attribute('data-native-x', '551')
            self.check('Touch focus retains one-native-pixel keyboard editing: '+label, True)
            self.shot(page, 'measurement-entry-'+label+'-dark.png')
            set_theme(page, 'light')
            self.shot(page, 'measurement-entry-'+label+'-light.png')
            page.context.close()

    def slit_comparison_visibility(self):
        page = self.page()
        self.route(page, 'joensson')
        toggle = page.locator('#course-slit-clear')
        saved_curve = page.locator('#slit-profile path[stroke-dasharray="2 5"]')
        expect(toggle).to_be_disabled()
        page.locator('#course-model-g').fill('2')
        page.locator('#course-model-b').fill('0.5')
        page.locator('#course-slit-save').click()
        expect(toggle).to_have_text('Vergleich ausblenden')
        expect(toggle).to_have_attribute('aria-pressed', 'true')
        expect(saved_curve).to_have_count(1)
        saved_path = saved_curve.get_attribute('d')
        saved = self.snapshot(page)['inquiry']['slitComparison']
        page.locator('#course-model-g').fill('4')
        before_toggle = self.snapshot(page)
        toggle.click()
        expect(toggle).to_have_text('Vergleich anzeigen')
        expect(toggle).to_have_attribute('aria-pressed', 'false')
        expect(saved_curve).to_have_count(0)
        expect(page.locator('#slit-readout')).not_to_contain_text('A · gespeichert')
        self.check('Hiding the comparison preserves all stored scientific work',
                   self.snapshot(page) == before_toggle)
        toggle.click()
        expect(toggle).to_have_text('Vergleich ausblenden')
        expect(saved_curve).to_have_attribute('d', saved_path)
        self.check('Showing the comparison restores the same curve and saved setting',
                   self.snapshot(page) == before_toggle and
                   self.snapshot(page)['inquiry']['slitComparison'] == saved)
        toggle.click()
        reload_with_images(page)
        expect(toggle).to_have_text('Vergleich ausblenden')
        expect(saved_curve).to_have_attribute('d', saved_path)
        self.check('Reload retains the saved setting and resets only its temporary visibility',
                   self.snapshot(page)['inquiry']['slitComparison'] == saved)
        toggle.click()
        page.locator('#course-slit-save').click()
        expect(toggle).to_have_text('Vergleich ausblenden')
        expect(saved_curve).to_have_count(1)
        self.check('Saving a new setting makes the replacement comparison visible',
                   self.snapshot(page)['inquiry']['slitComparison']['spacingUm'] == 4 and
                   saved_curve.get_attribute('d') != saved_path)
        page.context.close()

    def drafts(self):
        self.slit_comparison_visibility()
        self.measurement_entry()
        page = self.page(has_touch=True, viewport={'width': 1024, 'height': 768})
        self.route(page, 'joensson/messung')
        expect(page.locator('#joensson-messung')).to_be_focused()
        image = page.locator('#jonsson-native-image').bounding_box()
        self.check('Image is a substantial visible work surface on tablet entry', image['y'] < 768 * .5 and image['height'] > 200)
        for coordinate in (550, 565, 580):
            self.open_marker_coordinates(page)
            page.locator('#marker-new-x').fill(str(coordinate)); page.locator('#marker-add').click()
        page.locator('#jonsson-justification').fill('QA: benachbarte helle Streifen im Basisband.')
        page.locator('#jonsson-seal-selection').click(); page.locator('#jonsson-show-reference').click()
        before = self.snapshot(page)['measurement']
        self.open_disclosures(page, '#marker-reset')
        page.locator('#marker-reset').click()
        expect(page.locator('#jonsson-count')).to_contain_text('0 Marken')
        page.locator('#measurement-undo').click()
        restored = self.snapshot(page)['measurement']
        self.check('Undo restores selection while retaining first choice and reference provenance', restored == before)
        self.open_marker_coordinates(page)
        page.locator('#marker-new-x').fill('58,')
        page.locator('#image-zoom').fill('1.75')
        page.locator('#tutor-help-image-measurement').click()
        page.locator('#compact-tutor-question').fill('QA: Diesen eigenen Gedanken nach Reload behalten.')
        reload_with_images(page)
        expect(page.locator('#marker-new-x')).to_have_value('58,')
        expect(page.locator('#image-zoom')).to_have_value('1.75')
        page.locator('#tutor-help-image-measurement').click()
        expect(page.locator('#compact-tutor-question')).to_have_value('QA: Diesen eigenen Gedanken nach Reload behalten.')
        expect(page.locator('#compact-tutor-copy')).to_have_text('Nachricht kopieren')
        expect(page.locator('#compact-tutor-own')).not_to_be_checked()
        page.locator('#compact-tutor-own').check()
        page.get_by_text('Vollständige Nachricht ansehen', exact=True).click()
        message, context = two.handoff(page)
        self.check('Tutor preview contains the retained question and explicitly selected image context',
                   context['question'] == 'QA: Diesen eigenen Gedanken nach Reload behalten.' and
                   context.get('ownWork') is not None and 'QA: benachbarte helle Streifen' in message)
        self.check('No work-file import or JSON backup actions remain in the active page',
                   page.locator('#course-export,#session-import-file,#session-draft-export').count() == 0)
        self.check('Question, unfinished coordinate and zoom survive actual reload', True)
        self.check('Method help retains scientific measurement after prior reference contact', self.snapshot(page)['measurement'] == before)
        self.shot(page, 'tablet-local-help.png')

    def math_failure(self):
        context = self.browser.new_context(viewport={'width': 1280, 'height': 800}, reduced_motion='reduce',
                                           permissions=['clipboard-read', 'clipboard-write'])
        page = context.new_page(); self.pages.append(page); self.current = page; self.observe(page)
        page.goto(self.url + '#debroglie', wait_until='networkidle')
        note = 'QA: Meine eigene Erklärung muss bei einem Formelfehler lesbar bleiben.'
        question = 'QA: Meine vorbereitete Frage ebenfalls behalten.'
        page.locator('#article-notes').fill(note)
        self.open_tutor(page)
        page.locator('#compact-tutor-question').fill(question)
        page.wait_for_function('([key,note]) => localStorage.getItem(key)?.includes(note)', arg=[two.STORAGE, note])
        page.wait_for_function('question => localStorage.getItem("materiewelle.ui-drafts.v1")?.includes(question)', arg=question)
        stored = page.evaluate('key => localStorage.getItem(key)', two.STORAGE)
        page.route('**/math-text/catalog.json', lambda route: route.fulfill(status=200, content_type='application/json', body='{}'))
        page.reload(wait_until='networkidle')
        expect(page.locator('#typeset-status')).to_contain_text('nicht verfügbar')
        self.open_storage(page)
        expect(page.locator('#session-work-copy')).to_be_enabled()
        page.locator('#session-work-copy').click()
        expect(page.locator('#session-work-copy-status')).to_contain_text('kopiert')
        copied = page.evaluate('navigator.clipboard.readText()')
        self.check('Own answer and prepared tutor question can be copied without the formula catalog',
                   copied.startswith('Arbeitsstand') and note in copied and question in copied and
                   not copied.lstrip().startswith('{'))
        self.check('Copy rescue does not replace the stored scientific record',
                   page.evaluate('key => localStorage.getItem(key)', two.STORAGE) == stored)
        self.check('Formula error offers readable copy instead of JSON backup tools',
                   page.locator('#course-export,#session-import-file,#session-draft-export').count() == 0)
        for name in ('De Broglie', 'Davisson–Germer', 'Jönsson', 'Fazit'):
            expect(page.get_by_role('button', name=name, exact=True)).to_be_visible()
        page.get_by_role('button', name='Davisson–Germer', exact=True).click()
        expect(page).to_have_title(re.compile('Davisson'))
        page.locator('#theme-preference').click()
        self.check('Navigation and theme remain usable when the formula catalog is invalid', True)
        self.check('No visible scientific Unicode fallback is introduced', page.locator('#dg-article').count() == 0)
        self.shot(page, 'formula-load-recovery.png')
        page.unroute('**/math-text/catalog.json'); page.locator('#typeset-retry').click()
        expect(page.locator('#dg-article')).to_be_visible()
        self.route(page, 'davisson-germer/rechnen')
        page.locator('#dg-show-geometry').click()
        expect(page.locator('#dg-geometry-result')).to_have_attribute('data-value-nm', re.compile(r'0\.165'))
        self.check('Retry renders the selected article with actual calculation after catalog recovery', True)

    def performance(self):
        page = self.page(init_script='''window.__uiWrites={};const original=Storage.prototype.setItem;
            Storage.prototype.setItem=function(k,v){window.__uiWrites[k]=(window.__uiWrites[k]||0)+1;return original.call(this,k,v)};''')
        settle_images(page)
        timing = page.evaluate('''() => ({resources:performance.getEntriesByType('resource').map(e=>({name:new URL(e.name).pathname,bytes:e.transferSize,encoded:e.encodedBodySize})),navigation:performance.getEntriesByType('navigation').map(e=>({bytes:e.transferSize,encoded:e.encodedBodySize}))})''')
        self.route(page, 'joensson/messung'); page.locator('#jonsson-native-image').scroll_into_view_if_needed()
        page.evaluate('window.__uiWrites={}')
        box = page.locator('#jonsson-native-image').bounding_box()
        for step in range(30):
            page.mouse.move(box['x'] + 20 + step * (box['width']-40)/30, box['y'] + box['height']/2)
        writes = page.evaluate('window.__uiWrites')
        self.report['performance_probe'] = {'scope':'Local uncompressed HTTP, first De-Broglie entry; no real Internet timing',
            'initial': timing, 'writes_during_30_hover_moves': writes}
        self.check('Hover never writes scientific measurement data', writes.get(two.STORAGE, 0) == 0)
        self.check('Transient hover never writes persisted view drafts', writes.get('materiewelle.ui-drafts.v1', 0) == 0)
        self.check('Initial resources were actually measured', len(timing['resources']) > 0)


if __name__ == '__main__':
    two.main(scenarios=('presentation', 'reading', 'drafts', 'math_failure', 'performance'),
             suite_type=RefinementChecks, harness_path=Path(__file__))
