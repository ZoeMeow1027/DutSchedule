package io.zoemeow.dutschedule.repository

import io.dutwrapper.dutwrapper.Account
import io.dutwrapper.dutwrapper.model.accounts.AccountInformation
import io.dutwrapper.dutwrapper.model.accounts.SubjectFeeItem
import io.dutwrapper.dutwrapper.model.accounts.SubjectScheduleItem
import io.dutwrapper.dutwrapper.model.accounts.trainingresult.AccountTrainingStatus
import io.zoemeow.dutschedule.model.account.AccountSession
import io.zoemeow.dutschedule.model.account.SchoolYearItem
import io.zoemeow.dutschedule.util.GlobalVariables
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DutAccountRepository {
    /**
     * Detect if a account existed in account session.
     *
     * @param accountSession Your account information.
     * @return true if exist, otherwise false.
     */
    fun hasSavedLogin(accountSession: AccountSession): Boolean {
        return (
                accountSession.accountAuth.username != null &&
                        accountSession.accountAuth.password != null &&
                        accountSession.accountAuth.rememberLogin
                )
    }

    /**
     * Login your account on sv.dut.udn.vn.
     *
     * @param accountSession Your account information.
     * @param forceLogin Make this task force login, whenever current session is logged in.
     * @param onSessionChanged (SessionID, SessionIDDuration) Triggered only when session ID has changed.
     * @return true if successful, otherwise false.
     */
    fun login(
        accountSession: AccountSession,
        forceLogin: Boolean = false,
        onSessionChanged: ((String?, Long?) -> Unit)? = null
    ): Boolean {
        return when {
            (accountSession.sessionId != null && System.currentTimeMillis() - accountSession.sessionLastRequest >= GlobalVariables.SESSIONID_DURATION && Account.isLoggedIn(accountSession.sessionId) && !forceLogin) -> true
            (accountSession.accountAuth.username != null && accountSession.accountAuth.password != null) -> {
                val sessionId = generateNewSessionId()
                val timestamp = System.currentTimeMillis()

                Account.login(
                    sessionId,
                    accountSession.accountAuth.username,
                    accountSession.accountAuth.password
                )

                // Return here, after send data on result
                when (Account.isLoggedIn(sessionId)) {
                    true -> {
                        onSessionChanged?.let {
                            it(sessionId, if (sessionId == null) 0 else timestamp)
                        }
                        true
                    }

                    false -> {
                        onSessionChanged?.let {
                            it(null, 0)
                        }
                        false
                    }
                }
            }
            else -> false
        }
    }

    /**
     * Logout your account from sv.dut.udn.vn. If you have account exist in system,
     * it will be removed there to avoid auto logging in.
     *
     * @param accountSession Your account information.
     * @return Result when this function completed execute. True means completed logout.
     * Exception and otherwise false.
     */
    fun logout(accountSession: AccountSession): Boolean {
        return try {
            CoroutineScope(Dispatchers.IO).launch {
                kotlin.runCatching {
                    Account.logout(accountSession.sessionId)
                }
            }
            true
        } catch (ex: Exception) {
            ex.printStackTrace()
            true
        }
    }

    fun getSubjectSchedule(
        accountSession: AccountSession,
        schoolYearItem: SchoolYearItem
    ): List<SubjectScheduleItem>? {
        return try {
            if (Account.isLoggedIn(accountSession.sessionId)) {
                Account.getSubjectSchedule(
                    accountSession.sessionId,
                    schoolYearItem.year,
                    schoolYearItem.semester
                )
            } else null
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    fun getSubjectFee(
        accountSession: AccountSession,
        schoolYearItem: SchoolYearItem
    ): List<SubjectFeeItem>? {
        return try {
            if (Account.isLoggedIn(accountSession.sessionId)) {
                Account.getSubjectFee(
                    accountSession.sessionId,
                    schoolYearItem.year,
                    schoolYearItem.semester
                )
            } else null
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    fun getAccountInformation(
        accountSession: AccountSession
    ): AccountInformation? {
        return try {
            if (Account.isLoggedIn(accountSession.sessionId)) {
                Account.getAccountInformation(accountSession.sessionId)
            }
            else null
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    fun getAccountTrainingStatus(
        accountSession: AccountSession
    ): AccountTrainingStatus? {
        return try {
            if (Account.isLoggedIn(accountSession.sessionId)) {
                Account.getAccountTrainingStatus(accountSession.sessionId)
            }
            else null
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    /**
     * Get new Session ID from sv.dut.udn.vn.
     *
     * @return A string will return if successfully fetched data. Otherwise will null.
     */
    private fun generateNewSessionId(): String? {
        return try {
            Account.getSessionId().sessionId
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }
}