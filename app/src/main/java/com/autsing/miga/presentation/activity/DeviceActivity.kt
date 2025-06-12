package com.autsing.miga.presentation.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.autsing.miga.presentation.screen.DeviceScreen
import com.autsing.miga.presentation.viewmodel.DeviceViewModel

class DeviceActivity : ComponentActivity() {

    companion object {
        private const val EXTRA_DEVICE_MODEL = "extra_device_model"

        fun startActivity(context: Context, deviceModel: String) {
            val intent = Intent(context, DeviceActivity::class.java)
            intent.putExtra(EXTRA_DEVICE_MODEL, deviceModel)
            context.startActivity(intent)
        }
    }

    private val deviceViewModel: DeviceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent { DeviceScreen(deviceViewModel) }

        intent.getStringExtra(EXTRA_DEVICE_MODEL)?.let { deviceViewModel.handleLoad(it) }
    }
}
