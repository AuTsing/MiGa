package com.autsing.miga.presentation.model

import kotlinx.serialization.Serializable

@Serializable
data class GetDevicesData(
    val getVirtualModel: Boolean,
    val getHuamiDevices: Int,
)
