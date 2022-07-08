package io.zoemeow.dutapp.android.viewmodel

import io.zoemeow.dutapi.Account
import io.zoemeow.dutapi.objects.AccountInformation
import io.zoemeow.dutapi.objects.SubjectFeeItem
import io.zoemeow.dutapi.objects.SubjectScheduleItem
import java.io.Serializable

class AccountSession: Serializable {
    private var username: String? = null
    private var password: String? = null
    private var sessionId: String? = null

    private var sessionIdLastRequest: Long = 0

    fun isLoggedIn(): Boolean {
        return !isSessionIdExpired() && sessionId != null
    }

    private fun isSessionIdExpired(): Boolean {
        return System.currentTimeMillis() - sessionIdLastRequest >= (1000 * 60 * 30)
    }

    fun setSessionId(sessionId: String) {
        this.sessionId = sessionId

        if (Account.isLoggedIn(this.sessionId)) {
            this.sessionIdLastRequest = System.currentTimeMillis()
        }
        else {
            this.sessionIdLastRequest = 0
            this.sessionId = null
        }
    }

    fun getUsername(): String? {
        return username
    }

    private fun checkOrGetSessionId(): Boolean {
        return try {
            if (!isLoggedIn()) {
                val response = Account.getSessionId()
                sessionId = response.sessionId
            }
            true
        } catch (ex: Exception) {
            false
        }
    }

    fun login(username: String, password: String): Boolean {
        this.username = username
        this.password = password

        checkOrGetSessionId()
        Account.login(this.sessionId, this.username, this.password)

        if (!Account.isLoggedIn(this.sessionId)) {
            this.sessionId = null
            this.sessionIdLastRequest = 0
        }
        else this.sessionIdLastRequest = System.currentTimeMillis()

        return isLoggedIn()
    }

    fun logout(): Boolean {
        return try {
            if (isLoggedIn()) {
                Account.logout(this.sessionId)
                this.sessionId = null
                this.sessionIdLastRequest = 0
            }
            true
        }
        catch (ex: Exception) {
            false
        }
    }

    fun getSubjectSchedule(year: Int, semester: Int): ArrayList<SubjectScheduleItem> {
        return if (isLoggedIn()) {
            try {
                Account.getSubjectSchedule(this.sessionId, year, semester)
            }
            catch (ex: Exception) {
                ArrayList()
            }
        } else {
            ArrayList()
        }
    }

    fun getSubjectFee(year: Int, semester: Int): ArrayList<SubjectFeeItem> {
        return if (isLoggedIn()) {
            try {
                Account.getSubjectFee(this.sessionId, year, semester)
            }
            catch (ex: Exception) {
                ArrayList()
            }
        } else {
            ArrayList()
        }
    }

    fun getAccountInformation(): AccountInformation? {
        return if (isLoggedIn()) {
            try {
                Account.getAccountInformation(this.sessionId)
            }
            catch (ex: Exception) {
                null
            }
        } else {
            null
        }
    }
}