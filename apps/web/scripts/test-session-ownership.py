#!/usr/bin/env python3
"""Real-browser regressions for one writer per origin and math-independent rescue.

Browser plugin not available: Python Playwright and system Chromium. All input is
synthetic. Uses actual static files and binds evidence to their complete manifest.
Does not claim real iPad/Safari, old app compatibility or human learning evidence.
"""
from __future__ import annotations

import argparse
from datetime import datetime, timezone
import hashlib
import json
import os
from pathlib import Path
import re
import traceback
from urllib.parse import urljoin

from playwright.sync_api import expect, sync_playwright

PROJECT = Path(__file__).resolve().parents[1]
import browser_harness as one
STORAGE = one.STORAGE
DRAFTS = "materiewelle.ui-drafts.v1"
READING = "materiewelle.reading.position.v1"
LOCK = "materiewelle.personal-work.writer.v1"
SCENARIOS = ("two_tabs", "simultaneous", "navigation_lifecycle", "lock_fallback", "formula_rescue", "storage_failure")


def sha(raw):
    return hashlib.sha256(raw).hexdigest()


class Checks(one.Checks):
    def context(self, script=None, **options):
        settings = dict(viewport={"width": 1440, "height": 900}, locale="de-DE",
            reduced_motion="reduce", permissions=["clipboard-read", "clipboard-write"])
        settings.update(options)
        context = self.browser.new_context(**settings)
        if script:
            context.add_init_script(script)
        return context

    def create(self, context):
        page = context.new_page()
        self.pages.append(page)
        self.current = page
        self.observe(page)
        return page

    def load(self, page):
        response = page.goto(self.url + "#debroglie", wait_until="networkidle")
        expect(page).to_have_title(re.compile("Materiewelle"))
        expect(page.locator("#article-notes")).to_be_visible()
        self.check("Page identity and successful entry", response is not None and response.status == 200)
        self.check("No framework overlay", page.locator("#webpack-dev-server-client-overlay,vite-error-overlay,nextjs-portal").count() == 0)
        self.no_file_tools(page)
        return page

    def no_file_tools(self, page):
        self.check("No session-file import, export or rescue controls", page.locator(
            "#course-export,#session-import-file,#session-draft-export,#session-stored-export,#session-stored-draft-export").count() == 0)

    def workspace(self, page):
        if not page.locator("#session-work-copy").is_visible():
            page.locator("#session-status").get_by_text("Arbeit auf diesem Gerät", exact=True).click()
        expect(page.locator("#session-work-copy")).to_be_visible()

    def question(self, page, text):
        if not page.locator("#compact-tutor-question").is_visible():
            page.locator("#compact-tutor-open").click()
        expect(page.locator("#compact-tutor-question")).to_be_visible()
        page.locator("#compact-tutor-question").fill(text)

    def note(self, page, text):
        page.locator("#article-notes").fill(text)
        expect(page.locator("#article-notes")).to_have_value(text)

    def raw(self, page, key=STORAGE):
        return page.evaluate("key => localStorage.getItem(key)", key)

    def local(self, page):
        return json.loads(self.raw(page))

    def memory_status(self, page):
        expect(page.locator("#course-storage-status")).to_contain_text(re.compile("Tab|gespeichert|Speicher"))

    def rescue(self, page):
        self.workspace(page)
        page.locator("#session-work-copy").click()
        expect(page.locator("#session-work-copy-status")).to_contain_text(re.compile("[Kk]opiert"))
        text = page.evaluate("navigator.clipboard.readText()")
        self.check("Rescue is readable text, not a disguised restore JSON", text.startswith("Arbeitsstand") and not text.lstrip().startswith("{"))
        return text

    def two_tabs(self):
        self.reading_in_second_tab()
        context = self.context()
        try:
            a = self.load(self.create(context))
            self.note(a, "QA A: eigene Rechnung bleibt erhalten.")
            self.question(a, "QA A: Warum ist die Energie positiv?")
            a.wait_for_function("key => JSON.parse(localStorage.getItem(key))?.questions?.DE_BROGLIE === 'QA A: Warum ist die Energie positiv?'", arg=DRAFTS)
            a_state, a_drafts = self.raw(a), self.raw(a, DRAFTS)
            b = self.load(self.create(context))
            self.memory_status(b)
            expect(b.locator("#article-notes")).to_be_disabled()
            expect(b.locator("#compact-tutor-open")).to_be_enabled()
            expect(b.locator("#course-storage-status")).to_contain_text("in einem anderen Tab")
            expect(b.locator("#session-storage-alert")).to_have_count(0)
            self.check("New second tab cannot overwrite owner's scientific work", self.raw(a) == a_state)
            self.check("New second tab cannot overwrite owner's tutor question", self.raw(a, DRAFTS) == a_drafts)
            own = self.rescue(b)
            self.check("Read-only tab can copy the work and question it opened", "QA A: eigene Rechnung bleibt erhalten." in own and "QA A: Warum ist die Energie positiv?" in own)
            expect(b.locator("#session-delete-open")).to_be_disabled()
            self.shot(b, "second-tab-readonly-copy.png")
            self.note(a, "QA A: neuester Stand während Tab B liest.")
            a_state = self.raw(a)
            a.close()
            b.bring_to_front()
            expect(b.locator("#article-notes")).to_be_disabled()
            self.check("Closing writer never automatically promotes the stale second tab", self.raw(b) == a_state)
            b.locator("#session-reload-current").click()
            expect(b.locator("#article-notes")).to_be_enabled()
            expect(b.locator("#article-notes")).to_have_value("QA A: neuester Stand während Tab B liest.")
            self.question(b, "QA B: neue Frage nach bewusstem Neuladen.")
            self.note(b, "QA B: frischer Stand darf wieder speichern.")
            self.check("Explicit reload acquires ownership from fresh stored baseline", self.local(b)["work"]["S06_CALCULATION"]["note"] == "QA B: frischer Stand darf wieder speichern.")
            b.wait_for_function("key => JSON.parse(localStorage.getItem(key))?.questions?.DE_BROGLIE === 'QA B: neue Frage nach bewusstem Neuladen.'", arg=DRAFTS)
            b.reload(wait_until="networkidle")
            expect(b.locator("#article-notes")).to_have_value("QA B: frischer Stand darf wieder speichern.")
            self.check("Reload releases the old document lock and restores own changes", True)
        finally:
            context.close()

    def reading_in_second_tab(self):
        keys = (STORAGE, DRAFTS, READING)
        script = """window.__qaPersonalWrites = [];
            const original = Storage.prototype.setItem;
            Storage.prototype.setItem = function(key, value) {
                if (KEYS.includes(key)) window.__qaPersonalWrites.push(key);
                return original.call(this, key, value);
            };""".replace("KEYS", json.dumps(keys))
        context = self.context(script, viewport={"width": 1080, "height": 810}, has_touch=True)
        try:
            a = self.create(context)
            a.goto(self.url, wait_until="networkidle")
            expect(a.locator("#course-storage-status")).to_contain_text(re.compile("aktiv|gespeichert"))
            initial = {key: self.raw(a, key) for key in keys}
            b = self.create(context)
            b.goto(self.url + "#davisson-germer", wait_until="networkidle")
            expect(b.locator("#course-storage-status")).to_contain_text("in einem anderen Tab")
            expect(b.locator("#session-storage-alert")).to_have_count(0)
            expect(b.locator("#dg-lesson-next")).to_be_enabled()
            b.locator("#dg-lesson-next").tap()
            b.locator("#dg-lesson-next").focus(); b.keyboard.press("Enter")
            expect(b.locator("#dg-aufbau figure")).to_have_attribute("data-step", "2")
            b.locator("#dg-lesson-all").tap()
            expect(b.locator("[data-dg-lesson-step]")).to_have_count(6)
            b.locator("#dg-source-notes > summary").click()
            expect(b.locator("#dg-source-notes")).to_have_attribute("open", "")
            for selector in ("#dg-observation-note", "#dg-prediction-input", "#dg-geometry-input", "#dg-conclusion-note"):
                expect(b.locator(selector)).to_be_disabled()
            b.locator("#dg-lesson-all").tap()
            b.locator("#dg-lesson-footer-next").tap()
            expect(b.locator("#dg-beobachtung")).to_be_focused()
            b.locator("#dg-lesson-next").tap()
            expect(b.locator("#dg-bragg")).to_be_focused()
            b.locator("#dg-lesson-next").tap(); b.locator("#dg-lesson-next").tap()
            expect(b.locator("#dg-bragg .article-formula")).to_have_count(2)
            self.question(b, "QA B: Diese Braggfrage bleibt nur in diesem Tab.")
            expect(b.locator("#session-storage-alert")).to_have_count(0)
            b.locator("#compact-tutor-copy").click()
            expect(b.locator("#compact-tutor-status")).to_contain_text(re.compile("[Kk]opiert"))
            self.check("Read-only tutor message is usable and copyable", "QA B: Diese Braggfrage" in b.evaluate("navigator.clipboard.readText()"))
            b.locator("#compact-tutor-close").click()
            self.shot(b, "second-tab-dg-reading-enabled.png")

            b.locator("#nav-debroglie").click()
            expect(b.locator("#article-notes")).to_be_disabled()
            expect(b.locator("#course-task-answer")).to_be_disabled()
            b.get_by_role("button", name="Kontrollrechnung öffnen", exact=True).click()
            expect(b.locator("#course-worked-calculation")).to_be_visible()
            b.locator("#nav-joensson").click()
            expect(b.locator("#jonsson-justification")).to_be_disabled()
            expect(b.locator("#measurement-profile")).to_be_disabled()
            expect(b.locator("#jonsson-show-reference")).to_be_disabled()
            expect(b.locator("#jonsson-reference-status")).to_contain_text("im anderen Tab")
            b.get_by_text("Bildansicht und Bedienung", exact=True).click()
            b.get_by_role("button", name="Gesamtbild zeigen", exact=True).click()
            expect(b.locator("#joensson-messung")).to_contain_text("Gesamtbild: links Einspalt")
            b.locator("#course-nav").click()
            expect(b.locator("#conclusion-draft")).to_be_disabled()
            b.get_by_text("Deinen Arbeitsbericht lesen und kopieren", exact=True).click()
            self.check("Reading every chapter does not alter the owner's work, drafts or bookmark",
                       {key: self.raw(a, key) for key in keys} == initial)
            self.check("Read-only reading makes no hidden personal-storage write attempts",
                       b.evaluate("window.__qaPersonalWrites") == [])
            a.close()
            expect(b.locator("#conclusion-draft")).to_be_disabled()
            self.workspace(b)
            b.once("dialog", lambda dialog: dialog.dismiss())
            b.locator("#session-reload-current").click()
            expect(b.locator("#conclusion-draft")).to_be_disabled()
            self.check("Cancelling reload preserves the reader's unsaved tutor question",
                       "QA B: Diese Braggfrage" in self.rescue(b))
            b.once("dialog", lambda dialog: dialog.accept())
            b.locator("#session-reload-current").click()
            expect(b.locator("#conclusion-draft")).to_be_enabled()
            self.check("A landing-only owner still requires explicit reader reload before editing", True)
        finally:
            context.close()

    def simultaneous(self):
        context = self.context()
        try:
            pages = [self.create(context), self.create(context)]
            for page in pages:
                page.goto(self.url + "#debroglie", wait_until="commit")
            for page in pages:
                expect(page.locator("#article-notes")).to_be_visible()
                page.wait_for_load_state("networkidle")
            held = pages[0].evaluate("async name => (await navigator.locks.query()).held.filter(x => x.name === name)", LOCK)
            self.check("Concurrent startup holds exactly one exclusive writer lock", len(held) == 1 and held[0]["mode"] == "exclusive")
            owners = [p for p in pages if p.locator("#article-notes").is_enabled()]
            readers = [p for p in pages if p.locator("#article-notes").is_disabled()]
            self.check("Concurrent startup exposes one writer and one read-only view", len(owners) == len(readers) == 1)
            self.note(owners[0], "QA simultaneous owner")
            stored = self.raw(owners[0])
            self.memory_status(readers[0])
            self.rescue(readers[0])
            self.check("Read-only losing requester does not overwrite the winner", self.raw(readers[0]) == stored)
        finally:
            context.close()

    def navigation_lifecycle(self):
        context = self.context("window.__qaPageShows = []; addEventListener('pageshow', event => window.__qaPageShows.push(event.persisted));")
        try:
            a = self.load(self.create(context))
            self.note(a, "QA before leaving the app")
            a.route("**/__qa-other-document", lambda route: route.fulfill(status=200,
                content_type="text/html", body='<html><head><title>QA other document</title><link rel="icon" href="data:,"></head><body>QA navigation fixture</body></html>'))
            a.goto(urljoin(self.url, "__qa-other-document"), wait_until="networkidle")
            b = self.load(self.create(context))
            self.note(b, "QA newly opened writer after navigation")
            self.check("Leaving the app releases the writer lock", self.local(b)["work"]["S06_CALCULATION"]["note"] == "QA newly opened writer after navigation")
            stored = self.raw(b)
            a.go_back(wait_until="networkidle")
            expect(a.locator("#article-notes")).to_be_visible()
            restored_from_bfcache = bool(a.evaluate("window.__qaPageShows.includes(true)"))
            self.report["navigation_lifecycle"] = {"back_forward_cache_restored": restored_from_bfcache,
                "interpretation": "Real history navigation; a retained document may retain editable memory work, a fresh denied session is read-only."}
            if a.locator("#article-notes").is_enabled():
                self.note(a, "QA returned document edits only in memory")
                own = self.rescue(a)
                self.check("Retained document can copy divergent memory work", "QA returned document edits only in memory" in own)
            else:
                self.check("Fresh history document denied the lease is read-only", a.locator("#article-notes").is_disabled())
            self.check("History return never overwrites the new writer", self.raw(b) == stored)
            self.memory_status(a)
        finally:
            context.close()

    def lock_fallback(self):
        scripts = {
            "missing": "Object.defineProperty(navigator, 'locks', {value: undefined, configurable: true});",
            "rejected": "Object.defineProperty(navigator, 'locks', {value: {request: () => Promise.reject(new Error('QA lock denial'))}, configurable: true});",
            "throws": "Object.defineProperty(navigator, 'locks', {get() {throw new Error('QA lock API unavailable');}, configurable: true});",
        }
        for fixture, script in scripts.items():
            context = self.context(script)
            try:
                page = self.load(self.create(context))
                self.note(page, "QA lock fallback: " + fixture)
                self.question(page, "QA unsaved tutor question: " + fixture)
                self.memory_status(page)
                self.check("Lock fallback does not create personal session: " + fixture, self.raw(page) is None)
                self.check("Lock fallback does not create personal UI drafts: " + fixture, self.raw(page, DRAFTS) is None)
                own = self.rescue(page)
                self.check("Lock fallback retains live answer and tutor draft in one copy: " + fixture,
                    "QA lock fallback: " + fixture in own and "QA unsaved tutor question: " + fixture in own)
                if fixture == "missing": self.shot(page, "web-lock-unavailable-copy.png")
            finally:
                context.close()

    def formula_rescue(self):
        for fixture, status, body in (("503", 503, "QA expected unavailable catalogue"),
                ("malformed", 200, '{"schemaVersion":"QA invalid"}')):
            context = self.context()
            try:
                page = self.load(self.create(context))
                note = "QA existing work despite formula " + fixture
                question = "QA separate question despite formula " + fixture
                self.note(page, note)
                self.question(page, question)
                page.wait_for_function("([key, expected]) => JSON.parse(localStorage.getItem(key))?.questions?.DE_BROGLIE === expected", arg=[DRAFTS, question])
                stored = self.raw(page)
                page.route("**/math-text/catalog.json", lambda route, *, code=status, text=body:
                    route.fulfill(status=code, content_type="application/json", body=text))
                page.reload(wait_until="networkidle")
                expect(page.locator("#typeset-status")).to_contain_text("nicht verfügbar")
                own = self.rescue(page)
                self.check("Catalogue failure leaves existing work and question copyable: " + fixture, note in own and question in own)
                self.check("Catalogue failure leaves stored scientific record untouched: " + fixture, self.raw(page) == stored)
                self.check("Catalogue failure does not replace mathematical content with plaintext", page.locator("#article-notes").count() == 0)
                self.no_file_tools(page)
                self.shot(page, "formula-" + fixture + "-copy.png")
            finally:
                context.close()

    def storage_failure(self):
        context = self.context("""const original = Storage.prototype.setItem;
            Storage.prototype.setItem = function(key, value) {
              if (key === 'materiewelle.course.session.v1' || key === 'materiewelle.ui-drafts.v1') {
                throw new DOMException('QA quota fixture', 'QuotaExceededError');
              }
              return original.call(this, key, value);
            };""")
        try:
            page = self.load(self.create(context))
            self.note(page, "QA quota: working answer retained in memory")
            self.question(page, "QA quota: question retained in memory")
            self.memory_status(page)
            self.check("Quota failure does not falsely persist scientific work", self.raw(page) is None)
            own = self.rescue(page)
            self.check("Quota failure permits live work and question rescue", "QA quota: working answer retained in memory" in own and "QA quota: question retained in memory" in own)
            page.evaluate("Object.defineProperty(navigator, 'clipboard', {value: {writeText: () => Promise.reject(new Error('QA clipboard denial'))}, configurable: true})")
            page.locator("#session-work-copy").click()
            expect(page.locator("#session-work-copy-status")).to_contain_text("Automatisches Kopieren")
            expect(page.locator("#session-work-preview")).to_be_visible()
            page.locator("#session-work-select").click()
            selected = page.evaluate("getSelection().toString()")
            preview = page.locator("#session-work-preview").text_content()
            # Chromium's native Selection omits a final line break in a pre.
            # Preserve every payload character in the DOM and every content
            # character in the selection; do not trim spaces or internal breaks.
            self.check("Denied clipboard preserves exact preview and complete native selection",
                preview == own and (selected == own or selected + "\n" == own))
            self.shot(page, "quota-manual-copy.png")
        finally:
            context.close()
        # An uncooperating older document changes storage after our tab has written.
        context = self.context()
        try:
            page = self.load(self.create(context))
            self.note(page, "QA conflict: original owner text")
            external = self.local(page)
            external["work"]["S06_CALCULATION"]["note"] = "QA external record remains protected"
            encoded = json.dumps(external, ensure_ascii=False, separators=(",", ":"))
            page.evaluate("([key,text])=>localStorage.setItem(key,text)", [STORAGE, encoded])
            self.note(page, "QA conflict: new work in this tab")
            self.check("Post-edit conflict protects the externally changed record", self.raw(page) == encoded)
            own = self.rescue(page)
            self.check("Post-edit conflict retains readable own work without making this tab read-only", "QA conflict: new work in this tab" in own and page.locator("#article-notes").is_enabled())
        finally:
            context.close()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--dist", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--chromium", default=os.environ.get("SCREENSHOT_CHROMIUM", "/usr/bin/chromium"))
    parser.add_argument("--only", nargs="+", choices=SCENARIOS)
    args = parser.parse_args()
    args.output.mkdir(parents=True, exist_ok=True)
    if any(args.output.iterdir()):
        parser.error("Choose a fresh evidence directory.")
    report = dict(recorded_at=datetime.now(timezone.utc).isoformat(),
        method="Browser plugin not available; Python Playwright/system Chromium",
        scope="fileless-session-writer-and-rescue-2026-11-08", test_harness_sha256=sha(Path(__file__).read_bytes()),
        not_tested=["real iPad/Safari", "Safari bfcache behavior", "uncooperating old open app versions", "human learning outcomes", "public deployment"],
        checks=[], scenarios=[], screenshots=[], downloads=[], page_errors=[], console_messages=[],
        request_failures=[], http_errors=[], forbidden_requests=[], outbound_requests=[])
    try:
        with one.served(args.dist) as url, sync_playwright() as pw:
            browser = pw.chromium.launch(executable_path=args.chromium, args=["--no-sandbox"])
            report.update(url=url, browser=browser.version)
            suite = Checks(browser, url, args.output, report)
            try:
                request = pw.request.new_context()
                response = request.get(urljoin(url, "manifest.json"))
                manifest = response.json()
                js = request.get(urljoin(url, "materiewellen.js"))
                suite.check("Served JavaScript matches static manifest", js.ok and sha(js.body()) == manifest["files"]["materiewellen.js"]["sha256"])
                report["release"] = dict(manifest_sha256=sha(response.body()), javascript_sha256=sha(js.body()), mode=manifest["mode"], deployment_base=manifest["deployment_base"])
                request.dispose()
                for scenario in args.only or SCENARIOS:
                    record = dict(name=scenario, passed=False)
                    report["scenarios"].append(record)
                    try:
                        getattr(suite, scenario)()
                        record["passed"] = True
                    except Exception as error:
                        record.update(failure=f"{type(error).__name__}: {error}", traceback=traceback.format_exc())
                    finally:
                        for page in suite.pages:
                            if not page.is_closed():
                                page.close()
                        suite.pages = []
                        print(json.dumps(record, ensure_ascii=False), flush=True)
                        (args.output / "report.json").write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n")
                # Explicit route fixtures are the only permitted HTTP/browser load failures.
                expected_http = [item for item in report["http_errors"] if item["url"].endswith("/math-text/catalog.json") and item["status"] == 503]
                report["expected_fixture_http_errors"] = expected_http
                expected_console = [item for item in report["console_messages"] if "503" in item["text"] and "Failed to load resource" in item["text"]]
                report["expected_fixture_console_errors"] = expected_console
                suite.check("No unexpected HTTP errors", len(expected_http) == len(report["http_errors"]))
                suite.check("No unexpected console warnings/errors", len(expected_console) == len(report["console_messages"]))
                for key in ("page_errors", "request_failures", "forbidden_requests", "outbound_requests"):
                    suite.check("No " + key, not report[key])
                suite.check("Every selected regression passed", all(item["passed"] for item in report["scenarios"]))
            finally:
                browser.close()
        report["status"] = "passed"
    except Exception as error:
        report.update(status="failed", failure=f"{type(error).__name__}: {error}")
        raise
    finally:
        (args.output / "report.json").write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n")
        print(json.dumps({"status": report.get("status"), "report": str(args.output / "report.json")}, ensure_ascii=False))


if __name__ == "__main__":
    main()
