package io.zoemeow.dutschedule.ui.component.base

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun RadioButtonOption(
    modifier: Modifier = Modifier,
    modifierInside: Modifier = Modifier.padding(vertical = 15.dp),
    title: String,
    description: String? = null,
    onClick: () -> Unit,
    radioButtonColors: RadioButtonColors = RadioButtonDefaults.colors(),
    isChecked: Boolean = false,
    isEnabled: Boolean = true,
    isVisible: Boolean = true
) {
    OptionItem(
        modifier = modifier,
        modifierInside = modifierInside,
        title = title,
        description = description,
        leadingIcon = {
            RadioButton(
                selected = isChecked,
                onClick = onClick,
                enabled = isEnabled,
                colors = radioButtonColors
            )
        },
        onClick = {
            if (isEnabled) onClick()
        },
        isEnabled = isEnabled,
        isVisible = isVisible
    )
}

@Preview
@Composable
private fun RadioButtonOptionPreview() {
    RadioButtonOption(
        title = "This title",
        description = "This description",
        onClick = { },
        isChecked = true
    )
}

@Preview
@Composable
private fun RadioButtonOptionWithoutDescriptionPreview() {
    RadioButtonOption(
        title = "This title",
        onClick = { },
        isChecked = true
    )
}