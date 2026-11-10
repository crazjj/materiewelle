@file:OptIn(org.jetbrains.compose.web.ExperimentalComposeWebApi::class)

package materiewelle.presentation.styles

import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.css.keywords.auto

/** Canonical Kotlin style definitions, using shared DOM identities and typed values. */
internal fun StyleRules.stationActivitiesStyles() {
    style(c(StationActivityClass.ROOT)) {
        minWidth(0.px)
        margin((14).px, 0.px, (20).px)
    }
    style(desc(c(StationActivityClass.ROOT), t(SelectorTag.H3))) {
        fontSize((18).px)
        set(CssProperty.LINE_HEIGHT, 1.4)
        margin(0.px, 0.px, (10).px)
    }
    style(desc(c(StationActivityClass.ROOT), t(SelectorTag.H4))) {
        fontSize((14).px)
        set(CssProperty.LINE_HEIGHT, 1.5)
        margin(0.px, 0.px, (8).px)
    }
    style(desc(c(StationActivityClass.ROOT), t(SelectorTag.P))) {
        fontSize((13).px)
        set(CssProperty.LINE_HEIGHT, 1.65)
    }
    style(c(StationActivityClass.MEDIA_GRID)) {
        display(DisplayStyle.Grid)
        columns(GridTrack.Repeat(3, GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1))))
        gap((12).px)
        margin((14).px, 0.px)
    }
    style(anyOf(c(StationActivityClass.MEDIA_CARD), c(StationActivityClass.CARD))) {
        minWidth(0.px)
        padding((14).px)
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius(Tokens.RADIUS.value())
        background(Tokens.SURFACE.value())
    }
    style(anyOf(c(StationActivityClass.KIND), c(StationActivityClass.YEAR))) {
        important { font(FontSpec((11).px, FontStack.MONO, lineHeight = 1.5)) }
        setFontVariantNumeric(FontVariantNumericValue.TABULAR_NUMS)
        letterSpacing((0.035).em)
        color(Tokens.ACCENT.value())
    }
    style(c(StationActivityClass.KIND)) {
        minHeight((34).px)
        margin(0.px, 0.px, (10).px)
    }
    style(c(StationActivityClass.MEDIA_FRAME)) {
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        justifyContent(JustifyContent.Center)
        minWidth(0.px)
        minHeight((148).px)
        marginBottom((14).px)
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius((6).px)
        setOverflow(OverflowValue.HIDDEN)
        background(Tokens.SURFACE_RAISED.value())
    }
    style(child(c(StationActivityClass.MEDIA_FRAME), c(LabClass.CHART))) {
        width((100).percent)
        margin(0.px)
    }
    style(desc(child(c(StationActivityClass.MEDIA_FRAME), c(LabClass.CHART)), t(SelectorTag.SVG))) {
        display(DisplayStyle.Block)
        width((100).percent)
        height(auto)
    }
    style(c(StationActivityClass.PHOTO)) {
        display(DisplayStyle.Block)
        maxWidth((100).percent)
        height(auto)
        background(Color.white)
        setImageRendering(ImageRenderingValue.AUTO)
    }
    style(c(StationActivityClass.MUTED)) {
        color(Tokens.MUTED.value())
        important { fontSize((12).px) }
        set(CssProperty.LINE_HEIGHT, 1.65)
    }
    style(c(StationActivityClass.CARD_GRID)) {
        display(DisplayStyle.Grid)
        columns(GridTrack.Repeat(2, GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1))))
        gap((12).px)
        margin((14).px, 0.px)
    }
    style(desc(c(StationActivityClass.CARD), t(SelectorTag.H4))) { fontSize((16).px) }
    style(c(StationActivityClass.EQUATION)) {
        important { font(FontSpec((24).px, FontStack.MONO, lineHeight = 1.5)) }
        padding((10).px, 0.px)
        margin((8).px, 0.px)
        color(Tokens.TEXT.value())
    }
    style(c(StationActivityClass.TIMELINE)) {
        display(DisplayStyle.Grid)
        gap((12).px)
    }
    style(
        anyOf(
            child(c(StationActivityClass.TIMELINE), t(SelectorTag.H3)),
            child(c(StationActivityClass.TIMELINE), t(SelectorTag.P)),
        )
    ) {
        margin(0.px)
    }
    style(c(StationActivityClass.TIMELINE_LANE)) {
        display(DisplayStyle.Grid)
        columns(
            GridTrack.Fixed((160).px),
            GridTrack.Repeat(2, GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1))),
        )
        gap((14).px)
        edge(CssProperty.BORDER_TOP, (1).px, LineStyle.Solid, Tokens.BORDER.value())
        paddingTop((14).px)
        alignItems(AlignItems.Start)
    }
    style(child(c(StationActivityClass.TIMELINE_LANE), t(SelectorTag.H4))) {
        color(Tokens.MUTED.value())
        paddingTop((4).px)
    }
    style(c(StationActivityClass.TIMELINE_ENTRY)) {
        paddingLeft((12).px)
        edge(CssProperty.BORDER_LEFT, (2).px, LineStyle.Solid, Tokens.ACCENT.value())
    }
    style(desc(c(StationActivityClass.TIMELINE_ENTRY), t(SelectorTag.P))) {
        margin(0.px, 0.px, (5).px)
    }
    style(c(StationActivityClass.TABLE_SCROLL)) {
        setOverflowX(OverflowXValue.AUTO)
        setOverscrollBehaviorInline(OverscrollBehaviorInlineValue.CONTAIN)
        margin((14).px, 0.px)
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius(Tokens.RADIUS.value())
    }
    style(c(StationActivityClass.TABLE)) {
        width((100).percent)
        minWidth((660).px)
        setBorderCollapse(BorderCollapseValue.COLLAPSE)
        setTextAlign(TextAlignValue.LEFT)
        fontSize((12).px)
        set(CssProperty.LINE_HEIGHT, 1.6)
        background(Tokens.SURFACE.value())
    }
    style(
        anyOf(
            desc(c(StationActivityClass.TABLE), t(SelectorTag.TH)),
            desc(c(StationActivityClass.TABLE), t(SelectorTag.TD)),
        )
    ) {
        padding((11).px, (12).px)
        setVerticalAlign(VerticalAlignValue.TOP)
        edge(CssProperty.BORDER_BOTTOM, (1).px, LineStyle.Solid, Tokens.BORDER.value())
    }
    style(desc(c(StationActivityClass.TABLE), t(SelectorTag.TH))) {
        background(Tokens.SURFACE_RAISED.value())
        color(Tokens.TEXT.value())
        fontWeight(600)
    }
    style(
        desc(
            desc(
                c(StationActivityClass.TABLE),
                allOf(t(SelectorTag.TR), pseudo(SelectorPseudo.LAST_CHILD)),
            ),
            t(SelectorTag.TD),
        )
    ) {
        composite(CssProperty.BORDER_BOTTOM, "0")
    }
    style(
        desc(
            c(StationActivityClass.TABLE),
            allOf(t(SelectorTag.TD), pseudo(SelectorPseudo.FIRST_CHILD)),
        )
    ) {
        minWidth((130).px)
    }
    style(
        anyOf(
            desc(c(StationActivityClass.TABLE), allOf(t(SelectorTag.TD), nthChild("2"))),
            desc(c(StationActivityClass.TABLE), allOf(t(SelectorTag.TD), nthChild("3"))),
        )
    ) {
        setWhiteSpace(WhiteSpaceValue.NOWRAP)
    }
    style(c(StationActivityClass.MONO)) {
        fontFamily(FontStack.MONO)
        setFontVariantNumeric(FontVariantNumericValue.TABULAR_NUMS)
    }
    style(c(StationActivityClass.LIMIT_CASE)) {
        padding((14).px)
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius(Tokens.RADIUS.value())
        margin((12).px, 0.px)
        background(Tokens.SURFACE.value())
    }
    style(desc(c(StationActivityClass.LIMIT_CASE), t(SelectorTag.SELECT))) {
        width((100).percent)
        maxWidth((440).px)
        minHeight((44).px)
    }
    style(
        anyOf(
            desc(c(StationActivityClass.LIMIT_CASE), t(SelectorTag.LABEL)),
            desc(c(StationActivityClass.EVIDENCE_CELL), t(SelectorTag.LABEL)),
        )
    ) {
        display(DisplayStyle.Block)
        fontSize((12).px)
        set(CssProperty.LINE_HEIGHT, 1.5)
        margin((10).px, 0.px, (7).px)
    }
    style(c(StationActivityClass.FEEDBACK)) {
        edge(CssProperty.BORDER_LEFT, (3).px, LineStyle.Solid, Tokens.ACCENT.value())
        padding((10).px, (12).px)
        background(Tokens.ACCENT_SOFT.value())
        important { fontSize((12).px) }
    }
    style(c(StationActivityClass.EVIDENCE_ROW)) {
        padding(14.px, 0.px)
        edge(CssProperty.BORDER_TOP, 1.px, LineStyle.Solid, Tokens.BORDER.value())
        margin((14).px, 0.px)
    }
    style(desc(i(StationActivityId.EVIDENCE), t(SelectorTag.P))) { fontSize(16.px) }
    style(desc(i(StationActivityId.EVIDENCE), c(StationActivityClass.MUTED))) {
        important { fontSize(15.px) }
    }
    style(c(StationActivityClass.EVIDENCE_GRID)) {
        display(DisplayStyle.Grid)
        columns(GridTrack.Repeat(3, GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1))))
        gap((12).px)
    }
    style(c(StationActivityClass.EVIDENCE_CELL)) { minWidth(0.px) }
    style(desc(c(StationActivityClass.EVIDENCE_CELL), t(SelectorTag.LABEL))) { fontSize(15.px) }
    style(desc(c(StationActivityClass.EVIDENCE_CELL), t(SelectorTag.TEXTAREA))) {
        width((100).percent)
        minHeight((128).px)
        fontSize((16).px)
        set(CssProperty.LINE_HEIGHT, 1.6)
        setResize(ResizeValue.VERTICAL)
    }
    style(c(StationActivityClass.EVIDENCE_ACTIONS)) {
        display(DisplayStyle.Flex)
        flexWrap(FlexWrap.Wrap)
        gap((8).px)
        marginTop((12).px)
    }
    style(desc(c(StationActivityClass.EVIDENCE_ACTIONS), t(SelectorTag.BUTTON))) {
        minHeight((44).px)
        setWhiteSpace(WhiteSpaceValue.NORMAL)
        setTextAlign(TextAlignValue.LEFT)
    }
    style(c(StationActivityClass.SUGGESTION)) {
        padding((10).px, (12).px)
        background(Tokens.SURFACE_RAISED.value())
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius((6).px)
        setOverflowWrap(OverflowWrapValue.ANYWHERE)
        important { fontSize((12).px) }
    }
    style(c(StationActivityClass.SVG)) {
        display(DisplayStyle.Block)
        width((100).percent)
        height(auto)
        fontFamily(FontStack.TEXT)
    }
    style(c(StationActivityClass.DRAWING_SURFACE)) {
        set(CssProperty.FILL, Tokens.SURFACE.value())
        set(CssProperty.STROKE, Tokens.MUTED.value())
        set(CssProperty.STROKE_WIDTH, 1.5)
    }
    style(c(StationActivityClass.DRAWING_LINE)) {
        set(CssProperty.STROKE, Tokens.MUTED.value())
        set(CssProperty.STROKE_WIDTH, 1.5)
        setFill(FillValue.NONE)
    }
    style(c(StationActivityClass.DRAWING_ACCENT)) {
        set(CssProperty.FILL, Tokens.ACCENT_SOFT.value())
        set(CssProperty.STROKE, Tokens.ACCENT.value())
        set(CssProperty.STROKE_WIDTH, 2)
    }
    style(c(StationActivityClass.DRAWING_LABEL)) {
        font(FontSpec((13).px, FontStack.TEXT))
        set(CssProperty.FILL, Tokens.TEXT.value())
    }
    style(c(StationActivityClass.DRAWING_TRACE)) {
        setFill(FillValue.NONE)
        set(CssProperty.STROKE, Tokens.ACCENT.value())
        set(CssProperty.STROKE_WIDTH, 2)
    }
    style(
        anyOf(
            allOf(c(StationActivityClass.TABLE_SCROLL), pseudo(SelectorPseudo.FOCUS_VISIBLE)),
            desc(
                c(StationActivityClass.ROOT),
                allOf(
                    matchesAny(
                        anyOf(t(SelectorTag.BUTTON), t(SelectorTag.SELECT), t(SelectorTag.TEXTAREA))
                    ),
                    pseudo(SelectorPseudo.FOCUS_VISIBLE),
                ),
            ),
        )
    ) {
        edge(CssProperty.OUTLINE, (3).px, LineStyle.Solid, Tokens.ACCENT.value())
        outlineOffset((3).px)
    }
    media(CSSMediaQuery.MediaFeature("max-width", (1100).px)) {
        style(c(StationActivityClass.MEDIA_GRID)) { columns(GridTrack.Fraction(1)) }
        style(c(StationActivityClass.MEDIA_CARD)) {
            display(DisplayStyle.Grid)
            columns(
                GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(0.8)),
                GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1)),
            )
            set(CssProperty.COLUMN_GAP, (14).px)
        }
        style(c(StationActivityClass.KIND)) {
            composite(CssProperty.GRID_COLUMN, "1/-1")
            minHeight(0.px)
        }
        style(c(StationActivityClass.MEDIA_FRAME)) {
            composite(CssProperty.GRID_ROW, "2/5")
            marginBottom(0.px)
        }
        style(
            anyOf(
                desc(c(StationActivityClass.MEDIA_CARD), t(SelectorTag.H4)),
                child(
                    c(StationActivityClass.MEDIA_CARD),
                    allOf(t(SelectorTag.P), negate(c(StationActivityClass.KIND))),
                ),
            )
        ) {
            composite(CssProperty.GRID_COLUMN, "2")
        }
        style(desc(c(StationActivityClass.MEDIA_CARD), t(SelectorTag.P))) { marginTop(0.px) }
    }
    media(CSSMediaQuery.MediaFeature("max-width", (760).px)) {
        style(c(StationActivityClass.CARD_GRID)) { columns(GridTrack.Fraction(1)) }
        style(c(StationActivityClass.TIMELINE_LANE)) {
            columns(GridTrack.Fraction(1))
            gap((8).px)
        }
        style(child(c(StationActivityClass.TIMELINE_LANE), t(SelectorTag.H4))) { margin(0.px) }
        style(c(StationActivityClass.TIMELINE_ENTRY)) { paddingLeft((12).px) }
        style(c(StationActivityClass.EVIDENCE_GRID)) {
            columns(GridTrack.Fraction(1))
            gap((4).px)
        }
        style(desc(c(StationActivityClass.EVIDENCE_CELL), t(SelectorTag.TEXTAREA))) {
            minHeight((96).px)
        }
        style(c(StationActivityClass.EVIDENCE_ACTIONS)) { display(DisplayStyle.Grid) }
        style(allOf(c(StationActivityClass.TABLE_SCROLL), pseudo(SelectorPseudo.AFTER))) {
            composite(CssProperty.CONTENT, "'↔ Herkunftstabelle bei Bedarf seitlich verschieben'")
            display(DisplayStyle.Block)
            padding((8).px, (10).px)
            fontSize((11).px)
            color(Tokens.MUTED.value())
        }
    }
    media(CSSMediaQuery.MediaFeature("max-width", (480).px)) {
        style(c(StationActivityClass.MEDIA_CARD)) { display(DisplayStyle.Block) }
        style(c(StationActivityClass.MEDIA_FRAME)) {
            margin((10).px, 0.px, (14).px)
            minHeight((150).px)
        }
        style(c(StationActivityClass.KIND)) { marginBottom((10).px) }
        style(desc(c(StationActivityClass.MEDIA_CARD), t(SelectorTag.H4))) { marginBottom((8).px) }
    }
    media(DesignMedia.PREFERS_REDUCED_MOTION_REDUCE.query) {
        style(desc(c(StationActivityClass.ROOT), universal)) {
            important { setAnimation(AnimationValue.NONE) }
            important { setTransition(TransitionValue.NONE) }
        }
    }
}
