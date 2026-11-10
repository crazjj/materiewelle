@file:OptIn(org.jetbrains.compose.web.ExperimentalComposeWebApi::class)

package materiewelle.presentation.styles

import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.css.keywords.auto

/** Canonical Kotlin style definitions, using shared DOM identities and typed values. */
internal fun StyleRules.experimentsStyles() {
    style(c(LabClass.LAB_WORKSPACE)) {
        display(DisplayStyle.Grid)
        columns(
            GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(2.7)),
            GridTrack.MinMax(GridTrack.Fixed((280).px), GridTrack.Fraction(1)),
        )
        gap((16).px)
        alignItems(AlignItems.Start)
        margin((18).px, 0.px)
    }
    style(desc(c(LabClass.LAB_WORKSPACE), c(LabClass.LAB_PANEL))) {
        minWidth(0.px)
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius((7).px)
        background(Tokens.SURFACE.value())
        setOverflow(OverflowValue.HIDDEN)
    }
    style(desc(c(LabClass.LAB_WORKSPACE), c(LabClass.GRAPH_AREA))) { padding(0.px) }
    style(c(LabClass.PANEL_TOOLBAR)) {
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        justifyContent(JustifyContent.SpaceBetween)
        gap((12).px)
        padding((12).px, (16).px)
        edge(CssProperty.BORDER_BOTTOM, (1).px, LineStyle.Solid, Tokens.BORDER.value())
        minHeight((63).px)
    }
    style(desc(c(LabClass.PANEL_TOOLBAR), c(LabClass.MODE_SWITCH))) { margin(0.px) }
    style(
        anyOf(
            desc(c(LabClass.LAB_WORKSPACE), c(LabClass.CONTROLS)),
            desc(c(LabClass.LAB_WORKSPACE), c(LabClass.DG_DATA)),
        )
    ) {
        padding(0.px)
        edge(CssProperty.BORDER_LEFT, (1).px, LineStyle.Solid, Tokens.BORDER.value())
    }
    style(c(LabClass.PANEL_LABEL)) {
        margin(0.px)
        padding((17).px, (18).px)
        edge(CssProperty.BORDER_BOTTOM, (1).px, LineStyle.Solid, Tokens.BORDER.value())
        fontFamily(FontStack.MONO)
        fontSize((12).px)
        letterSpacing((0.14).em)
        set(CssProperty.LINE_HEIGHT, 1.4)
    }
    style(c(LabClass.CONTROL_BODY)) { padding((19).px, (18).px) }
    style(c(LabClass.CONTROL_LABEL)) {
        fontFamily(FontStack.MONO)
        fontSize((10).px)
        set(CssProperty.LINE_HEIGHT, 1.5)
        letterSpacing((0.11).em)
        color(Tokens.MUTED.value())
        margin(0.px, 0.px, (10).px)
    }
    style(desc(c(LabClass.LAB_WORKSPACE), t(SelectorTag.LABEL))) {
        fontFamily(FontStack.TEXT)
        fontWeight(500)
        fontSize((12).px)
        set(CssProperty.LINE_HEIGHT, 1.5)
        margin(0.px, 0.px, (10).px)
        color(Tokens.MUTED.value())
    }
    style(desc(c(LabClass.LAB_WORKSPACE), c(LabClass.NUMBER_ROW))) {
        display(DisplayStyle.Flex)
        gap(0.px)
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius((5).px)
        setOverflow(OverflowValue.HIDDEN)
        background(Tokens.SURFACE_RAISED.value())
    }
    style(
        desc(
            c(LabClass.LAB_WORKSPACE),
            allOf(c(LabClass.NUMBER_ROW), pseudo(SelectorPseudo.FOCUS_WITHIN)),
        )
    ) {
        edge(CssProperty.OUTLINE, (2).px, LineStyle.Solid, Tokens.ACCENT.value())
        outlineOffset((3).px)
    }
    style(
        desc(
            desc(desc(c(LabClass.LAB_WORKSPACE), c(LabClass.CONTROL_BODY)), c(LabClass.NUMBER_ROW)),
            t(SelectorTag.INPUT),
        )
    ) {
        composite(CssProperty.FLEX, "1")
        minWidth(0.px)
        width((100).percent)
        composite(CssProperty.BORDER, "0")
        borderRadius(0.px)
        background(Color.transparent)
        color(Tokens.TEXT.value())
        padding((10).px, (12).px)
        fontFamily(FontStack.MONO)
        fontSize((25).px)
        minHeight((49).px)
    }
    style(
        desc(
            desc(c(LabClass.LAB_WORKSPACE), c(LabClass.NUMBER_ROW)),
            allOf(t(SelectorTag.INPUT), pseudo(SelectorPseudo.FOCUS_VISIBLE)),
        )
    ) {
        composite(CssProperty.OUTLINE, "0")
    }
    style(desc(c(LabClass.LAB_WORKSPACE), c(LabClass.UNIT))) {
        alignSelf(AlignSelf.Stretch)
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        padding(0.px, (14).px)
        composite(CssProperty.BORDER, "0")
        edge(CssProperty.BORDER_LEFT, (1).px, LineStyle.Solid, Tokens.BORDER.value())
        background(Color.transparent)
        color(Tokens.MUTED.value())
        fontFamily(FontStack.MONO)
        fontSize((17).px)
    }
    style(
        desc(
            c(LabClass.LAB_WORKSPACE),
            allOf(t(SelectorTag.INPUT), attribute(SelectorAttribute.TYPE, AttributeValue.RANGE)),
        )
    ) {
        width((100).percent)
        minWidth(0.px)
        set(CssProperty.ACCENT_COLOR, Tokens.ACCENT.value())
        height((30).px)
        margin((9).px, 0.px, 0.px)
        setCursor(CursorValue.POINTER)
    }
    style(c(LabClass.RANGE_LABELS)) {
        display(DisplayStyle.Flex)
        justifyContent(JustifyContent.SpaceBetween)
        fontFamily(FontStack.MONO)
        fontSize((11).px)
        color(Tokens.MUTED.value())
        margin(0.px, 0.px, (23).px)
    }
    style(c(LabClass.QUICK_VALUES)) {
        display(DisplayStyle.Flex)
        gap((7).px)
        marginBottom((22).px)
    }
    style(desc(c(LabClass.QUICK_VALUES), t(SelectorTag.BUTTON))) {
        minWidth(0.px)
        composite(CssProperty.FLEX, "1")
        padding((8).px, (3).px)
        fontFamily(FontStack.MONO)
        fontSize((12).px)
    }
    style(
        desc(
            c(LabClass.LAB_WORKSPACE),
            allOf(
                c(LabClass.SECONDARY),
                attribute(SelectorAttribute.ARIA_PRESSED, AttributeValue.TRUE),
            ),
        )
    ) {
        borderColor(Tokens.ACCENT.value())
        color(Tokens.ACCENT.value())
        background(Tokens.ACCENT_SOFT.value())
    }
    style(desc(c(LabClass.LAB_WORKSPACE), c(LabClass.RESULT))) {
        marginTop(0.px)
        padding((16).px, 0.px)
        edge(CssProperty.BORDER_TOP, (1).px, LineStyle.Solid, Tokens.BORDER.value())
    }
    style(desc(desc(c(LabClass.LAB_WORKSPACE), c(LabClass.RESULT)), c(LabClass.CONTROL_LABEL))) {
        fontSize((10).px)
        margin(0.px, 0.px, (9).px)
    }
    style(desc(c(LabClass.LAB_WORKSPACE), c(LabClass.RESULT_VALUE))) {
        fontFamily(FontStack.MONO)
        setFontVariantNumeric(FontVariantNumericValue.TABULAR_NUMS)
        color(Tokens.TEXT.value())
        composite(CssProperty.FONT_SIZE, "clamp(23px,2.25vw,32px)")
        fontWeight(600)
        letterSpacing((-0.055).em)
        set(CssProperty.LINE_HEIGHT, 1.2)
        margin(0.px, 0.px, (8).px)
        setOverflowWrap(OverflowWrapValue.ANYWHERE)
    }
    style(desc(c(LabClass.LAB_WORKSPACE), c(LabClass.SI_VALUE))) {
        fontFamily(FontStack.MONO)
        fontSize((11).px)
        color(Tokens.MUTED.value())
        margin(0.px)
    }
    style(c(LabClass.CHART_HEADING)) {
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        justifyContent(JustifyContent.SpaceBetween)
        gap((12).px)
        padding((17).px, (17).px, (2).px)
        minHeight((49).px)
    }
    style(desc(c(LabClass.CHART_HEADING), c(LabClass.AXIS_TITLE))) {
        fontFamily(FontStack.TEXT)
        fontSize((13).px)
        color(Tokens.TEXT.value())
        margin(0.px)
    }
    style(c(LabClass.CHART_LEGEND)) {
        display(DisplayStyle.Flex)
        flexWrap(FlexWrap.Wrap)
        alignItems(AlignItems.Center)
        justifyContent(JustifyContent.FlexEnd)
        gap((11).px)
        fontSize((10).px)
        color(Tokens.MUTED.value())
    }
    style(
        anyOf(
            allOf(c(LabClass.LEGEND_CURRENT), pseudo(SelectorPseudo.BEFORE)),
            allOf(c(LabClass.LEGEND_REFERENCE), pseudo(SelectorPseudo.BEFORE)),
            allOf(c(LabClass.LEGEND_PLAN), pseudo(SelectorPseudo.BEFORE)),
        )
    ) {
        composite(CssProperty.CONTENT, "\"\"")
        display(DisplayStyle.InlineBlock)
        width((7).px)
        height((7).px)
        marginRight((6).px)
        borderRadius((50).percent)
        background(Tokens.ACCENT.value())
    }
    style(allOf(c(LabClass.LEGEND_REFERENCE), pseudo(SelectorPseudo.BEFORE))) {
        composite(CssProperty.BACKGROUND, "var(--reference,#b57617)")
    }
    style(allOf(c(LabClass.LEGEND_PLAN), pseudo(SelectorPseudo.BEFORE))) {
        background(Color.transparent)
        border((1.5).px, LineStyle.Solid, Tokens.TEXT.value())
    }
    style(c(LabClass.ICON_BUTTON)) {
        display(DisplayStyle.LegacyInlineFlex)
        alignItems(AlignItems.Center)
        justifyContent(JustifyContent.Center)
        minHeight((32).px)
        minWidth((32).px)
        padding((2).px)
        border((1).px, LineStyle.Solid, Color.transparent)
        borderRadius((4).px)
        background(Color.transparent)
        color(Tokens.MUTED.value())
        fontSize((20).px)
        set(CssProperty.LINE_HEIGHT, 1)
    }
    style(allOf(c(LabClass.ICON_BUTTON), pseudo(SelectorPseudo.HOVER))) {
        background(Tokens.SURFACE_RAISED.value())
        borderColor(Tokens.BORDER.value())
        color(Tokens.TEXT.value())
    }
    style(c(LabClass.INTERACTIVE_CHART)) {
        position(Position.Relative)
        borderRadius((4).px)
        outlineOffset((-4).px)
        setTouchAction(TouchActionValue.PAN_Y)
        setCursor(CursorValue.CROSSHAIR)
    }
    style(desc(desc(c(LabClass.LAB_WORKSPACE), c(LabClass.CHART)), t(SelectorTag.SVG))) {
        display(DisplayStyle.Block)
        width((100).percent)
        height(auto)
        fontFamily(FontStack.TEXT)
    }
    style(c(LabClass.CHART_READOUT)) {
        display(DisplayStyle.Flex)
        gap((15).px)
        flexWrap(FlexWrap.Wrap)
        edge(CssProperty.BORDER_TOP, (1).px, LineStyle.Solid, Tokens.BORDER.value())
        padding((12).px, (17).px, (8).px)
        fontFamily(FontStack.MONO)
        fontSize((12).px)
        set(CssProperty.LINE_HEIGHT, 1.5)
        setFontVariantNumeric(FontVariantNumericValue.TABULAR_NUMS)
    }
    style(
        child(
            c(LabClass.CHART_READOUT),
            allOf(t(SelectorTag.SPAN), pseudo(SelectorPseudo.FIRST_CHILD)),
        )
    ) {
        color(Tokens.MUTED.value())
        fontSize((10).px)
        letterSpacing((0.08).em)
        alignSelf(AlignSelf.Center)
        setMarginRight(MarginRightValue.AUTO)
    }
    style(desc(c(LabClass.LAB_WORKSPACE), c(LabClass.GRAPH_CAPTION))) {
        margin(0.px)
        padding(0.px, (17).px, (15).px)
        color(Tokens.MUTED.value())
        fontSize((11).px)
        set(CssProperty.LINE_HEIGHT, 1.5)
    }
    style(c(LabClass.COMPARISON_DISPLAY)) {
        edge(CssProperty.BORDER_TOP, (1).px, LineStyle.Solid, Tokens.BORDER.value())
        paddingTop((17).px)
    }
    style(c(LabClass.COMPARISON_LINE)) {
        display(DisplayStyle.Flex)
        justifyContent(JustifyContent.SpaceBetween)
        gap((6).px)
        fontFamily(FontStack.MONO)
        fontSize((12).px)
        set(CssProperty.LINE_HEIGHT, 1.5)
        setFontVariantNumeric(FontVariantNumericValue.TABULAR_NUMS)
    }
    style(c(LabClass.REFERENCE_TEXT)) { composite(CssProperty.COLOR, "var(--reference,#b57617)") }
    style(c(LabClass.RATIO_READOUT)) {
        fontFamily(FontStack.MONO)
        fontSize((13).px)
        padding((10).px, 0.px, 0.px)
        margin(0.px)
        color(Tokens.MUTED.value())
    }
    style(c(LabClass.MODEL_STRIP)) {
        display(DisplayStyle.Grid)
        columns(GridTrack.Fraction(1), GridTrack.Auto, GridTrack.Fraction(1))
        alignItems(AlignItems.Center)
        gap((24).px)
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius((6).px)
        background(Tokens.SURFACE.value())
        padding((21).px, (22).px)
        margin((15).px, 0.px)
    }
    style(desc(c(LabClass.MODEL_STRIP), t(SelectorTag.P))) {
        margin(0.px)
        fontSize((13).px)
    }
    style(desc(c(LabClass.MODEL_STRIP), c(LabClass.CONTROL_LABEL))) {
        fontSize((10).px)
        marginBottom((7).px)
    }
    style(child(c(LabClass.MODEL_STRIP), c(LabClass.FORMULA))) {
        composite(CssProperty.BORDER, "0")
        background(Color.transparent)
        padding(0.px, (24).px)
        fontSize((26).px)
    }
    style(child(c(LabClass.MODEL_STRIP), c(LabClass.SMALL))) {
        edge(CssProperty.BORDER_LEFT, (1).px, LineStyle.Solid, Tokens.BORDER.value())
        paddingLeft((24).px)
        fontSize((12).px)
    }
    style(c(LabClass.GUIDED_PANEL)) {
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius((6).px)
        padding((20).px)
        background(Tokens.SURFACE.value())
        margin((16).px, 0.px)
    }
    style(desc(c(LabClass.GUIDED_PANEL), c(LabClass.DERIVATION_STEP))) {
        composite(CssProperty.BORDER, "0")
    }
    style(desc(c(LabClass.GUIDED_PANEL), c(LabClass.STEP_CONTROLS))) { margin((15).px, 0.px, 0.px) }
    style(c(LabClass.PLOT_SWITCH)) {
        display(DisplayStyle.Flex)
        gap((3).px)
        flexWrap(FlexWrap.Wrap)
    }
    style(desc(c(LabClass.PLOT_SWITCH), t(SelectorTag.BUTTON))) {
        padding((8).px, (9).px)
        minHeight((36).px)
        background(Color.transparent)
        border((1).px, LineStyle.Solid, Color.transparent)
        borderRadius((4).px)
        fontSize((12).px)
        color(Tokens.MUTED.value())
    }
    style(
        desc(
            c(LabClass.PLOT_SWITCH),
            allOf(
                t(SelectorTag.BUTTON),
                attribute(SelectorAttribute.ARIA_PRESSED, AttributeValue.TRUE),
            ),
        )
    ) {
        color(Tokens.ACCENT.value())
        borderColor(Tokens.BORDER.value())
        background(Tokens.ACCENT_SOFT.value())
    }
    style(
        desc(c(LabClass.PLOT_SWITCH), allOf(t(SelectorTag.BUTTON), pseudo(SelectorPseudo.HOVER)))
    ) {
        color(Tokens.TEXT.value())
        background(Tokens.SURFACE_RAISED.value())
    }
    style(desc(c(LabClass.LAB_WORKSPACE), c(LabClass.DATA_POINTS))) {
        display(DisplayStyle.Grid)
        columns(GridTrack.Repeat(3, GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1))))
        gap((7).px)
        margin(0.px, 0.px, (18).px)
    }
    style(desc(c(LabClass.LAB_WORKSPACE), c(LabClass.DATA_CHOICE))) {
        fontFamily(FontStack.MONO)
        fontSize((12).px)
        padding((9).px, (5).px)
        minWidth(0.px)
        minHeight((40).px)
    }
    style(desc(c(LabClass.LAB_WORKSPACE), allOf(c(LabClass.DATA_CHOICE), c(LabClass.CHOSEN)))) {
        borderColor(Tokens.ACCENT.value())
        background(Tokens.ACCENT_SOFT.value())
        color(Tokens.ACCENT.value())
    }
    style(desc(c(LabClass.LAB_WORKSPACE), c(LabClass.CALCULATION))) {
        padding((14).px, 0.px)
        edge(CssProperty.BORDER_TOP, (1).px, LineStyle.Solid, Tokens.BORDER.value())
        composite(CssProperty.BORDER_BOTTOM, "0")
        margin(0.px)
    }
    style(desc(desc(c(LabClass.LAB_WORKSPACE), c(LabClass.CALCULATION)), t(SelectorTag.H2))) {
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Baseline)
        gap((9).px)
        fontSize((14).px)
        set(CssProperty.LINE_HEIGHT, 1.4)
        margin(0.px, 0.px, (7).px)
    }
    style(desc(c(LabClass.LAB_WORKSPACE), c(LabClass.CALCULATION_NUMBER))) {
        fontFamily(FontStack.MONO)
        fontSize((11).px)
        letterSpacing((0.08).em)
        color(Tokens.ACCENT.value())
        background(Color.transparent)
        composite(CssProperty.BORDER, "0")
        borderRadius(0.px)
        width(auto)
        height(auto)
    }
    style(c(LabClass.CALCULATION_INPUT)) {
        color(Tokens.MUTED.value())
        fontFamily(FontStack.MONO)
        fontSize((10).px)
        set(CssProperty.LINE_HEIGHT, 1.6)
        margin(0.px, 0.px, (8).px)
    }
    style(
        desc(desc(c(LabClass.LAB_WORKSPACE), c(LabClass.CALCULATION)), c(LabClass.COMPARE_VALUE))
    ) {
        padding(0.px)
        margin(0.px)
        background(Color.transparent)
        color(Tokens.TEXT.value())
        fontFamily(FontStack.MONO)
        fontSize((26).px)
        fontWeight(600)
        letterSpacing((-0.04).em)
        setFontVariantNumeric(FontVariantNumericValue.TABULAR_NUMS)
        setOverflowWrap(OverflowWrapValue.ANYWHERE)
    }
    style(desc(desc(c(LabClass.LAB_WORKSPACE), c(LabClass.CALCULATION)), c(LabClass.SMALL))) {
        fontSize((12).px)
        margin((5).px, 0.px, 0.px)
    }
    style(desc(c(LabClass.LAB_WORKSPACE), c(LabClass.DIFFERENCE))) {
        display(DisplayStyle.Flex)
        justifyContent(JustifyContent.SpaceBetween)
        alignItems(AlignItems.Baseline)
        gap((12).px)
        edge(CssProperty.BORDER_TOP, (1).px, LineStyle.Solid, Tokens.BORDER.value())
        paddingTop((15).px)
    }
    style(desc(desc(c(LabClass.LAB_WORKSPACE), c(LabClass.DIFFERENCE)), t(SelectorTag.H2))) {
        fontFamily(FontStack.TEXT)
        fontSize((12).px)
        fontWeight(500)
        color(Tokens.MUTED.value())
        margin(0.px)
    }
    style(desc(desc(c(LabClass.LAB_WORKSPACE), c(LabClass.DIFFERENCE)), t(SelectorTag.STRONG))) {
        fontFamily(FontStack.MONO)
        fontSize((22).px)
        setFontVariantNumeric(FontVariantNumericValue.TABULAR_NUMS)
        color(Tokens.ACCENT.value())
    }
    style(adjacent(desc(c(LabClass.LAB_WORKSPACE), c(LabClass.DIFFERENCE)), c(LabClass.SMALL))) {
        fontSize((10).px)
        set(CssProperty.LINE_HEIGHT, 1.6)
        margin((8).px, 0.px, 0.px)
    }
    style(c(LabClass.SAVED_SETTING)) {
        display(DisplayStyle.Flex)
        flexWrap(FlexWrap.Wrap)
        gap((8).px, (22).px)
        padding((14).px, (17).px)
        edge(CssProperty.BORDER_TOP, (1).px, LineStyle.Solid, Tokens.BORDER.value())
        fontFamily(FontStack.MONO)
        fontSize((11).px)
        set(CssProperty.LINE_HEIGHT, 1.5)
    }
    style(
        child(
            c(LabClass.SAVED_SETTING),
            allOf(t(SelectorTag.SPAN), pseudo(SelectorPseudo.FIRST_CHILD)),
        )
    ) {
        letterSpacing((0.07).em)
    }
    media(
        CSSMediaQuery.And(
            mutableListOf(
                CSSMediaQuery.MediaFeature("min-width", (721).px),
                CSSMediaQuery.MediaFeature("max-width", (1080).px),
            )
        )
    ) {
        style(c(LabClass.LAB_WORKSPACE)) {
            columns(
                GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1.8)),
                GridTrack.MinMax(GridTrack.Fixed((255).px), GridTrack.Fraction(1)),
            )
        }
        style(c(LabClass.PANEL_TOOLBAR)) { flexWrap(FlexWrap.Wrap) }
        style(c(LabClass.CHART_HEADING)) {
            alignItems(AlignItems.FlexStart)
            flexDirection(FlexDirection.Column)
        }
        style(c(LabClass.CHART_LEGEND)) { justifyContent(JustifyContent.FlexStart) }
        style(c(LabClass.MODEL_STRIP)) { gap((16).px) }
        style(child(c(LabClass.MODEL_STRIP), c(LabClass.FORMULA))) {
            padding(0.px, (10).px)
            fontSize((23).px)
        }
        style(child(c(LabClass.MODEL_STRIP), c(LabClass.SMALL))) { paddingLeft((16).px) }
    }
    media(CSSMediaQuery.MediaFeature("max-width", (720).px)) {
        style(c(LabClass.LAB_WORKSPACE)) {
            display(DisplayStyle.Grid)
            columns(GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1)))
            gap((13).px)
        }
        style(desc(c(LabClass.LAB_WORKSPACE), c(LabClass.CONTROLS))) {
            setOrder(OrderValue.INITIAL)
        }
        style(c(LabClass.PANEL_TOOLBAR)) {
            gap((8).px)
            padding((10).px)
            flexWrap(FlexWrap.Wrap)
        }
        style(desc(c(LabClass.PANEL_TOOLBAR), c(LabClass.SECONDARY))) {
            padding((7).px, (10).px)
            fontSize((11).px)
            minHeight((36).px)
            minWidth(0.px)
        }
        style(c(LabClass.CHART_HEADING)) {
            padding((13).px, (11).px, 0.px)
            gap((8).px)
            alignItems(AlignItems.FlexStart)
            flexWrap(FlexWrap.Wrap)
        }
        style(desc(c(LabClass.CHART_HEADING), c(LabClass.AXIS_TITLE))) { fontSize((12).px) }
        style(c(LabClass.CHART_LEGEND)) {
            gap((8).px)
            fontSize((10).px)
        }
        style(c(LabClass.CHART_READOUT)) {
            gap((6).px, (14).px)
            padding((11).px)
            fontSize((11).px)
        }
        style(
            child(
                c(LabClass.CHART_READOUT),
                allOf(t(SelectorTag.SPAN), pseudo(SelectorPseudo.FIRST_CHILD)),
            )
        ) {
            set(CssProperty.FLEX_BASIS, (100).percent)
            fontSize((9).px)
        }
        style(desc(c(LabClass.LAB_WORKSPACE), c(LabClass.GRAPH_CAPTION))) {
            padding(0.px, (11).px, (12).px)
            fontSize((10).px)
        }
        style(desc(c(LabClass.LAB_WORKSPACE), c(LabClass.RESULT_VALUE))) { fontSize((34).px) }
        style(
            desc(
                desc(c(LabClass.LAB_WORKSPACE), c(LabClass.CALCULATION)),
                c(LabClass.COMPARE_VALUE),
            )
        ) {
            fontSize((29).px)
        }
        style(c(LabClass.COMPARISON_LINE)) { fontSize((14).px) }
        style(c(LabClass.MODEL_STRIP)) {
            columns(GridTrack.Fraction(1))
            padding((17).px)
            gap((14).px)
        }
        style(child(c(LabClass.MODEL_STRIP), c(LabClass.FORMULA))) {
            padding((3).px, 0.px)
            fontSize((26).px)
        }
        style(child(c(LabClass.MODEL_STRIP), c(LabClass.SMALL))) {
            composite(CssProperty.BORDER_LEFT, "0")
            edge(CssProperty.BORDER_TOP, (1).px, LineStyle.Solid, Tokens.BORDER.value())
            padding((12).px, 0.px, 0.px)
        }
        style(c(LabClass.SAVED_SETTING)) {
            padding((12).px)
            fontSize((11).px)
        }
        style(
            desc(
                desc(desc(c(LabClass.LAB_WORKSPACE), c(LabClass.CHART)), t(SelectorTag.SVG)),
                t(SelectorTag.TEXT),
            )
        ) {
            fontSize((26).px)
        }
        style(
            desc(
                desc(desc(c(LabClass.LAB_WORKSPACE), c(LabClass.CHART)), t(SelectorTag.SVG)),
                c(LabClass.CHART_AXIS_HEADING),
            )
        ) {
            fontSize((23).px)
        }
        style(
            desc(
                desc(desc(c(LabClass.LAB_WORKSPACE), c(LabClass.CHART)), t(SelectorTag.SVG)),
                c(LabClass.CHART_VALUE_LABEL),
            )
        ) {
            display(DisplayStyle.None)
        }
        style(desc(c(LabClass.PLOT_SWITCH), t(SelectorTag.BUTTON))) { fontSize((11).px) }
        style(c(LabClass.GUIDED_PANEL)) { padding((17).px) }
    }
    style(child(c(LabClass.SECTION_HEADING), c(LabClass.STATUS_TAG))) {
        font(FontSpec((10).px, FontStack.MONO))
        letterSpacing((0.1).em)
        set(CssProperty.LINE_HEIGHT, 1.6)
        padding((8).px, 0.px, (8).px, (13).px)
        edge(CssProperty.BORDER_LEFT, (2).px, LineStyle.Solid, Tokens.ACCENT.value())
        color(Tokens.MUTED.value())
        marginTop((20).px)
        setWhiteSpace(WhiteSpaceValue.NOWRAP)
    }
    media(CSSMediaQuery.MediaFeature("max-width", (720).px)) {
        style(child(c(LabClass.SECTION_HEADING), c(LabClass.STATUS_TAG))) {
            margin(0.px, 0.px, (8).px)
            padding((3).px, 0.px, (3).px, (10).px)
        }
        style(c(LabClass.LAB_WORKSPACE)) { marginTop((12).px) }
        style(
            desc(
                desc(desc(c(LabClass.LAB_WORKSPACE), c(LabClass.CHART)), t(SelectorTag.SVG)),
                c(LabClass.CHART_AXIS_HEADING),
            )
        ) {
            fontSize((22).px)
        }
    }
}
