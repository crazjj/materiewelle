package materiewelle.course

import androidx.compose.runtime.*
import kotlin.math.abs
import materiewelle.core.*
import materiewelle.core.course.*
import materiewelle.pages.Formula
import materiewelle.state.LabSession
import materiewelle.ui.MathText as Text
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*

/** Parsing is independent of checking; this never announces correct/incorrect. */
@Composable
internal fun WavelengthInputPreview(raw: String) {
    P(attrs = { elementId(LearningHelpId.INPUT_PREVIEW); styles(LabClass.SMALL, LabClass.MUTED) }) {
        when (val input = WavelengthInputs.parse(raw)) {
            is WavelengthInput.Ready -> {
                val shown = WavelengthInputs.previewPicometers(input.length.picometers)
                Text("Deine Eingabe in Pikometern: $shown pm · gerundete Vorschau, noch keine Bewertung.")
            }
            is WavelengthInput.Rejected -> Text(if (raw.isBlank())
                "Komma/Punkt, e-Notation und Zehnerpotenzen sind möglich. Die Eingabevorschau bewertet deinen Rechenweg nicht."
                else "Noch keine gültige Länge gelesen. Du kannst den Text weiter bearbeiten und anschließend prüfen.")
        }
    }
}

@Composable
internal fun NumericStepHelp(session: LabSession) {
    var step by remember { mutableStateOf<NumericHelpStep?>(null) }
    Section(attrs = { elementId(LearningHelpId.NUMERIC_HELP); styles(LabClass.PANEL) }) {
        H3 { Text("Nur den nächsten Schritt klären") }
        P { Text("Zu welchem Schritt brauchst du einen Hinweis?") }
        Div(attrs = { styles(LabClass.COURSE_ACTIONS) }) {
            NumericHelpStep.entries.forEach { choice ->
                Button(attrs = {
                    styles(LabClass.SECONDARY)
                    ariaPressed(step == choice)
                    onClick {
                        step = if (step == choice) null else choice
                        session.editWork(StationId.S06_CALCULATION) { it.copy(hintsOpened = (it.hintsOpened + 1).coerceAtMost(10)) }
                    }
                }) { Text(choice.label) }
            }
        }
        when (step) {
            NumericHelpStep.ENERGY -> {
                Formula("E_kin = eU")
                P { Text("Start aus Ruhe: Die elektrische Energie wird kinetische Energie. e ist der positive Ladungsbetrag. C mal V ergibt J. Schreibe zuerst diesen Energieansatz mit deinen SI-Werten auf.") }
            }
            NumericHelpStep.ROOT -> {
                Formula("E_kin = p²/(2mₑ)")
                Formula("p = √(2mₑeU)")
                P { Text("Erst mit 2mₑ multiplizieren, dann die positive Wurzel ziehen: Gesucht ist der Impulsbetrag. Eine vervierfachte Energie verdoppelt den Impuls; sie vervierfacht ihn nicht.") }
            }
            NumericHelpStep.UNIT -> {
                P { Text("Nach h/p steht die Länge zunächst in m. Für pm multiplizierst du mit 10¹². Ein anderes Beispiel: 2,5·10^-9 m entsprechen 2,5 nm und 2500 pm.") }
            }
            null -> Unit
        }
    }
}

@Composable
internal fun WorkedElectronCalculation() {
    val result = remember { ElectronPrediction(Voltage(150.0)) }
    val energy = Physics.decimal(result.kineticEnergyJoules * 1e17, 6)
    val momentum = Physics.decimal(result.momentumKilogramMetersPerSecond * 1e24, 6)
    val wavelength = Physics.decimal(result.wavelength.meters * 1e10, 6)
    Section(attrs = { elementId(LearningHelpId.WORKED_CALCULATION); styles(LabClass.PANEL) }) {
        H3 { Text("Musterrechnung in SI-Einheiten") }
        P { Text("Annahmen: Elektron startet aus Ruhe; positiver Ladungsbetrag; nichtrelativistische Energiebeziehung. Konstanten: h = ${Physics.H} J s, e = ${Physics.E} C, mₑ = ${Physics.ELECTRON_MASS} kg.") }
        Formula("E_kin = eU")
        P { Text("Einsetzen: ${Physics.E} C mal 150 V. E_kin ≈ $energy × 10⁻¹⁷ J.") }
        Formula("p = √(2mₑeU)")
        P { Text("Einsetzen: Wurzel aus 2 mal ${Physics.ELECTRON_MASS} kg mal $energy × 10⁻¹⁷ J. p ≈ $momentum × 10⁻²⁴ kg m/s.") }
        Formula("λ = h/p")
        P { Text("Einsetzen: ${Physics.H} J s geteilt durch $momentum × 10⁻²⁴ kg m/s. λ ≈ $wavelength × 10⁻¹⁰ m ≈ ${Physics.decimal(result.wavelength.picometers, 4)} pm.") }
        P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) { Text("Zwischenwerte sind für die Anzeige gerundet. Die Berechnung verwendet durchgehend ungerundete Kernwerte.") }
        P { Text("Sinnvoll gerundet etwa 100 pm oder 0,100 nm: atomare Größenordnung, keine Aussage über einen Elektronendurchmesser. Rechenstellen sind keine experimentelle Messpräzision.") }
    }
}

@Composable
internal fun ComparisonCompletion() {
    var answer by remember { mutableStateOf("") }
    var checked by remember { mutableStateOf(false) }
    Details(attrs = { elementId(LearningHelpId.COMPARISON_COMPLETION); styles(LabClass.NOTES) }) {
        Summary { Text("Teilbeispiel ergänzen: Von Energie zu Impuls") }
        P { Text("Beim geführten 600-V-Fall ist die Energie gegenüber 150 V viermal so groß. Welcher Faktor folgt für den Impulsbetrag? Das ist eine Übung am erklärten Beispiel, kein neuer Erstversuch.") }
        FieldLabel(LearningHelpId.COMPARISON_COMPLETION_INPUT) { Text("Dein Impulsfaktor · ohne Einheit") }
        Input(InputType.Text, attrs = {
            elementId(LearningHelpId.COMPARISON_COMPLETION_INPUT); value(answer); maxLength(40)
            onInput { answer = it.value.take(40); checked = false }
        })
        Button(attrs = { styles(LabClass.SECONDARY); onClick { checked = true } }) { Text("Teilbeispiel prüfen") }
        if (checked) P(attrs = { role(AriaRole.STATUS) }) {
            Text(if (answer.replace(',', '.').toDoubleOrNull()?.let { it.isFinite() && abs(it - 2.0) < .01 } == true)
                "Der Faktor passt. Begründe die Wurzel, bevor du auf die Wellenlänge schließt."
                else "Verwende die positive Quadratwurzel des Energiefaktors. Probiere den Zwischenschritt erneut; daraus folgt keine Diagnose deines gesamten Rechenwegs.")
        }
        P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) { Text("Diese kurze Teilprobe wird nicht gespeichert. Deine Pflichtantwort bleibt erhalten.") }
    }
}
