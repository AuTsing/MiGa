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
                is GetDeviceInfoResponse.Props.Spec.Service.Property.Ranges.Uint8 -> Long(
                    from = range.values[0].toLong(),
                    to = range.values[1].toLong(),
                    step = range.values[2].toLong(),
                )

                is GetDeviceInfoResponse.Props.Spec.Service.Property.Ranges.Uint16 -> Long(
                    from = range.values[0].toLong(),
                    to = range.values[1].toLong(),
                    step = range.values[2].toLong(),
                )

                is GetDeviceInfoResponse.Props.Spec.Service.Property.Ranges.Uint32 -> Long(
                    from = range.values[0].toLong(),
                    to = range.values[1].toLong(),
                    step = range.values[2].toLong(),
                )

                is GetDeviceInfoResponse.Props.Spec.Service.Property.Ranges.Int8 -> Long(
                    from = range.values[0].toLong(),
                    to = range.values[1].toLong(),
                    step = range.values[2].toLong(),
                )

                is GetDeviceInfoResponse.Props.Spec.Service.Property.Ranges.Int16 -> Long(
                    from = range.values[0].toLong(),
                    to = range.values[1].toLong(),
                    step = range.values[2].toLong(),
                )

                is GetDeviceInfoResponse.Props.Spec.Service.Property.Ranges.Int32 -> Long(
                    from = range.values[0].toLong(),
                    to = range.values[1].toLong(),
                    step = range.values[2].toLong(),
                )

                is GetDeviceInfoResponse.Props.Spec.Service.Property.Ranges.Float -> Double(
                    from = range.values[0].toDouble(),
                    to = range.values[1].toDouble(),
                    step = range.values[2].toDouble(),
                )

                else -> None()
            }
        }
    }

    fun getValueOfPercentage(percentage: Float): DevicePropertyValue {
        return when (this) {

            is Long -> {
                val length = to - from
                val point = (length * percentage).toLong()
                val at = point + from
                DevicePropertyValue.Long(at)
            }

            is Double -> {
                val length = to - from
                val point = (length * percentage).toDouble()
                val at = point + from
                DevicePropertyValue.Double(at)
            }

            is None -> DevicePropertyValue.None
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Serializable
    data class Long(
        @EncodeDefault(EncodeDefault.Mode.ALWAYS)
        val type: String = "long",
        val from: kotlin.Long,
        val to: kotlin.Long,
        val step: kotlin.Long,
    ) : DevicePropertyRange()

    @OptIn(ExperimentalSerializationApi::class)
    @Serializable
    data class Double(
        @EncodeDefault(EncodeDefault.Mode.ALWAYS)
        val type: String = "double",
        val from: kotlin.Double,
        val to: kotlin.Double,
        val step: kotlin.Double,
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
                "long" -> Long.serializer()
                "double" -> Double.serializer()
                else -> None.serializer()
            }
        }
    }
}
