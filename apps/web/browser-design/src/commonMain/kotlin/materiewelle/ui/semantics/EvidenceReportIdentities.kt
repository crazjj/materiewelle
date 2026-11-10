package materiewelle.ui.semantics

enum class EvidenceReportId(override val value: String) : ElementId {
    ROOT("personal-evidence-report"),
    DOWNLOAD("personal-evidence-download"),
    COPY("personal-evidence-copy"),
    COPY_STATUS("personal-evidence-copy-status"),
    COPY_PREVIEW("personal-evidence-copy-preview"),
    COPY_SELECT("personal-evidence-copy-select"),
    EMPTY("personal-evidence-empty"),
    CALCULATIONS("personal-evidence-calculations"),
    NOTES("personal-evidence-notes"),
    MEASUREMENT("personal-evidence-measurement"),
    CLAIMS("personal-evidence-claims"),
    CONCLUSION("personal-evidence-conclusion"),
    SOURCES("personal-evidence-sources"),
}

enum class EvidenceReportClass(override val cssName: String) : StyleClass {
    ROOT("personal-evidence-report"),
    ACTIONS("personal-evidence-actions"),
    GRID("personal-evidence-grid"),
    CARD("personal-evidence-card"),
    TRANSCRIPT("personal-evidence-transcript"),
    STATUS("personal-evidence-status"),
    IDENTIFIER("personal-evidence-identifier"),
}

enum class ReportTextKind {
    CALCULATION,
    DG_PREDICTION,
    DG_GEOMETRY,
    DG_INTERPRETATION,
    PREDICTION,
    EXPLANATION,
    CLAIM,
    EVIDENCE,
    LIMITATION,
    CONCLUSION,
}

data class ReportTextId(val kind: ReportTextKind, val index: Int = 0) : ElementId {
    init {
        require(index in 0..100)
    }

    override val value: String
        get() = "personal-report-${kind.name.lowercase()}-$index"
}

object EvidenceReportDownload {
    const val FILENAME = "materiewellen-arbeitsbericht.txt"
}
