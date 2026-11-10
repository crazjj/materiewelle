package materiewelle.pages

import kotlin.math.roundToInt
import materiewelle.MeasurementProfile
import materiewelle.core.NativePixel

/** One resolved raster sample drives the image crosshair, profile line, slider and readout. */
internal data class MeasurementProfileProbe(val xPx: Int, val grayscale: Double)

/**
 * A profile covers only part of the photograph. Resolve its probe before rendering any view,
 * including the first frame after loading or switching bands. This never moves a measurement
 * marker: a requested point outside the available profile only limits this separate probe.
 */
internal fun MeasurementProfile.resolveProbe(requested: NativePixel?): MeasurementProfileProbe? {
    if (grayscale.isEmpty()) return null
    val first = profileStartX
    val last = first + grayscale.lastIndex
    val x = (requested?.value?.roundToInt() ?: first).coerceIn(first, last)
    return MeasurementProfileProbe(x, grayscale[x - first])
}
