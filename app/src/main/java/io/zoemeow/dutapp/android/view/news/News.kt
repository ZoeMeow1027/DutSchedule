package io.zoemeow.dutapp.android.view.news

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import io.zoemeow.dutapp.android.R
import io.zoemeow.dutapp.android.utils.openLink
import io.zoemeow.dutapp.android.viewmodel.GlobalViewModel
import io.zoemeow.dutapp.android.viewmodel.NewsViewModel
import io.zoemeow.dutapp.android.viewmodel.UIStatus
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class)
@Composable
fun News(
    newsViewModel: NewsViewModel,
    uiStatus: UIStatus
) {
    val globalViewModel = GlobalViewModel.getInstance()

    val tabList = listOf(
        stringResource(id = R.string.news_tab_global),
        stringResource(id = R.string.news_tab_subject)
    )
    val pagerState = rememberPagerState(initialPage = 0)
    val context = LocalContext.current

    uiStatus.newsLazyListGlobalState = rememberLazyListState()
    uiStatus.newsLazyListSubjectState = rememberLazyListState()

    BackHandler(
        enabled = uiStatus.newsDetectItemChosen(needClear = false),
        onBack = { uiStatus.newsDetectItemChosen(needClear = true) }
    )

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = if (uiStatus.mainActivityIsDarkTheme.value) Color.White else Color.Black,
        topBar = {
            SmallTopAppBar(
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color.Transparent
                ),
                navigationIcon = {
                    if (uiStatus.newsDetectItemChosen(needClear = false)) {
                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .height(48.dp)
                                .clickable { uiStatus.newsDetectItemChosen(needClear = true) },
                            content = {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_baseline_arrow_back_24),
                                    contentDescription = "",
                                    tint = if (uiStatus.mainActivityIsDarkTheme.value) Color.White else Color.Black,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        )
                    }
                },
                title = { Text(stringResource(id = R.string.navbar_news)) },
                actions = {
                    if (!uiStatus.newsDetectItemChosen(needClear = false)) {
                        var count = 0
                        for (tabItem in tabList) {
                            val count2: Int = count
                            Button(
                                onClick = { uiStatus.scope.launch { pagerState.animateScrollToPage(count2) } },
                                colors = ButtonDefaults.buttonColors(
                                    if (pagerState.currentPage == count2) MaterialTheme.colorScheme.secondaryContainer
                                    else MaterialTheme.colorScheme.background
                                ),
                                modifier = Modifier.padding(start = 2.dp, end = 2.dp)
                            ) {
                                Text(
                                    text = tabItem,
                                    color = if (uiStatus.mainActivityIsDarkTheme.value) Color.White else Color.Black
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
            if (uiStatus.newsItemChosenGlobal.value != null) {
                NewsDetailsGlobal(
                    padding = padding,
                    news = uiStatus.newsItemChosenGlobal.value!!,
                    uiStatus = uiStatus,
                    linkClicked = {
                        openLink(it, context, globalViewModel.openLinkType.value)
                    }
                )
            }
            else if (uiStatus.newsItemChosenSubject.value != null) {
                NewsDetailsSubject(
                    padding = padding,
                    news = uiStatus.newsItemChosenSubject.value!!,
                    uiStatus = uiStatus,
                    linkClicked = {
                        openLink(it, context, globalViewModel.openLinkType.value)
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
                                newsGlobalList = newsViewModel.newsGlobalListByDate,
                                isLoading = newsViewModel.newsGlobalState,
                                lazyListState = uiStatus.newsLazyListGlobalState,
                                reloadRequested = {
                                    newsViewModel.getNewsGlobal(it)
                                },
                                itemClicked = {
                                    uiStatus.newsItemChosenGlobal.value = it
                                }
                            )
                            1 -> NewsSubject(
                                newsSubjectList = newsViewModel.newsSubjectListByDate,
                                isLoading = newsViewModel.newsSubjectState,
                                lazyListState = uiStatus.newsLazyListSubjectState,
                                reloadRequested = {
                                    newsViewModel.getNewsSubject(it)
                                },
                                itemClicked = {
                                    uiStatus.newsItemChosenSubject.value = it
                                }
                            )
                        }
                    }
                }
            }
        }
    )
}