package materiewelle.presentation.styles

import org.jetbrains.compose.web.css.*

/** Invariance prevents Kotlin from widening a color token and a length to a common value type. */
class DesignToken<T : StylePropertyValue> internal constructor(val name: String) {
    private val reference = CSSStyleVariable<T>(name)

    fun value(): T = reference.value()
}

/** Colors, sizes, fonts and motion variables have distinct Kotlin value types. */
object Tokens {
    val ACCENT = DesignToken<CSSColorValue>("accent")
    val ACCENT_INK = DesignToken<CSSColorValue>("accent-ink")
    val ACCENT_SOFT = DesignToken<CSSColorValue>("accent-soft")
    val BG = DesignToken<CSSColorValue>("bg")
    val BORDER = DesignToken<CSSColorValue>("border")
    val CONTROL_HEIGHT = DesignToken<CSSNumeric>("control-height")
    val CONTROL_RADIUS = DesignToken<CSSNumeric>("control-radius")
    val DANGER = DesignToken<CSSColorValue>("danger")
    val DANGER_SOFT = DesignToken<CSSColorValue>("danger-soft")
    val ENERGY = DesignToken<CSSColorValue>("energy")
    val FOCUS = DesignToken<CSSColorValue>("focus")
    val GRAPH = DesignToken<CSSColorValue>("graph")
    val GRID = DesignToken<CSSColorValue>("grid")
    val INK = DesignToken<CSSColorValue>("ink")
    val LINE = DesignToken<CSSColorValue>("line")
    val MATH_DESCENT = DesignToken<CSSNumeric>("math-descent")
    val MATH_HEIGHT = DesignToken<CSSNumeric>("math-height")
    val MATH_WIDTH = DesignToken<CSSNumeric>("math-width")
    val MOMENTUM = DesignToken<CSSColorValue>("momentum")
    val FONT_MONO = DesignToken<StylePropertyString>("font-mono")
    val MOTION_ARRIVE = DesignToken<CSSSizeValue<CSSUnit.ms>>("motion-arrive")
    val MOTION_EASE = DesignToken<AnimationTimingFunction>("motion-ease")
    val MOTION_FAST = DesignToken<CSSSizeValue<CSSUnit.ms>>("motion-fast")
    val MUTED = DesignToken<CSSColorValue>("muted")
    val PAPER = DesignToken<CSSColorValue>("paper")
    val RADIUS = DesignToken<CSSNumeric>("radius")
    val REFERENCE = DesignToken<CSSColorValue>("reference")
    val REFERENCE_SOFT = DesignToken<CSSColorValue>("reference-soft")
    val REPRODUCTION_SCALE = DesignToken<StylePropertyNumber>("reproduction-scale")
    val FONT_TEXT = DesignToken<StylePropertyString>("font-text")
    val SIGNAL_INDEX = DesignToken<StylePropertyNumber>("signal-index")
    val SPACE = DesignToken<CSSNumeric>("space")
    val SPACE_1 = DesignToken<CSSNumeric>("space-1")
    val SPACE_2 = DesignToken<CSSNumeric>("space-2")
    val SPACE_3 = DesignToken<CSSNumeric>("space-3")
    val SPACE_4 = DesignToken<CSSNumeric>("space-4")
    val SPACE_5 = DesignToken<CSSNumeric>("space-5")
    val SPACE_6 = DesignToken<CSSNumeric>("space-6")
    val SPACE_8 = DesignToken<CSSNumeric>("space-8")
    val SURFACE = DesignToken<CSSColorValue>("surface")
    val SURFACE_RAISED = DesignToken<CSSColorValue>("surface-raised")
    val TEXT = DesignToken<CSSColorValue>("text")
    val WAVE_SHIFT = DesignToken<CSSNumeric>("wave-shift")
}
