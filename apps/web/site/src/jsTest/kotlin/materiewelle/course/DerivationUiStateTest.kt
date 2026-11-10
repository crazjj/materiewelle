package materiewelle.course

import kotlin.test.*

class DerivationUiStateTest {
    @Test fun restoredReadingPositionIsBoundedAndHasNoAutomaticPlayback() {
        assertNull(DeBroglieUiState().visibleStep.value)
        assertEquals(0, DeBroglieUiState(initialStep = -4).visibleStep.value)
        assertEquals(5, DeBroglieUiState(initialStep = 30).visibleStep.value)
    }

    @Test fun readingBackwardKeepsEarlierFormulaRowsAvailable() {
        val view = DeBroglieUiState()
        view.visitStep(4)
        view.visitStep(1)
        assertEquals(1, view.visibleStep.value)
        assertEquals(4, view.furthestStep.value)
        view.visitStep(5)
        assertEquals(5, view.furthestStep.value)
        assertFailsWith<IllegalArgumentException> { view.visitStep(6) }
    }

    @Test fun stepSelectionDoesNotErasePracticeText() {
        val view = DeBroglieUiState(initialStep = 5)
        view.practice.value = DerivationPracticeState(answer = "Mein eigener Ansatz")
        view.visitStep(0)
        assertEquals(0, view.visibleStep.value)
        assertEquals(5, view.furthestStep.value)
        assertEquals("Mein eigener Ansatz", view.practice.value.answer)
    }

    @Test fun practiceKeepsReadingStateAndOwnDraftAcrossLeavingAndReopening() {
        val view = DeBroglieUiState(initialStep = 3)
        view.openPractice()
        view.practice.value = view.practice.value.copy(
            answer = "Ich stelle erst um.", hypothesis = "Meine eigene Erklärung.",
            hint = DerivationHint.PARTIAL_STEP, comparisonOpen = true, exampleOpen = true,
        )
        view.closePractice()
        assertEquals(3, view.readingReturnStep.value)
        view.openPractice()
        assertNull(view.readingReturnStep.value)
        assertEquals(3, view.visibleStep.value)
        assertEquals(3, view.furthestStep.value)
        assertEquals("Ich stelle erst um.", view.practice.value.answer)
        assertEquals("Meine eigene Erklärung.", view.practice.value.hypothesis)
        assertEquals(DerivationHint.PARTIAL_STEP, view.practice.value.hint)
        assertTrue(view.practice.value.comparisonOpen)
        assertFalse(view.practice.value.exampleOpen)
        assertEquals(DerivationPracticeState(), DeBroglieUiState().practice.value)
    }

    @Test fun choicesRequireRequestedHelpAndNeverRewriteFreeAnswers() {
        val first = DerivationPracticeState(answer = "Mein alternativer Weg", hypothesis = "Hypothese zuerst")
        assertEquals(first, first.choose(DerivationPracticeChoice.MOMENTUM))
        val prompted = first.copy(choicesVisible = true).choose(DerivationPracticeChoice.ENERGY)
        val compared = prompted.copy(comparisonOpen = true)
        assertEquals(first.answer, compared.answer)
        assertEquals(first.hypothesis, compared.hypothesis)
        assertEquals(DerivationPracticeChoice.ENERGY, compared.choice)
    }

    @Test fun hintsAreCumulativeAndStopAtOnePartialStep() {
        val first = DerivationPracticeState().nextHint()
        assertEquals(DerivationHint.QUESTION, first.hint)
        val second = first.nextHint()
        assertEquals(DerivationHint.PARTIAL_STEP, second.hint)
        assertEquals(second, second.nextHint())
        assertFalse(second.choicesVisible)
        assertFalse(second.comparisonOpen)
    }

    @Test fun focusRequestIsConsumedOnceRatherThanStealingFocusOnChapterReturn() {
        val view = DeBroglieUiState()
        assertFalse(view.takePracticeFocus())
        view.openPractice()
        assertTrue(view.takePracticeFocus())
        assertFalse(view.takePracticeFocus())
        view.closePractice()
        assertEquals(0, view.readingReturnStep.value)
        assertFalse(view.takePracticeFocus())
        view.finishReadingReturn()
        assertNull(view.readingReturnStep.value)
    }

    @Test fun explicitReturnKeepsItsDestinationWhileTheReadingLayoutRemounts() {
        val view = DeBroglieUiState(initialStep = 2)
        view.openPractice()
        view.practice.value = view.practice.value.copy(answer = "Mein Ansatz")
        view.closePractice()
        view.visitStep(4)
        assertEquals(2, view.readingReturnStep.value)
        assertEquals("Mein Ansatz", view.practice.value.answer)
        view.finishReadingReturn()
        assertNull(view.readingReturnStep.value)
    }
}
