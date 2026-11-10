package materiewelle.course.graphics

import androidx.compose.runtime.*
import kotlin.math.sqrt
import materiewelle.NativeSvg
import materiewelle.core.Length
import materiewelle.core.Physics
import materiewelle.core.course.CrystalAssignmentChoice
import materiewelle.core.course.CrystalViewMode
import materiewelle.core.course.InquiryState
import materiewelle.pages.number
import materiewelle.ui.MathText as Text
import materiewelle.ui.semantics.*
import materiewelle.ui.vector.*
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.*

private enum class CrystalAssignment(
    val label: String,
    val id: GeometrySlitsId,
    val value: CrystalAssignmentChoice,
) {
    NEIGHBOR(
        "1 Strecke entlang einer Atomreihe",
        GeometrySlitsId.CRYSTAL_ASSIGN_NEIGHBOR,
        CrystalAssignmentChoice.NEIGHBOR,
    ),
    ROWS(
        "2 Höhe zwischen zwei Oberflächenreihen",
        GeometrySlitsId.CRYSTAL_ASSIGN_ROWS,
        CrystalAssignmentChoice.ROWS,
    ),
    PLANES(
        "3 Abstand senkrecht zwischen zwei (111)-Ebenen",
        GeometrySlitsId.CRYSTAL_ASSIGN_PLANES,
        CrystalAssignmentChoice.PLANES,
    ),
}

