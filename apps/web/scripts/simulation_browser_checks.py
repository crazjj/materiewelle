"""Real UI checks for the optional slit investigation; no injected product state.

The owning Jönsson suite supplies its normal browser, synthetic source-work seed,
screenshots, error collection and immutable artifact/report identity.
"""
import hashlib
import json
import math
import re
from pathlib import Path
from playwright.sync_api import expect
from browser_assets import reload_with_images, settle_fonts, settle_images
from browser_theme import set_theme


def open_details(page, selector):
    details = page.locator(selector)
    if details.get_attribute('open') is None:
        details.locator(':scope > summary').click()


def settle_render(page):
    """Compose schedules a DOM render after input; inspect its result, not the prior frame."""
    page.evaluate('() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)))')


def stable_plot_geometry(page):
    """Coordinate input requires the final drawn SVG, unlike a locator-managed click."""
    return page.locator('#slit-profile svg').first.evaluate("""svg => new Promise((resolve,reject) => {
      let previous, stable = 0, frame;
      const timer = setTimeout(() => {
        cancelAnimationFrame(frame);
        reject(new Error('Slit plot geometry did not stabilize within 5000 ms'));
      },5000);
      function sample() {
        if (!svg.isConnected) {
          clearTimeout(timer); reject(new Error('Slit SVG was replaced while preparing coordinate input')); return;
        }
        const box=svg.getBoundingClientRect(), matrix=svg.getScreenCTM();
        const values=[box.x,box.y,box.width,box.height,matrix.a,matrix.b,matrix.c,matrix.d,matrix.e,matrix.f,scrollX,scrollY];
        stable=previous && values.every((v,i)=>Math.abs(v-previous[i])<.01) ? stable+1 : 0;
        previous=values;
        if (stable>=2) {
          clearTimeout(timer); resolve({bounds:box.toJSON(),scroll:scrollY,stableFrames:stable+1});
        } else frame=requestAnimationFrame(sample);
      }
      frame=requestAnimationFrame(sample);
    })""")


def preserved_work(snapshot):
    """Model controls are intentionally editable; personal evidence is independent."""
    preserved = {key: snapshot.get(key) for key in
                 ('work', 'measurement', 'deBroglie', 'dg', 'questions', 'practice', 'imageIdentity',
                  'schemaVersion', 'courseRevision', 'dataRevision', 'measurementResetReason')}
    preserved['inquiry'] = {key: value for key, value in snapshot.get('inquiry', {}).items()
                            if key != 'slitComparison'}
    return preserved


def read_phase(page):
    settle_render(page)
    values = page.locator('#slit-phase-readout').evaluate("""e => Object.fromEntries(
      ['position-m','path-difference-m','phase-turns','left-intensity','right-intensity','total-intensity']
        .map(key => [key, e.hasAttribute('data-'+key) ? Number(e.getAttribute('data-'+key)) : null]))""")
    assert all(value is not None and math.isfinite(value) for value in values.values()), values
    return values


def histogram(page):
    """Read the actual accessible table, including Typst's textual readings."""
    settle_render(page)
    rows = page.locator('#slit-detection-table tbody tr').evaluate_all(
        'rows => rows.map(row => [...row.querySelectorAll("td")].map(cell => cell.textContent))')
    def number(text):
        return float(text.strip().replace('\u202f', '').replace('\u00a0', '').replace(' ', '').replace(',', '.'))
    return [(int(number(row[-2])), number(row[-1])) for row in rows]


def detector_count(page):
    settle_render(page)
    text = page.locator('#slit-detection-count').text_content()
    return int(re.search(r'[0-9][0-9\s\u00a0\u202f]*', text).group().replace(' ', '').replace('\n', '').replace('\u00a0', '').replace('\u202f', ''))


def canvas_digest(page):
    settle_render(page)
    return hashlib.sha256(page.locator('#slit-detection-canvas').evaluate('e=>e.toDataURL()').encode()).hexdigest()


