package materiewelle.course

import androidx.compose.runtime.*
import kotlinx.browser.window
import materiewelle.core.course.AppSection
import materiewelle.core.course.SessionSnapshot
import materiewelle.core.ScientificData
import materiewelle.state.LabSession
import materiewelle.state.UiDraftStorage
import materiewelle.state.UiDraftSnapshot
import materiewelle.state.StorageHealth
import materiewelle.ui.PlainTextCopy
import materiewelle.ui.PlainTextCopyIds
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

private val workCopyIds = PlainTextCopyIds(SessionUiId.COPY, SessionUiId.COPY_STATUS,
    SessionUiId.PREVIEW, SessionUiId.SELECT)

/** Includes the live tab's answers and question drafts, without a restore-file format. */
internal fun readableSessionWork(
    session: LabSession,
    questions: Map<AppSection, String>,
    extraNotes: String = "",
    data: ScientificData? = null,
    includeTechnicalAppendix: Boolean = false,
): String = buildString {
    appendLine("Arbeitsstand · Stand dieses Tabs")
    appendLine("Diese Textkopie lässt sich lesen und weitergeben; sie wird nicht wieder in die Website eingelesen.")
    appendLine()
    appendLine(session.snapshot.reportForReading(data).readableText(includeTechnicalAppendix))
    val written = AppSection.entries.mapNotNull { section ->
        questions[section]?.takeIf { it.isNotBlank() }?.let { section.title to it }
    }
    if (written.isNotEmpty()) {
        appendLine()
        appendLine("Eigene Fragen für den KI-Chat")
        written.forEach { (title, question) -> appendLine(); appendLine(title); appendLine(question) }
    }
    if (extraNotes.isNotBlank()) {
        appendLine()
        appendLine(extraNotes)
    }
}

private fun draftNotice(session: LabSession, drafts: UiDraftStorage?) =
    drafts?.notice?.takeIf { it != session.storageMessage }

internal fun sessionRescueNeeded(session: LabSession, drafts: UiDraftStorage?, snapshot: UiDraftSnapshot?): Boolean {
    if (session.localDataDeleted) {
        // Successful deletion is intentional; subsequent in-memory work still needs a rescue path.
        val blank = SessionSnapshot(imageIdentity = session.snapshot.imageIdentity)
        val ownWork = session.snapshot.copy(station = blank.station, visited = emptySet()) != blank
        return ownWork || snapshot?.let { drafts?.hasUnsavedText(it) } == true
    }
    return (!session.isReadOnly && session.health != StorageHealth.SAVED) || drafts?.needsRescue == true
}

/** Only actual save failures interrupt reading. Kept outside TypesetGate for direct text rescue. */
@Composable
fun SessionStatus(
    session: LabSession,
    questions: Map<AppSection, String> = emptyMap(),
    draftStorage: UiDraftStorage? = null,
    draftSnapshot: UiDraftSnapshot? = null,
    extraNotes: String = "",
    data: ScientificData? = null,
) {
    if (!sessionRescueNeeded(session, draftStorage, draftSnapshot)) return
    val draftNotice = draftNotice(session, draftStorage)
    val message = when {
        session.localDataDeleted -> "Du hast nach dem Löschen neue Angaben gemacht. Sie sind noch nicht gespeichert. Kopiere sie, bevor du neu lädst; danach speichert die Website wieder automatisch."
        session.health == StorageHealth.SAVED || session.isReadOnly -> draftNotice ?: session.storageMessage
        else -> session.storageMessage
    }
    Div(attrs = { elementId(SessionUiId.ALERT); sessionUiStyles(SessionUiClass.ALERT) }) {
        P(attrs = {
            elementId(Id.COURSE_STORAGE_STATUS); role(AriaRole.STATUS)
            styles(LabClass.SOURCE_WARNING)
        }) { Text(message) }
        draftNotice?.takeIf { it != message }?.let { notice ->
            P(attrs = { styles(LabClass.SMALL) }) { Text(notice) }
        }
        SessionWorkCopy(session, questions, extraNotes, data)
        SessionReload(session, draftStorage, draftSnapshot)
    }
}

