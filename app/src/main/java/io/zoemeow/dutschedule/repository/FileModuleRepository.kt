package io.zoemeow.dutschedule.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.zoemeow.dutschedule.model.settings.AppSettings
import java.io.File

class FileModuleRepository(
    context: Context
) {
    // context.cacheDir

    private val PATH_CACHE_NEWSGLOBAL = "${context.filesDir.path}/cache_news_global.json"
    private val PATH_CACHE_NEWSSUBJECT = "${context.filesDir.path}/cache_news_subject.json"
    private val PATH_APPSETTINGS = "${context.filesDir.path}/settings.json"
    private val PATH_ACCOUNT = "${context.filesDir.path}/account.json"
    private val PATH_CACHE_ACCOUNT = "${context.filesDir.path}/cache_account.json"

    fun saveAppSettings(
        appSettings: AppSettings
    ) {
        val file = File(PATH_APPSETTINGS)
        file.writeText(Gson().toJson(appSettings))
    }

    fun getAppSettings(): AppSettings {
        val file = File(PATH_APPSETTINGS)
        try {
            file.bufferedReader().apply {
                val text = this.use { it.readText() }
                val appSettings =
                    Gson().fromJson<AppSettings>(text, (object : TypeToken<AppSettings>() {}.type))
                this.close()

                return appSettings
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            return AppSettings()
        }
    }
}