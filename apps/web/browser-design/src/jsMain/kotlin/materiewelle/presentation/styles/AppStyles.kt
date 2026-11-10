@file:OptIn(org.jetbrains.compose.web.ExperimentalComposeWebApi::class)

package materiewelle.presentation.styles

import materiewelle.ui.semantics.*
import materiewelle.presentation.AppTypeface
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.css.keywords.auto

/** Canonical Kotlin style definitions, using shared DOM identities and typed values. */
internal fun StyleRules.appStyles() {
    EmbeddedFont.entries.forEach { fontFace(it) }
    style(pseudo(SelectorPseudo.ROOT)) {
        setColorScheme(ColorSchemeValue.LIGHT)
        setToken(Tokens.BG, rgb(243, 246, 248))
        setToken(Tokens.SURFACE, rgb(255, 255, 255))
        setToken(Tokens.SURFACE_RAISED, rgb(234, 240, 244))
        setToken(Tokens.TEXT, rgb(24, 40, 48))
        setToken(Tokens.MUTED, rgb(83, 102, 113))
        setToken(Tokens.ACCENT, rgb(0, 108, 128))
        setToken(Tokens.ACCENT_SOFT, rgb(224, 243, 246))
        setToken(Tokens.ACCENT_INK, rgb(255, 255, 255))
        setToken(Tokens.BORDER, rgb(197, 210, 217))
        setToken(Tokens.GRID, rgb(220, 229, 233))
        setToken(Tokens.DANGER, rgb(171, 39, 71))
        setToken(Tokens.DANGER_SOFT, rgb(255, 240, 243))
        setToken(Tokens.REFERENCE, rgb(135, 86, 0))
        setToken(Tokens.REFERENCE_SOFT, rgb(255, 244, 217))
        setToken(Tokens.FOCUS, rgb(0, 108, 128))
        setToken(Tokens.FONT_TEXT, AppTypeface.TEXT.css)
        setToken(Tokens.FONT_MONO, AppTypeface.MONO.css)
        setToken(Tokens.RADIUS, (8).px)
        setToken(Tokens.CONTROL_RADIUS, (6).px)
        setToken(Tokens.CONTROL_HEIGHT, (44).px)
        setToken(Tokens.SPACE, (8).px)
        setToken(Tokens.SPACE_1, (4).px)
        setToken(Tokens.SPACE_2, (8).px)
        setToken(Tokens.SPACE_3, (12).px)
        setToken(Tokens.SPACE_4, (16).px)
        setToken(Tokens.SPACE_5, (20).px)
        setToken(Tokens.SPACE_6, (24).px)
        setToken(Tokens.SPACE_8, (32).px)
        setToken(Tokens.INK, Tokens.TEXT.value())
        setToken(Tokens.LINE, Tokens.BORDER.value())
        setToken(Tokens.GRAPH, Tokens.ACCENT.value())
        setToken(Tokens.PAPER, Tokens.SURFACE.value())
    }
    style(
        allOf(
            pseudo(SelectorPseudo.ROOT),
            attribute(SelectorAttribute.DATA_THEME, AttributeValue.DARK),
        )
    ) {
        setColorScheme(ColorSchemeValue.DARK)
        setToken(Tokens.BG, rgb(16, 21, 25))
        setToken(Tokens.SURFACE, rgb(23, 30, 36))
        setToken(Tokens.SURFACE_RAISED, rgb(32, 42, 50))
        setToken(Tokens.TEXT, rgb(234, 241, 245))
        setToken(Tokens.MUTED, rgb(164, 183, 196))
        setToken(Tokens.ACCENT, rgb(89, 216, 238))
        setToken(Tokens.ACCENT_SOFT, rgb(24, 54, 64))
        setToken(Tokens.ACCENT_INK, rgb(9, 33, 40))
        setToken(Tokens.BORDER, rgb(52, 69, 80))
        setToken(Tokens.GRID, rgb(43, 57, 66))
        setToken(Tokens.DANGER, rgb(255, 152, 174))
        setToken(Tokens.DANGER_SOFT, rgb(60, 32, 42))
        setToken(Tokens.REFERENCE, rgb(239, 193, 106))
        setToken(Tokens.REFERENCE_SOFT, rgb(53, 47, 32))
        setToken(Tokens.FOCUS, rgb(141, 230, 245))
    }
    style(universal) { setBoxSizing(BoxSizingValue.BORDER_BOX) }
    style(t(SelectorTag.HTML)) {
        background(Tokens.BG.value())
        color(Tokens.TEXT.value())
        fontFamily(FontStack.TEXT)
        fontSize((18).px)
        set(CssProperty.SCROLL_PADDING_TOP, (24).px)
    }
    style(t(SelectorTag.BODY)) {
        margin(0.px)
        minWidth((320).px)
    }
    style(attribute(SelectorAttribute.HIDDEN)) { important { display(DisplayStyle.None) } }
    style(anyOf(t(SelectorTag.BUTTON), t(SelectorTag.INPUT), t(SelectorTag.SELECT), t(SelectorTag.TEXTAREA))) {
        setFont(FontValue.INHERIT)
        setColor(ColorValue.INHERIT)
    }
    style(
        anyOf(
            t(SelectorTag.BUTTON),
            t(SelectorTag.A),
            t(SelectorTag.INPUT),
            t(SelectorTag.SELECT),
            t(SelectorTag.SUMMARY),
        )
    ) {
        setTouchAction(TouchActionValue.MANIPULATION)
    }
    style(anyOf(t(SelectorTag.BUTTON), t(SelectorTag.SUMMARY), t(SelectorTag.SELECT))) {
        setCursor(CursorValue.POINTER)
    }
    style(allOf(t(SelectorTag.BUTTON), pseudo(SelectorPseudo.DISABLED))) {
        opacity(0.45)
        setCursor(CursorValue.NOT_ALLOWED)
    }
    style(t(SelectorTag.A)) {
        color(Tokens.ACCENT.value())
        set(CssProperty.TEXT_UNDERLINE_OFFSET, (3).px)
    }
    style(
        anyOf(
            t(SelectorTag.BUTTON),
            t(SelectorTag.A),
            t(SelectorTag.INPUT),
            t(SelectorTag.SELECT),
            t(SelectorTag.SUMMARY),
            attribute(SelectorAttribute.TABINDEX),
        )
    ) {
        setWebkitTapHighlightColor(WebkitTapHighlightColorValue.TRANSPARENT)
    }
    style(pseudo(SelectorPseudo.FOCUS_VISIBLE)) {
        important { edge(CssProperty.OUTLINE, (3).px, LineStyle.Solid, Tokens.FOCUS.value()) }
        important { outlineOffset((3).px) }
    }
    style(
        allOf(t(SelectorTag.INPUT), attribute(SelectorAttribute.ARIA_INVALID, AttributeValue.TRUE))
    ) {
        borderColor(Tokens.DANGER.value())
        background(Tokens.DANGER_SOFT.value())
    }
    style(c(LabClass.SITE_HEADER)) {
        edge(CssProperty.BORDER_BOTTOM, (1).px, LineStyle.Solid, Tokens.BORDER.value())
        background(Tokens.SURFACE.value())
        composite(CssProperty.PADDING, "12px max(24px,calc((100vw - 1536px)/2))")
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        gap((28).px)
        justifyContent(JustifyContent.SpaceBetween)
    }
    style(c(LabClass.BRAND)) {
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        gap(Tokens.SPACE_3.value())
        setWhiteSpace(WhiteSpaceValue.NOWRAP)
        setTextDecoration(TextDecorationValue.NONE)
        color(Tokens.TEXT.value())
        fontSize((18).px)
        fontWeight(600)
        letterSpacing((0.16).em)
    }
    style(c(LabClass.BRAND_MARK)) {
        color(Tokens.ACCENT.value())
        font(FontSpec((28).px, FontStack.MONO))
        letterSpacing((-0.25).em)
        paddingRight((8).px)
    }
    style(c(LabClass.BRAND_SUBTITLE)) {
        font(FontSpec((11).px, FontStack.MONO))
        color(Tokens.MUTED.value())
        letterSpacing((0.13).em)
        edge(CssProperty.BORDER_LEFT, (1).px, LineStyle.Solid, Tokens.BORDER.value())
        paddingLeft((16).px)
    }
    style(desc(c(LabClass.SITE_HEADER), t(SelectorTag.NAV))) {
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        gap((4).px)
    }
    style(desc(c(LabClass.SITE_HEADER), c(LabClass.NAV_ITEM))) {
        background(Color.transparent)
        border((1).px, LineStyle.Solid, Color.transparent)
        borderRadius(Tokens.CONTROL_RADIUS.value())
        color(Tokens.MUTED.value())
        font(FontSpec((16).px, FontStack.TEXT, weight = 400))
        setWhiteSpace(WhiteSpaceValue.NOWRAP)
        minHeight(Tokens.CONTROL_HEIGHT.value())
        padding((10).px, (14).px)
    }
    style(
        desc(c(LabClass.SITE_HEADER), allOf(c(LabClass.NAV_ITEM), pseudo(SelectorPseudo.HOVER)))
    ) {
        background(Tokens.SURFACE_RAISED.value())
        color(Tokens.TEXT.value())
    }
    style(desc(c(LabClass.SITE_HEADER), allOf(c(LabClass.NAV_ITEM), c(LabClass.ACTIVE)))) {
        color(Tokens.ACCENT.value())
        background(Tokens.ACCENT_SOFT.value())
        borderColor(Tokens.BORDER.value())
    }
    style(c(LabClass.THEME_CHOOSER)) {
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        gap(Tokens.SPACE_2.value())
    }
    style(desc(c(LabClass.THEME_CHOOSER), t(SelectorTag.LABEL))) {
        font(FontSpec((11).px, FontStack.MONO))
        color(Tokens.MUTED.value())
        margin(0.px)
    }
    style(desc(c(LabClass.THEME_CHOOSER), t(SelectorTag.SELECT))) {
        minHeight((40).px)
        padding((6).px, (28).px, (6).px, (10).px)
        fontSize((13).px)
    }
    style(c(LabClass.SKIP_LINK)) {
        position(Position.Absolute)
        set(CssProperty.LEFT, (12).px)
        set(CssProperty.TOP, (-60).px)
        zIndex(20)
        background(Tokens.SURFACE.value())
        padding((10).px)
    }
    style(allOf(c(LabClass.SKIP_LINK), pseudo(SelectorPseudo.FOCUS))) {
        set(CssProperty.TOP, (8).px)
    }
    style(c(LabClass.MAIN)) {
        maxWidth((1536).px)
        composite(CssProperty.MARGIN, "0 auto")
        padding((32).px, (24).px, (48).px)
        composite(CssProperty.MIN_HEIGHT, "calc(100vh - 140px)")
    }
    style(
        anyOf(
            c(LabClass.EXPERIMENT_EYEBROW),
            c(LabClass.EYEBROW),
            c(AdditionalStyleClass.INSTRUMENT_LABEL),
        )
    ) {
        font(FontSpec((11).px, FontStack.MONO))
        letterSpacing((0.12).em)
        setTextTransform(TextTransformValue.UPPERCASE)
        color(Tokens.MUTED.value())
        margin(0.px, 0.px, (12).px)
    }
    style(anyOf(t(SelectorTag.H1), t(SelectorTag.H2), t(SelectorTag.H3))) {
        fontFamily(FontStack.TEXT)
        set(CssProperty.LINE_HEIGHT, 1.2)
    }
    style(t(SelectorTag.H1)) {
        composite(CssProperty.FONT_SIZE, "clamp(26px,3vw,38px)")
        fontWeight(600)
        letterSpacing((-0.035).em)
        margin(0.px, 0.px, (8).px)
    }
    style(t(SelectorTag.H2)) {
        fontSize((19).px)
        fontWeight(600)
        margin(0.px, 0.px, (12).px)
    }
    style(t(SelectorTag.H3)) {
        fontSize((16).px)
        fontWeight(600)
        margin((16).px, 0.px, (8).px)
    }
    style(t(SelectorTag.P)) {
        set(CssProperty.LINE_HEIGHT, 1.55)
        margin((8).px, 0.px)
    }
    style(c(LabClass.SUBTITLE)) {
        fontSize((16).px)
        color(Tokens.MUTED.value())
        margin(0.px, 0.px, (24).px)
        maxWidth((980).px)
    }
    style(c(LabClass.SECTION_HEADING)) {
        display(DisplayStyle.Flex)
        justifyContent(JustifyContent.SpaceBetween)
        alignItems(AlignItems.FlexStart)
        gap(Tokens.SPACE_5.value())
    }
    style(anyOf(c(LabClass.SMALL), c(AdditionalStyleClass.SOURCE), c(LabClass.GRAPH_CAPTION))) {
        fontSize((15).px)
        set(CssProperty.LINE_HEIGHT, 1.6)
    }
    style(c(LabClass.MUTED)) { color(Tokens.MUTED.value()) }
    style(c(LabClass.WORKSPACE)) {
        display(DisplayStyle.Grid)
        columns(
            GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(2.6)),
            GridTrack.MinMax(GridTrack.Fixed((280).px), GridTrack.Fraction(1)),
        )
        gap(Tokens.SPACE_4.value())
        marginBottom((20).px)
    }
    style(anyOf(c(LabClass.GRAPH_AREA), c(LabClass.CONTROLS), c(LabClass.PANEL))) {
        minWidth(0.px)
        background(Tokens.SURFACE.value())
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius(Tokens.RADIUS.value())
        padding(Tokens.SPACE_5.value())
    }
    style(c(LabClass.GRAPH_AREA)) {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        justifyContent(JustifyContent.Center)
    }
    style(c(LabClass.AXIS_TITLE)) {
        fontSize((13).px)
        color(Tokens.MUTED.value())
        margin(0.px, 0.px, (12).px)
    }
    style(c(LabClass.CHART)) { minWidth(0.px) }
    style(desc(c(LabClass.CHART), t(SelectorTag.SVG))) {
        display(DisplayStyle.Block)
        width((100).percent)
        height(auto)
        fontFamily(FontStack.TEXT)
        color(Tokens.TEXT.value())
    }
    style(c(LabClass.GRAPH_CAPTION)) {
        color(Tokens.MUTED.value())
        marginTop((12).px)
    }
    style(anyOf(desc(c(LabClass.CONTROLS), t(SelectorTag.LABEL)), t(SelectorTag.LABEL))) {
        display(DisplayStyle.Block)
        font(FontSpec((16).px, FontStack.TEXT, weight = 400))
        letterSpacing((0.025).em)
        marginBottom((8).px)
    }
    style(c(LabClass.NUMBER_ROW)) {
        display(DisplayStyle.Flex)
        gap(Tokens.SPACE_2.value())
    }
    style(desc(c(LabClass.NUMBER_ROW), t(SelectorTag.INPUT))) {
        minWidth(0.px)
        width((100).percent)
        font(FontSpec((24).px, FontStack.MONO))
    }
    style(
        anyOf(
            allOf(
                t(SelectorTag.INPUT),
                negate(attribute(SelectorAttribute.TYPE, AttributeValue.RANGE)),
                negate(attribute(SelectorAttribute.TYPE, AttributeValue.CHECKBOX)),
            ),
            t(SelectorTag.SELECT),
        )
    ) {
        background(Tokens.BG.value())
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius(Tokens.CONTROL_RADIUS.value())
        minHeight(Tokens.CONTROL_HEIGHT.value())
        padding((10).px, (12).px)
    }
    style(c(LabClass.UNIT)) {
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        padding(0.px, (12).px)
        font(FontSpec((16).px, FontStack.MONO))
        color(Tokens.MUTED.value())
    }
    style(c(AdditionalStyleClass.RANGE_ROW)) {
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        gap(Tokens.SPACE_3.value())
        setWhiteSpace(WhiteSpaceValue.NOWRAP)
        font(FontSpec((11).px, FontStack.MONO))
        color(Tokens.MUTED.value())
        margin((14).px, 0.px)
    }
    style(allOf(t(SelectorTag.INPUT), attribute(SelectorAttribute.TYPE, AttributeValue.RANGE))) {
        width((100).percent)
        minWidth(0.px)
        set(CssProperty.ACCENT_COLOR, Tokens.ACCENT.value())
        minHeight((32).px)
    }
    style(allOf(t(SelectorTag.INPUT), attribute(SelectorAttribute.TYPE, AttributeValue.CHECKBOX))) {
        set(CssProperty.ACCENT_COLOR, Tokens.ACCENT.value())
        width((20).px)
        height((20).px)
    }
    style(anyOf(c(LabClass.SECONDARY), c(LabClass.PRIMARY), c(LabClass.DATA_CHOICE))) {
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius(Tokens.CONTROL_RADIUS.value())
        minHeight(Tokens.CONTROL_HEIGHT.value())
        padding((10).px, (14).px)
        background(Tokens.SURFACE.value())
        color(Tokens.TEXT.value())
        fontSize((16).px)
        fontWeight(400)
    }
    style(
        anyOf(
            allOf(c(LabClass.SECONDARY), pseudo(SelectorPseudo.HOVER)),
            allOf(c(LabClass.DATA_CHOICE), pseudo(SelectorPseudo.HOVER)),
        )
    ) {
        background(Tokens.SURFACE_RAISED.value())
        borderColor(Tokens.MUTED.value())
    }
    style(
        anyOf(
            c(LabClass.PRIMARY),
            allOf(c(LabClass.DATA_CHOICE), c(LabClass.CHOSEN)),
            c(LabClass.CHOSEN),
        )
    ) {
        background(Tokens.ACCENT.value())
        color(Tokens.ACCENT_INK.value())
        borderColor(Tokens.ACCENT.value())
    }
    style(allOf(c(LabClass.PRIMARY), pseudo(SelectorPseudo.HOVER))) {
        composite(CssProperty.FILTER, "brightness(.92)")
    }
    style(c(LabClass.MODE_SWITCH)) {
        display(DisplayStyle.LegacyInlineFlex)
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius(Tokens.CONTROL_RADIUS.value())
        setOverflow(OverflowValue.HIDDEN)
        flexShrink(0)
        marginBottom((16).px)
    }
    style(desc(c(LabClass.MODE_SWITCH), t(SelectorTag.BUTTON))) {
        composite(CssProperty.BORDER, "0")
        background(Tokens.SURFACE.value())
        color(Tokens.MUTED.value())
        minHeight(Tokens.CONTROL_HEIGHT.value())
        padding((10).px, (15).px)
        fontSize((13).px)
    }
    style(
        desc(c(LabClass.MODE_SWITCH), allOf(t(SelectorTag.BUTTON), pseudo(SelectorPseudo.HOVER)))
    ) {
        background(Tokens.SURFACE_RAISED.value())
        color(Tokens.TEXT.value())
    }
    style(desc(c(LabClass.MODE_SWITCH), allOf(t(SelectorTag.BUTTON), c(LabClass.CHOSEN)))) {
        background(Tokens.ACCENT_SOFT.value())
        color(Tokens.ACCENT.value())
    }
    style(c(LabClass.RESULT)) {
        edge(CssProperty.BORDER_TOP, (1).px, LineStyle.Solid, Tokens.BORDER.value())
        marginTop((18).px)
        paddingTop((16).px)
    }
    style(desc(c(LabClass.RESULT), t(SelectorTag.P))) {
        fontSize((12).px)
        color(Tokens.MUTED.value())
    }
    style(anyOf(c(LabClass.RESULT_VALUE), c(LabClass.COMPARE_VALUE), c(LabClass.PARAMETER_VALUE))) {
        fontFamily(FontStack.MONO)
        setFontVariantNumeric(FontVariantNumericValue.TABULAR_NUMS)
        letterSpacing((-0.04).em)
        composite(CssProperty.FONT_SIZE, "clamp(25px,2.8vw,36px)")
        color(Tokens.ACCENT.value())
        margin((7).px, 0.px)
        set(CssProperty.LINE_HEIGHT, 1.25)
        setOverflowWrap(OverflowWrapValue.ANYWHERE)
    }
    style(c(LabClass.SI_VALUE)) { font(FontSpec((12).px, FontStack.MONO)) }
    style(c(LabClass.FORMULA)) {
        fontFamily(FontStack.SERIF)
        setFontStyle(FontStyleValue.ITALIC)
        fontSize((23).px)
        setTextAlign(TextAlignValue.CENTER)
        padding((12).px)
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius(Tokens.CONTROL_RADIUS.value())
        background(Tokens.SURFACE_RAISED.value())
        color(Tokens.TEXT.value())
        set(CssProperty.LINE_HEIGHT, 1.4)
        setOverflowWrap(OverflowWrapValue.ANYWHERE)
    }
    style(c(LabClass.DERIVATION)) {
        display(DisplayStyle.Grid)
        columns(GridTrack.Repeat(3, GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1))))
        gap(Tokens.SPACE_5.value())
        padding(Tokens.SPACE_5.value(), 0.px)
        edge(CssProperty.BORDER_BOTTOM, (1).px, LineStyle.Solid, Tokens.BORDER.value())
    }
    style(c(LabClass.DERIVATION_STEP)) {
        display(DisplayStyle.Flex)
        gap((14).px)
        minWidth(0.px)
    }
    style(child(c(LabClass.DERIVATION_STEP), t(SelectorTag.DIV))) {
        minWidth(0.px)
        composite(CssProperty.FLEX, "1")
    }
    style(desc(c(LabClass.DERIVATION_STEP), t(SelectorTag.P))) {
        fontSize((13).px)
        color(Tokens.MUTED.value())
    }
    style(c(LabClass.STEP_NUMBER)) {
        font(FontSpec((20).px, FontStack.MONO))
        color(Tokens.ACCENT.value())
    }
    style(c(AdditionalStyleClass.PREDICTION)) {
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        justifyContent(JustifyContent.SpaceBetween)
        gap(Tokens.SPACE_6.value())
        padding(Tokens.SPACE_5.value(), 0.px)
    }
    style(desc(c(AdditionalStyleClass.PREDICTION), t(SelectorTag.P))) {
        fontSize((14).px)
        color(Tokens.MUTED.value())
    }
    style(c(LabClass.NOTES)) {
        edge(CssProperty.BORDER_TOP, (1).px, LineStyle.Solid, Tokens.BORDER.value())
        padding((15).px, 0.px)
        color(Tokens.MUTED.value())
        fontSize((14).px)
    }
    style(desc(c(LabClass.NOTES), t(SelectorTag.SUMMARY))) {
        fontWeight(500)
        minHeight((32).px)
        color(Tokens.TEXT.value())
        set(CssProperty.LINE_HEIGHT, 1.6)
    }
    style(
        desc(allOf(c(LabClass.NOTES), attribute(SelectorAttribute.OPEN)), t(SelectorTag.SUMMARY))
    ) {
        marginBottom((14).px)
    }
    style(desc(c(LabClass.NOTES), t(SelectorTag.LI))) { set(CssProperty.LINE_HEIGHT, 1.65) }
    style(
        anyOf(
            c(LabClass.SOURCE_WARNING),
            c(LabClass.MODEL_LIMIT),
            c(LabClass.ERROR),
            c(LabClass.CALIBRATION_NOTE),
        )
    ) {
        edge(CssProperty.BORDER_LEFT, (3).px, LineStyle.Solid, Tokens.REFERENCE.value())
        background(Tokens.REFERENCE_SOFT.value())
        color(Tokens.TEXT.value())
        padding((12).px, (16).px)
        margin((16).px, 0.px)
        fontSize((13).px)
        set(CssProperty.LINE_HEIGHT, 1.6)
    }
    style(c(LabClass.ERROR)) {
        borderColor(Tokens.DANGER.value())
        background(Tokens.DANGER_SOFT.value())
        color(Tokens.DANGER.value())
    }
    style(anyOf(c(LabClass.MODEL_LIMIT), c(LabClass.CALIBRATION_NOTE))) {
        borderColor(Tokens.ACCENT.value())
        background(Tokens.ACCENT_SOFT.value())
    }
    style(c(AdditionalStyleClass.TABLE_WRAP)) { setOverflowX(OverflowXValue.AUTO) }
    style(t(SelectorTag.TABLE)) {
        width((100).percent)
        setBorderCollapse(BorderCollapseValue.COLLAPSE)
        margin((12).px, 0.px)
        setFontVariantNumeric(FontVariantNumericValue.TABULAR_NUMS)
    }
    style(anyOf(t(SelectorTag.TD), t(SelectorTag.TH))) {
        setTextAlign(TextAlignValue.LEFT)
        edge(CssProperty.BORDER_BOTTOM, (1).px, LineStyle.Solid, Tokens.BORDER.value())
        padding((10).px, (12).px)
        fontSize((13).px)
    }
    style(t(SelectorTag.TH)) {
        color(Tokens.MUTED.value())
        fontWeight(500)
    }
    style(c(AdditionalStyleClass.SELECTED_ROW)) { background(Tokens.ACCENT_SOFT.value()) }
    style(t(SelectorTag.DL)) {
        display(DisplayStyle.Grid)
        columns(GridTrack.Fraction(1), GridTrack.Fraction(1))
        gap((10).px)
        fontSize((13).px)
    }
    style(t(SelectorTag.DT)) { color(Tokens.MUTED.value()) }
    style(t(SelectorTag.DD)) {
        margin(0.px)
        fontFamily(FontStack.MONO)
        setTextAlign(TextAlignValue.RIGHT)
    }
    style(c(AdditionalStyleClass.COMPARISON)) {
        display(DisplayStyle.Grid)
        columns(GridTrack.Repeat(3, GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1))))
        gap(Tokens.SPACE_4.value())
        margin((20).px, 0.px)
    }
    style(child(c(AdditionalStyleClass.COMPARISON), t(SelectorTag.SECTION))) { minWidth(0.px) }
    style(c(LabClass.EVIDENCE_STRIP)) {
        edge(CssProperty.BORDER_TOP, (1).px, LineStyle.Solid, Tokens.BORDER.value())
        paddingTop((20).px)
        margin((20).px, 0.px)
    }
    style(c(LabClass.EVIDENCE_COLUMNS)) {
        display(DisplayStyle.Grid)
        columns(GridTrack.Repeat(4, GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1))))
        gap(Tokens.SPACE_5.value())
    }
    style(desc(c(LabClass.EVIDENCE_COLUMNS), t(SelectorTag.P))) {
        fontSize((13).px)
        color(Tokens.MUTED.value())
    }
    style(desc(c(LabClass.EVIDENCE_COLUMNS), t(SelectorTag.H3))) { margin(0.px, 0.px, (8).px) }
    style(c(LabClass.STEP_CONTROLS)) {
        display(DisplayStyle.Flex)
        justifyContent(JustifyContent.SpaceBetween)
        gap(Tokens.SPACE_3.value())
        margin((20).px, 0.px)
    }
    style(anyOf(c(LabClass.GUIDED_PROGRESS), c(LabClass.GUIDED_SUMMARY))) {
        color(Tokens.ACCENT.value())
        fontSize((14).px)
        margin((10).px, 0.px, (20).px)
    }
    style(c(LabClass.FOOTER)) {
        edge(CssProperty.BORDER_TOP, (1).px, LineStyle.Solid, Tokens.BORDER.value())
        padding((18).px, (24).px)
        font(FontSpec((11).px, FontStack.MONO))
        color(Tokens.MUTED.value())
        display(DisplayStyle.Flex)
        justifyContent(JustifyContent.SpaceBetween)
        gap(Tokens.SPACE_4.value())
        flexWrap(FlexWrap.Wrap)
    }
    style(desc(c(LabClass.FOOTER), t(SelectorTag.A))) { color(Tokens.MUTED.value()) }
    style(c(LabClass.STATUS_DOT)) { color(Tokens.ACCENT.value()) }
    style(c(LabClass.LOADING)) {
        padding((24).px)
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius((8).px)
        background(Tokens.SURFACE.value())
    }
    media(DesignMedia.PREFERS_REDUCED_MOTION_NO_PREFERENCE.query) {
        style(
            anyOf(
                t(SelectorTag.BUTTON),
                t(SelectorTag.A),
                t(SelectorTag.INPUT),
                t(SelectorTag.SELECT),
            )
        ) {
            composite(
                CssProperty.TRANSITION,
                "background-color .14s ease,border-color .14s ease,color .14s ease",
            )
        }
        style(allOf(c(LabClass.EXPERIMENT_PANEL), negate(attribute(SelectorAttribute.HIDDEN)))) {
            animation(MotionKeyframe.PANEL_ENTER) {
                duration((0.18).s)
                timingFunction(AnimationTimingFunction.EaseOut)
            }
        }
        frames(MotionKeyframe.PANEL_ENTER) {
            each(0.percent) {
                opacity(0.55)
                transform { translateY((3).px) }
            }
            each(100.percent) {
                opacity(1)
                setTransform(TransformValue.NONE)
            }
        }
    }
    media(CSSMediaQuery.MediaFeature("max-width", (1100).px)) {
        style(c(LabClass.BRAND_SUBTITLE)) { display(DisplayStyle.None) }
        style(c(LabClass.SITE_HEADER)) { gap((14).px) }
        style(desc(c(LabClass.SITE_HEADER), c(LabClass.NAV_ITEM))) { padding((10).px) }
        style(desc(c(LabClass.THEME_CHOOSER), t(SelectorTag.LABEL))) { display(DisplayStyle.None) }
        style(c(LabClass.WORKSPACE)) {
            columns(
                GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1.8)),
                GridTrack.MinMax(GridTrack.Fixed((260).px), GridTrack.Fraction(1)),
            )
        }
        style(c(LabClass.SECTION_HEADING)) { flexWrap(FlexWrap.Wrap) }
        style(c(LabClass.EVIDENCE_COLUMNS)) {
            columns(
                GridTrack.Repeat(2, GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1)))
            )
        }
    }
    media(CSSMediaQuery.MediaFeature("max-width", (760).px)) {
        style(c(LabClass.SITE_HEADER)) {
            display(DisplayStyle.Grid)
            columns(GridTrack.Fraction(1), GridTrack.Auto)
            padding((14).px, (16).px, (8).px)
            gap(Tokens.SPACE_3.value())
        }
        style(desc(c(LabClass.SITE_HEADER), t(SelectorTag.NAV))) {
            composite(CssProperty.GRID_COLUMN, "1/-1")
            composite(CssProperty.GRID_ROW, "2")
            display(DisplayStyle.Grid)
            columns(
                GridTrack.Repeat(3, GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1)))
            )
            width((100).percent)
            gap((4).px)
        }
        style(desc(c(LabClass.SITE_HEADER), c(LabClass.NAV_ITEM))) {
            fontSize((12).px)
            setWhiteSpace(WhiteSpaceValue.NORMAL)
            padding((10).px, (4).px)
            set(CssProperty.LINE_HEIGHT, 1.3)
        }
        style(c(LabClass.BRAND)) { fontSize((15).px) }
        style(desc(c(LabClass.THEME_CHOOSER), t(SelectorTag.SELECT))) {
            minHeight(Tokens.CONTROL_HEIGHT.value())
        }
        style(c(LabClass.MAIN)) { padding((24).px, (16).px, (32).px) }
        style(anyOf(c(LabClass.WORKSPACE), c(AdditionalStyleClass.COMPARISON))) {
            columns(GridTrack.Fraction(1))
        }
        style(anyOf(c(LabClass.GRAPH_AREA), c(LabClass.CONTROLS))) { padding((16).px) }
        style(c(LabClass.GRAPH_AREA)) { order(0) }
        style(c(LabClass.CONTROLS)) { order(1) }
        style(c(LabClass.DERIVATION)) {
            columns(GridTrack.Fraction(1))
            gap(Tokens.SPACE_5.value())
        }
        style(c(LabClass.DERIVATION_STEP)) {
            edge(CssProperty.BORDER_BOTTOM, (1).px, LineStyle.Solid, Tokens.BORDER.value())
            paddingBottom((18).px)
        }
        style(allOf(c(LabClass.DERIVATION_STEP), pseudo(SelectorPseudo.LAST_CHILD))) {
            composite(CssProperty.BORDER, "0")
            paddingBottom(0.px)
        }
        style(c(AdditionalStyleClass.PREDICTION)) {
            alignItems(AlignItems.FlexStart)
            flexDirection(FlexDirection.Column)
            gap(Tokens.SPACE_3.value())
        }
        style(c(LabClass.SECTION_HEADING)) { gap((4).px) }
        style(desc(c(LabClass.SECTION_HEADING), c(LabClass.MODE_SWITCH))) {
            margin((8).px, 0.px, (16).px)
        }
        style(c(LabClass.SUBTITLE)) { fontSize((14).px) }
        style(c(LabClass.FORMULA)) { fontSize((21).px) }
        style(c(LabClass.FOOTER)) { padding((16).px) }
        style(c(AdditionalStyleClass.RANGE_ROW)) { fontSize((10).px) }
        style(c(LabClass.RESULT_VALUE)) { fontSize((30).px) }
    }
    media(DesignMedia.PREFERS_CONTRAST_MORE.query) {
        style(pseudo(SelectorPseudo.ROOT)) { setToken(Tokens.BORDER, Tokens.MUTED.value()) }
        style(anyOf(t(SelectorTag.BUTTON), t(SelectorTag.INPUT), t(SelectorTag.SELECT))) {
            important { borderWidth((2).px) }
        }
    }
}
