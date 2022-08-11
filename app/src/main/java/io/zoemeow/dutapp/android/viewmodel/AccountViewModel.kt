package io.zoemeow.dutapp.android.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.zoemeow.dutapi.objects.AccountInformation
import io.zoemeow.dutapi.objects.SubjectFeeItem
import io.zoemeow.dutapi.objects.SubjectScheduleItem
import io.zoemeow.dutapp.android.model.ProcessState
import io.zoemeow.dutapp.android.model.account.SchoolYearItem
import io.zoemeow.dutapp.android.repository.AccountFileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val accountFileRepository: AccountFileRepository
) : ViewModel() {
    /**
     * UIStatus
     */
    private val uiStatus: UIStatus = UIStatus.getInstance()

    /**
     * GlobalViewModel
     */
    private val globalViewModel = GlobalViewModel.getInstance()

    /**
     * Check if you have logged in.
     */
    val isLoggedIn: MutableState<Boolean> = mutableStateOf(false)

    /**
     * Check if a progress for log in is running.
     */
    val processStateLoggingIn: MutableState<ProcessState> = mutableStateOf(ProcessState.NotRun)

    /**
     * List of your subject schedule in your account of sv.dut.udn.vn.
     */
    val subjectScheduleList: SnapshotStateList<SubjectScheduleItem> = mutableStateListOf()

    /**
     * Check if a progress for get subject schedule is running.
     */
    val processStateSubjectSchedule: MutableState<ProcessState> =
        mutableStateOf(ProcessState.NotRun)

    /**
     * List of your subject fee in your account of sv.dut.udn.vn.
     */
    val subjectFeeList: SnapshotStateList<SubjectFeeItem> = mutableStateListOf()

    /**
     * Check if a progress for get subject fee is running.
     */
    val processStateSubjectFee: MutableState<ProcessState> = mutableStateOf(ProcessState.NotRun)

    /**
     * Your account information of sv.dut.udn.vn.
     */
    val accountInformation: MutableState<AccountInformation?> = mutableStateOf(null)

    /**
     * Your username (this means your student ID) after logged in.
     */
    val username: MutableState<String> = mutableStateOf(String())
    private fun setUserName(username: String) {
        this.username.value = username
    }

    /**
     * Check if a progress for get account information is running.
     */
    val processStateAccInfo: MutableState<ProcessState> = mutableStateOf(ProcessState.NotRun)

    /**
     * Login account using username and password to sv.dut.udn.vn.
     * Result will return to ```isLoggedIn``` via checkIsLoggedIn().
     */
    fun login(username: String, password: String, remember: Boolean = true) {
        if (processStateLoggingIn.value == ProcessState.Running)
            return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                processStateLoggingIn.value = ProcessState.Running

                accountFileRepository.login(
                    username = username,
                    password = password,
                    remember = remember,
                    forceReLogin = true,
                    onResult = { result ->
                        if (result) {
                            // Set username
                            setUserName(username)

                            // Set view to 1
                            uiStatus.accountCurrentPage.value = 1

                            // Set process status to successful
                            processStateLoggingIn.value = ProcessState.Successful

                            // Set isLoggedIn to true
                            isLoggedIn.value = true

                            // Preload account information
                            getSubjectSchedule()
                            getSubjectFee()
                            getAccountInformation()

                            // Show snack bar
                            uiStatus.showSnackBarMessage("Successfully login!")
                        } else {
                            isLoggedIn.value = false
                            throw Exception("Failed while logging in!")
                        }
                    }
                )
            } catch (ex: Exception) {
                processStateLoggingIn.value = ProcessState.Failed
            }
        }
    }

    /**
     * Logout your account from sv.dut.udn.vn.
     * Result will return to ```isLoggedIn``` via checkIsLoggedIn().
     */
    fun logout() {
        if (processStateLoggingIn.value == ProcessState.Running)
            return

        CoroutineScope(Dispatchers.IO).launch {
            accountFileRepository.logout(
                onResult = { result ->
                    if (result) {
                        // Set process status to not run (known as not logged in)
                        processStateLoggingIn.value = ProcessState.NotRun

                        // Clear username
                        setUserName("")

                        // Reset view to 0
                        uiStatus.accountCurrentPage.value = 0

                        // Show snack bar
                        uiStatus.showSnackBarMessage("Successfully logout!")
                    }
                }
            )
        }
    }

    /**
     * Get subject schedule for your account in sv.dut.udn.vn.
     * Adjust year and semester in ```schoolYearItem```, or pass arguments directly in function.
     * Result will return to ```subjectScheduleList```.
     */
    fun getSubjectSchedule(schoolYearItemInput: SchoolYearItem = globalViewModel.schoolYear.value) {
        if (processStateSubjectSchedule.value == ProcessState.Running)
            return

        if (!isLoggedIn.value)
            return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                processStateSubjectSchedule.value = ProcessState.Running

                accountFileRepository.getSubjectSchedule(
                    schoolYearItemInput,
                    onResult = { arrayList ->
                        if (arrayList != null) {
                            subjectScheduleList.clear()
                            subjectScheduleList.addAll(arrayList)
                            arrayList.clear()
                            processStateSubjectSchedule.value = ProcessState.Successful
                        } else {
                            processStateSubjectSchedule.value = ProcessState.Failed
                        }
                    }
                )
            } catch (ex: Exception) {
                processStateSubjectSchedule.value = ProcessState.Failed
            }
        }
    }

    /**
     * Get subject fee for your account in sv.dut.udn.vn.
     * Adjust year and semester in ```schoolYearItem```, or pass arguments directly in function.
     * Result will return to ```subjectFeeList```.
     */
    fun getSubjectFee(schoolYearItemInput: SchoolYearItem = globalViewModel.schoolYear.value) {
        if (processStateSubjectFee.value == ProcessState.Running)
            return

        if (!isLoggedIn.value)
            return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                processStateSubjectFee.value = ProcessState.Running

                accountFileRepository.getSubjectFee(
                    schoolYearItemInput,
                    onResult = { arrayList ->
                        if (arrayList != null) {
                            subjectFeeList.clear()
                            subjectFeeList.addAll(arrayList)
                            arrayList.clear()
                            processStateSubjectFee.value = ProcessState.Successful
                        } else {
                            processStateSubjectFee.value = ProcessState.Failed
                        }
                    }
                )
            } catch (ex: Exception) {
                processStateSubjectFee.value = ProcessState.Failed
            }
        }
    }

    /**
     * Get account information for your account in sv.dut.udn.vn.
     * Result will return to ```subjectScheduleList```.
     */
    fun getAccountInformation() {
        if (processStateAccInfo.value == ProcessState.Running)
            return

        if (!isLoggedIn.value)
            return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                processStateAccInfo.value = ProcessState.Running

                accountFileRepository.getAccountInformation(
                    onResult = { item ->
                        if (item != null) {
                            accountInformation.value = item
                            setUserName(accountInformation.value?.studentId ?: "")
                            processStateAccInfo.value = ProcessState.Successful
                        } else {
                            processStateAccInfo.value = ProcessState.Failed
                        }
                    }
                )
            } catch (ex: Exception) {
                processStateAccInfo.value = ProcessState.Failed
            }
        }
    }

    /**
     * Re-login your account (this will be triggered when you have saved account in application.
     */
    private fun reLogin() {
        // If another login process is running, this thread will terminate now.
        if (processStateLoggingIn.value == ProcessState.Running)
            return

        CoroutineScope(Dispatchers.IO).launch {
            accountFileRepository.checkIsLoggedIn { result ->
                if (result) {
                    // Set view to 1
                    uiStatus.accountCurrentPage.value = 1

                    isLoggedIn.value = true

                    // Pre-load account information
                    getSubjectSchedule(globalViewModel.schoolYear.value)
                    getSubjectFee(globalViewModel.schoolYear.value)
                    getAccountInformation()
                } else {
                    // Reset view to 0
                    uiStatus.accountCurrentPage.value = 0

                    isLoggedIn.value = false
                    // Show snack bar
//                        uiStatus.showSnackBarMessage(
//                            "Something went wrong while logging you in! " +
//                                    "Don't worry, just try again. " +
//                                    "If still unsuccessful, try to logout and login again."
//                        )
                }
            }
        }
    }

    init {
        reLogin()
    }
}