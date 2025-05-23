package com.autsing.miga.presentation.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class LoginLpResponse(
    @Serializable(with = IntToStringTransformingSerializer::class)
    val userId: String,
    val ssecurity: String,
    val location: String,
)
