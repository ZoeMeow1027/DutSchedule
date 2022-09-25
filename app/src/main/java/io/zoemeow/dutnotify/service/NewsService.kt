package io.zoemeow.dutnotify.service

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.zoemeow.dutapi.objects.enums.LessonStatus
import io.zoemeow.dutapi.objects.news.NewsGlobalItem
import io.zoemeow.dutapi.objects.news.NewsSubjectItem
import io.zoemeow.dutnotify.view.PermissionRequestActivity
import io.zoemeow.dutnotify.R
import io.zoemeow.dutnotify.model.appsettings.AppSettings
import io.zoemeow.dutnotify.model.appsettings.CustomClock
import io.zoemeow.dutnotify.model.appsettings.SubjectCode
import io.zoemeow.dutnotify.model.enums.NewsPageType
import io.zoemeow.dutnotify.model.enums.ServiceBroadcastOptions
import io.zoemeow.dutnotify.module.FileModule
import io.zoemeow.dutnotify.module.NewsModule
import io.zoemeow.dutnotify.utils.AppUtils
import io.zoemeow.dutnotify.utils.DUTDateUtils
import io.zoemeow.dutnotify.utils.NotificationsUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class NewsService : Service() {
    private lateinit var file: FileModule
    private lateinit var settings: AppSettings
    private var fetchInBackground = false

    override fun onCreate() {
        setNotificationOnForeground("Loading settings...")
        file = FileModule(this)
        settings = file.getAppSettings()
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("NewsService", "Triggered NewsService start")

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                doWorkBackground(intent)
            }
        }.invokeOnCompletion {
            it?.printStackTrace()
            stopSelf()
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        // Schedule next run.
        if (fetchInBackground) scheduleNextRunIfNeeded()
        // Just destroy this service.
        super.onDestroy()
    }

    private fun doWorkBackground(intent: Intent?) {
        if (intent == null)
            return

        // Check if notify to user here!
        val notifyToUser = intent.getBooleanExtra(ServiceBroadcastOptions.ARGUMENT_NEWS_NOTIFYTOUSER, false)
        val newsPageType = when (intent.getStringExtra(ServiceBroadcastOptions.ARGUMENT_NEWS_PAGEOPTION)) {
            ServiceBroadcastOptions.ARGUMENT_NEWS_PAGEOPTION_NEXTPAGE -> NewsPageType.NextPage
            ServiceBroadcastOptions.ARGUMENT_NEWS_PAGEOPTION_GETPAGE1 -> NewsPageType.GetPage1
            ServiceBroadcastOptions.ARGUMENT_NEWS_PAGEOPTION_RESETTO1 -> NewsPageType.ResetToPage1
            else -> NewsPageType.NextPage
        }

        when (intent.getStringExtra(ServiceBroadcastOptions.ACTION)) {
            ServiceBroadcastOptions.ACTION_NEWS_INITIALIZATION -> {
                sendBroadcastToMainActivity(
                    action = ServiceBroadcastOptions.ACTION_NEWS_FETCHGLOBAL,
                    data = file.getCacheNewsGlobal().newsListByDate
                )
                sendBroadcastToMainActivity(
                    action = ServiceBroadcastOptions.ACTION_NEWS_FETCHSUBJECT,
                    data = file.getCacheNewsSubject().newsListByDate
                )
                setNotificationOnForeground("Getting news global...", 0)
                fetchNewsGlobalAndNotify(notifyToUser, NewsPageType.GetPage1)
                setNotificationOnForeground("Getting news subject...", 50)
                fetchNewsSubjectAndNotify(notifyToUser, NewsPageType.GetPage1)
            }
            ServiceBroadcastOptions.ACTION_NEWS_FETCHGLOBAL -> {
                setNotificationOnForeground("Getting news global...", 0)
                fetchNewsGlobalAndNotify(notifyToUser, newsPageType)
            }
            ServiceBroadcastOptions.ACTION_NEWS_FETCHSUBJECT -> {
                setNotificationOnForeground("Getting news subject...", 0)
                fetchNewsSubjectAndNotify(notifyToUser, newsPageType)
            }
            ServiceBroadcastOptions.ACTION_NEWS_FETCHALL -> {
                setNotificationOnForeground("Getting news global...", 0)
                fetchNewsGlobalAndNotify(notifyToUser, NewsPageType.GetPage1)
                setNotificationOnForeground("Getting news subject...", 50)
                fetchNewsSubjectAndNotify(notifyToUser, NewsPageType.GetPage1)
            }
            ServiceBroadcastOptions.ACTION_NEWS_FETCHALLBACKGROUND -> {
                fetchInBackground = true
                setNotificationOnForeground("Getting news global...", 0)
                fetchNewsGlobalAndNotify(notifyToUser, NewsPageType.GetPage1)
                setNotificationOnForeground("Getting news subject...", 50)
                fetchNewsSubjectAndNotify(notifyToUser, NewsPageType.GetPage1)
            }
        }
    }

    private fun fetchNewsGlobalAndNotify(
        notifyToUser: Boolean = false,
        newsPageType: NewsPageType = NewsPageType.NextPage,
    ) {
        try {
            sendBroadcastToMainActivity(
                action = ServiceBroadcastOptions.ACTION_NEWS_FETCHGLOBAL,
                status = ServiceBroadcastOptions.STATUS_PROCESSING
            )

            val newsCacheGlobal = file.getCacheNewsGlobal()
            val newsFromInternet = NewsModule.getNewsGlobal(
                page = when (newsPageType) {
                    NewsPageType.NextPage -> newsCacheGlobal.pageCurrent
                    NewsPageType.GetPage1 -> 1
                    NewsPageType.ResetToPage1 -> 1
                }
            )

            if (newsPageType == NewsPageType.ResetToPage1)
                newsCacheGlobal.newsListByDate.clear()

            val newsDiff = NewsModule.getNewsGlobalDiff(
                source = newsCacheGlobal.newsListByDate,
                target = newsFromInternet,
            )
            NewsModule.addAndCheckDuplicateNewsGlobal(
                source = newsCacheGlobal.newsListByDate,
                target = newsDiff,
                addItemToTop = newsPageType != NewsPageType.NextPage
            )

            when (newsPageType) {
                NewsPageType.NextPage -> {
                    newsCacheGlobal.pageCurrent += 1
                }
                NewsPageType.GetPage1 -> {
                    if (newsCacheGlobal.pageCurrent <= 1)
                        newsCacheGlobal.pageCurrent += 1
                }
                NewsPageType.ResetToPage1 -> {
                    newsCacheGlobal.pageCurrent = 2
                }
            }
            file.saveCacheNewsGlobal(newsCacheGlobal)

            sendBroadcastToMainActivity(
                action = ServiceBroadcastOptions.ACTION_NEWS_FETCHGLOBAL,
                status = ServiceBroadcastOptions.STATUS_SUCCESSFUL,
                data = newsCacheGlobal.newsListByDate
            )

            if (newsDiff.size > 0) {
                if (notifyToUser) notifyUsersGlobal(newsDiff)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            sendBroadcastToMainActivity(
                action = ServiceBroadcastOptions.ACTION_NEWS_FETCHGLOBAL,
                status = ServiceBroadcastOptions.STATUS_FAILED
            )
        }
    }

    private fun fetchNewsSubjectAndNotify(
        notifyToUser: Boolean = false,
        newsPageType: NewsPageType = NewsPageType.NextPage,
    ) {
        try {
            sendBroadcastToMainActivity(
                action = ServiceBroadcastOptions.ACTION_NEWS_FETCHSUBJECT,
                status = ServiceBroadcastOptions.STATUS_PROCESSING
            )

            val newsCacheSubject = file.getCacheNewsSubject()
            val newsFromInternet = NewsModule.getNewsSubject(
                page = when (newsPageType) {
                    NewsPageType.NextPage -> newsCacheSubject.pageCurrent
                    NewsPageType.GetPage1 -> 1
                    NewsPageType.ResetToPage1 -> 1
                }
            )

            if (newsPageType == NewsPageType.ResetToPage1)
                newsCacheSubject.newsListByDate.clear()

            val newsDiff = NewsModule.getNewsSubjectDiff(
                source = newsCacheSubject.newsListByDate,
                target = newsFromInternet
            )
            NewsModule.addAndCheckDuplicateNewsSubject(
                source = newsCacheSubject.newsListByDate,
                target = newsDiff,
                addItemToTop = newsPageType != NewsPageType.NextPage
            )

            when (newsPageType) {
                NewsPageType.NextPage -> {
                    newsCacheSubject.pageCurrent += 1
                }
                NewsPageType.GetPage1 -> {
                    if (newsCacheSubject.pageCurrent <= 1)
                        newsCacheSubject.pageCurrent += 1
                }
                NewsPageType.ResetToPage1 -> {
                    newsCacheSubject.pageCurrent = 2
                }
            }
            file.saveCacheNewsSubject(newsCacheSubject)

            sendBroadcastToMainActivity(
                action = ServiceBroadcastOptions.ACTION_NEWS_FETCHSUBJECT,
                status = ServiceBroadcastOptions.STATUS_SUCCESSFUL,
                data = newsCacheSubject.newsListByDate
            )

            if (newsDiff.size > 0) {
                if (notifyToUser) notifyUsersSubject(newsDiff)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            sendBroadcastToMainActivity(
                action = ServiceBroadcastOptions.ACTION_NEWS_FETCHSUBJECT,
                status = ServiceBroadcastOptions.STATUS_FAILED,
            )
        }
    }

    private fun notifyUsersGlobal(
        list: ArrayList<NewsGlobalItem>,
    ) {
        if (!PermissionRequestActivity.checkPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            )
        )
            return

        list.forEach { newsItem ->
            NotificationsUtils.showNewsNotification(
                context = this,
                channel_id = "dut_news_global",
                news_md5 = AppUtils.getMD5("${newsItem.date}_${newsItem.title}"),
                news_title = getString(R.string.notification_newsglobal_title),
                news_description = newsItem.title,
                data = newsItem
            )
        }
    }

    private fun notifyUsersSubject(
        list: ArrayList<NewsSubjectItem>,
    ) {
        if (!PermissionRequestActivity.checkPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            )
        )
            return

        list.forEach { newsItem ->
            // Default value is false.
            var notify = false
            // If enabled news filter, do following.

            // If filter was empty -> Not set -> All news -> Enable notify.
            if (settings.newsFilterList.isEmpty())
                notify = true
            // If a news in filter list -> Enable notify.
            else if (settings.newsFilterList.any { source ->
                    newsItem.affectedClass.any { targetGroup ->
                        targetGroup.codeList.any { target ->
                            source.isEquals(
                                SubjectCode(
                                    target.studentYearId,
                                    target.classId,
                                    targetGroup.subjectName
                                )
                            )
                        }
                    }
                }
            ) notify = true

            // If no notify, continue with return@forEach.
            if (!notify)
                return@forEach

            val notifyTitle = when (newsItem.lessonStatus) {
                LessonStatus.Leaving -> {
                    String.format(
                        getString(R.string.notification_newssubject_titlewitharg),
                        getString(R.string.notification_newssubject_leaving),
                        newsItem.lecturerName
                    )
                }
                LessonStatus.MakeUp -> {
                    String.format(
                        getString(R.string.notification_newssubject_titlewitharg),
                        getString(R.string.notification_newssubject_makeup),
                        newsItem.lecturerName
                    )
                }
                else -> {
                    String.format(
                        getString(R.string.notification_newssubject_title),
                        newsItem.lecturerName
                    )
                }
            }

            // Affected classrooms
            var affectedClassrooms = ""
            newsItem.affectedClass.forEach { className ->
                if (affectedClassrooms.isEmpty()) {
                    affectedClassrooms = className.subjectName
                } else {
                    affectedClassrooms += ", ${className.subjectName}"
                }
                var first = true
                for (item in className.codeList) {
                    if (first) {
                        affectedClassrooms += " ("
                        first = false
                    } else {
                        affectedClassrooms += ", "
                    }
                    affectedClassrooms += "${item.studentYearId}.${item.classId}"
                }
                affectedClassrooms += ")"
            }

            val notifyContentList = arrayListOf<String>()
            // Affected classrooms
            notifyContentList.add(
                "${
                    String.format(
                        getString(R.string.notification_newssubject_appliedto),
                        affectedClassrooms
                    )
                }\n"
            )
            // Date and lessons
            if (
                newsItem.lessonStatus == LessonStatus.Leaving ||
                newsItem.lessonStatus == LessonStatus.MakeUp
            ) {
                // Date
                notifyContentList.add(
                    String.format(
                        getString(R.string.notification_newssubject_date),
                        DUTDateUtils.dateToString(newsItem.affectedDate, "dd/MM/yyyy")
                    )
                )
                // Lessons
                notifyContentList.add(
                    String.format(
                        getString(R.string.notification_newssubject_lesson),
                        when (newsItem.lessonStatus) {
                            LessonStatus.Leaving -> getString(R.string.notification_newssubject_leaving)
                            LessonStatus.MakeUp -> getString(R.string.notification_newssubject_makeup)
                            else -> ""
                        },
                        if (newsItem.affectedLesson != null) newsItem.affectedLesson.toString() else getString(
                            R.string.notification_newssubject_unknown
                        ),
                    )
                )
                // Make-up room
                if (newsItem.lessonStatus == LessonStatus.MakeUp) {
                    // Make up in room
                    notifyContentList.add(
                        String.format(
                            getString(R.string.notification_newssubject_room),
                            newsItem.affectedRoom
                        )
                    )
                }
            } else {
                notifyContentList.add(newsItem.contentString)
            }

            NotificationsUtils.showNewsNotification(
                context = this,
                channel_id = "dut_news_subject",
                news_md5 = AppUtils.getMD5("${newsItem.date}_${newsItem.title}"),
                news_title = notifyTitle,
                news_description = notifyContentList.joinToString("\n"),
                data = newsItem
            )
        }
    }

    /**
     * Send ReloadFromCacheRequested in news cache main activity.
     */
    private fun sendBroadcastToMainActivity(
        action: String,
        status: String? = null,
        data: Any? = null,
        errorMsg: String? = null
    ) {
        Intent(action).apply {
            if (status != null) putExtra(ServiceBroadcastOptions.STATUS, status)
            if (data != null) putExtra(ServiceBroadcastOptions.DATA, data as java.io.Serializable)
            if (errorMsg != null) putExtra(ServiceBroadcastOptions.ERRORMESSAGE, errorMsg)
        }.also {
            sendBroadcast(it)
        }
    }

    override fun sendBroadcast(intent: Intent) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    /**
     * Schedule next run when needed.
     */
    private fun scheduleNextRunIfNeeded() {
        // If not enabled, will skip set alarm.
        if (!settings.refreshNewsEnabled)
            return

        // Get next unix timestamp in millis.
        val nextTimeInMillis = getNextRunInUnixTimestamp(
            timeStart = settings.refreshNewsTimeStart,
            timeEnd = settings.refreshNewsTimeEnd,
            intervalInMinute = settings.refreshNewsIntervalInMinute
        )
        // Get pending intent.
        val pendingIntent = getPendingIntentOnBackground(
            context = this
        )
        // Schedule next run with set alarm.
        setAlarm(
            pendingIntent = pendingIntent,
            nextUpdateTimeMillis = nextTimeInMillis,
        )
        Log.d("NewsService", "Scheduled a new run")
    }

    /**
     * Set notification in foreground to keep service running.
     */
    private fun setNotificationOnForeground(
        contentText: String,
        progress: Int? = null
    ) {
        val builder = NotificationCompat.Builder(this, "dut_service")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOnlyAlertOnce(true)
            .setContentTitle("DUT News Service is running in background")
            .setContentText(contentText)
        if (progress != null)
            builder.setProgress(100, progress, false)
        val notifyBuild = builder.build()
        startForeground(1, notifyBuild)
    }

    /**
     * Get next unix timestamp in millis with time range and interval in minute.
     */
    private fun getNextRunInUnixTimestamp(
        timeStart: CustomClock,
        timeEnd: CustomClock,
        intervalInMinute: Int,
    ): Long {
        // Get next time with calendar instance.
        val nextTimeCalendar = Calendar.getInstance()

        // Current timestamp in millis.
        val currentTimeInMillis = nextTimeCalendar.timeInMillis

        // Add minute in parameters to instance.
        nextTimeCalendar.add(Calendar.MINUTE, intervalInMinute)
        // Convert to CustomClock.
        val nextTimeRefresh = CustomClock(
            nextTimeCalendar.get(Calendar.HOUR_OF_DAY),
            nextTimeCalendar.get(Calendar.MINUTE)
        )

        // Check is in time range in parameters. If not, set to time start at tomorrow.
        if (!nextTimeRefresh.isInRange(timeStart, timeEnd)) {
            nextTimeCalendar.set(Calendar.HOUR_OF_DAY, timeStart.hour)
            nextTimeCalendar.set(Calendar.MINUTE, timeStart.minute)
            nextTimeCalendar.set(Calendar.SECOND, 0)
            // If current time is greater than next time, add a day to that.
            if (nextTimeCalendar.timeInMillis < currentTimeInMillis)
                nextTimeCalendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        // Next timestamp in millis.
        val nextTimeInMillis = nextTimeCalendar.timeInMillis

        // Clear instance.
        nextTimeCalendar.clear()

        // Return next time in millis.
        return nextTimeInMillis
    }

    /**
     * Set pending intent to run in time millis.
     */
    private fun setAlarm(
        pendingIntent: PendingIntent,
        nextUpdateTimeMillis: Long,
    ) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextUpdateTimeMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                nextUpdateTimeMillis,
                pendingIntent
            )
        }
    }

    companion object {
        fun getPendingIntentOnBackground(context: Context): PendingIntent {
            val intent = Intent(context, NewsService::class.java)
            intent.putExtra(ServiceBroadcastOptions.ACTION, ServiceBroadcastOptions.ACTION_NEWS_FETCHALLBACKGROUND)
            intent.putExtra(ServiceBroadcastOptions.ARGUMENT_NEWS_NOTIFYTOUSER, true)
            val pendingIntent: PendingIntent
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                pendingIntent = PendingIntent.getForegroundService(
                    context,
                    1,
                    intent,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                        PendingIntent.FLAG_IMMUTABLE
                    else PendingIntent.FLAG_UPDATE_CURRENT
                )
            } else {
                pendingIntent = PendingIntent.getService(
                    context,
                    1,
                    intent,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                        PendingIntent.FLAG_IMMUTABLE
                    else PendingIntent.FLAG_UPDATE_CURRENT
                )
            }

            return pendingIntent
        }

        fun startService(
            context: Context,
            intent: Intent
        ) {
            // https://stackoverflow.com/a/47654126
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                context.startForegroundService(intent)
            else context.startService(intent)
        }

        fun cancelSchedule(
            context: Context
        ) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(getPendingIntentOnBackground(context))
        }
    }
}