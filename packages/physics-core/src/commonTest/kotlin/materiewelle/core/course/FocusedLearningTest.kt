package materiewelle.core.course

import materiewelle.core.*
import kotlin.test.*

class FocusedLearningTest {
    private val image = SessionSnapshot.CANONICAL_IMAGE_IDENTITY
    private val marks = listOf(551, 564, 578, 592, 606, 620, 634, 647).map(::NativeImageMark)

    @Test fun voltageScalingKeepsEnergyMomentumAndWavelengthDistinct() {
        val fourfold = VoltageScaling(Voltage(150.0), Voltage(600.0))
        assertEquals(4.0, fourfold.energyFactor)
        assertEquals(2.0, fourfold.momentumFactor)
        assertEquals(.5, fourfold.wavelengthFactor, 1e-12)
        val ninefold = VoltageScaling(Voltage(100.0), Voltage(900.0))
        assertEquals(9.0, ninefold.energyFactor)
        assertEquals(3.0, ninefold.momentumFactor)
        assertEquals(1.0 / 3, ninefold.wavelengthFactor, 1e-12)
    }

    @Test fun innerPixelChangesTwoIntervalsAndPreservesMean() {
        val result = assertIs<PixelSensitivity.Ready>(nativePixelSensitivity(marks, image, MarkerPerturbation.INNER, 1))
        assertEquals(listOf(14, 13, 14, 14, 14, 14, 13), result.changedIntervals)
        assertEquals(96.0 / 7, result.originalMeanPx, 1e-12)
        assertEquals(0.0, result.meanChangePx)
        assertEquals(564, marks[1].xPx)
    }

    @Test fun endpointPixelChangesMeanByOneOverNumberOfIntervals() {
        for (delta in -1..1) {
            val result = assertIs<PixelSensitivity.Ready>(nativePixelSensitivity(marks, image, MarkerPerturbation.LAST, delta))
            assertEquals(delta / 7.0, result.meanChangePx, 1e-12)
            assertEquals(647, marks.last().xPx)
        }
    }

    @Test fun noUnstatedMeasurementOrInnerPointIsInvented() {
        assertIs<PixelSensitivity.Unavailable>(nativePixelSensitivity(emptyList(), image, MarkerPerturbation.LAST, 1))
        assertIs<PixelSensitivity.Unavailable>(nativePixelSensitivity(marks.take(1), image, MarkerPerturbation.LAST, 1))
        assertIs<PixelSensitivity.Unavailable>(nativePixelSensitivity(marks.take(2), image, MarkerPerturbation.INNER, 1))
        assertIs<PixelSensitivity.Ready>(nativePixelSensitivity(marks.take(2), image, MarkerPerturbation.LAST, 1))
    }

    @Test fun crossingMarkersAndLeavingActualCropAreUnavailable() {
        val adjacent = listOf(550, 551, 552).map(::NativeImageMark)
        assertIs<PixelSensitivity.Unavailable>(nativePixelSensitivity(adjacent, image, MarkerPerturbation.INNER, 1))
        val edge = listOf(690, 702).map(::NativeImageMark)
        assertIs<PixelSensitivity.Unavailable>(nativePixelSensitivity(edge, image, MarkerPerturbation.LAST, 1))
        assertFailsWith<IllegalArgumentException> { nativePixelSensitivity(marks, image, MarkerPerturbation.LAST, 2) }
    }

    @Test fun predictionPhaseAndExplanationSurviveWithoutClaimingAutomaticAssessment() {
        val work = StationWork(prediction = "Ich vermute eine kleinere Wellenlänge.", learningPhase = LearningPhase.EXPLORE,
            note = "Meine spätere Erklärung", explanationStep = 3)
        val state = SessionSnapshot(work = mapOf(StationId.S05_DERIVATION to work))
        assertEquals(work, SessionSnapshot.fromJson(state.json()).stationWork(StationId.S05_DERIVATION))
        assertFalse(work.checked)
        assertFailsWith<IllegalArgumentException> { work.copy(prediction = "a".repeat(1001)).validate() }
        assertEquals(QuestionOptionId.C, QuestionBank[QuestionId.Q09].options.single { it.isCorrect }.id)
    }

    @Test fun previousSavedWorkWithoutLearningFieldsKeepsItsOwnTextAndDerivationStep() {
        val restored = SessionSnapshot.fromJson("""{"work":{"S05_DERIVATION":{"note":"Meine bisherige Erklärung","explanationStep":2}}}""")
        val work = restored.stationWork(StationId.S05_DERIVATION)
        assertEquals("Meine bisherige Erklärung",work.note)
        assertEquals(2,work.explanationStep)
        assertEquals(LearningPhase.PREDICT,work.learningPhase)
        assertEquals("",work.prediction)
    }
}
