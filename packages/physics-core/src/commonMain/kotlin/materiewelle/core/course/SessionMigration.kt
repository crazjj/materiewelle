package materiewelle.core.course

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import materiewelle.core.*

/** Decode strictly before conversion. A future or malformed record is never treated as empty work. */
object SessionMigration {
    data class Restored(val snapshot: SessionSnapshot, val sourceSchemaVersion: Int) {
        val migrated: Boolean get() = sourceSchemaVersion < SessionSnapshot.SCHEMA_VERSION
    }
    private val codec = Json {
        encodeDefaults = true
        ignoreUnknownKeys = false
        allowSpecialFloatingPointValues = false
    }
    fun restore(text: String): Restored {
        require(text.length <= SessionSnapshot.MAX_JSON_LENGTH) { "Gespeicherter Lernstand ist zu groß." }
        val root = codec.parseToJsonElement(text).jsonObject
        val version = if ("schemaVersion" !in root) 1 // V1 allowed omitted default fields.
        else {
            val value = root["schemaVersion"]
            require(value is JsonPrimitive && !value.isString) { "Ungültige Speicherfassung." }
            requireNotNull(value.intOrNull) { "Ungültige Speicherfassung." }
        }
        // V2 was always exported with explicit identities. Never let changed Kotlin defaults
        // turn an unidentified/future course into apparently compatible current work.
        if (version == SessionSnapshot.SCHEMA_VERSION) {
            require(root["courseRevision"] == JsonPrimitive(SessionSnapshot.COURSE_REVISION) &&
                root["dataRevision"] == JsonPrimitive(SessionSnapshot.DATA_REVISION)) {
                "Unbekannter Fach- oder Datenstand; ein ausdrücklicher Übergang ist erforderlich."
            }
        }
        return when (version) {
            1 -> Restored(codec.decodeFromString<LegacySessionV1>(text).migrate(), 1)
            SessionSnapshot.SCHEMA_VERSION -> Restored(codec.decodeFromString<SessionSnapshot>(text).validate(), version)
            else -> throw IllegalArgumentException("Unbekannte Speicherfassung $version; Originalinhalt bleibt erhalten.")
        }
    }
}

/* Frozen V1 wire contract. Do not add V2 defaults here or reuse evolving session DTOs. */
@Serializable
internal data class LegacySessionV1(
    val schemaVersion: Int = 1,
    val courseRevision: String = "abgabe-2026-10-14",
    val dataRevision: String = "2.0",
    val imageIdentity: MeasurementImageIdentity? = MeasurementImageIdentity(
        "d8952b3336cb499884f2cbe600cc15645f8bc1c406b1c4e7f43f3bc4d9553bb6", 703, 242, 490, 703, NativeImageBand(100, 220)),
    val measurementResetReason: MeasurementResetReason? = null,
    val station: StationId = StationId.S01_OBSERVATION,
    val visited: Set<StationId> = emptySet(),
    val work: Map<StationId, LegacyStationWorkV1> = emptyMap(),
    val questions: Map<QuestionId, LegacyQuestionAttemptV1> = emptyMap(),
    val activeQuestion: QuestionId? = null,
    val deBroglie: LegacyDeBroglieV1 = LegacyDeBroglieV1(),
    val dg: LegacyDgV1 = LegacyDgV1(),
    val measurement: LegacyMeasurementV1 = LegacyMeasurementV1(),
    val modelSlitSpacingUm: Double = 2.0,
    val modelSlitWidthUm: Double = 0.5,
) {
    fun migrate(): SessionSnapshot {
        require(schemaVersion == 1 && courseRevision == "abgabe-2026-10-14" && dataRevision == "2.0") {
            "Dieser alte Lernstand gehört zu einem anderen Fach- oder Datenstand."
        }
        require(imageIdentity != null || measurement == LegacyMeasurementV1()) {
            "Ohne zugeordnetes Bild darf keine Pixelmessung übernommen werden."
        }
        return SessionSnapshot(
            imageIdentity = imageIdentity, measurementResetReason = measurementResetReason,
            station = station, visited = visited, work = work.mapValues { it.value.migrate() },
            questions = questions.mapValues { it.value.migrate() }, activeQuestion = activeQuestion,
            deBroglie = deBroglie.migrate(), dg = dg.migrate(),
            measurement = if (imageIdentity == null) MeasurementSession() else measurement.migrate(),
            modelSlitSpacingUm = modelSlitSpacingUm, modelSlitWidthUm = modelSlitWidthUm,
        ).validate()
    }
}

