package materiewelle.pages

import androidx.compose.runtime.*
import kotlinx.browser.window
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
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event


/** Browser viewport and transient pointer state, independent of accepted native coordinates. */
@Composable
internal fun MeasurementCanvas(data: JonssonData, measurement: MeasurementSession, profile: MeasurementProfile?, viewState: JonssonUiState, editor: MeasurementEditor) {
    val focusResult = rememberMeasurementResultFocus()
    var selected by viewState.selected
    var zoom by viewState.zoom
    var wholeImage by viewState.wholeImage
    var showMarkers by viewState.showMarkers
    var sampleX by viewState.sampleX
    var cursor by remember { mutableStateOf<NativePoint?>(null) }
    var imageWindow by remember { mutableStateOf<HTMLElement?>(null) }
    var pointerInteracting by remember { mutableStateOf(false) }
    var autoPanInitialized by remember { mutableStateOf(false) }
    val markers = measurement.markers.map { it.xPx.toDouble() }
    val nativeBand = measurement.nativeBand(data)
    val bandTop = nativeBand.topInclusive.toDouble()
    val bandBottom = nativeBand.bottomExclusive.toDouble()
    val reference = if (editor.referenceVisible) profile?.markers.orEmpty() else emptyList()
    val profileProbe = profile?.resolveProbe(sampleX)
    val markerY = profile?.markerY ?: (bandTop + bandBottom) / 2.0
    val viewportHeight = minOf(maxOf(96.0, bandBottom - bandTop), data.height.toDouble())
    val displayData =
        data.copy(
            markerY = markerY,
            cropTop = ((bandTop + bandBottom - viewportHeight) / 2.0).coerceIn(0.0, data.height - viewportHeight),
            cropHeight = viewportHeight,
        )
    val view =
        if (wholeImage) ImageViewport(0.0, 0.0, data.width.toDouble(), data.height.toDouble())
        else
            ImageViewport(
                displayData.cropLeft,
                displayData.cropTop,
                displayData.cropWidth,
                displayData.cropHeight,
            )
    DisposableEffect(selected, measurement.markers, zoom, wholeImage, view, imageWindow, pointerInteracting) {
        val selectedX = measurement.markers.getOrNull(selected)?.xPx?.toDouble()
        val host = imageWindow
        var frame = 0
        // Mounting restores the reader's last viewport. Only subsequent changes to the
        // selection, zoom or image framing may bring the selected marker into view.
        // Otherwise this effect can overwrite restoration on a later Compose frame.
        val autoPan = autoPanInitialized
        if (host != null) autoPanInitialized = true
        if (autoPan && !pointerInteracting && selectedX != null && host != null)
            frame = window.requestAnimationFrame {
                val svg = host.querySelector(DomQuery.SVG.selector)
                // A pointer gesture owns its coordinate transform until release/cancel.
                // Also guard an already queued frame before Compose disposes this effect.
                if (!pointerInteracting && svg != null) {
                    val box = svg.getBoundingClientRect()
                    val hostBox = host.getBoundingClientRect()
                    val scale = minOf(box.width / view.width, box.height / view.height)
                    val screenX =
                        box.left +
                            (box.width - view.width * scale) / 2.0 +
                            (selectedX - view.x) * scale
                    val margin = minOf(40.0, host.clientWidth / 4.0)
                    if (screenX < hostBox.left + margin || screenX > hostBox.right - margin) {
                        host.scrollLeft =
                            (host.scrollLeft + screenX - hostBox.left - host.clientWidth / 2.0)
                                .coerceIn(
                                    0.0,
                                    maxOf(0.0, (host.scrollWidth - host.clientWidth).toDouble()),
                                )
                    }
                }
            }
        onDispose { window.cancelAnimationFrame(frame) }
    }
    DisposableEffect(imageWindow) {
        val host = imageWindow
        val restoreLeft = viewState.imageScrollLeft.value
        val listener: (Event) -> Unit = {
            if (host != null && window.document.contains(host))
                viewState.imageScrollLeft.value = host.scrollLeft
        }
        host?.addEventListener(BrowserEvent.SCROLL.eventName, listener)
        val frame = if (host != null) window.requestAnimationFrame { host.scrollLeft = restoreLeft } else null
        onDispose {
            frame?.let(window::cancelAnimationFrame)
            if (host != null) {
                // Teardown may remove the overflowing SVG before disposing this
                // effect, making scrollLeft zero even while the host is connected.
                // Keep the last scroll event instead of reading a dismantled DOM.
                host.removeEventListener(BrowserEvent.SCROLL.eventName, listener)
            }
        }
    }
    Div(
        attrs = {
            styles(LabClass.MEASUREMENT_WINDOW)
            elementId(Id.JONSSON_IMAGE_WINDOW)
            ref {
                imageWindow = it
                onDispose { imageWindow = null }
            }
        }
    ) {
        Div(
            attrs = {
                style {
                    width((zoom * 100.0).percent)
                    minWidth(100.percent)
                }
                data(LabData.NATIVE_WIDTH, data.width)
            }
        ) {
            JonssonImage(
                displayData,
                view,
                markers,
                reference,
                selected,
                showMarkers,
                editor.canEditMeasurement,
                bandTop,
                bandBottom,
                editor::selectMarker,
                editor::setMarker,
                editor::addMarker,
                { point ->
                    cursor = point
                    sampleX = NativePixel(kotlin.math.round(point.x.value))
                },
                editor::nudge,
                { pointerInteracting = it; editor.interaction(it) },
                profileProbe?.xPx?.toDouble(),
            )
        }
    }
    if (editor.referenceVisible && reference.isNotEmpty()) {
        P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
            MathText("Gelb gestrichelt: dokumentierte Referenz. Türkis und nummeriert: deine Marken.")
        }
        Button(attrs = {
            elementId(MeasurementElement.REFERENCE_RETURN)
            styles(LabClass.SECONDARY)
            onClick { focusResult(MeasurementElement.REFERENCE_RESULT) }
        }) { MathText("Zum Zahlenvergleich") }
    }
    Div(attrs = { styles(LabClass.IMAGE_STATUS) }) {
        Span(
            attrs = {
                elementId(Id.JONSSON_CURSOR)
                cursor?.let {
                    data(LabData.NATIVE_X, it.x.value)
                    data(LabData.NATIVE_Y, it.y.value)
                }
            }
        ) {
            MathText(
                cursor?.let { "x ${number(it.x.value, 1)} · y ${number(it.y.value, 1)} px" }
                    ?: "Koordinaten in Originalpixeln"
            )
        }
        Span {
            MathText(
                if (wholeImage) "Links Einspalt · rechts Zweispalt"
                else "Ausschnitt · Zweispalt"
            )
        }
    }

}
