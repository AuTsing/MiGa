package com.autsing.miga.presentation.repository

import android.annotation.SuppressLint
import android.content.Context
import com.autsing.miga.presentation.data.getDeviceIconUrls
import com.autsing.miga.presentation.data.getDeviceInfo
import com.autsing.miga.presentation.data.getDevices
import com.autsing.miga.presentation.data.getFavoriteDeviceIds
import com.autsing.miga.presentation.data.requestGetDeviceIconUrl
import com.autsing.miga.presentation.data.requestGetDeviceInfo
import com.autsing.miga.presentation.data.requestGetDeviceProperties
import com.autsing.miga.presentation.data.requestGetDevices
import com.autsing.miga.presentation.data.requestSetDeviceProperty
import com.autsing.miga.presentation.data.setDeviceIconUrls
import com.autsing.miga.presentation.data.setDeviceInfo
import com.autsing.miga.presentation.data.setDevices
import com.autsing.miga.presentation.data.setFavoriteDeviceIds
import com.autsing.miga.presentation.model.Auth
import com.autsing.miga.presentation.model.Device
import com.autsing.miga.presentation.model.DeviceInfo
import com.autsing.miga.presentation.model.DevicePropertyValue
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
            val devices = requestGetDevices(auth).getOrThrow().sortedByDescending { it.isOnline }
            context.setDevices(devices).getOrThrow()

            devices
        }
    }

    suspend fun getRemoteDeviceIconUrls(
        devices: List<Device>,
    ): Result<Map<String, String>> = withContext(Dispatchers.IO) {
        runCatching {
            val urls = devices
                .map { async { requestGetDeviceIconUrl(it.model).getOrDefault("") } }
                .awaitAll()
            val deviceIconUrls = devices.map { it.model }.zip(urls).toMap()
            context.setDeviceIconUrls(deviceIconUrls).getOrThrow()

            deviceIconUrls
        }
    }

    suspend fun getLocalDeviceInfo(
        model: String,
    ): Result<DeviceInfo> = withContext(Dispatchers.IO) {
        runCatching {
            context.getDeviceInfo(model).getOrThrow() ?: throw Exception("$model is null")
        }
    }

    suspend fun getRemoteDeviceInfo(
        model: String,
    ): Result<DeviceInfo> = withContext(Dispatchers.IO) {
        runCatching {
            val info = requestGetDeviceInfo(model).getOrThrow()
            context.setDeviceInfo(model, info).getOrThrow()

            info
        }
    }

    suspend fun getDeviceProperties(
        auth: Auth,
        device: Device,
        deviceInfo: DeviceInfo,
    ): Result<List<Pair<DeviceInfo.Property, DevicePropertyValue>>> = withContext(Dispatchers.IO) {
        runCatching { requestGetDeviceProperties(auth, device, deviceInfo).getOrThrow() }
    }

    suspend fun setDeviceProperty(
        auth: Auth,
        device: Device,
        deviceProperty: DeviceInfo.Property,
        value: DevicePropertyValue,
    ): Result<Pair<DeviceInfo.Property, DevicePropertyValue>> = withContext(Dispatchers.IO) {
        runCatching { requestSetDeviceProperty(auth, device, deviceProperty, value).getOrThrow() }
    }
}
