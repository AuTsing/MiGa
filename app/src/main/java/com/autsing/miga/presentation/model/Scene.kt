package com.autsing.miga.presentation.model

import kotlinx.serialization.Serializable

@Serializable
data class Scene(
    val scene_id: String,
    val name: String,
    val icon_url: String,
)
