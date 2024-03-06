package io.zoemeow.dutschedule.ui.component.base

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OptionSwitchItem(
    modifier: Modifier = Modifier,
    modifierInside: Modifier = Modifier.padding(vertical = 15.dp),
    leadingIcon: (@Composable () -> Unit)? = null,
    title: String,
    description: String? = null,
    onValueChanged: (Boolean) -> Unit,
    isChecked: Boolean = false,
    isEnabled: Boolean = true,
    isVisible: Boolean = true
) {
    OptionItem(
        modifier = modifier,
        modifierInside = modifierInside,
        title = title,
        description = description,
        leadingIcon = leadingIcon,
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