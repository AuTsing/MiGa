package com.autsing.miga.presentation.component

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text

@Composable
fun ListTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.title1,
        modifier = Modifier.padding(12.dp),
    )
}
