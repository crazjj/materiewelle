package materiewelle.pages

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import materiewelle.course.BraggGeometryFocus
import materiewelle.course.SurfaceGeometryFocus
import materiewelle.course.BraggPresentation
import materiewelle.ui.semantics.DgComparisonId
import materiewelle.ui.semantics.DgComparisonClass
import materiewelle.core.BraggPlaneAngle
import materiewelle.core.Length
import materiewelle.core.Physics
import materiewelle.ui.semantics.TextAnchor
import materiewelle.ui.semantics.MotionClass
import materiewelle.ui.vector.*

/**
 * Application-owned vector geometry. No intensity curve or historical detector scan is invented.
 */
fun braggGeometryChart(stage: Int = 2, focus: BraggGeometryFocus? = null, presentation: BraggPresentation = BraggPresentation.LESSON): VectorFragment {
    require(stage in 0..2)
    val angle = BraggPlaneAngle(30.0)
    val theta = angle.degrees * PI / 180.0
    val ax = 220.0
    val ay = 100.0
    val bx = ax
    val by = 225.0
    val rayLength = 185.0
    // The projection uses the same path-difference calculation as the physical model.
    val extra = Physics.braggPathDifference(Length(by - ay), angle).incomingExtra.meters
    val px = bx - extra * cos(theta)
    val py = by - extra * sin(theta)
    val qx = bx + extra * cos(theta)
    val qy = py
    val symbolSize = if (presentation == BraggPresentation.COMPARISON) 19 else 18
    val description = when (stage) {
        0 -> "Bragg-Konstruktion: parallele Netzebenen im Abstand d, Winkel Theta B gleich 30 Grad zur Ebene. Der untere Strahl hat zusätzliche Wege."
        1 -> "Bragg-Konstruktion: Die Projektion von A auf den einlaufenden Strahl ergibt PB gleich d sin Theta B. Der rechte Winkel ist bei P markiert."
        else -> "Bragg-Konstruktion: PB und BQ sind je d sin Theta B. Zwei rechte Projektionswinkel. Der gesamte Zusatzweg ist zwei d sin Theta B, konstruktiv gleich n Lambda."
    }
    fun rightAngle(x: Double, y: Double, side: Double) = vector {
        val ux = -side * sin(theta)
        val uy = -cos(theta)
        val vx = -side * cos(theta)
        val vy = sin(theta)
        path({ geometry("M${x + 9 * ux} ${y + 9 * uy} l${9 * vx} ${9 * vy} l${-9 * ux} ${-9 * uy}"); fill(VectorPaint.None); stroke(VectorPaint.TEXT); strokeWidth(1.5) })
    }
    return vector {
        svg({ viewBox(0, 0, 430, 320); imageRole(); ariaLabel(description) }) {
            title(description)
            for (y in listOf(ay, by)) line({ x1(25); y1(y); x2(410); y2(y); stroke(VectorPaint.MUTED); strokeWidth(1.8) })
            label("Netzebenen", { atX(294); atY(88); fill(VectorPaint.MUTED); fontSize(16) })
            path({ geometry("M40 $ay V$by M34 $ay H46 M34 $by H46"); fill(VectorPaint.None); stroke(VectorPaint.TEXT); strokeWidth(1.8) })
            label("d", { atX(29); atY(170); anchor(TextAnchor.END); fill(VectorPaint.TEXT); fontSize(symbolSize) })
            for (y in listOf(ay, by)) path({
                geometry("M${ax - rayLength * cos(theta)} ${y - rayLength * sin(theta)} L$ax $y L${ax + rayLength * cos(theta)} ${y - rayLength * sin(theta)}")
                fill(VectorPaint.None); stroke(if (y == ay) VectorPaint.MUTED else VectorPaint.TEXT); strokeWidth(2)
            })
            if (stage >= 1) {
                path({ geometry("M$ax $ay L$bx $by L$px $py Z"); fill(VectorPaint.ACCENT); fillOpacity(.09) })
                path({ geometry("M$ax $ay V$by M$ax $ay L$px $py"); fill(VectorPaint.None); stroke(VectorPaint.MUTED); strokeWidth(1.5); dash(4, 4) })
                path({ geometry("M$px $py L$bx $by"); styles(MotionClass.CONSTRUCTION_INK); pathLength(100); stroke(VectorPaint.ACCENT); strokeWidth(5) })
                include(rightAngle(px, py, -1.0))
                label("P", { atX(154); atY(176); anchor(TextAnchor.END); fill(VectorPaint.ACCENT); fontSize(symbolSize) })
                path({ geometry("M$ax ${ay + 28} A28 28 0 0 1 ${ax - 28 * sin(theta)} ${ay + 28 * cos(theta)}"); fill(VectorPaint.None); stroke(VectorPaint.ACCENT); strokeWidth(1.5) })
                label("θᴮ", { atX(190); atY(141); anchor(TextAnchor.END); fill(VectorPaint.ACCENT); fontSize(symbolSize) })
                label("PB = d sin θᴮ", { atX(25); atY(292); fill(VectorPaint.ACCENT); fontSize(17) })
            }
            if (stage >= 2) {
                path({ geometry("M$ax $ay L$qx $qy"); fill(VectorPaint.None); stroke(VectorPaint.MUTED); strokeWidth(1.5); dash(4, 4) })
                path({ geometry("M$bx $by L$qx $qy"); styles(MotionClass.CONSTRUCTION_INK, MotionClass.CONSTRUCTION_SECOND); pathLength(100); stroke(VectorPaint.REFERENCE); strokeWidth(5) })
                include(rightAngle(qx, qy, 1.0))
                label("Q", { atX(286); atY(176); fill(VectorPaint.REFERENCE); fontSize(symbolSize) })
                label("BQ = d sin θᴮ", { atX(243); atY(292); fill(VectorPaint.REFERENCE); fontSize(17) })
            }
            circle({ cx(ax); cy(ay); radius(4); fill(VectorPaint.TEXT) })
            circle({ cx(bx); cy(by); radius(4); fill(VectorPaint.TEXT) })
            label("A", { atX(226); atY(80); fill(VectorPaint.TEXT); fontSize(symbolSize) })
            label("B", { atX(211); atY(251); fill(VectorPaint.TEXT); fontSize(symbolSize) })
            path({ geometry("M${bx + 49} $by A49 49 0 0 0 ${bx + 49 * cos(theta)} ${by - 49 * sin(theta)}"); fill(VectorPaint.None); stroke(VectorPaint.ACCENT); strokeWidth(1.8) })
            label("θᴮ", { atX(282); atY(218); fill(VectorPaint.ACCENT); fontSize(symbolSize) })
            if (stage == 0) label("Wo liegen die zusätzlichen Wege?", { atX(215); atY(292); anchor(TextAnchor.MIDDLE); fill(VectorPaint.MUTED); fontSize(16) })
            focus?.let { selected ->
                val highlightedPath = when (selected) {
                    BraggGeometryFocus.PLANE_AND_ANGLE -> "M$bx $by H410 M${bx + 49} $by A49 49 0 0 0 ${bx + 49 * cos(theta)} ${by - 49 * sin(theta)}"
                    BraggGeometryFocus.PLANE_SPACING -> "M40 $ay V$by M34 $ay H46 M34 $by H46"
                    BraggGeometryFocus.TWO_EXTRA_PATHS -> "M$px $py L$bx $by L$qx $qy"
                }
                path({ geometry(highlightedPath); fill(VectorPaint.None); stroke(VectorPaint.SURFACE); strokeWidth(9) })
                path({ elementId(DgComparisonId.BRAGG_HIGHLIGHT); styles(DgComparisonClass.HIGHLIGHT); geometry(highlightedPath); fill(VectorPaint.None); stroke(VectorPaint.REFERENCE); strokeWidth(4); if (selected == BraggGeometryFocus.PLANE_SPACING) dash(8, 3) })
            }
        }
    }
}

