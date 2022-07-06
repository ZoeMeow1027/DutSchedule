package io.zoemeow.dutapp.android.view.news

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import io.zoemeow.dutapp.android.ui.thirdparty.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import io.zoemeow.dutapp.android.R
import io.zoemeow.dutapp.android.model.ProcessState
import io.zoemeow.dutapp.android.viewmodel.NewsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun News() {
    val newsViewModel = viewModel<NewsViewModel>()

    val tabTitles = listOf(
        "News Global",
        "News Subject"
    )
    val pagerState = rememberPagerState(initialPage = 0)
    val scope = rememberCoroutineScope()

    val isProcessGlobal = remember { mutableStateOf(false) }
    val isProcessSubject = remember { mutableStateOf(false) }

    LaunchedEffect(
        newsViewModel.newsGlobalState.value,
        newsViewModel.newsSubjectState.value
    ) {
        isProcessGlobal.value = (
                try { newsViewModel.newsGlobalState.value == ProcessState.Running }
                catch (_: Exception) { false }
                )

        isProcessSubject.value = (
                try { newsViewModel.newsSubjectState.value == ProcessState.Running }
                catch (_: Exception) { false }
                )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { },
        floatingActionButton = {
            FloatingActionButton(
                containerColor = MaterialTheme.colorScheme.primary,
                onClick = {
                    // TODO: Search in news global and news subject here!
                },
                content = {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_baseline_search_24),
                        contentDescription = "Search",
                    )
                }
            )
        }
    ) { paddingValue ->
        Column(modifier = Modifier.padding(paddingValue)) {
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
                        isLoading = isProcessGlobal.value,
                        reloadRequested = { newsViewModel.getNewsGlobal(it) }
                    )
                    1 -> NewsSubject(
                        newsSubjectList = newsViewModel.newsSubjectList,
                        isLoading = isProcessSubject.value,
                        reloadRequested = { newsViewModel.getNewsSubject(it) }
                    )
                }
            }
        }
    }
}