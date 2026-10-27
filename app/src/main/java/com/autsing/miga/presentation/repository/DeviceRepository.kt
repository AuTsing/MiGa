package com.autsing.miga.presentation.repository

import android.annotation.SuppressLint
import android.content.Context
import com.autsing.miga.presentation.api.reqGetDeviceIconUrl
import com.autsing.miga.presentation.api.reqGetDevices
import com.autsing.miga.presentation.data.getDeviceIconUrls
import com.autsing.miga.presentation.data.getDevices
import com.autsing.miga.presentation.data.getFavoriteDeviceIds
import com.autsing.miga.presentation.data.setDeviceIconUrls
import com.autsing.miga.presentation.data.setDevices
import com.autsing.miga.presentation.data.setFavoriteDeviceIds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
data class Device(
    val did: String,
    val name: String,
    val isOnline: Boolean,
    val model: String,
)

fun List<Device>.sort(favoriteDeviceIds: List<String>): List<Device> {
    val favoriteDeviceIdsMap = favoriteDeviceIds.withIndex()
        .associate { it.value to it.index }
    return this.sortedBy { favoriteDeviceIdsMap[it.did] ?: Int.MAX_VALUE }
}

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
            val deviceIconUrls = devices.map { it.model }.zip(urls).toMap()

            context.setDeviceIconUrls(deviceIconUrls).getOrThrow()

            deviceIconUrls
        }
    }
}
