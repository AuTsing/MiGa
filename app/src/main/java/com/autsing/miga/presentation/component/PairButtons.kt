package com.autsing.miga.presentation.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PairButtons(
    primaryButtonIconId: Int,
    secondaryButtonIconId: Int,
    onClickPrimaryButton: () -> Unit,
    onClickSecondaryButton: () -> Unit,
) {
    Row(
        modifier = Modifier.padding(top = 16.dp),
    ) {
        SecondaryButton(
            iconId = secondaryButtonIconId,
            onClick = onClickSecondaryButton,
        )
        PrimaryButton(
            iconId = primaryButtonIconId,
            onClick = onClickPrimaryButton,
        )
    }
}
