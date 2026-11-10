package materiewelle.ui.semantics

import androidx.compose.runtime.Composable
import materiewelle.presentation.styles.*
import materiewelle.ui.TypesetGlyph
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.css.*
import org.w3c.dom.svg.SVGElement

fun AttrsScope<*>.typesetInline(
    width: Double,
    height: Double,
    baseline: Double,
    source: String,
    number: Boolean = false,
) {
    classes(AdditionalStyleClass.TYPST_INLINE.cssName)
    style {
        setToken(Tokens.MATH_WIDTH, width.em)
        setToken(Tokens.MATH_HEIGHT, height.em)
        setToken(Tokens.MATH_DESCENT, (height - baseline).em)
    }
    attr("role", "math")
    attr("aria-label", source)
    attr("data-typst-source", source)
    attr("data-typst-kind", if (number) "number" else "expression")
}

fun AttrsScope<*>.typesetReading() {
    classes(AdditionalStyleClass.TYPST_READING.cssName)
}

fun AttrsScope<*>.typesetAsset(dark: Boolean) {
    classes(
        AdditionalStyleClass.TYPST_ASSET.cssName,
        if (dark) AdditionalStyleClass.TYPST_DARK.cssName
        else AdditionalStyleClass.TYPST_LIGHT.cssName,
    )
}

private const val MATH_SVG_NS = "http://www.w3.org/2000/svg"

@Composable
fun TypstNumberDefinitions(glyphs: List<TypesetGlyph>) {
    SvgTag(
        SvgTagName.SVG,
        attrs = {
            classes(AdditionalStyleClass.TYPST_NUMBER_DEFINITIONS.cssName)
            ariaHidden(true)
            attr("width", "0")
            attr("height", "0")
            ref { element ->
                val definitions = svgElement("defs")
                glyphs.forEach { glyph ->
                    val group = svgElement("g", "id" to glyph.id)
                    // Only validated, generated Typst glyph data crosses the XML import boundary.
                    val parsed =
                        org.w3c.dom.parsing
                            .DOMParser()
                            .parseFromString(
                                "<svg xmlns=\"$MATH_SVG_NS\">${glyph.body}</svg>",
                                "image/svg+xml",
                            )
                    val source = parsed.documentElement ?: error("Fehlende Typst-Glyphe")
                    for (n in 0 until source.childNodes.length) {
                        group.appendChild(
                            kotlinx.browser.document.importNode(source.childNodes.item(n)!!, true)
                        )
                    }
                    definitions.appendChild(group)
                }
                element.appendChild(definitions)
                onDispose {}
            }
        },
    )
}

@Composable
fun TypstNumberSvg(glyphs: List<TypesetGlyph>, width: Double, height: Double, baseline: Double) {
    SvgTag(
        SvgTagName.SVG,
        attrs = {
            attr("viewBox", "0 0 ${width*16} ${height*16}")
            classes(AdditionalStyleClass.TYPST_NUMBER.cssName)
            ariaHidden(true)
        },
    ) {
        var x = 0.0
        glyphs.forEach { glyph ->
            val offset = x
            x += glyph.width
            SvgTag(
                SvgTagName.USE,
                attrs = {
                    attr("href", "#${glyph.id}")
                    attr("x", (offset * 16).toString())
                    attr("y", ((baseline - glyph.baseline) * 16).toString())
                },
            )
        }
    }
}

/** Preserve typed drawing attributes; paint math directly as Typst paths/images. */
@Composable
fun TypesetSvgLabel(
    namespace: String,
    x: Number,
    y: Number,
    attrs: AttrsScope<SVGElement>.() -> Unit,
    text: String,
    inheritColor: Boolean = false,
) {
    androidx.compose.runtime.key(text, x.toDouble(), y.toDouble()) {
        SvgTag(
            SvgTagName.GROUP,
            attrs = {
                attrs()
                attr("data-typst-label", "true")
                attr("aria-label", text)
                attr("role", "img")
                ref { element ->
                    var stopObserving: () -> Unit = {}
                    fun renderLabel() {
                        stopObserving()
                        while (element.firstChild != null) element.removeChild(element.firstChild!!)
                        val anchor = element.getAttribute("text-anchor") ?: "start"
                        val style = kotlinx.browser.window.getComputedStyle(element)
                        val size = style.fontSize.removeSuffix("px").toDoubleOrNull() ?: 16.0
                        // Optics labels follow CSS on theme/stage changes instead of capturing an RGB value.
                        val color = if (inheritColor) "inherit" else style.getPropertyValue("fill").ifBlank { "var(--text)" }
                        val body = typstSvgLabel(text, size, anchor, style.fontFamily)
                        element.appendChild(
                            svgElement(
                                    "g",
                                    "transform" to "translate($x $y)",
                                    "style" to "color:$color",
                                )
                                .apply { appendChild(body) }
                        )
                        layoutTypesetLabels(element)
                        stopObserving = observeTypesetLabels(element)
                    }
                    renderLabel()
                    // The first render may use fallback metrics. Re-measure once when local fonts settle.
                    val cancelFontLayout = afterFontMetricsReady(::renderLabel)
                    onDispose { cancelFontLayout(); stopObserving() }
                }
            },
        )
    }
}

/** Reject active/external markup before the static, typed numeric atlas enters the DOM. */
fun validateTypesetGlyphs(glyphs: List<TypesetGlyph>) {
    val tags = setOf("g", "defs", "symbol", "use", "path", "rect", "clipPath")
    val allowedAttributes =
        setOf(
            "id",
            "class",
            "overflow",
            "transform",
            "d",
            "fill",
            "fill-rule",
            "stroke",
            "stroke-width",
            "stroke-linecap",
            "stroke-linejoin",
            "stroke-miterlimit",
            "x",
            "y",
            "width",
            "height",
            "viewBox",
            "href",
            "xmlns",
            "xmlns:xlink",
            "xlink:href",
        )
    glyphs.forEach { glyph ->
        // Parse into an inert XML document, never into the live page for validation.
        val parser = org.w3c.dom.parsing.DOMParser()
        val parsed =
            parser.parseFromString(
                "<svg xmlns=\"$MATH_SVG_NS\">${glyph.body}</svg>",
                "image/svg+xml",
            )
        require(parsed.getElementsByTagName("parsererror").length == 0)
        val nodes = parsed.getElementsByTagName("*")
        for (n in 1 until nodes.length) {
            val element = nodes.item(n) ?: error("Fehlendes Glyphenelement")
            require(element.localName in tags)
            for (a in 0 until element.attributes.length) {
                val attribute = element.attributes.item(a) ?: error("Fehlendes Glyphenattribut")
                require(attribute.name in allowedAttributes)
                if (attribute.name.endsWith("href"))
                    require(attribute.value.startsWith("#${glyph.id}-"))
                if (attribute.name == "id") require(attribute.value.startsWith(glyph.id + "-"))
            }
        }
    }
}
