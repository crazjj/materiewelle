@file:OptIn(org.jetbrains.compose.web.ExperimentalComposeWebApi::class)

package materiewelle.presentation.styles

import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.css.*

internal fun StyleRules.slitPhaseStyles() {
    style(c(SlitPhaseClass.ROOT)) { minWidth(0.px); padding(16.px, 0.px) }
    style(c(SlitPhaseClass.CONTROLS)) {
        display(DisplayStyle.Flex); flexWrap(FlexWrap.Wrap); gap(10.px); margin(12.px, 0.px, 22.px)
    }
    style(desc(c(SlitPhaseClass.CONTROLS), t(SelectorTag.BUTTON))) { minHeight(44.px); setWhiteSpace(WhiteSpaceValue.NORMAL) }
    style(c(SlitPhaseClass.SELECTED)) {
        background(Tokens.ACCENT_SOFT.value()); borderColor(Tokens.ACCENT.value())
        edge(CssProperty.BORDER_BOTTOM, 3.px, LineStyle.Solid, Tokens.ACCENT.value())
    }
    style(c(SlitPhaseClass.LAYOUT)) {
        display(DisplayStyle.Grid)
        columns(GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1)), GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1)))
        gap(28.px); alignItems(AlignItems.Start)
    }
    style(c(SlitPhaseClass.FIGURE)) { minWidth(0.px); margin(0.px) }
    style(desc(c(SlitPhaseClass.FIGURE), t(SelectorTag.SVG))) { display(DisplayStyle.Block); width(100.percent); maxWidth(500.px) }
    style(c(SlitPhaseClass.READOUT)) {
        minWidth(0.px); margin(0.px); padding(0.px, 0.px, 12.px)
        edge(CssProperty.BORDER_BOTTOM, 1.px, LineStyle.Solid, Tokens.BORDER.value())
    }
    style(c(SlitPhaseClass.READOUT_TERM)) { fontSize(15.px); color(Tokens.MUTED.value()); margin(14.px, 0.px, 5.px) }
    style(c(SlitPhaseClass.READOUT_VALUE)) { margin(0.px); fontSize(21.px) }
    style(c(SlitPhaseClass.NOTE)) { maxWidth(820.px); lineHeight("1.6"); margin(18.px, 0.px) }
    style(c(SlitPhaseClass.LEGEND)) { fontSize(15.px); lineHeight("1.6"); maxWidth(500.px) }
    media(CSSMediaQuery.MediaFeature("max-width", 900.px)) {
        style(c(SlitPhaseClass.LAYOUT)) { columns(GridTrack.Fraction(1)); gap(12.px) }
    }
}
