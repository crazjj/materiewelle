@file:OptIn(org.jetbrains.compose.web.ExperimentalComposeWebApi::class)

package materiewelle.presentation.styles

import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.css.keywords.auto

/** Canonical Kotlin style definitions, using shared DOM identities and typed values. */
internal fun StyleRules.motionDesignStyles() {
    style(pseudo(SelectorPseudo.ROOT)) {
        setToken(Tokens.MOTION_EASE, AnimationTimingFunction.cubicBezier(0.22, 0.8, 0.22, 1.0))
        setToken(Tokens.MOTION_FAST, (180).ms)
        setToken(Tokens.MOTION_ARRIVE, (480).ms)
    }
    style(c(MotionClass.TOOLS)) {
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        gap((8).px)
        setMarginLeft(MarginLeftValue.AUTO)
        flexShrink(0)
    }
    style(c(MotionClass.TOGGLE)) {
        display(DisplayStyle.LegacyInlineFlex)
        alignItems(AlignItems.Center)
        justifyContent(JustifyContent.Center)
        gap((8).px)
        minHeight((44).px)
        padding((8).px, (11).px)
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius((8).px)
        background(Tokens.SURFACE.value())
        font(FontSpec((12).px, FontStack.TEXT, weight = 500))
        setWhiteSpace(WhiteSpaceValue.NOWRAP)
    }
    style(allOf(c(MotionClass.TOGGLE), pseudo(SelectorPseudo.HOVER))) {
        background(Tokens.ACCENT_SOFT.value())
        borderColor(Tokens.ACCENT.value())
    }
    style(c(MotionClass.ICON)) {
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        justifyContent(JustifyContent.Center)
        gap((3).px)
        width((18).px)
        height((20).px)
        color(Tokens.ACCENT.value())
    }
    style(child(c(MotionClass.ICON), t(SelectorTag.SPAN))) {
        width((2).px)
        height((12).px)
        background(Color.currentColor)
        borderRadius((3).px)
        setTransformOrigin(TransformOriginValue.CENTER)
    }
    style(child(c(MotionClass.ICON), allOf(t(SelectorTag.SPAN), nthChild("2")))) { height((18).px) }
    style(child(c(MotionClass.ICON), allOf(t(SelectorTag.SPAN), nthChild("3")))) { height((8).px) }
    style(
        child(
            desc(
                allOf(
                    t(SelectorTag.HTML),
                    negate(attribute(SelectorAttribute.DATA_MOTION, AttributeValue.RUNNING)),
                ),
                c(MotionClass.ICON),
            ),
            t(SelectorTag.SPAN),
        )
    ) {
        height((12).px)
    }
    style(c(MotionClass.HERO_LAYOUT)) {
        display(DisplayStyle.Grid)
        columns(
            GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1.35)),
            GridTrack.MinMax(GridTrack.Fixed((300).px), GridTrack.Fraction(1)),
        )
        gap((40).px)
        alignItems(AlignItems.Center)
        margin((10).px, 0.px, (36).px)
    }
    style(desc(c(MotionClass.HERO_LAYOUT), c(LabClass.COURSE_HERO))) {
        padding((24).px, 0.px)
        minWidth(0.px)
    }
    style(desc(desc(c(MotionClass.HERO_LAYOUT), c(LabClass.COURSE_HERO)), t(SelectorTag.H1))) {
        composite(CssProperty.FONT_SIZE, "clamp(36px,4.4vw,60px)")
    }
    style(desc(c(MotionClass.HERO_LAYOUT), c(LabClass.COURSE_LEAD))) {
        fontSize((18).px)
        marginBottom((20).px)
    }
    style(c(MotionClass.HERO)) {
        position(Position.Relative)
        minWidth(0.px)
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius((14).px)
        padding((20).px, (22).px, (16).px)
        composite(
            CssProperty.BACKGROUND,
            "radial-gradient(ellipse at 65% 30%,var(--accent-soft),transparent 75%),var(--surface)",
        )
        setOverflow(OverflowValue.HIDDEN)
        setIsolation(IsolationValue.ISOLATE)
    }
    style(c(MotionClass.HEAD)) {
        display(DisplayStyle.Flex)
        justifyContent(JustifyContent.SpaceBetween)
        gap((16).px)
        alignItems(AlignItems.Center)
        font(FontSpec((10).px, FontStack.MONO, lineHeight = 1.5))
        letterSpacing((0.06).em)
        color(Tokens.MUTED.value())
    }
    style(c(MotionClass.STATUS)) {
        display(DisplayStyle.LegacyInlineFlex)
        alignItems(AlignItems.Center)
        gap((7).px)
        setWhiteSpace(WhiteSpaceValue.NOWRAP)
    }
    style(c(MotionClass.DOT)) {
        display(DisplayStyle.InlineBlock)
        width((5).px)
        height((5).px)
        borderRadius((50).percent)
        background(Tokens.ACCENT.value())
        flexShrink(0)
    }
    style(c(MotionClass.FIGURE)) {
        position(Position.Relative)
        margin((18).px, (-22).px, (12).px)
        setOverflow(OverflowValue.HIDDEN)
        minHeight((120).px)
        display(DisplayStyle.Grid)
        alignItems(AlignItems.Center)
    }
    style(
        anyOf(
            desc(c(MotionClass.FIGURE), t(SelectorTag.SVG)),
            desc(c(MotionClass.WAVE_STRIP), t(SelectorTag.SVG)),
        )
    ) {
        width((100).percent)
        height(auto)
        setOverflow(OverflowValue.HIDDEN)
        display(DisplayStyle.Block)
    }
    style(c(MotionClass.GRID)) {
        set(CssProperty.STROKE, Tokens.BORDER.value())
        set(CssProperty.STROKE_WIDTH, 0.65)
        opacity(0.6)
        setFill(FillValue.NONE)
    }
    style(
        desc(c(MotionClass.GRID), allOf(t(SelectorTag.PATH), pseudo(SelectorPseudo.FIRST_CHILD)))
    ) {
        composite(CssProperty.STROKE_DASHARRAY, "3 6")
    }
    style(c(MotionClass.WAVE_TRACE)) {
        set(CssProperty.STROKE, Tokens.ACCENT.value())
        set(CssProperty.STROKE_WIDTH, 2.5)
        setFill(FillValue.NONE)
        setStrokeLinejoin(StrokeLinejoinValue.ROUND)
    }
    style(c(MotionClass.WAVE_GHOST)) {
        set(CssProperty.STROKE, Tokens.ACCENT.value())
        set(CssProperty.STROKE_WIDTH, 12)
        opacity(0.075)
        setFill(FillValue.NONE)
    }
    style(c(MotionClass.SPARK)) {
        position(Position.Absolute)
        set(CssProperty.TOP, 0.px)
        set(CssProperty.BOTTOM, 0.px)
        set(CssProperty.LEFT, (-16).percent)
        width((12).percent)
        composite(
            CssProperty.BACKGROUND,
            "linear-gradient(90deg,transparent,color-mix(in srgb,var(--accent) 10%,transparent),transparent)",
        )
        transform { skewX((-15).deg) }
        setPointerEvents(PointerEventsValue.NONE)
    }
    style(c(MotionClass.MASCOT_ROW)) {
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        gap((14).px)
        edge(CssProperty.BORDER_TOP, (1).px, LineStyle.Solid, Tokens.BORDER.value())
        paddingTop((16).px)
        minHeight((83).px)
    }
    style(child(c(MotionClass.MASCOT_ROW), t(SelectorTag.DIV))) { minWidth(0.px) }
    style(desc(c(MotionClass.MASCOT_ROW), t(SelectorTag.P))) { margin(0.px) }
    style(c(MotionClass.JOKE)) {
        font(FontSpec((13).px, FontStack.TEXT, weight = 500, lineHeight = 1.5))
        minHeight((2.9).em)
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
    }
    style(desc(c(MotionClass.MASCOT_ROW), c(LabClass.SMALL))) {
        fontSize((10).px)
        set(CssProperty.LINE_HEIGHT, 1.6)
    }
    style(c(MotionClass.MASCOT)) {
        position(Position.Relative)
        display(DisplayStyle.Grid)
        setPlaceItems(PlaceItemsValue.CENTER)
        flexShrink(0)
        width((48).px)
        height((48).px)
        minHeight((44).px)
        borderRadius((50).percent)
        border((1).px, LineStyle.Solid, Tokens.ACCENT.value())
        background(Tokens.ACCENT_SOFT.value())
        color(Tokens.ACCENT.value())
        composite(CssProperty.BOX_SHADOW, "inset -5px -5px 12px #0001")
        setCursor(CursorValue.POINTER)
    }
    style(allOf(c(MotionClass.MASCOT), pseudo(SelectorPseudo.BEFORE))) {
        composite(CssProperty.CONTENT, "\"\"")
        position(Position.Absolute)
        set(CssProperty.INSET, (-6).px)
        border((1).px, LineStyle.Dashed, Tokens.BORDER.value())
        borderRadius((50).percent)
        setPointerEvents(PointerEventsValue.NONE)
    }
    style(c(MotionClass.EYES)) {
        display(DisplayStyle.Block)
        position(Position.Relative)
        width((18).px)
        height((6).px)
    }
    style(
        anyOf(
            allOf(c(MotionClass.EYES), pseudo(SelectorPseudo.BEFORE)),
            allOf(c(MotionClass.EYES), pseudo(SelectorPseudo.AFTER)),
        )
    ) {
        composite(CssProperty.CONTENT, "\"\"")
        position(Position.Absolute)
        set(CssProperty.TOP, 0.px)
        width((4).px)
        height((6).px)
        borderRadius((3).px)
        background(Color.currentColor)
    }
    style(allOf(c(MotionClass.EYES), pseudo(SelectorPseudo.BEFORE))) {
        set(CssProperty.LEFT, (2).px)
    }
    style(allOf(c(MotionClass.EYES), pseudo(SelectorPseudo.AFTER))) {
        set(CssProperty.RIGHT, (2).px)
    }
    style(allOf(c(MotionClass.MASCOT), pseudo(SelectorPseudo.HOVER))) {
        background(Tokens.ACCENT.value())
        color(Tokens.ACCENT_INK.value())
    }
    style(child(c(MotionClass.HERO), allOf(t(SelectorTag.P), pseudo(SelectorPseudo.LAST_CHILD)))) {
        fontSize((10).px)
        margin((12).px, 0.px, 0.px)
    }
    style(c(MotionClass.WAVE_STRIP)) {
        edge(CssProperty.BORDER_TOP, (1).px, LineStyle.Solid, Tokens.BORDER.value())
        marginTop((18).px)
        paddingTop((14).px)
        setOverflow(OverflowValue.HIDDEN)
    }
    style(c(MotionClass.WAVE_META)) {
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        justifyContent(JustifyContent.SpaceBetween)
        gap((10).px)
        font(FontSpec((10).px, FontStack.MONO, lineHeight = 1.7))
        letterSpacing((0.025).em)
        color(Tokens.MUTED.value())
        flexWrap(FlexWrap.Wrap)
    }
    style(
        child(
            c(MotionClass.WAVE_META),
            allOf(t(SelectorTag.SPAN), pseudo(SelectorPseudo.LAST_CHILD)),
        )
    ) {
        color(Tokens.ACCENT.value())
    }
    style(desc(c(MotionClass.WAVE_STRIP), c(LabClass.SMALL))) {
        fontSize((11).px)
        margin(0.px)
        maxWidth((85).ch)
    }
    style(desc(c(MotionClass.WAVE_STRIP), c(LabClass.CHART))) { margin((4).px, 0.px, (2).px) }
    style(c(MotionClass.SIGNAL)) {
        display(DisplayStyle.Flex)
        gap((24).px)
        alignItems(AlignItems.Center)
        justifyContent(JustifyContent.SpaceBetween)
        margin(0.px, 0.px, (26).px)
        padding((12).px, 0.px, (14).px)
        edge(CssProperty.BORDER_BOTTOM, (1).px, LineStyle.Solid, Tokens.BORDER.value())
    }
    style(c(MotionClass.SIGNAL_STAGES)) {
        position(Position.Relative)
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        justifyContent(JustifyContent.SpaceBetween)
        gap((24).px)
        minWidth(0.px)
        composite(CssProperty.WIDTH, "min(100%,680px)")
    }
    style(allOf(c(MotionClass.SIGNAL_STAGES), pseudo(SelectorPseudo.BEFORE))) {
        composite(CssProperty.CONTENT, "\"\"")
        position(Position.Absolute)
        set(CssProperty.LEFT, 0.px)
        set(CssProperty.RIGHT, 0.px)
        set(CssProperty.TOP, (50).percent)
        edge(CssProperty.BORDER_TOP, (1).px, LineStyle.Dashed, Tokens.BORDER.value())
        zIndex(0)
    }
    style(c(MotionClass.SIGNAL_NODE)) {
        position(Position.Relative)
        zIndex(1)
        display(DisplayStyle.LegacyInlineFlex)
        alignItems(AlignItems.Center)
        gap((8).px)
        padding((5).px, (8).px)
        background(Tokens.BG.value())
        border((1).px, LineStyle.Solid, Color.transparent)
        borderRadius((5).px)
        font(FontSpec((11).px, FontStack.MONO, lineHeight = 1.5))
        setWhiteSpace(WhiteSpaceValue.NOWRAP)
        color(Tokens.MUTED.value())
    }
    style(desc(c(MotionClass.SIGNAL_NODE), c(MotionClass.DOT))) {
        background(Tokens.MUTED.value())
        opacity(0.6)
    }
    style(child(c(MotionClass.SIGNAL), c(LabClass.SMALL))) {
        font(FontSpec((10).px, FontStack.MONO, lineHeight = 1.5))
        flexShrink(0)
    }
    style(c(MotionClass.READING_UPDATE)) {
        display(DisplayStyle.LegacyInlineFlex)
        alignItems(AlignItems.Baseline)
        maxWidth((100).percent)
    }
    style(desc(c(LabClass.COURSE_INDEX), c(LabClass.COURSE_CARD))) {
        setAnimation(AnimationValue.NONE)
    }
    media(DesignMedia.PREFERS_REDUCED_MOTION_NO_PREFERENCE.query) {
        style(anyOf(c(MotionClass.WAVE_TRACE), c(MotionClass.WAVE_GHOST))) {
            animation(MotionKeyframe.LAB_WAVE_TRAVEL) {
                duration((4.8).s)
                timingFunction(AnimationTimingFunction.Linear)
                iterationCount(null)
            }
        }
        style(
            anyOf(
                desc(c(MotionClass.HERO), c(MotionClass.WAVE_TRACE)),
                desc(c(MotionClass.HERO), c(MotionClass.WAVE_GHOST)),
            )
        ) {
            important { set(CssProperty.ANIMATION_DURATION, (7).s) }
        }
        style(c(MotionClass.SPARK)) {
            animation(MotionKeyframe.LAB_SIGNAL_SCAN) {
                duration((11).s)
                timingFunction(Tokens.MOTION_EASE.value())
                iterationCount(null)
            }
        }
        style(desc(c(MotionClass.STATUS), c(MotionClass.DOT))) {
            animation(MotionKeyframe.LAB_READY_BREATHE) {
                duration((5).s)
                timingFunction(AnimationTimingFunction.EaseInOut)
                iterationCount(null)
            }
        }
        style(allOf(c(MotionClass.MASCOT), pseudo(SelectorPseudo.BEFORE))) {
            animation(MotionKeyframe.LAB_MASCOT_FLOAT) {
                duration((6).s)
                timingFunction(AnimationTimingFunction.EaseInOut)
                iterationCount(null)
            }
        }
        style(c(MotionClass.EYES)) {
            animation(MotionKeyframe.LAB_MASCOT_BLINK) {
                duration((9).s)
                timingFunction(AnimationTimingFunction.Linear)
                iterationCount(null)
            }
            setTransformOrigin(TransformOriginValue.CENTER)
        }
        style(
            desc(
                allOf(
                    c(MotionClass.MASCOT),
                    attribute(SelectorAttribute.DATA_GREETING, AttributeValue.TRUE),
                ),
                c(MotionClass.EYES),
            )
        ) {
            animation(MotionKeyframe.LAB_MASCOT_WINK) {
                duration((0.7).s)
                timingFunction(AnimationTimingFunction.EaseInOut)
                iterationCount(1)
            }
        }
        style(
            allOf(
                c(MotionClass.MASCOT),
                attribute(SelectorAttribute.DATA_GREETING, AttributeValue.TRUE),
                pseudo(SelectorPseudo.BEFORE),
            )
        ) {
            animation(MotionKeyframe.LAB_MASCOT_HELLO) {
                duration((1.2).s)
                timingFunction(AnimationTimingFunction.EaseOut)
                iterationCount(1)
            }
        }
        style(c(MotionClass.SIGNAL_NODE)) {
            animation(MotionKeyframe.LAB_SIGNAL_STAGE) {
                duration((10).s)
                timingFunction(AnimationTimingFunction.Linear)
                iterationCount(null)
            }
            composite(CssProperty.ANIMATION_DELAY, "calc(var(--signal-index) * 1.8s)")
        }
        style(child(c(MotionClass.ICON), t(SelectorTag.SPAN))) {
            animation(MotionKeyframe.LAB_ICON_WAVE) {
                duration((1.6).s)
                timingFunction(AnimationTimingFunction.EaseInOut)
                iterationCount(null)
                direction(AnimationDirection.Alternate)
            }
        }
        style(child(c(MotionClass.ICON), allOf(t(SelectorTag.SPAN), nthChild("2")))) {
            set(CssProperty.ANIMATION_DELAY, (-0.5).s)
        }
        style(child(c(MotionClass.ICON), allOf(t(SelectorTag.SPAN), nthChild("3")))) {
            set(CssProperty.ANIMATION_DELAY, (-0.9).s)
        }
        style(desc(c(MotionClass.HERO_LAYOUT), c(LabClass.COURSE_HERO))) {
            animation(MotionKeyframe.LAB_ARRIVE) {
                duration((0.65).s)
                timingFunction(Tokens.MOTION_EASE.value())
                fillMode(AnimationFillMode.Both)
            }
        }
        style(c(MotionClass.HERO)) {
            animation(MotionKeyframe.LAB_ARRIVE) {
                duration((0.8).s)
                timingFunction(Tokens.MOTION_EASE.value())
                fillMode(AnimationFillMode.Both)
            }
            set(CssProperty.ANIMATION_DELAY, (70).ms)
        }
        style(c(LabClass.COURSE_CARD)) {
            composite(
                CssProperty.TRANSITION,
                "transform .24s var(--motion-ease),background-color .2s,border-color .2s,box-shadow .24s",
            )
        }
        style(
            desc(
                c(LabClass.COURSE_INDEX),
                allOf(
                    c(LabClass.COURSE_CARD),
                    attribute(SelectorAttribute.DATA_MOTION_ENTERED, AttributeValue.TRUE),
                ),
            )
        ) {
            animation(MotionKeyframe.LEARNING_CONTENT_ARRIVE) {
                duration((0.45).s)
                timingFunction(Tokens.MOTION_EASE.value())
                fillMode(AnimationFillMode.Backwards)
            }
        }
        style(
            anyOf(
                allOf(c(LabClass.COURSE_CARD), pseudo(SelectorPseudo.HOVER)),
                allOf(c(LabClass.COURSE_CARD), pseudo(SelectorPseudo.FOCUS_VISIBLE)),
            )
        ) {
            transform { translateY((-3).px) }
            composite(CssProperty.BOX_SHADOW, "0 8px 24px #0000000b")
        }
        style(desc(c(LabClass.SITE_HEADER), c(LabClass.NAV_ITEM))) {
            position(Position.Relative)
            composite(
                CssProperty.TRANSITION,
                "background-color .18s,color .18s,transform .18s var(--motion-ease)",
            )
        }
        style(
            desc(
                c(LabClass.SITE_HEADER),
                allOf(c(LabClass.NAV_ITEM), c(LabClass.ACTIVE), pseudo(SelectorPseudo.AFTER)),
            )
        ) {
            composite(CssProperty.CONTENT, "\"\"")
            position(Position.Absolute)
            set(CssProperty.BOTTOM, (4).px)
            set(CssProperty.LEFT, (25).percent)
            set(CssProperty.RIGHT, (25).percent)
            height((2).px)
            background(Tokens.ACCENT.value())
            borderRadius((2).px)
            animation(MotionKeyframe.LAB_NAV_LINE) {
                duration((0.32).s)
                timingFunction(Tokens.MOTION_EASE.value())
                fillMode(AnimationFillMode.Both)
            }
            setTransformOrigin(TransformOriginValue.CENTER)
        }
        style(
            anyOf(
                c(LabClass.PRIMARY),
                c(LabClass.SECONDARY),
                c(MotionClass.TOGGLE),
                c(MotionClass.MASCOT),
                t(SelectorTag.SUMMARY),
            )
        ) {
            composite(
                CssProperty.TRANSITION,
                "transform var(--motion-fast) var(--motion-ease),background-color .18s,border-color .18s,box-shadow .18s",
            )
        }
        style(allOf(c(LabClass.PRIMARY), pseudo(SelectorPseudo.HOVER))) {
            composite(
                CssProperty.BOX_SHADOW,
                "0 4px 15px color-mix(in srgb,var(--accent) 15%,transparent)",
            )
        }
        style(
            anyOf(
                allOf(c(LabClass.PRIMARY), pseudo(SelectorPseudo.ACTIVE)),
                allOf(c(LabClass.SECONDARY), pseudo(SelectorPseudo.ACTIVE)),
                allOf(c(MotionClass.TOGGLE), pseudo(SelectorPseudo.ACTIVE)),
            )
        ) {
            transform {
                translateY((1).px)
                scale(0.98)
            }
        }
        style(i(Id.COURSE_START)) {
            position(Position.Relative)
            setOverflow(OverflowValue.HIDDEN)
            setIsolation(IsolationValue.ISOLATE)
        }
        style(allOf(i(Id.COURSE_START), pseudo(SelectorPseudo.AFTER))) {
            composite(CssProperty.CONTENT, "\"\"")
            position(Position.Absolute)
            composite(CssProperty.INSET, "-50% -100%")
            composite(
                CssProperty.BACKGROUND,
                "linear-gradient(110deg,transparent 38%,#ffffff30 48%,transparent 58%)",
            )
            setPointerEvents(PointerEventsValue.NONE)
            animation(MotionKeyframe.LAB_BUTTON_SHEEN) {
                duration((10).s)
                timingFunction(AnimationTimingFunction.EaseInOut)
                iterationCount(null)
            }
        }
        style(
            child(
                allOf(c(LabClass.EXPERIMENT_PANEL), negate(attribute(SelectorAttribute.HIDDEN))),
                c(LabClass.WORKSPACE),
            )
        ) {
            animation(MotionKeyframe.LAB_ARRIVE) {
                duration((0.46).s)
                timingFunction(Tokens.MOTION_EASE.value())
                fillMode(AnimationFillMode.Both)
            }
        }
        style(anyOf(c(LabClass.COURSE_TOPLINE), c(LabClass.COURSE_HISTORY))) {
            animation(MotionKeyframe.LAB_ARRIVE) {
                duration((0.38).s)
                timingFunction(Tokens.MOTION_EASE.value())
                fillMode(AnimationFillMode.Both)
            }
        }
        style(child(c(LabClass.COURSE_CONTEXT), t(SelectorTag.DIV))) {
            animation(MotionKeyframe.LAB_ARRIVE) {
                duration((0.45).s)
                timingFunction(Tokens.MOTION_EASE.value())
                fillMode(AnimationFillMode.Backwards)
            }
        }
        style(child(c(LabClass.COURSE_CONTEXT), allOf(t(SelectorTag.DIV), nthChild("2")))) {
            set(CssProperty.ANIMATION_DELAY, (60).ms)
        }
        style(child(c(LabClass.COURSE_CONTEXT), allOf(t(SelectorTag.DIV), nthChild("3")))) {
            set(CssProperty.ANIMATION_DELAY, (120).ms)
        }
        style(desc(c(LabClass.COURSE_PHASES), c(LabClass.ACTIVE))) {
            animation(MotionKeyframe.LAB_PHASE_LAND) {
                duration((0.5).s)
                timingFunction(Tokens.MOTION_EASE.value())
                fillMode(AnimationFillMode.Both)
            }
        }
        style(c(MotionClass.READING_UPDATE)) {
            animation(MotionKeyframe.LAB_READING_UPDATE) {
                duration((0.55).s)
                timingFunction(AnimationTimingFunction.EaseOut)
                fillMode(AnimationFillMode.Both)
            }
        }
        style(allOf(c(LabClass.ERROR), attribute(SelectorAttribute.ROLE, AttributeValue.ALERT))) {
            animation(MotionKeyframe.LAB_FEEDBACK) {
                duration((0.35).s)
                timingFunction(AnimationTimingFunction.EaseOut)
                fillMode(AnimationFillMode.Both)
            }
        }
        style(
            anyOf(
                c(LabClass.COURSE_RESULT),
                c(AdditionalStyleClass.QUIZ_FEEDBACK),
                c(LabClass.FOCUS_COMPARISON),
            )
        ) {
            animation(MotionKeyframe.LAB_FEEDBACK) {
                duration((0.35).s)
                timingFunction(Tokens.MOTION_EASE.value())
                fillMode(AnimationFillMode.Both)
            }
        }
        style(
            child(
                allOf(t(SelectorTag.DETAILS), attribute(SelectorAttribute.OPEN)),
                negate(t(SelectorTag.SUMMARY)),
            )
        ) {
            animation(MotionKeyframe.LAB_ARRIVE) {
                duration((0.28).s)
                timingFunction(Tokens.MOTION_EASE.value())
                fillMode(AnimationFillMode.Both)
            }
        }
        style(
            anyOf(
                allOf(
                    t(SelectorTag.INPUT),
                    negate(attribute(SelectorAttribute.TYPE, AttributeValue.RANGE)),
                    pseudo(SelectorPseudo.FOCUS_VISIBLE),
                ),
                allOf(t(SelectorTag.TEXTAREA), pseudo(SelectorPseudo.FOCUS_VISIBLE)),
            )
        ) {
            composite(CssProperty.TRANSITION, "box-shadow .2s")
            composite(CssProperty.BOX_SHADOW, "0 0 0 5px var(--accent-soft)")
        }
    }
    frames(MotionKeyframe.LAB_WAVE_TRAVEL) {
        each(100.percent) {
            composite(CssProperty.TRANSFORM, "translateX(var(--wave-shift,-152px))")
        }
    }
    frames(MotionKeyframe.LAB_SIGNAL_SCAN) {
        each(0.percent, 10.percent) {
            set(CssProperty.LEFT, (-16).percent)
            opacity(0)
        }
        each(15.percent) { opacity(1) }
        each(45.percent) {
            set(CssProperty.LEFT, (110).percent)
            opacity(0)
        }
        each(100.percent) {
            set(CssProperty.LEFT, (110).percent)
            opacity(0)
        }
    }
    frames(MotionKeyframe.LAB_READY_BREATHE) {
        each(0.percent, 100.percent) { composite(CssProperty.BOX_SHADOW, "0 0 0 0 transparent") }
        each(50.percent) { composite(CssProperty.BOX_SHADOW, "0 0 0 4px var(--accent-soft)") }
    }
    frames(MotionKeyframe.LAB_MASCOT_FLOAT) {
        each(0.percent, 100.percent) {
            transform {
                translateY(0.px)
                rotate((-4).deg)
            }
        }
        each(50.percent) {
            transform {
                translateY((-4).px)
                rotate((4).deg)
            }
        }
    }
    frames(MotionKeyframe.LAB_MASCOT_BLINK) {
        each(0.percent, 42.percent, 45.percent, 72.percent, 75.percent, 100.percent) {
            transform { scaleY(1) }
        }
        each(43.5.percent, 73.5.percent) { transform { scaleY(0.12) } }
    }
    frames(MotionKeyframe.LAB_MASCOT_WINK) {
        each(0.percent, 100.percent) { transform { rotate(0.deg) } }
        each(40.percent) {
            transform {
                rotate((-15).deg)
                scaleY(0.25)
            }
        }
        each(70.percent) { transform { rotate((8).deg) } }
    }
    frames(MotionKeyframe.LAB_MASCOT_HELLO) {
        each(0.percent) {
            transform { scale(1) }
            opacity(1)
        }
        each(100.percent) {
            transform {
                scale(1.65)
                rotate((50).deg)
            }
            opacity(0)
        }
    }
    frames(MotionKeyframe.LAB_SIGNAL_STAGE) {
        each(0.percent, 17.percent) {
            color(Tokens.ACCENT.value())
            borderColor(Tokens.BORDER.value())
            background(Tokens.ACCENT_SOFT.value())
        }
        each(22.percent, 100.percent) {
            color(Tokens.MUTED.value())
            borderColor(Color.transparent)
            background(Tokens.BG.value())
        }
    }
    frames(MotionKeyframe.LAB_ICON_WAVE) {
        each(0.percent) { transform { scaleY(0.5) } }
        each(100.percent) { transform { scaleY(1) } }
    }
    frames(MotionKeyframe.LAB_ARRIVE) {
        each(0.percent) {
            opacity(0)
            transform { translateY((12).px) }
        }
        each(100.percent) {
            opacity(1)
            transform { translateY(0.px) }
        }
    }
    frames(MotionKeyframe.LAB_NAV_LINE) {
        each(0.percent) { transform { scaleX(0) } }
        each(100.percent) { transform { scaleX(1) } }
    }
    frames(MotionKeyframe.LAB_BUTTON_SHEEN) {
        each(0.percent, 68.percent) { transform { translateX((-55).percent) } }
        each(84.percent, 100.percent) { transform { translateX((55).percent) } }
    }
    frames(MotionKeyframe.LAB_PHASE_LAND) {
        each(0.percent) { composite(CssProperty.BOX_SHADOW, "inset 0 -2px transparent") }
        each(100.percent) { composite(CssProperty.BOX_SHADOW, "inset 0 -2px var(--accent)") }
    }
    frames(MotionKeyframe.LAB_READING_UPDATE) {
        each(0.percent) {
            background(Tokens.ACCENT_SOFT.value())
            composite(CssProperty.FILTER, "brightness(1.25)")
        }
        each(100.percent) {
            background(Color.transparent)
            setFilter(FilterValue.NONE)
        }
    }
    frames(MotionKeyframe.LAB_FEEDBACK) {
        each(0.percent) {
            opacity(0.5)
            transform { translateY((4).px) }
        }
        each(100.percent) {
            opacity(1)
            setTransform(TransformValue.NONE)
        }
    }
    style(
        anyOf(
            desc(
                allOf(
                    t(SelectorTag.HTML),
                    negate(attribute(SelectorAttribute.DATA_MOTION, AttributeValue.RUNNING)),
                ),
                universal,
            ),
            desc(
                allOf(
                    t(SelectorTag.HTML),
                    negate(attribute(SelectorAttribute.DATA_MOTION, AttributeValue.RUNNING)),
                ),
                allOf(universal, pseudo(SelectorPseudo.BEFORE)),
            ),
            desc(
                allOf(
                    t(SelectorTag.HTML),
                    negate(attribute(SelectorAttribute.DATA_MOTION, AttributeValue.RUNNING)),
                ),
                allOf(universal, pseudo(SelectorPseudo.AFTER)),
            ),
            desc(attribute(SelectorAttribute.DATA_MOTION_IN_VIEW, AttributeValue.FALSE), universal),
            desc(
                attribute(SelectorAttribute.DATA_MOTION_IN_VIEW, AttributeValue.FALSE),
                allOf(universal, pseudo(SelectorPseudo.BEFORE)),
            ),
            desc(
                attribute(SelectorAttribute.DATA_MOTION_IN_VIEW, AttributeValue.FALSE),
                allOf(universal, pseudo(SelectorPseudo.AFTER)),
            ),
            desc(attribute(SelectorAttribute.HIDDEN), universal),
            desc(
                attribute(SelectorAttribute.HIDDEN),
                allOf(universal, pseudo(SelectorPseudo.BEFORE)),
            ),
            desc(
                attribute(SelectorAttribute.HIDDEN),
                allOf(universal, pseudo(SelectorPseudo.AFTER)),
            ),
        )
    ) {
        important { setAnimationPlayState(AnimationPlayStateValue.PAUSED) }
    }
    style(
        anyOf(
            desc(
                allOf(
                    t(SelectorTag.HTML),
                    negate(attribute(SelectorAttribute.DATA_MOTION, AttributeValue.RUNNING)),
                ),
                universal,
            ),
            desc(
                allOf(
                    t(SelectorTag.HTML),
                    negate(attribute(SelectorAttribute.DATA_MOTION, AttributeValue.RUNNING)),
                ),
                allOf(universal, pseudo(SelectorPseudo.BEFORE)),
            ),
            desc(
                allOf(
                    t(SelectorTag.HTML),
                    negate(attribute(SelectorAttribute.DATA_MOTION, AttributeValue.RUNNING)),
                ),
                allOf(universal, pseudo(SelectorPseudo.AFTER)),
            ),
        )
    ) {
        important { setTransition(TransitionValue.NONE) }
        important { setScrollBehavior(ScrollBehaviorValue.AUTO) }
    }
    style(
        anyOf(
            desc(
                allOf(
                    t(SelectorTag.HTML),
                    negate(attribute(SelectorAttribute.DATA_MOTION, AttributeValue.RUNNING)),
                ),
                matchesAny(
                    anyOf(
                        c(LabClass.COURSE_HERO),
                        c(MotionClass.HERO),
                        c(LabClass.COURSE_CARD),
                        c(LabClass.COURSE_VISUAL),
                        c(LabClass.EXPERIMENT_PANEL),
                        c(LabClass.WORKSPACE),
                        c(LabClass.COURSE_TOPLINE),
                        c(LabClass.COURSE_HISTORY),
                        c(LabClass.DERIVATION_CARD),
                        c(LabClass.DERIVATION_CONNECTOR),
                        c(LabClass.REVEAL_ENTER),
                        c(LabClass.DERIVATION_PLAYFUL),
                        c(MotionClass.READING_UPDATE),
                        c(LabClass.ERROR),
                        c(LabClass.COURSE_RESULT),
                        c(LabClass.FOCUS_COMPARISON),
                        child(c(LabClass.COURSE_CONTEXT), t(SelectorTag.DIV)),
                    )
                ),
            ),
            child(
                desc(
                    allOf(
                        t(SelectorTag.HTML),
                        negate(attribute(SelectorAttribute.DATA_MOTION, AttributeValue.RUNNING)),
                    ),
                    allOf(t(SelectorTag.DETAILS), attribute(SelectorAttribute.OPEN)),
                ),
                negate(t(SelectorTag.SUMMARY)),
            ),
        )
    ) {
        important { setAnimation(AnimationValue.NONE) }
    }
    media(DesignMedia.PREFERS_REDUCED_MOTION_REDUCE.query) {
        style(
            anyOf(
                universal,
                allOf(universal, pseudo(SelectorPseudo.BEFORE)),
                allOf(universal, pseudo(SelectorPseudo.AFTER)),
            )
        ) {
            important { setAnimation(AnimationValue.NONE) }
            important { setTransition(TransitionValue.NONE) }
            important { setScrollBehavior(ScrollBehaviorValue.AUTO) }
        }
    }
    media(DesignMedia.HOVER_NONE.query) {
        style(allOf(c(LabClass.COURSE_CARD), pseudo(SelectorPseudo.HOVER))) {
            setTransform(TransformValue.NONE)
            setBoxShadow(BoxShadowValue.NONE)
        }
    }
    media(CSSMediaQuery.MediaFeature("max-width", (1180).px)) {
        style(c(MotionClass.TOOLS)) {
            width((100).percent)
            justifyContent(JustifyContent.FlexEnd)
        }
        style(desc(c(LabClass.SITE_HEADER), c(LabClass.BRAND))) {
            setMarginRight(MarginRightValue.AUTO)
        }
        style(c(MotionClass.HERO_LAYOUT)) {
            gap((24).px)
            columns(
                GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1.15)),
                GridTrack.MinMax(GridTrack.Fixed((300).px), GridTrack.Fraction(1)),
            )
        }
        style(c(MotionClass.SIGNAL)) { gap((12).px) }
        style(child(c(MotionClass.SIGNAL), c(LabClass.SMALL))) { display(DisplayStyle.None) }
    }
    media(CSSMediaQuery.MediaFeature("max-width", (900).px)) {
        style(c(MotionClass.HERO_LAYOUT)) {
            columns(GridTrack.Fraction(1))
            gap((12).px)
            marginBottom((30).px)
        }
        style(desc(c(MotionClass.HERO_LAYOUT), c(LabClass.COURSE_HERO))) { paddingBottom(0.px) }
        style(c(MotionClass.HERO)) {
            maxWidth((620).px)
            width((100).percent)
            setJustifySelf(JustifySelfValue.CENTER)
        }
        style(c(MotionClass.FIGURE)) {
            minHeight((100).px)
            marginTop((8).px)
            marginBottom((4).px)
        }
        style(desc(c(MotionClass.FIGURE), t(SelectorTag.SVG))) { maxHeight((155).px) }
        style(desc(desc(c(MotionClass.HERO_LAYOUT), c(LabClass.COURSE_HERO)), t(SelectorTag.H1))) {
            maxWidth((750).px)
        }
        style(c(MotionClass.SIGNAL_STAGES)) {
            gap((8).px)
            width((100).percent)
        }
    }
    media(CSSMediaQuery.MediaFeature("max-width", (760).px)) {
        style(c(MotionClass.TOOLS)) {
            width(auto)
            composite(CssProperty.GRID_COLUMN, "2")
            composite(CssProperty.GRID_ROW, "1")
            gap((6).px)
        }
        style(c(MotionClass.TOGGLE)) {
            fontSize((11).px)
            padding((8).px)
            minWidth((44).px)
        }
        style(
            child(
                c(MotionClass.TOGGLE),
                allOf(t(SelectorTag.SPAN), pseudo(SelectorPseudo.LAST_CHILD)),
            )
        ) {
            display(DisplayStyle.None)
        }
        style(desc(c(LabClass.THEME_CHOOSER), i(Id.THEME_PREFERENCE))) {
            minWidth((98).px)
            padding((7).px, (8).px)
        }
        style(desc(c(LabClass.THEME_CHOOSER), c(LabClass.THEME_TOGGLE_LABEL))) {
            minWidth((36).px)
            fontSize((11).px)
        }
        style(c(MotionClass.HERO)) { padding((16).px) }
        style(c(MotionClass.HERO_LAYOUT)) { marginTop(0.px) }
        style(desc(c(MotionClass.HERO_LAYOUT), c(LabClass.COURSE_HERO))) { paddingTop((14).px) }
        style(desc(desc(c(MotionClass.HERO_LAYOUT), c(LabClass.COURSE_HERO)), t(SelectorTag.H1))) {
            fontSize((38).px)
        }
        style(desc(c(MotionClass.HERO_LAYOUT), c(LabClass.COURSE_LEAD))) { fontSize((16).px) }
        style(c(MotionClass.FIGURE)) {
            marginLeft((-16).px)
            marginRight((-16).px)
        }
        style(c(MotionClass.HEAD)) {
            fontSize((9).px)
            gap((10).px)
        }
        style(c(MotionClass.SIGNAL)) { marginBottom((20).px) }
        style(c(MotionClass.SIGNAL_NODE)) {
            fontSize((9).px)
            gap((5).px)
            padding((5).px, (3).px)
        }
        style(c(MotionClass.SIGNAL_STAGES)) { gap((4).px) }
        style(desc(c(MotionClass.SIGNAL_NODE), c(MotionClass.DOT))) {
            width((4).px)
            height((4).px)
        }
        style(c(MotionClass.WAVE_META)) { fontSize((9).px) }
    }
    media(CSSMediaQuery.MediaFeature("max-width", (360).px)) {
        style(desc(c(LabClass.SITE_HEADER), c(LabClass.BRAND))) {
            fontSize((12).px)
            letterSpacing((0.06).em)
            gap((7).px)
        }
        style(c(LabClass.SITE_HEADER)) { set(CssProperty.COLUMN_GAP, (8).px) }
    }
}
