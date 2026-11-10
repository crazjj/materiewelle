#!/usr/bin/env python3
"""Execute the actual early head script with controlled storage and forbidden OS access."""
import json
from pathlib import Path
import shutil
import subprocess
import unittest

BOOTSTRAP = Path(__file__).resolve().parents[1] / "browser-bootstrap/build/kotlin-webpack/js/productionExecutable/browser-init.js"


class ThemeBootstrapTest(unittest.TestCase):
    def execute(self, saved, blocked=False, motion=None, reduced=False, include_motion=False):
        fixture = {"saved": saved, "blocked": blocked, "motion": motion, "reduced": reduced}
        program = """
const vm = require('node:vm');
const source = INPUT_SOURCE;
const fixture = INPUT_FIXTURE;
let stored = fixture.saved;
const attributes = {};
const storage = {getItem: key => key === "materiewelle.theme" ? stored : fixture.motion, setItem: (key,value) => { stored=value; }};
const context = {
  document: {documentElement: {setAttribute: (key,value) => {attributes[key]=value;}}},
  window: {matchMedia: query => {if(query !== '(prefers-reduced-motion: reduce)') throw new Error('OS appearance must not be read'); return {matches:fixture.reduced};}}
};
if (fixture.blocked) Object.defineProperty(context.window,'localStorage',{get(){throw new Error('Storage blocked')}});
else context.window.localStorage = storage;
context.self = context;
vm.runInNewContext(source, context);
process.stdout.write(JSON.stringify({theme:attributes['data-theme'],stored,motion:attributes['data-motion']}));
""".replace("INPUT_SOURCE", json.dumps(BOOTSTRAP.read_text())).replace("INPUT_FIXTURE", json.dumps(fixture))
        node = shutil.which("node")
        self.assertIsNotNone(node, "Node is part of the reproducible JS toolchain")
        run = subprocess.run([node, "-e", program], text=True, check=True, capture_output=True)
        result = json.loads(run.stdout)
        if not include_motion: result.pop("motion")
        return result

    def test_compiled_motion_obeys_preference_and_accessibility_before_compose(self):
        for choice, reduced, expected in [(None, False, "running"), ("still", False, "paused"), ("animated", True, "reduced"), ("still", True, "reduced"), ("unknown", False, "running")]:
            with self.subTest(choice=choice, reduced=reduced):
                self.assertEqual(expected, self.execute(None, motion=choice, reduced=reduced, include_motion=True)["motion"])

    def test_explicit_binary_choices_are_retained(self):
        for choice in ["light", "dark"]:
            with self.subTest(choice=choice):
                self.assertEqual({"theme": choice, "stored": choice}, self.execute(choice))

    def test_default_migration_and_storage_failure_all_start_dark_without_os_access(self):
        self.assertEqual({"theme": "dark", "stored": None}, self.execute(None))
        for previous in ["system", "unknown", "", "LIGHT"]:
            with self.subTest(previous=previous):
                self.assertEqual({"theme": "dark", "stored": "dark"}, self.execute(previous))
        self.assertEqual({"theme": "dark", "stored": "light"}, self.execute("light", blocked=True))


if __name__ == "__main__":
    unittest.main()
