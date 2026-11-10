@file:OptIn(org.jetbrains.compose.web.ExperimentalComposeWebApi::class)

package materiewelle.presentation.styles

import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.css.*

internal fun StyleRules.derivationPracticeStyles() {
    style(c(DerivationPracticeClass.ROOT)) {
        maxWidth(860.px); padding(16.px, 0.px)
        edge(CssProperty.BORDER_TOP, 1.px, LineStyle.Solid, Tokens.BORDER.value())
    }
    style(c(DerivationPracticeClass.EQUATION)) { margin(18.px, 0.px); fontSize(23.px) }
    style(c(DerivationPracticeClass.GAP)) {
        edge(CssProperty.BORDER_LEFT, 3.px, LineStyle.Solid, Tokens.ACCENT.value())
        padding(10.px, 14.px); color(Tokens.INK.value())
    }
    style(c(DerivationPracticeClass.ACTIONS)) {
        display(DisplayStyle.Flex); flexWrap(FlexWrap.Wrap); gap(8.px, 12.px); margin(18.px, 0.px)
    }
    style(c(DerivationPracticeClass.CHOICES)) {
        display(DisplayStyle.Flex); flexWrap(FlexWrap.Wrap); gap(12.px); margin(12.px, 0.px)
    }
    style(c(DerivationPracticeClass.CHOICE)) {
        minHeight(56.px); padding(12.px, 16.px); fontSize(18.px)
    }
    style(c(DerivationPracticeClass.SELECTED)) {
        background(Tokens.ACCENT_SOFT.value()); borderColor(Tokens.ACCENT.value())
        edge(CssProperty.BORDER_BOTTOM, 3.px, LineStyle.Solid, Tokens.ACCENT.value())
    }
    style(c(DerivationPracticeClass.HELP)) { margin(16.px, 0.px) }
    style(c(DerivationPracticeClass.EXPLANATION)) {
        padding(16.px, 0.px); edge(CssProperty.BORDER_TOP, 1.px, LineStyle.Solid, Tokens.BORDER.value())
    }
    style(desc(c(DerivationPracticeClass.ROOT), t(SelectorTag.TEXTAREA))) {
        width(100.percent); margin(8.px, 0.px, 18.px)
    }
}
