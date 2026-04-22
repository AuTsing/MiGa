package com.autsing.miga.presentation.repository

import android.annotation.SuppressLint
import android.content.Context
import com.autsing.miga.presentation.data.getAuth
import com.autsing.miga.presentation.data.setAuth
import com.autsing.miga.presentation.helper.ApiHelper
import com.autsing.miga.presentation.helper.LoginHelper
import com.autsing.miga.presentation.model.Auth
import com.autsing.miga.presentation.model.LoginUrlResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class AuthRepository(
    private val context: Context,
    private val apiHelper: ApiHelper,
    private val loginHelper: LoginHelper,
) {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var instance: AuthRepository
    }

    private val auth: MutableStateFlow<Auth?> = MutableStateFlow(null)

    suspend fun loadLocalAuth(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            var auth = context.getAuth().getOrThrow() ?: throw NullPointerException("Auth is null")
            val profile = apiHelper.getProfile(auth).getOrThrow()

            if (profile.code != 0) {
                val location = loginHelper.getLocation(auth).getOrThrow()
                val serviceToken = loginHelper.getServiceToken(location.location).getOrThrow()
                auth = auth.copy(serviceToken = serviceToken, ssecurity = location.ssecurity)
                context.setAuth(auth).getOrThrow()
            }

            this@AuthRepository.auth.value = auth
        }
    }

    suspend fun getLoginUrl(): Result<LoginUrlResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val loginIndexResponse = loginHelper.getLoginIndex().getOrThrow()
            val loginUrlResponse = loginHelper.getLoginUrl(loginIndexResponse).getOrThrow()

            loginUrlResponse
        }
    }

    suspend fun loadRemoteAuth(resp: LoginUrlResponse): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val loginLpResponse = LoginHelper.instance
                .getLoginLpResponse(resp)
                .getOrThrow()
            val serviceToken = LoginHelper.instance
                .getServiceToken(loginLpResponse.location)
                .getOrThrow()
            val deviceId = LoginHelper.instance.getDeviceId()

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
}
