package materiewelle.pages

import androidx.compose.runtime.*
import materiewelle.*
import materiewelle.core.MeasurementSelection
import materiewelle.core.MeasurementSession
import materiewelle.presentation.styles.*
import materiewelle.ui.MathText
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*


/** Compact current values; provenance stays attached to the actual selection. */
@Composable
internal fun MeasurementReadout(data: JonssonData, measurement: MeasurementSession, profile: MeasurementProfile?) {
    val markers = measurement.markers.map { it.xPx.toDouble() }
    val ready = measurement.selection as? MeasurementSelection.Ready
    Div(attrs = { styles(LabClass.MEASUREMENT_SUMMARY) }) {
        Div {

            H2(attrs = { elementId(Id.JONSSON_COUNT) }) {
                val markerCount = markers.size
                val intervalCount = ready?.intervalsPx?.size ?: 0
                val markerLabel = if (markerCount == 1) "Marke" else "Marken"
                val intervalLabel = if (intervalCount == 1) "Abstand" else "Abstände"
                MathText("$markerCount $markerLabel → $intervalCount $intervalLabel")
            }
        }
        Div(attrs = { styles(LabClass.MEASUREMENT_DISPLAY) }) {
            Span { MathText("Mittlerer Abstand") }
            Strong(
                attrs = {
                    elementId(Id.JONSSON_SPACING)
                    ready?.let { data(LabData.SPACING_PX, it.meanPx) }
                }
            ) {
                MathText(ready?.let { "≈ ${number(it.meanPx, 1)}" } ?: "—")
            }
            Span { MathText(if (ready == null) "mindestens zwei Marken nötig" else "Originalpixel") }
        }
    }
    if (ready != null) {
        Details {
            Summary(attrs = { elementId(MeasurementGuidanceId.RESULT_METHOD) }) {
                MathText("Mittel: (${markers.last().toInt()} − ${markers.first().toInt()}) / ${ready.intervalsPx.size} ≈ ${number(ready.meanPx, 1)} px")
            }
            P { Text("Abstand von der ersten bis zur letzten Marke, geteilt durch die Anzahl der Intervalle. Prüfe im Bild, ob deine Marken auf benachbarten Maxima liegen.") }
        }
    }
}

/** Saved and documented selections have their own comparison area. */
@Composable
internal fun MeasurementComparisonReadout(data: JonssonData, measurement: MeasurementSession, profile: MeasurementProfile?, editor: MeasurementEditor) {
    val ready = measurement.selection as? MeasurementSelection.Ready
    val nativeBand = measurement.nativeBand(data)
    val reference = if (editor.referenceVisible) profile?.markers.orEmpty() else emptyList()
    val focusResult = rememberMeasurementResultFocus()
    if (editor.referenceVisible && reference.size >= 2) {
        val referenceMean = PixelMeasurement.mean(reference)
        Section(attrs = {
            elementId(MeasurementElement.REFERENCE_RESULT)
            labelledBy(MeasurementElement.REFERENCE_TITLE)
            measurementComparisonStyles(MeasurementComparisonClass.CARD)
            focusIndex(-1)
        }) {
            H3(attrs = { elementId(MeasurementElement.REFERENCE_TITLE) }) {
                MathText("Deine Auswahl und die dokumentierte Referenz")
            }
            P { MathText("${measurement.band.label} · ${nativeBand.topInclusive} ≤ y < ${nativeBand.bottomExclusive} Originalpixel") }
            Div(attrs = { styles(LabClass.MEASUREMENT_SUMMARY) }) {
                Div(attrs = { styles(LabClass.MEASUREMENT_DISPLAY) }) {
                    Span { MathText("Deine aktuelle Auswahl") }
                    Strong { MathText(ready?.let { "≈ ${number(it.meanPx, 1)} px" } ?: "Noch unvollständig") }
                }
                Div(attrs = { styles(LabClass.MEASUREMENT_DISPLAY, LabClass.REFERENCE_DISPLAY) }) {
                    Span { MathText("Dokumentierte heutige Referenz") }
                    Strong { MathText("≈ ${number(referenceMean, 1)} px") }
                }
            }
            P {
                MathText(ready?.let {
                    "Eigene Auswahl − Referenz: ${number(it.meanPx - referenceMean, 1)} px."
                } ?: "Für den Zahlenvergleich brauchst du mindestens zwei eigene Marken.")
            }
            if (measurementComparisonHint(measurement.selection, measurement.band, nativeBand, profile, editor.referenceVisible) ==
                MeasurementComparisonHint.CHECK_INTERMEDIATE_MAXIMA) {
                P(attrs = { elementId(MeasurementGuidanceId.INTERMEDIATE_MAXIMA); role(AriaRole.STATUS) }) {
                    Text("Dein Abstand liegt etwa beim Doppelten der Referenz. Prüfe im Bild, ob zwischen zwei Marken ein weiterer heller Streifen liegt.")
                }
            }
            P(attrs = { elementId(MeasurementElement.REFERENCE_MARKS) }) {
                MathText("Referenzmarken bei x = ${reference.joinToString(", ") { number(it, 0) }} px. Im Bild sind sie gelb gestrichelt; deine Marken bleiben türkis und nummeriert.")
            }
            P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
                MathText("Das ist eine dokumentierte heutige Auswahl am selben Bild, keine historische Sollvorgabe. Andere begründete Auswahlen können zulässig sein. Deine erste festgehaltene Auswahl bleibt unverändert.")
            }
            Button(attrs = {
                elementId(MeasurementElement.REFERENCE_IMAGE)
                styles(LabClass.SECONDARY)
                onClick {
                    editor.view.showMarkers.value = true
                    focusResult(Id.JONSSON_INTERACTIVE_IMAGE)
                }
            }) { MathText("Marken im Bild ansehen") }
        }
    }
}
