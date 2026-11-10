package materiewelle.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.browser.window
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import materiewelle.core.course.AppSection
import materiewelle.core.course.DgLessonStep
import materiewelle.course.graphics.SlitExplorationDraft
import materiewelle.presentation.PresentationKeys

/** Optional view metadata. Scientific answers and measurement provenance retain schema 2. */
@Serializable
data class JonssonViewDraft(
    val imageHash: String? = null,
    val zoom: Double = 1.0,
    val wholeImage: Boolean = false,
    val scrollLeft: Double = 0.0,
    val selected: Int = 0,
    val showMarkers: Boolean = true,
    val coordinate: String = "",
    val editingCoordinate: Boolean = false,
    val newCoordinate: String = "",
    val sampleX: Double? = null,
    val opticsStage: Int = 0,
    val slit: SlitExplorationDraft = SlitExplorationDraft(),
) {
    fun validate() = apply {
        require(imageHash == null || Regex("[a-f0-9]{64}").matches(imageHash))
        require(zoom.isFinite() && zoom in 1.0..2.0)
        require(scrollLeft.isFinite() && scrollLeft in 0.0..100_000.0)
        require(selected in 0..63 && opticsStage in 0..4)
        require(coordinate.length <= 120 && newCoordinate.length <= 120)
        require(sampleX == null || sampleX.isFinite() && sampleX in 0.0..100_000.0)
        slit.validate()
    }
}

@Serializable
data class DgViewDraft(
    val step: DgLessonStep = DgLessonStep.SETUP,
    val showAll: Boolean = false,
    val setupStage: Int = 0,
    val braggStage: Int = 0,
    val showPrediction: Boolean = false,
    val showGeometry: Boolean = false,
) {
    fun validate() = apply { require(setupStage in 0..2 && braggStage in 0..2) }
}

@Serializable
data class UiDraftSnapshot(
    val schemaVersion: Int = 1,
    val questions: Map<AppSection, String> = emptyMap(),
    val jonsson: JonssonViewDraft = JonssonViewDraft(),
    val derivationStep: Int? = null,
    val followDerivation: Boolean = false,
    val dg: DgViewDraft = DgViewDraft(),
) {
    fun validate() = apply {
        require(schemaVersion == 1 && questions.size <= AppSection.entries.size)
        require(questions.values.all { it.length <= 2000 })
        require(derivationStep == null || derivationStep in 0..20)
        jonsson.validate()
        dg.validate()
    }
}

/** Failed or unknown records are protected, exactly like the scientific storage boundary. */
class UiDraftStorage(private val backend: SessionStorageBackend = object : SessionStorageBackend {
    override fun read(key: String) = window.localStorage.getItem(key)
    override fun write(key: String, value: String) = window.localStorage.setItem(key, value)
    override fun remove(key: String) = window.localStorage.removeItem(key)
}, private val writePermit: SessionWritePermit = NoSessionWriter) {
    private val codec = Json { encodeDefaults = true }
    private var protected = false
    private var lastSaved: UiDraftSnapshot? = null
    private var observedSource: String? = null
    var notice by mutableStateOf<String?>(null); private set
    /** An expected reading tab is not a storage failure. */
    var needsRescue by mutableStateOf(false); private set

    /** Only learner text needs a reload safeguard; reading and model-view changes do not. */
    fun hasUnsavedText(snapshot: UiDraftSnapshot): Boolean {
        val saved = lastSaved ?: UiDraftSnapshot()
        fun questions(value: UiDraftSnapshot) = value.questions.filterValues { it.isNotBlank() }
        fun slitNotes(value: UiDraftSnapshot) = value.jonsson.slit.notes.filterValues {
            it.prediction.isNotBlank() || it.reason.isNotBlank()
        }
        return questions(snapshot) != questions(saved) || slitNotes(snapshot) != slitNotes(saved)
    }

    fun restore(): UiDraftSnapshot = try {
        val text = backend.read(PresentationKeys.UI_DRAFTS)
        observedSource = text
        val restored = if (text == null) UiDraftSnapshot() else {
            require(text.length <= MAX_RECORD_LENGTH)
            codec.decodeFromString<UiDraftSnapshot>(text).validate()
        }
        lastSaved = restored
        if (writePermit.access != WriteAccess.EXCLUSIVE) notice = writePermit.access.storageNotice
        restored
    } catch (_: Throwable) {
        protected = true
        needsRescue = true
        notice = "Deine letzten Notizen, Tutorfragen und Ansichtseinstellungen konnten nicht geladen werden. Der gespeicherte Eintrag bleibt geschützt. Neue Texte bleiben nur in diesem Tab; kopiere sie vor dem Schließen."
        UiDraftSnapshot()
    }

    /** Preserve the bounded raw record, including an unknown/corrupt version, without decoding it. */
    fun storedDraftForExport(): String? = try {
        backend.read(PresentationKeys.UI_DRAFTS)?.takeIf { it.length <= MAX_RECORD_LENGTH }
    } catch (_: Throwable) { null }

    fun save(snapshot: UiDraftSnapshot) {
        if (protected || snapshot == lastSaved && !needsRescue) return
        if (writePermit.access != WriteAccess.EXCLUSIVE) {
            needsRescue = writePermit.access != WriteAccess.OTHER_TAB
            notice = "Neue Notizen und Tutorfragen bleiben in diesem Tab. Du kannst sie hier kopieren."
            return
        }
        try {
            if (backend.read(PresentationKeys.UI_DRAFTS) != observedSource) {
                protected = true
                needsRescue = true
                notice = "Im Speicher wurden andere Notizen oder Tutorfragen gefunden. Sie bleiben unverändert. Kopiere deine Texte aus diesem Tab vor dem Schließen oder Neuladen."
                return
            }
            if (snapshot == lastSaved) {
                // Undoing a failed edit restores the still-current saved baseline without a write.
                notice = null
                needsRescue = false
                return
            }
            val encoded = codec.encodeToString(snapshot.validate())
            require(encoded.length <= MAX_RECORD_LENGTH)
            backend.write(PresentationKeys.UI_DRAFTS, encoded)
            observedSource = encoded
            lastSaved = snapshot
            notice = null
            needsRescue = false
        } catch (_: Throwable) {
            needsRescue = true
            notice = "Deine Notizen, Tutorfragen und Ansichtseinstellungen können gerade nicht gespeichert werden. Kopiere deine Texte vor dem Schließen oder Neuladen."
        }
    }

    companion object { private const val MAX_RECORD_LENGTH = 32_000 }
}
