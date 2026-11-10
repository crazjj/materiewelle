package materiewelle.learning

import androidx.compose.runtime.*
import materiewelle.ui.MathText as Text
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.events.Event

@Composable
fun SpeechExample(text: String, active: Boolean) {
    val supported = remember { speechSupported() }
    var voice by remember { mutableStateOf<SpeechVoice?>(null) }
    var speaking by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    var requestId by remember { mutableStateOf(0) }
    fun cancel() {
        requestId++ // Ignore late end/error events from the canceled utterance.
        if (supported) speechEngine.cancel()
        speaking = false
    }
    DisposableEffect(Unit) {
        fun update() {
            if (supported)
                voice =
                    speechEngine
                        .getVoices()
                        .filter { it.lang.startsWith("de", ignoreCase = true) }
                        .sortedBy { !it.localService }
                        .firstOrNull()
        }
        update()
        val listener: (Event) -> Unit = { update() }
        if (supported)
            speechEngine.addEventListener(BrowserEvent.VOICES_CHANGED.eventName, listener)
        onDispose {
            if (supported) {
                speechEngine.removeEventListener(BrowserEvent.VOICES_CHANGED.eventName, listener)
                cancel()
            }
        }
    }
    DisposableEffect(active, text) {
        cancel()
        failed = false
        onDispose { cancel() }
    }
    Div(attrs = { styles(LabClass.WORKSHOP_ACTIONS) }) {
        Button(
            attrs = {
                elementId(Id.MEDIA_SPEAK)
                styles(LabClass.PRIMARY)
                if (!active || !supported || voice == null || speaking) disabled()
                onClick {
                    val utterance = SpeechSynthesisUtterance(text)
                    val token = ++requestId
                    utterance.lang = "de-DE"
                    utterance.voice = voice
                    utterance.rate = .95
                    utterance.onend = { if (token == requestId) speaking = false }
                    utterance.onerror = {
                        if (token == requestId) {
                            speaking = false
                            failed = true
                        }
                    }
                    failed = false
                    speaking = true
                    speechEngine.speak(utterance)
                }
            }
        ) {
            Text("Sprechertext anhören")
        }
        Button(
            attrs = {
                elementId(Id.MEDIA_STOP)
                styles(LabClass.SECONDARY)
                if (!speaking) disabled()
                onClick {
                    cancel()
                    failed = false
                }
            }
        ) {
            Text("Stopp")
        }
    }
    P(
        attrs = {
            styles(LabClass.SMALL, LabClass.MUTED)
            elementId(Id.SPEECH_STATUS)
            role(AriaRole.STATUS)
        }
    ) {
        Text(
            when {
                failed -> "Die Sprachausgabe ist fehlgeschlagen. Der Text bleibt lesbar."
                !supported -> "Dieser Browser bietet keine Sprachausgabe."
                voice == null ->
                    "Keine deutsche Stimme verfügbar. Die Sprachausgabe hängt von Browser und Betriebssystem ab."
                speaking -> "Sprechertext läuft …"
                else ->
                    "Stimme: ${voice?.name} · ${if (voice?.localService == true) "lokale Gerätestimme" else "Gerätestimme mit möglichem Netzwerkdienst"}. Kein Stimmenklon."
            }
        )
    }
}
