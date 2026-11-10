package materiewelle.course

import androidx.compose.runtime.*
import kotlinx.browser.window
import materiewelle.state.LabSession
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*

/** Local deletion stays explicit; no import, download or restoration-file controls. */
@Composable
fun SessionTransferControls(session: LabSession, hasNewUnsavedWork: Boolean = false, onResetNotes: () -> Unit = {}) {
    var confirmDeletion by remember { mutableStateOf(false) }
    if (session.localDataDeleted) {
        P(attrs = { role(AriaRole.STATUS) }) { Text(session.storageMessage) }
        Button(attrs = {
            elementId(SessionTransferId.RELOAD); styles(LabClass.SECONDARY)
            onClick {
                if (!hasNewUnsavedWork || window.confirm(
                    "Neue Arbeit seit dem Löschen ist noch nicht gespeichert. Kopiere sie vorher, wenn du sie behalten möchtest. Jetzt neu starten?"
                )) window.location.reload()
            }
        }) { Text("Mit leerer Arbeit neu starten") }
    } else {
        Div(attrs = { styles(LabClass.COURSE_ACTIONS) }) {
            Button(attrs = {
                elementId(Id.COURSE_RESET); styles(LabClass.SECONDARY)
                if (session.isReadOnly) disabled()
                onClick {
                    if (window.confirm("Eigene Lernnotizen und Antworten löschen? Messungen und Modelleinstellungen bleiben erhalten.")) {
                        session.resetCourse()
                        onResetNotes()
                    }
                }
            }) { Text("Antworten und Notizen löschen …") }
            if (!confirmDeletion) Button(attrs = {
                elementId(SessionTransferId.DELETE_OPEN); styles(LabClass.SECONDARY)
                if (!session.localDeletionAllowed) disabled()
                onClick { confirmDeletion = true }
            }) { Text("Arbeit auf diesem Gerät löschen …") }
        }
        if (confirmDeletion) {
            P { Text("Gelöscht werden in diesem Browser: deine Antworten, Messungen, Fragen für den KI-Chat, lokalen Sicherungen und Anzeigeeinstellungen. Kopiere vorher alles, was du behalten möchtest. Bereits kopierte Texte, Downloads und externe Chats bleiben davon unberührt.") }
            Div(attrs = { styles(LabClass.COURSE_ACTIONS) }) {
                Button(attrs = {
                    elementId(SessionTransferId.DELETE_CONFIRM); styles(LabClass.SECONDARY)
                    if (!session.localDeletionAllowed) disabled()
                    onClick { session.deleteAllLocalData(); confirmDeletion = false }
                }) { Text("Arbeit auf diesem Gerät löschen") }
                Button(attrs = {
                    elementId(SessionTransferId.DELETE_CANCEL); styles(LabClass.SECONDARY)
                    onClick { confirmDeletion = false }
                }) { Text("Behalten") }
            }
        }
    }
}
