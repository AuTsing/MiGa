package com.autsing.miga.presentation.data

import com.autsing.miga.presentation.helper.Constants
import com.autsing.miga.presentation.model.Auth
import com.autsing.miga.presentation.model.Device
import com.autsing.miga.presentation.model.DeviceInfo
import com.autsing.miga.presentation.model.DevicePropertyValue
import com.autsing.miga.presentation.model.GetDeviceBaikeResponse
import com.autsing.miga.presentation.model.GetDeviceInfoResponse
import com.autsing.miga.presentation.model.GetDevicePropertiesData
import com.autsing.miga.presentation.model.GetDevicePropertiesResponse
import com.autsing.miga.presentation.model.GetDevicesData
import com.autsing.miga.presentation.model.GetDevicesResponse
import com.autsing.miga.presentation.model.GetHomesData
import com.autsing.miga.presentation.model.GetHomesResponse
import com.autsing.miga.presentation.model.GetProfileData
import com.autsing.miga.presentation.model.GetProfileResponse
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
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.parameters
import io.ktor.serialization.kotlinx.json.json
import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

private val defaultHttpClient: HttpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(
            json = defaultJson,
            contentType = ContentType.Any,
        )
    }
    defaultRequest {
        header("User-Agent", Constants.USER_AGENT)
        header("x-xiaomi-protocal-flag-cli", "PROTOCAL-HTTP2")
    }
}

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
): Result<HttpResponse> = runCatching {
    val nonce = generateNonce()
    val signedNonce = generateSignedNonce(auth.ssecurity, nonce)
    val signature = generateSignature(uri, signedNonce, nonce, dataJson)

    val resp = defaultHttpClient.submitForm(
        url = Constants.API_URL + uri,
        formParameters = parameters {
            append("_nonce", nonce)
            append("data", dataJson)
            append("signature", signature)
        },
    ) {
        header(
            "Cookie",
            "PassportDeviceId=${auth.deviceId};userId=${auth.userId};serviceToken=${auth.serviceToken};",
        )
    }

    resp
}

suspend fun requestGetHomes(auth: Auth): Result<List<Home>> = runCatching {
    val uri = "/v2/homeroom/gethome"
    val data = GetHomesData(
        fg = false,
        fetch_share = true,
        fetch_share_dev = true,
        limit = 300,
        app_ver = 7,
    )
    val dataJson = data.encode().getOrThrow()
    val resp = post(auth, uri, dataJson).getOrThrow()
    val getHomesResp = resp.body<GetHomesResponse>()
    val homes = getHomesResp.result.homelist

    homes
}

suspend fun requestGetScenes(auth: Auth): Result<List<Scene>> = runCatching {
    val homes = requestGetHomes(auth).getOrThrow()
    val homeId = homes.first().id
    val uri = "/appgateway/miot/appsceneservice/AppSceneService/GetSceneList"
    val data = GetScenesData(
        home_id = homeId,
    )
    val dataJson = data.encode().getOrThrow()
    val resp = post(auth, uri, dataJson).getOrThrow()
    val getScenesResp = resp.body<GetScenesResponse>()
    val scenes = getScenesResp.result.scene_info_list

    scenes
}

suspend fun requestGetDevices(auth: Auth): Result<List<Device>> = runCatching {
    val uri = "/home/device_list"
    val data = GetDevicesData(
        getVirtualModel = false,
        getHuamiDevices = 0,
    )
    val dataJson = data.encode().getOrThrow()
    val resp = post(auth, uri, dataJson).getOrThrow()
    val getDevicesResp = resp.body<GetDevicesResponse>()
    val devices = getDevicesResp.result.list

    devices
}

suspend fun requestRunScene(
    auth: Auth,
    scene: Scene,
): Result<String> = runCatching {
    val uri = "/appgateway/miot/appsceneservice/AppSceneService/RunScene"
    val data = RunSceneData(
        scene_id = scene.scene_id,
        trigger_key = "user.click",
    )
    val dataJson = data.encode().getOrThrow()
    val resp = post(auth, uri, dataJson).getOrThrow()
    val runSceneResp = resp.body<RunSceneResponse>()
    val message = runSceneResp.message

    message
}

