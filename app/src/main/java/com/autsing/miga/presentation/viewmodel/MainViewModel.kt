package com.autsing.miga.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autsing.miga.presentation.activity.LoginActivity
import com.autsing.miga.presentation.model.Auth
import com.autsing.miga.presentation.model.Device
import com.autsing.miga.presentation.model.Scene
import com.autsing.miga.presentation.util.ApiUtil
import com.autsing.miga.presentation.util.Constants.TAG
import com.autsing.miga.presentation.util.FileUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

data class MainUiState(
    val loading: Boolean = true,
    val auth: Auth? = null,
    val showedLogin: Boolean = false,
    val scenes: List<Scene> = emptyList(),
    val devices: List<Device> = emptyList(),
)

class MainViewModel : ViewModel() {

    var uiState: MainUiState by mutableStateOf(MainUiState())
        private set

    fun handleLoadAuth() = viewModelScope.launch(Dispatchers.IO) {
        val minDelay = launch(Dispatchers.IO) { delay(500) }

        runCatching {
            withContext(Dispatchers.Main) {
                uiState = uiState.copy(loading = true)
            }

            val authJson = FileUtil.instance.readJson("auth.json").getOrThrow()
            val auth = Json.decodeFromString<Auth>(authJson)

            withContext(Dispatchers.Main) {
                uiState = uiState.copy(auth = auth)
            }
        }.onFailure {
            Log.e(TAG, "handleLoadAuth: ${it.stackTraceToString()}")
        }.also {
            minDelay.join()
            withContext(Dispatchers.Main) {
                uiState = uiState.copy(loading = false)
            }
        }
    }

    fun handleNavigateToLogin(context: Context) = viewModelScope.launch {
        LoginActivity.startActivity(context)
        uiState = uiState.copy(showedLogin = true)
    }

    private suspend fun loadScenesLocal(): Result<List<Scene>> = withContext(Dispatchers.IO) {
        runCatching {
            val scenesJson = FileUtil.instance.readJson("scenes.json").getOrThrow()
            val scenes = Json.decodeFromString<List<Scene>>(scenesJson)
            return@runCatching scenes
        }
    }

    private suspend fun loadScenesRemote(
        auth: Auth,
    ): Result<List<Scene>> = withContext(Dispatchers.IO) {
        runCatching {
            val scenes = ApiUtil.instance.getScenes(auth).getOrThrow()
            val scenesJson = Json.encodeToString(scenes)
            FileUtil.instance.writeJson("scenes.json", scenesJson).getOrThrow()
            return@runCatching scenes
        }
    }

    private suspend fun loadDevicesLocal(): Result<List<Device>> = withContext(Dispatchers.IO) {
        runCatching {
            val devicesJson = FileUtil.instance.readJson("devices.json").getOrThrow()
            val devices = Json.decodeFromString<List<Device>>(devicesJson)
                .sortedByDescending { it.isOnline }
            return@runCatching devices
        }
    }

    private suspend fun loadDevicesRemote(
        auth: Auth,
    ): Result<List<Device>> = withContext(Dispatchers.IO) {
        runCatching {
            val devices = ApiUtil.instance.getDevices(auth).getOrThrow()
                .sortedByDescending { it.isOnline }
            val devicesJson = Json.encodeToString(devices)
            FileUtil.instance.writeJson("devices.json", devicesJson).getOrThrow()
            return@runCatching devices
        }
    }

    fun handleLoadScenesAndDevices(auth: Auth) = viewModelScope.launch(Dispatchers.IO) {
        runCatching {
            withContext(Dispatchers.Main) {
                uiState = uiState.copy(loading = true)
            }

            val scenesJob = async(Dispatchers.IO) {
                loadScenesLocal().getOrElse { loadScenesRemote(auth).getOrDefault(emptyList()) }
            }
            val devicesJob = async(Dispatchers.IO) {
                loadDevicesLocal().getOrElse { loadDevicesRemote(auth).getOrDefault(emptyList()) }
            }

            val scenes = scenesJob.await()
            val devices = devicesJob.await()

            withContext(Dispatchers.Main) {
                uiState = uiState.copy(
                    scenes = scenes,
                    devices = devices,
                )
            }
        }.onFailure {
            Log.e(TAG, "handleLoadScenesAndDevices: ${it.stackTraceToString()}")
        }.also {
            withContext(Dispatchers.Main) {
                uiState = uiState.copy(loading = false)
            }
        }
    }

    fun handleReload(auth: Auth) = viewModelScope.launch(Dispatchers.IO) {
        runCatching {
            withContext(Dispatchers.Main) {
                uiState = uiState.copy(loading = true)
            }

            val scenesJob = async(Dispatchers.IO) {
                loadScenesRemote(auth).getOrDefault(emptyList())
            }
            val devicesJob = async(Dispatchers.IO) {
                loadDevicesRemote(auth).getOrDefault(emptyList())
            }

            val scenes = scenesJob.await()
            val devices = devicesJob.await()

            withContext(Dispatchers.Main) {
                uiState = uiState.copy(
                    scenes = scenes,
                    devices = devices,
                )
            }
        }.onFailure {
            Log.e(TAG, "handleReload: ${it.stackTraceToString()}")
        }.also {
            withContext(Dispatchers.Main) {
                uiState = uiState.copy(loading = false)
            }
        }
    }
}
