package io.zoemeow.dutapp.android.view.account

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import io.zoemeow.dutapp.android.R
import io.zoemeow.dutapp.android.viewmodel.AccountViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Account(accountViewModel: AccountViewModel) {
    val topAppTitle = remember { mutableStateOf("") }
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            SmallTopAppBar(
                title = {
                    Text(text = topAppTitle.value)
                }
            )
        },
        content = { padding ->
            if (accountViewModel.isLoggedIn.value) {
                topAppTitle.value = String.format(
                    stringResource(id = R.string.topbar_account),
                    if (accountViewModel.username.value.isNotEmpty())
                        "(${accountViewModel.username.value})"
                    else ""
                )
                AccountDashboard(
                    padding = padding,
                    logoutRequested = {
                        accountViewModel.logout()
                    }
                )
            }
            else {
                topAppTitle.value = String.format(stringResource(id = R.string.topbar_account), "")
                AccountNotLoggedIn(
                    padding = padding,
                    accountViewModel = accountViewModel
                )
            }
        }
    )
}