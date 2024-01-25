package io.zoemeow.dutschedule.ui.component.base

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun OptionSwitchItem(
    modifier: Modifier = Modifier,
    title: String,
    description: String? = null,
    onValueChanged: (Boolean) -> Unit,
    isChecked: Boolean = false,
    isEnabled: Boolean = true,
    isVisible: Boolean = true
) {
    OptionItem(
        modifier = modifier,
        title = title,
        description = description,
        trailingIcon = {
            Switch(
                checked = isChecked,
                onCheckedChange = { onValueChanged(!isChecked) },
                enabled = isEnabled,
                modifier = Modifier.width(IntrinsicSize.Max)
            )
        },
        onClick = {
            if (isEnabled) onValueChanged(!isChecked)
        },
        isEnabled = isEnabled,
        isVisible = isVisible
    )
}