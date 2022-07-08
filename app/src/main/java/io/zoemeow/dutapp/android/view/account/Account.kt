package io.zoemeow.dutapp.android.view.account

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import io.zoemeow.dutapp.android.model.ProcessState
import io.zoemeow.dutapp.android.viewmodel.AccountViewModel

@Composable
fun Account(accountViewModel: AccountViewModel) {
    if (accountViewModel.isLoggedIn.value) {
        AccountDashboard(
            username = accountViewModel.username.value,
            logoutRequested = {
                accountViewModel.logout()
            }
        )
    }
    else {
        val context = LocalContext.current
        AccountNotLoggedIn(
            loginRequested = {
                accountViewModel.processStateLoggingIn.value = ProcessState.NotRun
                val intent = Intent(context, AccountLoginActivity::class.java)
                context.startActivity(intent)
            }
        )
    }
}