@file:OptIn(org.jetbrains.compose.web.ExperimentalComposeWebApi::class)

package materiewelle.presentation.styles

import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.css.*

internal fun StyleRules.dgLessonStyles() {
    style(c(DgLessonClass.HEADER)) { maxWidth(850.px); marginBottom(28.px) }
    style(desc(c(DgLessonClass.HEADER), t(SelectorTag.H1))) { fontSize(38.px); margin(8.px, 0.px, 12.px) }
    style(desc(c(DgLessonClass.HEADER), t(SelectorTag.P))) { margin(0.px); color(Tokens.MUTED.value()); fontSize(16.px) }
    style(c(DgLessonClass.NAV)) {
        position(Position.Sticky); composite(CssProperty.TOP, "var(${DgLessonProperty.NAV_TOP.cssName}, 68px)"); zIndex(10); background(Tokens.BG.value())
        display(DisplayStyle.Flex); alignItems(AlignItems.Center); flexWrap(FlexWrap.Wrap)
        gap(8.px, 20.px); padding(0.px, 0.px, 16.px); marginBottom(20.px)
        edge(CssProperty.BORDER_BOTTOM, 1.px, LineStyle.Solid, Tokens.BORDER.value())
    }
    style(desc(c(DgLessonClass.NAV), t(SelectorTag.A))) {
        display(DisplayStyle.LegacyInlineFlex); alignItems(AlignItems.Center); gap(8.px)
        minHeight(44.px); fontSize(14.px); color(Tokens.MUTED.value()); textDecoration("none")
    }
    style(desc(c(DgLessonClass.NAV), c(DgLessonClass.ACTIVE))) { color(Tokens.ACCENT.value()); fontWeight(600) }
    style(c(DgLessonClass.NUMBER)) { font(FontSpec(12.px, FontStack.MONO)); color(Tokens.MUTED.value()) }
    style(c(DgLessonClass.STAGE)) { padding(4.px, 0.px, 24.px); minWidth(0.px) }
    style(c(DgLessonClass.META)) { fontSize(13.px); color(Tokens.MUTED.value()); margin(0.px, 0.px, 8.px) }
    style(desc(c(DgLessonClass.STAGE), t(SelectorTag.H2))) { fontSize(28.px); margin(0.px, 0.px, 24.px); lineHeight("1.25") }
    style(c(DgLessonClass.SCENE)) {
        display(DisplayStyle.Grid); columns(GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1.65)), GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1)))
        gap(36.px); alignItems(AlignItems.Start)
    }
    style(c(DgLessonClass.FIGURE)) {
        position(Position.Sticky); composite(CssProperty.TOP, "var(${DgLessonProperty.FIGURE_TOP.cssName}, 136px)")
        margin(0.px); padding(20.px); borderRadius(6.px); background(Tokens.SURFACE.value()); minWidth(0.px)
    }
    style(desc(c(DgLessonClass.FIGURE), t(SelectorTag.FIGCAPTION))) { marginTop(12.px); color(Tokens.MUTED.value()); fontSize(14.px); lineHeight("1.45") }
    style(desc(c(DgLessonClass.EXPLANATION), t(SelectorTag.P))) { margin(0.px, 0.px, 16.px); fontSize(18.px) }
    style(desc(c(DgLessonClass.EXPLANATION), t(SelectorTag.H3))) { fontSize(18.px); margin(0.px, 0.px, 14.px) }
    style(c(DgLessonClass.COMPLETE_SCENE)) { columns(GridTrack.Fraction(1)); gap(24.px) }
    style(desc(c(DgLessonClass.COMPLETE_SCENE), c(DgLessonClass.FIGURE))) {
        position(Position.Static); maxWidth(800.px); width(100.percent); composite(CssProperty.MARGIN, "0 auto")
    }
    style(c(DgLessonClass.SETUP_OVERVIEW)) {
        display(DisplayStyle.Grid)
        columns(GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1)), GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1)), GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1)))
        gap(24.px)
    }
    style(c(DgLessonClass.FIGURE_ACTIONS)) { display(DisplayStyle.Flex); flexDirection(FlexDirection.Column); gap(10.px); marginBottom(16.px) }
    style(desc(c(DgLessonClass.FIGURE_ACTIONS), t(SelectorTag.DIV))) { display(DisplayStyle.Flex); flexWrap(FlexWrap.Wrap); gap(8.px) }
    style(desc(c(DgLessonClass.FIGURE_ACTIONS), t(SelectorTag.BUTTON))) { minHeight(44.px); fontSize(16.px); setWhiteSpace(WhiteSpaceValue.NORMAL) }
    style(desc(c(DgLessonClass.FIGURE_ACTIONS), i(DgLessonId.NEXT))) { minHeight(64.px); width(100.percent); lineHeight("1.25") }
    style(c(DgLessonClass.EXPLANATION_STEP)) {
        marginBottom(20.px); paddingLeft(14.px)
        edge(CssProperty.BORDER_LEFT, 2.px, LineStyle.Solid, Tokens.BORDER.value())
    }
    style(c(DgLessonClass.EXPLANATION_ACTIVE)) { edge(CssProperty.BORDER_LEFT, 2.px, LineStyle.Solid, Tokens.ACCENT.value()) }
    style(desc(desc(i(DgArticleId.BRAGG), c(DgLessonClass.FIGURE)), t(SelectorTag.SVG))) {
        width(100.percent); maxWidth(400.px); display(DisplayStyle.Block); composite(CssProperty.MARGIN, "0 auto")
    }
    style(c(DgLessonClass.FOOTER)) {
        display(DisplayStyle.Flex); justifyContent(JustifyContent.SpaceBetween); alignItems(AlignItems.Center)
        gap(16.px); marginTop(30.px); paddingTop(22.px)
        edge(CssProperty.BORDER_TOP, 1.px, LineStyle.Solid, Tokens.BORDER.value())
    }
    style(c(DgLessonClass.SOURCE_CASE)) { display(DisplayStyle.Flex); gap(36.px); flexWrap(FlexWrap.Wrap); margin(20.px, 0.px) }
    style(c(DgLessonClass.QUANTITY)) { fontSize(30.px); color(Tokens.ACCENT.value()) }
    style(c(DgLessonClass.CALCULATIONS)) { display(DisplayStyle.Grid); columns(GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1)), GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1))); gap(28.px); alignItems(AlignItems.Start) }
    style(c(DgLessonClass.CALCULATION)) { minWidth(0.px); paddingTop(14.px); edge(CssProperty.BORDER_TOP, 2.px, LineStyle.Solid, Tokens.BORDER.value()) }
    style(desc(c(DgLessonClass.CALCULATION), t(SelectorTag.H3))) { fontSize(20.px); margin(0.px, 0.px, 12.px); minHeight(52.px) }
    style(c(DgLessonClass.CALCULATION_INTRO)) { minHeight(88.px); margin(0.px, 0.px, 12.px) }
    style(c(DgLessonClass.CALCULATION_FORMULA)) { minHeight(88.px); margin(8.px, 0.px) }
    style(c(DgLessonClass.CALCULATION_ENTRY)) { minHeight(132.px); display(DisplayStyle.Flex); flexDirection(FlexDirection.Column); justifyContent(JustifyContent.FlexStart); marginBottom(12.px) }
    style(desc(c(DgLessonClass.CALCULATION_ENTRY), t(SelectorTag.LABEL))) { minHeight(48.px); margin(0.px) }
    style(desc(c(DgLessonClass.CALCULATION_ENTRY), t(SelectorTag.TEXTAREA))) { minHeight(76.px); margin(8.px, 0.px, 0.px) }
    style(desc(c(DgLessonClass.CALCULATION_ENTRY), t(SelectorTag.INPUT))) { width(100.percent); minHeight(76.px); margin(8.px, 0.px, 0.px) }
    style(c(DgLessonClass.RESULT)) { marginTop(18.px); padding(14.px, 0.px); color(Tokens.ACCENT.value()); fontSize(24.px) }
    style(c(DgLessonClass.NOTE)) { maxWidth(760.px); marginTop(24.px) }
    media(CSSMediaQuery.MediaFeature("max-width", 1050.px)) {
        style(c(DgLessonClass.SCENE)) { gap(24.px); columns(GridTrack.Fraction(1.45), GridTrack.Fraction(1)) }
        style(c(DgLessonClass.FIGURE)) { padding(12.px) }
        style(c(DgLessonClass.COMPLETE_SCENE)) { columns(GridTrack.Fraction(1)) }
        style(c(DgLessonClass.NAV)) { gap(6.px, 16.px) }
    }
    media(CSSMediaQuery.MediaFeature("max-width", 850.px)) {
        style(c(DgLessonClass.NAV)) { position(Position.Static) }
        style(c(DgLessonClass.FIGURE)) { position(Position.Static) }
    }
    media(CSSMediaQuery.MediaFeature("max-height", 600.px)) {
        style(c(DgLessonClass.NAV)) { position(Position.Static) }
        style(c(DgLessonClass.FIGURE)) { position(Position.Static) }
    }
    media(CSSMediaQuery.MediaFeature("max-width", 760.px)) {
        style(anyOf(c(DgLessonClass.SCENE), c(DgLessonClass.CALCULATIONS), c(DgLessonClass.SETUP_OVERVIEW))) { columns(GridTrack.Fraction(1)) }
        style(anyOf(c(DgLessonClass.CALCULATION_INTRO), c(DgLessonClass.CALCULATION_FORMULA), c(DgLessonClass.CALCULATION_ENTRY))) { minHeight(0.px) }
    }
}
