package io.zoemeow.dutapp.android.view.account

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AccountQueryingYourRequest() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Loading your data",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.size(5.dp))
        Text(
            text = "Please wait...",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.size(10.dp))
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    }
}