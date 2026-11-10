#!/usr/bin/env python3
"""Build and validate the three-page product handout.

Usage: python3 content/typst/build-handout.py --output-dir /tmp/handout
Requires Typst's Python binding and Poppler's pdfinfo/pdffonts/pdftotext.
The managed environment's installed Typst Python is selected automatically.
"""
from __future__ import annotations

import argparse
import hashlib
import importlib.metadata
import json
import math
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys
import tempfile
import xml.etree.ElementTree as ET

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[1]
FONT_DIRECTORY = ROOT / "apps/web/site/src/jsMain/resources/public/fonts"
KIND = "materiewellen-product-handout"
PDF_NAME = "materiewellen.pdf"
MANIFEST_NAME = "manifest.json"
SOURCE_INPUTS = (
    "content/typst/handout.typ",
    "content/typst/fachwerte.typ",
    "content/typst/vorlage.typ",
    "content/typst/handoutassets/bragg-summary.svg",
    "content/typst/auswahl.json",
    "content/data/versuchsdaten.json",
)


def sha256(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def tool(name: str, *args: str) -> str:
    if not shutil.which(name):
        raise RuntimeError(f"Required validation tool is missing: {name} (install poppler-utils)")
    return subprocess.check_output([name, *args], text=True, encoding="utf-8")


def control_values(data: dict) -> dict:
    """Independent Python equations; Typst computes its own displayed values."""
    if data["schema_version"] != "2.0":
        raise RuntimeError("Review the handout against the changed canonical data schema")
    constants = data["constants"]
    h, e, me, c = (constants[k]["value"] for k in ("h", "e", "electron_mass", "c"))
    nr = lambda u: h / math.sqrt(2 * me * e * u)
    rel = lambda u: nr(u) / math.sqrt(1 + e * u / (2 * me * c * c))
    dg = data["davisson_germer"]
    if len(dg["rows"]) != 1 or dg["evidence"]["source_id"] != "D2":
        raise RuntimeError("Review the selected DG evidence before building this handout")
    row = dg["rows"][0]
    D = dg["geometry"]["lattice_constant_m"] * math.sqrt(3 / 8)
    geo = D * math.sin(math.radians(row["phi_deg"]))
    image = data["jonsson"]["image"]
    marks = image["markers_px"]
    if len(marks) < 2 or any(b <= a for a, b in zip(marks, marks[1:])):
        raise RuntimeError("Reference image coordinates must be strictly increasing")
    u = data["jonsson"]["parameter_model"]["voltage_V"]
    return {
        "de_broglie": {
            "voltage_V": 150,
            "kinetic_energy_J": 150 * e,
            "momentum_kg_m_per_s": math.sqrt(2 * me * e * 150),
            "wavelength_150V_pm": nr(150) * 1e12,
            "wavelength_600V_pm": nr(600) * 1e12,
            "wavelength_ratio_600V_to_150V": nr(600) / nr(150),
        },
        "davisson_germer": {
            "voltage_V": row["voltage_V"], "phi_deg": row["phi_deg"],
            "surface_row_spacing_nm": D * 1e9,
            "geometric_wavelength_nm": geo * 1e9,
            "nonrelativistic_wavelength_nm": nr(row["voltage_V"]) * 1e9,
            "relative_difference_percent_NR_denominator": 100 * (geo - nr(row["voltage_V"])) / nr(row["voltage_V"]),
        },
        "jonsson": {
            "separate_model_voltage_V": u,
            "nonrelativistic_wavelength_pm": nr(u) * 1e12,
            "relativistic_wavelength_pm": rel(u) * 1e12,
            "relative_NR_difference_percent_rel_denominator": 100 * (nr(u) - rel(u)) / rel(u),
            "reference_markers_native_px": marks,
            "reference_intervals_native_px": [b - a for a, b in zip(marks, marks[1:])],
            "mean_reference_interval_native_px": (marks[-1] - marks[0]) / (len(marks) - 1),
            "measurement_band_y_native_px": image["measurement_band_y_px"],
            "image_dimensions_native_px": [image["native_width_px"], image["native_height_px"]],
            "source_image_sha256": image["sha256"],
            "absolute_wavelength_from_image": None,
        },
    }


def validate_pdf(path: Path, values: dict) -> dict:
    info = tool("pdfinfo", str(path))
    pages = int(re.search(r"^Pages:\s+(\d+)", info, re.M)[1])
    size = re.search(r"^Page size:\s+([\d.]+) x ([\d.]+) pts", info, re.M)
    width, height = map(float, size.groups())
    if pages != 3 or abs(width - 595.276) > .05 or abs(height - 841.89) > .05:
        raise RuntimeError(f"Expected exactly three A4 pages, got {pages}, {width} × {height} pt")
    fonts = []
    for line in tool("pdffonts", str(path)).splitlines()[2:]:
        parts = line.split()
        if not parts:
            continue
        # The five rightmost fields are emb/sub/uni/object/generation.
        if parts[-5] != "yes":
            raise RuntimeError(f"Unembedded PDF font: {line}")
        fonts.append({"name": parts[0].split("+", 1)[-1], "embedded": True})
    if not any("Roman" in font["name"] for font in fonts) or not any("Math" in font["name"] for font in fonts):
        raise RuntimeError("Expected embedded Roman and mathematical fonts")
    text = tool("pdftotext", "-layout", str(path), "-")
    if "�" in text:
        raise RuntimeError("PDF contains replacement glyphs")
    expected = []
    for group, key, digits in (
        ("de_broglie", "wavelength_150V_pm", 3),
        ("de_broglie", "wavelength_600V_pm", 3),
        ("davisson_germer", "surface_row_spacing_nm", 4),
        ("davisson_germer", "geometric_wavelength_nm", 3),
        ("davisson_germer", "nonrelativistic_wavelength_nm", 3),
        ("jonsson", "nonrelativistic_wavelength_pm", 3),
        ("jonsson", "relativistic_wavelength_pm", 3),
        ("jonsson", "relative_NR_difference_percent_rel_denominator", 2),
        ("jonsson", "mean_reference_interval_native_px", 1),
    ):
        rendered = f"{values[group][key]:.{digits}f}".replace(".", ",")
        if rendered not in text:
            raise RuntimeError(f"Independent control value missing from PDF: {group}.{key} = {rendered}")
        expected.append(rendered)
    for term in ("De Broglie", "Davisson", "Jönsson", "Langevin", "Bragg", "H1", "H3", "D2", "D1", "J1", "keine absolute", "Sekundäranalyse"):
        if term not in text:
            raise RuntimeError(f"Required handout content missing: {term}")
    # Guard clipping, including SVG labels. This is not a substitute for visual review.
    bbox = ET.fromstring(tool("pdftotext", "-bbox", str(path), "-"))
    word_count = 0
    for page in bbox.iter("{http://www.w3.org/1999/xhtml}page"):
        pw, ph = float(page.attrib["width"]), float(page.attrib["height"])
        for word in page.iter("{http://www.w3.org/1999/xhtml}word"):
            coords = [float(word.attrib[k]) for k in ("xMin", "yMin", "xMax", "yMax")]
            if not (0 <= coords[0] <= coords[2] <= pw and 0 <= coords[1] <= coords[3] <= ph):
                raise RuntimeError(f"Text outside page: {word.text}")
            word_count += 1
    if word_count < 500:
        raise RuntimeError("Unexpectedly sparse or nonextractable PDF text")
    return {
        "pages": pages, "paper": "A4", "page_size_pt": [width, height],
        "body_font_pt": 11, "smallest_authored_text_pt": 9.5,
        "font_size_scope": "Body/caption/header/footer base sizes; mathematical subscripts are naturally smaller.",
        "fonts": fonts, "all_fonts_embedded": True,
        "text_inside_page_bounds": True, "extractable_words": word_count,
        "independent_displayed_values": expected,
        "pdf_creator": re.search(r"^Creator:\s+(.+)", info, re.M)[1],
    }


def check_output(output: Path, replace_generated: bool, payload: dict[str, bytes]) -> bool:
    """True means write. Refuse unrelated files or edits, even with replacement."""
    if output.is_symlink():
        raise RuntimeError("Output directory must not be a symbolic link")
    if output.exists() and not output.is_dir():
        raise RuntimeError("Output path must be a directory")
    if not output.exists() or not any(output.iterdir()):
        return True
    names = {p.name for p in output.iterdir()}
    if names != set(payload) or any(p.is_symlink() or not p.is_file() for p in output.iterdir()):
        raise RuntimeError("Use a fresh handout directory: existing unrelated files will not be removed")
    if all((output / name).read_bytes() == data for name, data in payload.items()):
        return False
    if not replace_generated:
        raise RuntimeError("Output differs. Use a fresh directory or --replace-generated for this builder's unedited artifacts")
    try:
        previous = json.loads((output / MANIFEST_NAME).read_text(encoding="utf-8"))
        expected_hash = previous["artifact"]["sha256"]
    except (ValueError, KeyError) as error:
        raise RuntimeError("Existing output is not this builder's valid manifest") from error
    if previous.get("kind") != KIND or expected_hash != sha256((output / PDF_NAME).read_bytes()):
        raise RuntimeError("Existing PDF was changed or is not this builder's artifact; refusing overwrite")
    return True


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output-dir", required=True, type=Path)
    parser.add_argument("--replace-generated", action="store_true", help="Update only this builder's two unedited generated files; never remove other files")
    args = parser.parse_args()
    try:
        import typst
    except ImportError:
        cache = Path(os.environ.get("XDG_CACHE_HOME", Path.home() / ".cache"))
        installed = Path(os.environ.get("TASK_TOOLS_ROOT", cache / "materiewelle-tools")) / "typst/bin/python"
        if installed.exists() and Path(sys.executable).absolute() != installed.absolute():
            os.execv(str(installed), [str(installed), str(Path(__file__).resolve()), *sys.argv[1:]])
        raise RuntimeError("Install the pinned toolchain: python -m pip install typst==0.14.2")
    output = args.output_dir.absolute()
    if output == ROOT or output == HERE or output == HERE / "handoutassets":
        raise RuntimeError("Choose a separate generated output directory")
    data = json.loads((ROOT / "content/data/versuchsdaten.json").read_text(encoding="utf-8"))
    values = control_values(data)
    reference_image = data["jonsson"]["image"]
    if sha256((ROOT / reference_image["file"]).read_bytes()) != reference_image["sha256"]:
        raise RuntimeError("Canonical image provenance differs from the actual native reference image")
    with tempfile.TemporaryDirectory(prefix="materiewelle-handout-") as temporary:
        compiled = Path(temporary) / PDF_NAME
        _, warnings = typst.compile_with_warnings(str(HERE / "handout.typ"), output=str(compiled), root=str(ROOT),
                                                 font_paths=[str(FONT_DIRECTORY)])
        if warnings:
            raise RuntimeError(f"Typst warnings: {warnings}")
        pdf = compiled.read_bytes()
        validation = validate_pdf(compiled, values)
    manifest = {
        "kind": KIND, "version": "1.0.0", "document_revision": "2026-11-09",
        "product_scope": "Three-page handout, including sources, no cover.",
        "python_typst_version": importlib.metadata.version("typst"),
        "data_schema_version": data["schema_version"],
        "canonical_inputs": [{"path": name, "sha256": sha256((ROOT / name).read_bytes())} for name in SOURCE_INPUTS],
        "artifact": {"file": PDF_NAME, "bytes": len(pdf), "sha256": sha256(pdf)},
        "validation": {**validation, "compiler_warnings": 0},
        "control_values": values,
        "reference_image_provenance": {
            "path": reference_image["file"], "sha256": reference_image["sha256"],
            "verified_against_local_file": True, "embedded_in_handout": False,
        },
        "evidence_roles": {
            "H1_H3": "Historical recollection / secondary analysis, inspected in documented scope.",
            "D2": "Secondary report of approximate 54 eV / 50 degree finding; independent declared Ni(111) structure model.",
            "D1": "Original source cited, not inspected in this project.",
            "J1": "Abridged 1974 English translation of 1961 work; native pixel reference example, no absolute calibration.",
            "figures": "Project-authored schemata; no historical photograph embedded in this handout.",
            "rights": "Public reuse basis of the historical image remains unresolved; the handout only describes relative reference coordinates.",
        },
    }
    payload = {PDF_NAME: pdf, MANIFEST_NAME: (json.dumps(manifest, ensure_ascii=False, indent=2, sort_keys=True) + "\n").encode("utf-8")}
    if check_output(output, args.replace_generated, payload):
        output.mkdir(parents=True, exist_ok=True)
        for name, content in payload.items():
            with tempfile.NamedTemporaryFile(dir=output, prefix=f".{name}.", delete=False) as target:
                target.write(content)
                temporary_name = target.name
            os.replace(temporary_name, output / name)
    print(f"Handout: {output / PDF_NAME} · 3 × A4 · all fonts embedded · 0 compiler warnings")
    print(f"Manifest: {output / MANIFEST_NAME}")


if __name__ == "__main__":
    try:
        main()
    except Exception as error:
        diagnostic = getattr(error, "diagnostic", None)
        print(f"Handout build failed: {diagnostic or error}", file=sys.stderr)
        sys.exit(1)
