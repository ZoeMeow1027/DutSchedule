package io.zoemeow.dutschedule.ui.component.permissionrequest

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PermissionInformation(
    title: String,
    description: String,
    isRequired: Boolean = false,
    isGranted: Boolean = false,
    clicked: (() -> Unit)? = null,
    padding: PaddingValues = PaddingValues(0.dp)
) {
    Surface(
        modifier = Modifier.padding(padding)
            .fillMaxWidth()
            .wrapContentHeight()
            .border(
                border = BorderStroke(3.dp, if (isGranted) Color.Green else if (isRequired) Color.Red else Color(0xFFfc9003)),
                shape = RoundedCornerShape(7.dp),
            )
            .clip(shape = RoundedCornerShape(7.dp))
            .clickable { clicked?.let { it() } },
        content = {
            Surface(
                modifier = Modifier.padding(15.dp),
                color = Color.Transparent,
                content = {
                    Column(
                        content = {
                            Text(
                                title,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.W600,
                            )
                            Spacer(modifier = Modifier.size(7.dp))
                            Text(
                                description,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    )
                }
            )
        }
    )
}