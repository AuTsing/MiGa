package com.autsing.miga.presentation.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.lifecycleScope
import com.autsing.miga.presentation.helper.ApiHelper
import com.autsing.miga.presentation.helper.FileHelper
import com.autsing.miga.presentation.model.Auth
import com.autsing.miga.presentation.model.Component
import com.autsing.miga.presentation.model.DeviceInfo
import com.autsing.miga.presentation.model.DevicePropertyRange
import com.autsing.miga.presentation.model.DevicePropertyValue
import com.autsing.miga.presentation.repository.DeviceRepository
import com.autsing.miga.presentation.screen.RunActionScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class RunActionActivity : ComponentActivity() {

    companion object {
        private const val EXTRA_DEVICE_MODEL = "extra_device_model"
        private const val EXTRA_SIID = "extra_siid"
        private const val EXTRA_AIID = "extra_aiid"

        fun startActivity(context: Context, deviceModel: String, siid: Int, aiid: Int) {
            val intent = Intent(context, RunActionActivity::class.java)
            intent.putExtra(EXTRA_DEVICE_MODEL, deviceModel)
            intent.putExtra(EXTRA_SIID, siid)
            intent.putExtra(EXTRA_AIID, aiid)
            context.startActivity(intent)
        }
    }

    private val fileHelper: FileHelper = FileHelper.instance
    private val apiHelper: ApiHelper = ApiHelper.instance
    private val deviceRepository: DeviceRepository = DeviceRepository.instance

    private lateinit var deviceModel: String
    private lateinit var action: DeviceInfo.Action
    private lateinit var properties: List<DeviceInfo.Property>

    private val loading: MutableStateFlow<Boolean> = MutableStateFlow(true)
    private val message: MutableStateFlow<String> = MutableStateFlow("")
    private val title: MutableStateFlow<String> = MutableStateFlow("")
    private val sliders: MutableStateFlow<List<Component.Slider>> = MutableStateFlow(emptyList())
    private val selectors: MutableStateFlow<List<Component.Selector>> =
        MutableStateFlow(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val loading = loading.collectAsState()
            val message = message.collectAsState()
            val title = title.collectAsState()
            val sliders = sliders.collectAsState()
            val selectors = selectors.collectAsState()

            RunActionScreen(
                loading = loading.value,
                message = message.value,
                title = title.value,
                sliders = sliders.value,
                selectors = selectors.value,
                onClickSlider = this::handleChangeSlider,
                onClickSelector = this::handleChangeSelector,
                onClickRun = this::handleRunAction,
                onClickBack = this::handleClickBack,
            )
        }

        handleLoad()
    }

    private fun handleLoad() = lifecycleScope.launch {
        runCatching {
            val deviceModel = intent.getStringExtra(EXTRA_DEVICE_MODEL)
                ?: throw Exception("读取设备型号失败")
            val siid = intent.getIntExtra(EXTRA_SIID, -1).takeIf { it > -1 }
                ?: throw Exception("读取SIID失败")
            val aiid = intent.getIntExtra(EXTRA_AIID, -1).takeIf { it > -1 }
                ?: throw Exception("读取AIID失败")

            val deviceInfo = deviceRepository.loadDeviceInfosLocal().getOrThrow()[deviceModel]
                ?: throw Exception("读取设备信息失败")
            val action = deviceInfo.actions
                .find { it.method.siid == siid && it.method.aiid == aiid }
                ?: throw Exception("读取动作失败")
            val title = action.descZhCn.takeIf { it.isNotBlank() } ?: action.name
            val properties = action.inPiids
                .mapNotNull { inPiid -> deviceInfo.properties.find { it.method.siid == siid && it.method.piid == inPiid } }

            val sliders = properties
                .filter { it.range !is DevicePropertyRange.None }
                .map {
                    val v = when (it.range) {
                        is DevicePropertyRange.Long -> DevicePropertyValue.Long(it.range.from)
                        is DevicePropertyRange.Double -> DevicePropertyValue.Double(it.range.from)
                        is DevicePropertyRange.None -> DevicePropertyValue.None
                    }
                    Component.Slider.from(it, v)
                }
            val selectors = properties
                .filter { it.values.size > 1 }
                .map { Component.Selector.from(it, it.values[0].value) }

            this@RunActionActivity.deviceModel = deviceModel
            this@RunActionActivity.action = action
            this@RunActionActivity.properties = properties
            this@RunActionActivity.title.value = title
            this@RunActionActivity.sliders.value = sliders
            this@RunActionActivity.selectors.value = selectors
        }.onFailure {
            message.value = it.stackTraceToString()
        }.also {
            loading.value = false
        }
    }

    private fun handleChangeSlider(
        component: Component.Slider,
        percentage: Float,
    ) = lifecycleScope.launch {
        runCatching {
            sliders.value = sliders.value.map {
                if (it == component) {
                    val value = component.range.getValueOfPercentage(percentage)
                    val sliderValue = when (value) {
                        is DevicePropertyValue.Long -> "${value.value}"
                        is DevicePropertyValue.Double -> "${value.value}"
                        else -> ""
                    }
                    val sliderDisplay = "$sliderValue ${it.property.unit}"
                    it.copy(
                        value = percentage,
                        valueDisplay = sliderDisplay,
                    )
                } else {
                    it
                }
            }
        }.onFailure {
            message.value = it.stackTraceToString()
        }
    }

    private fun handleChangeSelector(
        component: Component.Selector,
        index: Int,
    ) = lifecycleScope.launch {
        runCatching {
            selectors.value = selectors.value.map {
                if (it == component) {
                    val selectorValue = component.values[index]
                    val selectorDisplay = selectorValue.desc_zh_cn ?: selectorValue.description
                    it.copy(
                        value = index,
                        valueDisplay = selectorDisplay,
                    )
                } else {
                    it
                }
            }
        }.onFailure {
            message.value = it.stackTraceToString()
        }
    }

    private fun handleRunAction() = lifecycleScope.launch {
        runCatching {
            loading.value = true

            val values = properties
                .mapNotNull { property ->
                    sliders.value.find { it.property == property }
                        ?: selectors.value.find { it.property == property }
                }
                .map {
                    when (it) {
                        is Component.Slider -> it.range.getValueOfPercentage(it.value)
                        is Component.Selector -> it.values[it.value].value
                        else -> DevicePropertyValue.None
                    }
                }

            val devices = deviceRepository.loadDevicesLocal().getOrThrow()
            val device = devices.find { it.model == deviceModel }
                ?: throw Exception("读取设备失败")
            val authJson = fileHelper.readJson("auth.json").getOrThrow()
            val auth = Json.decodeFromString<Auth>(authJson)

            val code = apiHelper.runAction(
                auth = auth,
                device = device,
                action = action,
                inValues = values,
            ).getOrThrow()

            message.value = "Code: $code"
            loading.value = false
            delay(1000)
            finish()
        }.onFailure {
            message.value = it.stackTraceToString()
            loading.value = false
        }
    }

    private fun handleClickBack() = lifecycleScope.launch {
        finish()
    }
}
