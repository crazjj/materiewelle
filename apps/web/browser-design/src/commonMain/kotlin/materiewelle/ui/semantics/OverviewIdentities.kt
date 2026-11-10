package materiewelle.ui.semantics

enum class OverviewId(override val value: String) : ElementId {
    ROOT("overview"), TITLE("overview-title"), START("overview-start"),
    CHAPTERS("overview-chapters"), HELP("overview-help"),
}

enum class OverviewClass(override val cssName: String) : StyleClass {
    ROOT("overview"), INTRO("overview-intro"), LEAD("overview-lead"),
    IMAGE("overview-image"), ACTIONS("overview-actions"),
    CHAPTERS("overview-chapters"), CHAPTER("overview-chapter"),
    NUMBER("overview-number"), SUPPORT("overview-support"), HELP_CONTEXT("overview-help-context"),
}
