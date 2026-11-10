package materiewelle.pages

import kotlin.test.*
import materiewelle.core.*
import materiewelle.state.MeasurementOwner

class MeasurementEditorTest {
    private val image = JonssonData("jonsson-page6-xref33.png", 703, 242,
        listOf(551.0, 564.0, 578.0, 592.0, 606.0, 620.0, 634.0, 647.0), 160.0,
        50_000.0, 2e-6, 0.35, "Keine Fotokalibrierung", 490.0, 100.0, 213.0, 120.0,
        "d8952b3336cb499884f2cbe600cc15645f8bc1c406b1c4e7f43f3bc4d9553bb6")
    private class Owner : MeasurementOwner {
        override var measurement = MeasurementSession()
        override var canEditMeasurement = true
        var updateCalls = 0
        override fun updateMeasurement(transform: (MeasurementSession) -> MeasurementSession) {
            updateCalls++
            measurement = transform(measurement)
        }
    }

    @Test fun newCoordinateExplainsTheSpecificDisabledAction() {
        val empty = MeasurementSession()
        val existing = empty.addMarker(578, image)
        assertEquals(NewMarkerIssue.EMPTY, newMarkerIssue("", empty, image))
        assertEquals(NewMarkerIssue.INVALID_COORDINATE, newMarkerIssue("703", empty, image))
        assertEquals(NewMarkerIssue.INVALID_COORDINATE, newMarkerIssue("578.5", empty, image))
        assertEquals(NewMarkerIssue.DUPLICATE, newMarkerIssue("578,0", existing, image))
        assertNull(newMarkerIssue("592", existing, image))
        val full = (0 until MeasurementSession.MAX_MARKERS).fold(empty) { current, index ->
            current.addMarker(500 + index * 2, image)
        }
        assertEquals(NewMarkerIssue.LIMIT_REACHED, newMarkerIssue("650", full, image))
    }

    @Test fun consecutiveEventsReadLatestOwnerAndRejectCrossing() {
        val owner = Owner(); val editor = MeasurementEditor(image, owner, JonssonUiState())
        editor.addMarker(NativePixel(550.0)); editor.addMarker(NativePixel(565.0)); editor.addMarker(NativePixel(580.0))
        assertEquals(listOf(550, 565, 580), owner.measurement.markers.map { it.xPx })
        editor.commitMarker(1, NativePixel(585.0), false)
        assertEquals(listOf(550, 565, 580), owner.measurement.markers.map { it.xPx })
        editor.commitMarker(1, NativePixel(566.0), false)
        assertEquals(listOf(550, 566, 580), owner.measurement.markers.map { it.xPx })
    }

    @Test fun gestureUndoKeepsReferenceContactAndSecuredFirstSelection() {
        val owner = Owner(); val editor = MeasurementEditor(image, owner, JonssonUiState())
        editor.addMarker(NativePixel(550.0)); editor.addMarker(NativePixel(565.0))
        editor.update { it.updateJustification("Zwei benachbarte helle Streifen im Basisband.") }
        editor.update { it.saveInitialSelection(image).revealReference().recordReferenceMaterial(ReferenceMaterial.PDF_REFERENCE) }
        val first = owner.measurement.initialSelection
        editor.interaction(true)
        editor.setMarker(1, NativePixel(566.0)); editor.setMarker(1, NativePixel(567.0))
        editor.interaction(false); editor.undoEdit()
        assertEquals(listOf(550, 565), owner.measurement.markers.map { it.xPx })
        assertEquals(first, owner.measurement.initialSelection)
        assertTrue(owner.measurement.referenceViewed)
        assertEquals(setOf(ReferenceMaterial.PDF_REFERENCE), owner.measurement.referenceMaterialsOpened)
    }

    @Test fun readOnlyMeasurementAllowsInspectionButRejectsEveryEditEntryPoint() {
        val owner = Owner()
        owner.measurement = owner.measurement
            .addMarker(550, image).addMarker(565, image)
            .updateJustification("Gespeicherte Auswahl bleibt unverändert.")
        owner.canEditMeasurement = false
        val view = JonssonUiState()
        val editor = MeasurementEditor(image, owner, view)
        val original = owner.measurement
        var transformed = false

        editor.selectMarker(1)
        assertEquals(1, view.selected.value)
        assertEquals("565", view.markerText.value)
        assertEquals(NativePixel(565.0), view.sampleX.value)

        editor.update { transformed = true; it.clearMeasurement() }
        editor.addMarker(NativePixel(580.0))
        editor.commitMarker(1, NativePixel(566.0), false)
        editor.setMarker(1, NativePixel(567.0))
        editor.nudge(1.0)
        editor.interaction(true)
        editor.setMarker(1, NativePixel(568.0))
        editor.interaction(false)
        editor.undoEdit()

        assertFalse(transformed)
        assertEquals(0, owner.updateCalls)
        assertEquals(original, owner.measurement)
        assertFalse(editor.canUndo)
        assertEquals("565", view.markerText.value)
    }

    @Test fun losingEditPermissionDoesNotConsumeOrApplyAnExistingUndoEntry() {
        val owner = Owner()
        val editor = MeasurementEditor(image, owner, JonssonUiState())
        editor.addMarker(NativePixel(550.0))
        val saved = owner.measurement
        val calls = owner.updateCalls
        assertTrue(editor.canUndo)

        owner.canEditMeasurement = false
        editor.undoEdit()
        assertFalse(editor.canUndo)
        assertEquals(saved, owner.measurement)
        assertEquals(calls, owner.updateCalls)

        owner.canEditMeasurement = true
        assertTrue(editor.canUndo)
        editor.undoEdit()
        assertTrue(owner.measurement.markers.isEmpty())
    }
}
