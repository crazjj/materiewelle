package materiewelle.ui

import androidx.compose.runtime.*
import materiewelle.NativeSvg
import materiewelle.core.course.ArgumentPhase
import materiewelle.ui.semantics.*
import materiewelle.ui.vector.*
import org.jetbrains.compose.web.dom.Span

/** Decorative phase identities: no measured time, particle trajectory or progress claim. */
@Composable
fun ArgumentGlyph(phase: ArgumentPhase, current: Boolean) {
    var inView by remember { mutableStateOf(false) }
    Span(
        attrs = {
            motionStyles(MotionClass.PHASE_ICON)
            if (current) motionStyles(MotionClass.PHASE_CURRENT)
            ariaHidden(true)
            motionInView(inView)
            ref { element ->
                val stop = observeMotionSurface(element) { inView = it }
                onDispose { stop() }
            }
        }
    ) {
        NativeSvg(remember(phase) { phaseDrawing(phase) })
    }
}

private fun phaseDrawing(phase: ArgumentPhase): VectorFragment = vector {
    svg({
        viewBox(0, 0, 32, 32)
        ariaHidden()
    }) {
        group({
            fill(VectorPaint.None)
            stroke(VectorPaint.CURRENTCOLOR)
            strokeWidth(1.6)
        }) {
            when (phase) {
                ArgumentPhase.IDEA -> {
                    circle({
                        cx(16)
                        cy(13)
                        radius(8)
                        styles(MotionClass.PHASE_PULSE)
                    })
                    path({ geometry("M12 22 H20 M13 26 H19 M14 20 V15 L12 12 M18 20 V15 L20 12") })
                }
                ArgumentPhase.PREDICTION -> {
                    path({
                        geometry("M2 17 C6 3 10 3 14 17 S22 31 30 17")
                        pathLength(100)
                        styles(MotionClass.PHASE_INK)
                    })
                    path({
                        geometry("M2 17 H30")
                        strokeOpacity(.25)
                    })
                }
                ArgumentPhase.EXPERIMENT -> {
                    rect({
                        atX(5)
                        atY(5)
                        width(22)
                        height(22)
                        cornerRadius(4)
                    })
                    path({
                        geometry("M8 16 H24")
                        styles(MotionClass.PHASE_SCAN)
                    })
                    path({
                        geometry("M12 9 V23 M20 9 V23")
                        strokeOpacity(.25)
                    })
                }
                ArgumentPhase.JUDGEMENT -> {
                    circle({
                        cx(16)
                        cy(16)
                        radius(12)
                        strokeOpacity(.4)
                    })
                    path({
                        geometry("M9 16 L14 21 L23 11")
                        pathLength(100)
                        styles(MotionClass.PHASE_INK)
                    })
                }
            }
        }
    }
}
