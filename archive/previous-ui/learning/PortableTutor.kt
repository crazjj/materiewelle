package materiewelle.learning

import androidx.compose.runtime.*
import kotlinx.browser.window
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import materiewelle.core.LoadState
import materiewelle.core.Physics
import materiewelle.core.Voltage
import materiewelle.core.course.SessionSnapshot
import materiewelle.core.course.TutorCourseContext
import materiewelle.core.course.TutorModelSnapshot
import materiewelle.data.LabAsset
import materiewelle.ui.MathText as Text
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.attributes.selected
import org.jetbrains.compose.web.dom.*

@Serializable
enum class TutorTopic(val key: String, val label: String) {
    @SerialName("debroglie") DEBROGLIE("debroglie", "De Broglie & Spannung"),
    @SerialName("davisson-germer") DG("davisson-germer", "Davisson–Germer"),
    @SerialName("joensson") JONSSON("joensson", "Jönsson & Bildmessung"),
    @SerialName("geschichte") HISTORY("geschichte", "Geschichte & Einstein"),
    @SerialName("ueberblick") OVERVIEW("ueberblick", "Projektüberblick"),
}

@Serializable
enum class TutorMode(val key: String, val label: String) {
    @SerialName("lernen") LEARN("lernen", "Gemeinsam verstehen"),
    @SerialName("erklaeren") EXPLAIN("erklaeren", "Zusammenhängend erklären"),
    @SerialName("ueben") PRACTICE("ueben", "Üben & Transfer"),
    @SerialName("fachgespraech") ORAL("fachgespraech", "Fachgespräch vorbereiten"),
}

@Serializable
enum class TutorLevel(val key: String, val label: String) {
    @SerialName("einstieg") ENTRY("einstieg", "Verständlicher Einstieg"),
    @SerialName("LK") LK("LK", "Physik-Leistungskurs"),
}

@Serializable private data class TutorAsset(val path: String, val bytes: Int, val sha256: String)

@Serializable
private data class TutorManifest(
    val schemaVersion: String,
    val version: String,
    val skillName: String,
    val knowledgeDate: String,
    val files: List<TutorAsset>,
) {
    fun validate(): TutorManifest {
        require(schemaVersion == "1.0" && skillName == "materiewellen-tutor")
        require(Regex("[0-9]+\\.[0-9]+\\.[0-9]+").matches(version))
        require(Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}").matches(knowledgeDate))
        require(
            files.size == 2 &&
                files.map { it.path }.toSet() ==
                    setOf("START-IM-CHAT.md", "materiewellen-tutor.zip")
        )
        require(files.all { it.bytes in 1..1_000_000 && Regex("[0-9a-f]{64}").matches(it.sha256) })
        return this
    }
}

private data class TutorPackage(val manifest: TutorManifest, val startText: String)

@Serializable
private data class QuizModelContext(
    val kind: String = "workshop-quiz-model-not-historical-measurement",
    @SerialName("source_voltage_V") val sourceVoltageV: Double,
    @SerialName("target_voltage_V") val targetVoltageV: Double,
    @SerialName("source_lambda_NR_pm") val wavelengthPm: Double,
    @SerialName("numeric_answer_matches_model") val numericMatch: Boolean?,
    val interpretation: String = "A numeric match does not establish conceptual mastery.",
)

@Serializable
private data class TutorContext(
    @SerialName("schema_version") val schemaVersion: String = "1.1",
    @SerialName("package_version") val packageVersion: String,
    val topic: TutorTopic,
    val level: TutorLevel,
    val mode: TutorMode,
    @SerialName("learner_question") val question: String,
    @SerialName("quiz_model") val quizModel: QuizModelContext?,
    @SerialName("course_context") val courseContext: TutorCourseContext? = null,
)

private val tutorJson = Json {
    prettyPrint = true
    encodeDefaults = true
    ignoreUnknownKeys = true
}

