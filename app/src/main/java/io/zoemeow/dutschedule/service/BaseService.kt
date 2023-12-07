package io.zoemeow.dutschedule.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import io.zoemeow.dutschedule.R
import io.zoemeow.dutschedule.model.CustomClock
import io.zoemeow.dutschedule.model.ProcessState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

abstract class BaseService(
    private val nNotifyId: String,
    private val nTitle: String,
    private val nContent: String
) : Service() {
    override fun onCreate() {
        setNotificationOnForeground(nTitle, nContent)
        onInitialize()
        super.onCreate()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                doWorkBackground(intent)
            }
        }.invokeOnCompletion {
            it?.printStackTrace()
            onCompleted(if (it != null) ProcessState.Successful else ProcessState.Failed)
        }

        return START_STICKY_COMPATIBILITY
    }

    override fun onDestroy() {
        onDestroying()
        super.onDestroy()
    }

    private fun setNotificationOnForeground(title: String, description: String) {
        val builder = NotificationCompat.Builder(this, nNotifyId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOnlyAlertOnce(true)
            .setContentTitle(title)
            .setContentText(description)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                1,
                builder.build(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE
            )
        } else {
            startForeground(
                1,
                builder.build()
            )
        }
    }

    abstract fun onInitialize()

    abstract fun doWorkBackground(intent: Intent?)

    abstract fun onCompleted(result: ProcessState)

    abstract fun onDestroying()

    fun scheduleNextRun(
        intervalInMinute: Int = 5,
        scheduleStart: CustomClock? = null,
        scheduleEnd: CustomClock? = null,
        pendingIntent: PendingIntent
    ) {
        // TODO: Exception if intervalInMinute is less than 5.
        val currentTime = Calendar.getInstance().also {
            it.add(Calendar.MINUTE, intervalInMinute)
        }
        val currentInstance = CustomClock(currentTime)
        var scheduleInstance = currentInstance

        // If in range, keep current, otherwise, set by scheduleStart
        if (scheduleStart != null && scheduleEnd != null) {
            if (!currentInstance.isInRange(scheduleStart, scheduleEnd)) {
                scheduleInstance = scheduleStart
            }
        }

        // Use scheduleInstance for set schedule.
        // Get to Unix milliseconds.
        val scheduleTime = Calendar.getInstance().also {
            it.set(Calendar.HOUR_OF_DAY, scheduleInstance.hour)
            it.set(Calendar.MINUTE, scheduleInstance.minute)
            if (scheduleInstance < CustomClock.getCurrent()) {
                it.add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        val clockInstanceInUnix = scheduleTime.timeInMillis

        // Have already pending intent
        // Schedule for next run
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val canSetExactAlarm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else true

        if (canSetExactAlarm) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    clockInstanceInUnix,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    clockInstanceInUnix,
                    pendingIntent
                )
            }
        } else {
            // TODO: Processing if missing permission android.permission.SCHEDULE_EXACT_ALARM.
        }
    }

    companion object {
        fun startService(
            context: Context,
            intent: Intent
        ) {
            // https://stackoverflow.com/a/47654126
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            }
            else context.startService(intent)
        }
    }
}