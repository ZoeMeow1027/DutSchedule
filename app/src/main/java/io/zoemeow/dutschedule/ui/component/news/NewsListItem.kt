package io.zoemeow.dutschedule.ui.component.news

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.zoemeow.dutschedule.utils.CustomDateUtil

@Composable
fun NewsListItem(
    modifier: Modifier = Modifier,
    opacity: Float = 1f,
    title: String,
    description: String,
    dateTime: Long? = null,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            // https://www.android--code.com/2021/09/jetpack-compose-box-rounded-corners_25.html
            .clip(RoundedCornerShape(10.dp))
            //.background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 1.0f))
            .clickable {
                onClick?.let { it() }
            },
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = opacity),
        content = {
            Column(
                modifier = Modifier.padding(
                    horizontal = 15.dp,
                    vertical = 10.dp
                ),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center,
                content = {
                    // https://stackoverflow.com/questions/2891361/how-to-set-time-zone-of-a-java-util-date
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    dateTime?.let {
                        Text(
                            text = CustomDateUtil.dateUnixToString(it, "dd/MM/yyyy"),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    Spacer(modifier = Modifier.size(15.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        // https://stackoverflow.com/a/65736376
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            )
        }
    )
}