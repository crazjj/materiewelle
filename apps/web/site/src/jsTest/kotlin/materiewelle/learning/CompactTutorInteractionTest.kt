package materiewelle.learning

import kotlin.test.*
import materiewelle.core.course.AppSection
import materiewelle.ui.semantics.CompactTutorId

class CompactTutorInteractionTest {
    @Test fun examplesPreserveDraftAndRefuseOverflow() {
        val example = TutorQuestionExample.SCALE
        assertEquals(example.question, withTutorExample("", example))
        val draft = "Mein eigener Gedanke."
        val combined = withTutorExample(draft, example)!!
        assertTrue(combined.startsWith(draft))
        assertEquals(combined, withTutorExample(combined, example))
        assertNull(withTutorExample("x".repeat(TUTOR_QUESTION_LIMIT), example))
    }

    @Test fun localOriginAndFocusAreScopedToTheVisibleChapter() {
        val state = CompactTutorUiState()
        state.openFrom(TutorHelpPoint.IMAGE_MEASUREMENT)
        assertTrue(state.needsFocus(AppSection.JONSSON))
        assertFalse(state.needsFocus(AppSection.DE_BROGLIE))
        state.focusHandled()
        assertFalse(state.needsFocus(AppSection.JONSSON))
        assertEquals(TutorHelpPoint.IMAGE_MEASUREMENT.entry, state.closeFor(AppSection.JONSSON))
        state.openFrom(TutorHelpPoint.CALCULATION)
        state.cancelNavigationFocus()
        assertFalse(state.needsFocus(AppSection.DE_BROGLIE))
        assertEquals(CompactTutorId.OPEN, state.closeFor(AppSection.CONCLUSION))
    }
}
