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
//        setNotificationsForeground(
//            "Getting news global...",
//            0,
//            3
//        )
        checkNewsGlobal(file = file)
//        setNotificationsForeground(
//            "Getting news subject...",
//            1,
//            3
//        )
        checkNewsSubject(file = file)
        // Send reload news requested to activity.
//        setNotificationsForeground(
//            "Processing filter existing news...",
//            2,
//            3
//        )
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
        if (newsCacheGlobal.pageCurrent <= 1)
            newsCacheGlobal.pageCurrent += 1
        file.saveCacheNewsGlobal(
            newsCacheGlobal = newsCacheGlobal
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
        if (newsCacheSubject.pageCurrent <= 1)
            newsCacheSubject.pageCurrent += 1
        file.saveCacheNewsSubject(
            newsCacheSubject = newsCacheSubject
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
                news_title = "New notification from News Global",
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

            // If filter was empty -> Not set -> All news.
            if (settings.newsFilterList.isEmpty())
                notify = true
            // If a news in filter list, enable notify.
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

            // Lecturer
            var lecturer = newsItem.title.split("thông báo đến lớp:")[0]
            if (lecturer.startsWith("Thầy "))
                lecturer = lecturer.substring(5)
            else if (lecturer.startsWith("Cô "))
                lecturer = lecturer.substring(3)

            // Lesson status
            val lessonStatus = when (newsItem.lessonStatus) {
                LessonStatus.Leaving -> "Leaving "
                LessonStatus.MakeUp -> "Make up "
                else -> ""
            }

            // Affected classrooms
            var affectedClassrooms = ""
            newsItem.affectedClass.forEach { className ->
                if (affectedClassrooms.isEmpty()) {
                    affectedClassrooms = "Affected classrooms: ${className.subjectName}"
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

            val notifyTitle = "New ${lessonStatus}notification from $lecturer"
            var notifyContent = "${affectedClassrooms}\n\n"
            val notifyContentList = arrayListOf<String>()
            when (newsItem.lessonStatus) {
                LessonStatus.MakeUp -> {
                    notifyContentList.add("on date: ${dateToString(newsItem.affectedDate, "dd/MM/yyyy")}")
                    notifyContentList.add("\nwith lesson: ${if (newsItem.affectedLesson != null) newsItem.affectedLesson else "(unknown)"}")
                    notifyContentList.add("\nin room: ${newsItem.affectedRoom}")
                    notifyContent += notifyContentList.joinToString("")
                }
                LessonStatus.Leaving -> {
                    notifyContentList.add("on date: ${dateToString(newsItem.affectedDate, "dd/MM/yyyy")}")
                    notifyContentList.add("\nwith lesson: ${if (newsItem.affectedLesson != null) newsItem.affectedLesson else "(unknown)"}")
                    notifyContent += notifyContentList.joinToString("")
                }
                else -> {
                    notifyContent += newsItem.contentString
                }
            }

            NotificationsUtils.showNewsNotification(
                context = this,
                channel_id = "dut_news_subject",
                news_md5 = getMD5("${newsItem.date}_${newsItem.title}"),
                news_title = notifyTitle,
                news_description = notifyContent,
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
            .setContentTitle("DUT Service is running in background")
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