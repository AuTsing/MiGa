package com.autsing.miga.presentation.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autsing.miga.complication.FavoriteSceneComplicationService
import com.autsing.miga.presentation.activity.LoginActivity
import com.autsing.miga.presentation.helper.ApiHelper
import com.autsing.miga.presentation.helper.Constants.TAG
import com.autsing.miga.presentation.helper.FileHelper
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
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

data class MainUiState(
    val loading: Boolean = true,
    val auth: Auth? = null,
    val showedLogin: Boolean = false,
    val scenes: List<Scene> = emptyList(),
    val devices: List<Device> = emptyList(),
    val favoriteScenes: Set<String> = emptySet(),
    val deviceIconUrls: Map<String, String> = emptyMap(),
)

class MainViewModel : ViewModel() {

    private val fileHelper: FileHelper = FileHelper.instance
    private val apiHelper: ApiHelper = ApiHelper.instance
    private val sceneRepository: SceneRepository = SceneRepository.instance
    private val deviceRepository: DeviceRepository = DeviceRepository.instance

    var uiState: MainUiState by mutableStateOf(MainUiState())
        private set

    fun handleLoad() = viewModelScope.launch(Dispatchers.IO) {
        val minDelay = launch(Dispatchers.IO) { delay(500) }

        runCatching {
            withContext(Dispatchers.Main) {
                uiState = uiState.copy(loading = true)
            }

            val authJson = fileHelper.readJson("auth.json").getOrThrow()
            val auth = Json.decodeFromString<Auth>(authJson)

            val scenesJob = async(Dispatchers.IO) {
                val favoriteSceneIds = sceneRepository.loadFavoriteSceneIds()
                    .getOrDefault(emptySet())
                val scenes = sceneRepository.loadScenesLocal(favoriteSceneIds)
                    .getOrElse {
                        sceneRepository
                            .loadScenesRemote(auth, favoriteSceneIds)
                            .getOrDefault(emptyList())
                    }
                return@async Pair(favoriteSceneIds, scenes)
            }
            val devicesJob = async(Dispatchers.IO) {
                val devices = deviceRepository.loadDevicesLocal()
                    .getOrElse {
                        deviceRepository.loadDevicesRemote(auth).getOrDefault(emptyList())
                    }
                val deviceIconUrls = deviceRepository.loadDeviceIconUrlsLocal()
                    .getOrElse {
                        deviceRepository.loadDeviceIconsRemote(devices).getOrDefault(emptyMap())
                    }
                return@async Pair(devices, deviceIconUrls)
            }

            val (favoriteScenes, scenes) = scenesJob.await()
            val (devices, deviceIconUrls) = devicesJob.await()

            withContext(Dispatchers.Main) {
                uiState = uiState.copy(
                    auth = auth,
                    scenes = scenes,
                    devices = devices,
                    favoriteScenes = favoriteScenes,
                    deviceIconUrls = deviceIconUrls,
                )
            }
        }.onFailure {
            Log.e(TAG, "handleLoad: ${it.stackTraceToString()}")
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

    fun handleReload(auth: Auth) = viewModelScope.launch(Dispatchers.IO) {
        runCatching {
            withContext(Dispatchers.Main) {
                uiState = uiState.copy(loading = true)
            }

            val scenesJob = async(Dispatchers.IO) {
                val favoriteSceneIds = sceneRepository.loadFavoriteSceneIds()
                    .getOrDefault(emptySet())
                val scenes = sceneRepository
                    .loadScenesRemote(auth, favoriteSceneIds)
                    .getOrDefault(emptyList())
                return@async Pair(favoriteSceneIds, scenes)
            }
            val devicesJob = async(Dispatchers.IO) {
                val devices = deviceRepository.loadDevicesRemote(auth).getOrDefault(emptyList())
                val deviceIconUrls = deviceRepository
                    .loadDeviceIconsRemote(devices)
                    .getOrDefault(emptyMap())
                return@async Pair(devices, deviceIconUrls)
            }

            val (favoriteScenes, scenes) = scenesJob.await()
            val (devices, deviceIconUrls) = devicesJob.await()

            withContext(Dispatchers.Main) {
                uiState = uiState.copy(
                    scenes = scenes,
                    devices = devices,
                    favoriteScenes = favoriteScenes,
                    deviceIconUrls = deviceIconUrls,
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

    fun handleRunScene(
        context: Context,
        auth: Auth,
        scene: Scene,
    ) = viewModelScope.launch(Dispatchers.IO) {
        runCatching {
            val message = apiHelper.runScene(auth, scene).getOrThrow()

            withContext(Dispatchers.Main) {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }.onFailure {
            Log.e(TAG, "handleRunScene: ${it.stackTraceToString()}")
        }
    }

    fun handleToggleSceneFavorite(
        context: Context,
        scene: Scene,
    ) = viewModelScope.launch(Dispatchers.IO) {
        runCatching {
            val favoriteScenes = uiState.favoriteScenes
                .toMutableSet()
                .apply { if (scene.scene_id in this) remove(scene.scene_id) else add(scene.scene_id) }
            val scenes = uiState.scenes
                .sortedByDescending { it.scene_id in favoriteScenes }

            withContext(Dispatchers.Main) {
                uiState = uiState.copy(
                    scenes = scenes,
                    favoriteScenes = favoriteScenes,
                )
            }

            val favoriteScenesJson = Json.encodeToString(favoriteScenes)
            fileHelper.writeJson("favorite_scene_ids.json", favoriteScenesJson).getOrThrow()

            MainTileService.requestUpdate(context)
            FavoriteSceneComplicationService.requestUpdate(context)
        }.onFailure {
            Log.e(TAG, "handleToggleSceneFavorite: ${it.stackTraceToString()}")
        }
    }
}
