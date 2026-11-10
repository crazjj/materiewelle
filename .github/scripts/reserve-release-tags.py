#!/usr/bin/env python3
"""Reserve immutable per-commit tags before long builds with the existing token.

No release is published here. Existing tags are checked, never moved or deleted.
"""
from __future__ import annotations

import argparse
from dataclasses import dataclass
from enum import Enum
import json
import os
from pathlib import Path
import re
import subprocess
import tempfile
from typing import Any


@dataclass(frozen=True)
class CommitSha:
    value: str

    def __post_init__(self) -> None:
        if not isinstance(self.value, str) or not re.fullmatch(r"[0-9a-f]{40}", self.value):
            raise ValueError("Expected a full lowercase 40-character commit SHA")

    @property
    def tag(self) -> str:
        return "build-" + self.value


@dataclass(frozen=True)
class ReleaseMatrix:
    commits: tuple[CommitSha, ...]

    @classmethod
    def parse(cls, raw: str) -> ReleaseMatrix:
        value = json.loads(raw)
        if not isinstance(value, dict) or set(value) != {"commit"} or not isinstance(value["commit"], list):
            raise ValueError("Release matrix must have exactly one commit array")
        if len(value["commit"]) > 256:
            raise ValueError("Release matrix exceeds GitHub's 256-commit limit")
        commits = tuple(CommitSha(item) for item in value["commit"])
        if len(set(commits)) != len(commits):
            raise ValueError("Release matrix must not contain duplicate commits")
        return cls(commits)


class ReservationStatus(Enum):
    CREATED = "created"
    EXISTING = "existing"


@dataclass(frozen=True)
class TagReservation:
    commit: CommitSha
    status: ReservationStatus


def response_detail(result: subprocess.CompletedProcess[str]) -> str:
    # Keep actionable API errors without printing signed query values.
    return re.sub(r"(https?://[^\s?]+)\?[^\s]+", r"\1?[redacted]", result.stderr.strip())


def http_status(result: subprocess.CompletedProcess[str], code: int) -> bool:
    return re.search(r"\bHTTP\s+" + str(code) + r"\b", result.stderr) is not None


def api(*arguments: str) -> subprocess.CompletedProcess[str]:
    return subprocess.run(["gh", "api", *arguments], text=True, capture_output=True)


def require_api(result: subprocess.CompletedProcess[str], action: str) -> dict[str, Any]:
    if result.returncode:
        raise RuntimeError(action + ": " + response_detail(result))
    value = json.loads(result.stdout)
    if not isinstance(value, dict):
        raise ValueError("GitHub returned an invalid object for " + action)
    return value


def read_tag(base: str, commit: CommitSha) -> dict[str, Any] | None:
    result = api(base + "/git/ref/tags/" + commit.tag)
    if result.returncode and http_status(result, 404):
        return None
    return require_api(result, "Could not read " + commit.tag)


def verify_target(base: str, commit: CommitSha, reference: dict[str, Any]) -> None:
    target = reference.get("object")
    seen: set[CommitSha] = set()
    for _ in range(8):
        if not isinstance(target, dict):
            raise ValueError("GitHub tag has no valid target object")
        sha = CommitSha(target.get("sha"))
        if target.get("type") == "commit":
            if sha != commit:
                raise ValueError("Existing release tag must point at the exact application commit; refusing to move " + commit.tag)
            return
        if target.get("type") != "tag" or sha in seen:
            raise ValueError("Unsupported or cyclic release tag target")
        seen.add(sha)
        target = require_api(api(base + "/git/tags/" + sha.value), "Could not resolve annotated tag").get("object")
    raise ValueError("Release tag nesting limit exceeded")


def reserve_tag(base: str, commit: CommitSha) -> TagReservation:
    existing = read_tag(base, commit)
    if existing is not None:
        verify_target(base, commit, existing)
        return TagReservation(commit, ReservationStatus.EXISTING)

    with tempfile.TemporaryDirectory(prefix="materiewelle-tag-reservation-") as temporary:
        payload = Path(temporary) / "ref.json"
        payload.write_text(json.dumps({"ref": "refs/tags/" + commit.tag, "sha": commit.value}) + "\n")
        created = api("--method", "POST", base + "/git/refs", "--input", str(payload))
    if created.returncode:
        # Another plan can win after our GET. Accept only an actually existing,
        # correctly targeted tag; never treat a permission failure as success.
        if http_status(created, 422):
            concurrent = read_tag(base, commit)
            if concurrent is not None:
                verify_target(base, commit, concurrent)
                return TagReservation(commit, ReservationStatus.EXISTING)
        raise RuntimeError(
            "Could not reserve " + commit.tag + ": " + response_detail(created) +
            ". Historical commits with different workflow files may require pre-tagging "
            "through already authorized Git access; this workflow does not escalate its token."
        )
    verify_target(base, commit, require_api(created, "Could not create release tag"))
    return TagReservation(commit, ReservationStatus.CREATED)


def reserve_matrix(repository: str, matrix: ReleaseMatrix) -> list[TagReservation]:
    if not re.fullmatch(r"[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+", repository) or any(
        part in {".", ".."} for part in repository.split("/")
    ):
        raise ValueError("Expected a GitHub owner/repository name")
    return [reserve_tag("repos/" + repository, commit) for commit in matrix.commits]


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--repo", default=os.environ.get("GITHUB_REPOSITORY"))
    parser.add_argument("--matrix-file", type=Path)
    args = parser.parse_args()
    raw = args.matrix_file.read_text() if args.matrix_file else os.environ["RELEASE_MATRIX_JSON"]
    if not args.repo:
        raise ValueError("GITHUB_REPOSITORY or --repo is required")
    reservations = reserve_matrix(args.repo, ReleaseMatrix.parse(raw))
    print(json.dumps([{"commit": item.commit.value, "tag": item.commit.tag, "status": item.status.value}
                      for item in reservations]))


if __name__ == "__main__":
    main()
