package io.zoemeow.dutnotify.view.account

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.zoemeow.dutnotify.R
import io.zoemeow.dutnotify.model.enums.LoginState
import io.zoemeow.dutnotify.viewmodel.MainViewModel

@Composable
fun AccountPageNotLoggedIn(
    padding: PaddingValues,
    mainViewModel: MainViewModel,
) {
    val dialogLoginEnabled = remember { mutableStateOf(false) }

    AccountDialogLogin(
        enabled = dialogLoginEnabled,
        mainViewModel = mainViewModel,
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.account_notloggedin_title),
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.size(5.dp))
        Text(
            text = stringResource(id = R.string.account_notloggedin_description),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.size(5.dp))
        Button(
            onClick = {
                mainViewModel.Account_LoginProcess.value = LoginState.NotTriggered
                dialogLoginEnabled.value = true
            },
            content = { Text(stringResource(id = R.string.account_notloggedin_btnlogin)) }
        )
    }
}

