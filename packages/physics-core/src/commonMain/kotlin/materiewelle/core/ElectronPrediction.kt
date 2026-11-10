package materiewelle.core

import kotlin.math.sqrt

/** Start from rest, positive charge magnitude, nonrelativistic kinetic energy. SI throughout. */
data class ElectronPrediction(val voltage: Voltage) {
    val kineticEnergyJoules: Double = Physics.E * voltage.volts
    val momentumKilogramMetersPerSecond: Double = sqrt(2.0 * Physics.ELECTRON_MASS * kineticEnergyJoules)
    val wavelength: Length = Physics.electronWavelength(voltage)
}
