package materiewelle.core.course

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import materiewelle.core.MeasurementSession
import materiewelle.core.MeasurementImageIdentity
import materiewelle.core.NativeImageBand
import materiewelle.core.ScientificData

@Serializable
enum class MeasurementResetReason { IMAGE_CHANGED, IMAGE_UNAVAILABLE }

@Serializable
data class StationWork(
    val note: String = "", val numericAnswer: String = "", val checked: Boolean = false,
    val transferAnswer: String = "", val transferChecked: Boolean = false,
    val explanationStep: Int = 0, val hintsOpened: Int = 0, val markedWorked: Boolean = false,
    val learningPhase: LearningPhase = LearningPhase.PREDICT, val prediction: String = "",
    val solutionViewed: Boolean = false, val transferSolutionViewed: Boolean = false,
    val explanationFurthestStep: Int = 0
) {
    fun validate(): StationWork {
        require(note.length <= 4000 && numericAnswer.length <= 120 && transferAnswer.length <= 120)
        require(explanationStep in 0..20 && explanationFurthestStep in 0..20 && hintsOpened in 0..10)
        require(prediction.length <= 1000)
        return this
    }
}

@Serializable
data class QuestionAttempt(
    val selected: QuestionOptionId? = null, val checked: Boolean = false,
    val nextReviewAt: Long = 0, val attempts: Int = 0,
    val firstSelected: QuestionOptionId? = null, val contentRevision: Int? = null
) {
    fun validate(): QuestionAttempt {
        require(!checked || selected != null)
        require(nextReviewAt in 0..4_102_444_800_000L && attempts in 0..100_000)
        require(contentRevision == null || contentRevision in 1..CURRENT_CONTENT_REVISION)
        return this
    }
    /** An old check is a historical record, never success on a changed question. */
    val isCurrentContent: Boolean get() = contentRevision == CURRENT_CONTENT_REVISION
    val isHistorical: Boolean get() = !isCurrentContent && (selected != null || checked || attempts > 0)
    companion object { const val CURRENT_CONTENT_REVISION = 2 }
}

