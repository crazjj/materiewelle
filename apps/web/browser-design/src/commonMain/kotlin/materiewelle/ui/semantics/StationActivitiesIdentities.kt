package materiewelle.ui.semantics

import materiewelle.core.course.LimitCaseId

enum class StationActivityId(override val value: String) : ElementId {
    MEDIA("station-media-comparison"),
    MEDIA_PHOTO("station-media-photo"),
    MEDIA_SCHEMA("station-media-schema"),
    MEDIA_MODEL("station-media-model"),
    MEDIA_MODEL_PLOT("station-media-model-plot"),
    LIGHT("station-light-comparison"),
    LIGHT_PLANCK_ENERGY("station-light-planck-energy"),
    LIGHT_EINSTEIN_ENERGY("station-light-einstein-energy"),
    RECEPTION("station-reception-timeline"),
    DG_TABLE("station-dg-evidence-table"),
    DG_TITLE("station-dg-evidence-title"),
    LIMITS("station-limits-exercise"),
    LIMITS_CHECK("station-limits-check"),
    LIMITS_FEEDBACK("station-limits-feedback"),
    LIMITS_HELP("station-limits-help"),
    EVIDENCE("station-conclusion-evidence"),
    EVIDENCE_HELP("station-conclusion-evidence-help"),
    EVIDENCE_AVAILABILITY("station-conclusion-evidence-availability"),
}

data class LimitAssignmentId(val case: LimitCaseId) : ElementId {
    override val value: String
        get() = "station-limit-${case.name.lowercase()}"
}

enum class EvidenceField {
    CLAIM,
    EVIDENCE,
    LIMITATION,
}

enum class EvidenceSuggestion {
    CALCULATION,
    MEASUREMENT,
}

data class EvidenceRowId(val row: Int) : ElementId {
    init {
        require(row in 0..2)
    }

    override val value: String
        get() = "station-evidence-row-${row + 1}"
}

data class EvidenceFieldId(val row: Int, val field: EvidenceField) : ElementId {
    init {
        require(row in 0..2)
    }

    override val value: String
        get() = "station-evidence-${row + 1}-${this.field.name.lowercase()}"
}

data class EvidenceSuggestionId(val row: Int, val suggestion: EvidenceSuggestion) : ElementId {
    init {
        require(row in 0..2)
    }

    override val value: String
        get() = "station-evidence-${row + 1}-use-${suggestion.name.lowercase()}"
}

enum class StationActivityClass(override val cssName: String) : StyleClass {
    ROOT("station-activity"),
    MEDIA_GRID("activity-media-grid"),
    MEDIA_CARD("activity-media-card"),
    MEDIA_FRAME("activity-media-frame"),
    PHOTO("activity-photo"),
    KIND("activity-kind"),
    CARD_GRID("activity-card-grid"),
    CARD("activity-card"),
    YEAR("activity-year"),
    EQUATION("activity-equation"),
    MUTED("activity-muted"),
    TIMELINE("activity-timeline"),
    TIMELINE_LANE("activity-timeline-lane"),
    TIMELINE_ENTRY("activity-timeline-entry"),
    TABLE_SCROLL("activity-table-scroll"),
    TABLE("activity-data-table"),
    MONO("activity-mono"),
    LIMIT_CASE("activity-limit-case"),
    FEEDBACK("activity-feedback"),
    EVIDENCE_ROW("activity-evidence-row"),
    EVIDENCE_GRID("activity-evidence-grid"),
    EVIDENCE_CELL("activity-evidence-cell"),
    EVIDENCE_ACTIONS("activity-evidence-actions"),
    SUGGESTION("activity-suggestion"),
    SVG("activity-svg"),
    DRAWING_SURFACE("activity-drawing-surface"),
    DRAWING_LINE("activity-drawing-line"),
    DRAWING_ACCENT("activity-drawing-accent"),
    DRAWING_LABEL("activity-drawing-label"),
    DRAWING_TRACE("activity-drawing-trace"),
}

enum class ActivityOption(val value: String) {
    UNSELECTED("")
}
