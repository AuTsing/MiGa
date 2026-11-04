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

    suspend fun getLoginUrl(): Result<LoginUrlRespData> = withContext(Dispatchers.IO) {
        runCatching {
            val serviceLoginRespData = reqServiceLogin().getOrThrow()
            val loginUrlRespData = reqLoginUrl(serviceLoginRespData.location).getOrThrow()

            loginUrlRespData
        }
    }

    suspend fun login(url: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val loginLpRespData = reqLoginLp(url).getOrThrow()
            val auth = reqLoginLocation(loginLpRespData.location).getOrThrow()

            context.setAuth(auth).getOrThrow()
            this@AuthRepository.auth.value = auth
        }
    }

    suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            context.setAuth(null).getOrThrow()
            this@AuthRepository.auth.value = null
        }
    }

    fun observeAuth(): StateFlow<Auth?> = auth.asStateFlow()

    suspend fun getValidAuth(): Result<Auth> = withContext(Dispatchers.IO) {
        runCatching {
            auth.value?.let { return@runCatching it }

            val auth = context.getAuth().getOrThrow() ?: throw Exception("未登录")
            val respData = reqServiceLogin(auth).getOrThrow()
            val newAuth = reqLoginLocation(respData.location).getOrThrow()

            context.setAuth(newAuth).getOrThrow()
            this@AuthRepository.auth.value = newAuth

            newAuth
        }
    }
}
