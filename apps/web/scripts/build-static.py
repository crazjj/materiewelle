"""Build a genuine Kobweb release for root or an explicit site.basePath.

The tracked default configuration is restored even when Gradle fails. A project
lock spans configuration, compilation, export and restoration; the shared tool
wrapper separately serializes heavyweight Gradle work across Kotlin build tasks.
"""
from pathlib import Path
import argparse
import json
import tempfile
import fcntl
import signal
import re
import subprocess
import sys

root = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser()
parser.add_argument('--base-path', default='/', help='Origin-root / or a path such as /materiewelle/')
parser.add_argument('--output-dir', type=Path, help='Fresh serving root; never overwritten')
args = parser.parse_args()
sys.path.insert(0, str(root.parents[1] / 'tools/build'))
from build_inputs import product_input_hashes
base = args.base_path
if not re.fullmatch(r'/(?:[A-Za-z0-9_-]+/)*', base):
    parser.error('Use / or a slash-terminated path containing letters, digits, hyphens and underscores.')
def interrupted(signum, frame):
    raise KeyboardInterrupt(f'Build interrupted by signal {signum}')

signal.signal(signal.SIGTERM, interrupted)
project_lock = (root / '.build-static.lock').open('a')
fcntl.flock(project_lock, fcntl.LOCK_EX)
config = root / 'site/.kobweb/conf.yaml'
original = config.read_text()
if re.search(r'^\s*basePath\s*:', original, flags=re.M):
    raise SystemExit('Default configuration already contains basePath; inspect before changing it.')
try:
    source_inputs = product_input_hashes(root.parents[1])
    if base != '/':
        config.write_text(original.replace('site:\n', f'site:\n  basePath: "{base.rstrip("/")}"\n', 1))
    target = args.output_dir or root / ('dist-run-two' if base == '/' else 'dist-run-two-subpath')
    if target.exists() and any(target.iterdir()):
        raise SystemExit('Output is not empty; choose a fresh --output-dir. Existing files are preserved.')
    subprocess.run([str(root / 'run-gradle.sh'), ':site:jsBrowserProductionWebpack', '-PkobwebBuildTarget=RELEASE'], cwd=root, check=True)
    with tempfile.TemporaryDirectory(prefix='materiewelle-build-inputs-') as temporary:
        metadata = Path(temporary) / 'inputs.json'
        metadata.write_text(json.dumps(source_inputs))
        subprocess.run([sys.executable, str(root / 'scripts/export-spa.py'), '--base-path', base, '--output-dir', str(target), '--source-inputs', str(metadata)], cwd=root, check=True)
finally:
    if config.read_text() != original:
        config.write_text(original)
