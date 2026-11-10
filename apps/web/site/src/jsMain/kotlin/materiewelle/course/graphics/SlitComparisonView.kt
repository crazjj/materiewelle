package materiewelle.course.graphics

import androidx.compose.runtime.*
import kotlin.math.roundToInt
import kotlin.math.sqrt
import materiewelle.NativeSvg
import materiewelle.core.*
import materiewelle.core.course.SlitModelComparison
import materiewelle.pages.number
import materiewelle.ui.FormulaId
import materiewelle.ui.FormulaPresentation
import materiewelle.ui.InlineFormula
import materiewelle.ui.MathText as Text
import materiewelle.ui.semantics.*
import materiewelle.ui.vector.*
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.HTMLElement

private const val SLIT_EXTENT_M = .003
private const val SLIT_SAMPLE_COUNT = 600

private data class SlitSample(val positionM: Double, val intensity: Double, val envelope: Double)

private fun sampleSlits(model: FiniteSlitModel, configuration: SlitConfiguration = SlitConfiguration.BOTH_COHERENT) =
    (0..SLIT_SAMPLE_COUNT).map { index ->
        val position = -SLIT_EXTENT_M + 2 * SLIT_EXTENT_M * index / SLIT_SAMPLE_COUNT
        SlitSample(position, model.relativeIntensity(ObservationPosition(position), configuration), model.relativeEnvelope(position))
    }

/**
 * Aperture, curve and strip use one model and one sampled coordinate axis. The fixed linear grey
 * mapping is an explanatory display, never a photograph, gamma fit or measured electron count.
 */
