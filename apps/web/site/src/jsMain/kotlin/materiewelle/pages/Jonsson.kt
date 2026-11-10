package materiewelle.pages

import androidx.compose.runtime.*
import materiewelle.*
import materiewelle.core.FiniteSlitModel
import materiewelle.core.Length
import materiewelle.core.LoadState
import materiewelle.core.MeasurementBand
import materiewelle.core.MeasurementSelection
import materiewelle.core.MeasurementSession
import materiewelle.core.Voltage
import materiewelle.core.WavelengthApproximation
import materiewelle.data.LabAsset
import materiewelle.presentation.styles.*
import materiewelle.ui.MathText
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.css.keywords.auto
import org.jetbrains.compose.web.dom.*

import materiewelle.state.MeasurementOwner

/** Controlled native measurement: one scientific owner, explicit view state, local tools. */
@Composable
fun JonssonMeasurement(
    data: JonssonData,
    owner: MeasurementOwner,
    profiles: LoadState<List<MeasurementProfile>>,
    viewState: JonssonUiState,
    embedded: Boolean = false,
    imageCaption: @Composable () -> Unit = {},
    help: @Composable () -> Unit = {},
    helpPanel: @Composable () -> Unit = {},
) {
    LaunchedEffect(data.imageSha256) { viewState.bindImage(data) }
    val measurement = owner.measurement
    val editor = remember(data, owner, viewState) { MeasurementEditor(data, owner, viewState) }
    val band = measurement.nativeBand(data)
    val profile = (profiles as? LoadState.Ready)?.value?.firstOrNull {
        it.id == measurement.band.profileId && it.bandTop == band.topInclusive.toDouble() && it.bandBottom == band.bottomExclusive.toDouble()
    }
    if (!embedded) H1 { MathText("Streifenabstand messen") }
    Div(attrs = { elementId(Id.MARKER_WORKSHOP); focusIndex(-1) }) {
        Div(attrs = {
            measurementStyles(MeasurementClass.FIRST_WORKSPACE)
            elementId(MeasurementElement.FIRST_WORKSPACE)
        }) {
            Div(attrs = { styles(LabClass.JONSSON_LAB, LabClass.MEASUREMENT_LAYOUT) }) {
                Section(attrs = { styles(LabClass.JONSSON_IMAGING, LabClass.LAB_PANEL) }) {
                    Div(attrs = { measurementStyles(MeasurementClass.IMAGE_CONTROLS) }) {
                        MeasurementBandPicker(data, measurement, editor)
                        MeasurementZoom(viewState)
                    }
                    MeasurementCanvas(data, measurement, profile, viewState, editor)
                    Div(attrs = { measurementStyles(MeasurementClass.WORKSPACE_PROFILE) }) {
                        MeasurementProfilePanel(data, measurement, profile, profiles, viewState)
                    }
                    Details {
                        Summary { MathText("Historische Aufnahme · Quelle und Bilddaten") }
                        imageCaption()
                        P(attrs = { elementId(Id.PROFILE_DESCRIPTION); styles(LabClass.SMALL, LabClass.MUTED) }) {
                            MathText("Gewähltes Band: ${band.topInclusive} ≤ y < ${band.bottomExclusive} Originalpixel. Prüfe nach einem Bandwechsel deine Marken und Begründung.")
                        }
                        P(attrs = { elementId(Id.JONSSON_SOURCE_STATUS); styles(LabClass.SMALL, LabClass.MUTED) }) {
                            MathText("Gemessen wird in Originalpixeln. Der Zoom ändert nur die Ansicht. Ohne unabhängigen Bildmaßstab lässt sich daraus keine Länge in Metern bestimmen.")
                        }
                    }
                    Details {
                        Summary { MathText("Bildansicht und Bedienung") }
                        MeasurementViewTools(measurement, profile, viewState)
                    }
                }
                Section(attrs = { styles(LabClass.JONSSON_CONTROLS, LabClass.LAB_PANEL) }) {
                    MeasurementReadout(data, measurement, profile)
                    MeasurementMarkerTools(data, measurement, profile, viewState, editor)
                    Div(attrs = { elementId(Id.JONSSON_EXPLANATION); measurementStyles(MeasurementClass.WORKSPACE_REASON) }) {
                        MeasurementDecisions(data, measurement, profile, editor)
                        help()
                    }
                }
            }
        }
        helpPanel()
    }
    Section(attrs = {
        elementId(MeasurementElement.CHECKS)
        labelledBy(MeasurementElement.CHECKS_TITLE)
        measurementStyles(MeasurementClass.CHECKS)
    }) {
        H2(attrs = { elementId(MeasurementElement.CHECKS_TITLE) }) { MathText("Eigene Auswahl und Referenz vergleichen") }
        InitialSelection(measurement)
        MeasurementReferenceAction(measurement, profile, editor)
        MeasurementComparisonReadout(data, measurement, profile, editor)
        Details(attrs = { elementId(MeasurementElement.COUNTERCHECK) }) {
            Summary { MathText("Gegenprobe: Habe ich ein Maximum ausgelassen?") }
            P { MathText("Prüfe im Bild und im Profil, ob zwischen zwei Marken ein weiteres helles Maximum liegt. Bei jedem zweiten Maximum wären die Abstände weiterhin gleichmäßig, aber etwa doppelt so groß. Zähle deshalb die hellen Streifen zwischen erster und letzter Marke mit.") }
        }
        Details(attrs = { elementId(MeasurementElement.OPTIONAL_COMPARISON) }) {
            Summary { MathText("Optional: eigene Auswahlen A und B vergleichen") }
            P { MathText("Speichere deine aktuelle Auswahl als A. Verändere danach eine Marke oder das Bildband; die aktuelle Auswahl ist B. Deine erste festgehaltene Auswahl bleibt erhalten.") }
            MeasurementComparisonActions(measurement, editor, data)
            MeasurementComparison(data, measurement)
        }
        MeasurementReport(data, measurement, profile)
    }

}

@Composable
private fun MeasurementBandPicker(data: JonssonData, measurement: MeasurementSession, editor: MeasurementEditor) {
    Div(attrs = { styles(LabClass.PROFILE_PICKER) }) {
        FieldLabel(Id.MEASUREMENT_PROFILE) { MathText("Auswerteband") }
        Select(attrs = {
            elementId(Id.MEASUREMENT_PROFILE)
            onChange { event -> event.value?.let(MeasurementBand::fromProfileId)?.let { next -> editor.update { it.changeBand(next) } } }
            protectPersonalWork(!editor.canEditMeasurement)
        }) {
            MeasurementBand.entries.filter { it == MeasurementBand.BASE || it.bottomExclusive <= data.height }.forEach { candidate ->
                Option(value = candidate.profileId, attrs = { if (candidate == measurement.band) selected() }) { MathText(candidate.label) }
            }
        }
    }
}
