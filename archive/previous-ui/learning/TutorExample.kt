package materiewelle.learning

import androidx.compose.runtime.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import materiewelle.core.*
import materiewelle.core.course.SessionSnapshot
import materiewelle.core.course.TutorCourseContext
import materiewelle.presentation.styles.*
import materiewelle.ui.MathText as Text
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

private data class TutorPrompt(val question: String, val hint: String, val explanation: String)

private val quizJson = Json { prettyPrint = true }
private val prompts =
    listOf(
        TutorPrompt(
            "Warum halbiert sich λ bei vierfachem U?",
            "Wie verändert sich √U, wenn U mit 4 multipliziert wird?",
            "Aus eU = p²/(2m) folgt p ∝ √U. Bei 4U verdoppelt sich p; λ = h/p halbiert sich. Das gilt hier nicht relativistisch.",
        ),
        TutorPrompt(
            "Kann ich aus 14 Pixeln die Wellenlänge messen?",
            "Welche Einheit haben Pixel? Welche zusätzliche Information würde daraus eine Länge in Metern machen?",
            "Nein. Erst ein unabhängig bestimmter Bildmaßstab und die zu dieser Aufnahme belegten Versuchsparameter ermöglichen die Umrechnung. Die Modellvorhersage als Bildmaßstab zu benutzen wäre zirkulär.",
        ),
        TutorPrompt(
            "Sind die Wellenlinien Elektronenbahnen?",
            "Was wird in der Schemazeichnung dargestellt, und was wird im Experiment tatsächlich registriert?",
            "Die Linien veranschaulichen Phasenfronten eines idealen Wellenmodells. Sie sind keine gemessenen individuellen Elektronenbahnen. Die Beugungs- und Interferenzstrukturen werden am jeweiligen Aufbau und seinen Daten untersucht.",
        ),
    )

