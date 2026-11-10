package materiewelle.state

import kotlin.test.*
import materiewelle.core.course.*
import materiewelle.presentation.PresentationKeys

/** Only in tests: an in-memory backend has no external writers unless a test creates one. */
internal object ExclusiveTestWriter : SessionWritePermit { override val access = WriteAccess.EXCLUSIVE }

class SessionWriterTest {
    private class Locks : SessionLock {
        var owner = false
        var requests = 0
        var supported = true
        var fail = false
        override fun request(acquired: ((() -> Unit)?) -> Unit, failed: () -> Unit): Boolean {
            requests++
            if (!supported) return false
            if (fail) { failed(); return true }
            if (owner) acquired(null)
            else { owner = true; acquired { owner = false } }
            return true
        }
    }
    private class Backend : SessionStorageBackend {
        val values = mutableMapOf<String, String>()
        var writes = 0
        var removals = 0
        override fun read(key: String) = values[key]
        override fun write(key: String, value: String) { values[key] = value; writes++ }
        override fun remove(key: String) { values.remove(key); removals++ }
    }

    @Test fun oneWholeSessionOwnsTheLockAndDeniedTabCannotTakeOverStaleWork() {
        val locks = Locks()
        val first = SessionWriter(locks); val second = SessionWriter(locks)
        assertEquals(WriteAccess.INITIALIZING, first.access)
        first.start(); second.start()
        assertEquals(WriteAccess.EXCLUSIVE, first.access)
        assertEquals(WriteAccess.OTHER_TAB, second.access)
        first.close()
        assertEquals(WriteAccess.RELEASED, first.access)
        second.start()
        assertEquals(WriteAccess.OTHER_TAB, second.access)
        assertEquals(2, locks.requests)
        val fresh = SessionWriter(locks); fresh.start()
        assertEquals(WriteAccess.EXCLUSIVE, fresh.access)
    }

    @Test fun unavailableOrFailedLockNeverFallsBackToUncoordinatedWriting() {
        listOf(Locks().apply { supported = false }, Locks().apply { fail = true }).forEach { locks ->
            val writer = SessionWriter(locks); writer.start()
            assertEquals(WriteAccess.UNAVAILABLE, writer.access)
            val backend = Backend()
            val session = LabSession(SessionStorage(backend, writer))
            session.editWork(StationId.S06_CALCULATION) { it.copy(note = "Memory-only work") }
            assertEquals(StorageHealth.MEMORY_ONLY, session.health)
            assertEquals(0, backend.writes)
            assertEquals("Memory-only work", session.snapshot.stationWork(StationId.S06_CALCULATION).note)
        }
    }

    @Test fun closingDuringAcquisitionReleasesLateLeaseAndCannotReopenIt() {
        var deliver: (((() -> Unit)?) -> Unit)? = null
        var released = false
        val writer = SessionWriter(object : SessionLock {
            override fun request(acquired: ((() -> Unit)?) -> Unit, failed: () -> Unit): Boolean {
                deliver = acquired; return true
            }
        })
        writer.start(); writer.close()
        deliver!! { released = true }
        assertTrue(released)
        assertEquals(WriteAccess.RELEASED, writer.access)
        writer.start()
        assertEquals(WriteAccess.RELEASED, writer.access)
    }

    @Test fun newSecondTabIsReadOnlyAndCannotAutosaveImportMigrateOrDelete() {
        val locks = Locks()
        val first = SessionWriter(locks).apply { start() }
        val second = SessionWriter(locks).apply { start() }
        val backend = Backend()
        val stored = SessionSnapshot(work = mapOf(StationId.S06_CALCULATION to StationWork(note = "Stored answer"))).json()
        backend.values[SessionStorage.KEY] = stored
        val session = LabSession(SessionStorage(backend, second))
        session.visit(StationId.S16_CONCLUSION)
        session.editWork(StationId.S06_CALCULATION) { it.copy(note = "Other tab answer") }
        assertEquals(StorageHealth.OTHER_TAB, session.health)
        assertTrue(session.isReadOnly)
        assertFalse(session.previewImport(SessionSnapshot().json()))
        assertFalse(session.confirmImport())
        assertFalse(session.deleteAllLocalData())
        assertEquals(stored, session.storedSnapshotForExport())
        assertEquals("Stored answer", SessionSnapshot.fromJson(session.snapshot.json()).stationWork(StationId.S06_CALCULATION).note)
        assertEquals(0, backend.writes)
        assertEquals(0, backend.removals)
        backend.values[SessionStorage.KEY] = "{}"
        val legacy = SessionStorage(backend, second).restore()
        assertTrue(legacy.migrated)
        assertEquals("{}", backend.values[SessionStorage.KEY])
        assertNull(backend.values[SessionStorage.BACKUP_KEY])
        first.close()
        session.editWork(StationId.S06_CALCULATION) { it.copy(note = "Still not a writer") }
        assertEquals(0, backend.writes)
    }

