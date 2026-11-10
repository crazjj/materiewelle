package materiewelle.course

/** Temporary rehearsal only: no session/report ownership and no free-text assessment. */
data class DerivationPracticeState(
    val active: Boolean = false,
    val exampleOpen: Boolean = false,
    val answer: String = "",
    val hypothesis: String = "",
    val hint: DerivationHint = DerivationHint.NONE,
    val choicesVisible: Boolean = false,
    val choice: DerivationPracticeChoice? = null,
    val comparisonOpen: Boolean = false,
) {
    fun nextHint(): DerivationPracticeState = copy(hint = hint.next)
    fun choose(value: DerivationPracticeChoice): DerivationPracticeState =
        if (choicesVisible) copy(choice = value) else this

    companion object { const val TEXT_LIMIT = 1200 }
}

enum class DerivationHint {
    NONE, QUESTION, PARTIAL_STEP;
    val next: DerivationHint get() = when (this) {
        NONE -> QUESTION
        QUESTION, PARTIAL_STEP -> PARTIAL_STEP
    }
}

/** Every offered relationship is valid; only MOMENTUM fills this particular gap. */
enum class DerivationPracticeChoice { MOMENTUM, ENERGY, HYPOTHESIS }