/** Triangular Ni(111) surface net plus a separate, labelled side projection of outgoing paths. */
enum class NickelSurfaceView { FULL, PROJECTION, COMPARISON }

fun nickelSurfaceChart(phiDegrees: Double, latticeConstantNm: Double, view: NickelSurfaceView = NickelSurfaceView.FULL, focus: SurfaceGeometryFocus? = null): VectorFragment {
    require(
        phiDegrees.isFinite() &&
            phiDegrees in 0.0..90.0 &&
            latticeConstantNm.isFinite() &&
            latticeConstantNm > 0.0
    )
    val pitch = 70.0
    val rowHeight = pitch * sqrt(3.0) / 2.0
    val phi = phiDegrees * PI / 180.0
    val ax = 465.0
    val ay = 235.0
    val bx = 635.0
    val cx = ax + (bx - ax) * sin(phi) * sin(phi)
    val cy = ay - (bx - ax) * sin(phi) * cos(phi)
    return vector {
        svg({
            when (view) {
                NickelSurfaceView.FULL -> viewBox(0, 0, 760, 440)
                NickelSurfaceView.PROJECTION -> viewBox(392, 40, 355, 325)
                // Same aspect ratio as the Bragg panel, with room for readable labels.
                NickelSurfaceView.COMPARISON -> viewBox(354, 40, 430, 320)
            }
            imageRole()
            ariaLabel(
                if (view == NickelSurfaceView.FULL)
                    "Ni111-Oberflächenmodell. Links Dreiecksnetz mit Nachbarabstand a durch Wurzel zwei. Ausgewählte Atomreihen sind horizontal; der senkrechte Reihenabstand D gleich a Wurzel drei Achtel. Die gewählte Richtung innerhalb der Oberfläche steht senkrecht zu diesen Reihen. Rechts Seitenprojektion: bei normalem Einfall ist die einlaufende Wegdifferenz null; auslaufend ist AC gleich D sin Phi. D ist nicht der Netzebenenabstand d111."
                else "Seitenprojektion des ausgewählten Ni111-Oberflächenmodells: A und B vertreten zwei Reihen im Abstand D. Bei normalem Einfall ist die einlaufende Wegdifferenz null; auslaufend ist die markierte Strecke AC gleich D sin Phi. Phi wird zur äußeren Normalen gemessen. D ist kein Netzebenenabstand."
            )
        }) {
            title("Ni(111): Reihenabstand und Wegdifferenz")
            if (view == NickelSurfaceView.FULL) {
                label(
                    "Draufsicht · Ni(111)-Dreiecksnetz",
                    {
                        atX(28)
                        atY(33)
                        fill(VectorPaint.TEXT)
                        fontSize(17)
                    },
                )
                for (row in 0..3) {
                    val y = 90.0 + row * rowHeight
                    run {
                        line({
                            x1(35)
                            y1(y)
                            x2(333)
                            y2(y)
                            stroke(VectorPaint.GRID)
                            dash(5, 4)
                        })
                    }
                    for (column in 0..3) {
                        val x = 50.0 + column * pitch + if (row % 2 == 1) pitch / 2.0 else 0.0
                        run {
                            circle({
                                cx(x)
                                cy(y)
                                radius(6)
                                fill(VectorPaint.ACCENT)
                            })
                        }
                    }
                }
                path({
                    geometry("M50 90 H120 M50 83 V97 M120 83 V97")
                    fill(VectorPaint.None)
                    stroke(VectorPaint.TEXT)
                    strokeWidth(2)
                })
                label(
                    "a/√2",
                    {
                        atX(80)
                        atY(75)
                        anchor(TextAnchor.MIDDLE)
                        fill(VectorPaint.TEXT)
                        fontSize(15)
                    },
                )
                path({
                    geometry("M325 90 V${90 + rowHeight} M318 90 H332 M318 ${90 + rowHeight} H332")
                    fill(VectorPaint.None)
                    stroke(VectorPaint.REFERENCE)
                    strokeWidth(2)
                })
                label(
                    "D",
                    {
                        atX(337)
                        atY(127)
                        fill(VectorPaint.REFERENCE)
                        fontSize(18)
                    },
                )
                path({
                    geometry("M210 180 V250 M204 243 L210 250 L216 243")
                    fill(VectorPaint.None)
                    stroke(VectorPaint.TEXT)
                    strokeWidth(2)
                })
                label(
                    "gewählter Azimut",
                    {
                        atX(225)
                        atY(244)
                        fill(VectorPaint.TEXT)
                        fontSize(14)
                    },
                )
                label(
                    "D = (a/√2) sin 60° = a√(3/8)",
                    {
                        atX(28)
                        atY(313)
                        fill(VectorPaint.TEXT)
                        fontSize(16)
                    },
                )
                label(
                    "a = ${number(latticeConstantNm, 3)} nm · fcc-Strukturannahme",
                    {
                        atX(28)
                        atY(343)
                        fill(VectorPaint.MUTED)
                        fontSize(15)
                    },
                )
            }
            label(
                "Seitenprojektion",
                {
                    atX(404)
                    atY(33)
                    fill(VectorPaint.TEXT)
                    fontSize(17)
                },
            )
            path({
                geometry("M430 235 H700 M$ax 65 V235 M$bx 65 V235")
                fill(VectorPaint.None)
                stroke(VectorPaint.GRID)
                strokeWidth(2)
            })
            path({
                geometry(
                    "M$ax 90 V223 M${ax - 5} 215 L$ax 223 L${ax + 5} 215 M$bx 90 V223 M${bx - 5} 215 L$bx 223 L${bx + 5} 215"
                )
                fill(VectorPaint.None)
                stroke(VectorPaint.MUTED)
                strokeWidth(2)
            })
            label(
                "normaler Einfall",
                {
                    atX((ax + bx) / 2)
                    atY(57)
                    anchor(TextAnchor.MIDDLE)
                    fill(VectorPaint.MUTED)
                    fontSize(14)
                },
            )
            label(
                "gleich lange Wege",
                {
                    atX((ax + bx) / 2)
                    atY(77)
                    anchor(TextAnchor.MIDDLE)
                    fill(VectorPaint.MUTED)
                    fontSize(14)
                },
            )
            path({
                geometry(
                    "M$ax $ay L${ax + 160 * sin(phi)} ${ay - 160 * cos(phi)} M$bx $ay L${bx + 70 * sin(phi)} ${ay - 70 * cos(phi)}"
                )
                fill(VectorPaint.None)
                stroke(VectorPaint.TEXT)
                strokeWidth(2)
            })
            path({
                geometry("M$ax $ay L$cx $cy")
                stroke(VectorPaint.ACCENT)
                strokeWidth(6)
            })
            path({
                geometry("M$bx $ay L$cx $cy")
                stroke(VectorPaint.MUTED)
                dash(5, 4)
            })
            path({
                geometry(
                    "M${cx - 11 * sin(phi)} ${cy + 11 * cos(phi)} l${11 * cos(phi)} ${11 * sin(phi)} l${11 * sin(phi)} ${-11 * cos(phi)}"
                )
                fill(VectorPaint.None)
                stroke(VectorPaint.REFERENCE)
                strokeWidth(2)
            })
            path({
                geometry("M$ax ${ay - 58} A58 58 0 0 1 ${ax + 58 * sin(phi)} ${ay - 58 * cos(phi)}")
                fill(VectorPaint.None)
                stroke(VectorPaint.ACCENT)
                strokeWidth(2)
            })
            label(
                "φ",
                {
                    atX(ax + 22)
                    atY(ay - 67)
                    fill(VectorPaint.ACCENT)
                    fontSize(16)
                },
            )
            circle({
                cx(ax)
                cy(ay)
                radius(5)
                fill(VectorPaint.TEXT)
            })
            circle({
                cx(bx)
                cy(ay)
                radius(5)
                fill(VectorPaint.TEXT)
            })
            label(
                "A",
                {
                    atX(455)
                    atY(259)
                    fill(VectorPaint.TEXT)
                    fontSize(16)
                },
            )
            label(
                "B",
                {
                    atX(632)
                    atY(259)
                    fill(VectorPaint.TEXT)
                    fontSize(16)
                },
            )
            label(
                "C",
                {
                    // Keep the glyph above-left of C, clear of the outgoing ray.
                    atX(cx - 12)
                    atY(cy - 12)
                    anchor(TextAnchor.END)
                    fill(VectorPaint.ACCENT)
                    fontSize(16)
                },
            )
            path({
                geometry("M$ax 283 H$bx M$ax 277 V289 M$bx 277 V289")
                stroke(VectorPaint.REFERENCE)
                fill(VectorPaint.None)
                strokeWidth(2)
            })
            label(
                "AB = D",
                {
                    atX(550)
                    atY(307)
                    anchor(TextAnchor.MIDDLE)
                    fill(VectorPaint.REFERENCE)
                    fontSize(17)
                },
            )
            label(
                "AC = D sin φ; φ = ${number(phiDegrees, 1)}°",
                {
                    atX(404)
                    atY(343)
                    fill(VectorPaint.ACCENT)
                    fontSize(17)
                },
            )
            label(
                "Erste Ordnung: Δs = D sin φ = λ; d₁₁₁ = a/√3 ≠ D",
                {
                    atX(380)
                    atY(398)
                    anchor(TextAnchor.MIDDLE)
                    fill(VectorPaint.TEXT)
                    fontSize(17)
                },
            )
            focus?.let { selected ->
                val highlightedPath = when (selected) {
                    SurfaceGeometryFocus.NORMAL_AND_ANGLE -> "M$ax 65 V$ay M$ax ${ay - 58} A58 58 0 0 1 ${ax + 58 * sin(phi)} ${ay - 58 * cos(phi)}"
                    SurfaceGeometryFocus.ROW_SPACING -> "M$ax 283 H$bx M$ax 277 V289 M$bx 277 V289"
                    SurfaceGeometryFocus.OUTGOING_EXTRA_PATH -> "M$ax $ay L$cx $cy"
                }
                path({ geometry(highlightedPath); fill(VectorPaint.None); stroke(VectorPaint.SURFACE); strokeWidth(9) })
                path({
                    elementId(DgComparisonId.SURFACE_HIGHLIGHT); styles(DgComparisonClass.HIGHLIGHT)
                    geometry(highlightedPath); fill(VectorPaint.None); stroke(VectorPaint.REFERENCE); strokeWidth(4)
                    if (selected == SurfaceGeometryFocus.ROW_SPACING) dash(8, 3)
                })
            }
        }
    }
}
