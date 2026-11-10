package materiewelle.ui.semantics

import androidx.compose.runtime.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlin.math.max
import kotlin.math.min

private data class MeasurementFocusRequest(val target: ElementId, val sequence: Int)

/** Only explicit result actions move focus. Changing a mark or a band never scrolls the page. */
@Composable
fun rememberMeasurementResultFocus(): (ElementId) -> Unit {
    var request by remember { mutableStateOf<MeasurementFocusRequest?>(null) }
    DisposableEffect(request) {
        val frame = request?.let { requested ->
            window.requestAnimationFrame {
                document.findHtmlElement(requested.target)
                    ?.takeIf { it.getClientRects().length > 0 }
                    ?.let { result ->
                        result.focusWithoutScrolling()
                        val header = document.findHtmlElement(ShellId.HEADER)
                        val sticky = header?.let {
                            window.getComputedStyle(it).position in setOf("sticky", "fixed")
                        } == true
                        val top = if (sticky) max(0.0, header!!.getBoundingClientRect().bottom) else 0.0
                        val box = result.getBoundingClientRect()
                        // Leave a result already in view where it is. Otherwise reveal its heading
                        // and first readout below the real header, without a motion preference trap.
                        if (box.top < top + 12.0 || box.top + min(180.0, box.height) > window.innerHeight - 12.0) {
                            window.scrollBy(0.0, box.top - top - 16.0)
                        }
                    }
            }
        }
        onDispose { frame?.let(window::cancelAnimationFrame) }
    }
    return { target -> request = MeasurementFocusRequest(target, (request?.sequence ?: 0) + 1) }
}
