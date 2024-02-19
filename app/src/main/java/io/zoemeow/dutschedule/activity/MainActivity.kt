package io.zoemeow.dutschedule.activity

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import io.zoemeow.dutschedule.model.CustomClock
import io.zoemeow.dutschedule.model.ProcessState
import io.zoemeow.dutschedule.model.news.NewsCache
import io.zoemeow.dutschedule.service.BaseService
import io.zoemeow.dutschedule.service.NewsUpdateService
import io.zoemeow.dutschedule.ui.component.main.DateAndTimeSummaryItem
import io.zoemeow.dutschedule.ui.component.main.LessonTodaySummaryItem
import io.zoemeow.dutschedule.ui.component.main.SchoolNewsSummaryItem
import io.zoemeow.dutschedule.ui.component.main.UpdateAvailableSummaryItem
import io.zoemeow.dutschedule.ui.view.main.MainViewDashboard
import io.zoemeow.dutschedule.utils.CustomDateUtil
import io.zoemeow.dutschedule.utils.NotificationsUtil
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
        NewsUpdateService.cancelSchedule(
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
                    isLoading = getMainViewModel().currentSchoolWeek.processState.value == ProcessState.Running,
                    currentSchoolWeek = getMainViewModel().currentSchoolWeek.data.value,
                    opacity = getControlBackgroundAlpha()
                )
                LessonTodaySummaryItem(
                    padding = PaddingValues(bottom = 10.dp, start = 15.dp, end = 15.dp),
                    hasLoggedIn = getMainViewModel().accountSession.value.processState == ProcessState.Successful,
                    isLoading = getMainViewModel().accountSession.value.processState == ProcessState.Running || getMainViewModel().subjectSchedule.processState.value == ProcessState.Running,
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
                    affectedList = getMainViewModel().subjectSchedule.data.value?.filter { subSch ->
                        subSch.subjectStudy.scheduleList.any { schItem -> schItem.dayOfWeek + 1 == CustomDateUtil.getCurrentDayOfWeek() } &&
                                subSch.subjectStudy.scheduleList.any { schItem ->
                                    schItem.lesson.end >= CustomClock.getCurrent().toDUTLesson2().lesson
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
                    isLoading = getMainViewModel().newsGlobal.processState.value == ProcessState.Running,
                    opacity = getControlBackgroundAlpha()
                )
                UpdateAvailableSummaryItem(
                    padding = PaddingValues(bottom = 10.dp, start = 15.dp, end = 15.dp),
                    isLoading = false,
                    updateAvailable = false,
                    latestVersionString = "",
                    clicked = {
                        openLink(
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
            (getMainViewModel().newsGlobal.data.value ?: NewsCache()).newsListByDate.firstOrNull {
                // https://stackoverflow.com/questions/77368433/how-to-get-current-date-with-reset-time-0000-with-kotlinx-localdatetime
                it.date == today.toEpochMilliseconds()
            }.also {
                if (it != null) data = it.itemList.count()
            }
        } else {
            (getMainViewModel().newsGlobal.data.value ?: NewsCache()).newsListByDate.forEach {
                // https://stackoverflow.com/questions/77368433/how-to-get-current-date-with-reset-time-0000-with-kotlinx-localdatetime
                if (it.date <= today.toEpochMilliseconds() && it.date >= before7Days.toEpochMilliseconds()) {
                    data += it.itemList.count()
                }
            }
        }
        return data
    }

    override fun onStop() {
        Log.d("MainActivity", "MainActivity is being stopped")
        NewsUpdateService.cancelSchedule(
            context = this,
            onDone = {
                Log.d("NewsBackgroundService", "Cancelled schedule")
            }
        )
        if (getMainViewModel().appSettings.value.newsBackgroundDuration > 0) {
            Log.d("NewsBackgroundService", "Started service")
            BaseService.startService(
                context = this,
                intent = Intent(applicationContext, NewsUpdateService::class.java).also {
                    it.action = "news.service.action.fetchallpage1background"
                }
            )
        }
        super.onStop()
    }
}