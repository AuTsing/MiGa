package com.autsing.miga.presentation.component

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text

@Composable
fun ComponentTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.title3,
        modifier = Modifier.padding(12.dp),
    )
}
