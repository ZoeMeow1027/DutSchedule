package io.zoemeow.dutschedule.model

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import io.dutwrapper.dutwrapper.model.accounts.AccountInformation
import io.dutwrapper.dutwrapper.model.accounts.SubjectFeeItem
import io.dutwrapper.dutwrapper.model.accounts.SubjectScheduleItem
import io.dutwrapper.dutwrapper.model.accounts.trainingresult.AccountTrainingStatus
import io.zoemeow.dutschedule.model.account.AccountAuth
import io.zoemeow.dutschedule.model.account.AccountSession
import io.zoemeow.dutschedule.model.account.SchoolYearItem
import io.zoemeow.dutschedule.repository.DutRequestRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @param onEventSent Event when done:
 * 1: Login/Logout
 * 2: Subject schedule
 * 3: Subject fee
 * 4: Account information
 * 5: Account training status
 */
class DUTAccountInstance(
    private val dutRequestRepository: DutRequestRepository,
    private val onEventSent: ((Int) -> Unit)? = null
) {
    val accountSession: VariableState<AccountSession> = VariableState(data = mutableStateOf(null))
    val schoolYear: MutableState<SchoolYearItem?> = mutableStateOf(null)
    val subjectSchedule: VariableListState<SubjectScheduleItem> = VariableListState()
    val subjectFee: VariableListState<SubjectFeeItem> = VariableListState()
    val accountInformation: VariableState<AccountInformation> = VariableState(data = mutableStateOf(null))
    val accountTrainingStatus: VariableState<AccountTrainingStatus> = VariableState(data = mutableStateOf(null))

    private fun launchOnScope(
        script: () -> Unit,
        onCompleted: ((Throwable?) -> Unit)? = null
    ) {
        val handler = CoroutineExceptionHandler { _, throwable ->
            onCompleted?.let { it(throwable) }
        }

        CoroutineScope(Dispatchers.Main).launch(handler) {
            withContext(Dispatchers.IO) {
                script()
            }
        }.invokeOnCompletion { thr ->
            onCompleted?.let { it(thr) }
        }
    }

    private fun checkVariable(): Boolean {
        return when {
            this.accountSession.data.value == null -> false
            this.schoolYear.value == null -> false
            else -> true
        }
    }

    fun getAccountSession(): AccountSession? {
        return this.accountSession.data.value
    }

    fun setAccountSession(accountSession: AccountSession) {
        if (this.accountSession.processState.value == ProcessState.Running) {
            return
        }

        this.accountSession.data.value = accountSession.clone()
    }

    fun setSubjectScheduleCache(data: List<SubjectScheduleItem>) {
        this.subjectSchedule.data.clear()
        this.subjectSchedule.data.addAll(data)
    }

    fun getSubjectScheduleCache(): List<SubjectScheduleItem> {
        return this.subjectSchedule.data.toList()
    }

    fun setSchoolYear(schoolYearItem: SchoolYearItem) {
        this.schoolYear.value = schoolYearItem
    }

    /**
     * Login account to this application.
     * @param accountAuth Your login.
     * @param onCompleted Return a bool value which this request has done correctly.
     */
    fun login(
        accountAuth: AccountAuth? = null,
        force: Boolean = true,
        onCompleted: ((Boolean) -> Unit)? = null
    ) {
        if (accountSession.processState.value == ProcessState.Running) {
            return
        }
        accountSession.processState.value = ProcessState.Running

        launchOnScope(
            script = {
                // If accountAuth isn't null, just login with new account
                if (accountAuth != null) {
                    Log.d("login", "new account")
                    accountAuth.let { d1 ->
                        accountSession.data.value?.accountAuth.let { d2 ->
                            if (d1.username == d2?.username && d1.password == d2?.password) {
                                dutRequestRepository.login(
                                    accountSession = accountSession.data.value!!,
                                    forceLogin = false,
                                    onSessionChanged = { sId, dateUnix ->
                                        if (dateUnix == null || dateUnix == 0L || sId == null) {
                                            // TODO: Account session isn't valid!
                                            accountSession.data.value = null
                                            throw Exception()
                                        } else {
                                            accountSession.data.value = accountSession.data.value!!.clone(
                                                accountAuth = accountSession.data.value!!.accountAuth,
                                                sessionId = sId,
                                                sessionLastRequest = dateUnix
                                            )
                                        }
                                    }
                                )
                            } else {
                                accountSession.data.value = AccountSession(
                                    accountAuth = accountAuth.clone()
                                )
                                dutRequestRepository.login(
                                    accountSession = accountSession.data.value!!,
                                    forceLogin = true,
                                    onSessionChanged = { sId, dateUnix ->
                                        if (dateUnix == null || dateUnix == 0L || sId == null) {
                                            // TODO: Account session isn't valid!
                                            throw Exception()
                                        } else {
                                            accountSession.data.value = accountSession.data.value!!.clone(
                                                accountAuth = accountSession.data.value!!.accountAuth,
                                                sessionId = sId,
                                                sessionLastRequest = dateUnix
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                // If accountSession is exist, let's re-login.
                else if (accountSession.data.value != null) {
                    Log.d("login", "have account")
                    // Check if logged in
                    // If so, return to accountSession
                    if (!accountSession.data.value!!.isValidLogin()) {
                        throw Exception()
                    }
                    dutRequestRepository.login(
                        accountSession = accountSession.data.value!!,
                        forceLogin = force,
                        onSessionChanged = { sId, dateUnix ->
                            if (dateUnix == null || dateUnix == 0L || sId == null) {
                                // TODO: Account session isn't valid!
                                throw Exception()
                            } else {
                                accountSession.data.value = accountSession.data.value!!.clone(
                                    accountAuth = accountSession.data.value!!.accountAuth,
                                    sessionId = sId,
                                    sessionLastRequest = dateUnix
                                )
                            }
                        }
                    )
                }
                // Otherwise, throw exception here
                else {
                    // TODO: Account auth isn't valid!
                    Log.d("login", "no accounts")
                    throw Exception()
                }
            },
            onCompleted = {
                // TODO: Throwable here
                Log.d("login", "done login")
                it?.printStackTrace()
                accountSession.processState.value = when {
                    it == null -> ProcessState.Successful
                    accountSession.data.value != null -> when {
                        accountSession.data.value!!.accountAuth.isValidLogin() -> ProcessState.Failed
                        else -> ProcessState.NotRunYet
                    }
                    else -> ProcessState.NotRunYet
                }
                onCompleted?.let { it2 -> it2(it == null) }
                onEventSent?.let { it(1) }
            }
        )
    }

    /**
     * Re-login your account on sv.dut.udn.vn
     */
    fun reLogin(
        force: Boolean = false,
        onCompleted: ((Boolean) -> Unit)? = null
    ) {
        if (accountSession.processState.value == ProcessState.Running) {
            return
        }

        login(
            force = force,
            onCompleted = {
                if (it && accountSession.processState.value == ProcessState.Successful) {
                    fetchAccountInformation()
                    fetchSubjectSchedule()
                }
                onCompleted?.let { it2 -> it2(it) }
            }
        )
    }

    /**
     * Logout account in this application from sv.dut.udn.vn server.
     */
    fun logout(
        onCompleted: ((Boolean) -> Unit)? = null
    ) {
        if (accountSession.processState.value == ProcessState.Running) {
            return
        }
        accountSession.processState.value = ProcessState.Running

        launchOnScope(
            script = {
                // TODO: Fully logout from server
                var temp = AccountSession()
                accountSession.processState.value = ProcessState.Successful
                accountSession.data.value?.let {
                    temp = it.clone()
                }
                accountSession.resetValue()
                subjectSchedule.resetValue()
                subjectFee.resetValue()
                accountInformation.resetValue()
                accountTrainingStatus.resetValue()

                CoroutineScope(Dispatchers.Main).launch {
                    withContext(Dispatchers.IO) {
                        dutRequestRepository.logout(temp)
                    }
                }
            },
            onCompleted = { throwable ->
                accountSession.processState.value = ProcessState.NotRunYet
                onEventSent?.let { it(1) }
                onCompleted?.let { it(throwable != null) }
            }
        )
    }

    fun fetchSubjectSchedule(force: Boolean = false) {
        if (!subjectSchedule.isSuccessfulRequestExpired() && !force) {
            return
        }
        if (!checkVariable()) {
            return
        }
        if (subjectSchedule.processState.value == ProcessState.Running) {
            return
        }
        subjectSchedule.processState.value = ProcessState.Running

        launchOnScope(
            script = {
                if (accountSession.data.value == null) {
                    // TODO: AccountSession null
                    throw Exception("")
                }

                val data = dutRequestRepository.getSubjectSchedule(
                    accountSession.data.value!!,
                    schoolYear.value!!
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
                subjectSchedule.lastRequest.longValue = System.currentTimeMillis()
                onEventSent?.let { it(2) }
            }
        )
    }

    fun fetchSubjectFee(force: Boolean = false) {
        if (!subjectFee.isSuccessfulRequestExpired() && !force) {
            return
        }
        if (!checkVariable()) {
            return
        }
        if (subjectFee.processState.value == ProcessState.Running) {
            return
        }
        subjectFee.processState.value = ProcessState.Running

        launchOnScope(
            script = {
                if (accountSession.data.value == null) {
                    // TODO: AccountSession null
                    throw Exception("")
                }

                val data = dutRequestRepository.getSubjectFee(
                    accountSession.data.value!!,
                    schoolYear.value!!
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
                subjectFee.lastRequest.longValue = System.currentTimeMillis()
                onEventSent?.let { it(3) }
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
                if (accountSession.data.value == null) {
                    // TODO: AccountSession null
                    throw Exception("")
                }

                val data = dutRequestRepository.getAccountInformation(accountSession.data.value!!)

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
                accountInformation.lastRequest.longValue = System.currentTimeMillis()
                onEventSent?.let { it(4) }
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
                if (accountSession.data.value == null) {
                    // TODO: AccountSession null
                    throw Exception("")
                }

                val data = dutRequestRepository.getAccountTrainingStatus(accountSession.data.value!!)

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
                accountTrainingStatus.lastRequest.longValue = System.currentTimeMillis()
                onEventSent?.let { it(5) }
            }
        )
    }
}