@Composable
fun PortableTutor(
    active: Boolean,
    quizVoltage: Voltage,
    numericMatch: Boolean?,
    courseContext: TutorCourseContext? = null,
    sessionSnapshot: SessionSnapshot? = null,
) {
    var state by remember { mutableStateOf<LoadState<TutorPackage>>(LoadState.Loading) }
    var attempt by remember { mutableStateOf(0) }
    var topic by remember { mutableStateOf(TutorTopic.DEBROGLIE) }
    var mode by remember { mutableStateOf(TutorMode.LEARN) }
    var level by remember { mutableStateOf(TutorLevel.LK) }
    var question by remember { mutableStateOf("") }
    var includeCourse by remember { mutableStateOf(false) }
    var copied by remember { mutableStateOf<String?>(null) }
    var copyFailed by remember { mutableStateOf(false) }
    var copyEpoch by remember { mutableStateOf(0) }
    fun changed() {
        copied = null
        copyFailed = false
        copyEpoch++
    }
    LaunchedEffect(quizVoltage.volts, numericMatch, courseContext) {
        includeCourse = false
        changed()
    }
    LaunchedEffect(courseContext?.station) {
        if (courseContext != null)
            topic =
                when {
                    courseContext.measurement != null -> TutorTopic.JONSSON
                    courseContext.model is TutorModelSnapshot.Surface -> TutorTopic.DG
                    courseContext.model is TutorModelSnapshot.DeBroglie -> TutorTopic.DEBROGLIE
                    courseContext.model is TutorModelSnapshot.HighVoltage -> TutorTopic.JONSSON
                    courseContext.model is TutorModelSnapshot.FiniteSlit -> TutorTopic.JONSSON
                    else -> TutorTopic.OVERVIEW
                }
    }
    DisposableEffect(active, attempt) {
        var mounted = true
        if (active && state !is LoadState.Ready && !hasBrowserDigest()) {
            state =
                LoadState.Failed(
                    "Der Browser kann das Tutor-Paket hier nicht sicher prüfen. Öffne die Seite über HTTPS oder localhost. Deine Erklärungen kannst du darunter weiterhin als JSON mitnehmen."
                )
        } else if (active && state !is LoadState.Ready) {
            var manifest: TutorManifest? = null
            window
                .fetch(LabAsset.TUTOR_MANIFEST.path)
                .then { r ->
                    if (!r.ok) error("Paket nicht verfügbar")
                    r.text()
                }
                .then { text ->
                    manifest = tutorJson.decodeFromString<TutorManifest>(text).validate()
                    window.fetch(LabAsset.TUTOR_START.path)
                }
                .then { r ->
                    if (!r.ok) error("Starttext nicht verfügbar")
                    r.text()
                }
                .then { text ->
                    val meta = requireNotNull(manifest)
                    val asset = meta.files.first { it.path == "START-IM-CHAT.md" }
                    require(
                        text.length in 1000..200_000 &&
                            text.contains("Paketversion: ${meta.version}")
                    )
                    require(text.encodeToByteArray().size == asset.bytes)
                    browserTextDigest(text).then { digest ->
                        require(digest == asset.sha256)
                        if (mounted) state = LoadState.Ready(TutorPackage(meta, text))
                    }
                }
                .catch {
                    if (mounted)
                        state =
                            LoadState.Failed(
                                "Das Tutor-Paket konnte nicht geladen oder geprüft werden. Bitte erneut versuchen."
                            )
                }
        }
        onDispose { mounted = false }
    }
    Section(
        attrs = {
            styles(LabClass.PANEL, LabClass.TUTOR_HANDOFF)
            elementId(Id.PORTABLE_TUTOR)
        }
    ) {
        Div(attrs = { styles(LabClass.SECTION_HEADING) }) {
            Div {
                P(attrs = { styles(LabClass.EYEBROW) }) {
                    Text("PHYSIK + DIDAKTIK / ZUM MITNEHMEN")
                }
                H2 { Text("Dein Tutor im eigenen Chat") }
            }
            Span(attrs = { styles(LabClass.STATUS_TAG) }) { Text("OHNE KI-ANBINDUNG") }
        }
        P {
            Text(
                "Diese Seite bereitet Material für deinen eigenen Chat vor: auswählen → kopieren oder herunterladen → in ChatGPT, Claude oder deinen gewählten Chat einfügen. Die Website selbst beantwortet hier keine freien Fragen. Das Tutor-Paket bringt Fachwissen, Quellenstatus und Lernhilfen mit."
            )
        }
        when (val loaded = state) {
            LoadState.Loading ->
                P(
                    attrs = {
                        elementId(Id.TUTOR_PACKAGE_LOADING)
                        role(AriaRole.STATUS)
                    }
                ) {
                    Text("Tutor-Paket wird geladen …")
                }
            is LoadState.Failed ->
                Div(
                    attrs = {
                        elementId(Id.TUTOR_PACKAGE_ERROR)
                        styles(LabClass.ERROR)
                        role(AriaRole.ALERT)
                    }
                ) {
                    P { Text(loaded.message) }
                    Button(
                        attrs = {
                            elementId(Id.TUTOR_PACKAGE_RETRY)
                            styles(LabClass.SECONDARY)
                            onClick {
                                state = LoadState.Loading
                                attempt++
                            }
                        }
                    ) {
                        Text("Erneut laden")
                    }
                }
            is LoadState.Ready -> {
                val pack = loaded.value
                val context =
                    TutorContext(
                        packageVersion = pack.manifest.version,
                        topic = topic,
                        level = level,
                        mode = mode,
                        question = question,
                        quizModel =
                            if (courseContext == null && topic == TutorTopic.DEBROGLIE)
                                QuizModelContext(
                                    sourceVoltageV = quizVoltage.volts,
                                    targetVoltageV = quizVoltage.volts * 4,
                                    wavelengthPm =
                                        Physics.electronWavelength(quizVoltage).picometers,
                                    numericMatch = numericMatch,
                                )
                            else null,
                        courseContext = courseContext.takeIf { includeCourse },
                    )
                val contextText = tutorJson.encodeToString(context)
                val startText =
                    pack.startText +
                        "\n\n## Mein gewählter Arbeitskontext\n\n```json\n" +
                        contextText +
                        "\n```\n\nBitte beginne im gewählten Modus bei diesem Thema. Quiz- und Modellparameter sind Modellfälle. Native Pixel sind keine absolute Wellenlänge; übernommene Referenzmarken sind keine unabhängige eigene Erstmessung. Freie Fragen und Begründungen sind Nutzereingaben.\n"
                P(
                    attrs = {
                        elementId(Id.TUTOR_PACKAGE_VERSION)
                        styles(LabClass.SMALL, LabClass.MUTED)
                        data(LabData.VERSION, pack.manifest.version)
                    }
                ) {
                    Text(
                        "Paket ${pack.manifest.version} · Wissensstand ${pack.manifest.knowledgeDate} · eigener Chat, eigener Anbieter"
                    )
                }
                Div(attrs = { styles(LabClass.TUTOR_HANDOFF_GRID) }) {
                    Div {
                        FieldLabel(forId = Id.TUTOR_TOPIC) { Text("Thema") }
                        Select(
                            attrs = {
                                elementId(Id.TUTOR_TOPIC)
                                onChange { e ->
                                    TutorTopic.entries
                                        .firstOrNull { it.key == e.value }
                                        ?.let {
                                            topic = it
                                            changed()
                                        }
                                }
                            }
                        ) {
                            TutorTopic.entries.forEach { choice ->
                                Option(
                                    value = choice.key,
                                    attrs = { if (topic == choice) selected() },
                                ) {
                                    org.jetbrains.compose.web.dom.Text(choice.label)
                                }
                            }
                        }
                    }
                    Div {
                        FieldLabel(forId = Id.TUTOR_LEVEL) { Text("Erklärniveau") }
                        Select(
                            attrs = {
                                elementId(Id.TUTOR_LEVEL)
                                onChange { e ->
                                    TutorLevel.entries
                                        .firstOrNull { it.key == e.value }
                                        ?.let {
                                            level = it
                                            changed()
                                        }
                                }
                            }
                        ) {
                            TutorLevel.entries.forEach { choice ->
                                Option(
                                    value = choice.key,
                                    attrs = { if (level == choice) selected() },
                                ) {
                                    org.jetbrains.compose.web.dom.Text(choice.label)
                                }
                            }
                        }
                    }
                    Div {
                        FieldLabel(forId = Id.TUTOR_MODE) { Text("Lernmodus") }
                        Select(
                            attrs = {
                                elementId(Id.TUTOR_MODE)
                                onChange { e ->
                                    TutorMode.entries
                                        .firstOrNull { it.key == e.value }
                                        ?.let {
                                            mode = it
                                            changed()
                                        }
                                }
                            }
                        ) {
                            TutorMode.entries.forEach { choice ->
                                Option(
                                    value = choice.key,
                                    attrs = { if (mode == choice) selected() },
                                ) {
                                    org.jetbrains.compose.web.dom.Text(choice.label)
                                }
                            }
                        }
                    }
                }
                FieldLabel(forId = Id.TUTOR_OWN_QUESTION) { Text("Deine Frage (optional)") }
                TextArea(
                    attrs = {
                        elementId(Id.TUTOR_OWN_QUESTION)
                        textRows(2)
                        maxLength(2000)
                        value(question)
                        onInput {
                            question = it.value.take(2000)
                            changed()
                        }
                    }
                )
                if (courseContext != null) {
                    Div(
                        attrs = {
                            elementId(Id.TUTOR_COURSE_SUMMARY)
                            styles(LabClass.NOTES)
                        }
                    ) {
                        P {
                            Text(
                                "Optional: aktuelle Station, Daten-/Quellenrevision, ausgewählte Modellparameter und native Messung mitgeben. Der vollständige Kontext steht unten. Keine anderen Antworten, Notizen oder Chatverläufe."
                            )
                        }
                        Button(
                            attrs = {
                                elementId(Id.TUTOR_INCLUDE_COURSE)
                                styles(LabClass.SECONDARY)
                                ariaPressed(includeCourse)
                                onClick {
                                    includeCourse = !includeCourse
                                    changed()
                                }
                            }
                        ) {
                            Text(
                                if (includeCourse) "Kurskontext ausgewählt · abwählen"
                                else "Diesen Kurskontext mitgeben"
                            )
                        }
                        Pre(attrs = {
                            styles(LabClass.SMALL, LabClass.TUTOR_START_TEXT)
                            data(LabData.LITERAL_EXPORT, true)
                        }) {
                            org.jetbrains.compose.web.dom.Text(
                                tutorJson.encodeToString(courseContext)
                            )
                        }
                    }
                }
                P(
                    attrs = {
                        elementId(Id.TUTOR_CONTEXT_SUMMARY)
                        styles(LabClass.SMALL, LabClass.MUTED)
                    }
                ) {
                    Text(
                        when {
                            includeCourse && courseContext != null ->
                                "Mitgegeben: Thema, Niveau, Modus, deine Frage und der oben ausdrücklich ausgewählte Kurskontext. Messwerte bleiben native Pixel; Herkunft und Auswahlbegründung reisen mit."
                            courseContext != null ->
                                "Mitgegeben: Thema, Niveau, Modus und deine Frage. Kurskontext und Messung sind abgewählt."
                            topic == TutorTopic.DEBROGLIE ->
                                "Mitgegeben: aktuelles Werkstatt-Quizmodell ${Physics.decimal(quizVoltage.volts, 0)} → ${Physics.decimal(quizVoltage.volts * 4, 0)} V. Kein Export der anderen Laborparameter oder Gespräche."
                            else ->
                                "Mitgegeben: Thema, Niveau, Modus und deine Frage. Ohne ausgewählten Kurskontext werden keine eigenen Labormessungen exportiert."
                        }
                    )
                }
                Div(attrs = { styles(LabClass.WORKSHOP_ACTIONS) }) {
                    Button(
                        attrs = {
                            elementId(Id.TUTOR_COPY_START)
                            styles(LabClass.PRIMARY)
                            onClick {
                                val epoch = ++copyEpoch
                                copied = null
                                copyFailed = false
                                try {
                                    copyBrowserText(startText)
                                        .then {
                                            if (copyEpoch == epoch)
                                                copied =
                                                    "Starttext mit Wissen und Arbeitskontext kopiert. Jetzt in deinen eigenen Chat einfügen."
                                        }
                                        .catch {
                                            if (copyEpoch == epoch) {
                                                copied =
                                                    "Kopieren ist hier nicht verfügbar. Nutze den sichtbaren Text oder den Download."
                                                copyFailed = true
                                            }
                                        }
                                } catch (_: Exception) {
                                    copied =
                                        "Kopieren ist hier nicht verfügbar. Nutze den sichtbaren Text oder den Download."
                                    copyFailed = true
                                }
                            }
                        }
                    ) {
                        Text("Tutor-Starttext kopieren")
                    }
                    A(
                        href = "data:text/markdown;charset=utf-8,${encodeTutorText(startText)}",
                        attrs = {
                            elementId(Id.TUTOR_DOWNLOAD_START)
                            styles(LabClass.SECONDARY)
                            downloadFile("materiewellen-tutor-start.md")
                        },
                    ) {
                        Text("Starttext herunterladen")
                    }
                    A(
                        href = LabAsset.TUTOR_SKILL.path,
                        attrs = {
                            elementId(Id.TUTOR_DOWNLOAD_SKILL)
                            styles(LabClass.SECONDARY)
                            downloadFile("materiewellen-tutor.zip")
                        },
                    ) {
                        Text("Skill-ZIP")
                    }
                    A(
                        href =
                            "data:application/json;charset=utf-8,${encodeTutorText(contextText)}",
                        attrs = {
                            elementId(Id.TUTOR_DOWNLOAD_CONTEXT)
                            styles(LabClass.SECONDARY)
                            downloadFile("materiewellen-arbeitskontext.json")
                        },
                    ) {
                        Text("Arbeitskontext")
                    }
                }
                copied?.let { message ->
                    P(
                        attrs = {
                            elementId(Id.TUTOR_COPY_STATUS)
                            styles(
                                if (copyFailed) LabClass.SOURCE_WARNING else LabClass.MODEL_LIMIT
                            )
                            role(AriaRole.STATUS)
                        }
                    ) {
                        Text(message)
                    }
                }
                Details(
                    attrs = {
                        elementId(Id.TUTOR_START_PREVIEW)
                        styles(LabClass.NOTES)
                        if (copyFailed) expanded()
                    }
                ) {
                    Summary() { Text("Vollständigen Starttext ansehen / manuell kopieren") }
                    FieldLabel(forId = Id.TUTOR_START_TEXT) {
                        Text("Starttext einschließlich Wissen und Arbeitskontext")
                    }
                    TextArea(
                        attrs = {
                            elementId(Id.TUTOR_START_TEXT)
                            textRows(10)
                            readOnly()
                            data(LabData.LITERAL_EXPORT, true)
                            value(startText)
                            styles(LabClass.TUTOR_START_TEXT)
                        }
                    )
                }
                P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
                    Text(
                        "Einfügen funktioniert ohne Skill-Funktion. Das ZIP ist für Clients mit Agent-Skills-Unterstützung; ein normaler Chat installiert daraus keinen Skill automatisch. "
                    )
                    A(href = "https://chatgpt.com/", attrs = { externalLink() }) {
                        Text("ChatGPT öffnen")
                    }
                    Text(" · ")
                    A(href = "https://claude.ai/", attrs = { externalLink() }) {
                        Text("Claude öffnen")
                    }
                }
                P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
                    Text(
                        "Die Website ruft keinen KI-Anbieter auf. Du entscheidest, was du in deinen Chat einfügst. Paket und Prüffälle sind kontrollierte Grundlagen; das Antwortverhalten deines Modells ist hier nicht getestet."
                    )
                }
            }
        }
        if (active) sessionSnapshot?.let { ReflectionExport(it) }
    }
}

@JsName("encodeURIComponent") private external fun encodeTutorText(text: String): String
