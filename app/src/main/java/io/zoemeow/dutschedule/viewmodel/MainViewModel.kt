package io.zoemeow.dutschedule.viewmodel

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import io.dutwrapper.dutwrapper.Utils
import io.dutwrapper.dutwrapper.model.news.NewsGlobalItem
import io.dutwrapper.dutwrapper.model.news.NewsSubjectItem
import io.dutwrapper.dutwrapper.model.utils.DutSchoolYearItem
import io.zoemeow.dutschedule.model.DUTAccountSession
import io.zoemeow.dutschedule.model.NotificationHistory
import io.zoemeow.dutschedule.model.ProcessVariable
import io.zoemeow.dutschedule.model.account.AccountSession
import io.zoemeow.dutschedule.model.news.NewsCache
import io.zoemeow.dutschedule.model.news.NewsFetchType
import io.zoemeow.dutschedule.model.news.NewsGroupByDate
import io.zoemeow.dutschedule.model.settings.AppSettings
import io.zoemeow.dutschedule.repository.DutRequestRepository
import io.zoemeow.dutschedule.repository.FileModuleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val fileModuleRepository: FileModuleRepository,
    private val dutRequestRepository: DutRequestRepository,
) : ViewModel() {
    val appSettings: MutableState<AppSettings> = mutableStateOf(AppSettings())

    val accountSession: DUTAccountSession = DUTAccountSession(
        dutRequestRepository = dutRequestRepository,
        onEventSent = { eventId ->
            when (eventId) {
                1 -> {
                    Log.d("app", "triggered saved login")
                    saveSettings()
                }
                2, 3, 4, 5 -> {
                    // TODO: Save account cache here!
                    // saveSettings()
                }
            }
        }
    )

    /**
     * Refresh or clear news global.
     *
     * @param newsfetchtype Following NewsFetchType enum class.
     */
    val newsGlobal = ProcessVariable<NewsCache<NewsGlobalItem>>(
        onRefresh = { baseData, arg ->
            val newsBase = baseData ?: NewsCache()
            val fetchType = NewsFetchType.fromValue(Integer.parseInt(arg?.get("newsfetchtype") ?: "1"))

            // Get news from internet
            val newsFromInternet = dutRequestRepository.getNewsGlobal(
                page = when (fetchType) {
                    NewsFetchType.NextPage -> newsBase.pageCurrent
                    NewsFetchType.FirstPage -> 1
                    NewsFetchType.ClearAndFirstPage -> 1
                }
            )

            // If requested, clear cache
            if (fetchType == NewsFetchType.ClearAndFirstPage) {
                newsBase.newsListByDate.clear()
            }

            // Remove duplicate news to new list
            val newsFiltered = arrayListOf<NewsGroupByDate<NewsGlobalItem>>()
            newsFromInternet.forEach { newsItem ->
                val anyMatch = newsBase.newsListByDate.any { newsSourceGroup ->
                    newsSourceGroup.itemList.any { newsSourceItem ->
                        newsSourceItem.date == newsItem.date
                                && newsSourceItem.title == newsItem.title
                                && newsSourceItem.contentString == newsItem.contentString
                    }
                }

                if (!anyMatch) {
                    // Check if date group exist
                    val groupExist =
                        newsFiltered.any { newsGroupTarget -> newsGroupTarget.date == newsItem.date }
                    if (!groupExist) {
                        val newsGroupNew = NewsGroupByDate(
                            date = newsItem.date,
                            itemList = arrayListOf(newsItem)
                        )
                        newsFiltered.add(newsGroupNew)
                    } else {
                        newsFiltered.first { newsGroupTarget -> newsGroupTarget.date == newsItem.date }
                            .add(newsItem)
                    }
                }
            }

            // Add to current cache
            newsFiltered.forEach { newsGroup ->
                var itemIndex = 0
                newsGroup.itemList.forEach { newsItem ->
                    if (newsBase.newsListByDate.any { group -> group.date == newsItem.date }) {
                        if (fetchType == NewsFetchType.FirstPage) {
                            newsBase.newsListByDate.first { group -> group.date == newsItem.date }
                                .itemList.add(itemIndex, newsItem)
                            itemIndex += 1
                        } else {
                            newsBase.newsListByDate.first { group -> group.date == newsItem.date }
                                .itemList.add(newsItem)
                        }
                    } else {
                        val newsGroupNew = NewsGroupByDate(
                            date = newsItem.date,
                            itemList = arrayListOf(newsItem)
                        )
                        newsBase.newsListByDate.add(newsGroupNew)
                    }
                }
            }
            newsBase.newsListByDate.sortByDescending { group -> group.date }

            when (fetchType) {
                NewsFetchType.NextPage -> {
                    newsBase.pageCurrent += 1
                }

                NewsFetchType.FirstPage -> {
                    if (newsBase.pageCurrent <= 1)
                        newsBase.pageCurrent += 1
                }

                NewsFetchType.ClearAndFirstPage -> {
                    newsBase.pageCurrent = 2
                }
            }

            newsBase.lastModifiedDate = System.currentTimeMillis()

            // TODO: Remove here!
            fileModuleRepository.saveCacheNewsGlobal(newsBase)

            return@ProcessVariable newsBase
        },
        onAfterRefresh = {
            // TODO: Save here!
            // fileModuleRepository.saveCacheNewsSubject(newsSubject2)
        }
    )

    /**
     * Refresh or clear news subject.
     *
     * @param newsfetchtype Following NewsFetchType enum class.
     */
    val newsSubject = ProcessVariable<NewsCache<NewsSubjectItem>>(
        onRefresh = { baseData, arg ->
            val newsBase = baseData ?: NewsCache()
            val fetchType = NewsFetchType.fromValue(Integer.parseInt(arg?.get("newsfetchtype") ?: "1"))

            // Get news from internet
            val newsFromInternet = dutRequestRepository.getNewsSubject(
                page = when (fetchType) {
                    NewsFetchType.NextPage -> newsBase.pageCurrent
                    NewsFetchType.FirstPage -> 1
                    NewsFetchType.ClearAndFirstPage -> 1
                }
            )

            // If requested, clear cache
            if (fetchType == NewsFetchType.ClearAndFirstPage) {
                newsBase.newsListByDate.clear()
            }

            // Remove duplicate news to new list
            val newsFiltered = arrayListOf<NewsGroupByDate<NewsSubjectItem>>()
            newsFromInternet.forEach { newsItem ->
                val anyMatch = newsBase.newsListByDate.any { newsSourceGroup ->
                    newsSourceGroup.itemList.any { newsSourceItem ->
                        newsSourceItem.date == newsItem.date
                                && newsSourceItem.title == newsItem.title
                                && newsSourceItem.contentString == newsItem.contentString
                    }
                }

                if (!anyMatch) {
                    // Check if date group exist
                    val groupExist =
                        newsFiltered.any { newsGroupTarget -> newsGroupTarget.date == newsItem.date }
                    if (!groupExist) {
                        val newsGroupNew = NewsGroupByDate(
                            date = newsItem.date,
                            itemList = arrayListOf(newsItem)
                        )
                        newsFiltered.add(newsGroupNew)
                    } else {
                        newsFiltered.first { newsGroupTarget -> newsGroupTarget.date == newsItem.date }
                            .add(newsItem)
                    }
                }
            }

            newsFiltered.forEach { newsGroup ->
                var itemIndex = 0
                newsGroup.itemList.forEach { newsItem ->
                    if (newsBase.newsListByDate.any { group -> group.date == newsItem.date }) {
                        if (fetchType == NewsFetchType.FirstPage) {
                            newsBase.newsListByDate.first { group -> group.date == newsItem.date }
                                .itemList.add(itemIndex, newsItem)
                            itemIndex += 1
                        } else {
                            newsBase.newsListByDate.first { group -> group.date == newsItem.date }
                                .itemList.add(newsItem)
                        }
                    } else {
                        val newsGroupNew = NewsGroupByDate(
                            date = newsItem.date,
                            itemList = arrayListOf(newsItem)
                        )
                        newsBase.newsListByDate.add(newsGroupNew)
                    }
                }
            }
            newsBase.newsListByDate.sortByDescending { group -> group.date }

            when (fetchType) {
                NewsFetchType.NextPage -> {
                    newsBase.pageCurrent += 1
                }

                NewsFetchType.FirstPage -> {
                    if (newsBase.pageCurrent <= 1)
                        newsBase.pageCurrent += 1
                }

                NewsFetchType.ClearAndFirstPage -> {
                    newsBase.pageCurrent = 2
                }
            }

            newsBase.lastModifiedDate = System.currentTimeMillis()

            // TODO: Remove here!
            fileModuleRepository.saveCacheNewsSubject(newsBase)

            return@ProcessVariable newsBase
        },
        onAfterRefresh = {
            // TODO: Save here!
            // fileModuleRepository.saveCacheNewsSubject(newsSubject2)
        }
    )

    /**
     * Get current school week if possible.
     */
    val currentSchoolWeek = ProcessVariable<DutSchoolYearItem?>(
        onRefresh = { _, _ ->
            try {
                return@ProcessVariable Utils.getCurrentSchoolWeek()
            } catch (_: Exception) {
                return@ProcessVariable null
            }
        },
        onAfterRefresh = {
            saveCurrentSchoolWeekCache()
        }
    )

    private fun saveCurrentSchoolWeekCache() {
        fileModuleRepository.saveSchoolYearCache(
            data = currentSchoolWeek.data.value,
            lastRequest = currentSchoolWeek.lastRequest.longValue
        )
    }

    val notificationHistory = mutableStateListOf<NotificationHistory>()

    /**
     * Save all current settings to file in storage.
     */
    fun saveSettings() {
        launchOnScope(
            script = {
                fileModuleRepository.saveAppSettings(appSettings.value)
                fileModuleRepository.saveAccountSession(accountSession.getAccountSession() ?: AccountSession())
                fileModuleRepository.saveAccountSubjectScheduleCache(ArrayList(accountSession.getSubjectScheduleCache()))
            }
        )
    }

    fun reloadNotification() {
        launchOnScope(
            script = {
                notificationHistory.clear()
                notificationHistory.addAll(fileModuleRepository.getNotificationHistory())
            }
        )
    }

    /**
     * Load all cache if possible for offline reading.
     */
    private fun loadCache() {
        launchOnScope(
            script = {
                // Get all news cache
                fileModuleRepository.getCacheNewsGlobal().also {
                    newsGlobal.data.value = it
                }
                fileModuleRepository.getCacheNewsSubject().also {
                    newsSubject.data.value = it
                }

                // Get school year cache
                fileModuleRepository.getSchoolYearCache().also {
                    if (it != null) {
                        try {
                            currentSchoolWeek.data.value = Gson().fromJson(
                                it["data"] ?: "",
                                (object : TypeToken<DutSchoolYearItem?>() {}.type)
                            )
                            currentSchoolWeek.lastRequest.longValue = (it["lastrequest"] ?: "0").toLong()
                        } catch (_: Exception) { }
                    }
                }

                // TODO: Get account subject schedule from cache
//                fileModuleRepository.getAccountSubjectScheduleCache().also {
//                    subjectSchedule.data.value = it
//                }
            }
        )
    }

    private fun launchOnScope(
        script: () -> Unit,
        invokeOnCompleted: ((Throwable?) -> Unit)? = null
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                script()
            }
        }.invokeOnCompletion { thr ->
            invokeOnCompleted?.let { it(thr) }
        }
    }

    private val runOnStartupEnabled = mutableStateOf(true)
    private fun runOnStartup(invokeOnCompleted: (() -> Unit)? = null) {
        if (!runOnStartupEnabled.value)
            return

        runOnStartupEnabled.value = false

        appSettings.value = fileModuleRepository.getAppSettings()
        accountSession.setAccountSession(fileModuleRepository.getAccountSession())
        accountSession.setSchoolYear(schoolYearItem = appSettings.value.currentSchoolYear)

        invokeOnCompleted?.let { it() }
    }

    init {
        runOnStartup(
            invokeOnCompleted = {
                loadCache()
                currentSchoolWeek.refreshData(force = true)
                reloadNotification()
                accountSession.reLogin(force = true)
                launchOnScope(script = {
                    newsGlobal.refreshData(
                        force = true,
                        args = mapOf("newsfetchtype" to NewsFetchType.FirstPage.value.toString())
                    )
                    newsSubject.refreshData(
                        force = true,
                        args = mapOf("newsfetchtype" to NewsFetchType.FirstPage.value.toString())
                    )
                })
            }
        )
    }
}