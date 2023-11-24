package io.zoemeow.dutschedule.ui.component.news

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.dutwrapperlib.dutwrapper.model.news.NewsGlobalItem
import io.zoemeow.dutschedule.model.ProcessState
import io.zoemeow.dutschedule.model.news.NewsGroupByDate
import io.zoemeow.dutschedule.util.CustomDateUtils
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun NewsListPage(
    newsList: List<NewsGroupByDate<NewsGlobalItem>> = listOf(),
    processState: ProcessState = ProcessState.NotRunYet,
    endOfListReached: (() -> Unit)? = null,
    itemClicked: ((NewsGlobalItem) -> Unit)? = null
) {
    val lazyListState = rememberLazyListState()
    when {
        (processState == ProcessState.Running && newsList.isEmpty()) -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        }
        else -> {
            NewsListPage_EndOfListHandler(
                listState = lazyListState,
                onLoadMore = { endOfListReached?.let { it() } }
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 20.dp, end = 20.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top,
                state = lazyListState,
                content = {
                    items (newsList) {
                        Column(
                            modifier = Modifier.padding(bottom = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = CustomDateUtils.dateToString(it.date, "dd/MM/yyyy"),
                                modifier = Modifier.padding(bottom = 5.dp)
                            )
                            it.itemList.forEach { newsGroupSubItem ->
                                NewsListItem(
                                    title = newsGroupSubItem.title ?: "",
                                    description = newsGroupSubItem.contentString ?: "",
                                    onClick = {
                                        itemClicked?.let { it(newsGroupSubItem) }
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.size(10.dp))
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun NewsListPage_EndOfListHandler(
    listState: LazyListState,
    buffer: Int = 1,
    onLoadMore: () -> Unit
) {
    val loadMore = remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItemsNumber = layoutInfo.totalItemsCount
            val lastVisibleItemIndex =
                (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1

            lastVisibleItemIndex > (totalItemsNumber - buffer)
        }
    }

    LaunchedEffect(loadMore) {
        snapshotFlow { loadMore.value }
            .distinctUntilChanged()
            .collect {
                if (loadMore.value)
                    onLoadMore()
            }
    }
}