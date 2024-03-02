package io.zoemeow.dutschedule.model

import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import io.dutwrapper.dutwrapper.model.accounts.AccountInformation
import io.dutwrapper.dutwrapper.model.accounts.SubjectFeeItem
import io.dutwrapper.dutwrapper.model.accounts.SubjectScheduleItem
import io.dutwrapper.dutwrapper.model.accounts.trainingresult.AccountTrainingStatus
import io.zoemeow.dutschedule.model.account.AccountAuth
import io.zoemeow.dutschedule.model.account.AccountSession
import io.zoemeow.dutschedule.model.account.SchoolYearItem
import io.zoemeow.dutschedule.repository.DutRequestRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DUTAccountSession {
    data class VariableState<T>(
        val data: MutableState<T>,
        val lastRequest: MutableLongState = mutableLongStateOf(0),
        val processState: MutableState<ProcessState> = mutableStateOf(ProcessState.NotRunYet)
    ) {
        fun isExpired(): Boolean {
            return (lastRequest.longValue + ProcessVariable.expiredDuration) < System.currentTimeMillis()
        }

        fun isSuccessfulRequestExpired(): Boolean {
            return when (processState.value) {
                ProcessState.Successful -> isExpired()
                else -> true
            }
        }
    }

    data class VariableListState<T>(
        val data: SnapshotStateList<T> = mutableStateListOf(),
        val lastRequest: MutableLongState = mutableLongStateOf(0),
        val processState: MutableState<ProcessState> = mutableStateOf(ProcessState.NotRunYet)
    ) {
        fun isExpired(): Boolean {
            return (lastRequest.longValue + ProcessVariable.expiredDuration) < System.currentTimeMillis()
        }

        fun isSuccessfulRequestExpired(): Boolean {
            return when (processState.value) {
                ProcessState.Successful -> isExpired()
                else -> true
            }
        }
    }

    val accountSession: MutableState<AccountSession?> = mutableStateOf(null)
    val subjectSchedule: VariableListState<SubjectScheduleItem> = VariableListState()
    val subjectFee: VariableListState<SubjectFeeItem> = VariableListState()
    val accountInformation: VariableState<AccountInformation?> = VariableState(data = mutableStateOf(null))
    val accountTrainingStatus: VariableState<AccountTrainingStatus?> = VariableState(data = mutableStateOf(null))

    val dutRequestRepository: DutRequestRepository
    val schoolYear: MutableState<SchoolYearItem>

    constructor(
        accountSession: AccountSession?,
        dutRequestRepository: DutRequestRepository,
        schoolYear: SchoolYearItem
        // TODO: Trigger event here!
    ) {
        this.accountSession.value = accountSession
        this.dutRequestRepository = dutRequestRepository
        this.schoolYear = mutableStateOf(schoolYear)
    }

    private fun launchOnScope(
        script: () -> Unit,
        onCompleted: ((Throwable?) -> Unit)? = null
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                script()
            }
        }.invokeOnCompletion { thr ->
            onCompleted?.let { it(thr) }
        }
    }

    fun login(accountAuth: AccountAuth? = null) {
        // If accountSession is exist, let's re-login.

        // Otherwise, use AccountAuth variable.
    }

    fun logout() {

    }

    fun fetchSubjectSchedule(force: Boolean = false) {
        if (!subjectSchedule.isSuccessfulRequestExpired() && !force) {
            return
        }
        if (subjectSchedule.processState.value == ProcessState.Running) {
            return
        }
        subjectSchedule.processState.value = ProcessState.Running

        launchOnScope(
            script = {
                if (accountSession.value == null) {
                    // TODO: AccountSession null
                    throw Exception("")
                }

                val data = dutRequestRepository.getSubjectSchedule(
                    accountSession.value!!,
                    schoolYear.value
                )

                if (data == null) {
                    // TODO: Exception when no data returned here!
                    throw Exception("")
                } else {
                    subjectSchedule.data.clear()
                    subjectSchedule.data.addAll(data)
                }
            },
            onCompleted = {
                subjectSchedule.processState.value = when {
                    (it != null) -> ProcessState.Failed
                    else -> ProcessState.Successful
                }
            }
        )
    }

    fun fetchSubjectFee(force: Boolean = false) {
        if (!subjectFee.isSuccessfulRequestExpired() && !force) {
            return
        }
        if (subjectFee.processState.value == ProcessState.Running) {
            return
        }
        subjectFee.processState.value = ProcessState.Running

        launchOnScope(
            script = {
                if (accountSession.value == null) {
                    // TODO: AccountSession null
                    throw Exception("")
                }

                val data = dutRequestRepository.getSubjectFee(
                    accountSession.value!!,
                    schoolYear.value
                )

                if (data == null) {
                    // TODO: Exception when no data returned here!
                    throw Exception("")
                } else {
                    subjectFee.data.clear()
                    subjectFee.data.addAll(data)
                }
            },
            onCompleted = {
                subjectFee.processState.value = when {
                    (it != null) -> ProcessState.Failed
                    else -> ProcessState.Successful
                }
            }
        )
    }

    fun fetchAccountInformation(force: Boolean = false) {
        if (!accountInformation.isSuccessfulRequestExpired() && !force) {
            return
        }
        if (accountInformation.processState.value == ProcessState.Running) {
            return
        }
        accountInformation.processState.value = ProcessState.Running

        launchOnScope(
            script = {
                if (accountSession.value == null) {
                    // TODO: AccountSession null
                    throw Exception("")
                }

                val data = dutRequestRepository.getAccountInformation(accountSession.value!!)

                if (data == null) {
                    // TODO: Exception when no data returned here!
                    throw Exception("")
                } else {
                    accountInformation.data.value = data
                }
            },
            onCompleted = {
                accountInformation.processState.value = when {
                    (it != null) -> ProcessState.Failed
                    else -> ProcessState.Successful
                }
            }
        )
    }

    fun fetchAccountTrainingStatus(force: Boolean = false) {
        if (!accountTrainingStatus.isSuccessfulRequestExpired() && !force) {
            return
        }
        if (accountTrainingStatus.processState.value == ProcessState.Running) {
            return
        }
        accountTrainingStatus.processState.value = ProcessState.Running

        launchOnScope(
            script = {
                if (accountSession.value == null) {
                    // TODO: AccountSession null
                    throw Exception("")
                }

                val data = dutRequestRepository.getAccountTrainingStatus(accountSession.value!!)

                if (data == null) {
                    // TODO: Exception when no data returned here!
                    throw Exception("")
                } else {
                    accountTrainingStatus.data.value = data
                }
            },
            onCompleted = {
                accountTrainingStatus.processState.value = when {
                    (it != null) -> ProcessState.Failed
                    else -> ProcessState.Successful
                }
            }
        )
    }
}