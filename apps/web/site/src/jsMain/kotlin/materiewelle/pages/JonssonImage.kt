package materiewelle.pages

import androidx.compose.runtime.*
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt
import kotlinx.browser.window
import materiewelle.JonssonData
import materiewelle.MeasurementProfile
import materiewelle.core.NativePixel
import materiewelle.ui.MathText as Text
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent

internal const val SVG_NS = "http://www.w3.org/2000/svg"

internal data class NativePoint(val x: NativePixel, val y: NativePixel)

internal data class ImageViewport(
    val x: Double,
    val y: Double,
    val width: Double,
    val height: Double,
)

/**
 * Screen-to-image conversion includes SVG letterboxing and uses the CURRENT bounding box. Neither
 * display zoom nor viewport dimensions enter the stored native-coordinate measurements.
 */
internal fun nativePoint(event: MouseEvent, host: HTMLElement, view: ImageViewport): NativePoint? {
    val svg = host.querySelector(DomQuery.SVG.selector) ?: return null
    val rect = svg.getBoundingClientRect()
    val scale = min(rect.width / view.width, rect.height / view.height)
    if (!scale.isFinite() || scale <= 0.0) return null
    val x = (event.clientX - rect.left - (rect.width - view.width * scale) / 2.0) / scale
    val y = (event.clientY - rect.top - (rect.height - view.height * scale) / 2.0) / scale
    if (x < 0.0 || y < 0.0 || x > view.width || y > view.height) return null
    return NativePoint(NativePixel(view.x + x), NativePixel(view.y + y))
}

