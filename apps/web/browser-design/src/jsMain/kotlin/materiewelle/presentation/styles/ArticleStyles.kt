@file:OptIn(org.jetbrains.compose.web.ExperimentalComposeWebApi::class)

package materiewelle.presentation.styles

import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.css.*

internal fun StyleRules.articleStyles() {
    style(desc(c(ArticleClass.ROOT), allOf(t(SelectorTag.INPUT),
        negate(attribute(SelectorAttribute.TYPE, AttributeValue.CHECKBOX))))) {
        minHeight(44.px)
    }
    style(desc(c(ArticleClass.ROOT), t(SelectorTag.TEXTAREA))) {
        width(100.percent); minWidth(0.px); minHeight(96.px); padding(12.px)
        background(Tokens.BG.value()); color(Tokens.TEXT.value())
        border(1.px, LineStyle.Solid, Tokens.BORDER.value()); borderRadius(6.px)
        font(FontSpec(16.px, FontStack.TEXT, lineHeight = 1.5)); setResize(ResizeValue.VERTICAL)
    }
    style(c(ArticleClass.ROOT)) {
        maxWidth(1160.px); composite(CssProperty.MARGIN, "0 auto"); padding(28.px, 16.px, 48.px)
        setOverflowWrap(OverflowWrapValue.ANYWHERE)
    }
    style(desc(c(ArticleClass.ROOT), t(SelectorTag.H1))) { fontSize(48.px); lineHeight("1.12"); maxWidth(850.px); margin(12.px, 0.px, 22.px) }
    style(anyOf(child(c(ArticleClass.ROOT), t(SelectorTag.H2)), child(child(c(ArticleClass.ROOT), t(SelectorTag.SECTION)), t(SelectorTag.H2)))) { fontSize(28.px); margin(0.px, 0.px, 18.px); lineHeight("1.25") }
    style(child(child(c(ArticleClass.ROOT), t(SelectorTag.SECTION)), t(SelectorTag.H3))) { fontSize(19.px); margin(0.px, 0.px, 12.px) }
    style(desc(c(ArticleClass.GRID), t(SelectorTag.H2))) { fontSize(28.px); margin(0.px, 0.px, 18.px); lineHeight("1.25") }
    style(desc(c(ArticleClass.ROOT), t(SelectorTag.P))) { lineHeight("1.55"); maxWidth(760.px) }
    style(c(ArticleClass.LEAD)) { fontSize(21.px); color(Tokens.MUTED.value()); maxWidth(880.px); marginBottom(24.px) }
    style(c(ArticleClass.EYEBROW)) { fontSize(13.px); color(Tokens.MUTED.value()); margin(0.px) }
    style(c(ArticleClass.CONTENTS)) {
        display(DisplayStyle.Flex); flexWrap(FlexWrap.Wrap); gap(8.px, 24.px)
        margin(0.px, 0.px, 28.px); padding(0.px)
    }
    style(desc(c(ArticleClass.CONTENTS), t(SelectorTag.A))) {
        display(DisplayStyle.LegacyInlineFlex); alignItems(AlignItems.Center)
        minHeight(44.px); fontSize(16.px)
    }
    style(c(ArticleClass.REFERENCE_TABLE)) {
        width(100.percent); margin(24.px, 0.px); setBorderCollapse(BorderCollapseValue.COLLAPSE)
        fontSize(15.px); textAlign("left")
    }
    style(desc(c(ArticleClass.REFERENCE_TABLE), anyOf(t(SelectorTag.TH), t(SelectorTag.TD)))) {
        padding(12.px); edge(CssProperty.BORDER_BOTTOM, 1.px, LineStyle.Solid, Tokens.BORDER.value())
        setVerticalAlign(VerticalAlignValue.TOP)
    }
    style(c(ArticleClass.CURRENT_LINE)) {
        edge(CssProperty.BORDER_BOTTOM, 1.px, LineStyle.Solid, Tokens.ACCENT.value())
        background(Tokens.ACCENT_SOFT.value())
    }
    style(desc(c(ArticleClass.ROOT), c(MotionClass.HERO))) {
        padding(0.px); margin(0.px, 0.px, 16.px); border(0.px, LineStyle.None)
        background(Tokens.BG.value()); borderRadius(0.px)
    }
    style(desc(desc(c(ArticleClass.ROOT), c(MotionClass.HERO)), c(MotionClass.FIGURE))) {
        margin(0.px); minHeight(0.px); height(48.px)
    }
    style(desc(desc(c(ArticleClass.ROOT), c(MotionClass.HERO)), t(SelectorTag.SVG))) {
        height(48.px); width(100.percent)
    }
    style(c(ArticleClass.GRID)) {
        display(DisplayStyle.Grid)
        columns(GridTrack.Repeat(2, GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1))))
        gap(48.px); alignItems(AlignItems.Start)
    }
    style(c(ArticleClass.FIGURE)) { margin(8.px, 0.px, 0.px); padding(20.px, 0.px) }
    style(desc(c(ArticleClass.FIGURE), t(SelectorTag.FIGCAPTION))) { color(Tokens.MUTED.value()); fontSize(15.px); lineHeight("1.55"); marginTop(20.px) }
    style(c(ArticleClass.SECTION)) { marginTop(40.px); paddingTop(28.px); edge(CssProperty.BORDER_TOP, 1.px, LineStyle.Solid, Tokens.BORDER.value()) }
    style(c(ArticleClass.FORMULA)) { fontSize(24.px); margin(22.px, 0.px) }
    style(c(ArticleClass.EQUATION)) {
        display(DisplayStyle.Grid); columns(GridTrack.Fixed(3.5.em), GridTrack.Fixed(1.3.em), GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1)))
        alignItems(AlignItems.Center); minWidth(0.px)
    }
    style(c(ArticleClass.EQUATION_LEFT)) { textAlign("right") }
    style(c(ArticleClass.EQUATION_RELATION)) { textAlign("center") }
    style(c(ArticleClass.ACTIONS)) { display(DisplayStyle.Flex); flexWrap(FlexWrap.Wrap); gap(10.px); margin(24.px, 0.px) }
    for (variant in listOf(LabClass.PRIMARY, LabClass.SECONDARY)) {
        style(desc(c(ArticleClass.ROOT), allOf(t(SelectorTag.A), c(variant)))) {
            display(DisplayStyle.LegacyInlineFlex); alignItems(AlignItems.Center); minHeight(44.px)
            margin(8.px, 0.px)
        }
    }
    style(c(ArticleClass.LINE)) { display(DisplayStyle.Grid); columns(GridTrack.Fraction(1), GridTrack.Fraction(1)); gap(42.px); padding(16.px, 0.px); edge(CssProperty.BORDER_BOTTOM, 1.px, LineStyle.Solid, Tokens.BORDER.value()) }
    style(desc(c(ArticleClass.LINE), c(ArticleClass.FORMULA))) { margin(0.px); alignSelf(AlignSelf.Center) }
    style(desc(c(ArticleClass.LINE), t(SelectorTag.P))) { margin(0.px); color(Tokens.MUTED.value()); fontSize(16.px) }
    style(c(ArticleClass.MODEL)) { display(DisplayStyle.Grid); columns(GridTrack.Fraction(1.2), GridTrack.Fraction(1), GridTrack.Fraction(1)); gap(30.px); padding(28.px); background(Tokens.SURFACE.value()); borderRadius(8.px); margin(24.px, 0.px) }
    style(desc(c(ArticleClass.MODEL), t(SelectorTag.INPUT))) { width(100.percent); minHeight(44.px) }
    style(c(ArticleClass.RESULT)) { fontSize(28.px) }
    style(desc(c(ArticleClass.MODEL), t(SelectorTag.P))) { fontSize(14.px); margin(0.px, 0.px, 12.px); color(Tokens.MUTED.value()) }
    style(c(ArticleClass.SMALL)) { fontSize(16.px); color(Tokens.MUTED.value()) }
    style(c(ArticleClass.SOURCE)) { fontSize(15.px); color(Tokens.MUTED.value()) }
    style(c(ArticleClass.SVG)) { width(100.percent); height(280.px); setOverflow(OverflowValue.VISIBLE) }
    style(c(ArticleClass.PLATE)) { set(CssProperty.STROKE, Tokens.MUTED.value()); set(CssProperty.STROKE_WIDTH, 5) }
    style(c(ArticleClass.BEAM)) { set(CssProperty.STROKE, Tokens.ACCENT.value()); set(CssProperty.STROKE_WIDTH, 2) }
    style(c(ArticleClass.ELECTRON)) { set(CssProperty.FILL, Tokens.ACCENT.value()) }
    style(c(ArticleClass.WAVE)) { composite(CssProperty.FILL, "none"); set(CssProperty.STROKE, Tokens.ACCENT.value()); set(CssProperty.STROKE_WIDTH, 2.5) }
    // The 560-unit drawing shrinks to about 475 CSS pixels on the target iPad layout.
    // 18 source pixels keep its labels above 15 CSS pixels after that scaling.
    style(c(ArticleClass.DRAW_LABEL)) { set(CssProperty.FILL, Tokens.MUTED.value()); fontSize(18.px) }
    media(DesignMedia.PREFERS_REDUCED_MOTION_NO_PREFERENCE.query) {
        style(c(ArticleClass.LINE)) { animation(MotionKeyframe.LAB_ARRIVE) { duration(.24.s); timingFunction(AnimationTimingFunction.EaseOut) } }
    }
    media(CSSMediaQuery.MediaFeature("max-width", 1050.px)) {
        style(c(ArticleClass.ROOT)) { padding(20.px, 0.px, 40.px) }
        style(desc(c(ArticleClass.ROOT), t(SelectorTag.H1))) { fontSize(40.px) }
        style(c(ArticleClass.GRID)) { gap(26.px) }
        style(c(ArticleClass.MODEL)) { gap(18.px); padding(20.px) }
    }
    media(CSSMediaQuery.MediaFeature("max-width", 760.px)) {
        style(anyOf(c(ArticleClass.GRID), c(ArticleClass.LINE), c(ArticleClass.MODEL))) { columns(GridTrack.Fraction(1)) }
    }
}
