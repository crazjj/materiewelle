package materiewelle.presentation.styles

import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.css.keywords.auto

internal fun StyleRules.overviewStyles() {
    style(c(OverviewClass.ROOT)) {
        maxWidth(1160.px); composite(CssProperty.MARGIN, "0 auto"); padding(30.px, 16.px, 48.px)
    }
    style(c(OverviewClass.INTRO)) {
        display(DisplayStyle.Grid)
        columns(GridTrack.Fraction(1.15), GridTrack.Fraction(1))
        gap(36.px); alignItems(AlignItems.Center)
    }
    style(desc(c(OverviewClass.ROOT), t(SelectorTag.H1))) {
        fontSize(48.px); lineHeight("1.12"); margin(8.px, 0.px, 20.px); maxWidth(650.px)
    }
    style(c(OverviewClass.LEAD)) { fontSize(21.px); lineHeight("1.5"); maxWidth(640.px) }
    style(desc(c(OverviewClass.ROOT), t(SelectorTag.P))) { lineHeight("1.55") }
    style(c(OverviewClass.HELP_CONTEXT)) { fontSize(15.px); color(Tokens.MUTED.value()); marginTop(8.px) }
    style(c(OverviewClass.IMAGE)) { margin(0.px) }
    style(desc(c(OverviewClass.IMAGE), t(SelectorTag.IMG))) {
        display(DisplayStyle.Block); width(100.percent); height(auto)
        border(1.px, LineStyle.Solid, Tokens.BORDER.value())
    }
    style(desc(c(OverviewClass.IMAGE), t(SelectorTag.FIGCAPTION))) {
        fontSize(16.px); lineHeight("1.45"); color(Tokens.MUTED.value()); marginTop(12.px)
    }
    style(c(OverviewClass.ACTIONS)) {
        display(DisplayStyle.Flex); flexWrap(FlexWrap.Wrap); gap(12.px); marginTop(22.px)
    }
    style(desc(c(OverviewClass.ROOT), t(SelectorTag.A))) { minHeight(44.px) }
    style(c(OverviewClass.CHAPTERS)) {
        display(DisplayStyle.Grid)
        columns(GridTrack.Fraction(1), GridTrack.Fraction(1))
        gap(0.px, 36.px); padding(0.px); margin(36.px, 0.px, 24.px)
        composite(CssProperty.LIST_STYLE, "none")
    }
    style(c(OverviewClass.CHAPTER)) {
        display(DisplayStyle.Grid); columns(GridTrack.Fixed(2.1.em), GridTrack.Fraction(1))
        gap(12.px); padding(20.px, 0.px)
        edge(CssProperty.BORDER_TOP, 1.px, LineStyle.Solid, Tokens.BORDER.value())
    }
    style(desc(c(OverviewClass.CHAPTER), t(SelectorTag.A))) {
        display(DisplayStyle.LegacyInlineFlex); alignItems(AlignItems.Center); fontSize(23.px)
    }
    style(desc(c(OverviewClass.CHAPTER), t(SelectorTag.P))) {
        margin(0.px); color(Tokens.MUTED.value())
    }
    style(c(OverviewClass.NUMBER)) { color(Tokens.MUTED.value()); paddingTop(12.px) }
    style(c(OverviewClass.SUPPORT)) {
        edge(CssProperty.BORDER_TOP, 1.px, LineStyle.Solid, Tokens.BORDER.value())
        paddingTop(18.px); maxWidth(850.px)
    }
    style(desc(c(OverviewClass.SUPPORT), t(SelectorTag.H2))) {
        fontSize(25.px); lineHeight("1.3"); margin(0.px, 0.px, 16.px)
    }
    style(desc(c(OverviewClass.SUPPORT), t(SelectorTag.LI))) {
        lineHeight("1.55"); marginBottom(12.px)
    }
    media(CSSMediaQuery.MediaFeature("max-width", 850.px)) {
        style(c(OverviewClass.INTRO)) { columns(GridTrack.Fraction(1)); gap(24.px) }
        style(c(OverviewClass.CHAPTERS)) { columns(GridTrack.Fraction(1)) }
        style(desc(c(OverviewClass.ROOT), t(SelectorTag.H1))) { fontSize(38.px) }
    }
}
