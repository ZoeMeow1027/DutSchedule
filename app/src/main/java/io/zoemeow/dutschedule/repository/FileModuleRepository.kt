package io.zoemeow.dutschedule.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.dutwrapper.dutwrapper.model.news.NewsGlobalItem
import io.dutwrapper.dutwrapper.model.news.NewsSubjectItem
import io.zoemeow.dutschedule.model.NotificationHistory
import io.zoemeow.dutschedule.model.account.AccountSession
import io.zoemeow.dutschedule.model.news.NewsCache
import io.zoemeow.dutschedule.model.news.NewsSearchHistory
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
    private val PATH_NOTIFICATION_HISTORY = "${context.filesDir.path}/notification_history.json"
    private val PATH_HISTORY_NEWSSEARCH = "${context.filesDir.path}/history_news_search.json"

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

    fun saveAccountSession(
        accountSession: AccountSession
    ) {
        val file = File(PATH_ACCOUNT)
        file.writeText(Gson().toJson(accountSession))
    }

    fun getAccountSession(): AccountSession {
        val file = File(PATH_ACCOUNT)
        try {
            file.bufferedReader().apply {
                val text = this.use { it.readText() }
                val accountSession =
                    Gson().fromJson<AccountSession>(text, (object : TypeToken<AccountSession>() {}.type))
                this.close()

                return accountSession
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            return AccountSession()
        }
    }

    fun saveCacheNewsGlobal(
        newsCacheGlobal: NewsCache<NewsGlobalItem>
    ) {
        val file = File(PATH_CACHE_NEWSGLOBAL)
        file.writeText(Gson().toJson(newsCacheGlobal))
    }

    fun getCacheNewsGlobal(): NewsCache<NewsGlobalItem> {
        val file = File(PATH_CACHE_NEWSGLOBAL)
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
        val file = File(PATH_CACHE_NEWSSUBJECT)
        file.writeText(Gson().toJson(newsCacheSubject))
    }

    fun getCacheNewsSubject(): NewsCache<NewsSubjectItem> {
        val file = File(PATH_CACHE_NEWSSUBJECT)
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

    fun getNewsSearchHistory(): ArrayList<NewsSearchHistory> {
        val file = File(PATH_HISTORY_NEWSSEARCH)
        try {
            file.bufferedReader().apply {
                val text = this.use { it.readText() }
                val objItem = Gson().fromJson<ArrayList<NewsSearchHistory>>(
                    text,
                    (object : TypeToken<ArrayList<NewsSearchHistory>>() {}.type)
                )
                this.close()
                return objItem
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            return ArrayList()
        }
    }

    fun saveNewsSearchHistory(data: ArrayList<NewsSearchHistory>) {
        val file = File(PATH_HISTORY_NEWSSEARCH)
        file.writeText(Gson().toJson(data))
    }

    fun getNotificationHistory(): ArrayList<NotificationHistory> {
        val file = File(PATH_NOTIFICATION_HISTORY)
        try {
            file.bufferedReader().apply {
                val text = this.use { it.readText() }
                val objItem = Gson().fromJson<ArrayList<NotificationHistory>>(
                    text,
                    (object : TypeToken<ArrayList<NotificationHistory>>() {}.type)
                )
                this.close()
                return objItem
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            return ArrayList()
        }
    }

    fun saveNotificationHistory(data: ArrayList<NotificationHistory>) {
        val file = File(PATH_NOTIFICATION_HISTORY)
        file.writeText(Gson().toJson(data))
    }
}