package materiewelle.ui.semantics

enum class SlitPhaseId(override val value: String) : ElementId {
    ROOT("slit-phase"), TITLE("slit-phase-title"), POSITION("slit-phase-position"),
    CENTER("slit-phase-center"), NULL_NEGATIVE("slit-phase-null-negative"), NULL_POSITIVE("slit-phase-null-positive"),
    DIAGRAM("slit-phase-diagram"), PATH_DIAGRAM("slit-phase-path-diagram"), READOUT("slit-phase-readout"), EXPLANATION("slit-phase-explanation"),
}

enum class SlitPhaseClass(override val cssName: String) : StyleClass {
    ROOT("slit-phase"), CONTROLS("slit-phase-controls"), SELECTED("slit-phase-selected"),
    LAYOUT("slit-phase-layout"), FIGURE("slit-phase-figure"), READOUT("slit-phase-readout"),
    READOUT_TERM("slit-phase-readout-term"), READOUT_VALUE("slit-phase-readout-value"),
    NOTE("slit-phase-note"), LEGEND("slit-phase-legend"),
}

/** Browser output of computed quantities, never an alternate state owner. */
object SlitPhaseData {
    val POSITION = LabData.Key<Double>("data-position-m") { checkedNumber(it) }
    val PATH_DIFFERENCE = LabData.Key<Double>("data-path-difference-m") { checkedNumber(it) }
    val PHASE_TURNS = LabData.Key<Double>("data-phase-turns") { checkedNumber(it) }
    val LEFT_INTENSITY = LabData.Key<Double>("data-left-intensity") { checkedNumber(it) }
    val RIGHT_INTENSITY = LabData.Key<Double>("data-right-intensity") { checkedNumber(it) }
    val TOTAL_INTENSITY = LabData.Key<Double>("data-total-intensity") { checkedNumber(it) }

    private fun checkedNumber(value: Double): String {
        require(value.isFinite())
        return value.toString()
    }
}
