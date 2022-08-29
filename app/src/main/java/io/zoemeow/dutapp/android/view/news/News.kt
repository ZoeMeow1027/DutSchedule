package io.zoemeow.dutapp.android.view.news

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import io.zoemeow.dutapp.android.model.enums.NewsPageType
import io.zoemeow.dutapp.android.utils.openLink
import io.zoemeow.dutapp.android.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class)
@Composable
fun News(
    mainViewModel: MainViewModel,
) {
    val tabList = listOf(
        stringResource(id = R.string.news_tab_global),
        stringResource(id = R.string.news_tab_subject)
    )
    val pagerState = rememberPagerState(initialPage = 0)
    val context = LocalContext.current

    mainViewModel.uiStatus.newsLazyListGlobalState = rememberLazyListState()
    mainViewModel.uiStatus.newsLazyListSubjectState = rememberLazyListState()

    BackHandler(
        enabled = mainViewModel.uiStatus.newsDetectItemChosen(needClear = false),
        onBack = { mainViewModel.uiStatus.newsDetectItemChosen(needClear = true) }
    )

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = if (mainViewModel.uiStatus.mainActivityIsDarkTheme.value) Color.White else Color.Black,
        topBar = {
            SmallTopAppBar(
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color.Transparent
                ),
                navigationIcon = {
                    if (mainViewModel.uiStatus.newsDetectItemChosen(needClear = false)) {
                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .height(48.dp)
                                .clickable { mainViewModel.uiStatus.newsDetectItemChosen(needClear = true) },
                            content = {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_baseline_arrow_back_24),
                                    contentDescription = "",
                                    tint = if (mainViewModel.uiStatus.mainActivityIsDarkTheme.value) Color.White else Color.Black,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        )
                    }
                },
                title = { Text(stringResource(id = R.string.navbar_news)) },
                actions = {
                    if (!mainViewModel.uiStatus.newsDetectItemChosen(needClear = false)) {
                        var count = 0
                        for (tabItem in tabList) {
                            val count2: Int = count
                            Button(
                                onClick = {
                                    mainViewModel.uiStatus.scope.launch {
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
                                    color = if (mainViewModel.uiStatus.mainActivityIsDarkTheme.value) Color.White else Color.Black
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
            if (mainViewModel.uiStatus.newsItemChosenGlobal.value != null) {
                NewsDetailsGlobal(
                    mainViewModel = mainViewModel,
                    padding = padding,
                    news = mainViewModel.uiStatus.newsItemChosenGlobal.value!!,
                    linkClicked = {
                        openLink(it, context, mainViewModel.settings.value.openLinkInCustomTab)
                    }
                )
            } else if (mainViewModel.uiStatus.newsItemChosenSubject.value != null) {
                NewsDetailsSubject(
                    mainViewModel = mainViewModel,
                    padding = padding,
                    news = mainViewModel.uiStatus.newsItemChosenSubject.value!!,
                    linkClicked = {
                        openLink(it, context, mainViewModel.settings.value.openLinkInCustomTab)
                    }
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top,
                    modifier = Modifier.padding(padding)
                ) {
                    HorizontalPager(count = tabList.size, state = pagerState) { index ->
                        when (index) {
                            0 -> NewsGlobal(
                                newsGlobalList = mainViewModel.uiStatus.listNewsGlobalByDate,
                                isLoading = mainViewModel.uiStatus.procNewsGlobal.value,
                                lazyListState = mainViewModel.uiStatus.newsLazyListGlobalState,
                                reloadRequested = {
                                    mainViewModel.fetchNewsGlobal(
                                        if (it) NewsPageType.GetFirstPage else NewsPageType.NextPage
                                    )
                                },
                                itemClicked = {
                                    mainViewModel.uiStatus.newsItemChosenGlobal.value = it
                                }
                            )
                            1 -> NewsSubject(
                                newsSubjectList = mainViewModel.uiStatus.listNewsSubjectByDate,
                                isLoading = mainViewModel.uiStatus.procNewsSubject.value,
                                lazyListState = mainViewModel.uiStatus.newsLazyListSubjectState,
                                reloadRequested = {
                                    mainViewModel.fetchNewsSubject(
                                        if (it) NewsPageType.GetFirstPage else NewsPageType.NextPage
                                    )
                                },
                                itemClicked = {
                                    mainViewModel.uiStatus.newsItemChosenSubject.value = it
                                }
                            )
                        }
                    }
                }
            }
        }
    )
}