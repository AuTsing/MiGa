package com.autsing.miga.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autsing.miga.presentation.data.Auth
import com.autsing.miga.presentation.data.Component
import com.autsing.miga.presentation.data.Device
import com.autsing.miga.presentation.data.DeviceInfoV2
import com.autsing.miga.presentation.data.DevicePropertyValueV2
import com.autsing.miga.presentation.data.encode
import com.autsing.miga.presentation.repository.AuthRepository
import com.autsing.miga.presentation.repository.DeviceRepository
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.round

data class DeviceUiState(
    val loading: Boolean = true,
    val exception: String = "",
    val auth: Auth? = null,
    val device: Device? = null,
    val deviceInfo: DeviceInfoV2? = null,
    val switches: List<Component.Switch> = emptyList(),
    val selectors: List<Component.Selector> = emptyList(),
    val triggers: List<Component.Trigger> = emptyList(),
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
            val info = deviceRepository.getLocalDeviceInfo(device.spec_type).getOrNull()
                ?: deviceRepository.getRemoteDeviceInfo(device.spec_type).getOrNull()
                ?: throw Exception("读取设备信息失败")
            val auth = authRepository.getValidAuth().getOrThrow()
            val properties = deviceRepository.getDeviceProperties(device, info, auth).getOrThrow()

            val switches = properties
                .filter { it.key is DeviceInfoV2.Property.Boolean }
                .map { Component.Switch.from(it.key, it.value) }
            val selectors = properties
                .filter { it.key is DeviceInfoV2.Property.DoubleRange || it.key is DeviceInfoV2.Property.LongRange }
                .map { Component.Selector.from(it.key, it.value) }
            val actions = info.actions.map { Component.Trigger.from(it) }

            _uiState.update {
                it.copy(
                    loading = false,
                    exception = "",
                    auth = auth,
                    device = device,
                    deviceInfo = info,
                    switches = switches,
                    selectors = selectors,
                    triggers = actions,
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

    fun handleChangeSwitch(component: Component.Switch, checked: Boolean) = viewModelScope.launch {
        runCatching {
            val auth = _uiState.value.auth ?: throw Exception("读取权限失败")
            val device = _uiState.value.device ?: throw Exception("读取设备失败")
            val value = DevicePropertyValueV2.Boolean(checked)

            deviceRepository.setDeviceProperty(device, component.property, value, auth).getOrThrow()

            val newSwitches = _uiState.value.switches.map {
                if (it == component) it.copy(value = checked) else it
            }

            _uiState.update { it.copy(switches = newSwitches) }
        }.onFailure { e ->
            _uiState.update { it.copy(exception = e.message ?: e.stackTraceToString()) }
        }
    }

    fun handleChangeSelector(component: Component.Selector, index: Int) = viewModelScope.launch {
        runCatching {
            val auth = _uiState.value.auth ?: throw Exception("读取权限失败")
            val device = _uiState.value.device ?: throw Exception("读取设备失败")
            val value = when (component.property) {
                is DeviceInfoV2.Property.DoubleRange -> {
                    val min = component.property.from
                    val max = component.property.to
                    val step = component.property.step
                    val actual = min + ((max - min) * index.toDouble() / 10)
                    val v = round(actual / step) * step
                    DevicePropertyValueV2.Double(v)
                }

                is DeviceInfoV2.Property.LongRange -> {
                    val min = component.property.from
                    val max = component.property.to
                    val step = component.property.step
                    val actual = min + ((max - min) * index.toDouble() / 10)
                    val v = (round(actual / step) * step).toLong()
                    DevicePropertyValueV2.Long(v)
                }

                is DeviceInfoV2.Property.IntEnum -> {
                    DevicePropertyValueV2.Long(component.property.values[index].value.toLong())
                }

                is DeviceInfoV2.Property.StringEnum -> {
                    DevicePropertyValueV2.String(component.property.values[index].value)
                }

                else -> throw Exception("参数错误")
            }

            deviceRepository.setDeviceProperty(device, component.property, value, auth).getOrThrow()

            val newSelectors = _uiState.value.selectors.map {
                if (it == component) {
                    it.copy(
                        value = index,
                        display = value.encode().getOrThrow(),
                    )
                } else it
            }

            _uiState.update { it.copy(selectors = newSelectors) }
        }.onFailure { e ->
            if (e is HttpRequestTimeoutException) return@onFailure
            _uiState.update { it.copy(exception = e.message ?: e.stackTraceToString()) }
        }
    }
}
