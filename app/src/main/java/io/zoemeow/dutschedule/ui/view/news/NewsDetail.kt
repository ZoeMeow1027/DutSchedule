package io.zoemeow.dutschedule.ui.view.news

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.dutwrapper.dutwrapper.model.enums.NewsType
import io.dutwrapper.dutwrapper.model.news.NewsGlobalItem
import io.dutwrapper.dutwrapper.model.news.NewsSubjectItem
import io.zoemeow.dutschedule.activity.NewsActivity
import io.zoemeow.dutschedule.ui.component.news.NewsDetailScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsActivity.NewsDetail(
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
                            setResult(ComponentActivity.RESULT_OK)
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