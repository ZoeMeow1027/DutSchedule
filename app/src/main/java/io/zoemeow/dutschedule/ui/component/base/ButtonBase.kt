package io.zoemeow.dutschedule.ui.component.base

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ButtonBase(
    modifier: Modifier = Modifier,
    modifierInside: Modifier = Modifier,
    clicked: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit,
    isOutlinedButton: Boolean = false,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
    cornerSize: Dp = 5.dp,
    opacity: Float = 1f
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(cornerSize))
            .border(
                BorderStroke(
                    if (opacity == 1f) 2.dp else 0.dp,
                    MaterialTheme.colorScheme.inversePrimary
                ),
                shape = RoundedCornerShape(5.dp)
            )
            .clickable { clicked?.let { it() } },
        color = when (isOutlinedButton) {
            true -> MaterialTheme.colorScheme.background.copy(alpha = opacity)
            false -> MaterialTheme.colorScheme.inversePrimary.copy(alpha = opacity)
        },
        content = {
            Row(
                horizontalArrangement = horizontalArrangement,
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifierInside.padding(vertical = 8.dp, horizontal = 12.dp),
                content = content,
            )
        },
    )
}

@Preview
@Composable
private fun ButtonWithTwoLinesPreview() {
    ButtonBase(
        modifier = Modifier.padding(bottom = 10.dp),
        modifierInside = Modifier.height(60.dp),
        horizontalArrangement = Arrangement.Start,
        opacity = 1f,
        isOutlinedButton = false,
        cornerSize = 10.dp,
        clicked = { },
        content = {
            BadgedBox(
                badge = {
                    // Badge { }
                },
                content = {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        "Account",
                        modifier = Modifier.size(27.dp)
                    )
                }
            )
            Spacer(modifier = Modifier.size(7.dp))
            Column(
                modifier = Modifier.fillMaxHeight(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center,
                content = {
                    Text(
                        "Account",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        "Not logged in",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            )
        }
    )
}