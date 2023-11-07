package io.zoemeow.dutschedule.model.permissionrequest

import android.content.Intent

data class PermissionInfo(
    val name: String,
    val code: String,
    val minSdk: Int,
    val description: String,
    val required: Boolean = false,
    val extraAction: Intent? = null
)