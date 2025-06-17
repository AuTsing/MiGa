package com.autsing.miga.presentation.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.float
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull

@Serializable(with = DevicePropertyValue.Serializer::class)
sealed class DevicePropertyValue {

    data class Boolean(val value: kotlin.Boolean) : DevicePropertyValue()
    data class Int(val value: kotlin.Int) : DevicePropertyValue()
    data class Float(val value: kotlin.Float) : DevicePropertyValue()
    data class String(val value: kotlin.String) : DevicePropertyValue()
    object None : DevicePropertyValue()

    object Serializer : KSerializer<DevicePropertyValue> {

        @OptIn(ExperimentalSerializationApi::class, InternalSerializationApi::class)
        override val descriptor: SerialDescriptor = buildSerialDescriptor(
            "Value",
            PolymorphicKind.SEALED,
        )

        @OptIn(ExperimentalSerializationApi::class)
        override fun serialize(encoder: Encoder, value: DevicePropertyValue) {
            require(encoder is JsonEncoder)

            when (value) {
                is Int -> encoder.encodeInt(value.value)
                is Float -> encoder.encodeFloat(value.value)
                is Boolean -> encoder.encodeBoolean(value.value)
                is String -> encoder.encodeString(value.value)
                is None -> encoder.encodeNull()
            }
        }

        override fun deserialize(decoder: Decoder): DevicePropertyValue {
            require(decoder is JsonDecoder)

            val jsonElement = decoder.decodeJsonElement()
            return if (jsonElement is JsonPrimitive) {
                when {
                    jsonElement.intOrNull != null -> Int(jsonElement.int)
                    jsonElement.floatOrNull != null -> Float(jsonElement.float)
                    jsonElement.booleanOrNull != null -> Boolean(jsonElement.boolean)
                    jsonElement.contentOrNull != null -> String(jsonElement.content)
                    else -> None
                }
            } else {
                None
            }
        }
    }
}
