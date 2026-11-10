package materiewelle.core

import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ClassicalSuperpositionTest {
    @Test fun integerAdditionalPathsReinforceAtEveryCommonPlaceAndTime() {
        for (additionalPath in listOf(-2.0, 0.0, 1.0, 3.0)) {
            val model = ClassicalSuperposition(38.0, 110.0, additionalPath)
            assertEquals(76.0, model.resultantAmplitude, 1e-12)
            for (x in listOf(0.0, 19.0, 110.0, 413.0)) for (time in listOf(0.0, .125, .8)) {
                val value = model.sample(x, time)
                assertEquals(value.first, value.second, 1e-11)
                assertEquals(2.0 * value.first, value.sum, 1e-11)
            }
        }
    }

    @Test fun halfWavelengthCancelsAcrossTheWholeCommonCycle() {
        val model = ClassicalSuperposition(45.0, 60.0, .5)
        assertEquals(0.0, model.resultantAmplitude, 1e-12)
        for (index in 0..100) {
            val value = model.sample(index * 6.0, index / 37.0)
            assertEquals(-value.first, value.second, 1e-11)
            assertEquals(0.0, value.sum, 1e-11)
        }
    }

    @Test fun quarterWavelengthHasIntermediateAmplitudeAndSamePeriod() {
        val model = ClassicalSuperposition(10.0, 180.0, .25)
        assertEquals(10.0 * sqrt(2.0), model.resultantAmplitude, 1e-12)
        val sample = model.sample(45.0, 0.0)
        assertEquals(10.0, sample.first, 1e-12)
        assertEquals(0.0, sample.second, 1e-12)
        assertEquals(sample.sum, model.sample(225.0, 1.0).sum, 1e-12)
        for (i in 0..100) assertTrue(kotlin.math.abs(model.sample(i * 1.8, 0.0).sum) <= model.resultantAmplitude + 1e-12)
    }

    @Test fun extraQuarterWavelengthIsATimeDelayNotATimeAdvance() {
        val model = ClassicalSuperposition(10.0, 180.0, .25)
        // u1 = sin(kx − ωt); at x=t=0 a delayed quarter-cycle is sin(+π/2).
        val origin = model.sample(0.0, 0.0)
        assertEquals(0.0, origin.first, 1e-12)
        assertEquals(10.0, origin.second, 1e-12)
        val quarterCycleLater = model.sample(0.0, .25)
        assertEquals(-10.0, quarterCycleLater.first, 1e-12)
        assertEquals(0.0, quarterCycleLater.second, 1e-12)
        val halfCycleLater = model.sample(0.0, .5)
        assertEquals(0.0, halfCycleLater.first, 1e-12)
        assertEquals(-10.0, halfCycleLater.second, 1e-12)
        // Independent physical relation: the second signal equals the first at earlier time.
        for (position in listOf(0.0, 13.0, 127.0)) for (time in listOf(0.0, .125, .6)) {
            assertEquals(model.sample(position, time - .25).first, model.sample(position, time).second, 1e-11)
        }
    }

    @Test fun changingAmplitudeDoesNotMoveCrestsOrChangeThePeriod() {
        val low = ClassicalSuperposition(10.0, 110.0, .25)
        val high = low.copy(amplitude = 45.0)
        for (i in 0..100) {
            val x = i * 4.0
            assertEquals(low.sample(x, .2).first * 4.5, high.sample(x, .2).first, 1e-12)
            assertEquals(low.sample(x, .2).sum * 4.5, high.sample(x, .2).sum, 1e-11)
            assertEquals(low.sample(x, .2).sum, low.sample(x + 110.0, .2).sum, 1e-11)
        }
    }

    @Test fun finiteExtremeCycleCountsDoNotOverflowTheTrigonometricArguments() {
        val model = ClassicalSuperposition(1.0, Double.MIN_VALUE, Double.MAX_VALUE)
        val value = model.sample(Double.MAX_VALUE, Double.MAX_VALUE)
        assertTrue(value.first.isFinite() && value.second.isFinite() && value.sum.isFinite())
        assertEquals(2.0, model.resultantAmplitude, 1e-12)
        assertFailsWith<IllegalArgumentException> { model.copy(amplitude = Double.MAX_VALUE) }
    }

    @Test fun rejectsNonfiniteAndInvalidDiagramParameters() {
        assertFailsWith<IllegalArgumentException> { ClassicalSuperposition(Double.NaN, 1.0, 0.0) }
        assertFailsWith<IllegalArgumentException> { ClassicalSuperposition(-1.0, 1.0, 0.0) }
        assertFailsWith<IllegalArgumentException> { ClassicalSuperposition(1.0, 0.0, 0.0) }
        assertFailsWith<IllegalArgumentException> { ClassicalSuperposition(1.0, Double.POSITIVE_INFINITY, 0.0) }
        assertFailsWith<IllegalArgumentException> { ClassicalSuperposition(1.0, 1.0, Double.NaN) }
        assertFailsWith<IllegalArgumentException> { ClassicalSuperposition(1.0, 1.0, 0.0).sample(Double.NaN, 0.0) }
        assertFailsWith<IllegalArgumentException> { ClassicalSuperposition(1.0, 1.0, 0.0).sample(1.0, Double.POSITIVE_INFINITY) }
        assertEquals(0.0, ClassicalSuperposition(0.0, 1.0, .25).sample(1.0, .3).sum, 1e-12)
    }
}
