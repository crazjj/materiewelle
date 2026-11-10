#!/usr/bin/env python3
"""Build the four-topic tutor from a shared core and curated references; no network."""
import argparse
import hashlib
import io
import json
from pathlib import Path
import zipfile

ROOT = Path(__file__).resolve().parent
SKILL = ROOT / 'materiewellen-fachtutor'
VERSION = '3.3.0'
MAX_START_WORDS = 1200
SKILL_ONLY_SECTION = '\n## Fachreferenzen im Skill\n'
TOPICS = {'debroglie': 'De Broglie', 'davisson-germer': 'Davisson–Germer',
          'joensson': 'Jönsson', 'fazit': 'Fazit'}


def build(output):
    output = Path(output)
    destination = output.resolve()
    if destination == ROOT or destination == SKILL.resolve() or SKILL.resolve() in destination.parents:
        raise ValueError('Generated assets must be outside authored sources')
    files = {p.relative_to(SKILL).as_posix(): p.read_bytes() for p in sorted(SKILL.rglob('*.md'))}
    expected = {'SKILL.md'} | {f'references/{topic}.md' for topic in TOPICS}
    if set(files) != expected:
        raise ValueError('Skill must contain exactly one shared core and four topic references')
    body = files['SKILL.md'].decode().split('---\n', 2)[2]
    # Keep file-reading instructions in the expert ZIP, never in a copy/paste message.
    if body.count(SKILL_ONLY_SECTION) != 1:
        raise ValueError('Skill-only reference instructions need one explicit section boundary')
    body = body.split(SKILL_ONLY_SECTION, 1)[0].strip()
    if not body:
        raise ValueError('Shared response instructions must not be empty')
    assets, scopes = {}, []
    for topic, title in TOPICS.items():
        reference = files[f'references/{topic}.md'].decode().strip()
        if not reference:
            raise ValueError(f'{topic}: topic reference must not be empty')
        start = f'# Tutor: {title}\n\n{body}\n\n---\n\n{reference}\n'
        words = len(start.split())
        if words > MAX_START_WORDS:
            raise ValueError(f'{topic}: curated start has {words} words; maximum is {MAX_START_WORDS}')
        name = f'START-{topic}.md'
        assets[name] = start.encode()
        scopes.append({'section': topic, 'title': title, 'startFile': name, 'wordCount': words})
    # Existing direct text links remain useful; this alias is explicitly De Broglie.
    assets['START-IM-CHAT.md'] = assets['START-debroglie.md']
    stream = io.BytesIO()
    with zipfile.ZipFile(stream, 'w', zipfile.ZIP_DEFLATED, compresslevel=9) as archive:
        for path, data in files.items():
            info = zipfile.ZipInfo(f'{SKILL.name}/{path}', date_time=(1980, 1, 1, 0, 0, 0))
            info.external_attr = 0o100644 << 16
            info.compress_type = zipfile.ZIP_DEFLATED
            archive.writestr(info, data)
    assets['materiewellen-fachtutor.zip'] = stream.getvalue()
    manifest = {'schemaVersion': '3.0', 'version': VERSION, 'skillName': SKILL.name,
                'knowledgeDate': '2026-11-09', 'sourceRevision': 'quellen-2026-10-18',
                'scopes': scopes,
                'files': [{'path': name, 'bytes': len(raw), 'sha256': hashlib.sha256(raw).hexdigest()}
                          for name, raw in assets.items()]}
    output.mkdir(parents=True, exist_ok=True)
    for name, raw in assets.items():
        (output / name).write_bytes(raw)
    (output / 'manifest.json').write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + '\n')
    return manifest


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--output-dir', required=True, type=Path)
    args = parser.parse_args()
    manifest = build(args.output_dir)
    print(f'Physics tutor {VERSION}: ' + ', '.join(f'{s["section"]} {s["wordCount"]} words' for s in manifest['scopes']))
