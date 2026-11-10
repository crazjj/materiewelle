package materiewelle.core.course

import materiewelle.core.Length
import materiewelle.core.Physics
import kotlin.math.pow

/** A bounded school-notation reader, never an expression evaluator. */
sealed interface WavelengthInput {
    data class Ready(val length: Length) : WavelengthInput
    data class Rejected(val problem: WavelengthInputProblem) : WavelengthInput
}

enum class WavelengthInputProblem(val status: NumericAnswerStatus, val feedback: String) {
    EMPTY(NumericAnswerStatus.EMPTY, "Gib deinen Zahlenwert zusammen mit einer Einheit ein: pm, nm oder m."),
    SYNTAX(NumericAnswerStatus.INVALID_NUMBER, "Die Schreibweise ist noch nicht lesbar. Komma/Punkt, e-Notation oder eine Zehnerpotenz mit ganzzahligem Exponenten sind möglich; Formatbeispiel: 2,5·10^-9 m. Das Beispiel ist keine Aufgabenlösung."),
    NOT_FINITE(NumericAnswerStatus.INVALID_NUMBER, "Dieser Zahlenwert lässt sich nicht berechnen. Prüfe die Zahl und die Zehnerpotenz."),
    MISSING_UNIT(NumericAnswerStatus.MISSING_UNIT, "Die Einheit fehlt. Für die Wellenlänge sind hier pm, nm oder m zulässig."),
    UNIT(NumericAnswerStatus.UNSUPPORTED_UNIT, "Verwende die Längeneinheit pm, nm oder m. Groß-/Kleinschreibung gehört zur Einheit."),
    RANGE(NumericAnswerStatus.OUT_OF_RANGE, "Gib eine positive Wellenlänge ein. Prüfe Vorzeichen und Zehnerpotenz; der Wert muss im darstellbaren Zahlenbereich liegen."),
}

object WavelengthInputs {
    const val MAX_LENGTH = 120
    /** Deliberately far from every offered numerical task, regression checked in SI units. */
    val syntaxExamples: List<String> = listOf("2,5·10^-9 m", "2,5 × 10⁻⁹ m", "2.5e-9 m", "2,5 nm", "2500 pm")
    private const val SUPERSCRIPT_DIGITS = "⁰¹²³⁴⁵⁶⁷⁸⁹"
    private val form = Regex("^([+-]?(?:[0-9]+(?:[.,][0-9]*)?|[.,][0-9]+))(?:(?:[eE]([+-]?[0-9]+))|(?:\\s*[·×*]\\s*10\\s*(?:\\^\\s*([+-]?[0-9]+)|([⁺⁻]?[$SUPERSCRIPT_DIGITS]+))))?\\s*([A-Za-zµμÅÅ]+)?$")

    /** Display only: checking and storage always retain the unrounded parsed value. */
    fun previewPicometers(value: Double): String {
        require(value.isFinite() && value > 0.0)
        return if (value in .001..1_000_000.0)
            Physics.decimal(value, 3).trimEnd('0').trimEnd(',')
        else value.toString().replace('.', ',').replace('E', 'e')
    }

    fun parse(raw: String): WavelengthInput {
        if (raw.length > MAX_LENGTH) return WavelengthInput.Rejected(WavelengthInputProblem.SYNTAX)
        val text = raw.trim().replace('−', '-').replace('\u00a0', ' ').replace('\u202f', ' ')
        if (text.isEmpty()) return WavelengthInput.Rejected(WavelengthInputProblem.EMPTY)
        val match = form.matchEntire(text) ?: return WavelengthInput.Rejected(WavelengthInputProblem.SYNTAX)
        val coefficient = match.groupValues[1].replace(',', '.').toDoubleOrNull()
            ?: return WavelengthInput.Rejected(WavelengthInputProblem.NOT_FINITE)
        // Only the already matched exponent is normalized. Superscripts in a
        // coefficient or a unit, and mixed baseline/raised digits, stay invalid.
        val exponentText = match.groupValues[2].ifEmpty { match.groupValues[3] }.ifEmpty {
            match.groupValues[4].map { character ->
                when (character) {
                    '⁺' -> '+'
                    '⁻' -> '-'
                    else -> SUPERSCRIPT_DIGITS.indexOf(character).digitToChar()
                }
            }.joinToString("")
        }
        val exponent = if (exponentText.isEmpty()) 0 else exponentText.toIntOrNull()
            ?: return WavelengthInput.Rejected(WavelengthInputProblem.NOT_FINITE)
        val value = coefficient * 10.0.pow(exponent.toDouble())
        if (!value.isFinite()) return WavelengthInput.Rejected(WavelengthInputProblem.NOT_FINITE)
        val unit = match.groupValues[5]
        if (unit.isEmpty()) return WavelengthInput.Rejected(WavelengthInputProblem.MISSING_UNIT)
        val pmFactor = when (unit) {
            "pm" -> 1.0
            "nm" -> 1e3
            "m" -> 1e12
            else -> return WavelengthInput.Rejected(WavelengthInputProblem.UNIT)
        }
        val pm = value * pmFactor
        val meters = pm * 1e-12
        if (!pm.isFinite() || pm <= 0.0 || meters <= 0.0)
            return WavelengthInput.Rejected(WavelengthInputProblem.RANGE)
        return WavelengthInput.Ready(Length(meters))
    }
}
