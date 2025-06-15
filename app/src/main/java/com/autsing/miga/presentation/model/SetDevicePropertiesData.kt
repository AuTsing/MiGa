package com.autsing.miga.presentation.model

import kotlinx.serialization.Serializable

@Serializable
data class SetDevicePropertiesData(
    val params: List<Params>,
) {

    @Serializable
    data class Params(
        val did: String,
        val siid: Int,
        val piid: Int,
        val value: DevicePropertyValue,
    )
}
