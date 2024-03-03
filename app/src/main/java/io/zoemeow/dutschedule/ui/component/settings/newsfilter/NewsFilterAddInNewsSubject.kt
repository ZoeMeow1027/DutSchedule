package io.zoemeow.dutschedule.ui.component.settings.newsfilter

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.zoemeow.dutschedule.ui.component.base.ExpandableContent
import io.zoemeow.dutschedule.ui.component.base.ExpandableContentDefaultTitle

@Composable
fun NewsFilterAddInNewsSubject(
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    onExpanded: (() -> Unit)? = null,
    opacity: Float = 1.0f
) {
    ExpandableContent(
        modifier = modifier,
        opacity = opacity,
        title = {
            ExpandableContentDefaultTitle(title = "Add filter via news subject or subject schedule")
        },
        isTitleCentered = true,
        onTitleClicked = onExpanded,
        content = {
            Text("This will make you add filter easier than manually add option below.\n\nYou can do this by following one of these options below:\n - Navigate to your subject information, click a subject and click \"Add to news filter\". Note: You need to be logged in first.\n - Navigate to news subject, click a news subject announcement that contains your subject, and click \"Add to news filter\".")
        },
        isContentVisible = expanded
    )
}