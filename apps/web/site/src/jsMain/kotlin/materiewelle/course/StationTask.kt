package materiewelle.course

import androidx.compose.runtime.*
import kotlin.js.Date
import materiewelle.core.*
import materiewelle.core.course.*
import materiewelle.course.content.ContentVisibility
import materiewelle.course.content.CourseContent
import materiewelle.state.LabSession
import materiewelle.ui.*
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*

/** Input repair, a checked value and deliberately opened worked control are separate actions. */
@Composable
internal fun NumericTask(session: LabSession) {
    val id = StationId.S06_CALCULATION
    val work = session.snapshot.stationWork(id)
    var localSolutionOpen by remember { mutableStateOf(false) }
    val solutionOpen = work.solutionViewed || localSolutionOpen
    Section(
        attrs = {
            elementId(LearningFlowId.PRIMARY_CALCULATION)
            learningFlowStyles(LearningFlowClass.PRIMARY_CALCULATION)
        }
    ) {
        FieldLabel(Id.COURSE_TASK_ANSWER) {
            MathText("Deine Wellenlänge bei 150 V · Zahl und Einheit (pm, nm oder m)")
        }
        Input(
            InputType.Text,
            attrs = {
                elementId(Id.COURSE_TASK_ANSWER)
                protectPersonalWork(session.isReadOnly)
                value(work.numericAnswer)
                maxLength(120)
                describedBy(LearningHelpId.INPUT_PREVIEW)
                ariaInvalid(work.checked && WavelengthInputs.parse(work.numericAnswer) is WavelengthInput.Rejected)
                onInput { event ->
                    session.editWork(id) {
                        it.copy(numericAnswer = event.value.take(120), checked = false)
                    }
                }
            },
        )
        WavelengthInputPreview(work.numericAnswer)
        Button(
            attrs = {
                elementId(Id.COURSE_NUMERIC_CHECK)
                protectPersonalWork(session.isReadOnly)
                styles(LabClass.PRIMARY)
                onClick { session.editWork(id) { it.copy(checked = true) } }
            }
        ) {
            MathText("Zahlenwert und Einheit prüfen")
        }
        if (work.checked) {
            val result = NumericWavelengthQuestion(Voltage(150.0)).evaluate(work.numericAnswer)
            P(
                attrs = {
                    elementId(Id.COURSE_NUMERIC_FEEDBACK)
                    role(AriaRole.STATUS)
                    data(LabData.ANSWER_STATUS, result.status)
                    styles(
                        if (result.status == NumericAnswerStatus.CORRECT) LabClass.COURSE_RESULT
                        else LabClass.ERROR
                    )
                }
            ) {
                MathText(result.feedback)
            }
        }
        Details(
            attrs = {
                elementId(LearningFlowId.CALCULATION_HELP)
            }
        ) {
            Summary { MathText("Konstanten und SI-Einheiten") }
            CourseContent[id]
                .steps
                .filter { it.visibility == ContentVisibility.ALWAYS }
                .forEach { step -> P { MathText(step.text) } }
            P { MathText("Rechne zunächst in Metern. 1 pm = 10⁻¹² m und 1 nm = 1000 pm.") }
        }
        Details {
            Summary { MathText("Hilfe zu einem Rechenschritt") }
            NumericStepHelp(session)
        }
        Button(
            attrs = {
                elementId(CourseControl.NUMERIC_SOLUTION)
                styles(LabClass.SECONDARY)
                onClick {
                    if (session.isReadOnly) localSolutionOpen = true
                    else session.editWork(id) { it.copy(solutionViewed = true) }
                }
            }
        ) {
            MathText(
                if (solutionOpen) "Kontrollrechnung geöffnet"
                else "Kontrollrechnung öffnen"
            )
        }
        if (solutionOpen)
            P(attrs = { styles(LabClass.COURSE_RESULT) }) {
                MathText(
                    "150-V-Kontrollwert: ${Physics.decimal(Physics.electronWavelength(Voltage(150.0)).picometers, 4)} pm"
                )
            }
        P { Text("Geprüft werden Zahl und Einheit. Deinen Rechenweg vergleichst du selbst mit der Musterrechnung.") }
        if (solutionOpen) WorkedElectronCalculation()
    }
}

/**
 * Already explained comparison and independent new exercises, kept apart from the primary input.
 */
