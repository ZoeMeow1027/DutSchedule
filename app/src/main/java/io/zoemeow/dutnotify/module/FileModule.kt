package io.zoemeow.dutnotify.module

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.zoemeow.dutapi.objects.news.NewsGlobalItem
import io.zoemeow.dutapi.objects.news.NewsSubjectItem
import io.zoemeow.dutnotify.model.account.AccountSession
import io.zoemeow.dutnotify.model.appsettings.AppSettings
import io.zoemeow.dutnotify.model.news.NewsCache
import java.io.File

class FileModule(
    context: Context
) {
    private val pathNewsCacheGlobal = "${context.cacheDir.path}/cache_news_global.json"
    private val pathNewsCacheSubject = "${context.cacheDir.path}/cache_news_subject.json"
    private val pathSettings = "${context.filesDir.path}/settings.json"
    private val pathAccountSettings = "${context.filesDir.path}/account.json"

    fun saveCacheNewsGlobal(
        newsCacheGlobal: NewsCache<NewsGlobalItem>
    ) {
        val file = File(pathNewsCacheGlobal)
        file.writeText(Gson().toJson(newsCacheGlobal))
    }

    fun getCacheNewsGlobal(): NewsCache<NewsGlobalItem> {
        val file = File(pathNewsCacheGlobal)
        try {
            file.bufferedReader().apply {
                val text = this.use { it.readText() }
                val newsCacheGlobal = Gson().fromJson<NewsCache<NewsGlobalItem>>(
                    text,
                    (object : TypeToken<NewsCache<NewsGlobalItem>>() {}.type)
                )
                this.close()
                return newsCacheGlobal
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            return NewsCache()
        }
    }

    fun saveCacheNewsSubject(
        newsCacheSubject: NewsCache<NewsSubjectItem>
    ) {
        val file = File(pathNewsCacheSubject)
        file.writeText(Gson().toJson(newsCacheSubject))
    }

    fun getCacheNewsSubject(): NewsCache<NewsSubjectItem> {
        val file = File(pathNewsCacheSubject)
        try {
            file.bufferedReader().apply {
                val text = this.use { it.readText() }
                val newsCacheGlobal = Gson().fromJson<NewsCache<NewsSubjectItem>>(
                    text,
                    (object : TypeToken<NewsCache<NewsSubjectItem>>() {}.type)
                )
                this.close()
                return newsCacheGlobal
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            return NewsCache()
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