package com.autsing.miga.presentation.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.wear.compose.material3.SwitchButton
import androidx.wear.compose.material3.Text
import com.autsing.miga.presentation.model.Component

@Composable
fun SwitchComponent(
    component: Component.Switch,
    onClick: (Boolean) -> Unit = {},
) {
    var value by remember { mutableStateOf(component.value) }

    SwitchButton(
        checked = value,
        onCheckedChange = {
            value = it
            onClick(it)
        },
        enabled = !component.readOnly,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(component.headline)
    }
}
