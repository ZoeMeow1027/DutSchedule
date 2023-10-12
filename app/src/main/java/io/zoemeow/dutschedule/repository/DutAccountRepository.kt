package io.zoemeow.dutschedule.repository

import android.util.Log
import io.dutwrapperlib.dutwrapper.Account
import io.dutwrapperlib.dutwrapper.objects.accounts.AccountInformation
import io.dutwrapperlib.dutwrapper.objects.accounts.SubjectFeeItem
import io.dutwrapperlib.dutwrapper.objects.accounts.SubjectScheduleItem
import io.zoemeow.dutschedule.model.account.AccountAuth
import io.zoemeow.dutschedule.model.account.AccountSession
import io.zoemeow.dutschedule.model.account.SchoolYearItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DutAccountRepository {
    private var accountSession = AccountSession()

    private val sessionIdDuration: Int = 1000 * 60 * 30

    /**
     * Detect if a account is exist in system.
     *
     * @return true if exist, otherwise false.
     */
    fun hasSavedLogin(): Boolean {
        return (accountSession.accountAuth.username != null && accountSession.accountAuth.password != null) && (accountSession.accountAuth.rememberLogin)
    }

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
            System.currentTimeMillis() - accountSession.sessionLastRequest >= sessionIdDuration &&
            Account.isLoggedIn(accountSession.sessionId)
        ) loggedIn = true
        // If not, false here!
        else {
            // If username and password is remembered, try to logging in
            if (
                accountSession.accountAuth.username != null &&
                accountSession.accountAuth.password != null
            ) {
                generateNewSessionId(
                    onResult = { result ->
                        if (result) {
                            login( // If successfully login, return true
                                username = accountSession.accountAuth.username,
                                password = accountSession.accountAuth.password,
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
            accountSession.sessionLastRequest = System.currentTimeMillis()
        else accountSession.sessionLastRequest = 0
    }

    private fun clearLogin() {
        accountSession = accountSession.clone(
            accountAuth = AccountAuth(
                username = null,
                password = null
            ),
            sessionId = ""
        )
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
            (accountSession.accountAuth.username != null && accountSession.accountAuth.password != null) -> {
                Account.login(
                    accountSession.sessionId,
                    accountSession.accountAuth.username,
                    accountSession.accountAuth.password
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
                    accountSession = accountSession.clone(
                        accountAuth = AccountAuth(
                            username = username,
                            password = password
                        )
                    )
                    saveSettings()
                }

                if (onResult != null) onResult(true)
            }
            else -> if (onResult != null) onResult(false)
        }
    }

    /**
     * Logout your account from sv.dut.udn.vn. If you have account exist in system,
     * it will be removed there to avoid auto logging in.
     *
     * @param onResult Result when this function completed execute. True means completed,
     * logout, exception and otherwise false.
     */
    fun logout(
        onResult: ((Boolean) -> Unit)? = null
    ) {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val temp = accountSession.sessionId
                clearLogin()
                generateNewSessionId()
                updateSessionIdLastRequest()
                kotlin.runCatching {
                    Account.logout(temp)
                }
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
        saveSettings()
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
        saveSettings()
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
        saveSettings()
    }

    fun loadSettings(
        accountSession: AccountSession
    ) {
        try {
            Log.d("AccountRead", "Triggered account reading...")
            this.accountSession = accountSession
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun saveSettings(
        result: ((AccountSession) -> Unit)? = null
    ) {
        Log.d("AccountWrite", "Triggered account writing...")
        if (result != null)
            result(accountSession)
    }
}