package materiewelle.state

import materiewelle.core.course.*
import materiewelle.core.MeasurementSession
import materiewelle.core.NativeImageMark
import materiewelle.core.ReferenceMaterial
import kotlin.test.*

class SessionStorageTest {
    @Test fun courseResetClearsAllAnswersButPreservesLaboratoryProvenanceAndSettings() {
        val backend = MemoryBackend()
        val session = LabSession(SessionStorage(backend, ExclusiveTestWriter))
        val inquiry = InquiryState(
            waveAmplitude = 30.0, phaseTurns = .25, momentumFactor = 2.0,
            superpositionPrediction = "QA: eigene Vorhersage", superpositionRevealed = true,
            opticsPrediction = OpticsStage.OPTICS, opticsRevealed = true,
            geometryPrediction = GeometryPathChoice.BOTH,
            crystalAssignment = CrystalAssignmentChoice.ROWS, crystalRevealed = true,
            limitAssignments = mapOf(LimitCaseId.SCAN_SCALE to LimitKind.CALIBRATION), limitsChecked = true,
            slitComparison = SlitModelComparison(spacingUm = 3.0, widthUm = .6),
            evidenceRows = listOf(EvidenceRow(claim = "QA: eigene Behauptung"), EvidenceRow(), EvidenceRow()),
        )
        session.update { it.copy(inquiry = inquiry, modelSlitSpacingUm = 3.0,
            deBroglie = DeBroglieSession(raw = "240", lastValidVoltage = 240.0),
            dg = DgSession(modelAngleRaw = "45", validModelAngle = 45.0),
            measurement = MeasurementSession(markers = listOf(NativeImageMark(550), NativeImageMark(580)),
                justification = "QA: zwei eigene native Marken",
                referenceMaterialsOpened = setOf(ReferenceMaterial.PDF_REFERENCE)),
        ) }
        session.editWork(StationId.S16_CONCLUSION) { it.copy(note = "QA: eigenes Fazit", markedWorked = true) }
        session.answer(QuestionId.Q02, QuestionOptionId.B)
        session.editPractice(PracticeCaseId.U240, "79 pm")
        val before = session.snapshot
        session.resetCourse()
        val reset = session.snapshot
        assertEquals(InquiryState(waveAmplitude = 30.0, phaseTurns = .25, momentumFactor = 2.0,
            slitComparison = inquiry.slitComparison), reset.inquiry)
        assertTrue(reset.visited.isEmpty())
        assertTrue(reset.work.isEmpty())
        assertTrue(reset.questions.isEmpty())
        assertTrue(reset.practice.isEmpty())
        assertNull(reset.activeQuestion)
        assertEquals(before.deBroglie, reset.deBroglie)
        assertEquals(before.dg, reset.dg)
        assertEquals(before.measurement, reset.measurement)
        assertEquals(before.imageIdentity, reset.imageIdentity)
        assertEquals(before.modelSlitSpacingUm, reset.modelSlitSpacingUm)
        assertEquals(reset, LabSession(SessionStorage(backend, ExclusiveTestWriter)).snapshot)
    }

