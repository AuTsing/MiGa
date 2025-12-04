package com.autsing.miga.presentation.model

import kotlinx.serialization.Serializable

@Serializable
data class Scene(
    val scene_id: String,
    val name: String,
    val icon_url: String,
)

fun List<Scene>.sort(favoriteSceneIds: List<String>): List<Scene> {
    val favoriteSceneIdsMap = favoriteSceneIds.withIndex()
        .associate { it.value to it.index }
    return this.sortedBy { favoriteSceneIdsMap[it.scene_id] ?: Int.MAX_VALUE }
}

fun getMockScene(index: Int? = 0): Scene {
    return Scene(
        scene_id = "abcde$index",
        name = "开灯$index",
        icon_url = "http://localhost",
    )
}
