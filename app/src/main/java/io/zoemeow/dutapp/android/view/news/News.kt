package io.zoemeow.dutapp.android.view.news

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import io.zoemeow.dutapp.android.ui.thirdparty.pagerTabIndicatorOffset
import io.zoemeow.dutapp.android.viewmodel.NewsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun News(newsViewModel: NewsViewModel) {
    val tabTitles = listOf(
        "News Global",
        "News Subject"
    )
    val pagerState = rememberPagerState(initialPage = 0)
    val scope = rememberCoroutineScope()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        TabRow(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 5.dp),
            selectedTabIndex = pagerState.currentPage,
            indicator = { tabPositions ->
                /**
                 * This is a temporary fix for require material instead of material3.
                 * Waiting for a release fix for this library.
                 *
                 * https://github.com/google/accompanist/issues/1076
                 */
                TabRowDefaults.Indicator(
                    Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
                )
            }
        ) {
            tabTitles.forEachIndexed { index, text ->
                val selected = pagerState.currentPage == index
                Tab(
                    selected = selected,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    text = {
                        Text(
                            text = text,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                )
            }
        }

        HorizontalPager(count = tabTitles.size, state = pagerState) { index ->
            when (index) {
                0 -> NewsGlobal(
                    newsGlobalList = newsViewModel.newsGlobalList,
                    isLoading = newsViewModel.newsGlobalState,
                    reloadRequested = {
                        newsViewModel.getNewsGlobal(it)
                    },
                    itemClicked = {
                        newsViewModel.openNewsDetailsGlobalActivity(it)
                    }
                )
                1 -> NewsSubject(
                    newsSubjectList = newsViewModel.newsSubjectList,
                    isLoading = newsViewModel.newsSubjectState,
                    reloadRequested = {
                        newsViewModel.getNewsSubject(it)
                    },
                    itemClicked = {
                        newsViewModel.openNewsDetailsSubjectActivity(it)
                    }
                )
            }
        }
    }
}