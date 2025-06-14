package com.autsing.miga.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.autsing.miga.presentation.component.LoadingContent
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
            } else if (uiState.components.isNotEmpty()) {
                DeviceInfoContent(uiState.components)
            }
        }
    }
}

@Composable
private fun DeviceInfoContent(components: List<Component>) {
    ScalingLazyColumn {
        items(components) {
            when (it) {
                is Component.Switch -> SwitchComponent(it)
                is Component.Slider -> SliderComponent(it)
                is Component.Selector -> SelectorComponent(it)
                is Component.Trigger -> TriggerComponent(it)
            }
        }
    }
}

@Composable
private fun SwitchComponent(component: Component.Switch) {
    var value by remember { mutableStateOf(false) }

    ToggleChip(
        checked = value,
        onCheckedChange = { value = it },
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
private fun SliderComponent(component: Component.Slider) {
    var value by remember { mutableIntStateOf(0) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        ComponentTitle("${component.headline}(${component.valueDisplay})")
        InlineSlider(
            value = value,
            onValueChange = { value = it },
            valueProgression = 0..10,
            segmented = false,
            enabled = !component.readOnly,
            increaseIcon = { Icon(InlineSliderDefaults.Increase, "Increase") },
            decreaseIcon = { Icon(InlineSliderDefaults.Decrease, "Decrease") },
        )
    }
}

@Composable
private fun SelectorComponent(component: Component.Selector) {
    var value by remember { mutableIntStateOf(0) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        ComponentTitle("${component.headline}(${component.valueDisplay})")
        InlineSlider(
            value = value,
            onValueChange = { value = it },
            valueProgression = 0..component.values.size - 1,
            segmented = true,
            enabled = !component.readOnly,
            increaseIcon = { Icon(InlineSliderDefaults.Increase, "Increase") },
            decreaseIcon = { Icon(InlineSliderDefaults.Decrease, "Decrease") },
        )
    }
}

@Composable
private fun TriggerComponent(component: Component.Trigger) {
    Chip(
        onClick = {},
        label = { Text(component.headline) },
        colors = ChipDefaults.gradientBackgroundChipColors(),
        modifier = Modifier.fillMaxWidth(),
    )
}