def expected_bins(spacing_um, width_um, voltage=150.0, distance=10.0, count=60):
    """Independent Simpson integration of the stated ideal coherent aperture law."""
    wavelength = 6.62607015e-34 / math.sqrt(2 * 9.1093837139e-31 * 1.602176634e-19 * voltage)
    def intensity(x):
        sine = x / math.hypot(distance, x)
        alpha = math.pi * width_um * 1e-6 * sine / wavelength
        beta = math.pi * spacing_um * 1e-6 * sine / wavelength
        return (1 if alpha == 0 else math.sin(alpha) / alpha) ** 2 * math.cos(beta) ** 2
    weights = []
    for index in range(count):
        left = -.003 + index * .006 / count
        right = left + .006 / count
        divisions = 128
        step = (right - left) / divisions
        weights.append(step / 3 * (intensity(left) + intensity(right) + sum(
            (4 if k % 2 else 2) * intensity(left + k * step) for k in range(1, divisions))))
    area = sum(weights)
    return [weight / area for weight in weights]


def check_investigation_and_phase(suite, page):
    before = preserved_work(suite.snapshot(page))
    suite.route(page, 'joensson')
    page.locator('#slit-lab-configuration').select_option('BOTH_COHERENT')
    page.locator('#course-model-g').fill('2')
    page.locator('#course-model-b').fill('0.5')
    open_details(page, '#slit-investigation')
    page.locator('#slit-investigate-spacing').click()
    prediction = 'QA-SYNTHETISCH: Ich vermute engere Streifen bei größerem Mittenabstand.'
    reason = 'QA-SYNTHETISCH: Die Hülle bleibt beim allein veränderten Mittenabstand gleich.'
    page.locator('#slit-investigation-prediction').fill(prediction)
    page.locator('#slit-investigation-compare').click()
    expect(page.locator('#slit-investigation-result')).to_contain_text('Merke zuerst')
    page.locator('#course-slit-save').click()
    saved = suite.snapshot(page)['inquiry']['slitComparison']
    page.locator('#slit-investigation-compare').click()
    expect(page.locator('#slit-investigation-result')).to_contain_text('dieselbe Geometrie')
    page.locator('#course-model-g').fill('4')
    page.locator('#slit-investigation-reason').fill(reason)
    page.locator('#slit-investigation-compare').click()
    expect(page.locator('#slit-investigation-result')).to_be_visible()
    expect(page.locator('#slit-investigation-prediction')).to_have_value(prediction)
    expect(page.locator('#slit-investigation-reason')).to_have_value(reason)
    expect(page.locator('#slit-investigation-result')).to_contain_text('Interferenzstreifen rücken näher zusammen')
    expect(page.locator('#slit-investigation-result')).to_contain_text('die breite Hülle bleibt unverändert')
    suite.check('A/B investigation retains own explanations and the actual saved baseline',
                suite.snapshot(page)['inquiry']['slitComparison'] == saved)
    for task, spacing, width, description in (
        ('spacing', '1', '0.5', 'Interferenzstreifen liegen weiter auseinander'),
        ('width', '2', '0.8', 'Die breite Hülle wird schmaler'),
        ('width', '2', '0.2', 'Die breite Hülle wird breiter'),
    ):
        page.locator('#slit-investigate-' + task).click()
        page.locator('#course-model-g').fill(spacing)
        page.locator('#course-model-b').fill(width)
        page.locator('#slit-investigation-compare').click()
        expect(page.locator('#slit-investigation-result')).to_contain_text(description)
        if task == 'width':
            expect(page.locator('#slit-investigation-result')).to_contain_text('Abstand der feinen Interferenzstreifen bleibt gleich')
        suite.check('Direction feedback matches the actual isolated change: '+task+' g='+spacing+' b='+width,
                    suite.snapshot(page)['inquiry']['slitComparison'] == saved)
    page.locator('#slit-investigate-spacing').click()
    page.locator('#course-model-g').fill('4')
    page.locator('#course-model-b').fill('0.5')
    page.locator('#course-model-b').fill('0.7')
    page.locator('#slit-investigation-compare').click()
    expect(page.locator('#slit-investigation-result')).to_contain_text('beide Größen verändert')
    page.locator('#course-model-b').fill('0.5')
    page.locator('#slit-investigate-width').click()
    expect(page.locator('#slit-investigation-prediction')).to_have_value('')
    page.locator('#slit-investigation-compare').click()
    expect(page.locator('#slit-investigation-result')).to_contain_text('andere Größe')
    page.locator('#slit-investigate-spacing').click()
    expect(page.locator('#slit-investigation-prediction')).to_have_value(prediction)
    expect(page.locator('#slit-investigation-reason')).to_have_value(reason)
    page.locator('#slit-investigation-compare').click()
    suite.check('Spacing and width investigations have distinct temporary answers', True)
    suite.show_workspace(page, '#slit-investigation')
    suite.shot(page, 'simulation-investigation-ipad.png')
    # Restore the declared 150-V, g=2um, b=.5um model for independent numerical checks.
    prior_curve = page.locator('#slit-current-curve').get_attribute('d')
    page.locator('#course-model-g').fill('2')
    expect(page.locator('#slit-aperture svg')).to_have_attribute('aria-label', re.compile('Mittenabstand g 2,00 Mikrometer'))
    expect(page.locator('#slit-current-curve')).not_to_have_attribute('d', prior_curve)
    expect(page.locator('#slit-investigation-result')).to_have_count(0)
    settle_images(page); settle_fonts(page)
    suite.show_workspace(page, '#slit-profile')
    stable_geometry = stable_plot_geometry(page)
    point = page.locator('#slit-profile svg').first.evaluate("""e => {
      const matrix = e.getScreenCTM();
      const p = new DOMPoint(62 + 550*2/3, 336).matrixTransform(matrix);
      // Mouse-compatible click coordinates are integer CSS pixels. Validate the exact
      // inverse coordinate of the actual tap instead of expecting subpixel input.
      const actual = new DOMPoint(Math.round(p.x), Math.round(p.y));
      const native = actual.matrixTransform(matrix.inverse());
      return {x:actual.x,y:actual.y,expectedPositionM:-.003+(native.x-62)/550*.006};
    }""")
    scroll_before = page.evaluate('scrollY')
    clicked = []
    page.expose_binding('__qaSlitClick', lambda _source, event: clicked.append(event))
    page.evaluate("""() => {
      document.addEventListener('click', event => {
        const svg = document.querySelector('#slit-profile svg');
        const native = new DOMPoint(event.clientX,event.clientY).matrixTransform(svg.getScreenCTM().inverse());
        window.__qaSlitClick({x:event.clientX,y:event.clientY,nativeX:native.x,nativeY:native.y,
          target:event.target.tagName,targetId:event.target.id,
          inProfile:!!event.target.closest('#slit-profile'),scroll:scrollY,
          svgBounds:svg.getBoundingClientRect().toJSON()});
      }, {once:true,capture:true});
    }""")
    before_tap = page.evaluate("""point => {
      const top = document.elementFromPoint(point.x,point.y);
      const svg = document.querySelector('#slit-profile svg');
      return {svgBounds:svg.getBoundingClientRect().toJSON(),target:top?.tagName,targetId:top?.id,
        inProfile:!!top?.closest('#slit-profile'),spacing:document.querySelector('#course-model-g').value,
        aperture:document.querySelector('#slit-aperture svg').getAttribute('aria-label'),
        feedbackPresent:!!document.querySelector('#slit-investigation-result'),scroll:scrollY};
    }""", point)
    page.touchscreen.tap(point['x'], point['y'])
    suite.report['simulation_pointer'] = {
        'requested_css_point': point, 'scroll_before': scroll_before, 'scroll_after': page.evaluate('scrollY'),
        'stable_geometry': stable_geometry,
        'before_tap': before_tap, 'click_events': clicked,
        'actual_phase': read_phase(page), 'svg_bounds': page.locator('#slit-profile svg').first.bounding_box(),
        'native_selected_x': page.locator('#slit-selected-position').get_attribute('x1')}
    expect(page.locator('#slit-phase-panel')).to_have_js_property('open', False)
    suite.check('A real touch on the shared model strip selects its native model position without scrolling',
                abs(read_phase(page)['position-m'] - point['expectedPositionM']) < 1e-10
                and abs(page.evaluate('scrollY') - scroll_before) < 1)
    page.locator('#slit-profile').focus(); page.keyboard.press('Home')
    suite.check('The same plot selection is reachable by keyboard', abs(read_phase(page)['position-m']) < 1e-12)
    expect(page.locator('#slit-phase-panel')).to_have_js_property('open', False)
    open_details(page, '#slit-phase-panel')
    expect(page.locator('#slit-phase-panel')).to_have_js_property('open', True)
    expect(page.locator('#slit-phase-panel')).to_contain_text('außerhalb der Aufgabenstellung')
    page.locator('#slit-phase-center').click()
    for mode, expected in [('BOTH_COHERENT', 1.0), ('LEFT_ONLY', .25),
                           ('RIGHT_ONLY', .25), ('BOTH_INCOHERENT', .5)]:
        page.locator('#slit-lab-configuration').select_option(mode)
        if mode == 'BOTH_COHERENT':
            expect(page.locator('#course-slit-save')).to_be_enabled()
        else:
            expect(page.locator('#course-slit-save')).to_be_disabled()
        actual = read_phase(page)
        curve = page.locator('#slit-current-curve').get_attribute('d')
        vertices = [(float(x), float(y)) for x, y in re.findall(r'[ML]([-+0-9.eE]+) ([-+0-9.eE]+)', curve)]
        strip_gray = page.locator('#slit-profile svg').first.evaluate("""svg => {
          const cells = [...svg.querySelectorAll(':scope > rect')].filter(e =>
            Number(e.getAttribute('y')) === 321 && Number(e.getAttribute('width')) < 2);
          cells.sort((a,b) => Math.abs(Number(a.getAttribute('x'))+.5*Number(a.getAttribute('width'))-337)
                            -Math.abs(Number(b.getAttribute('x'))+.5*Number(b.getAttribute('width'))-337));
          return Number(cells[0].getAttribute('fill').match(/\\d+/)[0]);
        }""")
        suite.check('Aperture comparison preserves the common intensity scale: '+mode,
                    abs(actual['position-m']) < 1e-12
                    and abs(actual['total-intensity'] - expected) < 1e-12)
        suite.check('Actual curve and strip share the fixed central intensity scale: '+mode,
                    len(vertices) == 601 and abs(vertices[300][0] - 337) < 1e-9
                    and abs(vertices[300][1] - (235 - 180 * expected)) < 1e-9
                    and strip_gray == round(255 * expected))
    page.locator('#slit-lab-configuration').select_option('BOTH_COHERENT')
    page.locator('#slit-phase-null-positive').focus(); page.keyboard.press('Enter')
    null = read_phase(page)
    suite.check('First positive interference cancellation agrees with half a wavelength',
                null['position-m'] > 0 and abs(abs(null['phase-turns']) - .5) < 1e-10
                and null['total-intensity'] < 1e-20)
    page.locator('#slit-phase-null-negative').tap()
    negative = read_phase(page)
    suite.check('Opposite interference cancellation has opposite position and path difference',
                abs(negative['position-m'] + null['position-m']) < 1e-12
                and abs(negative['path-difference-m'] + null['path-difference-m']) < 1e-18)
    page.locator('#slit-lab-configuration').select_option('BOTH_INCOHERENT')
    incoherent = read_phase(page)
    suite.check('An incoherent comparison keeps the selected coordinate but removes mutual cancellation',
                incoherent['position-m'] == negative['position-m']
                and incoherent['total-intensity'] > .1
                and abs(incoherent['total-intensity'] - incoherent['left-intensity'] - incoherent['right-intensity']) < 1e-12)
    expect(page.locator('#slit-phase-diagram')).to_have_attribute('aria-label', re.compile('Intensitätsaddition'))
    page.locator('#slit-lab-configuration').select_option('BOTH_COHERENT')
    page.locator('#slit-phase-position').focus(); page.keyboard.press('ArrowRight')
    suite.check('Phase position is independently operable with the keyboard',
                read_phase(page)['position-m'] > negative['position-m'])
    for theme in ('dark', 'light'):
        set_theme(page, theme)
        page.locator('#slit-phase-null-positive').click()
        suite.show_workspace(page, '#slit-phase')
        expect(page.locator('#slit-phase-diagram')).to_be_visible()
        suite.check('Phase explanation fits the iPad width: '+theme,
                    page.evaluate('document.documentElement.scrollWidth<=innerWidth+1'))
        suite.shot(page, 'simulation-phase-ipad-'+theme+'.png')
    suite.check('Exploring and explaining the model never rewrites historical or personal evidence',
                preserved_work(suite.snapshot(page)) == before)
    return prediction, reason


