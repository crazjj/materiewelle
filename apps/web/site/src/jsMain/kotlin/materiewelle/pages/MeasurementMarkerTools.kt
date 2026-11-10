package materiewelle.pages

import androidx.compose.runtime.*
import materiewelle.*
import materiewelle.core.MeasurementSelection
import materiewelle.core.MeasurementSession
import materiewelle.core.NativePixel
import materiewelle.presentation.styles.*
import materiewelle.ui.MathText
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*


@Composable
internal fun MeasurementMarkerTools(data: JonssonData, measurement: MeasurementSession, profile: MeasurementProfile?, viewState: JonssonUiState, editor: MeasurementEditor) {
    var selected by viewState.selected
    var markerText by viewState.markerText
    var editingMarkerText by viewState.editingMarkerText
    var newMarkerText by viewState.newMarkerText
    var sampleX by viewState.sampleX
    val validCoordinate = editor.validCoordinate
    val newCoordinateIssue = editor.newCoordinateIssue
    val markers = measurement.markers.map { it.xPx.toDouble() }
    val nativeBand = measurement.nativeBand(data)
    val bandTop = nativeBand.topInclusive.toDouble()
    val bandBottom = nativeBand.bottomExclusive.toDouble()
    LaunchedEffect(measurement.markers, selected) {
        selected = selected.coerceAtMost(measurement.markers.lastIndex).coerceAtLeast(0)
        val actual = measurement.markers.getOrNull(selected)?.xPx
        if (actual == null) {
            markerText = ""
            editingMarkerText = false
        } else {
            val acceptedDraft = parseMarkerCoordinate(markerText, markers, selected, data)
            // Preserve both a valid spelling such as 593,0 and an unfinished/invalid draft.
            // An explicit selection/drag/button action, or an externally changed accepted
            // coordinate, may replace the draft with the new coordinate.
            if (
                !editingMarkerText ||
                    (acceptedDraft != null && acceptedDraft.value != actual.toDouble())
            ) {
                markerText = actual.toString()
                editingMarkerText = false
            }
        }
    }
    Div(
        attrs = {
            styles(LabClass.MARKER_WORKSHOP)
            role(AriaRole.GROUP)
            ariaLabel("Eigene Messmarken am Originalbild")
        }
    ) {

        P(
            attrs = {
                elementId(Id.MARKER_STATE)
                styles(LabClass.SMALL, LabClass.MUTED)
                ariaLive(LiveMode.POLITE)
            }
        ) {
            MathText(
                when (measurement.selection) {
                    MeasurementSelection.Empty ->
                        "Tippe oder klicke vier benachbarte helle Streifen an. Für die Tastatur kannst du unten Koordinaten eingeben."
                    is MeasurementSelection.Partial ->
                        "Eine Marke ist gesetzt. Für einen Abstand brauchst du eine zweite."
                    is MeasurementSelection.Ready ->
                        "Wähle eine Marke. Ziehe sie im Bild oder korrigiere sie mit −1 px oder +1 px."
                }
            )
        }
        if (markers.isNotEmpty()) {
            Div(
                attrs = {
                    styles(LabClass.MARKER_CHOICES)
                    ariaLabel("Messmarke auswählen")
                }
            ) {
                markers.forEachIndexed { index, x ->
                    Button(
                        attrs = {
                            styles(LabClass.DATA_CHOICE)
                            if (selected == index) styles(LabClass.CHOSEN)
                            data(LabData.MARKER_INDEX, index)
                            ariaPressed(selected == index)
                            ariaLabel("Marke ${index + 1}, x = ${x.toInt()} Originalpixel")
                            onClick { editor.selectMarker(index) }
                        }
                    ) {
                        MathText((index + 1).toString())
                    }
                }
            }
            Div(attrs = { styles(LabClass.MARKER_ACTIONS) }) {
                listOf(-1.0, 1.0).forEach { delta ->
                    Button(
                        attrs = {
                            styles(LabClass.SECONDARY)
                            elementId(if (delta < 0) Id.MARKER_MINUS else Id.MARKER_PLUS)
                            ariaLabel(
                                if (delta < 0)
                                    "Ausgewählte Marke um ein Originalpixel nach links"
                                else "Ausgewählte Marke um ein Originalpixel nach rechts"
                            )
                            if (
                                !PixelMeasurement.canMove(
                                    markers,
                                    selected,
                                    delta,
                                    data.cropLeft,
                                    data.cropLeft + data.cropWidth,
                                )
                            )
                                disabled()
                            onClick { editor.nudge(delta) }
                            protectPersonalWork(!editor.canEditMeasurement)
                        }
                    ) {
                        MathText(if (delta < 0) "−1 px" else "+1 px")
                    }
                }
                Button(
                    attrs = {
                        styles(LabClass.SECONDARY)
                        elementId(Id.MARKER_REMOVE)
                        ariaLabel("Ausgewählte Messmarke entfernen")
                        onClick {
                            val index = selected
                            val next = editor.update { latest ->
                                if (index in latest.markers.indices)
                                    latest.removeMarker(index)
                                else latest
                            }
                            selected =
                                selected
                                    .coerceAtMost(next.markers.lastIndex)
                                    .coerceAtLeast(0)
                            markerText =
                                next.markers.getOrNull(selected)?.xPx?.toString() ?: ""
                            editingMarkerText = false
                            next.markers.getOrNull(selected)?.let {
                                sampleX = NativePixel(it.xPx.toDouble())
                            }
                        }
                        protectPersonalWork(!editor.canEditMeasurement)
                    }
                ) {
                    MathText("Marke entfernen")
                }
            }
        }
        Details(attrs = { elementId(MeasurementGuidanceId.COORDINATE_TOOLS) }) {
            Summary { MathText("Marken über Koordinaten setzen und ändern") }
            if (markers.isNotEmpty()) {
                FieldLabel(forId = Id.MARKER_X) {
                    MathText("Marke ${selected + 1}: x (Originalpixel)")
                }
                Input(
                    InputType.Text,
                    attrs = {
                        elementId(Id.MARKER_X)
                        value(markerText)
                        maxLength(120)
                        inputMode(LabInputMode.NUMERIC)
                        autoComplete(CompletionMode.OFF)
                        ariaInvalid(validCoordinate == null)
                        if (validCoordinate == null) describedBy(Id.MARKER_X_ERROR)
                        onInput { event ->
                            markerText = event.value
                            editingMarkerText = true
                            parseNativeCoordinate(event.value, data)?.let {
                                editor.commitMarker(selected, it, false)
                            }
                        }
                        protectPersonalWork(!editor.canEditMeasurement)
                    },
                )
                if (validCoordinate == null)
                    P(
                        attrs = {
                            elementId(Id.MARKER_X_ERROR)
                            styles(LabClass.INPUT_ERROR)
                            role(AriaRole.ALERT)
                        }
                    ) {
                        MathText(
                            "Gib eine ganze Zahl von ${markers.getOrNull(selected - 1)?.toInt()?.plus(1) ?: data.cropLeft.toInt()} bis ${markers.getOrNull(selected + 1)?.toInt()?.minus(1) ?: (data.cropLeft + data.cropWidth).toInt() - 1} ein. Marken dürfen nicht aufeinanderliegen oder ihre Reihenfolge wechseln."
                        )
                    }
                if (validCoordinate == null) P(attrs = { elementId(Id.SELECTED_MARKER); styles(LabClass.SMALL, LabClass.MUTED) }) {
                    MathText("Zuletzt übernommen: x = ${markers[selected].toInt()} Originalpixel")
                }
            }
            FieldLabel(forId = Id.MARKER_NEW_X) { MathText("Neue Marke bei x (Originalpixel)") }
            Div(attrs = { measurementStyles(MeasurementClass.COORDINATE_ROW) }) {
                Input(
                    InputType.Text,
                    attrs = {
                        elementId(Id.MARKER_NEW_X)
                        value(newMarkerText)
                        maxLength(120)
                        inputMode(LabInputMode.NUMERIC)
                        autoComplete(CompletionMode.OFF)
                        describedBy(MeasurementElement.NEW_COORDINATE_STATUS)
                        ariaInvalid(newCoordinateIssue == NewMarkerIssue.INVALID_COORDINATE || newCoordinateIssue == NewMarkerIssue.DUPLICATE)
                        onInput { newMarkerText = it.value }
                        protectPersonalWork(!editor.canEditMeasurement)
                    },
                )
                Button(
                    attrs = {
                        styles(LabClass.SECONDARY)
                        elementId(Id.MARKER_ADD)
                        if (
                            newCoordinateIssue != null
                        )
                            disabled()
                        onClick {
                            parseNativeCoordinate(newMarkerText, data)?.let {
                                editor.addMarker(it)
                                newMarkerText = ""
                            }
                        }
                        protectPersonalWork(!editor.canEditMeasurement)
                    }
                ) {
                    MathText("+ Marke setzen")
                }
            }
            P(attrs = {
                elementId(MeasurementElement.NEW_COORDINATE_STATUS)
                styles(LabClass.SMALL, LabClass.MUTED)
                ariaLive(LiveMode.POLITE)
            }) {
                MathText(
                    when (newCoordinateIssue) {
                        NewMarkerIssue.DUPLICATE -> "Bei x = ${parseNativeCoordinate(newMarkerText, data)?.value?.toInt()} liegt bereits eine Marke. Wähle eine andere Position oder bearbeite die vorhandene Marke."
                        NewMarkerIssue.LIMIT_REACHED -> "Du hast ${MeasurementSession.MAX_MARKERS} Marken gesetzt. Entferne eine Marke, bevor du eine neue hinzufügst."
                        NewMarkerIssue.INVALID_COORDINATE -> "Gib eine ganze Zahl von ${data.cropLeft.toInt()} bis ${(data.cropLeft + data.cropWidth).toInt() - 1} ein."
                        else -> "Erlaubt sind ganze Zahlen von ${data.cropLeft.toInt()} bis ${(data.cropLeft + data.cropWidth).toInt() - 1}. Der Zoom ändert diese Originalpixel-Koordinaten nicht."
                    }
                )
            }
            if (markers.isNotEmpty()) Details(attrs = { elementId(MeasurementElement.DETAIL_DISCLOSURE) }) {
                Summary { MathText("Ausgewählte Marke im Detail") }
                markers.getOrNull(selected)?.let { x ->
                    SelectedMarkerDetail(data, x, bandTop, bandBottom, profile)
                }
            }
        }
        Div(attrs = { measurementStyles(MeasurementClass.MARKER_RESET_ROW) }) {
            Details {
                Summary { MathText("Messung löschen") }
                P(attrs = { elementId(MeasurementElement.RESET_DESCRIPTION); styles(LabClass.SMALL, LabClass.MUTED) }) {
                    MathText("Löscht aktuelle Marken und Begründung. Gesicherte Auswahlen bleiben erhalten.")
                }
                Button(attrs = {
                    styles(LabClass.SECONDARY)
                    elementId(Id.MARKER_RESET)
                    if (markers.isEmpty() && measurement.justification.isBlank()) disabled()
                    describedBy(MeasurementElement.RESET_DESCRIPTION)
                    onClick {
                        editor.update { it.clearMeasurement() }
                        selected = 0
                        markerText = ""
                        editingMarkerText = false
                        newMarkerText = ""
                    }
                    protectPersonalWork(!editor.canEditMeasurement)
                }) { MathText("Aktuelle Messung löschen") }
            }
            Button(attrs = {
                elementId(MeasurementToolId.UNDO); styles(LabClass.SECONDARY)
                if (!editor.canUndo) disabled()
                onClick { editor.undoEdit() }
                protectPersonalWork(!editor.canEditMeasurement)
            }) { MathText("Rückgängig") }
        }
    }
}

