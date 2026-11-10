package materiewelle.pages

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import materiewelle.MeasurementProfile
import materiewelle.core.NativePixel

class MeasurementProfileProbeTest {
    private fun profile(first: Int = 545, values: List<Double> = listOf(0.0, 83.0, 192.0)) =
        MeasurementProfile("basis", "Testband", listOf(546.0), 100.0, 220.0, 160.0, first, values)

    @Test fun newlyLoadedProfileProvidesOneValidPointAndItsActualGrayValue() {
        assertEquals(MeasurementProfileProbe(545, 0.0), profile().resolveProbe(null))
        // The old image-crop start is outside the profile: it must not survive in the readout.
        assertEquals(MeasurementProfileProbe(545, 0.0), profile().resolveProbe(NativePixel(490.0)))
    }

    @Test fun bothEdgesLimitOnlyTheProbeAndLeaveOwnCoordinatesUntouched() {
        val scan = profile()
        val candidate = NativePixel(702.0)
        assertEquals(MeasurementProfileProbe(547, 192.0), scan.resolveProbe(candidate))
        assertEquals(702.0, candidate.value)
        assertEquals(listOf(546.0), scan.markers)
    }

    @Test fun switchingBandsUsesTheNewRangeAndTheNewBandsGrayValueImmediately() {
        val requested = NativePixel(546.0)
        assertEquals(MeasurementProfileProbe(546, 83.0), profile().resolveProbe(requested))
        assertEquals(
            MeasurementProfileProbe(548, 42.0),
            profile(548, listOf(42.0, 91.0)).resolveProbe(requested),
        )
        assertEquals(
            MeasurementProfileProbe(546, 17.0),
            profile(545, listOf(3.0, 17.0, 94.0)).resolveProbe(requested),
        )
    }

    @Test fun samplingUsesTheNearestNativeColumnAndNeverInventsMissingProfileData() {
        assertEquals(MeasurementProfileProbe(547, 192.0), profile().resolveProbe(NativePixel(546.7)))
        assertNull(profile(values = emptyList()).resolveProbe(NativePixel(546.0)))
    }
}
