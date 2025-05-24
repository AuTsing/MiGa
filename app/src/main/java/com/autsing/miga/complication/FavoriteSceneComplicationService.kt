package com.autsing.miga.complication

import android.content.ComponentName
import android.content.Context
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.autsing.miga.presentation.repository.SceneRepository

abstract class FavoriteSceneComplicationService : SuspendingComplicationDataSourceService() {

    companion object {
        fun requestUpdate(context: Context) {
            ComplicationDataSourceUpdateRequester.create(
                context = context,
                complicationDataSourceComponent = ComponentName(
                    context,
                    FavoriteScene1ComplicationService::class.java,
                ),
            ).requestUpdateAll()
            ComplicationDataSourceUpdateRequester.create(
                context = context,
                complicationDataSourceComponent = ComponentName(
                    context,
                    FavoriteScene2ComplicationService::class.java,
                ),
            ).requestUpdateAll()
            ComplicationDataSourceUpdateRequester.create(
                context = context,
                complicationDataSourceComponent = ComponentName(
                    context,
                    FavoriteScene3ComplicationService::class.java,
                ),
            ).requestUpdateAll()
            ComplicationDataSourceUpdateRequester.create(
                context = context,
                complicationDataSourceComponent = ComponentName(
                    context,
                    FavoriteScene4ComplicationService::class.java,
                ),
            ).requestUpdateAll()
        }
    }

    private val sceneRepository: SceneRepository = SceneRepository.instance

    abstract val favoriteIndex: Int
    abstract val previewText: String
    abstract val previewContentDescription: String

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        if (type != ComplicationType.SHORT_TEXT) {
            return null
        }
        return ComplicationDataUtil.createComplicationData(previewText, previewContentDescription)
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        val favoriteScenes = sceneRepository.loadFavoriteScenes().getOrDefault(emptyList())
        val maybeScene = favoriteScenes.getOrNull(favoriteIndex)
        if (maybeScene == null) {
            return ComplicationDataUtil.createAddComplicationData(
                context = this,
                requestCode = request.complicationInstanceId,
            )
        }
        return ComplicationDataUtil.createTriggerComplicationData(
            text = maybeScene.name,
            contentDescription = maybeScene.name,
            context = this,
            requestCode = request.complicationInstanceId,
            sceneId = maybeScene.scene_id,
        )
    }
}
