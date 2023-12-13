package io.zoemeow.dutschedule.activity

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import io.zoemeow.dutschedule.R
import io.zoemeow.dutschedule.model.CustomClock
import io.zoemeow.dutschedule.model.ProcessState
import io.zoemeow.dutschedule.model.news.NewsCache
import io.zoemeow.dutschedule.service.BaseService
import io.zoemeow.dutschedule.service.NewsUpdateService
import io.zoemeow.dutschedule.ui.component.main.DateAndTimeSummaryItem
import io.zoemeow.dutschedule.ui.component.main.LessonTodaySummaryItem
import io.zoemeow.dutschedule.ui.component.main.SchoolNewsSummaryItem
import io.zoemeow.dutschedule.ui.component.main.UpdateAvailableSummaryItem
import io.zoemeow.dutschedule.util.CustomDateUtils
import io.zoemeow.dutschedule.util.NotificationsUtils
import io.zoemeow.dutschedule.util.OpenLink
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    @Composable
    override fun OnPreloadOnce() {
        NotificationsUtils.initializeNotificationChannel(this)
        NewsUpdateService.cancelSchedule(this)
    }

    @Composable
    override fun OnMainView(
        context: Context,
        snackBarHostState: SnackbarHostState,
        containerColor: Color,
        contentColor: Color
    ) {
        MainView(
            context = context,
            snackBarHostState = snackBarHostState,
            containerColor = containerColor,
            contentColor = contentColor,
            newsClicked = {
                context.startActivity(Intent(this, NewsActivity::class.java))
            },
            accountClicked = {
                context.startActivity(Intent(this, AccountActivity::class.java))
            },
            settingsClicked = {
                context.startActivity(Intent(this, SettingsActivity::class.java))
            },
            externalLinkClicked = {
                val intent = Intent(context, HelpActivity::class.java)
                intent.action = "view_externallink"
                context.startActivity(intent)
            },
            content = {
                DateAndTimeSummaryItem(
                    padding = PaddingValues(bottom = 10.dp, start = 15.dp, end = 15.dp),
                    isLoading = getMainViewModel().currentSchoolWeek2.processState.value == ProcessState.Running,
                    currentSchoolWeek = getMainViewModel().currentSchoolWeek2.data.value,
                    opacity = getControlBackgroundAlpha()
                )
                LessonTodaySummaryItem(
                    padding = PaddingValues(bottom = 10.dp, start = 15.dp, end = 15.dp),
                    hasLoggedIn = getMainViewModel().accountSession.value.processState == ProcessState.Successful,
                    isLoading = getMainViewModel().accountSession.value.processState == ProcessState.Running || getMainViewModel().subjectSchedule2.processState.value == ProcessState.Running,
                    clicked = {
                        getMainViewModel().accountLogin(
                            after = {
                                if (it) {
                                    val intent = Intent(context, AccountActivity::class.java)
                                    intent.action = "subject_schedule"
                                    context.startActivity(intent)
                                }
                            }
                        )
                    },
                    affectedList = getMainViewModel().subjectSchedule2.data.value?.filter { subSch ->
                        subSch.subjectStudy.scheduleList.any { schItem -> schItem.dayOfWeek + 1 == CustomDateUtils.getCurrentDayOfWeek() } &&
                                subSch.subjectStudy.scheduleList.any { schItem ->
                                    schItem.lesson.end >= when (CustomClock.getCurrent().toDUTLesson()) {
                                        -3 -> -99.0
                                        -2 -> -99.0
                                        -1 -> 5.5
                                        0 -> 99.0
                                        else -> CustomClock.getCurrent().toDUTLesson().toDouble()
                                    }
                                }
                    }?.toList() ?: listOf(),
                    opacity = getControlBackgroundAlpha()
                )
//                AffectedLessonsSummaryItem(
//                    padding = PaddingValues(bottom = 10.dp, start = 15.dp, end = 15.dp),
//                    hasLoggedIn = getMainViewModel().accountSession.value.processState == ProcessState.Successful,
//                    isLoading = getMainViewModel().accountSession.value.processState == ProcessState.Running || getMainViewModel().subjectSchedule2.processState.value == ProcessState.Running,
//                    clicked = {},
//                    affectedList = arrayListOf("ie1i0921d - i029di12", "ie1i0921d - i029di12","ie1i0921d - i029di12","ie1i0921d - i029di12","ie1i0921d - i029di12"),
//                    opacity = getControlBackgroundAlpha()
//                )
                SchoolNewsSummaryItem(
                    padding = PaddingValues(bottom = 10.dp, start = 15.dp, end = 15.dp),
                    newsToday = getNews(false),
                    newsThisWeek = getNews(true),
                    clicked = {
                        context.startActivity(Intent(context, NewsActivity::class.java))
                    },
                    isLoading = getMainViewModel().newsGlobal2.processState.value == ProcessState.Running,
                    opacity = getControlBackgroundAlpha()
                )
                UpdateAvailableSummaryItem(
                    padding = PaddingValues(bottom = 10.dp, start = 15.dp, end = 15.dp),
                    isLoading = false,
                    updateAvailable = false,
                    latestVersionString = "",
                    clicked = {
                        OpenLink(
                            url = "https://github.com/ZoeMeow1027/DutSchedule/releases",
                            context = context,
                            customTab = false,
                        )
                    },
                    opacity = getControlBackgroundAlpha()
                )
            }
        )
    }

    private fun getNews(byWeek: Boolean = false): Int {
        var data = 0
        val today = LocalDateTime(
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
            LocalTime(0, 0, 0)
        ).toInstant(TimeZone.UTC)
        val before7Days = today.minus(7.days)

        if (!byWeek) {
            (getMainViewModel().newsGlobal2.data.value ?: NewsCache()).newsListByDate.firstOrNull {
                // https://stackoverflow.com/questions/77368433/how-to-get-current-date-with-reset-time-0000-with-kotlinx-localdatetime
                it.date == today.toEpochMilliseconds()
            }.also {
                if (it != null) data = it.itemList.count()
            }
        } else {
            (getMainViewModel().newsGlobal2.data.value ?: NewsCache()).newsListByDate.forEach {
                // https://stackoverflow.com/questions/77368433/how-to-get-current-date-with-reset-time-0000-with-kotlinx-localdatetime
                if (it.date <= today.toEpochMilliseconds() && it.date >= before7Days.toEpochMilliseconds()) {
                    data += it.itemList.count()
                }
            }
        }
        return data
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainView(
        context: Context,
        snackBarHostState: SnackbarHostState,
        containerColor: Color,
        contentColor: Color,
        newsClicked: (() -> Unit)? = null,
        accountClicked: (() -> Unit)? = null,
        settingsClicked: (() -> Unit)? = null,
        externalLinkClicked: (() -> Unit)? = null,
        content: (@Composable ColumnScope.() -> Unit)? = null,
    ) {
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
            containerColor = containerColor,
            contentColor = contentColor,
            topBar = {
                LargeTopAppBar(
                    title = { Text(text = "DutSchedule") },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    scrollBehavior = scrollBehavior
                )
            },
            bottomBar = {
                BottomAppBar(
                    containerColor = BottomAppBarDefaults.containerColor.copy(
                        alpha = getControlBackgroundAlpha()
                    ),
                    actions = {
                        IconButton(
                            onClick = { newsClicked?.let { it() } },
                            content = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_baseline_newspaper_24),
                                    "News",
                                    modifier = Modifier
                                        .size(30.dp)
                                        .padding(end = 7.dp),
                                )
                            }
                        )
                        IconButton(
                            onClick = { settingsClicked?.let { it() } },
                            content = {
                                Icon(
                                    Icons.Default.Settings,
                                    "Settings",
                                    modifier = Modifier
                                        .size(30.dp)
                                        .padding(end = 7.dp),
                                )
                            }
                        )
                        IconButton(
                            onClick = { externalLinkClicked?.let { it() } },
                            content = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_baseline_web_24),
                                    "External links",
                                    modifier = Modifier
                                        .size(30.dp)
                                        .padding(end = 7.dp),
                                )
                            }
                        )
                    },
                    floatingActionButton = {
                        ExtendedFloatingActionButton(
                            text = {
                                getMainViewModel().accountSession.value.let {
                                    Text(
                                        when (it.processState) {
                                            ProcessState.NotRunYet -> "Sign in"
                                            ProcessState.Running -> "Fetching..."
                                            ProcessState.Failed -> when (it.data.accountAuth.username == null) {
                                                true -> "Sign in"
                                                false -> String.format(
                                                    "%s (failed)",
                                                    it.data.accountAuth.username
                                                )
                                            }
                                            else -> it.data.accountAuth.username ?: "unknown"
                                        }
                                    )
                                }
                            },
                            icon = {
                                when (getMainViewModel().accountSession.value.processState) {
                                    ProcessState.Running -> CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp)
                                    )
                                    else -> Icon(Icons.Outlined.AccountCircle, "Account")
                                }
                            },
                            onClick = { accountClicked?.let { it() } }
                        )
                    }
                )
            },
            content = { padding ->
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    color = Color.Transparent,
                    content = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            content = {
                                content?.let { it() }
                            },
                        )
                    }
                )
            }
        )
    }

    override fun onPause() {
        NewsUpdateService.cancelSchedule(this)
        if (getMainViewModel().appSettings.value.newsBackgroundDuration > 0) {
            BaseService.startService(
                context = this,
                intent = Intent(applicationContext, NewsUpdateService::class.java).also {
                    it.action = "news.service.action.fetchallpage1background"
                }
            )
        }
        super.onPause()
    }

    override fun onDestroy() {
        NewsUpdateService.cancelSchedule(this)
        if (getMainViewModel().appSettings.value.newsBackgroundDuration > 0) {
            BaseService.startService(
                context = this,
                intent = Intent(applicationContext, NewsUpdateService::class.java).also {
                    it.action = "news.service.action.fetchallpage1background"
                }
            )
        }
        super.onDestroy()
    }

    override fun onResume() {
        NewsUpdateService.cancelSchedule(this)
        super.onResume()
    }

    override fun onRestart() {
        NewsUpdateService.cancelSchedule(this)
        super.onRestart()
    }
}