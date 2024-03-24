package io.zoemeow.dutschedule.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.dutwrapper.dutwrapper.model.accounts.SubjectScheduleItem
import io.dutwrapper.dutwrapper.model.utils.DutSchoolYearItem
import io.zoemeow.dutschedule.model.NotificationHistory
import io.zoemeow.dutschedule.model.account.AccountSession
import io.zoemeow.dutschedule.model.news.NewsGlobalItem
import io.zoemeow.dutschedule.model.news.NewsSearchHistory
import io.zoemeow.dutschedule.model.news.NewsSubjectItem
import io.zoemeow.dutschedule.model.settings.AppSettings
import java.io.File

class FileModuleRepository(
    context: Context
) {
    // context.cacheDir

    private val PATH_CACHE_NEWSGLOBAL = "${context.filesDir.path}/news.global.cache.json"
    private val PATH_CACHE_NEWSSUBJECT = "${context.filesDir.path}/news.subject.cache.json"
    private val PATH_APPSETTINGS = "${context.filesDir.path}/settings.json"
    private val PATH_ACCOUNT = "${context.filesDir.path}/account.json"
    private val PATH_ACCOUNT_SUBJECTSCHEDULE_CACHE = "${context.filesDir.path}/account.subjectschedule.cache.json"
    private val PATH_NOTIFICATION_HISTORY = "${context.filesDir.path}/notification.cache.json"
    private val PATH_HISTORY_NEWSSEARCH = "${context.filesDir.path}/history_news_search.json"
    private val PATH_SCHOOLYEAR_CACHE = "${context.filesDir.path}/schoolyear.cache.json"

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
        newsList: List<NewsGlobalItem>,
        newsNextPage: Int,
        lastRequest: Long = 0
    ) {
        val map = mapOf(
            "data" to Gson().toJson(newsList),
            "nextPage" to newsNextPage.toString(),
            "lastRequest" to lastRequest.toString()
        )
        val file = File(PATH_CACHE_NEWSGLOBAL)
        file.writeText(Gson().toJson(map))
    }

    fun getCacheNewsGlobal(
        onDataExported: (List<NewsGlobalItem>, Int, Long) -> Unit
    ) {
        val file = File(PATH_CACHE_NEWSGLOBAL)
        try {
            file.bufferedReader().apply {
                val text = this.use { it.readText() }
                val objItem = Gson().fromJson<Map<String, String?>>(
                    text,
                    (object : TypeToken<Map<String, String?>>() {}.type)
                )
                this.close()
                onDataExported(
                    Gson().fromJson(
                        objItem["data"],
                        (object : TypeToken<List<NewsGlobalItem>>() {}.type)
                    ),
                    objItem["nextPage"]?.toIntOrNull() ?: 1,
                    objItem["lastRequest"]?.toLongOrNull() ?: 0
                )
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            onDataExported(listOf(), 1, 0)
        }
    }

    fun saveCacheNewsSubject(
        newsList: List<NewsSubjectItem>,
        newsNextPage: Int,
        lastRequest: Long = 0
    ) {
        val map = mapOf(
            "data" to Gson().toJson(newsList),
            "nextPage" to newsNextPage.toString(),
            "lastRequest" to lastRequest.toString()
        )
        val file = File(PATH_CACHE_NEWSSUBJECT)
        file.writeText(Gson().toJson(map))
    }

    fun getCacheNewsSubject(
        onDataExported: (List<NewsSubjectItem>, Int, Long) -> Unit
    ) {
        val file = File(PATH_CACHE_NEWSSUBJECT)
        try {
            file.bufferedReader().apply {
                val text = this.use { it.readText() }
                val objItem = Gson().fromJson<Map<String, String?>>(
                    text,
                    (object : TypeToken<Map<String, String?>>() {}.type)
                )
                this.close()
                onDataExported(
                    Gson().fromJson(
                        objItem["data"],
                        (object : TypeToken<List<NewsSubjectItem>>() {}.type)
                    ),
                    objItem["nextPage"]?.toIntOrNull() ?: 1,
                    objItem["lastRequest"]?.toLongOrNull() ?: 0
                )
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            onDataExported(listOf(), 1, 0)
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

    fun getAccountSubjectScheduleCache(): ArrayList<SubjectScheduleItem> {
        val file = File(PATH_ACCOUNT_SUBJECTSCHEDULE_CACHE)
        try {
            file.bufferedReader().apply {
                val text = this.use { it.readText() }
                val objItem = Gson().fromJson<ArrayList<SubjectScheduleItem>>(
                    text,
                    (object : TypeToken<ArrayList<SubjectScheduleItem>>() {}.type)
                )
                this.close()
                return objItem
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            return ArrayList()
        }
    }

    fun saveAccountSubjectScheduleCache(data: ArrayList<SubjectScheduleItem>) {
        val file = File(PATH_ACCOUNT_SUBJECTSCHEDULE_CACHE)
        file.writeText(Gson().toJson(data))
    }

    fun getSchoolYearCache(): Map<String, String?>? {
        val file = File(PATH_SCHOOLYEAR_CACHE)
        try {
            file.bufferedReader().apply {
                val text = this.use { it.readText() }
                val objItem = Gson().fromJson<Map<String, String?>>(
                    text,
                    (object : TypeToken<Map<String, String?>>() {}.type)
                )
                this.close()
                return objItem
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            return mapOf("data" to null, "lastrequest" to null)
        }
    }

    fun saveSchoolYearCache(data: DutSchoolYearItem?, lastRequest: Long) {
        val file = File(PATH_SCHOOLYEAR_CACHE)
        val dataMap = mapOf("data" to Gson().toJson(data), "lastrequest" to lastRequest)
        file.writeText(Gson().toJson(dataMap))
    }
}