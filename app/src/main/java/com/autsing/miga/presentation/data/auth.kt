package com.autsing.miga.presentation.data

import kotlinx.serialization.Serializable

@Serializable
data class Auth(
    val deviceId: String,
    val cookies: Map<String, String>,
)
