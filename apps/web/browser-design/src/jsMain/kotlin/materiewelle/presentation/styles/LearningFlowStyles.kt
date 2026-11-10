@file:OptIn(org.jetbrains.compose.web.ExperimentalComposeWebApi::class)

package materiewelle.presentation.styles

import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.css.*

/** Local readability changes for the two pilot stations; no replacement layout or motion system. */
internal fun StyleRules.learningFlowStyles() {
    style(c(LearningFlowClass.CHAPTER_HEADING)) {
        composite(CssProperty.GRID_COLUMN, "1/-1")
        margin((28).px, 0.px, (2).px)
        paddingTop((18).px)
        edge(CssProperty.BORDER_TOP, (1).px, LineStyle.Solid, Tokens.BORDER.value())
        fontSize((20).px)
        set(CssProperty.LINE_HEIGHT, 1.45)
        fontWeight(500)
    }
    style(desc(c(LabClass.COURSE_CARD), t(SelectorTag.H3))) {
        margin(0.px)
        fontSize((18).px)
    }
    style(c(LearningFlowClass.PRIMARY_CALCULATION)) {
        padding((18).px)
        margin((14).px, 0.px)
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius(Tokens.RADIUS.value())
        background(Tokens.SURFACE_RAISED.value())
        minWidth(0.px)
    }
    style(desc(c(LearningFlowClass.PRIMARY_CALCULATION), t(SelectorTag.INPUT))) {
        width((100).percent)
        maxWidth((420).px)
        margin((8).px, 0.px, (12).px)
    }
    style(desc(c(LearningFlowClass.PRIMARY_CALCULATION), t(SelectorTag.BUTTON))) {
        margin((4).px, (10).px, (4).px, 0.px)
        setWhiteSpace(WhiteSpaceValue.NORMAL)
        minHeight((44).px)
    }
    style(c(LearningFlowClass.COMPARISON_CALCULATIONS)) { minWidth(0.px) }
    style(c(LearningFlowClass.PRACTICE_GRID)) {
        display(DisplayStyle.Grid)
        columns(GridTrack.Repeat(2, GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1))))
        gap((12).px)
    }
    style(desc(c(LearningFlowClass.PRACTICE_GRID), c(LabClass.PANEL))) {
        padding((14).px)
        margin(0.px)
    }
    style(c(LearningFlowClass.MISSION)) {
        edge(CssProperty.BORDER_LEFT, (3).px, LineStyle.Solid, Tokens.ACCENT.value())
        margin((18).px, 0.px)
        background(Tokens.SURFACE.value())
    }
    style(child(c(LearningFlowClass.MISSION), t(SelectorTag.SUMMARY))) {
        fontWeight(500)
        set(CssProperty.LINE_HEIGHT, 1.6)
    }
    style(c(LearningFlowClass.MISSION_BODY)) { paddingTop((8).px) }
    style(c(LearningFlowClass.MISSION_KICKER)) {
        font(FontSpec((11).px, FontStack.MONO, lineHeight = 1.6))
        color(Tokens.ACCENT.value())
    }
    style(c(LearningFlowClass.MISSION_STEPS)) {
        paddingLeft((22).px)
        margin((14).px, 0.px)
    }
    style(desc(c(LearningFlowClass.MISSION_STEPS), t(SelectorTag.LI))) {
        marginBottom((10).px)
        paddingLeft((4).px)
        set(CssProperty.LINE_HEIGHT, 1.7)
    }
    style(c(LearningFlowClass.MISSION_FINISH)) {
        padding((12).px, (14).px)
        background(Tokens.ACCENT_SOFT.value())
        borderRadius(Tokens.RADIUS.value())
    }
    style(allOf(c(GeometrySlitsClass.RETAINED), c(LearningFlowClass.COMPACT_RETAINED))) {
        columns(GridTrack.Repeat(2, GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1))))
        gap((10).px)
    }
    style(
        allOf(
            c(GeometrySlitsClass.CARD),
            c(LabClass.DERIVATION_CARD),
            c(LabClass.EQUATION_STEP),
            c(LearningFlowClass.COMPACT_CARD),
        )
    ) {
        padding((12).px, (14).px)
        minWidth(0.px)
    }
    style(desc(c(LearningFlowClass.COMPACT_CARD), t(SelectorTag.H3))) {
        fontSize((14).px)
        set(CssProperty.LINE_HEIGHT, 1.5)
    }
    style(desc(c(LearningFlowClass.COMPACT_CARD), c(LabClass.FORMULA))) {
        margin((6).px, 0.px)
        padding((6).px)
        fontSize((18).px)
    }
    style(c(LearningFlowClass.BRAGG_FEEDBACK)) {
        paddingTop((10).px)
        marginTop((10).px)
        edge(CssProperty.BORDER_TOP, (1).px, LineStyle.Solid, Tokens.BORDER.value())
    }
    style(desc(c(LearningFlowClass.BRAGG_FEEDBACK), t(SelectorTag.H4))) {
        fontSize((12).px)
        color(Tokens.ACCENT.value())
        margin((4).px, 0.px)
    }
    style(c(LearningFlowClass.SURFACE_EXPLANATION)) {
        padding((12).px, (14).px)
        background(Tokens.ACCENT_SOFT.value())
        edge(CssProperty.BORDER_LEFT, (3).px, LineStyle.Solid, Tokens.ACCENT.value())
        set(CssProperty.LINE_HEIGHT, 1.65)
    }
    style(c(LearningFlowClass.GEOMETRY_COMPARISON)) { marginTop((14).px) }
    style(c(LearningFlowClass.THEORY_DETAILS)) { margin((18).px, 0.px) }
    media(CSSMediaQuery.MediaFeature("max-width", (760).px)) {
        style(c(LearningFlowClass.CHAPTER_HEADING)) { fontSize((18).px) }
        style(c(LearningFlowClass.PRIMARY_CALCULATION)) { padding((14).px) }
        style(c(LearningFlowClass.PRACTICE_GRID)) { columns(GridTrack.Fraction(1)) }
        style(allOf(c(GeometrySlitsClass.RETAINED), c(LearningFlowClass.COMPACT_RETAINED))) {
            columns(GridTrack.Fraction(1))
        }
    }
}
