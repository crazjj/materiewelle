package materiewelle.core

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TeachingProbesTest {
    @Test fun focalPlaneMapsDirectionToOnePlaceRegardlessOfBothEntryHeights() {
        // Independent optical invariant: every ray with the same input direction meets at f*slope.
        for (slope in listOf(-.01, 0.0, .01)) {
            for (offset in listOf(-.025, 0.0, .025)) {
                val probe = ParaxialLensProbe(slope, offset, 1.0)
                assertTrue(probe.isFocalPlane)
                for (ray in LensProbeRay.entries) {
                    assertEquals(slope, probe.observedHeightOverF(ray), 1e-15)
                }
                assertEquals(0.0, probe.spotSeparationOverF, 1e-15)
            }
        }
    }

    @Test fun defocusRetainsEntryHeightAndReversesTheOrderingAcrossFocus() {
        // Two entries differ by .07 focal lengths. Their separation must scale with |1 - s/f|.
        for (distance in listOf(.6, 1.0, 1.4)) {
            for (slope in listOf(-.01, 0.0, .01)) {
                for (offset in listOf(-.025, 0.0, .025)) {
                    val probe = ParaxialLensProbe(slope, offset, distance)
                    val signedSeparation = probe.observedHeightOverF(LensProbeRay.SECOND) - probe.observedHeightOverF(LensProbeRay.FIRST)
                    assertEquals(.07 * (1.0 - distance), signedSeparation, 1e-15)
                    assertEquals(.07 * abs(1.0 - distance), probe.spotSeparationOverF, 1e-15)
                    if (distance != 1.0) assertFalse(probe.isFocalPlane)
                }
            }
        }
        val before = ParaxialLensProbe(observationDistanceOverF = .6)
        val moved = before.copy(heightOffsetOverF = .025)
        assertEquals(.01, moved.observedHeightOverF(LensProbeRay.FIRST) - before.observedHeightOverF(LensProbeRay.FIRST), 1e-15)
    }

    @Test fun raysAreContinuousAtLensAndHaveSmallAnglesAcrossSupportedRange() {
        for (slope in listOf(-.01, 0.0, .01)) {
            for (offset in listOf(-.025, 0.0, .025)) {
                val probe = ParaxialLensProbe(slope, offset)
                for (ray in LensProbeRay.entries) {
                    val entry = probe.entryHeightOverF(ray)
                    assertEquals(entry, probe.incomingHeightOverF(ray, 0.0))
                    assertEquals(entry, probe.heightAtDistanceOverF(ray, 0.0))
                    assertTrue(abs(probe.outgoingSlope(ray)) <= .07000000000000001)
                }
            }
        }
        // Independent hand-computed case: entry -.035, input slope .01, output slope .045.
        val probe = ParaxialLensProbe(directionSlope = .01)
        assertEquals(-.045, probe.incomingHeightOverF(LensProbeRay.FIRST, 1.0), 1e-15)
        assertEquals(.055, probe.heightAtDistanceOverF(LensProbeRay.FIRST, 2.0), 1e-15)
    }

    @Test fun positiveIndependentContributionsCannotReproduceTheIdealOppositePhaseNull() {
        val opposite = EqualIntensityInterferenceProbe(.5, 3.0)
        assertEquals(3.0, opposite.singleContribution)
        assertEquals(6.0, opposite.independentSum)
        assertEquals(0.0, opposite.coherentIntensity)
        assertEquals(12.0, opposite.copy(pathDifferenceInWavelengths = 0.0).coherentIntensity)
        assertEquals(6.0, opposite.copy(pathDifferenceInWavelengths = .25).coherentIntensity, 1e-14)
        assertEquals(12.0, opposite.copy(pathDifferenceInWavelengths = 1.0).coherentIntensity)
    }

    @Test fun coherentCurveUsesOneScaleHasSymmetryAndStaysNonnegative() {
        for (step in 0..100) {
            val phase = step / 100.0
            val unit = EqualIntensityInterferenceProbe(phase)
            val scaled = unit.copy(singleContribution = 7.0)
            assertEquals(2.0, unit.independentSum)
            assertTrue(unit.coherentIntensity in 0.0..4.0)
            assertEquals(7.0 * unit.coherentIntensity, scaled.coherentIntensity, 1e-13)
            assertEquals(unit.coherentIntensity, unit.copy(pathDifferenceInWavelengths = 1.0 - phase).coherentIntensity, 1e-13)
        }
    }

    @Test fun unboundedOrNonfiniteModelInputsAreRejected() {
        for (invalid in listOf(Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)) {
            assertFailsWith<IllegalArgumentException> { ParaxialLensProbe(directionSlope = invalid) }
            assertFailsWith<IllegalArgumentException> { ParaxialLensProbe(heightOffsetOverF = invalid) }
            assertFailsWith<IllegalArgumentException> { ParaxialLensProbe(observationDistanceOverF = invalid) }
            assertFailsWith<IllegalArgumentException> { ParaxialLensProbe().heightAtDistanceOverF(LensProbeRay.FIRST, invalid) }
            assertFailsWith<IllegalArgumentException> { EqualIntensityInterferenceProbe(invalid) }
            assertFailsWith<IllegalArgumentException> { EqualIntensityInterferenceProbe(.5, invalid) }
        }
        assertFailsWith<IllegalArgumentException> { ParaxialLensProbe(directionSlope = .02) }
        assertFailsWith<IllegalArgumentException> { ParaxialLensProbe(heightOffsetOverF = .03) }
        assertFailsWith<IllegalArgumentException> { ParaxialLensProbe(observationDistanceOverF = 2.0) }
        assertFailsWith<IllegalArgumentException> { ParaxialLensProbe().heightAtDistanceOverF(LensProbeRay.FIRST, -.1) }
        assertFailsWith<IllegalArgumentException> { ParaxialLensProbe().incomingHeightOverF(LensProbeRay.FIRST, 2.1) }
        assertFailsWith<IllegalArgumentException> { EqualIntensityInterferenceProbe(-.1) }
        assertFailsWith<IllegalArgumentException> { EqualIntensityInterferenceProbe(1.1) }
        assertFailsWith<IllegalArgumentException> { EqualIntensityInterferenceProbe(.5, 0.0) }
        assertFailsWith<IllegalArgumentException> { EqualIntensityInterferenceProbe(.5, -1.0) }
        assertFailsWith<IllegalArgumentException> { EqualIntensityInterferenceProbe(.5, Double.MAX_VALUE) }
    }
}
