package io.zoemeow.dutnotify.viewmodel

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import io.zoemeow.dutapi.objects.accounts.AccountInformation
import io.zoemeow.dutapi.objects.accounts.SubjectFeeItem
import io.zoemeow.dutapi.objects.accounts.SubjectScheduleItem
import io.zoemeow.dutnotify.model.enums.ProcessState
import io.zoemeow.dutnotify.model.account.AccountSession
import io.zoemeow.dutnotify.model.account.SchoolYearItem
import io.zoemeow.dutnotify.model.appsettings.AppSettings
import io.zoemeow.dutnotify.model.enums.LoginState
import io.zoemeow.dutnotify.module.AccountModule
import io.zoemeow.dutnotify.utils.DUTDateUtils.Companion.getCurrentDayOfWeek
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AccountDataStore(
    private val mainViewModel: MainViewModel,
    private val accountModule: AccountModule,
    private val appSettings: AppSettings,
) {
    // Account UI area =============================================================================
    /**
     * Gets or sets your current username (get from account information).
     */
    val username: MutableState<String> = mutableStateOf("")

    /**
     * Gets or sets if your account is logged in.
     */
    val loginState: MutableState<LoginState> = mutableStateOf(LoginState.NotLoggedIn)

    /**
     * Gets or sets if you are logging in.
     */
    val procAccLogin: MutableState<ProcessState> = mutableStateOf(ProcessState.NotRanYet)

    /**
     * Gets or sets if subject schedule process are running.
     */
    val procAccSubSch: MutableState<ProcessState> = mutableStateOf(ProcessState.NotRanYet)

    /**
     * Gets or sets if subject fee process are running.
     */
    val procAccSubFee: MutableState<ProcessState> = mutableStateOf(ProcessState.NotRanYet)

    /**
     * Gets or sets if account information process are running.
     */
    val procAccInfo: MutableState<ProcessState> = mutableStateOf(ProcessState.NotRanYet)

    /**
     * Gets or sets your current subject schedule list.
     */
    val subjectSchedule: SnapshotStateList<SubjectScheduleItem> = mutableStateListOf()

    /**
     * Gets or sets your current subject schedule list by day you specified.
     */
    val subjectScheduleByDay: SnapshotStateList<SubjectScheduleItem> = mutableStateListOf()

    /**
     * Gets or sets your current subject fee list.
     */
    val subjectFee: SnapshotStateList<SubjectFeeItem> = mutableStateListOf()

    /**
     * Gets or sets your current account information.
     */
    val accountInformation: MutableState<AccountInformation?> = mutableStateOf(null)
    // =============================================================================================

    fun login(
        username: String? = null,
        password: String? = null,
        remembered: Boolean,
    ) {
        if (procAccLogin.value == ProcessState.Running)
            return

        var result = false
        procAccLogin.value = ProcessState.Running
        loginState.value = LoginState.LoggingIn

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                if (username == null || password == null) {
                    runCatching {
                        if (accountModule.hasSavedLogin()) {
                            accountModule.login { result ->
                                if (!result)
                                    throw Exception("Login failed!")
                            }
                        } else {
                            throw Exception("Currently not have account in application!")
                        }
                    }.onSuccess {
                        result = true
                    }.onFailure {
                        it.printStackTrace()
                        result = false
                    }
                } else {
                    runCatching {
                        accountModule.login(
                            username = username, password = password,
                            remember = remembered, forceReLogin = true,
                            onResult = { result ->
                                if (!result)
                                    throw Exception("Login failed!")
                            }
                        )
                    }.onSuccess {
                        result = true
                    }.onFailure {
                        it.printStackTrace()
                        result = false
                    }
                }
            }
        }.invokeOnCompletion {
            it?.printStackTrace()
            if (result) {
                loginState.value = LoginState.LoggedIn
                procAccLogin.value = ProcessState.Successful

                fetchAccountInformation()
                fetchSubjectSchedule()
                fetchSubjectFee()

                mainViewModel.requestSaveChanges()

                // Show snack bar
                mainViewModel.showSnackBarMessage("Successfully login!", true)
            } else {
                loginState.value = if (accountModule.hasSavedLogin())
                    LoginState.NotLoggedInButRemembered else LoginState.NotLoggedIn
                procAccLogin.value = ProcessState.Failed
                mainViewModel.requestSaveChanges()
            }
        }
    }

    fun logout() {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                runCatching {
                    accountModule.logout(
                        onResult = { result ->
                            if (!result)
                                throw Exception("Logout failed!")
                        }
                    )
                }.onFailure {
                    it.printStackTrace()
                }
            }
        }.invokeOnCompletion {
            it?.printStackTrace()

            // Clear old data
            subjectSchedule.clear()
            subjectScheduleByDay.clear()
            subjectFee.clear()
            accountInformation.value = null

            // Reset state to default
            loginState.value = LoginState.NotLoggedIn
            procAccLogin.value = ProcessState.NotRanYet

            mainViewModel.requestSaveChanges()

            // Show snack bar
            mainViewModel.showSnackBarMessage("Successfully logout!", true)
        }
    }

    fun fetchSubjectSchedule(
        schoolYearItem: SchoolYearItem = appSettings.schoolYear,
    ) {
        if (procAccSubSch.value == ProcessState.Running)
            return
        procAccSubSch.value = ProcessState.Running

        var result = false
        val data: ArrayList<SubjectScheduleItem> = arrayListOf()

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                runCatching {
                    accountModule.getSubjectSchedule(
                        schoolYearItem = schoolYearItem
                    ) { arrayList ->
                        if (arrayList != null) {
                            data.addAll(arrayList)
                            arrayList.clear()
                        } else throw Exception("Subject schedule data is empty!")
                    }
                }.onSuccess {
                    result = true
                }.onFailure {
                    it.printStackTrace()
                    data.clear()
                    result = false
                }
            }
        }.invokeOnCompletion {
            it?.printStackTrace()
            if (result) {
                subjectSchedule.clear()
                subjectSchedule.addAll(data)
                data.clear()
                filterSubjectScheduleByDay()
                procAccSubSch.value = ProcessState.Successful
            } else {
                procAccSubSch.value = ProcessState.Failed
            }
        }
    }

    fun filterSubjectScheduleByDay(value: Int = getCurrentDayOfWeek() - 1) {
        val temp = arrayListOf<SubjectScheduleItem>()
        val dayOfWeekModified = if (value > 6) 0 else value

        temp.addAll(
            subjectSchedule.filter { item ->
                item.subjectStudy.scheduleList.any { week ->
                    week.dayOfWeek == dayOfWeekModified
                }
            }.sortedBy { item ->
                item.subjectStudy.scheduleList.filter { week ->
                    week.dayOfWeek == dayOfWeekModified
                }[0].lesson.start
            }
        )

        subjectScheduleByDay.clear()
        subjectScheduleByDay.addAll(temp)
        temp.clear()
    }

    fun fetchSubjectFee(
        schoolYearItem: SchoolYearItem = appSettings.schoolYear,
    ) {
        if (procAccSubFee.value == ProcessState.Running)
            return
        procAccSubFee.value = ProcessState.Running

        var result = false
        val data: ArrayList<SubjectFeeItem> = arrayListOf()

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                runCatching {
                    accountModule.getSubjectFee(
                        schoolYearItem = schoolYearItem
                    ) { arrayList ->
                        if (arrayList != null) {
                            data.addAll(arrayList)
                            arrayList.clear()
                        } else throw Exception("Subject fee data is empty!")
                    }
                }.onSuccess {
                    result = true
                }.onFailure {
                    it.printStackTrace()
                    data.clear()
                    result = false
                }
            }
        }.invokeOnCompletion {
            it?.printStackTrace()
            if (result) {
                subjectFee.clear()
                subjectFee.addAll(data)
                data.clear()
                procAccSubFee.value = ProcessState.Successful
            } else {
                procAccSubFee.value = ProcessState.Failed
            }
        }
    }

    fun fetchAccountInformation() {
        if (procAccInfo.value == ProcessState.Running)
            return
        procAccInfo.value = ProcessState.Running

        var result = false
        var data: AccountInformation? = null

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                runCatching {
                    accountModule.getAccountInformation { item ->
                        if (item != null) {
                            data = item
                        } else throw Exception("Account Information data is empty!")

                    }
                }.onSuccess {
                    result = true
                }.onFailure {
                    it.printStackTrace()
                    data = null
                    result = false
                }
            }
        }.invokeOnCompletion {
            it?.printStackTrace()
            if (result) {
                accountInformation.value = data
                username.value = data!!.studentId
                procAccInfo.value = ProcessState.Successful
            } else {
                procAccInfo.value = ProcessState.Failed
            }
        }
    }

    fun reLogin(
        silent: Boolean = false,
        reloadSubject: Boolean = true,
        schoolYearItem: SchoolYearItem,
    ) {
        var result = false

        if (accountModule.hasSavedLogin())
            loginState.value = LoginState.NotLoggedInButRemembered
        loginState.value = LoginState.LoggingIn

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                runCatching {
                    accountModule.checkIsLoggedIn { result ->
                        if (!result)
                            throw Exception("ReLogin account failed!")
                    }
                }.onSuccess {
                    result = true
                }.onFailure {
                    result = false
                }
            }
        }.invokeOnCompletion {
            if (result) {
                if (reloadSubject) {
                    fetchAccountInformation()
                    fetchSubjectSchedule(schoolYearItem)
                    fetchSubjectFee(schoolYearItem)
                }
                loginState.value = LoginState.LoggedIn

                if (!silent) {
                    // Show snack bar
                    mainViewModel.showSnackBarMessage(
                        "Successfully re-login your account!",
                        true
                    )
                }
            } else {
                if (accountModule.hasSavedLogin())
                    loginState.value = LoginState.NotLoggedInButRemembered
                else loginState.value = LoginState.NotTriggered
                // TODO: Notify user here!
            }
            mainViewModel.requestSaveChanges()
        }
    }

    fun isStoreAccount(): Boolean {
        return accountModule.hasSavedLogin()
    }

    fun loadSettings(accSession: AccountSession) {
        accountModule.loadSettings(accSession)
    }

    fun saveSettings(result: ((AccountSession) -> Unit)? = null) {
        if (result != null) {
            accountModule.saveSettings { accountSession ->
                result(accountSession)
            }
        }
    }
}