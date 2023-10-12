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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.zoemeow.dutschedule.ui.theme.DutScheduleTheme

@Composable
fun SummaryItem(
    title: String,
    content: String,
    clicked: () -> Unit,
    padding: PaddingValues = PaddingValues(10.dp),
) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(padding)
            .clip(RoundedCornerShape(7.dp))
            .clickable { clicked() }
            .background(MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp),
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 15.dp).padding(bottom = 10.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SummaryItemPreview() {
    DutScheduleTheme {
        SummaryItem(title = "Today schedule", content = "You have 2 subjects left", clicked = {})
    }
}