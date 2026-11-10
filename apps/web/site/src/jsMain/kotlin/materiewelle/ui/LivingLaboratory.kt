package materiewelle.ui

import androidx.compose.runtime.*
import kotlin.math.PI
import kotlin.math.sin
import materiewelle.NativeSvg
import materiewelle.core.Experiment
import materiewelle.core.Physics
import materiewelle.ui.MathText as Text
import materiewelle.ui.semantics.*
import materiewelle.ui.vector.*
import org.jetbrains.compose.web.dom.*

/** CSS advances a calculated, periodic spatial form. No invented flight time or particle path. */
private fun spatialWave(period: Double, height: Int): VectorFragment {
    val middle = height / 2.0
    val amplitude = height * .28
    val wave =
        (0..(760 + period.toInt() + 3) step 2).joinToString(" ") { x ->
            "${if (x == 0) "M" else "L"}$x,${middle - amplitude * sin(2 * PI * x / period)}"
        }
    return vector {
        svg({
            viewBox(0, 0, 760, height)
            ariaHidden()
        }) {
            group({ styles(MotionClass.GRID) }) {
                path { geometry("M0 $middle H760") }
                for (x in 0..760 step 76) path { geometry("M$x 0 V$height") }
            }
            path {
                styles(MotionClass.WAVE_GHOST)
                geometry(wave)
            }
            path {
                styles(MotionClass.WAVE_TRACE)
                geometry(wave)
            }
        }
    }
}

@Composable
fun LivingLaboratory() {
    var inView by remember { mutableStateOf(false) }
    Aside(
        attrs = {
            elementId(MotionId.HERO)
            motionStyles(MotionClass.HERO)
            motionSurface(MotionSurface.HERO)
            motionInView(inView)
            ariaLabel("Animierte schematische Wellenform")
            ref { element ->
                val stop = observeMotionSurface(element) { inView = it }
                onDispose { stop() }
            }
        }
    ) {
        Div(
            attrs = {
                motionStyles(MotionClass.FIGURE)
                wavePeriod(152.0)
                ariaHidden(true)
            }
        ) {
            NativeSvg(remember { spatialWave(152.0, 210) }, LabClass.CHART)
        }
    }
}

/** The correct value is immediate; only its background briefly acknowledges a validated change. */
@Composable
fun ReadingUpdate(value: Any?, content: @Composable () -> Unit) {
    key(value) { Span(attrs = { motionStyles(MotionClass.READING_UPDATE) }) { content() } }
}

/** The period uses the same validated voltage and pure physics kernel as the main chart. */
@Composable
fun WavelengthMotion(voltage: Double) {
    val period =
        120.0 * Physics.wavelengthNonRelativistic(voltage) /
            Physics.wavelengthNonRelativistic(150.0)
    var inView by remember { mutableStateOf(false) }
    Section(
        attrs = {
            elementId(MotionId.WAVE)
            motionStyles(MotionClass.WAVE_STRIP)
            motionSurface(MotionSurface.WAVELENGTH)
            motionInView(inView)
            wavePeriod(period)
            ariaLabel("Bewegte schematische Wellenform")
            ref { element ->
                val stop = observeMotionSurface(element) { inView = it }
                onDispose { stop() }
            }
        }
    ) {
        Div(attrs = { motionStyles(MotionClass.WAVE_META) }) {
            Span { Text("ABSTAND FOLGT DER SPANNUNG") }
            Span {
                Text(
                    "${Physics.decimal(voltage, 0)} V · ${Physics.decimal(Physics.wavelengthNonRelativistic(voltage) * 1e12, 2)} pm"
                )
            }
        }
        NativeSvg(remember(period) { spatialWave(period, 86) })
        P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
            Text(
                "Wellenabstand proportional zu λ · vergrößerte Schemazeichnung. Animationstempo frei gewählt; keine Teilchenbahn oder reale Flugzeit."
            )
        }
    }
}

/** Sequential highlights communicate functional order, never a measured electron trajectory. */
@Composable
fun ApparatusSignal(experiment: Experiment) {
    val stages =
        when (experiment) {
            Experiment.DE_BROGLIE -> listOf("Spannung", "Energie", "Impuls", "Wellenlänge")
            Experiment.DAVISSON_GERMER -> listOf("Quelle", "Kristall", "Winkelwahl", "Detektor")
            Experiment.JONSSON -> listOf("Quelle", "Doppelspalt", "Elektronenoptik", "Reproduktion")
        }
    var inView by remember { mutableStateOf(false) }
    Div(
        attrs = {
            motionStyles(MotionClass.SIGNAL)
            motionSurface(MotionSurface.APPARATUS)
            motionInView(inView)
            ariaLabel("Funktionsfolge")
            ref { element ->
                val stop = observeMotionSurface(element) { inView = it }
                onDispose { stop() }
            }
        }
    ) {
        Div(attrs = { motionStyles(MotionClass.SIGNAL_STAGES) }) {
            stages.forEachIndexed { index, label ->
                Span(
                    attrs = {
                        motionStyles(MotionClass.SIGNAL_NODE)
                        motionIndex(index)
                    }
                ) {
                    Span(
                        attrs = {
                            motionStyles(MotionClass.DOT)
                            ariaHidden(true)
                        }
                    ) {}
                    Text(label)
                }
            }
        }
        Span(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
            Text("Funktionsschema · keine Flugzeit")
        }
    }
}
