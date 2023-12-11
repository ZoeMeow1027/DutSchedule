package io.zoemeow.dutschedule.activity

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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import io.zoemeow.dutschedule.R
import io.zoemeow.dutschedule.model.CustomClock
import io.zoemeow.dutschedule.model.ProcessState
import io.zoemeow.dutschedule.model.news.NewsCache
import io.zoemeow.dutschedule.service.BaseService
import io.zoemeow.dutschedule.service.NewsUpdateService
import io.zoemeow.dutschedule.ui.component.main.AffectedLessonsSummaryItem
import io.zoemeow.dutschedule.ui.component.main.DateAndTimeSummaryItem
import io.zoemeow.dutschedule.ui.component.main.LessonTodaySummaryItem
import io.zoemeow.dutschedule.ui.component.main.SchoolNewsSummaryItem
import io.zoemeow.dutschedule.ui.component.main.UpdateAvailableSummaryItem
import io.zoemeow.dutschedule.ui.theme.DutScheduleTheme
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
    override fun OnMainView(padding: PaddingValues) {
        // A surface container using the 'background' color from the theme

        val context = LocalContext.current
        MainView(
            newsClicked = {
                this.startActivity(Intent(this, NewsActivity::class.java))
            },
            accountClicked = {
                this.startActivity(Intent(this, AccountActivity::class.java))
            },
            settingsClicked = {
                this.startActivity(Intent(this, SettingsActivity::class.java))
            },
            content = {
                DateAndTimeSummaryItem(
                    padding = PaddingValues(bottom = 10.dp, start = 15.dp, end = 15.dp),
                    isLoading = getMainViewModel().currentSchoolWeek2.processState.value == ProcessState.Running,
                    currentSchoolWeek = getMainViewModel().currentSchoolWeek2.data.value
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
                    }?.toList() ?: listOf()
                )
                AffectedLessonsSummaryItem(
                    padding = PaddingValues(bottom = 10.dp, start = 15.dp, end = 15.dp),
                    hasLoggedIn = getMainViewModel().accountSession.value.processState == ProcessState.Successful,
                    isLoading = getMainViewModel().accountSession.value.processState == ProcessState.Running || getMainViewModel().subjectSchedule2.processState.value == ProcessState.Running,
                    clicked = {},
                    affectedList = arrayListOf("ie1i0921d - i029di12", "ie1i0921d - i029di12","ie1i0921d - i029di12","ie1i0921d - i029di12","ie1i0921d - i029di12")
                )
                SchoolNewsSummaryItem(
                    padding = PaddingValues(bottom = 10.dp, start = 15.dp, end = 15.dp),
                    newsToday = getNews(false),
                    newsThisWeek = getNews(true),
                    clicked = {
                        context.startActivity(Intent(context, NewsActivity::class.java))
                    },
                    isLoading = getMainViewModel().newsGlobal2.processState.value == ProcessState.Running
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
                    }
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
        newsClicked: (() -> Unit)? = null,
        accountClicked: (() -> Unit)? = null,
        settingsClicked: (() -> Unit)? = null,
        content: (@Composable ColumnScope.() -> Unit)? = null,
    ) {
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

        Scaffold(
            modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = Color.Transparent,
            topBar = {
                LargeTopAppBar(
                    title = { Text(text = "DutSchedule") },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    scrollBehavior = scrollBehavior
                )
            },
            bottomBar = {
                BottomAppBar(
                    actions = {
                        IconButton(
                            onClick = { newsClicked?.let { it() } },
                            content = {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_newspaper_24),
                                    "News",
                                    modifier = Modifier
                                        .size(30.dp)
                                        .padding(end = 7.dp),
                                )
                            }
                        )
                        IconButton(
                            onClick = { accountClicked?.let { it() } },
                            content = {
                                Icon(
                                    Icons.Outlined.AccountCircle,
                                    "Account",
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
//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .wrapContentHeight()
//                                .verticalScroll(rememberScrollState()),
//                            horizontalArrangement = Arrangement.Center,
//                            verticalAlignment = Alignment.CenterVertically,
//                            content = {
//                                ButtonBase(
//                                    clicked = {
//                                        newsClicked?.let { it() }
//                                    },
//                                    content = {
//                                        Icon(
//                                            painter = painterResource(id = R.drawable.baseline_newspaper_24),
//                                            "News",
//                                            modifier = Modifier
//                                                .size(30.dp)
//                                                .padding(end = 7.dp),
//                                        )
//                                        Text("News")
//                                    }
//                                )
//                                ButtonBase(
//                                    modifier = Modifier.padding(start = 10.dp),
//                                    clicked = {
//                                        accountClicked?.let {
//                                            it()
//                                        }
//                                    },
//                                    content = {
//                                        Icon(
//                                            Icons.Outlined.AccountCircle,
//                                            "",
//                                            modifier = Modifier
//                                                .size(30.dp)
//                                                .padding(end = 7.dp),
//                                        )
//                                        Text("Account")
//                                    }
//                                )
//                                ButtonBase(
//                                    modifier = Modifier.padding(start = 10.dp),
//                                    clicked = {
//                                        settingsClicked?.let {
//                                            it()
//                                        }
//                                    },
//                                    content = {
//                                        Icon(
//                                            Icons.Default.Settings,
//                                            "",
//                                            modifier = Modifier
//                                                .size(30.dp)
//                                                .padding(end = 7.dp),
//                                        )
//                                        Text("Settings")
//                                    }
//                                )
//                            }
//                        )
                    },
                    floatingActionButton = {
                        ExtendedFloatingActionButton(
                            text = { Text("Refresh") },
                            icon = { Icon(Icons.Default.Refresh, "Refresh status") },
                            onClick = { /*TODO*/ }
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
//                putExtra("news.service.variable.fetchtype", 1)
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
//                putExtra("news.service.variable.fetchtype", 1)
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

    @Preview(showBackground = true)
    @Composable
    private fun SummaryItemPreview() {
        DutScheduleTheme {
            MainView()
        }
    }
}