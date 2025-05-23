package com.autsing.miga.complication

import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData

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
        sceneId: String,
    ): ComplicationData {
        val action = null

        return ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(text).build(),
            contentDescription = PlainComplicationText.Builder(contentDescription).build()
        ).setTapAction(action).build()
    }

    fun createAddComplicationData(): ComplicationData {
        val action = null

        return ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder("添加收藏").build(),
            contentDescription = PlainComplicationText.Builder("添加收藏").build()
        ).setTapAction(action).build()
    }
}
