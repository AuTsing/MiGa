package com.autsing.miga.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.autsing.miga.presentation.component.ListTitle
import com.autsing.miga.presentation.component.LoadingContent
import com.autsing.miga.presentation.component.MessageContent
import com.autsing.miga.presentation.component.SelectorComponent
import com.autsing.miga.presentation.component.SliderComponent
import com.autsing.miga.presentation.component.SwitchComponent
import com.autsing.miga.presentation.component.TriggerComponent
import com.autsing.miga.presentation.model.Component
import com.autsing.miga.presentation.theme.MiGaTheme
import com.autsing.miga.presentation.viewmodel.DeviceViewModel

@Composable
fun DeviceScreen(
    deviceViewModel: DeviceViewModel,
) {
    val uiState = deviceViewModel.uiState
    val context = LocalContext.current

    MiGaTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center,
        ) {
            if (uiState.loading) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LoadingContent("加载中...")
                    Text("(首次加载可能时间较长)")
                }
            } else if (uiState.exception.isNotBlank()) {
                MessageContent(uiState.exception)
            } else {
                DeviceInfoContent(
                    switches = uiState.switchComponents,
                    sliders = uiState.sliderComponents,
                    selectors = uiState.selectorComponents,
                    triggers = uiState.triggerComponents,
                    onClickSwitch = deviceViewModel::handleChangeSwitch,
                    onClickSlider = deviceViewModel::handleChangeSlider,
                    onClickSelector = deviceViewModel::handleChangeSelector,
                    onClickTrigger = { deviceViewModel.handleClickTrigger(context, it) },
                )
            }
        }
    }
}

@Composable
private fun DeviceInfoContent(
    switches: List<Component.Switch>,
    sliders: List<Component.Slider>,
    selectors: List<Component.Selector>,
    triggers: List<Component.Trigger>,
    onClickSwitch: (Component.Switch, Boolean) -> Unit = { _, _ -> },
    onClickSlider: (Component.Slider, Float) -> Unit = { _, _ -> },
    onClickSelector: (Component.Selector, Int) -> Unit = { _, _ -> },
    onClickTrigger: (Component.Trigger) -> Unit = {},
) {
    ScalingLazyColumn {
        item { ListTitle("开关") }
        items(switches) { switch -> SwitchComponent(switch) { onClickSwitch(switch, it) } }
        item { ListTitle("调整") }
        items(sliders) { slider -> SliderComponent(slider) { onClickSlider(slider, it) } }
        item { ListTitle("模式") }
        items(selectors) { selector ->
            SelectorComponent(selector) { onClickSelector(selector, it) }
        }
        item { ListTitle("动作") }
        items(triggers) { trigger -> TriggerComponent(trigger) { onClickTrigger(trigger) } }
    }
}
