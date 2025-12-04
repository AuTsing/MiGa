package com.autsing.miga.tile

import android.content.Context
import androidx.wear.protolayout.ColorBuilders.ColorProp
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.DimensionBuilders
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.LayoutElementBuilders.ColorFilter
import androidx.wear.protolayout.LayoutElementBuilders.Column
import androidx.wear.protolayout.LayoutElementBuilders.Image
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.ModifiersBuilders.Clickable
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material3.ButtonDefaults.filledTonalButtonColors
import androidx.wear.protolayout.material3.MaterialScope
import androidx.wear.protolayout.material3.button
import androidx.wear.protolayout.material3.buttonGroup
import androidx.wear.protolayout.material3.materialScope
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.material3.textEdgeButton
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.tooling.preview.Preview
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tiles.tooling.preview.TilePreviewHelper
import androidx.wear.tooling.preview.devices.WearDevices
import com.autsing.miga.R
import com.autsing.miga.presentation.activity.RunSceneActivity
import com.autsing.miga.presentation.model.Scene
import com.autsing.miga.tile.MainTileRenderer.Companion.RES_IC_ADD
import com.autsing.miga.tile.MainTileRenderer.Companion.RES_IC_SCENE
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.images.drawableResToImageResource
import com.google.android.horologist.tiles.render.SingleTileLayoutRenderer

@OptIn(ExperimentalHorologistApi::class)
class MainTileRenderer(context: Context) : SingleTileLayoutRenderer<MainTileState, Unit>(context) {
    companion object {
        const val RES_IC_SCENE = "res_ic_scene"
        const val RES_IC_ADD = "res_ic_add"
    }

    override fun ResourceBuilders.Resources.Builder.produceRequestedResources(
        resourceState: Unit,
        deviceParameters: DeviceParameters,
        resourceIds: List<String>,
    ) {
        addIdToImageMapping(
            RES_IC_SCENE,
            drawableResToImageResource(R.drawable.ic_fluent_star_regular_icon),
        )
        addIdToImageMapping(
            RES_IC_ADD,
            drawableResToImageResource(R.drawable.ic_fluent_add_regular_icon),
        )
    }

    override fun renderTile(
        state: MainTileState,
        deviceParameters: DeviceParameters,
    ): LayoutElement {
        return tileLayout(context, state, deviceParameters)
    }
}

private fun tileLayout(
    context: Context,
    state: MainTileState,
    deviceParameters: DeviceParameters,
): LayoutElement {
    return materialScope(
        context = context,
        deviceConfiguration = deviceParameters,
    ) {
        val visibleScenes = state.scenes.take(6)
        val (row1, row2) = visibleScenes.chunked(if (visibleScenes.size > 4) 3 else 2)
            .let { chunkedList ->
                Pair(
                    chunkedList.getOrElse(0) { emptyList() },
                    chunkedList.getOrElse(1) { emptyList() }
                )
            }

        primaryLayout(
            titleSlot = null,
            mainSlot = {
                column {
                    setWidth(expand())
                    setHeight(expand())
                    addContent(buttonGroup {
                        row1.forEach {
                            this@materialScope.sceneButton(context, it)
                        }
                    })
                }
            },
            bottomSlot = {
                textEdgeButton(
                    onClick = Clickable.Builder().build(),
                    labelContent = { text("More".layoutString) },
                    colors = filledTonalButtonColors(),
                )
            },
        )
    }
}

internal fun MaterialScope.sceneButton(context: Context, scene: Scene): LayoutElement {
    val action = RunSceneActivity.createLaunchAction(context, scene.scene_id)
    val clickable = Clickable.Builder()
        .setOnClick(action)
        .build()

    return button(
        onClick = clickable,
        labelContent = {
            text(
                text = scene.name.layoutString,
            )
        },
    )
}

private fun buttonContent(
    context: Context,
    iconId: String,
    iconColor: Int,
    text: String,
    textColor: Int,
): LayoutElement {
    val iconColorProp = ColorProp.Builder(iconColor)
        .build()
    val iconColorFilter = ColorFilter.Builder()
        .setTint(iconColorProp)
        .build()
    val iconContent = Image.Builder()
        .setWidth(dp(36F))
        .setHeight(dp(36F))
        .setResourceId(iconId)
        .setColorFilter(iconColorFilter)
        .build()
    val textColorProp = ColorProp.Builder(textColor)
        .build()
    val labelContent = Text.Builder(context, text)
        .setTypography(Typography.TYPOGRAPHY_CAPTION1)
        .setColor(textColorProp)
        .build()

    return Column.Builder()
        .setWidth(DimensionBuilders.expand())
        .addContent(iconContent)
        .addContent(labelContent)
        .build()
}

@Preview(device = WearDevices.LARGE_ROUND, name = "Large Round")
internal fun previewTile(context: Context): TilePreviewData {
    val state = getMockMainTileState()
    return TilePreviewData(
        resources {
            addIdToImageMapping(
                RES_IC_SCENE,
                drawableResToImageResource(R.drawable.ic_fluent_star_regular_icon),
            )
            addIdToImageMapping(
                RES_IC_ADD,
                drawableResToImageResource(R.drawable.ic_fluent_add_regular_icon),
            )
        }
    ) {
        TilePreviewHelper
            .singleTimelineEntryTileBuilder(
                tileLayout(context, state, it.deviceConfiguration)
            )
            .build()
    }
}

internal fun resources(
    fn: ResourceBuilders.Resources.Builder.() -> Unit
): (RequestBuilders.ResourcesRequest) -> ResourceBuilders.Resources = {
    ResourceBuilders.Resources.Builder().setVersion(it.version).apply(fn).build()
}
