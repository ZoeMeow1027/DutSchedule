package io.zoemeow.dutschedule.activity

import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import io.dutwrapper.dutwrapper.model.enums.NewsType
import io.dutwrapper.dutwrapper.model.news.NewsGlobalItem
import io.dutwrapper.dutwrapper.model.news.NewsSubjectItem
import io.zoemeow.dutschedule.R
import io.zoemeow.dutschedule.model.ProcessState
import io.zoemeow.dutschedule.model.news.NewsFetchType
import io.zoemeow.dutschedule.model.news.NewsGroupByDate
import io.zoemeow.dutschedule.ui.component.base.ButtonBase
import io.zoemeow.dutschedule.ui.component.news.NewsDetailScreen
import io.zoemeow.dutschedule.ui.component.news.NewsListPage
import io.zoemeow.dutschedule.ui.component.news.NewsSearchOptionAndHistory
import io.zoemeow.dutschedule.ui.component.news.NewsSearchResult
import io.zoemeow.dutschedule.utils.openLink
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

            "activity_detail" -> {
                NewsDetailView(
                    context = context,
                    snackBarHostState = snackBarHostState,
                    containerColor = containerColor,
                    contentColor = contentColor
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
                            getMainViewModel().newsGlobal2.processState.value != ProcessState.Running
                        }

                        1 -> {
                            getMainViewModel().newsSubject2.processState.value != ProcessState.Running
                        }

                        else -> false
                    }
                ) {
                    FloatingActionButton(
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
                                newsList = (getMainViewModel().newsGlobal2.data.value?.newsListByDate
                                    ?: arrayListOf()),
                                processState = getMainViewModel().newsGlobal2.processState.value,
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
                                newsList = (getMainViewModel().newsSubject2.data.value?.newsListByDate
                                    ?: arrayListOf()) as ArrayList<NewsGroupByDate<NewsGlobalItem>>,
                                processState = getMainViewModel().newsSubject2.processState.value,
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

        val isSearchFocused: MutableTransitionState<Boolean> = remember {
            MutableTransitionState(false).apply {
                targetState = false
            }
        }

        fun dismissFocus() {
            clearAllFocusAndHideKeyboard()
            isSearchFocused.targetState = false
        }

        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
            containerColor = containerColor,
            contentColor = contentColor,
            topBar = {
                TopAppBar(
                    title = {
                        TextField(
                            value = newsSearchViewModel.query.value,
                            onValueChange = { newsSearchViewModel.query.value = it },
                            modifier = Modifier
                                .focusRequester(focusRequester)
                                .onFocusChanged {
                                    if (it.isFocused) {
                                        isSearchFocused.targetState = true
                                    }
                                },
                            placeholder = {
                                Text("Type here to search")
                            },
                            trailingIcon = {
                                if (isSearchFocused.targetState) {
                                    IconButton(
                                        content = {
                                            Icon(Icons.Default.Clear, "")
                                        },
                                        onClick = {
                                            newsSearchViewModel.query.value = ""
                                        }
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    dismissFocus()
                                    newsSearchViewModel.invokeSearch(startOver = true)
                                }
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                if (isSearchFocused.targetState) {
                                    dismissFocus()
                                } else {
                                    setResult(RESULT_OK)
                                    finish()
                                }
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
                        if (isSearchFocused.targetState) {
                            IconButton(
                                modifier = Modifier.padding(start = 3.dp),
                                onClick = {
                                    dismissFocus()
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
                    }
                )
            },
            content = { padding ->
                NewsSearchResult(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 10.dp),
                    newsList = newsSearchViewModel.newsList,
                    lazyListState = lazyListState,
                    opacity = getControlBackgroundAlpha(),
                    processState = newsSearchViewModel.progress.value,
                    onEndOfList = {
                        newsSearchViewModel.invokeSearch()
                    },
                    onItemClicked = { item ->
                        dismissFocus()
                        context.startActivity(
                            Intent(
                                context,
                                NewsActivity::class.java
                            ).also {
                                it.action = "activity_detail"
                                it.putExtra("type", if (newsSearchViewModel.type.value == NewsType.Subject) "news_subject" else "news_global")
                                it.putExtra("data", Gson().toJson(item))
                            })
                    }
                )
                NewsSearchOptionAndHistory(
                    modifier = Modifier
                        .padding(padding)
                        .padding(horizontal = 10.dp)
                        .padding(top = 5.dp),
                    isVisible = isSearchFocused,
                    searchHistory = newsSearchViewModel.searchHistory.toList(),
                    backgroundColor = MaterialTheme.colorScheme.background,
                    query = newsSearchViewModel.query.value,
                    newsMethod = newsSearchViewModel.method.value,
                    newsType = newsSearchViewModel.type.value,
                    onSettingsChanged = { query, method, type ->
                        newsSearchViewModel.let {
                            it.query.value = query
                            it.method.value = method
                            it.type.value = type
                        }
                    },
                    onSearchTriggered = {
                        newsSearchViewModel.invokeSearch(startOver = true)
                    },
                    onClearHistoryTriggered = {
                        newsSearchViewModel.clearHistory()
                    },
                    onDismiss = {
                        dismissFocus()
                    }
                )
            }
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun NewsDetailView(
        context: Context,
        snackBarHostState: SnackbarHostState,
        containerColor: Color,
        contentColor: Color
    ) {
        val newsType = intent.getStringExtra("type")
        val newsData = intent.getStringExtra("data")

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
            containerColor = containerColor,
            contentColor = contentColor,
            topBar = {
                TopAppBar(
                    title = { Text("News detail") },
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
                )
            },
            floatingActionButton = {
                if (newsType == "news_subject") {
                    ExtendedFloatingActionButton(
                        content = {
                            Row {
                                Icon(Icons.Default.Add, "Add to news filter")
                                Spacer(modifier = Modifier.size(3.dp))
                                Text("Add to news filter")
                            }
                        },
                        onClick = {
                            try {
//                                (Gson().fromJson<NewsSubjectItem>(newsData, object : TypeToken<NewsSubjectItem>() {}.type).also {
//                                    getMainViewModel().appSettings.value.newsFilterList.add()
//                                }
                                // TODO: Develop a add news filter function for news subject detail.
                                showSnackBar("This function is in development. Check back soon.")
                            } catch (ex: Exception) {
                                ex.printStackTrace()
                                showSnackBar("We can't add this subject in this news to your filter! You can instead add manually them.")
                            }
                        }
                    )
                }
            },
            content = {
                when (newsType) {
                    "news_global" -> {
                        NewsDetailScreen(
                            padding = it,
                            newsItem = Gson().fromJson(newsData, object : TypeToken<NewsGlobalItem>() {}.type),
                            newsType = NewsType.Global,
                            linkClicked = { link ->
                                openLink(
                                    url = link,
                                    context = this,
                                    customTab = getMainViewModel().appSettings.value.openLinkInsideApp
                                )
                            }
                        )
                    }
                    "news_subject" -> {
                        NewsDetailScreen(
                            padding = it,
                            newsItem = Gson().fromJson(newsData, object : TypeToken<NewsSubjectItem>() {}.type) as NewsGlobalItem,
                            newsType = NewsType.Subject,
                            linkClicked = { link ->
                                openLink(
                                    url = link,
                                    context = this,
                                    customTab = getMainViewModel().appSettings.value.openLinkInsideApp
                                )
                            }
                        )
                    }
                    else -> { }
                }
            }
        )
    }
}
