package com.autsing.miga.presentation.model

import kotlinx.serialization.Serializable

@Serializable
data class Auth(
    val userId: Int,
    val ssecurity: String,
    val deviceId: String,
    val serviceToken: String,
)
