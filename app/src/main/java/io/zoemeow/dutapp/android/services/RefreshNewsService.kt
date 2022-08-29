package io.zoemeow.dutapp.android.services

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
import io.zoemeow.dutapi.objects.NewsGlobalItem
import io.zoemeow.dutapp.android.R
import io.zoemeow.dutapp.android.model.news.NewsCacheGlobal
import io.zoemeow.dutapp.android.module.FileModule
import io.zoemeow.dutapp.android.module.NewsModule
import io.zoemeow.dutapp.android.utils.NotificationsUtils
import io.zoemeow.dutapp.android.utils.getMD5
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class RefreshNewsService : Service() {
    private var intent1: Intent? = null

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
        intent1 = intent

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
        val intent = Intent(applicationContext, RefreshNewsService::class.java)
        val pendingIntent: PendingIntent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            pendingIntent = PendingIntent.getForegroundService(
                this,
                1,
                intent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    PendingIntent.FLAG_IMMUTABLE
                else PendingIntent.FLAG_UPDATE_CURRENT
            )
        } else {
            pendingIntent = PendingIntent.getService(
                this,
                1,
                intent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    PendingIntent.FLAG_IMMUTABLE
                else PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        // The update frequency should often be user configurable.  This is not.
        val currentTimeMillis = System.currentTimeMillis()
        // val nextUpdateTimeMillis = currentTimeMillis + secondToCheck * DateUtils.SECOND_IN_MILLIS

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.SECOND, secondToCheck)
        if (calendar.get(Calendar.HOUR_OF_DAY) !in 6..22) {
            if (calendar.get(Calendar.HOUR_OF_DAY) > 22)
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 6)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
        }

        val nextUpdateTimeMillis = calendar.timeInMillis

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextUpdateTimeMillis, pendingIntent)
        } else {
            alarmManager[AlarmManager.RTC, nextUpdateTimeMillis] = pendingIntent
        }

        Log.i("RefreshNewsService", "Service will auto-start after ${(nextUpdateTimeMillis - currentTimeMillis) / 1000} seconds.")
    }

    private val secondToCheck: Int = 120

    private fun doWorkBackground() {
        val file = FileModule(this.applicationContext)
        checkNewsGlobal(file)
        checkNewsSubject(file)

        sendRequestToActivity()
    }

    private fun checkNewsGlobal(
        file: FileModule
    ) {
        val newsCacheGlobal: NewsCacheGlobal = file.getCacheNewsGlobal()
        val newsFromInternet: ArrayList<NewsGlobalItem> = NewsModule.getNewsGlobal(
            page = 1
        )
        val newsDiff: ArrayList<NewsGlobalItem> = NewsModule.getNewsGlobalDiff(
            source = newsCacheGlobal.newsListByDate,
            target = newsFromInternet
        )
        NewsModule.addAndCheckDuplicateNewsGlobal(
            source = newsCacheGlobal.newsListByDate,
            target = newsDiff
        )
        if (newsCacheGlobal.pageCurrent <= 1)
            newsCacheGlobal.pageCurrent += 1
        file.saveCacheNewsGlobal(
            newsCacheGlobal = newsCacheGlobal
        )

        Log.d("RefreshNewsService", "News Global: Current group size: ${newsCacheGlobal.newsListByDate.size}")
        Log.d("RefreshNewsService", "News Global: News from internet size: ${newsFromInternet.size}")
        Log.d("RefreshNewsService", "News Global: News diff size: ${newsDiff.size}")

        // Notify to user and clear to avoid leak memory.
        if (newsCacheGlobal.newsListByDate.isNotEmpty()) {
            notifyUsers(newsDiff, "dut_news_global")
        }

        newsFromInternet.clear()
        newsDiff.clear();
    }

    private fun checkNewsSubject(
        file: FileModule
    ) {
        val newsCacheSubject: NewsCacheGlobal = file.getCacheNewsSubject()
        val newsFromInternet: ArrayList<NewsGlobalItem> = NewsModule.getNewsSubject(
            page = 1
        )
        val newsDiff: ArrayList<NewsGlobalItem> = NewsModule.getNewsSubjectDiff(
            source = newsCacheSubject.newsListByDate,
            target = newsFromInternet
        )
        NewsModule.addAndCheckDuplicateNewsGlobal(
            source = newsCacheSubject.newsListByDate,
            target = newsDiff
        )
        if (newsCacheSubject.pageCurrent <= 1)
            newsCacheSubject.pageCurrent += 1
        file.saveCacheNewsSubject(
            newsCacheSubject = newsCacheSubject
        )

        Log.d("RefreshNewsService", "News Subject: Current group size: ${newsCacheSubject.newsListByDate.size}")
        Log.d("RefreshNewsService", "News Subject: News from internet size: ${newsFromInternet.size}")
        Log.d("RefreshNewsService", "News Subject: News diff size: ${newsDiff.size}")

        // Notify to user and clear to avoid leak memory.
        if (newsCacheSubject.newsListByDate.isNotEmpty()) {
            notifyUsers(newsDiff, "dut_news_subject")
        }

        newsFromInternet.clear()
        newsDiff.clear()
    }

    private fun notifyUsers(
        list: ArrayList<NewsGlobalItem>,
        type: String,
    ) {
        // Notify to user and clear to avoid leak memory.
        list.forEach { newsItem ->
            NotificationsUtils.showNotification(
                context = this,
                channel_id = type,
                news_md5 = getMD5("${newsItem.date}___${newsItem.title}"),
                news_title = "New notification from ${if (type == "dut_news_global") "News Global" else "News Subject"}",
                news_description = newsItem.title
            )
        }
    }

    private fun sendRequestToActivity() {
        val intent = Intent("NewsReload")
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
}