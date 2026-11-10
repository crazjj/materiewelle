package materiewelle.ui.semantics

/** Local help controls are distinct from the single shared panel. */
enum class CompactTutorEntryId(override val value: String) : ElementId {
    CALCULATION("tutor-help-calculation"),
    DE_BROGLIE_MODEL("tutor-help-de-broglie-model"),
    DG_MODEL("tutor-help-dg-model"),
    SLIT_MODEL("tutor-help-slit-model"),
    DG_COMPARISON("tutor-help-dg-comparison"),
    IMAGE_MEASUREMENT("tutor-help-image-measurement"),
    CONCLUSION("tutor-help-conclusion"),
}

enum class CompactTutorExampleId(override val value: String) : ElementId {
    VOLTAGE("tutor-example-voltage"), HYPOTHESIS("tutor-example-hypothesis"),
    ANGLE("tutor-example-angle"), DIFFERENCE("tutor-example-difference"),
    SCALE("tutor-example-scale"), MAXIMA("tutor-example-maxima"),
    ARGUMENT("tutor-example-argument"), LIMIT("tutor-example-limit"),
    FIRST_STEP("tutor-example-first-step"), DETECTOR("tutor-example-detector"),
    SPACING("tutor-example-spacing"), WIDTH("tutor-example-width"),
}

enum class CompactTutorPanelClass(override val cssName: String) : StyleClass {
    CONTEXT("compact-tutor-context"),
    WIDE("compact-tutor-wide"),
    BACK("compact-tutor-back"),
    LAYOUT("compact-tutor-layout"), PREPARE("compact-tutor-prepare"), TRANSFER("compact-tutor-transfer"),
    MODES("compact-tutor-modes"), INTRO("compact-tutor-intro"), SCOPE("compact-tutor-scope"),
    STEP("compact-tutor-step"), CONTEXT_SUMMARY("compact-tutor-context-summary"),
}
