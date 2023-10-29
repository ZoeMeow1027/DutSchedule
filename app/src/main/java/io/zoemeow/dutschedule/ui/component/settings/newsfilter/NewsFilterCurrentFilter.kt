package io.zoemeow.dutschedule.ui.component.settings.newsfilter

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import io.zoemeow.dutschedule.ui.component.base.ContentExpandable

@Composable
fun NewsFilterCurrentFilter() {
    NewsFilterSurface {
        ContentExpandable(
            title = "Your current filter",
            expanded = true,
            onExpanded = { },
            content = {
                Text("Your current filter will show here")
            }
        )
    }
}