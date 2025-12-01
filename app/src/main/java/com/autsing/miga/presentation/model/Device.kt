package com.autsing.miga.presentation.model

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
