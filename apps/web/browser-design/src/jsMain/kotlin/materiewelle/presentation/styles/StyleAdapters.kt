@file:OptIn(org.jetbrains.compose.web.internal.runtime.ComposeWebInternalApi::class)

package materiewelle.presentation.styles

import org.jetbrains.compose.web.css.*
import materiewelle.presentation.AppTypeface

enum class DesignMedia(private val feature: String, private val value: String) {
    PREFERS_REDUCED_MOTION_REDUCE("prefers-reduced-motion", "reduce"),
    PREFERS_REDUCED_MOTION_NO_PREFERENCE("prefers-reduced-motion", "no-preference"),
    PREFERS_CONTRAST_MORE("prefers-contrast", "more"),
    HOVER_NONE("hover", "none");

    val query: CSSMediaQuery
        get() = CSSMediaQuery.MediaFeature(feature, StylePropertyValue(value))
}

enum class EmbeddedFont(
    val typeface: AppTypeface, val path: String, val weight: Int, val italic: Boolean = false,
) {
    ROMAN(AppTypeface.TEXT, "fonts/lmroman10-regular.woff2", 400),
    ROMAN_BOLD(AppTypeface.TEXT, "fonts/lmroman10-bold.woff2", 700),
    ROMAN_ITALIC(AppTypeface.TEXT, "fonts/lmroman10-italic.woff2", 400, italic = true),
    ROMAN_BOLD_ITALIC(AppTypeface.TEXT, "fonts/lmroman10-bolditalic.woff2", 700, italic = true),
    MONO(AppTypeface.MONO, "fonts/lmmono10-regular.woff2", 400),
    MONO_ITALIC(AppTypeface.MONO, "fonts/lmmono10-italic.woff2", 400, italic = true),
}

private data class FontFaceRule(override val style: StyleHolder) :
    CSSRuleDeclaration, CSSStyledRuleDeclaration {
    override val header = "@font-face"
}

internal fun StyleRules.fontFace(font: EmbeddedFont) {
    add(
        FontFaceRule(
            CSSRuleBuilderImpl().apply {
                fontFamily(font.typeface.family)
                property("src", "url('${font.path}') format('woff2')")
                property("font-weight", font.weight)
                property("font-style", if (font.italic) "italic" else "normal")
                property("font-display", "swap")
            }
        )
    )
}

internal fun StyleRules.frames(name: MotionKeyframe, block: CSSKeyframesBuilder.() -> Unit) {
    add(CSSKeyframesRuleDeclaration(name.name, CSSKeyframesBuilder(block).frames))
}

/** Apply priority without discarding the declaration's original Kotlin value type. */
internal fun StyleScope.important(block: StyleScope.() -> Unit) {
    val scope =
        object : StyleScope {
            override fun property(propertyName: String, value: StylePropertyValue) {
                this@important.property(propertyName, "$value!important")
            }

            override fun variable(variableName: String, value: StylePropertyValue) {
                this@important.variable(variableName, "$value!important")
            }
        }
    scope.block()
}

internal fun StyleScope.background(color: CSSColorValue) {
    property(CssProperty.BACKGROUND.key, color)
}

internal fun StyleScope.borderColor(value: CSSColorValue) {
    property(CssProperty.BORDER_COLOR.key, value)
}

internal fun StyleScope.outlineOffset(value: CSSNumeric) {
    property(CssProperty.OUTLINE_OFFSET.key, value)
}

internal fun StyleScope.zIndex(value: Int) {
    property(CssProperty.Z_INDEX.key, value)
}

internal fun StyleScope.edge(
    side: CssProperty,
    width: CSSNumeric,
    style: LineStyle,
    color: CSSColorValue,
) {
    require(
        side in
            setOf(
                CssProperty.BORDER_TOP,
                CssProperty.BORDER_LEFT,
                CssProperty.BORDER_BOTTOM,
                CssProperty.OUTLINE,
            )
    )
    property(
        side.key,
        CSSBorder().apply {
            width(width)
            style(style)
            color(color)
        },
    )
}

/**
 * Compose HTML 1.8's DOM applier writes StyleHolder.variables without their `--` prefix (its
 * stylesheet serializer adds it). Use the typed property path for inline tokens so both routes
 * address the same actual browser custom property.
 */
fun <T : StylePropertyValue> StyleScope.setToken(token: DesignToken<T>, value: T) {
    property("--${token.name}", value)
}

fun StyleScope.setToken(token: DesignToken<StylePropertyNumber>, value: Number) {
    property("--${token.name}", value)
}

fun StyleScope.setToken(token: DesignToken<StylePropertyString>, value: String) {
    property("--${token.name}", value)
}