@Composable
internal fun JonssonImage(
    data: JonssonData,
    view: ImageViewport,
    positions: List<Double>,
    reference: List<Double>,
    selected: Int,
    marks: Boolean,
    editable: Boolean,
    bandTop: Double,
    bandBottom: Double,
    onSelect: (Int) -> Unit,
    onSet: (Int, NativePixel) -> Unit,
    onAdd: (NativePixel) -> Unit,
    onCursor: (NativePoint) -> Unit,
    onNudge: (Double) -> Unit,
    onInteractionChange: (Boolean) -> Unit,
    cursorX: Double? = null,
) {
    var host by remember { mutableStateOf<HTMLElement?>(null) }
    val currentView by rememberUpdatedState(view)
    val currentPositions by rememberUpdatedState(positions)
    val currentEditable by rememberUpdatedState(editable)
    val setPosition by rememberUpdatedState(onSet)
    val addPosition by rememberUpdatedState(onAdd)
    val setSelected by rememberUpdatedState(onSelect)
    val updateCursor by rememberUpdatedState(onCursor)
    val updateInteraction by rememberUpdatedState(onInteractionChange)
    DisposableEffect(host) {
        val element = host
        var dragging: Int? = null
        var interacting = false
        val down: (Event) -> Unit = { event ->
            if (
                event is MouseEvent &&
                    event.button.toInt() == 0 &&
                    currentEditable &&
                    element != null
            ) {
                nativePoint(event, element, currentView)?.let { point ->
                    if (
                        point.x.value >= data.cropLeft &&
                            point.x.value < data.cropLeft + data.cropWidth
                    ) {
                        event.preventDefault()
                        interacting = true
                        updateInteraction(true)
                        // Keep the pointer's coordinate frame fixed when the image extends
                        // below a short viewport; focus still enables the arrow-key editor.
                        element.focusWithoutScrolling()
                        updateCursor(point)
                        val nearest =
                            currentPositions.indices.minByOrNull {
                                abs(currentPositions[it] - point.x.value)
                            }
                        val svgWidth =
                            element
                                .querySelector(DomQuery.SVG.selector)
                                ?.getBoundingClientRect()
                                ?.width ?: 1.0
                        val threshold = maxOf(2.0, markerHitRadiusCss(event) * currentView.width / svgWidth)
                        if (
                            nearest != null &&
                                abs(currentPositions[nearest] - point.x.value) <= threshold
                        ) {
                            dragging = nearest
                            setSelected(nearest)
                            setPosition(nearest, NativePixel(point.x.value.roundToInt().toDouble()))
                        } else {
                            // An empty first attempt is intentional. Clicking a new location never
                            // silently moves a different, previously selected maximum.
                            addPosition(NativePixel(point.x.value.roundToInt().toDouble()))
                        }
                    }
                }
            }
        }
        val move: (Event) -> Unit = { event ->
            if (event is MouseEvent && element != null) {
                // Hover belongs to this image; an active drag may continue outside its hit target.
                val target = event.target
                if (dragging != null || (target is org.w3c.dom.Node && element.contains(target))) {
                    nativePoint(event, element, currentView)?.let { point ->
                        updateCursor(point)
                        dragging?.let { index ->
                            event.preventDefault()
                            setPosition(index, NativePixel(point.x.value.roundToInt().toDouble()))
                        }
                    }
                }
            }
        }
        val up: (Event) -> Unit = {
            dragging = null
            if (interacting) {
                interacting = false
                updateInteraction(false)
            }
        }
        element?.addEventListener(BrowserEvent.POINTER_DOWN.eventName, down)
        window.addEventListener(BrowserEvent.POINTER_MOVE.eventName, move)
        window.addEventListener(BrowserEvent.POINTER_UP.eventName, up)
        window.addEventListener(BrowserEvent.POINTER_CANCEL.eventName, up)
        onDispose {
            element?.removeEventListener(BrowserEvent.POINTER_DOWN.eventName, down)
            window.removeEventListener(BrowserEvent.POINTER_MOVE.eventName, move)
            window.removeEventListener(BrowserEvent.POINTER_UP.eventName, up)
            window.removeEventListener(BrowserEvent.POINTER_CANCEL.eventName, up)
            if (interacting) updateInteraction(false)
        }
    }
    Div(
        attrs = {
            elementId(Id.JONSSON_INTERACTIVE_IMAGE)
            styles(LabClass.JONSSON_INTERACTIVE_IMAGE)
            role(AriaRole.GROUP)
            ariaLabel("Historische Aufnahme mit bearbeitbaren Messmarken")
            describedBy(Id.JONSSON_IMAGE_INSTRUCTIONS)
            if (editable) tabIndex(0)
            onKeyDown { event ->
                val key = LabKey.fromBrowser(event.key)
                if (editable && (key == LabKey.LEFT || key == LabKey.RIGHT)) {
                    event.preventDefault()
                    onNudge(if (key == LabKey.LEFT) -1.0 else 1.0)
                }
            }
            ref {
                host = it
                onDispose { host = null }
            }
        }
    ) {
        SvgTag(
            SvgTagName.SVG,
            attrs = {
                elementId(Id.JONSSON_NATIVE_IMAGE)
                viewBox(view.x, view.y, view.width, view.height)
                role(AriaRole.IMAGE)
                ariaLabel("Jönsson: reproduzierte historische Beugungsaufnahme, Koordinaten im Originalbild")
                data(LabData.NATIVE_WIDTH, data.width)
                data(LabData.NATIVE_HEIGHT, data.height)
            },
        ) {
            SvgTag(
                SvgTagName.IMAGE,
                attrs = {
                    svgHref(data.imageFilename)
                    svgNumber(SvgNumber.X, 0)
                    svgNumber(SvgNumber.Y, 0)
                    svgNumber(SvgNumber.WIDTH, data.width)
                    svgNumber(SvgNumber.HEIGHT, data.height)
                },
            )
            cursorX
                ?.takeIf { it >= view.x && it <= view.x + view.width }
                ?.let { x ->
                    SvgTag(
                        SvgTagName.LINE,
                        attrs = {
                            elementId(MeasurementElement.CROSSHAIR_IMAGE)
                            data(LabData.NATIVE_X, x)
                            svgNumber(SvgNumber.X1, x)
                            svgNumber(SvgNumber.X2, x)
                            svgNumber(SvgNumber.Y1, view.y)
                            svgNumber(SvgNumber.Y2, view.y + view.height)
                            measurementStyles(MeasurementClass.CROSSHAIR)
                        },
                    )
                }
            if (marks) {
                SvgTag(
                    SvgTagName.RECT,
                    attrs = {
                        svgNumber(SvgNumber.X, data.cropLeft)
                        svgNumber(SvgNumber.Y, bandTop)
                        svgNumber(SvgNumber.WIDTH, data.cropWidth)
                        svgNumber(SvgNumber.HEIGHT, (bandBottom - bandTop))
                        styles(LabClass.NATIVE_BAND)
                    },
                )
                reference.forEachIndexed { index, x ->
                    SvgTag(SvgTagName.LINE, attrs = {
                        svgNumber(SvgNumber.X1, x); svgNumber(SvgNumber.X2, x)
                        svgNumber(SvgNumber.Y1, data.markerY - 29); svgNumber(SvgNumber.Y2, data.markerY + 29)
                        measurementStyles(MeasurementClass.REFERENCE_HALO); ariaHidden(true)
                    })
                    SvgTag(
                        SvgTagName.LINE,
                        attrs = {
                            svgNumber(SvgNumber.X1, x)
                            svgNumber(SvgNumber.X2, x)
                            svgNumber(SvgNumber.Y1, (data.markerY - 29))
                            svgNumber(SvgNumber.Y2, (data.markerY + 29))
                            styles(LabClass.NATIVE_REFERENCE)
                            data(LabData.REFERENCE_MARKER, index)
                            data(LabData.NATIVE_X, x)
                        },
                    )
                }
                positions.forEachIndexed { index, x ->
                    // A contrasting halo stays readable over both bright and dark film stripes.
                    SvgTag(SvgTagName.LINE, attrs = {
                        svgNumber(SvgNumber.X1, x); svgNumber(SvgNumber.X2, x)
                        svgNumber(SvgNumber.Y1, data.markerY - 16); svgNumber(SvgNumber.Y2, data.markerY + 16)
                        measurementStyles(MeasurementClass.MARKER_HALO); ariaHidden(true)
                    })
                    SvgTag(
                        SvgTagName.LINE,
                        attrs = {
                            svgNumber(SvgNumber.X1, x)
                            svgNumber(SvgNumber.X2, x)
                            svgNumber(SvgNumber.Y1, (data.markerY - 16))
                            svgNumber(SvgNumber.Y2, (data.markerY + 16))
                            styles(LabClass.NATIVE_OWN)
                            if (index == selected) styles(LabClass.SELECTED)
                            data(LabData.OWN_MARKER, index)
                            data(LabData.NATIVE_X, x)
                        },
                    )
                    SvgTag(SvgTagName.RECT, attrs = {
                        svgNumber(SvgNumber.X, x - 5); svgNumber(SvgNumber.Y, data.markerY - 26)
                        svgNumber(SvgNumber.WIDTH, 10); svgNumber(SvgNumber.HEIGHT, 9)
                        measurementStyles(MeasurementClass.LABEL_PLATE); ariaHidden(true)
                    })
                    TypesetSvgLabel(
                        namespace = SVG_NS,
                        x = x,
                        y = (data.markerY - 19),
                        attrs = {
                            textAnchor(TextAnchor.MIDDLE)
                            styles(LabClass.NATIVE_MARKER_LABEL)
                        },
                        text = (index + 1).toString(),
                        inheritColor = true,
                    )
                }
            }
        }
    }
}