/** Three explicit projections of one fcc model, not an independently fitted electron wavelength. */
@Composable
fun CrystalView(state: InquiryState, onChange: ((InquiryState) -> InquiryState) -> Unit) {
    val assignment = CrystalAssignment.entries.firstOrNull { it.value == state.crystalAssignment }
    val lattice = Length(.352e-9)
    val selected = state.crystalView
    Section(attrs = { geometryStyles(GeometrySlitsClass.INVESTIGATION) }) {
        P {
            Text(
                "Welche Strecke ist der Reihenabstand D, der vollständig in der Oberfläche liegt? Vergleiche die drei Ansichten; ordne erst zu und öffne danach die Formeln."
            )
        }
        Div(
            attrs = {
                geometryStyles(GeometrySlitsClass.VIEWS)
                role(AriaRole.GROUP)
                ariaLabel("Ansicht des Nickel-Kristallmodells")
            }
        ) {
            listOf(
                    Triple(CrystalViewMode.CELL, GeometrySlitsId.CRYSTAL_CELL, "1 · fcc-Zelle"),
                    Triple(
                        CrystalViewMode.SURFACE,
                        GeometrySlitsId.CRYSTAL_SURFACE,
                        "2 · Oberfläche",
                    ),
                    Triple(
                        CrystalViewMode.PLANES,
                        GeometrySlitsId.CRYSTAL_PLANES,
                        "3 · Ebenenschnitt",
                    ),
                )
                .forEach { (mode, id, label) ->
                    Button(
                        attrs = {
                            elementId(id)
                            styles(LabClass.SECONDARY)
                            if (selected == mode) geometryStyles(GeometrySlitsClass.ACTIVE)
                            ariaPressed(selected == mode)
                            onClick { onChange { current -> current.copy(crystalView = mode) } }
                        }
                    ) {
                        Text(label)
                    }
                }
        }
        Div(
            attrs = {
                geometryStyles(GeometrySlitsClass.FRAME)
                focusIndex(0)
                ariaLabel(
                    "Kristallzeichnung mit Blickrichtung; zugehörige Erklärung folgt direkt darunter"
                )
            }
        ) {
            NativeSvg(
                when (selected) {
                    CrystalViewMode.CELL -> crystalCell()
                    CrystalViewMode.SURFACE -> crystalSurface(state.crystalRevealed)
                    CrystalViewMode.PLANES -> crystalPlanes(state.crystalRevealed)
                }
            )
        }
        P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
            Text(
                when (selected) {
                    CrystalViewMode.CELL ->
                        "fcc bedeutet: Atome an den Würfelecken und in den Flächenmitten. Die markierte Dreiecksfläche verbindet die Achsenabschnitte (a,0,0), (0,a,0), (0,0,a). Sie gehört zur Ebenenfamilie (111). Die Kugeln sind Ortsmarken, keine maßstäblichen Atomradien."
                    CrystalViewMode.SURFACE ->
                        "Blick entlang [111], senkrecht auf die Oberfläche. Die gestrichelten Linien wählen eine Familie paralleler Atomreihen aus; der Azimut zeigt quer zu diesen Reihen. Der Reihenabstand ist eine Höhe im Dreiecksnetz."
                    CrystalViewMode.PLANES ->
                        "Seitlicher Schnitt senkrecht zur Oberfläche. Zwei parallele (111)-Ebenen liegen hintereinander im Kristall; ihr Abstand wird entlang derselben Normalen [111] gemessen. Im Schnitt sieht man Ebenen als Linien."
                }
            )
        }
        Div(
            attrs = {
                geometryStyles(GeometrySlitsClass.CHOICES)
                role(AriaRole.GROUP)
                ariaLabel("Eigene Zuordnung des Oberflächenreihenabstands D")
            }
        ) {
            CrystalAssignment.entries.forEach { candidate ->
                Button(
                    attrs = {
                        elementId(candidate.id)
                        styles(LabClass.SECONDARY)
                        ariaPressed(assignment == candidate)
                        if (assignment == candidate) geometryStyles(GeometrySlitsClass.ACTIVE)
                        onClick {
                            onChange { current ->
                                current.copy(crystalAssignment = candidate.value)
                            }
                        }
                    }
                ) {
                    Text(candidate.label)
                }
            }
            Button(
                attrs = {
                    elementId(GeometrySlitsId.CRYSTAL_CHECK)
                    styles(LabClass.PRIMARY)
                    if (assignment == null && !state.crystalRevealed) disabled()
                    onClick { onChange { current -> current.copy(crystalRevealed = true) } }
                }
            ) {
                Text(
                    if (state.crystalRevealed) "Formelkontrolle ist geöffnet"
                    else "Zuordnung kontrollieren"
                )
            }
        }
        if (state.crystalRevealed) {
            P(
                attrs = {
                    elementId(GeometrySlitsId.CRYSTAL_FEEDBACK)
                    role(AriaRole.STATUS)
                }
            ) {
                Text(
                    when (assignment) {
                        CrystalAssignment.ROWS ->
                            "Ja: 2 ist die Höhe zwischen den ausgewählten Atomreihen innerhalb der Oberfläche. Das ist D."
                        CrystalAssignment.NEIGHBOR ->
                            "1 verbindet nächste Nachbarn innerhalb einer Reihe. D ist dagegen die dazu senkrechte Höhe 2 zwischen zwei Reihen."
                        CrystalAssignment.PLANES ->
                            "3 verläuft aus der Oberfläche heraus zur nächsten Ebene. Das ist d₁₁₁. Der Oberflächenreihenabstand D ist die Höhe 2 innerhalb der Oberfläche."
                        null ->
                            "Die Formelkontrolle wurde bereits geöffnet. 2 bezeichnet D innerhalb der Oberfläche; 3 bezeichnet d₁₁₁ senkrecht zu den Ebenen."
                    }
                )
            }
            DList(
                attrs = {
                    elementId(GeometrySlitsId.CRYSTAL_VALUES)
                    geometryStyles(GeometrySlitsClass.VALUES)
                }
            ) {
                DTerm() { Text("Würfelkante a · unabhängiger Strukturwert") }
                DDescription() { Text("${number(lattice.nanometers, 3)} nm") }
                DTerm() { Text("1 Nächster Nachbar: a/√2") }
                DDescription() {
                    Text("${number(Physics.nickel111NearestNeighbor(lattice).nanometers, 4)} nm")
                }
                DTerm() { Text("2 Reihenabstand: D = a√(3/8)") }
                DDescription() {
                    Text("${number(Physics.nickel111RowSpacing(lattice).nanometers, 4)} nm")
                }
                DTerm() { Text("3 Ebenenabstand: d₁₁₁ = a/√3") }
                DDescription() {
                    Text("${number(Physics.nickel111PlaneSpacing(lattice).nanometers, 4)} nm")
                }
            }
            P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
                Text(
                    "D folgt als Höhe eines gleichseitigen Dreiecks: (a/√2)·sin 60°. Für d₁₁₁ schneiden die parallelen Ebenen x+y+z=a und x+y+z=2a dieselbe Normale; ihr Abstand ist a/√3. Die Größen sind aus der fcc-Struktur berechnet, nicht an eine Elektronenwellenlänge angepasst."
                )
            }
        }
    }
}

