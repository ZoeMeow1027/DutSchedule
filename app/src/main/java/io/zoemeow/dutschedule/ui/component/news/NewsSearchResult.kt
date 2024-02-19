package io.zoemeow.dutschedule.ui.component.news

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.dutwrapper.dutwrapper.model.news.NewsGlobalItem
import io.zoemeow.dutschedule.model.ProcessState
import io.zoemeow.dutschedule.utils.endOfListReached

@Composable
fun NewsSearchResult(
    modifier: Modifier = Modifier,
    newsList: List<NewsGlobalItem>,
    lazyListState: LazyListState,
    opacity: Float = 1f,
    processState: ProcessState,
    onEndOfList: (() -> Unit)? = null,
    onItemClicked: ((NewsGlobalItem) -> Unit)? = null
) {
    LazyColumn(
        modifier = modifier
            .endOfListReached(
                lazyListState = lazyListState,
                endOfListReached = {
                    onEndOfList?.let { it() }
                }
            ),
        verticalArrangement = if (newsList.isNotEmpty() && processState != ProcessState.Running) Arrangement.Top else Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        state = lazyListState,
        content = {
            if (processState == ProcessState.Running) {
                item {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
            when {
                (newsList.isNotEmpty()) -> {
                    items(newsList) { item ->
                        NewsListItem(
                            title = item.title,
                            description = item.contentString,
                            dateTime = item.date,
                            opacity = opacity,
                            onClick = {
                                onItemClicked?.let { it(item) }
                            }
                        )
                        Spacer(modifier = Modifier.size(3.dp))
                    }
                }
                (processState == ProcessState.Running) -> {
                    item {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                            content = {
                                Text(
                                    "Fetching news. Please wait...",
                                    textAlign = TextAlign.Center
                                )
                            }
                        )
                    }
                }
                (processState == ProcessState.NotRunYet) -> {
                    item {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                            content = {
                                Text(
                                    "Tap search on top to get started.",
                                    textAlign = TextAlign.Center
                                )
                            }
                        )
                    }
                }
                else -> {
                    item {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                            content = {
                                Text(
                                    "No available news matches your search. Try again with new query.",
                                    textAlign = TextAlign.Center
                                )
                            }
                        )
                    }
                }
            }
        }
    )
}