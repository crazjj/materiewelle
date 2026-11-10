package materiewelle.pages

import materiewelle.core.MeasurementBand
import materiewelle.core.MeasurementProfile
import materiewelle.core.MeasurementSelection
import materiewelle.core.NativeImageBand
import materiewelle.core.PixelMeasurement

internal enum class MeasurementComparisonHint { CHECK_INTERMEDIATE_MAXIMA }

/** A reading prompt, never a grade or a correction of the learner's marks.
 * The ±10% window around twice the reference is a UI heuristic, not a measurement uncertainty.
 * Reference-derived hints are forbidden until the learner deliberately opens that comparison.
 */
internal fun measurementComparisonHint(
    selection: MeasurementSelection,
    band: MeasurementBand,
    nativeBand: NativeImageBand,
    profile: MeasurementProfile?,
    referenceVisible: Boolean,
): MeasurementComparisonHint? {
    if (!referenceVisible || profile == null || selection !is MeasurementSelection.Ready) return null
    if (profile.id != band.profileId || profile.bandTop != nativeBand.topInclusive.toDouble() ||
        profile.bandBottom != nativeBand.bottomExclusive.toDouble()) return null
    if (profile.markers.size < 2 || profile.markers.any { !it.isFinite() || it < 0.0 } ||
        profile.markers.zipWithNext().any { (left, right) -> right <= left }) return null
    val referenceMean = PixelMeasurement.mean(profile.markers)
    if (!selection.meanPx.isFinite() || !referenceMean.isFinite() || referenceMean <= 0.0) return null
    val ratio = selection.meanPx / referenceMean
    return MeasurementComparisonHint.CHECK_INTERMEDIATE_MAXIMA.takeIf { ratio in 1.8..2.2 }
}
