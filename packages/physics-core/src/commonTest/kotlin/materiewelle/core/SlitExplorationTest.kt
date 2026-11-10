package materiewelle.core

import kotlin.math.PI
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SlitExplorationTest {
    private val model = FiniteSlitModel(Voltage(150.0), Length(10.0), Length(2e-6), Length(.5e-6),
        WavelengthApproximation.NONRELATIVISTIC)

    @Test fun observationPositionIsSignedFiniteAndNotANativePixel() {
        assertEquals(-2.0, ObservationPosition(-.002).millimeters)
        assertEquals(0.0, ObservationPosition(0.0).meters)
        assertFailsWith<IllegalArgumentException> { ObservationPosition(Double.NaN) }
        assertFailsWith<IllegalArgumentException> { ObservationPosition(Double.POSITIVE_INFINITY) }
    }

    @Test fun centralContributionsHaveOneSharedScaleAcrossAllConfigurations() {
        val center = model.interferenceAt(ObservationPosition(0.0))
        assertEquals(ComplexAmplitude(.5, 0.0), center.left)
        assertEquals(ComplexAmplitude(.5, -0.0), center.right)
        assertEquals(1.0, center.intensity(SlitConfiguration.BOTH_COHERENT))
        assertEquals(.25, center.intensity(SlitConfiguration.LEFT_ONLY))
        assertEquals(.25, center.intensity(SlitConfiguration.RIGHT_ONLY))
        assertEquals(.5, center.intensity(SlitConfiguration.BOTH_INCOHERENT))
        SlitConfiguration.entries.forEach { configuration ->
            assertEquals(center.intensity(configuration), model.relativeIntensity(center.position, configuration))
        }
    }

    @Test fun halfCycleCancelsCoherentContributionsButNotTheIncoherentSum() {
        val minimum = assertNotNull(model.positionAtPathDifference(.5))
        val sample = model.interferenceAt(minimum)
        assertEquals(model.wavelength.meters * .5, sample.pathDifferenceM, 1e-25)
        assertEquals(.5, sample.relativePhaseTurns, 1e-14)
        assertEquals(-sample.right.imaginary, sample.left.imaginary, 1e-15)
        assertTrue(sample.intensity(SlitConfiguration.BOTH_COHERENT) < 1e-25)
        assertTrue(sample.intensity(SlitConfiguration.BOTH_INCOHERENT) > .1)
    }

    @Test fun probeMatchesTheEstablishedFiniteSlitCurveAndItsEvenIntensity() {
        for (index in -60..60) {
            val position = ObservationPosition(index * .00005)
            val sample = model.interferenceAt(position)
            val mirrored = model.interferenceAt(ObservationPosition(-position.meters))
            assertEquals(model.relativeIntensity(position.meters), sample.intensity(SlitConfiguration.BOTH_COHERENT), 2e-14)
            assertEquals(-sample.pathDifferenceM, mirrored.pathDifferenceM, 1e-25)
            assertEquals(sample.left.real, mirrored.left.real, 1e-14)
            assertEquals(-sample.left.imaginary, mirrored.left.imaginary, 1e-14)
            SlitConfiguration.entries.forEach { configuration ->
                assertEquals(sample.intensity(configuration), mirrored.intensity(configuration), 2e-14)
                assertEquals(sample.intensity(configuration), model.relativeIntensity(position, configuration), 2e-14)
            }
        }
    }

    @Test fun negativeSincBeyondTheEnvelopeZeroRetainsTheCommonPhaseFlip() {
        // g/b=4: six wavelength path difference makes alpha=1.5*pi and beta=6*pi.
        val side = model.interferenceAt(assertNotNull(model.positionAtPathDifference(6.0)))
        assertTrue(side.left.real < 0.0)
        assertTrue(side.right.real < 0.0)
        assertEquals(-1.0 / (3.0 * PI), side.left.real, 1e-14)
        assertEquals(model.relativeIntensity(side.position.meters), side.sum.intensity, 1e-14)
    }

    @Test fun envelopeZeroSuppressesEveryConfiguration() {
        val sine = model.wavelength.meters / model.slitWidth.meters
        val zero = ObservationPosition(model.distance.meters * sine / sqrt(1.0 - sine * sine))
        val sample = model.interferenceAt(zero)
        SlitConfiguration.entries.forEach { assertTrue(sample.intensity(it) < 1e-25) }
    }

    @Test fun pathDifferenceSelectionUsesTheFiniteObservationGeometry() {
        assertEquals(ObservationPosition(0.0), model.positionAtPathDifference(0.0))
        val positive = assertNotNull(model.positionAtPathDifference(1.0))
        val negative = assertNotNull(model.positionAtPathDifference(-1.0))
        assertEquals(positive.meters, -negative.meters)
        assertEquals(1.0, model.interferenceAt(positive).relativePhaseTurns, 1e-14)
        assertNull(model.positionAtPathDifference(Double.MAX_VALUE))
        assertFailsWith<IllegalArgumentException> { model.positionAtPathDifference(Double.NaN) }
    }
}
