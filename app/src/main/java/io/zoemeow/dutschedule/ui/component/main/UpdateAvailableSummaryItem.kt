package io.zoemeow.dutschedule.ui.component.main

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun UpdateAvailableSummaryItem(
    updateAvailable: Boolean = false,
    isLoading: Boolean = false,
    latestVersionString: String,
    padding: PaddingValues,
    clicked: () -> Unit
) {
    if (updateAvailable) {
        SummaryItem(
            title = if (isLoading) "Checking for updates..." else "Update is available",
            padding = padding,
            clicked = clicked,
            content = {
                Text(
                    text = String.format(
                        "%s\nLatest version: %s",
                        "Tap here to download update file on GitHub (this will open download page in default browser)",
                        latestVersionString
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 15.dp).padding(bottom = 10.dp),
                )
            }
        )
    }
}