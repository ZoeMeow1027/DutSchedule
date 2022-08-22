package io.zoemeow.dutapp.android.viewmodel

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import io.zoemeow.dutapi.objects.AccountInformation
import io.zoemeow.dutapi.objects.SubjectFeeItem
import io.zoemeow.dutapi.objects.SubjectScheduleItem
import io.zoemeow.dutapp.android.model.ProcessState
import io.zoemeow.dutapp.android.model.account.SchoolYearItem
import io.zoemeow.dutapp.android.repository.AccountFileRepository
import io.zoemeow.dutapp.android.utils.getCurrentDayOfWeek
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AccountModule(
    private val mainViewModel: MainViewModel,
    private val accountFileRepository: AccountFileRepository
) {
//    /**
//     * Check if you have logged in.
//     */
//    val isLoggedIn: MutableState<Boolean> = mutableStateOf(false)
//
//    /**
//     * Check if you have logged in (remember - for launch app)
//     */
//    val isRememberLoggedIn: MutableState<Boolean> = mutableStateOf(false)
//
//    /**
//     * Check if a progress for log in is running.
//     */
//    val processStateLoggingIn: MutableState<ProcessState> = mutableStateOf(ProcessState.NotRanYet)
//
//    /**
//     * List of your subject schedule in your account of sv.dut.udn.vn.
//     */
//    val subjectScheduleList: SnapshotStateList<SubjectScheduleItem> = mutableStateListOf()
//
//    /**
//     * Check if a progress for get subject schedule is running.
//     */
//    val processStateSubjectSchedule: MutableState<ProcessState> =
//        mutableStateOf(ProcessState.NotRanYet)
//
//    val subjectScheduleListByDay: SnapshotStateList<SubjectScheduleItem> = mutableStateListOf()
//
//    /**
//     * List of your subject fee in your account of sv.dut.udn.vn.
//     */
//    val subjectFeeList: SnapshotStateList<SubjectFeeItem> = mutableStateListOf()
//
//    /**
//     * Check if a progress for get subject fee is running.
//     */
//    val processStateSubjectFee: MutableState<ProcessState> = mutableStateOf(ProcessState.NotRanYet)
//
//    /**
//     * Your account information of sv.dut.udn.vn.
//     */
//    val accountInformation: MutableState<AccountInformation?> = mutableStateOf(null)
//
//    /**
//     * Your username (this means your student ID) after logged in.
//     */
//    val username: MutableState<String> = mutableStateOf(String())
//    private fun setUserName(username: String) {
//        this.username.value = username
//    }
//
//    /**
//     * Check if a progress for get account information is running.
//     */
//    val processStateAccInfo: MutableState<ProcessState> = mutableStateOf(ProcessState.NotRanYet)
//
//    /**
//     * Login account using username and password to sv.dut.udn.vn.
//     * Result will return to ```isLoggedIn``` via checkIsLoggedIn().
//     */
//    fun login(username: String, password: String, remember: Boolean = true) {
//        if (processStateLoggingIn.value == ProcessState.Running)
//            return
//
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                processStateLoggingIn.value = ProcessState.Running
//
//                accountFileRepository.login(
//                    username = username,
//                    password = password,
//                    remember = remember,
//                    forceReLogin = true,
//                    onResult = { result ->
//                        if (result) {
//                            // Set username
//                            setUserName(username)
//
//                            // Set view to 1
//                            mainViewModel.uiStatus.accountCurrentPage.value = 1
//
//                            // Set process status to successful
//                            processStateLoggingIn.value = ProcessState.Successful
//
//                            // Set isLoggedIn to true
//                            isLoggedIn.value = true
//
//                            // Enable remember login
//                            isRememberLoggedIn.value = true
//
//                            // Preload account information
//                            getSubjectSchedule()
//                            getSubjectFee()
//                            getAccountInformation()
//
//                            // Show snack bar
//                            mainViewModel.uiStatus.showSnackBarMessage("Successfully login!")
//                        } else {
//                            isLoggedIn.value = false
//                            throw Exception("Failed while logging in!")
//                        }
//                    }
//                )
//            } catch (ex: Exception) {
//                processStateLoggingIn.value = ProcessState.Failed
//            }
//        }
//    }
//
//    /**
//     * Logout your account from sv.dut.udn.vn.
//     * Result will return to ```isLoggedIn``` via checkIsLoggedIn().
//     */
//    fun logout() {
//        if (processStateLoggingIn.value == ProcessState.Running)
//            return
//
//        // Set process status to not run (known as not logged in)
//        processStateSubjectSchedule.value = ProcessState.NotRanYet
//
//        // Clear username
//        setUserName("")
//
//        // Disable remember login
//        isRememberLoggedIn.value = false
//
//        // Reset view to 0
//        mainViewModel.uiStatus.accountCurrentPage.value = 0
//
//        CoroutineScope(Dispatchers.Main).launch {
//            accountFileRepository.logout(
//                onResult = { result ->
//                    if (result) {
//                        // Show snack bar
//                        mainViewModel.uiStatus.showSnackBarMessage("Successfully logout!")
//                    }
//                }
//            )
//        }
//    }
//
//    /**
//     * Get subject schedule for your account in sv.dut.udn.vn.
//     * Adjust year and semester in ```schoolYearItem```, or pass arguments directly in function.
//     * Result will return to ```subjectScheduleList```.
//     */
//    fun getSubjectSchedule(schoolYearItemInput: SchoolYearItem = mainViewModel.settings.schoolYear.value) {
//        if (processStateSubjectSchedule.value == ProcessState.Running)
//            return
//
//        if (!isLoggedIn.value)
//            return
//
//        processStateSubjectSchedule.value = ProcessState.Running
//        var processResult: ProcessState = ProcessState.NotRanYet
//        CoroutineScope(Dispatchers.Main).launch {
//            withContext(Dispatchers.IO) {
//                runCatching {
//                    accountFileRepository.getSubjectSchedule(
//                        schoolYearItem = schoolYearItemInput,
//                        onResult = { arrayList ->
//                            if (arrayList != null) {
//                                subjectScheduleList.clear()
//                                subjectScheduleList.addAll(arrayList)
//                                arrayList.clear()
//                            } else throw Exception("Subject Schedule - Load data failed.")
//                        }
//                    )
//                }.onSuccess {
//                    processResult = ProcessState.Successful
//                }.onFailure {
//                    it.printStackTrace()
//                    processResult = ProcessState.Failed
//                }
//            }
//        }.invokeOnCompletion {
//            processStateSubjectSchedule.value = processResult
//            filterSubjectScheduleByDay()
//        }
//    }
//
//    fun filterSubjectScheduleByDay(value: Int = getCurrentDayOfWeek()) {
//        CoroutineScope(Dispatchers.Main).launch {
//            subjectScheduleListByDay.clear()
//
//            subjectScheduleListByDay.addAll(
//                subjectScheduleList.filter { item -> item.subjectStudy.scheduleList.any { week -> week.dayOfWeek == value } }
//            )
//        }
//    }
//
//    /**
//     * Get subject fee for your account in sv.dut.udn.vn.
//     * Adjust year and semester in ```schoolYearItem```, or pass arguments directly in function.
//     * Result will return to ```subjectFeeList```.
//     */
//    fun getSubjectFee(schoolYearItemInput: SchoolYearItem = mainViewModel.settings.schoolYear.value) {
//        if (processStateSubjectFee.value == ProcessState.Running)
//            return
//
//        if (!isLoggedIn.value)
//            return
//
//        processStateSubjectFee.value = ProcessState.Running
//
//        var processResult: ProcessState = ProcessState.NotRanYet
//        CoroutineScope(Dispatchers.Main).launch {
//            withContext(Dispatchers.IO) {
//                runCatching {
//                    accountFileRepository.getSubjectFee(
//                        schoolYearItemInput,
//                        onResult = { arrayList ->
//                            if (arrayList != null) {
//                                subjectFeeList.clear()
//                                subjectFeeList.addAll(arrayList)
//                                arrayList.clear()
//                            } else throw Exception("Subject Fee - Load data failed.")
//                        }
//                    )
//                }.onSuccess {
//                    processResult = ProcessState.Successful
//                }.onFailure {
//                    it.printStackTrace()
//                    processResult = ProcessState.Failed
//                }
//            }
//        }.invokeOnCompletion {
//            processStateSubjectFee.value = processResult
//        }
//    }
//
//    /**
//     * Get account information for your account in sv.dut.udn.vn.
//     * Result will return to ```subjectScheduleList```.
//     */
//    fun getAccountInformation() {
//        if (processStateAccInfo.value == ProcessState.Running)
//            return
//
//        if (!isLoggedIn.value)
//            return
//
//        processStateAccInfo.value = ProcessState.Running
//
//        var processResult: ProcessState = ProcessState.NotRanYet
//        CoroutineScope(Dispatchers.Main).launch {
//            withContext(Dispatchers.IO) {
//                runCatching {
//                    Log.d("Account Information", "Triggered")
//                    accountFileRepository.getAccountInformation(
//                        onResult = { item ->
//                            if (item != null) {
//                                accountInformation.value = item
//                                setUserName(accountInformation.value?.studentId ?: "")
//                            } else throw Exception("Account Information - Load data failed.")
//                        }
//                    )
//                }.onSuccess {
//                    processResult = ProcessState.Successful
//                }.onFailure {
//                    it.printStackTrace()
//                    processResult = ProcessState.Failed
//                }
//            }
//        }.invokeOnCompletion {
//            processStateAccInfo.value = processResult
//        }
//    }
//
//    /**
//     * Re-login your account (this will be triggered when you have saved account in application.
//     */
//    fun reLogin() {
//        // If another login process is running, this thread will terminate now.
//        if (processStateLoggingIn.value == ProcessState.Running)
//            return
//
//        if (accountFileRepository.hasSavedLogin()) {
//            isRememberLoggedIn.value = true
//            if (mainViewModel.uiStatus.accountCurrentPage.value < 1)
//                mainViewModel.uiStatus.accountCurrentPage.value = 1
//        }
//
//        var loggedIn = false
//        CoroutineScope(Dispatchers.Main).launch {
//            withContext(Dispatchers.IO) {
//                accountFileRepository.checkIsLoggedIn { result ->
//                    loggedIn = result
//                }
//            }
//        }.invokeOnCompletion {
//            if (loggedIn) {
//                if (mainViewModel.uiStatus.accountCurrentPage.value < 1)
//                    mainViewModel.uiStatus.accountCurrentPage.value = 1
//                isLoggedIn.value = true
//                kotlin.runCatching {
//                    getAccountInformation()
//                    getSubjectSchedule()
//                    getSubjectFee()
//                }.onFailure {
//                    it.printStackTrace()
//                }
//            }
//            else {
//                isLoggedIn.value = false
//            }
//        }
//    }



    // New function (this class will only processing and not storing data here). ===================
    fun login2(
        result: ((Boolean) -> Unit)? = null
    ) {
        runCatching {
            if (accountFileRepository.hasSavedLogin()) {
                accountFileRepository.login { result ->
                    if (!result)
                        throw Exception("Login failed!")
                }
            }
            else {
                throw Exception("Currently not have account in application!")
            }
        }.onSuccess {
            if (result != null) {
                result(true)
            }
        }.onFailure {
            it.printStackTrace()
            if (result != null) {
                result(false)
            }
        }
    }

    fun login2(
        username: String,
        password: String,
        remember: Boolean = false,
        result: ((Boolean) -> Unit)? = null
    ) {
        runCatching {
            accountFileRepository.login(
                username = username, password = password,
                remember = remember, forceReLogin = true,
                onResult = { result ->
                    if (!result)
                        throw Exception("Login failed!")
                }
            )
        }.onSuccess {
            if (result != null) {
                result(true)
            }
        }.onFailure {
            it.printStackTrace()
            if (result != null) {
                result(false)
            }
        }
    }

    fun logout2(
        result: ((Boolean) -> Unit)? = null
    ) {
        runCatching {
            accountFileRepository.logout(
                onResult = { result ->
                    if (!result)
                        throw Exception("Logout failed!")
                }
            )
        }.onSuccess {
            if (result != null) {
                result(true)
            }
        }.onFailure {
            it.printStackTrace()
            if (result != null) {
                result(false)
            }
        }
    }

    fun getSubjectSchedule2(
        schoolYearItem: SchoolYearItem = mainViewModel.settings.schoolYear.value,
        result: ((Boolean, ArrayList<SubjectScheduleItem>?) -> Unit)? = null
    ) {
        var data: ArrayList<SubjectScheduleItem>? = null
        runCatching {
            accountFileRepository.getSubjectSchedule(
                schoolYearItem = schoolYearItem
            ) { arrayList ->
                if (arrayList != null) {
                    data = arrayListOf()
                    data?.addAll(arrayList)
                    arrayList.clear()
                } else throw Exception("Subject schedule data is empty!")
            }
        }.onSuccess {
            if (result != null)
                result(true, data)
        }.onFailure {
            it.printStackTrace()
            if (result != null)
                result(false, null)
        }
    }

    fun getSubjectFee2(
        schoolYearItem: SchoolYearItem = mainViewModel.settings.schoolYear.value,
        result: ((Boolean, ArrayList<SubjectFeeItem>?) -> Unit)? = null
    ) {
        var data: ArrayList<SubjectFeeItem>? = null
        runCatching {
            accountFileRepository.getSubjectFee(
                schoolYearItem = schoolYearItem
            ) { arrayList ->
                if (arrayList != null) {
                    data = arrayListOf()
                    data?.addAll(arrayList)
                    arrayList.clear()
                } else throw Exception("Subject fee data is empty!")
            }
        }.onSuccess {
            if (result != null)
                result(true, data)
        }.onFailure {
            it.printStackTrace()
            if (result != null)
                result(false, null)
        }
    }

    fun getAccountInformation2(
        result: ((Boolean, AccountInformation?) -> Unit)? = null
    ) {
        var accInfo: AccountInformation? = null
        runCatching {
            accountFileRepository.getAccountInformation { item ->
                if (item != null) {
                    accInfo = item
                } else throw Exception("Account Information data is empty!")

            }
        }.onSuccess {
            if (result != null)
                result(true, accInfo)
        }.onFailure {
            it.printStackTrace()
            if (result != null)
                result(false, null)
        }
    }

    fun reLogin2(
        result: ((Boolean) -> Unit)? = null
    ) {
        runCatching {
            accountFileRepository.checkIsLoggedIn { result ->
                if (!result)
                    throw Exception("ReLogin account failed!")
            }
        }.onSuccess {
            if (result != null)
                result(true)
        }.onFailure {
            it.printStackTrace()
            if (result != null)
                result(false)
        }
    }
}