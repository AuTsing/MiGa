package com.autsing.miga.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.InlineSlider
import androidx.wear.compose.material.InlineSliderDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import com.autsing.miga.presentation.component.ListTitle
import com.autsing.miga.presentation.component.LoadingContent
import com.autsing.miga.presentation.component.MessageContent
import com.autsing.miga.presentation.model.Component
import com.autsing.miga.presentation.theme.MiGaTheme
import com.autsing.miga.presentation.viewmodel.DeviceViewModel

@Composable
fun DeviceScreen(
    deviceViewModel: DeviceViewModel,
) {
    val uiState = deviceViewModel.uiState

    MiGaTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center,
        ) {
            if (uiState.loading) {
                LoadingContent("加载中...")
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

@Composable
private fun SwitchComponent(
    component: Component.Switch,
    onClick: (Boolean) -> Unit = {},
) {
    var value by remember { mutableStateOf(component.value) }

    ToggleChip(
        checked = value,
        onCheckedChange = {
            value = it
            onClick(it)
        },
        label = { Text(component.headline) },
        toggleControl = { Switch(value) },
        enabled = !component.readOnly,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ComponentTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.title3,
        modifier = Modifier.padding(12.dp),
    )
}

@Composable
private fun SliderComponent(
    component: Component.Slider,
    onClick: (Float) -> Unit = {},
) {
    var value by remember { mutableFloatStateOf(component.value) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        ComponentTitle("${component.headline}: ${component.valueDisplay}")
        InlineSlider(
            value = value,
            onValueChange = {
                value = it
                onClick(it)
            },
            valueRange = 0F..1F,
            steps = 9,
            segmented = false,
            enabled = !component.readOnly,
            increaseIcon = { Icon(InlineSliderDefaults.Increase, "Increase") },
            decreaseIcon = { Icon(InlineSliderDefaults.Decrease, "Decrease") },
        )
    }
}

@Composable
private fun SelectorComponent(
    component: Component.Selector,
    onClick: (Int) -> Unit = {},
) {
    var value by remember { mutableIntStateOf(component.value) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        ComponentTitle("${component.headline}: ${component.valueDisplay}")
        InlineSlider(
            value = value,
            onValueChange = {
                value = it
                onClick(it)
            },
            valueProgression = 0..component.values.size - 1,
            segmented = true,
            enabled = !component.readOnly,
            increaseIcon = { Icon(InlineSliderDefaults.Increase, "Increase") },
            decreaseIcon = { Icon(InlineSliderDefaults.Decrease, "Decrease") },
        )
    }
}

@Composable
private fun TriggerComponent(
    component: Component.Trigger,
    onClick: () -> Unit = {},
) {
    Chip(
        onClick = onClick,
        label = { Text(component.headline) },
        colors = ChipDefaults.gradientBackgroundChipColors(),
        modifier = Modifier.fillMaxWidth(),
    )
}
