package com.autsing.miga.presentation.repository

import android.annotation.SuppressLint
import android.content.Context
import com.autsing.miga.presentation.api.LoginUrlRespData
import com.autsing.miga.presentation.api.reqLoginLocation
import com.autsing.miga.presentation.api.reqLoginLp
import com.autsing.miga.presentation.api.reqLoginUrl
import com.autsing.miga.presentation.api.reqServiceLogin
import com.autsing.miga.presentation.data.getAuth
import com.autsing.miga.presentation.data.setAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
data class Auth(
    val deviceId: String,
    val serviceToken: String,
    val cookies: Map<String, String>,
)

class AuthRepository(
    private val context: Context,
) {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var instance: AuthRepository
    }

    private val auth: MutableStateFlow<Auth?> = MutableStateFlow(null)

    suspend fun getLoginUrl(
        deviceId: String,
    ): Result<LoginUrlRespData> = withContext(Dispatchers.IO) {
        runCatching {
            val loginUrlRespData = reqLoginUrl(deviceId).getOrThrow()

            loginUrlRespData
        }
    }

    suspend fun login(
        deviceId: String,
        respData: LoginUrlRespData,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val loginLpRespData = reqLoginLp(deviceId, respData).getOrThrow()
            val location = loginLpRespData.location
            val cookies = reqLoginLocation(location, deviceId).getOrThrow()
            val serviceToken = cookies["serviceToken"] ?: throw Exception("获取 serviceToken 失败")

            val auth = with(loginLpRespData) {
                Auth(
                    deviceId = deviceId,
                    serviceToken = serviceToken,
                    cookies = mapOf(
                        "deviceId" to deviceId,
                        "userId" to userId.toString(),
                        "cUserId" to cUserId,
                        "nonce" to nonce.toString(),
                        "ssecurity" to ssecurity,
                        "psecurity" to psecurity,
                        "passToken" to passToken,
                    ),
                )
            }

            context.setAuth(auth).getOrThrow()
            this@AuthRepository.auth.value = auth
        }
    }

    suspend fun getAuth(): Result<Auth> = runCatching {
        var auth = context.getAuth().getOrThrow() ?: throw Exception("未登录")

        val serviceLoginRespData = reqServiceLogin(auth).getOrThrow()
        val location = serviceLoginRespData.location
        val cookies = reqLoginLocation(location, auth).getOrThrow()

        auth = auth.copy(
            cookies = auth.cookies.toMutableMap().apply { cookies.forEach { (k, v) -> set(k, v) } },
        )

        auth
    }
}
