package materiewelle.core.course

import kotlinx.serialization.Serializable
import materiewelle.core.ExperimentMode
import materiewelle.core.Physics
import materiewelle.core.Voltage

@Serializable
data class DeBroglieSession(
    val raw: String = "150", val lastValidVoltage: Double = 150.0,
    val comparisonVoltage: Double? = null, val mode: ExperimentMode = ExperimentMode.LAB, val step: Int = 0
) {
    fun edit(text: String) = copy(raw = text.take(120), lastValidVoltage = Physics.inputVoltage(text) ?: lastValidVoltage)
    fun validate(): DeBroglieSession {
        require(raw.length <= 120 && lastValidVoltage.isFinite() && lastValidVoltage in 20.0..1000.0)
        require(comparisonVoltage == null || comparisonVoltage.isFinite() && comparisonVoltage in 20.0..1000.0)
        require(step in 0..2)
        return this
    }
}

@Serializable
enum class DgDiagram { SETUP, BRAGG, SURFACE, WAVELENGTHS }

@Serializable
data class DgSession(
    val mode: ExperimentMode = ExperimentMode.LAB, val step: Int = 0,
    val selectedId: String? = null, val compareId: String? = null,
    val modelVoltageRaw: String = "54", val modelAngleRaw: String = "50",
    val validModelVoltage: Double = 54.0, val validModelAngle: Double = 50.0,
    val geometryStep: Int = 0, val view: DgDiagram = DgDiagram.SETUP,
    val comparisonVoltage: Double? = null, val comparisonAngle: Double? = null
) {
    fun validate(): DgSession {
        require(step in 0..3 && geometryStep in 0..4)
        require(selectedId == null || selectedId.length <= 100)
        require(compareId == null || compareId.length <= 100)
        require(modelVoltageRaw.length <= 120 && modelAngleRaw.length <= 120)
        // Imported source cases have a wider physical domain than the free-editing UI range.
        fun validateVoltage(value: Double) {
            val wavelength = Physics.electronWavelength(Voltage(value))
            require(wavelength.picometers.isFinite() && wavelength.picometers > 0.0)
        }
        validateVoltage(validModelVoltage)
        require(validModelAngle.isFinite() && validModelAngle in 0.1..90.0)
        require((comparisonVoltage == null) == (comparisonAngle == null))
        comparisonVoltage?.let(::validateVoltage)
        comparisonAngle?.let { require(it.isFinite() && it in 0.1..90.0) }
        return this
    }
}
