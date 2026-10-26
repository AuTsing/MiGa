package com.autsing.miga.presentation.repository

import android.annotation.SuppressLint
import android.content.Context
import com.autsing.miga.presentation.api.reqGetScenes
import com.autsing.miga.presentation.data.getFavoriteSceneIds
import com.autsing.miga.presentation.data.getScenes
import com.autsing.miga.presentation.data.setFavoriteSceneIds
import com.autsing.miga.presentation.data.setScenes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

class SceneRepository(
    private val context: Context,
) {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var instance: SceneRepository
    }

    suspend fun getLocalScenes(): Result<List<Scene>> = withContext(Dispatchers.IO) {
        runCatching { context.getScenes().getOrThrow() }
    }

    suspend fun getFavoriteSceneIds(): Result<List<String>> = withContext(Dispatchers.IO) {
        runCatching { context.getFavoriteSceneIds().getOrThrow() }
    }

    suspend fun setFavoriteSceneIds(
        ids: List<String>,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching { context.setFavoriteSceneIds(ids).getOrThrow() }
    }

    suspend fun getRemoteScenes(auth: Auth): Result<List<Scene>> = withContext(Dispatchers.IO) {
        runCatching {
            val scenes = reqGetScenes(auth).getOrThrow()

            context.setScenes(scenes).getOrThrow()

            scenes
        }
    }

    suspend fun getFavoriteScenes(): Result<List<Scene>> = withContext(Dispatchers.IO) {
        runCatching {
            val ids = getFavoriteSceneIds().getOrThrow()
            val scenes = getLocalScenes().getOrThrow()
                .filter { it.scene_id in ids }
                .sort(ids)

            scenes
        }
    }
}
