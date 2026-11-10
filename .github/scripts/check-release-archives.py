#!/usr/bin/env python3
"""Open the actual release ZIPs after extraction; reject changed or internal files."""
from datetime import datetime, timezone
import argparse
import hashlib
import json
import os
from pathlib import Path, PurePosixPath
import stat
import sys
import tempfile
from urllib.parse import urljoin
import zipfile

from playwright.sync_api import sync_playwright, expect
from product_delivery import inventory, allowed_asset, digest
from release_browser import chromium_executable

ROOT = Path(os.environ.get('RELEASE_PROJECT_ROOT', Path(__file__).resolve().parents[2]))
APP = ROOT/'apps/web'
sys.path.insert(0, str(APP/'scripts'))
from browser_evidence import served
from browser_assets import settle_images


def extract(archive, directory, prefix):
    with zipfile.ZipFile(archive) as bundle:
        assert bundle.testzip() is None
        names = bundle.namelist()
        assert len(names) == len(set(names)), 'Duplicate ZIP member'
        for member in bundle.infolist():
            path = PurePosixPath(member.filename)
            assert not path.is_absolute() and '..' not in path.parts and '\\' not in member.filename
            assert not stat.S_ISLNK(member.external_attr >> 16)
            name = member.filename
            if prefix:
                assert name.startswith(prefix)
                name = name.removeprefix(prefix)
            assert name == 'manifest.json' or allowed_asset(name), name
        bundle.extractall(directory)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--assets', type=Path, required=True)
    parser.add_argument('--output', type=Path, required=True)
    args = parser.parse_args()
    assert not args.output.exists(), 'Preserve previous evidence; choose a fresh output file'
    release = json.loads((args.assets/'release-manifest.json').read_text())
    report = {'recorded_at': datetime.now(timezone.utc).isoformat(), 'source_commit': release['source_commit'],
              'method': 'Real ZIP extraction, full asset inventory and Chromium interaction smoke test',
              'not_tested': ['Safari', 'real iPad', 'human learning outcomes'], 'archives': []}
    executable = chromium_executable()
    with tempfile.TemporaryDirectory(prefix='materiewelle-release-probe-') as temporary, sync_playwright() as playwright:
        browser = playwright.chromium.launch(executable_path=executable, args=['--no-sandbox'])
        report['browser'] = browser.version
        for filename, prefix, base in [('materiewelle-web.zip', '', '/'), ('materiewelle-web-subpath.zip', 'materiewelle/', '/materiewelle/')]:
            archive = args.assets/filename
            assert digest(archive) == release['files'][filename]['sha256']
            destination = Path(temporary)/filename.removesuffix('.zip')
            extract(archive, destination, prefix)
            serving = destination/prefix
            manifest = inventory(serving, base)
            entry = {'file': filename, 'sha256': digest(archive), 'base': base, 'asset_count': len(manifest['files']), 'page_errors': []}
            with served(destination) as origin:
                url = urljoin(origin, base.lstrip('/'))
                page = browser.new_page(viewport={'width':1280, 'height':800}, reduced_motion='reduce')
                page.on('pageerror', lambda error: entry['page_errors'].append(str(error)))
                page.goto(url, wait_until='networkidle')
                for fragment, selector in [('debroglie','#debroglie-article'), ('davisson-germer','#dg-article'), ('joensson','#joensson-article'), ('fazit','#conclusion-article')]:
                    settle_images(page)
                    page.evaluate('fragment=>location.hash=fragment', fragment)
                    expect(page.locator(selector)).to_be_visible()
                    page.wait_for_load_state('networkidle')
                page.evaluate("location.hash='joensson/messung'")
                for marker in (551,578):
                    page.locator('#marker-new-x').fill(str(marker));page.locator('#marker-add').click()
                expect(page.locator('[data-own-marker]')).to_have_count(2)
                expect(page.locator('[data-own-marker="0"]')).to_have_attribute('data-native-x','551')
                expect(page.locator('[data-own-marker="1"]')).to_have_attribute('data-native-x','578')
                page.locator('#compact-tutor-open').click()
                expect(page.locator('#compact-tutor-copy')).to_be_visible()
                page.get_by_text('Vollständige Nachricht ansehen', exact=True).click()
                expect(page.locator('#compact-tutor-preview')).to_contain_text('Kapitel: Jönsson')
                page.evaluate("location.hash='fazit'")
                with page.expect_download() as pending:
                    page.locator('#handout-download').click()
                data = Path(pending.value.path()).read_bytes()
                assert hashlib.sha256(data).hexdigest() == manifest['files']['handout/materiewellen.pdf']['sha256']
                assert data == (args.assets/'materiewellen-handout.pdf').read_bytes()
                entry.update(handout_sha256=hashlib.sha256(data).hexdigest(), articles_opened=4, native_markers=[551,578], tutor_section='joensson')
                settle_images(page);page.close()
            assert not entry['page_errors']
            entry['status']='passed';report['archives'].append(entry)
        browser.close()
    report['status']='passed'
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(report, ensure_ascii=False, indent=2)+'\n')
    print(json.dumps({'status':'passed', 'archives':len(report['archives']), 'report':str(args.output)}))


if __name__ == '__main__':
    main()
