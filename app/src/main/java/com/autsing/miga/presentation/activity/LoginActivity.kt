package com.autsing.miga.presentation.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.lifecycleScope
import com.autsing.miga.presentation.model.Auth
import com.autsing.miga.presentation.screen.LoginScreen
import com.autsing.miga.presentation.util.FileUtil
import com.autsing.miga.presentation.util.LoginUtil
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

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            val loginUrl = loginUrl.collectAsState()
            val exception = exception.collectAsState()

            LoginScreen(loginUrl.value, exception.value)
        }

        lifecycleScope.launch(Dispatchers.IO) {
            runCatching {
                val loginIndexResponse = LoginUtil.instance.getLoginIndex().getOrThrow()
                val loginUrlResponse = LoginUtil.instance
                    .getLoginUrl(loginIndexResponse)
                    .getOrThrow()
                loginUrl.value = loginUrlResponse.loginUrl
                val loginLpResponse = LoginUtil.instance
                    .getLoginLpResponse(loginUrlResponse)
                    .getOrThrow()
                val serviceToken = LoginUtil.instance
                    .getLoginServiceToken(loginLpResponse)
                    .getOrThrow()
                val deviceId = LoginUtil.instance.getDeviceId()

                val auth = Auth(
                    userId = loginLpResponse.userId,
                    ssecurity = loginLpResponse.ssecurity,
                    deviceId = deviceId,
                    serviceToken = serviceToken,
                )
                val authJson = Json.encodeToString(auth)

                FileUtil.instance.writeJson("auth.json", authJson).getOrThrow()

                finish()
            }.onFailure {
                exception.value = "登录失败: ${it.message}"
            }
        }
    }
}
