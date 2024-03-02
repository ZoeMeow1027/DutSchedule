package io.zoemeow.dutschedule.ui.component.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.zoemeow.dutschedule.activity.MainActivity
import io.zoemeow.dutschedule.ui.component.base.ButtonBase

@Composable
fun MainActivity.NavButton(
    modifier: Modifier = Modifier,
    opacity: Float = 1f,
    clicked: (() -> Unit)? = null,
    badgeText: String? = null,
    badgeContent: @Composable () -> Unit,
    title: String,
    description: String? = null
) {
    ButtonBase(
        modifier = modifier,
        modifierInside = Modifier.height(60.dp).padding(horizontal = 3.dp),
        horizontalArrangement = Arrangement.Start,
        opacity = opacity,
        isOutlinedButton = false,
        cornerSize = 10.dp,
        clicked = {
            clicked?.let { it() }
        },
        content = {
            BadgedBox(
                badge = {
                    badgeText?.let {
                        Badge { Text(it) }
                    }
                },
                content = {
                    badgeContent()
                }
            )
            Spacer(modifier = Modifier.size(7.dp))
            Column(
                modifier = Modifier.fillMaxHeight(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center,
                content = {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleSmall
                    )
                    description?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    }
                }
            )
        }
    )
}