package com.autsing.miga.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.Text
import com.google.android.horologist.compose.layout.fillMaxRectangle

@Composable
fun MessageContent(message: String) {
    ScalingLazyColumn {
        item {
            Text(
                text = message,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxRectangle(),
            )
        }
    }
}
