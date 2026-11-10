"""Regression checks for commits that a release workflow must not omit."""
import importlib.util
import os
from pathlib import Path
import subprocess
import tempfile
import unittest

SPEC = importlib.util.spec_from_file_location("release_plan", Path(__file__).with_name("release-plan.py"))
PLAN = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(PLAN)


class CommitSelection(unittest.TestCase):
    def setUp(self):
        self.previous = Path.cwd()
        self.directory = tempfile.TemporaryDirectory()
        os.chdir(self.directory.name)
        self.git("init", "--quiet", "--initial-branch=master")
        self.git("config", "user.name", "Release fixture")
        self.git("config", "user.email", "fixture@example.invalid")
        self.commits = []
        for number in range(3):
            Path("fixture.txt").write_text(str(number))
            self.git("add", "fixture.txt")
            self.git("commit", "--quiet", "-m", f"fixture {number}")
            self.commits.append(self.git("rev-parse", "HEAD").strip())

    def tearDown(self):
        os.chdir(self.previous)
        self.directory.cleanup()

    def git(self, *args):
        return subprocess.check_output(["git", *args], text=True)

    def test_multi_commit_push_builds_each_commit(self):
        a, b, c = self.commits
        self.assertEqual(PLAN.select_commits({"before": a, "after": c}, "push", c), [b, c])

    def test_new_branch_uses_all_payload_commits(self):
        _, b, c = self.commits
        event = {"before": "0" * 40, "after": c, "commits": [{"id": b}, {"id": c}]}
        self.assertEqual(PLAN.select_commits(event, "push", c), [b, c])

    def test_empty_initial_branch_payload_accepts_a_real_root(self):
        root = self.commits[0]
        event = {"before": "0" * 40, "after": root, "commits": []}
        self.assertEqual(PLAN.select_commits(event, "push", root), [root])

    def test_empty_initial_branch_payload_does_not_omit_earlier_commits(self):
        head = self.commits[-1]
        for before in ("0" * 40, ""):
            with self.subTest(before=before), self.assertRaisesRegex(RuntimeError, "silently omit"):
                PLAN.select_commits({"before": before, "after": head, "commits": []}, "push", head)

    def test_manual_dispatch_targets_the_exact_commit(self):
        a, _, c = self.commits
        self.assertEqual(PLAN.select_commits({"inputs": {"commit": a}}, "workflow_dispatch", c), [a])

    def test_deleted_branch_creates_no_release(self):
        self.assertEqual(PLAN.select_commits({"deleted": True}, "push", self.commits[-1]), [])

    def test_invalid_dispatch_ref_is_rejected(self):
        with self.assertRaisesRegex(ValueError, "full commit SHA"):
            PLAN.select_commits({"inputs": {"commit": "master"}}, "workflow_dispatch", self.commits[-1])

    def test_force_push_falls_back_to_payload(self):
        _, b, c = self.commits
        event = {"before": "d" * 40, "after": c, "commits": [{"id": b}, {"id": c}]}
        self.assertEqual(PLAN.select_commits(event, "push", c), [b, c])

    def test_missing_before_and_empty_payload_accepts_only_a_real_root(self):
        self.git("checkout", "--quiet", "--orphan", "public-root")
        Path("fixture.txt").write_text("public source snapshot")
        self.git("add", "fixture.txt")
        self.git("commit", "--quiet", "-m", "public root")
        root = self.git("rev-parse", "HEAD").strip()
        self.assertEqual(self.git("rev-list", "--count", root).strip(), "1")
        event = {"before": "d" * 40, "after": root, "commits": []}
        self.assertEqual(PLAN.select_commits(event, "push", root), [root])

    def test_unrelated_existing_payload_commit_is_rejected(self):
        a, _, c = self.commits
        self.git("checkout", "--quiet", "-b", "unrelated", a)
        Path("fixture.txt").write_text("different branch")
        self.git("add", "fixture.txt")
        self.git("commit", "--quiet", "-m", "unrelated source")
        unrelated = self.git("rev-parse", "HEAD").strip()
        event = {"before": "d" * 40, "after": c, "commits": [{"id": unrelated}, {"id": c}]}
        with self.assertRaisesRegex(ValueError, "not reachable"):
            PLAN.select_commits(event, "push", c)

    def test_shallow_boundary_is_not_accepted_as_a_parentless_root(self):
        c = self.commits[-1]
        Path(".git/shallow").write_text(c + "\n")
        self.assertEqual(self.git("rev-list", "--count", c).strip(), "1")
        self.assertFalse(PLAN.is_root_commit(c))
        with self.assertRaisesRegex(RuntimeError, "silently omit"):
            PLAN.select_commits({"before": "d" * 40, "after": c, "commits": []}, "push", c)

    def test_unresolvable_history_is_not_silently_reduced_to_head(self):
        with self.assertRaisesRegex(RuntimeError, "silently omit"):
            PLAN.select_commits({"before": "d" * 40, "after": self.commits[-1]}, "push", self.commits[-1])

    def test_matrix_limit_fails_instead_of_dropping_commits(self):
        ids = [f"{n:040x}" for n in range(1, 258)]
        with self.assertRaisesRegex(ValueError, "256"):
            PLAN.select_commits({"before": "0" * 40, "after": ids[-1], "commits": [{"id": n} for n in ids]}, "push", ids[-1])


if __name__ == "__main__":
    unittest.main(verbosity=2)
