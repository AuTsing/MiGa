package com.autsing.miga.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.AlertDialog
import androidx.wear.compose.material3.AlertDialogDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.autsing.miga.R

@Composable
fun Title(
    title: String,
    tip: String? = null,
) {
    var showDialog by remember { mutableStateOf(false) }

    TitleDialog(
        showDialog = showDialog,
        title = title,
        tip = tip ?: "",
        onDismiss = { showDialog = false },
    )

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.padding(4.dp),
        )
        if (tip != null) {
            Icon(
                painter = painterResource(R.drawable.ic_fluent_info_regular_icon),
                contentDescription = null,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { showDialog = true },
            )
        }
    }
}

@Composable
private fun TitleDialog(
    showDialog: Boolean,
    title: String,
    tip: String,
    onDismiss: () -> Unit = {},
) {
    AlertDialog(
        visible = showDialog,
        onDismissRequest = onDismiss,
        title = { Title(title) },
        text = { Text(tip) },
        edgeButton = { AlertDialogDefaults.EdgeButton(onClick = onDismiss) },
    )
}
