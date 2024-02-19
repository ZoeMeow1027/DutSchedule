package io.zoemeow.dutschedule.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.zoemeow.dutschedule.di.AppModule
import io.zoemeow.dutschedule.service.BaseService
import io.zoemeow.dutschedule.service.NewsUpdateService

class BootCompletedReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                if (context == null) {
                    return
                }

                val fileModule = AppModule.provideFileModuleRepository(context)
                val settings = fileModule.getAppSettings()
                if (settings.newsBackgroundDuration > 0) {
                    // Start service here
                    BaseService.startService(
                        context = context,
                        intent = Intent(context, NewsUpdateService::class.java).also {
                            it.action = "news.service.action.fetchallpage1background"
                        }
                    )
                }
            }
            else -> {}
        }
    }

}