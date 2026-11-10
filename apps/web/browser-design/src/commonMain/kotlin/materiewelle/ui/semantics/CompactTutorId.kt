package materiewelle.ui.semantics

/** Identities for the optional section handoff, independent of legacy workshop. */
enum class CompactTutorId(override val value: String) : ElementId {
    TITLE("compact-tutor-title"), EXAMPLES("compact-tutor-examples"),
    OPEN("compact-tutor-open"), PANEL("compact-tutor-panel"), QUESTION("compact-tutor-question"),
    SUMMARY("compact-tutor-summary"), OWN_SUMMARY("compact-tutor-own-summary"),
    OWN("compact-tutor-own"), COPY("compact-tutor-copy"), PREVIEW("compact-tutor-preview"),
    TEXT("compact-tutor-text"), ZIP("compact-tutor-zip"), STATUS("compact-tutor-status"),
    SELECT("compact-tutor-select"),
    HINT("compact-tutor-hint"), EXPLANATION("compact-tutor-explanation"), CHECK("compact-tutor-check"),
    INTENT("compact-tutor-intent"), EARLIER("compact-tutor-earlier"),
    HOW("compact-tutor-how"), SCOPE("compact-tutor-scope"),
    ERROR("compact-tutor-error"), RETRY("compact-tutor-retry"), CLOSE("compact-tutor-close"),
}
