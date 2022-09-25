package io.zoemeow.subjectnotifier.ui.controls

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun CustomTitleAndExpandableColumn(
    modifier: Modifier = Modifier,
    titleModifier: Modifier = Modifier,
    title: String,
    expanded: Boolean = true,
    onExpanded: () -> Unit,
    contentModifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clickable { onExpanded() },
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = titleModifier.padding(15.dp)
            )
        }
        AnimatedVisibility(
            visible = expanded
        ) {
            Column(
                modifier = contentModifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(start = 15.dp, end = 15.dp, bottom = 15.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start,
                content = content
            )
        }
    }
}