package io.zoemeow.dutschedule.utils

import android.Manifest
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.zoemeow.dutschedule.R
import io.zoemeow.dutschedule.activity.NewsActivity

class NotificationsUtil {
    companion object {
        private fun createNotificationChannelWithDefaultSettings(
            activity: Activity,
            id: String,
            name: String,
            description: String? = null,
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_DEFAULT)
                description?.let { channel.description = it }
                val service =
                    activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                service.createNotificationChannel(channel)
            }
        }

        private fun createNotificationChannelWithSilentSettings(
            activity: Activity,
            id: String,
            name: String,
            description: String? = null,
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_NONE)
                channel.enableVibration(false)
                channel.lockscreenVisibility = Notification.VISIBILITY_SECRET
                description?.let { channel.description = it }
                val service =
                    activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                service.createNotificationChannel(channel)
            }
        }

        /**
         * Create notification channel for easier manage which notifications will be received.
         *
         * Required by Android 8 and later.
         */
        fun initializeNotificationChannel(activity: Activity) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                return
            }

            createNotificationChannelWithDefaultSettings(
                activity = activity,
                id = "notification.id.announcement",
                name = activity.getString(R.string.notification_announcement_title),
                description = activity.getString(R.string.notification_announcement_description)
            )
            createNotificationChannelWithDefaultSettings(
                activity = activity,
                id = "notification.id.news.global",
                name = activity.getString(R.string.notification_newsglobal_title),
                description = activity.getString(R.string.notification_newsglobal_description)
            )
            createNotificationChannelWithDefaultSettings(
                activity = activity,
                id = "notification.id.news.subject",
                name = activity.getString(R.string.notification_newssubject_title),
                description = activity.getString(R.string.notification_newssubject_description)
            )
            createNotificationChannelWithSilentSettings(
                activity = activity,
                id = "notification.id.service",
                name = activity.getString(R.string.notification_newsbackgroundupdateservice_title),
                description = activity.getString(R.string.notification_newsbackgroundupdateservice_description)
            )
        }

        fun showNewsNotification(
            context: Context,
            channelId: String,
            newsMD5: String,
            newsTitle: String,
            newsDescription: String,
            jsonData: String
        ) {
            val notificationIntent = Intent(context, NewsActivity::class.java).also {
                it.action = "activity_detail"
                it.putExtra("type", when (channelId) {
                    "notification.id.news.global" -> "news_global"
                    "notification.id.news.subject" -> "news_subject"
                    else -> ""
                })
                it.putExtra("data", jsonData)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                newsMD5.calcToSumByCharArray(),
                notificationIntent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    (PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                else PendingIntent.FLAG_UPDATE_CURRENT
            )

            val builder = NotificationCompat.Builder(context, channelId)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_launcher_notification)
                .setContentTitle(newsTitle)
                .setContentText(newsDescription)
                .setStyle(
                    NotificationCompat.BigTextStyle().bigText(newsDescription)
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            with(NotificationManagerCompat.from(context)) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    notify(newsMD5.calcToSumByCharArray(), builder.build())
                }
            }
        }
    }
}