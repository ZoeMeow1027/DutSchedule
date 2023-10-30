package io.zoemeow.dutschedule.viewmodel

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.dutwrapperlib.dutwrapper.objects.accounts.AccountInformation
import io.dutwrapperlib.dutwrapper.objects.accounts.SubjectFeeItem
import io.dutwrapperlib.dutwrapper.objects.accounts.SubjectScheduleItem
import io.dutwrapperlib.dutwrapper.objects.news.NewsGlobalItem
import io.dutwrapperlib.dutwrapper.objects.news.NewsSubjectItem
import io.zoemeow.dutschedule.model.ProcessState
import io.zoemeow.dutschedule.model.VariableTimestamp
import io.zoemeow.dutschedule.model.account.AccountAuth
import io.zoemeow.dutschedule.model.account.AccountSession
import io.zoemeow.dutschedule.model.account.SchoolYearItem
import io.zoemeow.dutschedule.model.news.NewsCache
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
    private val dutAccountRepository: DutAccountRepository,
    private val application: Application
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

    val subjectSchedule: MutableState<VariableTimestamp<List<SubjectScheduleItem>?>> = mutableStateOf(
        VariableTimestamp(data = null)
    )
    val subjectFee: MutableState<VariableTimestamp<List<SubjectFeeItem>?>> = mutableStateOf(
        VariableTimestamp(data = null)
    )
    val accountInformation: MutableState<VariableTimestamp<AccountInformation?>> = mutableStateOf(
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
        if (accountSession.value.processState == ProcessState.Successful && !GlobalVariables.isExpired(accountSession.value.timestamp)) {
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

    fun accountGetSubjectSchedule(
        before: (() -> Unit)? = null,
        after: ((Boolean) -> Unit)? = null
    ) {
        // If current process is running, ignore this run.
        if (subjectSchedule.value.processState == ProcessState.Successful && !GlobalVariables.isExpired(subjectSchedule.value.timestamp)) {
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

    fun accountGetSubjectFee(
        before: (() -> Unit)? = null,
        after: ((Boolean) -> Unit)? = null
    ) {
        // If current process is running, ignore this run.
        if (subjectFee.value.processState == ProcessState.Successful && !GlobalVariables.isExpired(subjectFee.value.timestamp)) {
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

    fun accountGetInformation(
        before: (() -> Unit)? = null,
        after: ((Boolean) -> Unit)? = null
    ) {
        // If current process is running, ignore this run.
        if (accountInformation.value.processState == ProcessState.Successful && !GlobalVariables.isExpired(accountInformation.value.timestamp)) {
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

    fun fetchNewsGlobal(
        newsPageType: Int = 0,
        page: Int = 1
    ) {
        launchOnScope(
            script = {
                newsGlobal.value = newsGlobal.value.clone(
                    processState = ProcessState.Running
                )

                // Get news from internet
                val newsFromInternet = DutNewsRepository.getNewsGlobal(
                    page = when (newsPageType) {
                        0 -> newsGlobal.value.data.pageCurrent
                        2 -> page
                        1, 3 -> 1
                        else -> 1
                    }
                )

                // If requested, clear cache
                if (newsPageType == 3) {
                    newsGlobal.value.data.newsListByDate.clear()
                }

                val newsDiff = DutNewsRepository.getNewsGlobalDiff(
                    source = newsGlobal.value.data.newsListByDate,
                    target = newsFromInternet,
                )

                DutNewsRepository.addAndCheckDuplicateNewsGlobal(
                    source = newsGlobal.value.data.newsListByDate,
                    target = newsDiff,
                    addItemToTop = newsPageType != 0
                )

                when (newsPageType) {
                    0 -> {
                        newsGlobal.value.data.pageCurrent += 1
                    }
                    1 -> {
                        if (newsGlobal.value.data.pageCurrent <= 1)
                            newsGlobal.value.data.pageCurrent += 1
                    }
                    3 -> {
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

    fun fetchNewsSubject(
        newsPageType: Int = 0,
        page: Int = 1
    ) {
        launchOnScope(
            script = {
                newsSubject.value = newsSubject.value.clone(
                    processState = ProcessState.Running
                )

                // Get news from internet
                val newsFromInternet = DutNewsRepository.getNewsSubject(
                    page = when (newsPageType) {
                        0 -> newsSubject.value.data.pageCurrent
                        2 -> page
                        1, 3 -> 1
                        else -> 1
                    }
                )

                // If requested, clear cache
                if (newsPageType == 3) {
                    newsSubject.value.data.newsListByDate.clear()
                }

                val newsDiff = DutNewsRepository.getNewsSubjectDiff(
                    source = newsSubject.value.data.newsListByDate,
                    target = newsFromInternet,
                )

                DutNewsRepository.addAndCheckDuplicateNewsSubject(
                    source = newsSubject.value.data.newsListByDate,
                    target = newsDiff,
                    addItemToTop = newsPageType != 0
                )

                when (newsPageType) {
                    0 -> {
                        newsSubject.value.data.pageCurrent += 1
                    }
                    1 -> {
                        if (newsSubject.value.data.pageCurrent <= 1)
                            newsSubject.value.data.pageCurrent += 1
                    }
                    3 -> {
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

    init {
        appSettings.value = fileModuleRepository.getAppSettings()
        accountSession.value = VariableTimestamp(
            timestamp = 0,
            data = fileModuleRepository.getAccountSession()
        )
        loadNewsCache()
    }
}