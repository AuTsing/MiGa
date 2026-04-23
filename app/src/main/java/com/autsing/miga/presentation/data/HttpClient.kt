package com.autsing.miga.presentation.data

import com.autsing.miga.presentation.helper.Constants
import com.autsing.miga.presentation.helper.Constants.MSG_URL
import com.autsing.miga.presentation.helper.Constants.QR_URL
import com.autsing.miga.presentation.helper.Constants.USER_AGENT
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
import com.autsing.miga.presentation.model.GetLocationResponse
import com.autsing.miga.presentation.model.GetProfileData
import com.autsing.miga.presentation.model.GetProfileResponse
import com.autsing.miga.presentation.model.GetScenesData
import com.autsing.miga.presentation.model.GetScenesResponse
import com.autsing.miga.presentation.model.Home
import com.autsing.miga.presentation.model.LoginIndexResponse
import com.autsing.miga.presentation.model.LoginLpResponse
import com.autsing.miga.presentation.model.LoginUrlResponse
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
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.parameters
import io.ktor.http.setCookie
import io.ktor.serialization.kotlinx.json.json
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder
import java.security.MessageDigest
import java.time.Instant
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

private val defaultHttpClient: HttpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(
            json = defaultJson,
            contentType = ContentType.Any,
        )
    }
    defaultRequest {
        header("User-Agent", USER_AGENT)
        header("x-xiaomi-protocal-flag-cli", "PROTOCAL-HTTP2")
    }
}
private val loginHttpClient: HttpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(
            json = defaultJson,
            contentType = ContentType.Any,
        )
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 60000L
        connectTimeoutMillis = 60000L
        socketTimeoutMillis = 60000L
    }
    defaultRequest {
        header("User-Agent", USER_AGENT)
        header("Cookie", "deviceId=${deviceId}; sdkVersion=3.4.1")
    }
}
private val deviceId: String = (('0'..'9') + ('a'..'z') + ('A'..'Z'))
    .shuffled(Random)
    .take(16)
    .joinToString("")

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

fun getDeviceId(): String = deviceId

suspend fun requestGetLoginIndex(): Result<LoginIndexResponse> = runCatching {
    val resp = loginHttpClient.get(MSG_URL)
    val loginIndexJson = resp.bodyAsText().substring(11)
    val loginIndexResp = loginIndexJson.decode<LoginIndexResponse>().getOrThrow()

    loginIndexResp
}

suspend fun requestGetLoginUrl(resp: LoginIndexResponse): Result<LoginUrlResponse> = runCatching {
    val queryMap = URI(resp.location).query
        .split("&")
        .map { it.split("=") }
        .associate { (k, v) -> k to URLDecoder.decode(v, "UTF-8") }
    val params = mapOf(
        "_qrsize" to "240",
        "qs" to resp.qs,
        "bizDeviceType" to "",
        "callback" to resp.callback,
        "_json" to "true",
        "theme" to "",
        "sid" to "xiaomiio",
        "needTheme" to "false",
        "showActiveX" to "false",
        "serviceParam" to queryMap.getValue("serviceParam"),
        "_local" to "zh_CN",
        "_sign" to resp._sign,
        "_dc" to (Instant.now().toEpochMilli()).toString(),
    )
    val paramsString = params.map { (k, v) ->
        "${URLEncoder.encode(k, "UTF-8")}=${URLEncoder.encode(v, "UTF-8")}"
    }.joinToString("&")
    val qrUrl = "$QR_URL?$paramsString"
    val resp = loginHttpClient.get(qrUrl)
    val loginUrlJson = resp.bodyAsText().substring(11)
    val loginUrlResp = loginUrlJson.decode<LoginUrlResponse>().getOrThrow()

    loginUrlResp
}

suspend fun requestGetLoginLpResponse(
    resp: LoginUrlResponse,
): Result<LoginLpResponse> = runCatching {
    val newResp = loginHttpClient.get(resp.lp) {
        header("Connection", "keep-alive")
    }
    val loginLpJson = newResp.bodyAsText().substring(11)
    val loginLpResp = loginLpJson.decode<LoginLpResponse>().getOrThrow()

    loginLpResp
}

suspend fun requestGetServiceToken(location: String): Result<String> = runCatching {
    val resp = loginHttpClient.get(location)
    val cookies = resp.setCookie()
    val serviceToken = cookies.find { it.name == "serviceToken" }?.value
        ?: throw Exception("无法获取serviceToken")

    serviceToken
}

suspend fun requestGetLocation(auth: Auth): Result<GetLocationResponse> = runCatching {
    val resp = loginHttpClient.get(MSG_URL) {
        header("Cookie", auth.toCookie())
    }
    val getLocationJson = resp.bodyAsText().substring(11)
    val getLocationResp = getLocationJson.decode<GetLocationResponse>().getOrThrow()

    getLocationResp
}
