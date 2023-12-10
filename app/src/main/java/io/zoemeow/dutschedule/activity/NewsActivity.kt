package io.zoemeow.dutschedule.activity

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import io.dutwrapper.dutwrapper.model.enums.NewsSearchType
import io.dutwrapper.dutwrapper.model.news.NewsGlobalItem
import io.zoemeow.dutschedule.R
import io.zoemeow.dutschedule.model.ProcessState
import io.zoemeow.dutschedule.model.news.NewsFetchType
import io.zoemeow.dutschedule.model.news.NewsGroupByDate
import io.zoemeow.dutschedule.repository.DutNewsRepository
import io.zoemeow.dutschedule.ui.component.base.ButtonBase
import io.zoemeow.dutschedule.ui.component.news.NewsListItem
import io.zoemeow.dutschedule.ui.component.news.NewsListPage
import io.zoemeow.dutschedule.ui.component.news.NewsListPage_EndOfListHandler
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
        val context = LocalContext.current
        when (intent.action) {
            "activity_search" -> {
                SearchView()
            }

            else -> {
                MainView(
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
        searchRequested: (() -> Unit)? = null
    ) {
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
                            NewsListPage(
                                newsList = (getMainViewModel().newsGlobal2.data.value?.newsListByDate ?: arrayListOf()),
                                processState = getMainViewModel().newsGlobal2.processState.value,
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
    private fun SearchView() {
        val context = LocalContext.current

        val searchQuery = remember { mutableStateOf("") }
        val searchType = remember { mutableStateOf(NewsSearchType.ByTitle) }
        // False: Global - True: Subject
        val newsType = remember { mutableStateOf(false) }
        val newsList = remember { mutableStateListOf<NewsGlobalItem>() }
        val newsPage = remember { mutableIntStateOf(1) }
        val newsProgress = remember { mutableStateOf(ProcessState.NotRunYet) }

        val lazyListState = rememberLazyListState()

        val fetchExpanded = remember { mutableStateOf(false) }
        val searchExpanded = remember { mutableStateOf(false) }

        val focusRequester = remember { FocusRequester() }
        val focusManager = LocalFocusManager.current

        fun invokeSearch(searchOver: Boolean = false) {
            Log.d("DutSchedule", "Invoking search")
            CoroutineScope(Dispatchers.IO).launch {
                if (newsProgress.value == ProcessState.Running) {
                    return@launch
                }

                if (searchQuery.value.isEmpty()) {
                    newsProgress.value = ProcessState.NotRunYet
                    return@launch
                } else {
                    newsProgress.value = ProcessState.Running
                }

                try {
                    if (searchOver) {
                        newsList.clear()
                    }
                    if (newsType.value) {
                        newsList.addAll(
                            DutNewsRepository.getNewsSubject(
                                if (searchOver) 1 else newsPage.value,
                                searchType = searchType.value,
                                searchQuery = searchQuery.value
                            )
                        )
                    } else {
                        newsList.addAll(
                            DutNewsRepository.getNewsGlobal(
                                if (searchOver) 1 else newsPage.value,
                                searchType = searchType.value,
                                searchQuery = searchQuery.value
                            )
                        )
                    }
                    if (searchOver) {
                        newsPage.value = 2
                    } else {
                        newsPage.value += 1
                    }

                    newsProgress.value = ProcessState.Successful
                } catch (_: Exception) {
                    newsProgress.value = ProcessState.Failed
                }
            }
        }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        OutlinedTextField(
                            value = searchQuery.value,
                            onValueChange = { searchQuery.value = it },
                            modifier = Modifier.focusRequester(focusRequester),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Search
                            ),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    focusManager.clearFocus(force = true)
                                    invokeSearch(searchOver = true)
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
                            modifier = Modifier.padding(start = 3.dp),
                            color = if (searchType.value == NewsSearchType.ByContent) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                            shape = RoundedCornerShape(7.dp),
                            content = {
                                IconButton(
                                    onClick = {
                                        focusManager.clearFocus(force = true)
                                        fetchExpanded.value = true
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
                                    expanded = fetchExpanded.value,
                                    onDismissRequest = { fetchExpanded.value = false },
                                    content = {
                                        listOf(
                                            NewsSearchType.ByTitle,
                                            NewsSearchType.ByContent
                                        ).forEach {
                                            DropdownMenuItem(
                                                modifier = Modifier.background(
                                                    color = if (searchType.value == it) MaterialTheme.colorScheme.secondaryContainer
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
                                                    searchType.value = it
                                                    fetchExpanded.value = false
                                                }
                                            )
                                        }
                                    }
                                )
                            }
                        )
                        Surface(
                            modifier = Modifier.padding(start = 3.dp),
                            color = if (newsType.value) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                            shape = RoundedCornerShape(7.dp),
                            content = {
                                IconButton(
                                    onClick = {
                                        focusManager.clearFocus(force = true)
                                        searchExpanded.value = true
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
                                    expanded = searchExpanded.value,
                                    onDismissRequest = { searchExpanded.value = false },
                                    content = {
                                        listOf(false, true).forEach {
                                            DropdownMenuItem(
                                                modifier = Modifier.background(
                                                    color = if (newsType.value == it) MaterialTheme.colorScheme.secondaryContainer
                                                    else MaterialTheme.colorScheme.surface
                                                ),
                                                text = {
                                                    Text(
                                                        when (it) {
                                                            true -> "News subject"
                                                            false -> "News global"
                                                        }
                                                    )
                                                },
                                                onClick = {
                                                    newsType.value = it
                                                    searchExpanded.value = false
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
                                focusManager.clearFocus(force = true)
                                invokeSearch(searchOver = true)
                            },
                            enabled = newsProgress.value != ProcessState.Running,
                            content = {
                                if (newsProgress.value == ProcessState.Running) {
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
                        invokeSearch(searchOver = false)
                    }
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 10.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = {
                                focusManager.clearFocus()
                            })
                        },
                    verticalArrangement = if (newsList.isNotEmpty()) Arrangement.Top else Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    state = lazyListState,
                    content = {
                        when {
                            (newsList.isNotEmpty()) -> {
                                items(newsList) { item ->
                                    NewsListItem(
                                        title = item.title,
                                        description = item.contentString,
                                        dateTime = item.date,
                                        onClick = {
                                            focusManager.clearFocus()
                                            context.startActivity(
                                                Intent(
                                                    context,
                                                    NewsDetailActivity::class.java
                                                ).also {
                                                    it.action =
                                                        if (newsType.value) "news_subject" else "news_global"
                                                    it.putExtra("data", Gson().toJson(item))
                                                })
                                        }
                                    )
                                    Spacer(modifier = Modifier.size(3.dp))
                                }
                            }

                            (newsProgress.value == ProcessState.Running) -> {
                                item {
                                    CircularProgressIndicator()
                                }
                            }

                            (newsProgress.value == ProcessState.NotRunYet) -> {
                                item {
                                    Text(
                                        "Tap search on top to get started.",
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            (newsProgress.value != ProcessState.Running && newsList.isEmpty()) -> {
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

        val hasRun = remember { mutableStateOf(false) }
        run {
            if (!hasRun.value) {
                CoroutineScope(Dispatchers.Main).launch {
                    focusRequester.requestFocus()
                }
                hasRun.value = true
            }
        }
    }
}