@Composable
fun SlitComparisonView(
    model: FiniteSlitModel,
    comparison: SlitModelComparison? = null,
    configuration: SlitConfiguration = SlitConfiguration.BOTH_COHERENT,
    probe: ObservationPosition? = null,
    onProbeChange: ((ObservationPosition) -> Unit)? = null,
    controls: @Composable () -> Unit = {},
) {
    val samples = remember(model, configuration) { sampleSlits(model, configuration) }
    var plot by remember { mutableStateOf<HTMLElement?>(null) }
    val savedModel =
        remember(comparison) {
            comparison?.let {
                FiniteSlitModel(
                    Voltage(it.voltageV),
                    Length(it.distanceM),
                    Length(it.spacingUm * 1e-6),
                    Length(it.widthUm * 1e-6),
                    WavelengthApproximation.NONRELATIVISTIC,
                )
            }
        }
    val savedSamples = remember(savedModel) { savedModel?.let(::sampleSlits) }
    Section(attrs = { geometryStyles(GeometrySlitsClass.SLITS) }) {
        P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
            Text(
                "Lehrmodell · ${number(model.voltage.volts, 0)} V · L = ${number(model.distance.meters, 0)} m · ${if (model.approximation == WavelengthApproximation.NONRELATIVISTIC) "nichtrelativistisch" else "relativistisch"}."
            )
        }
        Div(attrs = {
            elementId(GeometrySlitsId.SLIT_WORKSPACE)
            geometryStyles(GeometrySlitsClass.WORKSPACE)
        }) {
            Div(attrs = { geometryStyles(GeometrySlitsClass.SETTINGS) }) {
                Div(attrs = {
                    elementId(GeometrySlitsId.SLIT_APERTURE)
                    geometryStyles(GeometrySlitsClass.APERTURE)
                }) { NativeSvg(slitAperture(model, configuration)) }
                Div(attrs = {
                    elementId(GeometrySlitsId.SLIT_CONTROLS)
                    geometryStyles(GeometrySlitsClass.CONTROLS)
                }) { controls() }
            }
            Div(attrs = {
                elementId(GeometrySlitsId.SLIT_PROFILE)
                geometryStyles(GeometrySlitsClass.PLOT)
                focusIndex(0)
                role(AriaRole.GROUP)
                ariaLabel("Berechnete Modellkurve mit darunterliegendem Streifenband auf derselben Achse; auf schmalen Ansichten horizontal verschiebbar")
                ref { plot = it; onDispose { plot = null } }
                onClick { event ->
                    if (onProbeChange != null) plot?.let { element ->
                        slitPositionAtPointer(element, event.clientX.toDouble(), event.clientY.toDouble())?.let {
                            element.focusWithoutScrolling(); onProbeChange(it)
                        }
                    }
                }
                onKeyDown { event ->
                    if (onProbeChange != null && probe != null) {
                        val target = when (LabKey.fromBrowser(event.key)) {
                            LabKey.LEFT -> probe.meters - .00005
                            LabKey.RIGHT -> probe.meters + .00005
                            LabKey.HOME -> 0.0
                            else -> null
                        }
                        target?.let { event.preventDefault(); onProbeChange(ObservationPosition(it.coerceIn(-SLIT_EXTENT_M, SLIT_EXTENT_M))) }
                    }
                }
            }) { NativeSvg(slitProfile(model, samples, savedModel, savedSamples, configuration, probe)) }
        }
        Div(
            attrs = {
                elementId(GeometrySlitsId.SLIT_READOUT)
                geometryStyles(GeometrySlitsClass.READOUT)
                role(AriaRole.STATUS)
            }
        ) {
            P {
                Text(
                    "${if (savedModel == null) "Aktuell" else "B · aktuell"}: g = ${number(model.slitSpacing.micrometers, 2)} µm · b = ${number(model.slitWidth.micrometers, 2)} µm · λ = ${number(model.wavelength.picometers, 3)} pm"
                )
            }
            P {
                Text(
                    "${if (configuration == SlitConfiguration.BOTH_COHERENT) "Paraxialer Interferenzmaßstab" else "Kohärenter Vergleichsmaßstab (hier keine Interferenzstreifen)"} Δx ≈ Lλ/g = ${number(model.paraxialFringeSpacing.micrometers, 3)} µm. ${firstEnvelopeZero(model)?.let { "Erste Hüllnullstelle |x₁| = ${number(it.meters * 1000, 3)} mm." } ?: "Keine Hüllnullstelle bei einem endlichen Beobachtungswinkel erreichbar."}"
                )
            }
            savedModel?.let { saved ->
                P {
                    Text(
                        "A · gespeichert: g = ${number(saved.slitSpacing.micrometers, 2)} µm · b = ${number(saved.slitWidth.micrometers, 2)} µm · Δx ≈ ${number(saved.paraxialFringeSpacing.micrometers, 3)} µm. Verhältnis Δx(B)/Δx(A) = ${number(model.paraxialFringeSpacing.meters / saved.paraxialFringeSpacing.meters, 3)}."
                    )
                }
            }
        }
        Div(attrs = { geometryStyles(GeometrySlitsClass.KEY) }) {
            P {
                Text(
                    "Durchgezogen: ${if (savedModel == null) "aktuell" else "B · aktuell"}, ${configuration.label()}. Lang gestrichelt: Beugungshülle, auf 1 normiert. ${if (savedModel == null) "" else "Gepunktet: gespeicherter kohärenter Zweispaltfall A."}"
                )
            }

        }
        Details(attrs = { styles(LabClass.NOTES) }) {
            Summary() { Text("Was das Modell voraussetzt und welche Maxima gemeint sind") }
            P {
                Text(
                    "Streifenband: feste relative Modellintensitäten als Grauwerte, 0 auf Schwarz und 1 auf Weiß. Die 1 bezeichnet immer das Zentralmaximum bei beiden kohärenten Spalten; ein einzelner Spalt erreicht dort 0,25, beide ohne Interferenz 0,5. Jeder Streifen und der Kurvenpunkt verwenden denselben Wert. Kein historisches Foto, keine gemessenen Grauwerte."
                )
            }
            P {
                Text(
                    "${when (configuration) { SlitConfiguration.BOTH_COHERENT -> "Identische, kohärent beleuchtete Rechteckspalte"; SlitConfiguration.BOTH_INCOHERENT -> "Identische Rechteckspalte ohne feste relative Phasenbeziehung; ihre Einzelintensitäten werden addiert"; else -> "Eine offene Rechtecköffnung, die andere ist abgedeckt" }}; monochromatisch und ideales Fraunhofer-Modell. b bezeichnet die Breite jeder Öffnung, g den Abstand ihrer Mitten. Die Zeichnung zeigt eine Blende von vorn und eine Beobachtungsebene; keine Elektronenbahnen."
                )
            }
            P {
                Text(
                    "Δx kennzeichnet den paraxialen Abstand des isolierten Interferenzfaktors cos²β. Die Maxima des gesamten Produkts können durch die Hülle verschoben oder unterdrückt werden. Die erste Hüllnullstelle erfüllt "
                )
                InlineFormula(FormulaId.ENVELOPE, FormulaPresentation.TEXT_SYMBOL)
                Text(". Im Modell ergibt sich ihre Schirmposition aus x = L tan θ.")
            }
            P {
                Text(
                    "Fernfeldkontrolle mit der gesamten Breite beider Spalte samt Zwischenraum: F = (g+b)²/(λL) = ${number(model.fullApertureFresnelNumber, 4)}. Gefordert ist F ≪ 1; dies prüft weder Kohärenz noch historische Linsenoptik oder einen Fotomaßstab."
                )
            }
            P {
                Text(
                    "Das Originalfoto und dessen relatives Rasterprofil bleiben eigenständige Belege. Die hier frei gewählte Entfernung darf nicht als Kalibrierung des Jönsson-Scans verwendet werden."
                )
            }
        }
    }
}

