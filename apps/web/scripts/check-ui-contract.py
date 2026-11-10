#!/usr/bin/env python3
"""Keep feature code on the typed DOM boundary; this is a focused guard, not a Kotlin parser."""
from pathlib import Path
import json
import re

PROJECT = Path(__file__).resolve().parents[1]
SOURCE = PROJECT / "site/src/jsMain/kotlin/materiewelle"
RULES = {
    "handwritten JavaScript snippet": r'\bjs\s*\(',
    "raw generic element": r'\bGenericTag\b',
    "raw inline stylesheet": r'\binlineStyle\s*\(',
    "owned XML template": r'"""\s*<(?:svg|g|path|text)\b',
    "raw HTML attribute": r'\battr\s*\(\s*"',
    "raw DOM identity": r'\bid\s*\(\s*"',
    "raw CSS class": r'\b(?:classes|classNames)\s*\(\s*"',
    "raw label reference": r'\bforId\s*=\s*"',
    "raw tour target": r'\bTourStep\s*\(\s*"',
    "raw DOM lookup": r'\b(?:getElementById|querySelector|querySelectorAll|setAttribute|getAttribute)\s*\(\s*"',
    "raw asset URL": r'\bfetch\s*\(\s*"',
    "raw event name": r'\b(?:addEventListener|removeEventListener)\s*\(\s*"',
    "raw keyboard comparison": r'\b(?:event|e)\.key\s*==\s*"',
    "raw SVG identity": r'\bid="[a-z]',
}


def source_violations(source: str):
    return [name for name, pattern in RULES.items() if re.search(pattern, source)]


def theme_bootstrap_violations(theme: str, bootstrap: str, metadata: str):
    """Both executable layers must use the same Kotlin preference and metadata identities."""
    errors = []
    if 'PresentationKeys.THEME' not in theme or 'PresentationKeys.THEME' not in bootstrap:
        errors.append("Theme bootstrap and Kotlin storage key disagree")
    if 'ThemePreference.fromStorage(storedTheme)' not in bootstrap:
        errors.append("Theme bootstrap must use the shared default and migration")
    if 'prefers-color-scheme' in theme + bootstrap:
        errors.append("Binary theme must not follow the operating-system color scheme")
    if 'PresentationKeys.THEME_ATTRIBUTE' not in bootstrap or not re.search(r'Key<ResolvedTheme>\(\s*(?:materiewelle\.presentation\.)?PresentationKeys\.THEME_ATTRIBUTE', metadata):
        errors.append("Theme bootstrap and typed theme attribute disagree")
    return errors


def main():
    errors, inspected = [], 0
    for path in SOURCE.rglob("*.kt"):
        if "semantics" in path.relative_to(SOURCE).parts:
            continue  # Browser APIs legitimately take strings inside the adapter.
        inspected += 1
        for rule in source_violations(path.read_text()):
            errors.append(f"{path.relative_to(PROJECT)}: {rule}")
    # A head bootstrap runs before Kotlin: its separate browser contract must agree.
    theme = (SOURCE / "ui/LabTheme.kt").read_text()
    bootstrap = (PROJECT / "browser-bootstrap/src/jsMain/kotlin/materiewelle/presentation/BrowserBootstrap.kt").read_text()
    theme_attr = (PROJECT / "browser-design/src/commonMain/kotlin/materiewelle/ui/semantics/LabData.kt").read_text()
    errors.extend(theme_bootstrap_violations(theme, bootstrap, theme_attr))
    resources = PROJECT / "site/src/jsMain/resources/public"
    for extension in ("*.css", "*.js", "*.html"):
        for path in resources.rglob(extension):
            errors.append(f"Hand-maintained presentation file: {path.relative_to(PROJECT)}")
    print(json.dumps({"status": "failed" if errors else "passed", "feature_files": inspected,
                      "rules": len(RULES), "errors": errors}, ensure_ascii=False))
    if errors:
        raise SystemExit(1)


if __name__ == "__main__":
    main()
