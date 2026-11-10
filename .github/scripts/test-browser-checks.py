"""Exercise real child processes and failure receipts; no browser/build/network required."""
import json
import os
from pathlib import Path
import runpy
import subprocess
import sys
import tempfile
import unittest
from unittest.mock import patch

import browser_checks as checks

SCENARIOS = {
    'main': ('articles', 'dg', 'measurement', 'state', 'tutor', 'handout', 'viewports', 'data_failure'),
    'refinement': ('presentation', 'reading', 'drafts', 'math_failure', 'performance'),
    'dg-learning': ('lesson', 'complete_reading', 'views', 'surface_annotations', 'layouts'),
    'ownership': ('two_tabs', 'simultaneous', 'navigation_lifecycle', 'lock_fallback', 'formula_rescue', 'storage_failure'),
    'jonsson': ('compact_model', 'first_selection'),
    'personal-report': ('personal_report', 'handout_request_provenance'),
}

HELPER = '''import argparse, hashlib, json, pathlib, sys

def run(file, scenarios):
    p=argparse.ArgumentParser()
    p.add_argument('--dist', type=pathlib.Path, required=True)
    p.add_argument('--output', type=pathlib.Path, required=True)
    p.add_argument('--chromium', required=True)
    if pathlib.Path(file).name != 'test-session-ownership.py':
        p.add_argument('--base-path', default='/')
    a=p.parse_args()
    base=getattr(a,'base_path','/')
    manifest_path=a.dist / base.strip('/') / 'manifest.json'
    manifest=json.loads(manifest_path.read_text())
    sha=lambda path: hashlib.sha256(pathlib.Path(path).read_bytes()).hexdigest()
    a.output.mkdir(parents=True)
    if pathlib.Path(file).with_suffix('.no-report').exists():
        return
    failed=pathlib.Path(file).with_suffix('.fail').exists()
    report={'status':'failed' if failed else 'passed', 'contract':manifest['product_contract'],
        'test_harness_sha256':sha(file), 'base_harness_sha256':sha(pathlib.Path(file).with_name('test-run-two.py')),
        'release':{'manifest_sha256':sha(manifest_path), 'javascript_sha256':manifest['files']['materiewellen.js']['sha256'], 'deployment_base':base},
        'checks':[{'name':'synthetic orchestration fixture','passed':not failed}],
        'scenarios':[{'name':name,'passed':not failed} for name in scenarios]}
    (a.output/'report.json').write_text(json.dumps(report))
    if failed:
        (a.output/'failure.txt').write_text('Synthetic child failure, retained for diagnosis')
        sys.exit(7)
'''


