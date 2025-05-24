package com.autsing.miga.complication

import android.content.Context
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import com.autsing.miga.presentation.activity.MainActivity
import com.autsing.miga.presentation.activity.RunSceneActivity

object ComplicationDataUtil {

    fun createComplicationData(
        text: String,
        contentDescription: String,
    ): ComplicationData {
        return ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(text).build(),
            contentDescription = PlainComplicationText.Builder(contentDescription).build()
        ).build()
    }

    fun createTriggerComplicationData(
        text: String,
        contentDescription: String,
        context: Context,
        requestCode: Int,
        sceneId: String,
    ): ComplicationData {
        val action = RunSceneActivity.createPendingIntent(
            context = context,
            requestCode = requestCode,
            sceneId = sceneId,
        )

        return ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(text).build(),
            contentDescription = PlainComplicationText.Builder(contentDescription).build()
        ).setTapAction(action).build()
    }

    fun createAddComplicationData(context: Context, requestCode: Int): ComplicationData {
        val action = MainActivity.createPendingIntent(
            context = context,
            requestCode = requestCode,
        )

        return ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder("添加收藏").build(),
            contentDescription = PlainComplicationText.Builder("添加收藏").build()
        ).setTapAction(action).build()
    }
}
