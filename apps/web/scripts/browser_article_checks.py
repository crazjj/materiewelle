"""The current De-Broglie article contract, separate from historical UI scenarios."""
import re
from playwright.sync_api import expect
from browser_assets import reload_with_images
from browser_theme import set_theme

class ArticleChecks:
    def article_checks(self, page):
        expect(page.locator("#debroglie-article")).to_be_visible()
        expect(page.locator("#article-title")).to_contain_text("Wellenlänge")
        nav = page.locator("header nav").first
        self.check("Four coherent primary destinations", nav.locator("button").count() == 4
            and all(title in nav.inner_text() for title in ("De Broglie", "Davisson", "Jönsson", "Fazit")))
        expect(page.locator("#course-task-answer")).to_have_value("")
        expect(page.locator("#article-result")).not_to_be_visible()
        self.check("Computed control is hidden until deliberately opened", True)
        self.check("Own calculation is not prefilled", self.snapshot(page).get("work",{}).get("S06_CALCULATION",{}).get("numericAnswer","") == "")
        expect(page.locator('[id^="article-line-"]')).to_have_count(6)
        expect(page.locator("#article-play,#article-follow,#article-next-line,#article-all-lines")).to_have_count(0)
        self.check("All six reasoning steps are immediately available without playback controls", True)
        page.locator("#article-reading-choice-1").click()
        expect(page.locator("#article-line-1")).to_be_focused()
        expect(page.locator("#article-line-0")).to_be_attached()
        page.locator("#article-reading-choice-5").click()
        expect(page.locator("#article-line-5")).to_be_focused()
        self.check("Explicit step selection preserves the complete explanation", page.locator('[id^="article-line-"]').count() == 6)
        self.check("Derivation uses actual SVG mathematics", page.locator("#article-herleitung img[src$='.svg'], #article-herleitung svg").count() >= 6)
        self.route(page,"davisson-germer")
        self.route(page,"debroglie")
        expect(page.locator("#article-line-5")).to_be_attached()
        self.check("Open derivation survives navigation", self.snapshot(page)["work"]["S05_DERIVATION"]["explanationFurthestStep"] == 5)
        page.locator("#course-task-answer").fill("100 nm")
        page.locator("#course-numeric-check").click()
        expect(page.locator("#course-numeric-feedback")).not_to_have_attribute("data-answer-status", "CORRECT")
        page.locator("#course-task-answer").fill("1,00137·10^-10 m")
        page.locator("#course-numeric-check").click()
        expect(page.locator("#course-numeric-feedback")).to_have_attribute("data-answer-status", "CORRECT")
        self.check("150V check distinguishes correct SI answer from wrong scale", True)
        page.locator("#article-model-disclosure > summary").click()
        self.check("New visitors have no remembered comparison before choosing one",
            self.snapshot(page)["deBroglie"]["comparisonVoltage"] is None
            and "Vergleichseinstellung:" not in page.locator("#article-model-disclosure").inner_text())
        page.get_by_role("button", name="Einstellung merken", exact=True).click()
        self.check("Remembering explicitly stores the current 150V setting",
            self.snapshot(page)["deBroglie"]["comparisonVoltage"] == 150)
        drawn_waves = {}
        for voltage, expected in ((150,100.13726074475609),(600,50.068630372378045)):
            page.locator("#article-voltage").fill(str(voltage))
            expect(page.locator("#article-voltage")).to_have_attribute("aria-invalid", "false")
            expect(page.locator("#article-result")).to_contain_text("pm")
            page.wait_for_function("v => JSON.parse(localStorage.getItem('materiewelle.course.session.v1')).deBroglie.lastValidVoltage === v", arg=voltage)
            expect(page.locator("#article-result")).to_contain_text(re.compile(re.escape(f"{expected:.3f}").replace(r"\.", "[.,]")))
            drawn_waves[voltage] = page.locator("#article-model path.article-wave").get_attribute("d")
            result_text = page.locator("#article-result").text_content()
            match = re.search(r"([0-9]+(?:[.,][0-9]+)?)\s*pm", result_text)
            self.check(f"Rendered wavelength at {voltage}V matches independent calculation", match is not None and abs(float(match.group(1).replace(',','.'))-expected) < 0.001)
        self.check("Parameter changes update the actual wavelength drawing", drawn_waves[150] != drawn_waves[600])
        self.check("Varying voltage preserves the deliberately remembered comparison",
            self.snapshot(page)["deBroglie"]["comparisonVoltage"] == 150)
        result_before = page.locator("#article-result").text_content()
        page.locator("#article-voltage").fill("keine Spannung")
        expect(page.locator("#article-voltage")).to_have_attribute("aria-invalid", "true")
        self.check("Invalid edited voltage preserves last valid diagram/result", page.locator("#article-result").text_content() == result_before
            and self.snapshot(page)["deBroglie"]["lastValidVoltage"] == 600
            and page.locator("#article-model path.article-wave").get_attribute("d") == drawn_waves[600])
        page.locator("#article-voltage").fill("150")
        set_theme(page,"light")
        page.locator("#article-herleitung").scroll_into_view_if_needed()
        self.shot(page,"derivation-light.png")
        page.locator("#article-rechnung").scroll_into_view_if_needed()
        self.shot(page,"own-calculation.png")
        reload_with_images(page)
        expect(page.locator("html")).to_have_attribute("data-theme","light")
        expect(page.locator("#course-task-answer")).to_have_value("1,00137·10^-10 m")
        self.check("Theme, own answer and derivation survive reload", self.snapshot(page)["work"]["S05_DERIVATION"]["explanationFurthestStep"] == 5)
        self.check("Remembered comparison survives reload", self.snapshot(page)["deBroglie"]["comparisonVoltage"] == 150)
        for fragment, target in (("lernen/spannung-zur-vorhersage","#article-herleitung"),
                ("ausarbeitung/rezeption","#article-rezeption"),("debroglie/rechnung","#article-rechnung")):
            self.route(page,fragment)
            expect(page.locator(target)).to_be_visible()
            expect(page.locator(target)).to_be_focused()
            page.wait_for_function("selector => {const r=document.querySelector(selector).getBoundingClientRect();return r.top >= 0 && r.top < innerHeight * 0.65;}", arg=target)
            self.check("Legacy/article route focuses and positions its corresponding explanation: " + fragment, True)
