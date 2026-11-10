package materiewelle.learning

import androidx.compose.runtime.Composable
import kotlinx.browser.document
import materiewelle.core.course.AppSection
import materiewelle.state.LabSession
import org.jetbrains.compose.web.attributes.*
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.dom.*

/** Authored questions are suggestions, never generated answers or student evidence. */
enum class TutorQuestionExample(val id: CompactTutorExampleId, val question: String) {
    VOLTAGE(CompactTutorExampleId.VOLTAGE, "Warum halbiert sich die Wellenlänge bei vierfacher Spannung?"),
    HYPOTHESIS(CompactTutorExampleId.HYPOTHESIS, "An welcher Stelle kommt de Broglies zusätzliche Hypothese hinzu?"),
    ANGLE(CompactTutorExampleId.ANGLE, "Warum darf ich den Detektorwinkel nicht einfach halbieren?"),
    DIFFERENCE(CompactTutorExampleId.DIFFERENCE, "Was sagt die Abweichung zwischen den beiden Rechenwegen aus?"),
    SCALE(CompactTutorExampleId.SCALE, "Was kann ich ohne Bildmaßstab aus meinen Marken schließen?"),
    MAXIMA(CompactTutorExampleId.MAXIMA, "Wie prüfe ich, ob ich aufeinanderfolgende Maxima gewählt habe?"),
    ARGUMENT(CompactTutorExampleId.ARGUMENT, "Wie verbinde ich Vorhersage und beide Versuche in meinem Fazit?"),
    LIMIT(CompactTutorExampleId.LIMIT, "Welche Grenze sollte ich bei meiner Auswertung nennen?"),
    FIRST_STEP(CompactTutorExampleId.FIRST_STEP, "Wie beginne ich die Rechnung, und welche Einheiten brauche ich?"),
    DETECTOR(CompactTutorExampleId.DETECTOR, "Was misst der Detektor, und warum ist das ein Hinweis auf Wellenverhalten?"),
    SPACING(CompactTutorExampleId.SPACING, "Wie verändert der Spaltabstand das Muster, und warum?"),
    WIDTH(CompactTutorExampleId.WIDTH, "Was ändert die Spaltbreite im Unterschied zum Spaltabstand?"),
}

