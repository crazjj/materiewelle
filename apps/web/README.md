# Webanwendung

Die statische Kotlin/JS-Anwendung verbindet vier Fachartikel zu de Broglie,
Davisson–Germer, Jönsson und dem Fazit mit interaktiven Modellen, eigenen
Messungen, einem lokalen Arbeitsstand und kopierbarer Tutorhilfe.
Formeln werden beim Build mit Typst in SVG-Pfade gesetzt. Im Browser sind weder
Typst noch ein Backend erforderlich.

## Lokal bauen und öffnen

Die [Buildwerkzeuge](../../tools/build/README.md) installieren die festgelegten
Versionen von JDK, Gradle und Typst; Node muss vorhanden sein (CI: Node 24).
Die vollständigen Voraussetzungen stehen in [START.md](../../docs/START.md),
die Anforderungen an Handout und PDF in [HANDOUT.md](../../content/typst/HANDOUT.md).

Vom Repository-Root:

```bash
export TASK_TOOLS_ROOT="${TASK_TOOLS_ROOT:-${XDG_CACHE_HOME:-$HOME/.cache}/materiewelle-tools}"
tools/build/setup-cloud.sh
export PATH="$TASK_TOOLS_ROOT/typst/bin:$PATH"
apps/web/build-static.sh --output-dir /tmp/materiewelle-web
apps/web/serve-static.sh 8082 /tmp/materiewelle-web
```

Die Anwendung ist anschließend unter `http://localhost:8082/` erreichbar.
Der Exportordner muss leer sein. Ein Export für einen Unterpfad verwendet eine
passende Kobweb-Konfiguration während des Builds und stellt sie danach wieder
her:

```bash
apps/web/build-static.sh --base-path /materiewelle/ --output-dir /tmp/materiewelle-pages
apps/web/serve-static.sh 8082 /tmp/materiewelle-pages
```

Dieser Export wird unter `http://localhost:8082/materiewelle/` geöffnet.
`TASK_TOOLS_ROOT` kann den Werkzeugordner überschreiben; standardmäßig liegt er
unter `${XDG_CACHE_HOME:-$HOME/.cache}/materiewelle-tools`.

## Module und Eingaben

| Pfad | Aufgabe |
|---|---|
| `site/` | Compose-HTML-Oberfläche, Artikel, Modelle, Messwerkzeuge und lokaler Arbeitsstand |
| `browser-design/` | Kotlin-Styles, Schriftwahl und typisierte DOM-Kennungen; erzeugt `app.css` |
| `browser-bootstrap/` | Frühe Theme- und Bewegungseinstellung; erzeugt `browser-init.js` |
| [physics-core](../../packages/physics-core/) | UI-unabhängige Physik, Datenprüfung und Zustandsmodelle |
| [content](../../content/) | Versuchsdaten, Quellenbild, Messprofile, Typst-Formeln, Handout und Tutorinhalt |
| `scripts/` | Assetgeneratoren, Export und Browserprüfungen |

`site/build/generated/materiewelle-product/public/` enthält ausschließlich
generierte Produktdateien. `scripts/product_assets.py` legt fest, welche Dateien
in den statischen Export gelangen. Die fachlichen Daten werden beim Build samt
Bild- und Skriptprüfsummen validiert. Eingabedateien werden nicht als zusätzliche
Testkopien gepflegt.

Die Bibliotheksversionen stehen in `gradle/libs.versions.toml`: Kotlin 2.2.20,
Compose HTML 1.8.0, Kobweb 0.23.3 und Serialization 1.9.0.

## Prüfungen

Rechenmodelle und UI-Zustände lassen sich ohne Browser prüfen:

```bash
apps/web/run-gradle.sh :physics-core:jvmTest :physics-core:jsNodeTest :site:jsNodeTest :browser-design:jsNodeTest
python3 apps/web/scripts/check-ui-contract.py
python3 apps/web/scripts/test-ui-contract.py
python3 apps/web/scripts/test-math-text-build.py
```

Die [Browsermatrix](../../.github/scripts/BROWSER-CHECKS.md) prüft sechs aktuelle
Suiten: Hauptfunktionen, UI-Details, DG-Lernweg, Schreibberechtigung des
Arbeitsstands, Jönsson-Messplatz und persönlichen Ergebnisbericht. Sie bindet
Prüfberichte und Screenshots an die tatsächlich ausgelieferten Dateien.

Ein eigener Arbeitsstand wird lokal im Browser gespeichert. Tutorhilfe und
Ergebnisbericht lassen sich als Text kopieren; das Kopieren löscht den
Arbeitsstand nicht. Beispieldaten der Browserprüfungen sind synthetisch. Eine
bestandene Chromium-Prüfung belegt keine Prüfung auf echten Touchgeräten oder
mit einem Screenreader.
