package com.autsing.miga.presentation.component

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon

@Composable
fun PrimaryButton(
    iconId: Int,
    onClick: () -> Unit = {},
) {
    Button(
        onClick = onClick,
        modifier = Modifier.padding(16.dp),
    ) {
        Icon(
            painter = painterResource(iconId),
            contentDescription = null,
        )
    }
}
