package materiewelle.learning

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import materiewelle.core.MeasurementSnapshot
import materiewelle.core.course.*

/** A consent value contains only the work selected by its chapter/task scope. */
@Serializable
internal sealed interface CompactOwnWork {
    @Serializable @SerialName("de-broglie")
    data class DeBroglie(
        val requiredCalculation: CompactRequiredCalculation? = null,
        val modelVariation: CompactVoltageModel? = null,
        val notes: List<CompactNote> = emptyList(),
    ) : CompactOwnWork

    @Serializable @SerialName("dg-model")
    data class Surface(
        val modelVariation: CompactSurfaceModel? = null,
        val notes: List<CompactNote> = emptyList(),
    ) : CompactOwnWork

    @Serializable @SerialName("native-image-selection")
    data class Image(
        val measurement: TutorImageMeasurement?,
        val selectionReason: String,
        val initialSelection: CompactSavedSelection?,
        val comparison: CompactSavedSelection?,
        val notes: List<CompactNote>,
        val includeMeasurement: Boolean = true,
        val slitModel: CompactSlitModel? = null,
        val calibration: CompactCalibration = CompactCalibration.NO_INDEPENDENT_ABSOLUTE_SCALE,
    ) : CompactOwnWork

    @Serializable @SerialName("own-conclusion")
    data class Conclusion(
        val ownConclusion: String,
        val claims: List<CompactEvidenceRow>,
        val relatedWork: List<CompactOwnWork> = emptyList(),
    ) : CompactOwnWork
}

@Serializable
internal data class CompactRequiredCalculation(
    val station: StationId,
    val voltageV: Double,
    val numericAnswer: String,
    val calculationNote: String,
)

@Serializable
internal data class CompactVoltageModel(
    val acceptedVoltageV: Double,
    val editedVoltage: String,
    val rememberedVoltageV: Double? = null,
)

@Serializable
internal data class CompactSurfaceSetting(val voltageV: Double, val angleDegrees: Double)

@Serializable
internal data class CompactSurfaceModel(
    val model: TutorModelSnapshot.Surface?,
    val acceptedVoltageV: Double,
    val acceptedAngleDegrees: Double,
    val editedVoltage: String,
    val editedAngle: String,
    val selectedReportedCaseId: String?,
    val modelAvailability: CompactModelAvailability,
    val remembered: CompactSurfaceSetting? = null,
    val sourceRole: CompactSourceRole = CompactSourceRole.SECONDARY_REPORT,
)

@Serializable
internal data class CompactSlitModel(
    val current: SlitModelComparison,
    val remembered: SlitModelComparison?,
    val exploration: CompactSlitExploration? = null,
)

/** Only explicitly supplied exploration enters this projection. */
internal data class CompactTutorExtras(val slitExploration: CompactSlitExploration? = null)

@Serializable
internal enum class CompactSlitConfiguration { BOTH_COHERENT, LEFT_ONLY, RIGHT_ONLY, BOTH_INCOHERENT }

@Serializable
internal enum class CompactSlitTask { SPACING, WIDTH }

@Serializable
internal data class CompactSlitNotes(val prediction: String = "", val reason: String = "")

@Serializable
internal data class CompactSlitExploration(
    val configuration: CompactSlitConfiguration,
    val task: CompactSlitTask,
    val notes: Map<CompactSlitTask, CompactSlitNotes> = emptyMap(),
    val observationMeters: Double? = null,
) {
    init { require(observationMeters == null || observationMeters.isFinite()) }
}

@Serializable
internal data class CompactEvidenceRow(val number: Int, val row: EvidenceRow)

@Serializable
internal enum class CompactSourceRole { @SerialName("secondary-report-not-original-data") SECONDARY_REPORT }

@Serializable
internal enum class CompactModelAvailability { SUPPORTED, OUTSIDE_HANDOFF_MODEL_RANGE }

@Serializable
internal enum class CompactCalibration { @SerialName("no-independent-absolute-image-scale") NO_INDEPENDENT_ABSOLUTE_SCALE }

@Serializable
internal data class CompactNote(val station: String, val note: String, val prediction: String,
    val numericAnswer: String = "")

@Serializable
internal data class CompactSavedSelection(val measurement: TutorImageMeasurement, val selectionReason: String)