    private class MemoryBackend : SessionStorageBackend {
        val values = mutableMapOf<String, String>()
        val writes = mutableListOf<String>()
        var failRead = false
        var failWriteKey: String? = null
        var failRemoveKey: String? = null
        override fun read(key: String): String? {
            if (failRead) error("storage unavailable")
            return values[key]
        }
        override fun write(key: String, value: String) {
            if (failWriteKey == key) error("quota exceeded")
            writes += key
            values[key] = value
        }
        override fun remove(key: String) {
            if (failRemoveKey == key) error("deletion unavailable")
            values.remove(key)
        }
    }
    @Test fun migrationBacksUpBeforeReplacingTheSameStorageKey() {
        val backend = MemoryBackend()
        val legacy = """{"work":{"S06_CALCULATION":{"note":"Originalnotiz","checked":true}}}"""
        backend.values[SessionStorage.KEY] = legacy
        val storage = SessionStorage(backend, ExclusiveTestWriter)
        val result = storage.restore()
        assertTrue(result.migrated)
        assertEquals(StorageHealth.SAVED, result.health)
        assertEquals(listOf(SessionStorage.BACKUP_KEY, SessionStorage.KEY), backend.writes)
        assertEquals(legacy, backend.values[SessionStorage.BACKUP_KEY])
        assertEquals(legacy, result.backup)
        val migrated = SessionSnapshot.fromJson(backend.values.getValue(SessionStorage.KEY))
        assertEquals("Originalnotiz", migrated.stationWork(StationId.S06_CALCULATION).note)
        assertFalse(migrated.stationWork(StationId.S06_CALCULATION).solutionViewed)
        assertEquals(2, migrated.schemaVersion)
    }
    @Test fun unknownOrBrokenOriginalCannotBeOverwrittenByAutomaticVisitOrAnEdit() {
        val future = SessionSnapshot().json().replace("\"schemaVersion\":2", "\"schemaVersion\":99")
        listOf(future, "{", """{"invented":true}""").forEach { original ->
            val backend = MemoryBackend().apply { values[SessionStorage.KEY] = original }
            val session = LabSession(SessionStorage(backend, ExclusiveTestWriter))
            session.visit(StationId.S02_CLASSICAL)
            session.editWork(StationId.S02_CLASSICAL) { it.copy(note = "Neue Notiz im Arbeitsspeicher") }
            assertEquals(original, backend.values[SessionStorage.KEY])
            assertTrue(backend.writes.isEmpty())
            assertEquals(StorageHealth.REJECTED, session.health)
            assertEquals(original, session.rejectedBackup)
            assertEquals("Neue Notiz im Arbeitsspeicher", session.snapshot.stationWork(StationId.S02_CLASSICAL).note)
            assertTrue(session.storageMessage.contains("geschützt"))
            assertEquals(session.snapshot, SessionSnapshot.fromJson(session.snapshot.json()))
        }
    }
    @Test fun unavailableOrFailedBackupNeverPermitsReplacingV1() {
        val original = """{"work":{"S01_OBSERVATION":{"note":"Unersetzbare alte Notiz"}}}"""
        val backend = MemoryBackend().apply {
            values[SessionStorage.KEY] = original
            failWriteKey = SessionStorage.BACKUP_KEY
        }
        val storage = SessionStorage(backend, ExclusiveTestWriter)
        val session = LabSession(storage)
        assertTrue(storage.sourceProtected)
        assertEquals(StorageHealth.MEMORY_ONLY, session.health)
        assertEquals(original, session.rejectedBackup)
        session.visit(StationId.S03_LIGHT)
        assertEquals(original, backend.values[SessionStorage.KEY])
        assertTrue(backend.writes.isEmpty())
        assertEquals("Unersetzbare alte Notiz", session.snapshot.stationWork(StationId.S01_OBSERVATION).note)
        val inaccessible = SessionStorage(MemoryBackend().apply { failRead = true }, ExclusiveTestWriter)
        assertEquals(StorageHealth.MEMORY_ONLY, inaccessible.restore().health)
        assertFalse(inaccessible.save(SessionSnapshot()))
    }
    @Test fun failedCurrentWriteKeepsTheSingleOriginalBackup() {
        val legacy = "{}"
        val backend = MemoryBackend().apply {
            values[SessionStorage.KEY] = legacy
            failWriteKey = SessionStorage.KEY
        }
        val storage = SessionStorage(backend, ExclusiveTestWriter)
        val result = storage.restore()
        assertEquals(StorageHealth.MEMORY_ONLY, result.health)
        assertEquals(legacy, backend.values[SessionStorage.KEY])
        assertEquals(legacy, backend.values[SessionStorage.BACKUP_KEY])
        assertFalse(storage.save(result.snapshot))
        assertEquals(listOf(SessionStorage.BACKUP_KEY), backend.writes)
    }
    @Test fun aLargeRejectedOriginalStaysIntactWithoutCreatingAnUnboundedBackup() {
        val original = "x".repeat(SessionStorage.MAX_BACKUP_LENGTH + 1)
        val backend = MemoryBackend().apply { values[SessionStorage.KEY] = original }
        val session = LabSession(SessionStorage(backend, ExclusiveTestWriter))
        assertNull(session.rejectedBackup)
        session.visit(StationId.S02_CLASSICAL)
        assertEquals(original, backend.values[SessionStorage.KEY])
        assertTrue(backend.writes.isEmpty())
    }
    @Test fun firstSelectionSurvivesCorrectionAndReopeningAtCurrentRevision() {
        val backend = MemoryBackend()
        val session = LabSession(SessionStorage(backend, ExclusiveTestWriter))
        session.answer(QuestionId.Q02, QuestionOptionId.A)
        session.check(QuestionId.Q02, 0)
        session.answer(QuestionId.Q02, QuestionOptionId.B)
        val revised = session.snapshot.questionWork(QuestionId.Q02)
        assertEquals(QuestionOptionId.A, revised.firstSelected)
        assertFalse(revised.checked)
        assertTrue(revised.isCurrentContent)
        session.reopenQuestion(QuestionId.Q02)
        session.answer(QuestionId.Q02, QuestionOptionId.C)
        assertEquals(QuestionOptionId.A, session.snapshot.questionWork(QuestionId.Q02).firstSelected)
        assertEquals(session.snapshot, SessionSnapshot.fromJson(backend.values.getValue(SessionStorage.KEY)))
    }
    @Test fun legacyCheckedQuestionStaysHistoricalUntilAnExplicitNewAction() {
        val backend = MemoryBackend().apply {
            values[SessionStorage.KEY] = """{"questions":{"Q02":{"selected":"B","checked":true,"attempts":1}}}"""
        }
        val session = LabSession(SessionStorage(backend, ExclusiveTestWriter))
        assertTrue(session.snapshot.questionWork(QuestionId.Q02).isHistorical)
        assertNull(session.snapshot.questionWork(QuestionId.Q02).firstSelected)
        session.visit(StationId.S06_CALCULATION)
        assertTrue(session.snapshot.questionWork(QuestionId.Q02).isHistorical)
        session.answer(QuestionId.Q02, QuestionOptionId.C)
        assertTrue(session.snapshot.questionWork(QuestionId.Q02).isCurrentContent)
        assertFalse(session.snapshot.questionWork(QuestionId.Q02).checked)
        assertEquals(QuestionOptionId.C, session.snapshot.questionWork(QuestionId.Q02).firstSelected)
    }
    @Test fun independentPracticeStatePersistsAndAnEditInvalidatesTheCheck() {
        val backend = MemoryBackend()
        val session = LabSession(SessionStorage(backend, ExclusiveTestWriter))
        session.editPractice(PracticeCaseId.U240, "70 pm")
        session.checkPractice(PracticeCaseId.U240)
        session.revealPracticeSolution(PracticeCaseId.U240)
        session.editPractice(PracticeCaseId.U240, "79 pm")
        val state = session.snapshot.practiceWork(PracticeCaseId.U240)
        assertEquals("70 pm", state.firstSubmission)
        assertFalse(state.checked)
        assertTrue(state.solutionViewed)
        assertEquals(PracticeAttempt(), session.snapshot.practiceWork(PracticeCaseId.U375))
        assertEquals(session.snapshot, SessionSnapshot.fromJson(backend.values.getValue(SessionStorage.KEY)))
    }

