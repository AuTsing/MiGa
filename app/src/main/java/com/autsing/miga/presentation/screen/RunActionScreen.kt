package com.autsing.miga.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material3.AlertDialogContent
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.tooling.preview.devices.WearDevices
import com.autsing.miga.R
import com.autsing.miga.presentation.component.FullScreenBox
import com.autsing.miga.presentation.component.LoadingContent
import com.autsing.miga.presentation.component.MessageContent
import com.autsing.miga.presentation.component.PairButtons
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
    onClickBack: () -> Unit = {},
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
                onClickBack = onClickBack,
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
    onClickBack: () -> Unit = {},
) {
    AlertDialogContent(
        title = {
            Title(
                title = "执行",
                style = MaterialTheme.typography.displaySmall,
            )
        },
        text = {
            Title(
                title = title,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        content = {
            items(sliders) { slider -> SliderComponent(slider) { onClickSlider(slider, it) } }
            items(selectors) { selector ->
                SelectorComponent(selector) { onClickSelector(selector, it) }
            }
            item {
                PairButtons(
                    primaryButtonIconId = R.drawable.ic_fluent_play_regular_icon,
                    secondaryButtonIconId = R.drawable.ic_fluent_ios_arrow_ltr_icon,
                    onClickPrimaryButton = onClickRun,
                    onClickSecondaryButton = onClickBack,
                )
            }
        },
    )
}

@Preview(device = WearDevices.LARGE_ROUND, showSystemUi = true)
@Composable
private fun PreviewRunActionContent() {
    FullScreenBox {
        RunActionContent(
            title = "开关状态切换",
            sliders = emptyList(),
            selectors = emptyList(),
        )
    }
}
