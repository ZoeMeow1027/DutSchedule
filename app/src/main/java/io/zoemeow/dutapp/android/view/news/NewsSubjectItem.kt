package io.zoemeow.dutapp.android.view.news

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun NewsSubjectItem(
    date: String,
    title: String,
    summary: String,
    clickable: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 5.dp, bottom = 5.dp)
            // https://www.android--code.com/2021/09/jetpack-compose-box-rounded-corners_25.html
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f))
            .clickable { clickable() }
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(start = 15.dp, end = 15.dp, top = 10.dp, bottom = 10.dp)
        ) {
            // https://stackoverflow.com/questions/2891361/how-to-set-time-zone-of-a-java-util-date
            Text(
                text = date,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.size(20.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.size(5.dp))
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
                // https://stackoverflow.com/a/65736376
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}