package com.autsing.miga.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.MaterialTheme
import com.autsing.miga.R
import com.autsing.miga.presentation.component.ComponentTitle
import com.autsing.miga.presentation.component.LoadingContent
import com.autsing.miga.presentation.component.MessageContent
import com.autsing.miga.presentation.component.PrimaryButton
import com.autsing.miga.presentation.component.SelectorComponent
import com.autsing.miga.presentation.component.SliderComponent
import com.autsing.miga.presentation.model.Component
import com.autsing.miga.presentation.theme.MiGaTheme

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
    MiGaTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center,
        ) {
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
    ScalingLazyColumn {
        item { ComponentTitle(title) }
        items(sliders) { slider -> SliderComponent(slider) { onClickSlider(slider, it) } }
        items(selectors) { selector ->
            SelectorComponent(selector) { onClickSelector(selector, it) }
        }
        item {
            PrimaryButton(
                iconId = R.drawable.ic_fluent_star_regular_icon,
                onClick = onClickRun,
            )
        }
    }
}
