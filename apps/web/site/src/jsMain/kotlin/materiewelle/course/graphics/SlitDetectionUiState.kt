package materiewelle.course.graphics

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.random.Random
import materiewelle.core.*

/** Temporary model-series owner. No path to historical measurements or persisted session work. */
class SlitDetectionUiState(initialSeed: Int = Random.nextInt(1, Int.MAX_VALUE)) {
    init { require(initialSeed in 1 until Int.MAX_VALUE) }
    var run: SlitDetectionRun? by mutableStateOf(null); private set
    var running by mutableStateOf(false); private set
    var showData by mutableStateOf(false); private set
    var expanded by mutableStateOf(false); private set
    var notice by mutableStateOf("Noch keine Nachweise gesammelt."); private set
    private var nextInitialSeed = initialSeed
    private var generation = 0

    fun matches(model: FiniteSlitModel, configuration: SlitConfiguration): Boolean =
        run?.distribution?.let { it.model == model && it.configuration == configuration } == true

    fun bind(distribution: SlitDetectionDistribution) {
        if (matches(distribution.model, distribution.configuration)) return
        val previous = run
        pause()
        if (previous != null) nextInitialSeed = differentSeed(previous.seed)
        run = SlitDetectionRun.start(distribution, nextInitialSeed)
        notice = if (previous == null) "Noch keine Nachweise gesammelt."
            else "Parameter geändert: neue Modellserie. Frühere Treffer werden nicht mit dem neuen Muster vermischt."
    }

    fun add(amount: Int) {
        pause()
        val current = run ?: return
        run = current.append(amount)
        notice = if (run!!.isFull) "Die Serie ist voll. Starte bei Bedarf eine neue Modellserie." else "Zusätzliche Nachweise gesammelt."
    }

    fun newSeries() {
        pause()
        val current = run ?: return
        run = SlitDetectionRun.start(current.distribution, differentSeed(current.seed))
        notice = "Neue Modellserie mit einer anderen Zufallsfolge. Die Parameter bleiben gleich."
    }

    fun replay() {
        pause()
        val current = run ?: return
        run = SlitDetectionRun.start(current.distribution, current.seed)
        notice = "Gleiche Zufallsfolge neu begonnen. Sammle dieselbe Trefferzahl für eine genaue Wiederholung."
    }

    fun togglePlayback(motionActive: Boolean, visible: Boolean) {
        if (running) pause()
        else if (motionActive && visible && run?.isFull == false && !showData) {
            generation++
            running = true
            notice = "Die Modellserie sammelt automatisch. Pause hält den bisherigen Stand fest."
        }
    }

    fun pause() {
        generation++
        if (running) notice = "Pausiert. Mit „Automatisch sammeln“ setzt du diese Serie fort; die bisherigen Treffer bleiben erhalten."
        running = false
    }
    fun setExpanded(open: Boolean) { expanded = open; if (!open) pause() }
    fun ticket(): Int = generation
    fun cancelPending() { generation++ }
    fun applyActivity(motionActive: Boolean, visible: Boolean) { if (!motionActive || !visible) pause() }
    fun toggleData() { pause(); showData = !showData }

    fun tick(ticket: Int, motionActive: Boolean, visible: Boolean) {
        if (ticket != generation || !running || !motionActive || !visible || showData) return
        run = run?.append(AUTOMATIC_BATCH)
        if (run?.isFull == true) {
            pause()
            notice = "Die Serie ist voll. Starte bei Bedarf eine neue Modellserie."
        }
    }

    private fun differentSeed(previous: Int): Int = Random.nextInt(1, Int.MAX_VALUE).let {
        if (it != previous) it else if (previous == Int.MAX_VALUE - 1) 1 else previous + 1
    }

    companion object {
        const val AUTOMATIC_BATCH = 20
        const val TICK_DELAY_MS = 160
    }
}
