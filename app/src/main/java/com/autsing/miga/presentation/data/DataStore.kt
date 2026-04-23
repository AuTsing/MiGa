package com.autsing.miga.presentation.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.autsing.miga.presentation.model.Auth
import com.autsing.miga.presentation.model.Device
import com.autsing.miga.presentation.model.DeviceInfo
import com.autsing.miga.presentation.model.Scene
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val AUTH: Preferences.Key<String> = stringPreferencesKey("auth")
private val SCENES: Preferences.Key<String> = stringPreferencesKey("scenes")
private val FAVORITE_SCENE_IDS: Preferences.Key<String> = stringPreferencesKey("favorite_scene_ids")
private val DEVICES: Preferences.Key<String> = stringPreferencesKey("devices")
private val FAVORITE_DEVICE_IDS: Preferences.Key<String> =
    stringPreferencesKey("favorite_device_ids")
private val DEVICE_ICON_URLS: Preferences.Key<String> = stringPreferencesKey("device_icon_urls")

private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")
private val Context.sceneDataStore: DataStore<Preferences> by preferencesDataStore(name = "scene")
private val Context.deviceDataStore: DataStore<Preferences> by preferencesDataStore(name = "device")
private val Context.deviceInfoDataStore: DataStore<Preferences> by preferencesDataStore(name = "device_info")

suspend fun Context.getAuth(): Result<Auth?> = runCatching {
    authDataStore.data
        .map { preferences -> preferences[AUTH] ?: "" }
        .first()
        .decode<Auth>()
        .getOrNull()
}

suspend fun Context.setAuth(auth: Auth?): Result<Unit> = runCatching {
    authDataStore.updateData {
        it.toMutablePreferences().also { preferences ->
            if (auth == null) {
                preferences.remove(AUTH)
            } else {
                preferences[AUTH] = auth.encode().getOrThrow()
            }
        }
    }
}

suspend fun Context.getScenes(): Result<List<Scene>> = runCatching {
    sceneDataStore.data
        .map { preferences -> preferences[SCENES] ?: "" }
        .first()
        .decode<List<Scene>>()
        .getOrDefault()
}

suspend fun Context.setScenes(scenes: List<Scene>): Result<Unit> = runCatching {
    sceneDataStore.updateData {
        it.toMutablePreferences().also { preferences ->
            preferences[SCENES] = scenes.encode().getOrThrow()
        }
    }
}

suspend fun Context.getFavoriteSceneIds(): Result<List<String>> = runCatching {
    sceneDataStore.data
        .map { preferences -> preferences[FAVORITE_SCENE_IDS] ?: "" }
        .first()
        .decode<List<String>>()
        .getOrDefault()
}

suspend fun Context.setFavoriteSceneIds(ids: List<String>): Result<Unit> = runCatching {
    sceneDataStore.updateData {
        it.toMutablePreferences().also { preferences ->
            preferences[FAVORITE_SCENE_IDS] = ids.encode().getOrThrow()
        }
    }
}

suspend fun Context.getDevices(): Result<List<Device>> = runCatching {
    deviceDataStore.data
        .map { preferences -> preferences[DEVICES] ?: "" }
        .first()
        .decode<List<Device>>()
        .getOrDefault()
}

suspend fun Context.setDevices(devices: List<Device>): Result<Unit> = runCatching {
    deviceDataStore.updateData {
        it.toMutablePreferences().also { preferences ->
            preferences[DEVICES] = devices.encode().getOrThrow()
        }
    }
}

suspend fun Context.getFavoriteDeviceIds(): Result<List<String>> = runCatching {
    deviceDataStore.data
        .map { preferences -> preferences[FAVORITE_DEVICE_IDS] ?: "" }
        .first()
        .decode<List<String>>()
        .getOrDefault()
}

suspend fun Context.setFavoriteDeviceIds(ids: List<String>): Result<Unit> = runCatching {
    deviceDataStore.updateData {
        it.toMutablePreferences().also { preferences ->
            preferences[FAVORITE_DEVICE_IDS] = ids.encode().getOrThrow()
        }
    }
}

suspend fun Context.getDeviceIconUrls(): Result<Map<String, String>> = runCatching {
    deviceDataStore.data
        .map { preferences -> preferences[DEVICE_ICON_URLS] ?: "" }
        .first()
        .decode<Map<String, String>>()
        .getOrDefault()
}

suspend fun Context.setDeviceIconUrls(urls: Map<String, String>): Result<Unit> = runCatching {
    deviceDataStore.updateData {
        it.toMutablePreferences().also { preferences ->
            preferences[DEVICE_ICON_URLS] = urls.encode().getOrThrow()
        }
    }
}

suspend fun Context.getDeviceInfo(model: String): Result<DeviceInfo?> = runCatching {
    deviceInfoDataStore.data
        .map { preferences -> preferences[stringPreferencesKey(model)] ?: "" }
        .first()
        .decode<DeviceInfo>()
        .getOrNull()
}

suspend fun Context.setDeviceInfo(model: String, info: DeviceInfo): Result<Unit> = runCatching {
    deviceDataStore.updateData {
        it.toMutablePreferences().also { preferences ->
            preferences[stringPreferencesKey(model)] = info.encode().getOrThrow()
        }
    }
}
