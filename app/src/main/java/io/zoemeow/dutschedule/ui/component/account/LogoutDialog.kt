package io.zoemeow.dutschedule.ui.component.account

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.zoemeow.dutschedule.ui.component.base.DialogBase

@Composable
fun LogoutDialog(
    isVisible: Boolean = false,
    canDismiss: Boolean = false,
    logoutClicked: (() -> Unit)? = null,
    dismissClicked: (() -> Unit)? = null,
) {
    DialogBase(
        modifier = Modifier.fillMaxWidth().padding(25.dp),
        isVisible = isVisible,
        title = "Logout",
        canDismiss = canDismiss,
        dismissClicked = {
            dismissClicked?.let { it() }
        },
        content = {
            Column(
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    "Are you sure you want to logout?\n\n" +
                            "Note that:\n" +
                            "- You won't be received your any subject schedule anymore.\n" +
                            "- Your news filter settings won't be affected."
                )
            }
        },
        actionButtons = {
            TextButton(
                onClick = { logoutClicked?.let { it() } },
                content = { Text("Logout") },
            )
            TextButton(
                onClick = { dismissClicked?.let { it() } },
                content = { Text("Cancel") },
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    )
}