package io.zoemeow.dutschedule.ui.component.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun DividerItem(
    padding: PaddingValues = PaddingValues(0.dp)
) {
    HorizontalDivider(
        thickness = 1.dp,
        color = Color.LightGray,
        modifier = Modifier.padding(padding)
    )
}