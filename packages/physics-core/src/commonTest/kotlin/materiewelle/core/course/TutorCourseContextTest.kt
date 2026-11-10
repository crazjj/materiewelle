package materiewelle.core.course

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import materiewelle.core.MeasurementBand
import materiewelle.core.ReferenceMaterial
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TutorCourseContextTest {
    private val hash = "d8952b3336cb499884f2cbe600cc15645f8bc1c406b1c4e7f43f3bc4d9553bb6"
    private fun measurement(markers: List<Double> = emptyList()) = TutorImageMeasurement(
        hash, 703, 242, 100, 220, markers, TutorMeasurementOrigin.OWN_SELECTION,
    )

    @Test fun selectedContextRoundTripsWithClosedStationAndNativeProvenance() {
        val context = TutorCourseContext(StationId.entries.first(), "2026-10-14", "2.0", "2026-10-14",
            model = TutorModelSnapshot.Surface(54.0, 50.0, .352),
            measurement = measurement(listOf(551.0, 564.0)).copy(selectionReason = "Zwei erkennbare Maxima im gewählten Band.", referenceViewed = true))
        val raw = Json.encodeToString(context)
        assertEquals(context, Json.decodeFromString<TutorCourseContext>(raw))
        assertTrue(raw.contains("own-selection"))
        assertTrue(context.measurement!!.referenceViewed)
        assertEquals(TutorMeasurementOrigin.OWN_SELECTION, context.measurement.origin)
        assertTrue(raw.contains("ni111-surface-model-not-original-scan"))
        assertFalse(raw.contains("lambdaMeasured"))
        assertFalse(raw.contains("mastery"))
    }


    @Test fun provenanceExtensionKeepsLegacyUnknownSeparateFromNoRecordedContact() {
        val legacy = measurement(listOf(551.0, 564.0))
        val legacyJson = Json.encodeToString(legacy)
        val restoredLegacy = Json.decodeFromString<TutorImageMeasurement>(legacyJson)
        assertEquals(null, restoredLegacy.justificationBand)
        assertEquals(null, restoredLegacy.justificationMatchesBand)
        assertEquals(null, restoredLegacy.referenceMaterialsOpened)
        val explicit = legacy.copy(justificationBand = MeasurementBand.UPPER, justificationMatchesBand = false,
            referenceMaterialsOpened = setOf(ReferenceMaterial.PDF_REFERENCE),
            initialSelectionReferenceMaterials = setOf(ReferenceMaterial.UNKNOWN_LEGACY))
        val raw = Json.encodeToString(explicit)
        assertEquals(explicit, Json.decodeFromString<TutorImageMeasurement>(raw))
        assertEquals(TutorMeasurementOrigin.OWN_SELECTION, explicit.origin)
        assertFalse(explicit.referenceViewed)
        assertEquals(emptySet(), legacy.copy(referenceMaterialsOpened = emptySet()).referenceMaterialsOpened)
        assertFailsWith<IllegalArgumentException> { legacy.copy(justificationMatchesBand = true) }
    }

    @Test fun incompleteMeasurementIsRepresentedWithoutInventedResult() {
        assertTrue(measurement().markersNativeX.isEmpty())
        assertEquals(listOf(550.0), measurement(listOf(550.0)).markersNativeX)
        assertEquals(TutorMeasurementOrigin.REFERENCE_IMPORTED,
            measurement().copy(origin = TutorMeasurementOrigin.REFERENCE_IMPORTED).origin)
    }

    @Test fun nativeRasterAndBandValidationRejectsAmbiguousCoordinates() {
        assertFailsWith<IllegalArgumentException> { measurement(listOf(703.0)) }
        assertFailsWith<IllegalArgumentException> { measurement(listOf(Double.NaN)) }
        assertFailsWith<IllegalArgumentException> { measurement(listOf(550.0, 550.0)) }
        assertFailsWith<IllegalArgumentException> { measurement(listOf(551.0, 550.0)) }
        assertFailsWith<IllegalArgumentException> { measurement().copy(nativeWidth = 1406) }
        assertFailsWith<IllegalArgumentException> { measurement().copy(bandYEndExclusive = 100) }
        assertFailsWith<IllegalArgumentException> { measurement().copy(imageSha256 = "unknown") }
    }

    @Test fun handoffSizeAndRevisionsAreBounded() {
        assertFailsWith<IllegalArgumentException> { measurement().copy(selectionReason = "a".repeat(1001)) }
        assertFailsWith<IllegalArgumentException> { measurement((0..64).map { it.toDouble() }) }
        assertFailsWith<IllegalArgumentException> {
            TutorCourseContext(StationId.entries.first(), "", "2.0", "2026-10-14")
        }
        assertFailsWith<IllegalArgumentException> {
            TutorCourseContext(StationId.entries.first(), "a".repeat(65), "2.0", "2026-10-14")
        }
    }

    @Test fun finiteSlitContextPreservesSyntheticGeometryAndRejectsInvalidInputs() {
        val snapshot = TutorModelSnapshot.FiniteSlit(150.0, 10.0, 2e-6, .5e-6)
        val context = TutorCourseContext(StationId.S13_INTERFERENCE, "2026-10-14", "2.0", "2026-10-14", model = snapshot)
        val raw = Json.encodeToString(context)
        assertEquals(context, Json.decodeFromString<TutorCourseContext>(raw))
        assertTrue(raw.contains("finite-double-slit-nonrelativistic-teaching-model"))
        assertFailsWith<IllegalArgumentException> { snapshot.copy(voltageV = 50000.0) }
        assertFailsWith<IllegalArgumentException> { snapshot.copy(distanceM = 0.0) }
        assertFailsWith<IllegalArgumentException> { snapshot.copy(distanceM = Double.POSITIVE_INFINITY) }
        assertFailsWith<IllegalArgumentException> { snapshot.copy(slitSpacingM = Double.NaN) }
        assertFailsWith<IllegalArgumentException> { snapshot.copy(slitWidthM = 3e-6) }
    }

    @Test fun acceptedModelInputsExcludeInvalidAndNonfiniteValues() {
        assertEquals(150.0, TutorModelSnapshot.DeBroglie(150.0).voltageV)
        assertEquals(50000.0, TutorModelSnapshot.HighVoltage(50000.0).voltageV)
        assertFailsWith<IllegalArgumentException> { TutorModelSnapshot.DeBroglie(Double.POSITIVE_INFINITY) }
        assertFailsWith<IllegalArgumentException> { TutorModelSnapshot.Surface(54.0, 90.1, .352) }
        assertFailsWith<IllegalArgumentException> { TutorModelSnapshot.Surface(54.0, 50.0, 0.0) }
        assertFailsWith<IllegalArgumentException> { TutorModelSnapshot.HighVoltage(-1.0) }
    }
}
