package materiewelle.core

import kotlin.test.*

class LearningTest {
    @Test fun fringeModelHasCorrectPeriodAndMinima() {
        val model = DoubleSlitModel(Voltage(150.0), Length(1.0), Length(1e-6))
        assertEquals(100.137260744756e-6, model.period.meters, 1e-12)
        assertEquals(1.0, model.relativeIntensity(0.0), 1e-12)
        assertEquals(0.0, model.relativeIntensity(model.period.meters / 2), 1e-12)
        assertEquals(1.0, model.relativeIntensity(model.period.meters), 1e-12)
        val higher = model.copy(voltage = Voltage(600.0))
        assertEquals(model.period.meters / 2, higher.period.meters, 1e-12)
        assertFailsWith<IllegalArgumentException> { model.relativeIntensity(Double.NaN) }
        assertFailsWith<IllegalArgumentException> { model.copy(distance = Length(0.0)) }
    }
    @Test fun quizValidatesNumericsRatherThanAnswerText() {
        val quiz = WavelengthQuiz(Voltage(150.0))
        assertTrue(quiz.accepts("50,07"))
        assertFalse(quiz.accepts("100.14"))
        assertFalse(quiz.accepts("NaN"))
        assertFalse(quiz.accepts("Infinity"))
        assertFalse(quiz.accepts("50 pm"))
    }
    @Test fun untrustedQuizDraftMustPassIndependentOracle() {
        val draft = QuizDraft("Vorhersage", 150.0, 600.0, 50.07)
        assertEquals(draft, draft.verifyNumerics())
        assertFailsWith<IllegalArgumentException> { draft.copy(claimedAnswerPm = 100.14).verifyNumerics() }
        assertFailsWith<IllegalArgumentException> { draft.copy(targetVoltageV = 1200.0).verifyNumerics() }
        assertFailsWith<IllegalArgumentException> { draft.copy(sourceVoltageV = Double.NaN).verifyNumerics() }
    }
}
