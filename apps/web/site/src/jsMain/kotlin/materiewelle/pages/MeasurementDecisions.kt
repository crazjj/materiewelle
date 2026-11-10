package materiewelle.pages

import androidx.compose.runtime.*
import materiewelle.*
import materiewelle.core.MeasurementSession
import materiewelle.presentation.styles.*
import materiewelle.ui.MathText
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*


@Composable
internal fun MeasurementDecisions(data: JonssonData, measurement: MeasurementSession, profile: MeasurementProfile?, editor: MeasurementEditor) {
    FieldLabel(forId = Id.JONSSON_JUSTIFICATION) {
        MathText("Deine Begründung")
    }
    TextArea(
        attrs = {
            elementId(Id.JONSSON_JUSTIFICATION)
            value(measurement.justification)
            onInput { event ->
                val text = event.value.take(4000)
                editor.update { it.updateJustification(text) }
            }
            protectPersonalWork(!editor.canEditMeasurement)
        }
    )
    P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
        MathText(
            "Nenne den Abstand. Warum dieses Band und diese benachbarten Maxima? Wo bleibt die Ablesung unsicher?"
        )
    }
    if (measurement.justification.isNotBlank() && !measurement.justificationMatchesBand) {
        P(
            attrs = {
                elementId(MeasurementElement.JUSTIFICATION_STATUS)
                measurementStyles(MeasurementClass.BAND_WARNING)
                role(AriaRole.STATUS)
            }
        ) {
            MathText(
                "Marken und Text bleiben erhalten. Diese Begründung gehört ${measurement.justificationBand?.let { "zum Band „${it.label}“" } ?: "zu einem unbekannten früheren Band"}. Prüfe, ob sie auch zum jetzt gewählten Band „${measurement.band.label}“ passt. Passe sie an oder bestätige sie für dieses Band."
            )
        }
        Button(
            attrs = {
                elementId(MeasurementElement.CONFIRM_JUSTIFICATION)
                styles(LabClass.SECONDARY)
                onClick {
                    editor.update {
                        if (it.justification.isNotBlank()) it.confirmJustification() else it
                    }
                }
                protectPersonalWork(!editor.canEditMeasurement)
            }
        ) {
            MathText("Begründung für „${measurement.band.label}“ übernehmen")
        }
    }
    if (measurement.referenceMaterialsOpened.isNotEmpty()) Details {
        Summary { MathText("Bereits geöffnete Hilfen") }
        P(
            attrs = {
                elementId(MeasurementElement.MATERIAL_STATUS)
                measurementStyles(MeasurementClass.MATERIAL_WARNING)
                role(AriaRole.STATUS)
            }
        ) {
            MathText(
                "Vermerkt: ${measurement.referenceMaterialsOpened.joinToString { it.label }}. Erfasst werden Öffnungen und Dateianfragen. Daraus folgt nicht, dass Inhalte gelesen, übernommen oder Dateien erfolgreich heruntergeladen wurden."
            )
        }
    }
    if (measurement.initialSelection == null)
        Button(
            attrs = {
                elementId(Id.JONSSON_SEAL_SELECTION)
                styles(LabClass.PRIMARY)
                if (!measurement.canSave) disabled()
                onClick {
                    editor.update {
                        if (
                            it.canSave && it.initialSelection == null && !it.referenceViewed
                        )
                            it.saveInitialSelection(data)
                        else it
                    }
                }
                protectPersonalWork(!editor.canEditMeasurement)
            }
        ) {
            MathText("Erste Auswahl festhalten")
        }
    P(
        attrs = {
            elementId(Id.MEASUREMENT_SELECTION_STATUS)
            styles(LabClass.SMALL, LabClass.MUTED)
            ariaLive(LiveMode.POLITE)
        }
    ) {
        MathText(
            measurement.initialSelection?.let { first ->
                "Erste Auswahl gesichert: ${first.markers.size} Marken im Band „${first.band.label}“. Spätere Änderungen überschreiben sie nicht."
            }
                ?: "Setze mindestens zwei Marken und begründe die Auswahl. Danach kannst du sie festhalten und die Referenz öffnen."
        )
    }
}