@Composable
fun TutorExample(
    active: Boolean,
    courseContext: TutorCourseContext? = null,
    sessionSnapshot: SessionSnapshot? = null,
) {
    var quizIndex by remember { mutableStateOf(0) }
    var answer by remember { mutableStateOf("") }
    var checked by remember { mutableStateOf(false) }
    var calibrationAnswer by remember { mutableStateOf<Int?>(null) }
    var promptIndex by remember { mutableStateOf(0) }
    var revealed by remember { mutableStateOf(false) }
    var draftText by remember {
        mutableStateOf(
            quizJson.encodeToString(
                QuizDraft(
                    "Welche Wellenlänge folgt bei 600 V aus dem nicht relativistischen Modell?",
                    150.0,
                    600.0,
                    50.07,
                )
            )
        )
    }
    var draftStatus by remember { mutableStateOf<LoadState<QuizDraft>?>(null) }
    var imported by remember { mutableStateOf<QuizDraft?>(null) }
    val quiz =
        WavelengthQuiz(
            Voltage(imported?.sourceVoltageV ?: listOf(150.0, 100.0, 200.0, 60.0)[quizIndex % 4])
        )
    val prompt = prompts[promptIndex]
    PortableTutor(
        active,
        quiz.voltage,
        if (checked) quiz.accepts(answer) else null,
        courseContext,
        sessionSnapshot,
    )
    Div(attrs = { styles(LabClass.LEARNING_CARDS) }) {
        Section(
            attrs = {
                styles(LabClass.PANEL)
                elementId(Id.QUIZ_PANEL)
            }
        ) {
            P(attrs = { styles(LabClass.EYEBROW) }) { Text("PRÜFBARE AUFGABEN / LOKALE VORLAGEN") }
            H2 { Text("Vorhersage-Quiz") }
            imported?.let {
                P(attrs = { styles(LabClass.SOURCE_WARNING) }) {
                    Text(
                        "Importierter Entwurf: Rechenwert geprüft, Wortlaut noch nicht fachlich freigegeben. "
                    )
                    Text(it.prompt)
                }
            }
            P(attrs = { elementId(Id.QUIZ_QUESTION) }) {
                Text(
                    "Bei ${Physics.decimal(quiz.voltage.volts, 0)} V beträgt λ = ${Physics.decimal(Physics.electronWavelength(quiz.voltage).picometers, 2)} pm. Welche Wellenlänge erwartest du bei ${Physics.decimal(quiz.targetVoltage.volts, 0)} V?"
                )
            }
            FieldLabel(forId = Id.QUIZ_ANSWER) { Text("Deine Vorhersage / pm") }
            Input(
                InputType.Text,
                attrs = {
                    elementId(Id.QUIZ_ANSWER)
                    value(answer)
                    inputMode(LabInputMode.DECIMAL)
                    onInput {
                        answer = it.value
                        checked = false
                    }
                },
            )
            Div(attrs = { styles(LabClass.WORKSHOP_ACTIONS) }) {
                Button(
                    attrs = {
                        elementId(Id.QUIZ_CHECK)
                        styles(LabClass.PRIMARY)
                        onClick { checked = true }
                    }
                ) {
                    Text("Vorhersage prüfen")
                }
                Button(
                    attrs = {
                        elementId(Id.QUIZ_NEW)
                        styles(LabClass.SECONDARY)
                        onClick {
                            imported = null
                            quizIndex++
                            checked = false
                            answer = ""
                        }
                    }
                ) {
                    Text("Andere Zahlen")
                }
            }
            if (checked)
                Div(
                    attrs = {
                        elementId(Id.QUIZ_FEEDBACK)
                        styles(if (quiz.accepts(answer)) LabClass.MODEL_LIMIT else LabClass.ERROR)
                        role(AriaRole.STATUS)
                        data(LabData.CORRECT, quiz.accepts(answer))
                        data(LabData.ANSWER_PM, quiz.answerPm)
                    }
                ) {
                    Text(
                        if (quiz.accepts(answer))
                            "Richtig: ${Physics.decimal(quiz.answerPm, 2)} pm. Vierfaches U bedeutet doppelten Impuls und halbe Wellenlänge."
                        else
                            "Noch nicht. Nutze λ ∝ 1/√U. Erwartet werden ${Physics.decimal(quiz.answerPm, 2)} pm (Rundungstoleranz ±0,06 pm)."
                    )
                }
            H3 { Text("Aussageprüfung zum Foto") }
            P {
                Text(
                    "Ein Bildprofil zeigt etwa 14 native Pixel pro Streifen. Welche Aussage ist belegt?"
                )
            }
            listOf(
                    "Ich kenne damit die absolute Wellenlänge.",
                    "Ich kenne einen relativen Bildabstand; eine unabhängige Kalibrierung fehlt.",
                )
                .forEachIndexed { i, text ->
                    Button(
                        attrs = {
                            elementId(ElementId.CalibrationChoice(UiIndex(i)))
                            styles(LabClass.SECONDARY, LabClass.QUIZ_CHOICE)
                            ariaPressed((calibrationAnswer == i))
                            onClick { calibrationAnswer = i }
                        }
                    ) {
                        Text(text)
                    }
                }
            calibrationAnswer?.let { i ->
                P(
                    attrs = {
                        elementId(Id.QUIZ_CALIBRATION_FEEDBACK)
                        styles(if (i == 1) LabClass.MODEL_LIMIT else LabClass.ERROR)
                        role(AriaRole.STATUS)
                    }
                ) {
                    Text(
                        if (i == 1)
                            "Richtig. Pixelmessung und unabhängige physikalische Kalibrierung sind unterschiedliche Schritte."
                        else
                            "Die Pixelzahl besitzt noch keinen Maßstab in Metern. Eine absolute Wellenlänge ist damit nicht belegt."
                    )
                }
            }
            P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
                Text(
                    "Vorlagen mit wechselnden Zahlen; Lösungen werden aus dem Kotlin-Kern berechnet. Keine Live-KI-Generierung."
                )
            }
            Details(
                attrs = {
                    styles(LabClass.NOTES)
                    elementId(Id.QUIZ_IMPORT)
                }
            ) {
                Summary() { Text("KI- oder eigenen Aufgabenentwurf als JSON prüfen") }
                P(attrs = { styles(LabClass.SMALL) }) {
                    Text(
                        "Auch gültiges JSON kann fachlich falsch sein. Hier werden Spannungen und behauptete Zahlenlösung unabhängig geprüft; den Wortlaut musst du selbst prüfen."
                    )
                }
                A(
                    href =
                        "data:text/plain;charset=utf-8,${encodeURIComponent(quizAuthoringPrompt(quiz.voltage))}",
                    attrs = {
                        elementId(Id.QUIZ_AUTHORING_EXPORT)
                        styles(LabClass.SECONDARY)
                        downloadFile("materiewelle-ki-aufgabenbrief.txt")
                    },
                ) {
                    Text("Aufgabenbrief für deinen eigenen KI-Chat herunterladen")
                }
                Details(
                    attrs = {
                        elementId(Id.QUIZ_AUTHORING_PREVIEW)
                        styles(LabClass.NOTES)
                    }
                ) {
                    Summary() { Text("Vollständigen Aufgabenbrief vorab lesen") }
                    P(
                        attrs = {
                            style {
                                setWhiteSpace(WhiteSpaceValue.PRE_WRAP)
                                setOverflowWrap(OverflowWrapValue.ANYWHERE)
                            }
                        }
                    ) {
                        Text(quizAuthoringPrompt(quiz.voltage))
                    }
                }
                FieldLabel(forId = Id.QUIZ_DRAFT) { Text("Strukturierter Aufgabenentwurf") }
                TextArea(
                    attrs = {
                        elementId(Id.QUIZ_DRAFT)
                        textRows(7)
                        value(draftText)
                        onInput {
                            draftText = it.value
                            draftStatus = null
                        }
                    }
                )
                Button(
                    attrs = {
                        elementId(Id.QUIZ_IMPORT_CHECK)
                        styles(LabClass.SECONDARY)
                        onClick {
                            draftStatus =
                                try {
                                    val draft =
                                        Json.decodeFromString<QuizDraft>(draftText).verifyNumerics()
                                    imported = draft
                                    checked = false
                                    answer = ""
                                    LoadState.Ready(draft)
                                } catch (_: Exception) {
                                    LoadState.Failed(
                                        "Entwurf abgelehnt: Schema, Spannungsbereich oder behauptete Lösung stimmt nicht. Die bisherige Aufgabe bleibt erhalten."
                                    )
                                }
                        }
                    }
                ) {
                    Text("Entwurf unabhängig prüfen")
                }
                when (val status = draftStatus) {
                    is LoadState.Ready ->
                        P(
                            attrs = {
                                elementId(Id.QUIZ_IMPORT_STATUS)
                                styles(LabClass.MODEL_LIMIT)
                                role(AriaRole.STATUS)
                                data(LabData.VALID, true)
                            }
                        ) {
                            Text("Rechenwert geprüft. Entwurf übernommen; Wortlaut noch ungeprüft.")
                        }
                    is LoadState.Failed ->
                        P(
                            attrs = {
                                elementId(Id.QUIZ_IMPORT_STATUS)
                                styles(LabClass.ERROR)
                                role(AriaRole.STATUS)
                                data(LabData.VALID, false)
                            }
                        ) {
                            Text(status.message)
                        }
                    else -> Unit
                }
            }
        }
        Section(
            attrs = {
                styles(LabClass.PANEL)
                elementId(Id.TUTOR_PANEL)
            }
        ) {
            P(attrs = { styles(LabClass.EYEBROW) }) { Text("LERNTUTOR / VORBEREITETE ANTWORTEN") }
            H2 { Text("Erst ein Hinweis, dann die Begründung") }
            Div(attrs = { styles(LabClass.TUTOR_PROMPTS) }) {
                prompts.forEachIndexed { i, p ->
                    Button(
                        attrs = {
                            elementId(ElementId.TutorPrompt(UiIndex(i)))
                            styles(LabClass.SECONDARY)
                            ariaPressed((promptIndex == i))
                            onClick {
                                promptIndex = i
                                revealed = false
                            }
                        }
                    ) {
                        Text(p.question)
                    }
                }
            }
            Div(
                attrs = {
                    styles(LabClass.MODEL_LIMIT)
                    elementId(Id.TUTOR_HINT)
                    ariaLive(LiveMode.POLITE)
                }
            ) {
                Strong() { Text("Denkfrage") }
                P { Text(prompt.hint) }
            }
            Button(
                attrs = {
                    elementId(Id.TUTOR_REVEAL)
                    styles(LabClass.PRIMARY)
                    onClick { revealed = !revealed }
                }
            ) {
                Text(if (revealed) "Begründung ausblenden" else "Begründung zeigen")
            }
            if (revealed)
                P(
                    attrs = {
                        elementId(Id.TUTOR_EXPLANATION)
                        ariaLive(LiveMode.POLITE)
                    }
                ) {
                    Text(prompt.explanation)
                }
            WorkshopNotes(
                "Tutor im eigenen Chat",
                "Das mitnehmbare Paket oben verbindet geprüfte Physikinhalte mit gezielten Hilfen und Quellenstatus. Der vollständige Starttext funktioniert als Nutzeranweisung im eigenen Chat; das Skill-ZIP ist für kompatible Clients. Diese lokalen Hinweise bleiben auch ohne eigenen KI-Chat nutzbar. Die Website ruft keinen KI-Anbieter auf.",
            )
        }
    }
    MediaExample(active, quiz.voltage)
}

@JsName("encodeURIComponent") private external fun encodeURIComponent(text: String): String
