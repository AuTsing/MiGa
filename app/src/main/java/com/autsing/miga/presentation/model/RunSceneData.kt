package com.autsing.miga.presentation.model

import kotlinx.serialization.Serializable

@Serializable
data class RunSceneData(
    val scene_id: String,
    val trigger_key: String,
)
