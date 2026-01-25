package com.autsing.miga.presentation.helper

import com.autsing.miga.presentation.helper.Constants.MSG_URL
import com.autsing.miga.presentation.helper.Constants.QR_URL
import com.autsing.miga.presentation.helper.Constants.USER_AGENT
import com.autsing.miga.presentation.model.Auth
import com.autsing.miga.presentation.model.GetLocationResponse
import com.autsing.miga.presentation.model.LoginIndexResponse
import com.autsing.miga.presentation.model.LoginLpResponse
import com.autsing.miga.presentation.model.LoginUrlResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

class LoginHelper(
    val serdeHelper: SerdeHelper,
) {

    companion object {
        lateinit var instance: LoginHelper
    }

    private val deviceId: String = (('0'..'9') + ('a'..'z') + ('A'..'Z'))
        .shuffled(Random)
        .take(16)
        .joinToString("")
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("User-Agent", USER_AGENT)
                .addHeader("Cookie", "deviceId=${deviceId}; sdkVersion=3.4.1")
                .build()
            chain.proceed(request)
        }
        .connectTimeout(Duration.ofSeconds(60L))
        .callTimeout(Duration.ofSeconds(60L))
        .readTimeout(Duration.ofSeconds(60L))
        .writeTimeout(Duration.ofSeconds(60L))
        .build()

    fun getDeviceId(): String {
        return deviceId
    }

    suspend fun getLoginIndex(): Result<LoginIndexResponse> = withContext(Dispatchers.IO) {
        var maybeResponse: Response? = null

        runCatching {
            val loginIndexRequest = Request.Builder()
                .url(MSG_URL)
                .build()
            val response = okHttpClient.newCall(loginIndexRequest)
                .execute()
                .also { maybeResponse = it }
            val loginIndexJson = response.body.string().substring(11)
            val loginIndexResponse = serdeHelper.decode<LoginIndexResponse>(loginIndexJson)
                .getOrThrow()

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
            val qrUrl = "$QR_URL?$paramsString"
            val qrRequest = Request.Builder()
                .url(qrUrl)
                .build()
            val response = okHttpClient.newCall(qrRequest)
                .execute()
                .also { maybeResponse = it }
            val loginUrlJson = response.body.string().substring(11)
            val loginUrlResponse = serdeHelper.decode<LoginUrlResponse>(loginUrlJson).getOrThrow()

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
                .addHeader("Connection", "keep-alive")
                .build()
            val response = okHttpClient.newCall(lpRequest)
                .execute()
                .also { maybeResponse = it }
            val loginLpJson = response.body.string().substring(11)
            val loginLpResponse = serdeHelper.decode<LoginLpResponse>(loginLpJson).getOrThrow()

            return@runCatching loginLpResponse
        }.also {
            maybeResponse?.close()
        }
    }

    suspend fun getServiceToken(location: String): Result<String> = withContext(Dispatchers.IO) {
        var maybeResponse: Response? = null

        runCatching {
            val request = Request.Builder()
                .url(location)
                .build()
            val response = okHttpClient.newCall(request)
                .execute()
                .also { maybeResponse = it }
            val headers = response.headers("Set-Cookie")
            val cookies = headers.mapNotNull { Cookie.parse(request.url, it) }
            val serviceToken = cookies.find { it.name == "serviceToken" }?.value
                ?: throw Exception("无法获取serviceToken")

            return@runCatching serviceToken
        }.also {
            maybeResponse?.close()
        }
    }

    suspend fun getLocation(auth: Auth): Result<GetLocationResponse> = withContext(Dispatchers.IO) {
        var maybeResponse: Response? = null

        runCatching {
            val cookie = auth.toCookie()
            val request = Request.Builder()
                .url(MSG_URL)
                .addHeader("Cookie", cookie)
                .build()
            val response = okHttpClient.newCall(request)
                .execute()
                .also { maybeResponse = it }
            val getLocationResponseJson = response.body.string().substring(11)
            val getLocationResponse = serdeHelper
                .decode<GetLocationResponse>(getLocationResponseJson)
                .getOrThrow()

            return@runCatching getLocationResponse
        }.also {
            maybeResponse?.close()
        }
    }
}
