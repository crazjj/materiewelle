#!/usr/bin/env python3
"""Negative fixtures demonstrate that the build guard rejects accidental string APIs."""
import importlib.util
from pathlib import Path
import unittest

spec = importlib.util.spec_from_file_location("guard", Path(__file__).with_name("check-ui-contract.py"))
guard = importlib.util.module_from_spec(spec)
spec.loader.exec_module(guard)


class GuardTest(unittest.TestCase):
    def test_rejects_string_id_and_untyped_tour(self):
        self.assertIn("raw DOM identity", guard.source_violations('id("voltage")'))
        self.assertIn("raw tour target", guard.source_violations('TourStep("#voltage", "Title", "Body")'))

    def test_rejects_inline_attributes_selectors_and_assets(self):
        self.assertIn("raw HTML attribute", guard.source_violations('attr("aria-pressed", "true")'))
        self.assertIn("raw DOM lookup", guard.source_violations('document.querySelector("#voltage")'))
        self.assertIn("raw asset URL", guard.source_violations('window.fetch("versuchsdaten.json")'))

    def test_rejects_presentation_language_regressions(self):
        self.assertIn("handwritten JavaScript snippet", guard.source_violations('js("navigator.clipboard.writeText(x)")'))
        self.assertIn("raw generic element", guard.source_violations('GenericTag<SVGElement>("path")'))
        self.assertIn("raw inline stylesheet", guard.source_violations('inlineStyle("width:5px")'))
        self.assertIn("owned XML template", guard.source_violations('val svg = """<svg viewBox="0 0 5 5"/>"""'))
        self.assertEqual([], guard.source_violations('vector { path { geometry(points) } }; style { width(5.px) }'))

    def test_accepts_typed_contract_and_ordinary_prose(self):
        self.assertEqual([], guard.source_violations('elementId(Id.VOLTAGE); ariaPressed(true); window.fetch(LabAsset.SCIENTIFIC_DATA.path)'))
        self.assertEqual([], guard.source_violations('Text("Beschleunigungsspannung U")'))

    def test_binary_theme_rejects_default_or_storage_drift_and_os_observation(self):
        theme = 'const val KEY = PresentationKeys.THEME'
        bootstrap = 'window.localStorage.getItem(PresentationKeys.THEME); ThemePreference.fromStorage(storedTheme); document.documentElement.setAttribute(PresentationKeys.THEME_ATTRIBUTE, theme.key)'
        metadata = 'Key<ResolvedTheme>(materiewelle.presentation.PresentationKeys.THEME_ATTRIBUTE)'
        self.assertEqual([], guard.theme_bootstrap_violations(theme, bootstrap, metadata))
        self.assertIn("Theme bootstrap must use the shared default and migration", guard.theme_bootstrap_violations(theme, bootstrap.replace('ThemePreference.fromStorage(storedTheme)', 'arbitraryDefault()'), metadata))
        self.assertIn("Theme bootstrap and Kotlin storage key disagree", guard.theme_bootstrap_violations(theme, bootstrap.replace("PresentationKeys.THEME", "OtherKeys.THEME"), metadata))
        self.assertIn("Binary theme must not follow the operating-system color scheme", guard.theme_bootstrap_violations(theme + ' prefers-color-scheme', bootstrap, metadata))


if __name__ == "__main__":
    unittest.main()
