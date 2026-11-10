package materiewelle.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.browser.window
import materiewelle.core.course.*
import materiewelle.core.ScientificData
import materiewelle.core.MeasurementSession
import materiewelle.presentation.PresentationKeys

enum class StorageHealth { INITIALIZING, SAVED, OTHER_TAB, MEMORY_ONLY, REJECTED, CONFLICT }

/** Injectable public storage boundary; write failures leave that key unchanged, like setItem. */
interface SessionStorageBackend {
    fun read(key: String): String?
    fun write(key: String, value: String)
    fun remove(key: String)
}

private object BrowserSessionStorage : SessionStorageBackend {
    override fun read(key: String): String? = window.localStorage.getItem(key)
    override fun write(key: String, value: String) = window.localStorage.setItem(key, value)
    override fun remove(key: String) = window.localStorage.removeItem(key)
}

/** An unreadable record stays in place even when visit() immediately updates in-memory progress. */
class SessionStorage(
    private val backend: SessionStorageBackend = BrowserSessionStorage,
    private val writePermit: SessionWritePermit = NoSessionWriter,
) {
    data class Restored(
        val snapshot: SessionSnapshot,
        val health: StorageHealth,
        val message: String,
        val backup: String? = null,
        val migrated: Boolean = false,
        val importBackup: String? = null,
    )
    var sourceProtected: Boolean = false; private set
    var writesSuspendedAfterDeletion: Boolean = false; private set
    private var observedSource: String? = null
    var externalConflict: Boolean = false; private set
    val access: WriteAccess get() = writePermit.access
    val ownsWrites: Boolean get() = access == WriteAccess.EXCLUSIVE
    val canPersist: Boolean get() = ownsWrites && !sourceProtected && !externalConflict && !writesSuspendedAfterDeletion

    /** The live stored version is separate from this tab's potentially divergent in-memory snapshot. */
    fun storedSnapshotForExport(): String? = try {
        backend.read(KEY)?.takeIf { it.length <= MAX_BACKUP_LENGTH }
    } catch (_: Throwable) { null }

    private fun sourceUnchanged(): Boolean {
        if (backend.read(KEY) == observedSource) return true
        externalConflict = true
        return false
    }

    fun restore(): Restored {
        val text = try { backend.read(KEY) } catch (_: Throwable) {
            sourceProtected = true
            return Restored(SessionSnapshot(), StorageHealth.MEMORY_ONLY,
                "Der lokale Speicher ist nicht verfügbar. Neue Eingaben bleiben nur in diesem Tab. Kopiere deine Arbeit vor dem Schließen oder Neuladen.")
        }
        observedSource = text
        val importBackup = readImportBackup()
        if (text == null) return Restored(SessionSnapshot(), access.storageHealth,
            access.storageNotice, importBackup = importBackup)
        val decoded = try { SessionMigration.restore(text) } catch (_: Throwable) {
            sourceProtected = true
            return Restored(SessionSnapshot(), StorageHealth.REJECTED,
                "Der gespeicherte Arbeitsstand konnte nicht geöffnet werden. Das Original bleibt unverändert. Neue Eingaben bleiben nur in diesem Tab; kopiere sie vor dem Schließen oder Neuladen.",
                text.takeIf { it.length <= MAX_BACKUP_LENGTH }, importBackup = importBackup)
        }
        if (decoded.migrated && !ownsWrites) return Restored(decoded.snapshot, access.storageHealth,
            access.storageNotice + " Der ältere Stand wurde nur für die Ansicht gelesen; sein gespeichertes Original bleibt unverändert.",
            text.takeIf { it.length <= MAX_BACKUP_LENGTH }, migrated = true, importBackup = importBackup)
        if (decoded.migrated) {
            // One bounded backup slot, written successfully before replacing V1 at the original key.
            try {
                check(ownsWrites && sourceUnchanged())
                backend.write(BACKUP_KEY, text)
                backend.write(KEY, decoded.snapshot.json())
                observedSource = decoded.snapshot.json()
            } catch (_: Throwable) {
                sourceProtected = true
                return Restored(decoded.snapshot, StorageHealth.MEMORY_ONLY,
                    "Deine Angaben aus dem älteren Arbeitsstand wurden übernommen, konnten aber nicht neu gespeichert werden. Das Original bleibt geschützt. Kopiere neue Arbeit vor dem Schließen oder Neuladen.", text, migrated = true,
                    importBackup = importBackup)
            }
            return Restored(decoded.snapshot, StorageHealth.SAVED,
                "Deine Angaben aus dem älteren Arbeitsstand wurden übernommen. Eine lokale Kopie des Originals bleibt erhalten. Frühere Lösungskontrollen gelten nicht für neue Aufgaben; ordne alte Bildbegründungen dem sichtbaren Band neu zu.",
                text, migrated = true, importBackup = importBackup)
        }
        val backup = try { backend.read(BACKUP_KEY)?.takeIf { it.length <= MAX_BACKUP_LENGTH } } catch (_: Throwable) { null }
        return Restored(decoded.snapshot, access.storageHealth,
            if (ownsWrites) "Arbeitsstand auf diesem Gerät gespeichert." else access.storageNotice, backup,
            importBackup = importBackup)
    }

    fun readImportBackup(): String? =
        try { backend.read(IMPORT_BACKUP_KEY)?.takeIf { it.length <= MAX_BACKUP_LENGTH } } catch (_: Throwable) { null }

    fun save(snapshot: SessionSnapshot): Boolean {
        if (!canPersist) return false
        return try {
            if (!sourceUnchanged()) return false
            val text = snapshot.json()
            backend.write(KEY, text)
            observedSource = text
            true
        } catch (_: Throwable) { false }
    }

    /** Capture a storage precondition; another tab must not be overwritten by an import. */
    fun importBaseline(): String? {
        check(canPersist) {
            "Der vorhandene Speicher ist geschützt. Sichere die aktuelle Arbeit und den alten Inhalt zuerst separat."
        }
        val current = backend.read(KEY)
        if (current != observedSource) {
            externalConflict = true
            error("Der Speicher wurde in einem anderen Fenster verändert. Sichere eigene Änderungen und lade die App vor dem Import neu.")
        }
        return current
    }

    /** All cooperating writers share the held exclusive lease; the active key is written only after backup. */
    fun replaceAfterBackup(candidate: SessionSnapshot, current: SessionSnapshot, expectedSource: String?): Boolean {
        if (!canPersist) return false
        return try {
            val replacement = candidate.json()
            val backup = current.json()
            require(replacement.length <= SessionSnapshot.MAX_JSON_LENGTH && backup.length <= SessionSnapshot.MAX_JSON_LENGTH)
            if (!sourceUnchanged() || expectedSource != observedSource) return false
            backend.write(IMPORT_BACKUP_KEY, backup)
            backend.write(KEY, replacement)
            observedSource = replacement
            true
        } catch (_: Throwable) { false }
    }

    /** Explicit allowlist: never clear another application's data on this origin. */
    fun deleteProjectData(): Boolean {
        if (!ownsWrites) return false
        // Also on partial failure, prevent a stale component from silently recreating deleted data.
        writesSuspendedAfterDeletion = true
        var complete = true
        PROJECT_KEYS.forEach { key ->
            try { backend.remove(key) } catch (_: Throwable) { complete = false }
        }
        PROJECT_KEYS.forEach { key ->
            try { if (backend.read(key) != null) complete = false } catch (_: Throwable) { complete = false }
        }
        if (complete) {
            observedSource = null
            sourceProtected = false
        }
        return complete
    }
    companion object {
        // Retain the original key so an existing browser installation is found.
        const val KEY = "materiewelle.course.session.v1"
        const val BACKUP_KEY = "materiewelle.course.session.backup.before-v2"
        const val IMPORT_BACKUP_KEY = "materiewelle.course.session.backup.before-import"
        const val MAX_BACKUP_LENGTH = 1_000_000
        val PROJECT_KEYS = listOf(KEY, BACKUP_KEY, IMPORT_BACKUP_KEY, PresentationKeys.THEME, PresentationKeys.MOTION,
            PresentationKeys.UI_DRAFTS, ReadingPositionStore.KEY)
    }
}

