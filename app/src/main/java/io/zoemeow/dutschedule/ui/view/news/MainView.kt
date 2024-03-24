package io.zoemeow.dutschedule.ui.view.news

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import io.dutwrapper.dutwrapper.model.news.NewsGlobalItem
import io.zoemeow.dutschedule.R
import io.zoemeow.dutschedule.activity.NewsActivity
import io.zoemeow.dutschedule.model.ProcessState
import io.zoemeow.dutschedule.model.news.NewsFetchType
import io.zoemeow.dutschedule.ui.component.base.ButtonBase
import io.zoemeow.dutschedule.ui.component.news.NewsListPage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NewsActivity.MainView(
    context: Context,
    snackBarHostState: SnackbarHostState,
    containerColor: Color,
    contentColor: Color,
    searchRequested: (() -> Unit)? = null
) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        containerColor = containerColor,
        contentColor = contentColor,
        topBar = {
            TopAppBar(
                title = { Text(text = "News") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(
                        onClick = {
                            setResult(ComponentActivity.RESULT_OK)
                            finish()
                        },
                        content = {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                "",
                                modifier = Modifier.size(25.dp)
                            )
                        }
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            searchRequested?.let { it() }
                        },
                        content = {
                            Icon(Icons.Default.Search, "Search")
                        }
                    )
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = BottomAppBarDefaults.containerColor.copy(
                    alpha = getControlBackgroundAlpha()
                ),
                actions = {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        content = {
                            ButtonBase(
                                clicked = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(0)
                                    }
                                },
                                isOutlinedButton = pagerState.currentPage != 0,
                                content = {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_baseline_newspaper_24),
                                        "News global",
                                        modifier = Modifier
                                            .size(30.dp)
                                            .padding(end = 7.dp),
                                    )
                                    Text("News global")
                                }
                            )
                            ButtonBase(
                                modifier = Modifier.padding(start = 12.dp),
                                isOutlinedButton = pagerState.currentPage != 1,
                                clicked = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(1)
                                    }
                                },
                                content = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_baseline_newspaper_24),
                                        "News subject",
                                        modifier = Modifier
                                            .size(30.dp)
                                            .padding(end = 7.dp),
                                    )
                                    Text("News subject")
                                }
                            )
                        }
                    )
                }
            )
        },
        floatingActionButton = {
            if (when (pagerState.currentPage) {
                    0 -> {
                        getMainViewModel().newsInstance.newsGlobal.processState.value != ProcessState.Running
                    }

                    1 -> {
                        getMainViewModel().newsInstance.newsSubject.processState.value != ProcessState.Running
                    }

                    else -> false
                }
            ) {
                FloatingActionButton(
                    onClick = {
                        when (pagerState.currentPage) {
                            0 -> {
                                getMainViewModel().newsInstance.fetchGlobalNews(
                                    fetchType = NewsFetchType.ClearAndFirstPage,
                                    forceRequest = true
                                )
                            }

                            1 -> {
                                getMainViewModel().newsInstance.fetchSubjectNews(
                                    fetchType = NewsFetchType.ClearAndFirstPage,
                                    forceRequest = true
                                )
                            }

                            else -> {}
                        }
                    },
                    content = {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                )
            }
        },
        content = { padding ->
            HorizontalPager(
                modifier = Modifier.padding(padding),
                state = pagerState
            ) { pageIndex ->
                when (pageIndex) {
                    0 -> {
                        NewsListPage(
                            newsList = getMainViewModel().newsInstance.newsGlobal.data.toList(),
                            processState = getMainViewModel().newsInstance.newsGlobal.processState.value,
                            opacity = getControlBackgroundAlpha(),
                            itemClicked = { newsItem ->
                                context.startActivity(
                                    Intent(
                                        context,
                                        NewsActivity::class.java
                                    ).also {
                                        it.action = "activity_detail"
                                        it.putExtra("type", "news_global")
                                        it.putExtra("data", Gson().toJson(newsItem))
                                    })
                            },
                            endOfListReached = {
                                CoroutineScope(Dispatchers.Main).launch {
                                    withContext(Dispatchers.IO) {
                                        getMainViewModel().newsInstance.fetchGlobalNews(
                                            fetchType = NewsFetchType.NextPage,
                                            forceRequest = true
                                        )
                                    }
                                }
                            }
                        )
                    }

                    1 -> {
                        @Suppress("UNCHECKED_CAST")
                        (NewsListPage(
                            newsList = getMainViewModel().newsInstance.newsSubject.data.toList() as List<NewsGlobalItem>,
                            processState = getMainViewModel().newsInstance.newsSubject.processState.value,
                            opacity = getControlBackgroundAlpha(),
                            itemClicked = { newsItem ->
                                context.startActivity(
                                    Intent(
                                        context,
                                        NewsActivity::class.java
                                    ).also {
                                        it.action = "activity_detail"
                                        it.putExtra("type", "news_subject")
                                        it.putExtra("data", Gson().toJson(newsItem))
                                    })
                            },
                            endOfListReached = {
                                CoroutineScope(Dispatchers.Main).launch {
                                    withContext(Dispatchers.IO) {
                                        getMainViewModel().newsInstance.fetchSubjectNews(
                                            fetchType = NewsFetchType.NextPage,
                                            forceRequest = true
                                        )
                                    }
                                }
                            }
                        ))
                    }
                }
            }
        }
    )
}