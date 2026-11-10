package materiewelle.course

import androidx.compose.runtime.Composable
import materiewelle.core.*
import materiewelle.core.course.StationId
import materiewelle.learning.CompactTutorUiState
import materiewelle.learning.TutorHelpEntry
import materiewelle.learning.TutorHelpPoint
import materiewelle.pages.number
import materiewelle.state.LabSession
import materiewelle.ui.FormulaId
import materiewelle.ui.InlineFormula
import materiewelle.ui.MathText
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*

/** Source-case calculations are independent of the editable model variation below. */
@Composable
internal fun DgSourceCalculation(session: LabSession, data: ScientificData, view: DgLessonView, tutor: CompactTutorUiState) {
    val source = data.points.first()
    val predicted = Physics.electronWavelength(Voltage(source.voltageV)).nanometers
    val geometric = Physics.surfaceDiffraction(data.spacing, SurfaceNormalAngle(source.angleDegrees)).nanometers
    val difference = Physics.signedRelativeDifferencePercent(geometric, predicted)
    val work = session.snapshot.stationWork(StationId.S11_DG_EVALUATION)
    P { Text("Berechne beide Wellenlängen für den in der Sekundäranalyse D2 berichteten Fall. Die Elektronenenergie von "); MathText("${number(source.voltageV, 0)} eV"); Text(" entspricht bei Beschleunigung aus Ruhe einer Spannung von "); MathText("${number(source.voltageV, 0)} V"); Text(". Notiere deine Ergebnisse mit Einheit und vergleiche sie.") }
    P(attrs = { articleStyles(ArticleClass.SMALL) }) { Text("Vergleiche deinen Rechenweg mit den Formeln und den angezeigten Ergebnissen. Die Website bewertet deinen eingegebenen Rechenweg nicht automatisch.") }
    Div(attrs = { dgLessonStyles(DgLessonClass.CALCULATIONS) }) {
        Section(attrs = { dgLessonStyles(DgLessonClass.CALCULATION) }) {
            H3 { Text("1 · Vorhersage aus der Spannung") }
            P(attrs = { dgLessonStyles(DgLessonClass.CALCULATION_INTRO) }) { Text("Die Spannung liefert den Energiegewinn. Der Detektorwinkel geht in diese Rechnung nicht ein.") }
            Div(attrs = { articleStyles(ArticleClass.FORMULA); dgLessonStyles(DgLessonClass.CALCULATION_FORMULA) }) { InlineFormula(FormulaId.WAVELENGTH_VOLTAGE) }
            Div(attrs = { dgLessonStyles(DgLessonClass.CALCULATION_ENTRY) }) {
                FieldLabel(DgLessonId.PREDICTION_INPUT) { Text("Deine Rechnung oder Vorhersage mit Einheit") }
                TextArea(attrs = { elementId(DgLessonId.PREDICTION_INPUT); protectPersonalWork(session.isReadOnly); textRows(2); maxLength(1000); value(work.prediction)
                    onInput { event -> session.editWork(StationId.S11_DG_EVALUATION) { it.copy(prediction = event.value.take(1000)) } }
                })
            }
            Button(attrs = { elementId(DgLessonId.SHOW_PREDICTION); styles(LabClass.SECONDARY); ariaExpanded(view.showPrediction); controls(DgLessonId.PREDICTION_RESULT)
                onClick { view.showPrediction = !view.showPrediction; if (view.showPrediction) session.editWork(StationId.S11_DG_EVALUATION) { it.copy(solutionViewed = true) } }
            }) { Text(if (view.showPrediction) "Ergebnis ausblenden" else "Ergebnis anzeigen") }
            if (view.showPrediction) Div(attrs = { elementId(DgLessonId.PREDICTION_RESULT); dgLessonStyles(DgLessonClass.RESULT); data(LabData.VALUE_NM, predicted) }) {
                MathText("≈ ${number(predicted, 3)} nm")
                P(attrs = { articleStyles(ArticleClass.SMALL) }) { Text("Nichtrelativistische Vorhersage aus der Spannung.") }
            }
        }
        Section(attrs = { dgLessonStyles(DgLessonClass.CALCULATION) }) {
            H3 { Text("2 · Aus der Beugungsgeometrie") }
            P(attrs = { dgLessonStyles(DgLessonClass.CALCULATION_INTRO) }) {
                Text("Wir verwenden "); MathText("φ ≈ ${number(source.angleDegrees, 0)}°"); Text(" zur Normalen und ")
                MathText("D ≈ ${number(data.spacing.nanometers, 4)} nm"); Text(" aus der Nickelstruktur. Es gilt die erste Ordnung.")
            }
            Div(attrs = { articleStyles(ArticleClass.FORMULA); dgLessonStyles(DgLessonClass.CALCULATION_FORMULA) }) { InlineFormula(FormulaId.SURFACE) }
            Div(attrs = { dgLessonStyles(DgLessonClass.CALCULATION_ENTRY) }) {
                FieldLabel(DgLessonId.GEOMETRY_INPUT) { Text("Dein Ergebnis mit Einheit") }
                Input(InputType.Text, attrs = { elementId(DgLessonId.GEOMETRY_INPUT); protectPersonalWork(session.isReadOnly); maxLength(120); value(work.numericAnswer)
                    onInput { event -> session.editWork(StationId.S11_DG_EVALUATION) { it.copy(numericAnswer = event.value.take(120)) } }
                })
            }
            Button(attrs = { elementId(DgLessonId.SHOW_GEOMETRY); styles(LabClass.SECONDARY); ariaExpanded(view.showGeometry); controls(DgLessonId.GEOMETRY_RESULT)
                onClick { view.showGeometry = !view.showGeometry; if (view.showGeometry) session.editWork(StationId.S11_DG_EVALUATION) { it.copy(solutionViewed = true) } }
            }) { Text(if (view.showGeometry) "Ergebnis ausblenden" else "Ergebnis anzeigen") }
            if (view.showGeometry) Div(attrs = { elementId(DgLessonId.GEOMETRY_RESULT); dgLessonStyles(DgLessonClass.RESULT); data(LabData.VALUE_NM, geometric) }) {
                MathText("≈ ${number(geometric, 3)} nm")
                P(attrs = { articleStyles(ArticleClass.SMALL) }) { Text("Modellwert aus der Detektorrichtung und dem Reihenabstand.") }
            }
        }
    }
    if (view.showPrediction && view.showGeometry) Div(attrs = { elementId(DgLessonId.SOURCE_COMPARISON); dgLessonStyles(DgLessonClass.NOTE); data(LabData.VALUE_PERCENT, difference) }) {
        H3 { Text("Die Ergebnisse zusammenführen") }
        P { Text("Die rechnerische Differenz des geometrischen Werts zur Vorhersage beträgt "); MathText("${number(difference, 1)} %"); Text(". Dafür ziehen wir die Vorhersage vom Geometriewert ab, teilen durch die Vorhersage und geben das Ergebnis in Prozent an.") }
        P { Text("Der Reihenabstand stammt aus der Nickelstruktur und wurde nicht an die Vorhersage angepasst. Ohne die historischen Messunsicherheiten lässt sich daraus nicht beurteilen, ob die Werte innerhalb der damaligen Messgenauigkeit übereinstimmen.") }
    }
    TutorHelpEntry(TutorHelpPoint.DG_COMPARISON, session, tutor)
    Details(attrs = { elementId(DgLessonId.VARIATION) }) {
        Summary { Text("Danach ausprobieren: Modellwerte verändern") }
        P { Text("Diese freie Modellrechnung ist vom festen Quellenfall oben getrennt. Veränderte Zahlen sind keine zusätzlichen historischen Messpunkte.") }
        DgModelVariation(session, data)
        TutorHelpEntry(TutorHelpPoint.DG_MODEL, session, tutor)
    }
}

