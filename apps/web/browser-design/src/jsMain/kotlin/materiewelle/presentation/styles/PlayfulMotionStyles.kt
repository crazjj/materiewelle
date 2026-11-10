@file:OptIn(org.jetbrains.compose.web.ExperimentalComposeWebApi::class)

package materiewelle.presentation.styles

import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.css.*

/** Decorative motion stays in the Kotlin rule tree; scientific coordinates never move. */
internal fun StyleRules.playfulMotionStyles() {
    style(child(c(LabClass.COURSE_PHASES), t(SelectorTag.SPAN))) {
        display(DisplayStyle.LegacyInlineFlex)
        alignItems(AlignItems.Center)
        gap(7.px)
    }
    style(c(MotionClass.PHASE_ICON)) {
        display(DisplayStyle.LegacyInlineFlex)
        width(24.px)
        height(24.px)
        flexShrink(0)
        opacity(.65)
    }
    style(c(MotionClass.PHASE_CURRENT)) { opacity(1) }
    style(desc(c(MotionClass.PHASE_ICON), c(LabClass.CHART))) {
        width(100.percent)
        minWidth(0.px)
        margin(0.px)
    }
    style(desc(c(MotionClass.PHASE_ICON), t(SelectorTag.SVG))) {
        width(24.px)
        height(24.px)
        minWidth(0.px)
        setColor(ColorValue.INHERIT)
    }
    style(anyOf(c(MotionClass.PHASE_PULSE), c(MotionClass.PHASE_SCAN))) {
        setTransformBox(TransformBoxValue.FILL_BOX)
        setTransformOrigin(TransformOriginValue.CENTER)
    }
    style(c(MotionClass.MASCOT_RIPPLE)) {
        position(Position.Absolute)
        composite(CssProperty.INSET, "4px")
        border(1.px, LineStyle.Solid, Tokens.ACCENT.value())
        borderRadius(50.percent)
        setPointerEvents(PointerEventsValue.NONE)
        opacity(0)
    }
    style(c(MotionClass.CONSTRUCTION_INK)) {
        composite(CssProperty.STROKE_DASHARRAY, "100")
        composite(CssProperty.STROKE_DASHOFFSET, "0")
    }
    media(DesignMedia.PREFERS_REDUCED_MOTION_NO_PREFERENCE.query) {
        style(desc(c(MotionClass.PHASE_CURRENT), c(MotionClass.PHASE_PULSE))) {
            animation(MotionKeyframe.LAB_PHASE_PULSE) {
                duration(3.6.s)
                timingFunction(AnimationTimingFunction.EaseInOut)
                iterationCount(null)
                direction(AnimationDirection.Alternate)
            }
        }
        style(desc(c(MotionClass.PHASE_CURRENT), c(MotionClass.PHASE_SCAN))) {
            animation(MotionKeyframe.LAB_PHASE_SCAN) {
                duration(2.8.s)
                timingFunction(AnimationTimingFunction.EaseInOut)
                iterationCount(null)
                direction(AnimationDirection.Alternate)
            }
        }
        style(desc(c(MotionClass.PHASE_CURRENT), c(MotionClass.PHASE_INK))) {
            composite(CssProperty.STROKE_DASHARRAY, "100")
            animation(MotionKeyframe.LAB_INK_DRAW) {
                duration(4.8.s)
                timingFunction(AnimationTimingFunction.EaseInOut)
                iterationCount(null)
                direction(AnimationDirection.Alternate)
            }
        }
        style(c(MotionClass.MASCOT_RIPPLE)) {
            animation(MotionKeyframe.LAB_MASCOT_RIPPLE) {
                duration(1.1.s)
                timingFunction(AnimationTimingFunction.EaseOut)
                fillMode(AnimationFillMode.Both)
            }
        }
        style(allOf(c(MotionClass.MASCOT_RIPPLE), nthChild("3"))) {
            set(CssProperty.ANIMATION_DELAY, 120.ms)
        }
        style(c(MotionClass.CONSTRUCTION_INK)) {
            animation(MotionKeyframe.LAB_INK_DRAW) {
                duration(950.ms)
                timingFunction(AnimationTimingFunction.EaseInOut)
                fillMode(AnimationFillMode.Backwards)
            }
        }
        style(c(MotionClass.CONSTRUCTION_SECOND)) { set(CssProperty.ANIMATION_DELAY, 240.ms) }
        style(
            allOf(
                c(MotionClass.WORKED_BUTTON),
                attribute(SelectorAttribute.ARIA_PRESSED, AttributeValue.TRUE),
            )
        ) {
            animation(MotionKeyframe.LAB_WORKED_STAMP) {
                duration(420.ms)
                timingFunction(Tokens.MOTION_EASE.value())
            }
        }
        style(c(LabClass.COURSE_CARD)) { position(Position.Relative) }
        style(allOf(c(LabClass.COURSE_CARD), pseudo(SelectorPseudo.BEFORE))) {
            composite(CssProperty.CONTENT, "\"\"")
            position(Position.Absolute)
            set(CssProperty.LEFT, 0.px)
            set(CssProperty.TOP, 20.percent)
            width(2.px)
            height(60.percent)
            borderRadius(2.px)
            background(Tokens.ACCENT.value())
            transform { scaleY(0) }
            composite(CssProperty.TRANSITION, "transform .24s var(--motion-ease)")
        }
        style(
            anyOf(
                allOf(
                    c(LabClass.COURSE_CARD),
                    pseudo(SelectorPseudo.HOVER),
                    pseudo(SelectorPseudo.BEFORE),
                ),
                allOf(
                    c(LabClass.COURSE_CARD),
                    pseudo(SelectorPseudo.FOCUS_VISIBLE),
                    pseudo(SelectorPseudo.BEFORE),
                ),
            )
        ) {
            transform { scaleY(1) }
        }
    }
    frames(MotionKeyframe.LAB_PHASE_PULSE) {
        each(0.percent) {
            opacity(.6)
            transform { scale(.9) }
        }
        each(100.percent) {
            opacity(1)
            transform { scale(1.08) }
        }
    }
    frames(MotionKeyframe.LAB_PHASE_SCAN) {
        each(0.percent) { transform { translateY((-6).px) } }
        each(100.percent) { transform { translateY(6.px) } }
    }
    frames(MotionKeyframe.LAB_INK_DRAW) {
        each(0.percent) { composite(CssProperty.STROKE_DASHOFFSET, "100") }
        each(45.percent, 100.percent) { composite(CssProperty.STROKE_DASHOFFSET, "0") }
    }
    frames(MotionKeyframe.LAB_MASCOT_RIPPLE) {
        each(0.percent) {
            opacity(.65)
            transform { scale(.7) }
        }
        each(100.percent) {
            opacity(0)
            transform { scale(1.8) }
        }
    }
    frames(MotionKeyframe.LAB_WORKED_STAMP) {
        each(0.percent) { transform { scale(1) } }
        each(40.percent) {
            transform {
                scale(.96)
                rotate((-1).deg)
            }
        }
        each(100.percent) { transform { scale(1) } }
    }
    // Pause/reduced motion must reveal complete diagrams, not freeze half a construction.
    style(
        desc(
            allOf(
                t(SelectorTag.HTML),
                negate(attribute(SelectorAttribute.DATA_MOTION, AttributeValue.RUNNING)),
            ),
            matchesAny(
                anyOf(
                    c(MotionClass.CONSTRUCTION_INK),
                    c(MotionClass.WORKED_BUTTON),
                    c(MotionClass.PHASE_INK),
                    c(MotionClass.PHASE_PULSE),
                    c(MotionClass.PHASE_SCAN),
                )
            ),
        )
    ) {
        important { setAnimation(AnimationValue.NONE) }
        important { composite(CssProperty.STROKE_DASHOFFSET, "0") }
    }
    media(CSSMediaQuery.MediaFeature("max-width", 600.px)) {
        style(child(c(LabClass.COURSE_PHASES), t(SelectorTag.SPAN))) { gap(4.px) }
        style(c(MotionClass.PHASE_ICON)) {
            width(18.px)
            height(18.px)
        }
        style(desc(c(MotionClass.PHASE_ICON), t(SelectorTag.SVG))) {
            width(18.px)
            height(18.px)
        }
    }
    media(CSSMediaQuery.MediaFeature("max-width", 360.px)) {
        // Keep all four labels; only the current phase needs decoration on narrow screens.
        style(c(MotionClass.PHASE_ICON)) { display(DisplayStyle.None) }
        style(c(MotionClass.PHASE_CURRENT)) { display(DisplayStyle.LegacyInlineFlex) }
    }
}
