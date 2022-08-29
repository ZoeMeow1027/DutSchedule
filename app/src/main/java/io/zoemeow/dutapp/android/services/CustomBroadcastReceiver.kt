package io.zoemeow.dutapp.android.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

abstract class CustomBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        if (intent.action == "NewsReload") {
            newsReloadRequest()
        }
    }

    abstract fun newsReloadRequest()
}