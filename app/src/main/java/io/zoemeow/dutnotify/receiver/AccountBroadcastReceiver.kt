package io.zoemeow.dutnotify.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.zoemeow.dutnotify.model.enums.AccountServiceCode

abstract class AccountBroadcastReceiver : BroadcastReceiver {
    // this.componentName.className
    // 'class'::class.java.name
    private lateinit var packageFilter: String

    constructor() { }

    constructor(packageFilter: String) {
        this.packageFilter = packageFilter
    }

    @Suppress("DEPRECATION")
    override fun onReceive(context: Context, intent: Intent) {
        val callFrom = intent.getStringExtra(AccountServiceCode.SOURCE_COMPONENT)
        if (callFrom == null) {
            return
        } else if (callFrom.lowercase() != packageFilter.lowercase()) {
            return
        }

        intent.getStringExtra(AccountServiceCode.STATUS).also {
            if (it != null) {
                onStatusReceived(
                    key = intent.action ?: "",
                    value = it,
                )
            }
        }
        intent.getSerializableExtra(AccountServiceCode.DATA).also {
            if (it != null) {
                onDataReceived(
                    key = intent.action ?: "",
                    data = it
                )
            }
        }
        intent.getStringExtra(AccountServiceCode.ERRORMESSAGE).also {
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