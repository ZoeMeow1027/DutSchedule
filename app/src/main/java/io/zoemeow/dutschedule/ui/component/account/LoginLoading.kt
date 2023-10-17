package io.zoemeow.dutschedule.ui.component.account

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoginLoading(
    padding: PaddingValues = PaddingValues(0.dp),
) {
    Surface(
        modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(padding),
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(7.dp),
        content = {
            Box(
                modifier = Modifier.padding(vertical = 50.dp),
                contentAlignment = Alignment.Center,
                content = {
                    CircularProgressIndicator()
                }
            )
        }
    )
}