suspend fun requestGetDeviceIconUrl(model: String): Result<String> = runCatching {
    val resp = defaultHttpClient.get("${Constants.PRODUCT_URL}?model=$model")
    val getDeviceBaikeResp = resp.body<GetDeviceBaikeResponse>()
    val realIcon = getDeviceBaikeResp.data.realIcon

    realIcon
}

suspend fun requestGetDeviceInfo(model: String): Result<DeviceInfo> = runCatching {
    val resp = defaultHttpClient.get("${Constants.DEVICE_URL}/${model}")
    val respContent = resp.bodyAsText()
    val getDeviceInfoContent = Regex("""data-page="(.*?)">""").find(respContent)
        ?.groups
        ?.get(1)
        ?.value
        ?: throw Exception("解析设备信息失败")
    val getDeviceInfoJson = getDeviceInfoContent.replace("&quot;", "\"")
    val getDeviceInfoResp = getDeviceInfoJson.decode<GetDeviceInfoResponse>().getOrThrow()
    val deviceInfo = DeviceInfo.from(model, getDeviceInfoResp)

    deviceInfo
}

suspend fun requestGetDeviceProperties(
    auth: Auth,
    device: Device,
    deviceInfo: DeviceInfo,
): Result<List<Pair<DeviceInfo.Property, DevicePropertyValue>>> = runCatching {
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
    val dataJson = data.encode().getOrThrow()
    val resp = post(auth, uri, dataJson).getOrThrow()
    val getDevicePropertiesResp = resp.body<GetDevicePropertiesResponse>()
    val deviceProperties = deviceInfo.properties.mapIndexed { i, it ->
        Pair(it, getDevicePropertiesResp.result[i].value)
    }

    deviceProperties
}

suspend fun requestGetDeviceProperty(
    auth: Auth,
    device: Device,
    deviceProperty: DeviceInfo.Property,
): Result<Pair<DeviceInfo.Property, DevicePropertyValue>> = runCatching {
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
    val dataJson = data.encode().getOrThrow()
    val resp = post(auth, uri, dataJson).getOrThrow()
    val getDevicePropertiesResp = resp.body<GetDevicePropertiesResponse>()
    val deviceProperty = Pair(deviceProperty, getDevicePropertiesResp.result[0].value)

    deviceProperty
}

suspend fun requestSetDeviceProperty(
    auth: Auth,
    device: Device,
    deviceProperty: DeviceInfo.Property,
    value: DevicePropertyValue,
): Result<Pair<DeviceInfo.Property, DevicePropertyValue>> = runCatching {
    val oldDeviceProperty = requestGetDeviceProperty(auth, device, deviceProperty).getOrThrow()
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
    val dataJson = data.encode().getOrThrow()
    val resp = post(auth, uri, dataJson).getOrThrow()
    val setDevicePropertiesResp = resp.body<SetDevicePropertiesResponse>()
    val deviceProperty = if (setDevicePropertiesResp.result[0].code == 0) {
        Pair(deviceProperty, value)
    } else {
        Pair(deviceProperty, oldDeviceProperty.second)
    }

    deviceProperty
}

suspend fun requestRunAction(
    auth: Auth,
    device: Device,
    action: DeviceInfo.Action,
    inValues: List<DevicePropertyValue>,
): Result<Int> = runCatching {
    val uri = "/miotspec/action"
    val data = RunActionData(
        params = RunActionData.Params(
            did = device.did,
            siid = action.method.siid,
            aiid = action.method.aiid,
            _in = inValues,
        ),
    )
    val dataJson = data.encode().getOrThrow()
    val resp = post(auth, uri, dataJson).getOrThrow()
    val runActionResp = resp.body<RunActionResponse>()
    val code = runActionResp.result.code

    code
}

suspend fun requestGetProfile(auth: Auth): Result<GetProfileResponse> = runCatching {
    val uri = "/home/profile"
    val data = GetProfileData(id = auth.userId)
    val dataJson = data.encode().getOrThrow()
    val resp = post(auth, uri, dataJson).getOrThrow()
    val getProfileResp = resp.body<GetProfileResponse>()

    getProfileResp
}
