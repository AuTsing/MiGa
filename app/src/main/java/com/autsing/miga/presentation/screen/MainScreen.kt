package com.autsing.miga.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.OutlinedChip
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.autsing.miga.R
import com.autsing.miga.presentation.model.Device
import com.autsing.miga.presentation.model.Scene
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
            mainViewModel.handleLoadScenesAndDevices(uiState.auth)
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
                MainContent(
                    scenes = uiState.scenes,
                    devices = uiState.devices,
                    onClickScene = { mainViewModel.handleRunScene(context, uiState.auth, it) },
                    onClickReload = { mainViewModel.handleReload(uiState.auth) },
                )
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
private fun ListTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.title2,
        modifier = Modifier.padding(12.dp),
    )
}

@Composable
private fun EmptyChip(label: String) {
    OutlinedChip(
        onClick = {},
        label = {
            Text(
                text = label,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        enabled = false,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ChipIcon(iconId: Int) {
    Icon(
        painter = painterResource(iconId),
        contentDescription = null,
        modifier = Modifier
            .size(ChipDefaults.IconSize)
            .wrapContentSize(align = Alignment.Center),
    )
}

@Composable
private fun ChipText(label: String) {
    Text(
        text = label,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun SceneChip(
    scene: Scene,
    onClick: () -> Unit = {},
) {
    Chip(
        onClick = onClick,
        icon = { ChipIcon(R.drawable.ic_fluent_layer_regular_icon) },
        label = { ChipText(scene.name) },
        colors = ChipDefaults.secondaryChipColors(),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun DeviceChip(device: Device) {
    Chip(
        onClick = {},
        icon = {
            if (device.isOnline) {
                ChipIcon(R.drawable.ic_fluent_device_meeting_room_icon)
            } else {
                ChipIcon(R.drawable.ic_fluent_plug_disconnected_regular_icon)
            }
        },
        label = { ChipText(device.name) },
        colors = ChipDefaults.secondaryChipColors(),
        enabled = device.isOnline,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ReloadButton(
    onClick: () -> Unit = {},
) {
    Button(
        onClick = onClick,
        modifier = Modifier.padding(16.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_fluent_arrow_sync_regular_icon),
            contentDescription = null,
        )
    }
}

@Composable
private fun MainContent(
    scenes: List<Scene>,
    devices: List<Device>,
    onClickScene: (Scene) -> Unit = {},
    onClickReload: () -> Unit = {},
) {
    ScalingLazyColumn {
        item { ListTitle("智能") }
        if (scenes.isEmpty()) {
            item { EmptyChip("无智能") }
        }
        items(scenes) {
            SceneChip(
                scene = it,
                onClick = { onClickScene(it) },
            )
        }
        item { ListTitle("设备") }
        if (devices.isEmpty()) {
            item { EmptyChip("无设备") }
        }
        items(devices) { DeviceChip(it) }
        item { ReloadButton(onClickReload) }
    }
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

        }
    }
}
