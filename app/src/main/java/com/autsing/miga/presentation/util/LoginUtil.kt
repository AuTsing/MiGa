package com.autsing.miga.presentation.util

import com.autsing.miga.presentation.model.LoginIndexResponse
import com.autsing.miga.presentation.model.LoginLpResponse
import com.autsing.miga.presentation.model.LoginUrlResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.Cookie
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder
import java.time.Duration
import java.time.Instant
import kotlin.random.Random

class LoginUtil() {

    companion object {
        lateinit var instance: LoginUtil
    }

    private val deviceId: String = (('0'..'9') + ('a'..'z') + ('A'..'Z'))
        .shuffled(Random)
        .take(16)
        .joinToString("")
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(Duration.ofSeconds(60L))
        .callTimeout(Duration.ofSeconds(60L))
        .build()

    fun getDeviceId(): String {
        return deviceId
    }

    suspend fun getLoginIndex(): Result<LoginIndexResponse> = withContext(Dispatchers.IO) {
        var maybeResponse: Response? = null

        runCatching {
            val loginIndexRequest = Request.Builder()
                .url(Constants.MSG_URL)
                .header("User-Agent", Constants.USER_AGENT)
                .header("Cookie", "deviceId=${deviceId}; sdkVersion=3.4.1")
                .build()
            val response = okHttpClient.newCall(loginIndexRequest)
                .execute()
                .also { maybeResponse = it }
            val loginIndexJson = response.body?.string()?.substring(11) ?: ""
            val loginIndexResponse = Json.decodeFromString<LoginIndexResponse>(loginIndexJson)

            return@runCatching loginIndexResponse
        }.also {
            maybeResponse?.close()
        }
    }

    suspend fun getLoginUrl(
        loginIndexResponse: LoginIndexResponse,
    ): Result<LoginUrlResponse> = withContext(Dispatchers.IO) {
        var maybeResponse: Response? = null

        runCatching {
            val queryMap = URI(loginIndexResponse.location).query
                .split("&")
                .map { it.split("=") }
                .associate { (k, v) -> k to URLDecoder.decode(v, "UTF-8") }
            val params = mapOf(
                "_qrsize" to "240",
                "qs" to loginIndexResponse.qs,
                "bizDeviceType" to "",
                "callback" to loginIndexResponse.callback,
                "_json" to "true",
                "theme" to "",
                "sid" to "xiaomiio",
                "needTheme" to "false",
                "showActiveX" to "false",
                "serviceParam" to queryMap.getValue("serviceParam"),
                "_local" to "zh_CN",
                "_sign" to loginIndexResponse._sign,
                "_dc" to (Instant.now().toEpochMilli()).toString(),
            )
            val paramsString = params.map { (k, v) ->
                "${URLEncoder.encode(k, "UTF-8")}=${URLEncoder.encode(v, "UTF-8")}"
            }.joinToString("&")
            val qrUrl = Constants.QR_URL + "?" + paramsString
            val qrRequest = Request.Builder()
                .url(qrUrl)
                .header("User-Agent", Constants.USER_AGENT)
                .header("Cookie", "deviceId=${deviceId}; sdkVersion=3.4.1")
                .build()
            val response = okHttpClient.newCall(qrRequest)
                .execute()
                .also { maybeResponse = it }
            val loginUrlJson = response.body?.string()?.substring(11) ?: ""
            val loginUrlResponse = Json.decodeFromString<LoginUrlResponse>(loginUrlJson)

            return@runCatching loginUrlResponse
        }.also {
            maybeResponse?.close()
        }
    }

    suspend fun getLoginLpResponse(
        loginUrlResponse: LoginUrlResponse,
    ): Result<LoginLpResponse> = withContext(Dispatchers.IO) {
        var maybeResponse: Response? = null

        runCatching {
            val lpRequest = Request.Builder()
                .url(loginUrlResponse.lp)
                .header("User-Agent", Constants.USER_AGENT)
                .header("Cookie", "deviceId=${deviceId}; sdkVersion=3.4.1")
                .header("Connection", "keep-alive")
                .build()
            val response = okHttpClient.newCall(lpRequest)
                .execute()
                .also { maybeResponse = it }
            val loginLpJson = response.body?.string()?.substring(11) ?: ""
            val loginLpResponse = Json.decodeFromString<LoginLpResponse>(loginLpJson)

            return@runCatching loginLpResponse
        }.also {
            maybeResponse?.close()
        }
    }

    suspend fun getLoginServiceToken(
        loginLpResponse: LoginLpResponse,
    ): Result<String> = withContext(Dispatchers.IO) {
        var maybeResponse: Response? = null

        runCatching {
            val lpRequest = Request.Builder()
                .url(loginLpResponse.location)
                .header("User-Agent", Constants.USER_AGENT)
                .header("Cookie", "deviceId=${deviceId}; sdkVersion=3.4.1")
                .build()
            val response = okHttpClient.newCall(lpRequest)
                .execute()
                .also { maybeResponse = it }
            val headers = response.headers("Set-Cookie")
            val cookies = headers.mapNotNull { Cookie.parse(lpRequest.url, it) }
            val serviceToken = cookies.find { it.name == "serviceToken" }?.value
                ?: throw Exception("无法获取serviceToken")

            return@runCatching serviceToken
        }.also {
            maybeResponse?.close()
        }
    }
}
