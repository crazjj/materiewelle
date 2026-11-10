# Produkt-Handout

[handout.typ](handout.typ) erzeugt Handout **v1.7, Stand 10.11.2026**:
drei A4-Seiten einschließlich Quellen, ohne Deckblatt.

1. De Broglie: historische Einordnung, Herleitung aus Beschleunigung und Impuls,
   Kontrollrechnung bei 150 V und Vergleich mit 600 V.
2. Davisson–Germer: eigenes Aufbauschema und Bragg-Konstruktion; separat erklärter
   D2-Oberflächenfall bei 54 eV und etwa 50°.
3. Jönsson: eigene Spalt-/Streifenskizze, Lichtvergleich, relatives Pixelreferenzbeispiel, Messgrenzen,
   getrennter relativistischer 50-kV-Modellfall und gemeinsame Schlusskette.

Der Seitensatz verwendet Latin Modern, 11 pt im Text und mindestens 9,5 pt für
Quellen, Beschriftungen, Kopf und Fuß. Mathematische Indizes werden typografisch
verkleinert. Farbige Formelgrößen tragen zusätzlich ihre normalen Formelzeichen.

Das Handout enthält allgemeine Muster- und Referenzwerte. Seine Arbeitsaufträge
laden Besucher der Website zur eigenen Bearbeitung ein. Die persönliche
Belegauswahl des Autors für die Vorstellung ist davon zu unterscheiden;
[FACHGESPRAECH.md](../../docs/FACHGESPRAECH.md) beschreibt einen möglichen Weg.

## Bauen und prüfen

Vom Repository-Stamm aus:

```sh
python3 content/typst/build-handout.py --output-dir /tmp/materiewelle-handout
"${TASK_TOOLS_ROOT:-${XDG_CACHE_HOME:-$HOME/.cache}/materiewelle-tools}/typst/bin/python" content/typst/test-handout.py
```

Erforderlich sind Python 3, `typst==0.14.2` aus
[requirements.txt](requirements.txt), Poppler (`pdfinfo`, `pdffonts`, `pdftotext`)
und DejaVu Sans für die eigene Vektorgrafik. Latin Modern wird aus den
[Repository-Schriften](../../apps/web/site/src/jsMain/resources/public/fonts)
geladen. Die PDF-Geometrieprüfungen benötigen außerdem `PyMuPDF==1.26.6` aus
[tools/browser/requirements.txt](../../tools/browser/requirements.txt).

Falls das aufrufende Python kein Typst enthält, sucht der Builder unter
`TASK_TOOLS_ROOT/typst/bin/python`. Ohne diese Variable gilt
`${XDG_CACHE_HOME:-$HOME/.cache}/materiewelle-tools/typst/bin/python`.
Die gepinnte Python-Bindung 0.14.2 enthält den Compiler Typst 0.14.0;
Paketversion und tatsächlicher PDF-Creator stehen getrennt im Manifest.

Der Ausgabeort darf frisch, leer oder bereits bytegleich erzeugt sein und enthält
anschließend `materiewellen.pdf` und `manifest.json`. Vorhandene andere Dateien
werden nicht entfernt. Ausschließlich eigene, unveränderte Buildartefakte lassen
sich mit diesem Aufruf aktualisieren:

```sh
python3 content/typst/build-handout.py --output-dir /tmp/materiewelle-handout --replace-generated
```

Das Manifest dokumentiert Quellenhashes, tatsächliche Seitenzahl, Papierformat,
eingebettete Schriften und unabhängig in Python berechnete Kontrollwerte. Diese
werden mit dem extrahierten PDF-Text verglichen. Das PDF enthält kein automatisch
wechselndes Erstellungsdatum; gleiche Quellen und lokale Toolchain erzeugen
bytegleiche Ergebnisse.

Die elf Tests prüfen echte PDF-Builds, Reproduzierbarkeit, Quellhashes,
Kontrollwerte, Seitenbegrenzungen und den Schutz fremder bzw. veränderter Dateien.
Die Bragg-Grafik wird anhand tatsächlicher PDF-Textkästen und Vektorlinien
geprüft: mindestens 1,5 pt Abstand zu Strahlen, Ebenen, Pfeilspitzen und
Zuordnungslinien; Schriftgröße und abgesenkter Winkelindex; ausgerichtete
Bildunterschriften und Punktdefinitionen.

## Fachliche Grenzen

Die Zahlengrundlage ist [content/data/versuchsdaten.json](../data/versuchsdaten.json),
Schema 2.0. [fachwerte.typ](fachwerte.typ) berechnet die angezeigten Werte.
Der Builder prüft zusätzlich den Hash des nativen
[Jönsson-Referenzbildes](../sources/jonsson-page6-xref33.png).
Historische Befunde, gesetzte Strukturwerte, Modellfälle und dokumentierte
Pixelreferenzmarken bleiben getrennt. Aus den Pixelabständen wird keine absolute
Wellenlänge berechnet. D1 wurde im Projekt nicht eingesehen; die D2-Angabe ersetzt
keine originalgeprüfte Messreihe oder Statistik.

Die Schemata sind eigene Projektgrafiken. Das Bragg-SVG verwendet eine exakte
30°-Konstruktion mit freigestellten Punktbezeichnungen, kurzen Zuordnungslinien
und Lotmarkierungen. Seine Hauptbeschriftungen erscheinen mit etwa 10,59 pt;
das B in θ_B ist ein verkleinerter Index. Der Oberflächenfall mit D und φ wird
getrennt behandelt.

Die Punktnamen stimmen mit der Website überein: A/B sind Streupunkte, P/Q die
Lotfußpunkte; PB und BQ sind die Zusatzwege. Im DG-Aufbauschema markiert ein
Winkelbogen den Bezug zur äußeren Normalen. Der Übergang zur Oberflächenrechnung
wird ausdrücklich erklärt.

Das Jönsson-Schema bezeichnet Spaltbreite, Mittenabstand und Streifenabstand.
Sein Grauverlauf wird aus einem idealisierten endlichen Doppelspaltmodell
berechnet und als durchgehender SVG-Farbverlauf gesetzt. Er besitzt keinen
Maßstab für das historische Bild. Die zusammenhängende Fläche vermeidet feine
Rendernähte zwischen einzelnen Helligkeitsstreifen.

Historische Fotos werden nicht im PDF eingebettet, weil ihre öffentliche
Nutzungsgrundlage im Projekt offen ist. J1 wird als gekürzte englische Übersetzung
von 1974 bezeichnet. Eine eigene Messentscheidung und ein persönliches Fazit
werden nicht vorgetäuscht. Automatische Prüfungen ersetzen weder einen echten
Ausdruck noch eine unabhängige menschliche Schlusskorrektur.
