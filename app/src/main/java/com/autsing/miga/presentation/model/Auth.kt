package com.autsing.miga.presentation.model

import kotlinx.serialization.Serializable

@Serializable
data class Auth(
    val userId: Long,
    val ssecurity: String,
    val deviceId: String,
    val serviceToken: String,
)
