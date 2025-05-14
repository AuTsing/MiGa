package com.autsing.miga.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.autsing.miga.presentation.theme.MiGaTheme
import com.autsing.miga.presentation.viewmodel.MainViewModel

@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
) {
    val uiState = mainViewModel.uiState

    MiGaTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.loading) {
                LoadingContent()
            } else if (uiState.auth == null) {
                LoginContent()
            } else {
                MainContent()
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    CircularProgressIndicator()
}

@Composable
private fun LoginContent() {
    Text("未登录")
}

@Composable
private fun MainContent() {
    Text("列表")
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
private fun MainContentPreview() {
    MiGaTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            LoadingContent()
        }
    }
}
