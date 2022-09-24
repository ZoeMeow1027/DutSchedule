package io.zoemeow.dutnotify.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.zoemeow.dutnotify.model.account.AccountCache
import io.zoemeow.dutnotify.model.account.SchoolYearItem
import io.zoemeow.dutnotify.model.appsettings.AppSettings
import io.zoemeow.dutnotify.model.enums.ServiceCode
import io.zoemeow.dutnotify.module.AccountModule
import io.zoemeow.dutnotify.module.FileModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Suppress("SpellCheckingInspection", "DEPRECATION")
class AccountService: Service() {
    override fun onCreate() {
        initialize()
        loadSettings()
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            if (intent == null)
                throw Exception("Intent is null here!")

            val callFrom = intent.getStringExtra(ServiceCode.SOURCE_COMPONENT)
                ?: throw Exception("ServiceCode.SOURCE_COMPONENT is missing!")

            Log.d("AccountService", "Triggered from: $callFrom")

            when (intent.getStringExtra(ServiceCode.ACTION)) {
                ServiceCode.ACTION_ACCOUNT_LOGINSTARTUP -> {
                    Log.d("AccountService", "Triggered relogin")
                    val preload = intent.getBooleanExtra(ServiceCode.ARGUMENT_ACCOUNT_LOGINSTARTUP_PRELOAD, false)
                    // ServiceCode.ACTION_GETSTATUS_HASSAVEDLOGIN
                    sendToBroadcast(
                        action = ServiceCode.ACTION_ACCOUNT_GETSTATUS_HASSAVEDLOGIN,
                        data = accModule.hasSavedLogin(),
                        callFrom = callFrom
                    )
                    // Preload cache of Subject schedule, subject fee and account information
                    // subject schedule
                    sendToBroadcast(
                        action = ServiceCode.ACTION_ACCOUNT_SUBJECTSCHEDULE,
                        data = appCache.subjectScheduleList,
                        callFrom = callFrom
                    )
                    // Subject fee
                    sendToBroadcast(
                        action = ServiceCode.ACTION_ACCOUNT_SUBJECTFEE,
                        data = appCache.subjectFeeList,
                        callFrom = callFrom
                    )
                    // Account information
                    sendToBroadcast(
                        action = ServiceCode.ACTION_ACCOUNT_ACCOUNTINFORMATION,
                        data = appCache.accountInformation,
                        callFrom = callFrom
                    )
                    // Re-login account
                    reLogin(
                        preload = preload,
                        callFrom = callFrom
                    )
                }
                ServiceCode.ACTION_ACCOUNT_LOGIN -> {
                    val username = intent.getStringExtra(ServiceCode.ARGUMENT_ACCOUNT_LOGIN_USERNAME)
                    val password = intent.getStringExtra(ServiceCode.ARGUMENT_ACCOUNT_LOGIN_PASSWORD)
                    val remember = intent.getBooleanExtra(ServiceCode.ARGUMENT_ACCOUNT_LOGIN_REMEMBERED, false)
                    val preload = intent.getBooleanExtra(ServiceCode.ARGUMENT_ACCOUNT_LOGIN_PRELOAD, false)
                    if (username == null || password == null) {
                        sendToBroadcast(
                            action = ServiceCode.ACTION_ACCOUNT_LOGIN,
                            status = ServiceCode.STATUS_FAILED,
                            callFrom = callFrom
                        )
                    } else login(username, password, remember, preload, callFrom = callFrom)
                }
                ServiceCode.ACTION_ACCOUNT_LOGOUT -> {
                    Log.d("AccountService", "Triggered logout")
                    appCache.subjectScheduleList.clear()
                    appCache.subjectFeeList.clear()
                    appCache.accountInformation = null
                    logout(callFrom = callFrom)
                }
                ServiceCode.ACTION_ACCOUNT_SUBJECTSCHEDULE -> {
                    try {
                        val schoolYearItem = intent.getSerializableExtra(ServiceCode.ARGUMENT_ACCOUNT_SUBJECTSCHEDULE_SCHOOLYEAR) as SchoolYearItem
                        fetchSubjectSchedule(
                            schoolYearItem = schoolYearItem,
                            callFrom = callFrom
                        )
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        fetchSubjectSchedule(callFrom = callFrom)
                    }
                }
                ServiceCode.ACTION_ACCOUNT_SUBJECTFEE -> {
                    try {
                        val schoolYearItem = intent.getSerializableExtra(ServiceCode.ARGUMENT_ACCOUNT_SUBJECTFEE_SCHOOLYEAR) as SchoolYearItem
                        fetchSubjectFee(
                            schoolYearItem = schoolYearItem,
                            callFrom = callFrom
                        )
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        fetchSubjectFee(callFrom = callFrom)
                    }
                }
                ServiceCode.ACTION_ACCOUNT_ACCOUNTINFORMATION -> {
                    fetchAccountInformation(callFrom = callFrom)
                }
                ServiceCode.ACTION_ACCOUNT_GETSTATUS_HASSAVEDLOGIN -> {
                    sendToBroadcast(
                        action = ServiceCode.ACTION_ACCOUNT_GETSTATUS_HASSAVEDLOGIN,
                        data = accModule.hasSavedLogin(),
                        callFrom = callFrom
                    )
                }
                else -> { }
            }
            return super.onStartCommand(intent, flags, startId)
        } catch (ex: Exception) {
            ex.printStackTrace()
            return super.onStartCommand(intent, flags, startId)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        // TODO("Not yet implemented")
        Log.d("AccountService", "onBind()")
        return null
    }

