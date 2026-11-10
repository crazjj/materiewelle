# Relative Bildmessung und Methodenprüfung

[sensitivitaet.py](sensitivitaet.py) reproduziert die Streifenlagen aus dem
unveränderten [Jönsson-Raster](../sources/jonsson-page6-xref33.png). Der
[Quellennachweis](../sources/README.md#j1-image) beschreibt Herkunft, ursprünglichen
Prüfumfang und offene Nutzungsgrundlage. Die historische Aufnahme ist keine eigene
Messung der Lernenden; die dokumentierte Auswertung wurde im Ausgangsprojekt von
KI-Agenten vorgenommen.

## Datenvertrag und Verfahren

[joensson_sensitivitaet.json](joensson_sensitivitaet.json) ist eine abgeleitete
Methodenprüfung, kein zweiter Kanon historischer Daten. Die Website verwendet die
stabilen Profil-IDs `basis`, `oben`, `unten`, ihre `band_y_px`, acht
`integer_markers_x_px` und `integer_summary`. Der Datenkanon liegt unter
[content/data](../data/README.md). Eigene Marken bleiben eine separate Arbeitskopie.

Das Raster hat 703 × 242 Pixel. Alle Koordinaten beginnen bei null links oben im
**gesamten** Raster; x zeigt nach rechts, y nach unten. Ausgewertet wird nur Abb. 8
rechts. Die Auswahl `[100,220)` umfasst die Zeilen 100 bis einschließlich 219.
Das Bild wird weder skaliert noch geglättet oder im Kontrast verändert.

Pro x-Spalte wird der arithmetische Grauwertmittelwert im gewählten Band gebildet.
Acht disjunkte Suchfenster `[545,558)`, `[558,572)`, `[572,586)`, `[586,600)`,
`[600,614)`, `[614,628)`, `[628,642)`, `[642,655)` liefern je ein ganzzahliges
Maximum; Randmaxima werden abgewiesen. Diese Fenster-Argmax-Methode reproduziert
die frühere, im privaten Projektarchiv dokumentierte `scipy.find_peaks`-Auswahl.
Die öffentliche Reproduktion benötigt dieses Archiv nicht.

| Band / native y-Pixel | Ganzzahlige x-Marken | Endpunktmittel / px | Deskriptive Abstands-SD / px |
|---|---|---:|---:|
| `basis` `[100,220)` | 551, 564, 578, 592, 606, 620, 634, 647 | 13,714286 | 0,487950 |
| `oben` `[20,45)` | 550, 564, 578, 591, 605, 619, 633, 647 | 13,857143 | 0,377964 |
| `unten` `[145,220)` | 551, 564, 579, 592, 606, 620, 634, 648 | 13,857143 | 0,690066 |

Acht Marken ergeben sieben Abstände. Für die Basis sind es
`[13,14,14,14,14,14,13]`; das Mittel ist `(647−551)/7 = 96/7` Pixel. Die
Nachkommastellen dokumentieren die Rechnung. Die angemessene Bildaussage lautet
ungefähr **14 native Pixel**. Die Standardabweichung beschreibt die Ungleichheit
der sieben Abstände, keine vollständige Messunsicherheit.

Die JSON-Datei enthält außerdem drei zusätzliche Kontrollbänder, lokale
Dreipunkt-Parabelinterpolation, eine lineare Anpassung, explizit synthetische
Markenänderungen, einen verlustfreien Ausschnitt und ein illustratives
Rundungsmodell. Die Parabelinterpolation behauptet keine höhere Kameraauflösung.
Für die drei Hauptbänder und beide Peakmethoden reicht das Endpunktmittel von
13,714286 bis 13,881456 Pixel. Das ist die Empfindlichkeit der gewählten Methoden
an einem Raster, kein Konfidenzintervall; überlappende Bänder sind keine
unabhängigen Wiederholungsmessungen.

Eine innere Markenverschiebung ändert zwei Nachbarabstände gegensinnig und lässt
das Endpunktmittel unverändert. Ein um einen Pixel verschobener Endpunkt ändert
es um `1/7` Pixel. Unter nur angenommenen unabhängigen Positionsfehlern gleicher
Varianz haben benachbarte Abstände die Korrelation `−1/2`; das Mittel hat
`σ = √2 · σ_Position / 7`. Dieses Rechenmodell ist **kein validiertes Fehlermodell
des Fotos**. Gemeinsamer Koordinatenversatz fällt heraus, unbekannter Maßstab nicht.

Digitale Grauwerte sind keine kalibrierte Elektronenintensität. Bildzoom und
CSS-Pixel sind keine nativen Messpixel. Ohne unabhängigen Meter-pro-Pixel-Maßstab
und belegte Zuordnung von Spannung und Elektronenoptik folgt keine absolute
Wellenlänge. Die separate 50-kV-Parameterrechnung ist eine Modellvorhersage.

## Reproduzieren

Aus dem Repository-Stamm, mit Python und den in
[requirements.txt](requirements.txt) festgehaltenen Paketen:

```bash
python3 content/measurement/sensitivitaet.py --output-dir /tmp/materiewelle-measurement
# Optional auch die erläuterten Profile und Modellvariationen als PNG/SVG:
python3 content/measurement/sensitivitaet.py --output-dir /tmp/materiewelle-measurement --figures
```

Das Skript prüft den Rasterhash und die Bildgröße, berechnet
`joensson_sensitivitaet.json`, `dg_sensitivitaet.json` und `pruefprotokoll.json` und
führt acht analytische Selbstkontrollen aus. Es greift nicht aufs Netzwerk zu.
Quellbildpfad, Skriptpfad und SHA-256 stehen im Profilvertrag. Bei einer Änderung
des Skripts muss der Profilvertrag neu erzeugt und vor der Übernahme geprüft
werden. Nur der Profilvertrag gehört zum öffentlichen Eingabestand; Diagramme
und zusätzliche Rechenprotokolle können im gewählten Ausgabeordner bleiben.

Die ergänzende DG-Variation behält den früheren Fall 42,5 V / 60° mit
`D = 0,215 nm` als **gewählte Modellrechnung** bei. Er stammt aus dem verworfenen
privaten Plan und ist keine historische Belegbasis. Variationen von ±0,5 V,
±1° und ±0,001 nm sind keine historischen Unsicherheiten. Der aktive
[D2-Sekundärbefund](../data/versuchsdaten.json) ist 54 eV / ungefähr 50° mit
unabhängigem Ni(111)-Strukturmodell. Die Selbstkontrollen prüfen unter anderem
die Abhängigkeit benachbarter Abstände, die analytische Winkelableitung und die
fehlende reelle inverse Lösung bei 32 V im ergänzenden strikten Modell. Sie
ersetzen weder Quellenprüfung noch unabhängiges Review.
