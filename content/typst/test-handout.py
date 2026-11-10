#!/usr/bin/env python3
"""Focused acceptance checks for the product PDF and safe public build CLI."""
import hashlib
import importlib.util
import json
from pathlib import Path
import re
import subprocess
import sys
import tempfile
import unittest
from unittest.mock import patch

import fitz

HERE = Path(__file__).resolve().parent
SPEC = importlib.util.spec_from_file_location("handout_builder", HERE / "build-handout.py")
BUILDER = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(BUILDER)


class InterpreterSelected(Exception):
    """Stop the interpreter handoff before running the actual compiler."""


def segment_crosses_box(start, end, box):
    """Clip a PDF vector segment against the actual text box (Liang–Barsky)."""
    dx, dy = end.x - start.x, end.y - start.y
    low, high = 0.0, 1.0
    for p, q in ((-dx, start.x - box.x0), (dx, box.x1 - start.x),
                 (-dy, start.y - box.y0), (dy, box.y1 - start.y)):
        if p == 0:
            if q < 0:
                return False
        elif p < 0:
            low = max(low, q / p)
        else:
            high = min(high, q / p)
        if low > high:
            return False
    return True


def drawing_segments(drawing):
    """Read real PDF rays, arrowheads, leaders and curves, not source coordinates."""
    for item in drawing["items"]:
        if item[0] == "l":
            yield item[1], item[2]
        elif item[0] == "c":
            a, b, c, d = item[1:]
            previous = a
            for step in range(1, 33):
                t = step / 32
                weights = ((1 - t) ** 3, 3 * (1 - t) ** 2 * t, 3 * (1 - t) * t * t, t ** 3)
                point = fitz.Point(*(sum(weight * control[axis] for weight, control in zip(weights, (a, b, c, d)))
                                     for axis in (0, 1)))
                yield previous, point
                previous = point
        elif item[0] == "re":
            rect = item[1]
            corners = (rect.tl, rect.tr, rect.br, rect.bl, rect.tl)
            yield from zip(corners, corners[1:])
        else:
            raise AssertionError(f"Unreviewed PDF vector primitive: {item[0]}")


def bragg_label_collisions(page, clearance_pt=1.5):
    """Return any figure glyph box that touches ink or loses its reading margin."""
    labels = [span for block in page.get_text("dict")["blocks"]
              for line in block.get("lines", []) for span in line["spans"]
              if "DejaVuSans" in span["font"]]
    collisions = []
    for drawing in page.get_drawings():
        # The SVG background is intentionally behind the whole construction.
        if drawing["type"] == "f" and all(v > .99 for v in drawing["fill"]):
            continue
        padding = clearance_pt + (drawing.get("width") or 0) / 2
        for start, end in drawing_segments(drawing):
            for label in labels:
                box = fitz.Rect(label["bbox"]) + (-padding, -padding, padding, padding)
                if segment_crosses_box(start, end, box):
                    collisions.append(label["text"])
    return labels, sorted(set(collisions))


class TypstInterpreterSelectionTest(unittest.TestCase):
    def run_without_system_typst(self, tools, *, executable="/usr/bin/python3", exists=True):
        original_import = __import__

        def without_typst(name, *args, **kwargs):
            if name == "typst":
                raise ModuleNotFoundError("Isolated system Python has no Typst")
            return original_import(name, *args, **kwargs)

        with patch.dict(BUILDER.os.environ, {"TASK_TOOLS_ROOT": str(tools)}), \
                patch.object(BUILDER.sys, "executable", str(executable)), \
                patch.object(BUILDER.sys, "argv", [str(HERE / "build-handout.py"), "--output-dir", "/tmp/unused-handout"]), \
                patch("builtins.__import__", side_effect=without_typst), \
                patch.object(BUILDER.Path, "exists", return_value=exists), \
                patch.object(BUILDER.os, "execv", side_effect=InterpreterSelected) as handoff:
            try:
                BUILDER.main()
            except InterpreterSelected:
                pass
        return handoff

    def test_runner_tool_root_selects_its_own_typst_python(self):
        tools = Path("/tmp/runner temp/materiewelle-tools")
        python = tools / "typst/bin/python"
        handoff = self.run_without_system_typst(tools)
        handoff.assert_called_once_with(str(python), [str(python), str(HERE / "build-handout.py"),
                                                     "--output-dir", "/tmp/unused-handout"])

    def test_missing_configured_python_reports_the_missing_toolchain(self):
        with self.assertRaisesRegex(RuntimeError, "Install the pinned toolchain"):
            self.run_without_system_typst(Path("/tmp/missing-tools"), exists=False)

    def test_selected_python_does_not_reexecute_itself_indefinitely(self):
        tools = Path("/tmp/runner-tools")
        with self.assertRaisesRegex(RuntimeError, "Install the pinned toolchain"):
            self.run_without_system_typst(tools, executable=tools / "typst/bin/python")


class ProductHandoutTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.temporary = tempfile.TemporaryDirectory(prefix="test-product-handout-")
        cls.work = Path(cls.temporary.name)
        cls.output = cls.work / "arbitrary product" / "handout"
        cls.build(cls.output)
        cls.manifest = json.loads((cls.output / "manifest.json").read_text())

    @classmethod
    def tearDownClass(cls):
        cls.temporary.cleanup()

    @staticmethod
    def build(output):
        subprocess.run([sys.executable, str(HERE / "build-handout.py"), "--output-dir", str(output)],
                       cwd="/tmp", check=True, capture_output=True, text=True)

    def test_actual_pdf_three_pages_a4_fonts_and_source_hashes(self):
        self.assertEqual({"materiewellen.pdf", "manifest.json"}, {p.name for p in self.output.iterdir()})
        validation = self.manifest["validation"]
        self.assertEqual(3, validation["pages"])
        self.assertEqual("A4", validation["paper"])
        self.assertTrue(validation["all_fonts_embedded"])
        self.assertTrue(validation["text_inside_page_bounds"])
        self.assertEqual(0, validation["compiler_warnings"])
        pdf = (self.output / "materiewellen.pdf").read_bytes()
        self.assertEqual(hashlib.sha256(pdf).hexdigest(), self.manifest["artifact"]["sha256"])
        for source in self.manifest["canonical_inputs"]:
            self.assertEqual(hashlib.sha256((BUILDER.ROOT / source["path"]).read_bytes()).hexdigest(), source["sha256"])
        source = (HERE / "handout.typ").read_text()
        self.assertIn('size: 11pt, lang: "de"', source)
        self.assertGreaterEqual(min(float(v) for v in re.findall(r"size:\s*([\d.]+)pt", source)), 9.5)

    def test_controls_agree_with_documented_benchmarks(self):
        values = self.manifest["control_values"]
        self.assertAlmostEqual(100.137260744756, values["de_broglie"]["wavelength_150V_pm"], places=10)
        self.assertEqual(.5, values["de_broglie"]["wavelength_ratio_600V_to_150V"])
        self.assertAlmostEqual(.165124784522367, values["davisson_germer"]["geometric_wavelength_nm"], places=13)
        self.assertAlmostEqual(.166895434574594, values["davisson_germer"]["nonrelativistic_wavelength_nm"], places=13)
        self.assertAlmostEqual(5.355306956823, values["jonsson"]["relativistic_wavelength_pm"], places=10)
        self.assertAlmostEqual(96 / 7, values["jonsson"]["mean_reference_interval_native_px"], places=12)
        self.assertIsNone(values["jonsson"]["absolute_wavelength_from_image"])

    def test_actual_bragg_labels_clear_rays_planes_arrows_and_leaders(self):
        with fitz.open(self.output / "materiewellen.pdf") as document:
            labels, collisions = bragg_label_collisions(document[1])
        self.assertEqual(["A", "P", "Q", "B", "θ", "B", "d"], [span["text"] for span in labels])
        self.assertEqual([], collisions, "Bragg labels must have 1.5 pt of clear space around their text boxes")

    def test_actual_bragg_label_sizes_and_angle_subscript(self):
        with fitz.open(self.output / "materiewellen.pdf") as document:
            labels, _ = bragg_label_collisions(document[1])
        theta = next(span for span in labels if span["text"] == "θ")
        subscript = [span for span in labels if span["text"] == "B" and span["size"] < theta["size"]]
        self.assertEqual(1, len(subscript), "The B in θ_B must be a real mathematical subscript")
        self.assertGreater(subscript[0]["origin"][1], theta["origin"][1] + 1)
        for span in labels:
            if span is not subscript[0]:
                self.assertGreaterEqual(span["size"], 9.5, span["text"])

    def test_actual_figure_captions_align_and_define_the_points(self):
        with fitz.open(self.output / "materiewellen.pdf") as document:
            page = document[1]
            left, right = page.search_for("Aufbau:"), page.search_for("Bragg-Konstruktion:")
            self.assertEqual((1, 1), (len(left), len(right)))
            self.assertAlmostEqual(left[0].y0, right[0].y0, delta=.1)
            labels, _ = bragg_label_collisions(page)
            self.assertGreater(right[0].y0 - max(span["bbox"][3] for span in labels), 6)
            text = page.get_text()
            for definition in ("Streupunkte", "Lotfußpunkte", "Zusatzwege", "Ebenenabstand"):
                self.assertIn(definition, text)

    def test_reproducible_build_and_idempotent_existing_output(self):
        second = self.work / "second fresh" / "handout"
        self.build(second)
        before = {p.name: p.read_bytes() for p in self.output.iterdir()}
        self.build(self.output)
        self.assertEqual(before, {p.name: p.read_bytes() for p in second.iterdir()})
        self.assertEqual(before, {p.name: p.read_bytes() for p in self.output.iterdir()})

    def test_existing_unrelated_files_and_edited_pdf_are_preserved(self):
        payload = {p.name: p.read_bytes() for p in self.output.iterdir()}
        folder = self.work / "important existing files"
        folder.mkdir()
        important = folder / "notes.txt"
        important.write_text("Do not remove")
        with self.assertRaisesRegex(RuntimeError, "unrelated"):
            BUILDER.check_output(folder, True, payload)
        self.assertEqual("Do not remove", important.read_text())
        protected = self.work / "edited generated PDF"
        protected.mkdir()
        for name, data in payload.items():
            (protected / name).write_bytes(data)
        (protected / "materiewellen.pdf").write_bytes(b"personal annotations")
        with self.assertRaisesRegex(RuntimeError, "changed"):
            BUILDER.check_output(protected, True, payload)
        self.assertEqual(b"personal annotations", (protected / "materiewellen.pdf").read_bytes())

    def test_generated_update_requires_explicit_flag(self):
        changed = {p.name: p.read_bytes() for p in self.output.iterdir()}
        changed["materiewellen.pdf"] += b"new generated revision"
        with self.assertRaisesRegex(RuntimeError, "Output differs"):
            BUILDER.check_output(self.output, False, changed)
        self.assertTrue(BUILDER.check_output(self.output, True, changed))


if __name__ == "__main__":
    unittest.main(verbosity=2)
