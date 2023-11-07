package io.zoemeow.dutschedule.ui.component.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun OptionSwitchItem(
    title: String,
    description: String? = null,
    onValueChanged: ((Boolean) -> Unit)? = null,
    switchChecked: Boolean = false,
    isEnabled: Boolean = true,
    isVisible: Boolean = true,
    padding: PaddingValues = PaddingValues(0.dp)
) {
    val interactionSource = remember { MutableInteractionSource() }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(200)),
        exit = fadeOut(animationSpec = tween(200)),
        content = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clickable(
                        interactionSource = interactionSource,
                        indication = rememberRipple(),
                        onClick = {
                            if (isEnabled) onValueChanged?.let { it(!switchChecked) }
                        }
                    ),
                color = Color.Transparent,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(padding),
                    verticalAlignment = Alignment.CenterVertically,
                    content = {
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
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = switchChecked,
                            onCheckedChange = { onValueChanged?.let { it(!switchChecked) } },
                            enabled = isEnabled,
                            modifier = Modifier.width(IntrinsicSize.Max)
                        )
                    }
                )
            }
        }
    )
}