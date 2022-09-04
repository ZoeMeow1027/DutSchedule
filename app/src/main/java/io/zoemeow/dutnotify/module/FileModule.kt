package io.zoemeow.dutnotify.module

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.zoemeow.dutnotify.model.account.AccountSession
import io.zoemeow.dutnotify.model.appsettings.AppSettings
import io.zoemeow.dutnotify.model.news.NewsCacheGlobal
import java.io.File

class FileModule(
    context: Context
) {
    private val pathNewsCacheGlobal = "${context.cacheDir.path}/cache_news_global.json"
    private val pathNewsCacheSubject = "${context.cacheDir.path}/cache_news_subject.json"
    private val pathSettings = "${context.filesDir.path}/settings.json"
    private val pathAccountSettings = "${context.filesDir.path}/account.json"

    fun saveCacheNewsGlobal(
        newsCacheGlobal: NewsCacheGlobal
    ) {
        val file = File(pathNewsCacheGlobal)
        file.writeText(Gson().toJson(newsCacheGlobal))
    }

    fun getCacheNewsGlobal(): NewsCacheGlobal {
        val file = File(pathNewsCacheGlobal)
        try {
            file.bufferedReader().apply {
                val text = this.use { it.readText() }
                val newsCacheGlobal = Gson().fromJson<NewsCacheGlobal>(
                    text,
                    (object : TypeToken<NewsCacheGlobal>() {}.type)
                )
                this.close()
                return newsCacheGlobal
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            return NewsCacheGlobal()
        }
    }

    fun saveCacheNewsSubject(
        newsCacheSubject: NewsCacheGlobal
    ) {
        val file = File(pathNewsCacheSubject)
        file.writeText(Gson().toJson(newsCacheSubject))
    }

    fun getCacheNewsSubject(): NewsCacheGlobal {
        val file = File(pathNewsCacheSubject)
        try {
            file.bufferedReader().apply {
                val text = this.use { it.readText() }
                val newsCacheGlobal = Gson().fromJson<NewsCacheGlobal>(
                    text,
                    (object : TypeToken<NewsCacheGlobal>() {}.type)
                )
                this.close()
                return newsCacheGlobal
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            return NewsCacheGlobal()
        }
    }

    fun saveAppSettings(
        appSettings: AppSettings
    ) {
        val file = File(pathSettings)
        file.writeText(Gson().toJson(appSettings))
    }

    fun getAppSettings(): AppSettings {
        val file = File(pathSettings)
        try {
            file.bufferedReader().apply {
                val text = this.use { it.readText() }
                val appSettings =
                    Gson().fromJson<AppSettings>(text, (object : TypeToken<AppSettings>() {}.type))
                this.close()

                if (appSettings.refreshNewsIntervalInMinute < 1)
                    appSettings.refreshNewsIntervalInMinute = 3

                return appSettings
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            return AppSettings()
        }
    }

    fun getAccountSettings(): AccountSession {
        val file = File(pathAccountSettings)
        try {
            file.bufferedReader().apply {
                val text = this.use { it.readText() }
                val accountSession = Gson().fromJson<AccountSession>(
                    text,
                    (object : TypeToken<AccountSession>() {}.type)
                )
                this.close()
                return accountSession
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            return AccountSession()
        }
    }

    fun saveAccountSettings(
        accountSession: AccountSession
    ) {
        val file = File(pathAccountSettings)
        file.writeText(Gson().toJson(accountSession))
    }
}