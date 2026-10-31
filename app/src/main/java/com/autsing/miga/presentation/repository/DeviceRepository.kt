package com.autsing.miga.presentation.repository

import android.annotation.SuppressLint
import android.content.Context
import com.autsing.miga.presentation.api.reqGetDeviceIconUrl
import com.autsing.miga.presentation.api.reqGetDeviceInfoV2
import com.autsing.miga.presentation.api.reqGetDevicePropertiesV2
import com.autsing.miga.presentation.api.reqGetDevicesV2
import com.autsing.miga.presentation.api.reqSetDevicePropertyV2
import com.autsing.miga.presentation.data.Auth
import com.autsing.miga.presentation.data.Device
import com.autsing.miga.presentation.data.DeviceInfoV2
import com.autsing.miga.presentation.data.DevicePropertyValueV2
import com.autsing.miga.presentation.data.getDeviceIconUrls
import com.autsing.miga.presentation.data.getDeviceInfo
import com.autsing.miga.presentation.data.getDevices
import com.autsing.miga.presentation.data.getFavoriteDeviceIds
import com.autsing.miga.presentation.data.setDeviceIconUrls
import com.autsing.miga.presentation.data.setDeviceInfo
import com.autsing.miga.presentation.data.setDevices
import com.autsing.miga.presentation.data.setFavoriteDeviceIds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

class DeviceRepository(
    private val context: Context,
) {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var instance: DeviceRepository
    }

    suspend fun getLocalDevices(): Result<List<Device>> = withContext(Dispatchers.IO) {
        runCatching { context.getDevices().getOrThrow().sortedByDescending { it.isOnline } }
    }

    suspend fun getFavoriteDeviceIds(): Result<List<String>> = withContext(Dispatchers.IO) {
        runCatching { context.getFavoriteDeviceIds().getOrThrow() }
    }

    suspend fun setFavoriteDeviceIds(ids: List<String>) = withContext(Dispatchers.IO) {
        runCatching { context.setFavoriteDeviceIds(ids).getOrThrow() }
    }

    suspend fun getLocalDeviceIconUrls(): Result<Map<String, String>> =
        withContext(Dispatchers.IO) {
            runCatching { context.getDeviceIconUrls().getOrThrow() }
        }

    suspend fun getRemoteDevices(auth: Auth): Result<List<Device>> = withContext(Dispatchers.IO) {
        runCatching {
            val devices = reqGetDevicesV2(auth).getOrThrow().sortedByDescending { it.isOnline }

            context.setDevices(devices).getOrThrow()

            devices
        }
    }

    suspend fun getRemoteDeviceIconUrls(
        devices: List<Device>,
    ): Result<Map<String, String>> = withContext(Dispatchers.IO) {
        runCatching {
            val urls = devices
                .map { async { reqGetDeviceIconUrl(it.model).getOrDefault("") } }
                .awaitAll()
                .withIndex()
                .associate { (i, url) -> devices[i].model to url }

            context.setDeviceIconUrls(urls).getOrThrow()

            urls
        }
    }

    suspend fun getLocalDeviceInfo(
        key: String,
    ): Result<DeviceInfoV2> = withContext(Dispatchers.IO) {
        runCatching {
            val device = context.getDeviceInfo(key).getOrThrow()
                ?: throw Exception("$key 不存在")

            device
        }
    }

    suspend fun getRemoteDeviceInfo(
        key: String,
    ): Result<DeviceInfoV2> = withContext(Dispatchers.IO) {
        runCatching {
            val info = reqGetDeviceInfoV2(key).getOrThrow()

            context.setDeviceInfo(key, info).getOrThrow()

            info
        }
    }

    suspend fun getDeviceProperties(
        device: Device,
        info: DeviceInfoV2,
        auth: Auth,
    ): Result<Map<DeviceInfoV2.Property, DevicePropertyValueV2>> = withContext(Dispatchers.IO) {
        runCatching { reqGetDevicePropertiesV2(device, info, auth).getOrThrow() }
    }

    suspend fun setDeviceProperty(
        device: Device,
        property: DeviceInfoV2.Property,
        value: DevicePropertyValueV2,
        auth: Auth,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching { reqSetDevicePropertyV2(device, property, value, auth).getOrThrow() }
    }
}
