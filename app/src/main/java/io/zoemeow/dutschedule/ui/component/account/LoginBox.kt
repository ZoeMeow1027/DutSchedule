package io.zoemeow.dutschedule.ui.component.account

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.zoemeow.dutschedule.R

@Composable
fun LoginBox(
    modifier: Modifier = Modifier,
    isVisible: Boolean = true,
    isProcessing: Boolean = false,
    isControlEnabled: Boolean = false,
    isLoggedInBefore: Boolean = true,
    clearOnInvisible: Boolean = true,
    opacity: Float = 1f,
    onForgotPass: (() -> Unit)? = null,
    onClearLogin: (() -> Unit)? = null,
    onSubmit: (String, String, Boolean) -> Unit
) {
    val passTextFieldFocusRequester = remember { FocusRequester() }
    val passwordShow: MutableState<Boolean> = remember { mutableStateOf(false) }

    val username: MutableState<String> = remember { mutableStateOf("") }
    val password: MutableState<String> = remember { mutableStateOf("") }
    val rememberLogin: MutableState<Boolean> = remember { mutableStateOf(false) }

    LaunchedEffect(isVisible) {
        if (!isVisible && clearOnInvisible) {
            username.value = ""
            password.value = ""
            rememberLogin.value = false
            passwordShow.value = false
        }
    }

    if (isVisible) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center,
            content = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Login",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
                    Text(
                        "Use your account in sv.dut.udn.vn to login",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 7.dp),
                        enabled = isControlEnabled && !isProcessing,
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
                        enabled = isControlEnabled && !isProcessing,
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
                                if (isControlEnabled && !isProcessing) {
                                    onSubmit(username.value, password.value, rememberLogin.value)
                                }
                            }
                        )
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isControlEnabled && !isProcessing) {
                                    rememberLogin.value = !rememberLogin.value
                                }
                            }
                            .padding(bottom = 7.dp),
                    ) {
                        Checkbox(
                            enabled = isControlEnabled && !isProcessing,
                            checked = rememberLogin.value,
                            onCheckedChange = { if (isControlEnabled) rememberLogin.value = it },
                        )
                        Spacer(modifier = Modifier.size(5.dp))
                        Text("Remember this login")
                    }
                    ElevatedButton(
                        enabled = when {
                            (isControlEnabled || isLoggedInBefore) && !isProcessing -> {
                                username.value.length >= 6 && password.value.length >= 6
                            }
                            else -> false
                        },
                        onClick = {
                            onSubmit(username.value, password.value, rememberLogin.value)
                        },
                        content = {
                            Text("Login")
                        }
                    )
                    if (isLoggedInBefore) {
                        ElevatedButton(
                            enabled = !isProcessing,
                            onClick = { onClearLogin?.let { it() } },
                            content = { Text("Login with another account") }
                        )
                    }
                    TextButton(
                        enabled = isControlEnabled && !isProcessing,
                        onClick = { onForgotPass?.let { it() } },
                        content = { Text("Forgot your password?") }
                    )
                    if (isProcessing || isLoggedInBefore) {
                        Surface(
                            modifier = Modifier.padding(top = 10.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = opacity),
                            shape = RoundedCornerShape(5.dp),
                            content = {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 15.dp, horizontal = 15.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                    content = {
                                        if (isProcessing) {
                                            CircularProgressIndicator()
                                            Spacer(modifier = Modifier.size(10.dp))
                                            Text("Processing...")
                                        } else if (isLoggedInBefore) {
                                            Text("You have logged in before. Click \"Login\" button to proceed.")
                                        }
                                    }
                                )
                            }
                        )
                    }
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginBoxPreview() {
    LoginBox(
        isProcessing = false,
        isControlEnabled = true,
        isLoggedInBefore = false,
        onSubmit = { _, _, _ -> }
    )
}