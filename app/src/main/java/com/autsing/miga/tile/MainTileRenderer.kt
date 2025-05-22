package com.autsing.miga.tile

import android.content.Context
import androidx.wear.protolayout.ColorBuilders.ColorProp
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.DimensionBuilders
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.LayoutElementBuilders.Box
import androidx.wear.protolayout.LayoutElementBuilders.ColorFilter
import androidx.wear.protolayout.LayoutElementBuilders.Column
import androidx.wear.protolayout.LayoutElementBuilders.Image
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.LayoutElementBuilders.Row
import androidx.wear.protolayout.ModifiersBuilders.Clickable
import androidx.wear.protolayout.ModifiersBuilders.Modifiers
import androidx.wear.protolayout.ModifiersBuilders.Padding
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.material.Button
import androidx.wear.protolayout.material.ButtonDefaults
import androidx.wear.protolayout.material.Colors
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import com.autsing.miga.R
import com.autsing.miga.tile.MainTileRenderer.Companion.RES_IC_SCENE
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.images.drawableResToImageResource
import com.google.android.horologist.tiles.render.SingleTileLayoutRenderer

@OptIn(ExperimentalHorologistApi::class)
class MainTileRenderer(context: Context) : SingleTileLayoutRenderer<MainTileState, Unit>(context) {
    companion object {
        const val RES_IC_SCENE = "res_ic_scene"
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
    }

    override fun renderTile(
        state: MainTileState,
        deviceParameters: DeviceParameters,
    ): LayoutElement {
        return tileLayout(context, state)
    }
}

private fun tileLayout(context: Context, state: MainTileState): LayoutElement {
    val buttons = state.scenes.map { triggerButton(context, it.name) }
        .toMutableList()
        .apply {
            if (size < 4) {
                repeat(4 - size) { add(addButton(context)) }
            }
        }

    val row = Row.Builder()
        .addContent(buttons[0])
        .addContent(buttons[1])
        .build()
    val row2 = Row.Builder()
        .addContent(buttons[2])
        .addContent(buttons[3])
        .build()
    val column = Column.Builder()
        .addContent(row)
        .addContent(row2)
        .build()

    return Box.Builder()
        .setWidth(DimensionBuilders.expand())
        .setHeight(DimensionBuilders.expand())
        .addContent(column)
        .build()
}

private fun button(
    context: Context,
    clickable: Clickable,
    iconId: String,
    text: String,
): LayoutElement {
    val padding = Padding.Builder()
        .setAll(dp(2F))
        .build()
    val modifiers = Modifiers.Builder()
        .setPadding(padding)
        .build()
    val button = Button.Builder(context, clickable)
        .setCustomContent(buttonContent(context, iconId, text))
        .setSize(ButtonDefaults.EXTRA_LARGE_SIZE)
        .setButtonColors(ButtonDefaults.SECONDARY_COLORS)
        .build()

    return Box.Builder()
        .setModifiers(modifiers)
        .addContent(button)
        .build()
}

private fun buttonContent(context: Context, iconId: String, text: String): LayoutElement {
    val color = ColorProp.Builder(Colors.DEFAULT.onSurface)
        .build()
    val colorFilter = ColorFilter.Builder()
        .setTint(color)
        .build()
    val iconContent = Image.Builder()
        .setWidth(dp(36F))
        .setHeight(dp(36F))
        .setResourceId(iconId)
        .setColorFilter(colorFilter)
        .build()
    val labelContent = Text.Builder(context, text)
        .setTypography(Typography.TYPOGRAPHY_CAPTION1)
        .setColor(color)
        .build()

    return Column.Builder()
        .setWidth(DimensionBuilders.expand())
        .addContent(iconContent)
        .addContent(labelContent)
        .build()
}

private fun triggerButton(context: Context, text: String): LayoutElement {
    val clickable = Clickable.Builder()
        .build()

    return button(context, clickable, RES_IC_SCENE, text)
}

private fun addButton(context: Context): LayoutElement {
    val clickable = Clickable.Builder()
        .build()

    return button(context, clickable, RES_IC_SCENE, "添加收藏")
}
