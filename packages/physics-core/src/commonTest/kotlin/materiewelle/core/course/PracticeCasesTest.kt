package materiewelle.core.course

import kotlin.test.*

class PracticeCasesTest {
    @Test fun casesAreCalculatedIndependentlyAndOld150VoltChecksUnlockNothing() {
        val old = SessionSnapshot(work = mapOf(StationId.S06_CALCULATION to StationWork(checked = true, numericAnswer = "100 pm")))
        PracticeCaseId.entries.forEach { assertEquals(PracticeAttempt(), old.practiceWork(it)) }
        assertTrue(PracticeCaseId.U240.question.answerPm in 79.0..80.0)
        assertTrue(PracticeCaseId.U375.question.answerPm in 63.0..64.0)
        assertNotEquals(PracticeCaseId.U240.question.answerPm, PracticeCaseId.U375.question.answerPm)
    }
    @Test fun firstSubmissionSurvivesCorrectionsAndCheckingIsInvalidatedByEditing() {
        val first = PracticeAttempt().edit("70 pm").submit()
        val revised = first.edit("79 pm")
        assertFalse(revised.checked)
        assertEquals("70 pm", revised.firstSubmission)
        assertEquals("70 pm", revised.submit().firstSubmission)
        val state = SessionSnapshot(practice = mapOf(PracticeCaseId.U240 to revised.submit()))
        assertEquals(state, SessionSnapshot.fromJson(state.json()))
        assertEquals(PracticeAttempt(), state.practiceWork(PracticeCaseId.U375))
    }
    @Test fun formatFeedbackNeverAutomaticallyOpensASolution() {
        listOf("79", "NaN pm", "79 V", "-79 pm").forEach { text ->
            val submitted = PracticeAttempt(draft = text).submit()
            assertFalse(submitted.hasSubstantiveAnswer(PracticeCaseId.U240))
            assertFalse(submitted.solutionViewed)
        }
        assertTrue(PracticeAttempt(draft = "79 pm").submit().hasSubstantiveAnswer(PracticeCaseId.U240))
    }
    @Test fun explicitHelpDoesNotClaimAnIndependentFirstAnswer() {
        val helped = PracticeAttempt().revealSolution()
        assertTrue(helped.solutionViewed)
        assertFalse(helped.checked)
        assertNull(helped.firstSubmission)
        assertTrue(helped.edit("79 pm").solutionViewed) // Keep that help was seen.
        assertFailsWith<IllegalArgumentException> { PracticeAttempt(draft = " ").submit() }
        assertFailsWith<IllegalArgumentException> { PracticeAttempt(firstSubmission = "x".repeat(121)).validate() }
    }
}
