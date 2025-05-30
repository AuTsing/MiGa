package com.autsing.miga.tile

import android.content.Context
import androidx.wear.protolayout.ResourceBuilders.Resources
import androidx.wear.tiles.RequestBuilders.ResourcesRequest
import androidx.wear.tiles.RequestBuilders.TileRequest
import androidx.wear.tiles.TileBuilders.Tile
import com.autsing.miga.presentation.repository.SceneRepository
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.SuspendingTileService

@OptIn(ExperimentalHorologistApi::class)
class MainTileService : SuspendingTileService() {

    companion object {
        fun requestUpdate(context: Context) {
            getUpdater(context).requestUpdate(MainTileService::class.java)
        }
    }

    private val sceneRepository: SceneRepository = SceneRepository.instance

    private lateinit var renderer: MainTileRenderer

    override fun onCreate() {
        super.onCreate()

        renderer = MainTileRenderer(this)
    }

    override suspend fun resourcesRequest(requestParams: ResourcesRequest): Resources {
        return renderer.produceRequestedResources(Unit, requestParams)
    }

    override suspend fun tileRequest(requestParams: TileRequest): Tile {
        val favoriteScenes = sceneRepository.loadFavoriteScenes().getOrDefault(emptyList())

        val state = MainTileState(
            scenes = favoriteScenes,
        )

        return renderer.renderTimeline(state, requestParams)
    }
}
