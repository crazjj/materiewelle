@file:OptIn(org.jetbrains.compose.web.ExperimentalComposeWebApi::class)

package materiewelle.presentation.styles

import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.css.*

/** Readable A/B comparison; a scoped Kotlin rule tree, inheriting both laboratory themes. */
internal fun StyleRules.measurementComparisonStyles() {
    style(c(MeasurementComparisonClass.ROOT)) {
        minWidth(0.px)
        edge(CssProperty.BORDER_TOP, 1.px, LineStyle.Solid, Tokens.BORDER.value())
        marginTop(24.px)
        paddingTop(22.px)
    }
    style(desc(c(MeasurementComparisonClass.ROOT), t(SelectorTag.H3))) {
        font(FontSpec(19.px, FontStack.TEXT, weight = 600, lineHeight = 1.4))
        margin(0.px, 0.px, 10.px)
    }
    style(desc(c(MeasurementComparisonClass.ROOT), t(SelectorTag.H4))) {
        font(FontSpec(15.px, FontStack.TEXT, weight = 600, lineHeight = 1.5))
        margin(0.px, 0.px, 10.px)
    }
    style(desc(c(MeasurementComparisonClass.ROOT), t(SelectorTag.P))) {
        fontSize(16.px)
        set(CssProperty.LINE_HEIGHT, 1.65)
        margin(8.px, 0.px)
        setOverflowWrap(OverflowWrapValue.ANYWHERE)
    }
    style(c(MeasurementComparisonClass.HEADER)) { marginBottom(18.px) }
    style(c(MeasurementComparisonClass.GRID)) {
        display(DisplayStyle.Grid)
        columns(GridTrack.Repeat(2, GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1))))
        gap(16.px)
        alignItems(AlignItems.Start)
    }
    style(c(MeasurementComparisonClass.CARD)) {
        minWidth(0.px)
        padding(18.px)
        border(1.px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius(Tokens.RADIUS.value())
        background(Tokens.SURFACE.value())
    }
    style(allOf(c(MeasurementComparisonClass.CARD), c(MeasurementComparisonClass.CURRENT))) {
        edge(CssProperty.BORDER_TOP, 3.px, LineStyle.Solid, Tokens.ACCENT.value())
        paddingTop(16.px)
    }
    style(c(MeasurementComparisonClass.EMPTY)) { background(Tokens.BG.value()) }
    style(c(MeasurementComparisonClass.CASE_LABEL)) { color(Tokens.ACCENT.value()) }
    style(c(MeasurementComparisonClass.MEAN)) {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        gap(6.px)
        margin(16.px, 0.px)
        padding(13.px, 14.px)
        background(Tokens.SURFACE_RAISED.value())
        borderRadius(6.px)
        font(FontSpec(15.px, FontStack.TEXT, lineHeight = 1.5))
        color(Tokens.MUTED.value())
    }
    style(child(c(MeasurementComparisonClass.MEAN), t(SelectorTag.STRONG))) {
        font(FontSpec(25.px, FontStack.MONO, weight = 500, lineHeight = 1.35))
        setFontVariantNumeric(FontVariantNumericValue.TABULAR_NUMS)
        color(Tokens.TEXT.value())
    }
    style(c(MeasurementComparisonClass.FIELD)) { margin(16.px, 0.px) }
    style(desc(c(MeasurementComparisonClass.CARD), c(MeasurementComparisonClass.FIELD_LABEL))) {
        fontWeight(600)
        color(Tokens.MUTED.value())
        fontSize(14.px)
        margin(0.px, 0.px, 5.px)
    }
    style(c(MeasurementComparisonClass.COORDINATES)) {
        font(FontSpec(13.px, FontStack.MONO, lineHeight = 1.8))
        setFontVariantNumeric(FontVariantNumericValue.TABULAR_NUMS)
        setOverflowWrap(OverflowWrapValue.ANYWHERE)
    }
    style(c(MeasurementComparisonClass.INTERVALS)) {
        setListStyle(ListStyleValue.NONE)
        padding(0.px)
        margin(8.px, 0.px, 0.px)
        display(DisplayStyle.Grid)
        columns(GridTrack.AutoFit(GridTrack.MinMax(GridTrack.Fixed(66.px), GridTrack.Fraction(1))))
        gap(6.px)
    }
    style(desc(c(MeasurementComparisonClass.INTERVALS), t(SelectorTag.LI))) {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        alignItems(AlignItems.Center)
        gap(4.px)
        border(1.px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius(5.px)
        padding(9.px, 5.px)
        background(Tokens.BG.value())
        font(FontSpec(11.px, FontStack.MONO, lineHeight = 1.5))
        setFontVariantNumeric(FontVariantNumericValue.TABULAR_NUMS)
        color(Tokens.MUTED.value())
    }
    style(desc(c(MeasurementComparisonClass.INTERVALS), t(SelectorTag.STRONG))) {
        fontSize(15.px)
        fontWeight(500)
        color(Tokens.TEXT.value())
    }
    style(c(MeasurementComparisonClass.JUSTIFICATION)) {
        setWhiteSpace(WhiteSpaceValue.PRE_WRAP)
        setOverflowWrap(OverflowWrapValue.ANYWHERE)
    }
    style(c(MeasurementComparisonClass.CONTACT)) {
        edge(CssProperty.BORDER_TOP, 1.px, LineStyle.Solid, Tokens.BORDER.value())
        paddingTop(12.px)
        marginTop(16.px)
    }
    style(desc(c(MeasurementComparisonClass.CONTACT), t(SelectorTag.P))) { fontSize(14.px) }
    style(c(MeasurementComparisonClass.CONTACT_NOTE)) {
        color(Tokens.MUTED.value())
        padding(8.px, 0.px)
    }
    style(c(MeasurementComparisonClass.DIFFERENCE)) {
        margin(18.px, 0.px)
        padding(16.px, 18.px)
        background(Tokens.SURFACE_RAISED.value())
        border(1.px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius(Tokens.RADIUS.value())
    }
    style(c(MeasurementComparisonClass.CHANGES)) {
        margin(8.px, 0.px)
        paddingLeft(22.px)
        font(FontSpec(13.px, FontStack.MONO, lineHeight = 1.8))
        setFontVariantNumeric(FontVariantNumericValue.TABULAR_NUMS)
    }
    style(c(MeasurementComparisonClass.WARNING)) {
        edge(CssProperty.BORDER_LEFT, 2.px, LineStyle.Solid, Tokens.REFERENCE.value())
        paddingLeft(12.px)
        color(Tokens.TEXT.value())
    }
    style(c(MeasurementComparisonClass.DETAILS)) {
        margin(14.px, 0.px)
        padding(12.px, 15.px)
        border(1.px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius(6.px)
        background(Tokens.SURFACE.value())
    }
    style(
        anyOf(
            child(c(MeasurementComparisonClass.DETAILS), t(SelectorTag.SUMMARY)),
            child(c(MeasurementComparisonClass.IMAGE_ID), t(SelectorTag.SUMMARY)),
        )
    ) {
        minHeight(44.px)
        padding(9.px, 0.px)
        font(FontSpec(16.px, FontStack.TEXT, weight = 500, lineHeight = 1.6))
        setCursor(CursorValue.POINTER)
    }
    style(c(MeasurementComparisonClass.IMAGE_ID)) {
        edge(CssProperty.BORDER_TOP, 1.px, LineStyle.Solid, Tokens.BORDER.value())
        marginTop(14.px)
        paddingTop(5.px)
        color(Tokens.MUTED.value())
    }
    style(desc(c(MeasurementComparisonClass.IMAGE_ID), t(SelectorTag.P))) {
        font(FontSpec(11.px, FontStack.MONO, lineHeight = 1.8))
        setOverflowWrap(OverflowWrapValue.ANYWHERE)
    }
    style(desc(c(MeasurementComparisonClass.ORIGIN), c(MeasurementComparisonClass.CARD))) {
        marginTop(12.px)
    }
    media(CSSMediaQuery.MediaFeature("max-width", 760.px)) {
        style(c(MeasurementComparisonClass.GRID)) { columns(GridTrack.Fraction(1)) }
        style(c(MeasurementComparisonClass.CARD)) { padding(14.px) }
        style(allOf(c(MeasurementComparisonClass.CARD), c(MeasurementComparisonClass.CURRENT))) {
            paddingTop(12.px)
        }
        style(c(MeasurementComparisonClass.DIFFERENCE)) { padding(14.px) }
        style(c(MeasurementComparisonClass.DETAILS)) { padding(10.px, 12.px) }
    }
    media(DesignMedia.PREFERS_REDUCED_MOTION_REDUCE.query) {
        style(desc(c(MeasurementComparisonClass.ROOT), universal)) {
            important { setAnimation(AnimationValue.NONE) }
            important { setTransition(TransitionValue.NONE) }
        }
    }
}
