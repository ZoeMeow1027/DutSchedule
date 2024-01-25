package io.zoemeow.dutschedule.ui.component.settings.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.zoemeow.dutschedule.activity.SettingsActivity
import io.zoemeow.dutschedule.ui.component.base.DialogBase

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsActivity.DialogFetchNewsInBackgroundSettings(
    isVisible: Boolean = false,
    value: Int = 0,
    onDismiss: () -> Unit,
    onValueChanged: (Int) -> Unit
) {
    val duration = remember { mutableIntStateOf(0) }

    LaunchedEffect(isVisible) {
        duration.intValue = value
    }

    DialogBase(
        modifier = Modifier
            .fillMaxWidth()
            .padding(25.dp),
        title = "Fetch news in background",
        isVisible = isVisible,
        canDismiss = true,
        isTitleCentered = true,
        dismissClicked = onDismiss,
        content = {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    "Drag slider below to adjust news background duration." +
                            "\n - Drag slider to 0 to disable this function." +
                            "\n - If you set this value below than 5 minutes, this will automatically adjust back to 5 minutes.",
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                Slider(
                    valueRange = 0f..240f,
                    steps = 241,
                    value = duration.intValue.toFloat(),
                    colors = SliderDefaults.colors(
                        activeTickColor = Color.Transparent,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTickColor = Color.Transparent,
                        inactiveTrackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                    ),
                    onValueChange = {
                        duration.intValue = it.toInt()
                    }
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    content = {
                        Text("${duration.intValue} minute${if (duration.intValue != 1) "s" else ""}")
                    }
                )
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(top = 7.dp),
                    horizontalArrangement = Arrangement.Center,
                    content = {
                        listOf(0, 5, 15, 30, 60).forEach { min ->
                            SuggestionChip(
                                modifier = Modifier.padding(horizontal = 5.dp),
                                onClick = {
                                    duration.intValue = min
                                },
                                label = { Text(if (min == 0) "Turn off" else "$min min") }
                            )
                        }
                    }
                )
            }
        },
        actionButtons = {
            TextButton(
                onClick = { onValueChanged(duration.intValue) },
                content = { Text("Save") },
                modifier = Modifier.padding(start = 8.dp),
            )
            TextButton(
                onClick = onDismiss,
                content = { Text("Cancel") },
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    )
}