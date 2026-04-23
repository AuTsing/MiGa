package com.autsing.miga.presentation.repository

import android.annotation.SuppressLint
import android.content.Context
import com.autsing.miga.presentation.data.getFavoriteSceneIds
import com.autsing.miga.presentation.data.getScenes
import com.autsing.miga.presentation.data.setFavoriteSceneIds
import com.autsing.miga.presentation.data.setScenes
import com.autsing.miga.presentation.helper.ApiHelper
import com.autsing.miga.presentation.model.Auth
import com.autsing.miga.presentation.model.Scene
import com.autsing.miga.presentation.model.sort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SceneRepository(
    private val context: Context,
    private val apiHelper: ApiHelper,
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
            val scenes = apiHelper.getScenes(auth).getOrThrow().filter { it.icon_url.isNotBlank() }
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
