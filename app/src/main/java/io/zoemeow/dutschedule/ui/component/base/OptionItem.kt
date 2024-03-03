package io.zoemeow.dutschedule.ui.component.base

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun OptionItem(
    modifier: Modifier = Modifier,
    modifierInside: Modifier = Modifier.padding(vertical = 15.dp),
    title: String,
    description: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
    isEnabled: Boolean = true,
    isVisible: Boolean = true
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(200)),
        exit = fadeOut(animationSpec = tween(200)),
        content = {
            Surface(
                modifier = modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clickable { if (isEnabled) onClick() },
                color = Color.Transparent,
                content = {
                    Row(
                        modifier = modifierInside,
                        verticalAlignment = Alignment.CenterVertically,
                        content = {
                            leadingIcon?.let {
                                it()
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Column(
                                modifier = Modifier.wrapContentHeight()
                                    .weight(1f, fill = true),
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.Top,
                                content = {
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
                            )
                            trailingIcon?.let {
                                Spacer(modifier = Modifier.width(8.dp))
                                it()
                            }
                        }
                    )
                }
            )
        }
    )
}