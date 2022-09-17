package io.zoemeow.dutnotify.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

abstract class AccountBroadcastReceiver: BroadcastReceiver() {
    @Suppress("SpellCheckingInspection")
    companion object {
        const val STATUS_LOGIN = "status.login"
        const val STATUS_SUBJECTSCHEDULE = "status.subjectschedule"
        const val STATUS_SUBJECTFEE = "status.subjectfee"
        const val STATUS_ACCOUNTINFORMATION = "status.accountinformation"

        // Optimal
        const val DATATYPE_STATUS = "datatype.statustype"
        const val DATATYPE_DATA = "datatype.data"
        const val DATATYPE_MSG_ERROR = "datatype.msg.error"

        // Required
        const val STATUSTYPE_UNKNOWN = "statustype.unknown"
        const val STATUSTYPE_PROCESSING = "statustype.processing"
        const val STATUSTYPE_ALREADYPROCESSING = "statustype.alreadyprocessing"
        const val STATUSTYPE_SUCCESSFUL = "statustype.successful"
        const val STATUSTYPE_FAILED = "statustype.failed"
    }

    @Suppress("DEPRECATION")
    override fun onReceive(context: Context, intent: Intent) {
        intent.getStringExtra(DATATYPE_STATUS).also {
            if (it != null) {
                onStatusReceived(
                    key = intent.action ?: "",
                    value = it
                )
            }
        }
        intent.getSerializableExtra(DATATYPE_DATA).also {
            if (it != null) {
                onDataReceived(
                    key = intent.action ?: "",
                    data = it
                )
            }
        }
        intent.getStringExtra(DATATYPE_MSG_ERROR).also {
            if (it != null) {
                onErrorReceived(
                    key = intent.action ?: "",
                    msg = it
                )
            }
        }
    }

    abstract fun onStatusReceived(key: String, value: String)
    abstract fun onDataReceived(key: String, data: Any)
    abstract fun onErrorReceived(key: String, msg: String)
}