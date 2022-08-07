package io.zoemeow.dutapp.android.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.zoemeow.dutapi.objects.AccountInformation
import io.zoemeow.dutapi.objects.SubjectFeeItem
import io.zoemeow.dutapi.objects.SubjectScheduleItem
import io.zoemeow.dutapp.android.model.ProcessState
import io.zoemeow.dutapp.android.model.SchoolYearItem
import io.zoemeow.dutapp.android.repository.AccountFileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val accountFileRepository: AccountFileRepository
): ViewModel() {
    companion object {
        private val instance: MutableState<AccountViewModel?> = mutableStateOf(null)

        fun getInstance(): AccountViewModel {
            return instance.value!!
        }

        fun setInstance(accViewModel: AccountViewModel) {
            this.instance.value = accViewModel
        }
    }

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
    val processStateSubjectSchedule: MutableState<ProcessState> = mutableStateOf(ProcessState.NotRun)

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

    /**
     * Check if a progress for get account information is running.
     */
    val processStateAccInfo: MutableState<ProcessState> = mutableStateOf(ProcessState.NotRun)

    /**
     * Get login state.
     * Result will return to ```isLoggedIn```.
     */
    private fun checkIsLoggedIn() {
        isLoggedIn.value = accountFileRepository.isLoggedIn()
    }

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

                val result = accountFileRepository.login(username, password, remember)

                if (result) {
                    processStateLoggingIn.value = ProcessState.Successful

                    // Set view to 1
                    accountPage.value = 1

                    globalViewModel.showMessageSnackBar("Successfully login!")
                }
                else {
                    processStateLoggingIn.value = ProcessState.Failed
//                    globalViewModel.showMessageSnackBar(
//                        "Something went wrong with your account! " +
//                            "Make sure your username and password is correct."
//                    )
                }
                checkIsLoggedIn()
                getAccountInformation()
            }
            catch (ex: Exception) {
                processStateLoggingIn.value = ProcessState.Failed
//                globalViewModel.showMessageSnackBar(
//                    "Something went wrong while logging you in! " +
//                        "Don't worry, just try again."
//                )
            }
        }
    }

    var reLoginFirstTime: Boolean = true

    fun reLogin() {
        if (!reLoginFirstTime)
            return

        if (processStateLoggingIn.value == ProcessState.Running)
            return

        viewModelScope.launch {
            try {
                processStateLoggingIn.value = ProcessState.Running

                if (accountFileRepository.isLoggedIn() || accountFileRepository.login()) {
                    processStateLoggingIn.value = ProcessState.Successful
                    checkIsLoggedIn()

                    // Set view to 1
                    accountPage.value = 1
                }
                else throw Exception("Session expired and your account is out-of-date.")
            }
            catch (ex: Exception) {
                processStateLoggingIn.value = ProcessState.Failed
                ex.printStackTrace()
                accountFileRepository.checkOrGetSessionId()
//                globalViewModel.showMessageSnackBar(
//                    "Something went wrong while logging you in! " +
//                            "Don't worry, just try again. " +
//                            "If still unsuccessful, try to logout and login again."
//                )
            }
        }

        reLoginFirstTime = false
    }

    /**
     * Logout your account from sv.dut.udn.vn.
     * Result will return to ```isLoggedIn``` via checkIsLoggedIn().
     */
    fun logout() {
        if (processStateLoggingIn.value == ProcessState.Running)
            return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                processStateLoggingIn.value = ProcessState.Running

                val result = accountFileRepository.logout()

                if (result) {
                    processStateLoggingIn.value = ProcessState.Successful

                    // Reset view to 0
                    accountPage.value = 0

                    globalViewModel.showMessageSnackBar("Successfully logout!")
                }
                else {
                    processStateLoggingIn.value = ProcessState.Failed
                    globalViewModel.showMessageSnackBar(
                        "Something went wrong while logging you out! " +
                                "Don't worry, just try again. " +
                                "If still unsuccessful, try to logout and login again."
                    )
                }
                checkIsLoggedIn()
            }
            catch (ex: Exception) {
                processStateLoggingIn.value = ProcessState.Failed
                globalViewModel.showMessageSnackBar(
                    "Something went wrong while logging you out! " +
                            "Don't worry, just try again. " +
                            "If still unsuccessful, try to logout and login again."
                )
            }
        }
    }

    /**
     * Set custom Session ID, which can get from sv.dut.udn.vn.
     * (FOR OFFLINE AND CACHE USING ONLY, USE THIS AT YOUR OWN RISK)
     */
    fun setSessionId(sessionId: String) {
        accountFileRepository.setSessionId(sessionId)
        checkIsLoggedIn()
    }

    /**
     * Get subject schedule for your account in sv.dut.udn.vn.
     * Adjust year and semester in ```schoolYearItem```, or pass arguments directly in function.
     * Result will return to ```subjectScheduleList```.
     */
    fun getSubjectSchedule(schoolYearItemInput: SchoolYearItem? = null) {
        if (processStateSubjectSchedule.value == ProcessState.Running)
            return

        if (!isLoggedIn.value)
            return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                processStateSubjectSchedule.value = ProcessState.Running

                val subTemp = if (schoolYearItemInput != null)
                    accountFileRepository.getSubjectSchedule(
                        schoolYearItemInput.year,
                        schoolYearItemInput.semester
                    )
                else accountFileRepository.getSubjectSchedule(
                    globalViewModel.schoolYear.value.year,
                    globalViewModel.schoolYear.value.semester
                )

                subjectScheduleList.clear()
                subjectScheduleList.addAll(subTemp)
                subTemp.clear()

                processStateSubjectSchedule.value = ProcessState.Successful
            }
            catch (ex: Exception) {
                processStateSubjectSchedule.value = ProcessState.Failed
            }
        }
    }

    /**
     * Get subject fee for your account in sv.dut.udn.vn.
     * Adjust year and semester in ```schoolYearItem```, or pass arguments directly in function.
     * Result will return to ```subjectFeeList```.
     */
    fun getSubjectFee(schoolYearItemInput: SchoolYearItem? = null) {
        if (processStateSubjectFee.value == ProcessState.Running)
            return

        if (!isLoggedIn.value)
            return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                processStateSubjectFee.value = ProcessState.Running

                val subTemp: ArrayList<SubjectFeeItem> = if (schoolYearItemInput != null)
                    accountFileRepository.getSubjectFee(
                        schoolYearItemInput.year,
                        schoolYearItemInput.semester
                    )
                else accountFileRepository.getSubjectFee(
                    globalViewModel.schoolYear.value.year,
                    globalViewModel.schoolYear.value.semester
                )

                subjectFeeList.clear()
                subjectFeeList.addAll(subTemp)
                subTemp.clear()

                processStateSubjectFee.value = ProcessState.Successful
            }
            catch (ex: Exception) {
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

                accountInformation.value = accountFileRepository.getAccountInformation()
                username.value = accountInformation.value?.studentId ?: ""

                processStateAccInfo.value = ProcessState.Successful
            }
            catch (ex: Exception) {
                processStateAccInfo.value = ProcessState.Failed
            }
        }
    }


    // View here
    /**
     * Current page of account
     * 0: Not logged in
     * 1: Dashboard
     * 2: Subject schedule
     * 3: Subject fee
     */
    val accountPage: MutableState<Int> = mutableStateOf(0)
}