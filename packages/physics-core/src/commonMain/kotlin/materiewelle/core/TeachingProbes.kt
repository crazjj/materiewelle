package materiewelle.core

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos

enum class LensProbeRay { FIRST, SECOND }

/**
 * Ideal thin-lens teaching probe, with all lengths divided by one arbitrary focal length.
 * The bounded slopes and entry heights keep both incoming and outgoing slopes small.
 * This is neither a historical lens reconstruction nor a photograph calibration.
 */
data class ParaxialLensProbe(
    val directionSlope: Double = 0.0,
    val heightOffsetOverF: Double = 0.0,
    val observationDistanceOverF: Double = 1.0,
) {
    init {
        require(directionSlope.isFinite() && directionSlope in -.01.. .01)
        require(heightOffsetOverF.isFinite() && heightOffsetOverF in -.025.. .025)
        require(observationDistanceOverF.isFinite() && observationDistanceOverF in .6..1.4)
    }

    val isFocalPlane: Boolean get() = observationDistanceOverF == 1.0

    fun entryHeightOverF(ray: LensProbeRay): Double =
        heightOffsetOverF + if (ray == LensProbeRay.FIRST) -.035 else .035

    fun outgoingSlope(ray: LensProbeRay): Double = directionSlope - entryHeightOverF(ray)

    fun incomingHeightOverF(ray: LensProbeRay, distanceBeforeLensOverF: Double): Double {
        require(distanceBeforeLensOverF.isFinite() && distanceBeforeLensOverF in 0.0..2.0)
        return entryHeightOverF(ray) - distanceBeforeLensOverF * directionSlope
    }

    /** A propagation coordinate, distinct from the user-selected observation plane. */
    fun heightAtDistanceOverF(ray: LensProbeRay, distanceAfterLensOverF: Double): Double {
        require(distanceAfterLensOverF.isFinite() && distanceAfterLensOverF in 0.0..2.0)
        return entryHeightOverF(ray) + distanceAfterLensOverF * outgoingSlope(ray)
    }

    fun observedHeightOverF(ray: LensProbeRay): Double =
        heightAtDistanceOverF(ray, observationDistanceOverF)

    val spotSeparationOverF: Double
        get() = abs(observedHeightOverF(LensProbeRay.SECOND) - observedHeightOverF(LensProbeRay.FIRST))
}

/**
 * Equal positive isolated intensities at one fixed observation point, stable relative phase.
 * All results share the same scale: no independent normalization of the two model predictions.
 * Only the independent-contributions model is compared with the ideal coherent model.
 */
data class EqualIntensityInterferenceProbe(
    val pathDifferenceInWavelengths: Double,
    val singleContribution: Double = 1.0,
) {
    init {
        require(pathDifferenceInWavelengths.isFinite() && pathDifferenceInWavelengths in 0.0..1.0)
        require(singleContribution.isFinite() && singleContribution > 0.0 && singleContribution <= Double.MAX_VALUE / 4.0)
    }

    val independentSum: Double get() = 2.0 * singleContribution

    val coherentIntensity: Double
        get() = singleContribution * (2.0 + 2.0 * cos(2.0 * PI * pathDifferenceInWavelengths)).coerceIn(0.0, 4.0)
}
