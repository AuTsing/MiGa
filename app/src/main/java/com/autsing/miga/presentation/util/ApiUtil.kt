package com.autsing.miga.presentation.util

import com.autsing.miga.presentation.model.Auth
import com.autsing.miga.presentation.model.Device
import com.autsing.miga.presentation.model.GetDevicesData
import com.autsing.miga.presentation.model.GetDevicesResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class ApiUtil {

    companion object {
        lateinit var instance: ApiUtil
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("User-Agent", Constants.USER_AGENT)
                .addHeader("x-xiaomi-protocal-flag-cli", "PROTOCAL-HTTP2")
                .build()
            chain.proceed(request)
        }
        .build()

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

    fun generateSignature(
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

    suspend fun getDevices(auth: Auth): Result<List<Device>> = withContext(Dispatchers.IO) {
        var maybeResponse: Response? = null

        runCatching {
            val uri = "/home/device_list"
            val data = GetDevicesData(
                getVirtualModel = false,
                getHuamiDevices = 0,
            )
            val dataJson = Json.encodeToString(data)
            val nonce = generateNonce()
            val signedNonce = generateSignedNonce(auth.ssecurity, nonce)
            val signature = generateSignature(uri, signedNonce, nonce, dataJson)

            val postData = FormBody.Builder()
                .add("_nonce", nonce)
                .add("data", dataJson)
                .add("signature", signature)
                .build()
            val request = Request.Builder()
                .url(Constants.API_URL + uri)
                .post(postData)
                .addHeader(
                    "Cookie",
                    "PassportDeviceId=${auth.deviceId};userId=${auth.userId};serviceToken=${auth.serviceToken};",
                )
                .build()
            val response = okHttpClient.newCall(request).execute().also { maybeResponse = it }
            val getDevicesJson = response.body?.string() ?: ""
            val getDevicesResponse = Json.decodeFromString<GetDevicesResponse>(getDevicesJson)

            return@runCatching getDevicesResponse.result.list
        }.also {
            maybeResponse?.close()
        }
    }
}
