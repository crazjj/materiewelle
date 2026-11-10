@file:OptIn(org.jetbrains.compose.web.ExperimentalComposeWebApi::class)

package materiewelle.presentation.styles

import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.css.keywords.auto

/** Canonical Kotlin style definitions, using shared DOM identities and typed values. */
internal fun StyleRules.learningMotionStyles() {
    style(c(LabClass.INLINE_FORMULA)) {
        display(DisplayStyle.LegacyInlineFlex)
        alignItems(AlignItems.Center)
        setVerticalAlign(VerticalAlignValue.MIDDLE)
        maxWidth((100).percent)
        minWidth(0.px)
        set(CssProperty.LINE_HEIGHT, 1)
        setFontStyle(FontStyleValue.NORMAL)
    }
    style(desc(c(LabClass.INLINE_FORMULA), c(LabClass.INLINE_FORMULA_ASSET))) {
        display(DisplayStyle.Block)
        width(auto)
        height(auto)
        maxWidth((100).percent)
        maxHeight((4.2).em)
        setObjectFit(ObjectFitValue.CONTAIN)
        composite(CssProperty.OBJECT_POSITION, "left center")
        background(Color.transparent)
        composite(CssProperty.BORDER, "0")
        setBoxShadow(BoxShadowValue.NONE)
        margin(0.px)
    }
    // Single point/segment labels follow prose size; displayed equations retain their own dimensions.
    style(allOf(c(LabClass.INLINE_FORMULA), c(LabClass.INLINE_FORMULA_TEXT_SYMBOL))) {
        display(DisplayStyle.InlineBlock)
        setVerticalAlign(VerticalAlignValue.BASELINE)
    }
    style(desc(c(LabClass.INLINE_FORMULA_TEXT_SYMBOL), c(LabClass.INLINE_FORMULA_ASSET))) {
        height(1.em)
        width(auto)
    }
    style(desc(c(LabClass.INLINE_FORMULA), c(LabClass.INLINE_FORMULA_DARK))) {
        display(DisplayStyle.None)
    }
    style(
        desc(
            desc(
                allOf(
                    t(SelectorTag.HTML),
                    attribute(SelectorAttribute.DATA_THEME, AttributeValue.DARK),
                ),
                c(LabClass.INLINE_FORMULA),
            ),
            c(LabClass.INLINE_FORMULA_LIGHT),
        )
    ) {
        display(DisplayStyle.None)
    }
    style(
        desc(
            desc(
                allOf(
                    t(SelectorTag.HTML),
                    attribute(SelectorAttribute.DATA_THEME, AttributeValue.DARK),
                ),
                c(LabClass.INLINE_FORMULA),
            ),
            c(LabClass.INLINE_FORMULA_DARK),
        )
    ) {
        display(DisplayStyle.Block)
    }
    style(desc(c(LabClass.FORMULA), c(LabClass.INLINE_FORMULA))) {
        justifyContent(JustifyContent.Center)
        width((100).percent)
        minHeight((1.7).em)
    }
    style(c(LabClass.INLINE_FORMULA_FALLBACK)) {
        setFont(FontValue.INHERIT)
        setFontStyle(FontStyleValue.NORMAL)
        setOverflowWrap(OverflowWrapValue.ANYWHERE)
    }
    style(c(LabClass.DERIVATION_TRACK)) {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        alignItems(AlignItems.Stretch)
        margin((24).px, 0.px, (20).px)
        setIsolation(IsolationValue.ISOLATE)
    }
    style(allOf(c(LabClass.DERIVATION_CARD), c(LabClass.EQUATION_STEP))) {
        display(DisplayStyle.Grid)
        columns(
            GridTrack.Fixed((2).cssRem),
            GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1)),
        )
        alignItems(AlignItems.Start)
        gap((14).px)
        margin(0.px)
        padding((20).px)
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius((10).px)
        background(Tokens.SURFACE.value())
        minWidth(0.px)
        position(Position.Relative)
    }
    style(allOf(c(LabClass.DERIVATION_CARD), c(LabClass.CURRENT))) {
        borderColor(Tokens.ACCENT.value())
        composite(
            CssProperty.BACKGROUND,
            "linear-gradient(115deg,var(--accent-soft),var(--surface) 72%)",
        )
        composite(CssProperty.BOX_SHADOW, "0 0 0 1px var(--accent-soft)")
    }
    style(allOf(c(LabClass.DERIVATION_CARD), c(LabClass.COMPLETE))) {
        edge(CssProperty.BORDER_LEFT, (3).px, LineStyle.Solid, Tokens.ACCENT.value())
    }
    style(desc(c(LabClass.DERIVATION_CARD), c(LabClass.STEP_NUMBER))) {
        font(FontSpec((13).px, FontStack.MONO, weight = 500, lineHeight = 1.5))
        paddingTop((2).px)
        color(Tokens.MUTED.value())
    }
    style(desc(allOf(c(LabClass.DERIVATION_CARD), c(LabClass.CURRENT)), c(LabClass.STEP_NUMBER))) {
        color(Tokens.ACCENT.value())
    }
    style(c(LabClass.DERIVATION_CONTENT)) {
        minWidth(0.px)
        setOverflowWrap(OverflowWrapValue.ANYWHERE)
    }
    style(desc(c(LabClass.DERIVATION_CONTENT), t(SelectorTag.H3))) {
        fontSize((16).px)
        letterSpacing((-0.015).em)
        set(CssProperty.LINE_HEIGHT, 1.35)
        margin(0.px, 0.px, (9).px)
    }
    style(child(c(LabClass.DERIVATION_CONTENT), t(SelectorTag.P))) {
        fontSize((14).px)
        set(CssProperty.LINE_HEIGHT, 1.65)
        margin(0.px, 0.px, (14).px)
        maxWidth((65).ch)
        color(Tokens.MUTED.value())
    }
    style(child(c(LabClass.DERIVATION_CONTENT), c(LabClass.FORMULA))) {
        margin((12).px, 0.px)
        padding((12).px, (8).px)
        background(Color.transparent)
        borderColor(Color.transparent)
        borderRadius(0.px)
        setTextAlign(TextAlignValue.LEFT)
    }
    style(
        desc(child(c(LabClass.DERIVATION_CONTENT), c(LabClass.FORMULA)), c(LabClass.INLINE_FORMULA))
    ) {
        justifyContent(JustifyContent.FlexStart)
    }
    style(
        child(
            c(LabClass.DERIVATION_CONTENT),
            allOf(t(SelectorTag.P), pseudo(SelectorPseudo.LAST_CHILD)),
        )
    ) {
        marginBottom(0.px)
    }
    style(desc(c(LabClass.DERIVATION_CONTENT), c(LabClass.COURSE_DIAGRAM))) {
        margin((15).px, 0.px)
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        background(Tokens.SURFACE.value())
        borderRadius((6).px)
    }
    style(
        desc(desc(c(LabClass.DERIVATION_CONTENT), c(LabClass.COURSE_DIAGRAM)), t(SelectorTag.SVG))
    ) {
        minWidth((600).px)
    }
    style(c(LabClass.DERIVATION_CONNECTOR)) {
        position(Position.Relative)
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        height((30).px)
        marginLeft((34).px)
        color(Tokens.ACCENT.value())
        font(FontSpec((16).px, FontStack.MONO))
        setPointerEvents(PointerEventsValue.NONE)
    }
    style(allOf(c(LabClass.DERIVATION_CONNECTOR), pseudo(SelectorPseudo.BEFORE))) {
        composite(CssProperty.CONTENT, "\"\"")
        position(Position.Absolute)
        set(CssProperty.TOP, 0.px)
        set(CssProperty.BOTTOM, 0.px)
        set(CssProperty.LEFT, (4).px)
        edge(CssProperty.BORDER_LEFT, (1).px, LineStyle.Dashed, Tokens.BORDER.value())
    }
    style(child(c(LabClass.DERIVATION_CONNECTOR), t(SelectorTag.SPAN))) {
        position(Position.Relative)
        display(DisplayStyle.LegacyInlineFlex)
        alignItems(AlignItems.Center)
        justifyContent(JustifyContent.Center)
        zIndex(1)
        width((20).px)
        height((20).px)
        background(Tokens.SURFACE.value())
        borderRadius((50).percent)
        marginLeft((-5).px)
    }
    style(c(LabClass.DERIVATION_PLAYFUL)) {
        font(FontSpec((12).px, FontStack.MONO, lineHeight = 1.5))
        color(Tokens.MUTED.value())
        letterSpacing((0.01).em)
        edge(CssProperty.BORDER_LEFT, (2).px, LineStyle.Solid, Tokens.ACCENT.value())
        padding((4).px, 0.px, (4).px, (12).px)
        margin((16).px, 0.px)
    }
    style(allOf(c(LabClass.COURSE_STEP), c(LabClass.REVEAL_ENTER))) {
        set(CssProperty.SCROLL_MARGIN_TOP, (90).px)
    }
    media(DesignMedia.PREFERS_REDUCED_MOTION_NO_PREFERENCE.query) {
        style(desc(c(LabClass.COURSE_INDEX), c(LabClass.COURSE_CARD))) {
            animation(MotionKeyframe.LEARNING_CONTENT_ARRIVE) {
                duration((0.34).s)
                timingFunction(AnimationTimingFunction.EaseOut)
                fillMode(AnimationFillMode.Both)
            }
        }
        style(desc(c(LabClass.COURSE_INDEX), allOf(c(LabClass.COURSE_CARD), nthChild("2n")))) {
            set(CssProperty.ANIMATION_DELAY, (35).ms)
        }
        style(desc(c(LabClass.COURSE_INDEX), allOf(c(LabClass.COURSE_CARD), nthChild("3n")))) {
            set(CssProperty.ANIMATION_DELAY, (70).ms)
        }
        style(desc(c(LabClass.COURSE_INDEX), allOf(c(LabClass.COURSE_CARD), nthChild("4n")))) {
            set(CssProperty.ANIMATION_DELAY, (105).ms)
        }
        style(
            anyOf(
                child(
                    child(
                        desc(c(LabClass.WORKSHOP_WORKSPACE), c(LabClass.GRAPH_AREA)),
                        allOf(t(SelectorTag.DIV), pseudo(SelectorPseudo.FIRST_OF_TYPE)),
                    ),
                    c(LabClass.CHART),
                ),
                child(c(LabClass.COURSE_VISUAL), t(SelectorTag.SVG)),
            )
        ) {
            animation(MotionKeyframe.LEARNING_CONTENT_ARRIVE) {
                duration((0.38).s)
                timingFunction(AnimationTimingFunction.EaseOut)
                fillMode(AnimationFillMode.Both)
            }
        }
        style(c(LabClass.DERIVATION_CARD)) {
            animation(MotionKeyframe.LEARNING_CARD_ARRIVE) {
                duration((0.48).s)
                timingFunction(AnimationTimingFunction.cubicBezier(0.22, 0.76, 0.22, 1.0))
                fillMode(AnimationFillMode.Both)
            }
            composite(CssProperty.TRANSFORM_ORIGIN, "top left")
        }
        style(c(LabClass.DERIVATION_CONNECTOR)) {
            animation(MotionKeyframe.LEARNING_CONNECTOR_ARRIVE) {
                duration((0.4).s)
                timingFunction(AnimationTimingFunction.EaseOut)
                fillMode(AnimationFillMode.Both)
            }
        }
        style(allOf(c(LabClass.DERIVATION_CARD), c(LabClass.CURRENT))) {
            composite(
                CssProperty.TRANSITION,
                "border-color .2s ease,background-color .2s ease,box-shadow .2s ease",
            )
        }
        style(
            desc(allOf(c(LabClass.DERIVATION_CARD), c(LabClass.CURRENT)), c(LabClass.STEP_NUMBER))
        ) {
            animation(MotionKeyframe.LEARNING_NUMBER_GLOW) {
                duration((1.25).s)
                timingFunction(AnimationTimingFunction.EaseOut)
                iterationCount(1)
            }
        }
        style(allOf(c(LabClass.COURSE_STEP), c(LabClass.REVEAL_ENTER))) {
            animation(MotionKeyframe.LEARNING_CONTENT_ARRIVE) {
                duration((0.42).s)
                timingFunction(AnimationTimingFunction.cubicBezier(0.22, 0.76, 0.22, 1.0))
                fillMode(AnimationFillMode.Both)
            }
        }
        style(allOf(c(LabClass.COURSE_STEP), c(LabClass.REVEAL_ENTER), nthOfType("2"))) {
            set(CssProperty.ANIMATION_DELAY, (35).ms)
        }
        style(allOf(c(LabClass.COURSE_STEP), c(LabClass.REVEAL_ENTER), nthOfType("3"))) {
            set(CssProperty.ANIMATION_DELAY, (70).ms)
        }
        style(allOf(c(LabClass.COURSE_STEP), c(LabClass.REVEAL_ENTER), nthOfType("4"))) {
            set(CssProperty.ANIMATION_DELAY, (105).ms)
        }
        style(c(LabClass.DERIVATION_PLAYFUL)) {
            animation(MotionKeyframe.LEARNING_CONTENT_ARRIVE) {
                duration((0.35).s)
                timingFunction(AnimationTimingFunction.EaseOut)
                fillMode(AnimationFillMode.Both)
            }
        }
        style(
            desc(
                desc(desc(c(LabClass.COURSE_DIAGRAM), c(LabClass.CHART)), t(SelectorTag.SVG)),
                allOf(t(SelectorTag.PATH), negate(attribute(SelectorAttribute.STROKE_DASHARRAY))),
            )
        ) {
            composite(CssProperty.TRANSITION, "stroke .16s ease")
        }
        style(
            desc(
                desc(c(LabClass.COURSE_DIAGRAM), t(SelectorTag.SVG)),
                allOf(t(SelectorTag.CIRCLE), pseudo(SelectorPseudo.ONLY_OF_TYPE)),
            )
        ) {
            animation(MotionKeyframe.LEARNING_POINT_FEEDBACK) {
                duration((0.36).s)
                timingFunction(AnimationTimingFunction.EaseOut)
                iterationCount(1)
            }
            setTransformBox(TransformBoxValue.FILL_BOX)
            setTransformOrigin(TransformOriginValue.CENTER)
        }
    }
    frames(MotionKeyframe.LEARNING_CARD_ARRIVE) {
        each(0.percent) {
            opacity(0)
            transform { translateY((14).px) }
            composite(CssProperty.CLIP_PATH, "inset(0 0 12% 0)")
        }
        each(100.percent) {
            opacity(1)
            transform { translateY(0.px) }
            composite(CssProperty.CLIP_PATH, "inset(0)")
        }
    }
    frames(MotionKeyframe.LEARNING_CONNECTOR_ARRIVE) {
        each(0.percent) {
            opacity(0)
            transform { scaleY(0.4) }
        }
        each(100.percent) {
            opacity(1)
            transform { scaleY(1) }
        }
    }
    frames(MotionKeyframe.LEARNING_NUMBER_GLOW) {
        each(0.percent) { composite(CssProperty.TEXT_SHADOW, "0 0 0 transparent") }
        each(35.percent) { composite(CssProperty.TEXT_SHADOW, "0 0 14px var(--accent)") }
        each(100.percent) { composite(CssProperty.TEXT_SHADOW, "0 0 0 transparent") }
    }
    frames(MotionKeyframe.LEARNING_CONTENT_ARRIVE) {
        each(0.percent) {
            opacity(0)
            transform { translateY((8).px) }
        }
        each(100.percent) {
            opacity(1)
            transform { translateY(0.px) }
        }
    }
    frames(MotionKeyframe.LEARNING_POINT_FEEDBACK) {
        each(0.percent) { transform { scale(1) } }
        each(40.percent) { transform { scale(1.22) } }
        each(100.percent) { transform { scale(1) } }
    }
    media(CSSMediaQuery.MediaFeature("max-width", (600).px)) {
        style(allOf(c(LabClass.DERIVATION_CARD), c(LabClass.EQUATION_STEP))) {
            columns(
                GridTrack.Fixed((1.4).cssRem),
                GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1)),
            )
            padding((16).px, (12).px)
            gap((9).px)
            borderRadius((8).px)
        }
        style(desc(c(LabClass.DERIVATION_CONTENT), t(SelectorTag.H3))) { fontSize((15).px) }
        style(child(c(LabClass.DERIVATION_CONTENT), t(SelectorTag.P))) { fontSize((13).px) }
        style(child(c(LabClass.DERIVATION_CONTENT), c(LabClass.FORMULA))) { padding((10).px, 0.px) }
        style(c(LabClass.DERIVATION_CONNECTOR)) {
            marginLeft((23).px)
            height((24).px)
        }
        style(desc(c(LabClass.INLINE_FORMULA), c(LabClass.INLINE_FORMULA_ASSET))) {
            maxHeight((3.8).em)
        }
        style(c(LabClass.DERIVATION_TRACK)) { marginTop((18).px) }
    }
    media(DesignMedia.PREFERS_REDUCED_MOTION_REDUCE.query) {
        style(
            anyOf(
                c(LabClass.DERIVATION_CARD),
                desc(
                    allOf(c(LabClass.DERIVATION_CARD), c(LabClass.CURRENT)),
                    c(LabClass.STEP_NUMBER),
                ),
                c(LabClass.DERIVATION_CONNECTOR),
                allOf(c(LabClass.COURSE_STEP), c(LabClass.REVEAL_ENTER)),
                c(LabClass.DERIVATION_PLAYFUL),
                desc(c(LabClass.COURSE_INDEX), c(LabClass.COURSE_CARD)),
                child(
                    child(
                        desc(c(LabClass.WORKSHOP_WORKSPACE), c(LabClass.GRAPH_AREA)),
                        allOf(t(SelectorTag.DIV), pseudo(SelectorPseudo.FIRST_OF_TYPE)),
                    ),
                    c(LabClass.CHART),
                ),
                child(c(LabClass.COURSE_VISUAL), t(SelectorTag.SVG)),
                desc(
                    desc(c(LabClass.COURSE_DIAGRAM), t(SelectorTag.SVG)),
                    allOf(t(SelectorTag.CIRCLE), pseudo(SelectorPseudo.ONLY_OF_TYPE)),
                ),
            )
        ) {
            setAnimation(AnimationValue.NONE)
            setTransition(TransitionValue.NONE)
            setTransform(TransformValue.NONE)
            setClipPath(ClipPathValue.NONE)
            opacity(1)
        }
    }
}
