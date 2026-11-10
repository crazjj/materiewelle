@file:OptIn(org.jetbrains.compose.web.ExperimentalComposeWebApi::class)

package materiewelle.presentation.styles

import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.css.*

/** Canonical Kotlin style definitions, using shared DOM identities and typed values. */
internal fun StyleRules.geometrySlitsStyles() {
    style(anyOf(c(GeometrySlitsClass.INVESTIGATION), c(GeometrySlitsClass.SLITS))) {
        minWidth(0.px)
    }
    style(anyOf(c(GeometrySlitsClass.VIEWS), c(GeometrySlitsClass.CHOICES))) {
        display(DisplayStyle.Flex)
        flexWrap(FlexWrap.Wrap)
        gap((8).px)
        margin((12).px, 0.px)
    }
    style(child(c(GeometrySlitsClass.CHOICES), t(SelectorTag.P))) {
        set(CssProperty.FLEX_BASIS, (100).percent)
        margin((4).px, 0.px, (8).px)
    }
    style(
        anyOf(
            desc(c(GeometrySlitsClass.VIEWS), t(SelectorTag.BUTTON)),
            desc(c(GeometrySlitsClass.CHOICES), t(SelectorTag.BUTTON)),
        )
    ) {
        minHeight((44).px)
        setTextAlign(TextAlignValue.LEFT)
    }
    style(c(GeometrySlitsClass.ACTIVE)) {
        important { borderColor(Tokens.ACCENT.value()) }
        important { background(Tokens.ACCENT_SOFT.value()) }
        important { color(Tokens.TEXT.value()) }
    }
    style(c(GeometrySlitsClass.FRAME)) {
        setOverflowX(OverflowXValue.AUTO)
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius(Tokens.RADIUS.value())
        background(Tokens.SURFACE.value())
        margin((12).px, 0.px)
        setOverscrollBehaviorInline(OverscrollBehaviorInlineValue.CONTAIN)
    }
    style(c(GeometrySlitsClass.FIGURE)) {
        set(CssProperty.SCROLL_MARGIN_TOP, (90).px)
    }
    style(child(c(GeometrySlitsClass.FRAME), c(LabClass.CHART))) {
        maxWidth((760).px)
        setMarginInline(MarginInlineValue.AUTO)
    }
    style(
        child(
            desc(c(GeometrySlitsClass.INVESTIGATION), c(GeometrySlitsClass.FRAME)),
            c(LabClass.CHART),
        )
    ) {
        maxWidth((630).px)
    }
    style(desc(c(GeometrySlitsClass.FRAME), t(SelectorTag.SVG))) { fontFamily(FontStack.TEXT) }
    style(c(GeometrySlitsClass.VALUES)) {
        columns(GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1)), GridTrack.Auto)
        padding((12).px, 0.px)
        composite(CssProperty.BORDER_BLOCK, "1px solid var(--border)")
        gap((12).px)
    }
    style(desc(c(GeometrySlitsClass.VALUES), t(SelectorTag.DD))) {
        setFontVariantNumeric(FontVariantNumericValue.TABULAR_NUMS)
        color(Tokens.ACCENT.value())
        fontSize((14).px)
    }
    style(desc(c(GeometrySlitsClass.VALUES), t(SelectorTag.DT))) {
        set(CssProperty.LINE_HEIGHT, 1.5)
    }
    style(c(GeometrySlitsClass.RETAINED)) {
        display(DisplayStyle.Grid)
        gap((10).px)
        margin((18).px, 0.px)
    }
    style(c(GeometrySlitsClass.CARD)) {
        padding((14).px, (16).px)
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius(Tokens.RADIUS.value())
        background(Tokens.SURFACE.value())
    }
    style(
        allOf(c(GeometrySlitsClass.CARD), c(LabClass.DERIVATION_CARD), c(LabClass.EQUATION_STEP))
    ) {
        display(DisplayStyle.Block)
        padding((14).px, (16).px)
    }
    style(desc(c(GeometrySlitsClass.CARD), t(SelectorTag.H3))) {
        margin(0.px, 0.px, (6).px)
        fontSize((15).px)
    }
    style(desc(c(GeometrySlitsClass.CARD), t(SelectorTag.P))) {
        fontSize((13).px)
        set(CssProperty.LINE_HEIGHT, 1.55)
    }
    style(desc(c(GeometrySlitsClass.CARD), c(LabClass.FORMULA))) {
        fontSize((20).px)
        padding((8).px)
        margin((8).px, 0.px)
        background(Tokens.SURFACE_RAISED.value())
    }
    style(desc(c(GeometrySlitsClass.CARD), t(SelectorTag.BUTTON))) { marginTop((8).px) }
    style(c(GeometrySlitsClass.COMPARISON)) {
        display(DisplayStyle.Grid)
        columns(GridTrack.Repeat(2, GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1))))
        gap((12).px)
        edge(CssProperty.BORDER_TOP, (1).px, LineStyle.Solid, Tokens.BORDER.value())
        paddingTop((14).px)
    }
    style(child(c(GeometrySlitsClass.COMPARISON), t(SelectorTag.SECTION))) {
        minWidth(0.px)
        padding((12).px)
        background(Tokens.SURFACE_RAISED.value())
        borderRadius(Tokens.RADIUS.value())
    }
    style(desc(c(GeometrySlitsClass.COMPARISON), t(SelectorTag.P))) { fontSize((13).px) }
    style(c(GeometrySlitsClass.WORKSPACE)) {
        display(DisplayStyle.Grid)
        columns(GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1)), GridTrack.Fixed(320.px))
        gap(18.px)
        alignItems(AlignItems.Start)
        margin(16.px, 0.px)
    }
    style(c(GeometrySlitsClass.SETTINGS)) {
        composite(CssProperty.GRID_COLUMN, "2")
        composite(CssProperty.GRID_ROW, "1")
        minWidth(0.px)
    }
    style(child(c(GeometrySlitsClass.WORKSPACE), c(GeometrySlitsClass.PLOT))) {
        composite(CssProperty.GRID_COLUMN, "1")
        composite(CssProperty.GRID_ROW, "1")
        minWidth(0.px)
    }
    style(c(GeometrySlitsClass.APERTURE)) {
        width(100.percent)
        margin(0.px, 0.px, 8.px)
    }
    style(desc(c(GeometrySlitsClass.CONTROLS), c(SlitLabClass.TOOLBAR))) {
        margin(4.px, 0.px, 10.px); gap(10.px); flexWrap(FlexWrap.Nowrap)
    }
    style(desc(desc(c(GeometrySlitsClass.CONTROLS), c(SlitLabClass.TOOLBAR)), t(SelectorTag.LABEL))) {
        margin(0.px); fontSize(16.px)
    }
    style(desc(desc(c(GeometrySlitsClass.CONTROLS), c(SlitLabClass.TOOLBAR)), t(SelectorTag.SELECT))) {
        flexGrow(1); minWidth(0.px); padding(8.px); fontSize(16.px)
    }
    style(desc(c(GeometrySlitsClass.CONTROLS), t(SelectorTag.LABEL))) {
        display(DisplayStyle.Block)
        font(FontSpec(16.px, FontStack.TEXT, lineHeight = 1.5))
    }
    style(desc(c(GeometrySlitsClass.CONTROLS), t(SelectorTag.INPUT))) {
        width(100.percent)
        minHeight(44.px)
        margin(2.px, 0.px, 12.px)
    }
    style(desc(c(GeometrySlitsClass.CONTROLS), c(ArticleClass.ACTIONS))) {
        gap(8.px)
        margin(8.px, 0.px)
    }
    style(desc(c(GeometrySlitsClass.CONTROLS), t(SelectorTag.BUTTON))) {
        minHeight(44.px)
        fontSize(16.px)
        minWidth(0.px)
        padding(8.px)
        flexGrow(1)
        flexBasis(0.px)
    }
    style(c(GeometrySlitsClass.PLOT)) {
        setOverflowX(OverflowXValue.AUTO)
        setOverscrollBehaviorInline(OverscrollBehaviorInlineValue.CONTAIN)
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius(Tokens.RADIUS.value())
        background(Tokens.SURFACE.value())
    }
    style(desc(c(GeometrySlitsClass.KEY), t(SelectorTag.P))) {
        fontSize((16).px)
        color(Tokens.MUTED.value())
        set(CssProperty.LINE_HEIGHT, 1.6)
    }
    style(c(GeometrySlitsClass.READOUT)) {
        padding((12).px, (14).px)
        edge(CssProperty.BORDER_LEFT, (3).px, LineStyle.Solid, Tokens.ACCENT.value())
        background(Tokens.ACCENT_SOFT.value())
        margin((12).px, 0.px)
    }
    style(desc(c(GeometrySlitsClass.READOUT), t(SelectorTag.P))) {
        font(FontSpec((16).px, FontStack.MONO, lineHeight = 1.65))
        setFontVariantNumeric(FontVariantNumericValue.TABULAR_NUMS)
        margin((4).px, 0.px)
    }
    media(CSSMediaQuery.MediaFeature("max-width", 1100.px)) {
        style(c(GeometrySlitsClass.WORKSPACE)) {
            columns(GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1)), GridTrack.Fixed(320.px))
            gap(14.px)
        }
    }
    media(CSSMediaQuery.MediaFeature("max-width", 850.px)) {
        style(c(GeometrySlitsClass.WORKSPACE)) { columns(GridTrack.Fraction(1)) }
        style(c(GeometrySlitsClass.SETTINGS)) {
            composite(CssProperty.GRID_COLUMN, "1")
            composite(CssProperty.GRID_ROW, "1")
            display(DisplayStyle.Grid)
            columns(GridTrack.Fraction(1), GridTrack.Fraction(1))
            gap(16.px)
        }
        style(child(c(GeometrySlitsClass.WORKSPACE), c(GeometrySlitsClass.PLOT))) {
            composite(CssProperty.GRID_ROW, "2")
        }
    }
    media(CSSMediaQuery.MediaFeature("max-width", (760).px)) {
        style(c(GeometrySlitsClass.VIEWS)) {
            display(DisplayStyle.Grid)
            columns(
                GridTrack.Repeat(3, GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1)))
            )
        }
        style(desc(c(GeometrySlitsClass.VIEWS), t(SelectorTag.BUTTON))) {
            fontSize((12).px)
            padding((9).px, (6).px)
        }
        style(c(GeometrySlitsClass.CHOICES)) {
            display(DisplayStyle.Grid)
            columns(GridTrack.Fraction(1))
        }
        style(c(GeometrySlitsClass.VALUES)) { fontSize((12).px) }
        style(desc(c(GeometrySlitsClass.VALUES), t(SelectorTag.DD))) { fontSize((12).px) }
        style(c(GeometrySlitsClass.COMPARISON)) { columns(GridTrack.Fraction(1)) }
        style(c(GeometrySlitsClass.CARD)) { padding((12).px) }
        style(child(c(GeometrySlitsClass.FIGURE), c(LabClass.CHART))) { minWidth((540).px) }
        style(
            child(
                desc(c(GeometrySlitsClass.INVESTIGATION), c(GeometrySlitsClass.FRAME)),
                c(LabClass.CHART),
            )
        ) {
            minWidth((390).px)
        }
        style(child(c(GeometrySlitsClass.PLOT), c(LabClass.CHART))) { minWidth((560).px) }
        style(
            anyOf(
                allOf(c(GeometrySlitsClass.PLOT), pseudo(SelectorPseudo.AFTER)),
                allOf(c(GeometrySlitsClass.FIGURE), pseudo(SelectorPseudo.AFTER)),
                desc(
                    c(GeometrySlitsClass.INVESTIGATION),
                    allOf(c(GeometrySlitsClass.FRAME), pseudo(SelectorPseudo.AFTER)),
                ),
            )
        ) {
            composite(CssProperty.CONTENT, "'↔ Zeichnung bei Bedarf seitlich verschieben'")
            display(DisplayStyle.Block)
            padding((7).px, (10).px)
            font(FontSpec((11).px, FontStack.TEXT))
            color(Tokens.MUTED.value())
        }
    }
    media(DesignMedia.PREFERS_REDUCED_MOTION_REDUCE.query) {
        style(
            anyOf(
                desc(c(GeometrySlitsClass.INVESTIGATION), universal),
                desc(c(GeometrySlitsClass.RETAINED), universal),
                desc(c(GeometrySlitsClass.SLITS), universal),
            )
        ) {
            important { setAnimation(AnimationValue.NONE) }
            important { setTransition(TransitionValue.NONE) }
        }
    }
}