/** Background saving needs no banner. Management stays collapsed below the learning content. */
@Composable
fun SessionTools(
    session: LabSession,
    questions: Map<AppSection, String> = emptyMap(),
    draftStorage: UiDraftStorage? = null,
    draftSnapshot: UiDraftSnapshot? = null,
    extraNotes: String = "",
    onResetNotes: () -> Unit = {},
    data: ScientificData? = null,
) {
    val needsRescue = sessionRescueNeeded(session, draftStorage, draftSnapshot)
    Div(attrs = { elementId(SessionUiId.ROOT); sessionUiStyles(SessionUiClass.ROOT) }) {
        if (session.snapshot.measurementImageChanged) P(attrs = { styles(LabClass.SOURCE_WARNING) }) {
            Text("Die Bilddatei hat sich geändert oder ist nicht verfügbar. Frühere Marken wurden nicht auf ein anderes Bild übertragen; deine Notizen bleiben erhalten.")
        }
        Details {
            Summary { Text("Arbeit auf diesem Gerät") }
            if (!needsRescue) P(attrs = {
                elementId(Id.COURSE_STORAGE_STATUS); styles(LabClass.SMALL)
            }) {
                Text(when {
                    session.localDataDeleted -> session.storageMessage
                    session.isReadOnly -> "Deine Arbeit wird in einem anderen Tab bearbeitet. Hier kannst du lesen und die KI-Hilfe nutzen."
                    else -> "Deine Antworten, Messungen und Notizen werden automatisch in diesem Browser gespeichert."
                })
            }
            if (!needsRescue) draftNotice(session, draftStorage)?.let { notice ->
                P(attrs = { styles(LabClass.SMALL) }) { Text(notice) }
            }
            session.restoreNotice?.takeIf { it != session.storageMessage }?.let { notice ->
                P(attrs = { styles(LabClass.SMALL) }) { Text(notice) }
            }
            P { Text("Der Website-Link enthält deine Eingaben nicht. Kopiere deinen Arbeitsstand, um ihn an einem anderen Ort aufzubewahren.") }
            if (!needsRescue) SessionWorkCopy(session, questions, extraNotes, data)
            if (session.isReadOnly && !needsRescue) {
                P { Text("Möchtest du hier weiterarbeiten? Schließe den anderen Tab und lade dann hier den aktuellen Stand.") }
                SessionReload(session, draftStorage, draftSnapshot)
            }
            SessionTransferControls(session, hasNewUnsavedWork = needsRescue, onResetNotes = onResetNotes)
        }
    }
}

@Composable
private fun SessionWorkCopy(
    session: LabSession,
    questions: Map<AppSection, String>,
    extraNotes: String,
    data: ScientificData?,
) {
    var includeTechnicalAppendix by remember { mutableStateOf(false) }
    val workText = remember(session.snapshot, questions, extraNotes, data, includeTechnicalAppendix) {
        readableSessionWork(session, questions, extraNotes, data, includeTechnicalAppendix)
    }
    P(attrs = { styles(LabClass.SMALL) }) {
        Text("Die Kopie enthält deinen Arbeitsbericht und deine Fragen für den KI-Chat. Sie lässt sich lesen und weitergeben, aber nicht wieder in die Website einlesen.")
    }
    if (data == null) P(attrs = { styles(LabClass.SMALL) }) {
        Text("Die wissenschaftlichen Datendateien sind gerade nicht geladen. Deine Eingaben und die Literaturangaben werden kopiert; der Abgleich mit der aktuellen Bilddatei fehlt.")
    }
    Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(0.5.cssRem); minHeight(44.px) } }) {
        Input(InputType.Checkbox, attrs = {
            checked(includeTechnicalAppendix)
            onChange { includeTechnicalAppendix = it.value }
        })
        Text("Technischen Anhang in den Arbeitsstand aufnehmen")
    }
    PlainTextCopy(workText, workCopyIds, "Arbeitsstand kopieren",
        previewLabel = "Kopiertext ansehen",
        successMessage = "Arbeitsstand kopiert. Füge ihn an einem Ort ein, an dem du ihn behalten kannst.")
}

@Composable
private fun SessionReload(session: LabSession, drafts: UiDraftStorage?, snapshot: UiDraftSnapshot?) {
    Button(attrs = {
        elementId(SessionTransferId.RELOAD_CURRENT); styles(LabClass.SECONDARY)
        onClick {
            val unsavedText = snapshot?.let { drafts?.hasUnsavedText(it) } == true
            if ((!unsavedText && session.isReadOnly) || window.confirm(
                "Beim Neuladen gehen ungespeicherte Texte dieses Tabs verloren. Kopiere sie vorher, wenn du sie behalten möchtest. Jetzt neu laden?"
            )) window.location.reload()
        }
    }) { Text(if (session.isReadOnly) "Hier weiterarbeiten" else "Gespeicherten Stand neu laden") }
}

/** Reading stays available; controls that edit personal work declare their own protection. */
@Composable
fun SessionEditingBoundary(session: LabSession, content: @Composable () -> Unit) {
    Div(attrs = {
        elementId(SessionUiId.EDITING_BOUNDARY); sessionUiStyles(SessionUiClass.EDITING_BOUNDARY)
        if (session.isReadOnly) {
            describedBy(Id.COURSE_STORAGE_STATUS)
        }
    }) { content() }
}
