"""Canonical source inputs for the public web application and Typst handout."""
import hashlib
from pathlib import Path


def product_input_hashes(root):
    """Bind a distribution to all its source, generator, font and license inputs."""
    root = Path(root)
    names = {"apps/web/" + name for name in (
        "settings.gradle.kts", "build.gradle.kts", "gradle/libs.versions.toml",
        "gradle.properties", "site/build.gradle.kts", "browser-design/build.gradle.kts",
        "browser-bootstrap/build.gradle.kts", "site/.kobweb/conf.yaml",
        "kotlin-js-store/package-lock.json", "run-gradle.sh", "build-static.sh",
        "scripts/build-static.py", "scripts/export-spa.py", "scripts/sync-data.py",
        "scripts/build-math-text.py", "scripts/product_assets.py")}
    names.update({
        "LICENSE", "content/licenses/Apache-2.0.txt", "content/licenses/Octicons-LICENSE.txt", "content/licenses/NOTICE.txt", "content/licenses/DejaVu-LICENSE.txt",
        "packages/physics-core/build.gradle.kts",
        "tools/build/build_inputs.py", "tools/build/gradle-run",
        "content/data/versuchsdaten.json", "content/sources/jonsson-page6-xref33.png",
        "content/measurement/joensson_sensitivitaet.json", "content/measurement/sensitivitaet.py",
        "content/tutor/build-compact-pack.py", "content/typst/build-handout.py",
        "content/typst/handout.typ", "content/typst/fachwerte.typ", "content/typst/vorlage.typ",
        "content/typst/requirements.txt", "content/typst/auswahl.json",
    })
    directories = [
        "content/tutor/materiewellen-fachtutor", "content/typst/math",
        "content/typst/handoutassets", "content/typst/fonts",
    ]
    for module in ("apps/web/site", "apps/web/browser-design", "apps/web/browser-bootstrap",
                   "packages/physics-core"):
        directories.extend(f"{module}/src/{target}" for target in ("commonMain", "jsMain"))
    for directory in directories:
        base = root / directory
        for path in base.rglob("*"):
            relative = path.relative_to(base)
            generated_manifest = directory == "content/typst/math" and path.name == "manifest.json"
            if (path.is_file() and not generated_manifest
                    and not {"__pycache__", "ausgabe", "vorschau"}.intersection(relative.parts)
                    and path.suffix != ".pyc"):
                names.add(path.relative_to(root).as_posix())
    return {name: hashlib.sha256((root / name).read_bytes()).hexdigest()
            for name in sorted(names)}


def build_input_hashes(root):
    """Compatibility name for callers checking the same current product inputs."""
    return product_input_hashes(root)
