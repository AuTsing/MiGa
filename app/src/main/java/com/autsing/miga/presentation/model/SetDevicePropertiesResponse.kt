package com.autsing.miga.presentation.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@Serializable
data class SetDevicePropertiesResponse(
    val code: Int,
    val message: String,
    val result: List<Result>,
) {

    @OptIn(ExperimentalSerializationApi::class)
    @Serializable
    @JsonIgnoreUnknownKeys
    data class Result(
        val did: String,
        val iid: String,
        val siid: Int,
        val piid: Int,
        val code: Int,
    )
}
