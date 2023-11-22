package io.zoemeow.dutschedule.ui.component.base

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun OutlinedTextBox(
    modifier: Modifier = Modifier,
    title: String,
    value: String? = null
) {
    OutlinedTextField(
        modifier = modifier,
        value = if (value.isNullOrEmpty()) "(no information)" else value,
        readOnly = true,
        onValueChange = { },
        label = { Text(title) }
    )
}