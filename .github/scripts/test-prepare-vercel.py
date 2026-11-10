"""Exercise the public deployment boundary using actual manifests and file hashes."""
import importlib.util
import json
from pathlib import Path
import sys
import tempfile
import unittest

sys.path.insert(0, str(Path(__file__).resolve().parent))
from product_delivery import CONTRACT, ROOT, TUTOR_FILES, LEGAL_INPUTS, digest, inventory, stage_public_site
sys.path.insert(0, str(ROOT / "tools/build"))
from build_inputs import product_input_hashes

SPEC = importlib.util.spec_from_file_location("prepare_vercel", Path(__file__).with_name("prepare-vercel.py"))
VERCEL = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(VERCEL)
COMMIT = "a" * 40


class PublicDeploymentTest(unittest.TestCase):
    def setUp(self):
        temporary = tempfile.TemporaryDirectory(prefix="materiewelle-vercel-test-")
        self.addCleanup(temporary.cleanup)
        self.root = Path(temporary.name)
        self.source, self.output = self.root / "source", self.root / "deploy"
        hashes = product_input_hashes(ROOT)
        files = {"index.html": b"<main>test fixture</main>", "app.css": b"body{}",
                 "materiewellen.js": b"console.log('fixture');\n//# sourceMappingURL=private.js.map\n",
                 "versuchsdaten.json": b"{}", "handout/materiewellen.pdf": b"%PDF-1.7 fixture"}
        files.update({"tutor/" + name: b"curated fixture" for name in TUTOR_FILES})
        files.update({name: (ROOT / source).read_bytes() for name, source in LEGAL_INPUTS.items()})
        for name, data in files.items():
            path = self.source / name
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_bytes(data)
        pdf = self.source / "handout/materiewellen.pdf"
        handout = {"kind": "materiewellen-product-handout",
                   "artifact": {"file": pdf.name, "bytes": pdf.stat().st_size, "sha256": digest(pdf)},
                   "canonical_inputs": [{"path": "content/typst/handout.typ", "sha256": hashes["content/typst/handout.typ"]}],
                   "validation": {"pages": 3, "paper": "A4", "all_fonts_embedded": True,
                                  "text_inside_page_bounds": True, "compiler_warnings": 0}}
        (self.source / "handout/manifest.json").write_text(json.dumps(handout))
        self.manifest = {"product_contract": CONTRACT, "deployment_base": "/",
                         "mode": "static SPA; no prerendering claim", "input_hashes": hashes,
                         "files": {p.relative_to(self.source).as_posix(): {"bytes": p.stat().st_size, "sha256": digest(p)}
                                   for p in self.source.rglob("*") if p.is_file()}}
        self.write_manifest()

    def write_manifest(self):
        (self.source / "manifest.json").write_text(json.dumps(self.manifest))

    def test_prebuilt_workspace_contains_only_curated_static_product(self):
        public = VERCEL.prepare(self.source, self.output, COMMIT)
        static = self.output / ".vercel/output/static"
        self.assertEqual({".vercel"}, {p.name for p in self.output.iterdir()})
        self.assertEqual({"version": 3}, json.loads((static.parent / "config.json").read_text()))
        self.assertEqual(COMMIT, public["source_commit"])
        self.assertFalse(public["source_maps_published"])
        self.assertEqual(public, inventory(static, "/"))
        self.assertNotIn("sourceMappingURL", (static / "materiewellen.js").read_text())
        self.assertEqual(self.manifest["files"]["handout/materiewellen.pdf"], public["files"]["handout/materiewellen.pdf"])

    def test_rejects_subpath_build(self):
        self.manifest["deployment_base"] = "/materiewelle/"
        self.write_manifest()
        with self.assertRaises(AssertionError):
            VERCEL.prepare(self.source, self.output, COMMIT)
        self.assertFalse(self.output.exists())

    def test_rejects_added_private_files_and_source_maps(self):
        for name in ("Index.kt", "materiewellen.js.map", "arbeitsfassung.pdf"):
            with self.subTest(name=name):
                path = self.source / name
                path.write_bytes(b"private fixture")
                with self.assertRaisesRegex(AssertionError, "Unmanifested"):
                    VERCEL.prepare(self.source, self.output, COMMIT)
                self.manifest["files"][name] = {"bytes": path.stat().st_size, "sha256": digest(path)}
                self.write_manifest()
                with self.assertRaisesRegex(AssertionError, "Excluded"):
                    VERCEL.prepare(self.source, self.output, COMMIT)
                path.unlink()
                del self.manifest["files"][name]
                self.write_manifest()
        self.assertFalse(self.output.exists())

    def test_rejects_tampered_files_and_stale_source_inputs(self):
        (self.source / "app.css").write_text("tampered")
        with self.assertRaises(AssertionError):
            VERCEL.prepare(self.source, self.output, COMMIT)
        (self.source / "app.css").write_bytes(b"body{}")
        # Change a non-handout source to exercise the full source snapshot check.
        key = "apps/web/site/build.gradle.kts"
        self.manifest["input_hashes"] = product_input_hashes(ROOT)
        self.manifest["input_hashes"][key] = "b" * 64
        self.write_manifest()
        with self.assertRaisesRegex(AssertionError, "Source changed"):
            VERCEL.prepare(self.source, self.output, COMMIT)

    def test_preserves_existing_deployment_workspace(self):
        self.output.mkdir()
        important = self.output / "notes.txt"
        important.write_text("keep this")
        with self.assertRaisesRegex(AssertionError, "fresh deployment workspace"):
            VERCEL.prepare(self.source, self.output, COMMIT)
        self.assertEqual("keep this", important.read_text())

    def test_rejects_overlapping_source_and_output(self):
        with self.assertRaisesRegex(AssertionError, "overlap"):
            VERCEL.prepare(self.source, self.source / "deploy", COMMIT)
        self.assertFalse((self.source / "deploy").exists())

    def test_staging_preserves_requested_subpath_base(self):
        self.manifest["deployment_base"] = "/materiewelle/"
        self.write_manifest()
        public = stage_public_site(self.source, self.output, base="/materiewelle/", commit=COMMIT)
        self.assertEqual(public, inventory(self.output, "/materiewelle/"))

    def test_rejects_incomplete_commit_identifier(self):
        with self.assertRaisesRegex(AssertionError, "full source commit"):
            VERCEL.prepare(self.source, self.output, "HEAD")
        self.assertFalse(self.output.exists())


if __name__ == "__main__":
    unittest.main(verbosity=2)
