package com.autsing.miga.presentation.repository

import android.annotation.SuppressLint
import android.content.Context
import com.autsing.miga.presentation.data.getAuth
import com.autsing.miga.presentation.data.getDeviceId
import com.autsing.miga.presentation.data.requestGetLocation
import com.autsing.miga.presentation.data.requestGetLoginIndex
import com.autsing.miga.presentation.data.requestGetLoginLpResponse
import com.autsing.miga.presentation.data.requestGetLoginUrl
import com.autsing.miga.presentation.data.requestGetProfile
import com.autsing.miga.presentation.data.requestGetServiceToken
import com.autsing.miga.presentation.data.setAuth
import com.autsing.miga.presentation.model.Auth
import com.autsing.miga.presentation.model.LoginUrlResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

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
            var auth = context.getAuth().getOrThrow() ?: throw NullPointerException("Auth is null")
            val profile = requestGetProfile(auth).getOrThrow()

            if (profile.code != 0) {
                val location = requestGetLocation(auth).getOrThrow()
                val serviceToken = requestGetServiceToken(location.location).getOrThrow()
                auth = auth.copy(serviceToken = serviceToken, ssecurity = location.ssecurity)
                context.setAuth(auth).getOrThrow()
            }

            this@AuthRepository.auth.value = auth
        }
    }

    suspend fun getLoginUrl(): Result<LoginUrlResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val loginIndexResponse = requestGetLoginIndex().getOrThrow()
            val loginUrlResponse = requestGetLoginUrl(loginIndexResponse).getOrThrow()

            loginUrlResponse
        }
    }

    suspend fun loadRemoteAuth(resp: LoginUrlResponse): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val loginLpResponse = requestGetLoginLpResponse(resp).getOrThrow()
            val serviceToken = requestGetServiceToken(loginLpResponse.location).getOrThrow()
            val deviceId = getDeviceId()

            val auth = with(loginLpResponse) {
                Auth(
                    deviceId = deviceId,
                    serviceToken = serviceToken,
                    userId = userId,
                    cUserId = cUserId,
                    nonce = nonce,
                    ssecurity = ssecurity,
                    psecurity = psecurity,
                    passToken = passToken,
                )
            }

            context.setAuth(auth).getOrThrow()
            this@AuthRepository.auth.value = auth
        }
    }

    fun getAuth(): Result<Auth> = runCatching {
        auth.value ?: throw NullPointerException("Auth is null")
    }

    suspend fun waitAuth(): Result<Auth> = runCatching { auth.filterNotNull().first() }

    fun removeAuthSoft(): Result<Unit> = runCatching {
        auth.value = null
    }

    suspend fun removeAuthHard(): Result<Unit> = runCatching {
        context.setAuth(null).getOrThrow()
        auth.value = null
    }
}
