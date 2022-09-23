package io.zoemeow.dutnotify.viewmodel

import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dagger.hilt.android.lifecycle.HiltViewModel
import io.zoemeow.dutapi.objects.accounts.AccountInformation
import io.zoemeow.dutapi.objects.accounts.SubjectFeeItem
import io.zoemeow.dutapi.objects.accounts.SubjectScheduleItem
import io.zoemeow.dutapi.objects.news.NewsGlobalItem
import io.zoemeow.dutapi.objects.news.NewsSubjectItem
import io.zoemeow.dutnotify.MainActivity
import io.zoemeow.dutnotify.model.appsettings.AppSettings
import io.zoemeow.dutnotify.model.enums.ServiceCode
import io.zoemeow.dutnotify.model.enums.LoginState
import io.zoemeow.dutnotify.model.enums.ProcessState
import io.zoemeow.dutnotify.model.news.NewsCache
import io.zoemeow.dutnotify.model.news.NewsGroupByDate
import io.zoemeow.dutnotify.module.FileModule
import io.zoemeow.dutnotify.receiver.AccountBroadcastReceiver
import io.zoemeow.dutnotify.receiver.AppBroadcastReceiver
import io.zoemeow.dutnotify.utils.DUTDateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@Suppress("PropertyName")
@HiltViewModel
class MainViewModel @Inject constructor(
    private val file: FileModule,
    private val application: Application,
) : ViewModel() {
    val subjectScheduleItem: MutableState<SubjectScheduleItem?> = mutableStateOf(null)
    val subjectScheduleEnabled: MutableState<Boolean> = mutableStateOf(false)

    /**
     * Drawable and painter for background image.
     */
    val mainActivityBackgroundDrawable: MutableState<Drawable?> = mutableStateOf(null)

    /**
     * Main Activity: Check if current theme is dark mode.
     */
    val mainActivityIsDarkTheme: MutableState<Boolean> = mutableStateOf(false)

    // App settings
    val appSettings: MutableState<AppSettings> = mutableStateOf(AppSettings())

    // News UI area
    val newsDataStore: NewsDataStore = NewsDataStore(this)

    fun requestSaveChanges() {
        file.saveAppSettings(
            appSettings = appSettings.value
        )
    }

    fun showSnackBarMessage(
        title: String,
        forceCloseOld: Boolean = false
    ) {
        val intent = Intent(AppBroadcastReceiver.SNACKBARMESSAGE)
        intent.putExtra(
            AppBroadcastReceiver.SNACKBARMESSAGE_TEXT,
            title
        )
        intent.putExtra(
            AppBroadcastReceiver.SNACKBARMESSAGE_CLOSEOLDMSG,
            forceCloseOld
        )
        LocalBroadcastManager.getInstance(application.applicationContext)
            .sendBroadcast(intent)
    }

    fun requestSaveCache() {
        file.saveCacheNewsGlobal(
            NewsCache(
                newsListByDate = arrayListOf<NewsGroupByDate<NewsGlobalItem>>().apply {
                    addAll(newsDataStore.listNewsGlobalByDate)
                },
                pageCurrent = newsDataStore.newsGlobalPageCurrent.value
            )
        )
        file.saveCacheNewsSubject(
            NewsCache(
                newsListByDate = arrayListOf<NewsGroupByDate<NewsSubjectItem>>().apply {
                    addAll(newsDataStore.listNewsSubjectByDate)
                },
                pageCurrent = newsDataStore.newsSubjectPageCurrent.value
            )
        )
    }

    private fun getAppBroadcastReceiver(): AppBroadcastReceiver {
        object : AppBroadcastReceiver() {
            override fun onSnackBarMessage(title: String?, forceCloseOld: Boolean) {}
            override fun onNewsScrollToTopRequested() { }
            override fun onPermissionRequested(
                permission: String?,
                granted: Boolean,
                notifyToUser: Boolean
            ) { }

            override fun onNewsReloadRequested() {
                CoroutineScope(Dispatchers.Main).launch {
                    withContext(Dispatchers.IO) {
                        file.getCacheNewsGlobal().apply {
                            newsDataStore.newsGlobalPageCurrent.value = pageCurrent
                            newsDataStore.listNewsGlobalByDate.swapList(newsListByDate)
                        }
                        file.getCacheNewsSubject().apply {
                            newsDataStore.newsSubjectPageCurrent.value = pageCurrent
                            newsDataStore.listNewsSubjectByDate.swapList(newsListByDate)
                        }
                    }
                }
            }

            override fun onAccountReloadRequested(newsType: String) {
                when (newsType) {
                    ACCOUNT_SUBJECTSCHEDULE_RELOADREQUESTED -> {

                    }
                    ACCOUNT_SUBJECTFEE_RELOADREQUESTED -> {
                    }
                    ACCOUNT_ACCINFORMATION_RELOADREQUESTED -> {
                    }
                }
            }

            override fun onSettingsReloadRequested() {
                appSettings.value = file.getAppSettings()
            }
        }.apply {
            return this
        }
    }

    val Account_HasSaved = mutableStateOf(false)
    val Account_LoginProcess = mutableStateOf(LoginState.NotTriggered)
    val Account_Process_SubjectSchedule: MutableState<ProcessState> = mutableStateOf(ProcessState.NotRanYet)
    val Account_Process_SubjectFee: MutableState<ProcessState> = mutableStateOf(ProcessState.NotRanYet)
    val Account_Process_AccountInformation: MutableState<ProcessState> = mutableStateOf(ProcessState.NotRanYet)
    val Account_Data_SubjectSchedule: SnapshotStateList<SubjectScheduleItem> = mutableStateListOf()
    val Account_Data_SubjectFee: SnapshotStateList<SubjectFeeItem> = mutableStateListOf()
    val Account_Data_AccountInformation: MutableState<AccountInformation?> = mutableStateOf(null)
    val Account_Data_SubjectScheduleByDay: SnapshotStateList<SubjectScheduleItem> = mutableStateListOf()

    fun filterSubjectScheduleByDay(
        week: Int = DUTDateUtils.getDUTWeek(),
        dayOfWeek: Int = DUTDateUtils.getCurrentDayOfWeek() - 1
    ) {
        val temp = arrayListOf<SubjectScheduleItem>()
        val dayOfWeekModified = if (dayOfWeek > 6) 0 else dayOfWeek

        temp.addAll(
            Account_Data_SubjectSchedule.filter { item ->
                // First query: Week range
                item.subjectStudy.weekList.any { weekItem -> weekItem.start <= week && week <= weekItem.end } &&
                        // Second query: Subject day of week
                        item.subjectStudy.scheduleList.any { dayOfWeek -> dayOfWeek.dayOfWeek == dayOfWeekModified }
            }.sortedBy { item ->
                item.subjectStudy.scheduleList.filter { week ->
                    week.dayOfWeek == dayOfWeekModified
                }[0].lesson.start
            }
        )

        Account_Data_SubjectScheduleByDay.clear()
        Account_Data_SubjectScheduleByDay.addAll(temp)
        temp.clear()
    }

    private fun getAccountBroadcastReceiver(): AccountBroadcastReceiver {
        object : AccountBroadcastReceiver(packageFilter = MainActivity::class.java.name) {
            override fun onStatusReceived(key: String, value: String) {
                Log.d("AccountService", "AccountBroadcastReceiver - $key: $value")
                when (key) {
                    ServiceCode.ACTION_ACCOUNT_LOGIN -> {
                        when (value) {
                            ServiceCode.STATUS_SUCCESSFUL -> {
                                Account_LoginProcess.value = LoginState.LoggedIn
                                Account_HasSaved.value = true

                                requestSaveChanges()
                                showSnackBarMessage("Successfully login!", true)
                            }
                            ServiceCode.STATUS_FAILED -> {
                                Account_LoginProcess.value = LoginState.NotLoggedIn
                                Account_HasSaved.value = false

                                requestSaveChanges()
                            }
                            ServiceCode.STATUS_PROCESSING -> {
                                Account_LoginProcess.value = LoginState.LoggingIn
                                Account_HasSaved.value = false
                            }
                        }
                    }
                    ServiceCode.ACTION_ACCOUNT_LOGINSTARTUP -> {
                        when (value) {
                            ServiceCode.STATUS_SUCCESSFUL -> {
                                Account_LoginProcess.value = LoginState.LoggedIn
                                requestSaveChanges()

                                showSnackBarMessage("Successfully re-login!", true)
                            }
                            ServiceCode.STATUS_FAILED -> {
                                if (Account_HasSaved.value) {
                                    Account_LoginProcess.value = LoginState.NotLoggedInButRemembered
                                } else {
                                    Account_LoginProcess.value = LoginState.NotLoggedIn
                                }
                                requestSaveChanges()
                            }
                        }
                    }
                    ServiceCode.ACTION_ACCOUNT_LOGOUT -> {
                        when (value) {
                            ServiceCode.STATUS_SUCCESSFUL -> {
                                Account_HasSaved.value = false
                                showSnackBarMessage("Successfully logout!", true)
                            }
                        }
                    }
                    ServiceCode.ACTION_ACCOUNT_SUBJECTSCHEDULE -> {
                        when (value) {
                            ServiceCode.STATUS_PROCESSING -> {
                                Account_Process_SubjectSchedule.value = ProcessState.Running
                            }
                            ServiceCode.STATUS_SUCCESSFUL -> {
                                Account_Process_SubjectSchedule.value = ProcessState.Successful
                            }
                            ServiceCode.STATUS_FAILED -> {
                                Account_Process_SubjectSchedule.value = ProcessState.Failed
                            }
                        }
                    }
                    ServiceCode.ACTION_ACCOUNT_SUBJECTFEE -> {
                        when (value) {
                            ServiceCode.STATUS_PROCESSING -> {
                                Account_Process_SubjectFee.value = ProcessState.Running
                            }
                            ServiceCode.STATUS_SUCCESSFUL -> {
                                Account_Process_SubjectFee.value = ProcessState.Successful
                            }
                            ServiceCode.STATUS_FAILED -> {
                                Account_Process_SubjectFee.value = ProcessState.Failed
                            }
                        }
                    }
                    ServiceCode.ACTION_ACCOUNT_ACCOUNTINFORMATION -> {
                        when (value) {
                            ServiceCode.STATUS_PROCESSING -> {
                                Account_Process_AccountInformation.value = ProcessState.Running
                            }
                            ServiceCode.STATUS_SUCCESSFUL -> {
                                Account_Process_AccountInformation.value = ProcessState.Successful
                            }
                            ServiceCode.STATUS_FAILED -> {
                                Account_Process_AccountInformation.value = ProcessState.Failed
                            }
                        }
                    }
                    else -> { }
                }
            }

            @Suppress("UNCHECKED_CAST")
            override fun onDataReceived(key: String, data: Any) {
                Log.d("AccountService", "Triggered data")
                when (key) {
                    ServiceCode.ACTION_ACCOUNT_ACCOUNTINFORMATION -> {
                        Account_Data_AccountInformation.value = data as AccountInformation
                    }
                    ServiceCode.ACTION_ACCOUNT_SUBJECTSCHEDULE -> {
                        Account_Data_SubjectSchedule.apply {
                            clear()
                            addAll(data as ArrayList<SubjectScheduleItem>)
                        }
                        filterSubjectScheduleByDay(
                            week = DUTDateUtils.getDUTWeek(),
                            dayOfWeek = if (DUTDateUtils.getCurrentDayOfWeek() - 1 == 0) 7 else DUTDateUtils.getCurrentDayOfWeek() - 1
                        )
                    }
                    ServiceCode.ACTION_ACCOUNT_SUBJECTFEE -> {
                        Account_Data_SubjectFee.apply {
                            clear()
                            addAll(data as ArrayList<SubjectFeeItem>)
                        }
                    }
                    ServiceCode.ACTION_ACCOUNT_GETSTATUS_HASSAVEDLOGIN -> {
                        Account_HasSaved.value = data as Boolean
                    }
                }
                // TODO("Not yet implemented")
            }

            override fun onErrorReceived(key: String, msg: String) {
                Log.d("AccountService", "Triggered error")
                // TODO("Not yet implemented")
            }
        }.apply {
            return this
        }
    }

    override fun onCleared() {
        Log.d("MainViewModel", "Destroyed")
        LocalBroadcastManager.getInstance(application.applicationContext).unregisterReceiver(
            getAppBroadcastReceiver()
        )
        LocalBroadcastManager.getInstance(application.applicationContext).unregisterReceiver(
            getAccountBroadcastReceiver()
        )
        super.onCleared()
    }

    private var initOnce: Boolean = false
    init {
        run {
            if (initOnce)
                return@run

            LocalBroadcastManager.getInstance(application.applicationContext).registerReceiver(
                getAccountBroadcastReceiver(),
                IntentFilter().apply {
                    addAction(ServiceCode.ACTION_ACCOUNT_LOGIN)
                    addAction(ServiceCode.ACTION_ACCOUNT_LOGINSTARTUP)
                    addAction(ServiceCode.ACTION_ACCOUNT_LOGOUT)
                    addAction(ServiceCode.ACTION_ACCOUNT_SUBJECTSCHEDULE)
                    addAction(ServiceCode.ACTION_ACCOUNT_SUBJECTFEE)
                    addAction(ServiceCode.ACTION_ACCOUNT_ACCOUNTINFORMATION)
                    addAction(ServiceCode.ACTION_ACCOUNT_GETSTATUS_HASSAVEDLOGIN)
                }
            )

            LocalBroadcastManager.getInstance(application.applicationContext).registerReceiver(
                getAppBroadcastReceiver(),
                IntentFilter(AppBroadcastReceiver.NEWS_RELOADREQUESTED_SERVICE_ACTIVITY)
            )

            appSettings.value = file.getAppSettings()

            // Reload news from cache
            val intent = Intent(AppBroadcastReceiver.NEWS_RELOADREQUESTED_SERVICE_ACTIVITY)
            LocalBroadcastManager.getInstance(application.applicationContext).sendBroadcast(intent)

            Log.d("MainViewModel", "Initialized")
            initOnce = true
        }
    }
}

fun <T> SnapshotStateList<T>.swapList(element: Collection<T>) {
    clear()
    addAll(element)
}