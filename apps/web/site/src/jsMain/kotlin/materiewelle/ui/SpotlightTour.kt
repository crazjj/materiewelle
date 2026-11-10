package materiewelle.ui

import androidx.compose.runtime.*
import kotlinx.browser.document
import kotlinx.browser.window
import materiewelle.NativeSvg
import materiewelle.presentation.styles.*
import materiewelle.ui.MathText as Text
import materiewelle.ui.semantics.*
import materiewelle.ui.vector.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.css.keywords.auto
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent

data class TourStep(
    val target: ElementId,
    val title: String,
    val explanation: String,
    val enter: () -> Unit = {},
)

data class TourController(val start: (List<TourStep>) -> Unit, val close: () -> Unit)

val LocalTour = staticCompositionLocalOf { TourController({}, {}) }

private data class SpotlightRect(val x: Double, val y: Double, val w: Double, val h: Double)

/** Identity-based adapter, independent of any experiment or page. Targets remain interactive. */
@Composable
fun TourProvider(content: @Composable () -> Unit) {
    var steps by remember { mutableStateOf<List<TourStep>>(emptyList()) }
    var index by remember { mutableStateOf(0) }
    var previousFocus by remember { mutableStateOf<HTMLElement?>(null) }
    var rect by remember { mutableStateOf<SpotlightRect?>(null) }
    var viewport by remember {
        mutableStateOf(window.innerWidth.toDouble() to window.innerHeight.toDouble())
    }
    var cardHeight by remember { mutableStateOf(280.0) }
    var targetMissing by remember { mutableStateOf(false) }
    fun close() {
        if (steps.isNotEmpty()) {
            steps = emptyList()
            previousFocus?.focus()
        }
    }
    CompositionLocalProvider(
        LocalTour provides
            TourController(
                { next ->
                    previousFocus = document.activeElement as? HTMLElement
                    viewport = window.innerWidth.toDouble() to window.innerHeight.toDouble()
                    rect = null
                    targetMissing = false
                    index = 0
                    steps = next
                },
                ::close,
            )
    ) {
        content()
    }
    val step = steps.getOrNull(index) ?: return
    val activeClose by rememberUpdatedState({ close() })
    DisposableEffect(step) {
        step.enter()
        var target = document.findHtmlElement(step.target)
        var wasVisible = false
        target?.scrollIntoViewCentered()
        fun measure() {
            viewport = window.innerWidth.toDouble() to window.innerHeight.toDouble()
            // An enter callback can mount or replace a target on a later Compose
            // frame. Do not retain a missing/stale DOM node for the whole step.
            val resolved = document.findHtmlElement(step.target)
            val replaced = resolved !== target
            target = resolved
            document.findHtmlElement(Id.SPOTLIGHT_DIALOG)?.getBoundingClientRect()?.let {
                cardHeight = it.height
            }
            var box = target?.getBoundingClientRect()
            targetMissing = box == null || box.width <= 0 || box.height <= 0
            if (!targetMissing && (!wasVisible || replaced)) {
                target?.scrollIntoViewCentered()
                box = target?.getBoundingClientRect()
            }
            wasVisible = !targetMissing
            rect =
                box?.takeIf { it.width > 0 && it.height > 0 }
                    ?.let {
                        val x = (it.left - 6).coerceIn(0.0, window.innerWidth.toDouble())
                        val y = (it.top - 6).coerceIn(0.0, window.innerHeight.toDouble())
                        SpotlightRect(
                            x,
                            y,
                            (it.right + 6 - x).coerceIn(0.0, window.innerWidth - x),
                            (it.bottom + 6 - y).coerceIn(0.0, window.innerHeight - y),
                        )
                    }
        }
        measure()
        val timer = window.setInterval({ measure() }, 120)
        val layoutListener: (Event) -> Unit = { measure() }
        window.addEventListener(BrowserEvent.RESIZE.eventName, layoutListener)
        window.addEventListener(BrowserEvent.SCROLL.eventName, layoutListener)
        val focusTimer =
            window.setTimeout(
                {
                    measure()
                    target?.scrollIntoViewCentered()
                    measure()
                    document.findHtmlElement(Id.TOUR_NEXT)?.focus()
                },
                40,
            )
        val listener: (Event) -> Unit = { event ->
            if (event is KeyboardEvent) {
                val key = LabKey.fromBrowser(event.key)
                if (key == LabKey.ESCAPE) {
                    event.preventDefault()
                    activeClose()
                }
                if (key == LabKey.TAB) {
                    target = document.findHtmlElement(step.target)
                    val focusable = DomQuery.FOCUSABLE.selector
                    val candidates = mutableListOf<HTMLElement>()
                    target?.takeIf { it.matches(focusable) }?.let { candidates += it }
                    target?.querySelectorAll(focusable)?.let { children ->
                        for (n in 0 until children.length) (children.item(n) as? HTMLElement)?.let {
                            candidates += it
                        }
                    }
                    document
                        .findElement(Id.SPOTLIGHT_DIALOG)
                        ?.querySelectorAll(DomQuery.BUTTON.selector)
                        ?.let { buttons ->
                            for (n in 0 until buttons.length) (buttons.item(n) as? HTMLElement)
                                ?.let { candidates += it }
                        }
                    val controls =
                        candidates.filter {
                            it.getBoundingClientRect().height > 0 && !it.hasAttribute("disabled")
                        }
                    if (controls.isNotEmpty()) {
                        val current = controls.indexOf(document.activeElement)
                        val next =
                            if (event.shiftKey)
                                (if (current <= 0) controls.lastIndex else current - 1)
                            else (current + 1) % controls.size
                        event.preventDefault()
                        controls[next].focus()
                    }
                }
            }
        }
        document.addEventListener(BrowserEvent.KEY_DOWN.eventName, listener)
        onDispose {
            window.clearInterval(timer)
            window.clearTimeout(focusTimer)
            document.removeEventListener(BrowserEvent.KEY_DOWN.eventName, listener)
            window.removeEventListener(BrowserEvent.RESIZE.eventName, layoutListener)
            window.removeEventListener(BrowserEvent.SCROLL.eventName, layoutListener)
        }
    }
    val (width, height) = viewport
    val r = rect ?: SpotlightRect(0.0, 0.0, 0.0, 0.0)
    @Composable
    fun shade(x: Double, y: Double, w: Double, h: Double) {
        Div(
            attrs = {
                styles(LabClass.TOUR_SHADE)
                style {
                    left(x.px)
                    top(y.px)
                    width(w.px)
                    height(h.px)
                }
                ariaHidden(true)
            }
        )
    }
    shade(0.0, 0.0, width, r.y)
    shade(0.0, r.y, r.x, r.h)
    shade(r.x + r.w, r.y, width - r.x - r.w, r.h)
    shade(0.0, r.y + r.h, width, height - r.y - r.h)
    Div(
        attrs = {
            styles(LabClass.TOUR_RING)
            style {
                left(r.x.px)
                top(r.y.px)
                width(r.w.px)
                height(r.h.px)
            }
            ariaHidden(true)
        }
    )
    val below = r.y + r.h + 16
    val above = r.y - cardHeight - 16
    val top =
        when {
            below + cardHeight <= height - 12 -> below
            above >= 12 -> above
            else -> (height - cardHeight - 12).coerceAtLeast(12.0)
        }
    val cardWidth = 380.0.coerceAtMost(width - 24)
    val beside = below + cardHeight > height - 12 && above < 12 && r.x > cardWidth + 24
    // Use this render's placement, not the card rectangle from the previous
    // measurement. Target changes can otherwise leave a stale arrow for a frame.
    val card =
        SpotlightRect(
            if (beside) 12.0 else width - 12 - cardWidth,
            top,
            cardWidth,
            minOf(cardHeight, height - top - 12).coerceAtLeast(0.0),
        )
    connectorSvg(width, height, r, card)?.let { markup ->
        Div(
            attrs = {
                elementId(Id.TOUR_CONNECTOR)
                styles(LabClass.TOUR_CONNECTOR)
                ariaHidden(true)
            }
        ) {
            NativeSvg(markup)
        }
    }
    Div(
        attrs = {
            elementId(Id.SPOTLIGHT_DIALOG)
            styles(LabClass.TOUR_DIALOG)
            role(AriaRole.DIALOG)
            ariaModal(false)
            labelledBy(Id.TOUR_TITLE)
            describedBy(Id.TOUR_DESCRIPTION)
            data(LabData.TOUR_STEP, (index + 1))
            style {
                top(top.px)
                maxHeight(viewportHeightMinus((top + 12).px))
                if (beside) {
                    left(12.px)
                    right(auto)
                } else right(12.px)
            }
        }
    ) {
        P(attrs = { styles(LabClass.EYEBROW) }) {
            Text("LERNSPUR / ${index + 1} VON ${steps.size}")
        }
        H2(attrs = { elementId(Id.TOUR_TITLE) }) { Text(step.title) }
        P(attrs = { elementId(Id.TOUR_DESCRIPTION) }) { Text(step.explanation) }
        if (targetMissing)
            P(attrs = { role(AriaRole.STATUS) }) {
                Text("Das Ziel ist gerade nicht sichtbar. Du kannst die Tour fortsetzen.")
            }
        Div(attrs = { styles(LabClass.WORKSHOP_ACTIONS) }) {
            Button(
                attrs = {
                    elementId(Id.TOUR_BACK)
                    styles(LabClass.SECONDARY)
                    if (index == 0) disabled()
                    onClick { index = (index - 1).coerceAtLeast(0) }
                }
            ) {
                Text("Zurück")
            }
            Button(
                attrs = {
                    elementId(Id.TOUR_NEXT)
                    styles(LabClass.PRIMARY)
                    onClick { if (index == steps.lastIndex) close() else index++ }
                }
            ) {
                Text(if (index == steps.lastIndex) "Fertig" else "Weiter")
            }
            Button(
                attrs = {
                    elementId(Id.TOUR_CLOSE)
                    styles(LabClass.SECONDARY)
                    onClick { close() }
                }
            ) {
                Text("Beenden")
            }
        }
        P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
            Text("Das markierte Element bleibt bedienbar. Esc beendet die Tour.")
        }
    }
}