private data class CellPoint(val x: Double, val y: Double, val z: Double)

private data class Point(val x: Double, val y: Double)

private fun project(p: CellPoint) =
    Point(180 + 170 * p.x - 75 * p.y + 10 * p.z, 290 + 45 * p.x + 60 * p.y - 145 * p.z)

private fun point(p: Point) = "${p.x},${p.y}"

private fun crystalSvg(title: String, content: VectorFragment) = vector {
    svg({
        viewBox(0, 0, 520, 430)
        imageRole()
        ariaLabel(title)
    }) {
        title(title)
        rect({
            width(520)
            height(430)
            fill(VectorPaint.SURFACE)
        })
        include(content)
    }
}

private fun crystalCell(): VectorFragment =
    crystalSvg(
        "Axonometrische fcc-Einheitszelle mit markierter 111-Fläche, Würfelkante a und äußerer Normale 111",
        vector {
            val corners =
                (0..1).flatMap { x ->
                    (0..1).flatMap { y ->
                        (0..1).map { z -> CellPoint(x.toDouble(), y.toDouble(), z.toDouble()) }
                    }
                }
            val faces =
                listOf(
                    CellPoint(.5, .5, 0.0),
                    CellPoint(.5, .5, 1.0),
                    CellPoint(.5, 0.0, .5),
                    CellPoint(.5, 1.0, .5),
                    CellPoint(0.0, .5, .5),
                    CellPoint(1.0, .5, .5),
                )
            run {
                label(
                    "fcc · kubisch flächenzentriert",
                    {
                        atX(24)
                        atY(32)
                        fill(VectorPaint.TEXT)
                        fontSize(18)
                    },
                )
            }
            corners.forEach { a ->
                corners
                    .filter { b ->
                        listOf(a.x != b.x, a.y != b.y, a.z != b.z).count { it } == 1 &&
                            corners.indexOf(b) > corners.indexOf(a)
                    }
                    .forEach { b ->
                        val p = project(a)
                        val q = project(b)
                        run {
                            path({
                                geometry("M${point(p)} L${point(q)}")
                                stroke(VectorPaint.MUTED)
                                strokeWidth(1.8)
                                fill(VectorPaint.None)
                            })
                        }
                    }
            }
            val triangle =
                listOf(CellPoint(1.0, 0.0, 0.0), CellPoint(0.0, 1.0, 0.0), CellPoint(0.0, 0.0, 1.0))
                    .map(::project)
            run {
                polygon({
                    points(triangle.joinToString(" ", transform = ::point))
                    fill(VectorPaint.ACCENT)
                    fillOpacity(0.13)
                    stroke(VectorPaint.ACCENT)
                    strokeWidth(2.5)
                })
            }
            (corners + faces)
                .sortedBy { it.z }
                .forEach { p ->
                    val q = project(p)
                    val onPlane = kotlin.math.abs(p.x + p.y + p.z - 1) < .001
                    run {
                        circle({
                            cx(q.x)
                            cy(q.y)
                            radius(if (p in faces) 7 else 5)
                            fill(if (onPlane) VectorPaint.ACCENT else VectorPaint.SURFACE_RAISED)
                            stroke(if (onPlane) VectorPaint.ACCENT else VectorPaint.MUTED)
                            strokeWidth(2)
                        })
                    }
                }
            val normalStart = project(CellPoint(1.0 / 3, 1.0 / 3, 1.0 / 3))
            val normalEnd = project(CellPoint(1.30, 1.30, 1.30))
            run {
                path({
                    geometry("M${point(normalStart)} L${point(normalEnd)} l-14,-1 m14,1 l-10,10")
                    fill(VectorPaint.None)
                    stroke(VectorPaint.REFERENCE)
                    strokeWidth(2.5)
                })
                label(
                    "[111] · Normale",
                    {
                        atX(normalEnd.x + 8)
                        atY(normalEnd.y - 8)
                        fill(VectorPaint.REFERENCE)
                        fontSize(17)
                    },
                )
                label(
                    "(111)-Fläche",
                    {
                        atX(177)
                        atY(127)
                        fill(VectorPaint.ACCENT)
                        fontSize(17)
                    },
                )
                label(
                    "a",
                    {
                        atX(258)
                        atY(342)
                        fill(VectorPaint.TEXT)
                        fontSize(18)
                    },
                )
                label(
                    "x",
                    {
                        atX(352)
                        atY(346)
                        fill(VectorPaint.MUTED)
                        fontSize(16)
                    },
                )
                label(
                    "y",
                    {
                        atX(82)
                        atY(365)
                        fill(VectorPaint.MUTED)
                        fontSize(16)
                    },
                )
                label(
                    "z",
                    {
                        atX(189)
                        atY(143)
                        fill(VectorPaint.MUTED)
                        fontSize(16)
                    },
                )
                label(
                    "Weiter: entlang der Normalen auf die Fläche blicken →",
                    {
                        atX(24)
                        atY(405)
                        fill(VectorPaint.MUTED)
                        fontSize(16)
                    },
                )
            }
        },
    )

