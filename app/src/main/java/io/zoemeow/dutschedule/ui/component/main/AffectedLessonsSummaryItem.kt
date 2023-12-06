package io.zoemeow.dutschedule.ui.component.main

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AffectedLessonsSummaryItem(
    padding: PaddingValues,
    clicked: () -> Unit,
    hasLoggedIn: Boolean = false,
    affectedList: ArrayList<String> = arrayListOf(),
    isLoading: Boolean = false
) {
    fun affectedListStringBuilder(): String {
        return if (!hasLoggedIn) {
            "You haven't logged in! We can't fetch data for you!\nLog in to continue using this function."
        } else if (affectedList.isEmpty()) {
            "Your lessons don't affected by school announcements right now."
        } else {
            String.format(
                "%s\n\n%s",
                "Your lessons will be affected by school announcements in future:",
                affectedList.joinToString(separator = "\n")
            )
        }
    }

    SummaryItem(
        padding = padding,
        title = "Affected lessons by announcement",
        clicked = clicked,
        isLoading = isLoading,
        content = {
            Text(
                text = affectedListStringBuilder(),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(horizontal = 15.dp)
                    .padding(bottom = 10.dp)
            )
        }
    )
}