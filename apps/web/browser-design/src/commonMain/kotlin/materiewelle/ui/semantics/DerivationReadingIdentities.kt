package materiewelle.ui.semantics

enum class DerivationReadingMode(val key: String, val label: String) {
    MANUAL("manual", "Selbst steuern"),
    FOLLOW("follow", "Automatisch mitgehen"),
    SCROLL("scroll", "Beim Scrollen aufdecken"),
}

enum class DerivationReadingScope(val key: String) {
    COURSE("course"),
    WORKSHOP("workshop"),
}

enum class DerivationReadingPart(val key: String) {
    CONTROLS("controls"),
    STATUS("status"),
    RESUME("resume"),
    FRONTIER("frontier"),
}

data class DerivationReadingId(val scope: DerivationReadingScope, val part: DerivationReadingPart) :
    ElementId {
    override val value
        get() = "${scope.key}-reading-${part.key}"
}

data class DerivationReadingChoiceId(
    val scope: DerivationReadingScope,
    val mode: DerivationReadingMode,
) : ElementId {
    override val value
        get() = "${scope.key}-reading-mode-${mode.key}"
}

data class DerivationReadingCardId(val scope: DerivationReadingScope, val index: Int) : ElementId {
    init {
        require(index >= 0)
    }

    override val value
        get() = "${scope.key}-reading-card-$index"
}

enum class DerivationReadingClass(override val cssName: String) : StyleClass {
    SURFACE("derivation-reading-surface"),
    CONTROLS("derivation-reading-controls"),
    MODES("derivation-reading-modes"),
    SELECTED("derivation-reading-selected"),
    ACTIONS("derivation-reading-actions"),
    FRONTIER("derivation-reading-frontier"),
}
