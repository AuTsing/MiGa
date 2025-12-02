package com.autsing.miga.presentation.repository

import com.autsing.miga.presentation.helper.ApiHelper
import com.autsing.miga.presentation.helper.FileHelper
import com.autsing.miga.presentation.helper.SerdeHelper
import com.autsing.miga.presentation.model.Auth
import com.autsing.miga.presentation.model.Scene
import com.autsing.miga.presentation.model.sort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SceneRepository(
    private val serdeHelper: SerdeHelper,
    private val fileHelper: FileHelper,
    private val apiHelper: ApiHelper,
) {

    companion object {
        lateinit var instance: SceneRepository

        private const val FAVORITE_SCENE_IDS_FILENAME = "favorite_scene_ids.json"
        private const val SCENES_FILENAME = "scenes.json"
    }

    suspend fun loadFavoriteSceneIds(): Result<List<String>> = withContext(Dispatchers.IO) {
        runCatching {
            val favoriteSceneIdsJson = fileHelper.readJson(FAVORITE_SCENE_IDS_FILENAME).getOrThrow()
            val favoriteSceneIds = serdeHelper.decode<List<String>>(favoriteSceneIdsJson)
                .getOrThrow()
            return@runCatching favoriteSceneIds
        }
    }

    suspend fun loadScenesLocal(): Result<List<Scene>> = withContext(Dispatchers.IO) {
        runCatching {
            val scenesJson = fileHelper.readJson(SCENES_FILENAME).getOrThrow()
            val scenes = serdeHelper.decode<List<Scene>>(scenesJson)
                .getOrThrow()
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
            val scenesJson = serdeHelper.encode(scenes).getOrThrow()
            fileHelper.writeJson(SCENES_FILENAME, scenesJson).getOrThrow()
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

    suspend fun saveFavoriteSceneIds(
        favoriteSceneIds: List<String>,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val favoriteSceneIdsJson = serdeHelper.encode(favoriteSceneIds).getOrThrow()
            fileHelper.writeJson(FAVORITE_SCENE_IDS_FILENAME, favoriteSceneIdsJson).getOrThrow()
        }
    }
}
