@file:OptIn(org.jetbrains.compose.web.ExperimentalComposeWebApi::class)

package materiewelle.presentation.styles

import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.css.keywords.auto

/** Canonical Kotlin style definitions, using shared DOM identities and typed values. */
internal fun StyleRules.opticsWavesStyles() {
    style(c(OpticsWavesClass.SETUP)) { minWidth(0.px) }
    style(c(OpticsWavesClass.SETUP_CONTROLS)) {
        display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(12.px)
        minHeight(48.px); marginTop(20.px)
    }
    style(c(OpticsWavesClass.SETUP_READING)) {
        display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(12.px)
        flexGrow(1); flexWrap(FlexWrap.Wrap)
    }
    style(desc(c(OpticsWavesClass.SETUP_CONTROLS), t(SelectorTag.P))) {
        margin(0.px); color(Tokens.MUTED.value()); fontSize(16.px)
    }
    style(desc(c(OpticsWavesClass.SETUP_CONTROLS), t(SelectorTag.BUTTON))) { minWidth(108.px); minHeight(44.px) }
    style(c(OpticsWavesClass.SETUP_STEPS)) {
        display(DisplayStyle.Grid)
        columns(GridTrack.Fraction(1), GridTrack.Fraction(1))
        gap(8.px)
    }
    style(desc(c(OpticsWavesClass.SETUP_STEPS), t(SelectorTag.BUTTON))) {
        minHeight(44.px); fontSize(16.px); setWhiteSpace(WhiteSpaceValue.NORMAL)
    }
    style(c(OpticsWavesClass.SETUP_EXPLANATION)) {
        padding(18.px, 0.px); minHeight(170.px)
    }
    style(desc(c(OpticsWavesClass.SETUP_EXPLANATION), t(SelectorTag.H3))) { margin(0.px, 0.px, 10.px) }
    style(desc(c(OpticsWavesClass.SETUP_EXPLANATION), t(SelectorTag.P))) { margin(0.px) }
    style(c(OpticsWavesClass.INVESTIGATION)) {
        minWidth(0.px)
        set(CssProperty.LINE_HEIGHT, 1.65)
    }
    style(desc(c(OpticsWavesClass.INVESTIGATION), t(SelectorTag.H2))) {
        margin(0.px, 0.px, (12).px)
        letterSpacing((-0.025).em)
    }
    style(desc(c(OpticsWavesClass.INVESTIGATION), t(SelectorTag.H3))) {
        margin(0.px, 0.px, (12).px)
        fontSize((17).px)
        letterSpacing((-0.01).em)
    }
    style(c(OpticsWavesClass.BLOCK)) {
        padding((22).px, 0.px)
        edge(CssProperty.BORDER_TOP, (1).px, LineStyle.Solid, Tokens.BORDER.value())
        marginTop((20).px)
    }
    style(c(OpticsWavesClass.PANEL)) {
        padding((18).px)
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius(Tokens.RADIUS.value())
        background(Tokens.SURFACE_RAISED.value())
        margin((16).px, 0.px)
    }
    style(desc(c(OpticsWavesClass.PANEL), t(SelectorTag.P))) { margin((8).px, 0.px) }
    style(c(OpticsWavesClass.FIGURE)) {
        maxWidth((100).percent)
        setOverflowX(OverflowXValue.AUTO)
        setOverscrollBehaviorX(OverscrollBehaviorXValue.CONTAIN)
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius(Tokens.RADIUS.value())
        background(Tokens.BG.value())
        margin((16).px, 0.px)
        composite(CssProperty.SCROLLBAR_COLOR, "var(--muted) var(--surface)")
    }
    style(child(c(OpticsWavesClass.FIGURE), c(OpticsWavesClass.DIAGRAM))) {
        display(DisplayStyle.Block)
        setMaxWidth(MaxWidthValue.NONE)
        width((100).percent)
        height(auto)
        fontFamily(FontStack.TEXT)
    }
    style(child(c(OpticsWavesClass.FIGURE), c(OpticsWavesClass.OPTICS_DIAGRAM))) {
        minWidth((730).px)
    }
    style(child(c(OpticsWavesClass.FIGURE), c(OpticsWavesClass.WAVE_DIAGRAM))) {
        minWidth((560).px)
    }
    style(desc(c(OpticsWavesClass.DIAGRAM), c(OpticsWavesClass.TEXT))) {
        fontSize((17).px)
        set(CssProperty.FILL, Tokens.TEXT.value())
        color(Tokens.TEXT.value())
        setStroke(StrokeValue.NONE)
        fontFamily(FontStack.TEXT)
    }
    style(desc(c(OpticsWavesClass.DIAGRAM), c(OpticsWavesClass.SMALL_TEXT))) { fontSize((14).px) }
    style(desc(c(OpticsWavesClass.DIAGRAM), c(OpticsWavesClass.AXIS))) {
        set(CssProperty.STROKE, Tokens.MUTED.value())
        set(CssProperty.STROKE_WIDTH, 1)
        composite(CssProperty.STROKE_DASHARRAY, "3 6")
        setFill(FillValue.NONE)
        opacity(0.6)
    }
    style(desc(c(OpticsWavesClass.DIAGRAM), c(OpticsWavesClass.GUIDE))) {
        set(CssProperty.STROKE, Tokens.MUTED.value())
        set(CssProperty.STROKE_WIDTH, 1)
        composite(CssProperty.STROKE_DASHARRAY, "4 6")
        setFill(FillValue.NONE)
        opacity(0.8)
    }
    style(desc(c(OpticsWavesClass.DIAGRAM), c(OpticsWavesClass.PATH))) {
        set(CssProperty.STROKE, Tokens.ACCENT.value())
        set(CssProperty.STROKE_WIDTH, 2.4)
        setFill(FillValue.NONE)
        setStrokeLinecap(StrokeLinecapValue.ROUND)
        setStrokeLinejoin(StrokeLinejoinValue.ROUND)
    }
    style(desc(c(OpticsWavesClass.DIAGRAM), c(OpticsWavesClass.DIRECTION))) {
        set(CssProperty.STROKE, Tokens.MUTED.value())
        set(CssProperty.STROKE_WIDTH, 1.5)
        setFill(FillValue.NONE)
        setStrokeLinecap(StrokeLinecapValue.ROUND)
        setStrokeLinejoin(StrokeLinejoinValue.ROUND)
    }
    style(desc(c(OpticsWavesClass.DIAGRAM), c(OpticsWavesClass.MEASURE))) {
        set(CssProperty.STROKE, Tokens.TEXT.value())
        set(CssProperty.STROKE_WIDTH, 2.2)
        setFill(FillValue.NONE)
        setStrokeLinecap(StrokeLinecapValue.ROUND)
        setStrokeLinejoin(StrokeLinejoinValue.ROUND)
    }
    style(desc(c(OpticsWavesClass.DIAGRAM), c(OpticsWavesClass.SURFACE))) {
        set(CssProperty.FILL, Tokens.SURFACE_RAISED.value())
        set(CssProperty.STROKE, Tokens.MUTED.value())
        set(CssProperty.STROKE_WIDTH, 1.6)
    }
    style(desc(c(OpticsWavesClass.DIAGRAM), c(OpticsWavesClass.SLIT))) {
        set(CssProperty.FILL, Tokens.TEXT.value())
        setStroke(StrokeValue.NONE)
    }
    style(desc(c(OpticsWavesClass.DIAGRAM), c(OpticsWavesClass.LIGHT))) {
        set(CssProperty.FILL, Tokens.ACCENT_SOFT.value())
        set(CssProperty.FILL_OPACITY, 0.6)
        set(CssProperty.STROKE, Tokens.MUTED.value())
        set(CssProperty.STROKE_WIDTH, 1.7)
    }
    style(desc(c(OpticsWavesClass.DIAGRAM), c(OpticsWavesClass.PATTERN))) {
        set(CssProperty.STROKE, Tokens.ACCENT.value())
        set(CssProperty.STROKE_WIDTH, 2)
        setFill(FillValue.NONE)
    }
    style(desc(c(OpticsWavesClass.DIAGRAM), c(OpticsWavesClass.SAMPLE))) {
        set(CssProperty.FILL, Tokens.ACCENT.value())
        set(CssProperty.STROKE, Tokens.BG.value())
        set(CssProperty.STROKE_WIDTH, 1.5)
    }
    style(
        anyOf(
            desc(
                desc(c(OpticsWavesClass.DIAGRAM), c(OpticsWavesClass.ACTIVE_PART)),
                c(OpticsWavesClass.PATH),
            ),
            desc(
                desc(c(OpticsWavesClass.DIAGRAM), c(OpticsWavesClass.ACTIVE_PART)),
                c(OpticsWavesClass.DIRECTION),
            ),
        )
    ) {
        set(CssProperty.STROKE, Tokens.ACCENT.value())
        set(CssProperty.STROKE_WIDTH, 2.6)
    }
    style(
        anyOf(
            desc(
                desc(c(OpticsWavesClass.DIAGRAM), c(OpticsWavesClass.ACTIVE_PART)),
                c(OpticsWavesClass.SURFACE),
            ),
            desc(
                desc(c(OpticsWavesClass.DIAGRAM), c(OpticsWavesClass.ACTIVE_PART)),
                c(OpticsWavesClass.LIGHT),
            ),
        )
    ) {
        set(CssProperty.STROKE, Tokens.ACCENT.value())
        set(CssProperty.STROKE_WIDTH, 3)
        set(CssProperty.FILL, Tokens.ACCENT_SOFT.value())
    }
    style(
        desc(
            desc(c(OpticsWavesClass.DIAGRAM), c(OpticsWavesClass.ACTIVE_PART)),
            c(OpticsWavesClass.SLIT),
        )
    ) {
        set(CssProperty.FILL, Tokens.ACCENT.value())
    }
    style(
        desc(
            desc(c(OpticsWavesClass.DIAGRAM), c(OpticsWavesClass.ACTIVE_PART)),
            c(OpticsWavesClass.TEXT),
        )
    ) {
        set(CssProperty.FILL, Tokens.ACCENT.value())
        color(Tokens.ACCENT.value())
    }
    style(desc(c(OpticsWavesClass.DIAGRAM), c(OpticsWavesClass.FIRST))) {
        set(CssProperty.STROKE, Tokens.ACCENT.value())
    }
    style(desc(c(OpticsWavesClass.DIAGRAM), c(OpticsWavesClass.SECOND))) {
        set(CssProperty.STROKE, Tokens.REFERENCE.value())
        composite(CssProperty.STROKE_DASHARRAY, "7 5")
    }
    style(desc(c(OpticsWavesClass.DIAGRAM), c(OpticsWavesClass.SUM))) {
        set(CssProperty.STROKE, Tokens.TEXT.value())
        set(CssProperty.STROKE_WIDTH, 3.2)
    }
    style(
        desc(c(OpticsWavesClass.DIAGRAM), allOf(c(OpticsWavesClass.TEXT), c(OpticsWavesClass.FIRST)))
    ) {
        set(CssProperty.FILL, Tokens.ACCENT.value())
        setStroke(StrokeValue.NONE)
    }
    style(
        desc(c(OpticsWavesClass.DIAGRAM), allOf(c(OpticsWavesClass.TEXT), c(OpticsWavesClass.SECOND)))
    ) {
        set(CssProperty.FILL, Tokens.REFERENCE.value())
        setStroke(StrokeValue.NONE)
    }
    style(desc(c(OpticsWavesClass.DIAGRAM), allOf(c(OpticsWavesClass.TEXT), c(OpticsWavesClass.SUM)))) {
        set(CssProperty.FILL, Tokens.TEXT.value())
        setStroke(StrokeValue.NONE)
    }
    style(
        desc(c(OpticsWavesClass.DIAGRAM), allOf(t(SelectorTag.CIRCLE), c(OpticsWavesClass.SECOND)))
    ) {
        set(CssProperty.FILL, Tokens.REFERENCE.value())
        set(CssProperty.STROKE, Tokens.BG.value())
        setStrokeDasharray(StrokeDasharrayValue.NONE)
    }
    style(
        desc(c(OpticsWavesClass.DIAGRAM), allOf(t(SelectorTag.CIRCLE), c(OpticsWavesClass.SUM)))
    ) {
        set(CssProperty.FILL, Tokens.TEXT.value())
        set(CssProperty.STROKE, Tokens.BG.value())
    }
    style(c(OpticsWavesClass.ACTIONS)) {
        display(DisplayStyle.Flex)
        flexWrap(FlexWrap.Wrap)
        gap((8).px)
        alignItems(AlignItems.Center)
        margin((12).px, 0.px)
    }
    style(c(OpticsWavesClass.STAGES)) {
        display(DisplayStyle.Grid)
        columns(GridTrack.Repeat(2, GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1))))
        gap((8).px)
        margin((14).px, 0.px)
    }
    style(desc(c(OpticsWavesClass.STAGES), c(OpticsWavesClass.CHOICE))) {
        setWhiteSpace(WhiteSpaceValue.NORMAL)
        set(CssProperty.LINE_HEIGHT, 1.45)
        setTextAlign(TextAlignValue.LEFT)
        minWidth(0.px)
    }
    style(
        anyOf(
            desc(c(OpticsWavesClass.ACTIONS), c(OpticsWavesClass.SELECTED)),
            desc(c(OpticsWavesClass.STAGES), c(OpticsWavesClass.SELECTED)),
        )
    ) {
        borderColor(Tokens.ACCENT.value())
        background(Tokens.ACCENT_SOFT.value())
        color(Tokens.ACCENT.value())
        composite(CssProperty.BOX_SHADOW, "inset 0 -2px var(--accent)")
    }
    style(c(OpticsWavesClass.CONTROLS)) {
        display(DisplayStyle.Grid)
        columns(GridTrack.Repeat(2, GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1))))
        gap((16).px)
        margin((16).px, 0.px, (4).px)
    }
    style(child(c(OpticsWavesClass.CONTROLS), t(SelectorTag.DIV))) { minWidth(0.px) }
    style(desc(c(OpticsWavesClass.CONTROLS), t(SelectorTag.LABEL))) {
        set(CssProperty.LINE_HEIGHT, 1.5)
    }
    style(c(OpticsWavesClass.FEEDBACK)) {
        padding((16).px, (18).px)
        edge(CssProperty.BORDER_LEFT, (3).px, LineStyle.Solid, Tokens.ACCENT.value())
        background(Tokens.ACCENT_SOFT.value())
        margin((16).px, 0.px)
        color(Tokens.TEXT.value())
        set(CssProperty.LINE_HEIGHT, 1.65)
    }
    style(desc(c(OpticsWavesClass.FEEDBACK), t(SelectorTag.P))) { margin((8).px, 0.px) }
    style(c(OpticsWavesClass.VALUES)) {
        display(DisplayStyle.Flex)
        gap((8).px, (18).px)
        flexWrap(FlexWrap.Wrap)
        font(FontSpec((14).px, FontStack.MONO, lineHeight = 1.8))
        setFontVariantNumeric(FontVariantNumericValue.TABULAR_NUMS)
    }
    style(desc(c(OpticsWavesClass.VALUES), t(SelectorTag.P))) { margin((4).px, 0.px) }
    style(c(OpticsWavesClass.LEGEND)) {
        display(DisplayStyle.Flex)
        gap((8).px, (18).px)
        flexWrap(FlexWrap.Wrap)
        fontSize((12).px)
        color(Tokens.MUTED.value())
    }
    style(
        anyOf(
            desc(c(OpticsWavesClass.LEGEND), c(OpticsWavesClass.FIRST)),
            desc(c(OpticsWavesClass.VALUES), c(OpticsWavesClass.FIRST)),
        )
    ) {
        color(Tokens.ACCENT.value())
    }
    style(
        anyOf(
            desc(c(OpticsWavesClass.LEGEND), c(OpticsWavesClass.SECOND)),
            desc(c(OpticsWavesClass.VALUES), c(OpticsWavesClass.SECOND)),
        )
    ) {
        color(Tokens.REFERENCE.value())
    }
    style(
        anyOf(
            desc(c(OpticsWavesClass.LEGEND), c(OpticsWavesClass.SUM)),
            desc(c(OpticsWavesClass.VALUES), c(OpticsWavesClass.SUM)),
        )
    ) {
        color(Tokens.TEXT.value())
    }
    style(c(OpticsWavesClass.NOTE)) {
        fontSize((13).px)
        set(CssProperty.LINE_HEIGHT, 1.7)
        color(Tokens.MUTED.value())
        margin((14).px, 0.px)
    }
    style(desc(c(OpticsWavesClass.NOTE), t(SelectorTag.SUMMARY))) {
        minHeight((44).px)
        display(DisplayStyle.ListItem)
        setCursor(CursorValue.POINTER)
        padding((10).px, 0.px)
        color(Tokens.TEXT.value())
    }
    style(c(OpticsWavesClass.REPRODUCTION_WINDOW)) {
        setOverflow(OverflowValue.AUTO)
        maxWidth((100).percent)
        maxHeight((350).px)
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius(Tokens.CONTROL_RADIUS.value())
        background(Tokens.SURFACE_RAISED.value())
        setOverscrollBehavior(OverscrollBehaviorValue.CONTAIN)
    }
    style(
        child(
            desc(c(LabClass.COURSE_VISUAL), c(OpticsWavesClass.REPRODUCTION_WINDOW)),
            c(OpticsWavesClass.REPRODUCTION_IMAGE),
        )
    ) {
        composite(CssProperty.WIDTH, "calc(100% * var(--reproduction-scale,1))")
        setMaxWidth(MaxWidthValue.NONE)
        height(auto)
        display(DisplayStyle.Block)
    }
    media(CSSMediaQuery.MediaFeature("min-width", (1200).px)) {
        style(c(OpticsWavesClass.STAGES)) {
            columns(
                GridTrack.Repeat(4, GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1)))
            )
        }
    }
    media(CSSMediaQuery.MediaFeature("max-width", (600).px)) {
        style(c(OpticsWavesClass.CONTROLS)) {
            columns(GridTrack.Fraction(1))
            gap((8).px)
        }
        style(c(OpticsWavesClass.PANEL)) { padding((14).px) }
        style(c(OpticsWavesClass.FEEDBACK)) { padding((14).px) }
        style(desc(c(OpticsWavesClass.ACTIONS), t(SelectorTag.BUTTON))) {
            maxWidth((100).percent)
            setWhiteSpace(WhiteSpaceValue.NORMAL)
        }
        style(desc(c(OpticsWavesClass.STAGES), c(OpticsWavesClass.CHOICE))) {
            fontSize((12).px)
            padding((10).px)
        }
        style(c(OpticsWavesClass.NOTE)) { fontSize((13).px) }
        style(c(OpticsWavesClass.BLOCK)) { paddingTop((18).px) }
    }
    media(DesignMedia.PREFERS_REDUCED_MOTION_NO_PREFERENCE.query) {
        style(
            anyOf(
                desc(c(OpticsWavesClass.PART), c(OpticsWavesClass.PATH)),
                desc(c(OpticsWavesClass.PART), c(OpticsWavesClass.DIRECTION)),
                desc(c(OpticsWavesClass.PART), c(OpticsWavesClass.SURFACE)),
                desc(c(OpticsWavesClass.PART), c(OpticsWavesClass.LIGHT)),
                desc(c(OpticsWavesClass.PART), c(OpticsWavesClass.SLIT)),
                desc(c(OpticsWavesClass.PART), c(OpticsWavesClass.TEXT)),
            )
        ) {
            composite(
                CssProperty.TRANSITION,
                "stroke .18s ease,fill .18s ease,stroke-width .18s ease",
            )
        }
        style(desc(desc(c(OpticsWavesClass.STILL), c(OpticsWavesClass.PART)), universal)) {
            setTransition(TransitionValue.NONE)
        }
    }
    media(DesignMedia.PREFERS_REDUCED_MOTION_REDUCE.query) {
        style(desc(c(OpticsWavesClass.INVESTIGATION), universal)) {
            important { setAnimation(AnimationValue.NONE) }
            important { setTransition(TransitionValue.NONE) }
        }
    }
}
