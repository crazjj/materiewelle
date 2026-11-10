package materiewelle.core

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

/**
 * Probability conditional on a detection inside the displayed synthetic window. Trapezoidal
 * cell integrals are normalized by their sum, then grouped into histogram bins. Sampling uses
 * the corresponding piecewise-constant cell-average density, not the peak-normalized intensity.
 * No incident particle count, transmission fraction, efficiency, or two-dimensional model is inferred.
 */
class SlitDetectionDistribution private constructor(
    val model: FiniteSlitModel,
    val configuration: SlitConfiguration,
    val binCount: Int,
    val integrationIntervals: Int,
    val binProbabilities: List<Double>,
    private val cumulative: DoubleArray,
) {
    val lower = ObservationPosition(-WINDOW_HALF_WIDTH_M)
    val upper = ObservationPosition(WINDOW_HALF_WIDTH_M)
    val binWidthM: Double get() = (upper.meters - lower.meters) / binCount

    fun binIndex(position: ObservationPosition): Int {
        require(position.meters in lower.meters..upper.meters) { "Treffer liegt außerhalb des Modellfensters." }
        return ((position.meters - lower.meters) / binWidthM).toInt().coerceIn(0, binCount - 1)
    }

    fun sample(unitValue: Double): ObservationPosition {
        require(unitValue.isFinite() && unitValue >= 0.0 && unitValue < 1.0)
        // First cumulative boundary strictly greater than u; zero-area cells are skipped.
        var low = 1
        var high = cumulative.lastIndex
        while (low < high) {
            val middle = (low + high) ushr 1
            if (cumulative[middle] > unitValue) high = middle else low = middle + 1
        }
        val cell = low - 1
        val mass = cumulative[cell + 1] - cumulative[cell]
        require(mass > 0.0)
        val fraction = ((unitValue - cumulative[cell]) / mass).coerceIn(0.0, 1.0)
        val x = lower.meters + (cell + fraction) * ((upper.meters - lower.meters) / integrationIntervals)
        return ObservationPosition(x.coerceIn(lower.meters, upper.meters))
    }

    companion object {
        const val WINDOW_HALF_WIDTH_M = 0.003
        const val DEFAULT_BIN_COUNT = 60
        private const val MIN_INTERVALS = 8192
        private const val MAX_INTERVALS = 131072
        private const val INTERVALS_PER_OSCILLATION = 64

        fun create(
            model: FiniteSlitModel,
            configuration: SlitConfiguration = SlitConfiguration.BOTH_COHERENT,
            binCount: Int = DEFAULT_BIN_COUNT,
        ): SlitDetectionDistribution {
            require(binCount in 10..200) { "Die Histogrammauflösung muss zwischen 10 und 200 liegen." }
            val spanM = 2.0 * WINDOW_HALF_WIDTH_M
            // Maximum local phase slope occurs at x=0. This bound also rejects grids that
            // would undersample a very narrow central structure at small L; it does not make
            // the underlying Fraunhofer model a valid near-field propagation model.
            val cycles = spanM * (max(model.slitSpacing.meters, model.slitWidth.meters) / model.wavelength.meters) / model.distance.meters
            val wanted = max(MIN_INTERVALS.toDouble(), ceil(cycles * INTERVALS_PER_OSCILLATION))
            require(wanted.isFinite() && wanted <= MAX_INTERVALS) { "Dieser Modellfall benötigt ein feineres Detektorgitter." }
            val cellsPerBin = ceil(wanted / binCount).toInt()
            val intervals = cellsPerBin * binCount
            require(intervals <= MAX_INTERVALS) { "Dieser Modellfall benötigt ein feineres Detektorgitter." }
            val dx = spanM / intervals
            val masses = DoubleArray(intervals)
            var previous = model.relativeIntensity(ObservationPosition(-WINDOW_HALF_WIDTH_M), configuration)
            var total = 0.0
            for (cell in 0 until intervals) {
                val x = -WINDOW_HALF_WIDTH_M + (cell + 1) * dx
                val next = model.relativeIntensity(ObservationPosition(x), configuration)
                val area = (previous + next) * 0.5 * dx
                require(area.isFinite() && area >= 0.0)
                masses[cell] = area
                total += area
                previous = next
            }
            require(total.isFinite() && total > 0.0) { "Die Verteilung besitzt keine endliche positive Fläche." }
            val cumulative = DoubleArray(intervals + 1)
            for (cell in masses.indices) cumulative[cell + 1] = cumulative[cell] + masses[cell]
            for (index in cumulative.indices) cumulative[index] /= total
            require(abs(cumulative.last() - 1.0) < 1e-9)
            cumulative[cumulative.lastIndex] = 1.0
            val probabilities = List(binCount) { bin ->
                cumulative[(bin + 1) * cellsPerBin] - cumulative[bin * cellsPerBin]
            }
            return SlitDetectionDistribution(model, configuration, binCount, intervals, probabilities, cumulative)
        }
    }
}

/** A bounded synthetic run. Keeping the initial seed allows an explicitly requested exact replay. */
class SlitDetectionRun private constructor(
    val distribution: SlitDetectionDistribution,
    val seed: Int,
    private val randomState: Int,
    val hits: List<ObservationPosition>,
    val counts: List<Int>,
) {
    val count: Int get() = hits.size
    val isFull: Boolean get() = count >= MAX_HITS

    fun append(count: Int): SlitDetectionRun {
        require(count in 1..MAX_BATCH)
        if (isFull) return this
        val amount = min(count, MAX_HITS - this.count)
        val nextHits = hits.toMutableList()
        val nextCounts = counts.toMutableList()
        var state = randomState
        repeat(amount) {
            // Park-Miller integer arithmetic; Long product remains exact on both JVM and Kotlin/JS.
            state = ((state.toLong() * 16807L) % 2147483647L).toInt()
            val point = distribution.sample(state.toDouble() / 2147483647.0)
            nextHits.add(point)
            val bin = distribution.binIndex(point)
            nextCounts[bin] = nextCounts[bin] + 1
        }
        return SlitDetectionRun(distribution, seed, state, nextHits.toList(), nextCounts.toList())
    }

    companion object {
        const val MAX_HITS = 10_000
        const val MAX_BATCH = 1_000

        fun start(distribution: SlitDetectionDistribution, seed: Int): SlitDetectionRun {
            require(seed in 1 until Int.MAX_VALUE) { "Der Modell-Seed muss zwischen 1 und 2147483646 liegen." }
            return SlitDetectionRun(distribution, seed, seed, emptyList(), List(distribution.binCount) { 0 })
        }
    }
}
