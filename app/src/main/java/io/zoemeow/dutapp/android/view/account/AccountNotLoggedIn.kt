package io.zoemeow.dutapp.android.view.account

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AccountNotLoggedIn(
    loginRequested: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "You are not logged in",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.size(5.dp))
        Text(
            text = "Login to using more features in this app.",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.size(5.dp))
        Button(
            onClick = {
                loginRequested()
            },
            content = {
                Text(
                    text = "Login",
                )
            }
        )
//        Spacer(modifier = Modifier.size(10.dp))
//        Text(
//            text = "By continue logging your account, you have agreed to our Private Policy."
//        )
    }
}