def show_detector_data(page, visible=True):
    control = page.locator('#slit-detection-data')
    if (control.get_attribute('aria-expanded') == 'true') != visible:
        control.click()


def check_detection(suite, page):
    before = preserved_work(suite.snapshot(page))
    set_theme(page, 'dark')
    page.locator('#slit-lab-configuration').select_option('BOTH_COHERENT')
    page.locator('#course-model-g').fill('2')
    page.locator('#course-model-b').fill('0.5')
    open_details(page, '#slit-detection-panel')
    expect(page.locator('#slit-detection-play')).to_be_disabled()
    suite.check('Reduced motion leaves manually requested detections available', detector_count(page) == 0)
    empty_nodes = page.locator('#slit-detection-body *').count()
    for button, count in [('one', 1), ('hundred', 101), ('thousand', 1101)]:
        page.locator('#slit-detection-'+button).click()
        suite.check('Actual requested detection batch reaches '+str(count), detector_count(page) == count)
    show_detector_data(page)
    bins = histogram(page)
    expected = expected_bins(2, .5)
    suite.check('Observed histogram contains precisely the displayed number of detections',
                len(bins) == 60 and all(n >= 0 for n, _ in bins) and sum(n for n, _ in bins) == 1101)
    suite.check('Expected counts are normalized to the same finite observation window',
                abs(sum(value for _, value in bins) - 1101) < .0001)
    suite.check('Displayed theoretical bins agree with independent integration of the coherent aperture law',
                all(abs(value / 1101 - probability) < 2e-5 for (_, value), probability in zip(bins, expected)))
    seed = page.locator('#slit-detection-seed').text_content()
    show_detector_data(page, False)
    original_pixels = canvas_digest(page)
    suite.show_workspace(page, '#slit-detection-body')
    suite.shot(page, 'simulation-detections-ipad-dark.png')
    set_theme(page, 'light')
    light_pixels = canvas_digest(page)
    set_theme(page, 'dark')
    suite.check('The same detections redraw with the theme without resampling or losing hits',
                light_pixels != original_pixels and canvas_digest(page) == original_pixels
                and detector_count(page) == 1101)
    page.locator('#slit-detection-replay').click()
    show_detector_data(page)
    suite.check('Replay starts an empty series with the same seed', detector_count(page) == 0
                and page.locator('#slit-detection-seed').text_content() == seed)
    show_detector_data(page, False)
    for button in ('one', 'hundred', 'thousand'):
        page.locator('#slit-detection-'+button).click()
    replay_pixels = canvas_digest(page)
    show_detector_data(page)
    suite.check('Identical seed and batch count reproduce real detector pixels and histogram',
                replay_pixels == original_pixels and histogram(page) == bins)
    show_detector_data(page, False)
    page.locator('#slit-detection-new').click()
    show_detector_data(page)
    suite.check('A new series clears only detections and uses a different seed', detector_count(page) == 0
                and page.locator('#slit-detection-seed').text_content() != seed)
    show_detector_data(page, False)
    for _ in range(10):
        page.locator('#slit-detection-thousand').click()
    suite.check('Detector memory has the stated 10000-hit bound', detector_count(page) == 10000)
    suite.check('Ten thousand detections add no per-hit DOM elements',
                page.locator('#slit-detection-canvas').count() == 1
                and abs(page.locator('#slit-detection-body *').count() - empty_nodes) < 100)
    for button in ('one', 'hundred', 'thousand', 'play'):
        expect(page.locator('#slit-detection-'+button)).to_be_disabled()
    show_detector_data(page)
    suite.check('Bounded histogram still counts every detected event exactly once',
                sum(n for n, _ in histogram(page)) == 10000)
    show_detector_data(page, False)
    page.locator('#course-model-g').fill('3')
    expect(page.locator('#slit-detection-notice')).to_contain_text('Parameter geändert')
    suite.check('Changing physical model parameters cannot mix old and new detector distributions',
                detector_count(page) == 0)
    page.locator('#slit-detection-hundred').click()
    page.locator('#course-model-b').fill('0.7')
    suite.check('A slit-width change also clears detections from the previous physical model', detector_count(page) == 0)
    page.locator('#course-model-b').fill('0.5')
    page.locator('#slit-detection-hundred').click()
    page.locator('#slit-lab-configuration').select_option('LEFT_ONLY')
    suite.check('Changing aperture configuration starts a distinct detector series', detector_count(page) == 0)
    page.locator('#slit-detection-hundred').click()
    show_detector_data(page)
    single_bins = histogram(page)
    page.locator('#slit-lab-configuration').select_option('RIGHT_ONLY')
    page.locator('#slit-detection-hundred').click()
    suite.check('Left and right isolated slits share the same normalized far-field envelope',
                [value for _, value in histogram(page)] == [value for _, value in single_bins])
    page.locator('#slit-lab-configuration').select_option('BOTH_INCOHERENT')
    page.locator('#slit-detection-hundred').click()
    suite.check('Incoherent detections use the same conditional envelope without inventing an emission rate',
                [value for _, value in histogram(page)] == [value for _, value in single_bins])
    page.locator('#slit-lab-configuration').select_option('BOTH_COHERENT')
    page.locator('#slit-detection-hundred').click()
    show_detector_data(page, False)
    retained = detector_count(page)
    suite.section(page, 'debroglie'); suite.route(page, 'joensson')
    open_details(page, '#slit-detection-panel')
    suite.check('Chapter changes retain the temporary detector series without continuing it',
                detector_count(page) == retained)
    set_theme(page, 'light')
    suite.show_workspace(page, '#slit-detection-body')
    suite.shot(page, 'simulation-detections-ipad-light.png')
    suite.check('Synthetic detections never become historical measurement or personal evidence',
                preserved_work(suite.snapshot(page)) == before)


