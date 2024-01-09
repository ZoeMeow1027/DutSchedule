package io.zoemeow.dutschedule.model.permissionrequest

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

class PermissionList {
    companion object {
        val PERMISSION_NOTIFICATION = PermissionInfo(
            name = "Notifications",
            code = "android.permission.POST_NOTIFICATIONS",
            minSdk = 33,
            description = "Allow this app to send new announcements " +
                    "(news global and news subject) and other for you.",
            required = false
        )

        val PERMISSION_MANAGE_EXTERNAL_STORAGE = PermissionInfo(
            name = "Manage External Storage",
            code = "android.permission.MANAGE_EXTERNAL_STORAGE",
            minSdk = 30,
            maxSdk = 33,
            description = "This app will use your current wallpaper as app background wallpaper. " +
                    "We promise not to upload or modify any data on your device, including your wallpaper. " +
                    "If you don't want to grant this permission, you can use \"Choose a image from media\" instead.",
            required = false,
            extraAction = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Intent(
                    Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                    Uri.parse("package:${"io.zoemeow.dutschedule"}")
                )
            } else {
                Intent(
                    "android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS",
                    Uri.parse("package:${"io.zoemeow.dutschedule"}")
                )
            }
        )

        val PERMISSION_SCHEDULE_EXACT_ALARM = PermissionInfo(
            name = "Schedule Exact Alarm",
            code = "android.permission.SCHEDULE_EXACT_ALARM",
            minSdk = 31,
            description = "Allow this app to schedule service to update news in background for you.",
            required = false,
            extraAction = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Intent(
                    Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                    Uri.parse("package:${"io.zoemeow.dutschedule"}")
                )
            } else {
                Intent(
                    "android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS",
                    Uri.parse("package:${"io.zoemeow.dutschedule"}")
                )
            }
        )

        fun getAllRequiredPermissions(): List<PermissionInfo> {
            return listOf(
                PERMISSION_NOTIFICATION,
                PERMISSION_MANAGE_EXTERNAL_STORAGE,
                PERMISSION_SCHEDULE_EXACT_ALARM
            )
        }
    }
}