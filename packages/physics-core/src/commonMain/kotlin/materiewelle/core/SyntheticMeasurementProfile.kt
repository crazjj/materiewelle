package materiewelle.core

import kotlin.math.exp

/**
 * Constructed relative heights for comparing a flank, a peak and a plateau in a teaching probe.
 * This is not a scan, an optical calibration, an electron intensity or an uncertainty model.
 */
fun syntheticMeasurementHeight(position: Int): Double {
    val distance = (position - 24) / 8.0
    val peak = 0.15 + 0.8 * exp(-distance * distance)
    val plateau =
        when {
            position < 48 || position > 76 -> 0.15
            position < 56 -> 0.15 + 0.6 * (position - 48) / 8.0
            position <= 66 -> 0.75
            else -> 0.15 + 0.6 * (76 - position) / 10.0
        }
    return maxOf(peak, plateau)
}
