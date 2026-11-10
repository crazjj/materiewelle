package materiewelle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import materiewelle.core.DgSetting
import materiewelle.pages.number
import materiewelle.ui.semantics.*
import materiewelle.ui.vector.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.Div

@Composable
fun NativeSvg(drawing: VectorFragment, cssClass: LabClass = LabClass.CHART) {
    key(drawing) {
        Div(
            attrs = {
                styles(cssClass)
                ref { element ->
                    var stopObserving: () -> Unit = {}
                    fun render() {
                        stopObserving()
                        element.textContent = ""
                        element.appendChild(renderVector(drawing))
                        layoutTypesetLabels(element)
                        stopObserving = observeTypesetLabels(element)
                    }
                    render()
                    val cancelFontLayout = afterFontMetricsReady(::render)
                    onDispose { cancelFontLayout(); stopObserving() }
                }
            }
        )
    }
}

/** Fixed viewBox coordinates are also used by the typed chart cursor in DeBroglie.kt. */
fun wavelengthChart(
    voltage: Double,
    comparison: Double? = null,
    cursor: Double? = null,
): VectorFragment {
    val w = 760.0
    val h = 420.0
    val l = 64.0
    val r = 22.0
    val t = 20.0
    val b = 54.0
    fun x(v: Double) = l + v / 1000.0 * (w - l - r)
    fun y(pm: Double) = h - b - pm / 300.0 * (h - t - b)
    fun pm(v: Double) = Physics.wavelengthNonRelativistic(v) * 1e12
    val path =
        (20..1000 step 2).joinToString(" ") { u ->
            "${if (u == 20) "M" else "L"}${x(u.toDouble())},${y(pm(u.toDouble()))}"
        }
    fun labelPosition(v: Double): Pair<Double, Double> {
        val px = x(v)
        return (if (px > 520.0) px - 199.0 else px + 13.0) to
            (y(pm(v)) - 42.0).coerceAtLeast(t + 2.0)
    }
    val currentLabel = labelPosition(voltage)
    val referenceLabelY =
        comparison?.let { saved ->
            val proposed = labelPosition(saved)
            val overlaps =
                proposed.first < currentLabel.first + 193.0 &&
                    proposed.first + 193.0 > currentLabel.first &&
                    proposed.second < currentLabel.second + 32.0 &&
                    proposed.second + 32.0 > currentLabel.second
            if (overlaps) {
                if (currentLabel.second >= t + 40.0) currentLabel.second - 36.0
                else currentLabel.second + 36.0
            } else proposed.second
        }
    fun point(
        v: Double,
        label: String,
        color: VectorPaint,
        filled: Boolean,
        labelYOverride: Double? = null,
    ): VectorFragment {
        val px = x(v)
        val py = y(pm(v))
        val labelX = labelPosition(v).first
        val labelY = labelYOverride ?: labelPosition(v).second
        return vector {
            path({
                geometry("M$l,$py H$px V${h-b}")
                fill(VectorPaint.None)
                stroke(color)
                strokeWidth(1.3)
                dash(5, 5)
                opacity(0.8)
            })
            circle({
                cx(px)
                cy(py)
                radius(6)
                fill(if (filled) color else VectorPaint.SURFACE)
                stroke(color)
                strokeWidth(2.5)
            })
            rect({
                styles(LabClass.CHART_VALUE_LABEL)
                atX(labelX)
                atY(labelY)
                width(189)
                height(28)
                cornerRadius(4)
                fill(VectorPaint.SURFACE_RAISED)
                stroke(color)
                strokeOpacity(0.55)
            })
            label(
                "$label ${number(v, 1)} V · ${number(pm(v), 2)} pm",
                {
                    styles(LabClass.CHART_VALUE_LABEL)
                    atX(labelX + 9)
                    atY(labelY + 19)
                    fill(VectorPaint.TEXT)
                    fontSize(12)
                    font(VectorFont.MONO)
                },
            )
        }
    }
    return vector {
        svg({
            viewBox(0, 0, w, h)
            imageRole()
            ariaLabel(
                "Berechnete De-Broglie-Modellkurve. Einstellung A: ${number(voltage, 1)} Volt, ${number(pm(voltage), 4)} Pikometer${comparison?.let { "; Einstellung B: ${number(it, 1)} Volt, ${number(pm(it), 4)} Pikometer" } ?: ""}."
            )
        }) {
            title("De-Broglie-Wellenlänge gegen Beschleunigungsspannung")
            for (u in 0..1000 step 200) run {
                line({
                    x1(x(u.toDouble()))
                    y1(t)
                    x2(x(u.toDouble()))
                    y2(h - b)
                    stroke(VectorPaint.GRID)
                })
                label(
                    u,
                    {
                        atX(x(u.toDouble()))
                        atY(h - b + 25)
                        anchor(if (u == 1000) TextAnchor.END else TextAnchor.MIDDLE)
                        fill(VectorPaint.MUTED)
                        fontSize(14)
                        font(VectorFont.MONO)
                    },
                )
            }
            for (value in 0..300 step 50) run {
                line({
                    x1(l)
                    y1(y(value.toDouble()))
                    x2(w - r)
                    y2(y(value.toDouble()))
                    stroke(VectorPaint.GRID)
                })
                label(
                    value,
                    {
                        atX(l - 12)
                        atY(y(value.toDouble()) + 5)
                        anchor(TextAnchor.END)
                        fill(VectorPaint.MUTED)
                        fontSize(14)
                        font(VectorFont.MONO)
                    },
                )
            }
            run {
                line({
                    x1(l)
                    y1(t)
                    x2(l)
                    y2(h - b)
                    stroke(VectorPaint.MUTED)
                })
                line({
                    x1(l)
                    y1(h - b)
                    x2(w - r)
                    y2(h - b)
                    stroke(VectorPaint.MUTED)
                })
                path({
                    geometry(path)
                    fill(VectorPaint.None)
                    stroke(VectorPaint.ACCENT)
                    strokeWidth(2.5)
                })
            }
            comparison
                ?.takeIf { it != voltage }
                ?.let { include(point(it, "B", VectorPaint.REFERENCE, false, referenceLabelY)) }
            include(
                point(voltage, if (comparison == voltage) "A=B" else "A", VectorPaint.ACCENT, true)
            )
            if (comparison == voltage)
                run {
                    circle({
                        cx(x(voltage))
                        cy(y(pm(voltage)))
                        radius(10)
                        fill(VectorPaint.None)
                        stroke(VectorPaint.REFERENCE)
                        strokeWidth(2)
                    })
                }
            cursor?.let { v ->
                run {
                    line({
                        x1(x(v))
                        y1(t)
                        x2(x(v))
                        y2(h - b)
                        stroke(VectorPaint.MUTED)
                        dash(2, 5)
                    })
                    circle({
                        cx(x(v))
                        cy(y(pm(v)))
                        radius(3.5)
                        fill(VectorPaint.TEXT)
                    })
                }
            }
            label(
                "Spannung U / V",
                {
                    atX(w - r)
                    atY(h - 7)
                    anchor(TextAnchor.END)
                    fill(VectorPaint.MUTED)
                    fontSize(14)
                },
            )
        }
    }
}

