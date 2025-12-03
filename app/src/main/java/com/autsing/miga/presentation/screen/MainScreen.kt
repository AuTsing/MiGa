package com.autsing.miga.presentation.screen

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.OutlinedCard
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TitleCard
import androidx.wear.tooling.preview.devices.WearDevices
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.toBitmap
import com.autsing.miga.R
import com.autsing.miga.presentation.component.FullScreenBox
import com.autsing.miga.presentation.component.LoadingContent
import com.autsing.miga.presentation.component.PrimaryButton
import com.autsing.miga.presentation.component.SecondaryButton
import com.autsing.miga.presentation.component.Title
import com.autsing.miga.presentation.model.Device
import com.autsing.miga.presentation.model.Scene
import com.autsing.miga.presentation.model.sort
import com.autsing.miga.presentation.viewmodel.MainViewModel
import kotlin.math.max

@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
) {
    val uiState = mainViewModel.uiState
    val context = LocalContext.current

    LaunchedEffect(uiState) {
        if (!uiState.showedLogin && uiState.auth == null && !uiState.loading) {
            mainViewModel.handleNavigateToLogin(context)
        }
    }

    FullScreenBox {
        if (uiState.loading) {
            LoadingContent("加载中...")
        } else if (uiState.auth == null) {
            LoginContent(
                onClickLogin = { mainViewModel.handleNavigateToLogin(context) }
            )
        } else {
            MainContent(
                scenes = uiState.scenes,
                devices = uiState.devices,
                favoriteSceneIds = uiState.favoriteSceneIds,
                favoriteDeviceIds = uiState.favoriteDeviceIds,
                deviceIconUrls = uiState.deviceIconUrls,
                onClickScene = { mainViewModel.handleRunScene(context, it) },
                onClickToggleSceneFavorite = {
                    mainViewModel.handleToggleSceneFavorite(context, it)
                },
                onClickDevice = { mainViewModel.handleOpenDevice(context, it) },
                onClickToggleDeviceFavorite = { mainViewModel.handleToggleDeviceFavorite(it) },
                onClickLogout = { mainViewModel.handleLogout() },
                onClickReload = { mainViewModel.handleLoad() },
            )
        }
    }
}

@Composable
private fun LoginContent(
    onClickLogin: () -> Unit = {},
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "未登录",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(24.dp),
        )
        Button(
            onClick = onClickLogin,
            modifier = Modifier.fillMaxWidth(0.5F),
        ) {
            Text(
                text = "登录",
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}


@Composable
private fun ChipIcon(iconId: Int) {
    Icon(
        painter = painterResource(iconId),
        contentDescription = null,
        modifier = Modifier
            .size(CardDefaults.AppImageSize)
            .wrapContentSize(align = Alignment.Center),
    )
}

@Composable
private fun CardTitle(@DrawableRes iconId: Int, label: String) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ChipIcon(iconId)
        Text(
            text = label,
            modifier = Modifier.padding(4.dp),
        )
    }
}

@Composable
private fun CardText(label: String) {
    Text(
        text = label,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun SceneChip(
    scene: Scene,
    favorite: Boolean,
    onClick: (Scene) -> Unit = {},
    onClickFavorite: (Scene) -> Unit = {},
) {
    TitleCard(
        onClick = { onClick(scene) },
        onLongClick = { onClickFavorite(scene) },
        border = if (favorite) {
            CardDefaults.outlinedCardBorder()
        } else {
            null
        },
        title = { CardTitle(R.drawable.ic_fluent_layer_regular_icon, "执行") },
        content = { CardText(scene.name) },
    )
}

@Composable
private fun DeviceChip(
    device: Device,
    iconUrl: String,
    favorite: Boolean,
    onClick: (Device) -> Unit = {},
    onClickFavorite: (Device) -> Unit = {},
) {
    val bgPainter = ColorPainter(CardDefaults.cardColors().containerColor)
    val imagePainter = rememberAsyncImagePainter(iconUrl)
    val containerPainter = remember(imagePainter) {
        object : Painter() {
            override val intrinsicSize = Size.Unspecified

            override fun DrawScope.onDraw() {
                with(bgPainter) { draw(size) }

                if (imagePainter.state.value is AsyncImagePainter.State.Success) {
                    val bitmap = (imagePainter.state.value as AsyncImagePainter.State.Success)
                        .result
                        .image
                        .toBitmap()
                        .asImageBitmap()

                    val scale = 200F / max(
                        imagePainter.intrinsicSize.width,
                        imagePainter.intrinsicSize.height,
                    )
                    val width = imagePainter.intrinsicSize.width * scale * 0.8F
                    val height = imagePainter.intrinsicSize.height * scale * 0.8F
                    val offset = IntOffset(
                        x = (size.width - width).toInt(),
                        y = ((size.height - height) / 2).toInt(),
                    )
                    drawImage(
                        image = bitmap,
                        dstOffset = offset,
                        dstSize = IntSize(width.toInt(), height.toInt()),
                    )
                }

                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.3F)
                        )
                    ),
                    size = size,
                )
            }
        }
    }

    TitleCard(
        onClick = { onClick(device) },
        onLongClick = { onClickFavorite(device) },
        border = if (favorite) {
            CardDefaults.outlinedCardBorder()
        } else {
            null
        },
        enabled = device.isOnline,
        title = {
            CardTitle(
                iconId = R.drawable.ic_fluent_device_meeting_room_icon,
                label = device.name,
            )
        },
        subtitle = {
            if (device.isOnline) {
                Text("设备在线")
            } else {
                Text("离线")
            }
        },
        containerPainter = containerPainter,
    )
}

