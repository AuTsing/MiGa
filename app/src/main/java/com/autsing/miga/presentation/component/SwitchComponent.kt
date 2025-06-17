package com.autsing.miga.presentation.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import com.autsing.miga.presentation.model.Component

@Composable
fun SwitchComponent(
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
