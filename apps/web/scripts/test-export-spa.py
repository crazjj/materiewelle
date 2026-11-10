#!/usr/bin/env python3
"""Check legal-file packaging and reject refreshes of changed source inputs."""
import hashlib
import json
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import unittest

from product_assets import LEGAL_INPUTS, allowed_asset


class ExportTest(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory()
        self.addCleanup(self.temporary.cleanup)
        self.root = Path(self.temporary.name)
        self.web = self.root / "apps/web"
        scripts = self.web / "scripts"
        scripts.mkdir(parents=True)
        for name in ("export-spa.py", "product_assets.py"):
            shutil.copy2(Path(__file__).with_name(name), scripts / name)
        for name in LEGAL_INPUTS.values():
            path = self.root / name
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text("Legal fixture: " + name + "\n")
        self.source = self.web / "site/src/jsMain/kotlin/Example.kt"
        self.source.parent.mkdir(parents=True)
        self.source.write_text("val answer = 42\n")
        names = [*LEGAL_INPUTS.values(), self.source.relative_to(self.root).as_posix()]
        self.inputs = {name: hashlib.sha256((self.root / name).read_bytes()).hexdigest()
                       for name in names}
        self.input_file = self.root / "inputs.json"
        self.input_file.write_text(json.dumps(self.inputs))
        helper = self.root / "tools/build/build_inputs.py"
        helper.parent.mkdir(parents=True)
        helper.write_text(
            "import hashlib\n"
            "def product_input_hashes(root):\n"
            f"    names = {names!r}\n"
            "    return {name: hashlib.sha256((root / name).read_bytes()).hexdigest() for name in names}\n"
        )
        self.resources = self.web / "site/build/processedResources/js/main/public"
        self.resources.mkdir(parents=True)
        self.production = self.web / "site/build/kotlin-webpack/js/productionExecutable"
        self.production.mkdir(parents=True)
        (self.production / "materiewellen.js").write_text("window.fixture = 42;\n")

    def run_export(self, output, *, base="/", refresh=False):
        command = [sys.executable, str(self.web / "scripts/export-spa.py"),
                   "--output-dir", str(output), "--base-path", base]
        if refresh:
            command.append("--refresh-existing")
        else:
            (self.resources / "index.html").write_text(
                f'<html><head><link href="favicon.ico" rel="icon"></head>'
                f'<body><script src="{base}materiewellen.js"></script></body></html>'
            )
            command.extend(["--source-inputs", str(self.input_file)])
        return subprocess.run(command, text=True, capture_output=True)

    def test_fresh_root_and_subpath_include_only_the_explicit_legal_assets(self):
        (self.resources / "unapproved.txt").write_text("Not a product asset")
        for base, name in (("/", "root"), ("/materiewelle/", "subpath")):
            with self.subTest(base=base):
                container = self.root / name
                result = self.run_export(container, base=base)
                self.assertEqual(0, result.returncode, result.stderr)
                output = container if base == "/" else container / "materiewelle"
                manifest = json.loads((output / "manifest.json").read_text())
                self.assertEqual(self.inputs, manifest["input_hashes"])
                for target, source in LEGAL_INPUTS.items():
                    self.assertTrue(allowed_asset(target))
                    self.assertEqual((self.root / source).read_bytes(), (output / target).read_bytes())
                    self.assertEqual(self.inputs[source], manifest["files"][target]["sha256"])
                self.assertFalse((output / "unapproved.txt").exists())
        for name in ("unapproved.txt", "fonts/Other-LICENSE.txt", "../LICENSE.txt"):
            self.assertFalse(allowed_asset(name), name)

    def test_refresh_preserves_compiled_bytes_and_source_identity(self):
        output = self.root / "site"
        self.assertEqual(0, self.run_export(output).returncode)
        original_script = (output / "materiewellen.js").read_bytes()
        (self.production / "materiewellen.js").write_text("Unrelated compiler output")
        (output / "NOTICE.txt").unlink()
        result = self.run_export(output, refresh=True)
        self.assertEqual(0, result.returncode, result.stderr)
        self.assertEqual(original_script, (output / "materiewellen.js").read_bytes())
        self.assertEqual(self.inputs, json.loads((output / "manifest.json").read_text())["input_hashes"])
        self.assertEqual((self.root / "content/licenses/NOTICE.txt").read_bytes(),
                         (output / "NOTICE.txt").read_bytes())

    def test_changed_source_rejects_refresh_without_rewriting_the_manifest(self):
        output = self.root / "site"
        self.assertEqual(0, self.run_export(output).returncode)
        before = (output / "manifest.json").read_bytes()
        self.source.write_text("val answer = 43\n")
        result = self.run_export(output, refresh=True)
        self.assertNotEqual(0, result.returncode)
        self.assertIn("perform a fresh build", result.stderr)
        self.assertEqual(before, (output / "manifest.json").read_bytes())

    def test_changed_legal_notice_rejects_a_stale_build_input_manifest(self):
        (self.root / "LICENSE").write_text("Changed after compilation")
        output = self.root / "site"
        result = self.run_export(output)
        self.assertNotEqual(0, result.returncode)
        self.assertIn("Legal notice changed after the recorded build", result.stderr)
        self.assertFalse((output / "manifest.json").exists())


if __name__ == "__main__":
    unittest.main()