private fun firstEnvelopeZero(model: FiniteSlitModel): Length? {
    val ratio = model.wavelength.meters / model.slitWidth.meters
    // Course controls guarantee lambda < b. A different input model may have no accessible zero.
    return if (ratio < 1) Length(model.distance.meters * ratio / sqrt(1 - ratio * ratio)) else null
}

private fun slitAperture(model: FiniteSlitModel, configuration: SlitConfiguration): VectorFragment {
    // Fixed scale throughout the course control range: changing g never changes the drawn b.
    val scale = 50.0
    val width = model.slitWidth.micrometers * scale
    val gap = model.slitSpacing.micrometers * scale
    val first = 260 - gap / 2
    val second = 260 + gap / 2
    return vector {
        svg({
            viewBox(0, 0, 520, 220)
            imageRole()
            ariaLabel(
                "Blende von vorn: ${configuration.label()}. Jede Öffnung hat Breite b ${number(model.slitWidth.micrometers, 2)} Mikrometer; Mittenabstand g ${number(model.slitSpacing.micrometers, 2)} Mikrometer. Gemeinsame schematische Darstellungsgröße, beschriftete Werte sind maßgeblich."
            )
        }) {
            title("Blende · Breite b und Mittenabstand g · ${configuration.label()}")
            rect({
                atX(65)
                atY(74)
                width(390)
                height(98)
                cornerRadius(4)
                fill(VectorPaint.SURFACE_RAISED)
                stroke(VectorPaint.BORDER)
            })
            rect({
                atX(first - width / 2)
                atY(74)
                width(width)
                height(98)
                fill(if (configuration == SlitConfiguration.RIGHT_ONLY) VectorPaint.SURFACE_RAISED else VectorPaint.ACCENT_SOFT)
                stroke(if (configuration == SlitConfiguration.RIGHT_ONLY) VectorPaint.MUTED else VectorPaint.ACCENT)
                strokeWidth(2)
            })
            rect({
                atX(second - width / 2)
                atY(74)
                width(width)
                height(98)
                fill(if (configuration == SlitConfiguration.LEFT_ONLY) VectorPaint.SURFACE_RAISED else VectorPaint.ACCENT_SOFT)
                stroke(if (configuration == SlitConfiguration.LEFT_ONLY) VectorPaint.MUTED else VectorPaint.ACCENT)
                strokeWidth(2)
            })
            path({
                geometry("M$first 65 V183 M$second 65 V183")
                stroke(VectorPaint.MUTED)
                dash(4, 4)
            })
            path({
                geometry("M$first 37 H$second M$first 30 V44 M$second 30 V44")
                stroke(VectorPaint.TEXT)
                strokeWidth(2)
            })
            label(
                "g = ${number(model.slitSpacing.micrometers, 2)} µm · Mittenabstand",
                {
                    atX(260)
                    atY(24)
                    anchor(TextAnchor.MIDDLE)
                    fill(VectorPaint.TEXT)
                    fontSize(25)
                },
            )
            path({
                geometry(
                    "M${first-width/2} 192 H${first+width/2} M${first-width/2} 186 V198 M${first+width/2} 186 V198 M${second-width/2} 192 H${second+width/2} M${second-width/2} 186 V198 M${second+width/2} 186 V198"
                )
                fill(VectorPaint.None)
                stroke(VectorPaint.REFERENCE)
                strokeWidth(2)
            })
            label(
                "b = ${number(model.slitWidth.micrometers, 2)} µm · je Öffnung",
                {
                    atX(260)
                    atY(216)
                    anchor(TextAnchor.MIDDLE)
                    fill(VectorPaint.REFERENCE)
                    fontSize(25)
                },
            )

        }
    }
}

