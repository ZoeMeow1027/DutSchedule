package io.zoemeow.dutschedule.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.dutwrapper.dutwrapper.Utils
import io.dutwrapper.dutwrapper.model.accounts.AccountInformation
import io.dutwrapper.dutwrapper.model.accounts.SubjectFeeItem
import io.dutwrapper.dutwrapper.model.accounts.SubjectScheduleItem
import io.dutwrapper.dutwrapper.model.accounts.trainingresult.AccountTrainingStatus
import io.dutwrapper.dutwrapper.model.news.NewsGlobalItem
import io.dutwrapper.dutwrapper.model.news.NewsSubjectItem
import io.dutwrapper.dutwrapper.model.utils.DutSchoolYearItem
import io.zoemeow.dutschedule.model.ProcessState
import io.zoemeow.dutschedule.model.ProcessVariable
import io.zoemeow.dutschedule.model.VariableTimestamp
import io.zoemeow.dutschedule.model.account.AccountAuth
import io.zoemeow.dutschedule.model.account.AccountSession
import io.zoemeow.dutschedule.model.account.SchoolYearItem
import io.zoemeow.dutschedule.model.news.NewsCache
import io.zoemeow.dutschedule.model.news.NewsFetchType
import io.zoemeow.dutschedule.model.news.NewsGroupByDate
import io.zoemeow.dutschedule.model.settings.AppSettings
import io.zoemeow.dutschedule.repository.DutAccountRepository
import io.zoemeow.dutschedule.repository.DutNewsRepository
import io.zoemeow.dutschedule.repository.FileModuleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val fileModuleRepository: FileModuleRepository,
    private val dutAccountRepository: DutAccountRepository
) : ViewModel() {
    val appSettings: MutableState<AppSettings> = mutableStateOf(AppSettings())
    val accountSession: MutableState<VariableTimestamp<AccountSession>> = mutableStateOf(
        VariableTimestamp(data = AccountSession())
    )

    fun accountLogin(
        data: AccountAuth? = null,
        before: (() -> Unit)? = null,
        after: ((Boolean) -> Unit)? = null
    ) {
        // If current process is running, ignore this run.
        if (accountSession.value.processState == ProcessState.Running)
            return

        // Before run
        before?.let { it() }

        // If ProcessState.Successful and last run doesn't last 5 minutes, ignore.
        // Otherwise will continue
        if (!accountSession.value.isSuccessfulRequestExpired()) {
            // After run
            after?.let { it(accountSession.value.processState == ProcessState.Successful) }

            return
        }

        // If data exist, merge it to accountSession
        data?.let {
            accountSession.value = accountSession.value.clone(
                data = accountSession.value.data.clone(
                    accountAuth = AccountAuth(
                        username = it.username,
                        password = it.password
                    )
                )
            )
        }

        accountSession.value = accountSession.value.clone(
            processState = ProcessState.Running
        )
        val response = dutAccountRepository.login(
            accountSession.value.data,
            forceLogin = true,
            onSessionChanged = { sessionId, timestamp ->
                accountSession.value = accountSession.value.clone(
                    data = accountSession.value.data.clone(
                        sessionId = sessionId,
                        sessionLastRequest = timestamp
                    ),
                    lastRequest = timestamp
                )
            }
        )
        when (response) {
            true -> {
                accountSession.value = accountSession.value.clone(
                    processState = ProcessState.Successful
                )
            }

            false -> {
                accountSession.value = accountSession.value.clone(
                    processState = ProcessState.Failed
                )
            }
        }

        // Save settings
        saveSettings()

        // After run
        after?.let { it(accountSession.value.processState == ProcessState.Successful) }
    }

    fun accountLogout(
        after: ((Boolean) -> Unit)? = null,
    ) {
        // If current process is running, ignore this run.
        if (accountSession.value.processState == ProcessState.Running)
            return

        // Delete all account sessions
        accountSession.value = accountSession.value.clone(
            lastRequest = 0,
            processState = ProcessState.NotRunYet,
            data = AccountSession(),
        )

        // Delete data after logout
//        accountInformation.value = VariableTimestamp(
//            data = null
//        )
        accountInformation2.resetToDefault()

        // Save settings
        saveSettings()

        // After run
        after?.let { it(true) }
    }

    val newsGlobal2 = ProcessVariable<NewsCache<NewsGlobalItem>>(
        onRefresh = { baseData, arg ->
            val newsBase = baseData ?: NewsCache<NewsGlobalItem>()
            val fetchType = NewsFetchType.fromValue(Integer.parseInt(arg?.get("newsfetchtype") ?: "1"))

            // Get news from internet
            val newsFromInternet = DutNewsRepository.getNewsGlobal(
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

    val newsSubject2 = ProcessVariable<NewsCache<NewsSubjectItem>>(
        onRefresh = { baseData, arg ->
            val newsBase = baseData ?: NewsCache<NewsSubjectItem>()
            val fetchType = NewsFetchType.fromValue(Integer.parseInt(arg?.get("newsfetchtype") ?: "1"))

            // Get news from internet
            val newsFromInternet = DutNewsRepository.getNewsSubject(
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

    val subjectSchedule2 = ProcessVariable<List<SubjectScheduleItem>>(
        onRefresh = { _, _ ->
            // TODO: Remember change year and semester here!
            return@ProcessVariable dutAccountRepository.getSubjectSchedule(
                accountSession.value.data,
                SchoolYearItem(
                    year = appSettings.value.currentSchoolYear.year,
                    semester = appSettings.value.currentSchoolYear.semester
                )
            )
        },
        onAfterRefresh = { saveSettings() }
    )

    val subjectFee2 = ProcessVariable<List<SubjectFeeItem>>(
        onRefresh = { _, _ ->
            // TODO: Remember change year and semester here!
            return@ProcessVariable dutAccountRepository.getSubjectFee(
                accountSession.value.data,
                SchoolYearItem(
                    year = appSettings.value.currentSchoolYear.year,
                    semester = appSettings.value.currentSchoolYear.semester
                )
            )
        },
        onAfterRefresh = { saveSettings() }
    )

    val accountInformation2 = ProcessVariable<AccountInformation>(
        onRefresh = { _, _ ->
            return@ProcessVariable dutAccountRepository.getAccountInformation(
                accountSession.value.data
            )
        },
        onAfterRefresh = { saveSettings() }
    )

    val accountTrainingStatus2 = ProcessVariable<AccountTrainingStatus>(
        onRefresh = { _, _ ->
            return@ProcessVariable dutAccountRepository.getAccountTrainingStatus(
                accountSession.value.data
            )
        },
        onAfterRefresh = { saveSettings() }
    )

    val currentSchoolWeek2 = ProcessVariable<DutSchoolYearItem?>(
        onRefresh = { _, _ ->
            return@ProcessVariable Utils.getCurrentSchoolWeek()
        }
    )

    fun saveSettings() {
        launchOnScope(
            script = {
                fileModuleRepository.saveAppSettings(appSettings.value)
                fileModuleRepository.saveAccountSession(accountSession.value.data)
            }
        )
    }

    private fun loadNewsCache() {
        launchOnScope(
            script = {
                fileModuleRepository.getCacheNewsGlobal().also {
                    newsGlobal2.data.value = it
                }
                fileModuleRepository.getCacheNewsSubject().also {
                    newsSubject2.data.value = it
                }
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
        accountSession.value = VariableTimestamp(
            lastRequest = 0,
            data = fileModuleRepository.getAccountSession()
        )

        invokeOnCompleted?.let { it() }
    }

    init {
        runOnStartup(
            invokeOnCompleted = {
                loadNewsCache()
                currentSchoolWeek2.refreshData(force = true)
                launchOnScope(script = {
                    newsGlobal2.refreshData(
                        force = true,
                        args = mapOf("newsfetchtype" to NewsFetchType.FirstPage.value.toString())
                    )
                    newsSubject2.refreshData(
                        force = true,
                        args = mapOf("newsfetchtype" to NewsFetchType.FirstPage.value.toString())
                    )
                    accountLogin(after = {
                        if (it) {
                            subjectSchedule2.refreshData()
                            accountInformation2.refreshData()
                        }
                    })
                })
            }
        )
    }
}