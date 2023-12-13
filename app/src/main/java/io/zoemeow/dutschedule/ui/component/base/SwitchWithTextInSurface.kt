package io.zoemeow.dutschedule.ui.component.base

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SwitchWithTextInSurface(
    modifier: Modifier = Modifier,
    text: String,
    enabled: Boolean = true,
    checked: Boolean = false,
    opacity: Float = 1f,
    onCheckedChange: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = modifier
            .padding(20.dp)
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(),
                onClick = {
                    if (enabled) {
                        onCheckedChange?.let { it() }
                    }
                }
            ),
        color = when (enabled && checked) {
            true -> MaterialTheme.colorScheme.primary
            false -> MaterialTheme.colorScheme.secondary
        }.copy(alpha = opacity),
        shape = RoundedCornerShape(30.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = {
                Column(
                    modifier = Modifier
                        .wrapContentHeight()
                        .weight(1f, fill = true),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Top,
                    content = {
                        Text(
                            text,
                            style = MaterialTheme.typography.titleLarge,
                        )
//                                            if (description != null) {
//                                                Text(
//                                                    description,
//                                                    style = MaterialTheme.typography.bodyMedium,
//                                                )
//                                            }
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = checked,
                    enabled = enabled,
                    onCheckedChange = { if (enabled) { onCheckedChange?.let { it() } } },
                    modifier = Modifier.width(IntrinsicSize.Max),
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = MaterialTheme.colorScheme.secondaryContainer,
                        checkedBorderColor = MaterialTheme.colorScheme.secondaryContainer,
                        checkedThumbColor = MaterialTheme.colorScheme.secondary,
                        uncheckedTrackColor = MaterialTheme.colorScheme.secondaryContainer,
                        uncheckedBorderColor = MaterialTheme.colorScheme.secondaryContainer,
                        uncheckedThumbColor = MaterialTheme.colorScheme.secondary
                    )
                )
            }
        )
    }
}