package com.autsing.miga.presentation.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RunActionReqData(
    val params: Params,
) {

    @Serializable
    data class Params(
        val did: String,
        val siid: Int,
        val aiid: Int,
        @SerialName("in")
        val _in: List<DevicePropertyValueV2>,
    )
}

@Serializable
data class RunActionRespData(
    val code: Int,
    val message: String,
    val result: Result,
) {

    @Serializable
    data class Result(
        val did: String,
        val siid: Int,
        val aiid: Int,
        val code: Int,
        @SerialName("out")
        val _out: List<DevicePropertyValueV2>? = null,
    )
}
