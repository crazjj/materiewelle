package materiewelle.ui.semantics

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.Element
import org.w3c.dom.events.Event

private external interface DgResizeWindow { val ResizeObserver: Any? }

@JsName("ResizeObserver")
private external class DgResizeObserver(callback: () -> Unit) {
    fun observe(element: Element)
    fun disconnect()
}

/**
 * The shell can wrap to two lines. Measure both navigation heights rather than
 * placing the chapter bar beneath a fixed, assumed header. Observation is local
 * to this mounted chapter; scrolling itself needs no listener or Compose state.
 */
internal fun observeDgLessonInsets(): () -> Unit {
    val root = document.findHtmlElement(DgArticleId.ROOT) ?: return {}
    val header = document.findHtmlElement(ShellId.HEADER)
    val navigation = document.findHtmlElement(DgLessonId.NAV) ?: return {}
    var disposed = false
    var frame = 0
    fun measure() {
        if (disposed) return
        val headerHeight = header?.takeIf {
            window.getComputedStyle(it).position in setOf("sticky", "fixed")
        }?.getBoundingClientRect()?.height ?: 0.0
        val chapterHeight = navigation.takeIf {
            window.getComputedStyle(it).position == "sticky"
        }?.getBoundingClientRect()?.height ?: 0.0
        root.style.setProperty(DgLessonProperty.NAV_TOP.cssName, "${headerHeight}px")
        root.style.setProperty(DgLessonProperty.FIGURE_TOP.cssName, "${headerHeight + chapterHeight + 12.0}px")
    }
    fun schedule() {
        if (!disposed && frame == 0) frame = window.requestAnimationFrame { frame = 0; measure() }
    }
    // Before the route's positioning frame: direct links include the measured bar.
    measure()
    val listener: (Event) -> Unit = { schedule() }
    window.addEventListener(BrowserEvent.RESIZE.eventName, listener)
    val resize = if (jsTypeOf(window.unsafeCast<DgResizeWindow>().ResizeObserver) == "function") {
        DgResizeObserver { schedule() }.also { observer ->
            header?.let(observer::observe)
            observer.observe(navigation)
        }
    } else null
    val cancelFonts = afterFontMetricsReady { schedule() }
    return {
        disposed = true
        resize?.disconnect()
        cancelFonts()
        window.cancelAnimationFrame(frame)
        window.removeEventListener(BrowserEvent.RESIZE.eventName, listener)
    }
}
