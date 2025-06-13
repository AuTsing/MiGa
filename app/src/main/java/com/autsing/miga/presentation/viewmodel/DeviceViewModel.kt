package com.autsing.miga.presentation.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autsing.miga.presentation.model.DeviceInfo
import com.autsing.miga.presentation.repository.DeviceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class DeviceUiState(
    val loading: Boolean = true,
    val exception: String = "",
    val deviceInfo: DeviceInfo? = null,
)

class DeviceViewModel : ViewModel() {

    private val deviceRepository: DeviceRepository = DeviceRepository.instance

    var uiState: DeviceUiState by mutableStateOf(DeviceUiState())
        private set

    fun handleLoad(deviceModel: String) = viewModelScope.launch(Dispatchers.IO) {
        runCatching {
            withContext(Dispatchers.Main) {
                uiState = uiState.copy(loading = true)
            }

            val devices = deviceRepository.loadDevicesLocal().getOrThrow()
            val device = devices.find { it.model == deviceModel }
                ?: throw Exception("读取设备失败")
            val deviceInfo = deviceRepository.loadDeviceInfosLocal().getOrNull()?.get(deviceModel)
                ?: deviceRepository.loadDeviceInfosRemote(device).getOrThrow()[deviceModel]
                ?: throw Exception("读取设备信息失败")

            withContext(Dispatchers.Main) {
                uiState = uiState.copy(
                    deviceInfo = deviceInfo,
                    exception = "",
                )
            }
        }.onFailure {
            withContext(Dispatchers.IO) {
                uiState = uiState.copy(exception = it.stackTraceToString())
            }
        }.also {
//            withContext(Dispatchers.Main) {
//                uiState = uiState.copy(loading = false)
//            }
            Log.d("TAG", "handleLoad: $it")
        }
    }
}
