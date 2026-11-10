"""Copy the single scientific dataset and its verified image into browser resources."""
from pathlib import Path
import hashlib
import json
import shutil
import struct
import subprocess
import sys

project = Path(__file__).resolve().parents[1]
repo = project.parents[1]
source = repo / 'content/data/versuchsdaten.json'
out = project / 'site/build/generated/materiewelle-product/public'
out.mkdir(parents=True, exist_ok=True)
data = json.loads(source.read_text())
if data.get('schema_version') != '2.0':
    raise ValueError('Only the selected scientific schema 2.0 may enter the browser build; old AI-plan rows are excluded.')
dg = data['davisson_germer']
geometry = dg['geometry']
if geometry['model'] != 'ni111_surface_rows_normal_incidence' or geometry['angle_reference'] != 'outward_surface_normal':
    raise ValueError('DG model or angle reference has changed; an explicit scientific review is required.')
for evidence, expected_role in ((dg['evidence'], 'secondary_report'), (geometry['structure_evidence'], 'independent_structure_assumption')):
    if evidence['role'] != expected_role or evidence['review_status'] != 'checked_in_scope' or not evidence['location'].strip():
        raise ValueError('Active DG evidence must preserve its reviewed role and concrete location.')
    if data['source_ids'].get(evidence['source_id'], {}).get('kind') != 'didactic_secondary_analysis':
        raise ValueError('The rejected AI project plan cannot supply active DG evidence.')
if dg['original_verified'] or any(row['kind'] != 'secondary_report' for row in dg['rows']):
    raise ValueError('A checked secondary analysis must not be relabelled as original data.')
shutil.copy2(source, out / source.name)
image = data.get('jonsson', {}).get('image', {})
if image.get('available'):
    original = (repo / image['file']).resolve()
    original.relative_to((repo / 'content/sources').resolve())
    image_bytes = original.read_bytes()
    if hashlib.sha256(image_bytes).hexdigest() != image['sha256']:
        raise ValueError('Jönsson image does not match the verified source checksum.')
    if len(image_bytes) < 24 or image_bytes[:8] != b'\x89PNG\r\n\x1a\n' or image_bytes[12:16] != b'IHDR':
        raise ValueError('Jönsson source must be a PNG with its original native raster header.')
    native_dimensions = struct.unpack('>II', image_bytes[16:24])
    declared_dimensions = (image['native_width_px'], image['native_height_px'])
    if native_dimensions != declared_dimensions:
        raise ValueError('Jönsson native raster dimensions differ from scientific metadata: '
                         f'PNG={native_dimensions}, metadata={declared_dimensions}.')
    shutil.copy2(original, out / original.name)
profiles_path = repo / 'content/measurement/joensson_sensitivitaet.json'
profiles = json.loads(profiles_path.read_text())
if profiles['source_image_sha256'] != image.get('sha256'):
    raise ValueError('Derived profiles do not belong to the canonical image.')
script = (repo / profiles['calculation_script']).resolve()
script.relative_to((repo / 'content/measurement').resolve())
if hashlib.sha256(script.read_bytes()).hexdigest() != profiles['calculation_script_sha256']:
    raise ValueError('Recompute derived profiles after changing the calculation script.')
shutil.copy2(profiles_path, out / profiles_path.name)
print('Scientific JSON, verified image and separately identified derived profiles copied unchanged.')
if "--data-only" not in sys.argv:
    subprocess.run([sys.executable, str(repo / 'content/typst/math/build.py'), '--output-dir', str(out / 'math')], check=True)
    subprocess.run([sys.executable, str(repo / 'content/tutor/build-compact-pack.py'), '--output-dir', str(out / 'tutor')], check=True)

    subprocess.run([sys.executable, str(repo / 'content/typst/build-handout.py'), '--output-dir', str(out / 'handout'), '--replace-generated'], check=True)

    subprocess.run([sys.executable, str(project / 'scripts/build-math-text.py')], check=True)
