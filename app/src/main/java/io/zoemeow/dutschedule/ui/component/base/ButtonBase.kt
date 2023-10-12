package io.zoemeow.dutschedule.ui.component.base

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ButtonBase(
    clicked: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit,
    isOutlinedButton: Boolean = false,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
    cornerSize: Dp = 5.dp,
    modifier: Modifier? = null,
) {
    Surface(
        modifier = (modifier ?: Modifier)
            .clip(RoundedCornerShape(cornerSize))
            .border(BorderStroke(2.dp, MaterialTheme.colorScheme.inversePrimary), shape = RoundedCornerShape(5.dp))
            .clickable { clicked?.let { it() } },
        color = when (isOutlinedButton) {
            true -> MaterialTheme.colorScheme.background
            false -> MaterialTheme.colorScheme.inversePrimary
        },
        content = {
            Row(
                horizontalArrangement = horizontalArrangement,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                content = content,
            )
        },
    )
}