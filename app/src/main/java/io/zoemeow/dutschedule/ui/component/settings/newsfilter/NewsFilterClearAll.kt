package io.zoemeow.dutschedule.ui.component.settings.newsfilter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.zoemeow.dutschedule.ui.component.base.ExpandableContent
import io.zoemeow.dutschedule.ui.component.base.ExpandableContentDefaultTitle

@Composable
fun NewsFilterClearAll(
    expanded: Boolean = false,
    onExpanded: (() -> Unit)? = null,
    onSubmit: (() -> Unit)? = null
) {
    ExpandableContent(
        title = {
            ExpandableContentDefaultTitle(title = "Clear all filters")
        },
        isTitleCentered = true,
        onTitleClicked = onExpanded,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Just click button below to clear all.\nNote:\n- This will delete all filters you added before and cannot be undone.\n- If you want to revert, close this settings and choose UNSAVE CHANGES. This is your ONLY chance to undo your action.",
                    modifier = Modifier.padding(bottom = 5.dp)
                )
                Button(
                    content = { Text("Clear all") },
                    onClick = {
                        onSubmit?.let { it() }
                    }
                )
            }
        },
        isContentVisible = expanded
    )
}