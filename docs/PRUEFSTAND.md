# Prüfstand · 10. November 2026

Geprüft wurde der aktuelle **lokale statische Root-Export**. Die Website wird im
Browser gerendert; dies ist kein Nachweis vorgerenderter Seiten oder einer
aktuellen Vercel-Liverevision. Maschinenlesbare Werte und Quellbindungen stehen
in [pruefstand.json](pruefstand.json).

| Tatsächlich ausgeführte Prüfung | Ergebnis |
|---|---|
| Kotlin: JVM, JavaScript, Browserdesign und Website | 572 Testausführungen, keine Fehler oder übersprungenen Tests. |
| Python: Generatoren, Import-/UI-Verträge und Paketierung | 133 Tests aus 15 Skripten bestanden. |
| Statischer Produktionsbuild | 320 Quellinputs und alle 1333 Exportdateien über SHA-256 gebunden und geprüft. |
| Vollständige Browsermatrix | Alle sechs Suiten bestanden: 28 Szenarien, 1779 Checks, 170 Screenshots. |
| SVG-Textabstände | 80 Zustände und 2896 sichtbare Beschriftungsprüfungen bei 1080×810 und 1366×900, Hell/Dunkel; keine Überläufe, falschen Textvorschübe oder Konsolenfehler. |
| Handout | Bisherige Quelle und PDF unverändert; beide Vorschauen haben drei A4-Seiten, eingebettete Schriften und bytegleiche Wiederholungsbuilds. Alle sechs Vorschauseiten visuell angesehen. |
| README-Galerie | Aktuelle Chromium-Aufnahmen; keine persönliche Arbeit als Leistung eines Schülers dargestellt. |

Die Browserprüfung umfasst Navigation und Scrollmarkierung, Zustandserhalt,
Schreibschutz zwischen Tabs, ungültige Daten, Theme und reduzierte Bewegung,
Bildmarken in Originalpixeln, Zoomunabhängigkeit, Referenzfreigabe, erhaltene
Modellvergleiche beim Ausblenden sowie echte Zwischenablage- und Dateiausgaben.
Technische Berichtsanhänge sind nur nach ausdrücklicher Auswahl enthalten.
Ein fehlender Quelldatensatz führt zu einer ehrlich bezeichneten Rettungskopie.

Die zusätzliche Textabstandsprüfung setzt auch nachträglich größere Wort- und
Buchstabenabstände. Rein rechteckige Begrenzungsboxen melden an der
Streifenabstandsformel Kontakte zur Bildunterschrift; die sichtgeprüften Glyphen
überschneiden sich dort nicht. Das ist keine vollständige Screenreader-Abnahme.

Der neue Jönsson-Arbeitsbereich wurde zusätzlich in beiden Themes bei 1080×810
und 1366×900 angesehen. Ein nachgewiesener Überlauf am Marken-Entfernen-Button
wurde behoben und mit derselben strengen Browserprüfung nachgeprüft. Die
Zielgruppe nutzt Laptop oder iPad im Querformat; vertikales Scrollen bleibt Teil
der Bedienung.

## Noch offen oder bewusst zurückgestellt

- Auswahl zwischen [Handout A und B](handout-varianten/README.md). Das bisherige
  [Handout](handout.pdf) bleibt bis dahin die veröffentlichte Fassung.
- Echtes iPad 9 mit Safari, Screenreader und zwei menschliche Probedurchgänge.
  Chromium mit Touch-Emulation ersetzt diese Prüfungen nicht.
- Öffentliche Nutzungsgrundlage des historischen Bildes; siehe [Quellennachweis](../content/sources/README.md#j1-image).
  Die begrenzte Nachrecherche brachte keine neue belastbare Freigabe oder historische Messreihe.
- Die zusätzliche Fehlerisolation beim Ausfall des Typst-Katalogs wurde wie
  vereinbart nicht umgebaut; der bestehende Rettungsweg bleibt geprüft.
- Das JavaScript-Bündel ist unkomprimiert rund 2,18 MiB groß. Die Webpack-Warnung
  zur Bündelgröße bleibt bestehen; echte Ladezeiten auf dem Zielgerät sind separat zu prüfen.
- Kein Vercel-Deployment durch diesen Lauf und keine bestätigte öffentliche
  Revision. In dieser Umgebung fehlen Vercel-Zugang und Hostfreigabe.

GitHub Actions sind abgeschaltet; die bisherigen Workflows sind archiviert.
Lokale Start-, Prüf- und Auslieferungsschritte stehen in [START.md](START.md).
Frühere Prüfnachweise bleiben im [Archiv](../archive/reviews/) erhalten. Die
zusätzliche Autoren-/Vortragsvorbereitung gehört nicht zur Abgabe.
