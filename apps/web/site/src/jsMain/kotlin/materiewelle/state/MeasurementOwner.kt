package materiewelle.state

import materiewelle.core.MeasurementSession

/** A controlled scientific tool always reads its owner's latest accepted measurement. */
interface MeasurementOwner {
    val measurement: MeasurementSession
    val canEditMeasurement: Boolean get() = true
    fun updateMeasurement(transform: (MeasurementSession) -> MeasurementSession)
}
