package io.zoemeow.dutschedule.ui.component.base

import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun CheckboxOption(
    modifier: Modifier = Modifier,
    modifierInside: Modifier = Modifier,
    title: String,
    description: String? = null,
    onClick: () -> Unit,
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
            Checkbox(
                checked = isChecked,
                onCheckedChange = { onClick() },
                enabled = isEnabled,
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
private fun CheckboxOptionPreview() {
    CheckboxOption(
        title = "This title",
        description = "This description",
        onClick = { },
        isChecked = true
    )
}

@Preview
@Composable
private fun CheckboxWithoutDescriptionPreview() {
    CheckboxOption(
        title = "This title",
        onClick = { },
        isChecked = true
    )
}