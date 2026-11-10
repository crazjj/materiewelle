package materiewelle.ui.semantics

import materiewelle.core.ObservationPosition
import org.jetbrains.compose.web.attributes.AttrsScope
import org.w3c.dom.HTMLElement

fun AttrsScope<*>.slitLabStyles(vararg values: SlitLabClass) = classes(*values.map { it.cssName }.toTypedArray())

/** The source SVG's viewBox, rather than the scrolled parent, defines the coordinate system. */
fun slitPositionAtPointer(container: HTMLElement, clientX: Double, clientY: Double): ObservationPosition? {
    val svg = container.querySelector("svg") ?: return null
    val box = svg.getBoundingClientRect()
    if (box.width <= 0.0 || box.height <= 0.0) return null
    val height = if (svg.getAttribute("viewBox")?.endsWith("440") == true) 440.0 else 385.0
    val x = (clientX - box.left) / box.width * 640.0
    val y = (clientY - box.top) / box.height * height
    if (x !in 62.0..612.0 || (y !in 45.0..235.0 && y !in 321.0..351.0)) return null
    return ObservationPosition(((x - 62.0) / 550.0 * .006 - .003).coerceIn(-.003, .003))
}
