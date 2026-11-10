package materiewelle.course.graphics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import materiewelle.NativeSvg
import materiewelle.core.*
import materiewelle.pages.number
import materiewelle.ui.MathText
import materiewelle.ui.semantics.*
import materiewelle.ui.vector.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*

private const val PHASE_WINDOW_M = .003
private const val POINT_TOLERANCE_M = 1e-9

/** A selected screen coordinate, not a second physical model or a particle trajectory. */
@Composable
fun SlitPhaseView(
    model: FiniteSlitModel,
    position: ObservationPosition,
    configuration: SlitConfiguration,
    onPositionChange: (ObservationPosition) -> Unit,
) {
    val sample = remember(model, position) { model.interferenceAt(position) }
    val firstNull = remember(model) { model.positionAtPathDifference(.5)?.takeIf { abs(it.meters) <= PHASE_WINDOW_M } }
    val leftIntensity = if (configuration == SlitConfiguration.RIGHT_ONLY) 0.0 else sample.left.intensity
    val rightIntensity = if (configuration == SlitConfiguration.LEFT_ONLY) 0.0 else sample.right.intensity
    val total = sample.intensity(configuration)
    Section(attrs = { elementId(SlitPhaseId.ROOT); slitPhaseStyles(SlitPhaseClass.ROOT); labelledBy(SlitPhaseId.TITLE) }) {
        H3(attrs = { elementId(SlitPhaseId.TITLE) }) { Text("Die Beiträge am gewählten Ort") }
        P(attrs = { slitPhaseStyles(SlitPhaseClass.NOTE) }) {
            Text("Freiwillige Vertiefung außerhalb der Aufgabenstellung: Hier verwenden wir Wahrscheinlichkeitsamplituden, um das Modell genauer zu erklären. Für Aufbau, Streifenvergleich und Bildmessung brauchst du diesen Zusatz nicht.")
        }
        P { Text("Der gewählte Ort gehört zum Streifenband oben. Verschiebe ihn oder vergleiche die Mitte mit den Orten, an denen sich die beiden kohärenten Beiträge erstmals auslöschen.") }
        FieldLabel(SlitPhaseId.POSITION) {
            Text("Ort im Modellschirm: "); MathText("x = ${number(position.millimeters, 3)} mm")
        }
        Input(InputType.Range, attrs = {
            elementId(SlitPhaseId.POSITION); minimum(-3); maximum(3); increment(.001)
            value(position.millimeters)
            onInput { event -> event.value?.toDouble()?.takeIf { it.isFinite() && it in -3.0..3.0 }?.let { onPositionChange(ObservationPosition(it * .001)) } }
        })
        Div(attrs = { slitPhaseStyles(SlitPhaseClass.CONTROLS); role(AriaRole.GROUP); ariaLabel("Ort im Modellschirm auswählen") }) {
            PhasePreset(SlitPhaseId.CENTER, "Mitte", ObservationPosition(0.0), position, onPositionChange)
            PhasePreset(SlitPhaseId.NULL_NEGATIVE, if (configuration == SlitConfiguration.BOTH_COHERENT) "Erste Auslöschung links" else "Kohärenter Vergleichsort links", firstNull?.let { ObservationPosition(-it.meters) }, position, onPositionChange)
            PhasePreset(SlitPhaseId.NULL_POSITIVE, if (configuration == SlitConfiguration.BOTH_COHERENT) "Erste Auslöschung rechts" else "Kohärenter Vergleichsort rechts", firstNull, position, onPositionChange)
        }
        Div(attrs = { slitPhaseStyles(SlitPhaseClass.LAYOUT) }) {
            Figure(attrs = { slitPhaseStyles(SlitPhaseClass.FIGURE) }) {
                NativeSvg(phaseDiagram(sample, configuration))
                Figcaption(attrs = { slitPhaseStyles(SlitPhaseClass.LEGEND) }) {
                    Text(when (configuration) {
                        SlitConfiguration.BOTH_INCOHERENT -> "Ohne feste Phasenbeziehung addieren sich die Intensitäten. Die Balken zeigen dieselbe feste Skala wie die Modellkurve."
                        SlitConfiguration.LEFT_ONLY -> "Türkis: der einzige offene Beitrag. Der markierte Endpunkt gehört daher zum selben Zeiger. Richtung bedeutet Phase, Länge bedeutet Amplitude – keine räumliche Elektronenbahn."
                        SlitConfiguration.RIGHT_ONLY -> "Gelb gestrichelt: der einzige offene Beitrag. Der markierte Endpunkt gehört daher zum selben Zeiger. Richtung bedeutet Phase, Länge bedeutet Amplitude – keine räumliche Elektronenbahn."
                        SlitConfiguration.BOTH_COHERENT -> "Der türkisfarbene Zeiger gehört zum linken Spalt, der gelbe gestrichelte zum rechten. Der zweite beginnt an der Spitze des ersten. Die Summe reicht vom Anfang des ersten Zeigers bis zur Spitze des zweiten. Die Richtung zeigt die Phase, die Länge die Amplitude. Die Zeiger sind keine Elektronenbahnen."
                    })
                }
            }
            Div {
                Figure(attrs = { slitPhaseStyles(SlitPhaseClass.FIGURE) }) {
                    NativeSvg(pathSketch(model, position, configuration))
                    Figcaption(attrs = { slitPhaseStyles(SlitPhaseClass.LEGEND) }) { Text("Gedrehte Draufsicht: Die Spaltmitten liegen hier übereinander. Abstand und Winkel sind stark überhöht. Linien sind eine Wegkonstruktion, keine beobachteten Elektronenbahnen; maßgeblich sind die berechneten Werte.") }
                }
                Div(attrs = {
                    elementId(SlitPhaseId.READOUT); slitPhaseStyles(SlitPhaseClass.READOUT)
                    data(SlitPhaseData.POSITION, position.meters)
                    data(SlitPhaseData.PATH_DIFFERENCE, sample.pathDifferenceM)
                    data(SlitPhaseData.PHASE_TURNS, sample.relativePhaseTurns)
                    data(SlitPhaseData.LEFT_INTENSITY, leftIntensity)
                    data(SlitPhaseData.RIGHT_INTENSITY, rightIntensity)
                    data(SlitPhaseData.TOTAL_INTENSITY, total)
                }) {
                    P(attrs = { slitPhaseStyles(SlitPhaseClass.READOUT_TERM) }) { Text("Wegunterschied von den Spaltmitten zum gewählten Ort (Fernfeldnäherung)") }
                    P(attrs = { slitPhaseStyles(SlitPhaseClass.READOUT_VALUE) }) { MathText("Δs = ${number(sample.pathDifferenceM * 1e12, 3)} pm") }
                    P(attrs = { slitPhaseStyles(SlitPhaseClass.READOUT_TERM) }) { Text("Geometrischer Versatz in Wellenlängen") }
                    P(attrs = { slitPhaseStyles(SlitPhaseClass.READOUT_VALUE) }) { MathText("Δs/λ = ${number(sample.relativePhaseTurns, 3)}") }
                    P(attrs = { slitPhaseStyles(SlitPhaseClass.READOUT_TERM) }) { Text("Intensität relativ zum kohärenten Zentralmaximum") }
                    P(attrs = { slitPhaseStyles(SlitPhaseClass.READOUT_VALUE) }) { MathText(number(total, 6)) }
                }
                P(attrs = { elementId(SlitPhaseId.EXPLANATION); slitPhaseStyles(SlitPhaseClass.NOTE); role(AriaRole.STATUS) }) {
                    Text(phaseExplanation(sample, configuration))
                }
            }
        }
        Details {
            Summary { Text("Was die Zeiger und die ausgewählten Nullstellen bedeuten") }
            P { Text("Die Zeiger stellen Beiträge der Wahrscheinlichkeitsamplitude dar. Im kohärenten Fall werden sie zuerst addiert; das Betragsquadrat der Summe liefert die relative Intensität. Die Länge eines einzelnen Zeigers ist keine Trefferwahrscheinlichkeit.") }
            P { Text("Die Wegdifferenz wird im Fernfeld aus dem Spaltmittenabstand und dem Beobachtungswinkel berechnet: "); MathText("Δs ≈ g sin θ"); Text(". Eine gemeinsame Phase beider Beiträge ist für die Intensität unerheblich und hier weggelassen.") }
            P { Text("„Erste Auslöschung“ bezeichnet einen halben Wellenlängenversatz zwischen beiden Spaltmitten. Bei zwei kohärenten Beiträgen liegt dort eine Nullstelle des Interferenzfaktors. Ein einzelner offener Spalt besitzt diese Auslöschung nicht. Weitere dunkle Stellen können aus der endlichen Spaltbreite entstehen; die Hülle kann außerdem Maxima verschieben oder unterdrücken.") }
        }
    }
}

