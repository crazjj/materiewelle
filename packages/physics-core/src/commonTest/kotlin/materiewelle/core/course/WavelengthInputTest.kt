package materiewelle.core.course

import materiewelle.core.Voltage
import kotlin.test.*

class WavelengthInputTest {
    @Test fun equivalentSchoolNotationsHaveOneMeaning() {
        val question = NumericWavelengthQuestion(Voltage(150.0))
        for (text in listOf("1,0·10^-10 m", "1.0 × 10 ^ −10 m", "1*10^-10 m", "1e-10 m", "0,1 nm", "100 pm",
            "1,0 × 10⁻¹⁰ m", "1·10⁺² pm", "100 × 10⁰ pm", "1,0\u00a0×\u202f10⁻¹⁰\u00a0m")) {
            val parsed = assertIs<WavelengthInput.Ready>(WavelengthInputs.parse(text), text)
            assertEquals(100.0, parsed.length.picometers, 1e-10, text)
            assertEquals(NumericAnswerStatus.CORRECT, question.evaluate(text).status, text)
        }
    }

    @Test fun previewConvertsUnitsWithoutPaddingZerosOrHidingSmallPositiveValues() {
        for (text in listOf("100 pm", "0,1 nm", "1e-10 m")) {
            val parsed = assertIs<WavelengthInput.Ready>(WavelengthInputs.parse(text))
            assertEquals("100", WavelengthInputs.previewPicometers(parsed.length.picometers), text)
        }
        val detailed = assertIs<WavelengthInput.Ready>(WavelengthInputs.parse("100,13726 pm"))
        assertEquals("100,137", WavelengthInputs.previewPicometers(detailed.length.picometers))
        val small = assertIs<WavelengthInput.Ready>(WavelengthInputs.parse("0,0001 pm"))
        val shownSmall = WavelengthInputs.previewPicometers(small.length.picometers)
        assertEquals(.0001, shownSmall.replace(',', '.').toDouble(), 1e-15)
    }

    @Test fun roundedPreviewDoesNotChangeCheckingAtTheToleranceBoundary() {
        val question = NumericWavelengthQuestion(Voltage(150.0))
        val outside = assertIs<WavelengthInput.Ready>(WavelengthInputs.parse("99,6372 pm"))
        val inside = assertIs<WavelengthInput.Ready>(WavelengthInputs.parse("99,6373 pm"))
        assertEquals("99,637", WavelengthInputs.previewPicometers(outside.length.picometers))
        assertEquals("99,637", WavelengthInputs.previewPicometers(inside.length.picometers))
        assertEquals(NumericAnswerStatus.INCORRECT, question.evaluate("99,6372 pm").status)
        assertEquals(NumericAnswerStatus.CORRECT, question.evaluate("99,6373 pm").status)
    }

    @Test fun feedbackStatesTheActualConfiguredRoundingAllowance() {
        val question = NumericWavelengthQuestion(Voltage(150.0), tolerancePm = .25)
        val result = question.evaluate("100 pm")
        assertEquals(NumericAnswerStatus.CORRECT, result.status)
        assertTrue(result.feedback.contains("±0,25 pm"))
        assertTrue(result.feedback.contains("keine Messunsicherheit"))
    }

    @Test fun superscriptsAreAcceptedOnlyAsCompleteIntegerExponents() {
        for (text in listOf(
            "1 × 10⁻ m", "1 × 10⁻¹⁰.² m", "1 × 10⁻¹0 m", "1 × 10⁻1⁰ m",
            "1 × 10¹0 pm", "1 × 10⁺⁻² pm", "1 × 10¹ ⁰ pm", "¹ × 10⁻¹⁰ m",
            "1 × 10⁻¹⁰ m²", "1 × 10⁻¹⁰ m⁻¹", "1 × 10⁻¹⁰ + 2 m",
            "1 × 10⁻⁹⁹⁹ m", "1 × 10³⁰⁹ pm",
        )) assertIs<WavelengthInput.Rejected>(WavelengthInputs.parse(text), text)
        assertEquals(WavelengthInputProblem.MISSING_UNIT,
            assertIs<WavelengthInput.Rejected>(WavelengthInputs.parse("2,5 × 10⁻⁹")).problem)
        assertEquals(WavelengthInputProblem.UNIT,
            assertIs<WavelengthInput.Rejected>(WavelengthInputs.parse("2,5 × 10⁻⁹ M")).problem)
    }

    @Test fun grammarDoesNotExecuteOrPartiallyReadExpressions() {
        for (text in listOf("1+2 pm", "sqrt(2) m", "1·10^- m", "1·10^2.5 pm", "1e-10.2 m", "1 m<script>", "9".repeat(121)))
            assertIs<WavelengthInput.Rejected>(WavelengthInputs.parse(text), text)
        for (text in listOf("0 m", "-1 nm", "1e-999 m", "1e300 m", "1e309 pm", "NaN m"))
            assertIs<WavelengthInput.Rejected>(WavelengthInputs.parse(text), text)
        assertEquals(WavelengthInputProblem.MISSING_UNIT, assertIs<WavelengthInput.Rejected>(WavelengthInputs.parse("2·10^-9")).problem)
        assertEquals(WavelengthInputProblem.UNIT, assertIs<WavelengthInput.Rejected>(WavelengthInputs.parse("2 PM")).problem)
    }

    @Test fun neutralSyntaxExamplesCannotPassAnyOfferedTask() {
        val questions = listOf(NumericWavelengthQuestion(Voltage(150.0)), NumericWavelengthQuestion(Voltage(600.0))) +
            PracticeCaseId.entries.map { it.question }
        for (example in WavelengthInputs.syntaxExamples) {
            assertIs<WavelengthInput.Ready>(WavelengthInputs.parse(example))
            for (question in questions) assertEquals(NumericAnswerStatus.INCORRECT, question.evaluate(example).status, example)
        }
        val error = NumericWavelengthQuestion(Voltage(150.0)).evaluate("1,00·10^- m")
        assertTrue(error.feedback.contains(WavelengthInputs.syntaxExamples.first()))
    }
}
