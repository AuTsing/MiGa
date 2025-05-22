package com.autsing.miga.tile

import android.content.Context
import androidx.wear.protolayout.ActionBuilders.AndroidActivity
import androidx.wear.protolayout.ActionBuilders.LaunchAction
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
import com.autsing.miga.presentation.activity.MainActivity
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
    iconColor: Int,
    text: String,
    textColor: Int,
): LayoutElement {
    val padding = Padding.Builder()
        .setAll(dp(2F))
        .build()
    val modifiers = Modifiers.Builder()
        .setPadding(padding)
        .build()
    val button = Button.Builder(context, clickable)
        .setCustomContent(buttonContent(context, iconId, iconColor, text, textColor))
        .setSize(ButtonDefaults.EXTRA_LARGE_SIZE)
        .setButtonColors(ButtonDefaults.SECONDARY_COLORS)
        .build()

    return Box.Builder()
        .setModifiers(modifiers)
        .addContent(button)
        .build()
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

private fun triggerButton(context: Context, text: String): LayoutElement {
    val clickable = Clickable.Builder()
        .build()

    return button(
        context = context,
        clickable = clickable,
        iconId = RES_IC_SCENE,
        iconColor = 0xFFFFC107.toInt(),
        text = text,
        textColor = Colors.DEFAULT.onSurface,
    )
}

private fun addButton(context: Context): LayoutElement {
    val activity = AndroidActivity.Builder()
        .setPackageName(context.packageName)
        .setClassName(MainActivity::class.java.name)
        .build()
    val action = LaunchAction.Builder()
        .setAndroidActivity(activity)
        .build()
    val clickable = Clickable.Builder()
        .setOnClick(action)
        .build()

    return button(
        context = context,
        clickable = clickable,
        iconId = RES_IC_ADD,
        iconColor = 0xFF4CAF50.toInt(),
        text = "添加收藏",
        textColor = Colors.DEFAULT.onSurface,
    )
}
