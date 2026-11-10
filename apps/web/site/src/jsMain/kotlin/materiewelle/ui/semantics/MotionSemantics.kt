package materiewelle.ui.semantics

import kotlinx.browser.document
import materiewelle.presentation.styles.*
import materiewelle.ui.MotionState
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.css.*
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement

fun AttrsScope<*>.motionStyles(vararg names: MotionClass) {
    classes(*names.map { it.cssName }.toTypedArray())
}

fun AttrsScope<*>.motionSurface(value: MotionSurface) {
    attr("data-motion-surface", value.key)
}

fun AttrsScope<*>.motionInView(value: Boolean) {
    attr("data-motion-in-view", value.toString())
}

fun AttrsScope<*>.motionGreeting(value: Boolean) {
    attr("data-greeting", value.toString())
}

fun AttrsScope<*>.motionEntered(value: Boolean) {
    attr("data-motion-entered", value.toString())
}

fun AttrsScope<*>.motionIndex(index: Int) {
    require(index in 0..3)
    style { setToken(Tokens.SIGNAL_INDEX, index) }
}

fun AttrsScope<*>.wavePeriod(value: Double) {
    require(value.isFinite() && value > 0)
    style { setToken(Tokens.WAVE_SHIFT, (-value).px) }
    attr("data-spatial-period", value.toString())
}

fun applyMotionState(value: MotionState) {
    document.documentElement?.setAttribute("data-motion", value.key)
}

private external interface VisibilityEntry {
    val isIntersecting: Boolean
}

@JsName("IntersectionObserver")
private external class MotionIntersectionObserver(callback: (Array<VisibilityEntry>) -> Unit) {
    fun observe(element: Element)

    fun disconnect()
}

private external interface VisibleDocument {
    val hidden: Boolean
}

fun isDocumentVisible(): Boolean = !document.unsafeCast<VisibleDocument>().hidden

/** Native observer, released with its Compose surface; no timer or per-frame recomposition. */
fun observeMotionSurface(element: HTMLElement, update: (Boolean) -> Unit): () -> Unit {
    return try {
        val observer = MotionIntersectionObserver { entries ->
            entries.lastOrNull()?.let { update(it.isIntersecting) }
        }
        observer.observe(element)
        val disconnect: () -> Unit = { observer.disconnect() }
        disconnect
    } catch (_: Throwable) {
        // Older browsers still render the content; the motion preference remains authoritative.
        update(true)
        val noObservation: () -> Unit = {}
        noObservation
    }
}
