package com.autsing.miga.presentation.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class GetScenesResult(
    val scene_info_list: List<Scene>,
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class GetScenesResponse(
    val code: Int,
    val message: String,
    val result: GetScenesResult,
)
