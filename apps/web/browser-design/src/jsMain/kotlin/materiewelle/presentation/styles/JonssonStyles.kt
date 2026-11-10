@file:OptIn(org.jetbrains.compose.web.ExperimentalComposeWebApi::class)

package materiewelle.presentation.styles

import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.css.keywords.auto

/** Canonical Kotlin style definitions, using shared DOM identities and typed values. */
internal fun StyleRules.jonssonStyles() {
    style(c(LabClass.JONSSON_LAB)) {
        display(DisplayStyle.Grid)
        columns(
            GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1.5)),
            GridTrack.MinMax(GridTrack.Fixed((350).px), GridTrack.Fraction(1)),
        )
        gap((18).px)
        alignItems(AlignItems.Start)
    }
    style(
        anyOf(
            desc(c(LabClass.JONSSON_LAB), c(LabClass.LAB_PANEL)),
            c(LabClass.JONSSON_MODEL),
            c(LabClass.MEASUREMENT_RESULT),
        )
    ) {
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius((8).px)
        background(Tokens.SURFACE.value())
    }
    style(c(LabClass.JONSSON_IMAGING)) {
        padding((18).px)
        minWidth(0.px)
    }
    style(c(LabClass.JONSSON_CONTROLS)) {
        padding((20).px)
        minWidth(0.px)
    }
    style(anyOf(c(LabClass.JONSSON_PANEL_HEADING), c(LabClass.JONSSON_PROFILE_HEADING))) {
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        justifyContent(JustifyContent.SpaceBetween)
        gap((12).px)
    }
    style(
        anyOf(
            desc(c(LabClass.JONSSON_PANEL_HEADING), t(SelectorTag.H2)),
            desc(c(LabClass.JONSSON_PROFILE_HEADING), t(SelectorTag.H2)),
            desc(c(LabClass.JONSSON_CONTROLS), t(SelectorTag.H2)),
            desc(c(LabClass.JONSSON_MODEL), t(SelectorTag.H2)),
        )
    ) {
        font(FontSpec((16).px, FontStack.TEXT, weight = 600, lineHeight = 1.35))
        margin(0.px, 0.px, (7).px)
    }
    style(desc(c(LabClass.JONSSON_PANEL_HEADING), t(SelectorTag.P))) { margin(0.px) }
    style(c(LabClass.DATA_KIND)) {
        font(FontSpec((11).px, FontStack.MONO, lineHeight = 1.4))
        color(Tokens.MUTED.value())
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius((4).px)
        padding((5).px, (7).px)
        maxWidth((55).percent)
    }
    style(desc(c(LabClass.JONSSON_LAB), c(LabClass.MEASUREMENT_WINDOW))) {
        setOverflow(OverflowValue.AUTO)
        maxHeight((460).px)
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius((5).px)
        marginTop((16).px)
        background(Tokens.BG.value())
        setOverscrollBehavior(OverscrollBehaviorValue.CONTAIN)
    }
    style(c(LabClass.JONSSON_INTERACTIVE_IMAGE)) {
        set(CssProperty.LINE_HEIGHT, 0)
        position(Position.Relative)
        setTouchAction(TouchActionValue.PAN_Y)
        outlineOffset((-3).px)
        setCursor(CursorValue.CROSSHAIR)
    }
    style(allOf(c(LabClass.JONSSON_INTERACTIVE_IMAGE), attribute(SelectorAttribute.TABINDEX))) {
        setTouchAction(TouchActionValue.NONE)
    }
    style(desc(c(LabClass.JONSSON_INTERACTIVE_IMAGE), t(SelectorTag.SVG))) {
        display(DisplayStyle.Block)
        width((100).percent)
        height(auto)
        setMaxWidth(MaxWidthValue.NONE)
    }
    style(allOf(c(LabClass.JONSSON_INTERACTIVE_IMAGE), pseudo(SelectorPseudo.FOCUS_VISIBLE))) {
        edge(CssProperty.OUTLINE, (3).px, LineStyle.Solid, Tokens.ACCENT.value())
        zIndex(1)
    }
    style(c(LabClass.NATIVE_BAND)) {
        set(CssProperty.FILL, Tokens.ACCENT.value())
        set(CssProperty.FILL_OPACITY, 0.045)
        set(CssProperty.STROKE, Tokens.ACCENT.value())
        set(CssProperty.STROKE_WIDTH, 0.5)
        composite(CssProperty.STROKE_DASHARRAY, "2 2")
    }
    style(c(LabClass.NATIVE_REFERENCE)) {
        set(CssProperty.STROKE, Tokens.REFERENCE.value())
        set(CssProperty.STROKE_WIDTH, 0.6)
        composite(CssProperty.STROKE_DASHARRAY, "2 2")
    }
    style(anyOf(c(MeasurementClass.MARKER_HALO), c(MeasurementClass.REFERENCE_HALO))) {
        set(CssProperty.STROKE, Tokens.BG.value()); set(CssProperty.STROKE_WIDTH, 1.8)
    }
    style(c(MeasurementClass.REFERENCE_HALO)) { composite(CssProperty.STROKE_DASHARRAY, "2 2") }
    style(c(MeasurementClass.LABEL_PLATE)) { set(CssProperty.FILL, Tokens.BG.value()) }
    style(c(LabClass.NATIVE_OWN)) {
        set(CssProperty.STROKE, Tokens.ACCENT.value())
        set(CssProperty.STROKE_WIDTH, 0.65)
    }
    style(allOf(c(LabClass.NATIVE_OWN), c(LabClass.SELECTED))) {
        set(CssProperty.STROKE_WIDTH, 1.3)
    }
    style(c(LabClass.NATIVE_MARKER_LABEL)) {
        color(Tokens.ACCENT.value())
        set(CssProperty.FILL, Tokens.ACCENT.value())
        set(CssProperty.STROKE, Tokens.BG.value())
        set(CssProperty.STROKE_WIDTH, 0.5)
        composite(CssProperty.PAINT_ORDER, "stroke fill")
        font(FontSpec((6).px, FontStack.MONO, weight = 600))
    }
    style(c(LabClass.IMAGE_STATUS)) {
        display(DisplayStyle.Flex)
        justifyContent(JustifyContent.SpaceBetween)
        gap((12).px)
        flexWrap(FlexWrap.Wrap)
        font(FontSpec((11).px, FontStack.MONO, lineHeight = 1.5))
        color(Tokens.MUTED.value())
        padding((8).px, 0.px)
    }
    style(c(LabClass.MEASUREMENT_LEGEND)) {
        display(DisplayStyle.Flex)
        gap((18).px)
        flexWrap(FlexWrap.Wrap)
        fontSize((12).px)
        margin((7).px, 0.px, (13).px)
    }
    style(desc(c(LabClass.MEASUREMENT_LEGEND), t(SelectorTag.SPAN))) {
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        gap((7).px)
    }
    style(
        desc(
            c(LabClass.MEASUREMENT_LEGEND),
            allOf(t(SelectorTag.SPAN), pseudo(SelectorPseudo.BEFORE)),
        )
    ) {
        composite(CssProperty.CONTENT, "\"\"")
        width((17).px)
        edge(CssProperty.BORDER_TOP, (2).px, LineStyle.Solid, Tokens.ACCENT.value())
    }
    style(
        desc(
            c(LabClass.MEASUREMENT_LEGEND),
            allOf(c(LabClass.REFERENCE_KEY), pseudo(SelectorPseudo.BEFORE)),
        )
    ) {
        borderColor(Tokens.REFERENCE.value())
        setBorderTopStyle(BorderTopStyleValue.DASHED)
    }
    style(desc(c(LabClass.JONSSON_LAB), c(LabClass.MEASUREMENT_TOOLS))) {
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        gap((14).px)
        margin((12).px, 0.px)
    }
    style(
        desc(desc(c(LabClass.JONSSON_LAB), c(LabClass.MEASUREMENT_TOOLS)), t(SelectorTag.LABEL))
    ) {
        font(FontSpec((12).px, FontStack.TEXT))
        setWhiteSpace(WhiteSpaceValue.NOWRAP)
        margin(0.px)
    }
    style(
        desc(desc(c(LabClass.JONSSON_LAB), c(LabClass.MEASUREMENT_TOOLS)), t(SelectorTag.INPUT))
    ) {
        composite(CssProperty.FLEX, "1")
        minWidth((50).px)
        margin(0.px)
    }
    style(
        child(desc(c(LabClass.JONSSON_LAB), c(LabClass.MEASUREMENT_TOOLS)), t(SelectorTag.SPAN))
    ) {
        font(FontSpec((12).px, FontStack.MONO))
        setWhiteSpace(WhiteSpaceValue.NOWRAP)
    }
    style(desc(c(LabClass.JONSSON_LAB), c(LabClass.MEASUREMENT_TOGGLES))) {
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        flexWrap(FlexWrap.Wrap)
        gap((10).px)
    }
    style(
        desc(desc(c(LabClass.JONSSON_LAB), c(LabClass.MEASUREMENT_TOGGLES)), t(SelectorTag.LABEL))
    ) {
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        gap((5).px)
        fontSize((12).px)
        margin(0.px)
        minHeight((44).px)
        setWhiteSpace(WhiteSpaceValue.NOWRAP)
    }
    style(
        desc(
            desc(c(LabClass.JONSSON_LAB), c(LabClass.MEASUREMENT_TOGGLES)),
            allOf(t(SelectorTag.INPUT), attribute(SelectorAttribute.TYPE, AttributeValue.CHECKBOX)),
        )
    ) {
        width((16).px)
        height((16).px)
        set(CssProperty.ACCENT_COLOR, Tokens.ACCENT.value())
        margin(0.px)
    }
    style(
        desc(desc(c(LabClass.JONSSON_LAB), c(LabClass.MEASUREMENT_TOGGLES)), t(SelectorTag.BUTTON))
    ) {
        fontSize((12).px)
        padding((7).px, (10).px)
        minWidth(0.px)
    }
    style(desc(c(LabClass.JONSSON_LAB), i(Id.JONSSON_IMAGE_INSTRUCTIONS))) {
        margin((8).px, 0.px, (18).px)
    }
    style(c(LabClass.JONSSON_PROFILE_HEADING)) {
        edge(CssProperty.BORDER_TOP, (1).px, LineStyle.Solid, Tokens.BORDER.value())
        paddingTop((17).px)
        marginTop((17).px)
    }
    style(c(LabClass.GRAY_PROFILE)) { paddingTop((8).px) }
    style(desc(c(LabClass.GRAY_PROFILE), t(SelectorTag.SVG))) {
        width((100).percent)
        display(DisplayStyle.Block)
        height(auto)
    }
    style(c(LabClass.PROFILE_GRID)) {
        set(CssProperty.STROKE, Tokens.GRID.value())
        set(CssProperty.STROKE_WIDTH, 1)
    }
    style(c(LabClass.PROFILE_LABEL)) {
        color(Tokens.MUTED.value())
        set(CssProperty.FILL, Tokens.MUTED.value())
        font(FontSpec((12).px, FontStack.MONO))
    }
    style(c(LabClass.PROFILE_REFERENCE)) {
        set(CssProperty.STROKE, Tokens.REFERENCE.value())
        set(CssProperty.STROKE_WIDTH, 1)
        composite(CssProperty.STROKE_DASHARRAY, "3 4")
        opacity(0.65)
    }
    style(c(LabClass.PROFILE_CURVE)) {
        set(CssProperty.STROKE, Tokens.ACCENT.value())
        set(CssProperty.STROKE_WIDTH, 2)
        setFill(FillValue.NONE)
        setStrokeLinejoin(StrokeLinejoinValue.ROUND)
    }
    style(c(LabClass.PROFILE_OWN)) {
        set(CssProperty.FILL, Tokens.ACCENT.value())
        set(CssProperty.STROKE, Tokens.SURFACE.value())
        set(CssProperty.STROKE_WIDTH, 1)
    }
    style(c(LabClass.PROFILE_CURSOR)) {
        set(CssProperty.STROKE, Tokens.TEXT.value())
        set(CssProperty.STROKE_WIDTH, 1)
        composite(CssProperty.STROKE_DASHARRAY, "2 3")
        opacity(0.6)
    }
    style(c(LabClass.PROFILE_AXIS_LEGEND)) {
        display(DisplayStyle.Flex)
        justifyContent(JustifyContent.SpaceBetween)
        gap((10).px)
        flexWrap(FlexWrap.Wrap)
        font(FontSpec((16).px, FontStack.MONO))
        color(Tokens.MUTED.value())
    }
    style(c(LabClass.PROFILE_SAMPLE_ROW)) {
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        gap((10).px)
        margin((12).px, 0.px)
        flexWrap(FlexWrap.Wrap)
    }
    style(desc(c(LabClass.PROFILE_SAMPLE_ROW), t(SelectorTag.LABEL))) {
        fontSize((16).px)
        margin(0.px)
    }
    style(desc(c(LabClass.PROFILE_SAMPLE_ROW), t(SelectorTag.INPUT))) {
        composite(CssProperty.FLEX, "1")
        minWidth((70).px)
        maxWidth((260).px)
        margin(0.px)
    }
    style(desc(c(LabClass.PROFILE_SAMPLE_ROW), i(Id.PROFILE_SAMPLE))) {
        font(FontSpec((16).px, FontStack.MONO))
        color(Tokens.TEXT.value())
        setFontVariantNumeric(FontVariantNumericValue.TABULAR_NUMS)
    }
    style(desc(c(LabClass.JONSSON_LAB), c(LabClass.PROFILE_PICKER))) { margin((18).px, 0.px) }
    style(desc(desc(c(LabClass.JONSSON_LAB), c(LabClass.PROFILE_PICKER)), t(SelectorTag.LABEL))) {
        fontSize((12).px)
        marginBottom((8).px)
        display(DisplayStyle.Block)
    }
    style(desc(desc(c(LabClass.JONSSON_LAB), c(LabClass.PROFILE_PICKER)), t(SelectorTag.SELECT))) {
        background(Tokens.BG.value())
        color(Tokens.TEXT.value())
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius((5).px)
        width((100).percent)
        padding((10).px)
        font(FontSpec((13).px, FontStack.TEXT))
        minHeight((44).px)
    }
    style(desc(desc(c(LabClass.JONSSON_LAB), c(LabClass.PROFILE_PICKER)), t(SelectorTag.P))) {
        margin((9).px, 0.px, 0.px)
    }
    style(desc(c(LabClass.JONSSON_LAB), c(LabClass.MARKER_WORKSHOP))) {
        edge(CssProperty.BORDER_TOP, (1).px, LineStyle.Solid, Tokens.BORDER.value())
        paddingTop((17).px)
        marginTop((17).px)
    }
    style(desc(desc(c(LabClass.JONSSON_LAB), c(LabClass.MARKER_WORKSHOP)), t(SelectorTag.H3))) {
        font(FontSpec((13).px, FontStack.TEXT, weight = 600))
        margin(0.px, 0.px, (12).px)
    }
    style(child(desc(c(LabClass.JONSSON_LAB), c(LabClass.MARKER_WORKSHOP)), t(SelectorTag.LABEL))) {
        fontSize((12).px)
        display(DisplayStyle.Block)
        margin((14).px, 0.px, (8).px)
    }
    style(desc(c(LabClass.JONSSON_LAB), c(LabClass.MARKER_CHOICES))) {
        display(DisplayStyle.Grid)
        columns(GridTrack.Repeat(4, GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1))))
        gap((6).px)
        margin((10).px, 0.px)
    }
    style(desc(desc(c(LabClass.JONSSON_LAB), c(LabClass.MARKER_CHOICES)), t(SelectorTag.BUTTON))) {
        minWidth(0.px)
        minHeight((44).px)
        font(FontSpec((13).px, FontStack.MONO))
        padding((7).px, 0.px)
    }
    style(
        desc(
            desc(c(LabClass.JONSSON_LAB), c(LabClass.MARKER_CHOICES)),
            allOf(t(SelectorTag.BUTTON), c(LabClass.CHOSEN)),
        )
    ) {
        background(Tokens.ACCENT_SOFT.value())
        borderColor(Tokens.ACCENT.value())
        color(Tokens.ACCENT.value())
    }
    style(i(Id.MARKER_X)) {
        width((100).percent)
        background(Tokens.BG.value())
        color(Tokens.TEXT.value())
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius((5).px)
        font(FontSpec((22).px, FontStack.MONO))
        setFontVariantNumeric(FontVariantNumericValue.TABULAR_NUMS)
        padding((10).px, (12).px)
        minHeight((46).px)
    }
    style(allOf(i(Id.MARKER_X), attribute(SelectorAttribute.ARIA_INVALID, AttributeValue.TRUE))) {
        borderColor(Tokens.DANGER.value())
    }
    style(desc(c(LabClass.JONSSON_LAB), c(LabClass.MARKER_ACTIONS))) {
        display(DisplayStyle.Flex)
        gap((6).px)
        flexWrap(FlexWrap.Wrap)
        margin((10).px, 0.px)
    }
    style(desc(desc(c(LabClass.JONSSON_LAB), c(LabClass.MARKER_ACTIONS)), t(SelectorTag.BUTTON))) {
        fontSize((12).px)
        minWidth(0.px)
        padding((8).px, (10).px)
        composite(CssProperty.FLEX, "1")
        setWhiteSpace(WhiteSpaceValue.NOWRAP)
    }
    style(desc(c(LabClass.JONSSON_LAB), c(LabClass.JONSSON_COMPARISON_ACTIONS))) {
        edge(CssProperty.BORDER_TOP, (1).px, LineStyle.Solid, Tokens.BORDER.value())
        paddingTop((14).px)
        marginTop((18).px)
    }
    style(desc(desc(c(LabClass.JONSSON_LAB), c(LabClass.MARKER_WORKSHOP)), t(SelectorTag.P))) {
        fontSize((12).px)
        set(CssProperty.LINE_HEIGHT, 1.55)
    }
    style(desc(c(LabClass.JONSSON_LAB), c(LabClass.INPUT_ERROR))) { color(Tokens.DANGER.value()) }
    style(c(LabClass.MEASUREMENT_RESULT)) {
        padding((20).px)
        marginTop((18).px)
    }
    style(c(LabClass.MEASUREMENT_SUMMARY)) {
        display(DisplayStyle.Flex)
        gap((28).px)
        alignItems(AlignItems.Center)
        flexWrap(FlexWrap.Wrap)
        justifyContent(JustifyContent.SpaceBetween)
    }
    style(desc(c(LabClass.MEASUREMENT_SUMMARY), t(SelectorTag.H2))) {
        font(FontSpec((17).px, FontStack.TEXT, weight = 600))
        margin((8).px, 0.px)
    }
    style(desc(c(LabClass.MEASUREMENT_SUMMARY), t(SelectorTag.P))) { margin((5).px, 0.px) }
    style(c(LabClass.MEASUREMENT_DISPLAY)) {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        gap((6).px)
        edge(CssProperty.BORDER_LEFT, (2).px, LineStyle.Solid, Tokens.ACCENT.value())
        paddingLeft((16).px)
        font(FontSpec((12).px, FontStack.TEXT))
        color(Tokens.MUTED.value())
    }
    style(
        anyOf(
            desc(c(LabClass.MEASUREMENT_DISPLAY), t(SelectorTag.STRONG)),
            desc(c(LabClass.MEASUREMENT_RESULT), i(Id.JONSSON_SPACING)),
        )
    ) {
        font(FontSpec((30).px, FontStack.MONO, weight = 500))
        setFontVariantNumeric(FontVariantNumericValue.TABULAR_NUMS)
        color(Tokens.ACCENT.value())
    }
    style(c(LabClass.REFERENCE_DISPLAY)) { borderColor(Tokens.REFERENCE.value()) }
    style(desc(c(LabClass.REFERENCE_DISPLAY), t(SelectorTag.STRONG))) {
        color(Tokens.REFERENCE.value())
        fontSize((23).px)
    }
    style(desc(c(LabClass.MEASUREMENT_RESULT), c(LabClass.INTERVAL_LIST))) {
        setListStyle(ListStyleValue.NONE)
        padding(0.px)
        display(DisplayStyle.Grid)
        columns(
            GridTrack.AutoFit(GridTrack.MinMax(GridTrack.Fixed((62).px), GridTrack.Fraction(1)))
        )
        gap((7).px)
        margin((18).px, 0.px)
    }
    style(
        desc(desc(c(LabClass.MEASUREMENT_RESULT), c(LabClass.INTERVAL_LIST)), t(SelectorTag.LI))
    ) {
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        background(Tokens.BG.value())
        borderRadius((5).px)
        padding((9).px, (4).px)
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        flexDirection(FlexDirection.Column)
        gap((7).px)
        fontSize((11).px)
        color(Tokens.MUTED.value())
    }
    style(
        desc(desc(c(LabClass.MEASUREMENT_RESULT), c(LabClass.INTERVAL_LIST)), t(SelectorTag.STRONG))
    ) {
        font(FontSpec((16).px, FontStack.MONO, weight = 500))
        color(Tokens.TEXT.value())
    }
    style(desc(c(LabClass.MEASUREMENT_RESULT), c(LabClass.MEASUREMENT_INSIGHT))) {
        fontSize((13).px)
        color(Tokens.MUTED.value())
        edge(CssProperty.BORDER_LEFT, (2).px, LineStyle.Solid, Tokens.BORDER.value())
        paddingLeft((12).px)
    }
    style(c(LabClass.JONSSON_MODEL)) {
        padding((20).px)
        marginTop((18).px)
    }
    style(c(LabClass.JONSSON_MODEL_RESULTS)) {
        display(DisplayStyle.Flex)
        gap((28).px)
        justifyContent(JustifyContent.SpaceBetween)
        alignItems(AlignItems.Center)
        flexWrap(FlexWrap.Wrap)
    }
    style(child(c(LabClass.JONSSON_MODEL_RESULTS), t(SelectorTag.DIV))) { minWidth((160).px) }
    style(desc(c(LabClass.JONSSON_MODEL_RESULTS), t(SelectorTag.P))) { margin((8).px, 0.px) }
    style(
        anyOf(
            desc(c(LabClass.JONSSON_MODEL), c(LabClass.PARAMETER_VALUE)),
            desc(c(LabClass.JONSSON_CONTROLS), c(LabClass.PARAMETER_VALUE)),
        )
    ) {
        font(FontSpec((23).px, FontStack.MONO, weight = 500))
        setFontVariantNumeric(FontVariantNumericValue.TABULAR_NUMS)
        color(Tokens.ACCENT.value())
        margin((8).px, 0.px, (14).px)
    }
    style(desc(c(LabClass.JONSSON_CONTROLS), c(LabClass.JONSSON_MODEL_RESULTS))) {
        display(DisplayStyle.Block)
    }
    style(desc(c(LabClass.JONSSON_CONTROLS), c(LabClass.LEARNING_QUESTION))) {
        font(FontSpec((16).px, FontStack.TEXT, weight = 500, lineHeight = 1.55))
        edge(CssProperty.BORDER_TOP, (1).px, LineStyle.Solid, Tokens.BORDER.value())
        paddingTop((16).px)
        marginTop((20).px)
    }
    style(desc(c(LabClass.JONSSON_CONTROLS), c(LabClass.CALIBRATION_NOTE))) {
        padding((12).px, 0.px)
    }
    style(child(c(LabClass.JONSSON_CONTROLS), t(SelectorTag.P))) {
        fontSize((14).px)
        set(CssProperty.LINE_HEIGHT, 1.65)
    }
    style(desc(c(LabClass.JONSSON_CONTROLS), c(LabClass.STEP_CONTROLS))) { marginTop((22).px) }
    style(c(MeasurementClass.SELECTION_EDITOR)) {
        marginTop(18.px)
        columns(GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1.5)), GridTrack.MinMax(GridTrack.Fixed(350.px), GridTrack.Fraction(1)))
    }
    style(desc(c(MeasurementClass.WORKSPACE_PROFILE), c(LabClass.PROFILE_LABEL))) {
        // The numeric glyph's ink is about 64% of its em; tablet viewBox scaling reduces it again.
        // 32 SVG units keep the actual glyph bounds above 14 CSS px at the 1024px landscape layout.
        fontSize(32.px)
    }
    style(c(MeasurementClass.CHECKS)) {
        marginTop(32.px)
        paddingTop(24.px)
        edge(CssProperty.BORDER_TOP, 1.px, LineStyle.Solid, Tokens.BORDER.value())
    }
    style(desc(c(MeasurementClass.FIRST_WORKSPACE), c(LabClass.MEASUREMENT_SUMMARY))) {
        gap(14.px)
    }
    style(child(c(MeasurementClass.CHECKS), t(SelectorTag.H2))) { fontSize(23.px) }
    style(desc(c(MeasurementClass.CHECKS), c(LabClass.MARKER_ACTIONS))) {
        display(DisplayStyle.Flex)
        flexWrap(FlexWrap.Wrap)
        gap(10.px)
        margin(14.px, 0.px)
    }
    style(desc(c(MeasurementClass.CHECKS), c(LabClass.MEASUREMENT_SUMMARY))) {
        justifyContent(JustifyContent.FlexStart)
        margin(18.px, 0.px)
    }
    style(desc(c(MeasurementClass.SELECTION_EDITOR), c(LabClass.MARKER_WORKSHOP))) {
        marginTop(0.px)
        paddingTop(0.px)
        border(0.px, LineStyle.None)
    }
    // Put the usable image near the chapter entry; source and band details stay below it.
    style(desc(c(LabClass.MEASUREMENT_LAYOUT), c(LabClass.JONSSON_IMAGING))) { padding(14.px) }
    style(desc(c(LabClass.MEASUREMENT_LAYOUT), c(LabClass.JONSSON_CONTROLS))) { padding(14.px) }
    style(desc(c(LabClass.MEASUREMENT_LAYOUT), c(LabClass.MEASUREMENT_WINDOW))) { marginTop(8.px) }
    style(desc(c(MeasurementClass.WORKSPACE_PROFILE), c(LabClass.JONSSON_PROFILE_HEADING))) {
        marginTop(10.px); paddingTop(10.px)
    }
    style(desc(c(MeasurementClass.FIRST_WORKSPACE), t(SelectorTag.P))) { margin(8.px, 0.px); set(CssProperty.LINE_HEIGHT, 1.45) }
    style(c(MeasurementClass.WORKSPACE_REASON)) {
        edge(CssProperty.BORDER_TOP, 1.px, LineStyle.Solid, Tokens.BORDER.value())
        marginTop(12.px); paddingTop(12.px)
    }
    style(desc(c(MeasurementClass.WORKSPACE_REASON), t(SelectorTag.LABEL))) {
        display(DisplayStyle.Block); fontSize(16.px)
    }
    style(desc(c(MeasurementClass.WORKSPACE_REASON), i(Id.JONSSON_JUSTIFICATION))) {
        minHeight(100.px); margin(6.px, 0.px); fontSize(16.px)
    }
    style(desc(c(MeasurementClass.WORKSPACE_REASON), i(Id.JONSSON_SEAL_SELECTION))) { margin(6.px, 0.px) }
    // The picture, band and coordinate editor form one working surface on landscape tablets.
    style(c(MeasurementClass.IMAGE_CONTROLS)) {
        display(DisplayStyle.Grid)
        columns(GridTrack.Fraction(1), GridTrack.Fraction(1))
        gap(16.px)
        alignItems(AlignItems.Start)
    }
    style(desc(c(MeasurementClass.IMAGE_CONTROLS), c(LabClass.PROFILE_PICKER))) { margin(4.px, 0.px) }
    style(desc(c(MeasurementClass.IMAGE_CONTROLS), c(LabClass.MEASUREMENT_TOOLS))) {
        display(DisplayStyle.Grid)
        columns(GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1)), GridTrack.Auto)
        gap(4.px, 8.px)
        margin(4.px, 0.px)
    }
    style(desc(c(MeasurementClass.IMAGE_CONTROLS), t(SelectorTag.LABEL))) { fontSize(16.px) }
    style(desc(desc(c(MeasurementClass.IMAGE_CONTROLS), c(LabClass.MEASUREMENT_TOOLS)), t(SelectorTag.LABEL))) {
        composite(CssProperty.GRID_COLUMN, "1 / -1")
    }
    style(desc(c(MeasurementClass.FIRST_WORKSPACE), c(LabClass.MARKER_WORKSHOP))) {
        marginTop(12.px); paddingTop(8.px)
    }
    style(c(MeasurementClass.COORDINATE_ROW)) {
        display(DisplayStyle.Grid)
        columns(GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1)), GridTrack.Auto)
        alignItems(AlignItems.Center); gap(8.px)
    }
    style(desc(c(MeasurementClass.COORDINATE_ROW), t(SelectorTag.INPUT))) { margin(0.px); minWidth(0.px) }
    style(desc(c(MeasurementClass.COORDINATE_ROW), t(SelectorTag.BUTTON))) { minHeight(44.px); padding(8.px, 10.px); fontSize(16.px) }
    style(c(MeasurementClass.MARKER_RESET_ROW)) { display(DisplayStyle.Flex); flexWrap(FlexWrap.Wrap); alignItems(AlignItems.Start); gap(8.px, 18.px); marginTop(6.px) }
    style(desc(c(MeasurementClass.MARKER_RESET_ROW), t(SelectorTag.BUTTON))) { minHeight(44.px); fontSize(16.px); padding(8.px, 10.px) }
    style(desc(c(MeasurementClass.FIRST_WORKSPACE), t(SelectorTag.SUMMARY))) { minHeight(44.px); padding(10.px, 0.px); fontSize(15.px) }
    style(desc(c(MeasurementClass.FIRST_WORKSPACE), c(LabClass.MARKER_CHOICES))) {
        display(DisplayStyle.Flex); flexWrap(FlexWrap.Wrap); gap(6.px)
    }
    style(desc(desc(c(MeasurementClass.FIRST_WORKSPACE), c(LabClass.MARKER_CHOICES)), t(SelectorTag.BUTTON))) { minWidth(44.px); padding(8.px) }
    style(desc(desc(c(MeasurementClass.FIRST_WORKSPACE), c(LabClass.MARKER_WORKSHOP)), t(SelectorTag.LABEL))) { fontSize(16.px); margin(10.px, 0.px, 6.px) }
    style(desc(desc(c(MeasurementClass.FIRST_WORKSPACE), c(LabClass.MARKER_WORKSHOP)), t(SelectorTag.P))) { fontSize(16.px); margin(8.px, 0.px) }
    style(desc(desc(c(MeasurementClass.FIRST_WORKSPACE), c(LabClass.MARKER_ACTIONS)), t(SelectorTag.BUTTON))) {
        fontSize(16.px); minHeight(44.px); minWidth(44.px)
        // Keep each label's intrinsic width; wrap the row before compressing a touch target.
        flexGrow(1); flexShrink(0); flexBasis("auto")
        setWhiteSpace(WhiteSpaceValue.NORMAL)
    }
    style(desc(c(MeasurementClass.FIRST_WORKSPACE), c(LabClass.MEASUREMENT_SUMMARY))) { flexWrap(FlexWrap.Nowrap); gap(12.px) }
    style(desc(desc(c(MeasurementClass.FIRST_WORKSPACE), c(LabClass.MEASUREMENT_SUMMARY)), t(SelectorTag.H2))) { fontSize(16.px) }
    style(desc(desc(c(MeasurementClass.FIRST_WORKSPACE), c(LabClass.MEASUREMENT_DISPLAY)), t(SelectorTag.STRONG))) { fontSize(26.px) }
    style(desc(c(MeasurementClass.FIRST_WORKSPACE), c(LabClass.JONSSON_PANEL_HEADING))) { alignItems(AlignItems.Start); flexDirection(FlexDirection.Column); gap(2.px) }
    style(desc(c(MeasurementClass.FIRST_WORKSPACE), t(SelectorTag.H3))) { margin(0.px, 0.px, 8.px); fontSize(18.px) }
    style(desc(c(MeasurementClass.SELECTION_EDITOR), c(LabClass.JONSSON_PROFILE_HEADING))) { margin(0.px, 0.px, 12.px) }
    style(desc(c(MeasurementClass.SELECTION_EDITOR), i(Id.JONSSON_JUSTIFICATION))) { minHeight(150.px) }
    media(CSSMediaQuery.MediaFeature("max-width", (980).px)) {
        style(c(LabClass.JONSSON_LAB)) {
            columns(
                GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1.5)),
                GridTrack.MinMax(GridTrack.Fixed((270).px), GridTrack.Fraction(1)),
            )
            gap((14).px)
        }
        style(c(LabClass.JONSSON_IMAGING)) { padding((14).px) }
        style(c(LabClass.JONSSON_CONTROLS)) { padding((16).px) }
        style(c(LabClass.PROFILE_SAMPLE_ROW)) { gap((7).px) }
        style(c(LabClass.DATA_KIND)) { fontSize((10).px) }
        style(c(LabClass.JONSSON_MODEL_RESULTS)) { gap((18).px) }
    }
    media(CSSMediaQuery.MediaFeature("max-width", (760).px)) {
        style(c(LabClass.JONSSON_LAB)) { columns(GridTrack.Fraction(1)) }
        style(desc(c(LabClass.JONSSON_LAB), c(LabClass.MEASUREMENT_WINDOW))) { maxHeight((340).px) }
        style(desc(c(LabClass.JONSSON_LAB), c(LabClass.MARKER_CHOICES))) {
            columns(
                GridTrack.Repeat(4, GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1)))
            )
        }
        style(c(LabClass.MEASUREMENT_SUMMARY)) {
            alignItems(AlignItems.FlexStart)
            gap((20).px)
        }
        style(
            anyOf(
                desc(c(LabClass.MEASUREMENT_DISPLAY), t(SelectorTag.STRONG)),
                desc(c(LabClass.MEASUREMENT_RESULT), i(Id.JONSSON_SPACING)),
            )
        ) {
            fontSize((27).px)
        }
        style(c(LabClass.PROFILE_LABEL)) { fontSize((26).px) }
        style(c(LabClass.NATIVE_MARKER_LABEL)) { fontSize((8).px) }
        style(anyOf(c(LabClass.JONSSON_PANEL_HEADING), c(LabClass.JONSSON_PROFILE_HEADING))) {
            gap((8).px)
        }
        style(c(LabClass.IMAGE_STATUS)) { fontSize((10).px) }
        style(c(LabClass.PROFILE_AXIS_LEGEND)) { fontSize((10).px) }
        style(anyOf(c(LabClass.MEASUREMENT_RESULT), c(LabClass.JONSSON_MODEL))) { padding((16).px) }
        style(desc(c(LabClass.JONSSON_CONTROLS), c(LabClass.JONSSON_MODEL_RESULTS))) {
            display(DisplayStyle.Flex)
        }
        style(
            child(
                desc(c(LabClass.JONSSON_CONTROLS), c(LabClass.JONSSON_MODEL_RESULTS)),
                t(SelectorTag.DIV),
            )
        ) {
            minWidth(0.px)
        }
        style(c(LabClass.JONSSON_MODEL_RESULTS)) { gap((12).px) }
        style(desc(c(LabClass.JONSSON_MODEL_RESULTS), c(LabClass.FORMULA))) { width((100).percent) }
    }
    media(DesignMedia.PREFERS_REDUCED_MOTION_REDUCE.query) {
        style(
            anyOf(
                desc(c(LabClass.JONSSON_LAB), universal),
                desc(c(LabClass.MEASUREMENT_RESULT), universal),
                desc(c(LabClass.JONSSON_MODEL), universal),
            )
        ) {
            important { setScrollBehavior(ScrollBehaviorValue.AUTO) }
            important { setTransition(TransitionValue.NONE) }
            important { setAnimation(AnimationValue.NONE) }
        }
    }
    style(i(Id.MARKER_NEW_X)) {
        width((100).percent)
        setBoxSizing(BoxSizingValue.BORDER_BOX)
        background(Tokens.BG.value())
        color(Tokens.TEXT.value())
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius((5).px)
        font(FontSpec((20).px, FontStack.MONO))
        setFontVariantNumeric(FontVariantNumericValue.TABULAR_NUMS)
        padding((10).px, (12).px)
        minHeight((46).px)
        margin(0.px, 0.px, (10).px)
    }
    style(i(Id.JONSSON_JUSTIFICATION)) {
        width((100).percent)
        setBoxSizing(BoxSizingValue.BORDER_BOX)
        minHeight((110).px)
        setResize(ResizeValue.VERTICAL)
        background(Tokens.BG.value())
        color(Tokens.TEXT.value())
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius((5).px)
        font(FontSpec((14).px, FontStack.TEXT, lineHeight = 1.6))
        padding((10).px, (12).px)
        margin((10).px, 0.px)
    }
    style(anyOf(i(Id.JONSSON_SEAL_SELECTION), i(Id.JONSSON_SHOW_REFERENCE))) {
        width((100).percent)
        setWhiteSpace(WhiteSpaceValue.NORMAL)
        margin((10).px, 0.px)
        minHeight((44).px)
    }
    style(child(c(LabClass.JONSSON_CONTROLS), t(SelectorTag.LABEL))) {
        display(DisplayStyle.Block)
        font(FontSpec((13).px, FontStack.TEXT, weight = 600))
        marginTop((20).px)
    }
    style(desc(c(LabClass.JONSSON_MODEL), c(LabClass.LEARNING_CARDS))) {
        gap((14).px)
        margin((18).px, 0.px)
    }
    style(desc(desc(c(LabClass.JONSSON_MODEL), c(LabClass.LEARNING_CARDS)), t(SelectorTag.LABEL))) {
        font(FontSpec((13).px, FontStack.TEXT))
    }
    style(
        desc(
            desc(c(LabClass.JONSSON_MODEL), c(LabClass.LEARNING_CARDS)),
            allOf(t(SelectorTag.INPUT), attribute(SelectorAttribute.TYPE, AttributeValue.RANGE)),
        )
    ) {
        display(DisplayStyle.Block)
        width((100).percent)
        minHeight((44).px)
        set(CssProperty.ACCENT_COLOR, Tokens.ACCENT.value())
    }
    style(desc(c(LabClass.MEASUREMENT_RESULT), c(LabClass.MARKER_ACTIONS))) {
        display(DisplayStyle.Flex)
        gap((10).px)
        flexWrap(FlexWrap.Wrap)
    }
    style(
        desc(desc(c(LabClass.MEASUREMENT_RESULT), c(LabClass.MARKER_ACTIONS)), t(SelectorTag.A))
    ) {
        display(DisplayStyle.LegacyInlineFlex)
        alignItems(AlignItems.Center)
        justifyContent(JustifyContent.Center)
        minHeight((44).px)
        setWhiteSpace(WhiteSpaceValue.NORMAL)
        setTextDecoration(TextDecorationValue.NONE)
        padding((10).px, (14).px)
    }
    style(c(MeasurementClass.CROSSHAIR)) {
        set(CssProperty.STROKE, Tokens.TEXT.value())
        set(CssProperty.STROKE_WIDTH, 0.4)
        composite(CssProperty.STROKE_DASHARRAY, "2 2")
        opacity(0.8)
        setPointerEvents(PointerEventsValue.NONE)
        setVectorEffect(VectorEffectValue.NON_SCALING_STROKE)
        set(CssProperty.STROKE_WIDTH, 1)
    }
    style(c(MeasurementClass.DETAIL)) {
        margin((12).px, 0.px)
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        background(Tokens.BG.value())
        borderRadius((6).px)
        padding((10).px)
        minWidth(0.px)
    }
    style(child(c(MeasurementClass.DETAIL), t(SelectorTag.P))) {
        margin(0.px, 0.px, (8).px)
        font(FontSpec((11).px, FontStack.MONO, lineHeight = 1.5))
        setFontVariantNumeric(FontVariantNumericValue.TABULAR_NUMS)
    }
    style(
        child(
            c(MeasurementClass.DETAIL),
            allOf(t(SelectorTag.P), pseudo(SelectorPseudo.LAST_CHILD)),
        )
    ) {
        margin((8).px, 0.px, 0.px)
    }
    style(c(MeasurementClass.DETAIL_VIEWS)) {
        display(DisplayStyle.Grid)
        columns(
            GridTrack.MinMax(GridTrack.Fixed((80).px), GridTrack.Fraction(0.65)),
            GridTrack.MinMax(GridTrack.Fixed((100).px), GridTrack.Fraction(1)),
        )
        alignItems(AlignItems.Stretch)
        gap((10).px)
        minWidth(0.px)
    }
    style(c(MeasurementClass.DETAIL_IMAGE)) {
        display(DisplayStyle.Block)
        width((100).percent)
        height((120).px)
        background(Tokens.SURFACE.value())
        borderRadius((3).px)
    }
    style(c(MeasurementClass.DETAIL_PROFILE)) {
        display(DisplayStyle.Block)
        width((100).percent)
        height((120).px)
        background(Tokens.SURFACE.value())
        borderRadius((3).px)
        alignSelf(AlignSelf.Center)
    }
    style(anyOf(c(MeasurementClass.BAND_WARNING), c(MeasurementClass.MATERIAL_WARNING))) {
        padding((11).px, (12).px)
        edge(CssProperty.BORDER_LEFT, (2).px, LineStyle.Solid, Tokens.REFERENCE.value())
        composite(CssProperty.BACKGROUND, "var(--reference-soft,var(--bg))")
        font(FontSpec((12).px, FontStack.TEXT, lineHeight = 1.6))
        setOverflowWrap(OverflowWrapValue.ANYWHERE)
    }
    style(i(MeasurementElement.CONFIRM_JUSTIFICATION)) {
        width((100).percent)
        setWhiteSpace(WhiteSpaceValue.NORMAL)
        minHeight((44).px)
        fontSize((12).px)
    }
    style(anyOf(c(MeasurementClass.PRECISION), c(MeasurementClass.MODEL_DETAILS))) {
        margin((16).px, 0.px)
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius((6).px)
        padding((12).px, (14).px)
        background(Tokens.SURFACE.value())
    }
    style(
        anyOf(
            desc(c(MeasurementClass.PRECISION), t(SelectorTag.SUMMARY)),
            child(c(MeasurementClass.MODEL_DETAILS), t(SelectorTag.SUMMARY)),
        )
    ) {
        setCursor(CursorValue.POINTER)
        minHeight((24).px)
        font(FontSpec((13).px, FontStack.TEXT, weight = 500, lineHeight = 1.6))
        color(Tokens.TEXT.value())
    }
    style(
        anyOf(
            desc(
                c(MeasurementClass.PRECISION),
                allOf(t(SelectorTag.SUMMARY), pseudo(SelectorPseudo.FOCUS_VISIBLE)),
            ),
            child(
                c(MeasurementClass.MODEL_DETAILS),
                allOf(t(SelectorTag.SUMMARY), pseudo(SelectorPseudo.FOCUS_VISIBLE)),
            ),
        )
    ) {
        edge(CssProperty.OUTLINE, (2).px, LineStyle.Solid, Tokens.ACCENT.value())
        outlineOffset((4).px)
        borderRadius((2).px)
    }
    style(desc(c(MeasurementClass.MODEL_DETAILS), c(LabClass.JONSSON_MODEL))) {
        marginTop((12).px)
        composite(CssProperty.BORDER, "0")
        padding((8).px, 0.px)
    }
    media(CSSMediaQuery.MediaFeature("max-width", (760).px)) {
        style(c(MeasurementClass.DETAIL_VIEWS)) {
            columns(
                GridTrack.MinMax(GridTrack.Fixed((90).px), GridTrack.Fraction(0.75)),
                GridTrack.MinMax(GridTrack.Fixed((110).px), GridTrack.Fraction(1)),
            )
        }
        style(anyOf(c(MeasurementClass.DETAIL_IMAGE), c(MeasurementClass.DETAIL_PROFILE))) {
            height((132).px)
        }
        style(desc(c(LabClass.JONSSON_LAB), c(LabClass.MEASUREMENT_WINDOW))) { maxHeight((300).px) }
        style(anyOf(c(MeasurementClass.PRECISION), c(MeasurementClass.MODEL_DETAILS))) {
            padding((12).px)
        }
        style(child(c(MeasurementClass.DETAIL), t(SelectorTag.P))) { fontSize((11).px) }
        style(desc(c(LabClass.GRAY_PROFILE), c(LabClass.PROFILE_LABEL))) { fontSize((18).px) }
    }
}
