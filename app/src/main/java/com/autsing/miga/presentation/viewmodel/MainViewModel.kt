package com.autsing.miga.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autsing.miga.complication.FavoriteSceneComplicationService
import com.autsing.miga.presentation.activity.DeviceActivity
import com.autsing.miga.presentation.activity.LoginActivity
import com.autsing.miga.presentation.activity.RunSceneActivity
import com.autsing.miga.presentation.helper.Constants.TAG
import com.autsing.miga.presentation.helper.FileHelper
import com.autsing.miga.presentation.helper.SerdeHelper
import com.autsing.miga.presentation.model.Auth
import com.autsing.miga.presentation.model.Device
import com.autsing.miga.presentation.model.Scene
import com.autsing.miga.presentation.repository.DeviceRepository
import com.autsing.miga.presentation.repository.SceneRepository
import com.autsing.miga.tile.MainTileService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class MainUiState(
    val loading: Boolean = true,
    val auth: Auth? = null,
    val showedLogin: Boolean = false,
    val scenes: List<Scene> = emptyList(),
    val devices: List<Device> = emptyList(),
    val favoriteSceneIds: List<String> = emptyList(),
    val favoriteDeviceIds: List<String> = emptyList(),
    val deviceIconUrls: Map<String, String> = emptyMap(),
)

class MainViewModel : ViewModel() {

    private val fileHelper: FileHelper = FileHelper.instance
    private val serdeHelper: SerdeHelper = SerdeHelper.instance
    private val sceneRepository: SceneRepository = SceneRepository.instance
    private val deviceRepository: DeviceRepository = DeviceRepository.instance

    var uiState: MainUiState by mutableStateOf(MainUiState())
        private set

    private fun handleLoadAuth() = viewModelScope.async {
        runCatching {
            val authJson = fileHelper.readJson("auth.json").getOrThrow()
            val auth = serdeHelper.decode<Auth>(authJson).getOrThrow()

            uiState = uiState.copy(auth = auth)
        }
    }

    private fun handleLoadFavorite() = viewModelScope.async {
        runCatching {
            val favoriteSceneIdsJob = async(Dispatchers.IO) {
                sceneRepository.loadFavoriteSceneIds().getOrNull() ?: emptyList()
            }
            val favoriteDeviceIdsJob = async(Dispatchers.IO) {
                deviceRepository.loadFavoriteDeviceIds().getOrNull() ?: emptyList()
            }

            val favoriteSceneIds = favoriteSceneIdsJob.await()
            val favoriteDeviceIds = favoriteDeviceIdsJob.await()

            uiState = uiState.copy(
                favoriteSceneIds = favoriteSceneIds,
                favoriteDeviceIds = favoriteDeviceIds,
            )
        }
    }

    private fun handleLoadLocal() = viewModelScope.async {
        runCatching {
            val scenesJob = async(Dispatchers.IO) {
                sceneRepository.loadScenesLocal().getOrNull() ?: emptyList()
            }
            val devicesJob = async(Dispatchers.IO) {
                deviceRepository.loadDevicesLocal().getOrNull() ?: emptyList()
            }

            val scenes = scenesJob.await()
            val devices = devicesJob.await()

            val deviceIconUrlsJob = async(Dispatchers.IO) {
                deviceRepository.loadDeviceIconUrlsLocal().getOrNull() ?: emptyMap()
            }

            val deviceIconUrls = deviceIconUrlsJob.await()

            uiState = uiState.copy(
                scenes = scenes,
                devices = devices,
                deviceIconUrls = deviceIconUrls,
            )
        }
    }

    private fun handleLoadRemote() = viewModelScope.async {
        runCatching {
            val auth = uiState.auth ?: throw Exception("未登录")

            val scenesJob = async(Dispatchers.IO) {
                sceneRepository.loadScenesRemote(auth).getOrNull() ?: emptyList()
            }
            val devicesJob = async(Dispatchers.IO) {
                deviceRepository.loadDevicesRemote(auth).getOrNull() ?: emptyList()
            }

            val scenes = scenesJob.await()
            val devices = devicesJob.await()

            val deviceIconUrlsJob = async(Dispatchers.IO) {
                deviceRepository.loadDeviceIconsRemote(devices).getOrNull() ?: emptyMap()
            }

            val deviceIconUrls = deviceIconUrlsJob.await()

            Log.d(TAG, "handleLoadRemote: $scenes,$devices")

            uiState = uiState.copy(
                scenes = scenes,
                devices = devices,
                deviceIconUrls = deviceIconUrls,
            )
        }
    }

    fun handleLoad() = viewModelScope.launch {
        val minDelay = launch(Dispatchers.IO) { delay(500) }

        runCatching {
            uiState = uiState.copy(loading = true)

            handleLoadAuth().await().getOrThrow()

            val handleLoadFavoriteJob = handleLoadFavorite()
            val handleLoadLocalJob = handleLoadLocal()

            handleLoadFavoriteJob.await().getOrThrow()
            handleLoadLocalJob.await().getOrThrow()

            uiState = uiState.copy(loading = false)

            handleLoadRemote().await().getOrThrow()
        }.onFailure {
            Log.e(TAG, "handleLoad: ${it.stackTraceToString()}")
        }.also {
            minDelay.join()
            uiState = uiState.copy(loading = false)
        }
    }

    fun handleNavigateToLogin(context: Context) = viewModelScope.launch {
        LoginActivity.startActivity(context)
        uiState = uiState.copy(showedLogin = true)
    }

    fun handleRunScene(context: Context, scene: Scene) = viewModelScope.launch {
        RunSceneActivity.startActivity(context, scene.scene_id)
    }

    fun handleToggleSceneFavorite(context: Context, scene: Scene) = viewModelScope.launch {
        runCatching {
            val favoriteSceneIds = uiState.favoriteSceneIds
                .toMutableList()
                .apply { if (scene.scene_id in this) remove(scene.scene_id) else add(scene.scene_id) }
            val favoriteSceneIdsMap = favoriteSceneIds.withIndex()
                .associate { it.value to it.index }
            val scenes = uiState.scenes
                .sortedBy { favoriteSceneIdsMap[it.scene_id] ?: Int.MAX_VALUE }

            uiState = uiState.copy(
                scenes = scenes,
                favoriteSceneIds = favoriteSceneIds,
            )

            val favoriteScenesJson = serdeHelper.encode(favoriteSceneIds).getOrThrow()
            fileHelper.writeJson("favorite_scene_ids.json", favoriteScenesJson).getOrThrow()

            MainTileService.requestUpdate(context)
            FavoriteSceneComplicationService.requestUpdate(context)
        }.onFailure {
            Log.e(TAG, "handleToggleSceneFavorite: ${it.stackTraceToString()}")
        }
    }

    fun handleOpenDevice(context: Context, device: Device) {
        DeviceActivity.startActivity(context, device.model)
    }

    fun handleLogout() = viewModelScope.launch {
        runCatching {
            uiState = uiState.copy(loading = true)

            fileHelper.remove("auth.json").getOrThrow()

            uiState = uiState.copy(
                auth = null,
                scenes = emptyList(),
                devices = emptyList(),
            )
        }.onFailure {
            Log.e(TAG, "handleLogout: ${it.stackTraceToString()}")
        }.also {
            uiState = uiState.copy(loading = false)
        }
    }
}
