package materiewelle.pages

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import materiewelle.core.MeasurementBand
import materiewelle.core.MeasurementProfile
import materiewelle.core.MeasurementSelection
import materiewelle.core.NativeImageBand
import materiewelle.core.NativeImageMark

class MeasurementGuidanceTest {
    private val band = NativeImageBand(100, 220)
    private val reference = MeasurementProfile("basis", "Basisband",
        listOf(551.0, 564.0, 578.0, 592.0, 606.0, 620.0, 634.0, 647.0), 100.0, 220.0, 160.0)
    private val everyOther = MeasurementSelection.Ready(listOf(27, 27, 27), 27.0)

    @Test fun hiddenReferenceNeverLeaksAComparisonHint() {
        assertNull(measurementComparisonHint(everyOther, MeasurementBand.BASE, band, reference, false))
    }

    @Test fun nearlyDoubledSpacingOffersAQuestionWithoutChangingEitherSelection() {
        val originalMarkers = reference.markers.toList()
        val originalIntervals = everyOther.intervalsPx.toList()
        assertEquals(MeasurementComparisonHint.CHECK_INTERMEDIATE_MAXIMA,
            measurementComparisonHint(everyOther, MeasurementBand.BASE, band, reference, true))
        assertEquals(originalMarkers, reference.markers)
        assertEquals(originalIntervals, everyOther.intervalsPx)
        assertEquals(27.0, everyOther.meanPx)
    }

    @Test fun differentBandOrDifferentNativeBoundsAreNotCompared() {
        assertNull(measurementComparisonHint(everyOther, MeasurementBand.UPPER, NativeImageBand(20, 45), reference, true))
        assertNull(measurementComparisonHint(everyOther, MeasurementBand.BASE, NativeImageBand(100, 200), reference, true))
        assertNull(measurementComparisonHint(everyOther, MeasurementBand.BASE, band, reference.copy(id = "unten"), true))
    }

    @Test fun ordinaryDifferencesAndIncompleteSelectionsHaveNoSuchHint() {
        assertNull(measurementComparisonHint(MeasurementSelection.Ready(listOf(13, 14, 14), 41.0 / 3), MeasurementBand.BASE, band, reference, true))
        assertNull(measurementComparisonHint(MeasurementSelection.Ready(listOf(20, 20, 20), 20.0), MeasurementBand.BASE, band, reference, true))
        assertNull(measurementComparisonHint(MeasurementSelection.Empty, MeasurementBand.BASE, band, reference, true))
        assertNull(measurementComparisonHint(MeasurementSelection.Partial(NativeImageMark(551)), MeasurementBand.BASE, band, reference, true))
    }

    @Test fun unavailableOrInvalidReferenceCannotProduceAHint() {
        assertNull(measurementComparisonHint(everyOther, MeasurementBand.BASE, band, null, true))
        assertNull(measurementComparisonHint(everyOther, MeasurementBand.BASE, band, reference.copy(markers = emptyList()), true))
        assertNull(measurementComparisonHint(everyOther, MeasurementBand.BASE, band, reference.copy(markers = listOf(551.0, 551.0)), true))
        assertNull(measurementComparisonHint(everyOther, MeasurementBand.BASE, band, reference.copy(markers = listOf(Double.NaN, 578.0)), true))
        assertNull(measurementComparisonHint(everyOther, MeasurementBand.BASE, band, reference.copy(markers = listOf(-1.0, 12.0)), true))
        assertNull(measurementComparisonHint(everyOther.copy(meanPx = Double.NaN), MeasurementBand.BASE, band, reference, true))
    }
}
