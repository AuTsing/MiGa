package com.autsing.miga.presentation.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.lifecycleScope
import com.autsing.miga.presentation.repository.AuthRepository
import com.autsing.miga.presentation.screen.LoginScreen
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
            val resp = authRepository.getLoginUrl().getOrThrow()
            loginUrlState.value = resp.loginUrl
            authRepository.loadRemoteAuth(resp).getOrThrow()

            finish()
        }.onFailure {
            exceptionState.value = "登录失败: ${it.message}"
        }
    }
}
