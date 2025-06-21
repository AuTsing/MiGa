package com.autsing.miga.presentation.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autsing.miga.presentation.activity.RunActionActivity
import com.autsing.miga.presentation.helper.ApiHelper
import com.autsing.miga.presentation.helper.FileHelper
import com.autsing.miga.presentation.model.Auth
import com.autsing.miga.presentation.model.Component
import com.autsing.miga.presentation.model.Device
import com.autsing.miga.presentation.model.DeviceInfo
import com.autsing.miga.presentation.model.DevicePropertyRange
import com.autsing.miga.presentation.model.DevicePropertyValue
import com.autsing.miga.presentation.repository.DeviceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

data class DeviceUiState(
    val loading: Boolean = true,
    val exception: String = "",
    val auth: Auth? = null,
    val device: Device? = null,
    val deviceInfo: DeviceInfo? = null,
    val switchComponents: List<Component.Switch> = emptyList(),
    val sliderComponents: List<Component.Slider> = emptyList(),
    val selectorComponents: List<Component.Selector> = emptyList(),
    val triggerComponents: List<Component.Trigger> = emptyList(),
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

            val switches = deviceProperties
                .filter { (property, _) -> property.type == "bool" }
                .map { (property, value) -> Component.Switch.from(property, value) }
            val sliders = deviceProperties
                .filter { (property, _) -> property.range is DevicePropertyRange.None == false }
                .map { (property, value) -> Component.Slider.from(property, value) }
            val selectors = deviceProperties
                .filter { (property, _) -> property.values.size > 1 }
                .map { (property, value) -> Component.Selector.from(property, value) }
            val actions = deviceInfo.actions
                .map { Component.Trigger.from(it) }

            withContext(Dispatchers.Main) {
                uiState = uiState.copy(
                    exception = "",
                    auth = auth,
                    device = device,
                    deviceInfo = deviceInfo,
                    switchComponents = switches,
                    sliderComponents = sliders,
                    selectorComponents = selectors,
                    triggerComponents = actions,
                )
            }
        }.onFailure {
            withContext(Dispatchers.Main) {
                uiState = uiState.copy(exception = it.stackTraceToString())
            }
        }.also {
            withContext(Dispatchers.Main) {
                uiState = uiState.copy(loading = false)
            }
        }
    }

    fun handleChangeSwitch(
        component: Component.Switch,
        checked: Boolean,
    ) = viewModelScope.launch(Dispatchers.IO) {
        runCatching {
            val auth = uiState.auth ?: throw Exception("读取权限失败")
            val device = uiState.device ?: throw Exception("读取设备失败")
            val value = DevicePropertyValue.Boolean(checked)
            val (newProperty, _) = apiHelper.setDeviceProperty(
                auth,
                device,
                component.property,
                value,
            ).getOrThrow()

            withContext(Dispatchers.Main) {
                val newSwitchComponents = uiState.switchComponents.map {
                    if (it.property == newProperty) {
                        it.copy(value = checked)
                    } else {
                        it
                    }
                }
                uiState = uiState.copy(switchComponents = newSwitchComponents)
            }
        }.onFailure {
            withContext(Dispatchers.Main) {
                uiState = uiState.copy(exception = it.stackTraceToString())
            }
        }
    }

    fun handleChangeSlider(
        component: Component.Slider,
        percentage: Float,
    ) = viewModelScope.launch(Dispatchers.IO) {
        runCatching {
            val auth = uiState.auth ?: throw Exception("读取权限失败")
            val device = uiState.device ?: throw Exception("读取设备失败")
            val value = component.range.getValueOfPercentage(percentage)
            val (newProperty, newValue) = apiHelper.setDeviceProperty(
                auth,
                device,
                component.property,
                value,
            ).getOrThrow()

            withContext(Dispatchers.Main) {
                val newSliderComponents = uiState.sliderComponents.map {
                    if (it.property == newProperty) {
                        val v = when (newValue) {
                            is DevicePropertyValue.Long -> "${newValue.value}"
                            is DevicePropertyValue.Double -> "${newValue.value}"
                            else -> ""
                        }
                        it.copy(
                            value = percentage,
                            valueDisplay = "$v ${it.property.unit}",
                        )
                    } else {
                        it
                    }
                }
                uiState = uiState.copy(
                    sliderComponents = newSliderComponents,
                )
            }
        }.onFailure {
            withContext(Dispatchers.Main) {
                uiState = uiState.copy(exception = it.stackTraceToString())
            }
        }
    }

    fun handleChangeSelector(
        component: Component.Selector,
        index: Int,
    ) = viewModelScope.launch(Dispatchers.IO) {
        runCatching {
            val auth = uiState.auth ?: throw Exception("读取权限失败")
            val device = uiState.device ?: throw Exception("读取设备失败")
            val value = component.values[index].value
            val (newProperty, newValue) = apiHelper.setDeviceProperty(
                auth,
                device,
                component.property,
                value,
            ).getOrThrow()

            withContext(Dispatchers.Main) {
                val newSelectorComponents = uiState.selectorComponents.map {
                    if (it.property == newProperty) {
                        val selectorValue = it.property.values.find { it.value == newValue }
                            ?: it.property.values[0]
                        val selectorDisplay = selectorValue.desc_zh_cn ?: selectorValue.description
                        it.copy(
                            value = index,
                            valueDisplay = selectorDisplay,
                        )
                    } else {
                        it
                    }
                }
                uiState = uiState.copy(
                    selectorComponents = newSelectorComponents,
                )
            }
        }.onFailure {
            withContext(Dispatchers.Main) {
                uiState = uiState.copy(exception = it.stackTraceToString())
            }
        }
    }

    fun handleClickTrigger(
        context: Context,
        component: Component.Trigger,
    ) = viewModelScope.launch(Dispatchers.IO) {
        runCatching {
            val device = uiState.device ?: throw Exception("读取设备失败")
            RunActionActivity.startActivity(
                context = context,
                deviceModel = device.model,
                siid = component.action.method.siid,
                aiid = component.action.method.aiid,
            )
        }.onFailure {
            withContext(Dispatchers.Main) {
                uiState = uiState.copy(exception = it.stackTraceToString())
            }
        }
    }
}
