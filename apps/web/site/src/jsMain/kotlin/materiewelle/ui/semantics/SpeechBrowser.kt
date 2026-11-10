package materiewelle.ui.semantics

import kotlinx.browser.window
import org.w3c.dom.events.Event

// Small typed browser adapter; no UI or scientific JSON uses dynamic values.
internal external interface SpeechVoice {
    val name: String
    val lang: String
    val localService: Boolean
}

internal external interface SpeechEngine {
    fun getVoices(): Array<SpeechVoice>

    fun speak(utterance: SpeechSynthesisUtterance)

    fun cancel()

    fun addEventListener(type: String, listener: (Event) -> Unit)

    fun removeEventListener(type: String, listener: (Event) -> Unit)
}

internal external class SpeechSynthesisUtterance(text: String) {
    var lang: String
    var voice: SpeechVoice?
    var rate: Double
    var onend: (() -> Unit)?
    var onerror: (() -> Unit)?
}

@JsName("speechSynthesis") internal external val speechEngine: SpeechEngine

private external interface SpeechWindow {
    val speechSynthesis: SpeechEngine?
    @JsName("SpeechSynthesisUtterance") val utteranceConstructor: Any?
}

internal fun speechSupported(): Boolean =
    window.unsafeCast<SpeechWindow>().let {
        it.speechSynthesis != null && it.utteranceConstructor != null
    }
