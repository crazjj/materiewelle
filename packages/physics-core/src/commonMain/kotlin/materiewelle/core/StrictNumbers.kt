package materiewelle.core

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull

// kotlinx.serialization accepts quoted numerals by default. The established data specification
// requires actual JSON numbers, so reject coercion at the import boundary on every KMP target.
internal typealias JsonNumber = @Serializable(with = StrictNumberSerializer::class) Double
internal typealias JsonInteger = @Serializable(with = StrictIntegerSerializer::class) Int

private fun strictNumber(decoder: Decoder): Double {
    if (decoder !is JsonDecoder) throw SerializationException("Wissenschaftliche Daten benötigen JSON.")
    val value = decoder.decodeJsonElement()
    if (value !is JsonPrimitive || value.isString) throw SerializationException("Numerisches JSON-Feld erwartet; Text wird nicht umgewandelt.")
    return value.doubleOrNull?.takeIf { it.isFinite() }
        ?: throw SerializationException("Endliche JSON-Zahl erwartet.")
}

internal object StrictNumberSerializer : KSerializer<Double> {
    override val descriptor = PrimitiveSerialDescriptor("StrictJsonNumber", PrimitiveKind.DOUBLE)
    override fun deserialize(decoder: Decoder): Double = strictNumber(decoder)
    override fun serialize(encoder: Encoder, value: Double) = encoder.encodeDouble(value)
}

internal object StrictIntegerSerializer : KSerializer<Int> {
    override val descriptor = PrimitiveSerialDescriptor("StrictJsonInteger", PrimitiveKind.INT)
    override fun deserialize(decoder: Decoder): Int {
        val value = strictNumber(decoder)
        if (value < Int.MIN_VALUE || value > Int.MAX_VALUE || value != value.toInt().toDouble())
            throw SerializationException("Ganze JSON-Zahl im gültigen Wertebereich erwartet.")
        return value.toInt()
    }
    override fun serialize(encoder: Encoder, value: Int) = encoder.encodeInt(value)
}
