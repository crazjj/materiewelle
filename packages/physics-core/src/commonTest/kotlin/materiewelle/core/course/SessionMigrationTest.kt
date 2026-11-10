package materiewelle.core.course

import materiewelle.core.*
import kotlinx.serialization.json.*
import kotlin.test.*

class SessionMigrationTest {
    private val legacy = """{
      "schemaVersion":1,"courseRevision":"abgabe-2026-10-14","dataRevision":"2.0",
      "station":"S14_MEASUREMENT","visited":["S06_CALCULATION","S14_MEASUREMENT"],
      "work":{"S06_CALCULATION":{"note":"Mein ursprünglicher Rechenweg","numericAnswer":"100 pm","checked":true,"transferAnswer":"50 pm","transferChecked":true}},
      "questions":{"Q02":{"selected":"A","checked":true,"attempts":2,"nextReviewAt":1000}},
      "measurement":{"band":"LOWER","markers":[{"xPx":578},{"xPx":592},{"xPx":606}],"justification":"Eigene Maxima, altes Band unprotokolliert", "referenceViewed":true,
        "initialSelection":{"band":"BASE","markers":[{"xPx":578},{"xPx":592}],"justification":"Erstwahl vor Laborreferenz","referenceViewedAtCapture":false,"imageSha256":"d8952b3336cb499884f2cbe600cc15645f8bc1c406b1c4e7f43f3bc4d9553bb6","nativeWidthPx":703,"nativeHeightPx":242},
        "comparison":{"band":"LOWER","markers":[{"xPx":578},{"xPx":593}],"justification":"Späterer Vergleich","referenceViewedAtCapture":true,"imageSha256":"d8952b3336cb499884f2cbe600cc15645f8bc1c406b1c4e7f43f3bc4d9553bb6","nativeWidthPx":703,"nativeHeightPx":242}}
    }"""
    @Test fun v1MigratesWithoutInventingNewChecksOrProvenance() {
        val result = SessionMigration.restore(legacy)
        assertTrue(result.migrated)
        assertEquals(1, result.sourceSchemaVersion)
        val state = result.snapshot
        assertEquals(2, state.schemaVersion)
        assertEquals("abgabe-2026-10-16", state.courseRevision)
        val work = state.stationWork(StationId.S06_CALCULATION)
        assertEquals("Mein ursprünglicher Rechenweg", work.note)
        assertTrue(work.checked)
        assertFalse(work.solutionViewed)
        assertFalse(work.transferSolutionViewed)
        assertEquals(emptyMap(), state.practice)
        val question = state.questionWork(QuestionId.Q02)
        assertEquals(QuestionOptionId.A, question.selected)
        assertNull(question.firstSelected) // V1 never knew the actual first choice.
        assertTrue(question.isHistorical)
        assertFalse(question.isCurrentContent)
        assertEquals(2, question.attempts)
        assertEquals(listOf(578, 592, 606), state.measurement.markers.map { it.xPx })
        assertNull(state.measurement.justificationBand)
        assertFalse(state.measurement.canSave)
        assertTrue(state.measurement.referenceViewed)
        assertEquals(setOf(ReferenceMaterial.UNKNOWN_LEGACY), state.measurement.referenceMaterialsOpened)
        val first = state.measurement.initialSelection!!
        assertEquals(listOf(578, 592), first.markers.map { it.xPx })
        assertFalse(first.referenceViewedAtCapture)
        assertEquals(setOf(ReferenceMaterial.UNKNOWN_LEGACY), first.priorReferenceMaterials)
        assertTrue(state.measurement.comparison!!.referenceViewedAtCapture)
        assertEquals(state, SessionSnapshot.fromJson(state.json()))
    }
    @Test fun legacyDerivationRemembersOnlyItsDocumentedCurrentStep() {
        val state = SessionSnapshot.fromJson("""{"work":{"S10_GEOMETRY":{"explanationStep":2}}}""")
        assertEquals(2, state.stationWork(StationId.S10_GEOMETRY).explanationFurthestStep)
        val current = state.copy(work = state.work + (StationId.S10_GEOMETRY to state.stationWork(StationId.S10_GEOMETRY).copy(explanationStep = 0)))
        assertEquals(2, SessionSnapshot.fromJson(current.json()).stationWork(StationId.S10_GEOMETRY).explanationFurthestStep)
        assertFailsWith<IllegalArgumentException> { StationWork(explanationFurthestStep = 21).validate() }
    }
    @Test fun sourceVersionsAndNestedFieldsAreStrictlyChecked() {
        assertFailsWith<IllegalArgumentException> { SessionMigration.restore(legacy.replace("\"schemaVersion\":1", "\"schemaVersion\":3")) }
        assertFailsWith<IllegalArgumentException> { SessionMigration.restore(legacy.replace("\"dataRevision\":\"2.0\"", "\"dataRevision\":\"3.0\"")) }
        assertFailsWith<IllegalArgumentException> { SessionMigration.restore(legacy.replace("\"attempts\":2", "\"attempts\":2,\"unrecognized\":true")) }
        assertFailsWith<IllegalArgumentException> { SessionMigration.restore(legacy.replace("\"markers\":[{\"xPx\":578},{\"xPx\":592},{\"xPx\":606}]", "\"markers\":[{\"xPx\":100}]")) }
        assertFailsWith<IllegalArgumentException> { SessionMigration.restore(legacy.replace("Erstwahl vor Laborreferenz\",\"referenceViewedAtCapture\":false", "Erstwahl vor Laborreferenz\",\"referenceViewedAtCapture\":true")) }
        val current = Json.parseToJsonElement(SessionSnapshot().json()).jsonObject
        val inquiryWithUnknownKey = JsonObject(current.getValue("inquiry").jsonObject + ("futureField" to JsonPrimitive(1)))
        val invalid = JsonObject(current + ("inquiry" to inquiryWithUnknownKey)).toString()
        assertFailsWith<IllegalArgumentException> { SessionMigration.restore(invalid) }
    }
    @Test fun v2DoesNotMigrateAgainAndDoesNotInventInquirySuccess() {
        val initial = SessionSnapshot(inquiry = InquiryState(phaseTurns = 0.5,
            opticsPrediction = OpticsStage.REPRODUCTION, reproductionScale = 2.0),
            practice = mapOf(PracticeCaseId.U240 to PracticeAttempt(draft = "80 pm")))
        val result = SessionMigration.restore(initial.json())
        assertFalse(result.migrated)
        assertEquals(initial, result.snapshot)
        assertFalse(result.snapshot.practiceWork(PracticeCaseId.U240).checked)
        assertEquals(PracticeAttempt(), result.snapshot.practiceWork(PracticeCaseId.U375))
    }
    @Test fun inquiryBoundsAndNonFiniteValuesAreRejected() {
        assertFailsWith<IllegalArgumentException> { InquiryState(waveAmplitude = 46.0).validate() }
        assertFailsWith<IllegalArgumentException> { InquiryState(phaseTurns = Double.NaN).validate() }
        assertFailsWith<IllegalArgumentException> { InquiryState(reproductionScale = 4.0).validate() }
        assertFailsWith<IllegalArgumentException> { InquiryState(momentumFactor = 0.0).validate() }
        assertFailsWith<IllegalArgumentException> { InquiryState(superpositionPrediction = "x".repeat(1001)).validate() }
        assertFailsWith<IllegalArgumentException> { SlitModelComparison(spacingUm = 2.0, widthUm = Double.POSITIVE_INFINITY).validate() }
    }
    @Test fun limitsAndOwnEvidenceRemainTypedAndBoundedAcrossReload() {
        val inquiry = InquiryState(limitAssignments = mapOf(LimitCaseId.NR_50KV to LimitKind.MODEL), limitsChecked = true,
            evidenceRows = listOf(EvidenceRow("DG", "Winkelsignal", "Strukturannahme"), EvidenceRow(), EvidenceRow()))
        val state = SessionSnapshot(inquiry = inquiry)
        assertEquals(state, SessionSnapshot.fromJson(state.json()))
        assertFailsWith<IllegalArgumentException> { inquiry.copy(evidenceRows = emptyList()).validate() }
        assertFailsWith<IllegalArgumentException> { inquiry.copy(evidenceRows = listOf(EvidenceRow(claim = "x".repeat(1001)), EvidenceRow(), EvidenceRow())).validate() }
    }
    @Test fun imageFingerprintMismatchIsRejectedBeforeMigration() {
        assertFailsWith<IllegalArgumentException> {
            SessionMigration.restore(legacy.replace("\"nativeWidthPx\":703", "\"nativeWidthPx\":702"))
        }
    }