/** Normal-incidence apparatus schematic; no SVG IDs, no invented detector intensity. */
enum class DgSetupFocus { SOURCE, CRYSTAL, DETECTOR }

fun detectorChart(degrees: Double, comparison: Double? = null, focus: DgSetupFocus? = null, showAngle: Boolean = true): VectorFragment {
    require(degrees.isFinite() && degrees in 0.0..90.0)
    val ox = 280.0
    val oy = 320.0
    fun px(a: Double) = ox + 225.0 * sin(a * PI / 180.0)
    fun py(a: Double) = oy - 225.0 * cos(a * PI / 180.0)
    val ax = ox + 69.0 * sin(degrees * PI / 180.0)
    val ay = oy - 69.0 * cos(degrees * PI / 180.0)
    return vector {
        svg({
            viewBox(0, 0, 700, 420)
            imageRole()
            ariaLabel(
                "Nickelversuch als Aufbau-Schema: Elektronenquelle, Beschleunigung durch U, normaler Einfall auf Ni111, beweglicher Detektor. Winkel Phi zur äußeren Normalen${if (showAngle) ": ${number(degrees, 1)} Grad" else ""}${comparison?.let { ", Vergleich ${number(it, 1)} Grad" } ?: ""}. Keine Intensitätskurve."
            )
        }) {
            title("Davisson–Germer: Aufbau und Winkel")
            rect({
                atX(80)
                atY(320)
                width(525)
                height(45)
                fill(VectorPaint.SURFACE_RAISED)
                stroke(VectorPaint.GRID)
            })
            line({
                x1(80)
                y1(320)
                x2(605)
                y2(320)
                stroke(VectorPaint.MUTED)
                strokeWidth(2)
            })
            label(
                "Nickel-Einkristall · Oberfläche (111)",
                {
                    atX(344)
                    atY(391)
                    anchor(TextAnchor.MIDDLE)
                    fontSize(20)
                    fill(VectorPaint.TEXT)
                },
            )
            line({
                x1(ox)
                y1(oy)
                x2(ox)
                y2(30)
                stroke(VectorPaint.MUTED)
                dash(6, 6)
            })
            label(
                "äußere Normale",
                {
                    atX(298)
                    atY(37)
                    fontSize(20)
                    fill(VectorPaint.MUTED)
                },
            )
            rect({
                atX(234)
                atY(55)
                width(92)
                height(63)
                cornerRadius(7)
                fill(VectorPaint.SURFACE_RAISED)
                stroke(VectorPaint.MUTED)
            })
            line({
                x1(247)
                y1(76)
                x2(313)
                y2(76)
                stroke(VectorPaint.TEXT)
                strokeWidth(3)
            })
            path({
                geometry("M247 101 H270 M290 101 H313")
                stroke(VectorPaint.TEXT)
                strokeWidth(3)
            })
            label(
                "Elektronenquelle",
                {
                    atX(218)
                    atY(71)
                    anchor(TextAnchor.END)
                    fontSize(20)
                    fill(VectorPaint.TEXT)
                },
            )
            label(
                "Spannung U",
                {
                    atX(218)
                    atY(100)
                    anchor(TextAnchor.END)
                    fontSize(20)
                    fill(VectorPaint.TEXT)
                },
            )
            line({
                x1(ox)
                y1(104)
                x2(ox)
                y2(306)
                stroke(VectorPaint.ACCENT)
                strokeWidth(3)
            })
            path({
                geometry("M274 296 L280 307 L286 296")
                fill(VectorPaint.None)
                stroke(VectorPaint.ACCENT)
                strokeWidth(3)
            })
            label(
                "normaler Einfall",
                {
                    atX(260)
                    atY(169)
                    anchor(TextAnchor.END)
                    fontSize(20)
                    fill(VectorPaint.ACCENT)
                },
            )
            comparison?.let {
                run {
                    line({
                        x1(ox)
                        y1(oy)
                        x2(px(it))
                        y2(py(it))
                        stroke(VectorPaint.REFERENCE)
                        strokeWidth(2)
                        dash(6, 5)
                    })
                    circle({
                        cx(px(it))
                        cy(py(it))
                        radius(9)
                        fill(VectorPaint.SURFACE)
                        stroke(VectorPaint.REFERENCE)
                        strokeWidth(2.5)
                    })
                }
            }
            line({
                x1(ox)
                y1(oy)
                x2(px(degrees))
                y2(py(degrees))
                stroke(VectorPaint.ACCENT)
                strokeWidth(3)
            })
            circle({
                cx(px(degrees))
                cy(py(degrees))
                radius(10)
                fill(VectorPaint.ACCENT)
            })
            label(
                "Detektor",
                {
                    atX(px(degrees) + 17)
                    atY(py(degrees) - 14)
                    fontSize(20)
                    fill(VectorPaint.TEXT)
                },
            )
            path({
                geometry("M280 251 A69 69 0 0 1 $ax $ay")
                fill(VectorPaint.None)
                stroke(VectorPaint.ACCENT)
                strokeWidth(2)
            })
            label(
                if (showAngle) "φ = ${number(degrees, 1)}°" else "φ",
                {
                    atX(309)
                    atY(241)
                    font(VectorFont.MONO)
                    fontSize(20)
                    fill(VectorPaint.ACCENT)
                },
            )
            when (focus) {
                DgSetupFocus.SOURCE -> rect({ atX(226); atY(47); width(108); height(79); cornerRadius(9); fill(VectorPaint.None); stroke(VectorPaint.ACCENT); strokeWidth(2.5) })
                DgSetupFocus.CRYSTAL -> rect({ atX(74); atY(314); width(537); height(57); cornerRadius(5); fill(VectorPaint.None); stroke(VectorPaint.ACCENT); strokeWidth(2.5) })
                DgSetupFocus.DETECTOR -> circle({ cx(px(degrees)); cy(py(degrees)); radius(20); fill(VectorPaint.None); stroke(VectorPaint.ACCENT); strokeWidth(2.5) })
                null -> Unit
            }
        }
    }
}

