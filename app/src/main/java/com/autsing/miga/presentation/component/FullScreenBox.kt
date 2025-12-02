package com.autsing.miga.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.material3.MaterialTheme
import com.autsing.miga.presentation.theme.MiGaTheme

@Composable
fun FullScreenBox(content: @Composable (BoxScope.() -> Unit)) {
    MiGaTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center,
            content = content,
        )
    }
}
