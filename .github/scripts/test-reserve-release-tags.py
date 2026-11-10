"""Exercise early tag reservation without contacting or mutating GitHub."""
import importlib.util
import json
from pathlib import Path
import subprocess
import sys
import unittest
from unittest.mock import patch

SPEC = importlib.util.spec_from_file_location("tag_reservation", Path(__file__).with_name("reserve-release-tags.py"))
RESERVATION = importlib.util.module_from_spec(SPEC)
sys.modules[SPEC.name] = RESERVATION
SPEC.loader.exec_module(RESERVATION)
SHA = "a" * 40
COMMIT = RESERVATION.CommitSha(SHA)
BASE = "repos/example/private-project"


def response(sha=SHA, *, kind="commit", code=0, error=""):
    return subprocess.CompletedProcess([], code, json.dumps({"object": {"type": kind, "sha": sha}}), error)


class TagReservation(unittest.TestCase):
    def test_missing_tag_is_created_with_an_exact_structured_json_payload(self):
        calls = []

        def api(*args):
            calls.append(args)
            if len(calls) == 1:
                return response(code=1, error="Not Found (HTTP 404)")
            self.assertEqual(args[:4], ("--method", "POST", BASE + "/git/refs", "--input"))
            self.assertEqual(json.loads(Path(args[4]).read_text()), {"ref": "refs/tags/build-" + SHA, "sha": SHA})
            return response()

        with patch.object(RESERVATION, "api", side_effect=api):
            result = RESERVATION.reserve_tag(BASE, COMMIT)
        self.assertEqual(result.status, RESERVATION.ReservationStatus.CREATED)
        self.assertEqual(len(calls), 2)

    def test_correct_existing_tag_is_verified_without_any_write(self):
        with patch.object(RESERVATION, "api", return_value=response()) as api:
            result = RESERVATION.reserve_tag(BASE, COMMIT)
        self.assertEqual(result.status, RESERVATION.ReservationStatus.EXISTING)
        api.assert_called_once_with(BASE + "/git/ref/tags/build-" + SHA)

    def test_existing_tag_target_mismatch_is_rejected_without_moving_it(self):
        with patch.object(RESERVATION, "api", return_value=response("b" * 40)) as api:
            with self.assertRaisesRegex(ValueError, "exact application commit"):
                RESERVATION.reserve_tag(BASE, COMMIT)
        self.assertEqual(api.call_count, 1)

    def test_correct_annotated_tag_is_resolved_and_left_unchanged(self):
        with patch.object(RESERVATION, "api", side_effect=[response("c" * 40, kind="tag"), response()]) as api:
            result = RESERVATION.reserve_tag(BASE, COMMIT)
        self.assertEqual(result.status, RESERVATION.ReservationStatus.EXISTING)
        self.assertEqual(api.call_args.args, (BASE + "/git/tags/" + "c" * 40,))

    def test_annotated_tag_with_the_wrong_commit_is_not_accepted(self):
        with patch.object(RESERVATION, "api", side_effect=[response("c" * 40, kind="tag"), response("b" * 40)]):
            with self.assertRaisesRegex(ValueError, "exact application commit"):
                RESERVATION.reserve_tag(BASE, COMMIT)

    def test_read_permission_failure_is_not_treated_as_a_missing_tag(self):
        with patch.object(RESERVATION, "api", return_value=response(code=1, error="Resource not accessible (HTTP 403)")) as api:
            with self.assertRaisesRegex(RuntimeError, "HTTP 403"):
                RESERVATION.reserve_tag(BASE, COMMIT)
        self.assertEqual(api.call_count, 1)

    def test_create_permission_failure_stops_without_token_escalation_or_retry(self):
        with patch.object(RESERVATION, "api", side_effect=[
            response(code=1, error="Not Found (HTTP 404)"),
            response(code=1, error="Resource not accessible by integration (HTTP 403)"),
        ]) as api:
            with self.assertRaisesRegex(RuntimeError, "does not escalate its token"):
                RESERVATION.reserve_tag(BASE, COMMIT)
        self.assertEqual(api.call_count, 2)

    def test_concurrent_creation_is_accepted_only_after_reading_the_correct_target(self):
        with patch.object(RESERVATION, "api", side_effect=[
            response(code=1, error="Not Found (HTTP 404)"),
            response(code=1, error="Reference already exists (HTTP 422)"), response(),
        ]) as api:
            result = RESERVATION.reserve_tag(BASE, COMMIT)
        self.assertEqual(result.status, RESERVATION.ReservationStatus.EXISTING)
        self.assertEqual(api.call_count, 3)

    def test_concurrently_created_wrong_target_is_rejected(self):
        with patch.object(RESERVATION, "api", side_effect=[
            response(code=1, error="Not Found (HTTP 404)"),
            response(code=1, error="Reference already exists (HTTP 422)"), response("b" * 40),
        ]):
            with self.assertRaisesRegex(ValueError, "exact application commit"):
                RESERVATION.reserve_tag(BASE, COMMIT)

    def test_empty_matrix_never_contacts_github(self):
        with patch.object(RESERVATION, "api") as api:
            self.assertEqual(RESERVATION.reserve_matrix("example/private-project", RESERVATION.ReleaseMatrix.parse('{"commit":[]}')), [])
        api.assert_not_called()

    def test_matrix_rejects_wrong_shape_bad_shas_duplicates_and_overflow(self):
        for value in [[], {"commit": SHA}, {"commit": ["master"]}, {"commit": [True]},
                      {"commit": [SHA, SHA]}, {"commit": [SHA], "include": []}, {"commit": [SHA] * 257}]:
            with self.subTest(value=str(value)[:80]), self.assertRaises(ValueError):
                RESERVATION.ReleaseMatrix.parse(json.dumps(value))

    def test_invalid_repository_is_rejected_before_any_network_request(self):
        with patch.object(RESERVATION, "api") as api:
            with self.assertRaisesRegex(ValueError, "owner/repository"):
                RESERVATION.reserve_matrix("../private-project", RESERVATION.ReleaseMatrix((COMMIT,)))
        api.assert_not_called()

    def test_signed_error_query_is_redacted(self):
        result = response(code=1, error="HTTP 403 https://example.invalid/path?signature=private-value")
        self.assertIn("HTTP 403", RESERVATION.response_detail(result))
        self.assertNotIn("private-value", RESERVATION.response_detail(result))


if __name__ == "__main__":
    unittest.main(verbosity=2)
