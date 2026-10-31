package com.autsing.miga.presentation.api

import android.util.Log
import com.autsing.miga.presentation.data.API_URL
import com.autsing.miga.presentation.data.Auth
import com.autsing.miga.presentation.data.DEVICE_URL
import com.autsing.miga.presentation.data.Device
import com.autsing.miga.presentation.data.DeviceInfoV2
import com.autsing.miga.presentation.data.DevicePropertyValueV2
import com.autsing.miga.presentation.data.GetDeviceInfoRespDataV2
import com.autsing.miga.presentation.data.GetDevicePropertiesReqDataV2
import com.autsing.miga.presentation.data.GetDevicePropertiesRespDataV2
import com.autsing.miga.presentation.data.GetDevicesReqDataV2
import com.autsing.miga.presentation.data.GetDevicesRespDataV2
import com.autsing.miga.presentation.data.GetHomesReqData
import com.autsing.miga.presentation.data.GetHomesRespData
import com.autsing.miga.presentation.data.GetScenesReqData
import com.autsing.miga.presentation.data.GetScenesRespDataV2
import com.autsing.miga.presentation.data.Home
import com.autsing.miga.presentation.data.Scene
import com.autsing.miga.presentation.data.SetDevicePropertiesReqDataV2
import com.autsing.miga.presentation.data.TAG
import com.autsing.miga.presentation.data.USER_AGENT
import com.autsing.miga.presentation.data.defaultJson
import com.autsing.miga.presentation.data.encode
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.cookie
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.parameters
import io.ktor.serialization.kotlinx.json.json
import org.json.JSONObject

private val client: HttpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(
            json = defaultJson,
            contentType = ContentType.Any,
        )
    }
    defaultRequest {
        header("User-Agent", USER_AGENT)
        header("Content-Type", "application/x-www-form-urlencoded")
        header("x-xiaomi-protocal-flag-cli", "PROTOCAL-HTTP2")
        cookie("countryCode", "CN")
        cookie("locale", "zh_CN")
        cookie("timezone_id", "Asia/Shanghai")
        cookie("timezone", "GMT+08:00")
        cookie("is_daylight", "0")
        cookie("dst_offset", "0")
        cookie("channel", "MI_APP_STORE")
    }
    install(Logging) {
        logger = Logger.ANDROID
        level = LogLevel.ALL
    }
}

private suspend fun request(
    uri: String,
    data: String,
    auth: Auth,
): Result<HttpResponse> = runCatching {
    val url = "$API_URL$uri"
    val params = mapOf("data" to data)
    val secret = auth.cookies["ssecurity"] ?: throw Exception("缺少字段 ssecurity")
    val nonce = genNonceV2()
    val signedNonce = genSignedNonceV2(secret, nonce)
    val signature = genSignatureV2(uri, signedNonce, nonce, params)

    val resp = client.submitForm(
        url = url,
        formParameters = parameters {
            append("_nonce", nonce)
            append("signature", signature)
            append("data", data)
        },
    ) {
        cookie("cUserId", auth.cookies["cUserId"] ?: "")
        cookie("yetAnotherServiceToken", auth.cookies["serviceToken"] ?: "")
        cookie("serviceToken", auth.cookies["serviceToken"] ?: "")
        cookie("PassportDeviceId", auth.deviceId)
    }

    resp
}

suspend fun reqGetHomesV2(auth: Auth): Result<List<Home>> = runCatching {
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
    val resp = request(uri, dataJson, auth).getOrThrow()
    val respData = resp.body<GetHomesRespData>()
    val homes = respData.result.homelist

    homes
}

suspend fun reqGetScenesV2(auth: Auth): Result<List<Scene>> = runCatching {
    val homes = reqGetHomesV2(auth).getOrThrow()
    val homeId = homes.first().id
    val uri = "/appgateway/miot/appsceneservice/AppSceneService/GetSimpleSceneList"
    val data = GetScenesReqData(
        home_id = homeId,
    )
    val dataJson = data.encode().getOrThrow()
    val resp = request(uri, dataJson, auth).getOrThrow()
    val respData = resp.body<GetScenesRespDataV2>()
    val scenes = respData.result.manual_scene_info_list

    scenes
}

suspend fun reqGetDevicesV2(auth: Auth): Result<List<Device>> = runCatching {
    val uri = "/v2/home/device_list_page"
    val data = GetDevicesReqDataV2(
        getVirtualModel = true,
        getHuamiDevices = 1,
        get_split_device = true,
        support_smart_home = true,
    )
    val dataJson = data.encode().getOrThrow()
    val resp = request(uri, dataJson, auth).getOrThrow()
    val respData = resp.body<GetDevicesRespDataV2>()
    val devices = respData.result.list

    devices
}

suspend fun reqGetDeviceInfoV2(specType: String): Result<DeviceInfoV2> = runCatching {
    val resp = client.get(DEVICE_URL) {
        parameter("type", specType)
    }
    val respData = resp.body<GetDeviceInfoRespDataV2>()
    val info = DeviceInfoV2.from(respData)

    info
}

suspend fun reqGetDevicePropertiesV2(
    device: Device,
    info: DeviceInfoV2,
    auth: Auth,
): Result<Map<DeviceInfoV2.Property, DevicePropertyValueV2>> = runCatching {
    val uri = "/miotspec/prop/get"
    val data = GetDevicePropertiesReqDataV2(
        params = info.properties.map {
            GetDevicePropertiesReqDataV2.Params(
                did = device.did,
                siid = it.method.siid,
                piid = it.method.piid,
            )
        }
    )
    val dataJson = data.encode().getOrThrow()
    val resp = request(uri, dataJson, auth).getOrThrow()
    val respData = resp.body<GetDevicePropertiesRespDataV2>()
    val properties = info.properties
        .withIndex()
        .associate { (i, it) -> it to respData.result[i].value }

    properties
}

suspend fun reqSetDevicePropertyV2(
    device: Device,
    property: DeviceInfoV2.Property,
    value: DevicePropertyValueV2,
    auth: Auth,
): Result<Unit> = runCatching {
    val uri = "/miotspec/prop/set"
    val data = SetDevicePropertiesReqDataV2(
        params = listOf(
            SetDevicePropertiesReqDataV2.Params(
                did = device.did,
                siid = property.method.siid,
                piid = property.method.piid,
                value = value,
            )
        )
    )
    val dataJson = data.encode().getOrThrow()
    val resp = request(uri, dataJson, auth).getOrThrow()
        .also { Log.d(TAG, "reqSetDevicePropertyV2: ${JSONObject(it.bodyAsText()).toString(4)}") }
}