@Composable
private fun EmptyCard(text: String) {
    OutlinedCard(onClick = {}) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .height(48.dp)
                .fillMaxWidth(),
        ) {
            Text(text)
        }
    }
}

@Composable
private fun MainContent(
    scenes: List<Scene>,
    devices: List<Device>,
    favoriteSceneIds: List<String>,
    favoriteDeviceIds: List<String>,
    deviceIconUrls: Map<String, String>,
    onClickScene: (Scene) -> Unit = {},
    onClickToggleSceneFavorite: (Scene) -> Unit = {},
    onClickDevice: (Device) -> Unit = {},
    onClickToggleDeviceFavorite: (Device) -> Unit = {},
    onClickLogout: () -> Unit = {},
    onClickReload: () -> Unit = {},
) {
    ScalingLazyColumn {
        item {
            Title(
                title = "智能",
                tip = "在手机米家APP添加智能，点击最下方刷新；点击智能可执行；长按智能可收藏",
                style = MaterialTheme.typography.displaySmall,
            )
        }
        items(scenes.sort(favoriteSceneIds)) {
            SceneChip(
                scene = it,
                favorite = it.scene_id in favoriteSceneIds,
                onClick = onClickScene,
                onClickFavorite = onClickToggleSceneFavorite,
            )
        }
        if (devices.isEmpty()) {
            item { EmptyCard("无智能") }
        }
        item {
            Title(
                title = "设备",
                tip = "在手机米家APP添加设备，点击最下方刷新；点击设备可进入设备页面；长按设备可收藏",
                style = MaterialTheme.typography.displaySmall,
            )
        }
        items(devices.sort(favoriteDeviceIds)) {
            DeviceChip(
                device = it,
                iconUrl = deviceIconUrls.getOrDefault(it.model, ""),
                favorite = it.did in favoriteDeviceIds,
                onClick = onClickDevice,
                onClickFavorite = onClickToggleDeviceFavorite,
            )
        }
        if (devices.isEmpty()) {
            item { EmptyCard("无设备") }
        }
        item {
            Row(
                modifier = Modifier.padding(top = 16.dp),
            ) {
                SecondaryButton(
                    iconId = R.drawable.ic_fluent_person_arrow_right_icon,
                    onClick = onClickLogout,
                )
                PrimaryButton(
                    iconId = R.drawable.ic_fluent_arrow_sync_regular_icon,
                    onClick = onClickReload,
                )
            }
        }
    }
}

@Composable
@Preview
private fun PreviewSceneChip() {
    SceneChip(
        scene = Scene(
            scene_id = "1",
            name = "电视机饮料",
            icon_url = "",
        ),
        favorite = false,
    )
}

@Composable
@Preview
private fun PreviewDeviceChip() {
    DeviceChip(
        device = Device(
            did = "",
            name = "米家台灯",
            isOnline = true,
            model = "",
        ),
        iconUrl = "https://cdn.cnbj1.fds.api.mi-img.com/iotweb-user-center/developer_1679040583270V7znmyVX.png?GalaxyAccessKeyId=AKVGLQWBOVIRQ3XLEW&Expires=9223372036854775807&Signature=JV+tC8MQ3QbAYiU3nC+T1U9DlZ0=",
        favorite = false,
    )
}

@Preview(device = WearDevices.LARGE_ROUND, showSystemUi = true)
@Composable
private fun PreviewEmptyContent() {
    FullScreenBox {
        MainContent(
            scenes = emptyList(),
            devices = emptyList(),
            favoriteSceneIds = emptyList(),
            favoriteDeviceIds = emptyList(),
            deviceIconUrls = emptyMap(),
        )
    }
}

@Preview(device = WearDevices.LARGE_ROUND, showSystemUi = true)
@Composable
private fun PreviewLoginContent() {
    FullScreenBox {
        LoginContent()
    }
}
