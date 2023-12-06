package io.zoemeow.dutschedule.ui.component.main

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SchoolNewsSummaryItem(
    padding: PaddingValues,
    clicked: () -> Unit,
    newsToday: Int = 0,
    newsThisWeek: Int = 0,
    isLoading: Boolean = false
) {
    SummaryItem(
        padding = padding,
        title = "School news",
        isLoading = isLoading,
        content = {
            Text(
                text = String.format(
                    "Tap here to open news.\nNews global: %s new today, %s new last 7 days.",
                    if (newsToday == 0) "No" else newsToday.toString(),
                    if (newsThisWeek == 0) "No" else newsThisWeek.toString()
                ),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 15.dp).padding(bottom = 10.dp),
            )
        },
        clicked = clicked,
    )
}