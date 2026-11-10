package materiewelle.core

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class SlitDetectionTest {
    private val model = FiniteSlitModel(Voltage(150.0), Length(10.0), Length(2e-6), Length(.5e-6),
        WavelengthApproximation.NONRELATIVISTIC)

    @Test fun distributionIsNormalizedSymmetricAndResolvedAcrossTheCourseRange() {
        for (spacing in listOf(1.0, 5.0)) for (width in listOf(.1, .9)) {
            val variant = model.copy(slitSpacing = Length(spacing * 1e-6), slitWidth = Length(width * 1e-6))
            SlitConfiguration.entries.forEach { configuration ->
                val distribution = SlitDetectionDistribution.create(variant, configuration)
                assertEquals(1.0, distribution.binProbabilities.sum(), 1e-12)
                assertTrue(distribution.integrationIntervals >= 4096)
                assertTrue(distribution.binProbabilities.all { it.isFinite() && it >= 0.0 })
                for (bin in distribution.binProbabilities.indices) {
                    assertEquals(distribution.binProbabilities[bin], distribution.binProbabilities[distribution.binCount - 1 - bin], 1e-12)
                }
            }
        }
    }

    @Test fun conditionalProbabilityDoesNotPreserveAbsoluteRateFactors() {
        val left = SlitDetectionDistribution.create(model, SlitConfiguration.LEFT_ONLY)
        val right = SlitDetectionDistribution.create(model, SlitConfiguration.RIGHT_ONLY)
        val incoherent = SlitDetectionDistribution.create(model, SlitConfiguration.BOTH_INCOHERENT)
        val coherent = SlitDetectionDistribution.create(model)
        assertEquals(left.binProbabilities, right.binProbabilities)
        assertEquals(left.binProbabilities, incoherent.binProbabilities)
        assertTrue(left.binProbabilities.zip(coherent.binProbabilities).any { abs(it.first - it.second) > .001 })
    }

    @Test fun integrationAgreesWithAnIndependentDenseSimpsonIntegral() {
        val distribution = SlitDetectionDistribution.create(model, binCount = 100)
        fun simpson(low: Double, high: Double, n: Int): Double {
            val step = (high - low) / n
            var sum = model.relativeIntensity(low) + model.relativeIntensity(high)
            for (index in 1 until n) sum += (if (index % 2 == 0) 2 else 4) * model.relativeIntensity(low + index * step)
            return sum * step / 3.0
        }
        val total = simpson(distribution.lower.meters, distribution.upper.meters, 40_000)
        for (bin in listOf(0, 10, 25, 49, 50, 64, 99)) {
            val low = distribution.lower.meters + bin * distribution.binWidthM
            val expected = simpson(low, low + distribution.binWidthM, 400) / total
            assertEquals(expected, distribution.binProbabilities[bin], 5e-7)
        }
    }

    @Test fun inverseDistributionSelectsTheDeclaredBinsWithoutUsingPeakIntensityAsProbability() {
        val distribution = SlitDetectionDistribution.create(model)
        var prefix = 0.0
        distribution.binProbabilities.forEachIndexed { bin, probability ->
            val sample = distribution.sample(prefix + probability * .5)
            assertEquals(bin, distribution.binIndex(sample))
            prefix += probability
        }
        assertTrue(distribution.sample(0.0).meters >= distribution.lower.meters)
        assertTrue(distribution.sample(.999999999999).meters <= distribution.upper.meters)
        assertEquals(distribution.binCount - 1, distribution.binIndex(distribution.upper))
    }

    @Test fun seedReplayAndBatchPartitioningProduceIdenticalEvents() {
        val distribution = SlitDetectionDistribution.create(model)
        val combined = SlitDetectionRun.start(distribution, 1).append(100)
        val partitioned = SlitDetectionRun.start(distribution, 1).append(10).append(90)
        assertEquals(combined.hits, partitioned.hits)
        assertEquals(combined.counts, partitioned.counts)
        assertEquals(distribution.sample(16807.0 / 2147483647.0), combined.hits[0])
        assertEquals(distribution.sample(282475249.0 / 2147483647.0), combined.hits[1])
        assertEquals(distribution.sample(1622650073.0 / 2147483647.0), combined.hits[2])
        assertNotEquals(combined.hits, SlitDetectionRun.start(distribution, 2).append(100).hits)
        assertEquals(combined.hits, SlitDetectionRun.start(distribution, combined.seed).append(100).hits)
    }

    @Test fun histogramCountsAreActualEventCountsAndEarlierRunsRemainUnchanged() {
        val distribution = SlitDetectionDistribution.create(model)
        val before = SlitDetectionRun.start(distribution, 8123)
        val first = before.append(100)
        val second = first.append(1000)
        assertEquals(0, before.count)
        assertEquals(100, first.count)
        assertEquals(1100, second.count)
        assertEquals(first.hits, second.hits.take(100))
        assertEquals(second.count, second.counts.sum())
        val independentlyCounted = IntArray(distribution.binCount)
        second.hits.forEach { independentlyCounted[distribution.binIndex(it)]++ }
        assertEquals(independentlyCounted.toList(), second.counts)
    }

    @Test fun runBoundsStopUnboundedWorkWithoutMixingConfigurations() {
        val distribution = SlitDetectionDistribution.create(model)
        var run = SlitDetectionRun.start(distribution, 77).append(1)
        repeat(10) { run = run.append(1000) }
        assertEquals(SlitDetectionRun.MAX_HITS, run.count)
        assertEquals(run.count, run.counts.sum())
        assertTrue(run.isFull)
        assertSame(run, run.append(1))
        val other = SlitDetectionDistribution.create(model, SlitConfiguration.LEFT_ONLY)
        val reset = SlitDetectionRun.start(other, run.seed)
        assertEquals(0, reset.count)
        assertSame(other, reset.distribution)
        assertSame(distribution, run.distribution)
    }

    @Test fun invalidSeedsBinsCoordinatesAndRequestsAreRejected() {
        val distribution = SlitDetectionDistribution.create(model)
        for (seed in listOf(0, -1, Int.MAX_VALUE)) {
            assertFailsWith<IllegalArgumentException> { SlitDetectionRun.start(distribution, seed) }
        }
        val run = SlitDetectionRun.start(distribution, 42)
        for (amount in listOf(0, -1, SlitDetectionRun.MAX_BATCH + 1)) {
            assertFailsWith<IllegalArgumentException> { run.append(amount) }
        }
        for (u in listOf(-.1, 1.0, Double.NaN, Double.POSITIVE_INFINITY)) {
            assertFailsWith<IllegalArgumentException> { distribution.sample(u) }
        }
        assertFailsWith<IllegalArgumentException> { distribution.binIndex(ObservationPosition(.004)) }
        assertFailsWith<IllegalArgumentException> { SlitDetectionDistribution.create(model, binCount = 0) }
        assertFailsWith<IllegalArgumentException> { SlitDetectionDistribution.create(model, binCount = 201) }
    }

    @Test fun unresolvedHighFrequencyModelsFailInsteadOfProducingAliasedPatterns() {
        val tooFine = FiniteSlitModel(Voltage(50_000.0), Length(.35), Length(2e-6), Length(.5e-6))
        assertFailsWith<IllegalArgumentException> { SlitDetectionDistribution.create(tooFine) }
    }
}
