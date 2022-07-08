package io.zoemeow.dutapp.android.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.zoemeow.dutapi.objects.AccountInformation
import io.zoemeow.dutapi.objects.SubjectFeeItem
import io.zoemeow.dutapi.objects.SubjectScheduleItem
import io.zoemeow.dutapp.android.model.ProcessState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AccountViewModel: ViewModel() {
    companion object {
        private val instance: MutableState<AccountViewModel> = mutableStateOf(AccountViewModel())

        fun getInstance(): AccountViewModel {
            return instance.value
        }

        fun setInstance(accViewModel: AccountViewModel) {
            this.instance.value = accViewModel
        }
    }

    private val accountSession = AccountSession()

    val isLoggedIn: MutableState<Boolean> = mutableStateOf(false)
    val processStateLoggingIn: MutableState<ProcessState> = mutableStateOf(ProcessState.NotRun)

    val subjectScheduleList: SnapshotStateList<SubjectScheduleItem> = mutableStateListOf()
    val processStateSubjectSchedule: MutableState<ProcessState> = mutableStateOf(ProcessState.NotRun)

    val subjectFeeList: SnapshotStateList<SubjectFeeItem> = mutableStateListOf()
    val processStateSubjectFee: MutableState<ProcessState> = mutableStateOf(ProcessState.NotRun)

    val accountInformation: MutableState<AccountInformation?> = mutableStateOf(null)
    val username: MutableState<String> = mutableStateOf(String())
    val processStateAccInfo: MutableState<ProcessState> = mutableStateOf(ProcessState.NotRun)

    private fun checkIsLoggedIn() {
        isLoggedIn.value = accountSession.isLoggedIn()
    }

    fun login(username: String, password: String) {
        if (processStateLoggingIn.value == ProcessState.Running)
            return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                processStateLoggingIn.value = ProcessState.Running

                val result = accountSession.login(username, password)

                if (result) {
                    processStateLoggingIn.value = ProcessState.Successful
                }
                else {
                    processStateLoggingIn.value = ProcessState.Failed
                }
                checkIsLoggedIn()
                getAccountInformation()
            }
            catch (ex: Exception) {
                processStateLoggingIn.value = ProcessState.Failed
            }
        }
    }

    fun logout() {
        if (processStateLoggingIn.value == ProcessState.Running)
            return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                processStateLoggingIn.value = ProcessState.Running

                val result = accountSession.logout()

                processStateLoggingIn.value = if (result) ProcessState.Successful else ProcessState.Failed
                checkIsLoggedIn()
            }
            catch (ex: Exception) {
                processStateLoggingIn.value = ProcessState.Failed
            }
        }
    }

    fun getSubjectSchedule(year: Int = 21, semester: Int = 3) {
        if (processStateSubjectSchedule.value == ProcessState.Running)
            return

        if (!isLoggedIn.value)
            return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                processStateSubjectSchedule.value = ProcessState.Running

                subjectScheduleList.clear()
                subjectScheduleList.addAll(accountSession.getSubjectSchedule(year, semester))

                processStateSubjectSchedule.value = ProcessState.Successful
            }
            catch (ex: Exception) {
                processStateSubjectSchedule.value = ProcessState.Failed
            }
        }
    }

    fun getSubjectFee(year: Int = 21, semester: Int = 3) {
        if (processStateSubjectFee.value == ProcessState.Running)
            return

        if (!isLoggedIn.value)
            return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                processStateSubjectFee.value = ProcessState.Running

                subjectFeeList.clear()
                subjectFeeList.addAll(accountSession.getSubjectFee(year, semester))

                processStateSubjectFee.value = ProcessState.Successful
            }
            catch (ex: Exception) {
                processStateSubjectFee.value = ProcessState.Failed
            }
        }
    }

    fun getAccountInformation() {
        if (processStateAccInfo.value == ProcessState.Running)
            return

        if (!isLoggedIn.value)
            return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                processStateAccInfo.value = ProcessState.Running

                accountInformation.value = accountSession.getAccountInformation()
                username.value = accountInformation.value?.studentId ?: ""

                processStateAccInfo.value = ProcessState.Successful
            }
            catch (ex: Exception) {
                processStateAccInfo.value = ProcessState.Failed
            }
        }
    }
}