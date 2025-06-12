package com.autsing.miga.presentation.repository

import com.autsing.miga.presentation.helper.ApiHelper
import com.autsing.miga.presentation.helper.FileHelper
import com.autsing.miga.presentation.model.Auth
import com.autsing.miga.presentation.model.Device
import com.autsing.miga.presentation.model.DeviceInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class DeviceRepository(
    private val fileHelper: FileHelper,
    private val apiHelper: ApiHelper,
) {

    companion object {
        lateinit var instance: DeviceRepository

        private const val DEVICES_FILENAME = "devices.json"
        private const val DEVICE_ICON_URLS_FILENAME = "device_icon_urls.json"
        private const val DEVICE_INFOS_FILENAME = "device_infos.json"
    }

    suspend fun loadDevicesLocal(): Result<List<Device>> = withContext(Dispatchers.IO) {
        runCatching {
            val devicesJson = fileHelper.readJson(DEVICES_FILENAME).getOrThrow()
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
            fileHelper.writeJson(DEVICES_FILENAME, devicesJson).getOrThrow()
            return@runCatching devices
        }
    }

    suspend fun loadDeviceIconUrlsLocal(
    ): Result<Map<String, String>> = withContext(Dispatchers.IO) {
        runCatching {
            val deviceIconsJson = fileHelper.readJson(DEVICE_ICON_URLS_FILENAME).getOrThrow()
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
            fileHelper.writeJson(DEVICE_ICON_URLS_FILENAME, deviceIconUrlsJson).getOrThrow()
            return@runCatching deviceIconUrls
        }
    }

    suspend fun loadDeviceInfosLocal(
    ): Result<Map<String, DeviceInfo>> = withContext(Dispatchers.IO) {
        runCatching {
            val deviceInfosJson = fileHelper.readJson(DEVICE_INFOS_FILENAME).getOrThrow()
            val deviceInfos = Json.decodeFromString<Map<String, DeviceInfo>>(deviceInfosJson)
            return@runCatching deviceInfos
        }
    }

    suspend fun loadDeviceInfosRemote(
        device: Device,
    ): Result<Map<String, DeviceInfo>> = withContext(Dispatchers.IO) {
        runCatching {
            val deviceInfo = apiHelper.getDeviceInfo(device).getOrThrow()
            val deviceInfos = loadDeviceInfosLocal().getOrDefault(emptyMap())
                .toMutableMap()
                .apply { set(device.model, deviceInfo) }
            val deviceInfosJson = Json.encodeToString(deviceInfos)
            fileHelper.writeJson(DEVICE_INFOS_FILENAME, deviceInfosJson).getOrThrow()
            return@runCatching deviceInfos
        }
    }
}