@Composable
internal fun GrayProfile(
    profile: MeasurementProfile,
    own: List<Double>,
    probe: MeasurementProfileProbe,
    showReference: Boolean = false,
    onSample: (NativePixel) -> Unit,
) {
    var host by remember { mutableStateOf<HTMLElement?>(null) }
    val left = 64.0
    val top = 26.0
    val plotWidth = 634.0
    val plotHeight = 170.0
    val first = profile.profileStartX
    val last = first + profile.grayscale.lastIndex
    val span = (last - first).coerceAtLeast(1)
    fun x(value: Double) = left + (value - first) / span * plotWidth
    fun y(value: Double) = top + plotHeight * (1.0 - value / 255.0)
    val path =
        profile.grayscale
            .mapIndexed { index, value ->
                "${if (index == 0) "M" else "L"}${x((first + index).toDouble())},${y(value)}"
            }
            .joinToString(" ")
    val sampleX = probe.xPx
    val sample = probe.grayscale
    Div(
        attrs = {
            styles(LabClass.GRAY_PROFILE)
            ref {
                host = it
                onDispose { host = null }
            }
            onMouseMove { event ->
                host?.querySelector(DomQuery.SVG.selector)?.getBoundingClientRect()?.let { rect ->
                    val chartX = (event.clientX - rect.left) * 728.0 / rect.width
                    if (chartX in left..(left + plotWidth))
                        onSample(
                            NativePixel(
                                (first + (chartX - left) / plotWidth * (last - first))
                                    .roundToInt()
                                    .toDouble()
                            )
                        )
                }
            }
        }
    ) {
        SvgTag(
            SvgTagName.SVG,
            attrs = {
                elementId(Id.JONSSON_GRAY_PROFILE)
                viewBox(0, 0, 728, 260)
                role(AriaRole.IMAGE)
                ariaLabel(
                    "Mittlerer Grauwert des Originalrasters über das gewählte Auswerteband; keine kalibrierte Intensität"
                )
                data(LabData.PROFILE_START_X, first)
                data(LabData.PROFILE_SAMPLE_COUNT, profile.grayscale.size)
                data(LabData.PROFILE_MIN, profile.grayscale.min())
                data(LabData.PROFILE_MAX, profile.grayscale.max())
            },
        ) {
            listOf(0, 64, 128, 192, 255).forEach { tick ->
                SvgTag(
                    SvgTagName.LINE,
                    attrs = {
                        svgNumber(SvgNumber.X1, left)
                        svgNumber(SvgNumber.X2, (left + plotWidth))
                        svgNumber(SvgNumber.Y1, y(tick.toDouble()))
                        svgNumber(SvgNumber.Y2, y(tick.toDouble()))
                        styles(LabClass.PROFILE_GRID)
                    },
                )
                TypesetSvgLabel(
                    namespace = SVG_NS,
                    x = 54,
                    y = (y(tick.toDouble()) + 4),
                    attrs = {
                        textAnchor(TextAnchor.END)
                        styles(LabClass.PROFILE_LABEL)
                    },
                    text = tick.toString(),
                    inheritColor = true,
                )
            }
            listOf(first, first + 25, first + 50, first + 75, last).forEach { tick ->
                TypesetSvgLabel(
                    namespace = SVG_NS,
                    x = x(tick.toDouble()),
                    y = 240,
                    attrs = {
                        textAnchor(TextAnchor.MIDDLE)
                        styles(LabClass.PROFILE_LABEL)
                    },
                    text = tick.toString(),
                    inheritColor = true,
                )
            }
            (if (showReference) profile.markers else emptyList())
                .filter { it in first.toDouble()..last.toDouble() }
                .forEach { value ->
                    SvgTag(
                        SvgTagName.LINE,
                        attrs = {
                            svgNumber(SvgNumber.X1, x(value))
                            svgNumber(SvgNumber.X2, x(value))
                            svgNumber(SvgNumber.Y1, top)
                            svgNumber(SvgNumber.Y2, (top + plotHeight))
                            styles(LabClass.PROFILE_REFERENCE)
                        },
                    )
                }
            SvgTag(
                SvgTagName.PATH,
                attrs = {
                    svgPath(path)
                    styles(LabClass.PROFILE_CURVE)
                },
            )
            own.filter { it in first.toDouble()..last.toDouble() }
                .forEach { value ->
                    SvgTag(
                        SvgTagName.CIRCLE,
                        attrs = {
                            svgNumber(SvgNumber.CX, x(value))
                            svgNumber(
                                SvgNumber.CY,
                                y(profile.grayscale[value.roundToInt() - first]),
                            )
                            svgNumber(SvgNumber.RADIUS, 3)
                            styles(LabClass.PROFILE_OWN)
                        },
                    )
                }
            SvgTag(
                SvgTagName.LINE,
                attrs = {
                    elementId(MeasurementElement.CROSSHAIR_PROFILE)
                    data(LabData.NATIVE_X, sampleX.toDouble())
                    svgNumber(SvgNumber.X1, x(sampleX.toDouble()))
                    svgNumber(SvgNumber.X2, x(sampleX.toDouble()))
                    svgNumber(SvgNumber.Y1, top)
                    svgNumber(SvgNumber.Y2, (top + plotHeight))
                    styles(LabClass.PROFILE_CURSOR)
                },
            )
        }
        Div(attrs = { styles(LabClass.PROFILE_AXIS_LEGEND) }) {
            Span { Text("Grauwert 0…255 · Mittel über y-Band") }
            Span { Text("x / Originalpixel") }
        }
        Div(attrs = { styles(LabClass.PROFILE_SAMPLE_ROW) }) {
            FieldLabel(forId = Id.PROFILE_SAMPLE_X) { Text("Profil abtasten") }
            Input(
                InputType.Range,
                attrs = {
                    elementId(Id.PROFILE_SAMPLE_X)
                    minimum(first)
                    maximum(last)
                    increment(1)
                    value(sampleX)
                    onInput { event -> event.value?.toDouble()?.let { onSample(NativePixel(it)) } }
                },
            )
            Span(
                attrs = {
                    elementId(Id.PROFILE_SAMPLE)
                    data(LabData.NATIVE_X, sampleX.toDouble())
                    data(LabData.GRAY_VALUE, sample)
                }
            ) {
                Text("Profilprobe: x = $sampleX px · G = ${number(sample, 2)}")
            }
        }
    }
}

