package materiewelle.core

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val storyboardJson = Json { prettyPrint = true }

/** Planned visual timing. It does not claim to match a device's TTS duration. */
data class StoryboardPosition(val sceneIndex: Int, val seconds: Double, val sceneProgress: Double)

class StoryboardTimeline(brief: VideoBrief) {
    val scenes = brief.scenes.toList()
    val durationSeconds: Int

    init {
        require(brief.schemaVersion == 1)
        require(scenes.size in 1..32)
        require(scenes.all { it.seconds in 1..60 && it.narration.isNotBlank() && it.narration.length <= 2000 })
        durationSeconds = scenes.sumOf { it.seconds }
        require(durationSeconds <= 600)
    }

    fun sceneStart(index: Int): Int {
        require(index in scenes.indices)
        return scenes.take(index).sumOf { it.seconds }
    }

    fun at(seconds: Double): StoryboardPosition {
        require(seconds.isFinite())
        val time = seconds.coerceIn(0.0, durationSeconds.toDouble())
        var start = 0
        scenes.forEachIndexed { index, scene ->
            if (time < start + scene.seconds || index == scenes.lastIndex) {
                return StoryboardPosition(index, time, ((time - start) / scene.seconds).coerceIn(0.0, 1.0))
            }
            start += scene.seconds
        }
        error("A validated storyboard always contains a scene")
    }

    /** Standard UTF-8 WebVTT cues; narration is escaped as literal subtitle text. */
    fun webVtt(): String = buildString {
        append("WEBVTT\n\nNOTE Planned storyboard cues; align with the final recorded audio before publication.\n\n")
        scenes.forEachIndexed { index, scene ->
            val start = sceneStart(index)
            val text = scene.narration.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.joinToString(" ")
                .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
            append("${index + 1}\n${timestamp(start)} --> ${timestamp(start + scene.seconds)}\n$text\n\n")
        }
    }

    fun transcript(): String = scenes.mapIndexed { index, scene ->
        "Szene ${index + 1} · ${timestamp(sceneStart(index))}\n${scene.narration}"
    }.joinToString("\n\n") + "\n"

    private fun timestamp(seconds: Int): String =
        "${(seconds / 3600).toString().padStart(2, '0')}:${(seconds / 60 % 60).toString().padStart(2, '0')}:${(seconds % 60).toString().padStart(2, '0')}.000"
}

/** Portable authoring request, not a provider call or an assertion of review. */
fun quizAuthoringPrompt(voltage: Voltage): String {
    require(voltage.volts in 20.0..250.0)
    val quiz = WavelengthQuiz(voltage)
    val example = storyboardJson.encodeToString(QuizDraft(
        "Wie verändert sich die de-Broglie-Wellenlänge bei vierfacher Beschleunigungsspannung?",
        voltage.volts, quiz.targetVoltage.volts, quiz.answerPm,
    ))
    return """
        Erstelle einen deutschen Aufgabenentwurf für den Physik-LK zum nicht relativistischen de-Broglie-Modell.
        Antworte ausschließlich mit einem JSON-Objekt derselben Struktur wie das Beispiel unten.
        Variiere den Wortlaut, nicht das Modell: Elektronen aus Ruhe, E_kin = e U, p² = 2 m e U, λ = h/p.
        sourceVoltageV ist in Volt und liegt zwischen 20 und 250; targetVoltageV ist genau viermal so groß.
        claimedAnswerPm ist die Wellenlänge bei targetVoltageV in Pikometern, nicht der Streifenabstand.
        Verwende keine historischen Messwerte oder Aussagen über unkalibrierte Foto-Pixel.
        Konstanten (SI): h = ${Physics.H}; e = ${Physics.E}; m_e = ${Physics.ELECTRON_MASS}.
        Der Import berechnet die Zahlenlösung unabhängig. Eine erfolgreiche Zahlenprüfung ersetzt keine fachliche Prüfung des Wortlauts.

        $example
    """.trimIndent()
}
