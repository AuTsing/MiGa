package com.autsing.miga.presentation.data

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull

@Serializable(with = DevicePropertyRange.Serializer::class)
sealed class DevicePropertyRange {

    companion object {

        fun from(range: GetDeviceInfoRespData.Props.Spec.Service.Property.Ranges?): DevicePropertyRange {
            return when (range) {
                is GetDeviceInfoRespData.Props.Spec.Service.Property.Ranges.Uint8 -> Long(
                    from = range.values[0].toLong(),
                    to = range.values[1].toLong(),
                    step = range.values[2].toLong(),
                )

                is GetDeviceInfoRespData.Props.Spec.Service.Property.Ranges.Uint16 -> Long(
                    from = range.values[0].toLong(),
                    to = range.values[1].toLong(),
                    step = range.values[2].toLong(),
                )

                is GetDeviceInfoRespData.Props.Spec.Service.Property.Ranges.Uint32 -> Long(
                    from = range.values[0].toLong(),
                    to = range.values[1].toLong(),
                    step = range.values[2].toLong(),
                )

                is GetDeviceInfoRespData.Props.Spec.Service.Property.Ranges.Int8 -> Long(
                    from = range.values[0].toLong(),
                    to = range.values[1].toLong(),
                    step = range.values[2].toLong(),
                )

                is GetDeviceInfoRespData.Props.Spec.Service.Property.Ranges.Int16 -> Long(
                    from = range.values[0].toLong(),
                    to = range.values[1].toLong(),
                    step = range.values[2].toLong(),
                )

                is GetDeviceInfoRespData.Props.Spec.Service.Property.Ranges.Int32 -> Long(
                    from = range.values[0].toLong(),
                    to = range.values[1].toLong(),
                    step = range.values[2].toLong(),
                )

                is GetDeviceInfoRespData.Props.Spec.Service.Property.Ranges.Float -> Double(
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
                val point = length * percentage
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

@Serializable(with = DevicePropertyValue.Serializer::class)
sealed class DevicePropertyValue {

    data class Boolean(val value: kotlin.Boolean) : DevicePropertyValue()
    data class Long(val value: kotlin.Long) : DevicePropertyValue()
    data class Double(val value: kotlin.Double) : DevicePropertyValue()
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
                is Boolean -> encoder.encodeBoolean(value.value)
                is Long -> encoder.encodeLong(value.value)
                is Double -> encoder.encodeDouble(value.value)
                is String -> encoder.encodeString(value.value)
                is None -> encoder.encodeNull()
            }
        }

        override fun deserialize(decoder: Decoder): DevicePropertyValue {
            require(decoder is JsonDecoder)

            val jsonElement = decoder.decodeJsonElement()
            return if (jsonElement is JsonPrimitive) {
                when {
                    jsonElement.booleanOrNull != null -> Boolean(jsonElement.boolean)
                    jsonElement.longOrNull != null -> Long(jsonElement.long)
                    jsonElement.doubleOrNull != null -> Double(jsonElement.double)
                    jsonElement.contentOrNull != null -> String(jsonElement.content)
                    else -> None
                }
            } else {
                None
            }
        }
    }
}

@Serializable
data class Device(
    val did: String,
    val name: String,
    val isOnline: Boolean,
    val model: String,
)

fun List<Device>.sort(favoriteDeviceIds: List<String>): List<Device> {
    val favoriteDeviceIdsMap = favoriteDeviceIds.withIndex()
        .associate { it.value to it.index }
    return this.sortedBy { favoriteDeviceIdsMap[it.did] ?: Int.MAX_VALUE }
}

@Serializable
data class DeviceInfo(
    val name: String,
    val model: String,
    val properties: List<Property>,
    val actions: List<Action>,
) {

    companion object {

        fun from(model: String, resp: GetDeviceInfoRespData): DeviceInfo {
            val (deviceInfoName, deviceInfoModel) = if (resp.props.product != null) {
                Pair(
                    resp.props.product.name,
                    resp.props.product.model,
                )
            } else {
                Pair(resp.props.spec.name, model)
            }
            val deviceInfoProperties = mutableListOf<Property>()
            val deviceInfoActions = mutableListOf<Action>()

            val services = resp.props.spec.services.values
            for (service in services) {

                val properties = service.properties?.values ?: emptyList()
                for (property in properties) {

                    val type = property.format
                    val access = Property.Access(
                        read = property.access.contains("read"),
                        write = property.access.contains("write"),
                        notify = property.access.contains("notify"),
                    )
                    val range = DevicePropertyRange.from(property.valueRange)
                    val values = when (property.valueList) {
                        is GetDeviceInfoRespData.Props.Spec.Service.Property.Values.Uint8 -> property.valueList.values.map {
                            Property.Value(
                                value = DevicePropertyValue.Long(it.value.toLong()),
                                description = it.description,
                                desc_zh_cn = it.desc_zh_cn,
                            )
                        }

                        is GetDeviceInfoRespData.Props.Spec.Service.Property.Values.String -> property.valueList.values.map {
                            Property.Value(
                                value = DevicePropertyValue.String(it.value),
                                description = it.description,
                                desc_zh_cn = it.desc_zh_cn,
                            )
                        }

                        else -> emptyList()
                    }

                    deviceInfoProperties.add(
                        Property(
                            name = property.name,
                            description = property.description,
                            descZhCn = property.desc_zh_cn ?: "",
                            type = type,
                            access = access,
                            unit = property.unit ?: "",
                            range = range,
                            values = values,
                            method = Property.Method(
                                siid = service.iid,
                                piid = property.iid,
                            )
                        )
                    )
                }

                val actions = service.actions?.values ?: emptyList()
                for (action in actions) {
                    deviceInfoActions.add(
                        Action(
                            name = action.name,
                            description = action.description,
                            descZhCn = action.desc_zh_cn ?: "",
                            method = Action.Method(
                                siid = service.iid,
                                aiid = action.iid,
                            ),
                            inPiids = action._in,
                            outPiids = action._out,
                        )
                    )
                }
            }

            return DeviceInfo(
                name = deviceInfoName,
                model = deviceInfoModel,
                properties = deviceInfoProperties,
                actions = deviceInfoActions,
            )
        }
    }

    @Serializable
    data class Property(
        val name: String,
        val description: String,
        val descZhCn: String,
        val type: String,
        val access: Access,
        val unit: String,
        val range: DevicePropertyRange,
        val values: List<Value>,
        val method: Method,
    ) {

        @Serializable
        data class Access(
            val read: Boolean,
            val write: Boolean,
            val notify: Boolean,
        )

        @Serializable
        data class Value(
            val value: DevicePropertyValue,
            val description: String,
            val desc_zh_cn: String? = null,
        )

        @Serializable
        data class Method(
            val siid: Int,
            val piid: Int,
        )
    }

    @Serializable
    data class Action(
        val name: String,
        val description: String,
        val descZhCn: String,
        val method: Method,
        val inPiids: List<Int>,
        val outPiids: List<Int>,
    ) {

        @Serializable
        data class Method(
            val siid: Int,
            val aiid: Int,
        )
    }
}

@Serializable
data class GetDevicesReqData(
    val getVirtualModel: Boolean,
    val getHuamiDevices: Int,
)

@Serializable
data class GetDevicesRespData(
    val code: Int,
    val message: String,
    val result: Result,
) {

    @Serializable
    data class Result(
        val list: List<Device>,
    )
}

@Serializable
data class GetDeviceBaikeRespData(
    val code: Int,
    val data: Data,
) {

    @Serializable
    data class Data(
        val realIcon: String,
    )
}

@Serializable
data class GetDeviceInfoRespData(
    val props: Props,
) {

    @Serializable
    data class Props(
        val product: Product? = null,
        val spec: Spec,
    ) {

        @Serializable
        data class Product(
            val name: String,
            val model: String,
        )

        @Serializable
        data class Spec(
            val name: String,
            val services: Map<String, Service>,
        ) {

            @Serializable
            data class Service(
                val iid: Int,
                val urn: String,
                val properties: Map<String, Property>? = null,
                val actions: Map<String, Action>? = null,
            ) {

                @Serializable(with = Property.Serializer::class)
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

                        override fun serialize(encoder: Encoder, value: Property) {
                            require(encoder is JsonEncoder)

                            val jsonObject = buildJsonObject {
                                put("iid", JsonPrimitive(value.iid))
                                put("urn", JsonPrimitive(value.urn))
                                put("format", JsonPrimitive(value.format))
                                put("name", JsonPrimitive(value.name))
                                put("description", JsonPrimitive(value.description))
                                put(
                                    "access",
                                    JsonArray(value.access.map { JsonPrimitive(it) }),
                                )

                                value.unit?.let { put("unit", JsonPrimitive(it)) }
                                value.valueRange?.let {
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
                                value.valueList?.let {
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
                                value.desc_zh_cn?.let { put("desc_zh_cn", JsonPrimitive(it)) }
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
                                        "uint8" -> Ranges.Uint8(array.map { it.jsonPrimitive.content.toUByte() })
                                        "uint16" -> Ranges.Uint16(array.map { it.jsonPrimitive.content.toUShort() })
                                        "uint32" -> Ranges.Uint32(array.map { it.jsonPrimitive.content.toUInt() })
                                        "int8" -> Ranges.Int8(array.map { it.jsonPrimitive.content.toByte() })
                                        "int16" -> Ranges.Int16(array.map { it.jsonPrimitive.content.toShort() })
                                        "int32" -> Ranges.Int32(array.map { it.jsonPrimitive.content.toInt() })
                                        "float" -> Ranges.Float(array.map { it.jsonPrimitive.content.toFloat() })
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

                    sealed class Ranges {
                        data class Uint8(val values: List<UByte>) : Ranges()
                        data class Uint16(val values: List<UShort>) : Ranges()
                        data class Uint32(val values: List<UInt>) : Ranges()
                        data class Int8(val values: List<Byte>) : Ranges()
                        data class Int16(val values: List<Short>) : Ranges()
                        data class Int32(val values: List<Int>) : Ranges()
                        data class Float(val values: List<kotlin.Float>) : Ranges()
                    }

                    sealed class Values {
                        data class Uint8(val values: List<Value<UByte>>) : Values()
                        data class String(val values: List<Value<kotlin.String>>) : Values()
                    }

                    @Serializable
                    data class Value<T>(
                        val value: T,
                        val description: String,
                        val desc_zh_cn: String? = null,
                    )
                }

                @Serializable
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

@Serializable
data class GetDevicePropertiesReqData(
    val params: List<Params>,
) {

    @Serializable
    data class Params(
        val did: String,
        val siid: Int,
        val piid: Int,
    )
}

@Serializable
data class GetDevicePropertiesRespData(
    val code: Int,
    val message: String,
    val result: List<Result>,
) {

    @Serializable
    data class Result(
        val did: String,
        val iid: String,
        val siid: Int,
        val piid: Int,
        val value: DevicePropertyValue = DevicePropertyValue.None,
        val code: Int,
    )
}

@Serializable
data class SetDevicePropertiesReqData(
    val params: List<Params>,
) {

    @Serializable
    data class Params(
        val did: String,
        val siid: Int,
        val piid: Int,
        val value: DevicePropertyValue,
    )
}

@Serializable
data class SetDevicePropertiesRespData(
    val code: Int,
    val message: String,
    val result: List<Result>,
) {

    @Serializable
    data class Result(
        val did: String,
        val iid: String,
        val siid: Int,
        val piid: Int,
        val code: Int,
    )
}
