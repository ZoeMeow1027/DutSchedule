package io.zoemeow.dutschedule.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import io.zoemeow.dutschedule.R
import io.zoemeow.dutschedule.model.ProcessState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BaseService(
    private val nNotifyId: String,
    private val nTitle: String,
    private val nContent: String
) : Service() {
    override fun onCreate() {
        setNotificationOnForeground(nTitle, nContent)
        onInitialize()
        super.onCreate()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                doWorkBackground(intent)
            }
        }.invokeOnCompletion {
            it?.printStackTrace()
            onCompleted(if (it != null) ProcessState.Successful else ProcessState.Failed)
        }

        return START_STICKY
    }

    override fun onDestroy() {
        onDestroying()
        super.onDestroy()
    }

    private fun setNotificationOnForeground(title: String, description: String) {
        val builder = NotificationCompat.Builder(this, nNotifyId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOnlyAlertOnce(true)
            .setContentTitle(title)
            .setContentText(description)
        startForeground(1, builder.build())
    }

    abstract fun onInitialize()

    abstract fun doWorkBackground(intent: Intent?)

    abstract fun onCompleted(result: ProcessState)

    abstract fun onDestroying()

    companion object {
        fun startService(
            context: Context,
            intent: Intent
        ) {
            // https://stackoverflow.com/a/47654126
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                context.startForegroundService(intent)
            else context.startService(intent)
        }
    }
}