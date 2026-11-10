package materiewelle.course.graphics

import kotlin.test.*
import materiewelle.core.*

class SlitDetectionUiStateTest {
    private val model = FiniteSlitModel(Voltage(150.0), Length(10.0), Length(2e-6), Length(.5e-6))
    private val distribution = SlitDetectionDistribution.create(model)
    private fun bound() = SlitDetectionUiState(101).also { it.bind(distribution) }

    @Test fun sameModelPreservesSeriesButChangedParametersNeverMixHits() {
        val state = bound()
        state.add(100)
        val previous = state.run
        state.bind(SlitDetectionDistribution.create(model))
        assertSame(previous, state.run)
        state.togglePlayback(motionActive = true, visible = true)
        val stale = state.ticket()
        val changed = SlitDetectionDistribution.create(model.copy(voltage = Voltage(200.0)))
        state.bind(changed)
        assertEquals(0, state.run!!.count)
        assertFalse(state.running)
        assertNotEquals(previous!!.seed, state.run!!.seed)
        assertTrue(state.notice.startsWith("Parameter geändert"))
        state.tick(stale, motionActive = true, visible = true)
        assertEquals(0, state.run!!.count)
    }

    @Test fun changedSlitConfigurationStartsANewSeries() {
        val state = bound()
        state.add(100)
        state.bind(SlitDetectionDistribution.create(model, SlitConfiguration.LEFT_ONLY))
        assertEquals(0, state.run!!.count)
        assertEquals(SlitConfiguration.LEFT_ONLY, state.run!!.distribution.configuration)
        assertTrue(state.notice.startsWith("Parameter geändert"))
    }

    @Test fun replayRepeatsPositionsAndNewSeriesUsesAnotherSeed() {
        val state = bound()
        state.add(100)
        state.add(1)
        val previous = state.run!!
        state.replay()
        assertEquals(0, state.run!!.count)
        assertEquals(previous.seed, state.run!!.seed)
        state.add(1)
        state.add(100)
        assertEquals(previous.hits, state.run!!.hits)
        assertEquals(previous.counts, state.run!!.counts)
        state.newSeries()
        assertNotEquals(previous.seed, state.run!!.seed)
        assertEquals(0, state.run!!.count)
        assertSame(previous.distribution, state.run!!.distribution)
    }

    @Test fun quietOrInvisibleViewCannotArmAndReactivationDoesNotResume() {
        val state = bound()
        state.togglePlayback(motionActive = false, visible = true)
        state.togglePlayback(motionActive = true, visible = false)
        assertFalse(state.running)
        state.togglePlayback(motionActive = true, visible = true)
        val pending = state.ticket()
        state.applyActivity(motionActive = false, visible = true)
        state.applyActivity(motionActive = true, visible = true)
        state.tick(pending, motionActive = true, visible = true)
        assertFalse(state.running)
        assertEquals(0, state.run!!.count)
        assertTrue(state.notice.startsWith("Pausiert"))
        state.add(100)
        assertEquals(100, state.run!!.count)
    }

    @Test fun closingThePanelPausesImmediatelyAndReopeningPreservesTheSeries() {
        val state = bound()
        state.setExpanded(true)
        state.togglePlayback(motionActive = true, visible = true)
        state.tick(state.ticket(), motionActive = true, visible = true)
        val pending = state.ticket()
        val previous = state.run
        state.setExpanded(false)
        state.tick(pending, motionActive = true, visible = true)
        state.setExpanded(true)
        assertSame(previous, state.run)
        assertFalse(state.running)
        assertTrue(state.expanded)
    }

    @Test fun staleTimerCannotAppendAfterPauseAndExplicitRestart() {
        val state = bound()
        state.togglePlayback(motionActive = true, visible = true)
        val old = state.ticket()
        state.pause()
        state.togglePlayback(motionActive = true, visible = true)
        state.tick(old, motionActive = true, visible = true)
        assertEquals(0, state.run!!.count)
        state.tick(state.ticket(), motionActive = true, visible = true)
        assertEquals(SlitDetectionUiState.AUTOMATIC_BATCH, state.run!!.count)
        val canceled = state.ticket()
        state.cancelPending()
        state.tick(canceled, motionActive = true, visible = true)
        assertEquals(SlitDetectionUiState.AUTOMATIC_BATCH, state.run!!.count)
    }

    @Test fun currentVisibilityAndMotionAreCheckedBeforePendingCallbackCanAppend() {
        val state = bound()
        state.togglePlayback(motionActive = true, visible = true)
        val pending = state.ticket()
        state.tick(pending, motionActive = true, visible = false)
        state.tick(pending, motionActive = false, visible = true)
        assertEquals(0, state.run!!.count)
    }

    @Test fun dataTablePausesAutomationWithoutChangingCounts() {
        val state = bound()
        state.add(100)
        state.togglePlayback(motionActive = true, visible = true)
        val pending = state.ticket()
        state.toggleData()
        assertTrue(state.showData)
        assertFalse(state.running)
        state.togglePlayback(motionActive = true, visible = true)
        state.tick(pending, motionActive = true, visible = true)
        assertFalse(state.running)
        assertEquals(100, state.run!!.count)
        state.toggleData()
        assertFalse(state.showData)
        assertFalse(state.running)
    }

    @Test fun capacityClampsPartialBatchAndCannotArmAutomation() {
        val state = bound()
        repeat(9) { state.add(1000) }
        state.add(1)
        state.add(1000)
        assertEquals(SlitDetectionRun.MAX_HITS, state.run!!.count)
        assertEquals(SlitDetectionRun.MAX_HITS, state.run!!.counts.sum())
        val previous = state.run
        state.add(100)
        assertSame(previous, state.run)
        state.togglePlayback(motionActive = true, visible = true)
        assertFalse(state.running)
    }
}
