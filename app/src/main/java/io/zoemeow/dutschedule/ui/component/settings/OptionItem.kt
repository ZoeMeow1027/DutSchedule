package io.zoemeow.dutschedule.ui.component.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OptionItem(
    title: String,
    description: String? = null,
    clicked: (() -> Unit)? = null,
    isEnabled: Boolean = true,
    isVisible: Boolean = true,
    padding: PaddingValues = PaddingValues(0.dp)
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(200)),
        exit = fadeOut(animationSpec = tween(200)),
        content = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clickable { if (isEnabled) clicked?.let { it() } },
                contentAlignment = Alignment.CenterStart,
            ) {
                Column(
                    modifier = Modifier.padding(padding),
                    verticalArrangement = Arrangement.Top,
                ) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    if (description != null) {
                        Text(
                            description,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    )
}