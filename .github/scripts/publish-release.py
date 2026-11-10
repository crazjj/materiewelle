#!/usr/bin/env python3
"""Publish all assets together; never move latest backwards after a slow build."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import sys
import tempfile


def gh(*args):
    result = subprocess.run(["gh", *args], text=True, capture_output=True)
    if result.returncode:
        # Keep the actionable GitHub response available in check annotations,
        # even when the cloud environment cannot download Actions log archives.
        detail = re.sub(r"(https?://[^\s?]+)\?[^\s]+", r"\1?[redacted]", result.stderr.strip())
        raise RuntimeError("gh " + " ".join(args[:2]) + " failed: " + detail)
    return result.stdout


def api(path):
    return json.loads(gh("api", path))


def all_releases(base):
    # --jq emits compact JSON objects, one per line, across every result page.
    # Avoid --slurp, which is absent in the cloud environment's older gh CLI.
    return [json.loads(line) for line in gh("api", "--paginate", "--jq", ".[]", base + "/releases?per_page=100").splitlines()
            if line.strip()]


def verify_tag_commit(base, tag, commit):
    result = subprocess.run(["gh", "api", base + "/git/ref/tags/" + tag], capture_output=True, text=True)
    if result.returncode:
        if "404" in result.stderr:
            raise RuntimeError("Release tag must be reserved before publication: " + tag)
        raise RuntimeError("Could not verify release tag: " + result.stderr.strip())
    target = json.loads(result.stdout)["object"]
    for _ in range(8):
        if target["type"] == "commit":
            assert target["sha"] == commit, "Release tag must point at the exact application commit"
            return
        assert target["type"] == "tag", "Unsupported release tag target"
        target = api(base + "/git/tags/" + target["sha"])["object"]
    raise RuntimeError("Release tag nesting limit exceeded")


def create_draft(repo, tag, commit, notes):
    # The plan reserves the exact ref before the long build. Do not send an
    # unnecessary target_commitish for an existing tag: GitHub can reject a
    # historical workflow tree even though this verified ref already exists.
    verify_tag_commit("repos/" + repo, tag, commit)
    gh("release", "create", tag, "--repo", repo, "--verify-tag",
       "--title", "Materiewelle · " + commit[:12], "--notes-file", str(notes), "--draft", "--latest=false")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--commit", required=True)
    parser.add_argument("--assets", type=Path, required=True)
    args = parser.parse_args()
    assert re.fullmatch(r"[0-9a-f]{40}", args.commit)
    repo = os.environ["GITHUB_REPOSITORY"]
    assert re.fullmatch(r"[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+", repo)
    tag = "build-" + args.commit
    assets = sorted(args.assets.iterdir())
    assert all(p.is_file() and not p.is_symlink() for p in assets)
    checksums = (args.assets / "SHA256SUMS.txt").read_text().splitlines()
    expected = {}
    for line in checksums:
        checksum, name = line.split("  ", 1)
        assert Path(name).name == name and re.fullmatch(r"[0-9a-f]{64}", checksum)
        path = args.assets / name
        assert hashlib.sha256(path.read_bytes()).hexdigest() == checksum
        expected[name] = {"sha256": checksum, "bytes": path.stat().st_size}
    sums = args.assets / "SHA256SUMS.txt"
    expected[sums.name] = {"sha256": hashlib.sha256(sums.read_bytes()).hexdigest(), "bytes": sums.stat().st_size}
    assert set(expected) == {p.name for p in assets}
    manifest = json.loads((args.assets / "release-manifest.json").read_text())
    assert manifest["source_commit"] == args.commit
    assert manifest.get("product_contract") == "run-two-2026-11-04" and manifest["typst_pdf_count"] == 1
    assert len(assets) == 6 and "materiewellen-handout.pdf" in expected
    base = "repos/" + repo
    verify_tag_commit(base, tag, args.commit)
    releases = all_releases(base)
    release = next((item for item in releases if item["tag_name"] == tag), None)
    if release is None:
        # Lookup old tags directly too; draft releases are found in the list above.
        result = subprocess.run(["gh", "api", base + "/releases/tags/" + tag], capture_output=True, text=True)
        if result.returncode == 0:
            release = json.loads(result.stdout)
        elif "404" not in result.stderr:
            raise RuntimeError(result.stderr)
    branch = api(base)["default_branch"]
    latest_sha = api(base + "/git/ref/heads/" + branch)["object"]["sha"]
    make_latest = latest_sha == args.commit

    if release and not release["draft"]:
        # A retry keeps the original complete release immutable.
        remote = {item["name"]: item for item in api(base + f"/releases/{release['id']}/assets?per_page=100")}
        assert set(expected) <= set(remote), "Existing published release is incomplete"
        with tempfile.TemporaryDirectory(prefix="materiewelle-existing-release-") as temporary:
            gh("release", "download", tag, "--repo", repo, "--pattern", "release-manifest.json",
               "--pattern", "SHA256SUMS.txt", "--dir", temporary)
            previous = json.loads((Path(temporary) / "release-manifest.json").read_text())
            assert previous["source_commit"] == args.commit
            for line in (Path(temporary) / "SHA256SUMS.txt").read_text().splitlines():
                checksum, name = line.split("  ", 1)
                assert name in remote
                if remote[name].get("digest"):
                    assert remote[name]["digest"] == "sha256:" + checksum
                if name == "release-manifest.json":
                    assert hashlib.sha256((Path(temporary) / name).read_bytes()).hexdigest() == checksum
        if make_latest:
            gh("release", "edit", tag, "--repo", repo, "--latest")
        print("Complete release already published:", release["html_url"])
        return

    with tempfile.TemporaryDirectory(prefix="materiewelle-release-") as temporary:
        notes = Path(temporary) / "notes.md"
        notes.write_text(
            f"Automatischer Produkt-Build von `{args.commit}`.\n\n"
            "- **Kobweb:** statischer SPA-Export für `/` und zusätzlich `/materiewelle/`.\n"
            "- **Umfang:** vier Fachartikel, vier Tutor-Starttexte, portabler Fachtutor und aktuelles Typst-Handout.\n\n"
            "Startanleitung: `START.md`. Herkunft und Prüfwerte: `release-manifest.json`, `SHA256SUMS.txt`.\n"
            f"Browserprüfungen: {manifest['kobweb']['browser_reports']}. Safari und reale Geräte separat prüfen.\n",
            encoding="utf8",
        )
        if release is None:
            create_draft(repo, tag, args.commit, notes)
        verify_tag_commit(base, tag, args.commit)
        gh("release", "upload", tag, "--repo", repo, "--clobber", *(str(p) for p in assets))
        releases = all_releases(base)
        release = next(item for item in releases if item["tag_name"] == tag)
        remote = {item["name"]: item for item in api(base + f"/releases/{release['id']}/assets?per_page=100")}
        assert set(expected) == set(remote), "Uploaded assets are incomplete"
        for name, item in expected.items():
            assert remote[name]["size"] == item["bytes"]
            if remote[name].get("digest"):
                assert remote[name]["digest"] == "sha256:" + item["sha256"]
        # Recheck immediately before publication: another commit may have arrived.
        make_latest = api(base + "/git/ref/heads/" + branch)["object"]["sha"] == args.commit
        gh("release", "edit", tag, "--repo", repo, "--draft=false", "--latest=" + str(make_latest).lower(),
           "--title", "Materiewelle · " + args.commit[:12], "--notes-file", str(notes))
        print("Published complete release:", api(base + "/releases/tags/" + tag)["html_url"])


if __name__ == "__main__":
    try:
        main()
    except Exception as error:
        if os.environ.get("GITHUB_ACTIONS") == "true":
            message = (type(error).__name__ + ": " + str(error))[:5000]
            message = message.replace("%", "%25").replace("\r", "%0D").replace("\n", "%0A")
            print("::error title=Release publication::" + message, file=sys.stderr)
        raise
