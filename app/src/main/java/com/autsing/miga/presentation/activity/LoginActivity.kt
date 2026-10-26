package com.autsing.miga.presentation.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.lifecycleScope
import com.autsing.miga.presentation.api.genDeviceId
import com.autsing.miga.presentation.repository.AuthRepository
import com.autsing.miga.presentation.screen.LoginScreen
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {

    companion object {

        fun startActivity(context: Context) {
            val intent = Intent(context, LoginActivity::class.java)
            context.startActivity(intent)
        }
    }

    private val authRepository: AuthRepository = AuthRepository.instance

    private val loginUrlState: MutableStateFlow<String> = MutableStateFlow("")
    private val exceptionState: MutableStateFlow<String> = MutableStateFlow("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginScreen(
                loginUrl = loginUrlState.collectAsState().value,
                exception = exceptionState.collectAsState().value,
                onRefresh = { handleRefresh() },
            )
        }
        handleRefresh()
    }

    private fun handleRefresh() = lifecycleScope.launch {
        runCatching {
            exceptionState.value = ""
            val deviceId = genDeviceId()
            val respData = authRepository.getLoginUrl(deviceId).getOrThrow()
            loginUrlState.value = respData.loginUrl
            authRepository.login(deviceId, respData).getOrThrow()

            finish()
        }.onFailure { e ->
            when (e) {
                is HttpRequestTimeoutException -> exceptionState.value = "登录超时"
                else -> exceptionState.value = "登录失败: ${e.message}"
            }
        }
    }
}
