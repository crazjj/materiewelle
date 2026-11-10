package materiewelle.core

import kotlinx.serialization.Serializable

/** SI value types: conversion to display units happens at the presentation boundary. */
data class Voltage(val volts: Double) {
    init { require(volts.isFinite() && volts > 0.0) { "Spannung muss positiv und endlich sein." } }
}

data class Length(val meters: Double) {
    init { require(meters.isFinite() && meters >= 0.0) { "Länge muss nichtnegativ und endlich sein." } }
    val picometers: Double get() = meters * 1e12
    val nanometers: Double get() = meters * 1e9
    val micrometers: Double get() = meters * 1e6
}

/** Degrees, explicitly distinct from radians. The diffraction model checks its own angular domain. */
data class Angle(val degrees: Double) {
    init { require(degrees.isFinite()) { "Winkel muss endlich sein." } }
}

/** Coordinate in the original image raster, independent of screen size and zoom. */
data class NativePixel(val value: Double) {
    init { require(value.isFinite() && value >= 0.0) { "Native Pixelkoordinate muss nichtnegativ und endlich sein." } }
}

data class PixelDistance(val value: Double) {
    init { require(value.isFinite() && value >= 0.0) { "Pixelabstand muss nichtnegativ und endlich sein." } }
}

@Serializable
enum class Experiment(val route: String) {
    DE_BROGLIE("debroglie"), DAVISSON_GERMER("davisson-germer"), JONSSON("joensson");

    companion object {
        fun fromRoute(route: String): Experiment? = when (val normalized = route.removePrefix("#").trim('/')) {
            "dg" -> DAVISSON_GERMER
            "jonsson" -> JONSSON
            else -> entries.firstOrNull { it.route == normalized }
        }
    }
}

@Serializable
enum class ExperimentMode { LAB, GUIDED }

/** Safe load diagnostics: no response bodies, exception text or personal work. */
sealed interface LoadFailure {
    data object Transport : LoadFailure
    data class Http(val status: Int) : LoadFailure
    data object Decode : LoadFailure
    data object Validation : LoadFailure
    data object Internal : LoadFailure
}

/** Loading errors are data, not empty successful records. */
sealed interface LoadState<out T> {
    data object Loading : LoadState<Nothing>
    data class Ready<T>(val value: T) : LoadState<T>
    data class Failed(val message: String, val reason: LoadFailure? = null) : LoadState<Nothing>
}

/** Raw editing text, accepted value and last drawable value remain separate. */
data class VoltageInput(val raw: String, val valid: Voltage?, val lastValid: Voltage) {
    fun edit(text: String): VoltageInput {
        val next = Physics.inputVoltage(text)?.let(::Voltage)
        return VoltageInput(text, next, next ?: lastValid)
    }

    companion object {
        fun initial(volts: Double = 150.0): VoltageInput {
            require(volts in Physics.MIN_VOLTS..Physics.MAX_VOLTS)
            val value = Voltage(volts)
            return VoltageInput(if (volts % 1.0 == 0.0) volts.toInt().toString() else volts.toString(), value, value)
        }
    }
}
