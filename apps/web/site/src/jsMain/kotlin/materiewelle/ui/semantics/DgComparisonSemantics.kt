package materiewelle.ui.semantics

import org.jetbrains.compose.web.attributes.AttrsScope
import kotlinx.browser.document
import kotlinx.browser.window

fun AttrsScope<*>.dgComparisonStyles(vararg names: DgComparisonClass) {
    classes(*names.map { it.cssName }.toTypedArray())
}

/** Explicit open/return only: account for both the site header and the sticky DG step bar. */
@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
fun requestDgComparisonFocus(target: ElementId): () -> Unit {
    val frame = window.requestAnimationFrame {
        document.findHtmlElement(target)?.takeIf { it.getClientRects().length > 0 }?.let { element ->
            element.focusWithoutScrolling()
            element.scrollIntoView(true)
            val occupiedTop = listOf<ElementId>(ShellId.HEADER, DgLessonId.NAV).mapNotNull { id ->
                document.findHtmlElement(id)?.takeIf {
                    window.getComputedStyle(it).position in setOf("sticky", "fixed")
                }?.getBoundingClientRect()?.bottom
            }.maxOrNull()?.coerceAtLeast(0.0) ?: 0.0
            window.scrollBy(0.0, element.getBoundingClientRect().top - occupiedTop - 16.0)
        }
    }
    return { window.cancelAnimationFrame(frame) }
}
