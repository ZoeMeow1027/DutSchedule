package io.zoemeow.dutschedule.model

import android.content.Context
import android.content.Intent
import io.zoemeow.dutschedule.utils.getRandomString
import java.io.Serializable

data class NotificationHistory(
    private val id: String = getRandomString(32),
    val title: String,
    val description: String,
    val tag: Int = 0,
    val timestamp: Long,
    val parameters: Map<String, String>,
    val isRead: Boolean = false,
    val isReceived: Boolean = false
): Serializable {
    fun toIntent(context: Context, target: Class<Any>, action: String): Intent {
        val intent = Intent(context, target)
        intent.action = action
        for (item in parameters) {
            intent.putExtra(item.key, item.value)
        }

        return intent
    }

    fun tagToString(): String {
        return when (tag) {
            0 -> "General"
            1 -> "News global"
            2 -> "Your news subject filter"
            else -> "(No tag)"
        }
    }

    fun isEqualId(item: NotificationHistory): Boolean {
        return this.id == item.id
    }

    fun clone(
        title: String? = null,
        description: String? = null,
        timestamp: Long? = null,
        tag: Int? = null,
        intentArguments: Map<String, String>? = null,
        isRead: Boolean? = null,
        isReceived: Boolean? = null,
        changeIdAfterCopy: Boolean = false
    ): NotificationHistory {
        val map: Map<String, String> = mapOf()
        map.plus(intentArguments ?: this.parameters)

        return NotificationHistory(
            id = if (changeIdAfterCopy) getRandomString(32) else id,
            title = title ?: this.title,
            description = description ?: this.description,
            timestamp = timestamp ?: this.timestamp,
            tag = tag ?: this.tag,
            parameters = map,
            isRead = isRead ?: this.isRead,
            isReceived = isReceived ?: this.isReceived
        )
    }
}