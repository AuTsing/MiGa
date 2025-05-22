package com.autsing.miga.presentation.repository

import com.autsing.miga.presentation.helper.ApiHelper
import com.autsing.miga.presentation.helper.FileHelper
import com.autsing.miga.presentation.model.Auth
import com.autsing.miga.presentation.model.Device
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class DeviceRepository(
    private val fileHelper: FileHelper,
    private val apiHelper: ApiHelper,
) {

    companion object {
        lateinit var instance: DeviceRepository
    }

    suspend fun loadDevicesLocal(): Result<List<Device>> = withContext(Dispatchers.IO) {
        runCatching {
            val devicesJson = fileHelper.readJson("devices.json").getOrThrow()
            val devices = Json.decodeFromString<List<Device>>(devicesJson)
                .sortedByDescending { it.isOnline }
            return@runCatching devices
        }
    }

    suspend fun loadDevicesRemote(
        auth: Auth,
    ): Result<List<Device>> = withContext(Dispatchers.IO) {
        runCatching {
            val devices = apiHelper.getDevices(auth).getOrThrow().sortedByDescending { it.isOnline }
            val devicesJson = Json.encodeToString(devices)
            fileHelper.writeJson("devices.json", devicesJson).getOrThrow()
            return@runCatching devices
        }
    }

    suspend fun loadDeviceIconUrlsLocal(
    ): Result<Map<String, String>> = withContext(Dispatchers.IO) {
        runCatching {
            val deviceIconsJson = fileHelper.readJson("device_icon_urls.json").getOrThrow()
            val deviceIcons = Json.decodeFromString<Map<String, String>>(deviceIconsJson)
            return@runCatching deviceIcons
        }
    }

    suspend fun loadDeviceIconsRemote(
        devices: List<Device>,
    ): Result<Map<String, String>> = withContext(Dispatchers.IO) {
        runCatching {
            val deviceIconUrls = devices.associate {
                Pair(it.model, apiHelper.getDeviceIconUrl(it.model).getOrDefault(""))
            }
            val deviceIconUrlsJson = Json.encodeToString(deviceIconUrls)
            fileHelper.writeJson("device_icon_urls.json", deviceIconUrlsJson).getOrThrow()
            return@runCatching deviceIconUrls
        }
    }
}
