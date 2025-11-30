package com.autsing.miga.presentation.model

import kotlinx.serialization.Serializable

@Serializable
data class Device(
    val did: String,
    val name: String,
    val isOnline: Boolean,
    val model: String,
)