@Composable
private fun PhasePreset(id: SlitPhaseId, label: String, target: ObservationPosition?, current: ObservationPosition, choose: (ObservationPosition) -> Unit) {
    val selected = target != null && abs(target.meters - current.meters) < POINT_TOLERANCE_M
    Button(attrs = {
        elementId(id); styles(LabClass.SECONDARY); ariaPressed(selected)
        if (selected) slitPhaseStyles(SlitPhaseClass.SELECTED)
        if (target == null) disabled()
        onClick { target?.let(choose) }
    }) { Text(label) }
}

private fun phaseExplanation(sample: SlitInterferenceSample, configuration: SlitConfiguration): String = when (configuration) {
    SlitConfiguration.LEFT_ONLY -> "Nur der linke Beitrag ist offen. Es gibt keine Interferenz zwischen zwei Spalten; das Profil wird von der Einzelspaltbeugung bestimmt."
    SlitConfiguration.RIGHT_ONLY -> "Nur der rechte Beitrag ist offen. Im idealen Fernfeld ist seine Intensitätsverteilung dieselbe wie beim linken Einzelspalt, obwohl seine Phase anders sein kann."
    SlitConfiguration.BOTH_INCOHERENT -> "Hier wird keine feste relative Phase angenommen. Die beiden Einzelintensitäten addieren sich; eine feste Zeigersumme wäre für diesen Fall irreführend."
    SlitConfiguration.BOTH_COHERENT -> when {
        sample.left.intensity + sample.right.intensity < 1e-10 -> "Hier sind bereits die Einzelbeiträge durch die Beugungshülle nahezu null. Nicht jede dunkle Stelle entsteht erst durch die gegenseitige Auslöschung der beiden Spaltenbeiträge."
        sample.sum.intensity < 1e-10 -> "Die beiden gleich langen Beiträge zeigen in entgegengesetzte Richtungen. Ihre Summe verschwindet: eine Auslöschung des Interferenzfaktors."
        sample.sum.intensity > sample.left.intensity + sample.right.intensity + 1e-9 -> "Die Beiträge verstärken sich hier gegenüber der Summe ihrer Einzelintensitäten. Die genaue Höhe wird zusätzlich durch die Beugungshülle begrenzt."
        sample.sum.intensity < sample.left.intensity + sample.right.intensity - 1e-9 -> "Die Beiträge schwächen sich hier gegenseitig ab. Erst ihre Amplitudensumme und danach deren Betragsquadrat bestimmen das gemeinsame Profil."
        else -> "Hier entspricht die Intensität der kohärenten Summe gerade der Summe der Einzelintensitäten. An anderen Orten unterscheidet sie sich durch die relative Phase."
    }
}

