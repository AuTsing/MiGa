package com.autsing.miga.presentation.api

import com.autsing.miga.presentation.data.Auth
import com.autsing.miga.presentation.data.LOGIN_URL
import com.autsing.miga.presentation.data.LoginLpRespData
import com.autsing.miga.presentation.data.LoginUrlRespData
import com.autsing.miga.presentation.data.SERVICE_LOGIN_URL
import com.autsing.miga.presentation.data.ServiceLoginRespData
import com.autsing.miga.presentation.data.decode
import com.autsing.miga.presentation.data.defaultJson
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.URLBuilder
import io.ktor.http.setCookie
import io.ktor.serialization.kotlinx.json.json
import java.time.Instant.now

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
}
var passO: String = ""
var userAgent: String = ""
var deviceId: String = ""
var cookies: MutableMap<String, String> = mutableMapOf()

suspend fun reqServiceLogin(): Result<ServiceLoginRespData> = runCatching {
    passO = genPassO()
    userAgent = genUserAgent(passO)
    deviceId = genDeviceId()
    cookies = mutableMapOf()

    val cookie = mapOf(
        "deviceId" to deviceId,
        "pass_o" to passO,
        "uLocale" to "zh_CN",
    ).genCookie()

    val resp = client.get(SERVICE_LOGIN_URL) {
        header("User-Agent", userAgent)
        header("Connection", "keep-alive")
        header("Content-Type", "application/x-www-form-urlencoded")
        header("Cookie", cookie)
    }
    val respData = resp.bodyAsText().decode<ServiceLoginRespData>().getOrThrow()

    respData
}

suspend fun reqServiceLogin(auth: Auth): Result<Map<String, String>> = runCatching {
    val cookie = mapOf(
        "deviceId" to auth.deviceId,
        "pass_o" to auth.passO,
        "passToken" to auth.cookies["passToken"],
        "userId" to auth.cookies["userId"],
        "cUserId" to auth.cookies["cUserId"],
        "uLocale" to "zh_CN",
    ).genCookie()

    val resp = client.get(SERVICE_LOGIN_URL) {
        header("User-Agent", auth.userAgent)
        header("Connection", "keep-alive")
        header("Content-Type", "application/x-www-form-urlencoded")
        header("Cookie", cookie)
    }
    val respData = resp.bodyAsText().decode<ServiceLoginRespData>().getOrThrow()

    if (respData.code != 0) {
        throw Exception(respData.description)
    }

//    val loginLocationCookies = reqLoginLocation(respData.location).getOrThrow()

    cookies
}

suspend fun reqLoginUrl(url: String): Result<LoginUrlRespData> = runCatching {
    val locationUrl = URLBuilder(url).build()
    val loginUrl = URLBuilder(LOGIN_URL).apply {
        parameters.appendAll(locationUrl.parameters)
        parameters.append("theme", "")
        parameters.append("bizDeviceType", "")
        parameters.append("_hasLogo", "false")
        parameters.append("_qrsize", "240")
        parameters.append("_dc", now().toEpochMilli().toString())
    }
    val resp = client.get(loginUrl.toString()) {
        header("User-Agent", userAgent)
        header("Connection", "keep-alive")
        header("Content-Type", "application/x-www-form-urlencoded")
    }
    val respData = resp.bodyAsText().decode<LoginUrlRespData>().getOrThrow()

    respData
}

suspend fun reqLoginLp(url: String): Result<LoginLpRespData> = runCatching {
    val resp = client.get(url) {
        header("User-Agent", userAgent)
        header("Connection", "keep-alive")
        header("Content-Type", "application/x-www-form-urlencoded")
    }
    resp.setCookie().forEach { cookies[it.name] = it.value }
    val respData = resp.bodyAsText().decode<LoginLpRespData>().getOrThrow()
    cookies["psecurity"] = respData.psecurity
    cookies["nonce"] = respData.nonce.toString()
    cookies["ssecurity"] = respData.ssecurity
    cookies["passToken"] = respData.passToken
    cookies["userId"] = respData.userId.toString()
    cookies["cUserId"] = respData.cUserId

    respData
}

suspend fun reqLoginLocation(url: String): Result<Auth> = runCatching {
    val cookie = cookies.genCookie()

    val resp = client.get(url) {
        header("User-Agent", userAgent)
        header("Connection", "keep-alive")
        header("Content-Type", "application/x-www-form-urlencoded")
        header("Cookie", cookie)
    }
    resp.setCookie().forEach { cookies[it.name] = it.value }
    val respData = resp.bodyAsText()

    if (respData != "ok") {
        throw Exception(respData)
    }

    val auth = Auth(
        passO = passO,
        userAgent = userAgent,
        deviceId = deviceId,
        cookies = cookies,
    )

    auth
}
