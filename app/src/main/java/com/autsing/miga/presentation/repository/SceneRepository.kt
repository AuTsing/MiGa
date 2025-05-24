package com.autsing.miga.presentation.repository

import com.autsing.miga.presentation.helper.ApiHelper
import com.autsing.miga.presentation.helper.FileHelper
import com.autsing.miga.presentation.model.Auth
import com.autsing.miga.presentation.model.Scene
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class SceneRepository(
    private val fileHelper: FileHelper,
    private val apiHelper: ApiHelper,
) {

    companion object {
        lateinit var instance: SceneRepository
    }

    suspend fun loadFavoriteSceneIds(): Result<List<String>> = withContext(Dispatchers.IO) {
        runCatching {
            val favoriteScenesJson = fileHelper.readJson("favorite_scene_ids.json").getOrThrow()
            val favoriteScenes = Json.decodeFromString<List<String>>(favoriteScenesJson)
            return@runCatching favoriteScenes
        }
    }

    suspend fun loadScenesLocal(
        favoriteSceneIds: List<String>,
    ): Result<List<Scene>> = withContext(Dispatchers.IO) {
        runCatching {
            val favoriteSceneIdsMap = favoriteSceneIds.withIndex()
                .associate { it.value to it.index }
            val scenesJson = fileHelper.readJson("scenes.json").getOrThrow()
            val scenes = Json.decodeFromString<List<Scene>>(scenesJson)
                .filter { it.icon_url.isNotBlank() }
                .sortedBy { favoriteSceneIdsMap[it.scene_id] ?: Int.MAX_VALUE }
            return@runCatching scenes
        }
    }

    suspend fun loadScenesRemote(
        auth: Auth,
        favoriteSceneIds: List<String>,
    ): Result<List<Scene>> = withContext(Dispatchers.IO) {
        runCatching {
            val favoriteSceneIdsMap = favoriteSceneIds.withIndex()
                .associate { it.value to it.index }
            val scenes = apiHelper.getScenes(auth).getOrThrow()
                .filter { it.icon_url.isNotBlank() }
                .sortedBy { favoriteSceneIdsMap[it.scene_id] ?: Int.MAX_VALUE }
            val scenesJson = Json.encodeToString(scenes)
            fileHelper.writeJson("scenes.json", scenesJson).getOrThrow()
            return@runCatching scenes
        }
    }

    suspend fun loadFavoriteScenes(): Result<List<Scene>> = withContext(Dispatchers.IO) {
        runCatching {
            val favoriteSceneIds = loadFavoriteSceneIds().getOrThrow()
            val favoriteSceneIdsMap = favoriteSceneIds.withIndex()
                .associate { it.value to it.index }
            val scenes = loadScenesLocal(favoriteSceneIds).getOrThrow()
                .filter { it.scene_id in favoriteSceneIds }
                .sortedBy { favoriteSceneIdsMap[it.scene_id] ?: Int.MAX_VALUE }
            return@runCatching scenes
        }
    }
}
