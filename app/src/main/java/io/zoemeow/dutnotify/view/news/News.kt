package io.zoemeow.dutnotify.view.news

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import io.zoemeow.dutnotify.NewsDetailsActivity
import io.zoemeow.dutnotify.R
import io.zoemeow.dutnotify.model.enums.ServiceCode
import io.zoemeow.dutnotify.service.NewsService
import io.zoemeow.dutnotify.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class)
@Composable
fun News(
    mainViewModel: MainViewModel,
    scope: CoroutineScope,
    lazyListTabGlobal: LazyListState,
    lazyListTabSubject: LazyListState,
) {
    val tabList = listOf(
        stringResource(id = R.string.news_tab_global),
        stringResource(id = R.string.news_tab_subject),
    )
    val pagerState = rememberPagerState(initialPage = 0)
    val context = LocalContext.current

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = if (mainViewModel.mainActivityIsDarkTheme.value) Color.White else Color.Black,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color.Transparent
                ),
                title = { Text(stringResource(id = R.string.topbar_news)) },
                actions = {
                    var count = 0
                    for (tabItem in tabList) {
                        val count2: Int = count
                        Button(
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(count2)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                if (pagerState.currentPage == count2) MaterialTheme.colorScheme.secondaryContainer
                                else MaterialTheme.colorScheme.background
                            ),
                            modifier = Modifier.padding(start = 2.dp, end = 2.dp)
                        ) {
                            Text(
                                text = tabItem,
                                color = if (mainViewModel.mainActivityIsDarkTheme.value) Color.White else Color.Black
                            )
                        }
                        count += 1
                    }
                },

            )
        },
        floatingActionButton = {
//            if (newsViewModel.newsGlobalItemChose.value == null && newsViewModel.newsSubjectItemChose.value == null) {
//                FloatingActionButton(
//                    containerColor = MaterialTheme.colorScheme.primary,
//                    onClick = {
//                        // TODO: Search in news global and news subject here!
//                    },
//                    content = {
//                        Icon(
//                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_baseline_search_24),
//                            contentDescription = "Search",
//                        )
//                    }
//                )
//            }
        },
        content = { padding ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier.padding(padding)
            ) {
                HorizontalPager(count = tabList.size, state = pagerState) { index ->
                    when (index) {
                        0 -> NewsGlobal(
                            newsGlobalList = mainViewModel.News_Data_Global,
                            isLoading = mainViewModel.News_Process_Global.value,
                            lazyListState = lazyListTabGlobal,
                            reloadRequested = {
                                NewsService.startService(
                                    context = context,
                                    intent = Intent(context, NewsService::class.java).apply {
                                        putExtra(ServiceCode.ACTION, ServiceCode.ACTION_NEWS_FETCHGLOBAL)
                                        putExtra(
                                            ServiceCode.ARGUMENT_NEWS_PAGEOPTION,
                                            if (it) ServiceCode.ARGUMENT_NEWS_PAGEOPTION_GETPAGE1
                                            else ServiceCode.ARGUMENT_NEWS_PAGEOPTION_NEXTPAGE
                                        )
                                        putExtra(ServiceCode.ARGUMENT_NEWS_NOTIFYTOUSER, false)
                                    }
                                )
                            },
                            itemClicked = { newsItem ->
                                val intent = Intent(context, NewsDetailsActivity::class.java)
                                intent.putExtra("type", "dut_news_global")
                                intent.putExtra("data", newsItem)
                                context.startActivity(intent)
                            }
                        )
                        1 -> NewsSubject(
                            newsSubjectList = mainViewModel.News_Data_Subject,
                            isLoading = mainViewModel.News_Process_Subject.value,
                            lazyListState = lazyListTabSubject,
                            reloadRequested = {
                                NewsService.startService(
                                    context = context,
                                    intent = Intent(context, NewsService::class.java).apply {
                                        putExtra(ServiceCode.ACTION, ServiceCode.ACTION_NEWS_FETCHSUBJECT)
                                        putExtra(
                                            ServiceCode.ARGUMENT_NEWS_PAGEOPTION,
                                            if (it) ServiceCode.ARGUMENT_NEWS_PAGEOPTION_GETPAGE1
                                            else ServiceCode.ARGUMENT_NEWS_PAGEOPTION_NEXTPAGE
                                        )
                                        putExtra(ServiceCode.ARGUMENT_NEWS_NOTIFYTOUSER, false)
                                    }
                                )
                            },
                            itemClicked = { newsItem ->
                                val intent = Intent(context, NewsDetailsActivity::class.java)
                                intent.putExtra("type", "dut_news_subject")
                                intent.putExtra("data", newsItem)
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
    )
}