package io.zoemeow.dutschedule.activity

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import io.dutwrapperlib.dutwrapper.objects.enums.NewsType
import io.dutwrapperlib.dutwrapper.objects.news.NewsGlobalItem
import io.dutwrapperlib.dutwrapper.objects.news.NewsSubjectItem
import io.zoemeow.dutschedule.ui.component.news.NewsDetailScreen
import io.zoemeow.dutschedule.util.OpenLink

@AndroidEntryPoint
class NewsDetailActivity: BaseActivity() {
    @Composable
    override fun OnPreloadOnce() { }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun OnMainView(padding: PaddingValues) {
        val newsType = intent.action
        val newsData = intent.getStringExtra("data")

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
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
            content = {
                when (newsType) {
                    "news_global" -> {
                        NewsDetailScreen(
                            padding = it,
                            newsItem = Gson().fromJson(newsData, object : TypeToken<NewsGlobalItem>() {}.type),
                            newsType = NewsType.Global,
                            linkClicked = { link ->
                                OpenLink(
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
                                OpenLink(
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