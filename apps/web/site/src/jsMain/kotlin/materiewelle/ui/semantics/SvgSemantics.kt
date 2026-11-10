package materiewelle.ui.semantics

import org.jetbrains.compose.web.attributes.AttrsScope

/** Small adapter for Compose HTML's String-only SVG API. Geometry remains numeric. */
enum class SvgNumber(val attribute: String) {
    X("x"), Y("y"), X1("x1"), X2("x2"), Y1("y1"), Y2("y2"),
    WIDTH("width"), HEIGHT("height"), CX("cx"), CY("cy"), RADIUS("r")
}
enum class TextAnchor(val attributeValue: String) { START("start"), MIDDLE("middle"), END("end") }

fun AttrsScope<*>.svgNumber(key: SvgNumber, value: Number) {
    require(value.toDouble().isFinite()); attr(key.attribute, value.toString())
}
fun AttrsScope<*>.viewBox(x: Number, y: Number, width: Number, height: Number) {
    require(listOf(x, y, width, height).all { it.toDouble().isFinite() })
    require(width.toDouble() > 0 && height.toDouble() > 0)
    attr("viewBox", "$x $y $width $height")
}
fun AttrsScope<*>.svgPath(path: String) { attr("d", path) }
fun AttrsScope<*>.svgHref(path: String) { attr("href", path) }
fun AttrsScope<*>.textAnchor(anchor: TextAnchor) { attr("text-anchor", anchor.attributeValue) }
