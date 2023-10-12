package io.zoemeow.dutschedule.model.permissionrequest

data class PermissionInfo(
    val name: String,
    val code: String,
    val minSdk: Int,
    val description: String,
    val required: Boolean = false,
)