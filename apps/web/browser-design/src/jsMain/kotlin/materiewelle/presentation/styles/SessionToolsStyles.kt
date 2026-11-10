@file:OptIn(org.jetbrains.compose.web.ExperimentalComposeWebApi::class)
package materiewelle.presentation.styles

import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.css.*

internal fun StyleRules.sessionToolsStyles() {
    style(c(SessionUiClass.ROOT)) {
        maxWidth(1160.px); composite(CssProperty.MARGIN, "0 auto"); padding(0.px, 16.px, 32.px); minWidth(0.px)
    }
    style(child(c(SessionUiClass.ROOT), t(SelectorTag.DETAILS))) {
        edge(CssProperty.BORDER_TOP, 1.px, LineStyle.Solid, Tokens.BORDER.value()); paddingTop(16.px)
    }
    style(c(SessionUiClass.ALERT)) {
        maxWidth(1160.px); composite(CssProperty.MARGIN, "0 auto 16px"); padding(16.px); minWidth(0.px)
    }
    style(child(c(SessionUiClass.ROOT), t(SelectorTag.P))) { margin(0.px, 0.px, 8.px) }
    style(c(SessionUiClass.EDITING_BOUNDARY)) {
        display(DisplayStyle.Block); minWidth(0.px); width(100.percent)
        padding(0.px); margin(0.px); border(0.px, LineStyle.None, Color.transparent)
    }
}
