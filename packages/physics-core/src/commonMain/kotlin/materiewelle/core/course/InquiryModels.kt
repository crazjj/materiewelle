package materiewelle.core.course

import kotlinx.serialization.Serializable
import materiewelle.core.Physics

@Serializable
enum class CrystalAssignmentChoice { NEIGHBOR, ROWS, PLANES }

@Serializable
enum class GeometryPathChoice { INCOMING, OUTGOING, BOTH }

@Serializable
enum class OpticsStage { SOURCE, SLITS, OPTICS, REPRODUCTION }

@Serializable
enum class CrystalViewMode { CELL, SURFACE, PLANES }

@Serializable
enum class LimitCaseId { NR_50KV, SOURCE_VOLTAGE, SCAN_SCALE }

@Serializable
enum class LimitKind { MODEL, SOURCE, CALIBRATION }

@Serializable
data class EvidenceRow(val claim: String = "", val evidence: String = "", val limitation: String = "") {
    fun validate(): EvidenceRow {
        require(claim.length <= 1000 && evidence.length <= 1000 && limitation.length <= 1000)
        return this
    }
}

/** Persisted model settings, not observations from a historical apparatus. */
@Serializable
data class SlitModelComparison(
    val voltageV: Double = 150.0,
    val distanceM: Double = 10.0,
    val spacingUm: Double,
    val widthUm: Double,
) {
    fun validate(): SlitModelComparison {
        require(voltageV.isFinite() && voltageV in Physics.MIN_VOLTS..Physics.MAX_VOLTS)
        require(distanceM.isFinite() && distanceM in 0.01..100.0)
        require(spacingUm.isFinite() && spacingUm in 1.0..5.0)
        require(widthUm.isFinite() && widthUm in 0.1..0.9 && widthUm < spacingUm)
        return this
    }
}

/** UI-independent experiment choices survive navigation and a later visit. */
@Serializable
data class InquiryState(
    val waveAmplitude: Double = 38.0,
    val waveWavelength: Double = 110.0,
    val phaseTurns: Double = 0.0,
    val superpositionRevealed: Boolean = false,
    val superpositionPrediction: String = "",
    val opticsStage: OpticsStage = OpticsStage.SOURCE,
    val reproductionScale: Double = 1.0,
    val opticsPrediction: OpticsStage? = null,
    val opticsRevealed: Boolean = false,
    val geometryPrediction: GeometryPathChoice? = null,
    val crystalView: CrystalViewMode = CrystalViewMode.CELL,
    val crystalAssignment: CrystalAssignmentChoice? = null,
    val crystalRevealed: Boolean = false,
    val momentumFactor: Double = 1.0,
    val slitComparison: SlitModelComparison? = null,
    val limitAssignments: Map<LimitCaseId, LimitKind> = emptyMap(),
    val limitsChecked: Boolean = false,
    val evidenceRows: List<EvidenceRow> = List(3) { EvidenceRow() },
) {
    fun validate(): InquiryState {
        require(waveAmplitude.isFinite() && waveAmplitude in 10.0..45.0)
        require(waveWavelength.isFinite() && waveWavelength in 60.0..180.0)
        require(phaseTurns.isFinite() && phaseTurns in 0.0..1.0)
        require(superpositionPrediction.length <= 1000)
        require(reproductionScale.isFinite() && reproductionScale in 1.0..3.0)
        require(momentumFactor.isFinite() && momentumFactor in 1.0..4.0)
        slitComparison?.validate()
        require(limitAssignments.size <= LimitCaseId.entries.size)
        require(evidenceRows.size == 3)
        evidenceRows.forEach { it.validate() }
        return this
    }
}
