@file:OptIn(org.jetbrains.compose.web.ExperimentalComposeWebApi::class)

package materiewelle.presentation.styles

import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.css.*

/** Canonical Kotlin style definitions, using shared DOM identities and typed values. */
internal fun StyleRules.courseV2Styles() {
    style(c(CourseStyle.WORK_SURFACE)) {
        minWidth(0.px)
        set(CssProperty.SCROLL_MARGIN_TOP, (24).px)
    }
    style(c(CourseStyle.GUIDANCE)) {
        display(DisplayStyle.Grid)
        columns(GridTrack.Repeat(2, GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1))))
        gap((12).px)
        margin((24).px, 0.px)
    }
    style(desc(c(CourseStyle.GUIDANCE), c(LabClass.NOTES))) {
        margin(0.px)
        padding((14).px, (18).px)
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius(Tokens.RADIUS.value())
        background(Tokens.SURFACE.value())
    }
    style(desc(c(CourseStyle.GUIDANCE), t(SelectorTag.SUMMARY))) { fontWeight(500) }
    style(
        anyOf(
            desc(c(CourseStyle.GUIDANCE), t(SelectorTag.P)),
            desc(c(CourseStyle.GUIDANCE), t(SelectorTag.LI)),
        )
    ) {
        fontSize((15).px)
        set(CssProperty.LINE_HEIGHT, 1.65)
    }
    style(c(CourseStyle.CASES)) {
        edge(CssProperty.BORDER_TOP, (1).px, LineStyle.Solid, Tokens.BORDER.value())
        marginTop((24).px)
        paddingTop((12).px)
        display(DisplayStyle.Grid)
        gap((16).px)
    }
    style(desc(c(CourseStyle.CASES), c(LabClass.PANEL))) {
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius(Tokens.RADIUS.value())
        padding((20).px)
        background(Tokens.SURFACE_RAISED.value())
        minWidth(0.px)
    }
    style(desc(c(CourseStyle.CASES), t(SelectorTag.INPUT))) {
        maxWidth((100).percent)
        width((280).px)
        margin((8).px, (12).px, (12).px, 0.px)
    }
    style(desc(c(CourseStyle.CASES), t(SelectorTag.BUTTON))) {
        margin((6).px, (10).px, (6).px, 0.px)
        setWhiteSpace(WhiteSpaceValue.NORMAL)
    }
    style(c(CourseStyle.TABLE)) {
        width((100).percent)
        setBorderCollapse(BorderCollapseValue.COLLAPSE)
        margin((18).px, 0.px)
        fontSize((14).px)
    }
    style(
        anyOf(
            desc(c(CourseStyle.TABLE), t(SelectorTag.TH)),
            desc(c(CourseStyle.TABLE), t(SelectorTag.TD)),
        )
    ) {
        padding((12).px)
        setTextAlign(TextAlignValue.LEFT)
        edge(CssProperty.BORDER_BOTTOM, (1).px, LineStyle.Solid, Tokens.BORDER.value())
        setVerticalAlign(VerticalAlignValue.TOP)
    }
    style(desc(c(CourseStyle.TABLE), t(SelectorTag.TH))) {
        color(Tokens.MUTED.value())
        fontWeight(500)
    }
    style(c(CourseStyle.MEDIA_GRID)) {
        display(DisplayStyle.Grid)
        columns(GridTrack.Repeat(3, GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1))))
        gap((12).px)
        margin((20).px, 0.px)
    }
    style(child(c(CourseStyle.MEDIA_GRID), t(SelectorTag.SECTION))) {
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius(Tokens.RADIUS.value())
        padding((12).px)
        minWidth(0.px)
    }
    style(
        anyOf(
            desc(c(CourseStyle.MEDIA_GRID), t(SelectorTag.SVG)),
            desc(c(CourseStyle.MEDIA_GRID), t(SelectorTag.IMG)),
        )
    ) {
        height((100).px)
        setObjectFit(ObjectFitValue.CONTAIN)
    }
    style(desc(c(CourseStyle.MEDIA_GRID), t(SelectorTag.P))) {
        fontSize((13).px)
        set(CssProperty.LINE_HEIGHT, 1.6)
    }
    style(anyOf(i(Id.MARKER_WORKSHOP), i(Id.LABORATORY), i(Id.COURSE_TITLE))) {
        set(CssProperty.SCROLL_MARGIN_TOP, (24).px)
    }
    media(CSSMediaQuery.MediaFeature("max-width", (760).px)) {
        style(c(CourseStyle.GUIDANCE)) { columns(GridTrack.Fraction(1)) }
        style(desc(c(CourseStyle.CASES), c(LabClass.PANEL))) { padding((14).px) }
        style(c(CourseStyle.TABLE)) { fontSize((12).px) }
        style(
            anyOf(
                desc(c(CourseStyle.TABLE), t(SelectorTag.TH)),
                desc(c(CourseStyle.TABLE), t(SelectorTag.TD)),
            )
        ) {
            padding((8).px)
        }
        style(c(CourseStyle.MEDIA_GRID)) { columns(GridTrack.Fraction(1)) }
        style(
            anyOf(
                desc(c(CourseStyle.MEDIA_GRID), t(SelectorTag.SVG)),
                desc(c(CourseStyle.MEDIA_GRID), t(SelectorTag.IMG)),
            )
        ) {
            height((140).px)
        }
        style(c(LabClass.COURSE_CONTEXT)) { display(DisplayStyle.Block) }
        style(
            anyOf(
                desc(c(LabClass.COURSE_CONTEXT), t(SelectorTag.BUTTON)),
                desc(c(LabClass.COURSE_CONTEXT), allOf(t(SelectorTag.A), c(LabClass.SECONDARY))),
            )
        ) {
            display(DisplayStyle.Block)
            width((100).percent)
            marginTop((12).px)
        }
        style(child(c(LabClass.COURSE_TASK), t(SelectorTag.BUTTON))) {
            setWhiteSpace(WhiteSpaceValue.NORMAL)
        }
        style(c(LabClass.COURSE_LAYOUT)) { gap((16).px) }
    }
}
