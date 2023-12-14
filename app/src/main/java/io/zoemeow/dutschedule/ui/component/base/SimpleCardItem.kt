package io.zoemeow.dutschedule.ui.component.base

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.unit.dp

@Composable
fun SimpleCardItem(
    title: String,
    isTitleCentered: Boolean = false,
    content: @Composable (() -> Unit)? = null,
    clicked: () -> Unit,
    padding: PaddingValues = PaddingValues(10.dp),
    opacity: Float = 1.0f
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
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = opacity))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp),
            horizontalArrangement = if (isTitleCentered) Arrangement.Center else Arrangement.Start,
            content = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp),
                )
            }
        )
        content?.let { it() }
    }
}