    private fun importedWork() = SessionSnapshot(
        station = StationId.S16_CONCLUSION,
        work = mapOf(StationId.S16_CONCLUSION to StationWork(note = "QA: importiertes eigenes Fazit")),
        measurement = MeasurementSession(markers = listOf(NativeImageMark(560), NativeImageMark(590)),
            justification = "QA: importierte Auswahl", referenceMaterialsOpened = setOf(ReferenceMaterial.PDF_REFERENCE)),
    )

    @Test fun previewDoesNotWriteAndConfirmedImportBacksUpThenRoundTrips() {
        val backend = MemoryBackend()
        val session = LabSession(SessionStorage(backend, ExclusiveTestWriter))
        session.editWork(StationId.S01_OBSERVATION) { it.copy(note = "QA: bisherige eigene Arbeit") }
        val before = session.snapshot
        val target = importedWork()
        backend.writes.clear()
        assertTrue(session.previewImport(target.json()))
        assertEquals(before, session.snapshot)
        assertTrue(backend.writes.isEmpty())
        assertEquals(target, session.importPreview!!.snapshot)
        assertTrue(session.confirmImport())
        assertEquals(listOf(SessionStorage.IMPORT_BACKUP_KEY, SessionStorage.KEY), backend.writes)
        assertEquals(before, SessionSnapshot.fromJson(backend.values.getValue(SessionStorage.IMPORT_BACKUP_KEY)))
        assertEquals(target, session.snapshot)
        val reopened = LabSession(SessionStorage(backend, ExclusiveTestWriter))
        assertEquals(target, reopened.snapshot)
        assertEquals(before.json(), reopened.importBackup)
        assertNull(session.importPreview)
        assertFalse(session.confirmImport())
    }

