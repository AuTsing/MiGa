package com.autsing.miga.presentation.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autsing.miga.presentation.activity.LoginActivity
import com.autsing.miga.presentation.model.Auth
import com.autsing.miga.presentation.model.Device
import com.autsing.miga.presentation.model.Scene
import com.autsing.miga.presentation.helper.ApiHelper
import com.autsing.miga.presentation.helper.Constants.TAG
import com.autsing.miga.presentation.helper.FileHelper
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

            val authJson = FileHelper.instance.readJson("auth.json").getOrThrow()
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

    private suspend fun loadFavoriteScenes(): Result<Set<String>> = withContext(Dispatchers.IO) {
        runCatching {
            val favoriteScenesJson = FileHelper.instance.readJson("favorite_scenes.json").getOrThrow()
            val favoriteScenes = Json.decodeFromString<Set<String>>(favoriteScenesJson)
            return@runCatching favoriteScenes
        }
    }

    private suspend fun loadScenesLocal(
        favoriteScenes: Set<String>,
    ): Result<List<Scene>> = withContext(Dispatchers.IO) {
        runCatching {
            val scenesJson = FileHelper.instance.readJson("scenes.json").getOrThrow()
            val scenes = Json.decodeFromString<List<Scene>>(scenesJson)
                .sortedByDescending { it.scene_id in favoriteScenes }
            return@runCatching scenes
        }
    }

    private suspend fun loadScenesRemote(
        auth: Auth,
        favoriteScenes: Set<String>,
    ): Result<List<Scene>> = withContext(Dispatchers.IO) {
        runCatching {
            val scenes = ApiHelper.instance.getScenes(auth).getOrThrow()
                .sortedByDescending { it.scene_id in favoriteScenes }
            val scenesJson = Json.encodeToString(scenes)
            FileHelper.instance.writeJson("scenes.json", scenesJson).getOrThrow()
            return@runCatching scenes
        }
    }

    private suspend fun loadDevicesLocal(): Result<List<Device>> = withContext(Dispatchers.IO) {
        runCatching {
            val devicesJson = FileHelper.instance.readJson("devices.json").getOrThrow()
            val devices = Json.decodeFromString<List<Device>>(devicesJson)
                .sortedByDescending { it.isOnline }
            return@runCatching devices
        }
    }

    private suspend fun loadDevicesRemote(
        auth: Auth,
    ): Result<List<Device>> = withContext(Dispatchers.IO) {
        runCatching {
            val devices = ApiHelper.instance.getDevices(auth).getOrThrow()
                .sortedByDescending { it.isOnline }
            val devicesJson = Json.encodeToString(devices)
            FileHelper.instance.writeJson("devices.json", devicesJson).getOrThrow()
            return@runCatching devices
        }
    }

    fun handleLoadScenesAndDevices(auth: Auth) = viewModelScope.launch(Dispatchers.IO) {
        runCatching {
            withContext(Dispatchers.Main) {
                uiState = uiState.copy(loading = true)
            }

            val scenesJob = async(Dispatchers.IO) {
                val favoriteScenes = loadFavoriteScenes().getOrDefault(emptySet())
                val scenes = loadScenesLocal(favoriteScenes)
                    .getOrElse { loadScenesRemote(auth, favoriteScenes).getOrDefault(emptyList()) }
                return@async Pair(favoriteScenes, scenes)
            }
            val devicesJob = async(Dispatchers.IO) {
                loadDevicesLocal().getOrElse { loadDevicesRemote(auth).getOrDefault(emptyList()) }
            }

            val (favoriteScenes, scenes) = scenesJob.await()
            val devices = devicesJob.await()

            withContext(Dispatchers.Main) {
                uiState = uiState.copy(
                    scenes = scenes,
                    devices = devices,
                    favoriteScenes = favoriteScenes,
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
                val favoriteScenes = loadFavoriteScenes().getOrDefault(emptySet())
                val scenes = loadScenesLocal(favoriteScenes)
                    .getOrElse { loadScenesRemote(auth, favoriteScenes).getOrDefault(emptyList()) }
                return@async Pair(favoriteScenes, scenes)
            }
            val devicesJob = async(Dispatchers.IO) {
                loadDevicesRemote(auth).getOrDefault(emptyList())
            }

            val (favoriteScenes, scenes) = scenesJob.await()
            val devices = devicesJob.await()

            withContext(Dispatchers.Main) {
                uiState = uiState.copy(
                    scenes = scenes,
                    devices = devices,
                    favoriteScenes = favoriteScenes,
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
            val message = ApiHelper.instance.runScene(auth, scene).getOrThrow()

            withContext(Dispatchers.Main) {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }.onFailure {
            Log.e(TAG, "handleRunScene: ${it.stackTraceToString()}")
        }
    }

    fun handleToggleSceneFavorite(scene: Scene) = viewModelScope.launch(Dispatchers.IO) {
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
            FileHelper.instance.writeJson("favorite_scenes.json", favoriteScenesJson).getOrThrow()
        }.onFailure {
            Log.e(TAG, "handleToggleSceneFavorite: ${it.stackTraceToString()}")
        }
    }
}
