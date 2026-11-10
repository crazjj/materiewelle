package materiewelle.state

import kotlin.test.*
import materiewelle.core.course.AppSection
import materiewelle.core.course.DgLessonStep
import materiewelle.core.SlitConfiguration
import materiewelle.course.graphics.*
import materiewelle.presentation.PresentationKeys

class UiDraftStorageTest {
    @Test fun lessonDraftRestoresItsViewWithoutChangingScientificAnswers() {
        val backend = Backend().apply { records[SessionStorage.KEY] = "existing own work" }
        val store = UiDraftStorage(backend, ExclusiveTestWriter); store.restore()
        val edited = UiDraftSnapshot(dg = DgViewDraft(step = DgLessonStep.GEOMETRY, braggStage = 2, showPrediction = true))
        store.save(edited)
        assertEquals(edited, UiDraftStorage(backend, ExclusiveTestWriter).restore())
        assertEquals("existing own work", backend.records[SessionStorage.KEY])
        assertFailsWith<IllegalArgumentException> { DgViewDraft(braggStage = 3).validate() }
        assertFailsWith<IllegalArgumentException> { DgViewDraft(setupStage = -1).validate() }
    }
    private class Backend : SessionStorageBackend {
        val records = mutableMapOf<String, String>()
        var writes = 0
        var fail = false
        override fun read(key: String) = records[key]
        override fun write(key: String, value: String) {
            check(!fail); records[key] = value; writes++
        }
        override fun remove(key: String) { records.remove(key) }
    }

    @Test fun draftsReloadWithoutWritingScientificSessionOrUnchangedState() {
        val backend = Backend()
        backend.records[SessionStorage.KEY] = "scientific record must stay unchanged"
        val store = UiDraftStorage(backend, ExclusiveTestWriter)
        val initial = store.restore()
        store.save(initial)
        assertEquals(0, backend.writes)
        val edited = initial.copy(questions = mapOf(AppSection.JONSSON to "Meine eigene Frage"),
            jonsson = JonssonViewDraft(zoom = 1.75, newCoordinate = "59,"))
        store.save(edited); store.save(edited)
        assertEquals(1, backend.writes)
        assertEquals(edited, UiDraftStorage(backend, ExclusiveTestWriter).restore())
        assertEquals("scientific record must stay unchanged", backend.records[SessionStorage.KEY])
    }

    @Test fun damagedDraftIsPreservedAndFailureIsVisible() {
        val backend = Backend().apply { records[PresentationKeys.UI_DRAFTS] = "not JSON" }
        val store = UiDraftStorage(backend, ExclusiveTestWriter)
        store.restore()
        assertNotNull(store.notice)
        assertTrue(store.needsRescue)
        store.save(UiDraftSnapshot(questions = mapOf(AppSection.DE_BROGLIE to "Neue Frage")))
        assertEquals("not JSON", backend.records[PresentationKeys.UI_DRAFTS])
        assertEquals(0, backend.writes)
    }

    @Test fun writeFailureCanRecoverWithoutLosingDraft() {
        val backend = Backend()
        val store = UiDraftStorage(backend, ExclusiveTestWriter); store.restore()
        val edited = UiDraftSnapshot(questions = mapOf(AppSection.CONCLUSION to "Mein Entwurf"))
        backend.fail = true; store.save(edited)
        assertNotNull(store.notice)
        assertTrue(store.needsRescue)
        backend.fail = false; store.save(edited)
        assertNull(store.notice)
        assertFalse(store.needsRescue)
        assertEquals(edited, UiDraftStorage(backend, ExclusiveTestWriter).restore())
    }

    @Test fun boundedViewsAndProjectDeletionIncludeNewKeys() {
        assertFailsWith<IllegalArgumentException> { JonssonViewDraft(zoom = Double.NaN).validate() }
        assertFailsWith<IllegalArgumentException> { UiDraftSnapshot(questions = mapOf(AppSection.JONSSON to "x".repeat(2001))).validate() }
        assertTrue(PresentationKeys.UI_DRAFTS in SessionStorage.PROJECT_KEYS)
        assertTrue(ReadingPositionStore.KEY in SessionStorage.PROJECT_KEYS)
    }