    @Test fun sessionAndQuestionsLoseWritePermissionTogetherOnPageLeave() {
        val writer = SessionWriter(Locks()).apply { start() }
        val backend = Backend()
        val session = LabSession(SessionStorage(backend, writer))
        val drafts = UiDraftStorage(backend, writer)
        val initialDrafts = drafts.restore()
        session.editWork(StationId.S06_CALCULATION) { it.copy(note = "Saved work") }
        drafts.save(initialDrafts.copy(questions = mapOf(AppSection.DE_BROGLIE to "Saved question")))
        val saved = backend.values.toMap()
        val writesBefore = backend.writes
        writer.close()
        session.editWork(StationId.S06_CALCULATION) { it.copy(note = "New memory work") }
        drafts.save(initialDrafts.copy(questions = mapOf(AppSection.DE_BROGLIE to "New memory question")))
        assertEquals(StorageHealth.MEMORY_ONLY, session.health)
        assertFalse(session.uiPersistenceAllowed)
        assertNotNull(drafts.notice)
        assertEquals(saved, backend.values)
        assertEquals(writesBefore, backend.writes)
    }

    @Test fun deniedSessionNeverInvokesPersonalTransformsEvenAfterOwnerCloses() {
        val locks = Locks()
        val owner = SessionWriter(locks).apply { start() }
        val reader = SessionWriter(locks).apply { start() }
        val backend = Backend()
        val session = LabSession(SessionStorage(backend, reader))
        val original = session.snapshot
        var transformed = false

        fun tryEditing() {
            session.update { transformed = true; it }
            session.editWork(StationId.S06_CALCULATION) { transformed = true; it }
            session.updateMeasurement { transformed = true; it }
        }

        assertFalse(session.canEditMeasurement)
        assertFalse(session.uiPersistenceAllowed)
        tryEditing()
        owner.close()
        reader.start()
        tryEditing()

        assertFalse(transformed)
        assertEquals(original, session.snapshot)
        assertFalse(session.canEditMeasurement)
        assertEquals(WriteAccess.OTHER_TAB, reader.access)
        assertEquals(2, locks.requests)
        assertEquals(0, backend.writes)
    }

    @Test fun externalUncooperativeChangeIsDetectedAndBothVersionsRemainAvailable() {
        val writer = SessionWriter(Locks()).apply { start() }
        val backend = Backend()
        val session = LabSession(SessionStorage(backend, writer))
        session.editWork(StationId.S06_CALCULATION) { it.copy(note = "Own first") }
        val external = SessionSnapshot(work = mapOf(StationId.S06_CALCULATION to StationWork(note = "Old version tab"))).json()
        backend.values[SessionStorage.KEY] = external
        session.editWork(StationId.S06_CALCULATION) { it.copy(note = "Own second") }
        assertEquals(StorageHealth.CONFLICT, session.health)
        assertFalse(session.isReadOnly)
        assertFalse(session.localWritesAllowed)
        assertEquals(external, session.storedSnapshotForExport())
        assertEquals("Own second", session.snapshot.stationWork(StationId.S06_CALCULATION).note)
    }

    @Test fun questionsUseSameOwnershipAndProtectChangedExternalDraft() {
        val locks = Locks()
        val first = SessionWriter(locks).apply { start() }
        val second = SessionWriter(locks).apply { start() }
        val backend = Backend()
        val owner = UiDraftStorage(backend, first)
        val other = UiDraftStorage(backend, second)
        owner.restore(); other.restore()
        owner.save(UiDraftSnapshot(questions = mapOf(AppSection.DE_BROGLIE to "Owner question")))
        val original = backend.values.getValue(PresentationKeys.UI_DRAFTS)
        other.save(UiDraftSnapshot(questions = mapOf(AppSection.DE_BROGLIE to "Other question")))
        assertEquals(original, backend.values[PresentationKeys.UI_DRAFTS])
        assertNotNull(other.notice)
        backend.values[PresentationKeys.UI_DRAFTS] = "unexpected external version"
        owner.save(UiDraftSnapshot(questions = mapOf(AppSection.DE_BROGLIE to "Owner changed")))
        assertEquals("unexpected external version", backend.values[PresentationKeys.UI_DRAFTS])
        assertNotNull(owner.notice)
    }

    @Test fun browserStorageDefaultsFailClosedWithoutAnExplicitPermit() {
        val backend = Backend()
        val session = LabSession(SessionStorage(backend))
        session.visit(StationId.S06_CALCULATION)
        val drafts = UiDraftStorage(backend)
        drafts.restore(); drafts.save(UiDraftSnapshot(questions = mapOf(AppSection.JONSSON to "Unsaved")))
        assertEquals(0, backend.writes)
        assertEquals(StorageHealth.MEMORY_ONLY, session.health)
    }
}