private fun phaseDiagram(sample: SlitInterferenceSample, configuration: SlitConfiguration): VectorFragment {
    val incoherent = configuration == SlitConfiguration.BOTH_INCOHERENT
    val left = if (configuration == SlitConfiguration.RIGHT_ONLY) ComplexAmplitude(0.0, 0.0) else sample.left
    val right = if (configuration == SlitConfiguration.LEFT_ONLY) ComplexAmplitude(0.0, 0.0) else sample.right
    val sum = left + right
    return vector {
        svg({
            elementId(SlitPhaseId.DIAGRAM); viewBox(0, 0, 440, 340); imageRole()
            ariaLabel(if (incoherent) "Intensitätsaddition ohne feste Phasenbeziehung: linker und rechter Einzelbeitrag sowie ihre Summe auf gemeinsamer Skala."
                else "Zeigeraddition im abstrakten Amplitudenraum: linker und rechter Beitrag werden Spitze an Anfang gesetzt. Der markierte Endpunkt zeigt die Summe. Keine Darstellung von Elektronenbahnen.")
        }) {
            title(if (incoherent) "Einzelintensitäten addieren" else "Beiträge der Wahrscheinlichkeitsamplitude")
            if (incoherent) {
                val values = listOf(sample.left.intensity, sample.right.intensity, sample.intensity(configuration))
                val labels = listOf("Linker Beitrag", "Rechter Beitrag", "Summe der Intensitäten")
                val colors = listOf(VectorPaint.ACCENT, VectorPaint.REFERENCE, VectorPaint.TEXT)
                values.forEachIndexed { index, value ->
                    val top = 60 + index * 85
                    label(labels[index], { atX(22); atY(top); fill(VectorPaint.TEXT); fontSize(20) })
                    rect({ atX(22); atY(top + 12); width(300); height(22); fill(VectorPaint.SURFACE_RAISED); stroke(VectorPaint.BORDER) })
                    rect({ atX(22); atY(top + 12); width(300 * value); height(22); fill(colors[index]) })
                    label(number(value, 3), { atX(344); atY(top + 30); fill(VectorPaint.TEXT); fontSize(20) })
                }
                label("Feste Skala: kohärentes Zentralmaximum = 1", { atX(22); atY(324); fill(VectorPaint.MUTED); fontSize(17) })
            } else {
                val ox = 220.0
                val oy = 160.0
                val scale = 150.0
                val lx = ox + scale * left.real
                val ly = oy - scale * left.imaginary
                val sx = ox + scale * sum.real
                val sy = oy - scale * sum.imaginary
                circle({ cx(ox); cy(oy); radius(scale * .5); fill(VectorPaint.None); stroke(VectorPaint.BORDER); strokeWidth(1); dash(4, 5) })
                line({ x1(28); y1(oy); x2(410); y2(oy); stroke(VectorPaint.GRID); strokeWidth(1) })
                line({ x1(ox); y1(28); x2(ox); y2(294); stroke(VectorPaint.GRID); strokeWidth(1) })
                if (hypot(sum.real, sum.imaginary) > 1e-7) include(phasorArrow(ox, oy, sx, sy, VectorPaint.TEXT, 7.0, false))
                include(phasorArrow(ox, oy, lx, ly, VectorPaint.ACCENT, 4.0, false))
                include(phasorArrow(lx, ly, sx, sy, VectorPaint.REFERENCE, 4.0, true))
                circle({ cx(ox); cy(oy); radius(4); fill(VectorPaint.MUTED) })
                circle({ cx(sx); cy(sy); radius(6); fill(VectorPaint.TEXT); stroke(VectorPaint.SURFACE); strokeWidth(2) })
                label("Start", { atX(ox - 10); atY(oy + 25); anchor(TextAnchor.END); fill(VectorPaint.MUTED); fontSize(18) })
                label("Zeigerraum · keine räumliche Bahn", { atX(220); atY(324); anchor(TextAnchor.MIDDLE); fill(VectorPaint.MUTED); fontSize(19) })
            }
        }
    }
}