    @Test fun invalidMismatchedOrCancelledImportDoesNotReplaceAnyExistingData() {
        val backend = MemoryBackend()
        val session = LabSession(SessionStorage(backend, ExclusiveTestWriter))
        session.editWork(StationId.S06_CALCULATION) { it.copy(note = "QA: behalten") }
        val before = session.snapshot
        val stored = backend.values.toMap()
        backend.writes.clear()
        for (invalid in listOf("{", importedWork().json().replace(SessionSnapshot.COURSE_REVISION, "future"),
            importedWork().json().replace(SessionSnapshot.IMAGE_SHA256, "b".repeat(64)))) {
            assertFalse(session.previewImport(invalid))
            assertNull(session.importPreview)
            assertEquals(before, session.snapshot)
            assertEquals(stored, backend.values)
        }
        assertTrue(session.previewImport(importedWork().json()))
        session.cancelImport()
        assertFalse(session.confirmImport())
        assertTrue(backend.writes.isEmpty())
        assertEquals(stored, backend.values)
    }

    @Test fun importCannotReplaceAProtectedUnknownOriginal() {
        val original = "{unknown future content}"
        val backend = MemoryBackend().apply { values[SessionStorage.KEY] = original }
        val session = LabSession(SessionStorage(backend, ExclusiveTestWriter))
        assertFalse(session.previewImport(importedWork().json()))
        assertFalse(session.confirmImport())
        assertEquals(original, backend.values[SessionStorage.KEY])
        assertEquals(original, session.rejectedBackup)
        assertTrue(backend.writes.isEmpty())
    }

    @Test fun failedBackupOrCommitKeepsTheActiveSnapshotAndStoredOriginal() {
        for (failedKey in listOf(SessionStorage.IMPORT_BACKUP_KEY, SessionStorage.KEY)) {
            val backend = MemoryBackend()
            val session = LabSession(SessionStorage(backend, ExclusiveTestWriter))
            session.editWork(StationId.S01_OBSERVATION) { it.copy(note = "QA: nicht verlieren") }
            val before = session.snapshot
            val original = backend.values.getValue(SessionStorage.KEY)
            assertTrue(session.previewImport(importedWork().json()))
            backend.failWriteKey = failedKey
            backend.writes.clear()
            assertFalse(session.confirmImport())
            assertEquals(before, session.snapshot)
            assertEquals(original, backend.values[SessionStorage.KEY])
            if (failedKey == SessionStorage.IMPORT_BACKUP_KEY) assertTrue(backend.writes.isEmpty())
            else assertEquals(before.json(), session.importBackup)
        }
    }

