package materiewelle.ui.semantics

import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.attributes.AttrsScope
import org.w3c.dom.AUTO
import org.w3c.dom.Node
import org.w3c.dom.SMOOTH
import org.w3c.dom.ScrollBehavior
import org.w3c.dom.ScrollToOptions
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent

fun AttrsScope<*>.readingStyles(vararg styles: DerivationReadingClass) {
    classes(*styles.map { it.cssName }.toTypedArray())
}

private enum class ReadingScrollKey(val browser: String) {
    UP("ArrowUp"),
    DOWN("ArrowDown"),
    PAGE_UP("PageUp"),
    PAGE_DOWN("PageDown"),
    HOME("Home"),
    END("End"),
    SPACE(" ");

    companion object {
        fun matches(key: String) = entries.any { it.browser == key }
    }
}

private fun scrollReadingTo(top: Double, behavior: ScrollBehavior) {
    window.scrollTo(
        ScrollToOptions(top = top.coerceAtLeast(0.0), left = window.scrollX, behavior = behavior)
    )
}

fun cancelReadingScroll() = scrollReadingTo(window.scrollY, ScrollBehavior.AUTO)

/** Align a heading, not a tall card's centre. Leave room for the measured sticky controls. */
fun followReadingCard(scope: DerivationReadingScope, index: Int) {
    val target = document.findHtmlElement(DerivationReadingCardId(scope, index)) ?: return
    val controls =
        document.findHtmlElement(DerivationReadingId(scope, DerivationReadingPart.CONTROLS))
    val phase =
        if (scope == DerivationReadingScope.COURSE) document.findHtmlElement(Id.COURSE_PHASE)
        else null
    val top =
        (controls?.getBoundingClientRect()?.height ?: 0.0) +
            (phase?.getBoundingClientRect()?.height ?: 0.0) +
            20.0
    val rect = target.getBoundingClientRect()
    if (rect.top >= top && rect.bottom <= window.innerHeight - 20) return
    scrollReadingTo(window.scrollY + rect.top - top, ScrollBehavior.SMOOTH)
}

/** Passive observation only: no preventDefault, wheel rewriting, or focus changes. */
@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
fun observeReadingScroll(
    scope: DerivationReadingScope,
    interrupted: () -> Unit,
    reachedFrontier: () -> Unit,
): () -> Unit {
    var lastY = window.scrollY
    var intent = false
    var frame = 0
    fun insideControls(event: Event): Boolean {
        val controls =
            document.findHtmlElement(DerivationReadingId(scope, DerivationReadingPart.CONTROLS))
        return controls?.contains(event.target as? Node) == true
    }
    val gesture: (Event) -> Unit = {
        intent = true
        interrupted()
    }
    val pointer: (Event) -> Unit = { event ->
        intent = true
        if (!insideControls(event)) interrupted()
    }
    val keyboard: (Event) -> Unit = { event ->
        val key = event as? KeyboardEvent
        if (
            key != null &&
                ReadingScrollKey.matches(key.key) &&
                (key.key != ReadingScrollKey.SPACE.browser || !insideControls(event))
        ) {
            intent = true
            interrupted()
        }
    }
    val focus: (Event) -> Unit = { event ->
        if (event.target != document.body && !insideControls(event)) interrupted()
    }
    val scroll: (Event) -> Unit = {
        if (frame == 0)
            frame =
                window.requestAnimationFrame {
                    frame = 0
                    val y = window.scrollY
                    val downward = y > lastY + 1.0
                    lastY = y
                    val frontier =
                        document.findHtmlElement(
                            DerivationReadingId(scope, DerivationReadingPart.FRONTIER)
                        )
                    val rect = frontier?.getBoundingClientRect()
                    if (
                        intent &&
                            downward &&
                            rect != null &&
                            frontier.getClientRects().length > 0 &&
                            rect.top < window.innerHeight * .84 &&
                            rect.bottom > 0
                    ) {
                        intent = false
                        reachedFrontier()
                    }
                }
    }
    window.addEventListener(BrowserEvent.WHEEL.eventName, gesture)
    window.addEventListener(BrowserEvent.TOUCH_MOVE.eventName, gesture)
    window.addEventListener(BrowserEvent.POINTER_DOWN.eventName, pointer)
    window.addEventListener(BrowserEvent.KEY_DOWN.eventName, keyboard)
    window.addEventListener(BrowserEvent.FOCUS_IN.eventName, focus)
    window.addEventListener(BrowserEvent.SCROLL.eventName, scroll)
    return {
        window.cancelAnimationFrame(frame)
        window.removeEventListener(BrowserEvent.WHEEL.eventName, gesture)
        window.removeEventListener(BrowserEvent.TOUCH_MOVE.eventName, gesture)
        window.removeEventListener(BrowserEvent.POINTER_DOWN.eventName, pointer)
        window.removeEventListener(BrowserEvent.KEY_DOWN.eventName, keyboard)
        window.removeEventListener(BrowserEvent.FOCUS_IN.eventName, focus)
        window.removeEventListener(BrowserEvent.SCROLL.eventName, scroll)
    }
}
