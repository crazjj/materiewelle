package materiewelle.core

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

enum class WavelengthApproximation { NONRELATIVISTIC, RELATIVISTIC }

/**
 * Ideal coherent Fraunhofer double slit with rectangular slits; no historical photo calibration.
 * For a free observation plane the complete aperture extent g+b must obey (g+b)^2/(lambda L) << 1.
 * A lens-generated Fourier plane is a separate optical assumption; L alone never establishes it.
 */
data class FiniteSlitModel(
    val voltage: Voltage, val distance: Length, val slitSpacing: Length, val slitWidth: Length,
    val approximation: WavelengthApproximation = WavelengthApproximation.RELATIVISTIC
) {
    init {
        require(distance.meters > 0.0 && slitWidth.meters > 0.0 && slitSpacing.meters >= slitWidth.meters) {
            "Positive Entfernung und Spaltbreite sowie Mittenabstand ≥ Breite erforderlich."
        }
    }

    val wavelength: Length get() = when (approximation) {
        WavelengthApproximation.NONRELATIVISTIC -> Physics.electronWavelength(voltage)
        WavelengthApproximation.RELATIVISTIC -> Physics.relativisticElectronWavelength(voltage)
    }
    val paraxialFringeSpacing: Length get() = Physics.fringeSpacing(distance, wavelength, slitSpacing)

    /** Complete transverse opening extent, from the left outer edge to the right outer edge. */
    val apertureExtent: Length get() = Length(slitSpacing.meters + slitWidth.meters)

    /**
     * Conservative full-width convention F=(g+b)^2/(lambda L), explicitly not a half-width convention.
     * A small value supports the free-space Fraunhofer assumption. It does not check an optical train,
     * coherence or an independently known scale, and a small observation angle alone is insufficient.
     */
    val fullApertureFresnelNumber: Double get() = ((apertureExtent.meters / wavelength.meters) * (apertureExtent.meters / distance.meters)).also {
        require(it.isFinite()) { "Fresnel-Kontrollzahl außerhalb des endlichen Rechenbereichs." }
    }

    /** Signed position on the ideal observation plane, in SI metres. Peak-normalized [0,1]. */
    fun relativeIntensity(positionM: Double): Double {
        require(positionM.isFinite())
        val sinTheta = positionM / hypot(distance.meters, positionM)
        val beta = PI * (slitSpacing.meters / wavelength.meters) * sinTheta
        require(beta.isFinite()) { "Modellphase außerhalb des endlichen Rechenbereichs." }
        val interferenceAmplitude = cos(beta)
        return (relativeEnvelope(positionM) * interferenceAmplitude * interferenceAmplitude).coerceIn(0.0, 1.0)
    }

    /** Individual rectangular-slit envelope, on the same normalized scale as relativeIntensity. */
    fun relativeEnvelope(positionM: Double): Double {
        require(positionM.isFinite())
        val sinTheta = positionM / hypot(distance.meters, positionM)
        val alpha = PI * (slitWidth.meters / wavelength.meters) * sinTheta
        require(alpha.isFinite()) { "Modellphase außerhalb des endlichen Rechenbereichs." }
        val envelopeAmplitude = if (alpha == 0.0) 1.0 else sin(alpha) / alpha
        return (envelopeAmplitude * envelopeAmplitude).coerceIn(0.0, 1.0)
    }
}
