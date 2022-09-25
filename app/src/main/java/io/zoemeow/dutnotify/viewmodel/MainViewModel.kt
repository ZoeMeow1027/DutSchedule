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
import io.zoemeow.dutnotify.model.enums.LoginState
import io.zoemeow.dutnotify.model.enums.ProcessState
import io.zoemeow.dutnotify.model.enums.ServiceBroadcastOptions
import io.zoemeow.dutnotify.model.news.NewsGroupByDate
import io.zoemeow.dutnotify.module.FileModule
import io.zoemeow.dutnotify.receiver.AccountBroadcastReceiver
import io.zoemeow.dutnotify.receiver.AppBroadcastReceiver
import io.zoemeow.dutnotify.receiver.NewsBroadcastReceiver
import io.zoemeow.dutnotify.utils.DUTDateUtils
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
     * Main Activity: Check if current theme is dark mode.
     */
    val isDarkTheme: MutableState<Boolean> = mutableStateOf(false)

    // App settings
    val appSettings: MutableState<AppSettings> = mutableStateOf(AppSettings())

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

    fun requestSaveChanges() {
        try {
            Log.d("AppSettings", "Saved changes!")
            file.saveAppSettings(appSettings = appSettings.value)
        } catch (ex: Exception) {
            Log.d("AppSettings", "Error while saving changes!")
            ex.printStackTrace()
        }
    }

    val News_Process_Global = mutableStateOf(ProcessState.NotRanYet)
    val News_Data_Global = mutableStateListOf<NewsGroupByDate<NewsGlobalItem>>()
    val News_Process_Subject = mutableStateOf(ProcessState.NotRanYet)
    val News_Data_Subject = mutableStateListOf<NewsGroupByDate<NewsSubjectItem>>()

    private fun getNewsBroadcastReceiver(): NewsBroadcastReceiver {
        // MainActivity::class.java.name
        object : NewsBroadcastReceiver() {
            override fun onStatusReceived(key: String, value: String) {
                Log.d("NewsService", "onStatusReceived - $key: $value")
                when (key) {
                    ServiceBroadcastOptions.ACTION_NEWS_FETCHGLOBAL -> {
                        when (value) {
                            ServiceBroadcastOptions.STATUS_SUCCESSFUL -> {
                                News_Process_Global.value = ProcessState.Successful
                            }
                            ServiceBroadcastOptions.STATUS_FAILED -> {
                                News_Process_Global.value = ProcessState.Failed
                            }
                            ServiceBroadcastOptions.STATUS_PROCESSING -> {
                                News_Process_Global.value = ProcessState.Running
                            }
                        }
                    }
                    ServiceBroadcastOptions.ACTION_NEWS_FETCHSUBJECT -> {
                        when (value) {
                            ServiceBroadcastOptions.STATUS_SUCCESSFUL -> {
                                News_Process_Subject.value = ProcessState.Successful
                            }
                            ServiceBroadcastOptions.STATUS_FAILED -> {
                                News_Process_Subject.value = ProcessState.Failed
                            }
                            ServiceBroadcastOptions.STATUS_PROCESSING -> {
                                News_Process_Subject.value = ProcessState.Running
                            }
                        }
                    }
                }
            }

            @Suppress("UNCHECKED_CAST")
            override fun onDataReceived(key: String, data: Any) {
                Log.d("NewsService", "onDataReceived - $key")
                when (key) {
                    ServiceBroadcastOptions.ACTION_NEWS_FETCHGLOBAL -> {
                        News_Data_Global.clear()
                        News_Data_Global.addAll(data as ArrayList<NewsGroupByDate<NewsGlobalItem>>)
                    }
                    ServiceBroadcastOptions.ACTION_NEWS_FETCHSUBJECT -> {
                        News_Data_Subject.clear()
                        News_Data_Subject.addAll(data as ArrayList<NewsGroupByDate<NewsSubjectItem>>)
                    }
                }
            }

            override fun onErrorReceived(key: String, msg: String) {}
        }.apply { return this }
    }

    val Account_HasSaved = mutableStateOf(false)
    val Account_LoginProcess = mutableStateOf(LoginState.NotTriggered)
    val Account_Process_SubjectSchedule: MutableState<ProcessState> =
        mutableStateOf(ProcessState.NotRanYet)
    val Account_Process_SubjectFee: MutableState<ProcessState> =
        mutableStateOf(ProcessState.NotRanYet)
    val Account_Process_AccountInformation: MutableState<ProcessState> =
        mutableStateOf(ProcessState.NotRanYet)
    val Account_Data_SubjectSchedule: SnapshotStateList<SubjectScheduleItem> = mutableStateListOf()
    val Account_Data_SubjectFee: SnapshotStateList<SubjectFeeItem> = mutableStateListOf()
    val Account_Data_AccountInformation: MutableState<AccountInformation?> = mutableStateOf(null)
    val Account_Data_SubjectScheduleByDay: SnapshotStateList<SubjectScheduleItem> =
        mutableStateListOf()

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
                    ServiceBroadcastOptions.ACTION_ACCOUNT_LOGIN -> {
                        when (value) {
                            ServiceBroadcastOptions.STATUS_SUCCESSFUL -> {
                                Account_LoginProcess.value = LoginState.LoggedIn
                                Account_HasSaved.value = true

                                showSnackBarMessage("Successfully login!", true)
                            }
                            ServiceBroadcastOptions.STATUS_FAILED -> {
                                Account_LoginProcess.value = LoginState.NotLoggedIn
                                Account_HasSaved.value = false
                            }
                            ServiceBroadcastOptions.STATUS_PROCESSING -> {
                                Account_LoginProcess.value = LoginState.LoggingIn
                                Account_HasSaved.value = false
                            }
                        }
                    }
                    ServiceBroadcastOptions.ACTION_ACCOUNT_LOGINSTARTUP -> {
                        when (value) {
                            ServiceBroadcastOptions.STATUS_SUCCESSFUL -> {
                                Account_LoginProcess.value = LoginState.LoggedIn

                                showSnackBarMessage("Successfully re-login!", true)
                            }
                            ServiceBroadcastOptions.STATUS_FAILED -> {
                                if (Account_HasSaved.value) {
                                    Account_LoginProcess.value = LoginState.NotLoggedInButRemembered
                                } else {
                                    Account_LoginProcess.value = LoginState.NotLoggedIn
                                }
                            }
                        }
                    }
                    ServiceBroadcastOptions.ACTION_ACCOUNT_LOGOUT -> {
                        when (value) {
                            ServiceBroadcastOptions.STATUS_SUCCESSFUL -> {
                                Account_HasSaved.value = false
                                showSnackBarMessage("Successfully logout!", true)
                            }
                        }
                    }
                    ServiceBroadcastOptions.ACTION_ACCOUNT_SUBJECTSCHEDULE -> {
                        when (value) {
                            ServiceBroadcastOptions.STATUS_PROCESSING -> {
                                Account_Process_SubjectSchedule.value = ProcessState.Running
                            }
                            ServiceBroadcastOptions.STATUS_SUCCESSFUL -> {
                                Account_Process_SubjectSchedule.value = ProcessState.Successful
                            }
                            ServiceBroadcastOptions.STATUS_FAILED -> {
                                Account_Process_SubjectSchedule.value = ProcessState.Failed
                            }
                        }
                    }
                    ServiceBroadcastOptions.ACTION_ACCOUNT_SUBJECTFEE -> {
                        when (value) {
                            ServiceBroadcastOptions.STATUS_PROCESSING -> {
                                Account_Process_SubjectFee.value = ProcessState.Running
                            }
                            ServiceBroadcastOptions.STATUS_SUCCESSFUL -> {
                                Account_Process_SubjectFee.value = ProcessState.Successful
                            }
                            ServiceBroadcastOptions.STATUS_FAILED -> {
                                Account_Process_SubjectFee.value = ProcessState.Failed
                            }
                        }
                    }
                    ServiceBroadcastOptions.ACTION_ACCOUNT_ACCOUNTINFORMATION -> {
                        when (value) {
                            ServiceBroadcastOptions.STATUS_PROCESSING -> {
                                Account_Process_AccountInformation.value = ProcessState.Running
                            }
                            ServiceBroadcastOptions.STATUS_SUCCESSFUL -> {
                                Account_Process_AccountInformation.value = ProcessState.Successful
                            }
                            ServiceBroadcastOptions.STATUS_FAILED -> {
                                Account_Process_AccountInformation.value = ProcessState.Failed
                            }
                        }
                    }
                    else -> {}
                }
            }

            @Suppress("UNCHECKED_CAST")
            override fun onDataReceived(key: String, data: Any) {
                Log.d("AccountService", "Triggered data")
                when (key) {
                    ServiceBroadcastOptions.ACTION_ACCOUNT_ACCOUNTINFORMATION -> {
                        Account_Data_AccountInformation.value = data as AccountInformation
                    }
                    ServiceBroadcastOptions.ACTION_ACCOUNT_SUBJECTSCHEDULE -> {
                        Account_Data_SubjectSchedule.apply {
                            clear()
                            addAll(data as ArrayList<SubjectScheduleItem>)
                        }
                        filterSubjectScheduleByDay(
                            week = DUTDateUtils.getDUTWeek(),
                            dayOfWeek = if (DUTDateUtils.getCurrentDayOfWeek() - 1 == 0) 7 else DUTDateUtils.getCurrentDayOfWeek() - 1
                        )
                    }
                    ServiceBroadcastOptions.ACTION_ACCOUNT_SUBJECTFEE -> {
                        Account_Data_SubjectFee.apply {
                            clear()
                            addAll(data as ArrayList<SubjectFeeItem>)
                        }
                    }
                    ServiceBroadcastOptions.ACTION_ACCOUNT_GETSTATUS_HASSAVEDLOGIN -> {
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
        LocalBroadcastManager.getInstance(application.applicationContext).apply {
            unregisterReceiver(getNewsBroadcastReceiver())
            unregisterReceiver(getAccountBroadcastReceiver())
        }
        super.onCleared()
    }

    private var initOnce: Boolean = false

    init {
        run {
            if (initOnce)
                return@run

            appSettings.value = file.getAppSettings()

            LocalBroadcastManager.getInstance(application.applicationContext).apply {
                registerReceiver(
                    getNewsBroadcastReceiver(),
                    IntentFilter().apply {
                        addAction(ServiceBroadcastOptions.ACTION_NEWS_FETCHGLOBAL)
                        addAction(ServiceBroadcastOptions.ACTION_NEWS_FETCHSUBJECT)
                    }
                )
                registerReceiver(
                    getAccountBroadcastReceiver(),
                    IntentFilter().apply {
                        addAction(ServiceBroadcastOptions.ACTION_ACCOUNT_LOGIN)
                        addAction(ServiceBroadcastOptions.ACTION_ACCOUNT_LOGINSTARTUP)
                        addAction(ServiceBroadcastOptions.ACTION_ACCOUNT_LOGOUT)
                        addAction(ServiceBroadcastOptions.ACTION_ACCOUNT_SUBJECTSCHEDULE)
                        addAction(ServiceBroadcastOptions.ACTION_ACCOUNT_SUBJECTFEE)
                        addAction(ServiceBroadcastOptions.ACTION_ACCOUNT_ACCOUNTINFORMATION)
                        addAction(ServiceBroadcastOptions.ACTION_ACCOUNT_GETSTATUS_HASSAVEDLOGIN)
                    }
                )
            }

            Log.d("MainViewModel", "Initialized")
            initOnce = true
        }
    }
}