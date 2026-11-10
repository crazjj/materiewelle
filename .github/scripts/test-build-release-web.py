"""Exercise root/subpath orchestration and retained failure evidence."""
import importlib.util
import json
from pathlib import Path
import subprocess
import tempfile
import unittest
from unittest.mock import patch

SPEC = importlib.util.spec_from_file_location("build_release_web", Path(__file__).with_name("build-release-web.py"))
BUILD = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(BUILD)


class ReleaseBuildTest(unittest.TestCase):
    def setUp(self):
        temporary = tempfile.TemporaryDirectory()
        self.addCleanup(temporary.cleanup)
        self.root = Path(temporary.name)
        self.app = self.root / "apps/web"
        self.app.mkdir(parents=True)
        self.helper = self.app / "build-static.sh"
        self.helper.write_text("#!/bin/sh\nexit 0\n")
        self.helper.chmod(0o755)

    def test_builds_both_bases_and_retains_completed_receipt(self):
        with patch.object(BUILD.subprocess, "run") as run:
            report = BUILD.build_release(self.root)
        self.assertEqual([call.args[0] for call in run.call_args_list], [
            [str(self.helper)], [str(self.helper), "--base-path", "/materiewelle/"]])
        self.assertTrue(all(call.kwargs == {"cwd": self.app, "check": True} for call in run.call_args_list))
        self.assertEqual(report["status"], "passed")
        self.assertEqual([item["status"] for item in report["builds"]], ["passed", "passed"])
        self.assertEqual(json.loads((self.root / "release-evidence/build.json").read_text()), report)
        with self.assertRaises(FileExistsError):
            BUILD.build_release(self.root)

    def test_real_child_failure_stops_before_subpath_and_retains_failure(self):
        self.helper.write_text("#!/bin/sh\nexit 7\n")
        with self.assertRaises(subprocess.CalledProcessError):
            BUILD.build_release(self.root)
        report = json.loads((self.root / "release-evidence/build.json").read_text())
        self.assertEqual(report["status"], "failed")
        self.assertEqual(len(report["builds"]), 1)
        self.assertEqual(report["builds"][0]["status"], "failed")


if __name__ == "__main__":
    unittest.main(verbosity=2)