    @Test fun currentWireIdentitiesMustBeExplicitAndKnownEvenWithoutASchemaChange() {
        val source = SessionSnapshot().json()
        for (invalid in listOf(
            source.replace(SessionSnapshot.COURSE_REVISION, "future-course"),
            source.replace("\"dataRevision\":\"2.0\"", "\"dataRevision\":\"3.0\""),
            source.replace("\"courseRevision\":\"${SessionSnapshot.COURSE_REVISION}\",", ""),
            source.replace("\"dataRevision\":\"2.0\",", ""),
            source.replace("\"schemaVersion\":2", "\"schemaVersion\":\"2\""),
        )) assertFailsWith<IllegalArgumentException> { SessionMigration.restore(invalid) }
        assertEquals(SessionSnapshot(), SessionMigration.restore(source).snapshot)
    }

    @Test fun importPreviewPreservesOwnWorkAndDoesNotInventSolutionHistory() {
        val snapshot = SessionSnapshot(
            station = StationId.S14_MEASUREMENT,
            work = mapOf(StationId.S06_CALCULATION to StationWork(note = "QA: mein Entwurf", solutionViewed = true)),
            questions = mapOf(QuestionId.Q02 to QuestionAttempt(selected = QuestionOptionId.A, checked = true,
                nextReviewAt = 1234, attempts = 2, firstSelected = QuestionOptionId.C, contentRevision = 2)),
            measurement = MeasurementSession(markers = listOf(NativeImageMark(550), NativeImageMark(580)),
                justification = "QA: meine Auswahl", referenceMaterialsOpened = setOf(ReferenceMaterial.PDF_REFERENCE)),
        )
        val preview = SessionImportPreview.read(snapshot.json(), SessionSnapshot.CANONICAL_IMAGE_IDENTITY)
        assertEquals(snapshot, preview.snapshot)
        assertEquals(1, preview.stationRecords)
        assertEquals(1, preview.questionRecords)
        assertEquals(2, preview.ownMarkers)
        assertFalse(preview.migrated)
        assertEquals(snapshot, SessionSnapshot.fromJson(preview.snapshot.json()))
        val old = SessionImportPreview.read(legacy, SessionSnapshot.CANONICAL_IMAGE_IDENTITY)
        assertTrue(old.migrated)
        assertTrue(old.snapshot.questionWork(QuestionId.Q02).isHistorical)
        assertNull(old.snapshot.questionWork(QuestionId.Q02).firstSelected)
    }

    @Test fun fileImportNeverSilentlyDropsMeasurementsWithADifferentImageIdentity() {
        val source = SessionSnapshot()
        val image = SessionSnapshot.CANONICAL_IMAGE_IDENTITY
        for (otherImage in listOf(image.copy(sha256 = "a".repeat(64)),
            image.copy(nativeWidthPx = 704), image.copy(cropLeftInclusivePx = 491), null)) {
            assertFailsWith<IllegalArgumentException> {
                SessionImportPreview.read(source.json(), otherImage)
            }
        }
        assertFailsWith<IllegalArgumentException> {
            SessionImportPreview.read(" ".repeat(SessionSnapshot.MAX_JSON_LENGTH + 1), image)
        }
        assertFailsWith<IllegalArgumentException> { SessionImportPreview.read("{", image) }
    }
}
