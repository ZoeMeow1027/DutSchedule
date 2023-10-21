package io.zoemeow.dutschedule.ui.component.base

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun OutlinedTextBox(
    title: String,
    value: String,
    modifier: Modifier,
) {
    OutlinedTextField(
        modifier = modifier,
        value = value,
        readOnly = true,
        onValueChange = { },
        label = { Text(title) }
    )
}