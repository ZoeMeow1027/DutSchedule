package io.zoemeow.dutschedule.activity

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import dagger.hilt.android.AndroidEntryPoint
import io.zoemeow.dutschedule.service.BaseService
import io.zoemeow.dutschedule.service.NewsBackgroundUpdateService
import io.zoemeow.dutschedule.ui.view.main.MainViewDashboard
import io.zoemeow.dutschedule.utils.NotificationsUtil

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    @Composable
    override fun OnPreloadOnce() {
        NewsBackgroundUpdateService.cancelSchedule(
            context = this,
            onDone = {
                Log.d("NewsBackgroundService", "Cancelled schedule")
            }
        )
        NotificationsUtil.initializeNotificationChannel(this)
    }

    @Composable
    override fun OnMainView(
        context: Context,
        snackBarHostState: SnackbarHostState,
        containerColor: Color,
        contentColor: Color
    ) {
        MainViewDashboard(
            context = context,
            snackBarHostState = snackBarHostState,
            containerColor = containerColor,
            contentColor = contentColor,
            newsClicked = {
                context.startActivity(Intent(context, NewsActivity::class.java))
            },
            accountClicked = {
                context.startActivity(Intent(context, AccountActivity::class.java))
            },
            settingsClicked = {
                context.startActivity(Intent(context, SettingsActivity::class.java))
            },
            externalLinkClicked = {
                val intent = Intent(context, HelpActivity::class.java)
                intent.action = "view_externallink"
                context.startActivity(intent)
            }
        )
    }

    override fun onStop() {
        Log.d("MainActivity", "MainActivity is being stopped")
        NewsBackgroundUpdateService.cancelSchedule(
            context = this,
            onDone = {
                Log.d("NewsBackgroundService", "Cancelled schedule")
            }
        )
        if (getMainViewModel().appSettings.value.newsBackgroundDuration > 0) {
            Log.d("NewsBackgroundService", "Started service")
            BaseService.startService(
                context = this,
                intent = Intent(applicationContext, NewsBackgroundUpdateService::class.java).also {
                    it.action = "news.service.action.fetchallpage1background.skipfirst"
                }
            )
        }
        super.onStop()
    }
}