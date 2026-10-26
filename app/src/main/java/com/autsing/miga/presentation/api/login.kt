package com.autsing.miga.presentation.api

import androidx.core.net.toUri
import com.autsing.miga.presentation.data.LOGIN_URL
import com.autsing.miga.presentation.data.SERVICE_LOGIN_URL
import com.autsing.miga.presentation.data.USER_AGENT
import com.autsing.miga.presentation.data.decode
import com.autsing.miga.presentation.data.defaultJson
import com.autsing.miga.presentation.repository.Auth
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.cookie
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.setCookie
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import java.time.Instant

private val client: HttpClient = HttpClient(CIO) {
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
    install(Logging) {
        logger = Logger.ANDROID
        level = LogLevel.ALL
    }
    defaultRequest {
        header("User-Agent", USER_AGENT)
        header("Connection", "keep-alive")
        cookie("uLocale", "zh_CN")
        cookie("sdkVersion", "3.4.1")
    }
}

@Serializable
data class ServiceLoginRespData(
    val qs: String,
    val _sign: String,
    val callback: String,
    val location: String,
)

suspend fun reqServiceLogin(deviceId: String): Result<ServiceLoginRespData> = runCatching {
    val resp = client.get(SERVICE_LOGIN_URL) {
        cookie("deviceId", deviceId)
    }
    val respData = resp.bodyAsText().decode<ServiceLoginRespData>().getOrThrow()

    respData
}

suspend fun reqServiceLogin(auth: Auth): Result<ServiceLoginRespData> = runCatching {
    val resp = client.get(SERVICE_LOGIN_URL) {
        auth.cookies.forEach { (k, v) -> cookie(k, v) }
    }
    val respData = resp.bodyAsText().decode<ServiceLoginRespData>().getOrThrow()

    respData
}

@Serializable
data class LoginUrlRespData(
    val loginUrl: String,
    val lp: String,
)

suspend fun reqLoginUrl(deviceId: String): Result<LoginUrlRespData> = runCatching {
    val serviceLoginRespData = reqServiceLogin(deviceId).getOrThrow()
    val locationUrl = serviceLoginRespData.location.toUri()
    val params = mapOf(
        "_qrsize" to "240",
        "qs" to serviceLoginRespData.qs,
        "bizDeviceType" to "",
        "callback" to serviceLoginRespData.callback,
        "_json" to "true",
        "theme" to "",
        "sid" to "xiaomiio",
        "needTheme" to "false",
        "showActiveX" to "false",
        "serviceParam" to locationUrl.getQueryParameter("serviceParam"),
        "_local" to "zh_CN",
        "_sign" to serviceLoginRespData._sign,
        "_dc" to (Instant.now().toEpochMilli()).toString(),
    )
    val loginUrl = LOGIN_URL.toUri()
        .buildUpon()
        .apply { params.forEach { (k, v) -> appendQueryParameter(k, v) } }
        .build()
    val resp = client.get(loginUrl.toString()) {
        cookie("deviceId", deviceId)
    }
    val respData = resp.bodyAsText().decode<LoginUrlRespData>().getOrThrow()

    respData
}

@Serializable
data class LoginLpRespData(
    val location: String,
    val userId: Long,
    val cUserId: String,
    val nonce: Long,
    val ssecurity: String,
    val psecurity: String,
    val passToken: String,
)

suspend fun reqLoginLp(
    deviceId: String,
    respData: LoginUrlRespData,
): Result<LoginLpRespData> = runCatching {
    val resp = client.get(respData.lp) {
        cookie("deviceId", deviceId)
    }
    val respData = resp.bodyAsText().decode<LoginLpRespData>().getOrThrow()

    respData
}

suspend fun reqLoginLocation(
    location: String,
    deviceId: String,
): Result<Map<String, String>> = runCatching {
    val resp = client.get(location) {
        cookie("deviceId", deviceId)
    }
    val cookies = resp.setCookie().associate { it.name to it.value }

    cookies
}

suspend fun reqLoginLocation(
    location: String,
    auth: Auth,
): Result<Map<String, String>> = runCatching {
    val resp = client.get(location) {
        auth.cookies.forEach { (k, v) -> cookie(k, v) }
    }
    val cookies = resp.setCookie().associate { it.name to it.value }

    cookies
}
