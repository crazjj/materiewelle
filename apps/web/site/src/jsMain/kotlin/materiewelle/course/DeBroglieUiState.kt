package materiewelle.course

import androidx.compose.runtime.mutableStateOf

/** Reading position and cumulative reveal survive chapter switches; practice is deliberately temporary. */
class DeBroglieUiState(initialStep: Int? = null) {
    val visibleStep = mutableStateOf(initialStep?.coerceIn(0, LAST_STEP))
    val furthestStep = mutableStateOf(initialStep?.coerceIn(0, LAST_STEP) ?: 0)
    val practice = mutableStateOf(DerivationPracticeState())
    val readingReturnStep = mutableStateOf<Int?>(null)
    private var practiceFocusPending = false

    /** Going back changes the current step without concealing previously reached formula lines. */
    fun visitStep(index: Int) {
        require(index in 0..LAST_STEP)
        if (visibleStep.value != index) visibleStep.value = index
        if (index > furthestStep.value) furthestStep.value = index
    }

    fun takePracticeFocus(): Boolean = practiceFocusPending.also { practiceFocusPending = false }

    fun openPractice() {
        practiceFocusPending = true
        readingReturnStep.value = null
        practice.value = practice.value.copy(active = true, exampleOpen = false)
    }

    fun closePractice() {
        practiceFocusPending = false
        readingReturnStep.value = visibleStep.value ?: 0
        practice.value = practice.value.copy(active = false)
    }

    fun finishReadingReturn() { readingReturnStep.value = null }

    companion object { const val LAST_STEP = 5 }
}