/** Zoom and image framing stay beside the picture; they never change native coordinates. */
@Composable
internal fun MeasurementViewTools(measurement: MeasurementSession, profile: MeasurementProfile?, viewState: JonssonUiState) {
    var showMarkers by viewState.showMarkers
    var wholeImage by viewState.wholeImage
    Div(attrs = { styles(LabClass.MEASUREMENT_LEGEND) }) {
        if (viewState.referenceVisible(measurement) && profile != null)
            Span(attrs = { styles(LabClass.REFERENCE_KEY) }) {
                MathText("Dokumentierte heutige Referenzauswahl")
            }
        Span(attrs = { styles(LabClass.OWN_KEY) }) { MathText("Eigene Messmarken") }
    }
    Div(attrs = { styles(LabClass.MEASUREMENT_TOGGLES) }) {
        Label {
            Input(
                InputType.Checkbox,
                attrs = {
                    elementId(Id.SHOW_MARKERS)
                    checked(showMarkers)
                    onChange { showMarkers = it.value }
                },
            )
            MathText(" Marken anzeigen")
        }
        Button(
            attrs = {
                elementId(Id.JONSSON_WHOLE_IMAGE)
                styles(LabClass.SECONDARY)
                ariaPressed(wholeImage)
                onClick { wholeImage = !wholeImage }
            }
        ) {
            MathText(if (wholeImage) "Doppelspaltausschnitt zeigen" else "Gesamtbild zeigen")
        }
        Button(
            attrs = {
                elementId(Id.JONSSON_VIEW_RESET)
                styles(LabClass.SECONDARY)
                onClick {
                    viewState.zoom.value = 1.0
                    showMarkers = true
                    wholeImage = false
                }
            }
        ) {
            MathText("Bildansicht zurücksetzen")
        }
    }
    P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
        MathText("Zurücksetzen zeigt den Doppelspaltausschnitt bei 100 % mit Marken. Deine Messung bleibt erhalten.")
    }
    Details {
        Summary { MathText("Bild mit Touch, Maus oder Tastatur bedienen") }
        P(
            attrs = {
                styles(LabClass.SMALL, LabClass.MUTED)
                elementId(Id.JONSSON_IMAGE_INSTRUCTIONS)
            }
        ) {
            MathText(
                "Im Bild neben vorhandene Marken klicken: neue Marke. Eine Marke anklicken und ziehen: verschieben. Bild fokussieren und ← / →: ausgewählte Marke um 1 Pixel verschieben. Alternativ Zahlenfelder verwenden. Zoom ändert keine Messkoordinate."
            )
        }
    }
}

/** View-only zoom is grouped with the band selector above the photograph. */
@Composable
internal fun MeasurementZoom(viewState: JonssonUiState) {
    var zoom by viewState.zoom
    Div(attrs = { styles(LabClass.MEASUREMENT_TOOLS) }) {
        FieldLabel(forId = Id.IMAGE_ZOOM) { MathText("Ansichtszoom") }
        Input(
            InputType.Range,
            attrs = {
                elementId(Id.IMAGE_ZOOM)
                minimum(1)
                maximum(2)
                increment(0.25)
                value(zoom)
                onInput { it.value?.toDouble()?.let { value -> zoom = value } }
            },
        )
        Span { MathText("${(zoom * 100).toInt()} %") }
    }
}
