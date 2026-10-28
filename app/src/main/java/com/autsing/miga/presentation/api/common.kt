package com.autsing.miga.presentation.api

import android.util.Log
import com.autsing.miga.presentation.data.API_URL
import com.autsing.miga.presentation.data.DEVICE_URL
import com.autsing.miga.presentation.data.DevicePropertyValue
import com.autsing.miga.presentation.data.GetDevicePropertiesReqData
import com.autsing.miga.presentation.data.GetDevicePropertiesRespData
import com.autsing.miga.presentation.data.GetHomesReqData
import com.autsing.miga.presentation.data.GetHomesRespData
import com.autsing.miga.presentation.data.Home
import com.autsing.miga.presentation.data.PRODUCT_URL
import com.autsing.miga.presentation.data.Scene
import com.autsing.miga.presentation.data.SetDevicePropertiesReqData
import com.autsing.miga.presentation.data.SetDevicePropertiesRespData
import com.autsing.miga.presentation.data.TAG
import com.autsing.miga.presentation.data.USER_AGENT
import com.autsing.miga.presentation.data.decode
import com.autsing.miga.presentation.data.defaultJson
import com.autsing.miga.presentation.data.encode
import com.autsing.miga.presentation.repository.Auth
import com.autsing.miga.presentation.repository.Device
import com.autsing.miga.presentation.repository.DeviceInfo
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.cookie
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.parameters
import io.ktor.serialization.kotlinx.json.json
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
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private val client: HttpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(
            json = defaultJson,
            contentType = ContentType.Any,
        )
    }
    defaultRequest {
        header("User-Agent", USER_AGENT)
        header("x-xiaomi-protocal-flag-cli", "PROTOCAL-HTTP2")
        cookie("uLocale", "zh_CN")
    }
    install(Logging) {
        logger = Logger.ANDROID
    }
}

private suspend fun post(
    auth: Auth,
    uri: String,
    dataJson: String,
): Result<HttpResponse> = runCatching {
    val secret = auth.cookies["ssecurity"] ?: throw Exception("缺少字段 ssecurity")
    val nonce = genNonce()
    val signedNonce = genSignedNonce(secret, nonce)
    val signature = genSignature(uri, signedNonce, nonce, dataJson)

    val resp = client.submitForm(
        url = API_URL + uri,
        formParameters = parameters {
            append("_nonce", nonce)
            append("data", dataJson)
            append("signature", signature)
        },
    ) {
        auth.cookies.forEach { (k, v) -> cookie(k, v) }
    }

    resp
}

suspend fun reqGetHomes(auth: Auth): Result<List<Home>> = runCatching {
    val uri = "/v2/homeroom/gethome_merged"
    val data = GetHomesReqData(
        fg = true,
        fetch_share = true,
        fetch_share_dev = true,
        fetch_cariot = true,
        limit = 300,
        app_ver = 7,
        plat_form = 0,
    )
    val dataJson = data.encode().getOrThrow()
    val resp = post(auth, uri, dataJson).getOrThrow()
    Log.d(TAG, "reqGetHomes: ${resp.bodyAsText()}")
    val respData = resp.body<GetHomesRespData>()
    val homes = respData.result.homelist

    homes
}

@Serializable
data class GetScenesReqData(
    val home_id: String,
)

@Serializable
data class GetScenesRespData(
    val code: Int,
    val message: String,
    val result: Result,
) {

    @Serializable
    data class Result(
        val scene_info_list: List<Scene>,
    )
}

