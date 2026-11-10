package materiewelle.ui.semantics

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLElement

private external interface FocusOptions { var preventScroll: Boolean }
private external interface NativeFocusable { fun focus(options: FocusOptions) }

/** Browser interop is confined here; callers decide whether an explicit gesture needs a scroll. */
fun HTMLElement.focusWithoutScrolling() {
    val options = kotlin.js.json().unsafeCast<FocusOptions>()
    options.preventScroll = true
    unsafeCast<NativeFocusable>().focus(options)
}

fun requestInlineTutorFocus(target: ElementId, returnTop: Double? = null, panel: ElementId? = null): () -> Unit {
    val frame = window.requestAnimationFrame {
        document.findHtmlElement(target)?.let { element ->
            element.focusWithoutScrolling()
            if (returnTop != null) window.scrollBy(0.0, element.getBoundingClientRect().top - returnTop)
            else if (panel != null) {
                val header = document.findHtmlElement(ShellId.HEADER)
                val sticky = header?.let { window.getComputedStyle(it).position in setOf("sticky", "fixed") } == true
                val top = if (sticky) kotlin.math.max(0.0, header!!.getBoundingClientRect().bottom) + 16.0 else 16.0
                val bounds = element.getBoundingClientRect()
                if (bounds.top < top || bounds.bottom > window.innerHeight - 24) {
                    val surface = document.findHtmlElement(panel) ?: element
                    window.scrollBy(0.0, surface.getBoundingClientRect().top - top)
                }
            }
        }
    }
    return { window.cancelAnimationFrame(frame) }
}

/** Position only an explicitly requested panel change, after its DOM has been composed. */
@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
fun requestPanelFocus(target: ElementId): () -> Unit {
    val frame = window.requestAnimationFrame {
        document.findHtmlElement(target)?.takeIf { it.getClientRects().length > 0 }?.let {
            it.focus()
            it.scrollIntoView(true)
        }
    }
    return { window.cancelAnimationFrame(frame) }
}

/** A single explicit help jump, with space for the measured sticky navigation.
 * No animated scrolling: the same predictable jump works with reduced motion.
 */
@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
fun requestTutorFocus(target: ElementId, panel: ElementId? = null): () -> Unit {
    val frame = window.requestAnimationFrame {
        document.findHtmlElement(target)?.takeIf { it.getClientRects().length > 0 }?.let { field ->
            field.focus()
            val surface = panel?.let(document::findHtmlElement) ?: field
            surface.scrollIntoView(true)
            val header = document.findHtmlElement(ShellId.HEADER)
            val sticky = header?.let { window.getComputedStyle(it).position in setOf("sticky", "fixed") } == true
            val top = if (sticky) kotlin.math.max(0.0, header!!.getBoundingClientRect().bottom) else 0.0
            window.scrollBy(0.0, surface.getBoundingClientRect().top - top - 16.0)
        }
    }
    return { window.cancelAnimationFrame(frame) }
}
