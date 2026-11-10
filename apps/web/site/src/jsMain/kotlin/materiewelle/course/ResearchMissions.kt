package materiewelle.course

import androidx.compose.runtime.Composable
import materiewelle.core.course.StationId
import materiewelle.ui.MathText as Text
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.dom.*

private data class ResearchMission(
    val id: ResearchMissionId,
    val title: String,
    val introduction: String,
    val steps: List<String>,
    val finish: String,
)

private val voltageMission =
    ResearchMission(
        ResearchMissionId.VOLTAGE,
        "Eine neue Spannung, drei begründete Faktoren",
        "Ein kurzer Zusatzauftrag mit den vorhandenen Rechenfeldern auf Station 6. Deine eigene 150-V-Rechnung bleibt der Ausgangspunkt; 600 V sind ein geführtes Übungsbeispiel.",
        listOf(
            "Halte deinen 150-V-Ansatz mit Einheiten fest. Benenne Start aus Ruhe und die nichtrelativistische Näherung.",
            "Wähle einen noch ungelösten Fall: 240 V oder später 375 V. Notiere vor dem Einsetzen, wie sich Energie, Impuls und Wellenlänge gegenüber 150 V ändern müssten.",
            "Rechne mit denselben Beziehungen und nutze die Zahlenprüfung. Passt etwas nicht, prüfe zuerst Energiebilanz, Wurzel und Einheit; Hilfe darfst du bewusst öffnen.",
        ),
        "Erkläre danach einen der drei Faktoren ohne Vorlage. Ein abweichender erster Versuch ist ein guter Anlass für die Gegenprobe; eine passende Zahl allein bewertet deine Erklärung nicht.",
    )

private val angleMission =
    ResearchMission(
        ResearchMissionId.ANGLE,
        "Dem passenden Winkel auf der Spur",
        "Nutze die Konstruktion auf Station 10 und den Quellenvergleich auf Station 11. Welche Bezugsrichtung und welcher Strukturabstand gehören jeweils zur Auswertung?",
        listOf(
            "Zeige in der Bragg-Zeichnung Ebene, Normale und beide Zusatzwege. Sage vor der Summenkarte voraus, welche Projektionen zusammengezählt werden.",
            "Wechsle zur Oberfläche. Erkläre bei normalem Einfall neu, wo der Gangunterschied entsteht und worauf sich φ bezieht. Die gespeicherte Bragg-Vorhersage ist dafür keine zweite Antwort.",
            "Verfolge auf Station 11 getrennt U → Impuls → λ und a, D, φ → λ. Sage zu jeder Eingabe, woher sie stammt. Eine eigene Winkelvariation im Labor ist eine Modell-Gegenprobe.",
        ),
        "Zeichne einen neuen Winkelbezug auf Papier und begründe die Umrechnung an den Linien. Winkel raten darf die Skizze nicht ersetzen – sie ist hier der bessere Spickzettel.",
    )

private val markerMission =
    ResearchMission(
        ResearchMissionId.MARKERS,
        "Was bewegt eine verschobene Marke?",
        "Prüfe nach deiner eigenen Bildmessung, ob der Mittelwert jede Änderung der Markensetzung sichtbar macht. Die Gegenprobe ist freiwillig.",
        listOf(
            "Sichere zuerst deine eigene Erstwahl mit Band und Begründung. Halte für die Gegenprobe einen eigenen Vergleich A fest; arbeite danach im veränderbaren Stand B.",
            "Sage die Wirkung voraus und verschiebe eine innere Marke um einen nativen Pixel. Halte die Endmarken fest. Vergleiche Bild, die zwei benachbarten Einzelabstände und das Mittel.",
            "Setze die Marke zurück und verschiebe nur eine Endmarke um einen Pixel. Notiere wieder zuerst deine Vorhersage. Dokumentiere, was sich nun ändert und welche Marken weiterhin zu den Maxima passen.",
        ),
        "Erkläre den Unterschied der beiden Gegenproben mit deinen Abständen. Ein unauffälliger Mittelwert ist kein Gütesiegel für die Maximalwahl; die Pixel bekommen durch diesen Trick auch noch keinen Metermaßstab.",
    )

/** Three optional prompts reuse the existing tools and notes, without a second progress system. */
@Composable
internal fun ResearchMissionPanel(station: StationId) {
    val mission =
        when (station) {
            StationId.S05_DERIVATION,
            StationId.S06_CALCULATION -> voltageMission
            StationId.S10_GEOMETRY,
            StationId.S11_DG_EVALUATION -> angleMission
            StationId.S14_MEASUREMENT -> markerMission
            else -> return
        }
    Details(
        attrs = {
            elementId(mission.id)
            styles(LabClass.NOTES)
            learningFlowStyles(LearningFlowClass.MISSION)
        }
    ) {
        Summary { Text("Freiwillig forschen · ${mission.title}") }
        Div(attrs = { learningFlowStyles(LearningFlowClass.MISSION_BODY) }) {
            P(attrs = { learningFlowStyles(LearningFlowClass.MISSION_KICKER) }) {
                Text("VORHERSAGEN · GEGENPROBE · SELBST ERKLÄREN")
            }
            P { Text(mission.introduction) }
            Ol(attrs = { learningFlowStyles(LearningFlowClass.MISSION_STEPS) }) {
                mission.steps.forEach { step -> Li { Text(step) } }
            }
            P(attrs = { learningFlowStyles(LearningFlowClass.MISSION_FINISH) }) {
                Text(mission.finish)
            }
            P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
                Text(
                    "Halte deine Vermutung und ihre Korrektur in den Notizen fest. Hilfe steht jederzeit bereit; prüfe deine Erklärung anschließend mit den Kriterien."
                )
            }
        }
    }
}
