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
import io.dutwrapper.dutwrapper.model.utils.DutSchoolYearItem
import io.zoemeow.dutschedule.model.DUTAccountInstance
import io.zoemeow.dutschedule.model.DUTNewsInstance
import io.zoemeow.dutschedule.model.NotificationHistory
import io.zoemeow.dutschedule.model.ProcessVariable
import io.zoemeow.dutschedule.model.account.AccountSession
import io.zoemeow.dutschedule.model.news.NewsFetchType
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

    val accountSession: DUTAccountInstance = DUTAccountInstance(
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

    val newsInstance: DUTNewsInstance = DUTNewsInstance(
        dutRequestRepository = dutRequestRepository,
        onEventSent = {eventId ->
            when (eventId) {
                1 -> {
                    Log.d("app", "triggered saved news")
                    saveSettings()
                }
            }
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
    fun saveSettings(saveSettingsOnly: Boolean = false) {
        launchOnScope(
            script = {
                fileModuleRepository.saveAppSettings(appSettings.value)
                fileModuleRepository.saveAccountSession(accountSession.getAccountSession() ?: AccountSession())

                if (!saveSettingsOnly) {
                    fileModuleRepository.saveAccountSubjectScheduleCache(ArrayList(accountSession.getSubjectScheduleCache()))
                    fileModuleRepository.saveNotificationHistory(ArrayList(notificationHistory.toList()))
                }
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
                fileModuleRepository.getCacheNewsGlobal { newsGlobalItems, i, lq ->
                    newsInstance.loadNewsCache(newsGlobalItems, i, lq, null, null, null)
                }
                fileModuleRepository.getCacheNewsSubject { newsSubjectItems, i, lq ->
                    newsInstance.loadNewsCache(null, null, null, newsSubjectItems, i, lq)
                }

                fileModuleRepository.getAccountSubjectScheduleCache().also {
                    accountSession.setSubjectScheduleCache(it)
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
                    newsInstance.fetchGlobalNews(
                        fetchType = NewsFetchType.FirstPage,
                        forceRequest = true
                    )
                    newsInstance.fetchSubjectNews(
                        fetchType = NewsFetchType.FirstPage,
                        forceRequest = true
                    )
                })
            }
        )
    }
}