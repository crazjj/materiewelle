"""Current artifact boundary: stale internal inputs cannot become deliverables.
"""
from pathlib import Path
import hashlib
import importlib.util
import json
import sys
import tempfile
import unittest
import zipfile

sys.path.insert(0, str(Path(__file__).resolve().parent))
from product_delivery import CONTRACT, LEGAL_INPUTS, allowed_asset, inventory, archive, proof, SCENARIOS, digest, ROOT

class ProductBoundary(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        self.dist = self.root / 'dist'
        self.dist.mkdir()
        self.files = {'index.html': b'<html>product</html>', 'materiewellen.js': b'compiled fixture',
                      'app.css': b':root{}', 'versuchsdaten.json': b'{}', 'tutor/manifest.json': b'{}',
                      'fonts/lmroman10-regular.woff2': b'font fixture'}
        from product_assets import TUTOR_FILES
        self.files.update({'tutor/'+name:b'curated fixture' for name in TUTOR_FILES})
        self.files.update({name: (ROOT / source).read_bytes() for name, source in LEGAL_INPUTS.items()})
        pdf = b"%PDF-1.7 handout fixture"
        self.files["handout/materiewellen.pdf"] = pdf
        self.files["handout/manifest.json"] = json.dumps({
            "kind":"materiewellen-product-handout",
            "artifact":{"file":"materiewellen.pdf","bytes":len(pdf),"sha256":hashlib.sha256(pdf).hexdigest()},
            "canonical_inputs":[{"path":"content/typst/handout.typ","sha256":"a"*64}],
            "validation":{"pages":3,"paper":"A4","all_fonts_embedded":True,"text_inside_page_bounds":True,"compiler_warnings":0}
        }).encode()
        for name, data in self.files.items():
            p = self.dist / name
            p.parent.mkdir(parents=True, exist_ok=True)
            p.write_bytes(data)
        self.manifest = {'product_contract': CONTRACT, 'deployment_base': '/',
            'mode': 'static SPA; no prerendering claim',
            'files': {n: {'bytes': len(b), 'sha256': hashlib.sha256(b).hexdigest()} for n,b in self.files.items()}}
        self.write_manifest()
    def write_manifest(self):
        (self.dist / 'manifest.json').write_text(json.dumps(self.manifest))
    def test_current_fonts_and_product_inventory_accepted(self):
        self.assertEqual(inventory(self.dist, '/'), self.manifest)
        self.assertTrue(allowed_asset('fonts/lmroman10-regular.woff2'))
        self.assertTrue(allowed_asset('fonts/lmmono10-regular.woff2'))
        self.assertTrue(allowed_asset('fonts/LatinModern-LICENSE.txt'))
        self.assertTrue(all(allowed_asset(name) for name in LEGAL_INPUTS))
        self.assertFalse(allowed_asset('fonts/IBMPlexMono-Regular.ttf'))
        self.assertFalse(allowed_asset('fonts/lmmono10-regular.otf'))

    def test_all_legal_notices_are_mandatory_even_with_a_consistent_inventory(self):
        for name in LEGAL_INPUTS:
            with self.subTest(name=name):
                path = self.dist / name
                path.unlink()
                item = self.manifest['files'].pop(name)
                self.write_manifest()
                with self.assertRaisesRegex(AssertionError, 'Missing required'):
                    inventory(self.dist, '/')
                path.write_bytes(self.files[name])
                self.manifest['files'][name] = item
                self.write_manifest()

    def test_legal_notice_content_must_match_its_recorded_source(self):
        self.manifest['input_hashes'] = {'content/typst/handout.typ': 'a' * 64,
            **{source: hashlib.sha256(self.files[name]).hexdigest() for name, source in LEGAL_INPUTS.items()}}
        self.write_manifest()
        inventory(self.dist, '/')
        for name in LEGAL_INPUTS:
            with self.subTest(name=name):
                path = self.dist / name
                path.write_bytes(b'incomplete legal notice')
                original = self.manifest['files'][name]
                self.manifest['files'][name] = {'bytes': path.stat().st_size, 'sha256': digest(path)}
                self.write_manifest()
                with self.assertRaisesRegex(AssertionError, 'License/notice differs'):
                    inventory(self.dist, '/')
                path.write_bytes(self.files[name])
                self.manifest['files'][name] = original
                self.write_manifest()
    def test_stale_internal_inputs_are_excluded_and_preserved(self):
        names = ['learning-docs/arbeitsfassung.pdf', 'learning-docs/page-1.svg',
                 'learning-docs/document.json', 'typst-alle-ausgaben.zip', 'Index.kt',
                 'materiewellen.js.map', '../private.txt', 'math/document.json', 'tutor/materiewellen-tutor.zip']
        inputs = self.root / 'old-inputs'
        for name in names[:-4]:
            path = inputs / name
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_bytes(b'internal original')
        before = {p: p.read_bytes() for p in inputs.rglob('*') if p.is_file()}
        for name in names:
            self.assertFalse(allowed_asset(name), name)
        self.assertEqual(before, {p:p.read_bytes() for p in before})
    def test_unmanifested_and_manifested_internal_files_are_rejected(self):
        path = self.dist / 'arbeitsfassung.pdf'
        path.write_bytes(b'%PDF-internal')
        with self.assertRaisesRegex(AssertionError, 'Unmanifested'):
            inventory(self.dist, '/')
        self.manifest['files'][path.name] = {'bytes': path.stat().st_size, 'sha256': hashlib.sha256(path.read_bytes()).hexdigest()}
        self.write_manifest()
        with self.assertRaisesRegex(AssertionError, 'Excluded'):
            inventory(self.dist, '/')
    def test_handout_provenance_and_internal_pdf_boundary(self):
        from product_delivery import validate_handout
        manifest = json.loads(self.files["handout/manifest.json"])
        validate_handout(manifest, self.files["handout/materiewellen.pdf"])
        with self.assertRaises(AssertionError): validate_handout(manifest, b"%PDF-different")
        manifest["validation"]["pages"] = 4
        with self.assertRaises(AssertionError): validate_handout(manifest, self.files["handout/materiewellen.pdf"])
        self.assertFalse(allowed_asset("handout/arbeitsfassung.pdf"))
        self.assertFalse(allowed_asset("handout/other.pdf"))

    def test_tampered_asset_and_wrong_base_rejected(self):
        (self.dist / 'materiewellen.js').write_bytes(b'changed')
        with self.assertRaises(AssertionError): inventory(self.dist, '/')
        with self.assertRaises(AssertionError): inventory(self.dist, '/materiewelle/')
    def test_archive_is_deterministic_and_contains_only_product(self):
        inventory(self.dist, '/')
        a,b = self.root/'a.zip', self.root/'b.zip'
        archive(a,self.dist); archive(b,self.dist)
        self.assertEqual(a.read_bytes(),b.read_bytes())
        with zipfile.ZipFile(a) as z:
            self.assertEqual(set(z.namelist()),set(self.files)|{'manifest.json'})
            self.assertIsNone(z.testzip())
    def test_proof_is_bound_to_real_javascript_and_complete_scenarios(self):
        report = {'status':'passed','contract':CONTRACT, 'test_harness_sha256':digest(ROOT/'apps/web/scripts/test-run-two.py'),
            'release':{'javascript_sha256':self.manifest['files']['materiewellen.js']['sha256'],'deployment_base':'/','manifest_sha256':digest(self.dist/'manifest.json')},
            'checks':[{'passed':True}], 'scenarios':[{'name':n,'passed':True} for n in SCENARIOS],
            **{key:[] for key in ('page_errors','console_messages','request_failures','http_errors','forbidden_requests','outbound_requests')}}
        p=self.root/'report.json';p.write_text(json.dumps(report))
        proof(p,self.manifest, manifest_path=self.dist/'manifest.json')
        report['release']['javascript_sha256']='b'*64;p.write_text(json.dumps(report))
        with self.assertRaises(AssertionError): proof(p,self.manifest, manifest_path=self.dist/'manifest.json')
        report['release']['javascript_sha256']=self.manifest['files']['materiewellen.js']['sha256']
        report['scenarios'].pop();p.write_text(json.dumps(report))
        with self.assertRaises(AssertionError): proof(p,self.manifest, manifest_path=self.dist/'manifest.json')
    def test_stale_harness_or_changed_non_javascript_asset_cannot_reuse_proof(self):
        report = {'status':'passed','contract':CONTRACT,
            'test_harness_sha256':digest(ROOT/'apps/web/scripts/test-run-two.py'),
            'release':{'javascript_sha256':self.manifest['files']['materiewellen.js']['sha256'],
                       'deployment_base':'/','manifest_sha256':digest(self.dist/'manifest.json')},
            'checks':[{'passed':True}], 'scenarios':[{'name':n,'passed':True} for n in SCENARIOS],
            **{key:[] for key in ('page_errors','console_messages','request_failures','http_errors','forbidden_requests','outbound_requests')}}
        p=self.root/'proof.json';p.write_text(json.dumps(report))
        proof(p,self.manifest,manifest_path=self.dist/'manifest.json')
        report['test_harness_sha256']='0'*64;p.write_text(json.dumps(report))
        with self.assertRaisesRegex(AssertionError,'harness changed'):
            proof(p,self.manifest,manifest_path=self.dist/'manifest.json')
        report['test_harness_sha256']=digest(ROOT/'apps/web/scripts/test-run-two.py')
        p.write_text(json.dumps(report))
        self.manifest['created_at']='changed asset inventory despite identical JavaScript'
        self.write_manifest()
        with self.assertRaisesRegex(AssertionError,'different asset manifest'):
            proof(p,self.manifest,manifest_path=self.dist/'manifest.json')

    def test_release_packages_exactly_six_assets_and_standalone_handout(self):
        import copy, shutil
        from unittest.mock import patch
        spec=importlib.util.spec_from_file_location("prepare_current_release",Path(__file__).with_name("prepare-release.py"))
        module=importlib.util.module_from_spec(spec);spec.loader.exec_module(module)
        subpath=self.root/'subpath';shutil.copytree(self.dist,subpath)
        other=copy.deepcopy(self.manifest);other['deployment_base']='/materiewelle/'
        (subpath/'manifest.json').write_text(json.dumps(other))
        evidence=self.root/'evidence'
        for label,manifest in [('root',self.manifest),('subpath',other)]:
            report={'status':'passed','contract':CONTRACT, 'test_harness_sha256':digest(ROOT/'apps/web/scripts/test-run-two.py'),
                'release':{'javascript_sha256':manifest['files']['materiewellen.js']['sha256'],'deployment_base':manifest['deployment_base'],'manifest_sha256':digest((self.dist if label=='root' else subpath)/'manifest.json')},
                'checks':[{'passed':True}], 'scenarios':[{'name':n,'passed':True} for n in SCENARIOS],
                **{key:[] for key in ('page_errors','console_messages','request_failures','http_errors','forbidden_requests','outbound_requests')}}
            (evidence/label).mkdir(parents=True);(evidence/label/'report.json').write_text(json.dumps(report))
            receipt={'schema':'materiewelle-browser-suites-1', 'group':'all' if label=='root' else 'route',
                     'manifest_sha256':report['release']['manifest_sha256'],
                     'suites':[{'suite':name} for name in (('main','refinement','dg-learning','ownership','jonsson','personal-report') if label=='root' else ('main','refinement','dg-learning'))]}
            (evidence/(label+'-suites.json')).write_text(json.dumps(receipt))
        output=self.root/'release';commit='a'*40
        arguments=['prepare-release.py','--commit',commit,'--output',str(output),'--root-dist',str(self.dist),'--subpath-dist',str(subpath),'--evidence',str(evidence)]
        def verified_group(**kwargs):
            return json.loads(module.summary_path(kwargs['output']).read_text())
        with patch.object(module,'verify_source_inputs'),patch.object(module,'verify_evidence',side_effect=verified_group) as groups,patch.object(module.subprocess,'check_output',return_value=commit),patch.object(sys,'argv',arguments):
            module.main()
        self.assertEqual([call.kwargs['group'] for call in groups.call_args_list], ['all', 'route'])
        self.assertTrue(all('historical' not in call.kwargs for call in groups.call_args_list))
        self.assertEqual({p.name for p in output.iterdir()},{'materiewelle-web.zip','materiewelle-web-subpath.zip','materiewellen-handout.pdf','START.md','release-manifest.json','SHA256SUMS.txt'})
        self.assertEqual((output/'materiewellen-handout.pdf').read_bytes(),self.files['handout/materiewellen.pdf'])
        instructions = (output / 'START.md').read_text(encoding='utf-8')
        self.assertIn('MIT-Lizenz gilt für eigenen Projektcode', instructions)
        for source in LEGAL_INPUTS.values():
            self.assertIn('## ' + source, instructions)
            self.assertIn((ROOT / source).read_text(encoding='utf-8'), instructions)
        manifest=json.loads((output/'release-manifest.json').read_text())
        self.assertEqual(manifest['typst_pdf_count'],1)
        self.assertEqual(set(manifest['products']),{'kobweb','tutor','handout'})
        self.assertEqual(len(manifest['kobweb']['browser_suite_groups']['root']['suites']),6)
        with zipfile.ZipFile(output/'materiewelle-web-subpath.zip') as archive:
            self.assertIn('materiewelle/handout/materiewellen.pdf',archive.namelist())
            self.assertFalse(any('learning-docs/' in name for name in archive.namelist()))

    def test_release_refuses_staging_when_required_group_evidence_is_missing(self):
        from unittest.mock import patch
        spec=importlib.util.spec_from_file_location('prepare_missing_group',Path(__file__).with_name('prepare-release.py'))
        module=importlib.util.module_from_spec(spec);spec.loader.exec_module(module)
        output=self.root/'must-not-publish';commit='a'*40
        arguments=['prepare-release.py','--commit',commit,'--output',str(output),'--root-dist',str(self.dist)]
        with patch.object(module,'verify_source_inputs'), \
             patch.object(module,'verify_evidence',side_effect=FileNotFoundError('mandatory group receipt missing')) as verify, \
             patch.object(module.subprocess,'check_output',return_value=commit),patch.object(sys,'argv',arguments):
            with self.assertRaisesRegex(FileNotFoundError,'mandatory group'):
                module.main()
        self.assertFalse(output.exists())
        self.assertEqual(verify.call_args.kwargs['group'],'all')

    def test_exporter_filters_actual_stale_resource_tree(self):
        # Run the exporter in an isolated complete fixture, including old cache files.
        import shutil, subprocess
        app=self.root/'apps/web';scripts=app/'scripts';scripts.mkdir(parents=True)
        actual=Path(__file__).resolve().parents[2]/'apps/web/scripts'
        for name in ('export-spa.py','product_assets.py'): shutil.copyfile(actual/name,scripts/name)
        for source in LEGAL_INPUTS.values():
            path = self.root / source
            path.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(ROOT / source, path)
        inputs = self.root / 'inputs.json'
        inputs.write_text(json.dumps({source: digest(self.root / source) for source in LEGAL_INPUTS.values()}))
        resources=app/'site/build/processedResources/js/main/public';resources.mkdir(parents=True)
        production=app/'site/build/kotlin-webpack/js/productionExecutable';production.mkdir(parents=True)
        (resources/'index.html').write_text('<html><link href="favicon.ico" rel="icon"><script src="/materiewellen.js"></script></html>')
        for name in ('learning-docs/arbeitsfassung.pdf','learning-docs/page-1.svg','learning-docs/document.json','typst-alle-ausgaben.zip'):
            p=resources/name;p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(b'original')
        (production/'materiewellen.js').write_text('compiled\n//# sourceMappingURL=materiewellen.js.map\n')
        out=self.root/'export'
        command = [sys.executable,str(scripts/'export-spa.py'),'--output-dir',str(out),'--source-inputs',str(inputs)]
        subprocess.run(command,check=True,capture_output=True)
        names={p.relative_to(out).as_posix() for p in out.rglob('*') if p.is_file()}
        self.assertEqual(names,{'index.html','materiewellen.js','.nojekyll','manifest.json','.materiewellen-generated'} | set(LEGAL_INPUTS))
        for name, source in LEGAL_INPUTS.items():
            self.assertEqual((out / name).read_bytes(), (ROOT / source).read_bytes())
        self.assertTrue((resources/'learning-docs/arbeitsfassung.pdf').exists())
        self.assertNotIn('sourceMappingURL',(out/'materiewellen.js').read_text())
        repeat=subprocess.run(command,capture_output=True)
        self.assertNotEqual(repeat.returncode,0)

class BuildSourceBinding(unittest.TestCase):
    def test_compact_tutor_edit_after_build_rejected_but_test_addition_ignored(self):
        import shutil
        from unittest.mock import patch
        import product_delivery as delivery
        source_root=Path(__file__).resolve().parents[2]
        sys.path.insert(0,str(source_root/'tools/build'))
        from build_inputs import product_input_hashes
        with tempfile.TemporaryDirectory() as temporary:
            root=Path(temporary)
            for name in product_input_hashes(source_root):
                target=root/name;target.parent.mkdir(parents=True,exist_ok=True)
                shutil.copyfile(source_root/name,target)
            manifest={"input_hashes":product_input_hashes(root)}
            with patch.object(delivery,"ROOT",root):
                delivery.verify_source_inputs(manifest)
                test=root/'packages/physics-core/src/commonTest/UnrelatedTest.kt'
                test.parent.mkdir(parents=True,exist_ok=True);test.write_text('test fixture')
                delivery.verify_source_inputs(manifest)
                note=root/'content/tutor/materiewellen-fachtutor/SKILL.md'
                note.write_text(note.read_text()+'\nNew authored rule.\n')
                with self.assertRaisesRegex(AssertionError,"Source changed"):
                    delivery.verify_source_inputs(manifest)

if __name__ == '__main__': unittest.main(verbosity=2)
