package com.autsing.miga.presentation.repository

import android.annotation.SuppressLint
import android.content.Context
import com.autsing.miga.presentation.api.reqGetDeviceIconUrl
import com.autsing.miga.presentation.api.reqGetDeviceInfo
import com.autsing.miga.presentation.api.reqGetDeviceProperties
import com.autsing.miga.presentation.api.reqGetDevices
import com.autsing.miga.presentation.api.reqSetDeviceProperty
import com.autsing.miga.presentation.data.Device
import com.autsing.miga.presentation.data.DeviceInfo
import com.autsing.miga.presentation.data.DevicePropertyValue
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
            val devices = reqGetDevices(auth).getOrThrow().sortedByDescending { it.isOnline }

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
        model: String,
    ): Result<DeviceInfo> = withContext(Dispatchers.IO) {
        runCatching {
            val device = context.getDeviceInfo(model).getOrThrow()
                ?: throw Exception("$model 不存在")

            device
        }
    }

    suspend fun getRemoteDeviceInfo(
        model: String,
    ): Result<DeviceInfo> = withContext(Dispatchers.IO) {
        runCatching {
            val info = reqGetDeviceInfo(model).getOrThrow()

            context.setDeviceInfo(model, info).getOrThrow()

            info
        }
    }

    suspend fun getDeviceProperties(
        auth: Auth,
        device: Device,
        info: DeviceInfo,
    ): Result<List<Pair<DeviceInfo.Property, DevicePropertyValue>>> = withContext(Dispatchers.IO) {
        runCatching { reqGetDeviceProperties(auth, device, info).getOrThrow() }
    }

    suspend fun setDeviceProperty(
        auth: Auth,
        device: Device,
        property: DeviceInfo.Property,
        value: DevicePropertyValue,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching { reqSetDeviceProperty(auth, device, property, value).getOrThrow() }
    }
}
