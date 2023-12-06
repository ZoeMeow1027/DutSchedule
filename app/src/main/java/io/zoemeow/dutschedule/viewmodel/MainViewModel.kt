package io.zoemeow.dutschedule.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.dutwrapper.dutwrapper.model.accounts.AccountInformation
import io.dutwrapper.dutwrapper.model.accounts.SubjectFeeItem
import io.dutwrapper.dutwrapper.model.accounts.SubjectScheduleItem
import io.dutwrapper.dutwrapper.model.accounts.trainingresult.AccountTrainingStatus
import io.dutwrapper.dutwrapper.model.news.NewsGlobalItem
import io.dutwrapper.dutwrapper.model.news.NewsSubjectItem
import io.zoemeow.dutschedule.model.ProcessState
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
import io.zoemeow.dutschedule.util.GlobalVariables
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

    val newsGlobal: MutableState<VariableTimestamp<NewsCache<NewsGlobalItem>>> = mutableStateOf(
        VariableTimestamp(data = NewsCache())
    )
    val newsSubject: MutableState<VariableTimestamp<NewsCache<NewsSubjectItem>>> = mutableStateOf(
        VariableTimestamp(data = NewsCache())
    )

    val subjectSchedule: MutableState<VariableTimestamp<List<SubjectScheduleItem>?>> =
        mutableStateOf(
            VariableTimestamp(data = null)
        )
    val subjectFee: MutableState<VariableTimestamp<List<SubjectFeeItem>?>> = mutableStateOf(
        VariableTimestamp(data = null)
    )
    val accountInformation: MutableState<VariableTimestamp<AccountInformation?>> = mutableStateOf(
        VariableTimestamp(data = null)
    )
    val accountTrainingStatus: MutableState<VariableTimestamp<AccountTrainingStatus?>> =
        mutableStateOf(
            VariableTimestamp(data = null)
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
        if (accountSession.value.processState == ProcessState.Successful && !GlobalVariables.isExpired(
                accountSession.value.timestamp
            )
        ) {
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
                    timestamp = timestamp
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
            timestamp = 0,
            processState = ProcessState.NotRunYet,
            data = AccountSession(),
        )

        // Delete data after logout
        accountInformation.value = VariableTimestamp(
            data = null
        )

        // Save settings
        saveSettings()

        // After run
        after?.let { it(true) }
    }

    fun fetchAccountSubjectSchedule(
        before: (() -> Unit)? = null,
        after: ((Boolean) -> Unit)? = null,
        force: Boolean = false
    ) {
        // If current process is running, ignore this run.
        if (subjectSchedule.value.processState == ProcessState.Successful && !GlobalVariables.isExpired(
                subjectSchedule.value.timestamp
            ) && !force
        ) {
            // After run
            after?.let { it(true) }

            return
        }

        // Before run
        before?.let { it() }

        subjectSchedule.value = subjectSchedule.value.clone(
            processState = ProcessState.Running
        )

        // Get data
        val response = dutAccountRepository.getSubjectSchedule(
            accountSession.value.data,
            SchoolYearItem(year = 22, semester = 1)
        )
        subjectSchedule.value = subjectSchedule.value.clone(
            data = response,
            processState = if (response != null) ProcessState.Successful else ProcessState.Failed,
            timestamp = GlobalVariables.currentTimestampInMilliseconds()
        )

        // Save settings
        saveSettings()

        // After run
        after?.let { it(response != null) }
    }

    fun fetchAccountSubjectFee(
        before: (() -> Unit)? = null,
        after: ((Boolean) -> Unit)? = null,
        force: Boolean = false
    ) {
        // If current process is running, ignore this run.
        if (subjectFee.value.processState == ProcessState.Successful && !GlobalVariables.isExpired(
                subjectFee.value.timestamp
            ) && !force
        ) {
            // After run
            after?.let { it(true) }

            return
        }

        // Before run
        before?.let { it() }

        subjectFee.value = subjectFee.value.clone(
            processState = ProcessState.Running
        )

        // Get data
        val response = dutAccountRepository.getSubjectFee(
            accountSession.value.data,
            SchoolYearItem(year = 22, semester = 1)
        )
        subjectFee.value = subjectFee.value.clone(
            data = response,
            processState = if (response != null) ProcessState.Successful else ProcessState.Failed,
            timestamp = GlobalVariables.currentTimestampInMilliseconds()
        )

        // Save settings
        saveSettings()

        // After run
        after?.let { it(response != null) }
    }

    fun fetchAccountInformation(
        before: (() -> Unit)? = null,
        after: ((Boolean) -> Unit)? = null,
        force: Boolean = false
    ) {
        // If current process is running, ignore this run.
        if (accountInformation.value.processState == ProcessState.Successful && !GlobalVariables.isExpired(
                accountInformation.value.timestamp
            ) && !force
        ) {
            // After run
            after?.let { it(true) }

            return
        }

        // Before run
        before?.let { it() }

        accountInformation.value = accountInformation.value.clone(
            processState = ProcessState.Running
        )

        // Get data
        val response = dutAccountRepository.getAccountInformation(
            accountSession.value.data,
        )
        accountInformation.value = accountInformation.value.clone(
            data = response,
            processState = if (response != null) ProcessState.Successful else ProcessState.Failed,
            timestamp = GlobalVariables.currentTimestampInMilliseconds()
        )

        // Save settings
        saveSettings()

        // After run
        after?.let { it(response != null) }
    }

    fun fetchNewsGlobal(fetchType: NewsFetchType = NewsFetchType.NextPage) {
        launchOnScope(
            script = {
                newsGlobal.value = newsGlobal.value.clone(
                    processState = ProcessState.Running
                )

                // Get news from internet
                val newsFromInternet = DutNewsRepository.getNewsGlobal(
                    page = when (fetchType) {
                        NewsFetchType.NextPage -> newsGlobal.value.data.pageCurrent
                        NewsFetchType.FirstPage -> 1
                        NewsFetchType.ClearAndFirstPage -> 1
                    }
                )

                // If requested, clear cache
                if (fetchType == NewsFetchType.ClearAndFirstPage) {
                    newsGlobal.value.data.newsListByDate.clear()
                }

                // Remove duplicate news to new list
                val newsFiltered = arrayListOf<NewsGroupByDate<NewsGlobalItem>>()
                newsFromInternet.forEach { newsItem ->
                    val anyMatch = newsGlobal.value.data.newsListByDate.any { newsSourceGroup ->
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
                        if (newsGlobal.value.data.newsListByDate.any { group -> group.date == newsItem.date }) {
                            if (fetchType == NewsFetchType.FirstPage) {
                                newsGlobal.value.data.newsListByDate.first { group -> group.date == newsItem.date }
                                    .itemList.add(itemIndex, newsItem)
                                itemIndex += 1
                            } else {
                                newsGlobal.value.data.newsListByDate.first { group -> group.date == newsItem.date }
                                    .itemList.add(newsItem)
                            }
                        } else {
                            val newsGroupNew = NewsGroupByDate(
                                date = newsItem.date,
                                itemList = arrayListOf(newsItem)
                            )
                            newsGlobal.value.data.newsListByDate.add(newsGroupNew)
                        }
                    }
                }
                newsGlobal.value.data.newsListByDate.sortByDescending { group -> group.date }

                when (fetchType) {
                    NewsFetchType.NextPage -> {
                        newsGlobal.value.data.pageCurrent += 1
                    }

                    NewsFetchType.FirstPage -> {
                        if (newsGlobal.value.data.pageCurrent <= 1)
                            newsGlobal.value.data.pageCurrent += 1
                    }

                    NewsFetchType.ClearAndFirstPage -> {
                        newsGlobal.value.data.pageCurrent = 2
                    }
                }
                fileModuleRepository.saveCacheNewsGlobal(newsGlobal.value.data)
            },
            invokeOnCompleted = {
                newsGlobal.value = newsGlobal.value.clone(
                    processState = if (it != null) ProcessState.Successful else ProcessState.Failed
                )
            }
        )
    }

    fun fetchNewsSubject(fetchType: NewsFetchType = NewsFetchType.NextPage) {
        launchOnScope(
            script = {
                newsSubject.value = newsSubject.value.clone(
                    processState = ProcessState.Running
                )

                // Get news from internet
                val newsFromInternet = DutNewsRepository.getNewsSubject(
                    page = when (fetchType) {
                        NewsFetchType.NextPage -> newsSubject.value.data.pageCurrent
                        NewsFetchType.FirstPage -> 1
                        NewsFetchType.ClearAndFirstPage -> 1
                    }
                )

                // If requested, clear cache
                if (fetchType == NewsFetchType.ClearAndFirstPage) {
                    newsSubject.value.data.newsListByDate.clear()
                }

                // Remove duplicate news to new list
                val newsFiltered = arrayListOf<NewsGroupByDate<NewsSubjectItem>>()
                newsFromInternet.forEach { newsItem ->
                    val anyMatch = newsSubject.value.data.newsListByDate.any { newsSourceGroup ->
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
                        if (newsSubject.value.data.newsListByDate.any { group -> group.date == newsItem.date }) {
                            if (fetchType == NewsFetchType.FirstPage) {
                                newsSubject.value.data.newsListByDate.first { group -> group.date == newsItem.date }
                                    .itemList.add(itemIndex, newsItem)
                                itemIndex += 1
                            } else {
                                newsSubject.value.data.newsListByDate.first { group -> group.date == newsItem.date }
                                    .itemList.add(newsItem)
                            }
                        } else {
                            val newsGroupNew = NewsGroupByDate(
                                date = newsItem.date,
                                itemList = arrayListOf(newsItem)
                            )
                            newsSubject.value.data.newsListByDate.add(newsGroupNew)
                        }
                    }
                }
                newsSubject.value.data.newsListByDate.sortByDescending { group -> group.date }

                when (fetchType) {
                    NewsFetchType.NextPage -> {
                        newsSubject.value.data.pageCurrent += 1
                    }

                    NewsFetchType.FirstPage -> {
                        if (newsSubject.value.data.pageCurrent <= 1)
                            newsSubject.value.data.pageCurrent += 1
                    }

                    NewsFetchType.ClearAndFirstPage -> {
                        newsSubject.value.data.pageCurrent = 2
                    }
                }
                fileModuleRepository.saveCacheNewsSubject(newsSubject.value.data)
            },
            invokeOnCompleted = {
                newsSubject.value = newsSubject.value.clone(
                    processState = if (it != null) ProcessState.Successful else ProcessState.Failed
                )
            }
        )
    }

    fun fetchAccountTrainingStatus(
        before: (() -> Unit)? = null,
        after: ((Boolean) -> Unit)? = null,
        force: Boolean = false
    ) {
        // If current process is running, ignore this run.
        if (accountTrainingStatus.value.processState == ProcessState.Successful && !GlobalVariables.isExpired(
                accountTrainingStatus.value.timestamp
            ) && !force
        ) {
            // After run
            after?.let { it(true) }

            return
        }

        // Before run
        before?.let { it() }

        accountTrainingStatus.value = accountTrainingStatus.value.clone(
            processState = ProcessState.Running
        )

        // Get data
        val response = dutAccountRepository.getAccountTrainingStatus(
            accountSession.value.data
        )
        accountTrainingStatus.value = accountTrainingStatus.value.clone(
            data = response,
            processState = if (response != null) ProcessState.Successful else ProcessState.Failed,
            timestamp = GlobalVariables.currentTimestampInMilliseconds()
        )

        // Save settings
        saveSettings()

        // After run
        after?.let { it(response != null) }
    }

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
                    newsGlobal.value = newsGlobal.value.clone(
                        data = it
                    )
                }
                fileModuleRepository.getCacheNewsSubject().also {
                    newsSubject.value = newsSubject.value.clone(
                        data = it
                    )
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
            timestamp = 0,
            data = fileModuleRepository.getAccountSession()
        )

        invokeOnCompleted?.let { it() }
    }

    init {
        runOnStartup(
            invokeOnCompleted = {
                loadNewsCache()
                launchOnScope(script = {
                    fetchNewsGlobal(fetchType = NewsFetchType.FirstPage)
                    fetchNewsSubject(fetchType = NewsFetchType.FirstPage)
                    accountLogin(after = {
                        if (it) {
                            fetchAccountInformation()
                        }
                    })
                })
            }
        )
    }
}