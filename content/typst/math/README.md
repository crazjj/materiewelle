# Typst-Formeln für die Weboberfläche

Die 25 Formeln in [formulas.json](formulas.json) werden mit der gepinnten
Typst-Bindung 0.14.2 (Compiler 0.14.0) als **50 eigenständige SVGs** gebaut:
je eine helle und dunkle Fassung. Die Formeln besitzen transparente Flächen,
einen engen 2-pt-Rand und gesetzte Brüche, Wurzeln und Indizes.

Die Farben folgen der Weboberfläche: Energie in Amber, Wellenlänge in Cyan,
Impuls in Violett. Namen und Größen stehen zusätzlich in den deutschen
Textalternativen. Farbe trägt keine fachliche Aussage allein.

## Build und Schnittstelle

Vom Repository-Stamm aus:

```sh
python3 content/typst/math/build.py
python3 content/typst/math/build.py --output-dir /tmp/materiewelle-math
# Zusätzlich Rasteransichten und eine Vergleichstafel erzeugen:
python3 content/typst/math/build.py --output-dir /tmp/materiewelle-math --previews
```

Die Standardausgabe liegt unter
`apps/web/site/build/generated/materiewelle-product/public/math`.
`--output-dir DIR` erlaubt eine getrennte Buildausgabe. Der Interpreter wird über
`TYPST_PYTHON` oder `TASK_TOOLS_ROOT/typst/bin/python` aufgelöst. Ohne diese
Variablen gilt `${XDG_CACHE_HOME:-$HOME/.cache}/materiewelle-tools/typst/bin/python`.
Die Bindung ist in [requirements.txt](../requirements.txt) gepinnt. Die Option
`--previews` benötigt zusätzlich Pillow aus
[tools/browser/requirements.txt](../../../tools/browser/requirements.txt).

- SVG-Dateien: `{id}-light.svg` und `{id}-dark.svg` im Ausgabeordner.
- Manifest: `manifest.json`, Schema 1; Webpfade beginnen mit `math/`.
- Jeder Eintrag enthält `id`, `alt`, `roles`, `light`, `dark`, `width`, `height`,
  `source_sha256` und SHA-256 beider erzeugten SVGs.
- Maße sind CSS-Pixel bei 96 dpi, aus der SVG-ViewBox umgerechnet. Hell und Dunkel
  haben identische Maße; alle Formeln bleiben innerhalb von 600 × 130 CSS-Pixeln.
- `annotations` nennt Rollen, deutsche Bezeichnungen und die Themenfarben.
- Glyphen-IDs und Verweise erhalten einen Namensraum aus Formel-ID und Thema.
- Optionale PNGs und die Vergleichstafel liegen unter `previews/` im Ausgabeordner.

Build-Eingaben sind [build.py](build.py), [inline.typ](inline.typ),
[formulas.json](formulas.json), [vorlage.typ](../vorlage.typ),
[auswahl.json](../auswahl.json) und die Schriften unter
`apps/web/site/src/jsMain/resources/public/fonts`. Generierte Manifeste und
Vorschaubilder werden ausschließlich in den Ausgabeordner geschrieben.

## Verfügbare Schlüssel

| Bereich | IDs |
|---|---|
| Energie und Impuls | `energy-gain`, `kinetic-momentum`, `kinetic-balance`, `momentum` |
| De Broglie | `de-broglie`, `wavelength-voltage`, `scaling` |
| Bragg und Oberfläche | `bragg-leg`, `bragg-sine`, `bragg`, `surface`, `surface-spacing` |
| Geometriebezeichnungen | `geometry-point-a`, `geometry-point-p`, `geometry-segment-ab`, `geometry-segment-pb` |
| Doppelspalt und Hülle | `fringe-spacing`, `envelope` |
| Relativistische Korrektur | `relativistic` |
| Hervorhebungen der Bragg-Geometrie | `bragg-focus-angle`, `bragg-focus-spacing`, `bragg-focus-path` |
| Hervorhebungen der Oberfläche | `surface-focus-angle`, `surface-focus-spacing`, `surface-focus-path` |

`formulas.json` ist ein kontrollierter Quellenkatalog. Typst `eval` verarbeitet
beim Build ausschließlich diese lokalen, versionierten Formeln. Die numerischen
Auswertungen bleiben im Physikkern. Energie und Impuls werden nichtrelativistisch
verknüpft; Bragg und Oberfläche haben unterschiedliche Winkelbezüge. Die
Streifenformel verlangt die jeweils erklärte Fraunhofer-Geometrie, die
Hüllenformel bezeichnet das erste Einspaltminimum.

## Prüfungen im Builder

Jede Formel muss ohne Compilerwarnung genau eine SVG-Seite erzeugen. Der Builder
prüft eindeutige Schlüssel, identische Themenmaße, kompakte Größen, auflösbare
lokale Glyphenverweise und eindeutige IDs über alle SVGs hinweg. Skripte,
Fremdobjekte und eingebettete Bilder sind ausgeschlossen. Hervorgehobene Formeln
müssen dieselben Maße wie ihre Grundform haben. Die Integration sowie Tastatur-
und Layoutprüfungen der Website erfolgen separat.
