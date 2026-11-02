package com.autsing.miga.presentation.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.autsing.miga.presentation.api.reqRunActionV2
import com.autsing.miga.presentation.data.Component
import com.autsing.miga.presentation.data.DeviceInfoV2
import com.autsing.miga.presentation.data.DevicePropertyValueV2
import com.autsing.miga.presentation.data.encode
import com.autsing.miga.presentation.repository.AuthRepository
import com.autsing.miga.presentation.repository.DeviceRepository
import com.autsing.miga.presentation.screen.RunActionScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

class RunActionActivity : ComponentActivity() {

    companion object {
        private const val EXTRA_URN = "extra_urn"
        private const val EXTRA_SIID = "extra_siid"
        private const val EXTRA_AIID = "extra_aiid"

        fun startActivity(context: Context, urn: String, siid: Int, aiid: Int) {
            val intent = Intent(context, RunActionActivity::class.java)
            intent.putExtra(EXTRA_URN, urn)
            intent.putExtra(EXTRA_SIID, siid)
            intent.putExtra(EXTRA_AIID, aiid)
            context.startActivity(intent)
        }
    }

    private val authRepository: AuthRepository = AuthRepository.instance
    private val deviceRepository: DeviceRepository = DeviceRepository.instance

    private lateinit var urn: String
    private lateinit var action: DeviceInfoV2.Action
    private lateinit var properties: List<DeviceInfoV2.Property>

    private val loading: MutableStateFlow<Boolean> = MutableStateFlow(true)
    private val success: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val message: MutableStateFlow<String> = MutableStateFlow("")
    private val title: MutableStateFlow<String> = MutableStateFlow("")
    private val selectors: MutableStateFlow<List<Component.Selector>> =
        MutableStateFlow(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val loading = loading.collectAsStateWithLifecycle()
            val message = message.collectAsStateWithLifecycle()
            val title = title.collectAsStateWithLifecycle()
            val selectors = selectors.collectAsStateWithLifecycle()

            RunActionScreen(
                loading = loading.value,
                message = message.value,
                title = title.value,
                selectors = selectors.value,
                onClickSelector = this::handleChangeSelector,
                onClickRun = this::handleRunAction,
                onClickBack = this::handleClickBack,
            )
        }

        handleLoad()
    }

    private fun handleLoad() = lifecycleScope.launch {
        runCatching {
            val urn = intent.getStringExtra(EXTRA_URN)
                ?: throw Exception("读取设备型号失败")
            val siid = intent.getIntExtra(EXTRA_SIID, -1).takeIf { it > -1 }
                ?: throw Exception("读取SIID失败")
            val aiid = intent.getIntExtra(EXTRA_AIID, -1).takeIf { it > -1 }
                ?: throw Exception("读取AIID失败")

            val info = deviceRepository.getLocalDeviceInfo(urn).getOrThrow()
            val action = info.actions.find { it.method.siid == siid && it.method.aiid == aiid }
                ?: throw Exception("读取动作失败")
            val title = action.nameZh ?: action.name
            val properties = action.inPiids
                .mapNotNull { inPiid -> info.properties.find { it.method.siid == siid && it.method.piid == inPiid } }
            val selectors = properties.map {
                when (it) {
                    is DeviceInfoV2.Property.DoubleRange ->
                        Component.Selector.from(it, DevicePropertyValueV2.Double(it.from))

                    is DeviceInfoV2.Property.LongRange ->
                        Component.Selector.from(it, DevicePropertyValueV2.Long(it.from))

                    is DeviceInfoV2.Property.IntEnum ->
                        Component.Selector.from(
                            it,
                            DevicePropertyValueV2.Long(it.values[0].value.toLong()),
                        )

                    is DeviceInfoV2.Property.StringEnum ->
                        Component.Selector.from(
                            it,
                            DevicePropertyValueV2.String(it.values[0].value),
                        )

                    else -> throw Exception("读取属性失败")
                }
            }

            this@RunActionActivity.urn = urn
            this@RunActionActivity.action = action
            this@RunActionActivity.properties = properties
            this@RunActionActivity.title.value = title
            this@RunActionActivity.selectors.value = selectors
            loading.value = false
        }.onFailure { e ->
            message.value = e.message ?: e.stackTraceToString()
            loading.value = false
        }
    }

    private fun handleChangeSelector(
        component: Component.Selector,
        index: Int,
    ) = lifecycleScope.launch {
        runCatching {
            val value = DevicePropertyValueV2.from(component.property, index)

            val newSelectors = selectors.value.map {
                if (it == component) {
                    it.copy(
                        value = index,
                        display = value.encode().getOrThrow(),
                    )
                } else it
            }

            selectors.update { newSelectors }
        }.onFailure { e ->
            message.value = e.message ?: e.stackTraceToString()
        }
    }

    private fun handleRunAction() = lifecycleScope.launch {
        runCatching {
            loading.value = true

            val values = selectors.value.map { DevicePropertyValueV2.from(it.property, it.value) }
            val devices = deviceRepository.getLocalDevices().getOrThrow()
            val device = devices.find { it.spec_type == urn } ?: throw Exception("读取设备失败")
            val auth = authRepository.getValidAuth().getOrThrow()

            withContext(Dispatchers.IO) {
                reqRunActionV2(device, action, values, auth).getOrThrow()
            }

            success.value = true
            loading.value = false
            delay(1000.milliseconds)
            finish()
        }.onFailure { e ->
            message.value = e.message ?: e.stackTraceToString()
            loading.value = false
        }
    }

    private fun handleClickBack() = lifecycleScope.launch {
        finish()
    }
}
