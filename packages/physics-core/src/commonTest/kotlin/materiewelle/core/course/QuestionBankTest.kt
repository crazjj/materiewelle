package materiewelle.core.course

import materiewelle.core.Voltage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class QuestionBankTest {
    @Test fun everyPlannedConceptAndTransferHasOneUnambiguousKey() {
        assertEquals(13, QuestionBank.questions.size)
        assertEquals(QuestionId.entries.toSet(), QuestionBank.questions.map { it.id }.toSet())
        for (question in QuestionBank.questions) {
            assertEquals(1, question.options.count { it.isCorrect }, question.id.name)
            assertEquals(QuestionOptionId.entries.toSet(), question.options.map { it.id }.toSet())
            assertTrue(question.options.all { it.feedback.isNotBlank() })
            assertTrue(question.hints.size >= 2)
            assertTrue(question.sourceIds.isNotEmpty())
        }
    }

    @Test fun fourfoldVoltageFeedbackPreservesEnergyMomentumBridge() {
        val question = QuestionBank[QuestionId.Q02]
        assertFalse(question.option(QuestionOptionId.A).isCorrect)
        assertTrue(question.option(QuestionOptionId.A).feedback.contains("√E_kin"))
        assertTrue(question.option(QuestionOptionId.B).isCorrect)
        assertTrue(question.followUp.contains("9U"))
    }

    @Test fun transferKeysPreserveReferenceLineAndIntervalCounts() {
        assertTrue(QuestionBank[QuestionId.T02].option(QuestionOptionId.C).isCorrect)
        assertTrue(QuestionBank[QuestionId.T03].option(QuestionOptionId.A).isCorrect)
        assertFalse(QuestionBank[QuestionId.T03].option(QuestionOptionId.B).isCorrect)
        assertTrue(QuestionBank[QuestionId.T03].followUp.contains("Sage voraus"))
        assertFalse(QuestionBank[QuestionId.T03].followUp.contains("Mittel bleibt gleich"))
        assertFalse(QuestionBank[QuestionId.T02].followUp.contains("70°"))
        assertFalse(QuestionBank[QuestionId.Q01].followUp.contains("λ/3"))
        assertTrue(QuestionBank[QuestionId.T04].option(QuestionOptionId.B).isCorrect)
    }

    @Test fun independentReferenceAnswersAt150And600VoltsAcceptCorrectUnitConversions() {
        val initial = NumericWavelengthQuestion(Voltage(150.0))
        val changed = NumericWavelengthQuestion(Voltage(600.0))
        // Independent saved CODATA-2022 calculations, not a test-generated answer.
        assertEquals(100.137260744756, initial.answerPm, 1e-10)
        assertEquals(50.068630372378, changed.answerPm, 1e-10)
        for (answer in listOf("100 pm", "100,14 pm", "0.100137 nm", "1,00137e-10 m", " 100,1\u00a0pm ")) {
            assertEquals(NumericAnswerStatus.CORRECT, initial.evaluate(answer).status, answer)
        }
        for (answer in listOf("50 pm", "50,07 pm", "0,05007 nm", "5.007e-11 m")) {
            assertEquals(NumericAnswerStatus.CORRECT, changed.evaluate(answer).status, answer)
        }
        assertEquals(NumericAnswerStatus.INCORRECT, changed.evaluate("100 pm").status)
        assertEquals(NumericAnswerStatus.INCORRECT, initial.evaluate("100 nm").status)
    }

    @Test fun roundingToleranceIsInPicometersRegardlessOfUnit() {
        val question = NumericWavelengthQuestion(Voltage(150.0))
        assertEquals(NumericAnswerStatus.CORRECT, question.evaluate("99.64 pm").status)
        assertEquals(NumericAnswerStatus.INCORRECT, question.evaluate("99.63 pm").status)
        assertEquals(NumericAnswerStatus.CORRECT, question.evaluate("0.10063 nm").status)
        assertEquals(NumericAnswerStatus.INCORRECT, question.evaluate("0.10064 nm").status)
    }

    @Test fun editedNumberAndUnitsHaveDistinctActionableFailures() {
        val question = NumericWavelengthQuestion(Voltage(150.0))
        assertEquals(NumericAnswerStatus.EMPTY, question.evaluate(" ").status)
        assertEquals(NumericAnswerStatus.MISSING_UNIT, question.evaluate("100").status)
        assertEquals(NumericAnswerStatus.UNSUPPORTED_UNIT, question.evaluate("100 V").status)
        assertEquals(NumericAnswerStatus.UNSUPPORTED_UNIT, question.evaluate("100 PM").status)
        assertEquals(NumericAnswerStatus.INVALID_NUMBER, question.evaluate("NaN pm").status)
        assertEquals(NumericAnswerStatus.INVALID_NUMBER, question.evaluate("Infinity pm").status)
        assertEquals(NumericAnswerStatus.INVALID_NUMBER, question.evaluate("1e309 pm").status)
        assertEquals(NumericAnswerStatus.INVALID_NUMBER, question.evaluate("100,14.2 pm").status)
        assertEquals(NumericAnswerStatus.INVALID_NUMBER, question.evaluate("100.14,2 pm").status)
        assertEquals(NumericAnswerStatus.INVALID_NUMBER, question.evaluate("1e-10.2 m").status)
        assertEquals(NumericAnswerStatus.OUT_OF_RANGE, question.evaluate("−100 pm").status)
        assertEquals(NumericAnswerStatus.OUT_OF_RANGE, question.evaluate("0 m").status)
        assertEquals(NumericAnswerStatus.OUT_OF_RANGE, question.evaluate("1e300 m").status)
    }
}
