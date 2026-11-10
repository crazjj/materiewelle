"""Validate the current static product and its actual browser proof."""
from pathlib import Path
import hashlib, json, os, re, shutil, sys, zipfile
ROOT = Path(os.environ.get("RELEASE_PROJECT_ROOT", Path(__file__).resolve().parents[2]))
sys.path.insert(0, str(ROOT / "apps/web/scripts"))
from product_assets import CONTRACT, TUTOR_FILES, LEGAL_INPUTS, allowed_asset
def verify_source_inputs(manifest):
    sys.path.insert(0, str(ROOT / "tools/build"))
    from build_inputs import product_input_hashes
    assert manifest.get("input_hashes") == product_input_hashes(ROOT), "Source changed after the recorded build"

SCENARIOS = {"articles", "dg", "measurement", "state", "tutor", "handout", "viewports", "data_failure"}
def digest(path): return hashlib.sha256(path.read_bytes()).hexdigest()
def inventory(directory, base):
    manifest = json.loads((directory / "manifest.json").read_text())
    assert manifest.get("product_contract") == CONTRACT, "Unsupported product contract"
    assert manifest["deployment_base"] == base
    assert "no prerendering claim" in manifest["mode"]
    files = manifest["files"]
    assert not any(p.is_symlink() for p in directory.rglob("*")), "Symlink in product artifact"
    actual = {p.relative_to(directory).as_posix() for p in directory.rglob("*") if p.is_file()} - {"manifest.json", ".materiewellen-generated"}
    assert actual == set(files), "Unmanifested or missing artifact files"
    for name, item in files.items():
        assert allowed_asset(name), "Excluded internal/unknown asset: " + name
        p = directory / name
        assert not p.is_symlink() and p.resolve().is_relative_to(directory.resolve())
        assert digest(p) == item["sha256"] and p.stat().st_size == item["bytes"]
    required = {"index.html", "materiewellen.js", "app.css", "versuchsdaten.json", "handout/materiewellen.pdf", "handout/manifest.json"}
    required.update("tutor/" + name for name in TUTOR_FILES)
    required.update(LEGAL_INPUTS)
    assert required <= set(files), "Missing required product assets"
    handout = json.loads((directory / "handout/manifest.json").read_text())
    validate_handout(handout, (directory / "handout/materiewellen.pdf").read_bytes())
    if "input_hashes" in manifest:
        assert all(manifest["input_hashes"].get(item["path"]) == item["sha256"] for item in handout["canonical_inputs"]), "Handout sources differ from application build"
        assert all(manifest["input_hashes"].get(source) == files[name]["sha256"]
                   for name, source in LEGAL_INPUTS.items()), "License/notice differs from recorded source"
    return manifest

def validate_handout(manifest, pdf):
    """Verify the build's PDF/provenance claim against the exact shipped bytes."""
    assert manifest.get("kind") == "materiewellen-product-handout", "Wrong handout kind"
    artifact = manifest["artifact"]
    assert artifact["file"] == "materiewellen.pdf"
    assert artifact["bytes"] == len(pdf) and artifact["sha256"] == hashlib.sha256(pdf).hexdigest(), "Handout provenance does not match PDF"
    assert pdf.startswith(b"%PDF-"), "Handout is not a PDF"
    validation = manifest["validation"]
    assert type(validation["pages"]) is int and 2 <= validation["pages"] <= 3
    assert validation["paper"] == "A4" and validation["all_fonts_embedded"] is True
    assert validation["text_inside_page_bounds"] is True and validation["compiler_warnings"] == 0
    assert manifest["canonical_inputs"], "Handout source provenance missing"
    for item in manifest["canonical_inputs"]:
        path = Path(item["path"])
        assert not path.is_absolute() and ".." not in path.parts
        assert len(item["sha256"]) == 64 and all(c in "0123456789abcdef" for c in item["sha256"])

def stage_public_site(source, output, *, base, commit):
    """Curate the same verified static product for a prebuilt static host."""
    assert re.fullmatch(r"[0-9a-f]{40}", commit), "Use a full source commit SHA"
    source, output = source.resolve(), output.resolve()
    assert not output.is_relative_to(source) and not source.is_relative_to(output), "Source and public staging must not overlap"
    manifest = inventory(source, base)
    verify_source_inputs(manifest)
    output.mkdir(parents=True, exist_ok=True)
    assert not any(output.iterdir()), "Public staging directory must be empty"
    for name, expected in manifest["files"].items():
        path = source / name
        assert path.resolve().is_relative_to(source) and not path.is_symlink()
        assert path.stat().st_size == expected["bytes"] and digest(path) == expected["sha256"]
        if name.endswith(".map"):
            continue
        assert not name.endswith(".kt"), "Do not publish Kotlin source files"
        target = output / name
        target.parent.mkdir(parents=True, exist_ok=True)
        if name.endswith(".js"):
            target.write_text(re.sub(r"(?m)^\s*//# sourceMappingURL=.*$", "", path.read_text()))
        else:
            shutil.copyfile(path, target)
    files = {p.relative_to(output).as_posix(): {"bytes": p.stat().st_size, "sha256": digest(p)}
             for p in sorted(output.rglob("*")) if p.is_file()}
    public = {"product_contract": CONTRACT, "input_hashes": manifest["input_hashes"],
              "source_commit": commit, "mode": manifest["mode"], "deployment_base": base,
              "public_deployment": True, "source_maps_published": False, "files": files}
    (output / "manifest.json").write_text(json.dumps(public, ensure_ascii=False, indent=2) + "\n")
    return public

def proof(path, manifest, *, manifest_path, harness_path=None):
    result = json.loads(path.read_text())
    assert result["status"] == "passed"
    assert result.get("contract") == CONTRACT, "Wrong browser contract"
    harness = harness_path or ROOT / "apps/web/scripts/test-run-two.py"
    assert result.get("test_harness_sha256") == digest(harness), "Browser harness changed after verification"
    assert result["release"].get("manifest_sha256") == digest(manifest_path), "Browser proof belongs to a different asset manifest"
    assert result["release"]["javascript_sha256"] == manifest["files"]["materiewellen.js"]["sha256"]
    assert result["release"]["deployment_base"] == manifest["deployment_base"]
    for key in ("page_errors", "console_messages", "request_failures", "http_errors", "forbidden_requests", "outbound_requests"):
        assert result[key] == [], key
    assert len(result["scenarios"]) == len(SCENARIOS)
    assert {s["name"] for s in result["scenarios"]} == SCENARIOS
    assert all(s["passed"] for s in result["scenarios"])
    assert result["checks"] and all(c["passed"] for c in result["checks"])
    return result

def archive(destination, directory, prefix=""):
    with zipfile.ZipFile(destination, "w", zipfile.ZIP_DEFLATED) as z:
        for p in sorted(directory.rglob("*")):
            if p.is_file() and p.name != ".materiewellen-generated":
                name = p.relative_to(directory).as_posix()
                assert name == "manifest.json" or allowed_asset(name), name
                assert not p.is_symlink()
                info = zipfile.ZipInfo(prefix + name)
                info.external_attr = 0o100644 << 16
                info.compress_type = zipfile.ZIP_DEFLATED
                z.writestr(info, p.read_bytes())
