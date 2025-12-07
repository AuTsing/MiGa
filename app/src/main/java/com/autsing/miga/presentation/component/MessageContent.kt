package com.autsing.miga.presentation.component

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.AlertDialogContent
import androidx.wear.compose.material3.Text
import com.autsing.miga.R

@Composable
fun MessageContent(
    message: String,
    title: String? = null,
    action: (() -> Unit)? = null,
    actionIconId: Int = R.drawable.ic_fluent_dismiss_regular_icon,
) {
    AlertDialogContent(
        title = {
            if (title != null) {
                Title(title)
            }
        },
        content = {
            item { Text(message) }
            if (action != null) {
                item {
                    PrimaryButton(
                        iconId = actionIconId,
                        onClick = action,
                    )
                }
            }
        },
    )
}
