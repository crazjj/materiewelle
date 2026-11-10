package materiewelle.learning

import androidx.compose.runtime.*
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import materiewelle.core.BrowserDocument
import materiewelle.core.LoadState
import materiewelle.core.course.AppRoute
import materiewelle.core.course.DocumentSectionId
import materiewelle.data.LabAsset
import materiewelle.presentation.styles.*
import materiewelle.ui.MathText as Text
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

@Composable
fun TypstExample(
    active: Boolean,
    requestedSection: DocumentSectionId? = null,
    navigate: (AppRoute) -> Unit = {},
) {
    var state by remember { mutableStateOf<LoadState<BrowserDocument>>(LoadState.Loading) }
    var attempt by remember { mutableStateOf(0) }
    var page by remember { mutableStateOf(0) }
    var zoom by remember { mutableStateOf(1.0) }
    var imageFailed by remember { mutableStateOf(false) }
    LaunchedEffect(state, requestedSection) {
        val document = (state as? LoadState.Ready)?.value
        if (document != null && requestedSection != null) {
            document.sections
                .firstOrNull { it.id == requestedSection }
                ?.let {
                    page = it.pageIndex
                    imageFailed = false
                }
        }
    }
    DisposableEffect(active, attempt) {
        var mounted = true
        if (active && state !is LoadState.Ready) {
            window
                .fetch(LabAsset.BROWSER_DOCUMENT.path)
                .then { r ->
                    if (!r.ok) error("Dokument nicht verfügbar")
                    r.text()
                }
                .then { text ->
                    val value = Json.decodeFromString<BrowserDocument>(text).validate()
                    if (mounted) state = LoadState.Ready(value)
                }
                .catch {
                    if (mounted)
                        state =
                            LoadState.Failed(
                                "Das Typst-Dokument konnte nicht geladen werden. Bitte erneut versuchen."
                            )
                }
        }
        onDispose { mounted = false }
    }
    H2 { Text("Fachfassung lesen") }
    P(attrs = { styles(LabClass.SUBTITLE) }) {
        Text(
            "Herleitung, beide Auswertungen, Quellen und Messprotokoll als gesetzte Vektorseiten. Die Textalternative ist für Vorleseprogramme und als Download verfügbar."
        )
    }
    when (val loaded = state) {
        LoadState.Loading ->
            P(attrs = { role(AriaRole.STATUS) }) { Text("Dokument wird geladen …") }
        is LoadState.Failed ->
            Div(
                attrs = {
                    styles(LabClass.ERROR)
                    elementId(Id.TYPST_LOAD_ERROR)
                    role(AriaRole.ALERT)
                }
            ) {
                P { Text(loaded.message) }
                Button(
                    attrs = {
                        styles(LabClass.SECONDARY)
                        onClick {
                            state = LoadState.Loading
                            attempt++
                        }
                    }
                ) {
                    Text("Dokument erneut laden")
                }
            }
        is LoadState.Ready -> {
            val doc = loaded.value
            val currentSection = doc.sections.lastOrNull { it.pageIndex <= page }
            Div(attrs = { styles(LabClass.DOCUMENT_TOOLBAR, LabClass.COURSE_ACTIONS) }) {
                FieldLabel(Id.TYPST_SECTION_SELECT) { Text("Fachabschnitt auswählen") }
                Select(
                    attrs = {
                        elementId(Id.TYPST_SECTION_SELECT)
                        onChange { event ->
                            DocumentSectionId.fromKey(event.value ?: "")?.let {
                                navigate(AppRoute.DocumentAt(it))
                            }
                        }
                    }
                ) {
                    doc.sections.forEach { section ->
                        Option(
                            value = section.id.key,
                            attrs = { if (currentSection?.id == section.id) selected() },
                        ) {
                            org.jetbrains.compose.web.dom.Text(
                                "${section.id.label} · S. ${section.pageIndex + 1}"
                            )
                        }
                    }
                }
                currentSection?.id?.station?.let { station ->
                    A(
                        href = AppRoute.Course(station).hash,
                        attrs = {
                            elementId(Id.TYPST_RETURN_STATION)
                            styles(LabClass.SECONDARY)
                            onClick {
                                it.preventDefault()
                                navigate(AppRoute.Course(station))
                            }
                        },
                    ) {
                        Text("Interaktiv erkunden · Station ${station.number} ↗")
                    }
                }
            }
            currentSection?.let { section ->
                P(
                    attrs = {
                        elementId(Id.TYPST_SECTION_STATUS)
                        data(LabData.DOCUMENT_SECTION, section.id)
                        styles(LabClass.SMALL, LabClass.MUTED)
                        ariaLive(LiveMode.POLITE)
                    }
                ) {
                    Text("${section.title} · Seitenzuordnung aus der aktuellen Typst-Kompilation.")
                }
                if (
                    section.id == DocumentSectionId.JONSSON_MEASUREMENT ||
                        section.id == DocumentSectionId.PROTOCOL
                ) {
                    P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
                        Text(
                            "Dieser Abschnitt enthält dokumentierte Referenzmarken. Für eine unbeeinflusste eigene Erstwahl zuerst in der Bildmessung arbeiten. Die Website protokolliert den Zugang zur Fachfassung als möglichen Einfluss. Sie kann nicht feststellen, welche Inhalte du tatsächlich gelesen hast oder schon von anderswo kennst."
                        )
                    }
                }
            }
            Div(attrs = { styles(LabClass.WORKSHOP_ACTIONS, LabClass.DOCUMENT_TOOLBAR) }) {
                Button(
                    attrs = {
                        elementId(Id.TYPST_PREV)
                        styles(LabClass.SECONDARY)
                        if (page == 0) disabled()
                        onClick {
                            page = (page - 1).coerceAtLeast(0)
                            imageFailed = false
                        }
                    }
                ) {
                    Text("Vorherige Seite")
                }
                Span(
                    attrs = {
                        elementId(Id.TYPST_PAGE_STATUS)
                        ariaLive(LiveMode.POLITE)
                    }
                ) {
                    Text("Seite ${page + 1} von ${doc.pages.size}")
                }
                Button(
                    attrs = {
                        elementId(Id.TYPST_NEXT)
                        styles(LabClass.SECONDARY)
                        if (page == doc.pages.lastIndex) disabled()
                        onClick {
                            page = (page + 1).coerceAtMost(doc.pages.lastIndex)
                            imageFailed = false
                        }
                    }
                ) {
                    Text("Nächste Seite")
                }
                Button(
                    attrs = {
                        elementId(Id.TYPST_ZOOM)
                        styles(LabClass.SECONDARY)
                        ariaPressed((zoom > 1))
                        onClick { zoom = if (zoom == 1.0) 1.5 else 1.0 }
                    }
                ) {
                    Text(if (zoom == 1.0) "150 %" else "Einpassen")
                }
                A(
                    href = "${doc.pdf}#page=${page + 1}",
                    attrs = {
                        elementId(Id.TYPST_PDF)
                        styles(LabClass.SECONDARY)
                        externalLink()
                    },
                ) {
                    Text("PDF auf dieser Seite öffnen")
                }
                A(
                    href = LabAsset.PRESENTATION_PDF.path,
                    attrs = {
                        elementId(Id.PRESENTATION_PDF)
                        styles(LabClass.SECONDARY)
                        externalLink()
                    },
                ) {
                    Text("Präsentation als PDF")
                }
            }
            if (imageFailed)
                P(
                    attrs = {
                        styles(LabClass.ERROR)
                        role(AriaRole.ALERT)
                    }
                ) {
                    Text(
                        "Die Vektorseite fehlt. Du kannst das PDF oder die Textalternative nutzen."
                    )
                }
            Div(
                attrs = {
                    styles(LabClass.DOCUMENT_VIEWPORT)
                    elementId(Id.TYPST_VIEWPORT)
                    focusIndex(0)
                    ariaLabel("Dokumentansicht, bei Vergrößerung horizontal scrollbar")
                }
            ) {
                Img(
                    src = doc.pages[page],
                    alt =
                        "${doc.title}, Seite ${page + 1}. Eine ausführliche Textalternative folgt unter dem Dokument.",
                    attrs = {
                        elementId(Id.TYPST_VECTOR_PAGE)
                        style {
                            width((zoom * 100).percent)
                            setMaxWidth(MaxWidthValue.NONE)
                        }
                        ref { element ->
                            val listener: (org.w3c.dom.events.Event) -> Unit = {
                                imageFailed = true
                            }
                            element.addEventListener(BrowserEvent.ERROR.eventName, listener)
                            onDispose {
                                element.removeEventListener(BrowserEvent.ERROR.eventName, listener)
                            }
                        }
                    },
                )
            }
            Details(
                attrs = {
                    elementId(CourseControl.CURRENT_PAGE_TEXT)
                    styles(LabClass.NOTES)
                }
            ) {
                Summary() { Text("Text zur aktuellen Seite ${page + 1}") }
                val pageText =
                    doc.pageText.getOrNull(page)?.ifBlank {
                        "Auf dieser Seite ist kein Text extrahierbar; nutze die gesetzte Darstellung und ihre Beschriftung."
                    }
                        ?: "Die seitenweise Textalternative fehlt in diesem älteren Dokument. Die vollständige Alternative bleibt unten erreichbar."
                P {
                    Text(
                        "Der Text folgt für Vorleseprogramme. Als Datei lässt er sich kopieren und außerhalb der Vektoransicht lesen."
                    )
                }
                Button(
                    attrs = {
                        elementId(Id.TYPST_PAGE_TEXT_DOWNLOAD)
                        styles(LabClass.SECONDARY)
                        onClick { downloadText(pageText, "materiewelle-seite-${page + 1}.txt") }
                    }
                ) {
                    Text("Seitentext herunterladen")
                }
                P(attrs = { typesetReading() }) { org.jetbrains.compose.web.dom.Text(pageText) }
            }
            Details(attrs = { styles(LabClass.NOTES) }) {
                Summary() { Text("Technische Dokumentinformationen") }
                P {
                    Text(
                        "${doc.compiler} · Vektorseiten aus der aktuellen Fachfassung · Papierfarben bleiben für die Druckfassung erhalten."
                    )
                }
            }
            Details(
                attrs = {
                    styles(LabClass.NOTES)
                    elementId(Id.TYPST_TEXT_ALTERNATIVE)
                }
            ) {
                Summary() { Text("Vollständige Textalternative aus derselben PDF") }
                P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
                    Text(
                        "Direkt aus derselben PDF extrahiert, für Vorleseprogramme und zum Herunterladen. Die mathematische Anordnung bleibt in der Vektorseite erhalten; die Extraktion ist eine Textalternative."
                    )
                }
                Button(
                    attrs = {
                        elementId(Id.TYPST_TEXT_DOWNLOAD)
                        styles(LabClass.SECONDARY)
                        onClick {
                            downloadText(doc.textAlternative, "materiewelle-fachfassung-text.txt")
                        }
                    }
                ) {
                    Text("Vollständigen Text herunterladen")
                }
                P(attrs = { typesetReading() }) {
                    org.jetbrains.compose.web.dom.Text(doc.textAlternative)
                }
            }
        }
    }
    WorkshopNotes(
        "Weitere Darstellungsoptionen",
        "PDF-Einbettung ist der kleinste Einstieg. PDF.js ergänzt Textauswahl, Suche und einen einheitlichen Viewer. Typst.ts ermöglicht WASM-Kompilation oder vorberechnete Vektorartefakte, benötigt aber Compiler-, Font- und Dateiverwaltung. Für diese statische Demo sind beim Build erzeugte SVG-Seiten zuverlässig und ohne zusätzlichen Browser-Compiler nutzbar.",
    )
}
