package materiewelle.core

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.sqrt

/** Angle measured from the outward surface normal, never from a Bragg plane. */
data class SurfaceNormalAngle(val degrees: Double) {
    init { require(degrees.isFinite() && degrees in 0.0..90.0) }
}

/** Glancing angle between a ray and the Bragg lattice plane. */
data class BraggPlaneAngle(val degrees: Double) {
    init { require(degrees.isFinite() && degrees in 0.0..90.0) }
}

data class BraggPathDifference(val incomingExtra: Length, val outgoingExtra: Length) {
    val total: Length get() = Length(incomingExtra.meters + outgoingExtra.meters)
}

/** UI-free SI calculations for the static Kobweb laboratory. */
object Physics {
    // h, e and c are exact SI constants; electron mass is CODATA 2022.
    const val H = 6.62607015e-34
    const val E = 1.602176634e-19
    const val ELECTRON_MASS = 9.1093837139e-31
    const val C = 299792458.0
    const val MIN_VOLTS = 20.0
    const val MAX_VOLTS = 1000.0

    fun electronWavelength(voltage: Voltage): Length =
        Length((H / sqrt(2.0 * ELECTRON_MASS * E)) / sqrt(voltage.volts))

    fun relativisticElectronWavelength(voltage: Voltage): Length = Length(
        electronWavelength(voltage).meters / sqrt(1.0 + E * voltage.volts / (2.0 * ELECTRON_MASS * C * C))
    )

    /** First-order surface-row model. D is not the interplanar spacing used by Bragg's law. */
    fun surfaceDiffraction(rowSpacing: Length, angle: Angle): Length {
        return surfaceDiffraction(rowSpacing, SurfaceNormalAngle(angle.degrees))
    }

    fun surfaceDiffraction(rowSpacing: Length, angle: SurfaceNormalAngle): Length {
        require(rowSpacing.meters > 0.0)
        return Length(rowSpacing.meters * sin(angle.degrees * PI / 180.0))
    }

    /** Independent fcc Ni(111) triangular-net geometry; no electron inputs. */
    fun nickel111RowSpacing(latticeConstant: Length): Length {
        require(latticeConstant.meters > 0.0)
        return Length(latticeConstant.meters * sqrt(3.0 / 8.0))
    }

    fun nickel111NearestNeighbor(latticeConstant: Length): Length {
        require(latticeConstant.meters > 0.0)
        return Length(latticeConstant.meters / sqrt(2.0))
    }

    fun nickel111PlaneSpacing(latticeConstant: Length): Length {
        require(latticeConstant.meters > 0.0)
        return Length(latticeConstant.meters / sqrt(3.0))
    }

    /** Two equal extra path lengths for reflection at successive parallel planes. */
    fun braggPathDifference(planeSpacing: Length, angle: BraggPlaneAngle): BraggPathDifference {
        require(planeSpacing.meters > 0.0)
        val extra = Length(planeSpacing.meters * sin(angle.degrees * PI / 180.0))
        return BraggPathDifference(extra, extra)
    }

    /** null is an explicit inaccessible first-order direction, not an invented maximum. */
    fun surfaceMaximumAngle(rowSpacing: Length, wavelength: Length): SurfaceNormalAngle? {
        require(rowSpacing.meters > 0.0 && wavelength.meters > 0.0)
        val ratio = wavelength.meters / rowSpacing.meters
        return if (ratio > 1.0) null else SurfaceNormalAngle(asin(ratio) * 180.0 / PI)
    }

    /** Independent parameter model, never a calibration of the historical photograph. */
    fun fringeSpacing(distance: Length, wavelength: Length, slitSpacing: Length): Length {
        require(distance.meters > 0.0 && wavelength.meters > 0.0 && slitSpacing.meters > 0.0)
        return Length(distance.meters * wavelength.meters / slitSpacing.meters)
    }

    // Numeric adapters keep display units at the application boundary.
    fun wavelengthNonRelativistic(voltageV: Double): Double = electronWavelength(Voltage(voltageV)).meters
    fun wavelengthRelativistic(voltageV: Double): Double = relativisticElectronWavelength(Voltage(voltageV)).meters
    fun wavelengthMeters(voltageVolts: Double): Double = wavelengthNonRelativistic(voltageVolts)
    fun surfaceDiffractionWavelength(rowSpacingM: Double, angleDegrees: Double): Double =
        surfaceDiffraction(Length(rowSpacingM), Angle(angleDegrees)).meters

    fun relativeDifferencePercent(observed: Double, predicted: Double): Double {
        require(predicted.isFinite() && predicted > 0.0 && observed.isFinite())
        return (abs(observed - predicted) / predicted * 100.0).also {
            require(it.isFinite()) { "Relative Abweichung liegt außerhalb des endlichen Rechenbereichs." }
        }
    }

    fun signedRelativeDifferencePercent(observed: Double, predicted: Double): Double {
        require(predicted.isFinite() && predicted > 0.0 && observed.isFinite())
        return ((observed - predicted) / predicted * 100.0).also {
            require(it.isFinite()) { "Relative Differenz liegt außerhalb des endlichen Rechenbereichs." }
        }
    }

    fun inputVoltage(raw: String): Double? = raw.trim().replace(',', '.').toDoubleOrNull()
        ?.takeIf { it.isFinite() && it in MIN_VOLTS..MAX_VOLTS }
    fun parseProbeVoltage(text: String): Double? = inputVoltage(text)
    fun displayPicometers(voltageVolts: Double): String = decimal(wavelengthMeters(voltageVolts) * 1e12, 2)

    /** Locale-independent German fixed-point display without JavaScript dynamic casts. */
    fun decimal(value: Double, places: Int): String {
        require(value.isFinite() && places in 0..9)
        var scale = 1L
        repeat(places) { scale *= 10 }
        if (abs(value) >= Long.MAX_VALUE.toDouble() / scale) {
            return value.toString().replace('E', 'e').replace('.', ',').replace('-', '−')
        }
        val scaled = round(abs(value) * scale).toLong()
        val sign = if (value < 0.0 && scaled != 0L) "−" else ""
        val whole = scaled / scale
        val fraction = (scaled % scale).toString().padStart(places, '0')
        return if (places == 0) "$sign$whole" else "$sign$whole,$fraction"
    }
}
