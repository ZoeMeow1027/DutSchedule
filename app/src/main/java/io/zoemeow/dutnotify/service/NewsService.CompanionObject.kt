package io.zoemeow.dutnotify.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.zoemeow.dutnotify.receiver.AppBroadcastReceiver

fun NewsService.Companion.getPendingIntent(context: Context): PendingIntent {
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

fun NewsService.Companion.startService(context: Context) {
    val intentService = Intent(context, NewsService::class.java)
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

fun NewsService.Companion.cancelSchedule(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.cancel(getPendingIntent(context))
}