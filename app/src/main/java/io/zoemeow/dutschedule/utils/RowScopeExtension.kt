package io.zoemeow.dutschedule.utils

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun RowScope.TableCell(
    modifier: Modifier = Modifier,
    text: String,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentAlign: Alignment = Alignment.Center,
    textAlign: TextAlign = TextAlign.Start,
    weight: Float
) {
    Surface(
        modifier = modifier.weight(weight),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.inverseSurface),
        color = backgroundColor,
        content = {
            Box(
                modifier = Modifier.padding(1.dp),
                contentAlignment = contentAlign,
                content = {
                    Text(
                        text = text,
                        textAlign = textAlign,
                        modifier = Modifier.padding(8.dp),
                    )
                }
            )
        }
    )
}