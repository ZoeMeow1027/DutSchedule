package io.zoemeow.dutschedule.ui.component.base

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ExpandableContent(
    modifier: Modifier = Modifier,
    title: @Composable RowScope. () -> Unit,
    isTitleCentered: Boolean = false,
    onTitleClicked: (() -> Unit)? = null,
    content: @Composable ColumnScope. () -> Unit,
    isContentCentered: Boolean = false,
    isContentVisible: Boolean = false,
    opacity: Float = 1.0f
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 5.dp)
            .padding(top = 10.dp)
            .clip(RoundedCornerShape(7.dp)),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = opacity),
        content = {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                content = {
                    Row(
                        modifier = Modifier.fillMaxWidth().wrapContentHeight()
                            .clickable { onTitleClicked?.let { it() } },
                        horizontalArrangement = when (isTitleCentered) {
                            true -> Arrangement.Center
                            false -> Arrangement.Start
                        },
                        content = title,
                    )
                    AnimatedVisibility(
                        visible = isContentVisible,
                        content = {
                            Column(
                                modifier = Modifier.fillMaxWidth().wrapContentHeight()
                                    .padding(horizontal = 15.dp).padding(bottom = 15.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = when (isContentCentered) {
                                    false -> Alignment.Start
                                    true -> Alignment.CenterHorizontally
                                },
                                content = content
                            )
                        }
                    )
                }
            )
        }
    )
}

@Composable
fun ExpandableContentDefaultTitle(
    modifier: Modifier = Modifier,
    title: String,
    textAlign: TextAlign = TextAlign.Center
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        textAlign = textAlign,
        modifier = modifier.padding(15.dp)
    )
}