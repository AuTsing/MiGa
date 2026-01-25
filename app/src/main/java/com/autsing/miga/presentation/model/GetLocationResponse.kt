package com.autsing.miga.presentation.model

import kotlinx.serialization.Serializable

@Serializable
data class GetLocationResponse(
    val location: String,
    val ssecurity: String,
)
