package materiewelle.course

import androidx.compose.runtime.Composable
import materiewelle.core.Experiment
import materiewelle.core.ScientificData
import materiewelle.core.course.*
import materiewelle.ui.MathText
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.dom.*

/** Four read-only rows from the same projection as the personal report, never a grade. */
@Composable
fun OwnWorkSummary(snapshot: SessionSnapshot, data: ScientificData?, onOpenWork: (AppRoute, ElementId) -> Unit) {
    val entries = snapshot.personalEvidenceReport(data).ownWorkPresence()
    Section(attrs = { elementId(OwnWorkId.ROOT); ownWorkStyles(OwnWorkClass.ROOT); labelledBy(OwnWorkId.TITLE) }) {
        H3(attrs = { elementId(OwnWorkId.TITLE) }) { Text("Deine Arbeit im Überblick") }
        P { Text("Was ist schon eingetragen, was fehlt noch? Freie Antworten werden nicht automatisch bewertet.") }
        Ul(attrs = { ownWorkStyles(OwnWorkClass.LIST) }) {
            entries.forEach { entry ->
                val destination = entry.destination()
                Li(attrs = { elementId(OwnWorkElement(entry.section, OwnWorkPart.ROW)); ownWorkStyles(OwnWorkClass.ROW) }) {
                    A(href = destination.first.hash, attrs = {
                        elementId(OwnWorkElement(entry.section, OwnWorkPart.LINK)); ownWorkStyles(OwnWorkClass.LINK)
                        onClick { event -> event.preventDefault(); onOpenWork(destination.first, destination.second) }
                    }) { Text(entry.section.label()) }
                    Div(attrs = { ownWorkStyles(OwnWorkClass.BODY) }) {
                        Span(attrs = { elementId(OwnWorkElement(entry.section, OwnWorkPart.STATUS)); ownWorkStyles(OwnWorkClass.STATUS) }) { Text(entry.presence.label()) }
                        Div(attrs = { elementId(OwnWorkElement(entry.section, OwnWorkPart.DETAILS)) }) {
                            if (entry.missing.isNotEmpty()) P(attrs = { ownWorkStyles(OwnWorkClass.DETAIL) }) {
                                Text("Es fehlen: "); MathText(entry.missing.joinToString { it.label() }); Text(".")
                            }
                            entry.notices.forEach { notice -> P(attrs = { ownWorkStyles(OwnWorkClass.DETAIL) }) { Text(notice.label()) } }
                            entry.numericFeedback?.takeIf { it.status != NumericAnswerStatus.CORRECT }?.let { feedback ->
                                P(attrs = { ownWorkStyles(OwnWorkClass.DETAIL) }) {
                                    Text("Zahlenprüfung: "); MathText(feedback.feedback)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

internal fun OwnWorkEntry.destination(): Pair<AppRoute, ElementId> = when (section) {
    OwnWorkSection.CALCULATION -> AppRoute.Course(StationId.S06_CALCULATION) to Id.COURSE_TASK_ANSWER
    OwnWorkSection.DG -> AppRoute.DgLesson(DgLessonStep.CALCULATION) to DgLessonId.PREDICTION_INPUT
    OwnWorkSection.MEASUREMENT -> AppRoute.Laboratory(Experiment.JONSSON, LabFocus.MEASUREMENT) to
        if (OwnWorkField.MEASUREMENT_MARKS !in missing && OwnWorkField.MEASUREMENT_REASON in missing)
            Id.JONSSON_JUSTIFICATION else Id.JONSSON_INTERACTIVE_IMAGE
    OwnWorkSection.CONCLUSION -> AppRoute.Course(StationId.S16_CONCLUSION) to ConclusionId.DRAFT
}

private fun OwnWorkSection.label(): String = when (this) {
    OwnWorkSection.CALCULATION -> "Eigene Rechnung"
    OwnWorkSection.DG -> "DG-Vergleich"
    OwnWorkSection.MEASUREMENT -> "Bildmessung"
    OwnWorkSection.CONCLUSION -> "Fazit"
}

private fun OwnWorkPresence.label(): String = when (this) {
    OwnWorkPresence.OPEN -> "Noch offen"
    OwnWorkPresence.PARTIAL -> "Teilweise eingetragen"
    OwnWorkPresence.PRESENT -> "Angaben vorhanden"
}

private fun OwnWorkField.label(): String = when (this) {
    OwnWorkField.CALCULATION_ANSWER -> "Ergebnis für 150 V"
    OwnWorkField.CALCULATION_NOTE -> "Rechenweg"
    OwnWorkField.DG_PREDICTION -> "eigene Vorhersage"
    OwnWorkField.DG_GEOMETRY -> "geometrischer Rechenwert"
    OwnWorkField.DG_INTERPRETATION -> "eigene Einordnung"
    OwnWorkField.MEASUREMENT_MARKS -> "mindestens zwei eigene Marken"
    OwnWorkField.MEASUREMENT_REASON -> "Begründung zum aktuellen Band"
    OwnWorkField.MEASUREMENT_BINDING -> "geprüfte Bildzuordnung"
    OwnWorkField.CONCLUSION -> "eigener Fazittext"
}

private fun OwnWorkNotice.label(): String = when (this) {
    OwnWorkNotice.IMAGE_UNCHECKED -> "Die Bildzuordnung ist noch ungeprüft. Deine Angaben bleiben erhalten."
    OwnWorkNotice.IMAGE_MISMATCH -> "Die gespeicherten Marken gehören zu einem anderen Bild."
    OwnWorkNotice.IMAGE_MISSING -> "Der Auswahl ist derzeit kein Bild zugeordnet."
    OwnWorkNotice.REASON_OTHER_BAND -> "Die Begründung gehört zum früheren Band; prüfe sie für das aktuelle Band."
    OwnWorkNotice.SELECTION_NOT_SEALED -> "Deine Auswahl ist ein Entwurf; eine erste Auswahl wurde noch nicht festgehalten."
    OwnWorkNotice.SAVED_SELECTION_EXISTS -> "Die aktuelle Auswahl ist leer. Deine früher festgehaltene Auswahl bleibt im Bericht."
    OwnWorkNotice.SAVED_COMPARISON_EXISTS -> "Die aktuelle Auswahl ist leer. Deine gespeicherte Vergleichsmessung bleibt im Bericht."
    OwnWorkNotice.NEEDS_SECOND_MARKER -> "Eine zweite Marke ist für einen Abstand nötig."
}
