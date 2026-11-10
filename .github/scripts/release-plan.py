#!/usr/bin/env python3
"""Select every pushed commit, including multi-commit pushes; no Git mutation."""
import json
import os
from pathlib import Path
import re
import subprocess


def require_commit_sha(commit):
    if not isinstance(commit, str) or not re.fullmatch(r"[0-9a-f]{40}", commit):
        raise ValueError("Expected a full commit SHA")


def is_root_commit(commit):
    """Inspect real commit headers; shallow boundaries must not imply a root."""
    kind = subprocess.check_output(["git", "cat-file", "-t", commit], text=True).strip()
    if kind != "commit":
        raise ValueError("Expected a commit object")
    raw = subprocess.check_output(["git", "cat-file", "-p", commit], text=True)
    headers = raw.split("\n\n", 1)[0].splitlines()
    return not any(line.startswith("parent ") for line in headers)


def select_commits(event, event_name, default_commit):
    payload_selection = False
    if event_name == "push":
        if event.get("deleted"):
            return []
        before, after = event.get("before", ""), event["after"]
        require_commit_sha(after)
        if before and before != "0" * 40:
            require_commit_sha(before)
            try:
                commits = subprocess.check_output(
                    ["git", "rev-list", "--reverse", "--topo-order", f"{before}..{after}"],
                    text=True, stderr=subprocess.PIPE,
                ).splitlines()
            except subprocess.CalledProcessError:
                # A force-push can remove the old object from the fetched history.
                commits = [item["id"] for item in event.get("commits", [])]
                payload_selection = True
                if not commits:
                    # The public-history rewrite has exactly one parentless
                    # commit. Its old private tip must never be fetched back.
                    if is_root_commit(after):
                        commits = [after]
                    else:
                        raise RuntimeError("Cannot resolve push history; refusing to silently omit commits")
        else:
            # Initial branch pushes use the payload, without rebuilding all history.
            commits = [item["id"] for item in event.get("commits", [])]
            payload_selection = True
            if not commits and not is_root_commit(after):
                raise RuntimeError("Cannot resolve initial branch history; refusing to silently omit commits")
        if after not in commits:
            commits.append(after)
    elif event_name == "workflow_dispatch":
        commits = [event.get("inputs", {}).get("commit") or default_commit]
    else:
        raise ValueError("Unsupported release event: " + event_name)
    commits = list(dict.fromkeys(commits))
    if len(commits) > 256:
        raise ValueError("GitHub permits at most 256 matrix jobs; split this push and rerun explicitly")
    for commit in commits:
        require_commit_sha(commit)
        subprocess.run(["git", "cat-file", "-e", commit + "^{commit}"], check=True)
        if payload_selection:
            ancestry = subprocess.run(["git", "merge-base", "--is-ancestor", commit, after])
            if ancestry.returncode == 1:
                raise ValueError("Push payload commit is not reachable from the pushed head: " + commit)
            if ancestry.returncode:
                raise RuntimeError("Cannot verify push payload ancestry")
    return commits


if __name__ == "__main__":
    event = json.loads(Path(os.environ["GITHUB_EVENT_PATH"]).read_text())
    commits = select_commits(event, os.environ["GITHUB_EVENT_NAME"], os.environ["GITHUB_SHA"])
    # Every commit in the public history has this contract. A missing file is a
    # failed release, never permission to silently skip a pushed commit.
    for commit in commits:
        subprocess.run(["git", "cat-file", "-e", commit + ":apps/web/scripts/product_assets.py"], check=True)
    with open(os.environ["GITHUB_OUTPUT"], "a") as output:
        output.write("matrix=" + json.dumps({"commit": commits}) + "\n")
        output.write("count=" + str(len(commits)) + "\n")
    print("Release commits:", ", ".join(commits))