/** A local magnifying view beside the coordinate editor; never a resampled measurement source. */
@Composable
internal fun SelectedMarkerDetail(
    data: JonssonData,
    selectedX: Double,
    bandTop: Double,
    bandBottom: Double,
    profile: MeasurementProfile?,
) {
    val width = minOf(48.0, data.cropWidth)
    val left =
        (selectedX - width / 2.0).coerceIn(data.cropLeft, data.cropLeft + data.cropWidth - width)
    val height = minOf(58.0, data.height.toDouble())
    val top = ((bandTop + bandBottom - height) / 2.0).coerceIn(0.0, data.height - height)
    Div(
        attrs = {
            elementId(MeasurementElement.DETAIL)
            measurementStyles(MeasurementClass.DETAIL)
            data(LabData.NATIVE_X, selectedX)
        }
    ) {
        P(attrs = { styles(LabClass.SMALL) }) {
            Text("Detail der ausgewählten Marke · x = ${selectedX.toInt()} px")
        }
        Div(attrs = { measurementStyles(MeasurementClass.DETAIL_VIEWS) }) {
            SvgTag(
                SvgTagName.SVG,
                attrs = {
                    elementId(MeasurementElement.DETAIL_IMAGE)
                    viewBox(left, top, width, height)
                    measurementStyles(MeasurementClass.DETAIL_IMAGE)
                    role(AriaRole.IMAGE)
                    ariaLabel(
                        "Unverändertes Bilddetail um die ausgewählte Marke bei x ${selectedX.toInt()} Originalpixel"
                    )
                },
            ) {
                SvgTag(
                    SvgTagName.IMAGE,
                    attrs = {
                        svgHref(data.imageFilename)
                        svgNumber(SvgNumber.X, 0)
                        svgNumber(SvgNumber.Y, 0)
                        svgNumber(SvgNumber.WIDTH, data.width)
                        svgNumber(SvgNumber.HEIGHT, data.height)
                    },
                )
                SvgTag(
                    SvgTagName.LINE,
                    attrs = {
                        svgNumber(SvgNumber.X1, selectedX)
                        svgNumber(SvgNumber.X2, selectedX)
                        svgNumber(SvgNumber.Y1, top)
                        svgNumber(SvgNumber.Y2, top + height)
                        styles(LabClass.NATIVE_OWN, LabClass.SELECTED)
                    },
                )
            }
            profile?.let { values ->
                val first = maxOf(left.toInt(), values.profileStartX)
                val last =
                    minOf((left + width).toInt(), values.profileStartX + values.grayscale.lastIndex)
                if (last > first) {
                    val path =
                        (first..last).joinToString(" ") { x ->
                            "${if (x == first) "M" else "L"}${(x - left) / width * 240.0},${90.0 * (1.0 - values.grayscale[x - values.profileStartX] / 255.0)}"
                        }
                    SvgTag(
                        SvgTagName.SVG,
                        attrs = {
                            viewBox(0, 0, 240, 100)
                            measurementStyles(MeasurementClass.DETAIL_PROFILE)
                            role(AriaRole.IMAGE)
                            ariaLabel(
                                "Grauwertdetail im selben x-Ausschnitt, von 0 bis 255; keine kalibrierte Intensität"
                            )
                        },
                    ) {
                        SvgTag(
                            SvgTagName.PATH,
                            attrs = {
                                svgPath(path)
                                styles(LabClass.PROFILE_CURVE)
                            },
                        )
                        SvgTag(
                            SvgTagName.LINE,
                            attrs = {
                                svgNumber(SvgNumber.X1, (selectedX - left) / width * 240.0)
                                svgNumber(SvgNumber.X2, (selectedX - left) / width * 240.0)
                                svgNumber(SvgNumber.Y1, 0)
                                svgNumber(SvgNumber.Y2, 90)
                                styles(LabClass.PROFILE_CURSOR)
                            },
                        )
                    }
                }
            }
        }
        P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
            Text(
                "Gleicher Ausschnitt im Originalbild: ${left.toInt()} ≤ x ≤ ${(left + width).toInt()} px. Zahl und ±1 px unten korrigieren dieselbe Marke."
            )
        }
    }
}
