package materiewelle.course

import kotlin.math.*
import materiewelle.core.*
import materiewelle.ui.semantics.LabClass
import materiewelle.ui.semantics.TextAnchor
import materiewelle.ui.vector.*

private fun graphic(title: String, content: VectorFragment): VectorFragment = vector {
    svg({
        viewBox(0, 0, 760, 350)
        styles(LabClass.COURSE_SVG)
        imageRole()
        ariaLabel(title)
    }) {
        title(title)
        rect({
            width(760)
            height(350)
            fill(VectorPaint.SURFACE)
        })
        include(content)
    }
}

fun energyDiagram(stage: Int): VectorFragment =
    graphic(
        "Von Spannung über Energie und Impuls zur Wellenlänge; drei begründete Schritte",
        vector {
            val labels = listOf("Energie gewinnen", "Impuls bestimmen", "Hypothese einsetzen")
            val equations = listOf("E_kin = eU", "p = √(2mₑeU)", "λ = h/p")
            repeat(3) { i ->
                val x = 30 + i * 245
                val active = i <= stage
                run {
                    rect({
                        atX(x)
                        atY(100)
                        width(210)
                        height(135)
                        cornerRadius(8)
                        fill(if (active) VectorPaint.ACCENT_SOFT else VectorPaint.SURFACE_RAISED)
                        stroke(if (active) VectorPaint.ACCENT else VectorPaint.BORDER)
                    })
                    label(
                        "0${i + 1} · ${labels[i]}",
                        {
                            atX(x + 18)
                            atY(132)
                            fill(VectorPaint.MUTED)
                            fontSize(15)
                        },
                    )
                }
                if (active)
                    run {
                        label(
                            equations[i],
                            {
                                atX(x + 105)
                                atY(187)
                                anchor(TextAnchor.MIDDLE)
                                fill(VectorPaint.TEXT)
                                fontSize(23)
                            },
                        )
                    }
                if (i < 2)
                    run {
                        path({
                            geometry("M ${x + 215} 167 h 23 m -7 -7 l 7 7 l -7 7")
                            fill(VectorPaint.None)
                            stroke(VectorPaint.MUTED)
                            strokeWidth(2)
                        })
                    }
            }
            run {
                label(
                    "Start aus Ruhe · positiver Spannungsbetrag U · nichtrelativistisch",
                    {
                        atX(30)
                        atY(50)
                        fill(VectorPaint.MUTED)
                        fontSize(16)
                    },
                )
                label(
                    "Energieerhaltung allein beweist λ = h/p nicht.",
                    {
                        atX(30)
                        atY(291)
                        fill(VectorPaint.TEXT)
                        fontSize(16)
                    },
                )
            }
        },
    )

fun classicalWaves(wavelength: Double, amplitude: Double = 38.0): VectorFragment =
    graphic(
        "Klassischer Vergleich: gleiche Amplitude, verschiedene Wellenlängen",
        vector {
            for ((row, period) in listOf(wavelength, wavelength * 2).withIndex()) {
                val center = 105 + row * 140
                run {
                    path({
                        geometry("M40 $center H720")
                        stroke(VectorPaint.BORDER)
                    })
                }
                val d =
                    (0..340).joinToString(" ") { n ->
                        val x = 40.0 + n * 2
                        "${if(n == 0) "M" else "L"} $x ${center - amplitude * sin(2 * PI * (x - 40) / period)}"
                    }
                run {
                    path({
                        geometry(d)
                        fill(VectorPaint.None)
                        stroke(if (row == 0) VectorPaint.ACCENT else VectorPaint.REFERENCE)
                        strokeWidth(3)
                    })
                    label(
                        "${if(row == 0) "λ" else "2λ"} · gleiche Amplitude A",
                        {
                            atX(40)
                            atY(center - 58)
                            fill(VectorPaint.TEXT)
                            fontSize(16)
                        },
                    )
                }
            }
            run {
                label(
                    "Klassisches Wellenbild, keine gemessene Elektronenbahn und kein Elektronenmedium.",
                    {
                        atX(40)
                        atY(328)
                        fill(VectorPaint.MUTED)
                        fontSize(14)
                    },
                )
            }
        },
    )