    @Test fun previewIsInvalidatedByOwnEditsOrAnotherTabsWrite() {
        val backend = MemoryBackend()
        val session = LabSession(SessionStorage(backend, ExclusiveTestWriter))
        session.editWork(StationId.S01_OBSERVATION) { it.copy(note = "QA: erste Notiz") }
        assertTrue(session.previewImport(importedWork().json()))
        session.editWork(StationId.S01_OBSERVATION) { it.copy(note = "QA: Änderung während Vorschau") }
        assertFalse(session.confirmImport())
        assertEquals("QA: Änderung während Vorschau", session.snapshot.stationWork(StationId.S01_OBSERVATION).note)
        assertTrue(session.previewImport(importedWork().json()))
        val otherTab = SessionSnapshot(work = mapOf(StationId.S01_OBSERVATION to StationWork(note = "QA: anderer Tab"))).json()
        backend.values[SessionStorage.KEY] = otherTab
        backend.writes.clear()
        assertFalse(session.confirmImport())
        assertEquals(otherTab, backend.values[SessionStorage.KEY])
        assertTrue(backend.writes.isEmpty())
        assertFalse(session.previewImport(importedWork().json()))
    }

    @Test fun unsuccessfulFirstImportBackupIsStillDownloadableAfterReload() {
        val backend = MemoryBackend()
        val session = LabSession(SessionStorage(backend, ExclusiveTestWriter))
        assertTrue(session.previewImport(importedWork().json()))
        backend.failWriteKey = SessionStorage.KEY
        assertFalse(session.confirmImport())
        assertEquals(SessionSnapshot().json(), session.importBackup)
        assertEquals(session.importBackup, LabSession(SessionStorage(backend, ExclusiveTestWriter)).importBackup)
    }

    @Test fun fullDeletionRemovesOnlyProjectKeysAndDoesNotRecreateOldWork() {
        val backend = MemoryBackend()
        val session = LabSession(SessionStorage(backend, ExclusiveTestWriter))
        session.editWork(StationId.S01_OBSERVATION) { it.copy(note = "QA: zu löschen") }
        SessionStorage.PROJECT_KEYS.filter { it != SessionStorage.KEY }.forEach { backend.values[it] = "QA: local data" }
        backend.values["other-application.preferences"] = "preserve"
        assertTrue(session.deleteAllLocalData())
        assertTrue(session.localDataDeleted)
        assertEquals(SessionSnapshot(), session.snapshot)
        assertNull(session.importBackup)
        assertNull(session.rejectedBackup)
        assertEquals(mapOf("other-application.preferences" to "preserve"), backend.values)
        backend.writes.clear()
        session.visit(StationId.S02_CLASSICAL)
        assertTrue(backend.writes.isEmpty())
        assertFalse(session.previewImport(importedWork().json()))
        assertEquals(SessionSnapshot(), LabSession(SessionStorage(backend, ExclusiveTestWriter)).snapshot)
    }

    @Test fun incompleteDeletionReportsFailureAndStopsAutomaticWritesUntilReload() {
        val backend = MemoryBackend()
        val session = LabSession(SessionStorage(backend, ExclusiveTestWriter))
        session.editWork(StationId.S01_OBSERVATION) { it.copy(note = "QA: im Speicher behalten") }
        val before = session.snapshot
        backend.failRemoveKey = SessionStorage.KEY
        assertFalse(session.deleteAllLocalData())
        assertFalse(session.localDataDeleted)
        assertEquals(before, session.snapshot)
        backend.writes.clear()
        session.visit(StationId.S02_CLASSICAL)
        assertTrue(backend.writes.isEmpty())
        assertEquals(before, SessionSnapshot.fromJson(backend.values.getValue(SessionStorage.KEY)))
        backend.failRemoveKey = null
        assertTrue(session.deleteAllLocalData())
    }
}
