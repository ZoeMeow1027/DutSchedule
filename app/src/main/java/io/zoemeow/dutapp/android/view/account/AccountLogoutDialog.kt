package io.zoemeow.dutapp.android.view.account

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AccountLogoutDialog(
    enabled: MutableState<Boolean>,
    logoutRequest: () -> Unit
) {
    // Alert dialog for logout
    if (enabled.value) {
        AlertDialog(
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            ),
            modifier = Modifier
                .padding(15.dp)
                .fillMaxWidth()
                .wrapContentHeight(),
            onDismissRequest = { enabled.value = false },
            title = { Text("Logout") },
            text = {
                Text(
                    text = "Logout will clear subjects cache. You will need to login again to continue receiving subjects.\n\nAre you sure you want to continue?"
                )
            },
            dismissButton = {
                TextButton(
                    onClick = { enabled.value = false },
                    content = {
                        Text("Never mind")
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        enabled.value = false
                        logoutRequest()
                    },
                    content = {
                        Text("Continue")
                    }
                )
            },
        )
    }
}