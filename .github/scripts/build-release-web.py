#!/usr/bin/env python3
"""Build the current root and subpath exports without editing source files."""
from __future__ import annotations

import json
import os
from pathlib import Path
import subprocess


def build_release(project_root: Path) -> dict:
    project_root = project_root.resolve()
    app = project_root / "apps/web"
    report_path = project_root / "release-evidence/build.json"
    report_path.parent.mkdir(parents=True, exist_ok=True)
    if report_path.exists():
        raise FileExistsError("Preserve previous build evidence; choose a fresh checkout")
    report = {"schema_version": "1.0", "status": "building", "builds": []}

    def save():
        report_path.write_text(json.dumps(report, indent=2) + "\n")

    save()
    try:
        for base in ("/", "/materiewelle/"):
            command = [str(app / "build-static.sh")]
            if base != "/":
                command.extend(["--base-path", base])
            build = {"base_path": base, "command": command, "status": "running"}
            report["builds"].append(build)
            save()
            try:
                subprocess.run(command, cwd=app, check=True)
            except BaseException as error:
                build.update(status="failed", failure=type(error).__name__ + ": " + str(error))
                raise
            build["status"] = "passed"
            save()
        report["status"] = "passed"
    except BaseException as error:
        report.update(status="failed", failure=type(error).__name__ + ": " + str(error))
        raise
    finally:
        save()
    return report


if __name__ == "__main__":
    build_release(Path(os.environ.get("RELEASE_PROJECT_ROOT", Path(__file__).resolve().parents[2])))