@Serializable
internal data class LegacyStationWorkV1(
    val note: String = "", val numericAnswer: String = "", val checked: Boolean = false,
    val transferAnswer: String = "", val transferChecked: Boolean = false,
    val explanationStep: Int = 0, val hintsOpened: Int = 0, val markedWorked: Boolean = false,
    val learningPhase: LearningPhase = LearningPhase.PREDICT, val prediction: String = "",
) {
    fun migrate() = StationWork(note, numericAnswer, checked, transferAnswer, transferChecked,
        explanationStep, hintsOpened, markedWorked, learningPhase, prediction,
        solutionViewed = false, transferSolutionViewed = false, explanationFurthestStep = explanationStep)
}

@Serializable
internal data class LegacyQuestionAttemptV1(
    val selected: QuestionOptionId? = null, val checked: Boolean = false,
    val nextReviewAt: Long = 0, val attempts: Int = 0,
) {
    fun migrate(): QuestionAttempt {
        // V1 stored the latest selection only. Its actual first selection is unknown.
        return QuestionAttempt(selected, checked, nextReviewAt, attempts, firstSelected = null, contentRevision = 1).validate()
    }
}

@Serializable
internal data class LegacyDeBroglieV1(
    val raw: String = "150", val lastValidVoltage: Double = 150.0,
    val comparisonVoltage: Double? = 600.0, val mode: ExperimentMode = ExperimentMode.LAB, val step: Int = 0,
) {
    fun migrate() = DeBroglieSession(raw, lastValidVoltage, comparisonVoltage, mode, step).validate()
}

@Serializable
internal data class LegacyDgV1(
    val mode: ExperimentMode = ExperimentMode.LAB, val step: Int = 0,
    val selectedId: String? = null, val compareId: String? = null,
    val modelVoltageRaw: String = "54", val modelAngleRaw: String = "50",
    val validModelVoltage: Double = 54.0, val validModelAngle: Double = 50.0,
    val geometryStep: Int = 0, val view: DgDiagram = DgDiagram.SETUP,
    val comparisonVoltage: Double? = null, val comparisonAngle: Double? = null,
) {
    fun migrate() = DgSession(mode, step, selectedId, compareId, modelVoltageRaw, modelAngleRaw,
        validModelVoltage, validModelAngle, geometryStep, view, comparisonVoltage, comparisonAngle).validate()
}

@Serializable
internal data class LegacyMeasurementSnapshotV1(
    val band: MeasurementBand,
    val markers: List<NativeImageMark>,
    val justification: String,
    val referenceViewedAtCapture: Boolean,
    val imageSha256: String,
    val nativeWidthPx: Int,
    val nativeHeightPx: Int,
    val bandTopInclusivePx: Int = band.topInclusive,
    val bandBottomExclusivePx: Int = band.bottomExclusive,
) {
    fun migrate() = MeasurementSnapshot(band, markers, justification, referenceViewedAtCapture,
        imageSha256, nativeWidthPx, nativeHeightPx, bandTopInclusivePx, bandBottomExclusivePx,
        priorReferenceMaterials = setOf(ReferenceMaterial.UNKNOWN_LEGACY))
}

@Serializable
internal data class LegacyMeasurementV1(
    val band: MeasurementBand = MeasurementBand.BASE,
    val markers: List<NativeImageMark> = emptyList(),
    val justification: String = "",
    val initialSelection: LegacyMeasurementSnapshotV1? = null,
    val referenceViewed: Boolean = false,
    val comparison: LegacyMeasurementSnapshotV1? = null,
) {
    fun migrate() = MeasurementSession(band, markers, justification, initialSelection?.migrate(),
        referenceViewed, comparison?.migrate(), justificationBand = null,
        referenceMaterialsOpened = setOf(ReferenceMaterial.UNKNOWN_LEGACY))
}
