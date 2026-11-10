"""Current browser plumbing; historical Run-01 scenarios are not imported.

Synthetic fixtures only. Screenshots and raw browser events retain their artifact binding.
"""
import json
import re
from urllib.parse import urlsplit
from browser_assets import settle_images, settle_fonts
from browser_evidence import served, sha

STORAGE = "materiewelle.course.session.v1"

class Checks:
    def __init__(self, browser, url, output, report):
        self.browser, self.url, self.output, self.report = browser, url, output, report
        self.pages = []
        self.current = None

    def check(self, name, condition=True):
        self.report["checks"].append({"name": name, "passed": bool(condition)})
        if not condition:
            raise AssertionError(name)

    def observe(self, page):
        page.on("pageerror", lambda error: self.report["page_errors"].append(str(error)))
        page.on("console", lambda message: self.report["console_messages"].append(
            {"type": message.type, "text": message.text}) if message.type in {"warning", "error"} else None)
        page.on("requestfailed", lambda request: self.report["request_failures"].append(
            {"url": request.url, "failure": request.failure}))
        page.on("response", lambda response: self.report["http_errors"].append(
            {"url": response.url, "status": response.status}) if response.status >= 400 else None)
        page.on("request", lambda request: self.report["outbound_requests"].append(request.url)
            if urlsplit(request.url).scheme in {"http", "https"} and urlsplit(request.url).netloc != urlsplit(self.url).netloc else None)
        page.on("request", lambda request: self.report["forbidden_requests"].append(request.url)
            if re.search(r"/learning-docs/|arbeitsfassung\.pdf|praesentation\.pdf", request.url) else None)

    def snapshot(self, page):
        raw = page.evaluate("key => localStorage.getItem(key)", STORAGE)
        return json.loads(raw) if raw is not None else {}

    def shot(self, page, name):
        settle_images(page)
        settle_fonts(page)
        target = self.output / name
        page.screenshot(path=str(target), full_page=False, animations="disabled")
        self.report["screenshots"].append({"file": name, "sha256": sha(target.read_bytes()),
            "viewport": page.viewport_size, "url": page.url,
            "theme": page.locator("html").get_attribute("data-theme"),
            "fixture": "Synthetic QA inputs, not personal learner work"})

    def contrast(self, page, selector, theme):
        colors = page.locator(selector).first.evaluate(r"""el => {
          const rgba=value=>value.match(/[\d.]+/g).map(Number);
          const chain=[];for(let e=el;e;e=e.parentElement)chain.unshift(e);
          let bg=[255,255,255], gradient=false;
          for(const e of chain){const s=getComputedStyle(e), c=rgba(s.backgroundColor), a=c.length>3?c[3]:1;
            bg=bg.map((v,i)=>c[i]*a+v*(1-a));gradient ||= s.backgroundImage!=='none';}
          const c=rgba(getComputedStyle(el).color),a=c.length>3?c[3]:1;
          return {foreground:bg.map((v,i)=>c[i]*a+v*(1-a)),background:bg,gradient};
        }""")
        def luminance(rgb):
            channels=[c/255 for c in rgb]
            linear=[c/12.92 if c<=0.04045 else ((c+0.055)/1.055)**2.4 for c in channels]
            return sum(c*w for c,w in zip(linear,(0.2126,0.7152,0.0722)))
        first,second=luminance(colors["foreground"]),luminance(colors["background"])
        ratio=(max(first,second)+0.05)/(min(first,second)+0.05)
        self.report["contrast_samples"].append({"selector":selector,"theme":theme,"ratio":ratio,**colors})
        self.check(f"Sampled text contrast {theme} {selector} reaches 4.5:1 on a solid background", not colors["gradient"] and ratio >= 4.5)

    def download(self, page, selector, filename):
        with page.expect_download() as event:
            page.locator(selector).click()
        target = self.output / filename
        event.value.save_as(target)
        self.report["downloads"].append({"file": filename, "sha256": sha(target.read_bytes())})
        return target.read_bytes()