def check_detection_motion(suite, page):
    show_detector_data(page, False)
    page.emulate_media(reduced_motion='no-preference')
    expect(page.locator('#slit-detection-play')).to_be_enabled()
    expect(page.locator('#slit-detection-play')).to_have_attribute('aria-pressed', 'false')
    suite.show_workspace(page, '#slit-detection-body')
    count = detector_count(page)
    page.locator('#slit-detection-play').click()
    expect(page.locator('#slit-detection-play')).to_have_attribute('aria-pressed', 'true')
    page.wait_for_function(r"n => Number(document.querySelector('#slit-detection-count').textContent.replace(/\D/g,'')) > n", arg=count)
    page.emulate_media(reduced_motion='reduce')
    expect(page.locator('#slit-detection-play')).to_be_disabled()
    paused = detector_count(page)
    page.wait_for_timeout(550)
    suite.check('Reduced motion stops real pending collection timers', detector_count(page) == paused)
    page.emulate_media(reduced_motion='no-preference')
    expect(page.locator('#slit-detection-play')).to_have_attribute('aria-pressed', 'false')
    page.wait_for_timeout(550)
    suite.check('Reactivating motion does not silently restart detector collection', detector_count(page) == paused)
    page.locator('#slit-detection-play').click()
    page.evaluate('window.scrollTo(0,0)')
    expect(page.locator('#slit-detection-play')).to_have_attribute('aria-pressed', 'false')
    offscreen = detector_count(page)
    page.wait_for_timeout(550)
    suite.show_workspace(page, '#slit-detection-body')
    page.wait_for_timeout(550)
    suite.check('An offscreen detector stops and stays paused after scrolling back', detector_count(page) == offscreen)
    page.locator('#slit-detection-play').click()
    page.locator('#motion-toggle').click()
    expect(page.locator('#slit-detection-play')).to_be_disabled()
    still = detector_count(page)
    page.wait_for_timeout(550)
    page.locator('#motion-toggle').click()
    expect(page.locator('#slit-detection-play')).to_have_attribute('aria-pressed', 'false')
    page.wait_for_timeout(550)
    suite.check('The visible motion toggle stops collection and requires a new explicit start', detector_count(page) == still)
    page.locator('#slit-detection-play').click()
    suite.section(page, 'debroglie'); suite.route(page, 'joensson')
    open_details(page, '#slit-detection-panel')
    expect(page.locator('#slit-detection-play')).to_have_attribute('aria-pressed', 'false')
    restored = detector_count(page)
    page.wait_for_timeout(550)
    suite.check('Leaving and returning to the chapter never restarts an old collector', detector_count(page) == restored)
    suite.show_workspace(page, '#slit-detection-body')
    page.locator('#slit-detection-play').click()
    page.locator('#slit-detection-panel > summary').click()
    page.wait_for_timeout(550)
    open_details(page, '#slit-detection-panel')
    expect(page.locator('#slit-detection-play')).to_have_attribute('aria-pressed', 'false')
    closed = detector_count(page)
    page.wait_for_timeout(550)
    suite.check('Closing the optional detector panel stops collection without a latent restart', detector_count(page) == closed)
    page.emulate_media(reduced_motion='reduce')


