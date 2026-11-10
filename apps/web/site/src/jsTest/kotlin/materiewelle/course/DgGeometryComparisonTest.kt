package materiewelle.course

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DgGeometryComparisonTest {
    @Test fun oneQuestionResolvesToDifferentGeometricalObjects() {
        assertEquals(BraggGeometryFocus.PLANE_AND_ANGLE, DgComparisonFeature.REFERENCE.braggFocus)
        assertEquals(SurfaceGeometryFocus.NORMAL_AND_ANGLE, DgComparisonFeature.REFERENCE.surfaceFocus)
        assertEquals(BraggGeometryFocus.PLANE_SPACING, DgComparisonFeature.DISTANCE.braggFocus)
        assertEquals(SurfaceGeometryFocus.ROW_SPACING, DgComparisonFeature.DISTANCE.surfaceFocus)
        assertEquals(BraggGeometryFocus.TWO_EXTRA_PATHS, DgComparisonFeature.PATH.braggFocus)
        assertEquals(SurfaceGeometryFocus.OUTGOING_EXTRA_PATH, DgComparisonFeature.PATH.surfaceFocus)
    }

    @Test fun everyCombinationHasIndependentAngleAndLengthFeedbackOnlyOnRequest() {
        for (angle in DgTrialAngle.entries) for (length in DgTrialLength.entries) {
            val trial = DgGeometryTrial(angle, length, "Eigener, möglicherweise falscher Grund.")
            assertNull(trial.feedback)
            val compared = trial.compare()
            if (angle == DgTrialAngle.UNANSWERED || length == DgTrialLength.UNANSWERED) {
                assertEquals(DgTrialFeedback.Incomplete, compared.feedback)
            } else {
                assertEquals(DgTrialFeedback.Compared(angle == DgTrialAngle.NORMAL, length == DgTrialLength.ROWS), compared.feedback)
            }
            assertEquals(trial.note, compared.note)
        }
    }

    @Test fun correctingAChoiceWithdrawsStaleFeedbackWithoutChangingOwnExplanation() {
        val compared = DgGeometryTrial(DgTrialAngle.PLANE, DgTrialLength.PLANES, "Mein eigener Text").compare()
        val revised = compared.chooseAngle(DgTrialAngle.NORMAL)
        assertNull(revised.feedback)
        assertEquals("Mein eigener Text", revised.note)
        assertEquals(DgTrialLength.PLANES, revised.length)
        assertNull(compared.chooseLength(DgTrialLength.ROWS).feedback)
        // Editing free prose never claims to evaluate it or modifies the actual selections.
        val note = compared.writeNote("Andere Begründung")
        assertEquals(compared.feedback, note.feedback)
        assertEquals(compared.angle, note.angle)
        assertEquals(compared.length, note.length)
    }

    @Test fun hintDoesNotChooseAnAnswerOrOpenFeedback() {
        val trial = DgGeometryTrial(note = "Noch unsicher").requestHint()
        assertTrue(trial.hintOpened)
        assertEquals(DgTrialAngle.UNANSWERED, trial.angle)
        assertEquals(DgTrialLength.UNANSWERED, trial.length)
        assertNull(trial.feedback)
        assertEquals("Noch unsicher", trial.note)
    }

    @Test fun trialSurvivesClosingButDoesNotLeakIntoPersistedViewDraft() {
        val view = DgLessonView()
        val persisted = view.draft()
        view.comparisonFeature = DgComparisonFeature.REFERENCE
        view.openGeometryTrial()
        assertNull(view.comparisonFeature)
        assertEquals(DgTrialAngle.UNANSWERED, view.geometryTrial.angle)
        view.geometryTrial = view.geometryTrial.chooseAngle(DgTrialAngle.NORMAL).chooseLength(DgTrialLength.ROWS).writeNote("Eigene Begründung").compare()
        val own = view.geometryTrial
        view.geometryTrialOpen = false
        view.openGeometryTrial()
        assertEquals(own, view.geometryTrial)
        assertEquals(persisted, view.draft())
        val reloaded = DgLessonView(view.draft())
        assertEquals(DgGeometryTrial(), reloaded.geometryTrial)
        assertFalse(reloaded.geometryTrialOpen)
    }

    @Test fun explicitResetAffectsOnlyTheTemporaryTrial() {
        val view = DgLessonView()
        view.setupStage = 2
        view.braggStage = 1
        view.showGeometry = true
        val before = view.draft()
        view.openGeometryTrial()
        view.geometryTrial = DgGeometryTrial(DgTrialAngle.NORMAL, DgTrialLength.ROWS, "Eigener Text", true, true)
        view.resetGeometryTrial()
        assertEquals(DgGeometryTrial(), view.geometryTrial)
        assertEquals(before, view.draft())
        assertTrue(view.geometryTrialOpen)
    }

    @Test fun focusIsRequestedOnlyByAnExplicitOpenOrReturnAndConsumedOnce() {
        val view = DgLessonView()
        assertFalse(view.takeGeometryTrialFocus())
        view.openGeometryTrial()
        assertTrue(view.takeGeometryTrialFocus())
        assertFalse(view.takeGeometryTrialFocus())
        view.resetGeometryTrial()
        assertFalse(view.takeGeometryTrialFocus())
        view.closeGeometryTrial()
        assertTrue(view.takeGeometryTrialFocus())
        assertFalse(view.takeGeometryTrialFocus())
        assertFalse(DgLessonView(view.draft()).takeGeometryTrialFocus())
    }

    @Test fun draftTextIsBoundedAndUnknownSelectValuesAreNotAccepted() {
        assertEquals(DgGeometryTrial.MAX_NOTE_LENGTH, DgGeometryTrial().writeNote("x".repeat(4000)).note.length)
        assertNull(DgTrialAngle.fromKey("NORMAL"))
        assertNull(DgTrialLength.fromKey("unknown"))
    }
}
