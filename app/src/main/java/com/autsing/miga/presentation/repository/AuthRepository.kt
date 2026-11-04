package com.autsing.miga.presentation.repository

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.autsing.miga.presentation.api.reqLoginLocation
import com.autsing.miga.presentation.api.reqLoginLp
import com.autsing.miga.presentation.api.reqLoginUrl
import com.autsing.miga.presentation.api.reqServiceLogin
import com.autsing.miga.presentation.data.Auth
import com.autsing.miga.presentation.data.LoginUrlRespData
import com.autsing.miga.presentation.data.encode
import com.autsing.miga.presentation.data.getAuth
import com.autsing.miga.presentation.data.setAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject

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

            Log.d("TAG", "login: ${JSONObject(auth.encode().getOrThrow()).toString(4)}")
            Unit
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
            throw Exception("未登录")
            Log.d("TAG", "getValidAuth: ${JSONObject(auth.encode().getOrThrow()).toString(4)}")
//            this@AuthRepository.auth.value = auth
//            return@runCatching auth

            val cookies = reqServiceLogin(auth).getOrThrow()
            Log.d("TAG", "getValidAuth: $cookies")
            throw Exception("未登录")
//            val newAuth = auth.copy(
//                cookies = auth.cookies.toMutableMap().apply { putAll(newCookies) },
//            )
//            Log.d("TAG", "newAuth: $newAuth")
//            context.setAuth(newAuth).getOrThrow()
//            this@AuthRepository.auth.value = newAuth

//            newAuth
        }
    }
}
