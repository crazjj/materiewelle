@file:OptIn(org.jetbrains.compose.web.ExperimentalComposeWebApi::class)
package materiewelle.presentation.styles

import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.css.*

internal fun StyleRules.textCopyStyles() {
    style(c(TextCopyClass.ROOT)) { minWidth(0.px) }
    style(desc(c(TextCopyClass.ROOT), t(SelectorTag.DETAILS))) { marginTop(10.px) }
    style(c(TextCopyClass.PREVIEW)) {
        display(DisplayStyle.Block); maxWidth(100.percent); minWidth(0.px)
        maxHeight(24.cssRem); padding(16.px)
        setWhiteSpace(WhiteSpaceValue.PRE_WRAP); setOverflowWrap(OverflowWrapValue.ANYWHERE)
        setOverflowX(OverflowXValue.AUTO); composite(CssProperty.OVERFLOW_Y, "auto")
        font(FontSpec(15.px, FontStack.TEXT, lineHeight = 1.55))
        background(Tokens.BG.value()); color(Tokens.TEXT.value())
        border(1.px, LineStyle.Solid, Tokens.BORDER.value()); borderRadius(6.px)
    }
}
