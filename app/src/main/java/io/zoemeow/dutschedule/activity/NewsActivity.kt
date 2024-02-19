package io.zoemeow.dutschedule.activity

import android.content.Context
import android.content.Intent
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import dagger.hilt.android.AndroidEntryPoint
import io.zoemeow.dutschedule.ui.view.news.MainView
import io.zoemeow.dutschedule.ui.view.news.NewsDetail
import io.zoemeow.dutschedule.ui.view.news.NewsSearch

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
                NewsSearch(
                    context = context,
                    snackBarHostState = snackBarHostState,
                    containerColor = containerColor,
                    contentColor = contentColor,
                )
            }

            "activity_detail" -> {
                NewsDetail(
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
}
