@file:OptIn(org.jetbrains.compose.web.ExperimentalComposeWebApi::class)

package materiewelle.presentation.styles

import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.css.keywords.auto

/** Canonical Kotlin style definitions, using shared DOM identities and typed values. */
internal fun StyleRules.courseStyles() {
    style(c(LabClass.COURSE_SHELL)) {
        maxWidth((1320).px)
        composite(CssProperty.MARGIN, "0 auto")
    }
    style(c(LabClass.COURSE_HERO)) {
        padding((38).px, 0.px, (30).px)
        maxWidth((1040).px)
    }
    style(desc(c(LabClass.COURSE_HERO), t(SelectorTag.H1))) {
        composite(CssProperty.FONT_SIZE, "clamp(34px,4.8vw,62px)")
        set(CssProperty.LINE_HEIGHT, 1.05)
        maxWidth((850).px)
        letterSpacing((-0.045).em)
    }
    style(c(LabClass.COURSE_LEAD)) {
        composite(CssProperty.FONT_SIZE, "clamp(16px,1.5vw,20px)")
        set(CssProperty.LINE_HEIGHT, 1.65)
        maxWidth((1000).px)
        color(Tokens.MUTED.value())
        margin((18).px, 0.px, (28).px)
    }
    style(c(LabClass.COURSE_ACTIONS)) {
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        gap((10).px)
        flexWrap(FlexWrap.Wrap)
        margin((16).px, 0.px)
    }
    style(c(LabClass.COURSE_INDEX)) {
        display(DisplayStyle.Grid)
        columns(GridTrack.Repeat(2, GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1))))
        gap((12).px)
        margin((12).px, 0.px, (32).px)
    }
    style(c(LabClass.COURSE_CARD)) {
        display(DisplayStyle.Flex)
        alignItems(AlignItems.FlexStart)
        gap((18).px)
        padding((22).px)
        background(Tokens.SURFACE.value())
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius(Tokens.RADIUS.value())
        setTextDecoration(TextDecorationValue.NONE)
        color(Tokens.TEXT.value())
        minWidth(0.px)
    }
    style(allOf(c(LabClass.COURSE_CARD), pseudo(SelectorPseudo.HOVER))) {
        borderColor(Tokens.ACCENT.value())
        background(Tokens.SURFACE_RAISED.value())
    }
    style(desc(c(LabClass.COURSE_CARD), t(SelectorTag.H2))) {
        margin(0.px)
        fontSize((18).px)
    }
    style(desc(c(LabClass.COURSE_CARD), t(SelectorTag.P))) {
        fontSize((14).px)
        color(Tokens.MUTED.value())
        margin((8).px, 0.px, (12).px)
    }
    style(
        child(
            c(LabClass.COURSE_CARD),
            allOf(
                t(SelectorTag.SPAN),
                pseudo(SelectorPseudo.LAST_CHILD),
                negate(pseudo(SelectorPseudo.FIRST_CHILD)),
            ),
        )
    ) {
        font(FontSpec((11).px, FontStack.MONO))
        color(Tokens.ACCENT.value())
        setMarginLeft(MarginLeftValue.AUTO)
    }
    style(c(LabClass.COURSE_STATION_NUMBER)) {
        font(FontSpec((24).px, FontStack.MONO))
        color(Tokens.ACCENT.value())
        paddingTop((2).px)
        minWidth((36).px)
    }
    style(c(LabClass.COURSE_TOPLINE)) {
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        justifyContent(JustifyContent.SpaceBetween)
        gap((12).px)
        flexWrap(FlexWrap.Wrap)
        margin(0.px, 0.px, (18).px)
    }
    style(c(LabClass.COURSE_PHASES)) {
        display(DisplayStyle.Flex)
        gap((8).px)
        edge(CssProperty.BORDER_BOTTOM, (1).px, LineStyle.Solid, Tokens.BORDER.value())
        padding(0.px, 0.px, (12).px)
        font(FontSpec((12).px, FontStack.MONO))
        color(Tokens.MUTED.value())
    }
    style(child(c(LabClass.COURSE_PHASES), t(SelectorTag.SPAN))) {
        padding((8).px, (13).px)
        borderRadius((5).px)
    }
    style(child(c(LabClass.COURSE_PHASES), allOf(t(SelectorTag.SPAN), c(LabClass.ACTIVE)))) {
        color(Tokens.ACCENT.value())
        background(Tokens.ACCENT_SOFT.value())
        composite(CssProperty.BOX_SHADOW, "inset 0 -2px var(--accent)")
    }
    style(c(LabClass.COURSE_HISTORY)) {
        font(FontSpec((12).px, FontStack.MONO, lineHeight = 1.6))
        color(Tokens.MUTED.value())
        margin((14).px, 0.px, (24).px)
        maxWidth((1080).px)
    }
    style(c(LabClass.COURSE_LAYOUT)) {
        display(DisplayStyle.Grid)
        columns(
            GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(3)),
            GridTrack.MinMax(GridTrack.Fixed((240).px), GridTrack.Fraction(1)),
        )
        gap((20).px)
        margin((24).px, 0.px, (32).px)
        alignItems(AlignItems.Start)
    }
    style(c(LabClass.COURSE_VISUAL)) {
        background(Tokens.SURFACE.value())
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius(Tokens.RADIUS.value())
        padding((22).px)
        minWidth(0.px)
        setOverflow(OverflowValue.HIDDEN)
    }
    style(
        anyOf(
            desc(c(LabClass.COURSE_VISUAL), t(SelectorTag.SVG)),
            desc(c(LabClass.COURSE_VISUAL), t(SelectorTag.IMG)),
        )
    ) {
        display(DisplayStyle.Block)
        width((100).percent)
        height(auto)
        maxWidth((100).percent)
    }
    style(desc(c(LabClass.COURSE_VISUAL), c(LabClass.FORMULA))) {
        margin((14).px, 0.px)
        fontSize((21).px)
    }
    style(desc(c(LabClass.COURSE_VISUAL), c(LabClass.COURSE_RESULT))) {
        font(FontSpec((17).px, FontStack.MONO, lineHeight = 1.7))
        setFontVariantNumeric(FontVariantNumericValue.TABULAR_NUMS)
    }
    style(c(LabClass.COURSE_CONTEXT)) {
        padding((6).px, 0.px)
        minWidth(0.px)
    }
    style(child(c(LabClass.COURSE_CONTEXT), t(SelectorTag.DIV))) {
        edge(CssProperty.BORDER_BOTTOM, (1).px, LineStyle.Solid, Tokens.BORDER.value())
        padding((12).px, 0.px, (18).px)
    }
    style(
        child(
            c(LabClass.COURSE_CONTEXT),
            allOf(t(SelectorTag.DIV), pseudo(SelectorPseudo.FIRST_CHILD)),
        )
    ) {
        paddingTop(0.px)
    }
    style(desc(c(LabClass.COURSE_CONTEXT), t(SelectorTag.P))) { fontSize((14).px) }
    style(desc(c(LabClass.COURSE_CONTEXT), c(LabClass.PANEL_LABEL))) {
        font(FontSpec((11).px, FontStack.MONO))
        letterSpacing((0.08).em)
        color(Tokens.ACCENT.value())
        marginBottom((8).px)
    }
    style(desc(c(LabClass.COURSE_CONTEXT), t(SelectorTag.BUTTON))) {
        width((100).percent)
        marginTop((20).px)
    }
    style(c(LabClass.COURSE_EXPLANATION)) {
        maxWidth((1020).px)
        margin((30).px, 0.px)
    }
    style(c(LabClass.COURSE_STEP)) {
        display(DisplayStyle.Flex)
        alignItems(AlignItems.FlexStart)
        gap((20).px)
        padding((20).px, 0.px)
        edge(CssProperty.BORDER_BOTTOM, (1).px, LineStyle.Solid, Tokens.BORDER.value())
    }
    style(desc(c(LabClass.COURSE_STEP), t(SelectorTag.H3))) { marginTop(0.px) }
    style(desc(c(LabClass.COURSE_STEP), t(SelectorTag.P))) { maxWidth((850).px) }
    style(desc(c(LabClass.COURSE_STEP), c(LabClass.STEP_NUMBER))) {
        minWidth((34).px)
        fontSize((17).px)
        paddingTop((2).px)
    }
    style(c(LabClass.COURSE_TASK)) {
        background(Tokens.SURFACE.value())
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius(Tokens.RADIUS.value())
        padding((24).px)
        margin((24).px, 0.px)
    }
    style(child(c(LabClass.COURSE_TASK), t(SelectorTag.INPUT))) {
        composite(CssProperty.WIDTH, "min(100%,420px)")
        margin(0.px, (12).px, (12).px, 0.px)
    }
    style(c(LabClass.COURSE_NOTE)) {
        display(DisplayStyle.Block)
        width((100).percent)
        minHeight((110).px)
        font(FontSpec((15).px, FontStack.TEXT, lineHeight = 1.65))
        padding((12).px)
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius(Tokens.CONTROL_RADIUS.value())
        color(Tokens.TEXT.value())
        background(Tokens.BG.value())
        setResize(ResizeValue.VERTICAL)
    }
    style(allOf(c(LabClass.COURSE_NOTE), pseudo(SelectorPseudo.FOCUS_VISIBLE))) {
        edge(CssProperty.OUTLINE, (3).px, LineStyle.Solid, Tokens.FOCUS.value())
        outlineOffset((3).px)
    }
    style(c(LabClass.COURSE_OPTIONS)) {
        display(DisplayStyle.Grid)
        gap((10).px)
        margin((14).px, 0.px)
    }
    style(desc(c(LabClass.COURSE_OPTIONS), t(SelectorTag.BUTTON))) {
        setTextAlign(TextAlignValue.LEFT)
        set(CssProperty.LINE_HEIGHT, 1.5)
        padding((14).px, (16).px)
        setWhiteSpace(WhiteSpaceValue.NORMAL)
    }
    style(c(LabClass.COURSE_RESULT)) {
        padding((14).px, (18).px)
        edge(CssProperty.BORDER_LEFT, (3).px, LineStyle.Solid, Tokens.ACCENT.value())
        background(Tokens.ACCENT_SOFT.value())
        color(Tokens.TEXT.value())
        set(CssProperty.LINE_HEIGHT, 1.6)
    }
    style(c(LabClass.COURSE_SOURCE)) {
        edge(CssProperty.BORDER_LEFT, (2).px, LineStyle.Solid, Tokens.BORDER.value())
        paddingLeft((14).px)
        fontSize((14).px)
    }
    style(c(LabClass.COURSE_STORAGE)) {
        edge(CssProperty.BORDER_TOP, (1).px, LineStyle.Solid, Tokens.BORDER.value())
        paddingTop((12).px)
        marginTop((30).px)
        color(Tokens.MUTED.value())
    }
    style(desc(c(LabClass.COURSE_STORAGE), c(LabClass.SECONDARY))) { fontSize((12).px) }
    style(
        desc(
            c(LabClass.COURSE_VISUAL),
            allOf(t(SelectorTag.INPUT), attribute(SelectorAttribute.TYPE, AttributeValue.RANGE)),
        )
    ) {
        margin((4).px, 0.px, (14).px)
    }
    style(c(LabClass.COURSE_SVG)) { fontFamily(FontStack.TEXT) }
    style(c(LabClass.SITE_HEADER)) {
        flexWrap(FlexWrap.Wrap)
        gap((12).px, (22).px)
    }
    style(desc(c(LabClass.SITE_HEADER), t(SelectorTag.NAV))) {
        flexWrap(FlexWrap.Wrap)
        justifyContent(JustifyContent.Center)
        composite(CssProperty.FLEX, "1")
    }
    style(desc(c(LabClass.SITE_HEADER), c(LabClass.NAV_ITEM))) { padding((9).px, (10).px) }
    style(desc(c(LabClass.SITE_HEADER), c(LabClass.BRAND))) {
        fontSize((15).px)
        letterSpacing((0.10).em)
    }
    style(desc(c(LabClass.SITE_HEADER), c(LabClass.THEME_CHOOSER))) {
        setMarginLeft(MarginLeftValue.AUTO)
    }
    media(CSSMediaQuery.MediaFeature("min-width", (1200).px)) {
        style(c(LabClass.COURSE_CONTEXT)) {
            position(Position.Sticky)
            set(CssProperty.TOP, (20).px)
        }
    }
    media(CSSMediaQuery.MediaFeature("max-width", (900).px)) {
        style(c(LabClass.COURSE_LAYOUT)) {
            columns(GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1)))
        }
        style(c(LabClass.COURSE_CONTEXT)) {
            display(DisplayStyle.Grid)
            columns(
                GridTrack.Repeat(3, GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1)))
            )
            gap((16).px)
        }
        style(child(c(LabClass.COURSE_CONTEXT), t(SelectorTag.DIV))) {
            padding((12).px, 0.px)
            edge(CssProperty.BORDER_TOP, (1).px, LineStyle.Solid, Tokens.BORDER.value())
            composite(CssProperty.BORDER_BOTTOM, "0")
        }
        style(
            child(
                c(LabClass.COURSE_CONTEXT),
                allOf(t(SelectorTag.DIV), pseudo(SelectorPseudo.FIRST_CHILD)),
            )
        ) {
            paddingTop((12).px)
        }
        style(
            anyOf(
                desc(c(LabClass.COURSE_CONTEXT), t(SelectorTag.BUTTON)),
                child(c(LabClass.COURSE_CONTEXT), c(LabClass.SMALL)),
            )
        ) {
            composite(CssProperty.GRID_COLUMN, "1/-1")
            margin(0.px)
        }
        style(c(LabClass.COURSE_HERO)) { paddingTop((20).px) }
        style(c(LabClass.COURSE_INDEX)) { gap((10).px) }
        style(c(LabClass.COURSE_CARD)) {
            padding((16).px)
            gap((12).px)
        }
    }
    media(CSSMediaQuery.MediaFeature("max-width", (600).px)) {
        style(c(LabClass.COURSE_INDEX)) { columns(GridTrack.Fraction(1)) }
        style(desc(c(LabClass.COURSE_HERO), t(SelectorTag.H1))) { fontSize((38).px) }
        style(desc(c(LabClass.COURSE_TOPLINE), c(LabClass.SECONDARY))) { fontSize((12).px) }
        style(c(LabClass.COURSE_PHASES)) {
            gap((2).px)
            justifyContent(JustifyContent.SpaceBetween)
            fontSize((10).px)
        }
        style(child(c(LabClass.COURSE_PHASES), t(SelectorTag.SPAN))) { padding((8).px, (6).px) }
        style(c(LabClass.COURSE_HISTORY)) { fontSize((11).px) }
        style(c(LabClass.COURSE_VISUAL)) { padding((12).px) }
        style(desc(c(LabClass.COURSE_VISUAL), c(LabClass.FORMULA))) { fontSize((19).px) }
        style(c(LabClass.COURSE_TASK)) { padding((16).px) }
        style(c(LabClass.COURSE_CONTEXT)) {
            columns(GridTrack.Fraction(1))
            gap((2).px)
        }
        style(desc(c(LabClass.COURSE_CONTEXT), t(SelectorTag.P))) { fontSize((13).px) }
        style(c(LabClass.COURSE_LEAD)) {
            fontSize((16).px)
            marginBottom((20).px)
        }
        style(c(LabClass.COURSE_STEP)) { gap((12).px) }
        style(desc(c(LabClass.COURSE_STEP), c(LabClass.STEP_NUMBER))) { minWidth((24).px) }
        style(desc(c(LabClass.COURSE_ACTIONS), t(SelectorTag.BUTTON))) { maxWidth((100).percent) }
        style(desc(c(LabClass.SITE_HEADER), t(SelectorTag.NAV))) {
            columns(
                GridTrack.Repeat(3, GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1)))
            )
        }
        style(desc(c(LabClass.SITE_HEADER), c(LabClass.NAV_ITEM))) { fontSize((11).px) }
        style(desc(c(LabClass.COURSE_SVG), t(SelectorTag.TEXT))) { fontSize((14).px) }
        style(desc(c(LabClass.COURSE_TASK), c(LabClass.PRIMARY))) { marginBottom((10).px) }
    }
    media(DesignMedia.PREFERS_REDUCED_MOTION_NO_PREFERENCE.query) {
        style(c(LabClass.COURSE_CARD)) {
            composite(CssProperty.TRANSITION, "background-color .15s,border-color .15s")
        }
        style(c(LabClass.COURSE_VISUAL)) {
            animation(MotionKeyframe.PANEL_ENTER) {
                duration((0.18).s)
                timingFunction(AnimationTimingFunction.EaseOut)
            }
        }
    }
    style(desc(c(LabClass.DG_DATA), c(LabClass.DATA_POINTS))) { columns(GridTrack.Fraction(1)) }
    style(c(LabClass.COURSE_DIAGRAM)) {
        setOverflowX(OverflowXValue.AUTO)
        maxWidth((100).percent)
        borderRadius((4).px)
    }
    style(desc(c(LabClass.COURSE_DIAGRAM), t(SelectorTag.SVG))) { minWidth((680).px) }
    style(c(LabClass.COURSE_VISUAL)) { set(CssProperty.SCROLL_MARGIN_TOP, (70).px) }
    style(c(LabClass.COURSE_PHASES)) {
        position(Position.Sticky)
        set(CssProperty.TOP, 0.px)
        zIndex(3)
        background(Tokens.BG.value())
        paddingTop((8).px)
    }
    media(CSSMediaQuery.MediaFeature("max-width", (760).px)) {
        style(desc(c(LabClass.DG_WORKSPACE), c(LabClass.CHART))) {
            setOverflowX(OverflowXValue.AUTO)
            maxWidth((100).percent)
        }
        style(desc(desc(c(LabClass.DG_WORKSPACE), c(LabClass.CHART)), t(SelectorTag.SVG))) {
            minWidth((680).px)
        }
        style(
            desc(
                desc(
                    desc(
                        allOf(c(LabClass.DG_WORKSPACE), c(LabClass.LAB_WORKSPACE)),
                        c(LabClass.CHART),
                    ),
                    t(SelectorTag.SVG),
                ),
                t(SelectorTag.TEXT),
            )
        ) {
            fontSize((16).px)
        }
        style(t(SelectorTag.HTML)) { set(CssProperty.SCROLL_PADDING_TOP, (70).px) }
    }
    style(
        desc(allOf(c(LabClass.DG_WORKSPACE), c(LabClass.LAB_WORKSPACE)), c(LabClass.DIFFERENCE))
    ) {
        flexDirection(FlexDirection.Column)
        alignItems(AlignItems.FlexStart)
        gap((6).px)
    }
    style(
        desc(
            desc(
                allOf(c(LabClass.DG_WORKSPACE), c(LabClass.LAB_WORKSPACE)),
                c(LabClass.DIFFERENCE),
            ),
            t(SelectorTag.STRONG),
        )
    ) {
        fontSize((22).px)
        setWhiteSpace(WhiteSpaceValue.NOWRAP)
    }
    style(
        desc(
            desc(
                allOf(c(LabClass.DG_WORKSPACE), c(LabClass.LAB_WORKSPACE)),
                c(LabClass.DIFFERENCE),
            ),
            t(SelectorTag.P),
        )
    ) {
        fontSize((12).px)
        set(CssProperty.LINE_HEIGHT, 1.6)
        margin((2).px, 0.px)
    }
    style(
        adjacent(
            desc(
                allOf(c(LabClass.DG_WORKSPACE), c(LabClass.LAB_WORKSPACE)),
                c(LabClass.DIFFERENCE),
            ),
            c(LabClass.SMALL),
        )
    ) {
        fontSize((12).px)
        set(CssProperty.LINE_HEIGHT, 1.6)
    }
    style(
        child(
            desc(c(LabClass.DG_WORKSPACE), c(LabClass.CONTROL_BODY)),
            allOf(
                t(SelectorTag.INPUT),
                negate(attribute(SelectorAttribute.TYPE, AttributeValue.RANGE)),
            ),
        )
    ) {
        display(DisplayStyle.Block)
        width((100).percent)
        marginBottom((16).px)
    }
}
