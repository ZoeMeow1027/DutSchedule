package io.zoemeow.subjectnotifier.view.account

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import io.zoemeow.subjectnotifier.R

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AccountDialogLogout(
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
            title = { Text(stringResource(id = R.string.account_logout_title)) },
            text = {
                Text(text = stringResource(id = R.string.account_logout_description))
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        enabled.value = false
                    },
                    content = { Text(stringResource(id = R.string.option_cancel)) }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        enabled.value = false
                        logoutRequest()
                    },
                    content = { Text(stringResource(id = R.string.option_continue)) }
                )
            },
        )
    }
}