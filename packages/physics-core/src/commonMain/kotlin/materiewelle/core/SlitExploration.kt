package materiewelle.core

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt

/** Signed SI coordinate in the synthetic observation plane; never a historical raster pixel. */
data class ObservationPosition(val meters: Double) {
    init { require(meters.isFinite()) { "Die Position muss endlich sein." } }
    val millimeters: Double get() = meters * 1000.0
}

enum class SlitConfiguration { BOTH_COHERENT, LEFT_ONLY, RIGHT_ONLY, BOTH_INCOHERENT }

/** Relative quantum amplitudes, not literal displacements or trajectories of electrons. */
data class ComplexAmplitude(val real: Double, val imaginary: Double) {
    init { require(real.isFinite() && imaginary.isFinite()) }
    val intensity: Double get() = real * real + imaginary * imaginary
    operator fun plus(other: ComplexAmplitude) =
        ComplexAmplitude(real + other.real, imaginary + other.imaginary)
}

@ConsistentCopyVisibility
data class SlitInterferenceSample internal constructor(
    val position: ObservationPosition,
    /** Far-field left-path minus right-path difference g sin(theta), not an exact near-field path. */
    val pathDifferenceM: Double,
    val relativePhaseTurns: Double,
    val left: ComplexAmplitude,
    val right: ComplexAmplitude,
) {
    /** Only physically applicable to the coherent configuration. */
    val sum: ComplexAmplitude get() = left + right

    fun intensity(configuration: SlitConfiguration): Double = when (configuration) {
        SlitConfiguration.BOTH_COHERENT -> sum.intensity
        SlitConfiguration.LEFT_ONLY -> left.intensity
        SlitConfiguration.RIGHT_ONLY -> right.intensity
        SlitConfiguration.BOTH_INCOHERENT -> left.intensity + right.intensity
    }
}

/**
 * Symmetric phase convention with the shared propagation phase removed. The signed sinc factor
 * is retained beyond the first envelope zero. Every configuration uses the same scale:
 * the coherent two-slit central maximum is one; either individual slit is one quarter there.
 */
fun FiniteSlitModel.interferenceAt(position: ObservationPosition): SlitInterferenceSample {
    val sinTheta = position.meters / hypot(distance.meters, position.meters)
    val difference = slitSpacing.meters * sinTheta
    val turns = difference / wavelength.meters
    val beta = PI * turns
    val alpha = PI * (slitWidth.meters / wavelength.meters) * sinTheta
    require(turns.isFinite() && beta.isFinite() && alpha.isFinite()) { "Modellphase außerhalb des endlichen Bereichs." }
    val sinc = if (abs(alpha) < 1e-8) 1.0 - alpha * alpha / 6.0 else sin(alpha) / alpha
    val real = 0.5 * sinc * cos(beta)
    val imaginary = 0.5 * sinc * sin(beta)
    return SlitInterferenceSample(position, difference, turns,
        ComplexAmplitude(real, imaginary), ComplexAmplitude(real, -imaginary))
}

/** Common intensity scale across configurations; this is not an absolute rate or a probability. */
fun FiniteSlitModel.relativeIntensity(position: ObservationPosition, configuration: SlitConfiguration): Double =
    when (configuration) {
        SlitConfiguration.BOTH_COHERENT -> relativeIntensity(position.meters)
        SlitConfiguration.LEFT_ONLY, SlitConfiguration.RIGHT_ONLY -> relativeEnvelope(position.meters) * 0.25
        SlitConfiguration.BOTH_INCOHERENT -> relativeEnvelope(position.meters) * 0.5
    }

/** Position where g sin(theta) is the requested signed multiple of wavelength, when finite. */
fun FiniteSlitModel.positionAtPathDifference(wavelengthMultiples: Double): ObservationPosition? {
    require(wavelengthMultiples.isFinite())
    val sine = wavelengthMultiples * (wavelength.meters / slitSpacing.meters)
    if (!sine.isFinite() || abs(sine) >= 1.0) return null
    val position = distance.meters * sine / sqrt(1.0 - sine * sine)
    return position.takeIf(Double::isFinite)?.let(::ObservationPosition)
}
