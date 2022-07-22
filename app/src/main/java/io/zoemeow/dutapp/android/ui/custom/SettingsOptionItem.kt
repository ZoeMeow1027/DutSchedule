package io.zoemeow.dutapp.android.ui.custom

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsOptionItem(
    title: String,
    description: String? = null,
    clickable: (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable { if (clickable != null) clickable() },
        contentAlignment = Alignment.CenterStart,
    ) {
        Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 15.dp, bottom = 15.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}