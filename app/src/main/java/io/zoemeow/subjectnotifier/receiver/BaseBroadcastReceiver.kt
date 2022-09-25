package io.zoemeow.subjectnotifier.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.zoemeow.subjectnotifier.model.enums.ServiceBroadcastOptions

abstract class BaseBroadcastReceiver : BroadcastReceiver {
    private lateinit var packageFilter: String

    constructor()
    constructor(packageFilter: String) {
        this.packageFilter = packageFilter
    }

    private fun shouldFireOnReceive(
        packageFilter: String?
    ): Boolean {
        return if (!this::packageFilter.isInitialized)
            true
        else if (packageFilter == null)
            false
        else this.packageFilter.lowercase() == packageFilter.lowercase()
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (shouldFireOnReceive(intent.getStringExtra(ServiceBroadcastOptions.SOURCE_COMPONENT)))
            onReceiveFilter(context, intent)
    }

    abstract fun onReceiveFilter(context: Context, intent: Intent)
}