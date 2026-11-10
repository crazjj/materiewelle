# Lokal bauen, prüfen und ausliefern

Der mitgelieferte Bootstrap unterstützt **Linux x86_64**. Benötigt werden Bash,
Python 3 mit venv, Node.js (erprobt: 24), curl, tar, unzip, sha256sum und flock.
Für das PDF: Poppler (`pdfinfo`, `pdffonts`, `pdftotext`), Fontconfig und DejaVu Sans.
Latin Modern liegt mit seinen Lizenzdateien im Repository. Andere Betriebssysteme
sind für diese Shell-Werkzeuge nicht geprüft.

## Werkzeugkette

Alle Befehle vom Repository-Stamm aus:

```bash
export TASK_TOOLS_ROOT="${XDG_CACHE_HOME:-$HOME/.cache}/materiewelle-tools"
tools/build/setup-cloud.sh
export PATH="$TASK_TOOLS_ROOT/typst/bin:$PATH"
# Browser einschließlich nötiger Systembibliotheken (kann Rootrechte benötigen):
python -m playwright install --with-deps chromium
```

Auf Debian/Ubuntu müssen beispielsweise `python3-venv`, `poppler-utils`,
`fontconfig` und `fonts-dejavu-core` vorhanden sein. Der Bootstrap lädt geprüfte
JDK-/Gradle-Archive; Pythonpakete und npm-Lockdatei sind versioniert. Download-
Caches dürfen zwischen Builds geteilt werden; Quellcode aus einem privaten
Archiv wird für den Build nicht benötigt.

## Bauen und öffnen

```bash
apps/web/build-static.sh --output-dir /tmp/materiewelle-web
apps/web/serve-static.sh 8082 /tmp/materiewelle-web
```

Danach `http://localhost:8082/` öffnen. Das Ausgabeverzeichnis muss frisch oder
leer sein. Die Ausgabe ist eine statische SPA; die Seite wird im Browser gerendert.
Kein Backend und kein Typst-Compiler werden vom Besucher benötigt.

## Prüfen

```bash
apps/web/run-gradle.sh :physics-core:jvmTest :physics-core:jsNodeTest \
  :browser-design:jsNodeTest :site:jsNodeTest
python content/typst/test-handout.py
python content/tutor/test-compact-pack.py
python .github/scripts/browser_checks.py \
  --dist /tmp/materiewelle-web --output /tmp/materiewelle-browser --group all
```

Der Browserrunner findet installierten Chromium oder verwendet die
Playwright-Installation. Ein eigener Browser lässt sich mit
`--chromium /usr/bin/chromium` angeben. Jede Prüfung braucht ein frisches
Nachweisverzeichnis. Erwartet werden alle sechs Root-Suiten; ein Teiltest ist
keine vollständige Abnahme. Die optionale Neuberechnung der Bildprofile ist in
[content/measurement](../content/measurement/README.md) beschrieben.

Unterpfad getrennt prüfen:

```bash
apps/web/build-static.sh --base-path /materiewelle/ --output-dir /tmp/materiewelle-subpath
python .github/scripts/browser_checks.py \
  --dist /tmp/materiewelle-subpath --base-path /materiewelle/ \
  --output /tmp/materiewelle-browser-subpath --group route
```

## Vercel

```bash
python .github/scripts/prepare-vercel.py \
  --source /tmp/materiewelle-web --output /tmp/materiewelle-vercel \
  --commit "$(git rev-parse HEAD)"
python .github/scripts/browser_checks.py \
  --dist /tmp/materiewelle-vercel/.vercel/output/static \
  --output /tmp/materiewelle-public-browser --group all
```

Das frische Vercel-Verzeichnis enthält `.vercel/output` für Build Output API v3,
keine Source Maps. Vor einem Upload dieses tatsächlich auszuliefernde Paket prüfen.

Nach erfolgreicher Prüfung mit einer angemeldeten Vercel CLI veröffentlichen:

```bash
cd /tmp/materiewelle-vercel
vercel link
vercel deploy --prebuilt --prod
```

Bei `vercel link` das bestehende Materiewellen-Projekt wählen. Ein fertig
vorbereitetes ZIP zuerst entpacken und die Befehle in dessen Hauptverzeichnis
ausführen. Dabei muss auch der versteckte Ordner `.vercel` vorhanden sein.

GitHub Actions sind abgeschaltet. Build, Browserprüfungen und Paketierung laufen
lokal; ein neuer Commit erzeugt kein automatisches GitHub-Release. Frühere
Workflows bleiben im Archiv. Die README-Galerie wird nach einer Sichtprüfung
aktualisiert.
[Verbindliche Browsermatrix](../.github/scripts/BROWSER-CHECKS.md) und
[Build-/Paketwerkzeuge](../tools/build/README.md) erklären den Vertrag.

Ein erfolgreicher Push oder ein lokaler Build bestätigt die öffentliche
Vercel-Revision nicht. Nach einem Deployment `/manifest.json` auf dem Host mit
dem lokalen Manifest vergleichen: `source_commit` muss zum ausgelieferten Stand
gehören. Die tatsächlich ausgelieferten Dateien `/materiewellen.js` und
`/handout/materiewellen.pdf` anhand ihrer SHA-256 im Manifest prüfen.
Nach einer Zusammenfassung der Git-Historie das
Paket mit der neuen Commit-ID vorbereiten; alte Pakete und Prüfberichte bleiben
Nachweise ihrer damaligen Fassung.

Zum Abschluss auf dem echten iPad 9 in Safari und im Querformat prüfen:
Herleitung vorwärts und rückwärts scrollen, DG-Bildschritte bedienen,
Jönsson-Erklärung öffnen, eigene Bildmarken setzen und zoomen, eine Tutorfrage
in einen anderen Chat kopieren und das Handout öffnen. Datum, Gerät/iPadOS und
Ergebnis getrennt von den Chromium-Prüfungen festhalten.

## Handoutvarianten vergleichen

Die neuen Varianten werden getrennt vom produktiven Handout gebaut. Anleitung und
Seitenvorschauen: [Handout A/B](handout-varianten/README.md). Erst nach einer Auswahl
wird eine Variante in die produktive Typst-Quelle und die Website übernommen.
