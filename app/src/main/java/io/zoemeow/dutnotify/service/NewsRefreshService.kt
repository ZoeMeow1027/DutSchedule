package io.zoemeow.dutnotify.service

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
import io.zoemeow.dutnotify.R
import io.zoemeow.dutnotify.model.appsettings.CustomClock
import io.zoemeow.dutnotify.model.news.NewsCache
import io.zoemeow.dutnotify.module.FileModule
import io.zoemeow.dutnotify.module.NewsModule
import io.zoemeow.dutnotify.receiver.AppBroadcastReceiver
import io.zoemeow.dutnotify.util.NotificationsUtils
import io.zoemeow.dutnotify.util.dateToString
import io.zoemeow.dutnotify.util.getMD5
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class NewsRefreshService : Service() {
    override fun onCreate() {
        Log.i("RefreshNewsService", "Service created")
        val builder = NotificationCompat.Builder(this, "dut_service")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle("DUT Service is running in background")
            .setContentText("Getting news from sv.dut.udn.vn...")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        startForeground(1, builder.build())
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                Log.i("RefreshNewsService", "Service is running...")
                doWorkBackground()
            }
        }.invokeOnCompletion {
            it?.printStackTrace()
            Log.i("RefreshNewsService", "Done work background!")
            stopSelf()
        }

        Log.i("RefreshNewsService", "Service started")
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        // TODO("Return the communication channel to the service.")
        return null
    }

    override fun onDestroy() {
        Log.i("RefreshNewsService", "Service is being destroyed")
        super.onDestroy()
        Log.i("RefreshNewsService", "Service destroyed")
        Log.i("RefreshNewsService", "Scheduling a plan for auto-refresh news")
        scheduleNextRun()
    }

    private fun scheduleNextRun() {
        val settings = FileModule(this).getAppSettings()

        if (!settings.refreshNewsEnabled)
            return

        // The update frequency should often be user configurable.  This is not.
        val currentTimeMillis = System.currentTimeMillis()

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, settings.refreshNewsIntervalInMinute)
        val nextTimeRefresh = CustomClock(
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE)
        )

        if (!nextTimeRefresh.isInRange(
                settings.refreshNewsTimeStart,
                settings.refreshNewsTimeEnd
            )
        ) {
            calendar.set(Calendar.HOUR_OF_DAY, settings.refreshNewsTimeStart.hour)
            calendar.set(Calendar.MINUTE, settings.refreshNewsTimeEnd.minute)
            calendar.set(Calendar.SECOND, 0)
            if (calendar.timeInMillis < Calendar.getInstance().timeInMillis)
                calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        val nextUpdateTimeMillis = calendar.timeInMillis

        val pendingIntent = getPendingIntent(applicationContext)
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

        Log.i(
            "RefreshNewsService",
            "Service will auto-start after ${(nextUpdateTimeMillis - currentTimeMillis) / 1000} seconds."
        )
    }

    private fun doWorkBackground() {
        val file = FileModule(this.applicationContext)
        checkNewsGlobal(file = file)
        checkNewsSubject(file = file)

        sendRequestToActivity()
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
            notifyUsersGlobal(newsDiff)
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
            notifyUsersSubject(newsDiff)
        }

        newsFromInternet.clear()
        newsDiff.clear()
    }

    private fun notifyUsersGlobal(
        list: ArrayList<NewsGlobalItem>,
    ) {
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
    ) {
        list.forEach { newsItem ->
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
                    if (newsItem.contentString.length > 400)
                        notifyContent += newsItem.contentString
                    else notifyContent += "Tap on this notification to open news details in app."
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

    private fun sendRequestToActivity() {
        val intent = Intent(AppBroadcastReceiver.NEWS_RELOADREQUESTED_SERVICE_ACTIVITY)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    companion object {
        fun getPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, NewsRefreshService::class.java)
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
            val intentService = Intent(context, NewsRefreshService::class.java)
            // https://stackoverflow.com/a/47654126

            try {
                context.stopService(intentService)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

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