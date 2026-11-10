#!/usr/bin/env python3
"""Package the current web product and its verified Typst handout."""
import argparse, json, os, re, shutil, subprocess
from pathlib import Path
from datetime import datetime, timezone
from product_delivery import CONTRACT, inventory, proof, archive, digest, verify_source_inputs
from browser_checks import verify_evidence, summary_path
ROOT = Path(os.environ.get("RELEASE_PROJECT_ROOT", Path(__file__).resolve().parents[2]))


def release_readme(root):
    text = ("# Materiewelle\n\n"
            "Root-ZIP entpacken; mit `python3 -m http.server 8082` starten.\n"
            "Projektpfad-ZIP im Elternordner starten und `/materiewelle/` öffnen.\n"
            "Clientseitige statische SPA, kein Prerendering. Vier zusammenhängende Artikel: de Broglie, Davisson–Germer, Jönsson, Fazit.\n"
            "Vier kuratierte Tutor-Starts und ein portabler Fachtutor sind enthalten; kein Anbieter wird kontaktiert.\n"
            "Das dreiseitige Handout ist zusätzlich als `materiewellen-handout.pdf` enthalten.\n"
            "Echte iPad-/Safari- und menschliche Lernprüfungen separat ausweisen.\n\n"
            "Die MIT-Lizenz gilt für eigenen Projektcode. Für fremde Inhalte und Schriftarten gelten die folgenden Hinweise. "
            "Diese Datei enthält auch die vollständige DejaVu-Lizenz für die im eigenständigen PDF eingebettete Schrift.\n")
    for name in ("LICENSE", "content/licenses/NOTICE.txt", "content/licenses/DejaVu-LICENSE.txt", "content/licenses/Apache-2.0.txt", "content/licenses/Octicons-LICENSE.txt"):
        text += "\n## " + name + "\n\n```text\n" + (root / name).read_text(encoding="utf-8") + "\n```\n"
    return text


def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("--commit", required=True); p.add_argument("--output", required=True, type=Path)
    p.add_argument("--root-dist", type=Path, default=ROOT / "apps/web/dist-run-two")
    p.add_argument("--subpath-dist", type=Path, default=ROOT / "apps/web/dist-run-two-subpath/materiewelle")
    p.add_argument("--evidence", type=Path, default=ROOT / "release-evidence")
    a = p.parse_args()
    assert re.fullmatch(r"[0-9a-f]{40}", a.commit)
    assert subprocess.check_output(["git", "rev-parse", "HEAD"], cwd=ROOT, text=True).strip() == a.commit
    reports, groups = {}, {}
    for label, directory, base in [("root", a.root_dist, "/"), ("subpath", a.subpath_dist, "/materiewelle/")]:
        m = inventory(directory, base)
        verify_source_inputs(m)
        group = verify_evidence(project_root=ROOT, output=a.evidence / label,
                                manifest_path=directory / "manifest.json",
                                group="all" if label == "root" else "route")
        groups[label] = {"schema": group["schema"], "group": group["group"],
                         "manifest_sha256": group["manifest_sha256"],
                         "receipt_sha256": digest(summary_path(a.evidence / label)),
                         "suites": [entry["suite"] for entry in group["suites"]]}
        reports[label] = proof(a.evidence / label / "report.json", m, manifest_path=directory / "manifest.json")
    instructions = release_readme(ROOT)
    a.output.mkdir(parents=True, exist_ok=True)
    assert not any(a.output.iterdir()), "Release staging must be empty"
    archive(a.output / "materiewelle-web.zip", a.root_dist)
    archive(a.output / "materiewelle-web-subpath.zip", a.subpath_dist, "materiewelle/")
    assert digest(a.root_dist / "handout/materiewellen.pdf") == digest(a.subpath_dist / "handout/materiewellen.pdf"), "Root/subpath handouts differ"
    shutil.copyfile(a.root_dist / "handout/materiewellen.pdf", a.output / "materiewellen-handout.pdf")
    (a.output / "START.md").write_text(instructions, encoding="utf-8")
    files = {f.name: {"bytes": f.stat().st_size, "sha256": digest(f)} for f in a.output.iterdir()}
    m = {"schema_version": "3.0", "product_contract": CONTRACT, "products": ["kobweb", "tutor", "handout"],
         "source_commit": a.commit, "created_at": datetime.now(timezone.utc).isoformat(), "typst_pdf_count": 1,
         "files": files, "kobweb": {"rendering": "static client-rendered SPA", "bases": ["/", "/materiewelle/"],
         "browser_reports": {k: len(v["checks"]) for k,v in reports.items()},
         "browser_suite_groups": groups}}
    (a.output / "release-manifest.json").write_text(json.dumps(m, indent=2) + "\n")
    (a.output / "SHA256SUMS.txt").write_text("".join(digest(f) + "  " + f.name + "\n" for f in sorted(a.output.iterdir())))
    print(json.dumps({"assets": len(list(a.output.iterdir())), "pdfs": 1}))
if __name__ == "__main__": main()