enum class TutorHelpPoint(
    val section: AppSection,
    val entry: CompactTutorEntryId,
    val returnLabel: String,
    val examples: List<TutorQuestionExample>,
) {
    CALCULATION(AppSection.DE_BROGLIE, CompactTutorEntryId.CALCULATION, "Zurück zur Rechnung",
        listOf(TutorQuestionExample.FIRST_STEP, TutorQuestionExample.HYPOTHESIS)),
    DE_BROGLIE_MODEL(AppSection.DE_BROGLIE, CompactTutorEntryId.DE_BROGLIE_MODEL, "Zurück zum Modell",
        listOf(TutorQuestionExample.VOLTAGE, TutorQuestionExample.HYPOTHESIS)),
    DG_COMPARISON(AppSection.DAVISSON_GERMER, CompactTutorEntryId.DG_COMPARISON, "Zurück zum Vergleich",
        listOf(TutorQuestionExample.ANGLE, TutorQuestionExample.DIFFERENCE)),
    DG_MODEL(AppSection.DAVISSON_GERMER, CompactTutorEntryId.DG_MODEL, "Zurück zum Modell",
        listOf(TutorQuestionExample.DIFFERENCE, TutorQuestionExample.ANGLE)),
    SLIT_MODEL(AppSection.JONSSON, CompactTutorEntryId.SLIT_MODEL, "Zurück zum Spaltmodell",
        listOf(TutorQuestionExample.SPACING, TutorQuestionExample.WIDTH)),
    IMAGE_MEASUREMENT(AppSection.JONSSON, CompactTutorEntryId.IMAGE_MEASUREMENT, "Zurück zur Messung",
        listOf(TutorQuestionExample.SCALE, TutorQuestionExample.MAXIMA)),
    CONCLUSION(AppSection.CONCLUSION, CompactTutorEntryId.CONCLUSION, "Zurück zum Fazit",
        listOf(TutorQuestionExample.ARGUMENT, TutorQuestionExample.LIMIT));

    val heading: String get() = when (this) {
        CALCULATION -> "De Broglie · Deine Rechnung bei 150 Volt"
        DE_BROGLIE_MODEL -> "De Broglie · Spannungen vergleichen"
        DG_COMPARISON -> "Davisson–Germer · Vorhersage und Befund vergleichen"
        DG_MODEL -> "Davisson–Germer · Modell verändern"
        SLIT_MODEL -> "Jönsson · Spaltmodell untersuchen"
        IMAGE_MEASUREMENT -> "Jönsson · Deine Bildmessung"
        CONCLUSION -> "Fazit · Deine Argumentation"
    }

    val nextAction: String get() = when (this) {
        CALCULATION -> "Prüfe danach deinen Rechenweg und ergänze ihn hier in deinen eigenen Worten."
        DE_BROGLIE_MODEL -> "Prüfe die Erklärung danach hier an zwei Spannungen im Modell."
        DG_COMPARISON -> "Prüfe danach beide Rechenwege und ergänze hier deine Einordnung des Vergleichs."
        DG_MODEL -> "Prüfe die Erklärung danach hier an einer veränderten Modelleinstellung."
        SLIT_MODEL -> "Prüfe die Erklärung danach hier, indem du Spaltabstand und Spaltbreite einzeln veränderst."
        IMAGE_MEASUREMENT -> "Prüfe danach hier deine Marken am Bild und ergänze deine Auswahlbegründung."
        CONCLUSION -> "Prüfe danach die Belege und Grenzen deiner Aussage und überarbeite hier dein eigenes Fazit."
    }

    companion object {
        fun forSection(section: AppSection): TutorHelpPoint = when (section) {
            AppSection.DE_BROGLIE -> CALCULATION
            AppSection.DAVISSON_GERMER -> DG_COMPARISON
            AppSection.JONSSON -> IMAGE_MEASUREMENT
            AppSection.CONCLUSION -> CONCLUSION
        }

        fun chapterExamples(section: AppSection): List<TutorQuestionExample> = when (section) {
            AppSection.DE_BROGLIE -> listOf(TutorQuestionExample.HYPOTHESIS, TutorQuestionExample.VOLTAGE)
            AppSection.DAVISSON_GERMER -> listOf(TutorQuestionExample.DETECTOR, TutorQuestionExample.ANGLE)
            AppSection.JONSSON -> listOf(TutorQuestionExample.SPACING, TutorQuestionExample.SCALE)
            AppSection.CONCLUSION -> listOf(TutorQuestionExample.ARGUMENT, TutorQuestionExample.LIMIT)
        }
    }
}

const val TUTOR_QUESTION_LIMIT = 2000

/** Append instead of overwriting a learner's draft; refuse overflow rather than truncate. */
fun withTutorExample(draft: String, example: TutorQuestionExample): String? {
    if (draft.lineSequence().any { it.trim() == example.question }) return draft
    val next = if (draft.isBlank()) example.question else draft + "\n\n" + example.question
    return next.takeIf { it.length <= TUTOR_QUESTION_LIMIT }
}

@Composable
fun TutorHelpEntry(point: TutorHelpPoint, session: LabSession, viewState: CompactTutorUiState) {
    TutorHelpButton(point, viewState)
    TutorHelpPanel(point, session, viewState)
}

/** The entry stays beside the task; a wide panel may be rendered after its workspace. */
@Composable
fun TutorHelpButton(point: TutorHelpPoint, viewState: CompactTutorUiState) {
    val expanded = viewState.open && viewState.originFor(point.section) == point
    Div(attrs = { articleStyles(ArticleClass.ACTIONS) }) {
        Button(attrs = {
            elementId(point.entry)
            styles(LabClass.SECONDARY)
            controls(CompactTutorId.PANEL)
            ariaExpanded(expanded)
            onClick {
                if (expanded) closeCompactTutor(viewState, point.section)
                else viewState.openFrom(point, document.findHtmlElement(point.entry)?.getBoundingClientRect()?.top)
            }
        }) { Text(if (expanded) "Hilfe schließen" else "Hilfe mit deinem KI-Chat") }
    }
}

@Composable
fun TutorHelpPanel(
    point: TutorHelpPoint,
    session: LabSession,
    viewState: CompactTutorUiState,
    placement: TutorPanelPlacement = TutorPanelPlacement.INLINE,
) {
    if (viewState.open && viewState.originFor(point.section) == point) {
        CompactTutorPanel(session, point.section, viewState.questions[point.section].orEmpty(),
            { viewState.questions[point.section] = it }, viewState, point.returnLabel,
            { closeCompactTutor(viewState, point.section) }, placement)
    }
}
