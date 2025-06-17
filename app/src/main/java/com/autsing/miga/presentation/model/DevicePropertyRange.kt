package com.autsing.miga.presentation.model

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable(with = DevicePropertyRange.Serializer::class)
sealed class DevicePropertyRange {

    companion object {
        fun from(range: GetDeviceInfoResponse.Props.Spec.Service.Property.Ranges?): DevicePropertyRange {
            return when (range) {
                is GetDeviceInfoResponse.Props.Spec.Service.Property.Ranges.Uint8 -> Int(
                    from = range.values[0].toInt(),
                    to = range.values[1].toInt(),
                    step = range.values[2].toInt(),
                )

                is GetDeviceInfoResponse.Props.Spec.Service.Property.Ranges.Uint16 -> Int(
                    from = range.values[0].toInt(),
                    to = range.values[1].toInt(),
                    step = range.values[2].toInt(),
                )

                is GetDeviceInfoResponse.Props.Spec.Service.Property.Ranges.Uint32 -> Int(
                    from = if (range.values[0] < kotlin.Int.MAX_VALUE.toUInt()) range.values[0].toInt() else kotlin.Int.MAX_VALUE,
                    to = if (range.values[1] < kotlin.Int.MAX_VALUE.toUInt()) range.values[1].toInt() else kotlin.Int.MAX_VALUE,
                    step = if (range.values[2] < kotlin.Int.MAX_VALUE.toUInt()) range.values[2].toInt() else kotlin.Int.MAX_VALUE,
                )

                is GetDeviceInfoResponse.Props.Spec.Service.Property.Ranges.Int8 -> Int(
                    from = range.values[0].toInt(),
                    to = range.values[1].toInt(),
                    step = range.values[2].toInt(),
                )

                is GetDeviceInfoResponse.Props.Spec.Service.Property.Ranges.Int16 -> Int(
                    from = range.values[0].toInt(),
                    to = range.values[1].toInt(),
                    step = range.values[2].toInt(),
                )

                is GetDeviceInfoResponse.Props.Spec.Service.Property.Ranges.Int32 -> Int(
                    from = range.values[0],
                    to = range.values[1],
                    step = range.values[2],
                )

                is GetDeviceInfoResponse.Props.Spec.Service.Property.Ranges.Float -> Float(
                    from = range.values[0],
                    to = range.values[1],
                    step = range.values[2],
                )

                else -> None()
            }
        }
    }

    fun getValueOfPercentage(percentage: kotlin.Float): DevicePropertyValue {
        return when (this) {

            is Int -> {
                val length = to - from
                val point = (length * percentage).toInt()
                val at = point + from
                DevicePropertyValue.Int(at)
            }

            is Float -> {
                val length = to - from
                val point = length * percentage
                val at = point + from
                DevicePropertyValue.Float(at)
            }

            is None -> DevicePropertyValue.None
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Serializable
    data class Int(
        @EncodeDefault(EncodeDefault.Mode.ALWAYS)
        val type: String = "int",
        val from: kotlin.Int,
        val to: kotlin.Int,
        val step: kotlin.Int,
    ) : DevicePropertyRange()

    @OptIn(ExperimentalSerializationApi::class)
    @Serializable
    data class Float(
        @EncodeDefault(EncodeDefault.Mode.ALWAYS)
        val type: String = "float",
        val from: kotlin.Float,
        val to: kotlin.Float,
        val step: kotlin.Float,
    ) : DevicePropertyRange()

    @OptIn(ExperimentalSerializationApi::class)
    @Serializable
    data class None(
        @EncodeDefault(EncodeDefault.Mode.ALWAYS)
        val type: String = "none"
    ) : DevicePropertyRange()

    object Serializer :
        JsonContentPolymorphicSerializer<DevicePropertyRange>(DevicePropertyRange::class) {
        override fun selectDeserializer(element: JsonElement): DeserializationStrategy<DevicePropertyRange> {
            val type = element.jsonObject["type"]?.jsonPrimitive?.content
                ?: throw SerializationException("Missing `type` field")
            return when (type) {
                "int" -> Int.serializer()
                "float" -> Float.serializer()
                else -> None.serializer()
            }
        }
    }
}
