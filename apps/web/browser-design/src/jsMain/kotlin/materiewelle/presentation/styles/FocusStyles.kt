@file:OptIn(org.jetbrains.compose.web.ExperimentalComposeWebApi::class)

package materiewelle.presentation.styles

import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.css.keywords.auto

/** Canonical Kotlin style definitions, using shared DOM identities and typed values. */
internal fun StyleRules.focusStyles() {
    style(pseudo(SelectorPseudo.ROOT)) {
        setToken(Tokens.ENERGY, rgb(135, 86, 0))
        setToken(Tokens.MOMENTUM, rgb(109, 72, 164))
    }
    style(
        allOf(
            pseudo(SelectorPseudo.ROOT),
            attribute(SelectorAttribute.DATA_THEME, AttributeValue.DARK),
        )
    ) {
        setToken(Tokens.ENERGY, rgb(239, 193, 106))
        setToken(Tokens.MOMENTUM, rgb(198, 164, 244))
    }
    style(c(LabClass.FOCUS_SEQUENCE)) { minWidth(0.px) }
    style(c(LabClass.FOCUS_PHASES)) {
        display(DisplayStyle.Grid)
        columns(GridTrack.Repeat(4, GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1))))
        gap((6).px)
        marginBottom((14).px)
    }
    style(desc(c(LabClass.FOCUS_PHASES), t(SelectorTag.BUTTON))) {
        fontSize((12).px)
        padding((10).px, (6).px)
        setWhiteSpace(WhiteSpaceValue.NORMAL)
    }
    style(
        desc(
            c(LabClass.FOCUS_PHASES),
            allOf(
                t(SelectorTag.BUTTON),
                attribute(SelectorAttribute.ARIA_PRESSED, AttributeValue.TRUE),
            ),
        )
    ) {
        background(Tokens.ACCENT_SOFT.value())
        borderColor(Tokens.ACCENT.value())
        color(Tokens.ACCENT.value())
    }
    style(c(LabClass.FOCUS_PANEL)) {
        padding((14).px, 0.px, (20).px)
        minWidth(0.px)
        set(CssProperty.SCROLL_MARGIN_TOP, (90).px)
    }
    style(child(c(LabClass.FOCUS_PANEL), t(SelectorTag.H2))) {
        margin((8).px, 0.px, (16).px)
        maxWidth((38).ch)
        composite(CssProperty.FONT_SIZE, "clamp(20px,2vw,27px)")
        letterSpacing((-0.02).em)
    }
    style(child(c(LabClass.FOCUS_PANEL), t(SelectorTag.P))) {
        maxWidth((72).ch)
        set(CssProperty.LINE_HEIGHT, 1.65)
    }
    style(child(desc(c(LabClass.FOCUS_PANEL), c(LabClass.COURSE_RESULT)), t(SelectorTag.P))) {
        font(FontSpec((14).px, FontStack.TEXT, lineHeight = 1.65))
    }
    style(
        child(
            desc(c(LabClass.FOCUS_PANEL), c(LabClass.COURSE_RESULT)),
            allOf(t(SelectorTag.P), negate(pseudo(SelectorPseudo.LAST_CHILD))),
        )
    ) {
        font(FontSpec((13).px, FontStack.MONO, lineHeight = 1.65))
    }
    style(desc(c(LabClass.FOCUS_PANEL), c(LabClass.COURSE_NOTE))) { margin((12).px, 0.px) }
    style(c(LabClass.FOCUS_OUTLINE)) {
        edge(CssProperty.BORDER_LEFT, (2).px, LineStyle.Solid, Tokens.BORDER.value())
        padding((4).px, 0.px, (4).px, (15).px)
        margin((20).px, 0.px)
        color(Tokens.MUTED.value())
    }
    style(desc(c(LabClass.FOCUS_OUTLINE), t(SelectorTag.H3))) {
        margin(0.px, 0.px, (8).px)
        font(FontSpec((12).px, FontStack.MONO))
        setTextTransform(TextTransformValue.UPPERCASE)
        letterSpacing((0.06).em)
    }
    style(desc(c(LabClass.FOCUS_OUTLINE), t(SelectorTag.P))) {
        setWhiteSpace(WhiteSpaceValue.PRE_WRAP)
        setOverflowWrap(OverflowWrapValue.ANYWHERE)
    }
    style(c(LabClass.FOCUS_COMPARISON)) {
        display(DisplayStyle.Grid)
        columns(GridTrack.Repeat(3, GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1))))
        gap((10).px)
        margin((24).px, 0.px)
    }
    style(child(c(LabClass.FOCUS_COMPARISON), t(SelectorTag.DIV))) {
        edge(CssProperty.BORDER_TOP, (2).px, LineStyle.Solid, Color.currentColor)
        paddingTop((10).px)
        minWidth(0.px)
    }
    style(desc(c(LabClass.FOCUS_COMPARISON), c(LabClass.RESULT_VALUE))) {
        display(DisplayStyle.Block)
        setColor(ColorValue.INHERIT)
        font(FontSpec(boundedSize((18).px, (2).vw, (27).px), FontStack.MONO))
        setFontVariantNumeric(FontVariantNumericValue.TABULAR_NUMS)
        setWhiteSpace(WhiteSpaceValue.NOWRAP)
    }
    style(desc(c(LabClass.FOCUS_COMPARISON), t(SelectorTag.P))) {
        fontSize((12).px)
        margin((4).px, 0.px, (8).px)
    }
    style(c(LabClass.SEMANTIC_ENERGY)) { color(Tokens.ENERGY.value()) }
    style(c(LabClass.SEMANTIC_MOMENTUM)) { color(Tokens.MOMENTUM.value()) }
    style(c(LabClass.SEMANTIC_WAVE)) { color(Tokens.ACCENT.value()) }
    style(c(LabClass.SCALING_TRACK)) {
        height((12).px)
        borderRadius((3).px)
        background(Tokens.SURFACE_RAISED.value())
        margin((8).px, 0.px, (16).px)
        setOverflow(OverflowValue.HIDDEN)
    }
    style(c(LabClass.SCALING_BAR)) {
        height((100).percent)
        background(Tokens.ACCENT.value())
        borderRadius((3).px)
    }
    style(c(LabClass.PIXEL_PREVIEW)) {
        display(DisplayStyle.Block)
        width((100).percent)
        height(auto)
        borderRadius((4).px)
        margin((16).px, 0.px)
        background(rgb(255, 255, 255))
    }
    style(desc(c(LabClass.PIXEL_PREVIEW), c(LabClass.NATIVE_OWN))) {
        set(CssProperty.STROKE_WIDTH, 1.2)
    }
    style(child(c(LabClass.COURSE_CONTEXT), allOf(t(SelectorTag.A), c(LabClass.SECONDARY)))) {
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        justifyContent(JustifyContent.Center)
        setTextDecoration(TextDecorationValue.NONE)
        width((100).percent)
        margin((16).px, 0.px, 0.px)
        fontSize((12).px)
        set(CssProperty.LINE_HEIGHT, 1.6)
    }
    style(desc(c(LabClass.DOCUMENT_TOOLBAR), t(SelectorTag.SELECT))) {
        maxWidth((100).percent)
        minWidth(0.px)
        composite(CssProperty.WIDTH, "min(100%,410px)")
    }
    style(desc(c(LabClass.DOCUMENT_TOOLBAR), t(SelectorTag.LABEL))) { margin(0.px) }
    style(desc(c(LabClass.DOCUMENT_TOOLBAR), allOf(t(SelectorTag.A), c(LabClass.SECONDARY)))) {
        setTextDecoration(TextDecorationValue.NONE)
    }
    media(CSSMediaQuery.MediaFeature("max-width", (900).px)) {
        style(child(c(LabClass.COURSE_CONTEXT), allOf(t(SelectorTag.A), c(LabClass.SECONDARY)))) {
            composite(CssProperty.GRID_COLUMN, "1/-1")
            margin(0.px)
        }
    }
    media(CSSMediaQuery.MediaFeature("max-width", (600).px)) {
        style(c(LabClass.FOCUS_PHASES)) {
            columns(
                GridTrack.Repeat(2, GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1)))
            )
        }
        style(desc(c(LabClass.FOCUS_PHASES), t(SelectorTag.BUTTON))) { fontSize((12).px) }
        style(c(LabClass.FOCUS_COMPARISON)) { gap((8).px) }
        style(desc(c(LabClass.FOCUS_COMPARISON), t(SelectorTag.P))) { fontSize((11).px) }
        style(c(LabClass.FOCUS_PANEL)) { paddingTop((8).px) }
    }
    media(DesignMedia.PREFERS_REDUCED_MOTION_NO_PREFERENCE.query) {
        style(c(LabClass.SCALING_BAR)) {
            composite(CssProperty.TRANSITION, "width .45s cubic-bezier(.22,.76,.22,1)")
        }
        style(allOf(c(LabClass.FOCUS_PANEL), c(LabClass.REVEAL_ENTER))) {
            animation(MotionKeyframe.LEARNING_CONTENT_ARRIVE) {
                duration((0.24).s)
                timingFunction(AnimationTimingFunction.EaseOut)
                fillMode(AnimationFillMode.Both)
            }
        }
    }
    media(DesignMedia.PREFERS_REDUCED_MOTION_REDUCE.query) {
        style(
            anyOf(c(LabClass.SCALING_BAR), allOf(c(LabClass.FOCUS_PANEL), c(LabClass.REVEAL_ENTER)))
        ) {
            setTransition(TransitionValue.NONE)
            setAnimation(AnimationValue.NONE)
        }
    }
}