suspend fun reqGetScenes(auth: Auth): Result<List<Scene>> = runCatching {
    val homes = reqGetHomes(auth).getOrThrow()
    val homeId = homes.first().id
    val uri = "/appgateway/miot/appsceneservice/AppSceneService/GetSceneList"
    val data = GetScenesReqData(
        home_id = homeId,
    )
    val dataJson = data.encode().getOrThrow()
    val resp = post(auth, uri, dataJson).getOrThrow()
    val respData = resp.body<GetScenesRespData>()
    val scenes = respData.result.scene_info_list.filter { it.icon_url.isNotBlank() }

    scenes
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

suspend fun reqGetDevices(auth: Auth): Result<List<Device>> = runCatching {
    val uri = "/home/device_list"
    val data = GetDevicesReqData(
        getVirtualModel = false,
        getHuamiDevices = 0,
    )
    val dataJson = data.encode().getOrThrow()
    val resp = post(auth, uri, dataJson).getOrThrow()
    val respData = resp.body<GetDevicesRespData>()
    val devices = respData.result.list

    devices
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

suspend fun reqGetDeviceIconUrl(model: String): Result<String> = runCatching {
    val resp = client.get("${PRODUCT_URL}?model=$model")
    val respData = resp.body<GetDeviceBaikeRespData>()
    val realIcon = respData.data.realIcon

    realIcon
}

@Serializable
data class RunSceneReqData(
    val scene_id: String,
    val trigger_key: String,
)

@Serializable
data class RunSceneRespData(
    val code: Int,
    val message: String,
    val result: Boolean,
)

suspend fun reqRunScene(auth: Auth, scene: Scene): Result<String> = runCatching {
    val uri = "/appgateway/miot/appsceneservice/AppSceneService/RunScene"
    val data = RunSceneReqData(
        scene_id = scene.scene_id,
        trigger_key = "user.click",
    )
    val dataJson = data.encode().getOrThrow()
    val resp = post(auth, uri, dataJson).getOrThrow()
    val respData = resp.body<RunSceneRespData>()
    val message = respData.message

    message
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

suspend fun reqGetDeviceInfo(model: String): Result<DeviceInfo> = runCatching {
    val resp = client.get("${DEVICE_URL}/${model}")
    val respText = resp.bodyAsText()
    val respDataJson = Regex("""data-page="(.*?)">""").find(respText)
        ?.groups
        ?.get(1)
        ?.value
        ?.replace("&quot;", "\"")
        ?: throw Exception("解析设备信息失败")
    val respData = respDataJson.decode<GetDeviceInfoRespData>().getOrThrow()
    val info = DeviceInfo.from(model, respData)

    info
}

suspend fun reqGetDeviceProperties(
    auth: Auth,
    device: Device,
    info: DeviceInfo,
): Result<List<Pair<DeviceInfo.Property, DevicePropertyValue>>> = runCatching {
    val uri = "/miotspec/prop/get"
    val data = GetDevicePropertiesReqData(
        params = info.properties.map {
            GetDevicePropertiesReqData.Params(
                did = device.did,
                siid = it.method.siid,
                piid = it.method.piid,
            )
        }
    )
    val dataJson = data.encode().getOrThrow()
    val resp = post(auth, uri, dataJson).getOrThrow()
    val respData = resp.body<GetDevicePropertiesRespData>()
    val properties = info.properties.mapIndexed { i, it ->
        Pair(it, respData.result[i].value)
    }

    properties
}

suspend fun reqGetDeviceProperty(
    auth: Auth,
    device: Device,
    deviceProperty: DeviceInfo.Property,
): Result<Pair<DeviceInfo.Property, DevicePropertyValue>> = runCatching {
    val uri = "/miotspec/prop/get"
    val data = GetDevicePropertiesReqData(
        params = listOf(
            GetDevicePropertiesReqData.Params(
                did = device.did,
                siid = deviceProperty.method.siid,
                piid = deviceProperty.method.piid,
            )
        )
    )
    val dataJson = data.encode().getOrThrow()
    val resp = post(auth, uri, dataJson).getOrThrow()
    val respData = resp.body<GetDevicePropertiesRespData>()
    val property = Pair(deviceProperty, respData.result[0].value)

    property
}

suspend fun reqSetDeviceProperty(
    auth: Auth,
    device: Device,
    property: DeviceInfo.Property,
    value: DevicePropertyValue,
): Result<Unit> = runCatching {
    val uri = "/miotspec/prop/set"
    val data = SetDevicePropertiesReqData(
        params = listOf(
            SetDevicePropertiesReqData.Params(
                did = device.did,
                siid = property.method.siid,
                piid = property.method.piid,
                value = value,
            )
        )
    )
    val dataJson = data.encode().getOrThrow()
    val resp = post(auth, uri, dataJson).getOrThrow()
    val respData = resp.body<SetDevicePropertiesRespData>()

    if (respData.result[0].code != 0) {
        throw Exception(respData.message)
    }
}
