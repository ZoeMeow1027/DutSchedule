package io.zoemeow.dutnotify.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import io.zoemeow.dutnotify.model.account.AccountCache
import io.zoemeow.dutnotify.model.account.SchoolYearItem
import io.zoemeow.dutnotify.model.appsettings.AppSettings
import io.zoemeow.dutnotify.module.AccountModule
import io.zoemeow.dutnotify.module.FileModule
import io.zoemeow.dutnotify.receiver.AccountBroadcastReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Suppress("SpellCheckingInspection")
class AccountService: Service() {
    override fun onCreate() {
        initialize()
        loadSettings()
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        // TODO("Not yet implemented")
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
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
            Intent(AccountBroadcastReceiver.STATUS_LOGIN).apply {
                putExtra(AccountBroadcastReceiver.DATATYPE_STATUS, AccountBroadcastReceiver.STATUSTYPE_ALREADYPROCESSING)
            }.also {
                sendBroadcast(it)
            }
            return
        }

        // Send broadcast with TYPE_LOGIN = STATUS_PROCESSING
        isProcessingLogin = true
        Intent(AccountBroadcastReceiver.STATUS_LOGIN).apply {
            putExtra(AccountBroadcastReceiver.DATATYPE_STATUS, AccountBroadcastReceiver.STATUSTYPE_PROCESSING)
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
                        val intent = Intent(AccountBroadcastReceiver.STATUS_LOGIN)
                        if (result) {
                            // Send broadcast with TYPE_LOGIN = STATUS_SUCCESSFUL
                            intent.putExtra(AccountBroadcastReceiver.DATATYPE_STATUS, AccountBroadcastReceiver.STATUSTYPE_SUCCESSFUL)
                        } else {
                            // Send broadcast with TYPE_LOGIN = STATUS_FAILED
                            intent.putExtra(AccountBroadcastReceiver.DATATYPE_STATUS, AccountBroadcastReceiver.STATUSTYPE_FAILED)
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
            Intent(AccountBroadcastReceiver.STATUS_LOGIN).apply {
                putExtra(AccountBroadcastReceiver.DATATYPE_STATUS, AccountBroadcastReceiver.STATUSTYPE_ALREADYPROCESSING)
            }.also {
                sendBroadcast(it)
            }
            return
        }

        // Send broadcast with TYPE_LOGIN = STATUS_PROCESSING
        isProcessingLogin = true
        Intent(AccountBroadcastReceiver.STATUS_LOGIN).apply {
            putExtra(AccountBroadcastReceiver.DATATYPE_STATUS, AccountBroadcastReceiver.STATUSTYPE_PROCESSING)
        }.also {
            sendBroadcast(it)
        }

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                accModule.logout(
                    onResult = { _ ->
                        // Send broadcast with TYPE_LOGIN = STATUS_SUCCESSFUL
                        isProcessingLogin = false
                        saveSettings()

                        val intent = Intent(AccountBroadcastReceiver.STATUS_LOGIN)
                        intent.putExtra(AccountBroadcastReceiver.DATATYPE_STATUS, AccountBroadcastReceiver.STATUSTYPE_SUCCESSFUL)
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
            Intent(AccountBroadcastReceiver.STATUS_SUBJECTSCHEDULE).apply {
                putExtra(AccountBroadcastReceiver.DATATYPE_STATUS, AccountBroadcastReceiver.STATUSTYPE_ALREADYPROCESSING)
            }.also {
                sendBroadcast(it)
            }
            return
        }

        // Send broadcast with TYPE_LOGIN = STATUS_PROCESSING
        Intent(AccountBroadcastReceiver.STATUS_SUBJECTSCHEDULE).apply {
            putExtra(AccountBroadcastReceiver.DATATYPE_STATUS, AccountBroadcastReceiver.STATUSTYPE_PROCESSING)
        }.also {
            sendBroadcast(it)
        }
        isProcessingSubjectSchedule = true

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                accModule.getSubjectSchedule(
                    schoolYearItem = schoolYearItem,
                    onResult = { arrayList ->
                        val intent = Intent(AccountBroadcastReceiver.STATUS_SUBJECTSCHEDULE)

                        if (arrayList != null) {
                            appCache.subjectScheduleList.clear()
                            appCache.subjectScheduleList.addAll(arrayList)

                            // Send broadcast with TYPE_LOGIN = STATUS_SUCCESSFUL
                            intent.putExtra(AccountBroadcastReceiver.DATATYPE_STATUS, AccountBroadcastReceiver.STATUSTYPE_SUCCESSFUL)
                            intent.putExtra(AccountBroadcastReceiver.DATATYPE_DATA, appCache.subjectScheduleList)
                        } else {
                            // Send broadcast with TYPE_LOGIN = STATUS_FAILED
                            intent.putExtra(AccountBroadcastReceiver.DATATYPE_STATUS, AccountBroadcastReceiver.STATUSTYPE_FAILED)
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
            Intent(AccountBroadcastReceiver.STATUS_SUBJECTFEE).apply {
                putExtra(AccountBroadcastReceiver.DATATYPE_STATUS, AccountBroadcastReceiver.STATUSTYPE_ALREADYPROCESSING)
            }.also {
                sendBroadcast(it)
            }
            return
        }

        // Send broadcast with TYPE_LOGIN = STATUS_PROCESSING
        Intent(AccountBroadcastReceiver.STATUS_SUBJECTFEE).apply {
            putExtra(AccountBroadcastReceiver.DATATYPE_STATUS, AccountBroadcastReceiver.STATUSTYPE_PROCESSING)
        }.also {
            sendBroadcast(it)
        }
        isProcessingSubjectFee = true

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                accModule.getSubjectFee(
                    schoolYearItem = schoolYearItem,
                    onResult = { arrayList ->
                        val intent = Intent(AccountBroadcastReceiver.STATUS_SUBJECTFEE)

                        if (arrayList != null) {
                            appCache.subjectFeeList.clear()
                            appCache.subjectFeeList.addAll(arrayList)

                            // Send broadcast with TYPE_LOGIN = STATUS_SUCCESSFUL
                            intent.putExtra(AccountBroadcastReceiver.DATATYPE_STATUS, AccountBroadcastReceiver.STATUSTYPE_SUCCESSFUL)
                            intent.putExtra(AccountBroadcastReceiver.DATATYPE_DATA, arrayList)
                        } else {
                            // Send broadcast with TYPE_LOGIN = STATUS_FAILED
                            intent.putExtra(AccountBroadcastReceiver.DATATYPE_STATUS, AccountBroadcastReceiver.STATUSTYPE_FAILED)
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
            Intent(AccountBroadcastReceiver.STATUS_ACCOUNTINFORMATION).apply {
                putExtra(AccountBroadcastReceiver.DATATYPE_STATUS, AccountBroadcastReceiver.STATUSTYPE_ALREADYPROCESSING)
            }.also {
                sendBroadcast(it)
            }
            return
        }

        isProcessingAccountInformation = true
        // Send broadcast with TYPE_LOGIN = STATUS_PROCESSING
        Intent(AccountBroadcastReceiver.STATUS_ACCOUNTINFORMATION).apply {
            putExtra(AccountBroadcastReceiver.DATATYPE_STATUS, AccountBroadcastReceiver.STATUSTYPE_PROCESSING)
        }.also {
            sendBroadcast(it)
        }

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                accModule.getAccountInformation(
                    onResult = { item ->
                        val intent = Intent(AccountBroadcastReceiver.STATUS_ACCOUNTINFORMATION)

                        if (item != null) {
                            appCache.accountInformation = item

                            // Send broadcast with TYPE_LOGIN = STATUS_SUCCESSFUL
                            intent.putExtra(AccountBroadcastReceiver.DATATYPE_STATUS, AccountBroadcastReceiver.STATUSTYPE_SUCCESSFUL)
                            intent.putExtra(AccountBroadcastReceiver.DATATYPE_DATA, item)
                        } else {
                            // Send broadcast with TYPE_LOGIN = STATUS_FAILED
                            intent.putExtra(AccountBroadcastReceiver.DATATYPE_STATUS, AccountBroadcastReceiver.STATUSTYPE_FAILED)
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
            Intent(AccountBroadcastReceiver.STATUS_LOGIN).apply {
                putExtra(AccountBroadcastReceiver.DATATYPE_STATUS, AccountBroadcastReceiver.STATUSTYPE_ALREADYPROCESSING)
            }.also {
                sendBroadcast(it)
            }
            return
        }

        isProcessingLogin = true
        // Send broadcast with TYPE_LOGIN = STATUS_PROCESSING
        Intent(AccountBroadcastReceiver.STATUS_LOGIN).apply {
            putExtra(AccountBroadcastReceiver.DATATYPE_STATUS, AccountBroadcastReceiver.STATUSTYPE_PROCESSING)
        }.also {
            sendBroadcast(it)
        }

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                accModule.checkIsLoggedIn { result ->
                    val intent = Intent(AccountBroadcastReceiver.STATUS_LOGIN)

                    if (result) {
                        // Send broadcast with TYPE_LOGIN = STATUS_SUCCESSFUL
                        intent.putExtra(AccountBroadcastReceiver.DATATYPE_STATUS, AccountBroadcastReceiver.STATUSTYPE_SUCCESSFUL)
                    } else {
                        // Send broadcast with TYPE_LOGIN = STATUS_FAILED
                        intent.putExtra(AccountBroadcastReceiver.DATATYPE_STATUS, AccountBroadcastReceiver.STATUSTYPE_FAILED)
                    }
                    saveSettings()

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
}