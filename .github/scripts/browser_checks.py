#!/usr/bin/env python3
"""One explicit browser-suite matrix, with immutable evidence for the tested bytes.

`all` runs the root contract; `route` runs the existing root/subpath checks;
`focused` runs the three root-only regressions without repeating route checks.
Every public revision must provide the complete current browser contract.
"""
from __future__ import annotations

import argparse
import ast
from dataclasses import dataclass
from datetime import datetime, timezone
import json
from pathlib import Path
import subprocess
import sys

from product_delivery import digest, inventory, proof
from release_browser import chromium_executable

SCHEMA = "materiewelle-browser-suites-1"
SCRIPTS = Path("apps/web/scripts")


@dataclass(frozen=True)
class Suite:
    name: str
    filename: str
    focused: bool = False


SUITES = (
    Suite("main", "test-run-two.py"),
    Suite("refinement", "test-ui-refinement.py"),
    Suite("dg-learning", "test-dg-learning-path.py"),
    Suite("ownership", "test-session-ownership.py", True),
    Suite("jonsson", "test-jonsson-workspace.py", True),
    Suite("personal-report", "test-personal-report.py", True),
)


def select_suites(root: Path, group: str, base: str):
    if group not in ("all", "route", "focused"):
        raise ValueError("Unknown browser suite group: " + group)
    if base not in ("/", "/materiewelle/"):
        raise ValueError("Unsupported deployment base: " + base)
    if group != "route" and base != "/":
        raise ValueError("Focused regressions require a real root export; use route for a subpath")
    candidates = [s for s in SUITES if group == "all" or s.focused == (group == "focused")]
    for suite in candidates:
        if not (root / SCRIPTS / suite.filename).is_file():
            raise FileNotFoundError("Missing mandatory browser suite: " + suite.filename)
    return candidates


def expected_scenarios(harness: Path):
    """Read the checked-out harness contract without importing Playwright or code execution."""
    tree = ast.parse(harness.read_text())
    for node in ast.walk(tree):
        if isinstance(node, ast.Call) and (
            (isinstance(node.func, ast.Attribute) and node.func.attr == "main") or
            (isinstance(node.func, ast.Name) and node.func.id == "main")
        ):
            for keyword in node.keywords:
                if keyword.arg == "scenarios" and isinstance(keyword.value, (ast.Tuple, ast.List)):
                    return tuple(ast.literal_eval(keyword.value))
    for node in ast.walk(tree):
        if isinstance(node, ast.Assign) and any(
            (isinstance(t, ast.Name) and t.id == "SCENARIOS") or
            (isinstance(t, ast.Attribute) and t.attr == "SCENARIOS") for t in node.targets
        ):
            return tuple(ast.literal_eval(node.value))
    raise ValueError("No explicit scenario contract in " + str(harness))


def suite_output(output: Path, suite: Suite):
    return output if suite.name == "main" else output.with_name(output.name + "-" + suite.name)


def summary_path(output: Path):
    return output.with_name(output.name + "-suites.json")


def harness_inputs(root: Path):
    """Hash the mandatory suites and their actual local Python dependencies.

    Static imports and the existing literal importlib paths are followed without
    executing a harness. Unknown dynamic paths fail closed. Archived scenarios or
    unrelated asset generators no longer invalidate current browser evidence.
    """
    directory = root / SCRIPTS
    pending = [directory / suite.filename for suite in SUITES if (directory / suite.filename).is_file()]
    paths = set()
    while pending:
        path = pending.pop()
        if path in paths:
            continue
        paths.add(path)
        tree = ast.parse(path.read_text())
        names = set()
        for node in ast.walk(tree):
            if isinstance(node, ast.Import):
                names.update(alias.name.split('.')[0] + '.py' for alias in node.names)
            elif isinstance(node, ast.ImportFrom) and node.module:
                names.add(node.module.split('.')[0] + '.py')
            elif isinstance(node, ast.Call) and isinstance(node.func, ast.Attribute) and node.func.attr == 'spec_from_file_location':
                if len(node.args) < 2:
                    raise ValueError('Unresolved dynamic harness import: ' + str(path))
                targets = [value.value for value in ast.walk(node.args[1])
                           if isinstance(value, ast.Constant) and isinstance(value.value, str) and value.value.endswith('.py')]
                if len(targets) != 1 or Path(targets[0]).name != targets[0]:
                    raise ValueError('Unresolved dynamic harness import: ' + str(path))
                dependency = directory / targets[0]
                if not dependency.is_file():
                    raise FileNotFoundError('Missing harness dependency: ' + str(dependency))
                names.add(targets[0])
        pending.extend(directory / name for name in names if (directory / name).is_file())
    paths = sorted(paths)
    return {p.relative_to(root).as_posix(): digest(p) for p in paths}


