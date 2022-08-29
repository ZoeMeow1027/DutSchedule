package io.zoemeow.dutapp.android.viewmodel

import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.zoemeow.dutapi.objects.*
import io.zoemeow.dutapp.android.model.ProcessState
import io.zoemeow.dutapp.android.model.account.SchoolYearItem
import io.zoemeow.dutapp.android.model.appsettings.AppSettings
import io.zoemeow.dutapp.android.model.enums.LoginState
import io.zoemeow.dutapp.android.model.enums.NewsPageType
import io.zoemeow.dutapp.android.model.news.NewsCacheGlobal
import io.zoemeow.dutapp.android.model.news.NewsGroupByDate
import io.zoemeow.dutapp.android.module.AccountModule
import io.zoemeow.dutapp.android.module.FileModule
import io.zoemeow.dutapp.android.module.NewsModule
import io.zoemeow.dutapp.android.services.CustomBroadcastReceiver
import io.zoemeow.dutapp.android.utils.getCurrentDayOfWeek
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel: ViewModel() {
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
    val settings: MutableState<AppSettings> = mutableStateOf(AppSettings())

    // File Module
    private lateinit var file: FileModule

    // Account Module
    private val accountModule: AccountModule = AccountModule()

    fun fetchNewsGlobal(
        newsPageType: NewsPageType = NewsPageType.NextPage,
    ) {
        // If another instance is running, immediately stop this thread now.
        if (uiStatus.procNewsGlobal.value == ProcessState.Running)
            return

        // Set to running to avoid another instance.
        uiStatus.procNewsGlobal.value = ProcessState.Running

        // Temporary variables
        val newsFromInternet = arrayListOf<NewsGlobalItem>()

        Log.d("NewsGlobal", "Triggered getting news")

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                try {
                    newsFromInternet.addAll(NewsModule.getNewsGlobal(
                        when (newsPageType) {
                            NewsPageType.NextPage -> uiStatus.newsGlobalPageCurrent.value
                            else -> 1
                        }
                    ))
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }.invokeOnCompletion {
            it?.printStackTrace()

            if (newsFromInternet.size == 0) {
                uiStatus.procNewsGlobal.value = ProcessState.Failed
                uiStatus.showSnackBarMessage(
                    "We ran into a problem while getting your ${NewsType.Global}. " +
                            "Check your internet connection and try again."
                )
                newsFromInternet.clear()
                return@invokeOnCompletion
            }

            if (newsPageType == NewsPageType.ResetToPage1)
                uiStatus.listNewsGlobalByDate.clear()

            val newsTemp = arrayListOf<NewsGroupByDate<NewsGlobalItem>>().apply {
                addAll(uiStatus.listNewsGlobalByDate)
            }
            val newsDiff = NewsModule.getNewsGlobalDiff(
                source = newsTemp,
                target = newsFromInternet,
            )
            NewsModule.addAndCheckDuplicateNewsGlobal(
                source = newsTemp,
                target = newsDiff,
                addItemToTop = newsPageType != NewsPageType.NextPage
            )
            uiStatus.listNewsGlobalByDate.swapList(newsTemp)
            newsTemp.clear()

            when (newsPageType) {
                NewsPageType.NextPage -> {
                    uiStatus.newsGlobalPageCurrent.value += 1
                }
                NewsPageType.ResetToPage1 -> {
                    uiStatus.newsGlobalPageCurrent.value = 2
                }
                else -> {}
            }

            uiStatus.procNewsGlobal.value = ProcessState.Successful
            requestSaveCache()
        }
    }

    fun fetchNewsSubject(
        newsPageType: NewsPageType = NewsPageType.NextPage,
    ) {
        // If another instance is running, immediately stop this thread now.
        if (uiStatus.procNewsSubject.value == ProcessState.Running)
            return

        // Set to running to avoid another instance.
        uiStatus.procNewsSubject.value = ProcessState.Running

        // Temporary variables
        val newsFromInternet = arrayListOf<NewsGlobalItem>()

        Log.d("NewsSubject", "Triggered getting news")

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                try {
                    newsFromInternet.addAll(NewsModule.getNewsSubject(
                        when (newsPageType) {
                            NewsPageType.NextPage -> uiStatus.newsSubjectPageCurrent.value
                            else -> 1
                        }
                    ))
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }.invokeOnCompletion {
            it?.printStackTrace()

            if (newsFromInternet.size == 0) {
                uiStatus.procNewsSubject.value = ProcessState.Failed
                uiStatus.showSnackBarMessage(
                    "We ran into a problem while getting your ${NewsType.Global}. " +
                            "Check your internet connection and try again."
                )
                newsFromInternet.clear()
                return@invokeOnCompletion
            }

            if (newsPageType == NewsPageType.ResetToPage1)
                uiStatus.listNewsSubjectByDate.clear()

            val newsTemp = arrayListOf<NewsGroupByDate<NewsGlobalItem>>().apply {
                addAll(uiStatus.listNewsSubjectByDate)
            }
            val newsDiff = NewsModule.getNewsGlobalDiff(
                source = newsTemp,
                target = newsFromInternet,
            )
            NewsModule.addAndCheckDuplicateNewsGlobal(
                source = newsTemp,
                target = newsDiff,
                addItemToTop = newsPageType != NewsPageType.NextPage
            )
            uiStatus.listNewsSubjectByDate.swapList(newsTemp)
            newsTemp.clear()

            when (newsPageType) {
                NewsPageType.NextPage -> {
                    uiStatus.newsSubjectPageCurrent.value += 1
                }
                NewsPageType.ResetToPage1 -> {
                    uiStatus.newsSubjectPageCurrent.value = 2
                }
                else -> {}
            }

            uiStatus.procNewsSubject.value = ProcessState.Successful
            requestSaveCache()
        }
    }

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
                    runCatching {
                        if (accountModule.hasSavedLogin()) {
                            accountModule.login { result ->
                                if (!result)
                                    throw Exception("Login failed!")
                            }
                        }
                        else {
                            throw Exception("Currently not have account in application!")
                        }
                    }.onSuccess {
                        result = true
                    }.onFailure {
                        it.printStackTrace()
                        result = false
                    }
                }
                else {
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
                uiStatus.loginState.value = LoginState.LoggedIn
                uiStatus.procAccLogin.value = ProcessState.Successful
                fetchAccountInformation()
                fetchSubjectSchedule()
                fetchSubjectFee()

                requestSaveChanges()

                // Show snack bar
                uiStatus.showSnackBarMessage("Successfully login!", true)
            }
            else {
                uiStatus.loginState.value = if (accountModule.hasSavedLogin())
                    LoginState.NotLoggedInButRemembered else LoginState.NotLoggedIn
                uiStatus.procAccLogin.value = ProcessState.Failed
                requestSaveChanges()
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
            uiStatus.subjectSchedule.clear()
            uiStatus.subjectScheduleByDay.clear()
            uiStatus.subjectFee.clear()
            uiStatus.accountInformation.value = null

            // Reset state to default
            uiStatus.loginState.value = LoginState.NotLoggedIn
            uiStatus.procAccLogin.value = ProcessState.NotRanYet

            requestSaveChanges()

            // Show snack bar
            uiStatus.showSnackBarMessage("Successfully logout!", true)
        }
    }

    fun fetchSubjectSchedule(
        schoolYearItem: SchoolYearItem = settings.value.schoolYear,
    ) {
        if (uiStatus.procAccSubSch.value == ProcessState.Running)
            return
        uiStatus.procAccSubSch.value = ProcessState.Running

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
    }

    fun fetchSubjectFee(
        schoolYearItem: SchoolYearItem = settings.value.schoolYear,
    ) {
        if (uiStatus.procAccSubFee.value == ProcessState.Running)
            return
        uiStatus.procAccSubFee.value = ProcessState.Running

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

        if (accountModule.hasSavedLogin())
            uiStatus.loginState.value = LoginState.NotLoggedInButRemembered
        uiStatus.loginState.value = LoginState.LoggingIn

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
                    fetchSubjectSchedule()
                    fetchSubjectFee()
                }
                uiStatus.loginState.value = LoginState.LoggedIn

                if (!silent) {
                    CoroutineScope(Dispatchers.Main).launch {
                        // Show snack bar
                        uiStatus.showSnackBarMessage(
                            "Successfully re-login your account!",
                            true
                        )
                    }
                }
            }
            else {
                if (accountModule.hasSavedLogin())
                    uiStatus.loginState.value = LoginState.NotLoggedInButRemembered
                else uiStatus.loginState.value = LoginState.NotTriggered
                // TODO: Notify user here!
            }
            requestSaveChanges()
        }
    }

    fun isStoreAccount(): Boolean {
        return accountModule.hasSavedLogin()
    }

    fun requestSaveChanges() {
        file.saveAppSettings(
            appSettings = settings.value
        )
        accountModule.saveSettings { accountSession ->
            file.saveAccountSettings(
                accountSession = accountSession
            )
        }
    }

    fun requestSaveCache() {
        file.saveCacheNewsGlobal(NewsCacheGlobal(
            newsListByDate = arrayListOf<NewsGroupByDate<NewsGlobalItem>>().apply {
                addAll(uiStatus.listNewsGlobalByDate)
            },
            pageCurrent = uiStatus.newsGlobalPageCurrent.value
        ))
        file.saveCacheNewsSubject(NewsCacheGlobal(
            newsListByDate = arrayListOf<NewsGroupByDate<NewsGlobalItem>>().apply {
                addAll(uiStatus.listNewsSubjectByDate)
            },
            pageCurrent = uiStatus.newsSubjectPageCurrent.value
        ))
    }

    fun initialize() {
        file = FileModule(uiStatus.pMainActivity.value!!)
        settings.value = file.getAppSettings()

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                file.getCacheNewsGlobal().apply {
                    uiStatus.newsGlobalPageCurrent.value = pageCurrent
                    uiStatus.listNewsGlobalByDate.addAll(newsListByDate)
                }

                file.getCacheNewsSubject().apply {
                    uiStatus.newsSubjectPageCurrent.value = pageCurrent
                    uiStatus.listNewsSubjectByDate.addAll(newsListByDate)
                }

                accountModule.loadSettings(file.getAccountSettings())

                val mMessageReceiver: BroadcastReceiver
                object : CustomBroadcastReceiver() {
                    override fun newsReloadRequest() {
                        CoroutineScope(Dispatchers.Main).launch {
                            withContext(Dispatchers.IO) {
                                file.getCacheNewsGlobal().apply {
                                    uiStatus.newsGlobalPageCurrent.value = pageCurrent
                                    uiStatus.listNewsGlobalByDate.swapList(newsListByDate)
                                }

                                file.getCacheNewsSubject().apply {
                                    uiStatus.newsSubjectPageCurrent.value = pageCurrent
                                    uiStatus.listNewsSubjectByDate.swapList(newsListByDate)
                                }
                            }
                        }
                    }
                }.apply {
                    mMessageReceiver = this
                }

                LocalBroadcastManager.getInstance(uiStatus.pMainActivity.value!!).registerReceiver(
                    mMessageReceiver,
                    IntentFilter("NewsReload")
                )
            }
        }.invokeOnCompletion {
            reLogin()
            it?.printStackTrace()
        }
    }

    override fun onCleared() {
//        requestSaveChanges()
//        super.onCleared()
//        initialize()
//        if (accountFileRepository.hasSavedLogin()) LoginState.NotLoggedInButRemembered
//        accountFileRepository.checkIsLoggedIn {
//            uiStatus.loginState.value =
//                if (it) LoginState.LoggedIn
//                else if (accountFileRepository.hasSavedLogin()) LoginState.NotLoggedInButRemembered
//                else LoginState.NotLoggedIn
//        }
    }
}

fun <T> SnapshotStateList<T>.swapList(element: Collection<T>) {
    clear()
    addAll(element)
}