package materiewelle.pages

import materiewelle.ui.MathText as Text
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.*
import androidx.compose.runtime.Composable
import materiewelle.core.ExperimentMode
import materiewelle.core.Experiment
import org.jetbrains.compose.web.dom.*
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round

/** Fixed German decimal output for the finite, bounded quantities displayed in this lab. */
fun number(value: Double, digits: Int = 2): String {
    require(digits in 0..9)
    if (!value.isFinite()) return "—"
    val scale = 10.0.pow(digits).toLong()
    // Imported valid physical quantities can be very large; retain a scientific representation.
    if (abs(value) > Long.MAX_VALUE.toDouble() / scale) return value.toString().replace('.', ',')
    val rounded = round(abs(value) * scale).toLong()
    val sign = if (value < 0 && rounded != 0L) "−" else ""
    val integral = (rounded / scale).toString()
    return sign + integral + if (digits == 0) "" else "," + (rounded % scale).toString().padStart(digits, '0')
}

@Composable
fun Formula(text: String) {
    Div(attrs = { styles(LabClass.FORMULA) }) { materiewelle.ui.InlineFormula(text) }
}

@Composable
fun ModeSwitch(experiment: Experiment, mode: ExperimentMode, onMode: (ExperimentMode) -> Unit) {
    Div(attrs = { styles(LabClass.MODE_SWITCH); role(AriaRole.GROUP); ariaLabel("Darstellungsweise") }) {
        listOf(ExperimentMode.LAB to "Auswertungslabor", ExperimentMode.GUIDED to "Schritt für Schritt").forEach { (choice, label) ->
            Button(attrs = {
                elementId(ElementId.Mode(experiment, choice))
                if (mode == choice) styles(LabClass.CHOSEN)
                ariaPressed((mode == choice))
                onClick { onMode(choice) }
            }) { Text(label) }
        }
    }
}

@Composable
fun DerivationStep(number: String, title: String, explanation: String, formula: String) {
    Div(attrs = { styles(LabClass.DERIVATION_STEP) }) {
        Span(attrs = { styles(LabClass.STEP_NUMBER) }) { Text(number) }
        Div { H2 { Text(title) }; P { Text(explanation) }; Formula(formula) }
    }
}
