package com.autsing.miga.presentation.api

import com.autsing.miga.presentation.data.API_URL
import com.autsing.miga.presentation.data.USER_AGENT
import com.autsing.miga.presentation.data.defaultJson
import com.autsing.miga.presentation.data.encode
import com.autsing.miga.presentation.model.GetHomesData
import com.autsing.miga.presentation.model.GetScenesData
import com.autsing.miga.presentation.repository.Auth
import com.autsing.miga.presentation.repository.Home
import com.autsing.miga.presentation.repository.Scene
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.cookie
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable

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

@Serializable
data class GetHomesRespData(
    val code: Int,
    val message: String,
    val result: Result,
) {

    @Serializable
    data class Result(
        val homelist: List<Home>,
    )
}

suspend fun reqGetHomes(auth: Auth): Result<List<Home>> = runCatching {
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
    val getHomesRespData = resp.body<GetHomesRespData>()
    val homes = getHomesRespData.result.homelist

    homes
}

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
    val data = GetScenesData(
        home_id = homeId,
    )
    val dataJson = data.encode().getOrThrow()
    val resp = post(auth, uri, dataJson).getOrThrow()
    val getScenesResp = resp.body<GetScenesRespData>()
    val scenes = getScenesResp.result.scene_info_list.filter { it.icon_url.isNotBlank() }

    scenes
}
