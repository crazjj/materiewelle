package materiewelle.learning

import androidx.compose.runtime.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import materiewelle.core.LoadState
import materiewelle.core.course.*
import materiewelle.core.SlitConfiguration
import materiewelle.course.graphics.SlitExplorationUiState
import materiewelle.course.graphics.SlitInvestigation
import materiewelle.state.LabSession
import materiewelle.ui.PlainTextCopyActions
import materiewelle.ui.PlainTextCopyPreview
import materiewelle.ui.rememberPlainTextCopyState
import materiewelle.ui.PlainTextCopyIds
import materiewelle.ui.MathText
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*

private enum class CompactTutorAsset(val path: String) {
    MANIFEST("tutor/manifest.json")
}

@Serializable
private data class CompactAsset(val path: String, val bytes: Int, val sha256: String)

@Serializable
private data class CompactScope(val section: String, val title: String, val startFile: String, val wordCount: Int)

@Serializable
private data class CompactManifest(
    val schemaVersion: String, val version: String, val skillName: String,
    val knowledgeDate: String, val sourceRevision: String,
    val scopes: List<CompactScope>, val files: List<CompactAsset>,
) {
    fun validate(): CompactManifest {
        require(schemaVersion == "3.0" && skillName == "materiewellen-fachtutor")
        require(Regex("[0-9]+\\.[0-9]+\\.[0-9]+").matches(version))
        require(Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}").matches(knowledgeDate))
        require(Regex("[A-Za-z0-9][A-Za-z0-9._/-]{0,63}").matches(sourceRevision))
        require(scopes.size == AppSection.entries.size && scopes.map { it.section }.toSet() == AppSection.entries.map { it.key }.toSet())
        require(scopes.all { it.title.isNotBlank() && it.title.length <= 80 && it.wordCount in 1..1200 && it.startFile == "START-${it.section}.md" })
        val paths = scopes.map { it.startFile }.toSet() + setOf("START-IM-CHAT.md", "materiewellen-fachtutor.zip")
        require(files.size == paths.size && files.map { it.path }.toSet() == paths)
        require(files.all { it.bytes in 1..200_000 && Regex("[a-f0-9]{64}").matches(it.sha256) })
        return this
    }
    fun scope(section: AppSection) = scopes.single { it.section == section.key }
}

private data class CompactPackage(val text: String)

private data class TutorTransfer(
    val text: String,
    val label: String,
    val previewLabel: String,
    val successMessage: String,
    val primary: Boolean,
)

private val tutorCopyIds = PlainTextCopyIds(
    CompactTutorId.COPY, CompactTutorId.STATUS, CompactTutorId.PREVIEW, CompactTutorId.SELECT,
)

private val compactJson = Json { encodeDefaults = true; prettyPrint = true; ignoreUnknownKeys = false; classDiscriminator = "kind" }

/** Placement only affects reading/focus; the question and scientific handoff have one owner. */
enum class TutorPanelPlacement { INLINE, BELOW_WORKSPACE }

/** View state can live beside the article owner; drafts never enter the scientific session. */
class CompactTutorUiState(initialQuestions: Map<AppSection, String> = emptyMap(), private val slit: SlitExplorationUiState = SlitExplorationUiState()) {
    internal var open by mutableStateOf(false)
    internal val questions = mutableStateMapOf<AppSection, String>().apply { putAll(initialQuestions) }
    internal val intents = mutableStateMapOf<AppSection, TutorIntent>()
    internal var focusRequest by mutableStateOf(0)
    private var origin: TutorHelpPoint? = null
    private var focusSection: AppSection? = null
    private var handledFocus = 0
    internal var returnTop: Double? = null; private set

    fun drafts(): Map<AppSection, String> = questions.toMap()

