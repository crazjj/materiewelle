"""Keep local validation ordered and automatic GitHub workflows disabled."""
from pathlib import Path
import shlex
import unittest

ROOT = Path(__file__).resolve().parents[2]
WORKFLOWS = ROOT / '.github/workflows'


class PublicDeliveryWorkflowTest(unittest.TestCase):
    def test_no_automatic_github_workflows_remain(self):
        self.assertEqual(list(WORKFLOWS.glob('*.yml')) + list(WORKFLOWS.glob('*.yaml')), [])
        self.assertTrue((ROOT / 'archive/github-actions/ci.yml').is_file())
        self.assertTrue((ROOT / 'archive/github-actions/commit-releases.yml').is_file())

    def test_compiled_asset_checks_follow_local_production_build(self):
        source = (ROOT / '.github/scripts/validate-product.sh').read_text()
        commands = [shlex.split(line) for line in source.splitlines()
                    if line.strip() and not line.lstrip().startswith('#')]
        build = [index for index, command in enumerate(commands)
                 if command[-1] == '.github/scripts/build-release-web.py']
        self.assertEqual(len(build), 1)
        for test in ('test-theme-bootstrap.py', 'test-browser-assets.py'):
            checks = [index for index, command in enumerate(commands)
                      if command == ['python', 'apps/web/scripts/' + test]]
            with self.subTest(test=test):
                self.assertEqual(len(checks), 1, 'Keep each generated-asset regression enabled')
                self.assertLess(build[0], checks[0], 'Fresh checkouts must build the assets first')



if __name__ == '__main__':
    unittest.main(verbosity=2)
