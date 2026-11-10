# Gemeinsamer Kotlin-Kern

`materiewelle.core` enthält Physik, native Pixelmessung, typisierte Dateneinlesung und kleine Zustandsmodelle für Kobweb. Das Modul importiert weder DOM/Canvas noch Compose oder Kobweb. JVM für die Rechen-/Importtests und Kotlin/JS für die App verwenden denselben `commonMain`-Quelltext.

Das [Kobweb-Gradle-Projekt](../../apps/web/) bindet dieses Modul als `:physics-core` ein. Kotlin **2.2.20** und der passende Serialization-Compiler werden im Root-Projekt geladen; `kotlinx.serialization-json` ist auf **1.9.0** festgelegt. Das Modul ist kein eigenständiges Root-Projekt. Die JVM-Zielversion ist **Java 17**, das Buildwerkzeug bleibt JDK 21.

## Zuständigkeiten

| Datei | Inhalt |
|---|---|
| `Quantities.kt` | SI-Längen, Spannungen, Winkel in Grad, native Pixelkoordinaten/-abstände; Experiment, Modus, Ladezustand, Eingabetext/validierter Wert |
| `Physics.kt` | De-Broglie-Modell, relativistische Korrektur, Oberflächenbeugung erster Ordnung, unabhängige Streifenabstandsrechnung |
| `PixelMeasurement.kt` | Geordnete native Marken, Abstände und kontrollierte Verschiebung ohne Bildschirmmaßstab |
| `ScientificData.kt` | Serialization-DTOs und fachliche Validierung der unveränderten Versuchsdaten-Spezifikation |
| `MeasurementProfiles.kt` | Herkunftsgebundene native Grauwertprofile und Referenzmarken aus der vorhandenen Methodenrechnung |
| `StrictNumbers.kt` | Zahlen bleiben JSON-Zahlen: keine stillschweigende Umwandlung von Text zu Zahlen |

Die bisherigen `materiewelle.Physics`- und Datenklassennamen sind kleine `typealias`-Adapter. Die App verwendet damit denselben geprüften Berechnungskern. Die bestehenden `Double`-Zugriffe bleiben für Darstellungscode verfügbar; neue Berechnungen können `Voltage`, `Length` und `Angle` verwenden. Eine `Length` ist ausdrücklich kein `NativePixel`.

## Prüfung

Vom Repository-Root:

```bash
apps/web/run-gradle.sh \
  :physics-core:jvmTest :physics-core:jsNodeTest
```

Die JVM-Importprüfungen lesen die tatsächlichen kanonischen Dateien, statt sie als zweite Testkopie zu pflegen. Die numerischen Referenzwerte stammen aus einer unabhängig gerechneten Decimal-Referenz. Zielkompilierung allein ist kein Browser- oder Gerätetest; diese Prüfung gehört zum jeweiligen UI-Projekt.

## Fachliche Grenzen

- Die aktiven DG-Werte sind als geprüfte Sekundärdarstellung gekennzeichnet; sie werden nicht als verifizierte Originalmessdaten ausgegeben. Erfolgreiche Deserialisierung ersetzt keine Quellenprüfung.
- Das Jönsson-Grauwertprofil ist der dokumentierte arithmetische Mittelwert unveränderter Bildgrauwerte im angegebenen nativen y-Band. Es ist keine kalibrierte Elektronenintensität.
- Der gemeinsame Kern bietet keine Umrechnung nativer Bildpixel in physikalische Längen an. Die 50-kV-Modellrechnung bleibt unabhängig vom historischen Raster.
- Neue, bislang nicht verwendete Metadatenfelder der Datenspezifikation werden toleriert; für alle verwendeten Felder bleiben Typ-, Bereichs-, Ordnungs- und Herkunftsprüfungen aktiv.
