#!/usr/bin/env python3
"""Cold image completion and genuine failure regression, using a real Typst SVG."""
import argparse
import json
import os
from pathlib import Path
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
import threading
import time
from playwright.sync_api import sync_playwright, Error
from browser_assets import settle_images, settle_fonts, close_with_images

PROJECT=Path(__file__).resolve().parents[1]


def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--chromium',default=os.environ.get('SCREENSHOT_CHROMIUM'))
    args=parser.parse_args()
    directory=PROJECT/'site/build/generated/materiewelle-product/public'
    catalogue=json.loads((directory/'math-text/catalog.json').read_text())
    svg=(directory/next(e['light'] for e in catalogue['entries'] if e['source']=='λ')).read_bytes()
    class Handler(BaseHTTPRequestHandler):
        def log_message(self,*_):pass
        def do_GET(self):
            if self.path.startswith('/good.svg'):
                time.sleep(.15)  # Explicit slow-network fixture, not an application delay.
                self.send_response(200);self.send_header('Content-Type','image/svg+xml');self.end_headers()
                try:self.wfile.write(svg)
                except BrokenPipeError:pass
            else:self.send_error(404)
    server=ThreadingHTTPServer(('127.0.0.1',0),Handler)
    thread=threading.Thread(target=server.serve_forever,daemon=True);thread.start()
    try:
        origin=f'http://127.0.0.1:{server.server_port}'
        with sync_playwright() as p:
            browser=p.chromium.launch(executable_path=args.chromium or p.chromium.executable_path,args=['--no-sandbox'])
            try:
                page=browser.new_page();failed=[]
                page.on('requestfailed',lambda request:failed.append(request.url))
                page.set_content(f'<img src="{origin}/good.svg?html"><svg><image href="{origin}/good.svg?svg" width="40" height="40"/></svg>',wait_until='domcontentloaded')
                settle_images(page)
                assert page.locator('img').evaluate('image=>image.complete && image.naturalWidth>0')
                close_with_images(page)
                assert not failed,failed
                broken=browser.new_page();broken.set_content(f'<img src="{origin}/missing.svg">',wait_until='domcontentloaded')
                try:settle_images(broken)
                except Error as error:assert 'Image could not be decoded' in str(error)
                else:raise AssertionError('A missing image must remain a failed check')
                broken.close()
                # Deliberate browser-promise fault injection in an isolated fixture,
                # never on the product page: loaded pixels do not excuse a stuck decode.
                stalled=browser.new_page()
                stalled.set_content(f'<img src="{origin}/good.svg?stalled">')
                stalled.evaluate('() => { HTMLImageElement.prototype.decode = () => new Promise(() => {}); }')
                started=time.monotonic()
                try:settle_images(stalled,timeout_ms=100)
                except Error as error:
                    assert 'Image decode timed out after 100 ms' in str(error),str(error)
                    assert '/good.svg?stalled' in str(error),str(error)
                    assert time.monotonic()-started < 3
                else:raise AssertionError('An unresolved image decode must fail with its URL and deadline')
                stalled.close()
                fonts=browser.new_page();fonts.set_content('<p>Font readiness fixture</p>')
                settle_fonts(fonts)
                fonts.evaluate("() => { Object.defineProperty(document.fonts,'ready',{value:new Promise(() => {})}); }")
                started=time.monotonic()
                try:settle_fonts(fonts,timeout_ms=100)
                except Error as error:
                    assert 'Font readiness timed out after 100 ms' in str(error),str(error)
                    assert 'status=' in str(error),str(error)
                    assert time.monotonic()-started < 3
                else:raise AssertionError('Unresolved font readiness must fail with a deadline and font status')
                fonts.close()
            finally:browser.close()
    finally:server.shutdown();server.server_close();thread.join()
    print(json.dumps({'status':'passed','checks':5,'scope':'delayed real Typst image in HTML/SVG; missing image remains fatal; unresolved image/font promises fail with bounded diagnostic deadlines'}))


if __name__=='__main__':main()
