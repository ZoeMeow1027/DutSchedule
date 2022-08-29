package io.zoemeow.dutapp.android.utils

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.zoemeow.dutapp.android.R

class NotificationsUtils {
    companion object {
        /**
         * Create notification channel for easier manage which notifications will be received.
         *
         * Required for Android 8 and later.
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
            val service = activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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
            val service = activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            service.createNotificationChannel(channel)
        }

        fun showNotification(
            context: Context,
            channel_id: String,
            news_md5: String,
            news_title: String,
            news_description: String,
        ) {
            val builder = NotificationCompat.Builder(context, channel_id)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(news_title)
                .setContentText(news_description)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            with (NotificationManagerCompat.from(context)) {
                notify(calcMD5CharValue(news_md5), builder.build())
            }
        }
    }
}
