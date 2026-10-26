package com.autsing.miga.presentation.repository

import android.annotation.SuppressLint
import android.content.Context
import com.autsing.miga.presentation.api.LoginUrlRespData
import com.autsing.miga.presentation.api.reqLoginIndex
import com.autsing.miga.presentation.api.reqLoginLocation
import com.autsing.miga.presentation.api.reqLoginLp
import com.autsing.miga.presentation.api.reqLoginUrl
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

    suspend fun loadLocalAuth(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            var auth = context.getAuth().getOrThrow() ?: throw Exception("Auth is null")
//            val profile = requestGetProfile(auth).getOrThrow()
//
//            if (profile.code != 0) {
//                val location = requestGetLocation(auth).getOrThrow()
//                val serviceToken = requestGetServiceToken(location.location).getOrThrow()
//                auth = auth.copy(serviceToken = serviceToken, ssecurity = location.ssecurity)
//                context.setAuth(auth).getOrThrow()
//            }

            this@AuthRepository.auth.value = auth
        }
    }

    suspend fun getLoginUrl(
        deviceId: String,
    ): Result<LoginUrlRespData> = withContext(Dispatchers.IO) {
        runCatching {
            val loginIndexRespData = reqLoginIndex(deviceId).getOrThrow()
            val loginUrlRespData = reqLoginUrl(deviceId, loginIndexRespData).getOrThrow()

            loginUrlRespData
        }
    }

    suspend fun loadRemoteAuth(
        deviceId: String,
        respData: LoginUrlRespData,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val loginLpRespData = reqLoginLp(deviceId, respData).getOrThrow()
            val serviceToken = reqLoginLocation(deviceId, loginLpRespData.location).getOrThrow()

            val auth = with(loginLpRespData) {
                Auth(
                    deviceId = deviceId,
                    serviceToken = serviceToken,
                    cookies = mapOf(
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

    fun getAuth(): Result<com.autsing.miga.presentation.model.Auth> = runCatching {
        TODO("getAuth")
    }

    suspend fun waitAuth(): Result<com.autsing.miga.presentation.model.Auth> = runCatching {
        TODO("getAuth")
    }

    fun removeAuthSoft(): Result<Unit> = runCatching {
        auth.value = null
    }

    suspend fun removeAuthHard(): Result<Unit> = runCatching {
        context.setAuth(null).getOrThrow()
        auth.value = null
    }
}