private fun crystalSurface(revealed: Boolean): VectorFragment =
    crystalSvg(
        "Draufsicht entlang 111 auf das Dreiecksnetz. Strecke eins entlang einer Reihe, Strecke zwei als senkrechte Höhe zwischen Reihen; Azimut quer zu den Reihen",
        vector {
            val pitch = 104.0
            val height = pitch * sqrt(3.0) / 2
            run {
                label(
                    "Draufsicht · Blick entlang [111]",
                    {
                        atX(24)
                        atY(32)
                        fill(VectorPaint.TEXT)
                        fontSize(18)
                    },
                )
                label(
                    "⊙ Normale zeigt aus der Zeichenfläche",
                    {
                        atX(24)
                        atY(59)
                        fill(VectorPaint.MUTED)
                        fontSize(16)
                    },
                )
            }
            for (row in 0..2) {
                val y = 113 + row * height
                run {
                    path({
                        geometry("M36 $y H454")
                        stroke(VectorPaint.MUTED)
                        dash(6, 5)
                    })
                }
                for (column in 0..3) {
                    val x = 58 + column * pitch + if (row % 2 == 1) pitch / 2 else 0.0
                    run {
                        circle({
                            cx(x)
                            cy(y)
                            radius(7)
                            fill(VectorPaint.ACCENT)
                        })
                    }
                }
            }
            run {
                path({
                    geometry("M58 113 L162 113 L110 ${113 + height} Z")
                    fill(VectorPaint.ACCENT)
                    fillOpacity(0.1)
                    stroke(VectorPaint.ACCENT)
                    strokeWidth(2)
                })
                path({
                    geometry("M58 87 H162 M58 81 V93 M162 81 V93")
                    fill(VectorPaint.None)
                    stroke(VectorPaint.TEXT)
                    strokeWidth(2)
                })
                label(
                    "1 ${if (revealed) "a/√2" else ""}",
                    {
                        atX(110)
                        atY(78)
                        anchor(TextAnchor.MIDDLE)
                        fill(VectorPaint.TEXT)
                        fontSize(17)
                    },
                )
                path({
                    geometry("M110 113 V${113 + height} M104 113 H116 M104 ${113 + height} H116")
                    stroke(VectorPaint.REFERENCE)
                    strokeWidth(3)
                })
                path({
                    geometry("M110 126 H123 V113")
                    fill(VectorPaint.None)
                    stroke(VectorPaint.REFERENCE)
                    strokeWidth(1.8)
                })
                label(
                    "2 ${if (revealed) "D" else ""}",
                    {
                        atX(126)
                        atY(166)
                        fill(VectorPaint.REFERENCE)
                        fontSize(18)
                    },
                )
                path({
                    geometry("M76 113 A18 18 0 0 1 67 128.588")
                    stroke(VectorPaint.ACCENT)
                    fill(VectorPaint.None)
                })
                label(
                    "60°",
                    {
                        atX(72)
                        atY(150)
                        fill(VectorPaint.TEXT)
                        fontSize(16)
                    },
                )
                path({
                    geometry("M415 137 V252 l-7,-12 m7,12 l7,-12")
                    fill(VectorPaint.None)
                    stroke(VectorPaint.REFERENCE)
                    strokeWidth(2.5)
                })
                label(
                    "Azimut: quer zu den Reihen",
                    {
                        atX(303)
                        atY(329)
                        fill(VectorPaint.REFERENCE)
                        fontSize(16)
                    },
                )
                label(
                    "Reihen und Höhe liegen in derselben Oberfläche.",
                    {
                        atX(24)
                        atY(374)
                        fill(VectorPaint.TEXT)
                        fontSize(17)
                    },
                )
                label(
                    "Kein Ebenenabstand aus dem Kristallinneren.",
                    {
                        atX(24)
                        atY(405)
                        fill(VectorPaint.MUTED)
                        fontSize(16)
                    },
                )
            }
        },
    )