def verify_report(root: Path, suite: Suite, report_path: Path, manifest: dict, manifest_path: Path):
    report = json.loads(report_path.read_text())
    assert report.get("status") == "passed", "Suite report is not passed: " + suite.name
    assert report.get("test_harness_sha256") == digest(root / SCRIPTS / suite.filename), "Stale suite harness: " + suite.name
    release = report["release"]
    assert release.get("manifest_sha256") == digest(manifest_path), "Suite belongs to another manifest: " + suite.name
    assert release.get("javascript_sha256") == manifest["files"]["materiewellen.js"]["sha256"], "Suite JavaScript differs"
    assert release.get("deployment_base") == manifest["deployment_base"], "Suite deployment base differs"
    actual = report.get("scenarios", [])
    expected = expected_scenarios(root / SCRIPTS / suite.filename)
    assert len(actual) == len(expected) and {s["name"] for s in actual} == set(expected), "Missing or duplicate scenarios: " + suite.name
    assert all(s.get("passed") is True for s in actual), "Failed scenario: " + suite.name
    assert report.get("checks") and all(c.get("passed") is True for c in report["checks"]), "Failed or absent checks: " + suite.name
    if suite.name != "ownership":
        assert report.get("base_harness_sha256") == digest(root / SCRIPTS / "test-run-two.py"), "Stale base harness"
        assert report.get("contract") == manifest["product_contract"], "Wrong product contract"
    if suite.name == "main":
        proof(report_path, manifest, manifest_path=manifest_path,
              harness_path=root / SCRIPTS / suite.filename)
    return report


def verify_evidence(*, project_root: Path, output: Path, manifest_path: Path,
                    group: str = "all"):
    """Require a complete group receipt and recheck every original report; never rewrite it."""
    manifest = json.loads(manifest_path.read_text())
    base = manifest["deployment_base"]
    inventory(manifest_path.parent, base)
    selected = select_suites(project_root, group, base)
    receipt = json.loads(summary_path(output).read_text())
    assert receipt.get("schema") == SCHEMA and receipt.get("status") == "passed", "Missing/failed browser group contract"
    assert receipt.get("group") == group, "Wrong browser suite selection"
    assert receipt.get("deployment_base") == base, "Wrong browser group base"
    assert receipt.get("manifest_sha256") == digest(manifest_path), "Browser group belongs to another manifest"
    assert receipt.get("harness_inputs") == harness_inputs(project_root), "Browser helpers changed after verification"
    assert receipt.get("runner_sha256") == digest(Path(__file__)), "Browser runner changed after verification"
    entries = receipt.get("suites", [])
    assert [e.get("suite") for e in entries] == [s.name for s in selected], "Required suite evidence missing or duplicated"
    for suite, entry in zip(selected, entries):
        path = suite_output(output, suite) / "report.json"
        assert entry.get("status") == "passed" and entry.get("report_sha256") == digest(path), "Suite report changed or failed"
        verify_report(project_root, suite, path, manifest, manifest_path)
    return receipt


def run_checks(*, project_root: Path, dist: Path, output: Path, base: str = "/",
               group: str = "all", chromium: str | None = None):
    selected = select_suites(project_root, group, base)
    artifact = dist if base == "/" else dist / base.strip("/")
    manifest_path = artifact / "manifest.json"
    manifest = inventory(artifact, base)
    summary = summary_path(output)
    if summary.exists():
        raise FileExistsError("Existing group evidence is preserved: " + str(summary))
    for suite in selected:
        directory = suite_output(output, suite)
        if directory.exists() and any(directory.iterdir()):
            raise FileExistsError("Choose fresh suite evidence: " + str(directory))
    output.parent.mkdir(parents=True, exist_ok=True)
    receipt = {"schema": SCHEMA, "recorded_at": datetime.now(timezone.utc).isoformat(),
               "status": "running", "group": group,
               "deployment_base": base, "manifest_sha256": digest(manifest_path),
               "harness_inputs": harness_inputs(project_root), "runner_sha256": digest(Path(__file__)),
               "suites": []}

    def save():
        summary.write_text(json.dumps(receipt, ensure_ascii=False, indent=2) + "\n")

    save()
    try:
        for suite in selected:
            destination = suite_output(output, suite)
            command = [sys.executable, str(project_root / SCRIPTS / suite.filename),
                       "--dist", str(dist), "--output", str(destination),
                       "--chromium", chromium or chromium_executable()]
            if suite.name != "ownership":
                command.extend(["--base-path", base])
            entry = {"suite": suite.name, "harness": suite.filename, "command": command, "status": "running"}
            receipt["suites"].append(entry)
            save()
            try:
                subprocess.run(command, check=True)
                path = destination / "report.json"
                verify_report(project_root, suite, path, manifest, manifest_path)
                entry.update(status="passed", report_sha256=digest(path))
            except BaseException as error:
                entry.update(status="failed", failure=type(error).__name__ + ": " + str(error))
                raise
            finally:
                save()
        receipt["status"] = "passed"
        save()
        return verify_evidence(project_root=project_root, output=output, manifest_path=manifest_path,
                               group=group)
    except BaseException as error:
        receipt.update(status="failed", failure=type(error).__name__ + ": " + str(error))
        save()
        raise


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--project-root", type=Path, default=Path(__file__).resolve().parents[2])
    parser.add_argument("--dist", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--base-path", default="/", choices=("/", "/materiewelle/"))
    parser.add_argument("--group", default="all", choices=("all", "route", "focused"))
    parser.add_argument("--chromium")
    args = parser.parse_args()
    run_checks(project_root=args.project_root.resolve(), dist=args.dist.resolve(), output=args.output.resolve(),
               base=args.base_path, group=args.group, chromium=args.chromium)


if __name__ == "__main__":
    main()
