package com.autsing.miga.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autsing.miga.presentation.data.Component
import com.autsing.miga.presentation.data.DevicePropertyRange
import com.autsing.miga.presentation.repository.Auth
import com.autsing.miga.presentation.repository.AuthRepository
import com.autsing.miga.presentation.repository.Device
import com.autsing.miga.presentation.repository.DeviceInfo
import com.autsing.miga.presentation.repository.DeviceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

    private val authRepository: AuthRepository = AuthRepository.instance
    private val deviceRepository: DeviceRepository = DeviceRepository.instance

    private val _uiState: MutableStateFlow<DeviceUiState> = MutableStateFlow(DeviceUiState())
    val uiState: StateFlow<DeviceUiState> = _uiState.asStateFlow()

    fun handleLoad(model: String) = viewModelScope.launch {
        runCatching {
            _uiState.update { it.copy(loading = true) }

            val devices = deviceRepository.getLocalDevices().getOrThrow()
            val device = devices.find { it.model == model } ?: throw Exception("读取设备失败")
            val info = deviceRepository.getLocalDeviceInfo(device.model).getOrNull()
                ?: deviceRepository.getRemoteDeviceInfo(device.model).getOrNull()
                ?: throw Exception("读取设备信息失败")
            val auth = authRepository.getValidAuth().getOrThrow()
            val deviceProperties = deviceRepository.getDeviceProperties(auth, device, info)
                .getOrThrow()

            val switches = deviceProperties
                .filter { (property, _) -> property.type == "bool" }
                .map { (property, value) -> Component.Switch.from(property, value) }
            val sliders = deviceProperties
                .filter { (property, _) -> property.range !is DevicePropertyRange.None }
                .map { (property, value) -> Component.Slider.from(property, value) }
            val selectors = deviceProperties
                .filter { (property, _) -> property.values.size > 1 }
                .map { (property, value) -> Component.Selector.from(property, value) }
            val actions = info.actions
                .map { Component.Trigger.from(it) }

            _uiState.update {
                it.copy(
                    loading = false,
                    exception = "",
                    auth = auth,
                    device = device,
                    deviceInfo = info,
                    switchComponents = switches,
                    sliderComponents = sliders,
                    selectorComponents = selectors,
                    triggerComponents = actions,
                )
            }
        }.onFailure { e ->
            _uiState.update {
                it.copy(
                    loading = false,
                    exception = e.message ?: e.stackTraceToString(),
                )
            }
        }
    }

    fun handleChangeSwitch(
        component: Component.Switch,
        checked: Boolean,
    ) = viewModelScope.launch {
//        runCatching {
//            val auth = uiState.auth ?: throw Exception("读取权限失败")
//            val device = uiState.device ?: throw Exception("读取设备失败")
//            val value = DevicePropertyValue.Boolean(checked)
//            val (newProperty, _) = deviceRepository.setDeviceProperty(
//                auth,
//                device,
//                component.property,
//                value,
//            ).getOrThrow()
//
//            val newSwitchComponents = uiState.switchComponents.map {
//                if (it.property == newProperty) {
//                    it.copy(value = checked)
//                } else {
//                    it
//                }
//            }
//            uiState = uiState.copy(switchComponents = newSwitchComponents)
//        }.onFailure {
//            uiState = uiState.copy(exception = it.stackTraceToString())
//        }
    }

    fun handleChangeSlider(
        component: Component.Slider,
        percentage: Float,
    ) = viewModelScope.launch {
//        runCatching {
//            val auth = uiState.auth ?: throw Exception("读取权限失败")
//            val device = uiState.device ?: throw Exception("读取设备失败")
//            val value = component.range.getValueOfPercentage(percentage)
//            val (newProperty, newValue) = deviceRepository.setDeviceProperty(
//                auth,
//                device,
//                component.property,
//                value,
//            ).getOrThrow()
//
//            val newSliderComponents = uiState.sliderComponents.map {
//                if (it.property == newProperty) {
//                    val v = when (newValue) {
//                        is DevicePropertyValue.Long -> "${newValue.value}"
//                        is DevicePropertyValue.Double -> "${newValue.value}"
//                        else -> ""
//                    }
//                    it.copy(
//                        value = percentage,
//                        valueDisplay = "$v ${it.property.unit}",
//                    )
//                } else {
//                    it
//                }
//            }
//            uiState = uiState.copy(sliderComponents = newSliderComponents)
//        }.onFailure {
//            uiState = uiState.copy(exception = it.stackTraceToString())
//        }
    }

    fun handleChangeSelector(
        component: Component.Selector,
        index: Int,
    ) = viewModelScope.launch {
//        runCatching {
//            val auth = uiState.auth ?: throw Exception("读取权限失败")
//            val device = uiState.device ?: throw Exception("读取设备失败")
//            val value = component.values[index].value
//            val (newProperty, newValue) = deviceRepository.setDeviceProperty(
//                auth,
//                device,
//                component.property,
//                value,
//            ).getOrThrow()
//
//            val newSelectorComponents = uiState.selectorComponents.map { component ->
//                if (component.property == newProperty) {
//                    val selectorValue = component.property.values.find { it.value == newValue }
//                        ?: component.property.values[0]
//                    val selectorDisplay = selectorValue.desc_zh_cn ?: selectorValue.description
//                    component.copy(
//                        value = index,
//                        valueDisplay = selectorDisplay,
//                    )
//                } else {
//                    component
//                }
//            }
//            uiState = uiState.copy(selectorComponents = newSelectorComponents)
//        }.onFailure {
//            uiState = uiState.copy(exception = it.stackTraceToString())
//        }
    }

    fun handleClickTrigger(
        context: Context,
        component: Component.Trigger,
    ) = viewModelScope.launch {
//        runCatching {
//            val device = uiState.device ?: throw Exception("读取设备失败")
//            RunActionActivity.startActivity(
//                context = context,
//                deviceModel = device.model,
//                siid = component.action.method.siid,
//                aiid = component.action.method.aiid,
//            )
//        }.onFailure {
//            uiState = uiState.copy(exception = it.stackTraceToString())
//        }
    }
}
