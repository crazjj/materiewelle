package materiewelle.ui.semantics

import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.attributes.AttrsScope
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.AUTO
import org.w3c.dom.ScrollBehavior
import org.w3c.dom.ScrollToOptions
import org.w3c.dom.events.Event
import kotlin.js.Json
import kotlin.js.json
import kotlin.math.abs

fun AttrsScope<*>.articleReadingStyles(vararg values: ArticleReadingClass) {
    classes(*values.map { it.cssName }.toTypedArray())
}

private external interface ReadingObserverWindow {
    val IntersectionObserver: Any?
    val ResizeObserver: Any?
}

private external interface ReadingIntersectionEntry { val isIntersecting: Boolean }

@JsName("IntersectionObserver")
private external class ReadingIntersectionObserver(
    callback: (Array<ReadingIntersectionEntry>) -> Unit,
    options: Json,
) {
    fun observe(element: Element)
    fun disconnect()
}

@JsName("ResizeObserver")
private external class ReadingResizeObserver(callback: () -> Unit) {
    fun observe(element: Element)
    fun disconnect()
}

private fun readingTop(): Double =
    ((document.findHtmlElement(ShellId.HEADER)?.getBoundingClientRect()?.bottom ?: 64.0) + 24.0)
        .coerceIn(48.0, (window.innerHeight / 2.0).coerceAtLeast(48.0))

/** A deliberate jump keeps its selected step until the reader scrolls again. */
private data class ExplicitReadingPosition(val index: Int, val scrollY: Double)
private var explicitReadingPosition: ExplicitReadingPosition? = null

/** Only an explicitly chosen step scrolls or receives focus. Passive reading never does. */
fun goToArticleDerivationStep(index: Int) {
    val target = document.findHtmlElement(ArticleLineId(index)) ?: return
    target.focus()
    val top = window.scrollY + target.getBoundingClientRect().top - readingTop()
    window.scrollTo(ScrollToOptions(top = top.coerceAtLeast(0.0), left = window.scrollX, behavior = ScrollBehavior.AUTO))
    explicitReadingPosition = ExplicitReadingPosition(index, window.scrollY)
}

/** An explicit return from the exercise waits for the chosen reading layout to be composed. */
fun requestArticleDerivationStep(index: Int, positioned: () -> Unit): () -> Unit {
    val frame = window.requestAnimationFrame {
        goToArticleDerivationStep(index)
        positioned()
    }
    return { window.cancelAnimationFrame(frame) }
}

/**
 * The current step follows the reading line just below the fixed header.
 * Merely seeing a later heading near the bottom must not skip the text being read.
 * A narrow observer band detects crossings in either scrolling direction.
 * Scroll refreshes are coalesced to one per frame, including jumps between gaps
 * that do not change any observed intersection. They never scroll or move focus.
 */
