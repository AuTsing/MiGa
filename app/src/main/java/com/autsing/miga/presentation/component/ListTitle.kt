package com.autsing.miga.presentation.component

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text

@Composable
fun ListTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.displaySmall,
    )
}
