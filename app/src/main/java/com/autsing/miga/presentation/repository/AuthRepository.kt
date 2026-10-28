package com.autsing.miga.presentation.repository

import android.annotation.SuppressLint
import android.content.Context
import com.autsing.miga.presentation.api.reqLoginLocation
import com.autsing.miga.presentation.api.reqLoginLp
import com.autsing.miga.presentation.api.reqLoginUrl
import com.autsing.miga.presentation.api.reqServiceLogin
import com.autsing.miga.presentation.data.Auth
import com.autsing.miga.presentation.data.LoginUrlRespData
import com.autsing.miga.presentation.data.getAuth
import com.autsing.miga.presentation.data.setAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class AuthRepository(
    private val context: Context,
) {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var instance: AuthRepository
    }

    private val auth: MutableStateFlow<Auth?> = MutableStateFlow(null)

    suspend fun getLoginUrl(deviceId: String): Result<LoginUrlRespData> = runCatching {
        val loginUrlRespData = reqLoginUrl(deviceId).getOrThrow()

        loginUrlRespData
    }

    suspend fun login(deviceId: String, respData: LoginUrlRespData): Result<Unit> = runCatching {
        val loginLpRespData = reqLoginLp(deviceId, respData).getOrThrow()
        val location = loginLpRespData.location
        val cookies = reqLoginLocation(location, deviceId).getOrThrow()
        val serviceToken = cookies["serviceToken"] ?: throw Exception("获取 serviceToken 失败")

        val auth = with(loginLpRespData) {
            Auth(
                deviceId = deviceId,
                cookies = mapOf(
                    "deviceId" to deviceId,
                    "userId" to userId.toString(),
                    "cUserId" to cUserId,
                    "nonce" to nonce.toString(),
                    "ssecurity" to ssecurity,
                    "psecurity" to psecurity,
                    "passToken" to passToken,
                    "serviceToken" to serviceToken,
                ),
            )
        }

        context.setAuth(auth).getOrThrow()
        this@AuthRepository.auth.value = auth
    }

    suspend fun logout(): Result<Unit> = runCatching {
        context.setAuth(null).getOrThrow()
        this@AuthRepository.auth.value = null
    }

    fun observeAuth(): StateFlow<Auth?> = auth.asStateFlow()

    suspend fun getValidAuth(): Result<Auth> = withContext(Dispatchers.IO) {
        runCatching {
            auth.value?.let { return@runCatching it }

            val auth = context.getAuth().getOrThrow() ?: throw Exception("未登录")
            val serviceLoginRespData = reqServiceLogin(auth).getOrThrow()
            val location = serviceLoginRespData.location
            val cookies = reqLoginLocation(location, auth).getOrThrow()

            val newAuth = auth.copy(
                cookies = auth.cookies.toMutableMap().apply { putAll(cookies) },
            )
            context.setAuth(newAuth).getOrThrow()
            this@AuthRepository.auth.value = newAuth

            newAuth
        }
    }
}
