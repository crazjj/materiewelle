"""Guard release provenance and actionable GitHub publication failures."""
import importlib.util
import json
from pathlib import Path
import subprocess
import unittest
from unittest.mock import patch

SPEC = importlib.util.spec_from_file_location("publication", Path(__file__).with_name("publish-release.py"))
PUBLICATION = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(PUBLICATION)
COMMIT = "a" * 40
BASE = "repos/example/private-project"
TAG = "build-" + COMMIT


def response(target=None, code=0, error=""):
    return subprocess.CompletedProcess([], code, json.dumps({"object": target}) if target else "", error)


class Publication(unittest.TestCase):
    def test_lightweight_tag_identifies_the_application_commit(self):
        with patch.object(PUBLICATION.subprocess, "run", return_value=response({"type": "commit", "sha": COMMIT})):
            PUBLICATION.verify_tag_commit(BASE, TAG, COMMIT)

    def test_mismatched_tag_cannot_publish_unrelated_source(self):
        with patch.object(PUBLICATION.subprocess, "run", return_value=response({"type": "commit", "sha": "b" * 40})):
            with self.assertRaisesRegex(AssertionError, "exact application commit"):
                PUBLICATION.verify_tag_commit(BASE, TAG, COMMIT)

    def test_annotated_tag_is_checked_against_its_commit(self):
        with patch.object(PUBLICATION.subprocess, "run", return_value=response({"type": "tag", "sha": "c" * 40})), \
             patch.object(PUBLICATION, "api", return_value={"object": {"type": "commit", "sha": COMMIT}}) as api:
            PUBLICATION.verify_tag_commit(BASE, TAG, COMMIT)
            api.assert_called_once_with(BASE + "/git/tags/" + "c" * 40)

    def test_missing_tag_is_not_created_implicitly_by_release_publication(self):
        with patch.object(PUBLICATION.subprocess, "run", return_value=response(code=1, error="Not Found (HTTP 404)")):
            with self.assertRaisesRegex(RuntimeError, "reserved before publication"):
                PUBLICATION.verify_tag_commit(BASE, TAG, COMMIT)

    def test_draft_uses_verified_existing_tag_without_historical_target_payload(self):
        notes = Path("notes.md")
        with patch.object(PUBLICATION, "verify_tag_commit") as verify, patch.object(PUBLICATION, "gh") as gh:
            PUBLICATION.create_draft("example/private-project", TAG, COMMIT, notes)
            verify.assert_called_once_with(BASE, TAG, COMMIT)
            self.assertIn("--verify-tag", gh.call_args.args)
            self.assertNotIn("--target", gh.call_args.args)
            self.assertIn("--draft", gh.call_args.args)

    def test_invalid_tag_stops_before_creating_draft(self):
        with patch.object(PUBLICATION, "verify_tag_commit", side_effect=AssertionError("wrong target")), patch.object(PUBLICATION, "gh") as gh:
            with self.assertRaisesRegex(AssertionError, "wrong target"):
                PUBLICATION.create_draft("example/private-project", TAG, COMMIT, Path("notes.md"))
            gh.assert_not_called()

    def test_permission_failure_is_not_treated_as_a_missing_tag(self):
        with patch.object(PUBLICATION.subprocess, "run", return_value=response(code=1, error="Resource not accessible (HTTP 403)")):
            with self.assertRaisesRegex(RuntimeError, "HTTP 403"):
                PUBLICATION.verify_tag_commit(BASE, TAG, COMMIT)

    def test_github_error_keeps_response_and_removes_signed_query(self):
        failure = response(code=1, error="HTTP 403 https://example.invalid/asset?signature=private-value")
        with patch.object(PUBLICATION.subprocess, "run", return_value=failure):
            with self.assertRaises(RuntimeError) as context:
                PUBLICATION.gh("release", "create", TAG)
            self.assertIn("HTTP 403", str(context.exception))
            self.assertNotIn("private-value", str(context.exception))

    def test_all_release_pages_are_included_without_new_cli_flags(self):
        with patch.object(PUBLICATION, "gh", return_value='{"id":1}\n{"id":2}\n') as gh:
            self.assertEqual(PUBLICATION.all_releases(BASE), [{"id": 1}, {"id": 2}])
            self.assertIn("--paginate", gh.call_args.args)
            self.assertNotIn("--slurp", gh.call_args.args)


if __name__ == "__main__":
    unittest.main(verbosity=2)
