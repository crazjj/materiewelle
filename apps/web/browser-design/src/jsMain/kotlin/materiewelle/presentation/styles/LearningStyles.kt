@file:OptIn(org.jetbrains.compose.web.ExperimentalComposeWebApi::class)

package materiewelle.presentation.styles

import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.css.keywords.auto

/** Canonical Kotlin style definitions, using shared DOM identities and typed values. */
internal fun StyleRules.learningStyles() {
    style(c(LabClass.LABOR_LEARNING_TOOLBAR)) {
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        justifyContent(JustifyContent.SpaceBetween)
        gap((16).px)
        marginBottom((18).px)
    }
    style(desc(c(LabClass.LABOR_LEARNING_TOOLBAR), t(SelectorTag.P))) { margin(0.px) }
    style(c(LabClass.WORKSHOP_TABS)) {
        display(DisplayStyle.Flex)
        flexWrap(FlexWrap.Wrap)
        gap((8).px)
        margin(0.px, 0.px, (24).px)
    }
    style(c(LabClass.WORKSHOP_ACTIONS)) {
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        gap((8).px)
        flexWrap(FlexWrap.Wrap)
        margin((12).px, 0.px)
    }
    style(c(LabClass.WORKSHOP_WORKSPACE)) {
        columns(
            GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(2.6)),
            GridTrack.MinMax(GridTrack.Fixed((280).px), GridTrack.Fraction(1)),
        )
    }
    style(child(desc(c(LabClass.WORKSHOP_WORKSPACE), c(LabClass.CONTROLS)), t(SelectorTag.LABEL))) {
        marginTop((20).px)
    }
    style(
        child(
            desc(c(LabClass.WORKSHOP_WORKSPACE), c(LabClass.CONTROLS)),
            allOf(
                t(SelectorTag.INPUT),
                negate(attribute(SelectorAttribute.TYPE, AttributeValue.RANGE)),
            ),
        )
    ) {
        width((100).percent)
        fontFamily(FontStack.MONO)
    }
    style(c(LabClass.DERIVATION_SEQUENCE)) {
        display(DisplayStyle.Grid)
        gap((12).px)
        margin((24).px, 0.px)
    }
    style(c(LabClass.EQUATION_STEP)) {
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        gap((24).px)
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        background(Tokens.SURFACE_RAISED.value())
        borderRadius((8).px)
        padding((18).px, (24).px)
    }
    style(allOf(c(LabClass.EQUATION_STEP), c(LabClass.CURRENT))) {
        borderColor(Tokens.ACCENT.value())
        background(Tokens.ACCENT_SOFT.value())
    }
    style(allOf(c(LabClass.EQUATION_STEP), c(LabClass.COMPLETE))) {
        edge(CssProperty.BORDER_LEFT, (4).px, LineStyle.Solid, Tokens.ACCENT.value())
    }
    style(desc(allOf(c(LabClass.EQUATION_STEP), c(LabClass.PENDING)), c(LabClass.STEP_NUMBER))) {
        color(Tokens.MUTED.value())
    }
    style(c(LabClass.EQUATION)) {
        fontFamily(FontStack.SERIF)
        composite(CssProperty.FONT_SIZE, "clamp(23px,3.3vw,38px)")
        set(CssProperty.LINE_HEIGHT, 1.35)
    }
    style(c(LabClass.LEARNING_CARDS)) {
        display(DisplayStyle.Grid)
        columns(GridTrack.Fraction(1), GridTrack.Fraction(1))
        gap((16).px)
        margin(0.px, 0.px, (16).px)
    }
    style(desc(c(LabClass.LEARNING_CARDS), t(SelectorTag.INPUT))) {
        width((100).percent)
        maxWidth((260).px)
        fontFamily(FontStack.MONO)
    }
    style(c(LabClass.QUIZ_CHOICE)) {
        display(DisplayStyle.Block)
        setTextAlign(TextAlignValue.LEFT)
        width((100).percent)
        marginTop((8).px)
    }
    style(c(LabClass.TUTOR_PROMPTS)) {
        display(DisplayStyle.Grid)
        gap((8).px)
    }
    style(desc(c(LabClass.TUTOR_PROMPTS), t(SelectorTag.BUTTON))) {
        setTextAlign(TextAlignValue.LEFT)
    }
    style(
        desc(
            c(LabClass.TUTOR_PROMPTS),
            allOf(
                t(SelectorTag.BUTTON),
                attribute(SelectorAttribute.ARIA_PRESSED, AttributeValue.TRUE),
            ),
        )
    ) {
        borderColor(Tokens.ACCENT.value())
        background(Tokens.ACCENT_SOFT.value())
        color(Tokens.ACCENT.value())
    }
    style(c(LabClass.MEDIA_PANEL)) { marginBottom((16).px) }
    style(c(LabClass.MEDIA_LAYOUT)) {
        display(DisplayStyle.Grid)
        columns(GridTrack.Fraction(1.1), GridTrack.Fraction(1))
        gap((24).px)
    }
    style(c(LabClass.STORYBOARD_VISUAL)) {
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius((8).px)
        background(Tokens.SURFACE_RAISED.value())
        padding((24).px)
        minHeight((220).px)
    }
    style(desc(c(LabClass.STORYBOARD_VISUAL), t(SelectorTag.P))) {
        fontSize((13).px)
        color(Tokens.MUTED.value())
    }
    style(child(c(LabClass.MEDIA_LAYOUT), t(SelectorTag.DIV))) { minWidth(0.px) }
    style(desc(c(LabClass.MEDIA_LAYOUT), allOf(t(SelectorTag.A), c(LabClass.SECONDARY)))) {
        display(DisplayStyle.InlineBlock)
        setTextDecoration(TextDecorationValue.NONE)
    }
    style(c(LabClass.SETUP_CLICKABLE)) { setCursor(CursorValue.POINTER) }
    style(allOf(c(LabClass.SETUP_CLICKABLE), pseudo(SelectorPseudo.HOVER))) {
        composite(CssProperty.FILTER, "brightness(1.2)")
    }
    style(desc(c(LabClass.DOCUMENT_TOOLBAR), t(SelectorTag.SPAN))) {
        font(FontSpec((13).px, FontStack.MONO))
    }
    style(desc(c(LabClass.DOCUMENT_TOOLBAR), t(SelectorTag.A))) {
        setTextDecoration(TextDecorationValue.NONE)
        display(DisplayStyle.LegacyInlineFlex)
        alignItems(AlignItems.Center)
    }
    style(c(LabClass.DOCUMENT_VIEWPORT)) {
        setOverflow(OverflowValue.AUTO)
        maxHeight((850).px)
        maxWidth((960).px)
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius((6).px)
        background(Tokens.SURFACE_RAISED.value())
        padding((20).px)
        composite(CssProperty.MARGIN, "16px auto")
        setOverscrollBehaviorX(OverscrollBehaviorXValue.CONTAIN)
    }
    style(desc(c(LabClass.DOCUMENT_VIEWPORT), t(SelectorTag.IMG))) {
        display(DisplayStyle.Block)
        background(Color.white)
        composite(CssProperty.BOX_SHADOW, "0 6px 20px #0002")
        height(auto)
        minWidth(0.px)
    }
    style(c(LabClass.TOUR_SHADE)) {
        position(Position.Fixed)
        zIndex(1000)
        composite(CssProperty.BACKGROUND, "rgb(5 12 18 / .74)")
        setTouchAction(TouchActionValue.NONE)
    }
    style(c(LabClass.TOUR_RING)) {
        position(Position.Fixed)
        zIndex(1001)
        setPointerEvents(PointerEventsValue.NONE)
        border((3).px, LineStyle.Solid, Tokens.FOCUS.value())
        borderRadius((8).px)
        composite(CssProperty.BOX_SHADOW, "0 0 0 2px var(--surface)")
    }
    style(c(LabClass.TOUR_DIALOG)) {
        position(Position.Fixed)
        zIndex(1002)
        set(CssProperty.RIGHT, (24).px)
        composite(CssProperty.WIDTH, "min(380px,calc(100vw - 24px))")
        composite(CssProperty.MAX_HEIGHT, "calc(100dvh - 24px)")
        setOverflow(OverflowValue.AUTO)
        border((1).px, LineStyle.Solid, Tokens.ACCENT.value())
        borderRadius((10).px)
        background(Tokens.SURFACE.value())
        color(Tokens.TEXT.value())
        composite(CssProperty.BOX_SHADOW, "0 16px 50px #0006")
        padding((20).px)
    }
    style(desc(c(LabClass.TOUR_DIALOG), t(SelectorTag.P))) { fontSize((14).px) }
    style(desc(c(LabClass.TOUR_DIALOG), c(LabClass.SMALL))) { fontSize((11).px) }
    style(desc(c(LabClass.TOUR_DIALOG), c(LabClass.WORKSHOP_ACTIONS))) { marginBottom(0.px) }
    style(desc(c(LabClass.TOUR_DIALOG), t(SelectorTag.H2))) { fontSize((18).px) }
    style(desc(c(LabClass.TOUR_DIALOG), c(LabClass.EYEBROW))) { fontSize((11).px) }
    style(
        anyOf(
            desc(c(LabClass.TOUR_DIALOG), c(LabClass.SECONDARY)),
            desc(c(LabClass.TOUR_DIALOG), c(LabClass.PRIMARY)),
        )
    ) {
        minHeight((44).px)
    }
    style(desc(c(LabClass.TOUR_DIALOG), pseudo(SelectorPseudo.FOCUS_VISIBLE))) {
        outlineColor(Tokens.FOCUS.value())
    }
    media(DesignMedia.PREFERS_REDUCED_MOTION_NO_PREFERENCE.query) {
        style(c(LabClass.EQUATION_STEP)) {
            composite(CssProperty.TRANSITION, "border-color .2s,background-color .2s")
        }
        style(c(LabClass.STORYBOARD_VISUAL)) {
            animation(MotionKeyframe.PANEL_ENTER) {
                duration((0.2).s)
                timingFunction(AnimationTimingFunction.EaseOut)
            }
        }
    }
    media(CSSMediaQuery.MediaFeature("max-width", (1000).px)) {
        style(c(LabClass.SITE_HEADER)) { flexWrap(FlexWrap.Wrap) }
        style(c(LabClass.WORKSHOP_WORKSPACE)) {
            columns(
                GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1.8)),
                GridTrack.MinMax(GridTrack.Fixed((260).px), GridTrack.Fraction(1)),
            )
        }
    }
    media(CSSMediaQuery.MediaFeature("max-width", (760).px)) {
        style(desc(allOf(t(SelectorTag.HEADER), c(LabClass.SITE_HEADER)), t(SelectorTag.NAV))) {
            columns(
                GridTrack.Repeat(4, GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1)))
            )
        }
        style(
            anyOf(
                c(LabClass.WORKSHOP_WORKSPACE),
                c(LabClass.LEARNING_CARDS),
                c(LabClass.MEDIA_LAYOUT),
            )
        ) {
            columns(GridTrack.Fraction(1))
        }
        style(c(LabClass.WORKSHOP_TABS)) {
            display(DisplayStyle.Grid)
            columns(
                GridTrack.Repeat(2, GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1)))
            )
        }
        style(c(LabClass.EQUATION_STEP)) {
            padding((14).px)
            gap((14).px)
        }
        style(c(LabClass.TOUR_DIALOG)) {
            set(CssProperty.RIGHT, (12).px)
            padding((16).px)
        }
        style(c(LabClass.DOCUMENT_VIEWPORT)) {
            padding((8).px)
            maxHeight((70).vh)
        }
        style(c(LabClass.DOCUMENT_TOOLBAR)) { gap((6).px) }
        style(desc(c(LabClass.DOCUMENT_TOOLBAR), t(SelectorTag.A))) { fontSize((12).px) }
        style(c(LabClass.MEDIA_LAYOUT)) { gap((12).px) }
        style(c(LabClass.LABOR_LEARNING_TOOLBAR)) { alignItems(AlignItems.FlexStart) }
        style(desc(c(LabClass.LABOR_LEARNING_TOOLBAR), c(LabClass.EXPERIMENT_EYEBROW))) {
            maxWidth((240).px)
            set(CssProperty.LINE_HEIGHT, 1.6)
        }
        style(desc(c(LabClass.LABOR_LEARNING_TOOLBAR), t(SelectorTag.BUTTON))) { flexShrink(0) }
        style(c(LabClass.STORYBOARD_VISUAL)) { padding((16).px) }
    }
    style(i(Id.QUIZ_DRAFT)) {
        width((100).percent)
        font(FontSpec((12).px, FontStack.MONO, lineHeight = 1.6))
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius((6).px)
        background(Tokens.BG.value())
        color(Tokens.TEXT.value())
        padding((12).px)
        setResize(ResizeValue.VERTICAL)
    }
    style(c(LabClass.TUTOR_HANDOFF)) { marginBottom((24).px) }
    style(c(LabClass.TUTOR_HANDOFF_GRID)) {
        display(DisplayStyle.Grid)
        columns(GridTrack.Repeat(3, GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1))))
        gap((16).px)
        margin((22).px, 0.px, (18).px)
    }
    style(child(c(LabClass.TUTOR_HANDOFF_GRID), t(SelectorTag.DIV))) { minWidth(0.px) }
    style(
        anyOf(
            desc(c(LabClass.TUTOR_HANDOFF), t(SelectorTag.SELECT)),
            desc(c(LabClass.TUTOR_HANDOFF), t(SelectorTag.TEXTAREA)),
        )
    ) {
        width((100).percent)
        minWidth(0.px)
    }
    style(desc(c(LabClass.TUTOR_HANDOFF), t(SelectorTag.TEXTAREA))) {
        setResize(ResizeValue.VERTICAL)
        margin((8).px, 0.px, (12).px)
    }
    style(desc(c(LabClass.TUTOR_HANDOFF), c(LabClass.TUTOR_START_TEXT))) {
        font(FontSpec((12).px, FontStack.MONO, lineHeight = 1.65))
        setWhiteSpace(WhiteSpaceValue.PRE_WRAP)
        setOverflowWrap(OverflowWrapValue.ANYWHERE)
    }
    style(desc(c(LabClass.TUTOR_HANDOFF), c(LabClass.WORKSHOP_ACTIONS))) {
        alignItems(AlignItems.Stretch)
    }
    style(child(desc(c(LabClass.TUTOR_HANDOFF), c(LabClass.WORKSHOP_ACTIONS)), t(SelectorTag.A))) {
        display(DisplayStyle.LegacyInlineFlex)
        alignItems(AlignItems.Center)
        justifyContent(JustifyContent.Center)
    }
    style(desc(c(LabClass.TUTOR_HANDOFF), c(LabClass.STATUS_TAG))) {
        setWhiteSpace(WhiteSpaceValue.NORMAL)
        setTextAlign(TextAlignValue.CENTER)
    }
    media(CSSMediaQuery.MediaFeature("max-width", (700).px)) {
        style(c(LabClass.TUTOR_HANDOFF_GRID)) {
            columns(GridTrack.Fraction(1))
            gap((12).px)
        }
        style(desc(c(LabClass.TUTOR_HANDOFF), c(LabClass.SECTION_HEADING))) {
            alignItems(AlignItems.FlexStart)
            flexDirection(FlexDirection.Column)
        }
        style(child(desc(c(LabClass.TUTOR_HANDOFF), c(LabClass.WORKSHOP_ACTIONS)), universal)) {
            composite(CssProperty.FLEX, "1 1 160px")
        }
    }
    style(desc(c(LabClass.TUTOR_HANDOFF), t(SelectorTag.TEXTAREA))) {
        background(Tokens.SURFACE_RAISED.value())
        color(Tokens.TEXT.value())
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius((6).px)
        padding((10).px, (12).px)
        setFont(FontValue.INHERIT)
        set(CssProperty.LINE_HEIGHT, 1.5)
        minHeight((68).px)
        setBoxSizing(BoxSizingValue.BORDER_BOX)
    }
    style(c(LabClass.TOUR_CONNECTOR)) {
        position(Position.Fixed)
        set(CssProperty.INSET, 0.px)
        zIndex(1001)
        setPointerEvents(PointerEventsValue.NONE)
    }
    style(
        anyOf(
            desc(c(LabClass.TOUR_CONNECTOR), c(LabClass.CHART)),
            desc(c(LabClass.TOUR_CONNECTOR), t(SelectorTag.SVG)),
        )
    ) {
        display(DisplayStyle.Block)
        width((100).percent)
        height((100).percent)
        margin(0.px)
    }
    style(desc(c(LabClass.TOUR_CONNECTOR), c(LabClass.CHART))) { setMaxWidth(MaxWidthValue.NONE) }
    style(
        desc(
            c(LabClass.MEDIA_PANEL),
            allOf(t(SelectorTag.INPUT), attribute(SelectorAttribute.TYPE, AttributeValue.RANGE)),
        )
    ) {
        width((100).percent)
        margin((12).px, 0.px)
    }
    style(desc(desc(c(LabClass.MEDIA_PANEL), c(LabClass.STORYBOARD_VISUAL)), c(LabClass.CHART))) {
        margin((14).px, (-8).px, 0.px)
    }
    style(desc(c(LabClass.MEDIA_PANEL), c(LabClass.STORYBOARD_VISUAL))) { minHeight((260).px) }
    style(desc(c(LabClass.MEDIA_PANEL), c(LabClass.CHART_READOUT))) {
        flexWrap(FlexWrap.Wrap)
        gap((8).px, (20).px)
    }
    style(desc(c(LabClass.MEDIA_PANEL), c(LabClass.MEDIA_LAYOUT))) { alignItems(AlignItems.Start) }
    style(
        child(
            child(desc(c(LabClass.MEDIA_PANEL), c(LabClass.MEDIA_LAYOUT)), t(SelectorTag.DIV)),
            t(SelectorTag.LABEL),
        )
    ) {
        display(DisplayStyle.Block)
        marginTop((16).px)
    }
    style(
        desc(
            desc(c(LabClass.MEDIA_PANEL), c(LabClass.MEDIA_LAYOUT)),
            allOf(t(SelectorTag.A), c(LabClass.SECONDARY)),
        )
    ) {
        setOverflowWrap(OverflowWrapValue.ANYWHERE)
        maxWidth((100).percent)
    }
    style(c(LabClass.MEDIA_CHART)) {
        maxWidth((100).percent)
        setOverflow(OverflowValue.AUTO)
        setOverscrollBehaviorX(OverscrollBehaviorXValue.CONTAIN)
    }
    style(desc(c(LabClass.MEDIA_CHART), c(LabClass.CHART))) { minWidth((520).px) }
    style(desc(c(LabClass.TOUR_CONNECTOR), t(SelectorTag.SVG))) {
        composite(CssProperty.FILTER, "drop-shadow(0 0 1px var(--surface))")
    }
    style(
        anyOf(
            desc(
                c(LabClass.MEDIA_LAYOUT),
                allOf(t(SelectorTag.INPUT), attribute(SelectorAttribute.TYPE, AttributeValue.FILE)),
            ),
            desc(c(LabClass.MEDIA_LAYOUT), t(SelectorTag.AUDIO)),
        )
    ) {
        display(DisplayStyle.Block)
        width((100).percent)
        maxWidth((100).percent)
        minWidth(0.px)
        setBoxSizing(BoxSizingValue.BORDER_BOX)
    }
}
