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
import io.zoemeow.dutschedule.ui.component.base.ContentExpandable

@Composable
fun NewsFilterClearAll(
    expanded: Boolean = false,
    onExpanded: (() -> Unit)? = null
) {
    NewsFilterSurface {
        ContentExpandable(
            title = "Clear all filters",
            expanded = expanded,
            onExpanded = { onExpanded?.let { it() } },
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Clear your all subject filters.",
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
                    Button(
                        content = { Text("Clear all") },
                        onClick = {
//                            newsFilterViewModel.selectedSubjects.clear()
//                            newsFilterViewModel.modifiedSettings.value = true
//                            updateTemporarySettings()
//                            showSnackBarMessage(getString(R.string.subjectnewsfilter_snackbar_deletedall))
                        }
                    )
                }
            }
        )
    }
}