/** A noninteractive arrow from the actual card edge to the actual target edge. */
private fun connectorSvg(
    width: Double,
    height: Double,
    target: SpotlightRect,
    card: SpotlightRect,
): VectorFragment? {
    if (target.w <= 0 || target.h <= 0 || card.w <= 0 || card.h <= 0) return null
    if (
        card.x < target.x + target.w &&
            card.x + card.w > target.x &&
            card.y < target.y + target.h &&
            card.y + card.h > target.y
    )
        return null
    val tx = target.x + target.w / 2
    val ty = target.y + target.h / 2
    val cx = card.x + card.w / 2
    val cy = card.y + card.h / 2
    val dx = tx - cx
    val dy = ty - cy
    fun edgeScale(rect: SpotlightRect): Double =
        minOf(
            if (dx == 0.0) Double.POSITIVE_INFINITY else rect.w / (2 * kotlin.math.abs(dx)),
            if (dy == 0.0) Double.POSITIVE_INFINITY else rect.h / (2 * kotlin.math.abs(dy)),
        )
    val start = edgeScale(card)
    val end = edgeScale(target)
    val sx = cx + dx * start
    val sy = cy + dy * start
    val ex = tx - dx * end
    val ey = ty - dy * end
    val length = kotlin.math.sqrt((ex - sx) * (ex - sx) + (ey - sy) * (ey - sy))
    if (length < 12) return null
    val ux = (ex - sx) / length
    val uy = (ey - sy) / length
    return vector {
        svg({
            viewBox(0, 0, width, height)
            ariaHidden()
        }) {
            path({
                geometry("M$sx $sy L${ex - ux * 7} ${ey - uy * 7}")
                fill(VectorPaint.None)
                stroke(VectorPaint.FOCUS)
                strokeWidth(2.5)
            })
            path({
                geometry(
                    "M$ex $ey L${ex - ux * 11 - uy * 5} ${ey - uy * 11 + ux * 5} L${ex - ux * 11 + uy * 5} ${ey - uy * 11 - ux * 5} Z"
                )
                fill(VectorPaint.FOCUS)
            })
        }
    }
}
