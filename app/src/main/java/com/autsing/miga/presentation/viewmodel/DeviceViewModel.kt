package com.autsing.miga.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autsing.miga.presentation.activity.RunActionActivity
import com.autsing.miga.presentation.data.Auth
import com.autsing.miga.presentation.data.Component
import com.autsing.miga.presentation.data.Device
import com.autsing.miga.presentation.data.DeviceInfoV2
import com.autsing.miga.presentation.data.DevicePropertyValueV2
import com.autsing.miga.presentation.repository.AuthRepository
import com.autsing.miga.presentation.repository.DeviceRepository
import io.ktor.client.plugins.HttpRequestTimeoutException
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
                .mapNotNull {
                    when (it.key) {
                        is DeviceInfoV2.Property.DoubleRange ->
                            Component.Selector.from(it.key, it.value)

                        is DeviceInfoV2.Property.LongRange ->
                            Component.Selector.from(it.key, it.value)

                        is DeviceInfoV2.Property.IntEnum ->
                            Component.Selector.from(it.key, it.value)

                        is DeviceInfoV2.Property.StringEnum ->
                            Component.Selector.from(it.key, it.value)

                        else -> null
                    }
                }
            selectors.forEach {
                Log.d("TAG", "handleLoad: $it")
            }
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
            val value = DevicePropertyValueV2.from(component.property, index)

            deviceRepository.setDeviceProperty(device, component.property, value, auth).getOrThrow()

            val newSelector = Component.Selector.from(component.property, value)
            val newSelectors = _uiState.value.selectors.map {
                if (it == component) newSelector else it
            }

            _uiState.update { it.copy(selectors = newSelectors) }
        }.onFailure { e ->
            if (e is HttpRequestTimeoutException) return@onFailure
            _uiState.update { it.copy(exception = e.message ?: e.stackTraceToString()) }
        }
    }

    fun handleClickTrigger(component: Component.Trigger, context: Context) = viewModelScope.launch {
        runCatching {
            val device = _uiState.value.device ?: throw Exception("读取设备失败")

            RunActionActivity.startActivity(
                context = context,
                urn = device.spec_type,
                siid = component.action.method.siid,
                aiid = component.action.method.aiid,
            )
        }.onFailure { e ->
            _uiState.update { it.copy(exception = e.message ?: e.stackTraceToString()) }
        }
    }
}
