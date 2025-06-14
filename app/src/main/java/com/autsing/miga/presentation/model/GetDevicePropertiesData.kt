package com.autsing.miga.presentation.model

import kotlinx.serialization.Serializable

@Serializable
data class GetDevicePropertiesData(
    val params: List<Params>,
) {

    @Serializable
    data class Params(
        val did: String,
        val siid: Int,
        val piid: Int,
    )
}
