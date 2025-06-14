package com.autsing.miga.presentation.screen

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.tooling.preview.devices.WearDevices
import com.autsing.miga.R
import com.autsing.miga.presentation.component.LoadingContent
import com.autsing.miga.presentation.component.MessageContent
import com.autsing.miga.presentation.theme.MiGaTheme

@Composable
fun RunSceneScreen(
    loading: Boolean,
    message: String,
) {
    MiGaTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center,
        ) {
            if (loading) {
                LoadingContent("执行中...")
            } else {
                ResultContent(message)
            }
        }
    }
}

@Composable
private fun ResultContent(message: String) {
    val context = LocalContext.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .weight(1F),
        ) {
            MessageContent(message)
        }
        Button(
            onClick = { (context as Activity).finish() },
            modifier = Modifier.padding(8.dp),
        ) {
            Icon(painterResource(R.drawable.ic_fluent_dismiss_regular_icon), null)
        }
    }
}

@Preview(device = WearDevices.LARGE_ROUND, showSystemUi = true)
@Composable
private fun PreviewLoadingContent() {
    RunSceneScreen(
        loading = true,
        message = "",
    )
}

@Preview(device = WearDevices.LARGE_ROUND, showSystemUi = true)
@Composable
private fun PreviewMessageContent() {
    RunSceneScreen(
        loading = false,
        message = "ok",
    )
}