fun observeArticleDerivation(
    surface: HTMLElement,
    motionActive: Boolean,
    modeChanged: (Boolean) -> Unit,
    stepChanged: (Int) -> Unit,
): () -> Unit {
    val browser = window.unsafeCast<ReadingObserverWindow>()
    val supported = jsTypeOf(browser.IntersectionObserver) == "function" &&
        jsTypeOf(browser.ResizeObserver) == "function"
    var observer: ReadingIntersectionObserver? = null
    var headerObserver: ReadingResizeObserver? = null
    var formulaObserver: ReadingResizeObserver? = null
    var frame = 0
    var readingFrame = 0
    var current = -1
    var disposed = false
    var refresh: (() -> Unit)? = null
    fun configure() {
        if (disposed) return
        observer?.disconnect()
        observer = null
        formulaObserver?.disconnect()
        formulaObserver = null
        refresh = null
        val rootTextSize = document.documentElement?.let { window.getComputedStyle(it).fontSize.removeSuffix("px").toDoubleOrNull() } ?: 16.0
        val enhanced = motionActive && supported && rootTextSize <= 22.0 &&
            window.innerWidth >= ArticleReadingLayout.MIN_WIDTH && window.innerHeight >= ArticleReadingLayout.MIN_HEIGHT
        modeChanged(enhanced)
        if (!enhanced) {
            surface.style.removeProperty(ArticleReadingProperty.FORMULA_HEIGHT.cssName)
            return
        }
        val top = readingTop()
        surface.style.setProperty(ArticleReadingProperty.TOP.cssName, "${top}px")
        document.findHtmlElement(ArticleReadingId.FORMULAS)?.let { board ->
            // The sticky containing block must reach below the full board even
            // when the last explanation is shorter. Its real size also follows
            // newly reached formulas, font rendering and responsive wrapping.
            // Only the final row grows; nothing above the reader moves or scrolls.
            fun reserveBoardHeight() {
                val height = board.getBoundingClientRect().height
                if (!disposed && height > 0.0) {
                    val value = "${height}px"
                    if (surface.style.getPropertyValue(ArticleReadingProperty.FORMULA_HEIGHT.cssName) != value) {
                        surface.style.setProperty(ArticleReadingProperty.FORMULA_HEIGHT.cssName, value)
                    }
                }
            }
            formulaObserver = ReadingResizeObserver { reserveBoardHeight() }.also { it.observe(board) }
            reserveBoardHeight()
        }
        val steps = (0..5).mapNotNull { index -> document.findHtmlElement(ArticleLineId(index))?.let { index to it } }
        fun update() {
            if (disposed || !isDocumentVisible()) return
            val bounds = surface.getBoundingClientRect()
            val viewportBottom = window.innerHeight.toDouble()
            if (bounds.top >= viewportBottom || bounds.bottom < top) return
            val explicit = explicitReadingPosition?.takeIf { abs(window.scrollY - it.scrollY) <= 1.0 }
            val index = explicit?.index ?: steps.lastOrNull { (_, element) ->
                element.getBoundingClientRect().top <= top + 1.0
            }?.first ?: steps.firstOrNull()?.first ?: return
            if (index != current) { current = index; stepChanged(index) }
        }
        refresh = ::update
        observer = ReadingIntersectionObserver({ update() }, json(
            "rootMargin" to "-${top}px 0px -${(window.innerHeight - top - 1.0).coerceAtLeast(0.0)}px 0px",
            "threshold" to 0,
        )).also { native -> steps.forEach { (_, element) -> native.observe(element) } }
    }
    fun schedule() {
        if (frame == 0 && !disposed) frame = window.requestAnimationFrame { frame = 0; configure() }
    }
    val resize: (Event) -> Unit = { schedule() }
    val releaseSelection: (Event) -> Unit = {
        val explicit = explicitReadingPosition
        if (explicit != null && abs(window.scrollY - explicit.scrollY) > 1.0) {
            explicitReadingPosition = null
        }
        if (refresh != null && readingFrame == 0 && !disposed) {
            readingFrame = window.requestAnimationFrame {
                readingFrame = 0
                refresh?.invoke()
            }
        }
    }
    window.addEventListener(BrowserEvent.RESIZE.eventName, resize)
    window.addEventListener(BrowserEvent.SCROLL.eventName, releaseSelection)
    if (jsTypeOf(browser.ResizeObserver) == "function") {
        headerObserver = ReadingResizeObserver { schedule() }.also { native ->
            document.findHtmlElement(ShellId.HEADER)?.let(native::observe)
        }
    }
    schedule()
    return {
        disposed = true
        explicitReadingPosition = null
        refresh = null
        observer?.disconnect()
        headerObserver?.disconnect()
        formulaObserver?.disconnect()
        window.cancelAnimationFrame(frame)
        window.cancelAnimationFrame(readingFrame)
        window.removeEventListener(BrowserEvent.RESIZE.eventName, resize)
        window.removeEventListener(BrowserEvent.SCROLL.eventName, releaseSelection)
    }
}