/** Persist inputs and provenance. All displayed scientific results are recalculated. */
@Serializable
data class SessionSnapshot(
    val schemaVersion: Int = SCHEMA_VERSION,
    val courseRevision: String = COURSE_REVISION,
    val dataRevision: String = DATA_REVISION,
    val imageIdentity: MeasurementImageIdentity? = CANONICAL_IMAGE_IDENTITY,
    val measurementResetReason: MeasurementResetReason? = null,
    val station: StationId = StationId.S01_OBSERVATION,
    val visited: Set<StationId> = emptySet(),
    val work: Map<StationId, StationWork> = emptyMap(),
    val questions: Map<QuestionId, QuestionAttempt> = emptyMap(),
    val activeQuestion: QuestionId? = null,
    val deBroglie: DeBroglieSession = DeBroglieSession(),
    val dg: DgSession = DgSession(),
    val measurement: MeasurementSession = MeasurementSession(),
    val modelSlitSpacingUm: Double = 2.0,
    val modelSlitWidthUm: Double = 0.5,
    val inquiry: InquiryState = InquiryState(),
    val practice: Map<PracticeCaseId, PracticeAttempt> = emptyMap()
) {
    fun validate(): SessionSnapshot {
        require(schemaVersion == SCHEMA_VERSION) { "Unbekannte Speicherfassung; vorhandene Daten wurden nicht übernommen." }
        require(courseRevision == COURSE_REVISION && dataRevision == DATA_REVISION) { "Dieser Lernstand gehört zu einem anderen Fachstand." }
        require(visited.size <= StationId.entries.size && work.size <= StationId.entries.size)
        work.values.forEach { it.validate() }; questions.values.forEach { it.validate() }
        require(questions.size <= QuestionId.entries.size)
        deBroglie.validate(); dg.validate(); inquiry.validate()
        require(practice.size <= PracticeCaseId.entries.size)
        practice.values.forEach { it.validate() }
        if (imageIdentity != null) measurement.validate(imageIdentity)
        else require(measurement == MeasurementSession()) { "Ohne zugeordnetes Bild darf keine Pixelmessung übernommen werden." }
        require(modelSlitSpacingUm.isFinite() && modelSlitSpacingUm in 1.0..5.0)
        require(modelSlitWidthUm.isFinite() && modelSlitWidthUm in 0.1..0.9)
        return this
    }

    /** Loaded source data, not a stored user record, decides which raster measurements belong to. */
    fun withScientificData(data: ScientificData): SessionSnapshot {
        validate()
        val actualImage = data.jonsson?.let { MeasurementImageIdentity.from(it) }
        if (actualImage == imageIdentity) return this
        val hasOwnWork = measurement.markers.isNotEmpty() || measurement.justification.isNotBlank() ||
            measurement.initialSelection != null || measurement.comparison != null || measurement.referenceViewed
        return copy(imageIdentity = actualImage, measurement = MeasurementSession(),
            measurementResetReason = if (hasOwnWork) {
                if (actualImage == null) MeasurementResetReason.IMAGE_UNAVAILABLE else MeasurementResetReason.IMAGE_CHANGED
            } else measurementResetReason).validate()
    }

    val measurementImageChanged: Boolean get() = measurementResetReason != null
    val imageSha256: String? get() = imageIdentity?.sha256
    fun stationWork(id: StationId) = work[id] ?: StationWork()
    fun questionWork(id: QuestionId) = questions[id] ?: QuestionAttempt()
    fun practiceWork(id: PracticeCaseId) = practice[id] ?: PracticeAttempt()
    fun json(): String = codec.encodeToString(validate())
    companion object {
        const val SCHEMA_VERSION = 2
        // These are persisted wire identities, not release labels. A new release or compatible
        // editorial improvement must not bump them. A changed identity needs an explicit reader
        // and migration in SessionMigration before any existing installation can adopt it.
        const val COURSE_REVISION = "abgabe-2026-10-16"
        const val DATA_REVISION = "2.0"
        const val IMAGE_SHA256 = "d8952b3336cb499884f2cbe600cc15645f8bc1c406b1c4e7f43f3bc4d9553bb6"
        val CANONICAL_IMAGE_IDENTITY = MeasurementImageIdentity(IMAGE_SHA256, 703, 242, 490, 703, NativeImageBand(100, 220))
        const val MAX_JSON_LENGTH = 180_000
        private val codec = Json { encodeDefaults = true; ignoreUnknownKeys = false; allowSpecialFloatingPointValues = false }
        fun fromJson(text: String): SessionSnapshot {
            require(text.length <= MAX_JSON_LENGTH) { "Gespeicherter Lernstand ist zu groß." }
            return SessionMigration.restore(text).snapshot
        }
    }
}

/** A fully checked file candidate. Preparing it never mutates the active session or its storage. */
class SessionImportPreview private constructor(
    val snapshot: SessionSnapshot,
    val sourceSchemaVersion: Int,
) {
    val migrated: Boolean get() = sourceSchemaVersion != SessionSnapshot.SCHEMA_VERSION
    val stationRecords: Int get() = snapshot.work.size
    val questionRecords: Int get() = snapshot.questions.size
    val ownMarkers: Int get() = snapshot.measurement.markers.size

    companion object {
        fun read(text: String, expectedImage: MeasurementImageIdentity?): SessionImportPreview {
            val restored = SessionMigration.restore(text)
            // Import is deliberately stricter than startup reconciliation: it never quietly
            // drops a file's measurement while importing only its other fields.
            require(restored.snapshot.imageIdentity == expectedImage) {
                "Die Bildzuordnung der Datei passt nicht zur aktuellen Datengrundlage. Nichts wurde übernommen."
            }
            require(restored.snapshot.json().length <= SessionSnapshot.MAX_JSON_LENGTH) {
                "Der vollständige Lernstand überschreitet die unterstützte Dateigröße."
            }
            return SessionImportPreview(restored.snapshot, restored.sourceSchemaVersion)
        }
    }
}
