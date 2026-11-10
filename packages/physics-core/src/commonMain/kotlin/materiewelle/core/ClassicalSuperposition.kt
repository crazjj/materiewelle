package materiewelle.core

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Classical teaching model of two coherent, equally large harmonic contributions.
 * Position, wavelength and displacement share arbitrary, explicitly non-SI diagram units.
 * [pathDifferenceWavelengths] is Δs/λ; [sample]'s time is t/T, common to both waves.
 * With u1 = A sin(kx − ωt), the longer second path delays the signal:
 * u2(x,t) = u1(x,t − τ), τ/T = Δs/λ, hence its phase shift has a plus sign.
 * This does not model an electron trajectory, a quantum wavefunction, or photographic grey values.
 */
data class ClassicalSuperposition(
    val amplitude: Double,
    val wavelength: Double,
    val pathDifferenceWavelengths: Double,
) {
    init {
        require(amplitude.isFinite() && amplitude >= 0.0 && amplitude <= Double.MAX_VALUE / 2.0) { "Die Amplitude muss endlich und nichtnegativ sein." }
        require(wavelength.isFinite() && wavelength > 0.0) { "Die Wellenlänge muss endlich und positiv sein." }
        require(pathDifferenceWavelengths.isFinite()) { "Der relative Zusatzweg muss endlich sein." }
    }

    /** Maximum magnitude over a full common cycle, not the instantaneous displacement. */
    val resultantAmplitude: Double
        get() = 2.0 * amplitude * abs(cos(PI * (pathDifferenceWavelengths % 1.0)))

    fun sample(position: Double, timeTurns: Double): ClassicalWaveSample {
        require(position.isFinite() && timeTurns.isFinite())
        // Reduce cycles before multiplication so finite, very large inputs cannot overflow the phase.
        val phase = 2.0 * PI * ((position % wavelength) / wavelength - timeTurns % 1.0)
        return ClassicalWaveSample(
            first = amplitude * sin(phase),
            second = amplitude * sin(phase + 2.0 * PI * (pathDifferenceWavelengths % 1.0)),
        )
    }
}

data class ClassicalWaveSample(val first: Double, val second: Double) {
    val sum: Double get() = first + second
}
