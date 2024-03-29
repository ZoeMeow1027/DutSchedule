package io.zoemeow.dutschedule.ui.component.account

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import io.zoemeow.dutschedule.R
import io.zoemeow.dutschedule.ui.component.base.DialogBase

@Composable
fun LoginDialog(
    loginClicked: (String, String, Boolean) -> Unit,
    cancelRequested: () -> Unit,
    canDismiss: Boolean = false,
    dismissClicked: (() -> Unit)? = null,
    isVisible: Boolean = false,
    controlEnabled: Boolean = true,
    clearOnClose: Boolean = true
) {
    val passTextFieldFocusRequester = remember { FocusRequester() }
    val passwordShow: MutableState<Boolean> = remember { mutableStateOf(false) }

    val username: MutableState<String> = remember { mutableStateOf("") }
    val password: MutableState<String> = remember { mutableStateOf("") }
    val rememberLogin: MutableState<Boolean> = remember { mutableStateOf(false) }

    LaunchedEffect(isVisible) {
        if (!isVisible && clearOnClose) {
            username.value = ""
            password.value = ""
            rememberLogin.value = false
            passwordShow.value = false
        }
    }

    DialogBase(
        modifier = Modifier
            .fillMaxWidth()
            .padding(25.dp),
        isVisible = isVisible,
        title = "Login",
        isTitleCentered = true,
        canDismiss = canDismiss,
        dismissClicked = {
            dismissClicked?.let { it() }
        },
        content = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 7.dp),
                    enabled = controlEnabled,
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(passTextFieldFocusRequester)
                        .padding(bottom = 7.dp),
                    enabled = controlEnabled,
                    value = password.value,
                    onValueChange = { password.value = it },
                    label = { Text("Password") },
                    suffix = {
                        IconButton(
                            onClick = { passwordShow.value = !passwordShow.value },
                            content = {
                                Icon(
                                    painter = painterResource(id = when (passwordShow.value) {
                                        false -> R.drawable.ic_baseline_visibility_24
                                        true -> R.drawable.ic_baseline_visibility_off_24
                                    }),
                                    contentDescription = ""
                                )
                            },
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    visualTransformation = when (passwordShow.value) {
                        false -> PasswordVisualTransformation()
                        true -> VisualTransformation.None
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Go
                    ),
                    keyboardActions = KeyboardActions(
                        onGo = {
                            loginClicked(username.value, password.value, rememberLogin.value)
                        }
                    )
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (controlEnabled) {
                                rememberLogin.value = !rememberLogin.value
                            }
                        }
                        .padding(bottom = 7.dp),
                ) {
                    Checkbox(
                        checked = rememberLogin.value,
                        onCheckedChange = { if (controlEnabled) rememberLogin.value = it },
                    )
                    Spacer(modifier = Modifier.size(5.dp))
                    Text("Remember this login")
                }
            }
        },
        actionButtons = {
            TextButton(
                onClick = { loginClicked(username.value, password.value, rememberLogin.value) },
                content = { Text("Login") },
            )
            TextButton(
                onClick = { cancelRequested() },
                content = { Text("Cancel") },
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    )
}