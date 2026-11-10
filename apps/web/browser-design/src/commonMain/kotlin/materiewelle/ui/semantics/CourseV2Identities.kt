package materiewelle.ui.semantics

import materiewelle.core.course.PracticeCaseId

enum class CourseControl(override val value: String) : ElementId {
    NUMERIC_SOLUTION("course-numeric-solution"),
    TRANSFER_SOLUTION("course-transfer-solution"),
    PRACTICE_LIST("practice-list"),
    GUIDE_TERMS("course-concept-bridges"),
    GUIDE_SELF_CHECK("course-self-check"),
    SLIT_SAVE("course-slit-save"),
    SLIT_CLEAR("course-slit-clear"),
    LIMIT_CASES("course-limit-cases"),
    LIMIT_FEEDBACK("course-limit-feedback"),
    MEDIA_COMPARISON("course-media-comparison"),
    EVIDENCE_TABLE("course-evidence-table"),
    CURRENT_PAGE_TEXT("typst-current-page-text"),
    ACTIVE_REFERENCE("course-active-band-reference"),
}

enum class PracticePart {
    INPUT,
    CHECK,
    SOLUTION,
    FEEDBACK,
}

data class PracticeControl(val case: PracticeCaseId, val part: PracticePart) : ElementId {
    override val value: String
        get() = "practice-${case.name.lowercase()}-${part.name.lowercase()}"
}

enum class CourseStyle(override val cssName: String) : StyleClass {
    WORK_SURFACE("course-work-surface"),
    TABLE("course-data-table"),
    MEDIA_GRID("course-media-grid"),
    CASES("course-new-cases"),
    HISTORY("course-history-pair"),
    GUIDANCE("course-guidance"),
    EVIDENCE_DRAFT("course-evidence-draft"),
}
