package io.zoemeow.dutnotify.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.zoemeow.dutnotify.model.account.AccountCache
import io.zoemeow.dutnotify.model.account.SchoolYearItem
import io.zoemeow.dutnotify.model.appsettings.AppSettings
import io.zoemeow.dutnotify.model.enums.AccountServiceCode
import io.zoemeow.dutnotify.module.AccountModule
import io.zoemeow.dutnotify.module.FileModule
import io.zoemeow.dutnotify.receiver.AccountBroadcastReceiver
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

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d("AccountService", "Triggered")

        when (intent.getStringExtra(AccountServiceCode.ACTION)) {
            AccountServiceCode.ACTION_LOGINSTARTUP -> {
                Log.d("AccountService", "Triggered relogin")
                val preload = intent.getBooleanExtra(AccountServiceCode.ARGUMENT_LOGINSTARTUP_PRELOAD, false)
                reLogin(preload)
            }
            AccountServiceCode.ACTION_LOGIN -> {
                val username = intent.getStringExtra(AccountServiceCode.ARGUMENT_LOGIN_USERNAME)
                val password = intent.getStringExtra(AccountServiceCode.ARGUMENT_LOGIN_PASSWORD)
                val remember = intent.getBooleanExtra(AccountServiceCode.ARGUMENT_LOGIN_REMEMBERED, false)
                val preload = intent.getBooleanExtra(AccountServiceCode.ARGUMENT_LOGIN_PRELOAD, false)
                if (username == null || password == null) {
                    // TODO: Invaild value here!
                } else login(username, password, remember, preload)
            }
            AccountServiceCode.ACTION_LOGOUT -> {
                Log.d("AccountService", "Triggered logout")
                logout()
            }
            AccountServiceCode.ACTION_SUBJECTSCHEDULE -> {
                try {
                    val schoolYearItem = intent.getSerializableExtra(AccountServiceCode.ARGUMENT_SUBJECTSCHEDULE_SCHOOLYEAR) as SchoolYearItem
                    fetchSubjectSchedule(schoolYearItem)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    fetchSubjectSchedule()
                }
            }
            AccountServiceCode.ACTION_SUBJECTFEE -> {
                try {
                    val schoolYearItem = intent.getSerializableExtra(AccountServiceCode.ARGUMENT_SUBJECTFEE_SCHOOLYEAR) as SchoolYearItem
                    fetchSubjectFee(schoolYearItem)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    fetchSubjectFee()
                }
            }
            AccountServiceCode.ACTION_ACCOUNTINFORMATION -> {
                fetchAccountInformation()
            }
            AccountServiceCode.ACTION_GETSTATUS_HASSAVEDLOGIN -> {
                // Send broadcast with TYPE_LOGIN = STATUS_ALREADYPROCESSING
                Intent(AccountServiceCode.ACTION_GETSTATUS_HASSAVEDLOGIN).apply {
                    putExtra(AccountServiceCode.DATA, accModule.hasSavedLogin())
                }.also {
                    sendBroadcast(it)
                }
            }
            else -> { }
        }

        return super.onStartCommand(intent, flags, startId)
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
            accModule.loadSettings(file.getAccountSettings())
        }
    }

    private fun loadSettings() {
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
    ) {
        loadSettings()

        if (isProcessingLogin) {
            // Send broadcast with TYPE_LOGIN = STATUS_ALREADYPROCESSING
            Intent(AccountServiceCode.ACTION_LOGIN).apply {
                putExtra(AccountServiceCode.STATUS, AccountServiceCode.STATUS_ALREADYPROCESSING)
            }.also {
                sendBroadcast(it)
            }
            return
        }

        // Send broadcast with TYPE_LOGIN = STATUS_PROCESSING
        isProcessingLogin = true
        Intent(AccountServiceCode.ACTION_LOGIN).apply {
            putExtra(AccountServiceCode.STATUS, AccountServiceCode.STATUS_PROCESSING)
        }.also {
            sendBroadcast(it)
        }

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                accModule.login(
                    username = username,
                    password = password,
                    remember = remembered,
                    forceReLogin = true,
                    onResult = { result ->
                        val intent = Intent(AccountServiceCode.ACTION_LOGIN)
                        if (result) {
                            // Send broadcast with TYPE_LOGIN = STATUS_SUCCESSFUL
                            intent.putExtra(AccountServiceCode.STATUS, AccountServiceCode.STATUS_SUCCESSFUL)
                        } else {
                            // Send broadcast with TYPE_LOGIN = STATUS_FAILED
                            intent.putExtra(AccountServiceCode.STATUS, AccountServiceCode.STATUS_FAILED)
                        }

                        isProcessingLogin = false
                        saveSettings()

                        sendBroadcast(intent)

                        if (preload && result) {
                            fetchSubjectSchedule()
                            fetchSubjectFee()
                            fetchAccountInformation()
                        }
                    }
                )
            }
        }
    }

    private fun logout() {
        loadSettings()

        if (isProcessingLogin) {
            // Send broadcast with TYPE_LOGIN = STATUS_ALREADYPROCESSING
            Intent(AccountServiceCode.ACTION_LOGOUT).apply {
                putExtra(AccountServiceCode.STATUS, AccountServiceCode.STATUS_ALREADYPROCESSING)
            }.also {
                sendBroadcast(it)
            }
            return
        }

        // Send broadcast with TYPE_LOGIN = STATUS_PROCESSING
        isProcessingLogin = true
        Intent(AccountServiceCode.ACTION_LOGOUT).apply {
            putExtra(AccountServiceCode.STATUS, AccountServiceCode.STATUS_PROCESSING)
        }.also {
            sendBroadcast(it)
        }

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                // Send broadcast with TYPE_LOGIN = STATUS_SUCCESSFUL
                isProcessingLogin = false

                accModule.logout(
                    onResult = {
                        saveSettings()
                        val intent = Intent(AccountServiceCode.ACTION_LOGOUT)
                        intent.putExtra(AccountServiceCode.STATUS, AccountServiceCode.STATUS_SUCCESSFUL)
                        sendBroadcast(intent)
                    }
                )
            }
        }
    }

    private fun fetchSubjectSchedule(
        schoolYearItem: SchoolYearItem = appSettings.schoolYear
    ) {
        loadSettings()

        if (isProcessingSubjectSchedule) {
            // Send broadcast with TYPE_LOGIN = STATUS_ALREADYPROCESSING
            Intent(AccountServiceCode.ACTION_SUBJECTSCHEDULE).apply {
                putExtra(AccountServiceCode.STATUS, AccountServiceCode.STATUS_ALREADYPROCESSING)
            }.also {
                sendBroadcast(it)
            }
            return
        }

        // Send broadcast with TYPE_LOGIN = STATUS_PROCESSING
        Intent(AccountServiceCode.ACTION_SUBJECTSCHEDULE).apply {
            putExtra(AccountServiceCode.STATUS, AccountServiceCode.STATUS_PROCESSING)
        }.also {
            sendBroadcast(it)
        }
        isProcessingSubjectSchedule = true

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                accModule.getSubjectSchedule(
                    schoolYearItem = schoolYearItem,
                    onResult = { arrayList ->
                        val intent = Intent(AccountServiceCode.ACTION_SUBJECTSCHEDULE)

                        if (arrayList != null) {
                            appCache.subjectScheduleList.clear()
                            appCache.subjectScheduleList.addAll(arrayList)

                            // Send broadcast with TYPE_LOGIN = STATUS_SUCCESSFUL
                            intent.putExtra(AccountServiceCode.STATUS, AccountServiceCode.STATUS_SUCCESSFUL)
                            intent.putExtra(AccountServiceCode.DATA, appCache.subjectScheduleList)
                        } else {
                            // Send broadcast with TYPE_LOGIN = STATUS_FAILED
                            intent.putExtra(AccountServiceCode.STATUS, AccountServiceCode.STATUS_FAILED)
                        }

                        isProcessingSubjectSchedule = false
                        saveSettings()

                        sendBroadcast(intent)
                    }
                )
            }
        }
    }

    private fun fetchSubjectFee(
        schoolYearItem: SchoolYearItem = appSettings.schoolYear
    ) {
        loadSettings()

        if (isProcessingSubjectFee) {
            // Send broadcast with TYPE_LOGIN = STATUS_ALREADYPROCESSING
            Intent(AccountServiceCode.ACTION_SUBJECTFEE).apply {
                putExtra(AccountServiceCode.STATUS, AccountServiceCode.STATUS_ALREADYPROCESSING)
            }.also {
                sendBroadcast(it)
            }
            return
        }

        // Send broadcast with TYPE_LOGIN = STATUS_PROCESSING
        Intent(AccountServiceCode.ACTION_SUBJECTFEE).apply {
            putExtra(AccountServiceCode.STATUS, AccountServiceCode.STATUS_PROCESSING)
        }.also {
            sendBroadcast(it)
        }
        isProcessingSubjectFee = true

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                accModule.getSubjectFee(
                    schoolYearItem = schoolYearItem,
                    onResult = { arrayList ->
                        val intent = Intent(AccountServiceCode.ACTION_SUBJECTFEE)

                        if (arrayList != null) {
                            appCache.subjectFeeList.clear()
                            appCache.subjectFeeList.addAll(arrayList)

                            // Send broadcast with TYPE_LOGIN = STATUS_SUCCESSFUL
                            intent.putExtra(AccountServiceCode.STATUS, AccountServiceCode.STATUS_SUCCESSFUL)
                            intent.putExtra(AccountServiceCode.DATA, arrayList)
                        } else {
                            // Send broadcast with TYPE_LOGIN = STATUS_FAILED
                            intent.putExtra(AccountServiceCode.STATUS, AccountServiceCode.STATUS_FAILED)
                        }

                        isProcessingSubjectFee = false
                        saveSettings()

                        sendBroadcast(intent)
                    }
                )
            }
        }
    }

    private fun fetchAccountInformation() {
        loadSettings()

        if (isProcessingAccountInformation) {
            // Send broadcast with TYPE_LOGIN = STATUS_ALREADYPROCESSING
            Intent(AccountServiceCode.ACTION_ACCOUNTINFORMATION).apply {
                putExtra(AccountServiceCode.STATUS, AccountServiceCode.STATUS_ALREADYPROCESSING)
            }.also {
                sendBroadcast(it)
            }
            return
        }

        isProcessingAccountInformation = true
        // Send broadcast with TYPE_LOGIN = STATUS_PROCESSING
        Intent(AccountServiceCode.ACTION_ACCOUNTINFORMATION).apply {
            putExtra(AccountServiceCode.STATUS, AccountServiceCode.STATUS_PROCESSING)
        }.also {
            sendBroadcast(it)
        }

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                accModule.getAccountInformation(
                    onResult = { item ->
                        val intent = Intent(AccountServiceCode.ACTION_ACCOUNTINFORMATION)

                        if (item != null) {
                            appCache.accountInformation = item

                            // Send broadcast with TYPE_LOGIN = STATUS_SUCCESSFUL
                            intent.putExtra(AccountServiceCode.STATUS, AccountServiceCode.STATUS_SUCCESSFUL)
                            intent.putExtra(AccountServiceCode.DATA, item)
                        } else {
                            // Send broadcast with TYPE_LOGIN = STATUS_FAILED
                            intent.putExtra(AccountServiceCode.STATUS, AccountServiceCode.STATUS_FAILED)
                        }

                        isProcessingAccountInformation = false
                        saveSettings()

                        sendBroadcast(intent)
                    }
                )
            }
        }
    }

    private fun reLogin(
        preload: Boolean = false,
    ) {
        loadSettings()

        if (isProcessingLogin) {
            // Send broadcast with TYPE_LOGIN = STATUS_ALREADYPROCESSING
            Intent(AccountServiceCode.ACTION_LOGINSTARTUP).apply {
                putExtra(AccountServiceCode.STATUS, AccountServiceCode.STATUS_ALREADYPROCESSING)
            }.also {
                sendBroadcast(it)
            }
            return
        }

        isProcessingLogin = true
        // Send broadcast with TYPE_LOGIN = STATUS_PROCESSING
        Intent(AccountServiceCode.ACTION_LOGINSTARTUP).apply {
            putExtra(AccountServiceCode.STATUS, AccountServiceCode.STATUS_PROCESSING)
        }.also {
            sendBroadcast(it)
        }

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                accModule.checkIsLoggedIn { result ->
                    val intent = Intent(AccountServiceCode.ACTION_LOGINSTARTUP)

                    if (result) {
                        // Send broadcast with TYPE_LOGIN = STATUS_SUCCESSFUL
                        intent.putExtra(AccountServiceCode.STATUS, AccountServiceCode.STATUS_SUCCESSFUL)
                    } else {
                        // Send broadcast with TYPE_LOGIN = STATUS_FAILED
                        intent.putExtra(AccountServiceCode.STATUS, AccountServiceCode.STATUS_FAILED)
                    }
                    saveSettings()

                    isProcessingLogin = false
                    sendBroadcast(intent)

                    if (preload && result) {
                        fetchSubjectSchedule()
                        fetchSubjectFee()
                        fetchAccountInformation()
                    }
                }
            }
        }
    }

    override fun sendBroadcast(intent: Intent) {
        LocalBroadcastManager.getInstance(application.applicationContext).sendBroadcast(intent)
    }
}