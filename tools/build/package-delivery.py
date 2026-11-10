#!/usr/bin/env python3
"""Package the current verified web exports and handout using the release checks.

No build, deployment, Git mutation or dependency installation is performed.
The output directory must be empty; use build-release-web.py and
check-release-web.py first to produce the distributions and browser evidence.
"""
import argparse
from pathlib import Path
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[2]


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("output", type=Path, help="Empty release staging directory")
    parser.add_argument("--commit", help="Full source commit SHA; defaults to HEAD")
    parser.add_argument("--root-dist", type=Path)
    parser.add_argument("--subpath-dist", type=Path)
    parser.add_argument("--evidence", type=Path)
    args = parser.parse_args()
    if subprocess.check_output(["git", "status", "--porcelain"], cwd=ROOT).strip():
        parser.error("Commit the reviewed working tree before packaging.")
    commit = args.commit or subprocess.check_output(
        ["git", "rev-parse", "HEAD"], cwd=ROOT, text=True).strip()
    command = [sys.executable, str(ROOT / ".github/scripts/prepare-release.py"),
               "--commit", commit, "--output", str(args.output.resolve())]
    for option in ("root_dist", "subpath_dist", "evidence"):
        value = getattr(args, option)
        if value is not None:
            command.extend(["--" + option.replace("_", "-"), str(value.resolve())])
    subprocess.run(command, cwd=ROOT, check=True)


if __name__ == "__main__":
    main()
