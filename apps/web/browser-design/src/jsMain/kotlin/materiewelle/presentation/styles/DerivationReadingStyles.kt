@file:OptIn(org.jetbrains.compose.web.ExperimentalComposeWebApi::class)

package materiewelle.presentation.styles

import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.css.*

internal fun StyleRules.derivationReadingStyles() {
    articleReadingStyles()
    // This surface scrolls with the document. An overflow:hidden ancestor traps sticky controls.
    style(c(DerivationReadingClass.SURFACE)) { setOverflow(OverflowValue.VISIBLE) }
    style(c(DerivationReadingClass.CONTROLS)) {
        position(Position.Sticky)
        set(CssProperty.TOP, 56.px)
        zIndex(2)
        padding(12.px)
        margin(16.px, 0.px)
        border(1.px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius(Tokens.RADIUS.value())
        background(Tokens.SURFACE.value())
        composite(CssProperty.BOX_SHADOW, "0 6px 18px #0000000c")
    }
    style(i(DerivationReadingId(DerivationReadingScope.WORKSHOP, DerivationReadingPart.CONTROLS))) {
        set(CssProperty.TOP, 8.px)
    }
    style(c(DerivationReadingClass.MODES)) {
        display(DisplayStyle.Grid)
        columns(GridTrack.Repeat(3, GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1))))
        gap(6.px)
    }
    style(child(c(DerivationReadingClass.MODES), t(SelectorTag.BUTTON))) {
        minWidth(0.px)
        font(FontSpec(12.px, FontStack.TEXT, weight = 500, lineHeight = 1.35))
        padding(8.px, 6.px)
        setWhiteSpace(WhiteSpaceValue.NORMAL)
    }
    style(c(DerivationReadingClass.SELECTED)) {
        important { background(Tokens.ACCENT_SOFT.value()) }
        important { borderColor(Tokens.ACCENT.value()) }
    }
    style(c(DerivationReadingClass.ACTIONS)) {
        display(DisplayStyle.Flex)
        flexWrap(FlexWrap.Wrap)
        gap(8.px)
        marginTop(8.px)
    }
    style(desc(c(DerivationReadingClass.ACTIONS), c(LabClass.COURSE_ACTIONS))) { margin(0.px) }
    style(desc(c(DerivationReadingClass.CONTROLS), t(SelectorTag.P))) {
        margin(8.px, 0.px, 0.px)
        fontSize(12.px)
    }
    style(c(DerivationReadingClass.FRONTIER)) {
        padding(30.px, 12.px)
        marginBottom(20.px)
        color(Tokens.MUTED.value())
        fontSize(13.px)
        textAlign("center")
        edge(CssProperty.BORDER_TOP, 1.px, LineStyle.Solid, Tokens.BORDER.value())
    }
    media(CSSMediaQuery.MediaFeature("max-height", 600.px)) {
        style(c(DerivationReadingClass.CONTROLS)) { position(Position.Static) }
    }
    media(CSSMediaQuery.MediaFeature("max-width", 600.px)) {
        style(child(c(DerivationReadingClass.MODES), t(SelectorTag.BUTTON))) { fontSize(11.px) }
        style(desc(c(DerivationReadingClass.ACTIONS), c(LabClass.COURSE_ACTIONS))) {
            display(DisplayStyle.Grid)
            columns(
                GridTrack.Repeat(3, GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1)))
            )
            width(100.percent)
            gap(6.px)
        }
        style(desc(c(DerivationReadingClass.ACTIONS), t(SelectorTag.BUTTON))) {
            minWidth(0.px)
            padding(8.px, 5.px)
            font(FontSpec(11.px, FontStack.TEXT, weight = 500, lineHeight = 1.35))
            setWhiteSpace(WhiteSpaceValue.NORMAL)
        }
    }
}