fun momentumDiagram(factor: Double): VectorFragment =
    graphic(
        "Modellkurve: Wellenlänge ist umgekehrt proportional zum Impuls",
        vector {
            run {
                path({
                    geometry("M70 35 V280 H720")
                    stroke(VectorPaint.MUTED)
                    fill(VectorPaint.None)
                })
                label(
                    "λ / λ₀",
                    {
                        atX(70)
                        atY(22)
                        fill(VectorPaint.TEXT)
                        fontSize(15)
                    },
                )
                label(
                    "p / p₀",
                    {
                        atX(600)
                        atY(320)
                        fill(VectorPaint.TEXT)
                        fontSize(15)
                    },
                )
            }
            for (i in 1..4) {
                val x = 70 + (i - 1) * 210
                run {
                    path({
                        geometry("M$x 40 V280")
                        stroke(VectorPaint.GRID)
                    })
                    label(
                        i,
                        {
                            atX(x)
                            atY(303)
                            fill(VectorPaint.MUTED)
                        },
                    )
                }
            }
            val path =
                (0..200).joinToString(" ") { i ->
                    val p = 1.0 + i * 3.0 / 200
                    "${if(i == 0) "M" else "L"} ${70 + (p - 1) * 210} ${280 - 220 / p}"
                }
            run {
                path({
                    geometry(path)
                    stroke(VectorPaint.ACCENT)
                    strokeWidth(3)
                    fill(VectorPaint.None)
                })
                circle({
                    cx(70 + (factor - 1) * 210)
                    cy(280 - 220 / factor)
                    radius(7)
                    fill(VectorPaint.ACCENT)
                })
                label(
                    "λ / λ₀ = ${Physics.decimal(1 / factor, 3)}",
                    {
                        atX(425)
                        atY(65)
                        fill(VectorPaint.TEXT)
                        fontSize(20)
                    },
                )
                label(
                    "Berechnete Folge der Hypothese λ = h/p",
                    {
                        atX(425)
                        atY(90)
                        fill(VectorPaint.MUTED)
                        fontSize(14)
                    },
                )
            }
        },
    )

fun finiteSlitChart(model: FiniteSlitModel): VectorFragment =
    graphic(
        "Fernfeld-Lehrmodell mit endlicher Spaltbreite; keine historische Intensitätsmessung",
        vector {
            run {
                label(
                    "relative Modellintensität / 1",
                    {
                        atX(55)
                        atY(23)
                        fill(VectorPaint.TEXT)
                        fontSize(15)
                    },
                )
                path({
                    geometry("M55 40 V282 H725")
                    fill(VectorPaint.None)
                    stroke(VectorPaint.MUTED)
                })
            }
            for (i in 0..4) {
                val y = 282 - i * 57
                run {
                    path({
                        geometry("M55 $y H725")
                        stroke(VectorPaint.GRID)
                    })
                    label(
                        Physics.decimal(i / 4.0, 2),
                        {
                            atX(12)
                            atY(y + 4)
                            fill(VectorPaint.MUTED)
                            fontSize(12)
                        },
                    )
                }
            }
            val maximumPosition = .003
            for (i in -3..3) {
                val x = 55 + (i + 3) / 6.0 * 670
                run {
                    label(
                        i,
                        {
                            atX(x)
                            atY(302)
                            anchor(TextAnchor.MIDDLE)
                            fill(VectorPaint.MUTED)
                            fontSize(12)
                        },
                    )
                }
            }
            fun curve(envelope: Boolean) =
                (0..600).joinToString(" ") { i ->
                    val position = -maximumPosition + 2 * maximumPosition * i / 600
                    val value =
                        if (envelope) model.relativeEnvelope(position)
                        else model.relativeIntensity(position)
                    "${if(i == 0) "M" else "L"} ${55 + i / 600.0 * 670} ${282 - value * 228}"
                }
            run {
                path({
                    geometry(curve(true))
                    fill(VectorPaint.None)
                    stroke(VectorPaint.REFERENCE)
                    strokeWidth(2)
                    dash(6, 5)
                })
                path({
                    geometry(curve(false))
                    fill(VectorPaint.None)
                    stroke(VectorPaint.ACCENT)
                    strokeWidth(2)
                })
                label(
                    "Koordinate y / mm im Lehrmodell",
                    {
                        atX(450)
                        atY(330)
                        fill(VectorPaint.TEXT)
                        fontSize(14)
                    },
                )
            }
        },
    )
