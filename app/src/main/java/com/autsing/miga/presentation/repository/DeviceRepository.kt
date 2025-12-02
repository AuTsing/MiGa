package com.autsing.miga.presentation.repository

import com.autsing.miga.presentation.helper.ApiHelper
import com.autsing.miga.presentation.helper.FileHelper
import com.autsing.miga.presentation.helper.SerdeHelper
import com.autsing.miga.presentation.model.Auth
import com.autsing.miga.presentation.model.Device
import com.autsing.miga.presentation.model.DeviceInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeviceRepository(
    private val serdeHelper: SerdeHelper,
    private val fileHelper: FileHelper,
    private val apiHelper: ApiHelper,
) {

    companion object {
        lateinit var instance: DeviceRepository

        private const val FAVORITE_DEVICE_IDS_FILENAME = "favorite_device_ids.json"
        private const val DEVICES_FILENAME = "devices.json"
        private const val DEVICE_ICON_URLS_FILENAME = "device_icon_urls.json"
        private const val DEVICE_INFOS_FILENAME = "device_infos.json"
    }

    suspend fun loadFavoriteDeviceIds(): Result<List<String>> = withContext(Dispatchers.IO) {
        runCatching {
            val favoriteDeviceIdsJson = fileHelper.readJson(FAVORITE_DEVICE_IDS_FILENAME)
                .getOrThrow()
            val favoriteDeviceIds = serdeHelper.decode<List<String>>(favoriteDeviceIdsJson)
                .getOrThrow()
            return@runCatching favoriteDeviceIds
        }
    }

    suspend fun loadDevicesLocal(): Result<List<Device>> = withContext(Dispatchers.IO) {
        runCatching {
            val devicesJson = fileHelper.readJson(DEVICES_FILENAME).getOrThrow()
            val devices = serdeHelper.decode<List<Device>>(devicesJson)
                .getOrThrow()
                .sortedByDescending { it.isOnline }
            return@runCatching devices
        }
    }

    suspend fun loadDevicesRemote(
        auth: Auth,
    ): Result<List<Device>> = withContext(Dispatchers.IO) {
        runCatching {
            val devices = apiHelper.getDevices(auth).getOrThrow().sortedByDescending { it.isOnline }
            val devicesJson = serdeHelper.encode(devices).getOrThrow()
            fileHelper.writeJson(DEVICES_FILENAME, devicesJson).getOrThrow()
            return@runCatching devices
        }
    }

    suspend fun loadDeviceIconUrlsLocal(
    ): Result<Map<String, String>> = withContext(Dispatchers.IO) {
        runCatching {
            val deviceIconsJson = fileHelper.readJson(DEVICE_ICON_URLS_FILENAME).getOrThrow()
            val deviceIcons = serdeHelper.decode<Map<String, String>>(deviceIconsJson).getOrThrow()
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
            val deviceIconUrlsJson = serdeHelper.encode(deviceIconUrls).getOrThrow()
            fileHelper.writeJson(DEVICE_ICON_URLS_FILENAME, deviceIconUrlsJson).getOrThrow()
            return@runCatching deviceIconUrls
        }
    }

    suspend fun loadDeviceInfosLocal(
    ): Result<Map<String, DeviceInfo>> = withContext(Dispatchers.IO) {
        runCatching {
            val deviceInfosJson = fileHelper.readJson(DEVICE_INFOS_FILENAME).getOrThrow()
            val deviceInfos = serdeHelper.decode<Map<String, DeviceInfo>>(deviceInfosJson)
                .getOrThrow()
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
            val deviceInfosJson = serdeHelper.encode(deviceInfos).getOrThrow()
            fileHelper.writeJson(DEVICE_INFOS_FILENAME, deviceInfosJson).getOrThrow()
            return@runCatching deviceInfos
        }
    }

    suspend fun saveFavoriteDeviceIds(
        favoriteDeviceIds: List<String>,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val favoriteDeviceIdsJson = serdeHelper.encode(favoriteDeviceIds).getOrThrow()
            fileHelper.writeJson(FAVORITE_DEVICE_IDS_FILENAME, favoriteDeviceIdsJson).getOrThrow()
        }
    }
}