private fun phasorArrow(fromX: Double, fromY: Double, toX: Double, toY: Double, color: VectorPaint, weight: Double, dashed: Boolean): VectorFragment {
    if (hypot(toX - fromX, toY - fromY) < 1e-7) return vector {}
    val angle = atan2(toY - fromY, toX - fromX)
    val head = 10.0
    val wing = .45
    return vector {
        line({
            x1(fromX); y1(fromY); x2(toX); y2(toY); stroke(color); strokeWidth(weight)
            if (dashed) dash(8, 4)
        })
        path({
            geometry("M${toX - head * cos(angle - wing)} ${toY - head * sin(angle - wing)} L$toX $toY L${toX - head * cos(angle + wing)} ${toY - head * sin(angle + wing)}")
            fill(VectorPaint.None); stroke(color); strokeWidth(weight)
        })
    }
}

/** A deliberately magnified direction construction, never the numerical source of the phase. */
private fun pathSketch(model: FiniteSlitModel, position: ObservationPosition, configuration: SlitConfiguration): VectorFragment {
    val center = 112.0
    val halfSeparation = model.slitSpacing.micrometers * 8.0
    val pointY = center - 82 * (position.meters / PHASE_WINDOW_M).coerceIn(-1.0, 1.0)
    val leftOpen = configuration != SlitConfiguration.RIGHT_ONLY
    val rightOpen = configuration != SlitConfiguration.LEFT_ONLY
    return vector {
        svg({
            elementId(SlitPhaseId.PATH_DIAGRAM); viewBox(0, 0, 440, 250); imageRole()
            ariaLabel("Schematisch gedrehte Draufsicht: linker und rechter Spaltbeitrag führen geometrisch zum selben gewählten Punkt P auf dem Modellschirm. Winkel und Spaltabstände sind stark überhöht; keine beobachteten Elektronenbahnen.")
        }) {
            title("Zwei Spaltmitten, ein gewählter Schirmpunkt")
            line({ x1(70); y1(16); x2(70); y2(209); stroke(VectorPaint.BORDER); strokeWidth(6) })
            line({ x1(350); y1(16); x2(350); y2(209); stroke(VectorPaint.MUTED); strokeWidth(3) })
            line({ x1(70); y1(center); x2(350); y2(center); stroke(VectorPaint.GRID); strokeWidth(1); dash(4, 5) })
            if (leftOpen) line({ x1(70); y1(center + halfSeparation); x2(350); y2(pointY); stroke(VectorPaint.ACCENT); strokeWidth(3) })
            if (rightOpen) line({ x1(70); y1(center - halfSeparation); x2(350); y2(pointY); stroke(VectorPaint.REFERENCE); strokeWidth(3); dash(8, 4) })
            circle({ cx(70); cy(center + halfSeparation); radius(5); fill(if (leftOpen) VectorPaint.ACCENT else VectorPaint.BORDER) })
            circle({ cx(70); cy(center - halfSeparation); radius(5); fill(if (rightOpen) VectorPaint.REFERENCE else VectorPaint.BORDER) })
            circle({ cx(350); cy(pointY); radius(6); fill(VectorPaint.TEXT) })
            label("links", { atX(61); atY(center + halfSeparation + 24); anchor(TextAnchor.END); fill(VectorPaint.ACCENT); fontSize(18) })
            label("rechts", { atX(61); atY(center - halfSeparation - 10); anchor(TextAnchor.END); fill(VectorPaint.REFERENCE); fontSize(18) })
            label("P", { atX(369); atY(pointY + 6); fill(VectorPaint.TEXT); fontSize(23) })
            label("Blende", { atX(70); atY(240); anchor(TextAnchor.MIDDLE); fill(VectorPaint.MUTED); fontSize(18) })
            label("Modellschirm", { atX(350); atY(240); anchor(TextAnchor.MIDDLE); fill(VectorPaint.MUTED); fontSize(18) })
        }
    }
}