@Composable
internal fun NumericComparisonTasks(session: LabSession) {
    val id = StationId.S06_CALCULATION
    val work = session.snapshot.stationWork(id)
    Section(
        attrs = {
            elementId(LearningFlowId.COMPARISON_CALCULATIONS)
            learningFlowStyles(LearningFlowClass.COMPARISON_CALCULATIONS)
        }
    ) {
        H3 { MathText("Geführter Vergleich: 600 V") }
        ComparisonCompletion()
        P {
            MathText(
                "Die Spannung steigt von 150 V auf 600 V. Überlege zuerst: Um welchen Faktor ändern sich Energie, Impuls und Wellenlänge? Berechne anschließend die neue Wellenlänge."
            )
        }
        FieldLabel(Id.COURSE_TRANSFER_ANSWER) { MathText("Deine Wellenlänge bei 600 V mit Einheit") }
        Input(
            InputType.Text,
            attrs = {
                elementId(Id.COURSE_TRANSFER_ANSWER)
                value(work.transferAnswer)
                maxLength(120)
                onInput { event ->
                    session.editWork(id) {
                        it.copy(transferAnswer = event.value.take(120), transferChecked = false)
                    }
                }
            },
        )
        Button(
            attrs = {
                elementId(Id.COURSE_TRANSFER_CHECK)
                styles(LabClass.PRIMARY)
                onClick { session.editWork(id) { it.copy(transferChecked = true) } }
            }
        ) {
            MathText("Vergleichswert prüfen")
        }
        if (work.transferChecked) {
            val result = NumericWavelengthQuestion(Voltage(600.0)).evaluate(work.transferAnswer)
            P(
                attrs = {
                    elementId(Id.COURSE_TRANSFER_FEEDBACK)
                    role(AriaRole.STATUS)
                    data(LabData.ANSWER_STATUS, result.status)
                    styles(
                        if (result.status == NumericAnswerStatus.CORRECT) LabClass.COURSE_RESULT
                        else LabClass.ERROR
                    )
                }
            ) {
                MathText(result.feedback)
            }
        }
        Button(
            attrs = {
                elementId(CourseControl.TRANSFER_SOLUTION)
                styles(LabClass.SECONDARY)
                onClick { session.editWork(id) { it.copy(transferSolutionViewed = true) } }
            }
        ) {
            MathText(
                if (work.transferSolutionViewed) "600-V-Kontrolle geöffnet"
                else "600-V-Kontrolle öffnen"
            )
        }
        if (work.transferSolutionViewed)
            P(attrs = { styles(LabClass.COURSE_RESULT) }) {
                MathText(
                    "600-V-Kontrollwert: ${Physics.decimal(Physics.electronWavelength(Voltage(600.0)).picometers, 4)} pm"
                )
            }
        Section(
            attrs = {
                elementId(CourseControl.PRACTICE_LIST)
                courseStyles(CourseStyle.CASES)
            }
        ) {
            H3 { MathText("Zwei weitere Spannungen") }
            P {
                MathText(
                    "Rechne zunächst selbst und prüfe dann Zahl und Einheit. Wenn du eine Kontrolle öffnest, wird das im Arbeitsstand vermerkt; ob du sie gelesen hast, wird nicht erfasst."
                )
            }
            Div(attrs = { learningFlowStyles(LearningFlowClass.PRACTICE_GRID) }) {
                PracticeCaseId.entries.forEach { case -> PracticeTask(case, session) }
            }
        }
    }
}

@Composable
private fun PracticeTask(case: PracticeCaseId, session: LabSession) {
    val attempt = session.snapshot.practice[case] ?: PracticeAttempt()
    Div(attrs = { styles(LabClass.PANEL) }) {
        H3 { MathText("Neuer Rechenfall · ${case.label}") }
        P {
            MathText(
                "Start aus Ruhe, nichtrelativistische Näherung. Bestimme λ und prüfe die Größenordnung gegenüber deiner 150-V-Rechnung."
            )
        }
        FieldLabel(PracticeControl(case, PracticePart.INPUT)) { MathText("Dein Ergebnis mit Einheit") }
        Input(
            InputType.Text,
            attrs = {
                elementId(PracticeControl(case, PracticePart.INPUT))
                value(attempt.draft)
                maxLength(120)
                onInput { event -> session.editPractice(case, event.value) }
            },
        )
        Button(
            attrs = {
                elementId(PracticeControl(case, PracticePart.CHECK))
                styles(LabClass.PRIMARY)
                if (attempt.draft.isBlank()) disabled()
                onClick { session.checkPractice(case) }
            }
        ) {
            MathText("Zahlenwert und Einheit prüfen")
        }
        if (attempt.checked) {
            val result = case.question.evaluate(attempt.draft)
            P(
                attrs = {
                    elementId(PracticeControl(case, PracticePart.FEEDBACK))
                    role(AriaRole.STATUS)
                    data(LabData.ANSWER_STATUS, result.status)
                    styles(
                        if (result.status == NumericAnswerStatus.CORRECT) LabClass.COURSE_RESULT
                        else LabClass.ERROR
                    )
                }
            ) {
                MathText(result.feedback)
            }
        }
        Button(
            attrs = {
                elementId(PracticeControl(case, PracticePart.SOLUTION))
                styles(LabClass.SECONDARY)
                onClick { session.revealPracticeSolution(case) }
            }
        ) {
            MathText(
                if (attempt.solutionViewed) "Kontrolle geöffnet"
                else "Kontrolle öffnen"
            )
        }
        if (attempt.solutionViewed) {
            P(attrs = { styles(LabClass.COURSE_RESULT) }) {
                MathText(
                    "NR-Kontrollwert bei ${case.label}: ${Physics.decimal(case.question.answerPm, 4)} pm. Verwende E_kin = eU, p = √(2mₑE_kin), dann λ = h/p."
                )
            }
            P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
                MathText(
                    if (attempt.firstSubmission == null)
                        "Die Kontrolle ist geöffnet; eine erste Abgabe ist hier noch nicht protokolliert. Du kannst anschließend mit der Kontrolle üben."
                    else
                        "Erste protokollierte Abgabe: ${attempt.firstSubmission}. Die Kontrolle wurde ebenfalls geöffnet; eine Reihenfolge oder Bewertung deiner freien Erklärung folgt daraus nicht."
                )
            }
        }
    }
}

