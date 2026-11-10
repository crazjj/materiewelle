package materiewelle.pages

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import materiewelle.JonssonData
import materiewelle.core.MeasurementSession
import materiewelle.core.NativePixel

class JonssonUiStateTest {
    @Test fun hidingModelComparisonIsOnlyAViewChoice() {
        val state = JonssonUiState()
        val before = state.draft()
        state.slitComparisonVisible.value = false
        assertEquals(before, state.draft())
        assertTrue(JonssonUiState(state.draft()).slitComparisonVisible.value)
    }
    @Test fun hoverIsTransientButExplicitProbePositionReloads() {
        val state = JonssonUiState()
        val before = state.draft()
        state.sampleX.value = NativePixel(580.0)
        assertEquals(before, state.draft())
        state.pinnedSampleX.value = NativePixel(580.0)
        assertEquals(580.0, JonssonUiState(state.draft()).sampleX.value?.value)
    }
    private fun image(hash: Char = 'a', spacing: Double = 2e-6) = JonssonData(
        "fixture.png", 703, 242, emptyList(), 160.0,
        50_000.0, spacing, 0.35, "Synthetic UI fixture",
        0.0, 100.0, 703.0, 120.0, hash.toString().repeat(64),
    )

    @Test fun rebindingSameRasterRetainsEditedViewState() {
        val state = JonssonUiState()
        state.bindImage(image())
        state.zoom.value = 2.0
        state.imageScrollLeft.value = 170.0
        state.selected.value = 2
        state.markerText.value = "593,"
        state.editingMarkerText.value = true
        state.newMarkerText.value = "6"
        state.slitSpacingUm.value = 3.5
        state.selectedOpticsStage.value = 2

        // Conditional remount supplies the same raster, not a new user experiment.
        state.bindImage(image())

        assertEquals(2.0, state.zoom.value)
        assertEquals(170.0, state.imageScrollLeft.value)
        assertEquals(2, state.selected.value)
        assertEquals("593,", state.markerText.value)
        assertEquals("6", state.newMarkerText.value)
        assertEquals(3.5, state.slitSpacingUm.value)
        assertEquals(2, state.selectedOpticsStage.value)
    }

    @Test fun differentRasterCannotInheritCoordinateDraftOrPan() {
        val state = JonssonUiState()
        state.bindImage(image())
        state.zoom.value = 2.0
        state.imageScrollLeft.value = 170.0
        state.markerText.value = "650"
        state.editingMarkerText.value = true
        state.newMarkerText.value = "551"
        state.bindImage(image('b', 3e-6))

        assertEquals(1.0, state.zoom.value)
        assertEquals(0.0, state.imageScrollLeft.value)
        assertEquals("", state.markerText.value)
        assertEquals("", state.newMarkerText.value)
        assertFalse(state.editingMarkerText.value)
        assertEquals(3.0, state.slitSpacingUm.value)
    }

    @Test fun readOnlyReferenceDisclosureNeverBecomesSavedMeasurementOrViewDraft() {
        val data = image()
        val state = JonssonUiState()
        state.bindImage(data)
        val measurement = MeasurementSession()
            .addMarker(550, data).addMarker(565, data)
            .updateJustification("Zwei benachbarte helle Streifen.")
            .saveInitialSelection(data)
        val savedDraft = state.draft()
        assertTrue(measurement.canViewReference)
        assertFalse(state.referenceVisible(measurement))

        state.readOnlyReferenceOpen.value = true

        assertTrue(state.referenceVisible(measurement))
        assertFalse(measurement.referenceViewed)
        assertEquals(savedDraft, state.draft())
        assertFalse(JonssonUiState(state.draft()).referenceVisible(measurement))
        state.bindImage(data)
        assertTrue(state.referenceVisible(measurement))
        state.bindImage(image('b'))
        assertFalse(state.referenceVisible(measurement))
        assertTrue(state.referenceVisible(measurement.revealReference()))
    }
}
