package io.zoemeow.dutapp.android.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.zoemeow.dutapi.Account
import io.zoemeow.dutapi.objects.AccountInformation
import io.zoemeow.dutapi.objects.SubjectFeeItem
import io.zoemeow.dutapi.objects.SubjectScheduleItem
import io.zoemeow.dutapp.android.model.account.AccountSession
import io.zoemeow.dutapp.android.model.account.SchoolYearItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import javax.inject.Inject

class AccountFileRepository @Inject constructor(
    @Transient private val file: File
) {
    private val sessionIdDuration: Int = 1000 * 60 * 30

    private var accountSession: AccountSession = AccountSession()

    /**
     * Check if your account has logged in.
     *
     * @param onResult Result when this function completed execute. True means logged in,
     * otherwise false.
     */
    fun checkIsLoggedIn(
        onResult: (Boolean) -> Unit
    ) {
        var loggedIn = false

        // If Session ID is exist before 30 minutes, return true.
        if (
            accountSession.sessionId != null &&
            System.currentTimeMillis() - accountSession.sessionIdLastRequest >= sessionIdDuration
        ) loggedIn = true
        // If not, false here!
        else {
            // If username and password is remembered, try to logging in
            if (
                accountSession.username != null &&
                accountSession.password != null
            ) {
                generateNewSessionId(
                    onResult = { result ->
                        if (result) {
                            login( // If successfully login, return true
                                username = accountSession.username,
                                password = accountSession.password,
                                remember = true, // This option is always true if exist username and password),
                                onResult = {
                                    loggedIn = it
                                }
                            )
                        }
                    }
                )
            }
        }

        onResult(loggedIn)
    }

    /**
     * Update Session ID time to avoid re-check logged in.
     */
    private fun updateSessionIdLastRequest() {
        if (accountSession.sessionId != null)
            accountSession.sessionIdLastRequest = System.currentTimeMillis()
        else accountSession.sessionIdLastRequest = 0
    }

    /**
     * Get new Session ID from sv.dut.udn.vn.
     */
    private fun generateNewSessionId(
        onResult: ((Boolean) -> Unit)? = null
    ) {
        try {
            val response = Account.getSessionId()
            accountSession.sessionId = response.sessionId
            updateSessionIdLastRequest()

            saveSettings()
            if (onResult != null) onResult(true)
        } catch (ex: Exception) {
            ex.printStackTrace()

            saveSettings()
            if (onResult != null) onResult(false)
        }
    }

    /**
     * Login to sv.dut.udn.vn using your account.
     *
     * @param username Your username (Student ID) (leave empty if you want to re-login).
     * @param password Your password (leave empty if you want to re-login).
     * @param remember Remember your password or not (if you re-login your account, this
     * option is not affected, so leave empty if you want to re-login).
     * @param forceReLogin Set to true to force logging you in. This will helpful if you tried too
     * many attempts but still not logged in.
     * @param onResult Result when this function completed execute. True means logged in,
     * otherwise false.
     */
    fun login(
        username: String? = null,
        password: String? = null,
        remember: Boolean = false,
        forceReLogin: Boolean = false,
        onResult: ((Boolean) -> Unit)? = null
    ) {
        if (forceReLogin)
            generateNewSessionId()

        // 0: Logged in, 1: Not logged in, 2: Invalid username & password
        var loggedInCode = 2
        // Login, but set code to 1 due to unknown you have logged in.
        when {
            (username != null && password != null) -> {
                Account.login(
                    accountSession.sessionId,
                    username,
                    password
                )
                updateSessionIdLastRequest()
                loggedInCode = 1
            }
            (accountSession.username != null && accountSession.password != null) -> {
                Account.login(
                    accountSession.sessionId,
                    accountSession.username,
                    accountSession.password
                )
                updateSessionIdLastRequest()
                loggedInCode = 1
            }
        }
        // Check if logged in
        if (loggedInCode < 2) {
            val loggedIn2 = Account.isLoggedIn(accountSession.sessionId)
            updateSessionIdLastRequest()
            loggedInCode = if (loggedIn2) 0 else 1
        }

        when (loggedInCode) {
            0 -> {
                if (username != null && password != null && remember) {
                    accountSession.username = username
                    accountSession.password = password
                    saveSettings()
                }

                if (onResult != null) onResult(true)
            }
            else -> if (onResult != null) onResult(false)
        }
    }

    fun logout(
        onResult: ((Boolean) -> Unit)? = null
    ) {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                accountSession.username = null
                accountSession.password = null
                kotlin.runCatching {
                    Account.logout(accountSession.sessionId)
                }
                generateNewSessionId()
                updateSessionIdLastRequest()
            }

            saveSettings()
            if (onResult != null) onResult(true)
        } catch (ex: Exception) {
            ex.printStackTrace()

            saveSettings()
            if (onResult != null) onResult(false)
        }
    }

    fun getSubjectSchedule(
        schoolYearItem: SchoolYearItem,
        onResult: ((ArrayList<SubjectScheduleItem>?) -> Unit)? = null
    ) {
        try {
            if (Account.isLoggedIn(accountSession.sessionId)) {
                val result = Account.getSubjectSchedule(
                    accountSession.sessionId,
                    schoolYearItem.year,
                    schoolYearItem.semester
                )
                updateSessionIdLastRequest()
                if (onResult != null) onResult(result)
            } else if (onResult != null) onResult(null)
            updateSessionIdLastRequest()
        } catch (ex: Exception) {
            ex.printStackTrace()
            if (onResult != null) onResult(null)
        }
    }

    fun getSubjectFee(
        schoolYearItem: SchoolYearItem,
        onResult: ((ArrayList<SubjectFeeItem>?) -> Unit)? = null
    ) {
        try {
            if (Account.isLoggedIn(accountSession.sessionId)) {
                val result = Account.getSubjectFee(
                    accountSession.sessionId,
                    schoolYearItem.year,
                    schoolYearItem.semester
                )
                updateSessionIdLastRequest()
                if (onResult != null) onResult(result)
            } else if (onResult != null) onResult(null)
            updateSessionIdLastRequest()
        } catch (ex: Exception) {
            ex.printStackTrace()
            if (onResult != null) onResult(null)
        }
    }

    fun getAccountInformation(
        onResult: ((AccountInformation?) -> Unit)? = null
    ) {
        try {
            if (Account.isLoggedIn(accountSession.sessionId)) {
                val result = Account.getAccountInformation(accountSession.sessionId)
                updateSessionIdLastRequest()
                if (onResult != null) onResult(result)
            } else if (onResult != null) onResult(null)
            updateSessionIdLastRequest()
        } catch (ex: Exception) {
            ex.printStackTrace()
            if (onResult != null) onResult(null)
        }
    }

    fun loadSettings() {
        try {
            Log.d("AccountRead", "Triggered account reading...")

            val buffer: BufferedReader = file.bufferedReader()
            val inputStr = buffer.use { it.readText() }
            buffer.close()

            val itemType = object : TypeToken<AccountSession>() {}.type
            accountSession = Gson().fromJson(inputStr, itemType)
        } catch (ex: Exception) {
            ex.printStackTrace()
        } finally {
            saveSettings()
        }
    }

    fun saveSettings() {
        Log.d("AccountWrite", "Triggered account writing...")

        try {
            val str = Gson().toJson(accountSession)
            file.writeText(str)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    init {
        loadSettings()
    }
}