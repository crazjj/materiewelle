@file:OptIn(org.jetbrains.compose.web.ExperimentalComposeWebApi::class)
package materiewelle.presentation.styles
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.css.keywords.auto

internal fun StyleRules.compactTutorStyles() {
    style(i(CompactTutorId.PANEL)) {
        width(100.percent); maxWidth(1160.px); minWidth(0.px)
        composite(CssProperty.MARGIN, "16px 0 24px"); padding(24.px)
        background(Tokens.SURFACE.value()); borderRadius(8.px)
        border(1.px, LineStyle.Solid, Tokens.BORDER.value())
        color(Tokens.TEXT.value()); fontSize(16.px); lineHeight("1.6")
    }
    style(desc(i(CompactTutorId.PANEL), t(SelectorTag.DETAILS))) {
        width(100.percent); maxWidth(100.percent); minWidth(0.px); marginTop(20.px)
    }
    style(i(CompactTutorId.PREVIEW)) {
        display(DisplayStyle.Block); width(100.percent); maxWidth(100.percent); minWidth(0.px)
        font(FontSpec(14.px, FontStack.MONO, lineHeight = 1.65))
        setWhiteSpace(WhiteSpaceValue.PRE_WRAP); setOverflowWrap(OverflowWrapValue.ANYWHERE)
        setOverflowX(OverflowXValue.AUTO); padding(16.px); background(Tokens.BG.value())
        color(Tokens.TEXT.value()); borderRadius(6.px)
    }
    for (target in listOf<ElementId>(CompactTutorId.QUESTION)) {
        style(i(target)) {
            display(DisplayStyle.Block); width(100.percent); maxWidth(100.percent); minWidth(0.px)
            minHeight(80.px); padding(12.px); margin(8.px, 0.px, 12.px)
            font(FontSpec(16.px, FontStack.TEXT, lineHeight = 1.5))
            background(Tokens.BG.value()); color(Tokens.TEXT.value())
            border(1.px, LineStyle.Solid, Tokens.BORDER.value()); borderRadius(6.px)
            setResize(ResizeValue.VERTICAL)
        }
    }
    style(desc(i(CompactTutorId.PANEL), c(LabClass.WORKSHOP_ACTIONS))) {
        display(DisplayStyle.Flex); flexWrap(FlexWrap.Wrap); gap(10.px)
    }
    style(child(desc(i(CompactTutorId.PANEL), c(LabClass.WORKSHOP_ACTIONS)), t(SelectorTag.H3))) {
        flexGrow(1); minWidth(0.px); margin(0.px); fontSize(18.px); alignSelf(AlignSelf.Center)
    }
    style(c(CompactTutorPanelClass.CONTEXT)) {
        display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(10.px)
        minHeight(44.px); fontSize(16.px); marginTop(12.px)
    }
    style(i(CompactTutorId.OWN)) {
        width(20.px); height(20.px); minWidth(20.px); minHeight(20.px)
        padding(0.px); margin(0.px); flexShrink(0)
    }
    style(desc(i(CompactTutorId.SUMMARY), t(SelectorTag.P))) { margin(6.px, 0.px, 12.px) }
    style(i(CompactTutorId.CLOSE)) { width(44.px); minHeight(44.px); padding(0.px); fontSize(24.px) }
    style(allOf(i(CompactTutorId.CLOSE), c(CompactTutorPanelClass.BACK))) {
        width(auto); minWidth(180.px); padding(10.px, 14.px); fontSize(16.px)
    }
    style(desc(c(CompactTutorPanelClass.WIDE), t(SelectorTag.P))) {
        maxWidth(46.cssRem)
    }
    style(desc(i(CompactTutorId.PANEL), i(CompactTutorId.EXAMPLES))) {
        display(DisplayStyle.Grid)
        columns(GridTrack.Fraction(1))
        gap(8.px); margin(8.px, 0.px)
    }
    style(desc(i(CompactTutorId.EXAMPLES), t(SelectorTag.BUTTON))) {
        minWidth(0.px); minHeight(44.px)
        setWhiteSpace(WhiteSpaceValue.NORMAL); textAlign("left")
    }
    style(desc(c(LabClass.JONSSON_CONTROLS), i(CompactTutorId.PANEL))) {
        padding(12.px); background(Tokens.BG.value())
        border(1.px, LineStyle.Solid, Tokens.BORDER.value())
    }
    style(desc(desc(c(LabClass.JONSSON_CONTROLS), i(CompactTutorId.PANEL)), i(CompactTutorId.EXAMPLES))) {
        columns(GridTrack.Fraction(1))
    }
    style(c(CompactTutorPanelClass.SCOPE)) { color(Tokens.ACCENT.value()); margin(8.px, 0.px); fontSize(15.px) }
    style(c(CompactTutorPanelClass.INTRO)) { maxWidth(60.cssRem); margin(10.px, 0.px, 24.px) }
    style(c(CompactTutorPanelClass.LAYOUT)) {
        display(DisplayStyle.Grid)
        columns(GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1.2)), GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1)))
        gap(32.px); alignItems(AlignItems.Start)
    }
    style(anyOf(c(CompactTutorPanelClass.PREPARE), c(CompactTutorPanelClass.TRANSFER))) { minWidth(0.px) }
    style(c(CompactTutorPanelClass.TRANSFER)) {
        paddingLeft(24.px); edge(CssProperty.BORDER_LEFT, 1.px, LineStyle.Solid, Tokens.BORDER.value())
    }
    style(c(CompactTutorPanelClass.STEP)) { margin(0.px, 0.px, 12.px); fontWeight(700); fontSize(17.px) }
    style(desc(c(CompactTutorPanelClass.TRANSFER), c(CompactTutorPanelClass.STEP))) { marginTop(20.px) }
    style(c(CompactTutorPanelClass.MODES)) { display(DisplayStyle.Flex); flexWrap(FlexWrap.Wrap); gap(6.px); marginBottom(18.px) }
    style(desc(c(CompactTutorPanelClass.MODES), t(SelectorTag.BUTTON))) { minHeight(44.px); padding(9.px, 12.px) }
    style(desc(c(CompactTutorPanelClass.MODES), allOf(t(SelectorTag.BUTTON), attribute(SelectorAttribute.ARIA_PRESSED, AttributeValue.TRUE)))) {
        background(Tokens.ACCENT.value()); color(Tokens.BG.value()); borderColor(Tokens.ACCENT.value())
    }
    style(desc(c(CompactTutorPanelClass.CONTEXT_SUMMARY), t(SelectorTag.UL))) { paddingLeft(20.px); fontSize(14.px); lineHeight("1.6"); margin(8.px, 0.px) }
    style(i(CompactTutorId.EARLIER)) { width(20.px); height(20.px); minWidth(20.px); padding(0.px); margin(0.px); flexShrink(0) }
    media(CSSMediaQuery.MediaFeature("max-width", 760.px)) {
        style(desc(i(CompactTutorId.PANEL), i(CompactTutorId.EXAMPLES))) { columns(GridTrack.Fraction(1)) }
        style(c(CompactTutorPanelClass.LAYOUT)) { columns(GridTrack.Fraction(1)); gap(20.px) }
        style(c(CompactTutorPanelClass.TRANSFER)) { paddingLeft(0.px); edge(CssProperty.BORDER_LEFT, 0.px, LineStyle.Solid, Tokens.BORDER.value()) }
    }

}
