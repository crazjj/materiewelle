package materiewelle.ui.semantics

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent

/** Actual user scroll intent interrupts following; programmatic scroll events do not. */
fun observeReadingInterruption(interrupt: () -> Unit): () -> Unit {
    val pointer: (Event) -> Unit = { interrupt() }
    val keys: (Event) -> Unit = { event ->
        if (event is KeyboardEvent && LabKey.fromBrowser(event.key) in
            setOf(LabKey.UP, LabKey.DOWN, LabKey.PAGE_UP, LabKey.PAGE_DOWN, LabKey.HOME, LabKey.END)) interrupt()
    }
    window.addEventListener(BrowserEvent.WHEEL.eventName, pointer)
    window.addEventListener(BrowserEvent.TOUCH_MOVE.eventName, pointer)
    window.addEventListener(BrowserEvent.KEY_DOWN.eventName, keys)
    return {
        window.removeEventListener(BrowserEvent.WHEEL.eventName, pointer)
        window.removeEventListener(BrowserEvent.TOUCH_MOVE.eventName, pointer)
        window.removeEventListener(BrowserEvent.KEY_DOWN.eventName, keys)
    }
}

/** A revealed line scrolls only if clipped; its previous explanation remains above it. */
fun followReadingLine(target: ElementId): () -> Unit {
    val frame = window.requestAnimationFrame {
        document.findHtmlElement(target)?.let { element ->
            val box = element.getBoundingClientRect()
            if (box.bottom > window.innerHeight - 24) {
                element.scrollIntoView(kotlin.js.json("block" to "end", "behavior" to "smooth"))
            }
        }
    }
    return { window.cancelAnimationFrame(frame) }
}
