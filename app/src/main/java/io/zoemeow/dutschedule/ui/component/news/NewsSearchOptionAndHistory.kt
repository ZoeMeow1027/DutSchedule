package io.zoemeow.dutschedule.ui.component.news

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import io.dutwrapper.dutwrapper.model.enums.NewsSearchType
import io.dutwrapper.dutwrapper.model.enums.NewsType
import io.zoemeow.dutschedule.R
import io.zoemeow.dutschedule.model.news.NewsSearchHistory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsSearchOptionAndHistory(
    modifier: Modifier = Modifier,
    isVisible: MutableTransitionState<Boolean>,
    searchHistory: List<NewsSearchHistory>,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    query: String = "",
    newsMethod: NewsSearchType,
    newsType: NewsType,
    onSettingsChanged: ((String, NewsSearchType, NewsType) -> Unit)? = null,
    onSearchTriggered: (() -> Unit)? = null,
    onClearHistoryTriggered: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null
) {
    // Search method option (by title/by content)
    val searchMethodOptionVisible = remember { mutableStateOf(false) }

    // Search type option (news global/news subject)
    val searchTypeOptionVisible = mutableStateOf(false)

    fun dismiss() {
        searchMethodOptionVisible.value = false
        searchTypeOptionVisible.value = false
        onDismiss?.let { it() }
    }

    AnimatedVisibility(
        visibleState = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        content = {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        dismiss()
                    },
                color = backgroundColor,
                content = {
                    Column(
                        modifier = modifier.clickable {
                            dismiss()
                        },
                        content = {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        dismiss()
                                    },
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.Top,
                                content = {
                                    ExposedDropdownMenuBox(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 5.dp)
                                            .padding(bottom = 5.dp),
                                        expanded = searchMethodOptionVisible.value,
                                        onExpandedChange = {
                                            searchMethodOptionVisible.value = !searchMethodOptionVisible.value
                                        },
                                        content = {
                                            OutlinedTextField(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .menuAnchor(),
                                                label = { Text("Search method") },
                                                readOnly = true,
                                                value = when (newsMethod) {
                                                    NewsSearchType.ByTitle -> "By title"
                                                    NewsSearchType.ByContent -> "By content"
                                                },
                                                onValueChange = { }
                                            )
                                            DropdownMenu(
                                                expanded = searchMethodOptionVisible.value,
                                                onDismissRequest = {
                                                    searchMethodOptionVisible.value = false
                                                },
                                                content = {
                                                    NewsSearchType.values().forEach { item ->
                                                        DropdownMenuItem(
                                                            modifier = Modifier.background(
                                                                color = if (newsMethod == item) MaterialTheme.colorScheme.secondaryContainer
                                                                else MaterialTheme.colorScheme.surface
                                                            ),
                                                            text = {
                                                                Text(
                                                                    when (item) {
                                                                        NewsSearchType.ByTitle -> "By title"
                                                                        NewsSearchType.ByContent -> "By content"
                                                                    }
                                                                )
                                                            },
                                                            onClick = {
                                                                onSettingsChanged?.let { it(query, item, newsType) }
                                                                searchMethodOptionVisible.value = false
                                                            }
                                                        )
                                                    }
                                                }
                                            )
                                        }
                                    )
                                    ExposedDropdownMenuBox(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 5.dp)
                                            .padding(bottom = 5.dp),
                                        expanded = searchTypeOptionVisible.value,
                                        onExpandedChange = {
                                            searchTypeOptionVisible.value = !searchTypeOptionVisible.value
                                        },
                                        content = {
                                            OutlinedTextField(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .menuAnchor(),
                                                label = { Text("News type") },
                                                readOnly = true,
                                                value = when (newsType) {
                                                    NewsType.Subject -> "News subject"
                                                    NewsType.Global -> "News global"
                                                },
                                                onValueChange = { }
                                            )
                                            DropdownMenu(
                                                expanded = searchTypeOptionVisible.value,
                                                onDismissRequest = {
                                                    searchTypeOptionVisible.value = false
                                                },
                                                content = {
                                                    NewsType.values().forEach { item ->
                                                        DropdownMenuItem(
                                                            modifier = Modifier.background(
                                                                color = if (newsType == item) MaterialTheme.colorScheme.secondaryContainer
                                                                else MaterialTheme.colorScheme.surface
                                                            ),
                                                            text = {
                                                                Text(
                                                                    when (item) {
                                                                        NewsType.Subject -> "News subject"
                                                                        NewsType.Global -> "News global"
                                                                    }
                                                                )
                                                            },
                                                            onClick = {
                                                                onSettingsChanged?.let {
                                                                    it(query, newsMethod, item)
                                                                }
                                                                searchTypeOptionVisible.value = false
                                                            }
                                                        )
                                                    }
                                                }
                                            )
                                        }
                                    )
                                }
                            )
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                content = {
                                    Row(
                                        modifier = Modifier
                                            .padding(horizontal = 3.dp, vertical = 5.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                        content = {
                                            Text(
                                                "Search history",
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Spacer(modifier = Modifier.weight(1f))
                                            IconButton(
                                                content = { Icon(Icons.Default.Delete, "") },
                                                onClick = {
                                                    onClearHistoryTriggered?.let { it() }
                                                }
                                            )
                                        }
                                    )
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .verticalScroll(rememberScrollState()),
                                        content = {
                                            searchHistory.forEach { queryItem ->
                                                ListItem(
                                                    headlineContent = {
                                                        Column {
                                                            Text(queryItem.query)
                                                            Text(
                                                                "Search ${
                                                                    when (queryItem.newsMethod) {
                                                                        NewsSearchType.ByTitle -> "by title"
                                                                        NewsSearchType.ByContent -> "by content"
                                                                    }
                                                                } in ${
                                                                    when (queryItem.newsType) {
                                                                        NewsType.Subject -> "News subject"
                                                                        NewsType.Global -> "News global"
                                                                    }
                                                                }",
                                                                style = MaterialTheme.typography.bodyMedium,
                                                            )
                                                        }
                                                    },
                                                    leadingContent = {
                                                        Icon(Icons.Outlined.Search, "")
                                                    },
                                                    trailingContent = {
                                                        IconButton(
                                                            content = {
                                                                Icon(imageVector = ImageVector.vectorResource(
                                                                    R.drawable.ic_baseline_north_west_24), "")
                                                            },
                                                            onClick = {
                                                                onSettingsChanged?.let { it(
                                                                    queryItem.query,
                                                                    queryItem.newsMethod,
                                                                    queryItem.newsType
                                                                ) }
                                                            }
                                                        )
                                                    },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            onSettingsChanged?.let { it(
                                                                queryItem.query,
                                                                queryItem.newsMethod,
                                                                queryItem.newsType
                                                            ) }
                                                            onSearchTriggered?.let { it() }
                                                            onDismiss?.let { it() }
                                                        }
                                                )
                                            }
                                        }
                                    )
                                }
                            )
                        }
                    )
                }
            )
        }
    )
}