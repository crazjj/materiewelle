package materiewelle.presentation.styles

import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.css.*

internal fun StyleRules.evidenceReportStyles() {
    style(c(EvidenceReportClass.ROOT)) {
        marginTop(16.px)
        minWidth(0.px)
    }
    style(c(EvidenceReportClass.ACTIONS)) { margin(16.px, 0.px) }
    style(c(EvidenceReportClass.GRID)) {
        display(DisplayStyle.Grid)
        columns(GridTrack.Repeat(2, GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1))))
        gap(12.px)
    }
    style(c(EvidenceReportClass.CARD)) {
        padding(12.px)
        marginBottom(12.px)
        border(1.px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius(Tokens.RADIUS.value())
        minWidth(0.px)
    }
    style(c(EvidenceReportClass.TRANSCRIPT)) {
        minWidth(0.px)
        minHeight(58.px)
        padding(12.px)
        borderRadius(Tokens.RADIUS.value())
        fontSize(16.px)
        setWhiteSpace(WhiteSpaceValue.PRE_WRAP)
        setOverflowWrap(OverflowWrapValue.ANYWHERE)
        backgroundColor(Tokens.SURFACE_RAISED.value())
        color(Tokens.TEXT.value())
    }
    style(c(EvidenceReportClass.STATUS)) {
        color(Tokens.MUTED.value())
        fontSize(15.px)
    }
    style(c(EvidenceReportClass.IDENTIFIER)) {
        fontSize(12.px)
        setOverflowWrap(OverflowWrapValue.ANYWHERE)
    }
    media(CSSMediaQuery.MediaFeature("max-width", 760.px)) {
        style(c(EvidenceReportClass.GRID)) { columns(GridTrack.Fraction(1)) }
        style(c(EvidenceReportClass.ROOT)) { padding(14.px) }
    }
}
