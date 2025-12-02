package com.autsing.miga.presentation.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.lifecycleScope
import com.autsing.miga.presentation.helper.FileHelper
import com.autsing.miga.presentation.helper.LoginHelper
import com.autsing.miga.presentation.model.Auth
import com.autsing.miga.presentation.screen.LoginScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class LoginActivity : ComponentActivity() {

    companion object {

        fun startActivity(context: Context) {
            val intent = Intent(context, LoginActivity::class.java)
            context.startActivity(intent)
        }
    }

    private val loginUrl: MutableStateFlow<String> = MutableStateFlow("")
    private val exception: MutableStateFlow<String> = MutableStateFlow("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val loginUrl = loginUrl.collectAsState()
            val exception = exception.collectAsState()

            LoginScreen(loginUrl.value, exception.value)
        }

        lifecycleScope.launch(Dispatchers.IO) {
            runCatching {
                val loginIndexResponse = LoginHelper.instance
                    .getLoginIndex()
                    .getOrThrow()
                val loginUrlResponse = LoginHelper.instance
                    .getLoginUrl(loginIndexResponse)
                    .getOrThrow()
                loginUrl.value = loginUrlResponse.loginUrl
                val loginLpResponse = LoginHelper.instance
                    .getLoginLpResponse(loginUrlResponse)
                    .getOrThrow()
                val serviceToken = LoginHelper.instance
                    .getLoginServiceToken(loginLpResponse)
                    .getOrThrow()
                val deviceId = LoginHelper.instance.getDeviceId()

                val auth = Auth(
                    userId = loginLpResponse.userId,
                    ssecurity = loginLpResponse.ssecurity,
                    deviceId = deviceId,
                    serviceToken = serviceToken,
                )
                val authJson = Json.encodeToString(auth)

                FileHelper.instance.writeJson("auth.json", authJson).getOrThrow()

                finish()
            }.onFailure {
                exception.value = "登录失败: ${it.message}"
            }
        }
    }
}
