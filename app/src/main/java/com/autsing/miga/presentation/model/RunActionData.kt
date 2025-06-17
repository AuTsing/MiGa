package com.autsing.miga.presentation.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RunActionData(
    val params: Params,
) {

    @Serializable
    data class Params(
        val did: String,
        val siid: Int,
        val aiid: Int,
        @SerialName("in")
        val _in: List<DevicePropertyValue>,
    )
}
