package materiewelle.ui

import androidx.compose.runtime.*
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*

/** Every placement owns stable identities; the copied text is never converted to markup. */
data class PlainTextCopyIds(
    val copy: ElementId,
    val status: ElementId,
    val preview: ElementId,
    val select: ElementId,
)

/** Shared interaction state lets a wide preview follow the instructions it supports. */
internal class PlainTextCopyState {
    var status by mutableStateOf<String?>(null); private set
    var manual by mutableStateOf(false); private set
    private var epoch = 0

    fun invalidate() {
        epoch++
        status = null
    }

    fun copy(text: String, successMessage: String) {
        val request = ++epoch
        copyBrowserText(text).then {
            if (request == epoch) status = successMessage
        }.catch {
            if (request == epoch) {
                manual = true
                status = "Automatisches Kopieren ist hier nicht möglich. Wähle ‚Gesamten Text markieren‘ und danach ‚Kopieren‘ im Auswahlmenü."
            }
        }
    }

    fun select(preview: ElementId) {
        status = if (selectBrowserText(preview))
            "Text markiert. Wähle ‚Kopieren‘ im Auswahlmenü. Am Computer: Strg+C oder ⌘C."
        else "Markiere den Text mit Touch oder Maus und wähle ‚Kopieren‘ im Auswahlmenü."
    }
}

@Composable
internal fun rememberPlainTextCopyState(text: String): PlainTextCopyState {
    val state = remember { PlainTextCopyState() }
    DisposableEffect(text) {
        state.invalidate()
        onDispose { state.invalidate() }
    }
    return state
}

@Composable
internal fun PlainTextCopyActions(
    state: PlainTextCopyState,
    text: String,
    ids: PlainTextCopyIds,
    label: String,
    successMessage: String,
    primary: Boolean = false,
) {
    Button(attrs = {
        elementId(ids.copy)
        styles(if (primary) LabClass.PRIMARY else LabClass.SECONDARY)
        onClick { state.copy(text, successMessage) }
    }) { Text(label) }
    state.status?.let { message ->
        P(attrs = { elementId(ids.status); role(AriaRole.STATUS); styles(LabClass.SMALL) }) { Text(message) }
    }
}

@Composable
internal fun PlainTextCopyPreview(
    state: PlainTextCopyState,
    text: String,
    ids: PlainTextCopyIds,
    previewLabel: String,
) {
    Details(attrs = { if (state.manual) expanded() }) {
        Summary { Text(previewLabel) }
        Button(attrs = {
            elementId(ids.select); styles(LabClass.SECONDARY)
            onClick { state.select(ids.preview) }
        }) { Text("Gesamten Text markieren") }
        Pre(attrs = {
            elementId(ids.preview); textCopyStyles(TextCopyClass.PREVIEW)
            data(LabData.LITERAL_EXPORT, true); focusIndex(0)
            ariaLabel(previewLabel)
        }) { Text(text) }
    }
}

/** Usable even when scientific data or the Typst catalogue cannot be loaded. */
@Composable
fun PlainTextCopy(
    text: String,
    ids: PlainTextCopyIds,
    label: String,
    previewLabel: String = "Text ansehen",
    successMessage: String = "Kopiert.",
    primary: Boolean = false,
) {
    val state = rememberPlainTextCopyState(text)
    Div(attrs = { textCopyStyles(TextCopyClass.ROOT) }) {
        PlainTextCopyActions(state, text, ids, label, successMessage, primary)
        PlainTextCopyPreview(state, text, ids, previewLabel)
    }
}
