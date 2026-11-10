package materiewelle.ui.semantics

enum class ConclusionId(override val value: String) : ElementId {
    ROOT("conclusion-article"), TITLE("conclusion-title"),
    SYNTHESIS("conclusion-synthesis"), RESOURCES("conclusion-resources"),
    DRAFT("conclusion-draft"), EVIDENCE("conclusion-evidence"),
    SOURCES("conclusion-sources"), HANDOUT("handout-download"),
    RESUME("section-resume"), WORKSPACE("section-workspace"),
}

enum class ConclusionClass(override val cssName: String) : StyleClass {
    SYNTHESIS("conclusion-synthesis"), CHAIN("conclusion-chain"),
    STEP("conclusion-step"), STATEMENT("conclusion-statement"), ACTIONS("conclusion-actions"),
}