/** Native document reading with an optional accompanying formula board. */
private fun StyleRules.articleReadingStyles() {
    style(c(ArticleReadingClass.SURFACE)) { setOverflow(OverflowValue.VISIBLE) }
    style(c(ArticleReadingClass.NAVIGATION)) {
        display(DisplayStyle.Flex); flexWrap(FlexWrap.Wrap); gap(8.px)
        margin(16.px, 0.px, 24.px)
    }
    style(desc(c(ArticleReadingClass.NAVIGATION), t(SelectorTag.BUTTON))) {
        display(DisplayStyle.LegacyInlineFlex); alignItems(AlignItems.Center); gap(8.px)
        minHeight(44.px); padding(8.px, 12.px); fontSize(16.px)
    }
    style(c(ArticleReadingClass.LAYOUT)) { display(DisplayStyle.Block) }
    style(c(ArticleReadingClass.STEPS)) { minWidth(0.px) }
    style(c(ArticleReadingClass.STEP)) {
        marginBottom(12.px)
        set(CssProperty.SCROLL_MARGIN_TOP, 96.px)
    }
    style(c(ArticleReadingClass.STEP_CONTENT)) {
        padding(20.px, 20.px, 24.px)
        edge(CssProperty.BORDER_LEFT, 3.px, LineStyle.Solid, Color.transparent)
        edge(CssProperty.BORDER_BOTTOM, 1.px, LineStyle.Solid, Tokens.BORDER.value())
    }
    style(desc(c(ArticleReadingClass.STEP), t(SelectorTag.H3))) { margin(0.px, 0.px, 12.px) }
    style(desc(c(ArticleReadingClass.STEP), t(SelectorTag.P))) { margin(12.px, 0.px) }
    style(c(ArticleReadingClass.INLINE_FORMULA)) { fontSize(24.px); margin(16.px, 0.px) }
    style(c(ArticleReadingClass.FORMULAS)) { display(DisplayStyle.None) }
    style(desc(c(ArticleReadingClass.ENHANCED), c(ArticleReadingClass.LAYOUT))) {
        display(DisplayStyle.Grid)
        columns(GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1.25)), GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1)))
        gap(28.px); alignItems(AlignItems.Start)
    }
    style(desc(desc(c(ArticleReadingClass.ENHANCED), c(ArticleReadingClass.CURRENT)), c(ArticleReadingClass.STEP_CONTENT))) {
        edge(CssProperty.BORDER_LEFT, 3.px, LineStyle.Solid, Tokens.ACCENT.value())
        background(Tokens.ACCENT_SOFT.value())
    }
    style(desc(c(ArticleReadingClass.ENHANCED), c(ArticleReadingClass.LAST_STEP))) {
        // The final reading row must still contain its complete companion board.
        // Reserve its measured height, not a viewport-sized empty ending.
        composite(CssProperty.MIN_HEIGHT, "var(${ArticleReadingProperty.FORMULA_HEIGHT.cssName}, 0px)")
    }
    style(desc(c(ArticleReadingClass.ENHANCED), c(ArticleReadingClass.INLINE_FORMULA))) {
        // Keep the complete formula in the accessible reading sequence, not duplicated by the visual board.
        position(Position.Absolute); width(1.px); height(1.px); padding(0.px); margin((-1).px)
        setOverflow(OverflowValue.HIDDEN); composite(CssProperty.CLIP_PATH, "inset(50%)")
        setWhiteSpace(WhiteSpaceValue.NOWRAP)
    }
    style(desc(c(ArticleReadingClass.ENHANCED), c(ArticleReadingClass.FORMULAS))) {
        display(DisplayStyle.Block); position(Position.Sticky)
        composite(CssProperty.TOP, "var(${ArticleReadingProperty.TOP.cssName}, 88px)")
        padding(20.px); border(1.px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius(8.px); background(Tokens.SURFACE.value()); minWidth(0.px)
    }
    style(desc(c(ArticleReadingClass.FORMULAS), t(SelectorTag.H3))) { margin(0.px, 0.px, 12.px); fontSize(18.px) }
    style(c(ArticleReadingClass.FORMULA_ROW)) {
        display(DisplayStyle.Grid); columns(GridTrack.Fixed(20.px), GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1)))
        gap(10.px); alignItems(AlignItems.Center); padding(10.px, 6.px); minHeight(44.px)
        fontSize(22.px); color(Tokens.MUTED.value())
    }
    style(child(c(ArticleReadingClass.FORMULA_ROW), t(SelectorTag.SPAN))) { fontSize(14.px) }
    style(c(ArticleReadingClass.FORMULA_CURRENT)) { background(Tokens.ACCENT_SOFT.value()); color(Tokens.TEXT.value()) }
    style(c(ArticleReadingClass.HYPOTHESIS)) {
        edge(CssProperty.BORDER_TOP, 1.px, LineStyle.Solid, Tokens.REFERENCE.value())
    }
    media(CSSMediaQuery.MediaFeature("max-width", (ArticleReadingLayout.MIN_WIDTH - 1).px)) {
        style(c(ArticleReadingClass.STEP_CONTENT)) { padding(16.px, 0.px) }
    }
}
