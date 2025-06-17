package com.autsing.miga.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.InlineSlider
import com.autsing.miga.R
import com.autsing.miga.presentation.model.Component

@Composable
fun SliderComponent(
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
            increaseIcon = {
                Icon(painterResource(R.drawable.ic_fluent_add_regular_icon), "Increase")
            },
            decreaseIcon = {
                Icon(painterResource(R.drawable.ic_fluent_subtract_regular_icon), "Decrease")
            },
        )
    }
}
