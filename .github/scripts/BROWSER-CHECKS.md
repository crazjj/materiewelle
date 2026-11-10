# Browser- und Releaseprüfung

Für die lokale Abnahme gilt derselbe vollständige Produktvertrag. Es gibt keine Abhängigkeit von privaten Commit-IDs und keine
historischen Ausnahmen für fehlende Tests.

| Export | Verpflichtende Suiten |
|---|---|
| Root `/` | Hauptsuite, UI-Refinement, DG-Lernpfad, Speicherbesitz, Jönsson, persönlicher Bericht |
| Unterpfad `/materiewelle/` | Hauptsuite, UI-Refinement, DG-Lernpfad |

`browser_checks.py` führt diese vorhandenen Browserharnesses aus. Die sechs
Root-Suiten werden als `--group all` ausgeführt, die drei Unterpfadsuiten als
`--group route`. `focused` ist eine lokale Teilprüfung und ersetzt keine
vollständige Releaseabnahme.

```sh
python .github/scripts/browser_checks.py \
  --dist apps/web/dist-run-two --output /tmp/neue-browser-evidenz/root \
  --group all --chromium /usr/bin/chromium
```

Das Ausgabeverzeichnis muss frisch sein. `root/report.json` enthält den
Hauptreport; weitere Suiten schreiben nach `root-refinement`, `root-dg-learning`,
`root-ownership`, `root-jonsson` und `root-personal-report`. `root-suites.json`
bindet Manifest, Runner, tatsächlich importierte Python-Helfer und Originalreports
über SHA-256. Ein erfolgreicher Prozess ohne vollständigen Report genügt nicht.
Abbrüche behalten ihre bisherigen Belege im gewählten lokalen Verzeichnis.

`verify_evidence(project_root=..., output=..., manifest_path=..., group="all")`
prüft diese Bindung vor dem Packen erneut. Geänderte Produktbytes, fehlende
Szenarien, veränderte Reports oder geänderte Helfer verhindern die Freigabe.
Die Prüfungen erzeugen keine Produktbytes und verändern keine alten Belege.

GitHub Actions sind abgeschaltet; die bisherigen Workflows liegen unter
`archive/github-actions/`. Diese Prüfungen werden lokal ausgeführt. Es gibt
weder automatische Commit-Releases noch einen Pages-Workflow.
`prepare-vercel.py` stellt einen geprüften Root-Export für den bestehenden
Vercel-Host bereit; das Skript veröffentlicht selbst nichts.

Orchestrierungs- und Paketprüfungen ohne Browser/Build:

```sh
for test in .github/scripts/test-*.py; do python3 "$test" || exit; done
```

Diese Fixtures ersetzen keinen echten Browserlauf auf dem finalen Export auf dem aktuellen Quellstand. Chromium deckt keine reale iPad-/Safari-
Abnahme oder menschliche Lernprüfung ab. Werkzeugversionen und Linux-Voraussetzungen
stehen in [tools/build/README.md](../../tools/build/README.md).
