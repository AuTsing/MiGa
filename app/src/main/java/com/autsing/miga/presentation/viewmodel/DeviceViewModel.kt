package com.autsing.miga.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autsing.miga.presentation.model.Component
import com.autsing.miga.presentation.model.DeviceInfo
import com.autsing.miga.presentation.repository.DeviceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class DeviceUiState(
    val loading: Boolean = true,
    val exception: String = "",
    val deviceInfo: DeviceInfo? = null,
    val components: List<Component> = emptyList(),
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
            val properties = deviceInfo.properties.mapNotNull {
                if (it.type == "bool") {
                    Component.Switch(
                        headline = it.descZhCn.takeIf { it.isNotBlank() }
                            ?.split(" ", "，")[0]
                            ?: it.description,
                        value = false,
                        readOnly = !it.access.write,
                    )
                } else if (it.range is DeviceInfo.Property.Range.None == false) {
                    Component.Slider(
                        headline = it.descZhCn.takeIf { it.isNotBlank() }
                            ?.split(" ", "，")[0]
                            ?: it.description,
                        value = 0,
                        range = it.range,
                        readOnly = !it.access.write,
                        valueDisplay = "1000" + it.unit,
                    )
                } else if (it.values.size > 1) {
                    Component.Selector(
                        headline = it.descZhCn.takeIf { it.isNotBlank() } ?: it.name,
                        value = 0,
                        values = it.values,
                        readOnly = !it.access.write,
                        valueDisplay = it.values[0].desc_zh_cn ?: it.values[0].description,
                    )
                } else {
                    null
                }
            }
                .sortedByDescending { it is Component.Slider }
                .sortedByDescending { it is Component.Switch }
            val actions = deviceInfo.actions.map {
                Component.Trigger(
                    headline = it.descZhCn.takeIf { it.isNotBlank() }
                        ?.split(" ", "，")[0]
                        ?: it.name,
                )
            }

            withContext(Dispatchers.Main) {
                uiState = uiState.copy(
                    exception = "",
                    deviceInfo = deviceInfo,
                    components = properties + actions,
                )
            }
        }.onFailure {
            withContext(Dispatchers.IO) {
                uiState = uiState.copy(exception = it.stackTraceToString())
            }
        }.also {
            withContext(Dispatchers.Main) {
                uiState = uiState.copy(loading = false)
            }
        }
    }
}
