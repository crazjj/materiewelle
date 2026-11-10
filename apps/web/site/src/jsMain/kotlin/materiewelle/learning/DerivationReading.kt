package materiewelle.learning

import androidx.compose.runtime.*
import kotlinx.browser.window
import materiewelle.ui.LocalLabMotion
import materiewelle.ui.MathText as Text
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.*

class DerivationReading internal constructor(val scope: DerivationReadingScope) {
    var mode by mutableStateOf(DerivationReadingMode.MANUAL)
        private set

    var suspended by mutableStateOf(false)
        private set

    private var request by mutableStateOf(0)
    internal val followRequest
        get() = request

    fun choose(value: DerivationReadingMode) {
        cancelReadingScroll()
        mode = value
        suspended = false
    }

    fun pauseFollowing() {
        if (mode == DerivationReadingMode.FOLLOW && !suspended) {
            suspended = true
            cancelReadingScroll()
        }
    }

    fun resume() {
        suspended = false
        request++
    }
}

@Composable
fun rememberDerivationReading(
    scope: DerivationReadingScope,
    step: Int,
    lastStep: Int,
    active: Boolean = true,
    next: () -> Unit,
    pause: () -> Unit,
): DerivationReading {
    val reading = remember(scope) { DerivationReading(scope) }
    val motion = LocalLabMotion.current
    val currentNext by rememberUpdatedState(next)
    val currentPause by rememberUpdatedState(pause)
    val currentStep by rememberUpdatedState(step)
    val currentLastStep by rememberUpdatedState(lastStep)
    LaunchedEffect(reading.mode) {
        if (reading.mode == DerivationReadingMode.SCROLL) currentPause()
    }
    LaunchedEffect(active, motion.active) {
        if (active && !motion.active && reading.mode == DerivationReadingMode.FOLLOW) {
            reading.pauseFollowing()
            currentPause()
        }
    }
    DisposableEffect(reading, active, reading.mode) {
        val stop =
            if (active)
                observeReadingScroll(
                    scope,
                    interrupted = {
                        if (reading.mode == DerivationReadingMode.FOLLOW) {
                            reading.pauseFollowing()
                            currentPause()
                        }
                    },
                    reachedFrontier = {
                        if (
                            reading.mode == DerivationReadingMode.SCROLL &&
                                currentStep < currentLastStep
                        )
                            currentNext()
                    },
                )
            else ({})
        onDispose {
            stop()
            if (active) cancelReadingScroll()
        }
    }
    var previousStep by remember(scope) { mutableStateOf(step) }
    var previousRequest by remember(scope) { mutableStateOf(reading.followRequest) }
    DisposableEffect(
        step,
        reading.mode,
        reading.suspended,
        reading.followRequest,
        motion.active,
        active,
    ) {
        val shouldFollow = step != previousStep || reading.followRequest != previousRequest
        previousStep = step
        previousRequest = reading.followRequest
        var frame = 0
        if (
            shouldFollow &&
                active &&
                reading.mode == DerivationReadingMode.FOLLOW &&
                !reading.suspended &&
                motion.active
        )
            frame = window.requestAnimationFrame { followReadingCard(scope, step) }
        onDispose { window.cancelAnimationFrame(frame) }
    }
    return reading
}

@Composable
fun DerivationReadingControls(reading: DerivationReading, actions: @Composable () -> Unit) {
    val motion = LocalLabMotion.current
    Div(
        attrs = {
            elementId(DerivationReadingId(reading.scope, DerivationReadingPart.CONTROLS))
            readingStyles(DerivationReadingClass.CONTROLS)
        }
    ) {
        Div(
            attrs = {
                readingStyles(DerivationReadingClass.MODES)
                role(AriaRole.GROUP)
                ariaLabel("Lesefluss der Herleitung")
            }
        ) {
            DerivationReadingMode.entries.forEach { mode ->
                Button(
                    attrs = {
                        elementId(DerivationReadingChoiceId(reading.scope, mode))
                        styles(LabClass.SECONDARY, LabClass.COMPACT)
                        if (reading.mode == mode) readingStyles(DerivationReadingClass.SELECTED)
                        ariaPressed(reading.mode == mode)
                        if (mode == DerivationReadingMode.FOLLOW && !motion.active) disabled()
                        onClick { reading.choose(mode) }
                    }
                ) {
                    Text(mode.label)
                }
            }
        }
        Div(attrs = { readingStyles(DerivationReadingClass.ACTIONS) }) { actions() }
        P(
            attrs = {
                elementId(DerivationReadingId(reading.scope, DerivationReadingPart.STATUS))
                styles(LabClass.SMALL, LabClass.MUTED)
            }
        ) {
            Text(
                when {
                    reading.mode == DerivationReadingMode.SCROLL ->
                        "Dein Scrollen deckt den nächsten Schritt auf. Frühere Karten bleiben; Zurückscrollen versteckt nichts."
                    reading.mode == DerivationReadingMode.FOLLOW && reading.suspended ->
                        "Begleitung pausiert: Du liest selbst. Die bereits geöffneten Karten bleiben erhalten."
                    reading.mode == DerivationReadingMode.FOLLOW ->
                        "Beim Ergänzen folgt die Ansicht der neuen Karte. Eigenes Scrollen oder Berühren außerhalb der Leiste pausiert die Begleitung."
                    else -> "Du bestimmst die Schritte. Die Ansicht scrollt nicht automatisch."
                }
            )
        }
        if (reading.mode == DerivationReadingMode.FOLLOW && reading.suspended && motion.active)
            Button(
                attrs = {
                    elementId(DerivationReadingId(reading.scope, DerivationReadingPart.RESUME))
                    styles(LabClass.SECONDARY, LabClass.COMPACT)
                    onClick { reading.resume() }
                }
            ) {
                Text("Zur aktuellen Karte folgen")
            }
    }
}

@Composable
fun DerivationReadingFrontier(reading: DerivationReading, step: Int, lastStep: Int) {
    if (reading.mode == DerivationReadingMode.SCROLL && step < lastStep)
        Div(
            attrs = {
                elementId(DerivationReadingId(reading.scope, DerivationReadingPart.FRONTIER))
                readingStyles(DerivationReadingClass.FRONTIER)
            }
        ) {
            Text("↓ Weiter nach unten scrollen oder „Nächster Schritt“ wählen")
        }
}
