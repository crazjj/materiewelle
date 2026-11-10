package materiewelle.presentation.styles

import org.jetbrains.compose.web.css.*

enum class FontStack(val css: String) {
    TEXT("var(--font-text)"),
    MONO("var(--font-mono)"),
    SERIF("var(--font-text)"),
}

data class FontSpec(
    val size: CSSNumeric,
    val family: FontStack,
    val weight: Int? = null,
    val lineHeight: Double? = null,
) {
    init {
        require(weight == null || weight in 1..1000)
        require(lineHeight == null || lineHeight.isFinite() && lineHeight >= 0)
    }

    internal fun css() =
        "${weight?.let { "$it " } ?: ""}$size${lineHeight?.let { "/$it" } ?: ""} ${family.css}"
}

/** Keep font shorthand reset semantics while validating weight, line height and units in Kotlin. */
internal fun StyleScope.font(value: FontSpec) {
    property(CssProperty.FONT.key, value.css())
}

internal fun StyleScope.fontFamily(value: FontStack) {
    property(CssProperty.FONT_FAMILY.key, value.css)
}

internal fun boundedSize(
    minimum: CSSNumeric,
    preferred: CSSNumeric,
    maximum: CSSNumeric,
): CSSNumeric = CSSVariableValue("clamp($minimum,$preferred,$maximum)")

sealed interface GridTrack {
    val css: String

    data class Fixed(val size: CSSNumeric) : GridTrack {
        override val css
            get() = size.toString()
    }

    data class Fraction(val value: Number) : GridTrack {
        init {
            require(value.toDouble().isFinite() && value.toDouble() > 0)
        }

        override val css
            get() = "${value}fr"
    }

    data object Auto : GridTrack {
        override val css = "auto"
    }

    data class MinMax(val minimum: GridTrack, val maximum: GridTrack) : GridTrack {
        override val css
            get() = "minmax(${minimum.css},${maximum.css})"
    }

    data class Repeat(val count: Int, val track: GridTrack) : GridTrack {
        init {
            require(count > 0)
        }

        override val css
            get() = "repeat($count,${track.css})"
    }

    data class AutoFit(val track: GridTrack) : GridTrack {
        override val css
            get() = "repeat(auto-fit,${track.css})"
    }
}

internal fun StyleScope.columns(vararg tracks: GridTrack) {
    require(tracks.isNotEmpty())
    property(CssProperty.GRID_TEMPLATE_COLUMNS.key, tracks.joinToString(" ") { it.css })
}

/** Dynamic viewport units are not exposed in Compose HTML 1.8's numeric unit set. */
fun viewportHeightMinus(inset: CSSNumeric): CSSNumeric = CSSVariableValue("calc(100dvh - $inset)")
