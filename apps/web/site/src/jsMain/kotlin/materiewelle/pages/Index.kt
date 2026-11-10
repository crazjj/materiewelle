package materiewelle.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.core.Page
import kotlinx.browser.document
import kotlinx.browser.window
import materiewelle.core.*
import materiewelle.core.course.*
import materiewelle.course.*
import materiewelle.data.*
import materiewelle.learning.*
import materiewelle.state.LabSession
import materiewelle.state.SessionStorage
import materiewelle.state.WriteAccess
import materiewelle.state.NavigationController
import materiewelle.state.PositionNavigation
import materiewelle.state.ReadingPositionStore
import materiewelle.state.UiDraftStorage
import materiewelle.state.UiDraftSnapshot
import materiewelle.ui.*
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.events.Event

@Page
@Composable
fun IndexPage() {
    val writer = remember { BrowserSessionWriter() }
    DisposableEffect(writer) {
        writer.start()
        onDispose { writer.close() }
    }
    LabMotion {
        TourProvider {
            if (writer.access == WriteAccess.INITIALIZING) {
                Main { P(attrs = { role(AriaRole.STATUS) }) { Text("Lernpfad wird geladen …") } }
            } else LaboratoryPage(writer)
        }
    }
}

@Composable
private fun LaboratoryPage(writer: BrowserSessionWriter) {
    val session = remember { LabSession(SessionStorage(writePermit = writer)) }
    val navigation = remember { NavigationController(window.location.hash) }
    val route = navigation.route.explanationRoute()
    val unknownRoute = navigation.unknown
    var readingPosition by remember { mutableStateOf(ReadingPositionStore.load()) }
    var lastSection by remember { mutableStateOf(readingPosition?.section ?: AppSection.DE_BROGLIE) }
    val section = if (route == AppRoute.Tutor) lastSection else AppSection.forRoute(route)
    val draftStorage = remember { UiDraftStorage(writePermit = writer) }
    val initialDrafts = remember { draftStorage.restore() }
    val jonssonView = remember { JonssonUiState(initialDrafts.jonsson) }
    val deBroglieView = remember { DeBroglieUiState(initialDrafts.derivationStep) }
    val tutorView = remember { CompactTutorUiState(initialDrafts.questions, jonssonView.exploration) }
    val dgView = remember { DgLessonView(initialDrafts.dg) }
    val viewDrafts = UiDraftSnapshot(questions = tutorView.drafts(), jonsson = jonssonView.draft(),
        derivationStep = deBroglieView.visibleStep.value, followDerivation = false, dg = dgView.draft())
    SideEffect { if (session.uiPersistenceAllowed) draftStorage.save(viewDrafts) }
    var dataState by remember { mutableStateOf<LoadState<ScientificData>>(LoadState.Loading) }
    var attempt by remember { mutableStateOf(0) }
    var profilesState by remember { mutableStateOf<LoadState<List<MeasurementProfile>>>(LoadState.Loading) }
    val tour = LocalTour.current
    DisposableEffect(attempt) {
        var mounted = true
        ScientificRepository.load { loaded ->
            if(mounted) {
                if(loaded is LoadState.Ready) session.adoptScientificData(loaded.value)
                dataState = loaded
            }
        }
        onDispose { mounted = false }
    }
    DisposableEffect(Unit) {
        val listener: (Event) -> Unit = {
            if (navigation.acceptHash(window.location.hash)) {
                tour.close(); tutorView.cancelNavigationFocus()
            }
        }
        window.addEventListener(BrowserEvent.HASH_CHANGE.eventName, listener)
        onDispose { window.removeEventListener(BrowserEvent.HASH_CHANGE.eventName, listener) }
    }
    DisposableEffect(dataState) {
        var mounted = true
        val image = (dataState as? LoadState.Ready)?.value?.jonsson
        if (image != null) MeasurementProfileRepository.load(image) { if (mounted) profilesState = it }
        else profilesState = if (dataState is LoadState.Failed) LoadState.Failed("Bilddaten nicht verfügbar.") else LoadState.Loading
        onDispose { mounted = false }
    }
    if (route == AppRoute.Overview || TypesetResources.state is LoadState.Ready) PositionNavigation(navigation, dataState)
    LaunchedEffect(navigation.request) {
        if (!unknownRoute && route != AppRoute.Tutor && route != AppRoute.Overview) {
            lastSection = section
            if (navigation.request > 0 || (window.location.hash.isNotBlank() && route != AppRoute.Overview)) {
                ReadingPosition.fromRoute(route)?.let { position ->
                    readingPosition = position
                    if (session.uiPersistenceAllowed) ReadingPositionStore.save(position)
                }
            }
        }
    }
    SideEffect { document.title = if (route == AppRoute.Overview) "Materiewellen · Übersicht" else "${section.title} · Materiewellen" }
    fun choose(next: AppRoute) {
        tour.close(); tutorView.cancelNavigationFocus(); navigation.choose(next)
    }
    fun openOwnWork(next: AppRoute, target: ElementId) {
        tour.close(); tutorView.cancelNavigationFocus(); navigation.choose(next, target)
    }
    A(href = Id.LABORATORY.fragment, attrs = {
        styles(LabClass.SKIP_LINK)
        onClick { it.preventDefault(); document.findHtmlElement(Id.LABORATORY)?.let { main -> main.focus(); main.scrollIntoView() } }
    }) { Text("Zum Hauptinhalt springen") }
    Header(attrs = { elementId(ShellId.HEADER); styles(LabClass.SITE_HEADER) }) {
        A(href = AppRoute.Overview.hash, attrs = { styles(LabClass.BRAND); onClick { it.preventDefault(); choose(AppRoute.Overview) } }) {
            Span(attrs = { styles(LabClass.BRAND_MARK); ariaHidden(true) }) { Text("∿") }; Text("MATERIEWELLE")
        }
        Nav(attrs = { ariaLabel("Abschnitte") }) {
            AppSection.entries.forEach { chapter ->
                val id = when (chapter) {
                    AppSection.DE_BROGLIE -> ElementId.Navigation(LabDestination.DE_BROGLIE)
                    AppSection.DAVISSON_GERMER -> ElementId.Navigation(LabDestination.DAVISSON_GERMER)
                    AppSection.JONSSON -> ElementId.Navigation(LabDestination.JONSSON)
                    AppSection.CONCLUSION -> Id.COURSE_NAV
                }
                ExperimentButton(chapter.title, route != AppRoute.Overview && section == chapter, id) { choose(chapter.route) }
            }
        }
        Div(attrs = { motionStyles(MotionClass.TOOLS) }) { MotionChooser(); ThemeChooser(); ProjectRepositoryLink() }
    }
    Main(attrs = { styles(LabClass.MAIN); elementId(Id.LABORATORY); focusIndex(-1) }) {
        SessionStatus(session, tutorView.drafts(), draftStorage, viewDrafts, jonssonView.exploration.readableNotes(), data = (dataState as? LoadState.Ready)?.value)
        if (unknownRoute) P(attrs = { styles(LabClass.SOURCE_WARNING); role(AriaRole.STATUS) }) {
            Text("Dieser Abschnitt wurde nicht gefunden. Wähle oben ein Kapitel.")
        }
        if (route == AppRoute.Overview) {
            Overview(readingPosition, lastSection, ::choose)
        } else SessionEditingBoundary(session) {
            TypesetGate {
                if (route != AppRoute.Tutor) when(section) {
                    AppSection.DE_BROGLIE -> DeBroglieArticle(session, ::choose, deBroglieView, tutorView)
                    AppSection.DAVISSON_GERMER -> when(val loaded = dataState) {
                        LoadState.Loading -> LoadingData()
                        is LoadState.Failed -> FailedData(loaded.message) { attempt++ }
                        is LoadState.Ready -> DavissonGermerArticle(session, loaded.value, ::choose, tutorView, route, dgView)
                    }
                    AppSection.JONSSON -> when(val loaded = dataState) {
                        LoadState.Loading -> LoadingData()
                        is LoadState.Failed -> FailedData(loaded.message) { attempt++ }
                        is LoadState.Ready -> JonssonArticle(session, loaded.value, profilesState, jonssonView, ::choose, tutorView)
                    }
                    AppSection.CONCLUSION -> ConclusionArticle(session, (dataState as? LoadState.Ready)?.value, tutorView, ::openOwnWork)
                }
                Section(attrs = { articleStyles(ArticleClass.ROOT) }) {
                    CompactTutor(session, section, initiallyOpen = route == AppRoute.Tutor, viewState = tutorView,
                        onReturn = if (route == AppRoute.Tutor) ({ choose(section.route) }) else null)
                }
            }
        }
        SessionTools(session, tutorView.drafts(), draftStorage, viewDrafts, jonssonView.exploration.readableNotes(),
            onResetNotes = jonssonView.exploration::clearNotes, data = (dataState as? LoadState.Ready)?.value)
    }
    Footer(attrs = { styles(LabClass.FOOTER) }) {
        Span { Text("Materiewellen · Physik-LK") }
        Span {
            A(href = AppRoute.DocumentAt(DocumentSectionId.SOURCES).hash, attrs = { onClick { it.preventDefault(); choose(AppRoute.DocumentAt(DocumentSectionId.SOURCES)) } }) { Text("Quellen und Arbeitsweise") }
        }
    }
}

@Composable
private fun LoadingData() { P(attrs = { styles(LabClass.LOADING); role(AriaRole.STATUS); data(LabData.LOAD_STATE, DataLoadState.LOADING) }) { Text("Versuchsdaten werden geladen …") } }
@Composable
private fun FailedData(message: String, retry: () -> Unit) {
    Div(attrs = { styles(LabClass.ERROR); role(AriaRole.ALERT); data(LabData.LOAD_STATE, DataLoadState.FAILED) }) {
        P { Text(message) }; Button(attrs = { styles(LabClass.SECONDARY); onClick { retry() } }) { Text("Erneut laden") }
    }
}
