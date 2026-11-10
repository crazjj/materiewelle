package materiewelle.core.course

import kotlinx.serialization.Serializable
import materiewelle.core.*
import kotlin.math.sqrt

@Serializable
enum class LearningPhase(val key: String, val label: String) {
    PREDICT("vorhersagen", "Vorhersagen"), EXPLORE("untersuchen", "Untersuchen"),
    EVALUATE("auswerten", "Auswerten"), EXPLAIN("erklaeren", "Erklären");
    val next: LearningPhase? get() = entries.getOrNull(ordinal + 1)
}

/** NR comparison for the same particle, from rest; no simulated measurement data. */
data class VoltageScaling(val reference: Voltage, val changed: Voltage) {
    val energyFactor: Double get() = changed.volts / reference.volts
    val momentumFactor: Double get() = sqrt(energyFactor)
    val wavelengthFactor: Double get() = Physics.electronWavelength(changed).meters / Physics.electronWavelength(reference).meters
}

enum class MarkerPerturbation(val label: String) { INNER("Erste innere Marke"), LAST("Letzte Marke") }

sealed interface PixelSensitivity {
    data class Unavailable(val reason: String) : PixelSensitivity
    data class Ready(val changedIndex: Int, val originalIntervals: List<Int>, val changedIntervals: List<Int>,
                     val originalMeanPx: Double, val changedMeanPx: Double) : PixelSensitivity {
        val meanChangePx: Double get() = changedMeanPx - originalMeanPx
    }
}

/** A hypothetical copy of an actual native selection; never edits stored own/initial marks. */
fun nativePixelSensitivity(markers: List<NativeImageMark>, image: MeasurementImageIdentity,
                           target: MarkerPerturbation, deltaPx: Int): PixelSensitivity {
    require(deltaPx in -1..1)
    MeasurementSession(markers = markers).validate(image)
    if (markers.size < 2) return PixelSensitivity.Unavailable("Setze zuerst mindestens zwei eigene Marken im Labor.")
    if (target == MarkerPerturbation.INNER && markers.size < 3)
        return PixelSensitivity.Unavailable("Eine innere Marke gibt es erst ab drei Marken.")
    val index = if (target == MarkerPerturbation.INNER) 1 else markers.lastIndex
    val coordinate = markers[index].xPx + deltaPx
    if (coordinate !in image.cropLeftInclusivePx until image.cropRightExclusivePx ||
        index > 0 && coordinate <= markers[index - 1].xPx ||
        index < markers.lastIndex && coordinate >= markers[index + 1].xPx)
        return PixelSensitivity.Unavailable("Die Kopie würde eine Marke kreuzen oder den gültigen Bildausschnitt verlassen.")
    val changed = markers.mapIndexed { i, mark -> if (i == index) NativeImageMark(coordinate) else mark }
    val originalIntervals = markers.zipWithNext { a, b -> b.xPx - a.xPx }
    val changedIntervals = changed.zipWithNext { a, b -> b.xPx - a.xPx }
    return PixelSensitivity.Ready(index, originalIntervals, changedIntervals, originalIntervals.average(), changedIntervals.average())
}
