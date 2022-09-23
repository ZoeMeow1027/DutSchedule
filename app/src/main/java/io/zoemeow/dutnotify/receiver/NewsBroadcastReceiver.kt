package io.zoemeow.dutnotify.receiver

import android.content.Context
import android.content.Intent
import io.zoemeow.dutnotify.model.enums.ServiceCode

abstract class NewsBroadcastReceiver: BaseBroadcastReceiver {
    constructor()
    constructor(packageFilter: String): super(packageFilter)

    @Suppress("DEPRECATION")
    override fun onReceiveFilter(context: Context, intent: Intent) {
        intent.getStringExtra(ServiceCode.STATUS).also {
            if (it != null) {
                onStatusReceived(
                    key = intent.action ?: "",
                    value = it,
                )
            }
        }
        intent.getSerializableExtra(ServiceCode.DATA).also {
            if (it != null) {
                onDataReceived(
                    key = intent.action ?: "",
                    data = it
                )
            }
        }
        intent.getStringExtra(ServiceCode.ERRORMESSAGE).also {
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