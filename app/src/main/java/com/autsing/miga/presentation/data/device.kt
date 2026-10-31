package com.autsing.miga.presentation.data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

@Serializable
data class Device(
    val did: String,
    val name: String,
    val isOnline: Boolean,
    val model: String,
    val spec_type: String,
)

fun List<Device>.sort(favoriteDeviceIds: List<String>): List<Device> {
    val favoriteDeviceIdsMap = favoriteDeviceIds.withIndex()
        .associate { it.value to it.index }
    return this.sortedBy { favoriteDeviceIdsMap[it.did] ?: Int.MAX_VALUE }
}

@Serializable
data class DeviceInfoV2(
    val name: String,
    val urn: String,
    val properties: List<Property>,
    val actions: List<Action>,
) {

    @Serializable
    sealed interface Property {
        val name: kotlin.String
        val urn: kotlin.String
        val access: Access
        val method: Method
        val nameZh: kotlin.String?

        @Serializable
        data class Access(
            val read: kotlin.Boolean,
            val write: kotlin.Boolean,
            val notify: kotlin.Boolean,
        )

        @Serializable
        data class Method(
            val siid: Int,
            val piid: Int,
        )

        @Serializable
        data class Boolean(
            override val name: kotlin.String,
            override val urn: kotlin.String,
            override val access: Access,
            override val method: Method,
            override val nameZh: kotlin.String? = null,
        ) : Property

        @Serializable
        data class String(
            override val name: kotlin.String,
            override val urn: kotlin.String,
            override val access: Access,
            override val method: Method,
            override val nameZh: kotlin.String? = null,
        ) : Property

        @Serializable
        data class DoubleRange(
            override val name: kotlin.String,
            override val urn: kotlin.String,
            override val access: Access,
            override val method: Method,
            override val nameZh: kotlin.String? = null,
            val from: Double,
            val to: Double,
            val step: Double,
            val unit: kotlin.String? = null,
        ) : Property

        @Serializable
        data class LongRange(
            override val name: kotlin.String,
            override val urn: kotlin.String,
            override val access: Access,
            override val method: Method,
            override val nameZh: kotlin.String? = null,
            val from: Long,
            val to: Long,
            val step: Long,
            val unit: kotlin.String? = null,
        ) : Property

        @Serializable
        data class Value<T>(
            val name: kotlin.String,
            val value: T,
            val nameZh: kotlin.String? = null,
        )

        @Serializable
        data class IntEnum(
            override val name: kotlin.String,
            override val urn: kotlin.String,
            override val access: Access,
            override val method: Method,
            override val nameZh: kotlin.String? = null,
            val values: List<Value<Int>>,
        ) : Property

        @Serializable
        data class StringEnum(
            override val name: kotlin.String,
            override val urn: kotlin.String,
            override val access: Access,
            override val method: Method,
            override val nameZh: kotlin.String? = null,
            val values: List<Value<kotlin.String>>,
        ) : Property

        @Serializable
        data class None(
            override val name: kotlin.String,
            override val urn: kotlin.String,
            override val access: Access,
            override val method: Method,
            override val nameZh: kotlin.String? = null,
        ) : Property
    }

    @Serializable
    data class Action(
        val name: String,
        val method: Method,
        val inPiids: List<Int>,
        val outPiids: List<Int>,
        val nameZh: String? = null,
    ) {

        @Serializable
        data class Method(
            val siid: Int,
            val aiid: Int,
        )
    }

    companion object {

        fun from(respData: GetDeviceInfoRespDataV2): DeviceInfoV2 {
            val properties = mutableListOf<Property>()
            val actions = mutableListOf<Action>()

            for (service in respData.services) {
                for (p in service.properties ?: emptyList()) {
                    val access = Property.Access(
                        read = p.access.contains("read"),
                        write = p.access.contains("write"),
                        notify = p.access.contains("notify"),
                    )
                    val method = Property.Method(
                        siid = service.iid,
                        piid = p.iid,
                    )
                    val property = if (p.format == "bool") {
                        Property.Boolean(
                            name = p.description,
                            urn = p.type,
                            access = access,
                            method = method,
                        )
                    } else if (p.format == "string") {
                        Property.String(
                            name = p.description,
                            urn = p.type,
                            access = access,
                            method = method,
                        )
                    } else if (p.valueRange != null) {
                        when (p.valueRange) {
                            is GetDeviceInfoRespDataV2.Service.Property.ValueRange.Double -> Property.DoubleRange(
                                name = p.description,
                                urn = p.type,
                                access = access,
                                method = method,
                                from = p.valueRange.value.first,
                                to = p.valueRange.value.second,
                                step = p.valueRange.value.third,
                                unit = p.unit,
                            )

                            is GetDeviceInfoRespDataV2.Service.Property.ValueRange.Long -> Property.LongRange(
                                name = p.description,
                                urn = p.type,
                                access = access,
                                method = method,
                                from = p.valueRange.value.first,
                                to = p.valueRange.value.second,
                                step = p.valueRange.value.third,
                                unit = p.unit,
                            )
                        }
                    } else if (p.valueList != null) {
                        when (p.valueList) {
                            is GetDeviceInfoRespDataV2.Service.Property.ValueList.Int -> Property.IntEnum(
                                name = p.description,
                                urn = p.type,
                                access = access,
                                method = method,
                                values = p.valueList.value.map {
                                    Property.Value(
                                        name = it.description,
                                        value = it.value,
                                    )
                                },
                            )

                            is GetDeviceInfoRespDataV2.Service.Property.ValueList.String -> Property.StringEnum(
                                name = p.description,
                                urn = p.type,
                                access = access,
                                method = method,
                                values = p.valueList.value.map {
                                    Property.Value(
                                        name = it.description,
                                        value = it.value,
                                    )
                                },
                            )
                        }
                    } else {
                        Property.None(
                            name = p.description,
                            urn = p.type,
                            access = access,
                            method = method,
                        )
                    }

                    properties.add(property)
                }

                for (a in service.actions ?: emptyList()) {
                    val method = Action.Method(
                        siid = service.iid,
                        aiid = a.iid,
                    )

                    val action = Action(
                        name = a.description,
                        method = method,
                        inPiids = a._in,
                        outPiids = a._out,
                    )

                    actions.add(action)
                }
            }

            val info = DeviceInfoV2(
                name = respData.description,
                urn = respData.type,
                properties = properties,
                actions = actions,
            )

            return info
        }
    }
}

