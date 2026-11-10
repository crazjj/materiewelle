package materiewelle.ui.semantics

import materiewelle.core.course.OwnWorkSection

enum class OwnWorkId(override val value: String) : ElementId {
    ROOT("own-work-summary"), TITLE("own-work-summary-title"),
}

enum class OwnWorkPart(val key: String) { ROW("row"), STATUS("status"), LINK("link"), DETAILS("details") }

data class OwnWorkElement(val section: OwnWorkSection, val part: OwnWorkPart) : ElementId {
    override val value: String get() {
        val key = when (section) {
            OwnWorkSection.CALCULATION -> "calculation"
            OwnWorkSection.DG -> "dg"
            OwnWorkSection.MEASUREMENT -> "measurement"
            OwnWorkSection.CONCLUSION -> "conclusion"
        }
        return "own-work-$key-${part.key}"
    }
}

enum class OwnWorkClass(override val cssName: String) : StyleClass {
    ROOT("own-work-summary"), LIST("own-work-list"), ROW("own-work-row"),
    LINK("own-work-link"), BODY("own-work-body"), STATUS("own-work-status"), DETAIL("own-work-detail"),
}
