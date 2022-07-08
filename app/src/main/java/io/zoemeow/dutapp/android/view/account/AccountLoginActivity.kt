package io.zoemeow.dutapp.android.view.account

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.zoemeow.dutapp.android.model.ProcessState
import io.zoemeow.dutapp.android.ui.theme.DUTAppForAndroidTheme
import io.zoemeow.dutapp.android.viewmodel.AccountViewModel
import kotlinx.coroutines.launch

class AccountLoginActivity: ComponentActivity() {
    private lateinit var activityViewModel: AccountViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DUTAppForAndroidTheme {
                activityViewModel = AccountViewModel.getInstance()

                MainScreen()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainScreen() {
        val username = remember { mutableStateOf(String()) }
        val password = remember { mutableStateOf(String()) }

        val snackBarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        LaunchedEffect(
            activityViewModel.processStateLoggingIn.value,
            activityViewModel.isLoggedIn.value
        ) {
            if (activityViewModel.processStateLoggingIn.value == ProcessState.Successful &&
                activityViewModel.isLoggedIn.value) {

                setResult(RESULT_OK)
                finish()
            }
            else if (activityViewModel.processStateLoggingIn.value != ProcessState.NotRun &&
                    activityViewModel.processStateLoggingIn.value != ProcessState.Running) {
                scope.launch {
                    snackBarHostState.currentSnackbarData?.dismiss()
                    snackBarHostState.showSnackbar("Failed while logging you in! Check your account information and try again.")
                }
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackBarHostState) },
            topBar = {
                SmallTopAppBar(
                    title = {
                        Text("Login")
                    }
                )
            },
            content = { padding ->
                if (activityViewModel.processStateLoggingIn.value == ProcessState.Running) {
                    Column(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Logging you in",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.size(5.dp))
                        Text(
                            text = "Please wait...",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.size(10.dp))
                        CircularProgressIndicator()
                    }
                }
                else {
                    Column(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        content = {
                            Spacer(modifier = Modifier.size(10.dp))
                            OutlinedTextField(
                                value = username.value,
                                onValueChange = { username.value = it },
                                label = { Text("Username") }
                            )
                            OutlinedTextField(
                                value = password.value,
                                onValueChange = { password.value = it },
                                label = { Text("Password") }
                            )
                            Spacer(modifier = Modifier.size(5.dp))
                            Button(
                                onClick = {
                                    activityViewModel.login(username.value, password.value)
                                },
                                content = {
                                    Text("Login")
                                }
                            )
                        }
                    )
                }
            }
        )
    }
}