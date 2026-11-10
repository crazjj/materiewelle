package materiewelle.pages

import androidx.compose.runtime.Composable
import materiewelle.JonssonData
import materiewelle.data.LabAsset
import materiewelle.ui.MathText
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.dom.*

@Composable
internal fun JonssonSources(data: JonssonData, referenceViewed: Boolean) {
    Details(attrs = { styles(LabClass.NOTES) }) {
        Summary() { MathText("Aufnahme, Messverfahren und Quellen") }
        P(attrs = { styles(LabClass.SOURCE_WARNING) }) { MathText(data.warning) }
        P {
            MathText(
                "Quelle: Jönsson, Electron Diffraction at Multiple Slits, American Journal of Physics 42 (1974), S. 9, Abb. 8; gekürzte englische Fassung der Arbeit von 1961. Das Raster wurde unverändert aus PDF-Seite 6, Bildobjekt 33 extrahiert; links liegt Abb. 7 (Einspalt)."
            )
        }
        P {
            MathText(
                "Die acht Referenzmarken stammen aus einer dokumentierten heutigen Auswertung: mittlerer Grauwert über das gewählte y-Band, je ein Maximum in acht getrennten Suchfenstern zwischen x = 545 und 654. Basis: 100 ≤ y < 220; oben: 20 ≤ y < 45; unten: 145 ≤ y < 220. Die Profilpunkte sind die aus dem Originalraster vorberechneten Grauwerte; Dateiprüfsumme und native Dimensionen werden beim Import mit dem Bilddatensatz abgeglichen. Die Bänder sind keine unabhängigen Wiederholungsmessungen."
            )
        }
        if (referenceViewed)
            P {
                MathText(
                    "Basisbandbeispiel (100 ≤ y < 220), unabhängig von der aktuell gewählten Bandreferenz; x / px: " +
                        data.markers.joinToString(", ") { it.toInt().toString() } +
                        ". Dokumentierte heutige Referenzauswahl, keine unverarbeiteten historischen Rohdaten."
                )
            }
        P {
            MathText(
                "Eigene Marken starten leer und sind ganzzahlige native Pixelkoordinaten. Klick neben vorhandene Marken ergänzt eine Marke, Ziehen verschiebt sie; sortierte Marken dürfen sich nicht kreuzen. Hinzugefügte Marken sind manuelle Kandidaten, keine automatisch nachgewiesenen Maxima."
            )
        }
        P {
            MathText(
                "Ein Grauwertprofil ist keine kalibrierte Elektronenintensität: Aufnahme, Entwicklung und Reproduktion verändern die Abbildung. Die eigene Prüfung des Schülers bleibt erforderlich. Ohne passenden Bildmaßstab darf keine absolute Wellenlänge aus den Pixeln abgeleitet werden."
            )
        }
        P {
            MathText(
                "Der Text der Originalübersetzung nennt 50 kV; Abb. 6 beschriftet die Skizze mit 40 kV. Der Relativitätsvergleich dieses Kapitels rechnet gesondert mit 50 kV, das veränderbare Spaltmodell mit 150 V. Spannung und Vergrößerung genau dieser Aufnahme sind damit nicht nachgewiesen."
            )
        }
        if (referenceViewed)
            P {
                A(href = LabAsset.MEASUREMENT_PROFILES.path, attrs = { externalLink() }) {
                    MathText("Abgeleitete Methodenprofile und Quellnachweis")
                }
            }
        else
            P {
                MathText(
                    "Die Tabelle der Referenzkoordinaten wird nach gesicherter eigener Erstwahl und bewusstem Referenzvergleich verlinkt. Die historische Originalquelle bleibt unabhängig davon erreichbar."
                )
            }
        A(href = "https://doi.org/10.1119/1.1987592", attrs = { externalLink() }) {
            MathText("Originalübersetzung: DOI 10.1119/1.1987592")
        }
    }
}
