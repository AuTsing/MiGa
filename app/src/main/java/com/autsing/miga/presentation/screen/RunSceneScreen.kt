package com.autsing.miga.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material3.AlertDialogContent
import androidx.wear.compose.material3.SuccessConfirmationDialogContent
import androidx.wear.compose.material3.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.autsing.miga.R
import com.autsing.miga.presentation.activity.RunSceneActivity
import com.autsing.miga.presentation.component.FullScreenBox
import com.autsing.miga.presentation.component.LoadingContent
import com.autsing.miga.presentation.component.PrimaryButton
import com.autsing.miga.presentation.component.Title
import com.google.android.horologist.annotations.ExperimentalHorologistApi

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
    SuccessConfirmationDialogContent(curvedText = null)
}

@OptIn(ExperimentalHorologistApi::class)
@Composable
private fun FailureContent(exception: String) {
    val context = LocalContext.current

    AlertDialogContent(
        title = { Title("执行失败") },
        content = {
            item { Text(exception) }
            item {
                PrimaryButton(
                    iconId = R.drawable.ic_fluent_dismiss_regular_icon,
                    onClick = { (context as RunSceneActivity).finish() },
                )
            }
        },
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
