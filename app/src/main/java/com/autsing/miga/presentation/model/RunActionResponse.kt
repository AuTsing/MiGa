package com.autsing.miga.presentation.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@Serializable
data class RunActionResponse(
    val code: Int,
    val message: String,
    val result: Result,
) {

    @OptIn(ExperimentalSerializationApi::class)
    @Serializable
    @JsonIgnoreUnknownKeys
    data class Result(
        val did: String,
        val siid: Int,
        val aiid: Int,
        val code: Int,
        @SerialName("out")
        val _out: List<DevicePropertyValue>? = null,
    )
}
