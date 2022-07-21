package io.zoemeow.dutapp.android.view.account

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import io.zoemeow.dutapp.android.model.ProcessState
import io.zoemeow.dutapp.android.viewmodel.AccountViewModel

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AccountLoginDialog(
    enabled: MutableState<Boolean>,
    accountViewModel: AccountViewModel
) {
    val passTextFieldFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val enabledControl: MutableState<Boolean> = remember { mutableStateOf(true) }
    val username: MutableState<String> = remember { mutableStateOf("") }
    val password: MutableState<String> = remember { mutableStateOf("") }
    val rememberLogin: MutableState<Boolean> = remember { mutableStateOf(false) }

    LaunchedEffect(enabled.value) {
        if (enabled.value) {
            enabledControl.value = true
            username.value = ""
            password.value = ""
            rememberLogin.value = false
        }
    }

    LaunchedEffect(
        accountViewModel.isLoggedIn.value,
        accountViewModel.processStateLoggingIn.value
    ) {
        // If is logging in
        if (accountViewModel.processStateLoggingIn.value == ProcessState.Running) {
            // Disable controls here to avoid editing
            enabledControl.value = false
        }
        // Otherwise
        else {
            // Enable controls again
            enabledControl.value = true

            if (
                // Process done but not logged in
                (!accountViewModel.isLoggedIn.value && accountViewModel.processStateLoggingIn.value != ProcessState.Running) ||
                // Self process failed
                accountViewModel.processStateLoggingIn.value == ProcessState.Failed
            ) {
                // TODO: Notify login failed here!
            }
            // Successfully logged in!
            else if (accountViewModel.isLoggedIn.value &&
                accountViewModel.processStateLoggingIn.value == ProcessState.Successful) {
                enabled.value = false
                enabledControl.value = true
            }
        }
    }

    // Alert dialog for login
    if (enabled.value) {
        AlertDialog(
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            ),
            modifier = Modifier
                .padding(15.dp)
                .fillMaxWidth()
                .wrapContentHeight(),
            onDismissRequest = {
                if (enabledControl.value)
                    enabled.value = false
            },
            title = {
                Text("Login")
            },
            dismissButton = {
                TextButton(
                    // enabled = enabledControl.value,
                    onClick = {
                        enabled.value = false
                    },
                    content = {
                        Text("Cancel")
                    }
                )
            },
            confirmButton = {
                TextButton(
                    enabled = enabledControl.value,
                    onClick = {
                        accountViewModel.login(username.value, password.value)
                    },
                    content = {
                        Text("Login")
                    }
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start,
                ) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = enabledControl.value,
                        value = username.value,
                        onValueChange = { username.value = it },
                        label = { Text("Username") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { passTextFieldFocusRequester.requestFocus() }
                        ),
                    )
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth()
                            .focusRequester(passTextFieldFocusRequester),
                        enabled = enabledControl.value,
                        value = password.value,
                        onValueChange = { password.value = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Go
                        ),
                        keyboardActions = KeyboardActions(
                            onGo = {
                                focusManager.clearFocus()
                                accountViewModel.login(username.value, password.value)
                            }
                        )
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                rememberLogin.value = !rememberLogin.value
                            }
                    ) {
                        Checkbox(
                            checked = rememberLogin.value,
                            onCheckedChange = { rememberLogin.value = it },
                        )
                        Spacer(modifier = Modifier.size(5.dp))
                        Text("Remember your login")
                    }
                }
            }
        )
    }
}