package materiewelle.ui

import androidx.compose.runtime.*
import kotlinx.browser.document
import kotlinx.browser.window
import materiewelle.learning.rememberReducedMotion
import materiewelle.presentation.PresentationKeys
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.attributes.disabled
import org.w3c.dom.events.Event

internal object MotionStorage {
    const val KEY = PresentationKeys.MOTION

    fun load() =
        MotionPreference.fromStorage(runCatching { window.localStorage.getItem(KEY) }.getOrNull())

    fun save(value: MotionPreference) {
        runCatching { window.localStorage.setItem(KEY, value.key) }
    }
}

data class MotionController(
    val preference: MotionPreference,
    val state: MotionState,
    val toggle: () -> Unit,
) {
    val active
        get() = state == MotionState.RUNNING
}

val LocalLabMotion = staticCompositionLocalOf {
    MotionController(MotionPreference.ANIMATED, MotionState.RUNNING) {}
}

@Composable
fun LabMotion(content: @Composable () -> Unit) {
    var preference by remember { mutableStateOf(MotionStorage.load()) }
    val reduced = rememberReducedMotion()
    var visible by remember { mutableStateOf(isDocumentVisible()) }
    DisposableEffect(Unit) {
        val listener: (Event) -> Unit = { visible = isDocumentVisible() }
        document.addEventListener(BrowserEvent.VISIBILITY_CHANGE.eventName, listener)
        onDispose {
            document.removeEventListener(BrowserEvent.VISIBILITY_CHANGE.eventName, listener)
        }
    }
    val state = resolveMotion(preference, reduced, visible)
    SideEffect { applyMotionState(state) }
    CompositionLocalProvider(
        LocalLabMotion provides
            MotionController(preference, state) {
                preference = preference.toggled()
                MotionStorage.save(preference)
            }
    ) {
        content()
    }
}

@Composable
fun MotionChooser() {
    val motion = LocalLabMotion.current
    Button(
        attrs = {
            elementId(MotionId.TOGGLE)
            motionStyles(MotionClass.TOGGLE)
            ariaLabel("Animationen")
            ariaPressed(motion.active)
            if (motion.state == MotionState.REDUCED) disabled()
            title(
                if (motion.state == MotionState.REDUCED)
                    "Animationen sind durch die Einstellung „Bewegung reduzieren“ deines Geräts ausgeschaltet."
                else "Automatische Bewegung und Übergänge ein- oder ausschalten"
            )
            onClick { motion.toggle() }
        }
    ) {
        Span(
            attrs = {
                motionStyles(MotionClass.ICON)
                ariaHidden(true)
            }
        ) {
            repeat(3) { Span {} }
        }
        Span {
            Text(
                if (motion.state == MotionState.REDUCED) "Ruhe · System"
                else if (motion.preference == MotionPreference.STILL) "Ruhe" else "Bewegung"
            )
        }
    }
}
