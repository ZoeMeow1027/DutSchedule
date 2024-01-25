package io.zoemeow.dutschedule.ui.component.base

import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun OptionCheckBoxItem(
    modifier: Modifier = Modifier,
    title: String,
    description: String? = null,
    onValueChanged: (Boolean) -> Unit,
    isChecked: Boolean = false,
    isEnabled: Boolean = true,
    isVisible: Boolean = true,
) {
    OptionItem(
        modifier = modifier,
        title = title,
        description = description,
        leadingIcon = {
            Checkbox(
                checked = isChecked,
                onCheckedChange = { if (isEnabled) onValueChanged(!isChecked) },
                enabled = isEnabled
            )
        },
        onClick = {
            if (isEnabled) onValueChanged(!isChecked)
        },
        isEnabled = isEnabled,
        isVisible = isVisible
    )
}