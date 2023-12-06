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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.zoemeow.dutschedule.ui.component.base.SimpleCardItem

@Composable
fun SummaryItem(
    title: String,
    content: @Composable () -> Unit,
    isLoading: Boolean = false,
    clicked: () -> Unit,
    padding: PaddingValues = PaddingValues(10.dp),
) {
    SimpleCardItem(
        title = title,
        clicked = clicked,
        padding = padding,
        content = {
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
                    CircularProgressIndicator()
                }
            } else content()
        }
    )
}