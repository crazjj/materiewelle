package materiewelle.presentation.styles

import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.css.*

internal fun StyleRules.ownWorkSummaryStyles() {
    style(c(OwnWorkClass.ROOT)) { margin(16.px, 0.px); maxWidth(1000.px) }
    style(c(OwnWorkClass.LIST)) { margin(12.px, 0.px); padding(0.px); setListStyle(ListStyleValue.NONE) }
    style(c(OwnWorkClass.ROW)) {
        display(DisplayStyle.Grid)
        columns(GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fixed(160.px)), GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1)))
        gap(8.px, 16.px); padding(6.px, 0.px)
        edge(CssProperty.BORDER_TOP, 1.px, LineStyle.Solid, Tokens.BORDER.value())
        alignItems(AlignItems.Start)
    }
    style(c(OwnWorkClass.LINK)) {
        display(DisplayStyle.LegacyInlineFlex); alignItems(AlignItems.Center)
        minHeight(44.px); color(Tokens.ACCENT.value()); fontWeight(600)
    }
    style(c(OwnWorkClass.BODY)) {
        minWidth(0.px); paddingTop(8.px)
        display(DisplayStyle.Grid)
        columns(GridTrack.Fixed(160.px), GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1)))
        gap(12.px)
    }
    style(c(OwnWorkClass.STATUS)) { color(Tokens.TEXT.value()); fontWeight(500) }
    style(c(OwnWorkClass.DETAIL)) { margin(0.px, 0.px, 4.px); color(Tokens.MUTED.value()); fontSize(15.px) }
    media(CSSMediaQuery.MediaFeature("max-width", 760.px)) {
        style(c(OwnWorkClass.ROW)) { columns(GridTrack.Fraction(1)); gap(0.px) }
        style(c(OwnWorkClass.BODY)) { paddingTop(0.px); columns(GridTrack.Fraction(1)); gap(6.px) }
    }
}
