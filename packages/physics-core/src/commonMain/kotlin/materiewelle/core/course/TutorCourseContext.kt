package materiewelle.core.course

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import materiewelle.core.MeasurementBand
import materiewelle.core.ReferenceMaterial

/** Explicit, bounded handoff. This is never a dump of storage, answers, notes or a chat. */
@Serializable
data class TutorCourseContext(
    val station: StationId,
    val courseRevision: String,
    val dataRevision: String,
    val sourceRevision: String,
    val model: TutorModelSnapshot? = null,
    val measurement: TutorImageMeasurement? = null,
) {
    init {
        listOf(courseRevision, dataRevision, sourceRevision).forEach {
            require(Regex("[A-Za-z0-9][A-Za-z0-9._/-]{0,63}").matches(it)) { "Ungültige Kontextrevision." }
        }
    }
}

/** Only accepted inputs travel; receiver recalculates results and keeps their model status. */
@Serializable
sealed interface TutorModelSnapshot {
    @Serializable
    @SerialName("de-broglie-nonrelativistic-model")
    data class DeBroglie(val voltageV: Double) : TutorModelSnapshot {
        init { require(voltageV.isFinite() && voltageV in 20.0..1000.0) }
    }

    @Serializable
    @SerialName("ni111-surface-model-not-original-scan")
    data class Surface(val voltageV: Double, val phiDegrees: Double, val latticeNm: Double) : TutorModelSnapshot {
        init {
            require(voltageV.isFinite() && voltageV > 0.0 && voltageV <= 1000.0)
            require(phiDegrees.isFinite() && phiDegrees > 0.0 && phiDegrees <= 90.0)
            require(latticeNm.isFinite() && latticeNm > 0.0 && latticeNm < 10.0)
        }
    }

    @Serializable
    @SerialName("finite-double-slit-nonrelativistic-teaching-model")
    data class FiniteSlit(
        val voltageV: Double,
        val distanceM: Double,
        val slitSpacingM: Double,
        val slitWidthM: Double,
    ) : TutorModelSnapshot {
        init {
            require(voltageV.isFinite() && voltageV in 20.0..1000.0)
            require(listOf(distanceM, slitSpacingM, slitWidthM).all { it.isFinite() && it > 0.0 })
            require(slitWidthM <= slitSpacingM && (slitSpacingM + slitWidthM).isFinite())
        }
    }

    @Serializable
    @SerialName("relativistic-comparison-model-not-photo-voltage")
    data class HighVoltage(val voltageV: Double) : TutorModelSnapshot {
        init { require(voltageV.isFinite() && voltageV > 0.0 && voltageV <= 200000.0) }
    }
}

@Serializable
enum class TutorMeasurementOrigin {
    @SerialName("own-selection") OWN_SELECTION,
    @SerialName("reference-imported") REFERENCE_IMPORTED,
    @SerialName("edited-after-reference-import") EDITED_AFTER_REFERENCE,
}

/** Native raster coordinates, no absolute scale and no claimed calibrated electron intensity. */
@Serializable
data class TutorImageMeasurement(
    val imageSha256: String,
    val nativeWidth: Int,
    val nativeHeight: Int,
    val bandYStart: Int,
    val bandYEndExclusive: Int,
    val markersNativeX: List<Double>,
    val origin: TutorMeasurementOrigin,
    val selectionReason: String = "",
    /** Seeing a reference never changes ownership/origin of the selected markers. */
    val referenceViewed: Boolean = false,
    /** Null means not recorded by an older handoff, not confirmed absent. */
    val justificationBand: MeasurementBand? = null,
    val justificationMatchesBand: Boolean? = null,
    val referenceMaterialsOpened: Set<ReferenceMaterial>? = null,
    val initialSelectionReferenceMaterials: Set<ReferenceMaterial>? = null,
) {
    init {
        require(Regex("[a-f0-9]{64}").matches(imageSha256))
        require(nativeWidth == 703 && nativeHeight == 242) { "Kontext unterstützt nur das bezeichnete J1-Raster." }
        require(bandYStart >= 0 && bandYEndExclusive <= nativeHeight && bandYStart < bandYEndExclusive)
        require(markersNativeX.size <= 64)
        require(markersNativeX.all { it.isFinite() && it >= 0.0 && it < nativeWidth })
        require(markersNativeX.zipWithNext().all { (a, b) -> a < b })
        require(selectionReason.length <= 1000)
        require(referenceMaterialsOpened == null || referenceMaterialsOpened.size <= ReferenceMaterial.entries.size)
        require(initialSelectionReferenceMaterials == null || initialSelectionReferenceMaterials.size <= ReferenceMaterial.entries.size)
        require(justificationMatchesBand != true || justificationBand != null)
    }
}