/** Secondary actions never obscure the first own selection. */
@Composable
internal fun MeasurementReferenceAction(measurement: MeasurementSession, profile: MeasurementProfile?, editor: MeasurementEditor) {
    val focusResult = rememberMeasurementResultFocus()
    Button(
        attrs = {
            elementId(Id.JONSSON_SHOW_REFERENCE)
            styles(LabClass.SECONDARY)
            if (
                !measurement.canViewReference || profile == null
            )
                disabled()
            describedBy(Id.JONSSON_REFERENCE_STATUS)
            onClick {
                if (editor.canEditMeasurement) {
                    editor.update { if (it.canViewReference && profile != null) it.revealReference() else it }
                } else if (editor.measurement.canViewReference && profile != null) {
                    editor.view.readOnlyReferenceOpen.value = true
                }
                if (editor.referenceVisible && profile != null) {
                    editor.view.showMarkers.value = true
                    focusResult(MeasurementElement.REFERENCE_RESULT)
                }
            }
        }
    ) {
        MathText(
            if (editor.referenceVisible) "Referenzvergleich ansehen"
            else "Mit der Referenz vergleichen"
        )
    }
    P(
        attrs = {
            elementId(Id.JONSSON_REFERENCE_STATUS)
            styles(LabClass.SMALL, LabClass.MUTED)
            ariaLive(LiveMode.POLITE)
        }
    ) {
        MathText(
            if (editor.referenceVisible)
                (if (!measurement.referenceViewed)
                    "Die dokumentierte Referenz ist nur in diesem Tab geöffnet. Dein gespeicherter Arbeitsstand bleibt unverändert."
                else "Referenz geöffnet. Deine eigenen Marken und die erste festgehaltene Auswahl bleiben erhalten.") +
                    (if (profile == null)
                        " Für das aktuelle Band ist derzeit keine geprüfte Referenz verfügbar."
                    else " Verglichen wird eine dokumentierte heutige Auswahl im selben Band.")
            else
                    (if (measurement.canViewReference) "Referenzmarken noch verborgen."
                    else if (!editor.canEditMeasurement) "Halte deine erste Auswahl im anderen Tab fest. Lade anschließend hier den gespeicherten Stand neu."
                    else "Halte zuerst deine begründete Auswahl mit „Erste Auswahl festhalten“ fest.") +
                    (if (profile == null)
                        " Für dieses Band ist derzeit kein geprüftes Profil verfügbar. Deshalb lässt sich die Referenz nicht öffnen."
                    else "")
        )
    }
}

/** A/B is an optional investigation, separate from the first selection and reference. */
@Composable
internal fun MeasurementComparisonActions(measurement: MeasurementSession, editor: MeasurementEditor, data: JonssonData) {
    val focusResult = rememberMeasurementResultFocus()
    Div(attrs = { styles(LabClass.MARKER_ACTIONS, LabClass.JONSSON_COMPARISON_ACTIONS) }) {
        Button(
            attrs = {
                elementId(Id.JONSSON_COMPARE_SAVE)
                styles(LabClass.SECONDARY)
                if (!measurement.canSave) disabled()
                onClick {
                    if (editor.measurement.canSave) {
                        val updated = editor.update { if (it.canSave) it.saveComparison(data) else it }
                        if (updated.comparison != null) focusResult(MeasurementComparisonId.ROOT)
                    }
                }
                protectPersonalWork(!editor.canEditMeasurement)
            }
        ) {
            MathText(if (measurement.comparison == null) "Aktuelle Auswahl als A speichern" else "A durch aktuelle Auswahl ersetzen")
        }
        if (measurement.comparison != null)
            Button(
                attrs = {
                    elementId(Id.JONSSON_COMPARE_CLEAR)
                    styles(LabClass.SECONDARY)
                    onClick { editor.update { it.clearComparison() } }
                    protectPersonalWork(!editor.canEditMeasurement)
                }
            ) {
                MathText("Vergleich A löschen")
            }
    }
    if (!measurement.canSave) P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
        MathText("Für A brauchst du mindestens zwei Marken und eine Begründung zum gewählten Band.")
    }

}
