package materiewelle.ui.semantics

import materiewelle.core.course.StationId

/**
 * Presentation groups only: existing station identities, routes and learning records stay intact.
 */
enum class LearningChapter(val title: String, val stations: Set<StationId>) {
    HYPOTHESIS(
        "Vom Befund zur Hypothese",
        setOf(
            StationId.S01_OBSERVATION,
            StationId.S02_CLASSICAL,
            StationId.S03_LIGHT,
            StationId.S04_HYPOTHESIS,
        ),
    ),
    PREDICTION(
        "Aus Spannung wird eine Vorhersage",
        setOf(StationId.S05_DERIVATION, StationId.S06_CALCULATION),
    ),
    CRYSTAL_TEST(
        "Von der Aufnahme der Idee zum Kristallversuch",
        setOf(
            StationId.S07_RECEPTION,
            StationId.S08_DG_SETUP,
            StationId.S09_CRYSTAL,
            StationId.S10_GEOMETRY,
            StationId.S11_DG_EVALUATION,
        ),
    ),
    OWN_MEASUREMENT(
        "Künstliche Spalte und deine eigene Bildmessung",
        setOf(StationId.S12_JONSSON_SETUP, StationId.S13_INTERFERENCE, StationId.S14_MEASUREMENT),
    ),
    JUDGMENT(
        "Grenzen prüfen und begründet urteilen",
        setOf(StationId.S15_LIMITS, StationId.S16_CONCLUSION),
    ),
}

data class LearningChapterHeading(val chapter: LearningChapter) : ElementId {
    override val value: String
        get() = "course-chapter-${chapter.name.lowercase()}"
}

enum class ResearchMissionId(override val value: String) : ElementId {
    VOLTAGE("research-voltage"),
    ANGLE("research-angle"),
    MARKERS("research-markers"),
}

enum class LearningFlowId(override val value: String) : ElementId {
    PRIMARY_CALCULATION("course-primary-calculation"),
    CALCULATION_HELP("course-calculation-help"),
    COMPARISON_CALCULATIONS("course-comparison-calculations"),
    BRAGG_FEEDBACK_TITLE("geometry-bragg-feedback-title"),
    SURFACE_EXPLANATION("geometry-surface-explanation"),
    GEOMETRY_COMPARISON("geometry-optional-comparison"),
    THEORY_DETAILS("course-theory-details"),
}

enum class LearningFlowClass(override val cssName: String) : StyleClass {
    CHAPTER_HEADING("course-chapter-heading"),
    PRIMARY_CALCULATION("course-primary-calculation"),
    COMPARISON_CALCULATIONS("course-comparison-calculations"),
    PRACTICE_GRID("course-practice-grid"),
    MISSION("research-mission"),
    MISSION_BODY("research-mission-body"),
    MISSION_KICKER("research-mission-kicker"),
    MISSION_STEPS("research-mission-steps"),
    MISSION_FINISH("research-mission-finish"),
    COMPACT_RETAINED("geometry-compact-retained"),
    COMPACT_CARD("geometry-compact-card"),
    BRAGG_FEEDBACK("geometry-bragg-feedback"),
    SURFACE_EXPLANATION("geometry-surface-explanation"),
    GEOMETRY_COMPARISON("geometry-optional-comparison"),
    THEORY_DETAILS("course-theory-details"),
}
