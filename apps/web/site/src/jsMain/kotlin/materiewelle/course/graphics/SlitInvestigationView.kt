package materiewelle.course.graphics

import androidx.compose.runtime.*
import materiewelle.core.FiniteSlitModel
import materiewelle.core.SlitConfiguration
import materiewelle.core.course.SlitModelComparison
import materiewelle.pages.number
import materiewelle.ui.MathText
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*
import kotlin.math.abs

@Composable
fun SlitInvestigationView(model: FiniteSlitModel, saved: SlitModelComparison?, view: SlitExplorationUiState, readOnly: Boolean = false) {
    LaunchedEffect(model, saved, view.task, view.configuration) { view.comparisonRequested = false }
    Details(attrs = { elementId(SlitLabId.INVESTIGATION); slitLabStyles(SlitLabClass.TASK) }) {
        Summary { Text("Mit einer Vorhersage beginnen") }
        P { Text("Notiere deine Vermutung. Nutze „Einstellung merken“ für den Ausgangsfall A und verändere nur die gewählte Größe. Erkläre anschließend den Unterschied.") }
        Div(attrs = { styles(LabClass.WORKSHOP_ACTIONS); role(AriaRole.GROUP); ariaLabel("Untersuchungsfrage auswählen") }) {
            SlitInvestigation.entries.forEach { task ->
                Button(attrs = {
                    elementId(if (task == SlitInvestigation.SPACING) SlitLabId.SPACING else SlitLabId.WIDTH)
                    styles(LabClass.SECONDARY); ariaPressed(view.task == task)
                    onClick { view.task = task }
                }) { Text(if (task == SlitInvestigation.SPACING) "Mittenabstand untersuchen" else "Spaltbreite untersuchen") }
            }
        }
        P { Strong { Text(if (view.task == SlitInvestigation.SPACING)
            "Wie ändern sich Streifenabstand und Hülle, wenn du nur den Mittenabstand vergrößerst?"
            else "Wie ändern sich Hülle und Streifenabstand, wenn du nur die Breite jedes Spalts vergrößerst?") } }
        P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
            Text(if (readOnly) "Diese Notizen werden im anderen Tab bearbeitet. Die Modellansichten kannst du hier weiter untersuchen."
                else "Deine Vorhersage und Erklärung kannst du anschließend mit der KI-Hilfe prüfen.")
        }
        val own = view.notes()
        Div(attrs = { slitLabStyles(SlitLabClass.FIELDS) }) {
            Div {
                FieldLabel(SlitLabId.PREDICTION) { Text("Meine Vorhersage") }
                TextArea(attrs = { elementId(SlitLabId.PREDICTION); protectPersonalWork(readOnly); textRows(3); maxLength(SlitExplorationUiState.TEXT_LIMIT); value(own.prediction); onInput { view.predict(it.value) } })
            }
            Div {
                FieldLabel(SlitLabId.REASON) { Text("Was hat sich verändert? Wie erkläre ich das?") }
                TextArea(attrs = { elementId(SlitLabId.REASON); protectPersonalWork(readOnly); textRows(3); maxLength(SlitExplorationUiState.TEXT_LIMIT); value(own.reason); onInput { view.explain(it.value) } })
            }
        }
        Button(attrs = { elementId(SlitLabId.COMPARE); styles(LabClass.SECONDARY); onClick { view.comparisonRequested = true } }) { Text("Parameteränderung prüfen") }
        if (view.comparisonRequested) Div(attrs = { elementId(SlitLabId.RESULT); slitLabStyles(SlitLabClass.FEEDBACK); role(AriaRole.STATUS) }) {
            when {
                saved == null -> P { Text("Merke zuerst einen Ausgangsfall mit „Einstellung merken“ und ändere danach genau eine Größe. Deine Notizen bleiben erhalten.") }
                view.configuration != SlitConfiguration.BOTH_COHERENT -> P { Text("Wähle für diesen Parametervergleich „Beide kohärent“. Die Öffnungsvarianten kannst du anschließend gesondert untersuchen.") }
                abs(saved.voltageV - model.voltage.volts) > 1e-8 || abs(saved.distanceM - model.distance.meters) > 1e-8 -> P { Text("A verwendet eine andere Spannung oder Entfernung. Merke zuerst einen Ausgangsfall mit denselben Rahmenbedingungen; erst dann lässt sich die Wirkung einer einzelnen Spaltgröße zuordnen.") }
                else -> {
                    val spacingChanged = abs(model.slitSpacing.micrometers - saved.spacingUm) > 1e-8
                    val widthChanged = abs(model.slitWidth.micrometers - saved.widthUm) > 1e-8
                    when {
                        !spacingChanged && !widthChanged -> P { Text("A und B haben noch dieselbe Geometrie. Verändere die gewählte Größe; die gemerkte Einstellung bleibt für den Vergleich erhalten.") }
                        spacingChanged && widthChanged -> P { Text("Du hast beide Größen verändert. Damit lässt sich die Wirkung nicht eindeutig einer davon zuordnen. Stelle eine Größe wieder auf A zurück und vergleiche erneut.") }
                        (view.task == SlitInvestigation.SPACING) != spacingChanged -> P { Text("Du hast die andere Größe verändert. Wechsle zur passenden Frage oder untersuche die zuerst gewählte Größe. Deine beiden Notizen bleiben getrennt erhalten.") }
                        else -> {
                            P {
                                Text(when {
                                    spacingChanged && model.slitSpacing.micrometers > saved.spacingUm -> "Die feinen Interferenzstreifen rücken näher zusammen; die breite Hülle bleibt unverändert. Du hast nur den Mittenabstand vergrößert."
                                    spacingChanged -> "Die feinen Interferenzstreifen liegen weiter auseinander; die breite Hülle bleibt unverändert. Du hast nur den Mittenabstand verkleinert."
                                    model.slitWidth.micrometers > saved.widthUm -> "Die breite Hülle wird schmaler; der rechnerische Abstand der feinen Interferenzstreifen bleibt gleich. Du hast nur die Spaltbreite vergrößert."
                                    else -> "Die breite Hülle wird breiter; der rechnerische Abstand der feinen Interferenzstreifen bleibt gleich. Du hast nur die Spaltbreite verkleinert."
                                })
                            }
                            P {
                                Text("Verhältnis B zu A: Interferenzmaßstab ")
                                MathText(number(saved.spacingUm / model.slitSpacing.micrometers, 3))
                                Text(". Kleiner als eins bedeutet rechnerisch engere, größer als eins weiter auseinanderliegende Interferenzstreifen.")
                            }
                            P { Text("Die Hülle kann Maxima des gesamten Profils verschieben oder unterdrücken. Deine freie Erklärung wird nicht automatisch bewertet: Vergleiche sie selbst mit Kurve und Hülle.") }
                        }
                    }
                }
            }
        }
    }
}

fun SlitConfiguration.label(): String = when (this) {
    SlitConfiguration.BOTH_COHERENT -> "Beide kohärent"
    SlitConfiguration.LEFT_ONLY -> "Nur linker Spalt"
    SlitConfiguration.RIGHT_ONLY -> "Nur rechter Spalt"
    SlitConfiguration.BOTH_INCOHERENT -> "Beide ohne Interferenz"
}
