package com.autsing.miga.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autsing.miga.presentation.helper.ApiHelper
import com.autsing.miga.presentation.helper.FileHelper
import com.autsing.miga.presentation.model.Auth
import com.autsing.miga.presentation.model.Component
import com.autsing.miga.presentation.model.DeviceInfo
import com.autsing.miga.presentation.model.GetDevicePropertiesResponse
import com.autsing.miga.presentation.repository.DeviceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

data class DeviceUiState(
    val loading: Boolean = true,
    val exception: String = "",
    val deviceInfo: DeviceInfo? = null,
    val components: List<Component> = emptyList(),
)

class DeviceViewModel : ViewModel() {

    private val deviceRepository: DeviceRepository = DeviceRepository.instance
    private val fileHelper: FileHelper = FileHelper.instance
    private val apiHelper: ApiHelper = ApiHelper.instance

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
            val authJson = fileHelper.readJson("auth.json").getOrThrow()
            val auth = Json.decodeFromString<Auth>(authJson)
            val deviceProperties = apiHelper.getDeviceProperties(auth, device, deviceInfo)
                .getOrThrow()

            val switchProperties = deviceProperties.filter { it.first.type == "bool" }
                .map { (property, value) ->
                    val v = if (value is GetDevicePropertiesResponse.Result.Value.Boolean) {
                        value.value
                    } else {
                        false
                    }
                    Component.Switch(
                        headline = property.descZhCn.takeIf { it.isNotBlank() }
                            ?.split(" ", "，")[0]
                            ?: property.description,
                        value = v,
                        readOnly = !property.access.write,
                    )
                }
            val sliderProperties = deviceProperties
                .filter { it.first.range is DeviceInfo.Property.Range.None == false }
                .map { (property, value) ->
                    val (sliderV, sliderDisplay) = when (property.range) {

                        is DeviceInfo.Property.Range.Int32 -> {
                            if (value is GetDevicePropertiesResponse.Result.Value.Int) {
                                val v = value.value
                                val min = property.range.from
                                val max = property.range.to
                                val percentage = (v - min).toFloat() / (max - min) * 100
                                val sliderV = (percentage / 100 * 10).toInt()
                                val sliderDisplay = "$v${property.unit}"
                                Pair(sliderV, sliderDisplay)
                            } else {
                                Pair(0, "")
                            }
                        }

                        is DeviceInfo.Property.Range.Uint32 -> {
                            if (value is GetDevicePropertiesResponse.Result.Value.Int) {
                                val v = value.value.toUInt()
                                val min = property.range.from
                                val max = property.range.to
                                val percentage = (v - min).toFloat() / (max - min).toInt() * 100
                                val sliderV = (percentage / 100 * 10).toInt()
                                val sliderDisplay = "$v${property.unit}"
                                Pair(sliderV, sliderDisplay)
                            } else {
                                Pair(0, "")
                            }
                        }

                        is DeviceInfo.Property.Range.Float -> {
                            if (value is GetDevicePropertiesResponse.Result.Value.Float) {
                                val v = value.value
                                val min = property.range.from
                                val max = property.range.to
                                val percentage = (v - min).toFloat() / (max - min).toInt() * 100
                                val sliderV = (percentage / 100 * 10).toInt()
                                val sliderDisplay = "$v${property.unit}"
                                Pair(sliderV, sliderDisplay)
                            } else {
                                Pair(0, "")
                            }
                        }

                        else -> Pair(0, "")
                    }
                    Component.Slider(
                        headline = property.descZhCn.takeIf { it.isNotBlank() }
                            ?.split(" ", "，")[0]
                            ?: property.description,
                        value = sliderV,
                        range = property.range,
                        readOnly = !property.access.write,
                        valueDisplay = sliderDisplay,
                    )
                }
            val selectorProperties = deviceProperties.filter { it.first.values.size > 1 }
                .map { (property, value) ->
                    val v = if (value is GetDevicePropertiesResponse.Result.Value.Int) {
                        value.value
                    } else {
                        0
                    }
                    val vDisplay = property.values[v].desc_zh_cn ?: property.values[v].description
                    Component.Selector(
                        headline = property.descZhCn.takeIf { it.isNotBlank() } ?: property.name,
                        value = v,
                        values = property.values,
                        readOnly = !property.access.write,
                        valueDisplay = vDisplay,
                    )
                }
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
                    components = switchProperties + sliderProperties + selectorProperties + actions,
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