@Composable
internal fun QuestionTask(questions: List<ConceptQuestion>, session: LabSession) {
    val questionId =
        session.snapshot.activeQuestion?.takeIf { active -> questions.any { it.id == active } }
            ?: questions.first().id
    val question = questions.single { it.id == questionId }
    val attempt = session.snapshot.questionWork(questionId)
    if (questions.size > 1)
        Div(attrs = { styles(LabClass.COURSE_ACTIONS) }) {
            questions.forEach { q ->
                Button(
                    attrs = {
                        styles(LabClass.SECONDARY)
                        ariaPressed(q.id == questionId)
                        onClick { session.update { it.copy(activeQuestion = q.id) } }
                    }
                ) {
                    MathText(questionLabel(q.id))
                }
            }
        }
    Div(
        attrs = {
            elementId(Id.COURSE_QUESTION)
            data(LabData.QUESTION, questionId)
        }
    ) {
        H3 { MathText("${question.id.name} · ${question.prompt}") }
        if (attempt.isHistorical)
            P(attrs = { styles(LabClass.SOURCE_WARNING) }) {
                MathText(
                    "Diese Frage wurde in einer früheren Inhaltsfassung bearbeitet. Der alte Stand bleibt gespeichert; prüfe den aktuellen Fall erneut."
                )
            }
        Div(
            attrs = {
                styles(LabClass.COURSE_OPTIONS)
                role(AriaRole.GROUP)
                ariaLabel("Antwortmöglichkeiten")
            }
        ) {
            question.options.forEach { option ->
                Button(
                    attrs = {
                        elementId(ElementId.CourseOption(question.id, option.id))
                        styles(LabClass.SECONDARY)
                        if (attempt.selected == option.id) styles(LabClass.CHOSEN)
                        ariaPressed(attempt.selected == option.id)
                        onClick { session.answer(question.id, option.id) }
                    }
                ) {
                    MathText(option.text)
                }
            }
        }
        Button(
            attrs = {
                elementId(Id.COURSE_TASK_CHECK)
                styles(LabClass.PRIMARY)
                if (attempt.selected == null) disabled()
                onClick { session.check(question.id, Date.now().toLong()) }
            }
        ) {
            MathText("Auswahl prüfen")
        }
        Details(
            attrs = {
                elementId(Id.COURSE_QUESTION_HINT)
                styles(LabClass.NOTES)
            }
        ) {
            Summary() { MathText("Denkhinweis zu dieser Frage") }
            question.hints.forEachIndexed { n, text -> P { MathText("${n + 1}. $text") } }
        }
        if (attempt.checked && attempt.isCurrentContent) {
            val answer = question.option(requireNotNull(attempt.selected))
            P(
                attrs = {
                    elementId(Id.COURSE_TASK_FEEDBACK)
                    role(AriaRole.STATUS)
                    data(LabData.CORRECT, answer.isCorrect)
                    styles(if (answer.isCorrect) LabClass.COURSE_RESULT else LabClass.ERROR)
                }
            ) {
                MathText(answer.feedback)
            }
            P { MathText("Anschlussfall: ${question.followUp}") }
            P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
                MathText(
                    "Geprüft wurde deine Auswahl. Begründe den Anschlussfall ohne Vorlage; deine freie Erklärung wird nicht automatisch fachlich bewertet."
                )
            }
        }
    }
}

internal fun questionLabel(id: QuestionId): String =
    when (id) {
        QuestionId.Q01 -> "Impuls und Wellenlänge"
        QuestionId.Q02 -> "Spannung, Energie und Wurzel"
        QuestionId.Q03 -> "Einsteins Zustimmung einordnen"
        QuestionId.Q04 -> "Was der DG-Detektor misst"
        QuestionId.Q05 -> "Bezugslinien eines Winkels"
        QuestionId.Q06 -> "Simulation oder unabhängiger Beleg?"
        QuestionId.Q07 -> "Bildzoom und Kalibrierung"
        QuestionId.Q08 -> "Zwei ergänzende Experimente"
        QuestionId.Q09 -> "Der neue Hypothesenschritt"
        QuestionId.T01 -> "Anfangsenergie"
        QuestionId.T02 -> "Winkel zur Ebene und Normalen"
        QuestionId.T03 -> "Innere Marke und Endmarke"
        QuestionId.T04 -> "Unabhängiger Fotomaßstab"
    }
