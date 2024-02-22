package io.zoemeow.dutschedule.model

import android.content.Context
import android.content.Intent
import io.zoemeow.dutschedule.utils.getRandomString
import java.io.Serializable

data class NotificationHistory(
    private val id: String = getRandomString(32),
    val title: String,
    val description: String,
    val timestamp: Long,
    val intentAction: String,
    val intentArguments: Map<String, String>,
    val isRead: Boolean = false
): Serializable {
    fun toIntent(context: Context, target: Class<Any>): Intent {
        val intent = Intent(context, target)
        intent.action = intentAction
        for (item in intentArguments) {
            intent.putExtra(item.key, item.value)
        }

        return intent
    }

    fun isEqualId(item: NotificationHistory): Boolean {
        return this.id == item.id
    }

    fun clone(
        title: String? = null,
        description: String? = null,
        timestamp: Long? = null,
        intentAction: String? = null,
        intentArguments: Map<String, String>? = null,
        isRead: Boolean? = null,
        changeId: Boolean = false
    ): NotificationHistory {
        val map: Map<String, String> = mapOf()
        map.plus(intentArguments ?: this.intentArguments)

        return NotificationHistory(
            id = if (changeId) getRandomString(32) else id,
            title = title ?: this.title,
            description = description ?: this.description,
            timestamp = timestamp ?: this.timestamp,
            intentAction = intentAction ?: this.intentAction,
            intentArguments = map,
            isRead = isRead ?: this.isRead
        )
    }
}