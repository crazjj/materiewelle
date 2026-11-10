package materiewelle.ui.semantics

enum class LearningHelpId(override val value: String) : ElementId {
    INPUT_PREVIEW("course-input-preview"),
    NUMERIC_HELP("course-numeric-step-help"),
    WORKED_CALCULATION("course-worked-calculation"),
    COMPARISON_COMPLETION("course-comparison-completion"),
    COMPARISON_COMPLETION_INPUT("course-comparison-completion-input"),
    SUM_PROBE("course-sum-probe"),
    SUM_PLOT("course-sum-plot"),
    SUM_ANSWER("course-sum-answer"),
    SUM_TIME("course-sum-time"),
    SUM_CHECK("course-sum-check"),
    SUM_FEEDBACK("course-sum-feedback"),
}

enum class NumericHelpStep(val label: String) {
    ENERGY("Ansatz: Woher kommt die Energie?"),
    ROOT("Wurzel: Wie wird aus Energie Impuls?"),
    UNIT("Einheit: Wie wird aus Metern pm?"),
}
