package materiewelle.learning

import androidx.compose.runtime.*
import kotlinx.browser.window
import materiewelle.ui.semantics.BrowserMedia
import materiewelle.ui.semantics.BrowserEvent
import org.w3c.dom.events.Event
import materiewelle.ui.LocalLabMotion

private external interface PageVisibility { val hidden: Boolean }

@Composable
fun rememberReducedMotion(): Boolean {
    var reduced by remember { mutableStateOf(window.matchMedia(BrowserMedia.REDUCED_MOTION).matches) }
    DisposableEffect(Unit) {
        val media = window.matchMedia(BrowserMedia.REDUCED_MOTION)
        val listener: (Event) -> Unit = { reduced = media.matches }
        media.addEventListener(BrowserEvent.CHANGE.eventName, listener)
        onDispose { media.removeEventListener(BrowserEvent.CHANGE.eventName, listener) }
    }
    return reduced
}

/** One time source; pause on hidden page/tab, stop on reduced motion or at the end. */
@Composable
fun TimelineClock(running: Boolean, active: Boolean, reduced: Boolean, advance: (Double) -> Unit, pause: () -> Unit) {
    val motion = LocalLabMotion.current
    val currentAdvance by rememberUpdatedState(advance)
    val currentPause by rememberUpdatedState(pause)
    LaunchedEffect(reduced) { if (reduced) currentPause() }
    DisposableEffect(running, active, reduced, motion.active) {
        var last: Double? = null
        var frame = 0
        var disposed = false
        fun tick(timestamp: Double) {
            if (disposed) return
            if (kotlinx.browser.document.unsafeCast<PageVisibility>().hidden) { currentPause(); return }
            val previous = last
            last = timestamp
            if (previous != null) currentAdvance(((timestamp - previous) / 1000).coerceIn(0.0, .1))
            frame = window.requestAnimationFrame(::tick)
        }
        if (running && active && !reduced && motion.active) frame = window.requestAnimationFrame(::tick)
        onDispose { disposed = true; window.cancelAnimationFrame(frame) }
    }
}
