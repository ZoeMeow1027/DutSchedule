package io.zoemeow.dutnotify.utils

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.zoemeow.dutnotify.NewsDetailsActivity
import io.zoemeow.dutnotify.R
import java.io.Serializable

class NotificationsUtils {
    companion object {
        @RequiresApi(Build.VERSION_CODES.O)
        private fun createNotificationChannelWithDefaultSettings(
            activity: Activity,
            id: String,
            name: String,
            description: String? = null,
        ) {
            val channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_DEFAULT)
            if (description != null)
                channel.description = description
            val service =
                activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            service.createNotificationChannel(channel)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun createNotificationChannelWithSilentSettings(
            activity: Activity,
            id: String,
            name: String,
            description: String? = null,
        ) {
            val channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_NONE)
            channel.enableVibration(false)
            channel.lockscreenVisibility = Notification.VISIBILITY_SECRET
            if (description != null)
                channel.description = description
            val service =
                activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            service.createNotificationChannel(channel)
        }

        /**
         * Create notification channel for easier manage which notifications will be received.
         *
         * Required by Android 8 and later.
         */
        @RequiresApi(Build.VERSION_CODES.O)
        fun initializeNotificationChannel(
            activity: Activity
        ) {
            createNotificationChannelWithDefaultSettings(
                activity = activity,
                id = "dut_news_global",
                name = "News Global",
                description = "This will receive new messages in \"Thông báo chung\" on sv.dut.udn.vn"
            )
            createNotificationChannelWithDefaultSettings(
                activity = activity,
                id = "dut_news_subject",
                name = "News Subject",
                description = "This will receive new messages in \"Thông báo lớp học phần\" on sv.dut.udn.vn"
            )
            createNotificationChannelWithSilentSettings(
                activity = activity,
                id = "dut_service",
                name = "Service",
                description = "This will ensure this service will able to run in background. You can turn off this notification."
            )
        }

        fun showNewsNotification(
            context: Context,
            channel_id: String,
            news_md5: String,
            news_title: String,
            news_description: String,
            data: Any?
        ) {
            val notificationIntent = Intent(context, NewsDetailsActivity::class.java).apply {
                putExtra("type", channel_id)
                if (data != null)
                    putExtra("data", data as Serializable)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                calcMD5CharValue(news_md5),
                notificationIntent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    (PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                else PendingIntent.FLAG_UPDATE_CURRENT
            )

            val builder = NotificationCompat.Builder(context, channel_id)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(news_title)
                .setContentText(news_description)
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText(news_description))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            with(NotificationManagerCompat.from(context)) {
                notify(calcMD5CharValue(news_md5), builder.build())
            }
        }
    }
}
