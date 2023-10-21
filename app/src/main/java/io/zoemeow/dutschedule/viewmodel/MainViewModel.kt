package io.zoemeow.dutschedule.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import io.zoemeow.dutschedule.model.news.NewsGroupByDate
import io.zoemeow.dutschedule.model.settings.AppSettings
import io.zoemeow.dutschedule.repository.DutAccountRepository
import io.zoemeow.dutschedule.repository.FileModuleRepository
import io.zoemeow.dutschedule.utils.GlobalVariable
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val fileModuleRepository: FileModuleRepository,
    private val dutAccountRepository: DutAccountRepository,
) : ViewModel() {
    val appSettings: MutableState<AppSettings> = mutableStateOf(AppSettings())
    val accountSession: MutableState<VariableTimestamp<AccountSession>> = mutableStateOf(
        VariableTimestamp(data = AccountSession())
    )

    val newsGlobal: MutableState<VariableTimestamp<List<NewsGroupByDate<NewsGlobalItem>>?>> = mutableStateOf(
        VariableTimestamp(data = null)
    )
    val newsSubject: MutableState<VariableTimestamp<List<NewsGroupByDate<NewsSubjectItem>>?>> = mutableStateOf(
        VariableTimestamp(data = null)
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
        if (accountSession.value.processState == ProcessState.Successful && !GlobalVariable.isExpired(accountSession.value.timestamp)) {
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
        if (subjectSchedule.value.processState == ProcessState.Successful && !GlobalVariable.isExpired(subjectSchedule.value.timestamp)) {
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
            timestamp = GlobalVariable.currentTimestampInMilliseconds()
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
        if (subjectFee.value.processState == ProcessState.Successful && !GlobalVariable.isExpired(subjectFee.value.timestamp)) {
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
            timestamp = GlobalVariable.currentTimestampInMilliseconds()
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
        if (accountInformation.value.processState == ProcessState.Successful && !GlobalVariable.isExpired(accountInformation.value.timestamp)) {
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
            timestamp = GlobalVariable.currentTimestampInMilliseconds()
        )

        // Save settings
        saveSettings()

        // After run
        after?.let { it(response != null) }
    }

    fun saveSettings() {
        viewModelScope.launch {
            fileModuleRepository.saveAppSettings(appSettings.value)
            fileModuleRepository.saveAccountSession(accountSession.value.data)
        }
    }

    init {
        viewModelScope.launch {
            appSettings.value = fileModuleRepository.getAppSettings()
            accountSession.value = VariableTimestamp(
                timestamp = 0,
                data = fileModuleRepository.getAccountSession()
            )
        }
    }
}