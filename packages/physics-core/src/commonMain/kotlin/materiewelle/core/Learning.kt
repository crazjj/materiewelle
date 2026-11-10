package materiewelle.core

import kotlinx.serialization.Serializable
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.abs

/** Teaching model: coherent equal point slits, paraxial far field, no envelope. */
data class DoubleSlitModel(val voltage: Voltage, val distance: Length, val slitSpacing: Length) {
    init { require(distance.meters > 0 && slitSpacing.meters > 0) }
    val wavelength: Length get() = Physics.electronWavelength(voltage)
    val period: Length get() = Physics.fringeSpacing(distance, wavelength, slitSpacing)
    fun relativeIntensity(position: Double): Double {
        require(position.isFinite())
        val amplitude = cos(PI * position / period.meters)
        return amplitude * amplitude
    }
}

/** Numerical oracle for reviewed/AI-authored quiz text. Never trust an LLM's answer key. */
data class WavelengthQuiz(val voltage: Voltage) {
    val targetVoltage: Voltage get() = Voltage(4 * voltage.volts)
    val answerPm: Double get() = Physics.electronWavelength(targetVoltage).meters * 1e12
    fun accepts(raw: String): Boolean = raw.trim().replace(',', '.').toDoubleOrNull()
        ?.takeIf { it.isFinite() }?.let { abs(it - answerPm) <= 0.06 } ?: false
}

@Serializable
data class VideoScene(val seconds: Int, val narration: String, val visual: String)

@Serializable
data class QuizDraft(val prompt: String, val sourceVoltageV: Double, val targetVoltageV: Double, val claimedAnswerPm: Double) {
    /** Only the supported numeric family is accepted; wording still needs human review. */
    fun verifyNumerics(): QuizDraft {
        require(prompt.isNotBlank() && prompt.length <= 1200)
        require(sourceVoltageV.isFinite() && sourceVoltageV in 20.0..250.0)
        require(targetVoltageV == 4 * sourceVoltageV)
        require(claimedAnswerPm.isFinite() && abs(claimedAnswerPm - WavelengthQuiz(Voltage(sourceVoltageV)).answerPm) <= .06)
        return this
    }
}

@Serializable
data class VideoBrief(
    val schemaVersion: Int = 1,
    val kind: String = "reviewed-storyboard-not-generated-video",
    val title: String,
    val voltageV: Double,
    val wavelengthPm: Double,
    val scenes: List<VideoScene>,
    val reviewRules: List<String> = listOf(
        "Formeln, Skalen und Ergebnisse als geprüfte Vektorgrafiken einblenden, nicht im Videomodell generieren.",
        "Schema und Modell nicht als historische Aufnahme oder individuelle Elektronenbahn ausgeben.",
        "Künstliche Sprecher kennzeichnen; keine angeblichen Zitate realer Personen.",
        "Vor Veröffentlichung fachlich und hinsichtlich Nutzungsrechten prüfen.",
    ),
)

fun videoBrief(voltage: Voltage): VideoBrief {
    val pm = Physics.electronWavelength(voltage).meters * 1e12
    return VideoBrief(title = "Wie kurz ist eine Materiewelle?", voltageV = voltage.volts, wavelengthPm = pm,
        scenes = listOf(
            VideoScene(6, "Eine Spannung von ${Physics.decimal(voltage.volts, 0)} Volt beschleunigt die Elektronen. Elektrische Energie wird zu kinetischer Energie.", "SVG: Quelle und Beschleunigungsstrecke; e U = p² / (2 m)."),
            VideoScene(7, "Aus dem Impuls ergibt sich die de Broglie Wellenlänge. Im nicht relativistischen Modell sind das ${Physics.decimal(pm, 2)} Pikometer.", "Geprüfte Formel λ = h / √(2 m e U), Zahlen aus dem Kotlin-Kern."),
            VideoScene(7, "Bei vierfacher Spannung halbiert sich die Wellenlänge. Die sichtbaren Wellenlinien sind ein vergrößertes Schema, keine Elektronenbahnen.", "Berechnete Kurve und Vergleich; keine erfundene Messaufnahme."),
        ))
}

@Serializable
data class BrowserDocument(
    val title: String,
    val pdf: String,
    val pages: List<String>,
    val sourceSha256: String,
    val compiler: String,
    /** Text extracted from this exact compiled PDF, not a separately maintained summary. */
    val textAlternative: String,
    val sections: List<materiewelle.core.course.DocumentSection> = emptyList(),
    val pageText: List<String> = emptyList(),
) {
    fun validate(): BrowserDocument {
        require(title.isNotBlank() && title.length <= 180)
        require(pages.size in 1..100)
        require(pages.withIndex().all { (index, path) -> path == "learning-docs/page-${index + 1}.svg" })
        require(Regex("learning-docs/[a-z][a-z0-9-]*\\.pdf").matches(pdf))
        require(Regex("[a-f0-9]{64}").matches(sourceSha256))
        require(compiler.isNotBlank() && compiler.length <= 180)
        require(textAlternative.isNotBlank() && textAlternative.length <= 300_000)
        require(pageText.isEmpty() || pageText.size == pages.size)
        require(pageText.all { it.length <= 100_000 } && pageText.sumOf { it.length } <= 300_000)
        require(sections.map { it.id }.toSet() == materiewelle.core.course.DocumentSectionId.entries.toSet())
        require(sections.map { it.id }.distinct().size == sections.size)
        require(sections.zipWithNext().all { (a, b) -> a.pageIndex <= b.pageIndex })
        sections.forEach { it.validate(pages.size) }
        return this
    }
}
