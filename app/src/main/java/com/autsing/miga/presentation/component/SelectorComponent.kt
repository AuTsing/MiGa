package com.autsing.miga.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Slider
import com.autsing.miga.R
import com.autsing.miga.presentation.model.Component

@Composable
fun SelectorComponent(
    component: Component.Selector,
    onClick: (Int) -> Unit = {},
) {
    var value by remember { mutableIntStateOf(component.value) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Title(
            title = "${component.headline}: ${component.valueDisplay}",
            style = MaterialTheme.typography.titleMedium,
        )
        Slider(
            value = value,
            onValueChange = {
                value = it
                onClick(it)
            },
            valueProgression = 0..<component.values.size,
            segmented = true,
            enabled = !component.readOnly,
            increaseIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_fluent_ios_arrow_rtl_icon),
                    contentDescription = "Increase",
                )
            },
            decreaseIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_fluent_ios_arrow_ltr_icon),
                    contentDescription = "Decrease",
                )
            },
        )
    }
}
