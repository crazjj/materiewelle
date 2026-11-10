@file:OptIn(org.jetbrains.compose.web.ExperimentalComposeWebApi::class)

package materiewelle.presentation.styles

import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.css.*

internal fun StyleRules.dgComparisonStyles() {
    style(c(DgComparisonClass.ROOT)) { margin(24.px, 0.px); minWidth(0.px) }
    style(c(DgComparisonClass.CONTROLS)) {
        display(DisplayStyle.Flex); flexWrap(FlexWrap.Wrap); gap(10.px)
        margin(16.px, 0.px)
    }
    style(desc(c(DgComparisonClass.CONTROLS), t(SelectorTag.BUTTON))) {
        minHeight(44.px); fontSize(15.px); setWhiteSpace(WhiteSpaceValue.NORMAL)
    }
    style(c(DgComparisonClass.ACTIVE)) {
        borderColor(Tokens.REFERENCE.value()); background(Tokens.ACCENT_SOFT.value())
        edge(CssProperty.OUTLINE, 2.px, LineStyle.Solid, Tokens.REFERENCE.value()); outlineOffset(2.px)
    }
    style(c(DgComparisonClass.FIGURES)) {
        display(DisplayStyle.Grid)
        columns(GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1)), GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1)))
        gap(16.px); alignItems(AlignItems.Start)
    }
    style(c(DgComparisonClass.FIGURE)) {
        minWidth(0.px); margin(0.px); padding(12.px)
        background(Tokens.SURFACE.value()); border(1.px, LineStyle.Solid, Tokens.BORDER.value()); borderRadius(6.px)
    }
    style(desc(c(DgComparisonClass.FIGURE), t(SelectorTag.H3))) { margin(4.px, 0.px, 16.px); fontSize(20.px) }
    style(c(DgComparisonClass.DRAWING)) { maxWidth(470.px); composite(CssProperty.MARGIN, "0 auto") }
    style(desc(c(DgComparisonClass.DRAWING), t(SelectorTag.SVG))) { width(100.percent); display(DisplayStyle.Block) }
    style(desc(c(DgComparisonClass.CAPTION), t(SelectorTag.P))) { fontSize(16.px); lineHeight("1.6"); margin(12.px, 0.px) }
    style(c(DgComparisonClass.FORMULA)) { fontSize(23.px); margin(16.px, 0.px); minHeight(48.px) }
    style(c(DgComparisonClass.TRIAL)) { maxWidth(900.px); padding(16.px, 0.px); minWidth(0.px) }
    style(c(DgComparisonClass.FIELDS)) {
        display(DisplayStyle.Grid); columns(GridTrack.Fraction(1), GridTrack.Fraction(1))
        gap(20.px); margin(20.px, 0.px)
    }
    style(c(DgComparisonClass.FIELD)) { minWidth(0.px) }
    style(desc(c(DgComparisonClass.TRIAL), t(SelectorTag.LABEL))) { display(DisplayStyle.Block); marginBottom(8.px); fontSize(16.px) }
    style(desc(c(DgComparisonClass.FIELD), t(SelectorTag.SELECT))) {
        width(100.percent); minHeight(48.px); padding(10.px)
        color(Tokens.TEXT.value()); background(Tokens.SURFACE.value())
        border(1.px, LineStyle.Solid, Tokens.BORDER.value()); borderRadius(5.px)
        font(FontSpec(16.px, FontStack.TEXT))
    }
    style(c(DgComparisonClass.FEEDBACK)) {
        padding(12.px, 16.px); margin(16.px, 0.px)
        edge(CssProperty.BORDER_LEFT, 3.px, LineStyle.Solid, Tokens.ACCENT.value())
        background(Tokens.ACCENT_SOFT.value())
    }
    media(CSSMediaQuery.MediaFeature("max-width", 850.px)) {
        style(c(DgComparisonClass.FIGURES)) { columns(GridTrack.Fraction(1)) }
    }
    media(CSSMediaQuery.MediaFeature("max-width", 850.px)) {
        style(c(DgComparisonClass.FIELDS)) { columns(GridTrack.Fraction(1)) }
    }
}