class BrowserMatrix(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        self.scripts = self.root / checks.SCRIPTS
        self.scripts.mkdir(parents=True)
        (self.scripts / '_fixture.py').write_text(HELPER)
        for suite in checks.SUITES:
            (self.scripts / suite.filename).write_text(
                'from _fixture import run\nSCENARIOS = ' + repr(SCENARIOS[suite.name]) + '\nrun(__file__, SCENARIOS)\n')
        self.dist = self.root / 'dist'
        self.dist.mkdir()
        self.output = self.root / 'evidence/root'
        self.manifest = {'product_contract':'fixture', 'deployment_base':'/',
                         'files':{'materiewellen.js':{'sha256':'a'*64}}}
        self.manifest_path = self.dist / 'manifest.json'
        self.manifest_path.write_text(json.dumps(self.manifest))
        self.inventory = patch.object(checks, 'inventory', side_effect=lambda directory, base: json.loads((directory/'manifest.json').read_text()))
        self.inventory.start()
        self.addCleanup(self.inventory.stop)
        self.proof = patch.object(checks, 'proof')
        self.proof_mock = self.proof.start()
        self.addCleanup(self.proof.stop)

    def run_group(self, **kwargs):
        return checks.run_checks(project_root=self.root, dist=self.dist, output=self.output,
                                 chromium='/fixture/chromium', **kwargs)

    def verify(self, **kwargs):
        return checks.verify_evidence(project_root=self.root, output=self.output,
                                      manifest_path=self.manifest_path, **kwargs)

    def test_unused_script_does_not_invalidate_current_evidence(self):
        before = checks.harness_inputs(self.root)
        (self.scripts / 'unused-helper.py').write_text('# unrelated and not imported\n')
        self.assertEqual(checks.harness_inputs(self.root), before)
        self.assertIn((checks.SCRIPTS / '_fixture.py').as_posix(), before)

    def test_literal_dynamic_import_is_included_transitively(self):
        helper = self.scripts / 'dynamic_helper.py'
        helper.write_text('from nested_helper import value\n')
        (self.scripts / 'nested_helper.py').write_text('value = 42\n')
        main = self.scripts / 'test-run-two.py'
        main.write_text(main.read_text() + "\nimportlib.util.spec_from_file_location('extra', HERE / 'dynamic_helper.py')\n")
        inputs = checks.harness_inputs(self.root)
        self.assertIn((checks.SCRIPTS / 'dynamic_helper.py').as_posix(), inputs)
        self.assertIn((checks.SCRIPTS / 'nested_helper.py').as_posix(), inputs)
        helper.unlink()
        with self.assertRaisesRegex(FileNotFoundError, 'Missing harness dependency'):
            checks.harness_inputs(self.root)

    def test_unknown_dynamic_harness_import_is_rejected(self):
        main = self.scripts / 'test-run-two.py'
        main.write_text(main.read_text() + "\nimportlib.util.spec_from_file_location('extra', selected_at_runtime)\n")
        with self.assertRaisesRegex(ValueError, 'Unresolved dynamic harness import'):
            checks.harness_inputs(self.root)

    def test_all_root_runs_six_complete_suites_and_binds_original_reports(self):
        receipt = self.run_group()
        self.assertEqual([item['suite'] for item in receipt['suites']], list(SCENARIOS))
        for suite, entry in zip(checks.SUITES, receipt['suites']):
            path = checks.suite_output(self.output, suite) / 'report.json'
            self.assertEqual(entry['report_sha256'], checks.digest(path))
            self.assertNotIn('--only', entry['command'])
            self.assertEqual('--base-path' in entry['command'], suite.name != 'ownership')
        self.assertEqual(receipt['manifest_sha256'], checks.digest(self.manifest_path))
        self.assertEqual(self.verify(), receipt)
        self.assertEqual(self.proof_mock.call_count, 3)

    def test_subpath_runs_only_route_contract_and_focused_runs_only_three_root_suites(self):
        sub = self.dist/'materiewelle'
        sub.mkdir()
        manifest = dict(self.manifest, deployment_base='/materiewelle/')
        (sub/'manifest.json').write_text(json.dumps(manifest))
        receipt = self.run_group(group='route', base='/materiewelle/')
        self.assertEqual([item['suite'] for item in receipt['suites']], ['main','refinement','dg-learning'])
        self.assertTrue(all(item['command'][-1] == '/materiewelle/' for item in receipt['suites']))
        self.output = self.output.with_name('focus')
        receipt = self.run_group(group='focused')
        self.assertEqual([item['suite'] for item in receipt['suites']], ['ownership','jonsson','personal-report'])

    def test_root_only_contract_cannot_masquerade_as_subpath(self):
        for group in ('all','focused'):
            with self.subTest(group=group), self.assertRaisesRegex(ValueError, 'root export'):
                self.run_group(group=group, base='/materiewelle/')

    def test_missing_required_script_fails_before_any_suite(self):
        (self.scripts/'test-personal-report.py').unlink()
        with self.assertRaisesRegex(FileNotFoundError, 'mandatory'):
            self.run_group()
        self.assertFalse(self.output.parent.exists())

    def test_real_failed_child_stops_release_and_preserves_failure_evidence(self):
        (self.scripts/'test-ui-refinement.fail').touch()
        with self.assertRaises(subprocess.CalledProcessError):
            self.run_group()
        receipt=json.loads(checks.summary_path(self.output).read_text())
        self.assertEqual(receipt['status'], 'failed')
        self.assertEqual([item['status'] for item in receipt['suites']], ['passed','failed'])
        self.assertTrue((self.output.with_name('root-refinement')/'failure.txt').exists())
        self.assertFalse(self.output.with_name('root-ownership').exists())
        with self.assertRaisesRegex(AssertionError, 'failed browser group'):
            self.verify()

    def test_success_exit_without_report_is_not_evidence(self):
        (self.scripts/'test-personal-report.no-report').touch()
        with self.assertRaises(FileNotFoundError):
            self.run_group()
        self.assertEqual(json.loads(checks.summary_path(self.output).read_text())['status'], 'failed')

    def test_existing_evidence_is_never_overwritten(self):
        self.run_group()
        original={p:p.read_bytes() for p in self.output.parent.rglob('*') if p.is_file()}
        with self.assertRaises(FileExistsError):
            self.run_group()
        self.assertEqual(original, {p:p.read_bytes() for p in original})

    def test_existing_suite_without_group_receipt_is_also_preserved(self):
        self.output.mkdir(parents=True)
        (self.output/'report.json').write_text('keep prior evidence')
        with self.assertRaises(FileExistsError):
            self.run_group()
        self.assertEqual((self.output/'report.json').read_text(), 'keep prior evidence')

    def test_manifest_or_helpers_changed_after_run_reject_reuse(self):
        self.run_group()
        original=self.manifest_path.read_bytes()
        self.manifest_path.write_bytes(original+b'\n')
        with self.assertRaisesRegex(AssertionError, 'another manifest'):
            self.verify()
        self.manifest_path.write_bytes(original)
        (self.scripts/'_fixture.py').write_text(HELPER+'\n# changed helper\n')
        with self.assertRaisesRegex(AssertionError, 'helpers changed'):
            self.verify()

    def test_deleted_or_modified_report_and_missing_suite_receipt_are_rejected(self):
        self.run_group()
        summary=checks.summary_path(self.output)
        original=summary.read_bytes()
        receipt=json.loads(original)
        receipt['suites'].pop()
        summary.write_text(json.dumps(receipt))
        with self.assertRaisesRegex(AssertionError, 'missing or duplicated'):
            self.verify()
        summary.write_bytes(original)
        report=self.output.with_name('root-personal-report')/'report.json'
        report.write_text(report.read_text()+'\n')
        with self.assertRaisesRegex(AssertionError, 'changed or failed'):
            self.verify()
        report.unlink()
        with self.assertRaises(FileNotFoundError):
            self.verify()

    def test_missing_scenario_cannot_be_hidden_by_updated_report_digest(self):
        self.run_group()
        path=self.output.with_name('root-jonsson')/'report.json'
        report=json.loads(path.read_text());report['scenarios'].pop()
        path.write_text(json.dumps(report))
        summary=checks.summary_path(self.output)
        receipt=json.loads(summary.read_text())
        next(item for item in receipt['suites'] if item['suite']=='jonsson')['report_sha256']=checks.digest(path)
        summary.write_text(json.dumps(receipt))
        with self.assertRaisesRegex(AssertionError, 'Missing or duplicate scenarios'):
            self.verify()

    def test_attribute_scenario_contract_is_read_without_execution(self):
        old=self.scripts/'old.py'
        old.write_text("raise RuntimeError('never execute')\nif __name__ == '__main__':\n    two.SCENARIOS = ('reading', 'drafts')\n    two.main()\n")
        self.assertEqual(checks.expected_scenarios(old),('reading','drafts'))

    def test_empty_report_scenario_list_is_not_the_selected_scenario_contract(self):
        fixture=self.scripts/'report-initializer.py'
        fixture.write_text("SCENARIOS = ('two_tabs', 'simultaneous')\nreport=dict(scenarios=[])\n")
        self.assertEqual(checks.expected_scenarios(fixture),('two_tabs','simultaneous'))
        root=Path(__file__).resolve().parents[2]
        for suite in checks.SUITES:
            self.assertEqual(checks.expected_scenarios(root/checks.SCRIPTS/suite.filename),SCENARIOS[suite.name])

    def test_report_requires_current_harness_identity(self):
        self.run_group()
        suite=checks.SUITES[1]
        report_path=checks.suite_output(self.output,suite)/'report.json'
        report=json.loads(report_path.read_text())
        report['test_harness_sha256']=checks.digest(self.scripts/'test-run-two.py')
        del report['base_harness_sha256']
        report_path.write_text(json.dumps(report))
        with self.assertRaisesRegex(AssertionError,'Stale suite harness'):
            checks.verify_report(self.root,suite,report_path,self.manifest,self.manifest_path)
    def test_release_entrypoint_executes_full_root_then_only_subpath_routes(self):
        script=Path(__file__).with_name('check-release-web.py')
        with patch.dict(os.environ,{'RELEASE_PROJECT_ROOT':str(self.root)}),patch.object(checks,'run_checks') as runner:
            runpy.run_path(str(script),run_name='__main__')
        calls=[call.kwargs for call in runner.call_args_list]
        self.assertEqual([(c['group'],c['base']) for c in calls],
                         [('all','/'),('route','/materiewelle/')])
        self.assertEqual([c['output'] for c in calls],
                         [self.root/'release-evidence/root',self.root/'release-evidence/subpath'])


if __name__ == '__main__':
    unittest.main(verbosity=2)
