package io.zoemeow.subjectnotifier.view.news

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import io.zoemeow.dutapi.objects.news.NewsSubjectItem
import io.zoemeow.subjectnotifier.model.enums.ProcessState
import io.zoemeow.subjectnotifier.model.news.NewsGroupByDate
import io.zoemeow.subjectnotifier.utils.AppUtils
import io.zoemeow.subjectnotifier.utils.DUTDateUtils.Companion.dateToString

@Composable
fun NewsSubject(
    newsSubjectList: SnapshotStateList<NewsGroupByDate<NewsSubjectItem>>,
    isLoading: ProcessState,
    lazyListState: LazyListState,
    reloadRequested: (Boolean) -> Unit,
    itemClicked: (NewsSubjectItem) -> Unit
) {
    val swipeRefreshState = rememberSwipeRefreshState(false)
    LaunchedEffect(isLoading) {
        swipeRefreshState.isRefreshing = (isLoading == ProcessState.Running)
    }

    AppUtils.LazyList_EndOfListHandler(
        listState = lazyListState,
        onLoadMore = { reloadRequested(false) }
    )

    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = {
            swipeRefreshState.isRefreshing = true
            reloadRequested(true)
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 20.dp, end = 20.dp),
            state = lazyListState,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top,
        ) {
            items(newsSubjectList) { item ->
                Column(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(bottom = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = dateToString(item.date, "dd/MM/yyyy"),
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
                    item.itemList.forEach { item2 ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 5.dp, bottom = 5.dp)
                                // https://www.android--code.com/2021/09/jetpack-compose-box-rounded-corners_25.html
                                .clip(RoundedCornerShape(10.dp))
                                //.background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 1.0f))
                                .clickable {
                                    itemClicked(item2)
                                },
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 1.0f)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(
                                    start = 15.dp,
                                    end = 15.dp,
                                    top = 10.dp,
                                    bottom = 10.dp
                                )
                            ) {
                                // https://stackoverflow.com/questions/2891361/how-to-set-time-zone-of-a-java-util-date
                                Text(
                                    text = item2.title ?: "",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Spacer(modifier = Modifier.size(15.dp))
                                Text(
                                    text = item2.contentString ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    // https://stackoverflow.com/a/65736376
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.size(10.dp))
            }
        }
    }
}