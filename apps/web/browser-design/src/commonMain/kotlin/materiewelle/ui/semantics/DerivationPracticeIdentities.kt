package materiewelle.ui.semantics

enum class DerivationPracticeId(override val value: String) : ElementId {
    OPEN("derivation-practice-open"), PANEL("derivation-practice-panel"), TITLE("derivation-practice-title"),
    ANSWER("derivation-practice-answer"), HYPOTHESIS("derivation-practice-hypothesis"),
    HINT("derivation-practice-hint"), HINT_ONE("derivation-practice-hint-one"), HINT_TWO("derivation-practice-hint-two"),
    CHOICES("derivation-practice-choices"), CHOICES_PANEL("derivation-practice-choices-panel"),
    CHOICE_MOMENTUM("derivation-practice-choice-momentum"), CHOICE_ENERGY("derivation-practice-choice-energy"),
    CHOICE_HYPOTHESIS("derivation-practice-choice-hypothesis"), CHOICE_FEEDBACK("derivation-practice-choice-feedback"),
    COMPARE("derivation-practice-compare"), COMPARISON("derivation-practice-comparison"), REVISE("derivation-practice-revise"), CLOSE("derivation-practice-close"),
    EXAMPLE("derivation-practice-example"), EXAMPLE_PANEL("derivation-practice-example-panel"),
    TEMPORARY("derivation-practice-temporary"), MOTION_NOTE("derivation-practice-motion-note"),
    GAP("derivation-practice-gap"), INPUT_HELP("derivation-practice-input-help"),
}

enum class DerivationPracticeClass(override val cssName: String) : StyleClass {
    ROOT("derivation-practice"), EQUATION("derivation-practice-equation"), GAP("derivation-practice-gap"),
    ACTIONS("derivation-practice-actions"), CHOICES("derivation-practice-choices"),
    CHOICE("derivation-practice-choice"), SELECTED("derivation-practice-selected"),
    EXPLANATION("derivation-practice-explanation"), HELP("derivation-practice-help"),
}
