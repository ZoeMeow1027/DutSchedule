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
import io.zoemeow.dutapp.android.model.SchoolYearItem
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

    /**
     * Account session. Handle all for easier use for DutAPI-Java.
     */
    private val accountSession = AccountSession()

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
     * School year item. That's it!
     */
    var schoolYearItem: SchoolYearItem = SchoolYearItem()

    /**
     * Get login state.
     * Result will return to ```isLoggedIn```.
     */
    private fun checkIsLoggedIn() {
        isLoggedIn.value = accountSession.isLoggedIn()
    }

    /**
     * Login account using username and password to sv.dut.udn.vn.
     * Result will return to ```isLoggedIn``` via checkIsLoggedIn().
     */
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

                val result = accountSession.logout()

                processStateLoggingIn.value = if (result) ProcessState.Successful else ProcessState.Failed
                checkIsLoggedIn()
            }
            catch (ex: Exception) {
                processStateLoggingIn.value = ProcessState.Failed
            }
        }
    }

    /**
     * Set custom Session ID, which can get from sv.dut.udn.vn.
     * (FOR OFFLINE AND CACHE USING ONLY, USE THIS AT YOUR OWN RISK)
     */
    fun setSessionId(sessionId: String) {
        accountSession.setSessionId(sessionId)
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
                    accountSession.getSubjectSchedule(
                        schoolYearItemInput.year,
                        schoolYearItemInput.semester
                    )
                else accountSession.getSubjectSchedule(
                    schoolYearItem.year,
                    schoolYearItem.semester
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
                    accountSession.getSubjectFee(
                        schoolYearItemInput.year,
                        schoolYearItemInput.semester
                    )
                else accountSession.getSubjectFee(
                    schoolYearItem.year,
                    schoolYearItem.semester
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