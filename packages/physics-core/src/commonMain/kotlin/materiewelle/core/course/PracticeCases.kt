package materiewelle.core.course

import kotlinx.serialization.Serializable
import materiewelle.core.Voltage

/** Separate identities prevent a checked 150 V example from solving a new case. */
@Serializable
enum class PracticeCaseId(val label: String, private val volts: Double) {
    U240("240 V", 240.0), U375("375 V", 375.0);
    val voltage: Voltage get() = Voltage(volts)
    val question: NumericWavelengthQuestion get() = NumericWavelengthQuestion(voltage)
}

@Serializable
data class PracticeAttempt(
    val draft: String = "",
    val checked: Boolean = false,
    val solutionViewed: Boolean = false,
    val firstSubmission: String? = null,
) {
    fun validate(): PracticeAttempt {
        require(draft.length <= 120 && (firstSubmission == null || firstSubmission.length <= 120))
        require(!checked || draft.isNotBlank())
        require(firstSubmission == null || firstSubmission.isNotBlank())
        return this
    }
    fun edit(text: String): PracticeAttempt = copy(draft = text.take(120), checked = false)
    fun submit(): PracticeAttempt {
        require(draft.isNotBlank())
        return copy(checked = true, firstSubmission = firstSubmission ?: draft).validate()
    }
    fun hasSubstantiveAnswer(id: PracticeCaseId): Boolean = checked &&
        id.question.evaluate(draft).status in setOf(NumericAnswerStatus.CORRECT, NumericAnswerStatus.INCORRECT)
    /** Deliberate help remains available without inventing an independent answer. */
    fun revealSolution(): PracticeAttempt = copy(solutionViewed = true)
}
