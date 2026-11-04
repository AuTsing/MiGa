package com.autsing.miga.presentation.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
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

    private val loginUrl: MutableStateFlow<String> = MutableStateFlow("")
    private val exception: MutableStateFlow<String> = MutableStateFlow("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val loginUrl by loginUrl.collectAsStateWithLifecycle()
            val exception by exception.collectAsStateWithLifecycle()

            LoginScreen(
                loginUrl = loginUrl,
                exception = exception,
                onRefresh = { handleLogin() },
            )
        }

        handleLogin()
    }

    private fun handleLogin() = lifecycleScope.launch {
        runCatching {
            exception.value = ""
            val respData = authRepository.getLoginUrl().getOrThrow()
            loginUrl.value = respData.loginUrl
            authRepository.login(respData.lp).getOrThrow()

            finish()
        }.onFailure { e ->
            when (e) {
                is HttpRequestTimeoutException -> exception.value = "登录超时"
                else -> exception.value = "登录失败: ${e.message}"
            }
        }
    }
}
