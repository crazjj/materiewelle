package materiewelle.learning

import androidx.compose.runtime.*
import materiewelle.ui.MathText as Text
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.HTMLAudioElement
import org.w3c.dom.url.URL
import org.w3c.files.File

private const val MAX_AUDIO_BYTES = 25 * 1024 * 1024

/** A file stays on this device. Object URLs and playback have bounded lifetimes. */
@Composable
fun ProducedAudio(active: Boolean) {
    var file by remember { mutableStateOf<File?>(null) }
    var source by remember { mutableStateOf<String?>(null) }
    var player by remember { mutableStateOf<HTMLAudioElement?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    DisposableEffect(file) {
        val url = file?.let { URL.createObjectURL(it) }
        source = url
        onDispose {
            player?.pause()
            url?.let(URL::revokeObjectURL)
        }
    }
    DisposableEffect(active) {
        if (!active) player?.pause()
        onDispose { player?.pause() }
    }
    Div(attrs = { styles(LabClass.PANEL) }) {
        H3 { Text("Vorproduzierte Sprecheraufnahme ausprobieren") }
        P(attrs = { styles(LabClass.SMALL) }) {
            Text(
                "Erzeuge den redigierten Sprechertext einmal mit einer guten Stimme oder sprich ihn selbst ein. Eine lokale MP3-/WAV-Datei kannst du hier anhören. Sie wird nicht hochgeladen und steuert die Clipvorschau noch nicht."
            )
        }
        FieldLabel(Id.VOICE_FILE) { Text("Lokale Audiodatei · höchstens 25 MiB") }
        Input(
            InputType.File,
            attrs = {
                elementId(Id.VOICE_FILE)
                acceptProducedAudio()
                onChange { event ->
                    val selected = event.target.files?.item(0)
                    error =
                        when {
                            selected == null -> null
                            selected.size.toDouble() <= 0 ||
                                selected.size.toDouble() > MAX_AUDIO_BYTES ->
                                "Bitte eine nicht leere Datei bis 25 MiB wählen."
                            selected.name.substringAfterLast('.', "").lowercase() !in
                                setOf("mp3", "wav") -> "Bitte eine MP3- oder WAV-Datei wählen."
                            else -> null
                        }
                    player?.pause()
                    file = selected?.takeIf { error == null }
                    event.target.value = ""
                }
            },
        )
        source?.let { url ->
            key(url) {
                Audio(
                    attrs = {
                        elementId(Id.VOICE_PLAYER)
                        ariaLabel("Lokale Sprecheraufnahme")
                        ref { element ->
                            element.controls = true
                            element.preload = "metadata"
                            element.src = url
                            player = element
                            val listener: (org.w3c.dom.events.Event) -> Unit = {
                                error =
                                    "Der Browser kann diese Datei nicht dekodieren. Bitte MP3 oder PCM-WAV exportieren."
                            }
                            element.addEventListener(BrowserEvent.ERROR.eventName, listener)
                            onDispose {
                                element.pause()
                                element.removeEventListener(BrowserEvent.ERROR.eventName, listener)
                                if (player == element) player = null
                            }
                        }
                    }
                ) {}
            }
        }
        P(
            attrs = {
                elementId(Id.VOICE_STATUS)
                role(AriaRole.STATUS)
                styles(if (error != null) LabClass.ERROR else LabClass.MUTED)
            }
        ) {
            Text(
                error
                    ?: file?.let {
                        "${it.name} · nur lokal geöffnet; keine synchronisierte Tonspur."
                    }
                    ?: "Noch keine Aufnahme gewählt."
            )
        }
        Button(
            attrs = {
                elementId(Id.VOICE_CLEAR)
                styles(LabClass.SECONDARY)
                if (file == null) disabled()
                onClick {
                    player?.pause()
                    file = null
                    error = null
                }
            }
        ) {
            Text("Aufnahme entfernen")
        }
    }
}
