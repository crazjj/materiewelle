"""Explicit resource boundary for the Run-02 public product."""
from pathlib import PurePosixPath
CONTRACT = "run-two-2026-11-04"
LEGAL_INPUTS = {
    "Apache-2.0.txt": "content/licenses/Apache-2.0.txt",
    "Octicons-LICENSE.txt": "content/licenses/Octicons-LICENSE.txt",
    "LICENSE.txt": "LICENSE",
    "NOTICE.txt": "content/licenses/NOTICE.txt",
    "fonts/DejaVu-LICENSE.txt": "content/licenses/DejaVu-LICENSE.txt",
}
ROOT_FILES = frozenset({"index.html", "app.css", "browser-init.js", "materiewellen.js", ".nojekyll", "versuchsdaten.json", "joensson_sensitivitaet.json", "jonsson-page6-xref33.png", "LICENSE.txt", "NOTICE.txt", "Apache-2.0.txt", "Octicons-LICENSE.txt"})
TUTOR_FILES = frozenset({"manifest.json", "START-IM-CHAT.md", "START-debroglie.md", "START-davisson-germer.md", "START-joensson.md", "START-fazit.md", "materiewellen-fachtutor.zip"})
FONT_FILES = frozenset({
    "lmroman10-regular.woff2", "lmroman10-bold.woff2", "lmroman10-italic.woff2", "lmroman10-bolditalic.woff2",
    "lmmono10-regular.woff2", "lmmono10-italic.woff2",
    "LatinModern-LICENSE.txt", "LatinModern-COPYRIGHT.txt", "LatinModern-MANIFEST.txt", "DejaVu-LICENSE.txt",
})
def allowed_asset(name):
    p = PurePosixPath(name)
    if p.is_absolute() or ".." in p.parts or "\\" in name: return False
    if name in ROOT_FILES: return True
    if name in {"handout/materiewellen.pdf", "handout/manifest.json"}: return True
    if len(p.parts) == 2 and p.parts[0] == "tutor": return p.name in TUTOR_FILES
    if name in {"math/manifest.json", "math-text/catalog.json"}: return True
    if len(p.parts) == 2 and p.parts[0] in {"math", "math-text"}: return p.suffix == ".svg"
    if len(p.parts) == 2 and p.parts[0] == "fonts": return p.name in FONT_FILES
    return False