@Serializable
data class GetDevicesReqDataV2(
    val getVirtualModel: Boolean,
    val getHuamiDevices: Int,
    val get_split_device: Boolean,
    val support_smart_home: Boolean,
)

@Serializable
data class GetDevicesRespDataV2(
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
data class GetDeviceBaikeRespDataV2(
    val code: Int,
    val data: Data,
) {

    @Serializable
    data class Data(
        val realIcon: String,
    )
}

@Serializable
data class GetDeviceInfoRespDataV2(
    val type: String,
    val description: String,
    val services: List<Service>,
) {

    @Serializable
    data class Service(
        val iid: Int,
        val type: String,
        val description: String,
        val properties: List<Property>? = null,
        val actions: List<Action>? = null,
    ) {

        @Serializable(with = Property.Serializer::class)
        data class Property(
            val iid: Int,
            val type: String,
            val description: String,
            val access: Set<String>,
            val format: String,
            val unit: String? = null,
            @SerialName("value-range")
            val valueRange: ValueRange? = null,
            @SerialName("value-list")
            val valueList: ValueList? = null,
        ) {

            sealed interface ValueRange {
                data class Double(
                    val value: Triple<kotlin.Double, kotlin.Double, kotlin.Double>,
                ) : ValueRange

                data class Long(
                    val value: Triple<kotlin.Long, kotlin.Long, kotlin.Long>,
                ) : ValueRange
            }

            sealed interface ValueList {
                data class Int(val value: List<Value<kotlin.Int>>) : ValueList
                data class String(val value: List<Value<kotlin.String>>) : ValueList

                @Serializable
                data class Value<T>(
                    val value: T,
                    val description: kotlin.String,
                )
            }

            object Serializer : KSerializer<Property> {
                override val descriptor: SerialDescriptor =
                    PrimitiveSerialDescriptor("PropertySerializer", PrimitiveKind.STRING)

                override fun serialize(encoder: Encoder, value: Property) {
                    require(encoder is JsonEncoder)

                    val jsonObject = buildJsonObject {
                        put("iid", JsonPrimitive(value.iid))
                        put("type", JsonPrimitive(value.type))
                        put("description", JsonPrimitive(value.description))
                        put(
                            "access",
                            JsonArray(value.access.map { JsonPrimitive(it) }),
                        )
                        put("format", JsonPrimitive(value.format))
                        value.unit?.let { put("unit", JsonPrimitive(it)) }
                        value.valueRange?.let { r ->
                            val array = when (r) {
                                is ValueRange.Double -> r.value.toList().map { JsonPrimitive(it) }
                                is ValueRange.Long -> r.value.toList().map { JsonPrimitive(it) }
                            }
                            put("value-range", JsonArray(array))
                        }
                        value.valueList?.let { r ->
                            val array = when (r) {
                                is ValueList.Int -> r.value.map {
                                    Json.encodeToJsonElement(
                                        ValueList.Value.serializer(Int.serializer()),
                                        it,
                                    )
                                }

                                is ValueList.String -> r.value.map {
                                    Json.encodeToJsonElement(
                                        ValueList.Value.serializer(String.serializer()),
                                        it,
                                    )
                                }

                            }
                            put("value-list", JsonArray(array))
                        }
                    }
                    encoder.encodeJsonElement(jsonObject)
                }

                override fun deserialize(decoder: Decoder): Property {
                    require(decoder is JsonDecoder)

                    val jsonObject = decoder.decodeJsonElement().jsonObject
                    val access = jsonObject["access"]!!.jsonArray
                        .map { it.jsonPrimitive.content }
                        .toSet()
                    val format = jsonObject["format"]!!.jsonPrimitive.content
                    val valueRange = jsonObject["value-range"]?.jsonArray
                        ?.let { a ->
                            when (format) {
                                "float" -> a.map { it.jsonPrimitive.content.toDouble() }
                                    .let { Triple(it[0], it[1], it[2]) }
                                    .let { ValueRange.Double(it) }

                                "uint8" -> a.map { it.jsonPrimitive.content.toLong() }
                                    .let { Triple(it[0], it[1], it[2]) }
                                    .let { ValueRange.Long(it) }

                                "uint16" -> a.map { it.jsonPrimitive.content.toLong() }
                                    .let { Triple(it[0], it[1], it[2]) }
                                    .let { ValueRange.Long(it) }

                                "uint32" -> a.map { it.jsonPrimitive.content.toLong() }
                                    .let { Triple(it[0], it[1], it[2]) }
                                    .let { ValueRange.Long(it) }

                                "int8" -> a.map { it.jsonPrimitive.content.toLong() }
                                    .let { Triple(it[0], it[1], it[2]) }
                                    .let { ValueRange.Long(it) }

                                "int16" -> a.map { it.jsonPrimitive.content.toLong() }
                                    .let { Triple(it[0], it[1], it[2]) }
                                    .let { ValueRange.Long(it) }

                                "int32" -> a.map { it.jsonPrimitive.content.toLong() }
                                    .let { Triple(it[0], it[1], it[2]) }
                                    .let { ValueRange.Long(it) }

                                else -> null
                            }
                        }
                    val valueList = jsonObject["value-list"]?.jsonArray
                        ?.let { a ->
                            when (format) {
                                "uint8" -> a.map {
                                    ValueList.Value(
                                        value = it.jsonObject["value"]!!.jsonPrimitive.content.toInt(),
                                        description = it.jsonObject["description"]!!.jsonPrimitive.content,
                                    )
                                }.let { ValueList.Int(it) }

                                "string" -> a.map {
                                    ValueList.Value(
                                        value = it.jsonObject["value"]!!.jsonPrimitive.content,
                                        description = it.jsonObject["description"]!!.jsonPrimitive.content,
                                    )
                                }.let { ValueList.String(it) }

                                else -> null
                            }
                        }

                    return Property(
                        iid = jsonObject["iid"]!!.jsonPrimitive.int,
                        type = jsonObject["type"]!!.jsonPrimitive.content,
                        description = jsonObject["description"]!!.jsonPrimitive.content,
                        access = access,
                        format = jsonObject["format"]!!.jsonPrimitive.content,
                        unit = jsonObject["unit"]?.jsonPrimitive?.content,
                        valueRange = valueRange,
                        valueList = valueList,
                    )
                }
            }
        }

        @Serializable
        data class Action(
            val iid: Int,
            val type: String,
            val description: String,
            @SerialName("in")
            val _in: List<Int>,
            @SerialName("out")
            val _out: List<Int>,
        )
    }
}

@Serializable
data class GetDevicePropertiesReqDataV2(
    val params: List<Params>,
) {

    @Serializable
    data class Params(
        val did: String,
        val siid: Int,
        val piid: Int,
    )
}

@Serializable(with = DevicePropertyValueV2.Serializer::class)
sealed interface DevicePropertyValueV2 {

    data class Boolean(val value: kotlin.Boolean) : DevicePropertyValueV2
    data class Long(val value: kotlin.Long) : DevicePropertyValueV2
    data class Double(val value: kotlin.Double) : DevicePropertyValueV2
    data class String(val value: kotlin.String) : DevicePropertyValueV2
    object None : DevicePropertyValueV2

    object Serializer : KSerializer<DevicePropertyValueV2> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("ValueSerializer", PrimitiveKind.STRING)

        @OptIn(ExperimentalSerializationApi::class)
        override fun serialize(encoder: Encoder, value: DevicePropertyValueV2) {
            require(encoder is JsonEncoder)

            when (value) {
                is Boolean -> encoder.encodeBoolean(value.value)
                is Long -> encoder.encodeLong(value.value)
                is Double -> encoder.encodeDouble(value.value)
                is String -> encoder.encodeString(value.value)
                None -> encoder.encodeNull()
            }
        }

        override fun deserialize(decoder: Decoder): DevicePropertyValueV2 {
            require(decoder is JsonDecoder)

            val element = decoder.decodeJsonElement() as? JsonPrimitive ?: return None
            val value = element.booleanOrNull?.let { Boolean(it) }
                ?: element.longOrNull?.let { Long(it) }
                ?: element.doubleOrNull?.let { Double(it) }
                ?: element.contentOrNull?.let { String(it) }
                ?: None

            return value
        }
    }
}

@Serializable
data class GetDevicePropertiesRespDataV2(
    val code: Int,
    val message: String,
    val result: List<Result>,
) {

    @Serializable
    data class Result(
        val code: Int,
        val did: String,
        val iid: String,
        val siid: Int,
        val piid: Int,
        val value: DevicePropertyValueV2 = DevicePropertyValueV2.None,
    )
}

@Serializable
data class SetDevicePropertiesReqDataV2(
    val params: List<Param>,
) {

    @Serializable
    data class Param(
        val did: String,
        val siid: Int,
        val piid: Int,
        val value: DevicePropertyValueV2,
    )
}

@Serializable
data class SetDevicePropertiesRespDataV2(
    val code: Int,
    val message: String,
    val result: List<Result>,
) {

    @Serializable
    data class Result(
        val code: Int,
        val did: String,
        val iid: String,
        val siid: Int,
        val piid: Int,
        val value: DevicePropertyValueV2 = DevicePropertyValueV2.None,
    )
}