def copy_personal_report(page):
    """Exercise the primary text handoff; temporary simulation work must stay excluded."""
    if not page.locator('#personal-evidence-copy').is_visible():
        page.get_by_text('Deinen Arbeitsbericht lesen und kopieren', exact=True).click()
    page.locator('#personal-evidence-copy').click()
    expect(page.locator('#personal-evidence-copy-status')).to_have_text('Arbeitsbericht kopiert.')
    return page.evaluate('navigator.clipboard.readText()')


def simulation_extensions(suite):
    suite.report['simulation_harness_sha256'] = hashlib.sha256(Path(__file__).read_bytes()).hexdigest()
    page = suite.page(viewport={'width': 1080, 'height': 810}, has_touch=True,
                      permissions=['clipboard-read', 'clipboard-write'])
    suite.seed(page)
    suite.route(page, 'joensson/messung')
    page.locator('#jonsson-justification').fill('QA-SYNTHETISCH: gesicherte historische Auswahl bleibt vom Modell getrennt.')
    page.locator('#jonsson-seal-selection').click()
    initial = preserved_work(suite.snapshot(page))
    suite.section(page, 'fazit')
    original_report = copy_personal_report(page)
    prediction, reason = check_investigation_and_phase(suite, page)
    check_detection(suite, page)
    check_detection_motion(suite, page)
    suite.route(page, 'joensson')
    open_details(page, '#slit-investigation')
    page.locator('#slit-investigate-spacing').click()
    expect(page.locator('#slit-investigation-prediction')).to_have_value(prediction)
    expect(page.locator('#slit-investigation-reason')).to_have_value(reason)
    suite.check('Own investigation wording survives the same-tab chapter excursion', True)
    reload_with_images(page)
    open_details(page, '#slit-investigation')
    expect(page.locator('#slit-investigation-prediction')).to_have_value(prediction)
    expect(page.locator('#slit-investigation-reason')).to_have_value(reason)
    open_details(page, '#slit-detection-panel')
    suite.check('Reload restores investigation notes while clearing generated detections and retaining scientific work',
                detector_count(page) == 0 and preserved_work(suite.snapshot(page)) == initial)
    suite.section(page, 'fazit')
    copied_report = copy_personal_report(page)
    suite.check('Model explanations and generated detections stay separate from the scientific personal report',
                copied_report == original_report and prediction not in copied_report and reason not in copied_report
                and 'gesicherte historische Auswahl bleibt vom Modell getrennt' in copied_report)
    page.context.close()
    check_extra_layouts(suite)
    check_restored_comparison(suite)


