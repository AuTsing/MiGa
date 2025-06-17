package com.autsing.miga.presentation.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Text
import com.autsing.miga.presentation.model.Component

@Composable
fun TriggerComponent(
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
