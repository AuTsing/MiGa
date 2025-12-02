package com.autsing.miga.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.material3.MaterialTheme
import com.autsing.miga.presentation.theme.MiGaTheme
import com.google.android.horologist.compose.layout.fillMaxRectangle

@Composable
fun FullRectBox(content: @Composable (BoxScope.() -> Unit)) {
    MiGaTheme {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxRectangle()
                .background(MaterialTheme.colorScheme.background),
            content = content,
        )
    }
}
