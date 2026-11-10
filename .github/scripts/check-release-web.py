#!/usr/bin/env python3
"""Run the revision's complete root contract and the project-path route contract."""
from pathlib import Path
import os
from browser_checks import run_checks

ROOT = Path(os.environ.get("RELEASE_PROJECT_ROOT", Path(__file__).resolve().parents[2]))
APP = ROOT / "apps/web"

for label, dist, base, group in (
    ("root", "dist-run-two", "/", "all"),
    ("subpath", "dist-run-two-subpath", "/materiewelle/", "route"),
):
    run_checks(project_root=ROOT, dist=APP / dist, output=ROOT / "release-evidence" / label,
               base=base, group=group)
