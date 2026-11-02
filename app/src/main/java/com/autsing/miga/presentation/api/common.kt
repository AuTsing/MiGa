package com.autsing.miga.presentation.api

import com.autsing.miga.presentation.data.GetDeviceBaikeRespDataV2
import com.autsing.miga.presentation.data.PRODUCT_URL
import com.autsing.miga.presentation.data.USER_AGENT
import com.autsing.miga.presentation.data.defaultJson
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.cookie
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.ContentType
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

suspend fun reqGetDeviceIconUrl(model: String): Result<String> = runCatching {
    val resp = client.get("${PRODUCT_URL}?model=$model")
    val respData = resp.body<GetDeviceBaikeRespDataV2>()
    val realIcon = respData.data.realIcon

    realIcon
}
