@file:OptIn(org.jetbrains.compose.web.ExperimentalComposeWebApi::class)

package materiewelle.presentation.styles

import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.css.*

internal fun StyleRules.slitLabStyles() {
    style(c(SlitLabClass.TOOLBAR)) {
        display(DisplayStyle.Flex); flexWrap(FlexWrap.Wrap); alignItems(AlignItems.Center); gap(12.px)
        margin(16.px, 0.px)
    }
    style(desc(c(SlitLabClass.TOOLBAR), t(SelectorTag.SELECT))) {
        minHeight(44.px); padding(10.px, 14.px); font(FontSpec(16.px, FontStack.TEXT))
        color(Tokens.TEXT.value()); background(Tokens.SURFACE.value())
        border(1.px, LineStyle.Solid, Tokens.BORDER.value()); borderRadius(5.px)
    }
    style(c(SlitLabClass.TASK)) { margin(18.px, 0.px); maxWidth(1000.px) }
    style(c(SlitLabClass.FIELDS)) { display(DisplayStyle.Grid); columns(GridTrack.Fraction(1), GridTrack.Fraction(1)); gap(18.px); margin(16.px, 0.px) }
    style(desc(c(SlitLabClass.FIELDS), t(SelectorTag.LABEL))) { display(DisplayStyle.Block); fontSize(15.px); marginBottom(8.px) }
    style(c(SlitLabClass.FEEDBACK)) { marginTop(16.px); padding(12.px, 16.px); edge(CssProperty.BORDER_LEFT, 3.px, LineStyle.Solid, Tokens.ACCENT.value()); background(Tokens.ACCENT_SOFT.value()) }
    style(c(SlitLabClass.POSITION)) { margin(12.px, 0.px); fontSize(16.px) }
    media(CSSMediaQuery.MediaFeature("max-width", 1100.px)) { style(c(SlitLabClass.FIELDS)) { columns(GridTrack.Fraction(1)) } }
}
