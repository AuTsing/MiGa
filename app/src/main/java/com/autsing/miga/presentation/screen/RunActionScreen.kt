package com.autsing.miga.presentation.screen

import androidx.compose.runtime.Composable
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material3.AlertDialogContent
import com.autsing.miga.R
import com.autsing.miga.presentation.component.FullScreenBox
import com.autsing.miga.presentation.component.LoadingContent
import com.autsing.miga.presentation.component.MessageContent
import com.autsing.miga.presentation.component.PrimaryButton
import com.autsing.miga.presentation.component.SelectorComponent
import com.autsing.miga.presentation.component.SliderComponent
import com.autsing.miga.presentation.component.Title
import com.autsing.miga.presentation.model.Component

@Composable
fun RunActionScreen(
    loading: Boolean,
    message: String,
    title: String,
    sliders: List<Component.Slider>,
    selectors: List<Component.Selector>,
    onClickSlider: (Component.Slider, Float) -> Unit = { _, _ -> },
    onClickSelector: (Component.Selector, Int) -> Unit = { _, _ -> },
    onClickRun: () -> Unit = {},
) {
    FullScreenBox {
        if (loading) {
            LoadingContent("加载中...")
        } else if (message.isNotBlank()) {
            MessageContent(message)
        } else {
            RunActionContent(
                title = title,
                sliders = sliders,
                selectors = selectors,
                onClickSlider = onClickSlider,
                onClickSelector = onClickSelector,
                onClickRun = onClickRun,
            )
        }
    }
}

@Composable
private fun RunActionContent(
    title: String,
    sliders: List<Component.Slider>,
    selectors: List<Component.Selector>,
    onClickSlider: (Component.Slider, Float) -> Unit = { _, _ -> },
    onClickSelector: (Component.Selector, Int) -> Unit = { _, _ -> },
    onClickRun: () -> Unit = {},
) {
    AlertDialogContent(
        title = { Title(title) },
        content = {
            items(sliders) { slider -> SliderComponent(slider) { onClickSlider(slider, it) } }
            items(selectors) { selector ->
                SelectorComponent(selector) { onClickSelector(selector, it) }
            }
            item {
                PrimaryButton(
                    iconId = R.drawable.ic_fluent_play_regular_icon,
                    onClick = onClickRun,
                )
            }
        },
    )
}
