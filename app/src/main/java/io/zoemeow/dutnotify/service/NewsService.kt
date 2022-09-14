package io.zoemeow.dutnotify.service

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.zoemeow.dutapi.objects.enums.LessonStatus
import io.zoemeow.dutapi.objects.news.NewsGlobalItem
import io.zoemeow.dutapi.objects.news.NewsSubjectItem
import io.zoemeow.dutnotify.PermissionRequestActivity
import io.zoemeow.dutnotify.R
import io.zoemeow.dutnotify.model.appsettings.CustomClock
import io.zoemeow.dutnotify.model.appsettings.SubjectCode
import io.zoemeow.dutnotify.model.news.NewsCache
import io.zoemeow.dutnotify.module.FileModule
import io.zoemeow.dutnotify.module.NewsModule
import io.zoemeow.dutnotify.receiver.AppBroadcastReceiver
import io.zoemeow.dutnotify.utils.DUTDateUtils.Companion.dateToString
import io.zoemeow.dutnotify.utils.NotificationsUtils
import io.zoemeow.dutnotify.utils.getMD5
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class NewsService : Service() {
    override fun onCreate() {
        setNotificationsForeground("Preparing...")
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                doWorkBackground()
            }
        }.invokeOnCompletion {
            it?.printStackTrace()
            stopSelf()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        // TODO("Return the communication channel to the service.")
        return null
    }

    override fun onDestroy() {
        // Schedule next run.
        scheduleNextRunIfNeeded()
        // Just destroy this service.
        super.onDestroy()
    }

    /**
     * Schedule next run when needed.
     */
    private fun scheduleNextRunIfNeeded() {
        // Get current settings.
        val settings = FileModule(this).getAppSettings()
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
        val pendingIntent = getPendingIntent(
            context = applicationContext
        )
        // Schedule next run with set alarm.
        setAlarm(
            pendingIntent = pendingIntent,
            nextUpdateTimeMillis = nextTimeInMillis,
        )
    }

    private fun doWorkBackground() {
        // Get file module.
        val file = FileModule(this.applicationContext)
        // Check news global and subject. After that, notify if needed.
        setNotificationsForeground(
            "Getting news global...",
            0,
            2
        )
        checkNewsGlobal(file = file)
        setNotificationsForeground(
            "Getting news subject...",
            1,
            2
        )
        checkNewsSubject(file = file)
        // Send reload news requested to activity.
        setNotificationsForeground(
            "Requesting broadcast to activity...",
            2,
            2
        )
        sendBroadcastToActivity()
    }

    private fun checkNewsGlobal(
        file: FileModule,
    ) {
        var shouldNotifyUsers = false
        val newsCacheGlobal: NewsCache<NewsGlobalItem> = file.getCacheNewsGlobal()
        val newsFromInternet: ArrayList<NewsGlobalItem> = NewsModule.getNewsGlobal(
            page = 1
        )
        setNotificationsForeground(
            "Getting news global...",
            1,
            8
        )
        val newsDiff: ArrayList<NewsGlobalItem> = NewsModule.getNewsGlobalDiff(
            source = newsCacheGlobal.newsListByDate,
            target = newsFromInternet
        )
        if (newsDiff.size > 0 && newsCacheGlobal.newsListByDate.isNotEmpty())
            shouldNotifyUsers = true
        NewsModule.addAndCheckDuplicateNewsGlobal(
            source = newsCacheGlobal.newsListByDate,
            target = newsDiff
        )
        setNotificationsForeground(
            "Getting news global...",
            2,
            8
        )
        if (newsCacheGlobal.pageCurrent <= 1)
            newsCacheGlobal.pageCurrent += 1
        file.saveCacheNewsGlobal(
            newsCacheGlobal = newsCacheGlobal
        )
        setNotificationsForeground(
            "Getting news global...",
            3,
            8
        )
        // Notify to user and clear to avoid leak memory.
        if (shouldNotifyUsers) {
            notifyUsersGlobal(
                list = newsDiff,
            )
        }

        newsFromInternet.clear()
        newsDiff.clear()
    }

    private fun checkNewsSubject(
        file: FileModule,
    ) {
        var shouldNotifyUsers = false
        val newsCacheSubject: NewsCache<NewsSubjectItem> = file.getCacheNewsSubject()
        val newsFromInternet: ArrayList<NewsSubjectItem> = NewsModule.getNewsSubject(
            page = 1
        )
        setNotificationsForeground(
            "Getting news subject...",
            5,
            8
        )
        val newsDiff: ArrayList<NewsSubjectItem> = NewsModule.getNewsSubjectDiff(
            source = newsCacheSubject.newsListByDate,
            target = newsFromInternet
        )
        if (newsDiff.size > 0 && newsCacheSubject.newsListByDate.isNotEmpty())
            shouldNotifyUsers = true
        NewsModule.addAndCheckDuplicateNewsSubject(
            source = newsCacheSubject.newsListByDate,
            target = newsDiff
        )
        setNotificationsForeground(
            "Getting news subject...",
            6,
            8
        )
        if (newsCacheSubject.pageCurrent <= 1)
            newsCacheSubject.pageCurrent += 1
        file.saveCacheNewsSubject(
            newsCacheSubject = newsCacheSubject
        )
        setNotificationsForeground(
            "Getting news subject...",
            7,
            8
        )
        // Notify to user and clear to avoid leak memory.
        if (shouldNotifyUsers) {
            notifyUsersSubject(
                list = newsDiff,
                fileModule = file,
            )
        }

        newsFromInternet.clear()
        newsDiff.clear()
    }

    private fun notifyUsersGlobal(
        list: ArrayList<NewsGlobalItem>,
    ) {
        if (!PermissionRequestActivity.checkPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS))
            return

        list.forEach { newsItem ->
            NotificationsUtils.showNewsNotification(
                context = this,
                channel_id = "dut_news_global",
                news_md5 = getMD5("${newsItem.date}_${newsItem.title}"),
                news_title = getString(R.string.notification_newsglobal_title),
                news_description = newsItem.title,
                data = newsItem
            )
        }
    }

    private fun notifyUsersSubject(
        list: ArrayList<NewsSubjectItem>,
        fileModule: FileModule,
    ) {
        if (!PermissionRequestActivity.checkPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS))
            return

        val settings = fileModule.getAppSettings()
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
                            source.isEquals(SubjectCode(target.studentYearId, target.classId, targetGroup.subjectName))
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
                }
                else {
                    affectedClassrooms += ", ${className.subjectName}"
                }
                var first = true
                for (item in className.codeList) {
                    if (first) {
                        affectedClassrooms += " ("
                        first = false
                    }
                    else {
                        affectedClassrooms += ", "
                    }
                    affectedClassrooms += "${item.studentYearId}.${item.classId}"
                }
                affectedClassrooms += ")"
            }

            val notifyContentList = arrayListOf<String>()
            // Affected classrooms
            notifyContentList.add("${getString(R.string.notification_newssubject_appliedto, affectedClassrooms)}\n")
            // Date and lessons
            if (
                newsItem.lessonStatus == LessonStatus.Leaving ||
                newsItem.lessonStatus == LessonStatus.MakeUp
            ) {
                // Date
                notifyContentList.add(String.format(
                    getString(R.string.notification_newssubject_date),
                    dateToString(newsItem.affectedDate, "dd/MM/yyyy")
                ))
                // Lessons
                notifyContentList.add(String.format(
                    getString(R.string.notification_newssubject_lesson),
                    if (newsItem.affectedLesson != null)
                        newsItem.affectedLesson else getString(R.string.notification_newssubject_unknown)
                ))
                // Make-up room
                if (newsItem.lessonStatus == LessonStatus.MakeUp) {
                    // Make up in room
                    notifyContentList.add(String.format(
                        getString(R.string.notification_newssubject_room),
                        newsItem.affectedRoom
                    ))
                }
            } else {
                notifyContentList.add(newsItem.contentString)
            }

            NotificationsUtils.showNewsNotification(
                context = this,
                channel_id = "dut_news_subject",
                news_md5 = getMD5("${newsItem.date}_${newsItem.title}"),
                news_title = notifyTitle,
                news_description = notifyContentList.joinToString("\n"),
                data = newsItem
            )
        }
    }

    private fun setNotificationsForeground(
        contentText: String,
        progress: Int? = null,
        progressMax: Int? = null
    ) {
        val builder = NotificationCompat.Builder(this, "dut_service")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOnlyAlertOnce(true)
            .setContentTitle("DUT News Service is running in background")
            .setContentText(contentText)
        if (progress != null && progressMax != null)
            builder.setProgress(progressMax, progress, false)
        val notifyBuild = builder.build()
        startForeground(1, notifyBuild)
    }

    /**
     * Send broadcast about request reload news cache to MainActivity.
     */
    private fun sendBroadcastToActivity() {
        val intent = Intent(AppBroadcastReceiver.NEWS_RELOADREQUESTED_SERVICE_ACTIVITY)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
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
        fun getPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, NewsService::class.java)
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

        fun startService(context: Context) {
            val intentService = Intent(context, NewsService::class.java)
            // https://stackoverflow.com/a/47654126

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                context.startForegroundService(intentService)
            else context.startService(intentService)
        }

        fun cancelSchedule(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(getPendingIntent(context))
        }
    }
}