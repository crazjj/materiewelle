"""Regression checks for source-bound public delivery and its local entry point."""
import contextlib
import importlib.util
import io
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import unittest
from unittest.mock import patch

from build_inputs import build_input_hashes, product_input_hashes

SOURCE = Path(__file__).with_name("package-delivery.py")
SPEC = importlib.util.spec_from_file_location("delivery", SOURCE)
DELIVERY = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(DELIVERY)
ROOT = SOURCE.resolve().parents[2]


class SourceBinding(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory(prefix="materiewelle-package-inputs-")
        self.addCleanup(self.temporary.cleanup)
        self.root = Path(self.temporary.name)
        self.snapshot = product_input_hashes(ROOT)
        for name in self.snapshot:
            target = self.root / name
            target.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(ROOT / name, target)

    def test_identical_inputs_are_stable_and_sorted(self):
        self.assertEqual(product_input_hashes(self.root), self.snapshot)
        self.assertEqual(build_input_hashes(self.root), self.snapshot)
        self.assertEqual(list(self.snapshot), sorted(self.snapshot))

    def test_changed_source_configuration_selection_and_licenses_are_bound(self):
        names = [
            "LICENSE",
            "content/licenses/NOTICE.txt",
            "content/licenses/DejaVu-LICENSE.txt",
            "packages/physics-core/src/commonMain/kotlin/materiewelle/core/Physics.kt",
            "packages/physics-core/build.gradle.kts",
            "apps/web/build.gradle.kts",
            "apps/web/settings.gradle.kts",
            "apps/web/browser-design/build.gradle.kts",
            "apps/web/browser-bootstrap/build.gradle.kts",
            "apps/web/browser-bootstrap/src/jsMain/kotlin/materiewelle/presentation/BrowserBootstrap.kt",
            "content/typst/auswahl.json",
            "content/typst/math/inline.typ",
            "apps/web/site/src/jsMain/resources/public/fonts/LatinModern-LICENSE.txt",
            "apps/web/site/src/jsMain/resources/public/fonts/IBMPlexSans.ttf",
        ]
        for name in names:
            with self.subTest(name=name):
                target = self.root / name
                original = target.read_bytes()
                target.write_bytes(original + b"\nchanged input\n")
                actual = product_input_hashes(self.root)
                self.assertNotEqual(actual[name], self.snapshot[name])
                target.write_bytes(original)

    def test_added_sources_and_font_licenses_are_bound(self):
        names = [
            "apps/web/site/src/commonMain/kotlin/NewShared.kt",
            "apps/web/site/src/jsMain/kotlin/NewBrowser.kt",
            "apps/web/browser-design/src/jsMain/kotlin/NewStyles.kt",
            "packages/physics-core/src/jsMain/kotlin/NewCalculation.kt",
            "content/typst/fonts/NewFont-LICENSE.txt",
        ]
        for name in names:
            with self.subTest(name=name):
                target = self.root / name
                target.parent.mkdir(parents=True, exist_ok=True)
                target.write_text("new source input\n")
                self.assertIn(name, product_input_hashes(self.root))
                target.unlink()

    def test_deleted_source_changes_snapshot(self):
        name = "packages/physics-core/src/commonMain/kotlin/materiewelle/core/Physics.kt"
        (self.root / name).unlink()
        self.assertNotIn(name, product_input_hashes(self.root))

    def test_missing_required_selection_fails_closed(self):
        (self.root / "content/typst/auswahl.json").unlink()
        with self.assertRaises(FileNotFoundError):
            product_input_hashes(self.root)

    def test_missing_required_license_fails_closed(self):
        for name in ("LICENSE", "content/licenses/NOTICE.txt", "content/licenses/DejaVu-LICENSE.txt"):
            with self.subTest(name=name):
                target = self.root / name
                original = target.read_bytes()
                target.unlink()
                with self.assertRaises(FileNotFoundError):
                    product_input_hashes(self.root)
                target.write_bytes(original)

    def test_generated_outputs_and_private_notes_are_not_product_inputs(self):
        for name in ["content/typst/math/manifest.json", "content/typst/math/__pycache__/cache.pyc",
                     "content/typst/math/ausgabe/inline.svg", "docs/screenshots/report.json",
                     "apps/web/site/build/generated/resource.json"]:
            target = self.root / name
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_text("generated output")
        self.assertEqual(product_input_hashes(self.root), self.snapshot)

    def test_no_private_layout_paths_remain(self):
        self.assertFalse(any(name.startswith(("vorarbeit/", "vertiefung/", "tutor/"))
                             for name in self.snapshot))


class PackagingEntryPoint(unittest.TestCase):
    def test_dirty_tree_stops_before_release_verification(self):
        with patch.object(sys, "argv", [str(SOURCE), "release-files"]), \
                patch.object(DELIVERY.subprocess, "check_output", return_value=b" M source.kt\n"), \
                patch.object(DELIVERY.subprocess, "run") as run, contextlib.redirect_stderr(io.StringIO()):
            with self.assertRaises(SystemExit) as error:
                DELIVERY.main()
            self.assertEqual(error.exception.code, 2)
            run.assert_not_called()

    def test_verification_failure_is_propagated(self):
        with patch.object(sys, "argv", [str(SOURCE), "release-files", "--commit", "a" * 40]), \
                patch.object(DELIVERY.subprocess, "check_output", return_value=b""), \
                patch.object(DELIVERY.subprocess, "run", side_effect=subprocess.CalledProcessError(1, "verify")):
            with self.assertRaises(subprocess.CalledProcessError):
                DELIVERY.main()


if __name__ == "__main__":
    unittest.main(verbosity=2)
