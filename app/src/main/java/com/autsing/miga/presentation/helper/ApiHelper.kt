package com.autsing.miga.presentation.helper

import android.util.Log
import com.autsing.miga.presentation.model.Auth
import com.autsing.miga.presentation.model.Device
import com.autsing.miga.presentation.model.DeviceInfo
import com.autsing.miga.presentation.model.DevicePropertyRange
import com.autsing.miga.presentation.model.DevicePropertyValue
import com.autsing.miga.presentation.model.GetDeviceBaikeResponse
import com.autsing.miga.presentation.model.GetDeviceInfoResponse
import com.autsing.miga.presentation.model.GetDevicePropertiesData
import com.autsing.miga.presentation.model.GetDevicePropertiesResponse
import com.autsing.miga.presentation.model.GetDevicesData
import com.autsing.miga.presentation.model.GetDevicesResponse
import com.autsing.miga.presentation.model.GetHomesData
import com.autsing.miga.presentation.model.GetHomesResponse
import com.autsing.miga.presentation.model.GetScenesData
import com.autsing.miga.presentation.model.GetScenesResponse
import com.autsing.miga.presentation.model.Home
import com.autsing.miga.presentation.model.RunActionData
import com.autsing.miga.presentation.model.RunActionResponse
import com.autsing.miga.presentation.model.RunSceneData
import com.autsing.miga.presentation.model.RunSceneResponse
import com.autsing.miga.presentation.model.Scene
import com.autsing.miga.presentation.model.SetDevicePropertiesData
import com.autsing.miga.presentation.model.SetDevicePropertiesResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class ApiHelper {

    companion object {
        lateinit var instance: ApiHelper
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("User-Agent", Constants.USER_AGENT)
                .addHeader("x-xiaomi-protocal-flag-cli", "PROTOCAL-HTTP2")
                .build()
            chain.proceed(request)
        }
        .build()

    private fun generateNonce(length: Int = 16): String {
        val chars = "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }

    private fun generateSignedNonce(secret: String, nonce: String): String {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        messageDigest.update(Base64.getDecoder().decode(secret))
        messageDigest.update(Base64.getDecoder().decode(nonce))
        return Base64.getEncoder().encodeToString(messageDigest.digest())
    }

    private fun generateSignature(
        uri: String,
        signedNonce: String,
        nonce: String,
        data: String,
    ): String {
        val hmac = Mac.getInstance("HmacSHA256")
        val sign = "$uri&$signedNonce&$nonce&data=$data"
        val key = SecretKeySpec(Base64.getDecoder().decode(signedNonce), "HmacSHA256")
        hmac.init(key)
        return Base64.getEncoder().encodeToString(hmac.doFinal(sign.toByteArray(Charsets.UTF_8)))
    }

    private suspend fun post(
        auth: Auth,
        uri: String,
        dataJson: String,
    ): Result<String> = withContext(Dispatchers.IO) {
        var maybeResponse: Response? = null

        runCatching {
            val nonce = generateNonce()
            val signedNonce = generateSignedNonce(auth.ssecurity, nonce)
            val signature = generateSignature(uri, signedNonce, nonce, dataJson)

            val postData = FormBody.Builder()
                .add("_nonce", nonce)
                .add("data", dataJson)
                .add("signature", signature)
                .build()
            val request = Request.Builder()
                .url(Constants.API_URL + uri)
                .post(postData)
                .addHeader(
                    "Cookie",
                    "PassportDeviceId=${auth.deviceId};userId=${auth.userId};serviceToken=${auth.serviceToken};",
                )
                .build()
            val response = okHttpClient.newCall(request).execute().also { maybeResponse = it }
            val json = response.body?.string() ?: ""

            return@runCatching json
        }.also {
            maybeResponse?.close()
        }
    }

    suspend fun getHomes(auth: Auth): Result<List<Home>> = withContext(Dispatchers.IO) {
        runCatching {
            val uri = "/v2/homeroom/gethome"
            val data = GetHomesData(
                fg = false,
                fetch_share = true,
                fetch_share_dev = true,
                limit = 300,
                app_ver = 7,
            )
            val dataJson = Json.encodeToString(data)

            val getHomesJson = post(auth, uri, dataJson).getOrThrow()
            val getHomesResponse = Json.decodeFromString<GetHomesResponse>(getHomesJson)

            return@runCatching getHomesResponse.result.homelist
        }.onFailure {
            Log.e(Constants.TAG, "getHomes: ${it.stackTraceToString()}")
        }
    }

    suspend fun getScenes(auth: Auth): Result<List<Scene>> = withContext(Dispatchers.IO) {
        runCatching {
            val homes = getHomes(auth).getOrThrow()
            val homeId = homes.first().id

            val uri = "/appgateway/miot/appsceneservice/AppSceneService/GetSceneList"
            val data = GetScenesData(
                home_id = homeId,
            )
            val dataJson = Json.encodeToString(data)
            val getScenesJson = post(auth, uri, dataJson).getOrThrow()
            val getScenesResponse = Json.decodeFromString<GetScenesResponse>(getScenesJson)

            return@runCatching getScenesResponse.result.scene_info_list
        }.onFailure {
            Log.e(Constants.TAG, "getScenes: ${it.stackTraceToString()}")
        }
    }

    suspend fun getDevices(auth: Auth): Result<List<Device>> = withContext(Dispatchers.IO) {
        runCatching {
            val uri = "/home/device_list"
            val data = GetDevicesData(
                getVirtualModel = false,
                getHuamiDevices = 0,
            )
            val dataJson = Json.encodeToString(data)

            val getDevicesJson = post(auth, uri, dataJson).getOrThrow()
            val getDevicesResponse = Json.decodeFromString<GetDevicesResponse>(getDevicesJson)

            return@runCatching getDevicesResponse.result.list
        }.onFailure {
            Log.e(Constants.TAG, "getDevices: ${it.stackTraceToString()}")
        }
    }

    suspend fun runScene(
        auth: Auth,
        scene: Scene,
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val uri = "/appgateway/miot/appsceneservice/AppSceneService/RunScene"
            val data = RunSceneData(
                scene_id = scene.scene_id,
                trigger_key = "user.click",
            )
            val dataJson = Json.encodeToString(data)

            val runSceneJson = post(auth, uri, dataJson).getOrThrow()
            val runSceneResponse = Json.decodeFromString<RunSceneResponse>(runSceneJson)

            return@runCatching runSceneResponse.message
        }.onFailure {
            Log.e(Constants.TAG, "getDevices: ${it.stackTraceToString()}")
        }
    }

    suspend fun getDeviceIconUrl(model: String): Result<String> = withContext(Dispatchers.IO) {
        var maybeResponse: Response? = null

        runCatching {
            val request = Request.Builder().url("${Constants.PRODUCT_URL}?model=$model").build()
            val response = okHttpClient.newCall(request).execute().also { maybeResponse = it }
            val json = response.body?.string() ?: ""
            val getDeviceBaikeResponse = Json.decodeFromString<GetDeviceBaikeResponse>(json)

            return@runCatching getDeviceBaikeResponse.data.realIcon
        }.onFailure {
            Log.e(Constants.TAG, "getDeviceIconUrl: ${it.stackTraceToString()}")
        }.also {
            maybeResponse?.close()
        }
    }

    suspend fun getDeviceInfo(device: Device): Result<DeviceInfo> = withContext(Dispatchers.IO) {
        var maybeResponse: Response? = null

        runCatching {
            val request = Request.Builder().url("${Constants.DEVICE_URL}/${device.model}").build()
            val response = okHttpClient.newCall(request).execute().also { maybeResponse = it }
            val responseContent = response.body?.string() ?: throw Exception("获取设备信息失败")
            val getDeviceInfoContent = Regex("""data-page="(.*?)">""").find(responseContent)
                ?.groups
                ?.get(1)
                ?.value
                ?: throw Exception("获取设备信息失败")
            val getDeviceInfoJson = getDeviceInfoContent.replace("&quot;", "\"")
            val getDeviceInfoResponse = Json
                .decodeFromString<GetDeviceInfoResponse>(getDeviceInfoJson)

            val (deviceInfoName, deviceInfoModel) = if (getDeviceInfoResponse.props.product != null) {
                Pair(
                    getDeviceInfoResponse.props.product.name,
                    getDeviceInfoResponse.props.product.model,
                )
            } else {
                Pair(getDeviceInfoResponse.props.spec.name, device.model)
            }
            val deviceInfoProperties = mutableListOf<DeviceInfo.Property>()
            val deviceInfoActions = mutableListOf<DeviceInfo.Action>()

            val services = getDeviceInfoResponse.props.spec.services.values
            for (service in services) {

                val properties = service.properties?.values ?: emptyList()
                for (property in properties) {

                    val type = property.format
                    val access = DeviceInfo.Property.Access(
                        read = property.access.contains("read"),
                        write = property.access.contains("write"),
                        notify = property.access.contains("notify"),
                    )
                    val range = DevicePropertyRange.from(property.valueRange)
                    val values = when (property.valueList) {
                        is GetDeviceInfoResponse.Props.Spec.Service.Property.Values.Uint8 -> property.valueList.values.map {
                            DeviceInfo.Property.Value(
                                value = DevicePropertyValue.Long(it.value.toLong()),
                                description = it.description,
                                desc_zh_cn = it.desc_zh_cn,
                            )
                        }

                        is GetDeviceInfoResponse.Props.Spec.Service.Property.Values.String -> property.valueList.values.map {
                            DeviceInfo.Property.Value(
                                value = DevicePropertyValue.String(it.value),
                                description = it.description,
                                desc_zh_cn = it.desc_zh_cn,
                            )
                        }

                        else -> emptyList()
                    }

                    deviceInfoProperties.add(
                        DeviceInfo.Property(
                            name = property.name,
                            description = property.description,
                            descZhCn = property.desc_zh_cn ?: "",
                            type = type,
                            access = access,
                            unit = property.unit ?: "",
                            range = range,
                            values = values,
                            method = DeviceInfo.Property.Method(
                                siid = service.iid,
                                piid = property.iid,
                            )
                        )
                    )
                }

                val actions = service.actions?.values ?: emptyList()
                for (action in actions) {
                    deviceInfoActions.add(
                        DeviceInfo.Action(
                            name = action.name,
                            description = action.description,
                            descZhCn = action.desc_zh_cn ?: "",
                            method = DeviceInfo.Action.Method(
                                siid = service.iid,
                                aiid = action.iid,
                            ),
                            inPiids = action._in,
                            outPiids = action._out,
                        )
                    )
                }
            }

            return@runCatching DeviceInfo(
                name = deviceInfoName,
                model = deviceInfoModel,
                properties = deviceInfoProperties,
                actions = deviceInfoActions,
            )
        }.onFailure {
            Log.e(Constants.TAG, "getDeviceInfo: ${it.stackTraceToString()}")
        }.also {
            maybeResponse?.close()
        }
    }

    suspend fun getDeviceProperties(
        auth: Auth,
        device: Device,
        deviceInfo: DeviceInfo,
    ): Result<List<Pair<DeviceInfo.Property, DevicePropertyValue>>> = withContext(Dispatchers.IO) {
        runCatching {
            val uri = "/miotspec/prop/get"
            val data = GetDevicePropertiesData(
                params = deviceInfo.properties.map {
                    GetDevicePropertiesData.Params(
                        did = device.did,
                        siid = it.method.siid,
                        piid = it.method.piid,
                    )
                }
            )
            val dataJson = Json.encodeToString(data)

            val getDevicePropertiesJson = post(auth, uri, dataJson).getOrThrow()
            val getDevicePropertiesResponse = Json
                .decodeFromString<GetDevicePropertiesResponse>(getDevicePropertiesJson)
            val deviceProperties = deviceInfo.properties.mapIndexed { i, it ->
                Pair(it, getDevicePropertiesResponse.result[i].value)
            }

            return@runCatching deviceProperties
        }.onFailure {
            Log.e(Constants.TAG, "getDeviceProperties: ${it.stackTraceToString()}")
        }
    }

    suspend fun getDeviceProperty(
        auth: Auth,
        device: Device,
        deviceProperty: DeviceInfo.Property,
    ): Result<Pair<DeviceInfo.Property, DevicePropertyValue>> = withContext(Dispatchers.IO) {
        runCatching {
            val uri = "/miotspec/prop/get"
            val data = GetDevicePropertiesData(
                params = listOf(
                    GetDevicePropertiesData.Params(
                        did = device.did,
                        siid = deviceProperty.method.siid,
                        piid = deviceProperty.method.piid,
                    )
                )
            )
            val dataJson = Json.encodeToString(data)

            val getDevicePropertiesJson = post(auth, uri, dataJson).getOrThrow()
            val getDevicePropertiesResponse = Json
                .decodeFromString<GetDevicePropertiesResponse>(getDevicePropertiesJson)
            val deviceProperty = Pair(deviceProperty, getDevicePropertiesResponse.result[0].value)

            return@runCatching deviceProperty
        }.onFailure {
            Log.e(Constants.TAG, "getDeviceProperties: ${it.stackTraceToString()}")
        }
    }

    suspend fun setDeviceProperty(
        auth: Auth,
        device: Device,
        deviceProperty: DeviceInfo.Property,
        value: DevicePropertyValue,
    ): Result<Pair<DeviceInfo.Property, DevicePropertyValue>> = withContext(Dispatchers.IO) {
        runCatching {
            val oldDeviceProperty = getDeviceProperty(auth, device, deviceProperty).getOrThrow()

            val uri = "/miotspec/prop/set"
            val data = SetDevicePropertiesData(
                params = listOf(
                    SetDevicePropertiesData.Params(
                        did = device.did,
                        siid = deviceProperty.method.siid,
                        piid = deviceProperty.method.piid,
                        value = value,
                    )
                )
            )
            val dataJson = Json.encodeToString(data)

            val setDevicePropertiesJson = post(auth, uri, dataJson).getOrThrow()
            val setDevicePropertiesResponse = Json
                .decodeFromString<SetDevicePropertiesResponse>(setDevicePropertiesJson)

            val deviceProperty = if (setDevicePropertiesResponse.result[0].code == 0) {
                Pair(deviceProperty, value)
            } else {
                Pair(deviceProperty, oldDeviceProperty.second)
            }

            return@runCatching deviceProperty
        }.onFailure {
            Log.e(Constants.TAG, "setDeviceProperties: ${it.stackTraceToString()}")
        }
    }

    suspend fun runAction(
        auth: Auth,
        device: Device,
        action: DeviceInfo.Action,
        inValues: List<DevicePropertyValue>,
    ): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val uri = "/miotspec/action"
            val data = RunActionData(
                params = RunActionData.Params(
                    did = device.did,
                    siid = action.method.siid,
                    aiid = action.method.aiid,
                    _in = inValues,
                ),
            )
            val dataJson = Json.encodeToString(data)

            val runActionJson = post(auth, uri, dataJson).getOrThrow()
            val runActionResponse = Json.decodeFromString<RunActionResponse>(runActionJson)

            return@runCatching runActionResponse.result.code
        }.onFailure {
            Log.e(Constants.TAG, "getDevices: ${it.stackTraceToString()}")
        }
    }
}