private fun slitProfile(
    model: FiniteSlitModel,
    samples: List<SlitSample>,
    saved: FiniteSlitModel?,
    savedSamples: List<SlitSample>?,
    configuration: SlitConfiguration,
    probe: ObservationPosition?,
): VectorFragment = vector {
    val left = 62.0
    val width = 550.0
    val bottom = 235.0
    val height = 180.0
    fun x(position: Double) = left + (position + SLIT_EXTENT_M) / (2 * SLIT_EXTENT_M) * width
    fun path(values: List<SlitSample>, envelope: Boolean) =
        values
            .mapIndexed { index, point ->
                "${if (index == 0) "M" else "L"}${x(point.positionM)} ${bottom - height * (if (envelope) point.envelope else point.intensity)}"
            }
            .joinToString(" ")
    svg({
        viewBox(0, 0, 640, if (saved == null) 385 else 440)
        imageRole()
        ariaLabel(
            "Relative Modellintensität: ${configuration.label()}, mit Beugungshülle im Bereich von minus drei bis plus drei Millimeter. Darunter dasselbe Intensitätsprofil als synthetisches Streifenband${if (saved != null) "; A bleibt der gespeicherte kohärente Zweispaltfall" else ""}."
        )
    }) {
        title("Ein Modell · Kurve und Streifen auf derselben x-Achse")
        rect({
            width(640)
            height(if (saved == null) 385 else 440)
            fill(VectorPaint.SURFACE)
        })
        label(
            "Relative Modellintensität · feste Skala",
            {
                atX(62)
                atY(25)
                fill(VectorPaint.TEXT)
                fontSize(18)
            },
        )
        listOf(0.0, .5, 1.0).forEach { value ->
            val y = bottom - height * value
            run {
                path({
                    geometry("M$left $y H${left+width}")
                    stroke(VectorPaint.GRID)
                })
                label(
                    number(value, 1),
                    {
                        atX(49)
                        atY(y + 5)
                        anchor(TextAnchor.END)
                        fill(VectorPaint.MUTED)
                        fontSize(17)
                    },
                )
            }
        }
        for (mm in -3..3) {
            val px = x(mm * .001)
            run {
                path({
                    geometry("M$px 45 V$bottom")
                    stroke(VectorPaint.GRID)
                })
                label(
                    mm,
                    {
                        atX(px)
                        atY(258)
                        anchor(TextAnchor.MIDDLE)
                        fill(VectorPaint.MUTED)
                        fontSize(17)
                    },
                )
            }
        }
        run {
            path({
                geometry("M$left 45 V$bottom H${left+width}")
                stroke(VectorPaint.MUTED)
                fill(VectorPaint.None)
            })
            path({
                geometry(path(samples, true))
                stroke(VectorPaint.MUTED)
                dash(9, 5)
                strokeWidth(2)
                fill(VectorPaint.None)
            })
        }
        savedSamples?.let {
            run {
                path({
                    geometry(path(it, false))
                    stroke(VectorPaint.REFERENCE)
                    dash(2, 5)
                    strokeWidth(2)
                    fill(VectorPaint.None)
                })
            }
        }
        run {
            path({
                elementId(SlitLabId.CURVE)
                geometry(path(samples, false))
                stroke(VectorPaint.ACCENT)
                strokeWidth(2.3)
                fill(VectorPaint.None)
            })
        }
        val fringe = model.paraxialFringeSpacing.meters
        if (configuration == SlitConfiguration.BOTH_COHERENT && fringe <= SLIT_EXTENT_M)
            run {
                path({
                    geometry(
                        "M${x(0.0)} 284 H${x(fringe)} M${x(0.0)} 279 V289 M${x(fringe)} 279 V289"
                    )
                    fill(VectorPaint.None)
                    stroke(VectorPaint.ACCENT)
                    strokeWidth(2)
                })
                label(
                    "Δx ≈ Lλ/g",
                    {
                        atX(x(fringe) + 8)
                        atY(289)
                        fill(VectorPaint.ACCENT)
                        fontSize(17)
                    },
                )
            }
        val zero = firstEnvelopeZero(model)
        if (zero != null && zero.meters <= SLIT_EXTENT_M)
            run {
                path({
                    geometry("M${x(zero.meters)} 221 V244")
                    stroke(VectorPaint.MUTED)
                    strokeWidth(2)
                })
                label(
                    "x₁ · Hüllnullstelle",
                    {
                        atX(x(zero.meters))
                        atY(210)
                        anchor(TextAnchor.MIDDLE)
                        fill(VectorPaint.MUTED)
                        fontSize(17)
                    },
                )
            }
        fun strip(values: List<SlitSample>, top: Double, label: String) {
            run {
                label(
                    "$label · Modellintensität in Graustufen",
                    {
                        atX(left)
                        atY(top - 8)
                        fill(VectorPaint.TEXT)
                        fontSize(17)
                    },
                )
                rect({
                    atX(left)
                    atY(top)
                    width(width)
                    height(30)
                    fill(VectorPaint.BLACK)
                })
            }
            // The same sample index produces the path vertex and its strip cell; no second
            // intensity model.
            values.forEachIndexed { index, value ->
                val gray = (value.intensity * 255).roundToInt().coerceIn(0, 255)
                val cellLeft =
                    if (index == 0) left else x(value.positionM) - width / SLIT_SAMPLE_COUNT / 2
                val cellWidth =
                    if (index == 0 || index == values.lastIndex) width / SLIT_SAMPLE_COUNT / 2
                    else width / SLIT_SAMPLE_COUNT
                run {
                    rect({
                        atX(cellLeft)
                        atY(top)
                        width(cellWidth + .05)
                        height(30)
                        fill(VectorPaint.rgb(gray, gray, gray))
                    })
                }
            }
            run {
                rect({
                    atX(left)
                    atY(top)
                    width(width)
                    height(30)
                    fill(VectorPaint.None)
                    stroke(VectorPaint.MUTED)
                })
            }
        }
        strip(samples, 321.0, if (saved == null) "Aktuell" else "B · aktuell")
        if (savedSamples != null) strip(savedSamples, 377.0, "A · gespeichert")
        probe?.let { selected ->
            line({ elementId(SlitLabId.PROBE); x1(x(selected.meters)); x2(x(selected.meters)); y1(45); y2(351)
                stroke(VectorPaint.REFERENCE); strokeWidth(2); dash(4, 3) })
            circle({ cx(x(selected.meters)); cy(bottom - height * model.relativeIntensity(selected, configuration)); radius(4)
                fill(VectorPaint.REFERENCE); stroke(VectorPaint.TEXT); strokeWidth(1) })
        }
        label(
            "Gemeinsame Koordinate x / mm",
            {
                atX(612)
                atY(if (saved == null) 377 else 433)
                anchor(TextAnchor.END)
                fill(VectorPaint.TEXT)
                fontSize(17)
            },
        )
    }
}
