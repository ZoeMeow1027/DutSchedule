package io.zoemeow.dutschedule.ui.component.news

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
    Column(modifier = modifier) {
        if (processState == ProcessState.Running) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        if (newsList.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when (processState) {
                    ProcessState.Running -> {
                        Text(
                            "Fetching news. Please wait...",
                            textAlign = TextAlign.Center
                        )
                    }
                    ProcessState.NotRunYet -> {
                        Text(
                            "Tap search on top to get started.",
                            textAlign = TextAlign.Center
                        )
                    }
                    else -> {
                        Text(
                            "No available news matches your search. Try again with new query.",
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .endOfListReached(
                        lazyListState = lazyListState,
                        endOfListReached = {
                            onEndOfList?.let { it() }
                        }
                    ),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
                state = lazyListState,
                content = {
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
            )
        }

    }
}