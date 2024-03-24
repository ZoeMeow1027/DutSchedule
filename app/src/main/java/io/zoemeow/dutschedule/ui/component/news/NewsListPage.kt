package io.zoemeow.dutschedule.ui.component.news

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.dutwrapper.dutwrapper.model.news.NewsGlobalItem
import io.zoemeow.dutschedule.model.ProcessState
import io.zoemeow.dutschedule.utils.CustomDateUtil
import io.zoemeow.dutschedule.utils.endOfListReached

@Composable
fun NewsListPage(
    newsList: List<NewsGlobalItem> = listOf(),
    processState: ProcessState = ProcessState.NotRunYet,
    endOfListReached: (() -> Unit)? = null,
    itemClicked: ((NewsGlobalItem) -> Unit)? = null,
    lazyListState: LazyListState = rememberLazyListState(),
    opacity: Float = 1f
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        content = {
            if (processState == ProcessState.Running) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 20.dp, end = 20.dp)
                    .endOfListReached(
                        lazyListState = lazyListState,
                        endOfListReached = { endOfListReached?.let { it() } }
                    ),
                horizontalAlignment = if (newsList.isNotEmpty()) Alignment.Start else Alignment.CenterHorizontally,
                verticalArrangement = if (newsList.isNotEmpty()) Arrangement.Top else Arrangement.Center,
                state = lazyListState,
                content = {
                    when {
                        (newsList.isNotEmpty()) -> {
                            newsList.groupBy { p -> p.date }.forEach { newsGroup ->
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        content = {
                                            Text(
                                                text = CustomDateUtil.dateUnixToString(newsGroup.key, "dd/MM/yyyy"),
                                                modifier = Modifier.padding(bottom = 5.dp)
                                            )
                                        }
                                    )
                                }
                                items (newsGroup.value) { newsItem ->
                                    NewsListItem(
                                        title = newsItem.title ?: "",
                                        description = newsItem.contentString ?: "",
                                        opacity = opacity,
                                        onClick = {
                                            itemClicked?.let { it(newsItem) }
                                        }
                                    )
                                }
                                item {
                                    Spacer(modifier = Modifier.size(10.dp))
                                }
                            }
                        }
                        (processState == ProcessState.Running && newsList.isEmpty()) -> {
                            item {
                                CircularProgressIndicator()
                            }
                        }
                        else -> {

                        }
                    }
                }
            )
        }
    )
}
