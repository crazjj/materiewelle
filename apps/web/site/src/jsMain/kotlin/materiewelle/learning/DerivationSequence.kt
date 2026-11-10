package materiewelle.learning

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import materiewelle.ui.FormulaId
import materiewelle.ui.InlineFormula
import materiewelle.ui.MathText as Text
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.dom.*

enum class DerivationStage {
    ENERGY,
    ENERGY_BALANCE,
    MOMENTUM,
    WAVELENGTH,
    BRAGG_REFERENCE,
    BRAGG_PATH,
    BRAGG_SUM,
    SURFACE,
}

data class DerivationEntry(
    val id: DerivationStage,
    val title: String,
    val explanation: String,
    val formulas: List<FormulaId>,
    val note: String? = null,
)

/** A prefix grows downwards: an advancing stage never replaces its predecessor. */
@Composable
fun DerivationSequence(
    entries: List<DerivationEntry>,
    revealedThrough: Int,
    reading: DerivationReading? = null,
    activeStep: Int = revealedThrough,
    diagram: @Composable (Int) -> Unit = {},
) {
    require(
        entries.isNotEmpty() &&
            revealedThrough in entries.indices &&
            activeStep in 0..revealedThrough
    )
    Div(attrs = { styles(LabClass.DERIVATION_TRACK) }) {
        P(attrs = { styles(LabClass.GRAPH_CAPTION) }) {
            Text(
                "Farbmarkierungen: Energie (Ocker), Impuls (Violett), Wellenlänge (Cyan). Die Formelzeichen erklären dieselbe Zuordnung ohne Farbe."
            )
        }
        entries.take(revealedThrough + 1).forEachIndexed { index, entry ->
            key(entry.id) {
                if (index > 0)
                    Div(
                        attrs = {
                            styles(LabClass.DERIVATION_CONNECTOR)
                            ariaHidden(true)
                        }
                    ) {
                        Span { Text("↓") }
                    }
                Section(
                    attrs = {
                        styles(
                            LabClass.DERIVATION_CARD,
                            LabClass.EQUATION_STEP,
                            if (index == activeStep) LabClass.CURRENT else LabClass.COMPLETE,
                        )
                        reading?.let { elementId(DerivationReadingCardId(it.scope, index)) }
                        data(LabData.EQUATION, index)
                        ariaLabel("Herleitungsschritt ${index + 1}: ${entry.title}")
                    }
                ) {
                    Span(attrs = { styles(LabClass.STEP_NUMBER) }) {
                        Text((index + 1).toString().padStart(2, '0'))
                    }
                    Div(attrs = { styles(LabClass.DERIVATION_CONTENT) }) {
                        H3 { Text(entry.title) }
                        P { Text(entry.explanation) }
                        diagram(index)
                        entry.formulas.forEach { formula ->
                            Div(attrs = { styles(LabClass.FORMULA) }) { InlineFormula(formula) }
                        }
                        entry.note?.let {
                            P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) { Text(it) }
                        }
                    }
                }
            }
        }
    }
}

val voltageDerivation =
    listOf(
        DerivationEntry(
            DerivationStage.ENERGY,
            "Elektrische Energie gewinnen",
            "Die Beschleunigungsspannung liefert kinetische Energie. Das Elektron startet näherungsweise aus Ruhe; U ist der positive Beschleunigungsbetrag.",
            listOf(FormulaId.ENERGY_GAIN),
            "Energie · positiv definierter Spannungsbetrag, keine relevanten Energieverluste.",
        ),
        DerivationEntry(
            DerivationStage.ENERGY_BALANCE,
            "Energie in Impuls übersetzen",
            "Im nichtrelativistischen Modell gilt E_kin = p²/(2mₑ). Setze die Energie aus dem ersten Schritt ein. Die Energie bleibt dieselbe; wir beschreiben sie mit einer anderen Größe.",
            listOf(FormulaId.KINETIC_BALANCE),
            "Impuls · der quadratische Zusammenhang ist die Brücke zur Wurzel.",
        ),
        DerivationEntry(
            DerivationStage.MOMENTUM,
            "Den positiven Impulsbetrag bestimmen",
            "Löse nach p auf. Der Impuls wächst mit der Quadratwurzel der Spannung und nicht linear mit ihr.",
            listOf(FormulaId.MOMENTUM),
        ),
        DerivationEntry(
            DerivationStage.WAVELENGTH,
            "Die Materiewellenhypothese anschließen",
            "Setze den Impuls in de Broglies Beziehung λ = h/p ein. Jetzt wird aus der Energiebilanz eine quantitative Wellenlängenvorhersage.",
            listOf(FormulaId.DE_BROGLIE, FormulaId.WAVELENGTH_VOLTAGE),
            "Energieerhaltung allein beweist die neue Hypothese nicht.",
        ),
    )
