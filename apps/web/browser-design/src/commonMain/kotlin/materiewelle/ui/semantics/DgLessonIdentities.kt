package materiewelle.ui.semantics

import materiewelle.core.course.DgLessonStep

enum class DgLessonId(override val value: String) : ElementId {
    NAV("dg-lesson-navigation"), ALL("dg-lesson-all"), PREVIOUS("dg-lesson-previous"), NEXT("dg-lesson-next"),
    FOOTER_NEXT("dg-lesson-footer-next"),
    SETUP_NEXT("dg-setup-next"), SETUP_REPLAY("dg-setup-replay"),
    SETUP_EXPLANATION("dg-setup-explanation"), BRAGG_EXPLANATION("dg-bragg-explanation"),
    SETUP_SOURCE("dg-setup-source-text"), SETUP_CRYSTAL("dg-setup-crystal-text"), SETUP_DETECTOR("dg-setup-detector-text"),
    BRAGG_REFERENCE("dg-bragg-reference-text"), BRAGG_INCOMING("dg-bragg-incoming-text"), BRAGG_OUTGOING("dg-bragg-outgoing-text"),
    BRAGG_REPLAY("dg-bragg-replay"), OBSERVATION_NOTE("dg-observation-note"),
    PREDICTION_INPUT("dg-prediction-input"), GEOMETRY_INPUT("dg-geometry-input"),
    SHOW_PREDICTION("dg-show-prediction"), SHOW_GEOMETRY("dg-show-geometry"),
    PREDICTION_RESULT("dg-prediction-result"), GEOMETRY_RESULT("dg-geometry-result"),
    SOURCE_COMPARISON("dg-source-comparison"), VARIATION("dg-model-variation"),
    CONCLUSION_NOTE("dg-conclusion-note"), SOURCE_NOTES("dg-source-notes"),
}

enum class DgLessonClass(override val cssName: String) : StyleClass {
    ROOT("dg-lesson"), HEADER("dg-lesson-header"), NAV("dg-lesson-nav"),
    ACTIVE("dg-lesson-active"), NUMBER("dg-lesson-number"), STAGE("dg-lesson-stage"),
    META("dg-lesson-meta"), SCENE("dg-lesson-scene"), FIGURE("dg-lesson-figure"),
    EXPLANATION("dg-lesson-explanation"), FOOTER("dg-lesson-footer"),
    SOURCE_CASE("dg-lesson-source-case"), QUANTITY("dg-lesson-quantity"),
    CALCULATIONS("dg-lesson-calculations"), CALCULATION("dg-lesson-calculation"),
    RESULT("dg-lesson-result"), NOTE("dg-lesson-note"),
    COMPLETE_SCENE("dg-lesson-complete-scene"), SETUP_OVERVIEW("dg-lesson-setup-overview"),
    FIGURE_ACTIONS("dg-lesson-figure-actions"),
    GUIDED_EXPLANATION("dg-guided-explanation"), EXPLANATION_STEP("dg-explanation-step"),
    EXPLANATION_ACTIVE("dg-explanation-active"),
    CALCULATION_INTRO("dg-calculation-intro"), CALCULATION_FORMULA("dg-calculation-formula"),
    CALCULATION_ENTRY("dg-calculation-entry"),
}

enum class DgLessonProperty(val cssName: String) {
    NAV_TOP("--dg-navigation-top"), FIGURE_TOP("--dg-figure-top"),
}

fun DgLessonStep.elementId(): DgArticleId = when (this) {
    DgLessonStep.SETUP -> DgArticleId.SETUP
    DgLessonStep.OBSERVATION -> DgArticleId.OBSERVATION
    DgLessonStep.BRAGG -> DgArticleId.BRAGG
    DgLessonStep.GEOMETRY -> DgArticleId.STRUCTURE
    DgLessonStep.CALCULATION -> DgArticleId.COMPARISON
    DgLessonStep.CONCLUSION -> DgArticleId.SOURCES
}
