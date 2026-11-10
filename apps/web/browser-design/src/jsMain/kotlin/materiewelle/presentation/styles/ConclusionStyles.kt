package materiewelle.presentation.styles

import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.css.*

/** One continuous argument; historical stages read as rows, not a second card dashboard. */
internal fun StyleRules.conclusionStyles() {
    style(c(ConclusionClass.SYNTHESIS)) { maxWidth(1040.px); marginTop(28.px) }
    style(c(ConclusionClass.CHAIN)) { margin(16.px, 0.px); padding(0.px); setListStyle(ListStyleValue.NONE) }
    style(c(ConclusionClass.STEP)) {
        display(DisplayStyle.Grid)
        columns(GridTrack.Fixed(220.px), GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1)))
        gap(24.px); padding(18.px, 0.px)
        edge(CssProperty.BORDER_TOP, 1.px, LineStyle.Solid, Tokens.BORDER.value())
        alignItems(AlignItems.Start)
    }
    style(desc(c(ConclusionClass.STEP), t(SelectorTag.H3))) { margin(0.px, 0.px, 6.px); fontSize(20.px); lineHeight("1.3") }
    style(desc(c(ConclusionClass.STEP), t(SelectorTag.P))) { margin(0.px) }
    style(c(ConclusionClass.STATEMENT)) {
        margin(26.px, 0.px, 18.px); paddingLeft(18.px); fontSize(21.px)
        edge(CssProperty.BORDER_LEFT, 2.px, LineStyle.Solid, Tokens.ACCENT.value())
    }
    style(c(ConclusionClass.ACTIONS)) {
        display(DisplayStyle.Flex); flexWrap(FlexWrap.Wrap); gap(12.px)
        alignItems(AlignItems.Center); margin(20.px, 0.px)
    }
    style(desc(c(ConclusionClass.ACTIONS), c(ArticleClass.ACTIONS))) { margin(0.px) }
    style(desc(c(ConclusionClass.ACTIONS), t(SelectorTag.A))) { margin(0.px) }
    media(CSSMediaQuery.MediaFeature("max-width", 1050.px)) {
        style(c(ConclusionClass.STEP)) {
            columns(GridTrack.Fixed(190.px), GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1)))
            gap(22.px)
        }
    }
    media(CSSMediaQuery.MediaFeature("max-width", 760.px)) {
        style(c(ConclusionClass.STEP)) { columns(GridTrack.Fraction(1)); gap(10.px) }
    }
}