    internal fun contextExtras(): CompactTutorExtras {
        val draft = slit.draft()
        fun task(value: SlitInvestigation) = when (value) {
            SlitInvestigation.SPACING -> CompactSlitTask.SPACING
            SlitInvestigation.WIDTH -> CompactSlitTask.WIDTH
        }
        return CompactTutorExtras(slitExploration = CompactSlitExploration(
            configuration = when (draft.configuration) {
                SlitConfiguration.BOTH_COHERENT -> CompactSlitConfiguration.BOTH_COHERENT
                SlitConfiguration.LEFT_ONLY -> CompactSlitConfiguration.LEFT_ONLY
                SlitConfiguration.RIGHT_ONLY -> CompactSlitConfiguration.RIGHT_ONLY
                SlitConfiguration.BOTH_INCOHERENT -> CompactSlitConfiguration.BOTH_INCOHERENT
            },
            task = task(draft.task),
            notes = draft.notes.mapKeys { task(it.key) }.mapValues { CompactSlitNotes(it.value.prediction, it.value.reason) },
            observationMeters = slit.position.meters,
        ))
    }

    fun openFrom(point: TutorHelpPoint, viewportTop: Double? = null) {
        origin = point
        returnTop = viewportTop
        focusSection = point.section
        open = true
        focusRequest++
    }

    internal fun openGeneral(section: AppSection) {
        origin = null
        focusSection = section
        open = true
        focusRequest++
    }

    internal fun needsFocus(section: AppSection) = focusSection == section && handledFocus != focusRequest
    internal fun focusHandled() { handledFocus = focusRequest }

    fun cancelNavigationFocus() {
        origin = null
        open = false
        handledFocus = focusRequest
    }

    internal fun originFor(section: AppSection): TutorHelpPoint? = origin?.takeIf { it.section == section }

    internal fun closeFor(section: AppSection): ElementId {
        val target = originFor(section)?.entry ?: CompactTutorId.OPEN
        open = false
        origin = null
        return target
    }
}

internal fun closeCompactTutor(viewState: CompactTutorUiState, section: AppSection) {
    val local = viewState.originFor(section) != null
    val previousTop = viewState.returnTop
    val target = viewState.closeFor(section)
    if (local) requestInlineTutorFocus(target, previousTop) else requestTutorFocus(target)
}

/** Scoped, user-initiated handoff. Never mounts the legacy workshop or calls a provider. */
@Composable
fun CompactTutor(session: LabSession, section: AppSection = AppSection.DE_BROGLIE,
    initiallyOpen: Boolean = false, viewState: CompactTutorUiState = remember { CompactTutorUiState() },
    onReturn: (() -> Unit)? = null) {
    val open = viewState.open && viewState.originFor(section) == null
    LaunchedEffect(initiallyOpen) { if (initiallyOpen) viewState.openGeneral(section) }
    fun close() {
        if (onReturn != null) { viewState.cancelNavigationFocus(); onReturn() }
        else closeCompactTutor(viewState, section)
    }
    Section {
        if (!initiallyOpen) Button(attrs = {
            elementId(CompactTutorId.OPEN); styles(LabClass.SECONDARY)
            ariaPressed(open); ariaExpanded(open); controls(CompactTutorId.PANEL)
            onClick { if (open) close() else viewState.openGeneral(section) }
        }) { Text(if (open) "Hilfe schließen" else "Hilfe mit deinem KI-Chat") }
        if (open) key(section) {
            CompactTutorPanel(session, section, viewState.questions[section].orEmpty(),
                { viewState.questions[section] = it }, viewState,
                viewState.originFor(section)?.returnLabel ?: "Zurück zum Kapitel", ::close)
        }
    }
}

