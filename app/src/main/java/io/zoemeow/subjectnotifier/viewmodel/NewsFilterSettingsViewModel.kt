package io.zoemeow.subjectnotifier.viewmodel

import android.app.Application
import android.content.IntentFilter
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dagger.hilt.android.lifecycle.HiltViewModel
import io.zoemeow.dutapi.objects.accounts.SubjectScheduleItem
import io.zoemeow.subjectnotifier.view.NewsFilterSettingsActivity
import io.zoemeow.subjectnotifier.model.appsettings.AppSettings
import io.zoemeow.subjectnotifier.model.appsettings.SubjectCode
import io.zoemeow.subjectnotifier.model.enums.LoginState
import io.zoemeow.subjectnotifier.model.enums.ProcessState
import io.zoemeow.subjectnotifier.model.enums.ServiceBroadcastOptions
import io.zoemeow.subjectnotifier.module.FileModule
import io.zoemeow.subjectnotifier.receiver.AccountBroadcastReceiver
import javax.inject.Inject

@Suppress("PropertyName")
@HiltViewModel
class NewsFilterSettingsViewModel @Inject constructor(
    private val file: FileModule,
    private val application: Application,
) : ViewModel() {
    /**
     * Store all selected subject which user chosen.
     */
    val selectedSubjects = mutableStateListOf<SubjectCode>()

    /**
     * Store all available subject which didn't in selected subjects.
     */
    val availableSubjectFromAccount = mutableStateListOf<SubjectScheduleItem>()

    /**
     * Define selected item index in selected subjects list.
     */
    val selectedAvailableSubjectFromAccountIndex = mutableStateOf(0)

    /**
     * Define selected item name. This will also display selected subject name
     * in outlined text box in 'add by subject schedule list' area.
     */
    val selectedAvailableSubjectFromAccountName = mutableStateOf("")

    /**
     * Index for selected main body for expend.
     */
    val selectedMainBodyIndex = mutableStateOf(0)

    /**
     * Detect if a setting in this activity has changed.
     */
    val modifiedSettings = mutableStateOf(false)

    /**
     * Show 'Unsaved changes' dialog.
     */
    val modifiedSettingsDialog = mutableStateOf(false)

    // App settings
    val appSettings: MutableState<AppSettings> = mutableStateOf(AppSettings())

    val Account_HasSaved = mutableStateOf(false)
    val Account_LoginProcess = mutableStateOf(LoginState.NotTriggered)
    val Account_Process_SubjectSchedule: MutableState<ProcessState> =
        mutableStateOf(ProcessState.NotRanYet)
    val Account_Data_SubjectSchedule: SnapshotStateList<SubjectScheduleItem> = mutableStateListOf()

    fun requestSaveChanges() {
        file.saveAppSettings(
            appSettings = appSettings.value
        )
    }

    private fun getAccountBroadcastReceiver(): AccountBroadcastReceiver {
        object : AccountBroadcastReceiver(NewsFilterSettingsActivity::class.java.name) {
            override fun onStatusReceived(key: String, value: String) {
                Log.d("AccountService", "NewsFilter - AccountBroadcastReceiver - $key: $value")
                when (key) {
                    ServiceBroadcastOptions.ACTION_ACCOUNT_LOGINSTARTUP -> {
                        when (value) {
                            ServiceBroadcastOptions.STATUS_SUCCESSFUL -> {
                                Account_LoginProcess.value = LoginState.LoggedIn
                                requestSaveChanges()
                            }
                            ServiceBroadcastOptions.STATUS_FAILED -> {
                                if (Account_HasSaved.value) {
                                    Account_LoginProcess.value = LoginState.NotLoggedInButRemembered
                                } else {
                                    Account_LoginProcess.value = LoginState.NotLoggedIn
                                }
                                requestSaveChanges()
                            }
                            ServiceBroadcastOptions.STATUS_PROCESSING -> {
                                Account_LoginProcess.value = LoginState.LoggingIn
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
                    else -> {}
                }
            }

            @Suppress("UNCHECKED_CAST")
            override fun onDataReceived(key: String, data: Any) {
                Log.d("AccountService", "Triggered data")
                when (key) {
                    ServiceBroadcastOptions.ACTION_ACCOUNT_SUBJECTSCHEDULE -> {
                        Account_Data_SubjectSchedule.apply {
                            clear()
                            addAll(data as ArrayList<SubjectScheduleItem>)
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
                    addAction(ServiceBroadcastOptions.ACTION_ACCOUNT_LOGINSTARTUP)
                    addAction(ServiceBroadcastOptions.ACTION_ACCOUNT_SUBJECTSCHEDULE)
                    addAction(ServiceBroadcastOptions.ACTION_ACCOUNT_GETSTATUS_HASSAVEDLOGIN)
                }
            )

            appSettings.value = file.getAppSettings()

            Log.d("NewsSubjectFilterVM", "Initialized")
            initOnce = true
        }
    }
}