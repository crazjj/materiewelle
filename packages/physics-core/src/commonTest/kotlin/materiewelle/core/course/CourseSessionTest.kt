package materiewelle.core.course

import materiewelle.core.MeasurementSession
import materiewelle.core.NativeImageMark
import materiewelle.core.*
import kotlin.test.*

class CourseSessionTest {
    @Test fun comparisonsRequireAChoiceButExistingExplicitValuesSurvive() {
        assertNull(SessionSnapshot.fromJson(SessionSnapshot().json()).deBroglie.comparisonVoltage)
        val saved = SessionSnapshot(
            deBroglie = DeBroglieSession(comparisonVoltage = 600.0),
            work = mapOf(StationId.S16_CONCLUSION to StationWork(note = "Meine Erklärung")),
        )
        assertEquals(saved, SessionSnapshot.fromJson(saved.json()))
    }
    private fun image() = JonssonData("raster.png", 703, 242, listOf(551.0, 564.0), 160.0,
        50_000.0, 2e-6, 0.35, "Keine Kalibrierung", 490.0, 100.0, 213.0, 120.0,
        SessionSnapshot.IMAGE_SHA256)
    private fun scientific(image: JonssonData?): ScientificData {
        val evidence = ScientificEvidence(MaterialRole.SECONDARY_REPORT, EvidenceReview.CHECKED_IN_SCOPE,
            "test", "test fixture", "synthetic typed session test")
        val structure = Nickel111Structure(Length(3.52e-10), evidence.copy(role = MaterialRole.INDEPENDENT_STRUCTURE_ASSUMPTION))
        return ScientificData(emptyList(), structure.rowSpacing.meters, "test", true, image,
            dgEvidence = evidence, dgStructure = structure)
    }
    @Test fun allPublicStationsAndLegacyLaboratoryLinksRoundTrip() {
        StationId.entries.forEach { id -> assertEquals(AppRoute.Course(id), AppRoute.parse(AppRoute.Course(id).hash)) }
        assertEquals(AppRoute.Overview, AppRoute.parse(""))
        val measurementLink = AppRoute.Laboratory(Experiment.JONSSON, LabFocus.MEASUREMENT)
        assertEquals("#joensson/messung", measurementLink.hash)
        assertEquals(measurementLink, AppRoute.parse(measurementLink.hash))
        assertFailsWith<IllegalArgumentException> { AppRoute.Laboratory(Experiment.DE_BROGLIE, LabFocus.MEASUREMENT) }
        assertEquals(AppRoute.Laboratory(materiewelle.core.Experiment.DAVISSON_GERMER), AppRoute.parse("#dg"))
        assertEquals(AppRoute.Laboratory(materiewelle.core.Experiment.JONSSON), AppRoute.parse("#jonsson"))
        assertNull(AppRoute.parse("#lernen/unknown")); assertNull(AppRoute.parse("#secret"))
        DocumentSectionId.entries.forEach { section ->
            assertEquals(AppRoute.DocumentAt(section), AppRoute.parse(AppRoute.DocumentAt(section).hash))
        }
        assertEquals(AppRoute.Document, AppRoute.parse("#ausarbeitung"))
        assertNull(AppRoute.parse("#ausarbeitung/unknown"))
        assertNull(AppRoute.parse("#ausarbeitung/../herleitung"))
        assertEquals(DocumentSectionId.DERIVATION, DocumentSectionId.forStation(StationId.S05_DERIVATION))
        assertEquals(DocumentSectionId.JONSSON_MEASUREMENT, DocumentSectionId.forStation(StationId.S14_MEASUREMENT))
    }
    @Test fun answersScientificInputsAndOwnNativeCoordinatesSurviveRoundTrip() {
        val original = SessionSnapshot(station = StationId.S14_MEASUREMENT,
            visited = setOf(StationId.S05_DERIVATION, StationId.S14_MEASUREMENT),
            deBroglie = DeBroglieSession(raw = "invalid", lastValidVoltage = 600.0, comparisonVoltage = 150.0),
            work = mapOf(StationId.S16_CONCLUSION to StationWork(note = "Eigene Begründung, noch selbst zu prüfen.")),
            measurement = MeasurementSession(markers = listOf(NativeImageMark(550), NativeImageMark(565)), justification = "Eigenes Maximum im gewählten Band"))
        assertEquals(original, SessionSnapshot.fromJson(original.json()))
        assertEquals(15.0, (original.measurement.selection as materiewelle.core.MeasurementSelection.Ready).meanPx)
    }
    @Test fun changedImageAndFutureSchemaCannotSilentlyReapplyPixelMarks() {
        assertFailsWith<IllegalArgumentException> { SessionSnapshot(imageIdentity = SessionSnapshot.CANONICAL_IMAGE_IDENTITY.copy(sha256 = "invalid")).validate() }
        assertFailsWith<IllegalArgumentException> { SessionSnapshot(schemaVersion = 99).validate() }
        assertFailsWith<IllegalArgumentException> { SessionSnapshot(dataRevision = "999").validate() }
    }
    @Test fun changedLoadedImageClearsOnlyIncompatibleMeasurementWithNotice() {
        val measured = MeasurementSession().addMarker(550, image()).addMarker(565, image())
            .copy(justification = "Band und benachbarte Maxima selbst gewählt.").saveInitialSelection(image()).revealReference()
        val original = SessionSnapshot(station = StationId.S14_MEASUREMENT,
            measurement = measured, work = mapOf(StationId.S16_CONCLUSION to StationWork(note = "Meine bisherige Argumentation")))
        val actual = image().copy(imageSha256 = "a".repeat(64))
        val reconciled = original.withScientificData(scientific(actual))
        assertEquals(MeasurementSession(), reconciled.measurement)
        assertEquals(MeasurementResetReason.IMAGE_CHANGED, reconciled.measurementResetReason)
        assertTrue(reconciled.measurementImageChanged)
        assertEquals(original.work, reconciled.work)
        assertEquals(original.station, reconciled.station)
        assertEquals(actual.imageSha256, reconciled.imageIdentity!!.sha256)
        assertEquals(reconciled, SessionSnapshot.fromJson(reconciled.json()))
    }
    @Test fun unchangedActualImageKeepsMarksReferenceHistoryAndComparisons() {
        val own = MeasurementSession().addMarker(550, image()).addMarker(565, image())
            .copy(justification = "Eigene begründete Auswahl.").saveInitialSelection(image()).revealReference().saveComparison(image())
        val state = SessionSnapshot(measurement = own)
        assertEquals(state, state.withScientificData(scientific(image())))
    }
    @Test fun smallerActualRasterCanBeAdoptedMeasuredAndRestoredWithoutCanonicalSizeAssumptions() {
        val actual = image().copy(height = 80, markerY = 40.0, cropTop = 20.0, cropHeight = 40.0, imageSha256 = "b".repeat(64))
        val adopted = SessionSnapshot().withScientificData(scientific(actual))
        assertEquals(80, adopted.imageIdentity!!.nativeHeightPx)
        assertFalse(adopted.measurementImageChanged) // No previous own work was discarded.
        val own = MeasurementSession().addMarker(551, actual).addMarker(564, actual)
            .copy(justification = "Eigene Auswahl im importierten 80-Pixel-Raster.").saveInitialSelection(actual)
        val state = adopted.copy(measurement = own)
        val restored = SessionSnapshot.fromJson(state.json())
        assertEquals(state, restored)
        assertEquals(state, restored.withScientificData(scientific(actual)))
        assertEquals(NativeImageBand(20, 60), restored.measurement.nativeBand(restored.imageIdentity!!))
    }
    @Test fun absentImageCannotKeepOrReceiveUnrelatedPixelMeasurements() {
        val state = SessionSnapshot(measurement = MeasurementSession(markers = listOf(NativeImageMark(550))))
        val withoutImage = state.withScientificData(scientific(null))
        assertNull(withoutImage.imageIdentity)
        assertEquals(MeasurementResetReason.IMAGE_UNAVAILABLE, withoutImage.measurementResetReason)
        assertEquals(MeasurementSession(), withoutImage.measurement)
        assertEquals(withoutImage, SessionSnapshot.fromJson(withoutImage.json()))
        assertFailsWith<IllegalArgumentException> { withoutImage.copy(measurement = state.measurement).validate() }
    }
    @Test fun metadataAndSavedSnapshotFingerprintMustAgreeBeforeReconciliation() {
        val secured = MeasurementSession().addMarker(551, image()).addMarker(564, image())
            .copy(justification = "Eigene Wahl.").saveInitialSelection(image())
        assertFailsWith<IllegalArgumentException> {
            SessionSnapshot(imageIdentity = SessionSnapshot.CANONICAL_IMAGE_IDENTITY.copy(sha256 = "a".repeat(64)), measurement = secured).validate()
        }
        val own = SessionSnapshot(measurement = secured)
        val recropped = image().copy(cropLeft = 540.0, cropWidth = 163.0)
        assertEquals(MeasurementResetReason.IMAGE_CHANGED, own.withScientificData(scientific(recropped)).measurementResetReason)
    }
    @Test fun corruptedOversizedAndUnknownStateIsRejected() {
        assertFailsWith<IllegalArgumentException> { SessionSnapshot.fromJson("{") }
        assertFailsWith<IllegalArgumentException> { SessionSnapshot.fromJson(" ".repeat(SessionSnapshot.MAX_JSON_LENGTH + 1)) }
        assertFailsWith<IllegalArgumentException> { SessionSnapshot.fromJson("{\"invented\":1}") }
        assertFailsWith<IllegalArgumentException> { SessionSnapshot(work = mapOf(StationId.S01_OBSERVATION to StationWork(note = "x".repeat(4001)))).validate() }
    }
    @Test fun editedTextAndLastValidCalculationRemainDistinct() {
        val valid = DeBroglieSession().edit("600")
        val invalid = valid.edit("NaN")
        assertEquals("NaN", invalid.raw); assertEquals(600.0, invalid.lastValidVoltage)
        assertEquals(DeBroglieSession().comparisonVoltage, invalid.comparisonVoltage)
        assertFailsWith<IllegalArgumentException> { invalid.copy(lastValidVoltage = Double.POSITIVE_INFINITY).validate() }
        assertFailsWith<IllegalArgumentException> { DgSession(comparisonVoltage = 54.0, comparisonAngle = null).validate() }
    }
    @Test fun progressAndAnswerStatusDoNotPretendFreeTextIsScientificallyGraded() {
        val work = StationWork(note = "Ein eigener Versuch", markedWorked = true)
        assertFalse(work.checked)
        assertFailsWith<IllegalArgumentException> { QuestionAttempt(checked = true).validate() }
        assertFailsWith<IllegalArgumentException> { QuestionAttempt(nextReviewAt = -1).validate() }
        assertEquals(16, StationId.entries.size)
    }
}
