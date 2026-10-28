package com.autsing.miga.presentation.api

import android.util.Log
import com.autsing.miga.presentation.data.API_URL
import com.autsing.miga.presentation.data.Auth
import com.autsing.miga.presentation.data.DEVICE_URL
import com.autsing.miga.presentation.data.Device
import com.autsing.miga.presentation.data.DeviceInfo
import com.autsing.miga.presentation.data.DevicePropertyValue
import com.autsing.miga.presentation.data.GetDeviceBaikeRespData
import com.autsing.miga.presentation.data.GetDeviceInfoRespData
import com.autsing.miga.presentation.data.GetDevicePropertiesReqData
import com.autsing.miga.presentation.data.GetDevicePropertiesRespData
import com.autsing.miga.presentation.data.GetDevicesReqData
import com.autsing.miga.presentation.data.GetDevicesRespData
import com.autsing.miga.presentation.data.GetHomesReqData
import com.autsing.miga.presentation.data.GetHomesRespData
import com.autsing.miga.presentation.data.GetScenesReqData
import com.autsing.miga.presentation.data.GetScenesRespData
import com.autsing.miga.presentation.data.Home
import com.autsing.miga.presentation.data.PRODUCT_URL
import com.autsing.miga.presentation.data.RunSceneReqData
import com.autsing.miga.presentation.data.RunSceneRespData
import com.autsing.miga.presentation.data.Scene
import com.autsing.miga.presentation.data.SetDevicePropertiesReqData
import com.autsing.miga.presentation.data.SetDevicePropertiesRespData
import com.autsing.miga.presentation.data.TAG
import com.autsing.miga.presentation.data.USER_AGENT
import com.autsing.miga.presentation.data.decode
import com.autsing.miga.presentation.data.defaultJson
import com.autsing.miga.presentation.data.encode
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

suspend fun reqGetDeviceIconUrl(model: String): Result<String> = runCatching {
    val resp = client.get("${PRODUCT_URL}?model=$model")
    val respData = resp.body<GetDeviceBaikeRespData>()
    val realIcon = respData.data.realIcon

    realIcon
}

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
