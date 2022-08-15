package io.zoemeow.dutapp.android.view.account

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.zoemeow.dutapp.android.model.ProcessState
import io.zoemeow.dutapp.android.viewmodel.AccountViewModel

@Composable
fun AccountNotLoggedIn(
    padding: PaddingValues,
    accountViewModel: AccountViewModel
) {
    val dialogLoginEnabled = remember { mutableStateOf(false) }

    AccountLoginDialog(
        enabled = dialogLoginEnabled,
        accountViewModel = accountViewModel
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "You are not logged in",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.size(5.dp))
        Text(
            text = "Login to use more features in this app.",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.size(5.dp))
        Button(
            onClick = {
                accountViewModel.processStateLoggingIn.value = ProcessState.NotRanYet
                dialogLoginEnabled.value = true
            },
            content = { Text("Login") }
        )
    }
}