/** Projection only: no writes, no inferred visual correctness, no automatic cross-chapter sharing. */
internal fun compactOwnWork(
    snapshot: SessionSnapshot,
    section: AppSection,
    point: TutorHelpPoint? = null,
    extras: CompactTutorExtras = CompactTutorExtras(),
    includeEarlierResults: Boolean = false,
): CompactOwnWork {
    snapshot.validate()
    require(point == null || point.section == section)
    fun notes(stations: List<StationId>) = stations.mapNotNull { station ->
        val work = snapshot.stationWork(station)
        if (work.note.isBlank() && work.prediction.isBlank() && work.numericAnswer.isBlank()) null
        else CompactNote(station.key, work.note, work.prediction, work.numericAnswer)
    }
    fun chapterNotes(first: Int, last: Int) = StationId.entries.filter { it.number in first..last }
    return when (section) {
        AppSection.DE_BROGLIE -> {
            val task = snapshot.stationWork(StationId.S06_CALCULATION)
            CompactOwnWork.DeBroglie(
                requiredCalculation = if (point == null || point == TutorHelpPoint.CALCULATION)
                    CompactRequiredCalculation(StationId.S06_CALCULATION, 150.0, task.numericAnswer, task.note)
                else null,
                modelVariation = if (point == null || point == TutorHelpPoint.DE_BROGLIE_MODEL)
                    CompactVoltageModel(snapshot.deBroglie.lastValidVoltage, snapshot.deBroglie.raw, snapshot.deBroglie.comparisonVoltage)
                else null,
                notes = if (point == null) notes(chapterNotes(1, 7).filter { it != StationId.S06_CALCULATION }) else emptyList(),
            )
        }
        AppSection.DAVISSON_GERMER -> CompactOwnWork.Surface(
            modelVariation = if (point == null || point == TutorHelpPoint.DG_MODEL) CompactSurfaceModel(
                snapshot.dg.validModelVoltage.takeIf { it <= 1000.0 }?.let { TutorModelSnapshot.Surface(it, snapshot.dg.validModelAngle, .352) },
                snapshot.dg.validModelVoltage, snapshot.dg.validModelAngle,
                snapshot.dg.modelVoltageRaw, snapshot.dg.modelAngleRaw, snapshot.dg.selectedId,
                if (snapshot.dg.validModelVoltage <= 1000.0) CompactModelAvailability.SUPPORTED else CompactModelAvailability.OUTSIDE_HANDOFF_MODEL_RANGE,
                remembered = snapshot.dg.comparisonVoltage?.let { CompactSurfaceSetting(it, requireNotNull(snapshot.dg.comparisonAngle)) },
            ) else null,
            notes = notes(when (point) {
                null -> chapterNotes(8, 11)
                TutorHelpPoint.DG_COMPARISON -> listOf(StationId.S11_DG_EVALUATION)
                else -> emptyList()
            }),
        )
        AppSection.JONSSON -> {
            val includeMeasurement = point == null || point == TutorHelpPoint.IMAGE_MEASUREMENT
            val image = snapshot.imageIdentity?.takeIf { includeMeasurement && it == SessionSnapshot.CANONICAL_IMAGE_IDENTITY }
            val current = image?.let {
                val band = snapshot.measurement.nativeBand(it)
                TutorImageMeasurement(it.sha256, it.nativeWidthPx, it.nativeHeightPx,
                    band.topInclusive, band.bottomExclusive, snapshot.measurement.markers.map { m -> m.xPx.toDouble() },
                    TutorMeasurementOrigin.OWN_SELECTION, referenceViewed = snapshot.measurement.referenceViewed,
                    justificationBand = snapshot.measurement.justificationBand,
                    justificationMatchesBand = snapshot.measurement.justificationMatchesBand,
                    referenceMaterialsOpened = snapshot.measurement.referenceMaterialsOpened,
                    initialSelectionReferenceMaterials = snapshot.measurement.initialSelection?.priorReferenceMaterials)
            }
            fun saved(value: MeasurementSnapshot?): CompactSavedSelection? = value?.takeIf {
                image != null && it.imageSha256 == image.sha256 && it.nativeWidthPx == image.nativeWidthPx && it.nativeHeightPx == image.nativeHeightPx
            }?.let {
                CompactSavedSelection(TutorImageMeasurement(it.imageSha256, it.nativeWidthPx, it.nativeHeightPx,
                    it.bandTopInclusivePx, it.bandBottomExclusivePx, it.markers.map { m -> m.xPx.toDouble() },
                    TutorMeasurementOrigin.OWN_SELECTION, referenceViewed = it.referenceViewedAtCapture,
                    justificationBand = it.band, justificationMatchesBand = true,
                    referenceMaterialsOpened = it.priorReferenceMaterials), it.justification)
            }
            CompactOwnWork.Image(
                current, if (includeMeasurement) snapshot.measurement.justification else "",
                saved(snapshot.measurement.initialSelection), saved(snapshot.measurement.comparison),
                notes(when (point) {
                    null -> chapterNotes(12, 15)
                    TutorHelpPoint.IMAGE_MEASUREMENT -> listOf(StationId.S14_MEASUREMENT)
                    TutorHelpPoint.SLIT_MODEL -> listOf(StationId.S13_INTERFERENCE)
                    else -> emptyList()
                }),
                includeMeasurement = includeMeasurement,
                slitModel = if (point == null || point == TutorHelpPoint.SLIT_MODEL) CompactSlitModel(
                    SlitModelComparison(spacingUm = snapshot.modelSlitSpacingUm, widthUm = snapshot.modelSlitWidthUm),
                    snapshot.inquiry.slitComparison,
                    extras.slitExploration?.let { exploration ->
                        exploration.copy(notes = exploration.notes.filterValues { it.prediction.isNotBlank() || it.reason.isNotBlank() })
                    },
                ) else null,
            )
        }
        AppSection.CONCLUSION -> CompactOwnWork.Conclusion(
            snapshot.stationWork(StationId.S16_CONCLUSION).note,
            snapshot.inquiry.evidenceRows.mapIndexedNotNull { index, row ->
                if (row.claim.isBlank() && row.evidence.isBlank() && row.limitation.isBlank()) null
                else CompactEvidenceRow(index + 1, row)
            },
            relatedWork = if (includeEarlierResults) listOf(AppSection.DE_BROGLIE, AppSection.DAVISSON_GERMER, AppSection.JONSSON)
                .map { compactOwnWork(snapshot, it, extras = extras) } else emptyList(),
        )
    }
}