/** One state owner for course and laboratories. DOM visibility does not own scientific data. */
class LabSession(private val storage: SessionStorage = SessionStorage()) : MeasurementOwner {
    override val canEditMeasurement: Boolean get() = !isReadOnly
    override val measurement: MeasurementSession get() = snapshot.measurement
    override fun updateMeasurement(transform: (MeasurementSession) -> MeasurementSession) =
        update { it.copy(measurement = transform(it.measurement)) }
    /** Only a newly denied tab is read-only; later conflicts retain editable unsaved work. */
    val isReadOnly: Boolean = storage.access == WriteAccess.OTHER_TAB
    val uiPersistenceAllowed: Boolean get() = storage.ownsWrites && !storage.writesSuspendedAfterDeletion
    val localWritesAllowed: Boolean get() = storage.canPersist
    val localDeletionAllowed: Boolean get() = storage.ownsWrites
    val writeAccess: WriteAccess get() = storage.access
    fun storedSnapshotForExport(): String? = storage.storedSnapshotForExport()
    private val restored = storage.restore()
    var restoreNotice by mutableStateOf(when {
        isReadOnly && restored.health == StorageHealth.REJECTED -> "Der gespeicherte Arbeitsstand konnte nicht geöffnet werden. Das Original bleibt unverändert."
        isReadOnly && restored.migrated -> "Die Angaben aus dem älteren Arbeitsstand wurden für die Ansicht übernommen. Das gespeicherte Original bleibt unverändert."
        restored.health == StorageHealth.REJECTED || restored.migrated -> restored.message
        else -> null
    }); private set
    var rejectedBackup by mutableStateOf(restored.backup); private set
    var snapshot by mutableStateOf(restored.snapshot); private set
    private var savedHealth by mutableStateOf(restored.health)
    val health: StorageHealth get() = when {
        isReadOnly -> StorageHealth.OTHER_TAB
        storage.sourceProtected -> savedHealth
        storage.externalConflict -> StorageHealth.CONFLICT
        !storage.ownsWrites -> storage.access.storageHealth
        else -> savedHealth
    }
    private var savedMessage by mutableStateOf(restored.message)
    val storageMessage: String get() = when {
        isReadOnly -> storage.access.storageNotice
        storage.sourceProtected -> savedMessage
        storage.externalConflict -> "Der gespeicherte Stand wurde in einem anderen Tab geändert. Deine Änderungen in diesem Tab werden nicht gespeichert. Kopiere sie vor dem Schließen oder Neuladen."
        !storage.ownsWrites -> storage.access.storageNotice
        else -> savedMessage
    }
    private data class PendingImport(val preview: SessionImportPreview, val current: SessionSnapshot, val source: String?)
    private var pendingImport by mutableStateOf<PendingImport?>(null)
    val importPreview: SessionImportPreview? get() = pendingImport?.preview
    var importBackup by mutableStateOf(restored.importBackup); private set
    var transferMessage by mutableStateOf("Dateien bleiben auf diesem Gerät. Vor einer Übernahme erscheint eine Vorschau."); private set
    var localDataDeleted by mutableStateOf(false); private set
    fun update(transform: (SessionSnapshot) -> SessionSnapshot) {
        if (isReadOnly) return
        snapshot = transform(snapshot).validate()
        if (storage.writesSuspendedAfterDeletion) {
            savedHealth = StorageHealth.MEMORY_ONLY
            savedMessage = "Die lokale Speicherung ist nach dem Löschversuch angehalten. Kopiere neue Arbeit vor dem Neuladen."
        } else if (storage.sourceProtected) {
            savedHealth = if (restored.health == StorageHealth.REJECTED) StorageHealth.REJECTED else StorageHealth.MEMORY_ONLY
            savedMessage = "Der ursprüngliche Speicherinhalt bleibt geschützt. Neue Arbeit ist nur in diesem Tab verfügbar. Kopiere sie vor dem Schließen oder Neuladen."
        } else if (storage.save(snapshot)) {
            savedHealth = StorageHealth.SAVED
            savedMessage = "Auf diesem Gerät gespeichert."
        } else {
            savedHealth = StorageHealth.MEMORY_ONLY
            savedMessage = "Deine Änderungen sind nur in diesem Tab verfügbar. Beim Schließen oder Neuladen gehen sie verloren. Kopiere deine Arbeit vorher."
        }
    }
    fun previewImport(text: String): Boolean {
        pendingImport = null
        return try {
            val preview = SessionImportPreview.read(text, snapshot.imageIdentity)
            val source = storage.importBaseline()
            pendingImport = PendingImport(preview, snapshot, source)
            transferMessage = "Datei geprüft. Noch nichts übernommen; die Übernahme ersetzt den aktuellen Arbeitsstand nach lokaler Sicherung."
            true
        } catch (_: Throwable) {
            transferMessage = "Datei nicht übernommen: ungültiger oder unpassender Lernstand, geschützter Speicher oder geänderter Stand in einem anderen Fenster. Aktuelle Arbeit und Original bleiben erhalten."
            false
        }
    }
    fun reportImportReadFailure() {
        pendingImport = null
        transferMessage = "Datei nicht lesbar oder zu groß. Bitte eine vollständige Lernstand-JSON-Datei bis 720 kB wählen; nichts wurde übernommen."
    }
    fun cancelImport() {
        pendingImport = null
        transferMessage = "Import abgebrochen. Der bisherige Arbeitsstand bleibt erhalten."
    }
    fun confirmImport(): Boolean {
        val pending = pendingImport ?: return false
        pendingImport = null
        if (pending.current != snapshot) {
            transferMessage = "Deine Arbeit hat sich seit der Vorschau geändert. Bitte die Datei erneut wählen und prüfen; nichts wurde ersetzt."
            return false
        }
        if (!storage.replaceAfterBackup(pending.preview.snapshot, snapshot, pending.source)) {
            importBackup = storage.readImportBackup()
            transferMessage = "Übernahme nicht möglich: Sicherung/Speicherung fehlgeschlagen oder Speicher inzwischen geändert. Aktueller Arbeitsstand und ursprünglicher Speichereintrag bleiben erhalten."
            return false
        }
        importBackup = snapshot.json()
        snapshot = pending.preview.snapshot
        savedHealth = StorageHealth.SAVED
        restoreNotice = null
        savedMessage = "Geprüften Arbeitsstand übernommen und lokal gespeichert. Der Stand unmittelbar davor ist separat abrufbar."
        transferMessage = "Arbeitsstand übernommen. Frühere Antworten und bekannte Hilfekontakte sind erhalten; unbekannte Kontakte wurden nicht ergänzt."
        return true
    }
    fun deleteAllLocalData(): Boolean {
        if (!storage.ownsWrites) {
            transferMessage = "Nichts gelöscht: Dieser Tab hat keine Schreibsitzung. Sichere seine eigene Arbeit; lösche Daten nur im speichernden Tab."
            return false
        }
        pendingImport = null
        if (!storage.deleteProjectData()) {
            savedHealth = StorageHealth.MEMORY_ONLY
            transferMessage = "Lokale Daten konnten nicht vollständig gelöscht werden. Speicherung ist angehalten; sichere benötigte Arbeit und versuche die Löschung erneut."
            savedMessage = transferMessage
            return false
        }
        snapshot = SessionSnapshot(imageIdentity = snapshot.imageIdentity)
        restoreNotice = null
        rejectedBackup = null
        importBackup = null
        localDataDeleted = true
        savedHealth = StorageHealth.MEMORY_ONLY
        transferMessage = "Lokale Antworten, Messungen, Tutorfragen, Sicherungen und Anzeigeeinstellungen wurden gelöscht. Bitte lade die App neu."
        savedMessage = transferMessage
        return true
    }
    fun adoptScientificData(data: ScientificData) {
        val reconciled = snapshot.withScientificData(data)
        if (reconciled != snapshot) {
            // Reconcile image identity for safe reading without recording learner actions.
            if (isReadOnly) snapshot = reconciled.validate() else update { reconciled }
        }
    }
    fun visit(id: StationId) = update { it.copy(station = id, visited = it.visited + id) }
    fun editWork(id: StationId, transform: (StationWork) -> StationWork) = update {
        it.copy(work = it.work + (id to transform(it.stationWork(id))))
    }
    fun editPractice(id: PracticeCaseId, text: String) = update {
        it.copy(practice = it.practice + (id to it.practiceWork(id).edit(text)))
    }
    fun checkPractice(id: PracticeCaseId) = update {
        it.copy(practice = it.practice + (id to it.practiceWork(id).submit()))
    }
    fun revealPracticeSolution(id: PracticeCaseId) = update {
        it.copy(practice = it.practice + (id to it.practiceWork(id).revealSolution()))
    }
    fun editInquiry(transform: (InquiryState) -> InquiryState) = update { it.copy(inquiry = transform(it.inquiry)) }
    fun answer(id: QuestionId, option: QuestionOptionId) = update {
        val previous = it.questionWork(id)
        val next = previous.copy(selected = option, checked = false, nextReviewAt = 0,
            firstSelected = if (previous.isCurrentContent) previous.firstSelected ?: option else option,
            contentRevision = QuestionAttempt.CURRENT_CONTENT_REVISION)
        it.copy(questions = it.questions + (id to next))
    }
    fun reopenQuestion(id: QuestionId) = update {
        val target = StationId.entries.single { station -> station.number == QuestionBank[id].stationNumber }
        it.copy(activeQuestion = id, questions = it.questions + (id to it.questionWork(id).copy(selected = null, checked = false, nextReviewAt = 0)),
            work = if (target.usesLearningPhases) it.work + (target to it.stationWork(target).copy(learningPhase = LearningPhase.EXPLAIN)) else it.work)
    }
    fun check(id: QuestionId, nowMillis: Long) = update {
        val previous = it.questionWork(id)
        val selected = requireNotNull(previous.selected)
        val correct = QuestionBank[id].option(selected).isCorrect
        it.copy(questions = it.questions + (id to previous.copy(checked = true, attempts = previous.attempts + 1,
            firstSelected = previous.firstSelected ?: previous.selected,
            contentRevision = QuestionAttempt.CURRENT_CONTENT_REVISION,
            nextReviewAt = nowMillis + if (correct) 86_400_000L else 600_000L)))
    }
    fun resetCourse() = update {
        it.copy(visited = emptySet(), work = emptyMap(), questions = emptyMap(), practice = emptyMap(),
            activeQuestion = null, station = StationId.S01_OBSERVATION,
            // Clear course answers and disclosures; retain experiment settings and comparisons.
            inquiry = it.inquiry.copy(
                superpositionPrediction = "", superpositionRevealed = false,
                opticsPrediction = null, opticsRevealed = false, geometryPrediction = null,
                crystalAssignment = null, crystalRevealed = false,
                limitAssignments = emptyMap(), limitsChecked = false,
                evidenceRows = List(3) { EvidenceRow() },
            ))
    }
}
