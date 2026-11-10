#!/usr/bin/env python3
"""Verify the four-topic texts, shared sources, and deterministic ZIP."""
import hashlib
import importlib.util
import io
from pathlib import Path
import tempfile
import shutil
import unittest
import zipfile

ROOT = Path(__file__).resolve().parent
spec = importlib.util.spec_from_file_location('compact_builder', ROOT / 'build-compact-pack.py')
builder = importlib.util.module_from_spec(spec)
spec.loader.exec_module(builder)


class CompactPackageTests(unittest.TestCase):
    def test_actual_topic_bytes_and_shared_archive_match_authored_sources(self):
        with tempfile.TemporaryDirectory() as directory:
            a, b = Path(directory) / 'a', Path(directory) / 'b'
            first, second = builder.build(a), builder.build(b)
            self.assertEqual(first, second)
            self.assertEqual(first['schemaVersion'], '3.0')
            self.assertEqual(first['sourceRevision'], 'quellen-2026-10-18')
            self.assertEqual({s['section'] for s in first['scopes']}, set(builder.TOPICS))
            self.assertEqual(len(first['files']), 6)
            for scope in first['scopes']:
                start = (a / scope['startFile']).read_text()
                self.assertEqual(scope['wordCount'], len(start.split()))
                self.assertTrue(0 < scope['wordCount'] <= builder.MAX_START_WORDS)
                reference = (builder.SKILL / f'references/{scope["section"]}.md').read_text().strip()
                self.assertIn(reference, start)
                self.assertIn('## Antwortauftrag', start)
                self.assertIn('Kontrollwerte', start)
                self.assertIn(f'# Tutor: {scope["title"]}', start)
                for internal in ('Paketversion:', 'Quellenrevision:', 'ownWork:', 'schemaVersion',
                                 'references/', 'Client Dateien', 'native Installation', 'JSON-Kontext',
                                 'arbeitsfassung.pdf'):
                    self.assertNotIn(internal, start)
                self.assertNotIn(first['version'], start)
            for asset in first['files']:
                self.assertEqual(Path(asset['path']).name, asset['path'])
                raw = (a / asset['path']).read_bytes()
                self.assertEqual(raw, (b / asset['path']).read_bytes())
                self.assertEqual(asset['bytes'], len(raw))
                self.assertEqual(asset['sha256'], hashlib.sha256(raw).hexdigest())
            with zipfile.ZipFile(io.BytesIO((a/'materiewellen-fachtutor.zip').read_bytes())) as archive:
                self.assertIsNone(archive.testzip())
                expected = {f'materiewellen-fachtutor/{p.relative_to(builder.SKILL).as_posix()}'
                    for p in builder.SKILL.rglob('*.md')}
                self.assertEqual(set(archive.namelist()), expected)
                self.assertEqual(len(expected), 5)
                for name in expected:
                    relative = name.split('/', 1)[1]
                    self.assertEqual(archive.read(name), (builder.SKILL/relative).read_bytes())
            self.assertEqual((a/'START-IM-CHAT.md').read_bytes(),(a/'START-debroglie.md').read_bytes())

    def test_short_reference_is_delivered_without_a_padding_requirement(self):
        original = builder.SKILL
        with tempfile.TemporaryDirectory() as directory:
            skill = Path(directory)/'skill'
            shutil.copytree(original, skill)
            reference = 'Kurzer Fachkontext für eine klar begrenzte Aufgabe.'
            (skill/'references/debroglie.md').write_text(reference)
            builder.SKILL = skill
            try:
                output = Path(directory)/'output'
                manifest = builder.build(output)
                scope = next(item for item in manifest['scopes'] if item['section'] == 'debroglie')
                delivered = (output/scope['startFile']).read_text()
                self.assertLess(scope['wordCount'], 600)
                self.assertEqual(scope['wordCount'], len(delivered.split()))
                self.assertTrue(delivered.rstrip().endswith(reference))
                asset = next(item for item in manifest['files'] if item['path'] == scope['startFile'])
                self.assertEqual(asset['sha256'], hashlib.sha256(delivered.encode()).hexdigest())
            finally: builder.SKILL = original

    def test_empty_response_instructions_or_reference_fail_before_delivery(self):
        original = builder.SKILL
        for empty_part in ('instructions', 'reference'):
            with self.subTest(empty_part=empty_part), tempfile.TemporaryDirectory() as directory:
                skill = Path(directory)/'skill'
                shutil.copytree(original, skill)
                if empty_part == 'instructions':
                    (skill/'SKILL.md').write_text('---\nname: test\n---\n' + builder.SKILL_ONLY_SECTION + 'Skill-only instructions')
                else:
                    (skill/'references/debroglie.md').write_text(' \n')
                builder.SKILL = skill
                try:
                    output = Path(directory)/'output'
                    with self.assertRaisesRegex(ValueError, 'must not be empty'):
                        builder.build(output)
                    self.assertFalse(output.exists())
                finally: builder.SKILL = original

    def test_missing_reference_fails_before_writing_delivery(self):
        original = builder.SKILL
        with tempfile.TemporaryDirectory() as directory:
            skill = Path(directory)/'skill';skill.mkdir()
            (skill/'SKILL.md').write_text('---\nname: test\n---\nBody')
            builder.SKILL = skill
            try:
                with self.assertRaisesRegex(ValueError, 'four topic references'):
                    builder.build(Path(directory)/'output')
                self.assertFalse((Path(directory)/'output').exists())
            finally: builder.SKILL = original

    def test_missing_skill_only_boundary_cannot_leak_file_instructions(self):
        original = builder.SKILL
        with tempfile.TemporaryDirectory() as directory:
            skill = Path(directory)/'skill'
            shutil.copytree(original, skill)
            source = skill/'SKILL.md'
            source.write_text(source.read_text().replace(builder.SKILL_ONLY_SECTION, '\n## Ohne Abgrenzung\n'))
            builder.SKILL = skill
            try:
                with self.assertRaisesRegex(ValueError, 'explicit section boundary'):
                    builder.build(Path(directory)/'output')
                self.assertFalse((Path(directory)/'output').exists())
            finally: builder.SKILL = original

    def test_refuses_overwriting_authored_sources(self):
        for output in (builder.SKILL, builder.SKILL/'generated', builder.ROOT):
            with self.assertRaises(ValueError): builder.build(output)


if __name__ == '__main__':
    unittest.main(verbosity=2)
