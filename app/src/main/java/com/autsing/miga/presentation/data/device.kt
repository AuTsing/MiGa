package com.autsing.miga.presentation.data

import com.autsing.miga.presentation.api.GetDeviceInfoRespData
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
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
