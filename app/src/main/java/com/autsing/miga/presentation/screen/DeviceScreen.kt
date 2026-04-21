package com.autsing.miga.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material3.Text
import com.autsing.miga.R
import com.autsing.miga.presentation.component.FullScreenBox
import com.autsing.miga.presentation.component.LoadingContent
import com.autsing.miga.presentation.component.MessageContent
import com.autsing.miga.presentation.component.PrimaryButton
import com.autsing.miga.presentation.component.SelectorComponent
import com.autsing.miga.presentation.component.SliderComponent
import com.autsing.miga.presentation.component.SwitchComponent
import com.autsing.miga.presentation.component.Title
import com.autsing.miga.presentation.component.TriggerComponent
import com.autsing.miga.presentation.model.Component
import com.autsing.miga.presentation.viewmodel.DeviceViewModel

@Composable
fun DeviceScreen(
    deviceViewModel: DeviceViewModel,
) {
    val uiState = deviceViewModel.uiState
    val context = LocalContext.current

    FullScreenBox {
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
                onBack = { deviceViewModel.handleClickBack(context) },
            )
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
    onBack: () -> Unit = {},
) {
    ScalingLazyColumn {
        item { Title("开关") }
        items(
            items = switches,
            key = { it.headline },
        ) { switch -> SwitchComponent(switch) { onClickSwitch(switch, it) } }
        item { Title("调整") }
        items(
            items = sliders,
            key = { it.headline },
        ) { slider -> SliderComponent(slider) { onClickSlider(slider, it) } }
        item { Title("模式") }
        items(
            items = selectors,
            key = { it.headline },
        ) { selector ->
            SelectorComponent(selector) { onClickSelector(selector, it) }
        }
        item { Title("动作") }
        items(
            items = triggers,
            key = { it.headline },
        ) { trigger -> TriggerComponent(trigger) { onClickTrigger(trigger) } }
        item {
            PrimaryButton(
                iconId = R.drawable.ic_fluent_ios_arrow_ltr_icon,
                onClick = onBack,
            )
        }
    }
}