    @Test fun bothSlitInvestigationsRestoreAndOlderDraftsKeepTheirDefaults() {
        val backend = Backend().apply {
            records[SessionStorage.KEY] = "scientific answers"
            records[PresentationKeys.UI_DRAFTS] = """{"schemaVersion":1,"questions":{"JONSSON":"Vorhandene Frage"}}"""
        }
        val store = UiDraftStorage(backend, ExclusiveTestWriter)
        val old = store.restore()
        assertEquals(SlitExplorationDraft(), old.jonsson.slit)
        val slit = SlitExplorationDraft(configuration = SlitConfiguration.LEFT_ONLY,
            task = SlitInvestigation.WIDTH, notes = mapOf(
                SlitInvestigation.SPACING to SlitInvestigationNotes("Engere Streifen", "Größerer Spaltabstand"),
                SlitInvestigation.WIDTH to SlitInvestigationNotes("Breitere Hülle", "Schmalerer Spalt")))
        val edited = old.copy(jonsson = old.jonsson.copy(slit = slit))
        store.save(edited)
        assertEquals(edited, UiDraftStorage(backend, ExclusiveTestWriter).restore())
        assertEquals("scientific answers", backend.records[SessionStorage.KEY])
        assertFailsWith<IllegalArgumentException> {
            slit.copy(notes = mapOf(SlitInvestigation.SPACING to SlitInvestigationNotes("x".repeat(1201)))).validate()
        }
    }

    @Test fun readOnlyDraftsRequireTextSafeguardWithoutBecomingAStorageFailure() {
        val backend = Backend()
        val reader = object : SessionWritePermit { override val access = WriteAccess.OTHER_TAB }
        val store = UiDraftStorage(backend, reader)
        val initial = store.restore()
        val viewed = initial.copy(jonsson = JonssonViewDraft(zoom = 2.0,
            slit = SlitExplorationDraft(task = SlitInvestigation.WIDTH)))
        assertFalse(store.hasUnsavedText(viewed))
        val question = viewed.copy(questions = mapOf(AppSection.JONSSON to "Meine neue Frage"))
        val notes = viewed.copy(jonsson = viewed.jonsson.copy(slit = SlitExplorationDraft(
            notes = mapOf(SlitInvestigation.WIDTH to SlitInvestigationNotes(reason = "Meine Erklärung")))))
        assertTrue(store.hasUnsavedText(question))
        assertTrue(store.hasUnsavedText(notes))
        store.save(question)
        assertNotNull(store.notice)
        assertFalse(store.needsRescue)
        assertEquals(0, backend.writes)
        assertTrue(store.hasUnsavedText(question))
    }

    @Test fun encodedSizeLimitCannotWriteAnUnrestorableRecord() {
        val backend = Backend()
        val store = UiDraftStorage(backend, ExclusiveTestWriter)
        store.restore()
        val tooLarge = UiDraftSnapshot(questions = AppSection.entries.associateWith { "\u0001".repeat(2000) })
        store.save(tooLarge)
        assertTrue(store.needsRescue)
        assertTrue(store.hasUnsavedText(tooLarge))
        assertEquals(0, backend.writes)
        val normal = UiDraftSnapshot(questions = AppSection.entries.associateWith { "Frage" })
        store.save(normal)
        assertFalse(store.needsRescue)
        assertFalse(store.hasUnsavedText(normal))
        assertEquals(normal, UiDraftStorage(backend, ExclusiveTestWriter).restore())
    }

    @Test fun revertingAFailedEditClearsItsWarningOnlyWhileTheSavedBaselineStillMatches() {
        val backend = Backend()
        val store = UiDraftStorage(backend, ExclusiveTestWriter)
        store.restore()
        val saved = UiDraftSnapshot(questions = mapOf(AppSection.JONSSON to "Gespeicherte Frage A"))
        val changed = saved.copy(questions = mapOf(AppSection.JONSSON to "Neue Frage B"))
        store.save(saved)
        val originalRecord = backend.records.getValue(PresentationKeys.UI_DRAFTS)
        val writesBefore = backend.writes
        backend.fail = true
        store.save(changed)
        assertTrue(store.needsRescue)
        store.save(saved)
        assertFalse(store.needsRescue)
        assertNull(store.notice)
        assertEquals(writesBefore, backend.writes)
        assertEquals(originalRecord, backend.records[PresentationKeys.UI_DRAFTS])

        store.save(changed)
        assertTrue(store.needsRescue)
        backend.records[PresentationKeys.UI_DRAFTS] = "externally changed record"
        store.save(saved)
        assertTrue(store.needsRescue)
        assertNotNull(store.notice)
        backend.records[PresentationKeys.UI_DRAFTS] = originalRecord
        store.save(saved)
        assertTrue(store.needsRescue, "A protected record requires a fresh restore, even if its old bytes reappear")
        assertEquals(writesBefore, backend.writes)
    }
}
