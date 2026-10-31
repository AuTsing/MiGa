package com.autsing.miga.presentation.api

import com.autsing.miga.presentation.data.API_URL
import com.autsing.miga.presentation.data.Auth
import com.autsing.miga.presentation.data.GetDeviceBaikeRespDataV2
import com.autsing.miga.presentation.data.PRODUCT_URL
import com.autsing.miga.presentation.data.RunSceneReqData
import com.autsing.miga.presentation.data.RunSceneRespData
import com.autsing.miga.presentation.data.Scene
import com.autsing.miga.presentation.data.USER_AGENT
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

suspend fun reqGetDeviceIconUrl(model: String): Result<String> = runCatching {
    val resp = client.get("${PRODUCT_URL}?model=$model")
    val respData = resp.body<GetDeviceBaikeRespDataV2>()
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
