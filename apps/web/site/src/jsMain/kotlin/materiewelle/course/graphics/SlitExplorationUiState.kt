package materiewelle.course.graphics

import androidx.compose.runtime.*
import kotlinx.serialization.Serializable
import materiewelle.core.ObservationPosition
import materiewelle.core.SlitConfiguration

@Serializable enum class SlitInvestigation { SPACING, WIDTH }
@Serializable data class SlitInvestigationNotes(val prediction: String = "", val reason: String = "")

@Serializable
data class SlitExplorationDraft(
    val configuration: SlitConfiguration = SlitConfiguration.BOTH_COHERENT,
    val task: SlitInvestigation = SlitInvestigation.SPACING,
    val notes: Map<SlitInvestigation, SlitInvestigationNotes> = emptyMap(),
) {
    fun validate() = apply {
        require(notes.size <= SlitInvestigation.entries.size)
        require(notes.values.all { it.prediction.length <= SlitExplorationUiState.TEXT_LIMIT && it.reason.length <= SlitExplorationUiState.TEXT_LIMIT })
    }
}

/** Automatically restored exploration drafts remain separate from historical image measurements. */
class SlitExplorationUiState(draft: SlitExplorationDraft = SlitExplorationDraft()) {
    var configuration by mutableStateOf(draft.configuration)
    var position by mutableStateOf(ObservationPosition(0.0))
    var phaseOpen by mutableStateOf(false)
    var task by mutableStateOf(draft.task)
    private val notes = mutableStateMapOf<SlitInvestigation, SlitInvestigationNotes>().apply { putAll(draft.notes) }
    var comparisonRequested by mutableStateOf(false)
    val detections = SlitDetectionUiState()
    fun notes() = notes[task] ?: SlitInvestigationNotes()
    fun clearNotes() { notes.clear() }
    fun draft() = SlitExplorationDraft(configuration, task, notes.toMap())
    fun readableNotes(): String = buildString {
        SlitInvestigation.entries.forEach { choice ->
            val written = notes[choice] ?: return@forEach
            if (written.prediction.isBlank() && written.reason.isBlank()) return@forEach
            appendLine(if (choice == SlitInvestigation.SPACING) "Spaltmodell · Spaltabstand" else "Spaltmodell · Spaltbreite")
            if (written.prediction.isNotBlank()) appendLine("Meine Vorhersage: ${written.prediction}")
            if (written.reason.isNotBlank()) appendLine("Meine Erklärung: ${written.reason}")
            appendLine()
        }
    }.trimEnd()
    fun predict(text: String) { notes[task] = notes().copy(prediction = text.take(TEXT_LIMIT)) }
    fun explain(text: String) { notes[task] = notes().copy(reason = text.take(TEXT_LIMIT)) }
    /** Inspecting the main diagram must not open the optional quantum-theory extension. */
    fun inspect(value: ObservationPosition) { position = value }
    companion object { const val TEXT_LIMIT = 1200 }
}
