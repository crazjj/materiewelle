package materiewelle.core

import kotlin.test.*

class ElectronPredictionTest {
    @Test fun independent150VoltIntermediatesAndSiBalance() {
        val result = ElectronPrediction(Voltage(150.0))
        assertEquals(2.403264951e-17, result.kineticEnergyJoules, 1e-28)
        assertEquals(6.61698762350755e-24, result.momentumKilogramMetersPerSecond, 1e-34)
        assertEquals(100.137260744756, result.wavelength.picometers, 1e-10)
        assertEquals(Physics.H, result.momentumKilogramMetersPerSecond * result.wavelength.meters, 1e-46)
    }
}