/**
 * Computed geometric values of the current/saved setting, alongside the independent lambda(U)
 * model.
 */
fun dgWavelengthChart(
    points: List<DgSetting>,
    spacingM: Double,
    selected: Int,
    comparison: Int? = null,
): VectorFragment {
    val left = 76.0
    val right = 670.0
    val top = 30.0
    val bottom = 346.0
    val lowestU = points.minOf { it.voltageV }
    val highestU = points.maxOf { it.voltageV }
    val paddingU = ((highestU - lowestU) * 0.1).coerceAtLeast(lowestU * 0.01)
    var minU = (lowestU - paddingU).coerceAtLeast(lowestU * 0.5).coerceAtLeast(Double.MIN_VALUE)
    var maxU = (highestU + paddingU).takeIf { it.isFinite() } ?: highestU
    // A one-point subnormal range can round to zero width. Use an adjacent representable
    // positive value instead; neither a zero voltage nor an infinite endpoint enters Physics.
    if (maxU <= minU) {
        if (minU < Double.MAX_VALUE) maxU = Double.fromBits(minU.toBits() + 1L)
        else minU = Double.fromBits(maxU.toBits() - 1L)
    }
    val allPm =
        points.flatMap {
            listOf(
                Physics.wavelengthNonRelativistic(it.voltageV) * 1e12,
                Physics.surfaceDiffractionWavelength(spacingM, it.angleDegrees) * 1e12,
            )
        }
    val minPm = allPm.min() - 8.0
    val maxPm = allPm.max() + 8.0
    fun x(u: Double) = left + (u - minU) / (maxU - minU) * (right - left)
    fun y(pm: Double) = bottom - (pm - minPm) / (maxPm - minPm) * (bottom - top)
    fun voltageAt(fraction: Double) = (minU + fraction * (maxU - minU)).coerceIn(minU, maxU)
    return vector {
        svg({
            viewBox(0, 0, 700, 420)
            imageRole()
            ariaLabel(
                "Modellvorhersage und aus ${points.size} gewählten Einstellungen berechnete Beugungswellenlängen. Einstellungen sind Modellvariationen; nur der gesonderte D2-Fall hat Quellenstatus."
            )
        }) {
            title("Wellenlängenvergleich: geometrische Rechnung und Modell")
            for (tick in 0..4) {
                val u = voltageAt(tick / 4.0)
                val pm = minPm + (tick / 4.0) * (maxPm - minPm)
                run {
                    line({
                        x1(x(u))
                        y1(top)
                        x2(x(u))
                        y2(bottom)
                        stroke(VectorPaint.GRID)
                    })
                    label(
                        number(u, 1),
                        {
                            atX(x(u))
                            atY(372)
                            anchor(TextAnchor.MIDDLE)
                            fill(VectorPaint.MUTED)
                            fontSize(14)
                            font(VectorFont.MONO)
                        },
                    )
                    line({
                        x1(left)
                        y1(y(pm))
                        x2(right)
                        y2(y(pm))
                        stroke(VectorPaint.GRID)
                    })
                    label(
                        number(pm, 0),
                        {
                            atX(62)
                            atY(y(pm) + 5)
                            anchor(TextAnchor.END)
                            fill(VectorPaint.MUTED)
                            fontSize(14)
                            font(VectorFont.MONO)
                        },
                    )
                }
            }
            val path =
                (0..120).joinToString(" ") { n ->
                    val u = voltageAt(n / 120.0)
                    "${if (n == 0) "M" else "L"}${x(u)},${y(Physics.wavelengthNonRelativistic(u) * 1e12)}"
                }
            run {
                path({
                    geometry(path)
                    fill(VectorPaint.None)
                    stroke(VectorPaint.ACCENT)
                    strokeWidth(2.5)
                })
            }
            points.forEachIndexed { index, point ->
                val px = x(point.voltageV)
                val measuredY =
                    y(Physics.surfaceDiffractionWavelength(spacingM, point.angleDegrees) * 1e12)
                val color = if (index == comparison) VectorPaint.REFERENCE else VectorPaint.TEXT
                run {
                    line({
                        x1(px)
                        y1(measuredY)
                        x2(px)
                        y2(y(Physics.wavelengthNonRelativistic(point.voltageV) * 1e12))
                        stroke(VectorPaint.MUTED)
                        dash(3, 3)
                    })
                    circle({
                        cx(px)
                        cy(measuredY)
                        radius(if (index == selected || index == comparison) 6 else 4)
                        fill(VectorPaint.SURFACE)
                        stroke(color)
                        strokeWidth(if (index == selected) 3 else 2)
                    })
                }
            }
            label(
                "λ / pm",
                {
                    styles(LabClass.CHART_AXIS_HEADING)
                    atX(76)
                    atY(22)
                    fontSize(14)
                    fill(VectorPaint.MUTED)
                },
            )
            label(
                "Beschleunigungsspannung U / V",
                {
                    atX(670)
                    atY(405)
                    anchor(TextAnchor.END)
                    fontSize(14)
                    fill(VectorPaint.MUTED)
                },
            )
        }
    }
}
