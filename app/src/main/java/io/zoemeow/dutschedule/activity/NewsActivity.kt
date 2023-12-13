package io.zoemeow.dutschedule.activity

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import io.dutwrapper.dutwrapper.model.enums.NewsSearchType
import io.dutwrapper.dutwrapper.model.enums.NewsType
import io.dutwrapper.dutwrapper.model.news.NewsGlobalItem
import io.zoemeow.dutschedule.R
import io.zoemeow.dutschedule.model.ProcessState
import io.zoemeow.dutschedule.model.news.NewsFetchType
import io.zoemeow.dutschedule.model.news.NewsGroupByDate
import io.zoemeow.dutschedule.ui.component.base.ButtonBase
import io.zoemeow.dutschedule.ui.component.news.NewsListItem
import io.zoemeow.dutschedule.ui.component.news.NewsListPage
import io.zoemeow.dutschedule.ui.component.news.NewsListPage_EndOfListHandler
import io.zoemeow.dutschedule.viewmodel.NewsSearchViewModel
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
    override fun OnMainView(
        context: Context,
        snackBarHostState: SnackbarHostState,
        containerColor: Color,
        contentColor: Color
    ) {
        when (intent.action) {
            "activity_search" -> {
                SearchView(
                    context = context,
                    snackBarHostState = snackBarHostState,
                    containerColor = containerColor,
                    contentColor = contentColor,
                )
            }

            else -> {
                MainView(
                    context = context,
                    snackBarHostState = snackBarHostState,
                    containerColor = containerColor,
                    contentColor = contentColor,
                    searchRequested = {
                        val intent = Intent(context, NewsActivity::class.java)
                        intent.action = "activity_search"
                        context.startActivity(intent)
                    }
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    @Composable
    private fun MainView(
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
                                        getMainViewModel().newsGlobal2.refreshData(
                                            force = true,
                                            args = mapOf("newsfetchtype" to NewsFetchType.ClearAndFirstPage.value.toString())
                                        )
                                    }

                                    1 -> {
                                        getMainViewModel().newsSubject2.refreshData(
                                            force = true,
                                            args = mapOf("newsfetchtype" to NewsFetchType.ClearAndFirstPage.value.toString())
                                        )
                                    }

                                    else -> {}
                                }
                            },
                            enabled = when (pagerState.currentPage) {
                                0 -> {
                                    getMainViewModel().newsGlobal2.processState.value != ProcessState.Running
                                }

                                1 -> {
                                    getMainViewModel().newsSubject2.processState.value != ProcessState.Running
                                }

                                else -> false
                            },
                            content = {
                                when {
                                    (pagerState.currentPage == 0 && getMainViewModel().newsGlobal2.processState.value == ProcessState.Running) || (pagerState.currentPage == 1 && getMainViewModel().newsSubject2.processState.value == ProcessState.Running) -> {
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
            content = { padding ->
                HorizontalPager(
                    modifier = Modifier.padding(padding),
                    state = pagerState
                ) { pageIndex ->
                    when (pageIndex) {
                        0 -> {
                            NewsListPage(
                                newsList = (getMainViewModel().newsGlobal2.data.value?.newsListByDate ?: arrayListOf()),
                                processState = getMainViewModel().newsGlobal2.processState.value,
                                opacity = getControlBackgroundAlpha(),
                                itemClicked = { newsItem ->
                                    context.startActivity(
                                        Intent(
                                            context,
                                            NewsDetailActivity::class.java
                                        ).also {
                                            it.action = "news_global"
                                            it.putExtra("data", Gson().toJson(newsItem))
                                        })
                                },
                                endOfListReached = {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        withContext(Dispatchers.IO) {
                                            getMainViewModel().newsGlobal2.refreshData(
                                                force = true,
                                                args = mapOf("newsfetchtype" to NewsFetchType.NextPage.value.toString())
                                            )
                                        }
                                    }
                                }
                            )
                        }

                        1 -> {
                            @Suppress("UNCHECKED_CAST")
                            NewsListPage(
                                newsList = (getMainViewModel().newsSubject2.data.value?.newsListByDate ?: arrayListOf()) as ArrayList<NewsGroupByDate<NewsGlobalItem>>,
                                processState = getMainViewModel().newsSubject2.processState.value,
                                opacity = getControlBackgroundAlpha(),
                                itemClicked = { newsItem ->
                                    context.startActivity(
                                        Intent(
                                            context,
                                            NewsDetailActivity::class.java
                                        ).also {
                                            it.action = "news_subject"
                                            it.putExtra("data", Gson().toJson(newsItem))
                                        })
                                },
                                endOfListReached = {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        withContext(Dispatchers.IO) {
                                            getMainViewModel().newsSubject2.refreshData(
                                                force = true,
                                                args = mapOf("newsfetchtype" to NewsFetchType.NextPage.value.toString())
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun SearchView(
        context: Context,
        snackBarHostState: SnackbarHostState,
        containerColor: Color,
        contentColor: Color,
    ) {
        val newsSearchViewModel: NewsSearchViewModel = viewModel()

        val lazyListState = rememberLazyListState()

        val focusRequester = remember { FocusRequester() }

        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
            containerColor = containerColor,
            contentColor = contentColor,
            topBar = {
                TopAppBar(
                    title = {
                        OutlinedTextField(
                            value = newsSearchViewModel.query.value,
                            onValueChange = { newsSearchViewModel.query.value = it },
                            modifier = Modifier.focusRequester(focusRequester),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    clearAllFocusAndHideKeyboard()
                                    newsSearchViewModel.invokeSearch(startOver = true)
                                }
                            )
                        )
                    },
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
                        Surface(
                            modifier = Modifier
                                .padding(start = 3.dp),
                            color = when (newsSearchViewModel.method.value == NewsSearchType.ByContent) {
                                true -> MaterialTheme.colorScheme.secondaryContainer
                                false -> Color.Transparent
                            },
                            shape = RoundedCornerShape(7.dp),
                            content = {
                                IconButton(
                                    onClick = {
                                        // TODO: Hide virtual keyboard
                                        clearAllFocusAndHideKeyboard()
                                        newsSearchViewModel.searchMethodOptionVisible.value = true
                                    },
                                    content = {
                                        Icon(
                                            ImageVector.vectorResource(R.drawable.ic_baseline_manage_search_24),
                                            "Search type"
                                        )
                                    }
                                )
                                DropdownMenu(
                                    modifier = Modifier.background(color = MaterialTheme.colorScheme.surface),
                                    expanded = newsSearchViewModel.searchMethodOptionVisible.value,
                                    onDismissRequest = { newsSearchViewModel.searchMethodOptionVisible.value = false },
                                    content = {
                                        listOf(
                                            NewsSearchType.ByTitle,
                                            NewsSearchType.ByContent
                                        ).forEach {
                                            DropdownMenuItem(
                                                modifier = Modifier.background(
                                                    color = if (newsSearchViewModel.method.value == it) MaterialTheme.colorScheme.secondaryContainer
                                                    else MaterialTheme.colorScheme.surface
                                                ),
                                                text = {
                                                    Text(
                                                        when (it) {
                                                            NewsSearchType.ByTitle -> "By title"
                                                            NewsSearchType.ByContent -> "By content"
                                                            else -> "(unknown)"
                                                        }
                                                    )
                                                },
                                                onClick = {
                                                    newsSearchViewModel.method.value = it
                                                    newsSearchViewModel.searchMethodOptionVisible.value = false
                                                }
                                            )
                                        }
                                    }
                                )
                            }
                        )
                        Surface(
                            modifier = Modifier
                                .padding(start = 3.dp),
                            color = if (newsSearchViewModel.type.value == NewsType.Subject) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                            shape = RoundedCornerShape(7.dp),
                            content = {
                                IconButton(
                                    onClick = {
                                        clearAllFocusAndHideKeyboard()
                                        newsSearchViewModel.newsSearchType.value = true
                                    },
                                    content = {
                                        Icon(
                                            ImageVector.vectorResource(R.drawable.ic_baseline_news_mode_24),
                                            "News type"
                                        )
                                    }
                                )
                                DropdownMenu(
                                    modifier = Modifier.background(color = MaterialTheme.colorScheme.surface),
                                    expanded = newsSearchViewModel.newsSearchType.value,
                                    onDismissRequest = { newsSearchViewModel.newsSearchType.value = false },
                                    content = {
                                        listOf(
                                            NewsType.Global,
                                            NewsType.Subject
                                        ).forEach {
                                            DropdownMenuItem(
                                                modifier = Modifier.background(
                                                    color = if (newsSearchViewModel.type.value == it) MaterialTheme.colorScheme.secondaryContainer
                                                    else MaterialTheme.colorScheme.surface
                                                ),
                                                text = {
                                                    Text(
                                                        when (it) {
                                                            NewsType.Subject -> "News subject"
                                                            NewsType.Global -> "News global"
                                                        }
                                                    )
                                                },
                                                onClick = {
                                                    newsSearchViewModel.type.value = it
                                                    newsSearchViewModel.newsSearchType.value = false
                                                }
                                            )
                                        }
                                    }
                                )
                            }
                        )
                        IconButton(
                            modifier = Modifier.padding(start = 3.dp),
                            onClick = {
                                clearAllFocusAndHideKeyboard()
                                newsSearchViewModel.invokeSearch(startOver = true)
                            },
                            enabled = newsSearchViewModel.progress.value != ProcessState.Running,
                            content = {
                                if (newsSearchViewModel.progress.value == ProcessState.Running) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 3.dp
                                    )
                                } else {
                                    Icon(Icons.Default.Search, "Search/Refresh search")
                                }
                            }
                        )
                    }
                )
            },
            content = { padding ->
                NewsListPage_EndOfListHandler(
                    listState = lazyListState,
                    onLoadMore = {
                        newsSearchViewModel.invokeSearch()
                    }
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 10.dp)
                        .clickable {
                            clearAllFocusAndHideKeyboard()
                        },
                    verticalArrangement = if (newsSearchViewModel.newsList.isNotEmpty()) Arrangement.Top else Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    state = lazyListState,
                    content = {
                        when {
                            (newsSearchViewModel.newsList.isNotEmpty()) -> {
                                items(newsSearchViewModel.newsList) { item ->
                                    NewsListItem(
                                        title = item.title,
                                        description = item.contentString,
                                        dateTime = item.date,
                                        opacity = getControlBackgroundAlpha(),
                                        onClick = {
                                            clearAllFocusAndHideKeyboard()
                                            context.startActivity(
                                                Intent(
                                                    context,
                                                    NewsDetailActivity::class.java
                                                ).also {
                                                    it.action =
                                                        if (newsSearchViewModel.type.value == NewsType.Subject) "news_subject" else "news_global"
                                                    it.putExtra("data", Gson().toJson(item))
                                                })
                                        }
                                    )
                                    Spacer(modifier = Modifier.size(3.dp))
                                }
                            }

                            (newsSearchViewModel.progress.value == ProcessState.Running) -> {
                                item {
                                    CircularProgressIndicator()
                                }
                            }

                            (newsSearchViewModel.progress.value == ProcessState.NotRunYet) -> {
                                item {
                                    Text(
                                        "Tap search on top to get started.",
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            (newsSearchViewModel.progress.value != ProcessState.Running && newsSearchViewModel.newsList.isEmpty()) -> {
                                item {
                                    Text(
                                        "No available news matches your search. Try again with new query.",
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                )
            }
        )
    }
}
