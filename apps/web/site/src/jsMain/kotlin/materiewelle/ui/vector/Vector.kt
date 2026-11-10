package materiewelle.ui.vector

import materiewelle.presentation.styles.Tokens
import materiewelle.presentation.AppTypeface
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.css.value

/** A vector is data, not an XML template. Browser spellings stay at the renderer boundary. */
class VectorFragment internal constructor(internal val nodes: List<VectorNode>) {
    // Stable value identity prevents an unrelated Compose update from rebuilding an unchanged SVG.
    override fun equals(other: Any?): Boolean = other is VectorFragment && nodes == other.nodes

    override fun hashCode(): Int = nodes.hashCode()
}

internal data class VectorNode(
    val tag: VectorTag,
    val attributes: Map<VectorAttribute, String>,
    val children: List<VectorNode> = emptyList(),
    val text: String? = null,
)

internal enum class VectorTag(val xml: String) {
    SVG("svg"),
    GROUP("g"),
    PATH("path"),
    CIRCLE("circle"),
    LINE("line"),
    RECT("rect"),
    POLYGON("polygon"),
    DEFINITIONS("defs"),
    CLIP_PATH("clipPath"),
    TITLE("title"),
    LABEL("text"),
}

internal enum class VectorAttribute(val xml: String) {
    X("x"),
    Y("y"),
    X1("x1"),
    X2("x2"),
    Y1("y1"),
    Y2("y2"),
    WIDTH("width"),
    HEIGHT("height"),
    CX("cx"),
    CY("cy"),
    RADIUS("r"),
    CORNER_RADIUS("rx"),
    FONT_SIZE("font-size"),
    OPACITY("opacity"),
    FILL_OPACITY("fill-opacity"),
    STROKE_WIDTH("stroke-width"),
    STROKE_OPACITY("stroke-opacity"),
    FILL("fill"),
    STROKE("stroke"),
    VIEW_BOX("viewBox"),
    CLASS("class"),
    ID("id"),
    CLIP("clip-path"),
    ROLE("role"),
    ARIA_HIDDEN("aria-hidden"),
    ARIA_LABEL("aria-label"),
    GEOMETRY("d"),
    PATH_LENGTH("pathLength"),
    POINTS("points"),
    ANCHOR("text-anchor"),
    FONT("font-family"),
    DASH("stroke-dasharray"),
    LINE_CAP("stroke-linecap"),
    SETUP_PART("data-setup-part"),
}

class VectorPaint private constructor(internal val css: String) {
    companion object {
        val None = VectorPaint("none")
        val BLACK = VectorPaint("black")
        val WHITE = VectorPaint("white")
        val TRANSPARENT = VectorPaint("transparent")
        val CURRENTCOLOR = VectorPaint("currentColor")
        val TEXT = VectorPaint(Tokens.TEXT.value().toString())
        val MUTED = VectorPaint(Tokens.MUTED.value().toString())
        val ACCENT = VectorPaint(Tokens.ACCENT.value().toString())
        val ACCENT_SOFT = VectorPaint(Tokens.ACCENT_SOFT.value().toString())
        val REFERENCE = VectorPaint(Tokens.REFERENCE.value().toString())
        val SURFACE = VectorPaint(Tokens.SURFACE.value().toString())
        val SURFACE_RAISED = VectorPaint(Tokens.SURFACE_RAISED.value().toString())
        val BORDER = VectorPaint(Tokens.BORDER.value().toString())
        val GRID = VectorPaint(Tokens.GRID.value().toString())
        val FOCUS = VectorPaint(Tokens.FOCUS.value().toString())

        fun rgb(red: Int, green: Int, blue: Int): VectorPaint {
            require(listOf(red, green, blue).all { it in 0..255 })
            return VectorPaint("rgb($red,$green,$blue)")
        }
    }
}

enum class VectorFont(internal val css: String, internal val typeface: AppTypeface) {
    MONO("var(--font-mono)", AppTypeface.MONO),
    TEXT("var(--font-text)", AppTypeface.TEXT),
}

enum class VectorLineCap(internal val css: String) {
    BUTT("butt"),
    ROUND("round"),
    SQUARE("square"),
}

@DslMarker annotation class VectorDsl

@VectorDsl
class VectorAttributes internal constructor() {
    internal val values = linkedMapOf<VectorAttribute, String>()

    private fun number(key: VectorAttribute, value: Number) {
        require(value.toDouble().isFinite()) { "Vector coordinates must be finite" }
        values[key] = value.toString()
    }

    fun atX(value: Number) = number(VectorAttribute.X, value)

    fun atY(value: Number) = number(VectorAttribute.Y, value)

    fun x1(value: Number) = number(VectorAttribute.X1, value)

    fun x2(value: Number) = number(VectorAttribute.X2, value)

    fun y1(value: Number) = number(VectorAttribute.Y1, value)

    fun y2(value: Number) = number(VectorAttribute.Y2, value)

    fun width(value: Number) = number(VectorAttribute.WIDTH, value)

    fun height(value: Number) = number(VectorAttribute.HEIGHT, value)

    fun cx(value: Number) = number(VectorAttribute.CX, value)

    fun cy(value: Number) = number(VectorAttribute.CY, value)

    fun radius(value: Number) = number(VectorAttribute.RADIUS, value)