@Composable
internal fun CompactTutorPanel(session: LabSession, section: AppSection, question: String, onQuestion: (String) -> Unit, viewState: CompactTutorUiState, closeLabel: String, close: () -> Unit, placement: TutorPanelPlacement = TutorPanelPlacement.INLINE) {
    var state by remember { mutableStateOf<LoadState<CompactPackage>>(LoadState.Loading) }
    var attempt by remember { mutableStateOf(0) }
    val snapshot = session.snapshot
    val point = viewState.originFor(section)
    val extras = viewState.contextExtras()
    var includeEarlier by remember(snapshot, extras, point) { mutableStateOf(false) }
    val own = compactOwnWork(snapshot, section, point, extras, includeEarlier)
    // Consent belongs to the rendered context. Reset it during recomposition,
    // before a new checkbox interaction, rather than in a delayed effect.
    var selectedWork by remember(snapshot, extras, section, point) { mutableStateOf<CompactOwnWork?>(null) }
    val includeOwn = selectedWork == own
    val intent = viewState.intents[section] ?: TutorIntent.EXPLANATION
    val examples = point?.examples ?: TutorHelpPoint.chapterExamples(section)
    DisposableEffect(attempt, section) {
        var mounted = true
        if (!hasBrowserDigest()) {
            state = LoadState.Failed("Die Website kann die Hilfetexte in diesem Browser nicht überprüfen. Öffne sie über HTTPS in einem aktuellen Browser und versuche es erneut.")
        } else {
            var metadata: CompactManifest? = null
            window.fetch(CompactTutorAsset.MANIFEST.path).then { response ->
                require(response.ok); response.text()
            }.then { text ->
                metadata = compactJson.decodeFromString<CompactManifest>(text).validate()
                val startPath = "tutor/${requireNotNull(metadata).scope(section).startFile}"
                window.fetch(startPath)
            }.then { response ->
                require(response.ok); response.text()
            }.then { text ->
                val meta = requireNotNull(metadata)
                val source = meta.files.single { it.path == meta.scope(section).startFile }
                require(text.encodeToByteArray().size == source.bytes)
                browserTextDigest(text).then { hash ->
                    require(hash == source.sha256)
                    if (mounted) {
                        state = LoadState.Ready(CompactPackage(text))
                    }
                }
            }.catch {
                if (mounted) state = LoadState.Failed("Die Hilfe ist gerade nicht verfügbar. Versuche es noch einmal.")
            }
        }
        onDispose { mounted = false }
    }
    // The question is available before its supporting guide loads. Focus it once
    // for the opening gesture; a later network response must never steal focus
    // from a field the learner has since chosen or redirect their typed text.
    DisposableEffect(viewState.focusRequest, placement) {
        val cancel = if (viewState.needsFocus(section)) {
            viewState.focusHandled()
            if (viewState.originFor(section) != null && placement == TutorPanelPlacement.INLINE)
                requestInlineTutorFocus(CompactTutorId.QUESTION, panel = CompactTutorId.PANEL)
            else requestTutorFocus(CompactTutorId.QUESTION, panel = CompactTutorId.PANEL)
        } else ({})
        onDispose { cancel() }
    }
    val transfer = when (val loaded = state) {
        is LoadState.Ready -> TutorTransfer(
            compactTutorMessage(section, question, loaded.value.text, own.takeIf { includeOwn }, intent, point),
            "Nachricht kopieren", "Vollständige Nachricht ansehen",
            "Nachricht kopiert. Öffne jetzt deinen KI-Chat, füge den ganzen Text ein und sende ihn ab.",
            primary = true,
        )
        is LoadState.Failed -> question.takeIf { it.isNotBlank() }?.let {
            TutorTransfer(it, "Nur meine Frage kopieren", "Frage ansehen",
                "Deine Frage ist kopiert. Aufgabe, Fachwissen und ausgewählte Arbeitsangaben fehlen in dieser Kopie.", primary = false)
        }
        LoadState.Loading -> null
    }
    val copyState = rememberPlainTextCopyState(transfer?.text.orEmpty())
    Div(attrs = {
        elementId(CompactTutorId.PANEL); styles(LabClass.NOTES); focusIndex(-1)
        if (placement == TutorPanelPlacement.BELOW_WORKSPACE) compactTutorPanelStyles(CompactTutorPanelClass.WIDE)
        role(AriaRole.REGION); labelledBy(CompactTutorId.TITLE)
        onKeyDown { event ->
            if (LabKey.fromBrowser(event.key) == LabKey.ESCAPE) {
                event.preventDefault(); event.stopPropagation(); close()
            }
        }
    }) {
        Div(attrs = { styles(LabClass.WORKSHOP_ACTIONS) }) {
            H3(attrs = { elementId(CompactTutorId.TITLE) }) { Text("Hilfe mit deinem KI-Chat") }
            Button(attrs = {
                elementId(CompactTutorId.CLOSE); styles(LabClass.SECONDARY)
                compactTutorPanelStyles(CompactTutorPanelClass.BACK)
                onClick { close() }
            }) { Text(closeLabel) }
        }
        P(attrs = { elementId(CompactTutorId.SCOPE); compactTutorPanelStyles(CompactTutorPanelClass.SCOPE) }) {
            Text(point?.heading ?: "${section.title} · Fragen zum Kapitel")
        }
        P(attrs = { compactTutorPanelStyles(CompactTutorPanelClass.INTRO) }) {
            Text("Bereite eine Nachricht zu dieser Aufgabe vor. Die Antwort bekommst du in deinem eigenen KI-Chat, zum Beispiel bei ChatGPT oder Claude.")
        }
        Div(attrs = { compactTutorPanelStyles(CompactTutorPanelClass.LAYOUT) }) {
          Div(attrs = { compactTutorPanelStyles(CompactTutorPanelClass.PREPARE) }) {
            P(attrs = { elementId(CompactTutorId.INTENT); compactTutorPanelStyles(CompactTutorPanelClass.STEP) }) { Text("1 · Welche Hilfe brauchst du?") }
            Div(attrs = { compactTutorPanelStyles(CompactTutorPanelClass.MODES); role(AriaRole.GROUP); labelledBy(CompactTutorId.INTENT) }) {
                TutorIntent.entries.forEach { choice ->
                    Button(attrs = {
                        elementId(choice.id); styles(LabClass.SECONDARY); ariaPressed(intent == choice)
                        onClick { viewState.intents[section] = choice }
                    }) { Text(choice.label) }
                }
            }
            FieldLabel(CompactTutorId.QUESTION) { Text("Deine Frage (optional)") }
            TextArea(attrs = {
                elementId(CompactTutorId.QUESTION); textRows(2); maxLength(TUTOR_QUESTION_LIMIT); value(question)
                describedBy(CompactTutorId.SCOPE)
                onInput { onQuestion(it.value) }
            })
            P(attrs = { styles(LabClass.SMALL) }) {
                Text("Ohne eigene Frage beginnt der Chat mit der gewählten Hilfe zum angezeigten Thema.")
            }
            if (session.isReadOnly && question.isNotBlank()) P(attrs = { styles(LabClass.SMALL) }) {
                Text("Diese Frage bleibt nur in diesem Tab. Kopiere sie vor dem Schließen.")
            }
            Details {
                Summary { Text("Eine Beispielfrage ergänzen") }
                Div(attrs = { elementId(CompactTutorId.EXAMPLES); styles(LabClass.WORKSHOP_ACTIONS) }) {
                    examples.forEach { example ->
                        val next = withTutorExample(question, example)
                        Button(attrs = {
                            elementId(example.id); styles(LabClass.SECONDARY)
                            if (next == null) disabled()
                            onClick {
                                if (next != null) { onQuestion(next); document.findHtmlElement(CompactTutorId.QUESTION)?.focusWithoutScrolling() }
                            }
                        }) { Text(example.question) }
                    }
                }
                if (examples.any { withTutorExample(question, it) == null }) P(attrs = { styles(LabClass.SMALL) }) {
                    Text("Kürze deinen Text, um noch eine Beispielfrage zu ergänzen.")
                }
            }
            Label(forId = CompactTutorId.OWN.value, attrs = { compactTutorPanelStyles(CompactTutorPanelClass.CONTEXT) }) {
                Input(InputType.Checkbox, attrs = {
                    elementId(CompactTutorId.OWN); checked(includeOwn); describedBy(CompactTutorId.OWN_SUMMARY)
                    onChange {
                        selectedWork = if (it.value) own else null
                        if (!it.value) includeEarlier = false
                    }
                })
                Text(if (point == null) "Meine Angaben aus diesem Kapitel in die Nachricht aufnehmen" else "Meine Angaben zu dieser Aufgabe in die Nachricht aufnehmen")
            }
            if (section == AppSection.CONCLUSION && includeOwn) Label(forId = CompactTutorId.EARLIER.value, attrs = { compactTutorPanelStyles(CompactTutorPanelClass.CONTEXT) }) {
                Input(InputType.Checkbox, attrs = {
                    elementId(CompactTutorId.EARLIER); checked(includeEarlier)
                    onChange {
                        includeEarlier = it.value
                        selectedWork = compactOwnWork(snapshot, section, point, extras, includeEarlier)
                    }
                })
                Text("Auch meine Rechnungen und Messungen aus den drei Kapiteln")
            }
            val summary = tutorCopySummary(question, own, includeOwn)
            Div(attrs = { elementId(CompactTutorId.SUMMARY); compactTutorPanelStyles(CompactTutorPanelClass.CONTEXT_SUMMARY) }) {
                P(attrs = { elementId(CompactTutorId.OWN_SUMMARY); styles(LabClass.SMALL) }) {
                    Text(if (state is LoadState.Loading)
                        "Die Nachricht wird vorbereitet. Deine ausgewählten Angaben werden erst mit der vollständigen Nachricht kopiert."
                    else if (state is LoadState.Failed)
                        "Derzeit kann nur deine Frage kopiert werden. Aufgabe, Fachwissen und ausgewählte Arbeitsangaben fehlen in dieser Kopie."
                    else if (includeOwn) tutorOwnWorkSummary(own, summary)
                    else "Deine Frage, die Aufgabe und das Fachwissen stehen in der Nachricht. Weitere Angaben aus den Arbeitsfeldern werden nur aufgenommen, wenn du sie hier auswählst.")
                }
                if (includeOwn && state is LoadState.Ready) {
                    val details = tutorOwnWorkDetails(own)
                    Ul { details.take(4).forEach { value -> Li { MathText(value) } } }
                    if (details.size > 4) Details {
                        Summary { Text("Weitere enthaltene Angaben ansehen") }
                        Ul { details.drop(4).forEach { value -> Li { MathText(value) } } }
                    }
                }
                if (intent == TutorIntent.CHECK && !includeOwn && question.isBlank()) P(attrs = { styles(LabClass.SMALL) }) {
                    Text("Wähle deine Angaben aus oder beschreibe oben deinen Ansatz, damit der Chat ihn prüfen kann.")
                }
            }
          }
          Div(attrs = { compactTutorPanelStyles(CompactTutorPanelClass.TRANSFER) }) {
            P(attrs = { compactTutorPanelStyles(CompactTutorPanelClass.STEP) }) { Text("2 · Nachricht kopieren") }
            if (state is LoadState.Ready) P { Text("Kopiere die ganze Nachricht, einschließlich Aufgabe und Fachwissen.") }
            when (val loaded = state) {
                LoadState.Loading -> P(attrs = { role(AriaRole.STATUS) }) { Text("Hilfe wird vorbereitet …") }
                is LoadState.Failed -> Div(attrs = { elementId(CompactTutorId.ERROR); role(AriaRole.ALERT); styles(LabClass.ERROR) }) {
                    P { Text(loaded.message) }
                    Button(attrs = { elementId(CompactTutorId.RETRY); styles(LabClass.SECONDARY); onClick { state = LoadState.Loading; attempt++ } }) { Text("Erneut laden") }
                }
                is LoadState.Ready -> Unit
            }
            transfer?.let {
                PlainTextCopyActions(copyState, it.text, tutorCopyIds, it.label, it.successMessage, it.primary)
            }
            P(attrs = { compactTutorPanelStyles(CompactTutorPanelClass.STEP) }) { Text("3 · Im eigenen Chat weiterfragen") }
            P {
                A(href = "https://chatgpt.com/", attrs = { externalLink() }) { Text("ChatGPT öffnen") }
                Text(" · ")
                A(href = "https://claude.ai/", attrs = { externalLink() }) { Text("Claude öffnen") }
            }
            P { Text("Füge die Nachricht ein und sende sie ab. ${ (point ?: TutorHelpPoint.forSection(section)).nextAction }") }
            P(attrs = { elementId(CompactTutorId.HOW); styles(LabClass.SMALL) }) {
                Text("Die Website sendet nichts automatisch und erhält keine Chatantwort. Bei geänderten Angaben kopierst du eine neue Nachricht.")
            }
            if (section == AppSection.JONSSON && point != TutorHelpPoint.SLIT_MODEL) P(attrs = { styles(LabClass.SMALL) }) {
                Text("Wenn der Chat die Lage deiner Marken beurteilen soll, füge dort zusätzlich einen Screenshot von Bild, Marken und Profil ein. Das Bild wird nicht mitkopiert.")
            }
          }
        }
        transfer?.let {
            PlainTextCopyPreview(copyState, it.text, tutorCopyIds, it.previewLabel)
        }
    }
}
