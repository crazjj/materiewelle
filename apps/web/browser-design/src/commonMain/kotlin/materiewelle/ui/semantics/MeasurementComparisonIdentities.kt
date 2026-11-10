package materiewelle.ui.semantics

/** Three distinct roles: saved own comparison, live editing state, immutable first selection. */
enum class MeasurementCase(val key: String) {
    SAVED("a"),
    CURRENT("b"),
    INITIAL("initial"),
}

enum class MeasurementComparisonId(override val value: String) : ElementId {
    ROOT("measurement-comparison-view"),
    TITLE("measurement-comparison-title"),
    DIFFERENCE("measurement-comparison-difference"),
    ORIGIN("measurement-comparison-origin"),
    CONTACT_NOTE("measurement-comparison-contact-note"),
    OPTIONAL_PROBE("measurement-comparison-optional-probe"),
}

enum class MeasurementCaseField(val key: String) {
    CARD("card"),
    TITLE("title"),
    IMAGE("image"),
    BAND("band"),
    MARKERS("markers"),
    INTERVALS("intervals"),
    MEAN("mean"),
    JUSTIFICATION("justification"),
    CONTACT("contact"),
}

data class MeasurementCaseId(val case: MeasurementCase, val part: MeasurementCaseField) :
    ElementId {
    override val value: String
        get() = "measurement-case-${case.key}-${part.key}"
}

enum class MeasurementComparisonClass(override val cssName: String) : StyleClass {
    ROOT("measurement-comparison-view"),
    HEADER("measurement-comparison-header"),
    GRID("measurement-comparison-grid"),
    CARD("measurement-comparison-card"),
    CURRENT("measurement-comparison-current"),
    CASE_LABEL("measurement-comparison-case-label"),
    EMPTY("measurement-comparison-empty"),
    MEAN("measurement-comparison-mean"),
    FIELD("measurement-comparison-field"),
    FIELD_LABEL("measurement-comparison-field-label"),
    COORDINATES("measurement-comparison-coordinates"),
    INTERVALS("measurement-comparison-intervals"),
    JUSTIFICATION("measurement-comparison-justification"),
    CONTACT("measurement-comparison-contact"),
    CONTACT_NOTE("measurement-comparison-contact-note"),
    DIFFERENCE("measurement-comparison-difference"),
    CHANGES("measurement-comparison-changes"),
    WARNING("measurement-comparison-warning"),
    DETAILS("measurement-comparison-details"),
    IMAGE_ID("measurement-comparison-image-id"),
    ORIGIN("measurement-comparison-origin"),
    OPTIONAL("measurement-comparison-optional"),
}
