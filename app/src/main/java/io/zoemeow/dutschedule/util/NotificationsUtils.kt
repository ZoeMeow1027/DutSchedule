package io.zoemeow.dutschedule.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.zoemeow.dutschedule.R
import io.zoemeow.dutschedule.activity.PermissionRequestActivity
import io.zoemeow.dutschedule.model.permissionrequest.PermissionList

class NotificationsUtils {
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
                name = "Announcement",
                description = "This will send you app recommend and important updates."
            )
            createNotificationChannelWithDefaultSettings(
                activity = activity,
                id = "notification.id.news.global",
                name = "News Global",
                description = "This will receive new messages in \"Thông báo chung\" on sv.dut.udn.vn"
            )
            createNotificationChannelWithDefaultSettings(
                activity = activity,
                id = "notification.id.news.subject",
                name = "News Subject",
                description = "This will receive new messages in \"Thông báo lớp học phần\" on sv.dut.udn.vn"
            )
            createNotificationChannelWithSilentSettings(
                activity = activity,
                id = "notification.id.service",
                name = "News Update Service",
                description = "This will ensure this service will able to run in background. You can turn off this notification if you don't want show them."
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
//            val notificationIntent = Intent(context, NewsDetailsActivity::class.java).apply {
//                putExtra("type", channel_id)
//                if (data != null)
//                    putExtra("data", data as Serializable)
//            }
//
//            val pendingIntent = PendingIntent.getActivity(
//                context,
//                AppUtils.calcMD5CharValue(news_md5),
//                notificationIntent,
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
//                    (PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
//                else PendingIntent.FLAG_UPDATE_CURRENT
//            )

            val builder = NotificationCompat.Builder(context, channel_id)
                //.setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(news_title)
                .setContentText(news_description)
                .setStyle(
                    NotificationCompat.BigTextStyle().bigText(news_description)
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            with(NotificationManagerCompat.from(context)) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    notify(AppUtils.calcMD5CharValue(news_md5), builder.build())
                }
            }
        }
    }
}