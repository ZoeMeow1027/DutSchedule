package io.zoemeow.dutapp.android.view.account

import android.content.Intent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import io.zoemeow.dutapp.android.model.ProcessState
import io.zoemeow.dutapp.android.viewmodel.AccountViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Account(accountViewModel: AccountViewModel) {
    val topAppTitle = remember { mutableStateOf("") }
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = {
                    Text(text = topAppTitle.value)
                }
            )
        },
        content = { padding ->
            if (accountViewModel.isLoggedIn.value) {
                topAppTitle.value = "Account (${accountViewModel.username.value})"
                AccountDashboard(
                    padding = padding,
                    logoutRequested = {
                        accountViewModel.logout()
                    }
                )
            }
            else {
                topAppTitle.value = "Account"
                val context = LocalContext.current
                AccountNotLoggedIn(
                    padding = padding,
                    loginRequested = {
                        accountViewModel.processStateLoggingIn.value = ProcessState.NotRun
                        val intent = Intent(context, AccountLoginActivity::class.java)
                        context.startActivity(intent)
                    }
                )
            }
        }
    )
}