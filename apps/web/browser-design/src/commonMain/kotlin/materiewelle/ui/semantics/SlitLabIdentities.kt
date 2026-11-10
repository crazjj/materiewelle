package materiewelle.ui.semantics

enum class SlitLabId(override val value: String) : ElementId {
    CONFIGURATION("slit-lab-configuration"), POSITION("slit-lab-position-label"),
    PROBE("slit-selected-position"), CURVE("slit-current-curve"), PHASE_PANEL("slit-phase-panel"),
    INVESTIGATION("slit-investigation"), SPACING("slit-investigate-spacing"), WIDTH("slit-investigate-width"),
    PREDICTION("slit-investigation-prediction"), REASON("slit-investigation-reason"),
    COMPARE("slit-investigation-compare"), RESULT("slit-investigation-result"),
}

enum class SlitLabClass(override val cssName: String) : StyleClass {
    TOOLBAR("slit-lab-toolbar"), TASK("slit-investigation"), FIELDS("slit-investigation-fields"),
    FEEDBACK("slit-investigation-feedback"), POSITION("slit-position-label"),
}
