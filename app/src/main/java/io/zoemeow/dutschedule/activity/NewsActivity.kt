package io.zoemeow.dutschedule.activity

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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import io.zoemeow.dutschedule.R
import io.zoemeow.dutschedule.ui.component.base.ButtonBase
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NewsActivity : BaseActivity() {
    @Composable
    override fun OnMainView(padding: PaddingValues) {
        MainView()
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    @Composable
    private fun MainView() {
        val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
        val scope = rememberCoroutineScope()

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(text = "News") },
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
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { /*TODO*/ }) {
                    Icon(Icons.Default.Search, "")
                }
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
                            NewsListPage(title = "News Global")
                        }

                        1 -> {
                            NewsListPage(title = "News Subject")
                        }
                    }
                }
            }
        )
    }

    @Composable
    private fun NewsListPage(title: String) {
        Surface {
            Text(text = title)
        }
    }
}
