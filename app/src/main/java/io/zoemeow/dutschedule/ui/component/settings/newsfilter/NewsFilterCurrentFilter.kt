package io.zoemeow.dutschedule.ui.component.settings.newsfilter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.InputChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.zoemeow.dutschedule.model.settings.SubjectCode
import io.zoemeow.dutschedule.ui.component.base.ContentExpandable

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NewsFilterCurrentFilter(
    selectedSubjects: List<SubjectCode>? = null,
    onRemoveRequested: ((SubjectCode) -> Unit)? = null
) {
    NewsFilterSurface {
        ContentExpandable(
            title = "Your current filter",
            expanded = true,
            onExpanded = {},
            content = {
                when {
                    selectedSubjects.isNullOrEmpty() -> {
                        Text("Your filter list will be here.\n\n- Look like you aren't set up your filter yet.\n- That\'s mean, all subject news will notify you.")
                    }
                    else -> {
                        Text(
                            "Your current filter list (click a item to remove):",
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            horizontalArrangement = Arrangement.Center,
                            verticalArrangement = Arrangement.Top
                        ) {
                            selectedSubjects.forEach { item ->
                                InputChip(
                                    selected = false,
                                    onClick = {
                                        onRemoveRequested?.let { it(item) }
                                    },
                                    label = { Text(item.toString()) },
                                    modifier = Modifier.padding(end = 5.dp)
                                )
                            }
                        }
                    }
                }
            }
        )
    }
}