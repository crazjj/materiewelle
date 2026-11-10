package materiewelle.ui.semantics

/** The measured navigation surface, independent of article content. */
enum class ShellId(override val value: String) : ElementId {
    HEADER("site-navigation-header"),
    GITHUB("project-github"),
}
