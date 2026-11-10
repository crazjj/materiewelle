package materiewelle.core

/** All operations use the unchanged raster. Display zoom is intentionally absent from this API. */
object PixelMeasurement {
    fun nativeIntervals(markers: List<NativePixel>): List<PixelDistance> {
        require(markers.size >= 2)
        return markers.zipWithNext { a, b ->
            require(b.value > a.value) { "Messmarken müssen streng geordnet sein." }
            PixelDistance(b.value - a.value)
        }
    }

    fun nativeMean(markers: List<NativePixel>): PixelDistance = PixelDistance(nativeIntervals(markers).map { it.value }.average())
    fun intervals(markers: List<Double>): List<Double> = nativeIntervals(markers.map(::NativePixel)).map { it.value }
    fun mean(markers: List<Double>): Double = nativeMean(markers.map(::NativePixel)).value

    fun canMove(markers: List<Double>, index: Int, delta: Double, left: Double, right: Double): Boolean {
        if (index !in markers.indices || !delta.isFinite() || !left.isFinite() || !right.isFinite() || left < 0 || left >= right) return false
        if (markers.any { !it.isFinite() } || markers.zipWithNext().any { (a, b) -> a >= b }) return false
        val next = markers[index] + delta
        return next >= left && next < right &&
            (index == 0 || next > markers[index - 1]) &&
            (index == markers.lastIndex || next < markers[index + 1])
    }

    fun move(markers: List<Double>, index: Int, delta: Double, left: Double, right: Double): List<Double> {
        require(canMove(markers, index, delta, left, right)) { "Marken müssen geordnet im Ausschnitt bleiben." }
        return markers.mapIndexed { i, x -> if (i == index) x + delta else x }
    }
}
