package materiewelle.ui.semantics

/** One shared class identity for DOM, style selectors and vector drawings. */
sealed interface StyleClass {
    val cssName: String
}
