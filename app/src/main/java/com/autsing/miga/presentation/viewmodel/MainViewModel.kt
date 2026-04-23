package com.autsing.miga.presentation.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autsing.miga.complication.FavoriteSceneComplicationService
import com.autsing.miga.presentation.activity.DeviceActivity
import com.autsing.miga.presentation.activity.LoginActivity
import com.autsing.miga.presentation.activity.RunSceneActivity
import com.autsing.miga.presentation.data.getOrDefault
import com.autsing.miga.presentation.model.Auth
import com.autsing.miga.presentation.model.Device
import com.autsing.miga.presentation.model.Scene
import com.autsing.miga.presentation.repository.AuthRepository
import com.autsing.miga.presentation.repository.DeviceRepository
import com.autsing.miga.presentation.repository.SceneRepository
import com.autsing.miga.tile.MainTileService
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

data class MainUiState(
    val authLoading: Boolean = true,
    val sceneLoading: Boolean = true,
    val deviceLoading: Boolean = true,
    val message: String = "",
    val auth: Auth? = null,
    val scenes: List<Scene> = emptyList(),
    val favoriteSceneIds: List<String> = emptyList(),
    val devices: List<Device> = emptyList(),
    val favoriteDeviceIds: List<String> = emptyList(),
    val deviceIconUrls: Map<String, String> = emptyMap(),
)

class MainViewModel : ViewModel() {

    private val authRepository: AuthRepository = AuthRepository.instance
    private val deviceRepository: DeviceRepository = DeviceRepository.instance
    private val sceneRepository: SceneRepository = SceneRepository.instance

    var uiState: MainUiState by mutableStateOf(MainUiState())
        private set

    init {
        handleLoadAuth()
        handleLoadScenes()
        handleLoadDevices()
    }

    fun handleLoadAuth() = viewModelScope.launch {
        runCatching {
            uiState = uiState.copy(authLoading = true)

            authRepository.loadLocalAuth().getOrNull()
            var auth = authRepository.getAuth().getOrNull()

            uiState = uiState.copy(
                authLoading = false,
                auth = auth,
            )

            if (auth != null) {
                return@runCatching
            }

            auth = authRepository.waitAuth().getOrThrow()

            uiState = uiState.copy(
                authLoading = false,
                auth = auth,
            )
        }.onFailure { e ->
            uiState = uiState.copy(
                authLoading = false,
                message = e.message ?: e.stackTraceToString(),
            )
        }
    }

    private fun handleLoadScenes() = viewModelScope.launch {
        runCatching {
            uiState = uiState.copy(sceneLoading = true)

            val locals = Pair(
                async { sceneRepository.getLocalScenes().getOrThrow() },
                async { sceneRepository.getFavoriteSceneIds().getOrThrow() },
            )
            val localScenes = locals.first.await()
            val favoriteSceneIds = locals.second.await()

            uiState = uiState.copy(
                scenes = localScenes,
                favoriteSceneIds = favoriteSceneIds,
            )

            val auth = authRepository.waitAuth().getOrThrow()
            val remoteScenes = sceneRepository.getRemoteScenes(auth).getOrDefault()

            uiState = uiState.copy(
                sceneLoading = false,
                scenes = remoteScenes,
            )
        }.onFailure { e ->
            uiState = uiState.copy(
                sceneLoading = false,
                message = e.message ?: e.stackTraceToString(),
            )
        }
    }

    private fun handleLoadDevices() = viewModelScope.launch {
        runCatching {
            uiState = uiState.copy(deviceLoading = true)

            val locals = Triple(
                async { deviceRepository.getLocalDevices().getOrThrow() },
                async { deviceRepository.getFavoriteDeviceIds().getOrThrow() },
                async { deviceRepository.getLocalDeviceIconUrls().getOrThrow() },
            )
            val localDevices = locals.first.await()
            val favoriteDeviceIds = locals.second.await()
            val localDeviceIconUrls = locals.third.await()

            uiState = uiState.copy(
                devices = localDevices,
                favoriteDeviceIds = favoriteDeviceIds,
                deviceIconUrls = localDeviceIconUrls,
            )

            val auth = authRepository.waitAuth().getOrThrow()
            val remoteDevices = deviceRepository.getRemoteDevices(auth).getOrDefault()
            val remoteDeviceIconUrls = deviceRepository.getRemoteDeviceIconUrls(remoteDevices)
                .getOrDefault()

            uiState = uiState.copy(
                deviceLoading = false,
                devices = remoteDevices,
                deviceIconUrls = remoteDeviceIconUrls,
            )
        }.onFailure { e ->
            uiState = uiState.copy(
                deviceLoading = false,
                message = e.message ?: e.stackTraceToString(),
            )
        }
    }

    fun handleNavigateToLogin(context: Context) = viewModelScope.launch {
        LoginActivity.startActivity(context)
    }

    fun handleRunScene(context: Context, scene: Scene) = viewModelScope.launch {
        RunSceneActivity.startActivity(context, scene.scene_id)
    }

    fun handleToggleSceneFavorite(context: Context, scene: Scene) = viewModelScope.launch {
        runCatching {
            val favoriteSceneIds = uiState.favoriteSceneIds
                .toMutableList()
                .apply {
                    if (scene.scene_id in this) {
                        remove(scene.scene_id)
                    } else {
                        add(scene.scene_id)
                    }
                }

            uiState = uiState.copy(favoriteSceneIds = favoriteSceneIds)

            sceneRepository.setFavoriteSceneIds(favoriteSceneIds).getOrThrow()

            MainTileService.requestUpdate(context)
            FavoriteSceneComplicationService.requestUpdate(context)
        }.onFailure { e ->
            uiState = uiState.copy(message = e.message ?: e.stackTraceToString())
        }
    }

    fun handleToggleDeviceFavorite(device: Device) = viewModelScope.launch {
        runCatching {
            val favoriteDeviceIds = uiState.favoriteDeviceIds
                .toMutableList()
                .apply {
                    if (device.did in this) {
                        remove(device.did)
                    } else {
                        add(device.did)
                    }
                }

            uiState = uiState.copy(favoriteDeviceIds = favoriteDeviceIds)

            deviceRepository.setFavoriteDeviceIds(favoriteDeviceIds).getOrThrow()
        }.onFailure { e ->
            uiState = uiState.copy(message = e.message ?: e.stackTraceToString())
        }
    }

    fun handleOpenDevice(context: Context, device: Device) {
        DeviceActivity.startActivity(context, device.model)
    }

    fun handleLogout() {
        TODO()
    }
}