def check_extra_layouts(suite):
    """Short landscape and mouse layout probes stay inside the existing scenario."""
    for name, viewport, touch in [('ipad-short', {'width': 1024, 'height': 680}, True),
                                  ('laptop', {'width': 1440, 'height': 900}, False)]:
        page = suite.page(viewport=viewport, has_touch=touch)
        suite.route(page, 'joensson')
        open_details(page, '#slit-phase-panel')
        open_details(page, '#slit-detection-panel')
        for theme in ('dark', 'light'):
            set_theme(page, theme)
            page.locator('#slit-phase-center').click()
            if touch:
                page.locator('#slit-phase-null-positive').tap()
                page.locator('#slit-detection-hundred').tap()
            else:
                page.locator('#slit-phase-null-positive').focus(); page.keyboard.press('Enter')
                page.locator('#slit-detection-hundred').focus(); page.keyboard.press('Enter')
            suite.check('Cancellation preset uses the real shared model '+name+' '+theme,
                        abs(read_phase(page)['phase-turns'] - .5) < 1e-10)
            for selector in ('#slit-lab-configuration', '#slit-phase-position', '#slit-phase-center',
                             '#slit-phase-null-positive', '#slit-detection-hundred', '#slit-detection-play'):
                bounds = page.locator(selector).bounding_box()
                suite.check('New control has a visible 44px interaction target '+selector+' '+name+' '+theme,
                            bounds['height'] >= 44 and bounds['width'] >= 44)
            suite.check('Optional simulation surfaces cause no page-level horizontal overflow '+name+' '+theme,
                        page.evaluate('document.documentElement.scrollWidth <= innerWidth+1'))
            suite.show_workspace(page, '#slit-phase-diagram')
            suite.shot(page, 'simulation-phase-'+name+'-'+theme+'.png')
            suite.show_workspace(page, '#slit-detection-canvas')
            suite.shot(page, 'simulation-detector-'+name+'-'+theme+'.png')
        page.context.close()


