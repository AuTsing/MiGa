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

    suspend fun loadFavoriteSceneIds(): Result<Set<String>> = withContext(Dispatchers.IO) {
        runCatching {
            val favoriteScenesJson = fileHelper.readJson("favorite_scene_ids.json").getOrThrow()
            val favoriteScenes = Json.decodeFromString<Set<String>>(favoriteScenesJson)
            return@runCatching favoriteScenes
        }
    }

    suspend fun loadScenesLocal(
        favoriteSceneIds: Set<String>,
    ): Result<List<Scene>> = withContext(Dispatchers.IO) {
        runCatching {
            val scenesJson = fileHelper.readJson("scenes.json").getOrThrow()
            val scenes = Json.decodeFromString<List<Scene>>(scenesJson)
                .filter { it.icon_url.isNotBlank() }
                .sortedByDescending { it.scene_id in favoriteSceneIds }
            return@runCatching scenes
        }
    }

    suspend fun loadScenesRemote(
        auth: Auth,
        favoriteScenes: Set<String>,
    ): Result<List<Scene>> = withContext(Dispatchers.IO) {
        runCatching {
            val scenes = apiHelper.getScenes(auth).getOrThrow()
                .filter { it.icon_url.isNotBlank() }
                .sortedByDescending { it.scene_id in favoriteScenes }
            val scenesJson = Json.encodeToString(scenes)
            fileHelper.writeJson("scenes.json", scenesJson).getOrThrow()
            return@runCatching scenes
        }
    }

    suspend fun loadFavoriteScenes(): Result<List<Scene>> = withContext(Dispatchers.IO) {
        runCatching {
            val favoriteSceneIds = loadFavoriteSceneIds().getOrThrow()
            val scenes = loadScenesLocal(favoriteSceneIds)
                .getOrThrow()
                .filter { it.scene_id in favoriteSceneIds }
            return@runCatching scenes
        }
    }
}
