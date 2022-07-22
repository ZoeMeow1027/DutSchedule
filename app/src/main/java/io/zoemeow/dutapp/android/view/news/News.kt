package io.zoemeow.dutapp.android.view.news

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import io.zoemeow.dutapp.android.R
import io.zoemeow.dutapp.android.ui.custom.BackgroundImage
import io.zoemeow.dutapp.android.utils.openLinkInCustomTab
import io.zoemeow.dutapp.android.viewmodel.GlobalViewModel
import io.zoemeow.dutapp.android.viewmodel.NewsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class)
@Composable
fun News(
    newsViewModel: NewsViewModel,
) {
    val tabList = listOf("Global", "Subject")
    val pagerState = rememberPagerState(initialPage = 0)
    val context = LocalContext.current

    newsViewModel.lazyListNewsGlobalState = rememberLazyListState()
    newsViewModel.lazyListNewsSubjectState = rememberLazyListState()
    newsViewModel.scope = rememberCoroutineScope()

    BackHandler(
        enabled = newsViewModel.newsGlobalItemChose.value != null || newsViewModel.newsSubjectItemChose.value != null,
        onBack = {
            newsViewModel.newsGlobalItemChose.value = null
            newsViewModel.newsSubjectItemChose.value = null
        }
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            SmallTopAppBar(
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color.Transparent
                ),
                title = { Text("News") },
                actions = {
                    if (newsViewModel.newsGlobalItemChose.value == null && newsViewModel.newsSubjectItemChose.value == null) {
                        var count = 0
                        for (tabItem in tabList) {
                            val count2: Int = count
                            Button(
                                onClick = {
                                    newsViewModel.scope.launch { pagerState.animateScrollToPage(count2) }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    if (pagerState.currentPage == count2) MaterialTheme.colorScheme.secondaryContainer
                                    else MaterialTheme.colorScheme.background
                                ),
                                modifier = Modifier.padding(start = 3.dp, end = 3.dp)
                            ) {
                                Text(
                                    text = tabItem,
                                    color = (
                                            if (isSystemInDarkTheme()) Color.White
                                            else Color.Black
                                            )
                                )
                            }
                            count += 1
                        }
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
            if (newsViewModel.newsGlobalItemChose.value != null) {
                NewsDetailsGlobal(
                    padding = padding,
                    news = newsViewModel.newsGlobalItemChose.value!!,
                    linkClicked = {
                        openLinkInCustomTab(context, it)
                    }
                )
            }
            else if (newsViewModel.newsSubjectItemChose.value != null) {
                NewsDetailsSubject(
                    padding = padding,
                    news = newsViewModel.newsSubjectItemChose.value!!,
                    linkClicked = {
                        openLinkInCustomTab(context, it)
                    }
                )
            }
            else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top,
                    modifier = Modifier.padding(padding)
                ) {
                    HorizontalPager(count = tabList.size, state = pagerState) { index ->
                        when (index) {
                            0 -> NewsGlobal(
                                newsGlobalList = newsViewModel.newsGlobalList,
                                isLoading = newsViewModel.newsGlobalState,
                                lazyListState = newsViewModel.lazyListNewsGlobalState,
                                reloadRequested = {
                                    newsViewModel.getNewsGlobal(it)
                                },
                                itemClicked = {
                                    // newsViewModel.openNewsDetailsGlobalActivity(it)
                                    newsViewModel.newsGlobalItemChose.value = it
                                }
                            )
                            1 -> NewsSubject(
                                newsSubjectList = newsViewModel.newsSubjectList,
                                isLoading = newsViewModel.newsSubjectState,
                                lazyListState = newsViewModel.lazyListNewsSubjectState,
                                reloadRequested = {
                                    newsViewModel.getNewsSubject(it)
                                },
                                itemClicked = {
//                                    newsViewModel.openNewsDetailsSubjectActivity(it)
                                    newsViewModel.newsSubjectItemChose.value = it
                                }
                            )
                        }
                    }
                }
            }
        }
    )
}