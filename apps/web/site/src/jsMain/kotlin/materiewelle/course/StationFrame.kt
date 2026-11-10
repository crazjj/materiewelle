package materiewelle.course

import androidx.compose.runtime.Composable
import materiewelle.core.*
import materiewelle.core.course.StationId
import materiewelle.state.LabSession
import materiewelle.ui.MathText as Text
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.dom.*

/** Concept help and argument criteria stay separate from worked numerical solutions. */
@Composable
internal fun StationGuidancePanel(id: StationId) {
    val guidance = StationGuidances[id]
    Div(attrs = { courseStyles(CourseStyle.GUIDANCE) }) {
        Details(
            attrs = {
                elementId(CourseControl.GUIDE_TERMS)
                styles(LabClass.NOTES)
            }
        ) {
            Summary() { Text("Begriffe auffrischen · bei Bedarf") }
            guidance.conceptBridges.forEach { P { Text(it) } }
        }
        Details(
            attrs = {
                elementId(CourseControl.GUIDE_SELF_CHECK)
                styles(LabClass.NOTES)
            }
        ) {
            Summary() { Text("Meine Erklärung selbst prüfen") }
            Ul { guidance.selfCheck.forEach { Li { Text(it) } } }
            P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
                Text(
                    "Das sind Kriterien für deine eigene Prüfung. Die App bewertet deinen freien Text nicht automatisch."
                )
            }
        }
    }
}

/** Same loaded profile as in the laboratory; a missing band never acquires a base reference. */
@Composable
internal fun BandReferenceReadout(
    session: LabSession,
    data: ScientificData?,
    profiles: LoadState<List<MeasurementProfile>>,
) {
    val measurement = session.snapshot.measurement
    if (!measurement.referenceViewed) return
    val image = data?.jonsson
    if (image == null) {
        P { Text("Eine passende Bildreferenz ist noch nicht verfügbar.") }
        return
    }
    val band = measurement.nativeBand(image)
    Div(
        attrs = {
            elementId(CourseControl.ACTIVE_REFERENCE)
            styles(LabClass.COURSE_RESULT)
        }
    ) {
        when (profiles) {
            LoadState.Loading -> P { Text("Referenz für ${measurement.band.label} wird geprüft …") }
            is LoadState.Failed -> P(attrs = { styles(LabClass.ERROR) }) { Text(profiles.message) }
            is LoadState.Ready -> {
                val profile =
                    profiles.value.singleOrNull {
                        it.id == measurement.band.profileId &&
                            it.bandTop == band.topInclusive.toDouble() &&
                            it.bandBottom == band.bottomExclusive.toDouble()
                    }
                if (profile == null)
                    P(attrs = { styles(LabClass.ERROR) }) {
                        Text(
                            "Keine geprüfte Referenz für dieses Band. Die eigene Messung bleibt erhalten."
                        )
                    }
                else {
                    val mean = PixelMeasurement.mean(profile.markers)
                    P(attrs = { data(LabData.VALUE_PX, mean) }) {
                        Text(
                            "Aktive heutige Referenz · ${measurement.band.label}, ${band.topInclusive} ≤ y < ${band.bottomExclusive}: ungefähr ${Physics.decimal(mean, 1)} native px."
                        )
                    }
                    P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
                        Text(
                            "Rechenkontrolle ${Physics.decimal(mean, 4)} px. Eigene Wahl und Referenz sind verschiedene Auswertungen derselben Reproduktion; dies ist kein historischer Sollwert."
                        )
                    }
                }
            }
        }
    }
}