@Composable
internal fun DgConclusion(session: LabSession, data: ScientificData) {
    val source = data.points.first()
    val predicted = Physics.electronWavelength(Voltage(source.voltageV)).nanometers
    val geometric = Physics.surfaceDiffraction(data.spacing, SurfaceNormalAngle(source.angleDegrees)).nanometers
    P { Text("Die aus dem Beugungsbefund berechnete Wellenlänge liegt nahe bei de Broglies Vorhersage aus der Spannung. Das stützt die Wellenbeschreibung des Elektrons im gewählten Modell.") }
    P { Text("Die Deutung setzt die angegebene Kristallstruktur und Streugeometrie voraus und stützt sich auf den in D2 berichteten Befund. Der Winkel ist nur ungefähr angegeben; historische Messunsicherheiten liegen hier nicht vor. Deshalb lässt sich nicht beurteilen, ob die beiden Wellenlängen innerhalb der damaligen Messgenauigkeit übereinstimmen.") }
    Details {
        Summary { Text("Den festen Zahlenvergleich noch einmal ansehen") }
        P { Text("Aus der Spannung: "); MathText("≈ ${number(predicted, 3)} nm"); Text(". Aus der Geometrie: "); MathText("≈ ${number(geometric, 3)} nm"); Text(". Die zusätzlichen Stellen der Strukturrechnung sind keine historische Messgenauigkeit.") }
    }
    Div(attrs = { dgLessonStyles(DgLessonClass.NOTE) }) {
        FieldLabel(DgLessonId.CONCLUSION_NOTE) { Text("Erkläre in eigenen Worten: Was vergleichen wir, und weshalb stützt das de Broglies Idee?") }
        TextArea(attrs = { elementId(DgLessonId.CONCLUSION_NOTE); protectPersonalWork(session.isReadOnly); textRows(3); maxLength(4000)
            value(session.snapshot.stationWork(StationId.S11_DG_EVALUATION).note)
            onInput { event -> session.editWork(StationId.S11_DG_EVALUATION) { it.copy(note = event.value.take(4000)) } }
        })
    }
    P { Text("Beim Kristallversuch liefert die Anordnung der Atome die periodische Struktur. Bei Jönsson übernehmen künstlich hergestellte Spalte diese Aufgabe. Im nächsten Kapitel misst du Streifenabstände in einer historischen Aufnahme.") }
}
