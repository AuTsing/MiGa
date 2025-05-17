package com.autsing.miga.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
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
    val context = LocalContext.current

    LaunchedEffect(uiState) {
        if (uiState.showedLogin == false && uiState.auth == null && uiState.loading == false) {
            mainViewModel.handleNavigateToLogin(context)
        }
    }

    LaunchedEffect(uiState.auth) {
        if (uiState.auth != null) {
            mainViewModel.handleLoadDevices(uiState.auth)
        }
    }

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
                LoginContent(
                    onClickLogin = { mainViewModel.handleNavigateToLogin(context) }
                )
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
private fun LoginContent(
    onClickLogin: () -> Unit = {},
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "未登录",
            style = MaterialTheme.typography.title1,
        )
        Button(
            onClick = onClickLogin,
            modifier = Modifier.fillMaxWidth(0.5F),
        ) {
            Text(
                text = "登录",
                style = MaterialTheme.typography.button,
            )
        }
    }

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
            MainContent()
        }
    }
}