def check_restored_comparison(suite):
    """A valid older experiment may use different U/L: changing g is no isolated test then."""
    for voltage, distance in ((300.0, 10.0), (150.0, 20.0)):
        page = suite.page()
        suite.seed(page)
        # The common seed deliberately omits StationWork defaults. One ordinary UI
        # save serializes them before comparison, so restored-state normalization
        # is not mistaken for changed answers or newly recorded help contact.
        suite.route(page, 'joensson')
        page.locator('#course-model-g').fill('3'); settle_render(page)
        page.locator('#course-model-g').fill('2'); settle_render(page)
        suite.section(page, 'debroglie')
        before = preserved_work(suite.snapshot(page))
        candidate = suite.snapshot(page)
        candidate['modelSlitSpacingUm'] = 4.0
        candidate['inquiry']['slitComparison'] = {
            'voltageV': voltage, 'distanceM': distance, 'spacingUm': 2.0, 'widthUm': .5}
        # This is a legacy local-storage fixture, not a supported file-import UI.
        page.evaluate('value => localStorage.setItem("materiewelle.course.session.v1", value)',
                      json.dumps(candidate))
        reload_with_images(page)
        suite.route(page, 'joensson')
        open_details(page, '#slit-investigation')
        page.locator('#slit-investigation-compare').click()
        expect(page.locator('#slit-investigation-result')).to_contain_text('A verwendet eine andere Spannung oder Entfernung')
        after = suite.snapshot(page)
        suite.report.setdefault('simulation_restored_comparisons', []).append({
            'voltage': voltage, 'distance': distance, 'actual_comparison': after['inquiry']['slitComparison'],
            'work_changes': {key: {'before': before[key], 'after': value}
                             for key, value in preserved_work(after).items() if value != before[key]}})
        suite.check('Restored A with different U/L cannot be described as an isolated slit-spacing change '+str((voltage, distance)),
                    after['inquiry']['slitComparison'] == candidate['inquiry']['slitComparison']
                    and preserved_work(after) == before)
        page.locator('#course-slit-save').click()
        page.locator('#course-model-g').fill('2')
        page.locator('#slit-investigation-compare').click()
        expect(page.locator('#slit-investigation-result')).to_contain_text('Interferenzstreifen liegen weiter auseinander')
        expect(page.locator('#slit-investigation-result')).to_contain_text('die breite Hülle bleibt unverändert')
        suite.check('Remembering a new A restores a valid isolated comparison '+str((voltage, distance)), True)
        page.context.close()
