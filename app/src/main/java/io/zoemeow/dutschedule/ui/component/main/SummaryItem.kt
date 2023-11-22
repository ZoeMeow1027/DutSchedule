package io.zoemeow.dutschedule.ui.component.main

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.zoemeow.dutschedule.ui.component.base.SimpleCardItem
import io.zoemeow.dutschedule.ui.theme.DutScheduleTheme

@Composable
fun SummaryItem(
    title: String,
    content: String,
    clicked: () -> Unit,
    padding: PaddingValues = PaddingValues(10.dp),
) {
    SimpleCardItem(
        title = title,
        clicked = clicked,
        padding = padding,
        content = {
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 15.dp).padding(bottom = 10.dp),
            )
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun SummaryItemPreview() {
    DutScheduleTheme {
        SummaryItem(title = "Today schedule", content = "You have 2 subjects left", clicked = {})
    }
}