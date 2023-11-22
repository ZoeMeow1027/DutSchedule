package io.zoemeow.dutschedule.ui.component.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun SchoolNewsSummaryItem(
    padding: PaddingValues,
    clicked: () -> Unit,
    newsToday: Int = 0,
    newsThisWeek: Int = 0,
    isLoading: Boolean = false
) {
    if (isLoading) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(padding)
                .clip(RoundedCornerShape(7.dp))
                .clickable { clicked() }
                .background(MaterialTheme.colorScheme.secondaryContainer),
        ) {
            Text(
                text = "School news",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp),
            )
            CircularProgressIndicator()
        }
    } else {
        SummaryItem(
            padding = padding,
            title = "School news",
            content = String.format(
                "Tap here to open news.\n\n%s new global announcement%s today.\n%s new subject announcement%s last 7 days.",
                if (newsToday == 0) "No" else newsToday.toString(),
                if (newsToday != 1) "s" else "",
                if (newsThisWeek == 0) "No" else newsThisWeek.toString(),
                if (newsThisWeek != 1) "s" else ""
            ),
            clicked = clicked,
        )
    }
}