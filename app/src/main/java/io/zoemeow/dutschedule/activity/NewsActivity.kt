package io.zoemeow.dutschedule.activity

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import io.dutwrapperlib.dutwrapper.objects.news.NewsGlobalItem
import io.zoemeow.dutschedule.R
import io.zoemeow.dutschedule.model.ProcessState
import io.zoemeow.dutschedule.model.news.NewsGroupByDate
import io.zoemeow.dutschedule.ui.component.base.ButtonBase
import io.zoemeow.dutschedule.ui.component.news.NewsListPage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class NewsActivity : BaseActivity() {
    @Composable
    override fun OnPreloadOnce() {

    }

    @Composable
    override fun OnMainView(padding: PaddingValues) {
        MainView()
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    @Composable
    private fun MainView() {
        val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
        val scope = rememberCoroutineScope()
        val context = LocalContext.current

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(text = "News") },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                setResult(RESULT_OK)
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
                                when (pagerState.currentPage) {
                                    0 -> {
                                        getMainViewModel().fetchNewsGlobal(
                                            newsPageType = 3
                                        )
                                    }
                                    1 -> {
                                        getMainViewModel().fetchNewsSubject(
                                            newsPageType = 3
                                        )
                                    }
                                    else -> {}
                                }
                            },
                            enabled = when (pagerState.currentPage) {
                                0 -> {
                                    getMainViewModel().newsGlobal.value.processState != ProcessState.Running
                                }
                                1 -> {
                                    getMainViewModel().newsSubject.value.processState != ProcessState.Running
                                }
                                else -> false
                            },
                            content = {
                                when {
                                    (pagerState.currentPage == 0 && getMainViewModel().newsGlobal.value.processState == ProcessState.Running) || (pagerState.currentPage == 1 && getMainViewModel().newsSubject.value.processState == ProcessState.Running) -> {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(25.dp),
                                            strokeWidth = 3.dp
                                        )
                                    }
                                    else -> {
                                        Icon(Icons.Default.Refresh, "Refresh")
                                    }
                                }
                            }
                        )
                        IconButton(
                            onClick = { },
                            content = {
                                Icon(Icons.Default.Search, "Search")
                            }
                        )
                    }
                )
            },
//            floatingActionButton = {
//                FloatingActionButton(onClick = { /*TODO*/ }) {
//                    Icon(Icons.Default.Refresh, "")
//                }
//            },
            bottomBar = {
                BottomAppBar(
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
                                            painter = painterResource(id = R.drawable.baseline_newspaper_24),
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
                                            painter = painterResource(id = R.drawable.baseline_newspaper_24),
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
            content = { padding ->
                HorizontalPager(
                    modifier = Modifier.padding(padding),
                    state = pagerState
                ) { pageIndex ->
                    when (pageIndex) {
                        0 -> {
                            getMainViewModel().newsGlobal.apply {
                                NewsListPage(
                                    newsList = this.value.data.newsListByDate,
                                    processState = this.value.processState,
                                    itemClicked = { newsItem ->
                                        context.startActivity(Intent(context, NewsDetailActivity::class.java).also {
                                            it.action = "news_global"
                                            it.putExtra("data", Gson().toJson(newsItem))
                                        })
                                    },
                                    endOfListReached = {
                                        CoroutineScope(Dispatchers.Main).launch {
                                            withContext(Dispatchers.IO) {
                                                getMainViewModel().fetchNewsGlobal(
                                                    newsPageType = 0
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                        }
                        1 -> {
                            getMainViewModel().newsSubject.apply {
                                NewsListPage(
                                    newsList = this.value.data.newsListByDate as ArrayList<NewsGroupByDate<NewsGlobalItem>>,
                                    processState = this.value.processState,
                                    itemClicked = { newsItem ->
                                        context.startActivity(Intent(context, NewsDetailActivity::class.java).also {
                                            it.action = "news_subject"
                                            it.putExtra("data", Gson().toJson(newsItem))
                                        })
                                    },
                                    endOfListReached = {
                                        CoroutineScope(Dispatchers.Main).launch {
                                            withContext(Dispatchers.IO) {
                                                getMainViewModel().fetchNewsSubject(
                                                    newsPageType = 0
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        )
    }
}
