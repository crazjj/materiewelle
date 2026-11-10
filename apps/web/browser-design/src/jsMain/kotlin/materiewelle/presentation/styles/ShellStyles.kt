@file:OptIn(org.jetbrains.compose.web.ExperimentalComposeWebApi::class)
package materiewelle.presentation.styles
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.css.*

/** Navigation geometry belongs to the shell, independently from any chapter. */
internal fun StyleRules.shellStyles() {
    style(c(LabClass.SITE_HEADER)) {
        position(Position.Sticky); set(CssProperty.TOP, 0.px); zIndex(20)
        flexWrap(FlexWrap.Nowrap); gap(12.px)
    }
    style(desc(c(LabClass.SITE_HEADER), c(MotionClass.TOOLS))) {
        composite(CssProperty.WIDTH, "auto"); flexShrink(0); gap(8.px)
    }
    style(desc(c(LabClass.SITE_HEADER), c(LabClass.BRAND))) { marginRight(0.px) }
    style(desc(c(LabClass.SITE_HEADER), t(SelectorTag.BUTTON))) { fontSize(16.px) }
    style(i(ShellId.GITHUB)) {
        minWidth(44.px); minHeight(44.px); flexShrink(0)
        gap(8.px); padding(4.px, 8.px); fontSize(16.px); setWhiteSpace(WhiteSpaceValue.NOWRAP)
    }
    style(desc(i(ShellId.GITHUB), t(SelectorTag.SVG))) {
        display(DisplayStyle.Block); width(23.px); height(23.px)
    }
    style(t(SelectorTag.HTML)) { set(CssProperty.SCROLL_PADDING_TOP, 0.px) }
    for (target in listOf<ElementId>(ArticleId.TITLE, ArticleId.ENTRY, ArticleId.HISTORY, ArticleId.HYPOTHESIS,
        ArticleId.DERIVATION, ArticleId.CALCULATION, ArticleId.RECEPTION, ArticleId.SOURCES,
        DgArticleId.TITLE, DgArticleId.SETUP, DgArticleId.OBSERVATION, DgArticleId.BRAGG,
        DgArticleId.STRUCTURE, DgArticleId.COMPARISON, DgArticleId.SOURCES,
        JonssonArticleId.TITLE, JonssonArticleId.SETUP, JonssonArticleId.LIGHT,
        JonssonArticleId.MODEL, JonssonArticleId.MEASUREMENT, JonssonArticleId.LIMITS,
        JonssonArticleId.SOURCES, ConclusionId.TITLE, ConclusionId.SOURCES)) {
        style(i(target)) { set(CssProperty.SCROLL_MARGIN_TOP, 88.px) }
        style(allOf(i(target), pseudo(SelectorPseudo.FOCUS_VISIBLE))) { important { composite(CssProperty.OUTLINE, "none") } }
    }
    media(CSSMediaQuery.MediaFeature("max-width", 1180.px)) {
        style(c(LabClass.SITE_HEADER)) { padding(10.px, 16.px) }
        style(desc(c(LabClass.SITE_HEADER), c(LabClass.BRAND))) { fontSize(14.px); letterSpacing(.08.em) }
        style(desc(c(LabClass.SITE_HEADER), c(LabClass.NAV_ITEM))) { padding(8.px, 10.px) }
        style(desc(c(LabClass.SITE_HEADER), c(MotionClass.TOOLS))) { composite(CssProperty.WIDTH, "auto") }
    }
    media(CSSMediaQuery.MediaFeature("max-width", 850.px)) {
        style(c(LabClass.SITE_HEADER)) { flexWrap(FlexWrap.Wrap); position(Position.Static) }
    }
    media(CSSMediaQuery.MediaFeature("max-height", 600.px)) {
        style(c(LabClass.SITE_HEADER)) { position(Position.Static) }
    }
}
