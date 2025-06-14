package com.autsing.miga.presentation.activity

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.wear.protolayout.ActionBuilders.AndroidActivity
import androidx.wear.protolayout.ActionBuilders.LaunchAction
import com.autsing.miga.presentation.screen.MainScreen
import com.autsing.miga.presentation.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    companion object {
        fun createLaunchAction(context: Context): LaunchAction {
            val activity = AndroidActivity.Builder()
                .setPackageName(context.packageName)
                .setClassName(MainActivity::class.java.name)
                .build()
            val action = LaunchAction.Builder()
                .setAndroidActivity(activity)
                .build()
            return action
        }

        fun createPendingIntent(context: Context, requestCode: Int): PendingIntent {
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            return pendingIntent
        }
    }

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setContent { MainScreen(mainViewModel) }

        lifecycleScope.launch(Dispatchers.IO) {
            delay(1000)
            DeviceActivity.startActivity(
                context = this@MainActivity,
                deviceModel = "yeelink.light.lamp27",
            )
        }
    }

    override fun onResume() {
        super.onResume()

        if (mainViewModel.uiState.auth == null) {
            mainViewModel.handleLoad()
        }
    }
}
