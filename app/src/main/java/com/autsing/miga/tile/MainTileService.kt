package com.autsing.miga.tile

import androidx.wear.protolayout.ResourceBuilders.Resources
import androidx.wear.tiles.RequestBuilders.ResourcesRequest
import androidx.wear.tiles.RequestBuilders.TileRequest
import androidx.wear.tiles.TileBuilders.Tile
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.SuspendingTileService

@OptIn(ExperimentalHorologistApi::class)
class MainTileService : SuspendingTileService() {

    private lateinit var renderer: MainTileRenderer

    override fun onCreate() {
        super.onCreate()

        renderer = MainTileRenderer(this)
    }

    override suspend fun resourcesRequest(requestParams: ResourcesRequest): Resources {
        return renderer.produceRequestedResources(Unit, requestParams)
    }

    override suspend fun tileRequest(requestParams: TileRequest): Tile {
        val state = MainTileState(
            scenes = emptyList(),
        )

        return renderer.renderTimeline(state, requestParams)
    }
}