    private lateinit var file: FileModule
    private lateinit var appSettings: AppSettings
    private lateinit var accModule: AccountModule
    private lateinit var appCache: AccountCache

    private var isProcessingLogin = false
    private var isProcessingSubjectSchedule = false
    private var isProcessingSubjectFee = false
    private var isProcessingAccountInformation = false

    private fun initialize() {
        if (!this::file.isInitialized) {
            file = FileModule(this)
            accModule = AccountModule()
        }
    }

    private fun loadSettings() {
        accModule.loadSettings(file.getAccountSettings())
        appSettings = file.getAppSettings()
        appCache = file.getAccountCache()
    }

    private fun saveSettings() {
        accModule.saveSettings {
            file.saveAccountSettings(it)
        }
        file.saveAccountCache(appCache)
    }

    private fun login(
        username: String,
        password: String,
        remembered: Boolean = false,
        preload: Boolean = false,
        callFrom: String,
    ) {
        loadSettings()

        if (isProcessingLogin) {
            // Send broadcast with TYPE_LOGIN = STATUS_ALREADYPROCESSING
            sendToBroadcast(
                action = ServiceCode.ACTION_ACCOUNT_LOGIN,
                status = ServiceCode.STATUS_ALREADYPROCESSING,
                callFrom = callFrom
            )
            return
        }

        // Send broadcast with TYPE_LOGIN = STATUS_PROCESSING
        isProcessingLogin = true
        sendToBroadcast(
            ServiceCode.ACTION_ACCOUNT_LOGIN,
            ServiceCode.STATUS_PROCESSING,
            callFrom = callFrom
        )

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                accModule.login(
                    username = username,
                    password = password,
                    remember = remembered,
                    forceReLogin = true,
                    onResult = { result ->
                        isProcessingLogin = false
                        saveSettings()
                        sendToBroadcast(
                            action = ServiceCode.ACTION_ACCOUNT_LOGIN,
                            status = if (result) ServiceCode.STATUS_SUCCESSFUL else ServiceCode.STATUS_FAILED,
                            callFrom = callFrom
                        )

                        if (preload && result) {
                            fetchSubjectSchedule(callFrom = callFrom)
                            fetchSubjectFee(callFrom = callFrom)
                            fetchAccountInformation(callFrom = callFrom)
                        }
                    }
                )
            }
        }
    }

    private fun logout(
        callFrom: String,
    ) {
        loadSettings()

        if (isProcessingLogin) {
            sendToBroadcast(
                action = ServiceCode.ACTION_ACCOUNT_LOGOUT,
                status = ServiceCode.STATUS_ALREADYPROCESSING,
                callFrom = callFrom
            )
            return
        }

        isProcessingLogin = true
        sendToBroadcast(
            action = ServiceCode.ACTION_ACCOUNT_LOGOUT,
            status = ServiceCode.STATUS_PROCESSING,
            callFrom = callFrom
        )

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                accModule.logout(
                    onResult = {
                        isProcessingLogin = false
                        saveSettings()

                        sendToBroadcast(
                            action = ServiceCode.ACTION_ACCOUNT_LOGOUT,
                            status = ServiceCode.STATUS_SUCCESSFUL,
                            callFrom = callFrom
                        )
                        stopSelf()
                    }
                )
            }
        }
    }

    private fun fetchSubjectSchedule(
        schoolYearItem: SchoolYearItem = appSettings.schoolYear,
        callFrom: String,
    ) {
        loadSettings()

        if (isProcessingSubjectSchedule) {
            sendToBroadcast(
                action = ServiceCode.ACTION_ACCOUNT_SUBJECTSCHEDULE,
                status = ServiceCode.STATUS_ALREADYPROCESSING,
                callFrom = callFrom
            )
            return
        }

        isProcessingSubjectSchedule = true
        sendToBroadcast(
            action = ServiceCode.ACTION_ACCOUNT_SUBJECTSCHEDULE,
            status = ServiceCode.STATUS_PROCESSING,
            callFrom = callFrom
        )

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                accModule.getSubjectSchedule(
                    schoolYearItem = schoolYearItem,
                    onResult = { arrayList ->
                        if (arrayList != null) {
                            appCache.subjectScheduleList.clear()
                            appCache.subjectScheduleList.addAll(arrayList)
                        }
                        isProcessingSubjectSchedule = false
                        saveSettings()

                        sendToBroadcast(
                            action = ServiceCode.ACTION_ACCOUNT_SUBJECTSCHEDULE,
                            status = if (arrayList != null) ServiceCode.STATUS_SUCCESSFUL else ServiceCode.STATUS_FAILED,
                            data = appCache.subjectScheduleList,
                            callFrom = callFrom
                        )
                    }
                )
            }
        }
    }

    private fun fetchSubjectFee(
        schoolYearItem: SchoolYearItem = appSettings.schoolYear,
        callFrom: String,
    ) {
        loadSettings()

        if (isProcessingSubjectFee) {
            sendToBroadcast(
                action = ServiceCode.ACTION_ACCOUNT_SUBJECTFEE,
                status = ServiceCode.STATUS_ALREADYPROCESSING,
                callFrom = callFrom
            )
            return
        }

        isProcessingSubjectFee = true
        sendToBroadcast(
            action = ServiceCode.ACTION_ACCOUNT_SUBJECTFEE,
            status = ServiceCode.STATUS_PROCESSING,
            callFrom = callFrom
        )

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                accModule.getSubjectFee(
                    schoolYearItem = schoolYearItem,
                    onResult = { arrayList ->
                        if (arrayList != null) {
                            appCache.subjectFeeList.clear()
                            appCache.subjectFeeList.addAll(arrayList)
                        }

                        isProcessingSubjectFee = false
                        saveSettings()

                        sendToBroadcast(
                            action = ServiceCode.ACTION_ACCOUNT_SUBJECTFEE,
                            status = if (arrayList != null) ServiceCode.STATUS_SUCCESSFUL else ServiceCode.STATUS_FAILED,
                            data = arrayList,
                            callFrom = callFrom
                        )
                    }
                )
            }
        }
    }

    private fun fetchAccountInformation(
        callFrom: String,
    ) {
        loadSettings()

        if (isProcessingAccountInformation) {
            sendToBroadcast(
                action = ServiceCode.ACTION_ACCOUNT_ACCOUNTINFORMATION,
                status = ServiceCode.STATUS_ALREADYPROCESSING,
                callFrom = callFrom
            )
            return
        }

        isProcessingAccountInformation = true
        sendToBroadcast(
            action = ServiceCode.ACTION_ACCOUNT_ACCOUNTINFORMATION,
            status = ServiceCode.STATUS_PROCESSING,
            callFrom = callFrom
        )

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                accModule.getAccountInformation(
                    onResult = { item ->
                        if (item != null)
                            appCache.accountInformation = item

                        isProcessingAccountInformation = false
                        saveSettings()

                        sendToBroadcast(
                            action = ServiceCode.ACTION_ACCOUNT_ACCOUNTINFORMATION,
                            status = if (item != null) ServiceCode.STATUS_SUCCESSFUL else ServiceCode.STATUS_FAILED,
                            data = item,
                            callFrom = callFrom
                        )
                    }
                )
            }
        }
    }

    private fun reLogin(
        preload: Boolean = false,
        callFrom: String,
    ) {
        loadSettings()

        if (isProcessingLogin) {
            sendToBroadcast(
                action = ServiceCode.ACTION_ACCOUNT_LOGINSTARTUP,
                status = ServiceCode.STATUS_ALREADYPROCESSING,
                callFrom = callFrom,
            )
            return
        }

        isProcessingLogin = true
        sendToBroadcast(
            action = ServiceCode.ACTION_ACCOUNT_LOGINSTARTUP,
            status = ServiceCode.STATUS_PROCESSING,
            callFrom = callFrom
        )

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                accModule.checkIsLoggedIn { result ->
                    isProcessingLogin = false
                    saveSettings()

                    sendToBroadcast(
                        action = ServiceCode.ACTION_ACCOUNT_LOGINSTARTUP,
                        status = if (result) ServiceCode.STATUS_SUCCESSFUL else ServiceCode.STATUS_FAILED,
                        callFrom = callFrom,
                    )

                    if (preload && result) {
                        fetchSubjectSchedule(callFrom = callFrom)
                        fetchSubjectFee(callFrom = callFrom)
                        fetchAccountInformation(callFrom = callFrom)
                    }
                }
            }
        }
    }

    override fun sendBroadcast(intent: Intent) {
        LocalBroadcastManager.getInstance(application.applicationContext).sendBroadcast(intent)
    }

    private fun sendToBroadcast(
        action: String,
        status: String? = null,
        data: Any? = null,
        errorMsg: String? = null,
        callFrom: String,
    ) {
        Intent(action).apply {
            if (status != null) putExtra(ServiceCode.STATUS, status)
            if (data != null) putExtra(ServiceCode.DATA, data as java.io.Serializable)
            if (errorMsg != null) putExtra(ServiceCode.ERRORMESSAGE, errorMsg)
            putExtra(ServiceCode.SOURCE_COMPONENT, callFrom)
        }.also {
            sendBroadcast(it)
        }
    }
}