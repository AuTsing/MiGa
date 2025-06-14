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
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.float
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull

@Serializable
data class GetDevicePropertiesResponse(
    val code: Int,
    val message: String,
    val result: List<Result>,
) {

    @OptIn(ExperimentalSerializationApi::class)
    @Serializable
    @JsonIgnoreUnknownKeys
    data class Result(
        val did: String,
        val iid: String,
        val siid: Int,
        val piid: Int,
        val value: Value = Value.None,
        val code: Int,
    ) {

        @Serializable(with = Value.Serializer::class)
        sealed class Value {

            data class Boolean(val value: kotlin.Boolean) : Value()
            data class Int(val value: kotlin.Int) : Value()
            data class Float(val value: kotlin.Float) : Value()
            object None : Value()

            object Serializer : KSerializer<Value> {

                @OptIn(ExperimentalSerializationApi::class, InternalSerializationApi::class)
                override val descriptor: SerialDescriptor = buildSerialDescriptor(
                    "Value",
                    PolymorphicKind.SEALED,
                )

                override fun serialize(encoder: Encoder, value: Value) {
                    require(encoder is JsonEncoder)

                    when (value) {
                        is Int -> encoder.encodeInt(value.value)
                        is Float -> encoder.encodeFloat(value.value)
                        is Boolean -> encoder.encodeBoolean(value.value)
                        None -> encoder.encodeString("none")
                    }
                }

                override fun deserialize(decoder: Decoder): Value {
                    require(decoder is JsonDecoder)

                    val jsonElement = decoder.decodeJsonElement()
                    return if (jsonElement is JsonPrimitive) {
                        when {
                            jsonElement.intOrNull != null -> Int(jsonElement.int)
                            jsonElement.floatOrNull != null -> Float(jsonElement.float)
                            jsonElement.booleanOrNull != null -> Boolean(jsonElement.boolean)
                            else -> None
                        }
                    } else {
                        None
                    }
                }
            }
        }
    }
}
