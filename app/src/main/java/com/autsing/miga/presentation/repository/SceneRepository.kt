package com.autsing.miga.presentation.repository

import com.autsing.miga.presentation.helper.ApiHelper
import com.autsing.miga.presentation.helper.FileHelper
import com.autsing.miga.presentation.model.Auth
import com.autsing.miga.presentation.model.Scene
import com.autsing.miga.presentation.model.sort
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
            val favoriteSceneIdsJson = fileHelper.readJson("favorite_scene_ids.json").getOrThrow()
            val favoriteSceneIds = Json.decodeFromString<List<String>>(favoriteSceneIdsJson)
            return@runCatching favoriteSceneIds
        }
    }

    suspend fun loadScenesLocal(): Result<List<Scene>> = withContext(Dispatchers.IO) {
        runCatching {
            val scenesJson = fileHelper.readJson("scenes.json").getOrThrow()
            val scenes = Json.decodeFromString<List<Scene>>(scenesJson)
                .filter { it.icon_url.isNotBlank() }
            return@runCatching scenes
        }
    }

    suspend fun loadScenesRemote(
        auth: Auth,
    ): Result<List<Scene>> = withContext(Dispatchers.IO) {
        runCatching {
            val scenes = apiHelper.getScenes(auth).getOrThrow()
                .filter { it.icon_url.isNotBlank() }
            val scenesJson = Json.encodeToString(scenes)
            fileHelper.writeJson("scenes.json", scenesJson).getOrThrow()
            return@runCatching scenes
        }
    }

    suspend fun loadFavoriteScenes(): Result<List<Scene>> = withContext(Dispatchers.IO) {
        runCatching {
            val favoriteSceneIds = loadFavoriteSceneIds().getOrThrow()
            val scenes = loadScenesLocal().getOrThrow()
                .filter { it.scene_id in favoriteSceneIds }
                .sort(favoriteSceneIds)
            return@runCatching scenes
        }
    }
}
