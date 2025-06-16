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

@Serializable
data class DeviceInfo(
    val name: String,
    val model: String,
    val properties: List<Property>,
    val actions: List<Action>,
) {

    @Serializable
    data class Property(
        val name: String,
        val description: String,
        val descZhCn: String,
        val type: String,
        val access: Access,
        val unit: String,
        val range: Range,
        val values: List<GetDeviceInfoResponse.Props.Spec.Service.Property.Value>,
        val method: Method,
    ) {

        @Serializable
        data class Access(
            val read: Boolean,
            val write: Boolean,
            val notify: Boolean,
        )

        @Serializable(with = Range.Serializer::class)
        sealed class Range {

            fun getValueOfPercentage(percentage: kotlin.Float): DevicePropertyValue {
                return when (this) {

                    is Uint32 -> {
                        val length = (to - from).toInt()
                        val point = (length * percentage).toInt()
                        val at = point + from.toInt()
                        DevicePropertyValue.Int(at)
                    }

                    is Int32 -> {
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
            data class Uint32(
                @EncodeDefault(EncodeDefault.Mode.ALWAYS)
                val type: String = "uint32",
                val from: UInt,
                val to: UInt,
                val step: UInt,
            ) : Range()

            @OptIn(ExperimentalSerializationApi::class)
            @Serializable
            data class Int32(
                @EncodeDefault(EncodeDefault.Mode.ALWAYS)
                val type: String = "int32",
                val from: Int,
                val to: Int,
                val step: Int,
            ) : Range()

            @OptIn(ExperimentalSerializationApi::class)
            @Serializable
            data class Float(
                @EncodeDefault(EncodeDefault.Mode.ALWAYS)
                val type: String = "float",
                val from: kotlin.Float,
                val to: kotlin.Float,
                val step: kotlin.Float,
            ) : Range()

            @OptIn(ExperimentalSerializationApi::class)
            @Serializable
            data class None(
                @EncodeDefault(EncodeDefault.Mode.ALWAYS)
                val type: String = "none"
            ) : Range()

            object Serializer : JsonContentPolymorphicSerializer<Range>(Range::class) {
                override fun selectDeserializer(element: JsonElement): DeserializationStrategy<Range> {
                    val type = element.jsonObject["type"]?.jsonPrimitive?.content
                        ?: throw SerializationException("Missing `type` field")
                    return when (type) {
                        "uint32" -> Uint32.serializer()
                        "int32" -> Int32.serializer()
                        "float" -> Float.serializer()
                        else -> None.serializer()
                    }
                }
            }
        }

        @Serializable
        data class Method(
            val ssid: Int,
            val piid: Int,
        )
    }

    @Serializable
    data class Action(
        val name: String,
        val description: String,
        val descZhCn: String,
        val method: Method,
    ) {

        @Serializable
        data class Method(
            val ssid: Int,
            val aiid: Int,
        )
    }
}
