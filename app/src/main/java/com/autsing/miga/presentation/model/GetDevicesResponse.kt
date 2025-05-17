package com.autsing.miga.presentation.model

import kotlinx.serialization.Serializable

@Serializable
data class GetDevicesResult(
    val list: List<Device>,
)

@Serializable
data class GetDevicesResponse(
    val code: Int,
    val message: String,
    val result: GetDevicesResult,
)
