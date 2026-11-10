@file:OptIn(org.jetbrains.compose.web.ExperimentalComposeWebApi::class)
package materiewelle.presentation.styles

import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.css.keywords.auto

internal fun StyleRules.slitDetectionStyles() {
    style(c(SlitDetectionClass.ROOT)) { margin(24.px, 0.px); minWidth(0.px) }
    style(c(SlitDetectionClass.ACTIONS)) { display(DisplayStyle.Flex); flexWrap(FlexWrap.Wrap); gap(10.px); margin(16.px, 0.px) }
    style(desc(c(SlitDetectionClass.ACTIONS), t(SelectorTag.BUTTON))) { minHeight(44.px); fontSize(15.px) }
    style(c(SlitDetectionClass.CANVAS)) {
        display(DisplayStyle.Block); width(100.percent); height(auto); minWidth(0.px)
        border(1.px, LineStyle.Solid, Tokens.BORDER.value()); borderRadius(4.px)
        background(Tokens.SURFACE.value())
    }
    style(c(SlitDetectionClass.FIGURE)) { margin(20.px, 0.px); maxWidth(1040.px) }
    style(desc(c(SlitDetectionClass.FIGURE), t(SelectorTag.SVG))) { display(DisplayStyle.Block); width(100.percent) }
    style(c(SlitDetectionClass.CAPTION)) { maxWidth(60.cssRem); fontSize(15.px); lineHeight("1.6"); margin(10.px, 0.px) }
    style(c(SlitDetectionClass.COUNT)) { fontSize(20.px); fontWeight(600); margin(18.px, 0.px, 10.px) }
    style(c(SlitDetectionClass.TABLE_SCROLL)) { setOverflowX(OverflowXValue.AUTO); maxWidth(100.percent) }
    style(c(SlitDetectionClass.TABLE)) { width(100.percent); maxWidth(900.px); fontSize(16.px) }
    style(desc(c(SlitDetectionClass.TABLE), t(SelectorTag.TD))) { padding(8.px); edge(CssProperty.BORDER_BOTTOM, 1.px, LineStyle.Solid, Tokens.BORDER.value()) }
    style(desc(c(SlitDetectionClass.TABLE), t(SelectorTag.TH))) { padding(8.px); setTextAlign(TextAlignValue.LEFT) }
}
