package materiewelle.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SyntheticMeasurementProfileTest {
    @Test fun theThreeExplanationsMatchTheActuallyComputedNeighbors() {
        assertTrue(syntheticMeasurementHeight(13) < syntheticMeasurementHeight(14))
        assertTrue(syntheticMeasurementHeight(14) < syntheticMeasurementHeight(15))
        assertTrue(syntheticMeasurementHeight(23) < syntheticMeasurementHeight(24))
        assertTrue(syntheticMeasurementHeight(25) < syntheticMeasurementHeight(24))
        (56..66).forEach { assertEquals(0.75, syntheticMeasurementHeight(it)) }
        assertTrue(syntheticMeasurementHeight(55) < syntheticMeasurementHeight(56))
        assertTrue(syntheticMeasurementHeight(67) < syntheticMeasurementHeight(66))
    }

    @Test fun theWholeSyntheticCurveUsesOneFiniteRelativeHeightScale() {
        (0..80).forEach {
            val height = syntheticMeasurementHeight(it)
            assertTrue(height.isFinite() && height in 0.0..1.0)
        }
    }
}
