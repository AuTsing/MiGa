package com.autsing.miga.presentation.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.float
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class GetDeviceInfoResponse(
    val props: Props,
) {

    @OptIn(ExperimentalSerializationApi::class)
    @Serializable
    @JsonIgnoreUnknownKeys
    data class Props(
        val product: Product? = null,
        val spec: Spec,
    ) {

        @OptIn(ExperimentalSerializationApi::class)
        @Serializable
        @JsonIgnoreUnknownKeys
        data class Product(
            val name: String,
            val model: String,
        )

        @OptIn(ExperimentalSerializationApi::class)
        @Serializable
        @JsonIgnoreUnknownKeys
        data class Spec(
            val name: String,
            val services: Map<String, Service>,
        ) {

            @OptIn(ExperimentalSerializationApi::class)
            @Serializable
            @JsonIgnoreUnknownKeys
            data class Service(
                val iid: Int,
                val urn: String,
                val properties: Map<String, Property>? = null,
                val actions: Map<String, Action>? = null,
            ) {

                @OptIn(ExperimentalSerializationApi::class)
                @Serializable(with = Property.Serializer::class)
                @JsonIgnoreUnknownKeys
                data class Property(
                    val iid: Int,
                    val urn: String,
                    val format: String,
                    val name: String,
                    val description: String,
                    val access: List<String>,
                    val unit: String? = null,
                    @SerialName("value-range")
                    val valueRange: Ranges? = null,
                    @SerialName("value-list")
                    val valueList: Values? = null,
                    val desc_zh_cn: String? = null,
                ) {

                    object Serializer : KSerializer<Property> {

                        override val descriptor: SerialDescriptor = buildClassSerialDescriptor(
                            "Property"
                        ) {
                            element<Int>("iid")
                            element<String>("urn")
                            element<String>("format")
                            element<String>("name")
                            element<String>("description")
                            element<JsonArray>("access")
                            element<String>("unit")
                            element<JsonArray>("value-range")
                            element<JsonArray>("value-list")
                            element<String>("desc_zh_cn")
                        }

                        override fun serialize(encoder: Encoder, property: Property) {
                            require(encoder is JsonEncoder)

                            val jsonObject = buildJsonObject {
                                put("iid", JsonPrimitive(property.iid))
                                put("urn", JsonPrimitive(property.urn))
                                put("format", JsonPrimitive(property.format))
                                put("name", JsonPrimitive(property.name))
                                put("description", JsonPrimitive(property.description))
                                put(
                                    "access",
                                    JsonArray(property.access.map { JsonPrimitive(it) }),
                                )

                                property.unit?.let { put("unit", JsonPrimitive(it)) }
                                property.valueRange?.let {
                                    val array = when (it) {
                                        is Ranges.Uint8 -> it.values.map { JsonPrimitive(it) }
                                        is Ranges.Uint16 -> it.values.map { JsonPrimitive(it) }
                                        is Ranges.Uint32 -> it.values.map { JsonPrimitive(it) }
                                        is Ranges.Int8 -> it.values.map { JsonPrimitive(it) }
                                        is Ranges.Int16 -> it.values.map { JsonPrimitive(it) }
                                        is Ranges.Int32 -> it.values.map { JsonPrimitive(it) }
                                        is Ranges.Float -> it.values.map { JsonPrimitive(it) }
                                    }
                                    put("value-range", JsonArray(array))
                                }
                                property.valueList?.let {
                                    val array = when (it) {
                                        is Values.Uint8 -> it.values.map { v ->
                                            Json.encodeToJsonElement(
                                                Value.serializer(UByte.serializer()),
                                                v,
                                            )
                                        }

                                        is Values.String -> it.values.map { v ->
                                            Json.encodeToJsonElement(
                                                Value.serializer(String.serializer()),
                                                v,
                                            )
                                        }
                                    }
                                    put("value-list", JsonArray(array))
                                }
                                property.desc_zh_cn?.let { put("desc_zh_cn", JsonPrimitive(it)) }
                            }

                            encoder.encodeJsonElement(jsonObject)
                        }

                        override fun deserialize(decoder: Decoder): Property {
                            require(decoder is JsonDecoder)

                            val jsonObject = decoder.decodeJsonElement().jsonObject
                            val format = jsonObject["format"]!!.jsonPrimitive.content
                            val access = jsonObject["access"]!!.jsonArray
                                .map { it.jsonPrimitive.content }
                            val valueRangeArray = jsonObject["value-range"]?.jsonArray
                            val valueRange = valueRangeArray
                                ?.takeIf { it.isNotEmpty() }
                                ?.let { array ->
                                    when (format) {
                                        "uint8" -> Ranges.Uint8(array.map { it.jsonPrimitive.int.toUByte() })
                                        "uint16" -> Ranges.Uint16(array.map { it.jsonPrimitive.int.toUShort() })
                                        "uint32" -> Ranges.Uint32(array.map { it.jsonPrimitive.int.toUInt() })
                                        "int8" -> Ranges.Int8(array.map { it.jsonPrimitive.int.toByte() })
                                        "int16" -> Ranges.Int16(array.map { it.jsonPrimitive.int.toShort() })
                                        "int32" -> Ranges.Int32(array.map { it.jsonPrimitive.int })
                                        "float" -> Ranges.Float(array.map { it.jsonPrimitive.float })
                                        else -> null
                                    }
                                }
                            val valueList = jsonObject["value-list"]?.jsonArray
                                ?.takeIf { it.isNotEmpty() }
                                ?.let { array ->
                                    when (format) {
                                        "uint8" -> Values.Uint8(array.map {
                                            Value(
                                                value = it.jsonObject["value"]!!.jsonPrimitive.int.toUByte(),
                                                description = it.jsonObject["description"]!!.jsonPrimitive.content,
                                                desc_zh_cn = it.jsonObject["desc_zh_cn"]?.jsonPrimitive?.content,
                                            )
                                        })

                                        "string" -> Values.String(array.map {
                                            Value(
                                                value = it.jsonObject["value"]!!.jsonPrimitive.content,
                                                description = it.jsonObject["description"]!!.jsonPrimitive.content,
                                                desc_zh_cn = it.jsonObject["desc_zh_cn"]?.jsonPrimitive?.content,
                                            )
                                        })

                                        else -> null
                                    }
                                }

                            return Property(
                                iid = jsonObject["iid"]!!.jsonPrimitive.int,
                                urn = jsonObject["urn"]!!.jsonPrimitive.content,
                                format = format,
                                name = jsonObject["name"]!!.jsonPrimitive.content,
                                description = jsonObject["description"]!!.jsonPrimitive.content,
                                access = access,
                                unit = jsonObject["unit"]?.jsonPrimitive?.content,
                                valueRange = valueRange,
                                valueList = valueList,
                                desc_zh_cn = jsonObject["desc_zh_cn"]?.jsonPrimitive?.content,
                            )
                        }
                    }

                    sealed class Ranges() {
                        data class Uint8(val values: List<UByte>) : Ranges()
                        data class Uint16(val values: List<UShort>) : Ranges()
                        data class Uint32(val values: List<UInt>) : Ranges()
                        data class Int8(val values: List<Byte>) : Ranges()
                        data class Int16(val values: List<Short>) : Ranges()
                        data class Int32(val values: List<Int>) : Ranges()
                        data class Float(val values: List<kotlin.Float>) : Ranges()
                    }

                    sealed class Values() {
                        data class Uint8(val values: List<Value<UByte>>) : Values()
                        data class String(val values: List<Value<kotlin.String>>) : Values()
                    }

                    @OptIn(ExperimentalSerializationApi::class)
                    @Serializable
                    @JsonIgnoreUnknownKeys
                    data class Value<T>(
                        val value: T,
                        val description: String,
                        val desc_zh_cn: String? = null,
                    )
                }

                @OptIn(ExperimentalSerializationApi::class)
                @Serializable
                @JsonIgnoreUnknownKeys
                data class Action(
                    val iid: Int,
                    val urn: String,
                    val name: String,
                    val description: String,
                    val desc_zh_cn: String? = null,
                    @SerialName("in")
                    val _in: List<Int>,
                    @SerialName("out")
                    val _out: List<Int>,
                )
            }
        }
    }
}
