package com.autsing.miga.presentation.model

import kotlinx.serialization.Serializable

@Serializable
data class Auth(
    @Serializable(with = IntToStringTransformingSerializer::class)
    val userId: String,
    val ssecurity: String,
    val deviceId: String,
    val serviceToken: String,
)
