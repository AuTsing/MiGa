package com.autsing.miga.presentation.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.autsing.miga.presentation.component.LoadingContent
import com.autsing.miga.presentation.component.MessageContent
import com.autsing.miga.presentation.theme.MiGaTheme
import com.google.android.horologist.compose.layout.fillMaxRectangle
import qrcode.QRCode
import qrcode.render.extensions.drawQRCode

@Composable
fun LoginScreen(
    loginUrl: String,
    exception: String,
) {
    MiGaTheme {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxRectangle()
                .background(MaterialTheme.colors.background),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("使用米家扫描登录")
                if (exception.isNotBlank()) {
                    MessageContent(exception)
                } else if (loginUrl.isBlank()) {
                    LoadingContent("加载中...")
                } else {
                    QrcodeContent(loginUrl)
                }
            }
        }
    }
}

@Composable
private fun QrcodeContent(text: String) {
    val qrCode = remember(text) {
        QRCode.ofSquares().build(text)
    }

    Canvas(
        modifier = Modifier
            .aspectRatio(1F)
            .fillMaxSize()
            .background(Color.White),
    ) {
        inset(12F) {
            drawQRCode(qrCode)
        }
    }
}
