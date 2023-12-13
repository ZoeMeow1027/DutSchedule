package io.zoemeow.dutschedule.ui.component.helpandexternallink

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.zoemeow.dutschedule.model.helpandexternallink.HelpLinkInfo

@Composable
fun HelpLinkClickable(
    item: HelpLinkInfo,
    modifier: Modifier = Modifier,
    opacity: Float = 1f,
    linkClicked: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .clickable { linkClicked?.let { it() } },
        color = MaterialTheme.colorScheme.secondaryContainer.copy(
            alpha = opacity
        ),
        shape = RoundedCornerShape(10.dp),
        content = {
            Column(
                modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp),
                content = {
                    Text(
                        item.title,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        item.url,
                        modifier = Modifier.padding(bottom = 15.dp)
                    )
                    Text(item.description ?: "(no description provided)")
                }
            )
        }
    )
}