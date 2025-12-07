package com.autsing.miga.presentation.screen

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.autsing.miga.R
import com.autsing.miga.presentation.component.FullRectBox
import com.autsing.miga.presentation.component.LoadingContent
import com.autsing.miga.presentation.component.MessageContent
import qrcode.QRCode

@Composable
fun LoginScreen(
    loginUrl: String,
    exception: String,
    onRefresh: () -> Unit = {},
) {
    FullRectBox {
        if (exception.isNotBlank()) {
            MessageContent(
                message = exception,
                action = onRefresh,
                actionIconId = R.drawable.ic_fluent_arrow_sync_regular_icon,
            )
        } else if (loginUrl.isBlank()) {
            LoadingContent("加载中...")
        } else {
            QrcodeContent(loginUrl)
        }
    }
}

@Composable
private fun QrcodeContent(text: String) {
    val bitmap = remember(text) {
        val bytes = QRCode.ofSquares().build(text).renderToBytes()
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("使用米家扫描登录")
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .aspectRatio(1F)
                .fillMaxSize()
                .background(Color.White)
                .padding(4.dp),
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
            )
        }
    }
}

@Preview(device = WearDevices.LARGE_ROUND, showSystemUi = true)
@Composable
private fun PreviewMessageScreen() {
    LoginScreen(
        loginUrl = "https://c3.account.xiaomi.com/longPolling/login?ticket=lp_51726C606941cc-c21f-45ef-9ab0-1f993b103078&dc=c3&sid=xiaomiio&ts=1764689335760",
        exception = "登录失败: timeout",
    )
}

@Preview(device = WearDevices.LARGE_ROUND, showSystemUi = true)
@Composable
private fun PreviewLoginScreen() {
    LoginScreen(
        loginUrl = "https://c3.account.xiaomi.com/longPolling/login?ticket=lp_51726C606941cc-c21f-45ef-9ab0-1f993b103078&dc=c3&sid=xiaomiio&ts=1764689335760",
        exception = "",
    )
}