private fun crystalPlanes(revealed: Boolean): VectorFragment =
    crystalSvg(
        "Schnitt senkrecht zur 111-Oberfläche. Zwei parallele Netzebenen und ihr Normalenabstand, Strecke drei, keine Strecke innerhalb der Oberfläche",
        vector {
            label(
                "Ebenenschnitt · Blick quer zu [111]",
                {
                    atX(24)
                    atY(32)
                    fill(VectorPaint.TEXT)
                    fontSize(18)
                },
            )
            label(
                "Darstellung eines Schnitts, keine Draufsicht",
                {
                    atX(24)
                    atY(59)
                    fill(VectorPaint.MUTED)
                    fontSize(16)
                },
            )
            path({
                geometry("M52 175 H450 M52 306 H450")
                stroke(VectorPaint.ACCENT)
                strokeWidth(3)
            })
            path({
                geometry("M90 331 V94 l-7,12 m7,-12 l7,12")
                fill(VectorPaint.None)
                stroke(VectorPaint.REFERENCE)
                strokeWidth(2.3)
            })
            label(
                "[111] · Normale",
                {
                    atX(106)
                    atY(108)
                    fill(VectorPaint.REFERENCE)
                    fontSize(17)
                },
            )
            label(
                "(111)-Oberfläche",
                {
                    atX(304)
                    atY(161)
                    fill(VectorPaint.ACCENT)
                    fontSize(17)
                },
            )
            label(
                "parallele (111)-Ebene",
                {
                    atX(280)
                    atY(335)
                    fill(VectorPaint.ACCENT)
                    fontSize(17)
                },
            )
            path({
                geometry("M222 175 V306 M215 175 H229 M215 306 H229")
                fill(VectorPaint.None)
                stroke(VectorPaint.TEXT)
                strokeWidth(3)
            })
            path({
                geometry("M222 175 H237 V190")
                fill(VectorPaint.None)
                stroke(VectorPaint.TEXT)
                strokeWidth(1.8)
            })
            label(
                "3 ${if (revealed) "d₁₁₁ = a/√3" else ""}",
                {
                    atX(242)
                    atY(249)
                    fill(VectorPaint.TEXT)
                    fontSize(20)
                },
            )
            label(
                "3 verbindet Ebenen entlang ihrer Normalen.",
                {
                    atX(24)
                    atY(380)
                    fill(VectorPaint.TEXT)
                    fontSize(17)
                },
            )
            label(
                "D verläuft dagegen innerhalb einer Ebene.",
                {
                    atX(24)
                    atY(408)
                    fill(VectorPaint.MUTED)
                    fontSize(16)
                },
            )
        },
    )
