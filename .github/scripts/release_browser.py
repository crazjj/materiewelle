"""Resolve and initialize the same Chromium engine used by release checks."""
from pathlib import Path
import shutil


def chromium_executable():
    from playwright.sync_api import sync_playwright
    with sync_playwright() as playwright:
        browser_path = playwright.chromium.executable_path
        if not Path(browser_path).is_file():
            browser_path = shutil.which("chromium") or shutil.which("chromium-browser")
        if not browser_path:
            raise RuntimeError("Install Playwright Chromium or system Chromium")
        # Complete driver initialization before closing its context; querying only
        # the path can leave a pending initialization task in Playwright 1.62.
        browser = playwright.chromium.launch(executable_path=browser_path, args=["--no-sandbox"])
        browser.close()
        return browser_path


if __name__ == "__main__":
    import argparse
    parser = argparse.ArgumentParser(description="Resolve and initialize the installed QA browser.")
    parser.add_argument("--github-env", type=Path, help="Export the same path to later workflow steps.")
    args = parser.parse_args()
    executable = chromium_executable()
    if args.github_env:
        with args.github_env.open("a") as environment:
            environment.write(f"SCREENSHOT_CHROMIUM={executable}\n")
    print(executable)