    fun cornerRadius(value: Number) = number(VectorAttribute.CORNER_RADIUS, value)

    fun fontSize(value: Number) = number(VectorAttribute.FONT_SIZE, value)

    fun opacity(value: Number) = number(VectorAttribute.OPACITY, value)

    fun fillOpacity(value: Number) = number(VectorAttribute.FILL_OPACITY, value)

    fun strokeWidth(value: Number) = number(VectorAttribute.STROKE_WIDTH, value)

    fun strokeOpacity(value: Number) = number(VectorAttribute.STROKE_OPACITY, value)

    fun pathLength(value: Number) {
        require(value.toDouble() > 0)
        number(VectorAttribute.PATH_LENGTH, value)
    }

    fun fill(value: VectorPaint) {
        values[VectorAttribute.FILL] = value.css
    }

    fun stroke(value: VectorPaint) {
        values[VectorAttribute.STROKE] = value.css
    }

    fun viewBox(x: Number, y: Number, width: Number, height: Number) {
        require(
            listOf(x, y, width, height).all { it.toDouble().isFinite() } &&
                width.toDouble() > 0 &&
                height.toDouble() > 0
        )
        values[VectorAttribute.VIEW_BOX] = "$x $y $width $height"
    }

    fun styles(vararg styles: StyleClass) {
        values[VectorAttribute.CLASS] = styles.joinToString(" ") { it.cssName }
    }

    fun elementId(id: ElementId) {
        values[VectorAttribute.ID] = id.value
    }

    fun clip(id: ElementId) {
        values[VectorAttribute.CLIP] = id.svgReference
    }

    fun imageRole() {
        values[VectorAttribute.ROLE] = "img"
    }

    fun ariaHidden() {
        values[VectorAttribute.ARIA_HIDDEN] = "true"
    }

    fun ariaLabel(value: String) {
        values[VectorAttribute.ARIA_LABEL] = value
    }

    fun geometry(path: String) {
        require(path.none { it == '<' || it == '>' })
        values[VectorAttribute.GEOMETRY] = path
    }

    fun points(points: String) {
        require(points.none { it == '<' || it == '>' })
        values[VectorAttribute.POINTS] = points
    }

    fun anchor(value: TextAnchor) {
        values[VectorAttribute.ANCHOR] = value.attributeValue
    }

    fun font(value: VectorFont) {
        values[VectorAttribute.FONT] = value.css
    }

    fun dash(vararg lengths: Number) {
        require(
            lengths.isNotEmpty() && lengths.all { it.toDouble().isFinite() && it.toDouble() >= 0 }
        )
        values[VectorAttribute.DASH] = lengths.joinToString(" ")
    }

    fun lineCap(value: VectorLineCap) {
        values[VectorAttribute.LINE_CAP] = value.css
    }

    fun setupPart(value: SetupPart) {
        values[VectorAttribute.SETUP_PART] = value.key
    }
}

@VectorDsl
class VectorBuilder internal constructor() {
    private val nodes = mutableListOf<VectorNode>()

    private fun node(
        tag: VectorTag,
        attributes: VectorAttributes.() -> Unit,
        content: VectorBuilder.() -> Unit = {},
        text: String? = null,
    ) {
        nodes +=
            VectorNode(
                tag,
                VectorAttributes().apply(attributes).values.toMap(),
                VectorBuilder().apply(content).nodes.toList(),
                text,
            )
    }

    fun svg(attributes: VectorAttributes.() -> Unit, content: VectorBuilder.() -> Unit) =
        node(VectorTag.SVG, attributes, content)

    fun group(attributes: VectorAttributes.() -> Unit = {}, content: VectorBuilder.() -> Unit) =
        node(VectorTag.GROUP, attributes, content)

    fun path(attributes: VectorAttributes.() -> Unit) = node(VectorTag.PATH, attributes)

    fun circle(attributes: VectorAttributes.() -> Unit) = node(VectorTag.CIRCLE, attributes)

    fun line(attributes: VectorAttributes.() -> Unit) = node(VectorTag.LINE, attributes)

    fun rect(attributes: VectorAttributes.() -> Unit) = node(VectorTag.RECT, attributes)

    fun polygon(attributes: VectorAttributes.() -> Unit) = node(VectorTag.POLYGON, attributes)

    fun definitions(
        attributes: VectorAttributes.() -> Unit = {},
        content: VectorBuilder.() -> Unit,
    ) = node(VectorTag.DEFINITIONS, attributes, content)

    fun clipPath(attributes: VectorAttributes.() -> Unit, content: VectorBuilder.() -> Unit) =
        node(VectorTag.CLIP_PATH, attributes, content)

    fun title(value: String, attributes: VectorAttributes.() -> Unit = {}) =
        node(VectorTag.TITLE, attributes, text = value)

    fun label(value: String, attributes: VectorAttributes.() -> Unit = {}) =
        node(VectorTag.LABEL, attributes, text = value)

    fun label(value: Number, attributes: VectorAttributes.() -> Unit = {}) =
        label(value.toString(), attributes)

    fun include(fragment: VectorFragment) {
        nodes += fragment.nodes
    }

    internal fun build() = VectorFragment(nodes.toList())
}

fun vector(block: VectorBuilder.() -> Unit): VectorFragment = VectorBuilder().apply(block).build()
