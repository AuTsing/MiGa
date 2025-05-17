package com.autsing.miga.presentation.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class GetHomesResult(
    val homelist: List<Home>,
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class GetHomesResponse(
    val code: Int,
    val message: String,
    val result: GetHomesResult,
)
