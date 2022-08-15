package io.zoemeow.dutapp.android.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.text.format.DateUtils
import android.util.Log


class RefreshNewsService : Service() {
    override fun onCreate() {
        Log.i("Service", "Service created")
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("Service", "Service started")
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        // TODO("Return the communication channel to the service.")
        return null
    }

    override fun onDestroy() {
        Log.i("Service", "Service is being destroyed...")

        scheduleNextRun()
        super.onDestroy()
        Log.i("Service", "Service destroyed")
    }

    private fun scheduleNextRun() {
        val intent = Intent(this, this.javaClass)
        val pendingIntent = PendingIntent.getService(
            this,
            0,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                PendingIntent.FLAG_IMMUTABLE
            else PendingIntent.FLAG_UPDATE_CURRENT
        )

        // The update frequency should often be user configurable.  This is not.
        val currentTimeMillis = System.currentTimeMillis()
        val nextUpdateTimeMillis = currentTimeMillis + 15 * DateUtils.SECOND_IN_MILLIS
        Log.i("Service", "Service will auto-start after 15 seconds.")

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager[AlarmManager.RTC, nextUpdateTimeMillis] = pendingIntent
    }
}