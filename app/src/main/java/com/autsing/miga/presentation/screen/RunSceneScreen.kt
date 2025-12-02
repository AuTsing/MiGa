package com.autsing.miga.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material3.ConfirmationDialogDefaults
import androidx.wear.compose.material3.FailureConfirmationDialog
import androidx.wear.compose.material3.SuccessConfirmationDialog
import androidx.wear.compose.material3.confirmationDialogCurvedText
import androidx.wear.tooling.preview.devices.WearDevices
import com.autsing.miga.presentation.component.FullScreenBox
import com.autsing.miga.presentation.component.LoadingContent

@Composable
fun RunSceneScreen(
    loading: Boolean,
    success: Boolean,
    exception: String,
) {
    FullScreenBox {
        if (loading) {
            LoadingContent("执行中...")
        } else if (success) {
            SuccessContent()
        } else {
            FailureContent(exception)
        }
    }
}

@Composable
private fun SuccessContent() {
    SuccessConfirmationDialog(
        visible = true,
        onDismissRequest = {},
        curvedText = null,
    )
}

@Composable
private fun FailureContent(exception: String) {
    val style = ConfirmationDialogDefaults.curvedTextStyle
    FailureConfirmationDialog(
        visible = true,
        onDismissRequest = {},
        curvedText = { confirmationDialogCurvedText(exception, style) },
    )
}

@Preview(device = WearDevices.LARGE_ROUND, showSystemUi = true)
@Composable
private fun PreviewLoading() {
    RunSceneScreen(
        loading = true,
        success = false,
        exception = "",
    )
}

@Preview(device = WearDevices.LARGE_ROUND, showSystemUi = true)
@Composable
private fun PreviewSuccess() {
    RunSceneScreen(
        loading = false,
        success = true,
        exception = "",
    )
}

@Preview(device = WearDevices.LARGE_ROUND, showSystemUi = true)
@Composable
private fun PreviewFailure() {
    RunSceneScreen(
        loading = false,
        success = false,
        exception = "abcdefghijklm",
    )
}
