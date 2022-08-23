package io.zoemeow.dutapp.android.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.zoemeow.dutapi.objects.AccountInformation
import io.zoemeow.dutapi.objects.SubjectFeeItem
import io.zoemeow.dutapi.objects.SubjectScheduleItem
import io.zoemeow.dutapp.android.model.ProcessState
import io.zoemeow.dutapp.android.model.account.SchoolYearItem
import io.zoemeow.dutapp.android.model.enums.LoginState
import io.zoemeow.dutapp.android.repository.AccountFileRepository
import io.zoemeow.dutapp.android.repository.CacheFileRepository
import io.zoemeow.dutapp.android.repository.SettingsFileRepository
import io.zoemeow.dutapp.android.utils.getCurrentDayOfWeek
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    cacheFileRepository: CacheFileRepository,
    settingsFileRepository: SettingsFileRepository,
    private val accountFileRepository: AccountFileRepository,
): ViewModel() {
    companion object {
        private val instance: MutableState<MainViewModel?> = mutableStateOf(null)

        fun getInstance(): MainViewModel {
            return instance.value!!
        }

        fun setInstance(viewModel: MainViewModel) {
            this.instance.value = viewModel
        }
    }

    // UI Changed
    val uiStatus: UIStatus = UIStatus()

    // App settings
    val settings: AppSettings = AppSettings(
        settingsFileRepository = settingsFileRepository
    )

    // News Cache for offline reading
    val appCache: AppCache = AppCache(
        cacheFileRepository = cacheFileRepository
    )

    // News module
    val news: NewsModule = NewsModule(
        mainViewModel = this
    )

    // Account Module
    private val accountModule: AccountModule = AccountModule(
        mainViewModel = this,
        accountFileRepository = accountFileRepository
    )

    fun login(
        username: String? = null,
        password: String? = null,
        remembered: Boolean,
    ) {
        if (uiStatus.procAccLogin.value == ProcessState.Running)
            return

        var result = false
        uiStatus.procAccLogin.value = ProcessState.Running
        uiStatus.loginState.value = LoginState.LoggingIn

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                if (username == null || password == null) {
                    if (accountFileRepository.hasSavedLogin())
                        accountModule.login2 {
                            result = it
                        }
                }
                else {
                    accountModule.login2(
                        username = username,
                        password = password,
                        remember = remembered,
                    ) {
                        result = it
                    }
                }
            }
        }.invokeOnCompletion {
            it?.printStackTrace()
            if (result) {
                uiStatus.loginState.value = LoginState.LoggedIn
                uiStatus.procAccLogin.value = ProcessState.Successful
                fetchAccountInformation()
                fetchSubjectSchedule()
                fetchSubjectFee()

                // Show snack bar
                uiStatus.showSnackBarMessage("Successfully login!", true)
            }
            else {
                uiStatus.loginState.value = if (accountFileRepository.hasSavedLogin())
                    LoginState.NotLoggedInButRemembered else LoginState.NotLoggedIn
                uiStatus.procAccLogin.value = ProcessState.Failed
            }
        }
    }

    fun logout() {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                accountModule.logout2()
            }
        }.invokeOnCompletion {
            it?.printStackTrace()

            // Clear old data
            uiStatus.subjectSchedule.clear()
            uiStatus.subjectScheduleByDay.clear()
            uiStatus.subjectFee.clear()
            uiStatus.accountInformation.value = null

            // Reset state to default
            uiStatus.loginState.value = LoginState.NotLoggedIn
            uiStatus.procAccLogin.value = ProcessState.NotRanYet

            // Show snack bar
            uiStatus.showSnackBarMessage("Successfully logout!", true)
        }
    }

    fun fetchSubjectSchedule(
        schoolYearItem: SchoolYearItem = settings.schoolYear.value,
    ) {
        if (uiStatus.procAccSubSch.value == ProcessState.Running)
            return
        uiStatus.procAccSubSch.value = ProcessState.Running

        var result = false
        val data: ArrayList<SubjectScheduleItem> = arrayListOf()

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                accountModule.getSubjectSchedule2(
                    schoolYearItem = schoolYearItem
                ) { result2, arrayList ->
                    if (arrayList != null) {
                        result = result2
                        data.addAll(arrayList)
                    }
                }
            }
        }.invokeOnCompletion {
            it?.printStackTrace()
            if (result) {
                uiStatus.subjectSchedule.clear()
                uiStatus.subjectSchedule.addAll(data)
                data.clear()
                filterSubjectScheduleByDay()
                uiStatus.procAccSubSch.value = ProcessState.Successful
            }
            else {
                uiStatus.procAccSubSch.value = ProcessState.Failed
            }
        }
    }

    fun filterSubjectScheduleByDay(value: Int = getCurrentDayOfWeek()) {
        val temp = arrayListOf<SubjectScheduleItem>()

        temp.addAll(
            uiStatus.subjectSchedule.filter { item ->
                item.subjectStudy.scheduleList.any {
                        week -> week.dayOfWeek == value
                }
            }.sortedBy { item ->
                item.subjectStudy.scheduleList.filter { week ->
                    week.dayOfWeek == value
                }[0].lesson.start
            }
        )

        uiStatus.subjectScheduleByDay.clear()
        uiStatus.subjectScheduleByDay.addAll(temp)
        temp.clear()

//        CoroutineScope(Dispatchers.Main).launch {
//        }
    }

    fun fetchSubjectFee(
        schoolYearItem: SchoolYearItem = settings.schoolYear.value,
    ) {
        if (uiStatus.procAccSubFee.value == ProcessState.Running)
            return
        uiStatus.procAccSubFee.value = ProcessState.Running

        var result = false
        val data: ArrayList<SubjectFeeItem> = arrayListOf()

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                accountModule.getSubjectFee2(
                    schoolYearItem = schoolYearItem
                ) { result2, arrayList ->
                    if (arrayList != null) {
                        result = result2
                        data.addAll(arrayList)
                    }
                }
            }
        }.invokeOnCompletion {
            it?.printStackTrace()
            if (result) {
                uiStatus.subjectFee.clear()
                uiStatus.subjectFee.addAll(data)
                data.clear()
                uiStatus.procAccSubFee.value = ProcessState.Successful
            }
            else {
                uiStatus.procAccSubFee.value = ProcessState.Failed
            }
        }
    }

    fun fetchAccountInformation() {
        if (uiStatus.procAccInfo.value == ProcessState.Running)
            return
        uiStatus.procAccInfo.value = ProcessState.Running

        var result = false
        var data: AccountInformation? = null

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                accountModule.getAccountInformation2 { result2, item ->
                    if (item != null) {
                        result = result2
                        data = item
                    }
                }
            }
        }.invokeOnCompletion {
            it?.printStackTrace()
            if (result) {
                uiStatus.accountInformation.value = data
                uiStatus.username.value = data!!.studentId
                uiStatus.procAccInfo.value = ProcessState.Successful
            }
            else {
                uiStatus.procAccInfo.value = ProcessState.Failed
            }
        }
    }

    fun reLogin(
        silent: Boolean = false,
        reloadSubject: Boolean = true,
    ) {
        var result = false

        if (accountFileRepository.hasSavedLogin())
            uiStatus.loginState.value = LoginState.NotLoggedInButRemembered
        uiStatus.loginState.value = LoginState.LoggingIn

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                accountModule.reLogin2 {
                    result = it
                }
            }
        }.invokeOnCompletion {
            if (result) {
                if (reloadSubject) {
                    fetchAccountInformation()
                    fetchSubjectSchedule()
                    fetchSubjectFee()
                }
                uiStatus.loginState.value = LoginState.LoggedIn

                // Show snack bar
                if (!silent)
                    uiStatus.showSnackBarMessage(
                        "Successfully re-login your account!",
                        true
                    )
            }
            else {
                if (accountFileRepository.hasSavedLogin())
                    uiStatus.loginState.value = LoginState.NotLoggedInButRemembered
                else uiStatus.loginState.value = LoginState.NotTriggered
                // TODO: Notify user here!
            }
        }
    }

    fun isStoreAccount(): Boolean {
        return accountFileRepository.hasSavedLogin()
    }

    fun requestSaveChanges() {
        settings.saveSettings()
        appCache.saveCache()
    }

    init {
        settings.loadSettings()
        appCache.loadCache()
        news.getNewsGlobal()
        news.getNewsSubject()
        reLogin()
    }

    override fun onCleared() {
//        super.onCleared()
//        if (accountFileRepository.hasSavedLogin()) LoginState.NotLoggedInButRemembered
//        accountFileRepository.checkIsLoggedIn {
//            uiStatus.loginState.value =
//                if (it) LoginState.LoggedIn
//                else if (accountFileRepository.hasSavedLogin()) LoginState.NotLoggedInButRemembered
//                else LoginState.NotLoggedIn
//        }
    }
}