package io.zoemeow.dutapp.android.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import io.zoemeow.dutapi.Account
import io.zoemeow.dutapi.objects.AccountInformation
import io.zoemeow.dutapi.objects.SubjectFeeItem
import io.zoemeow.dutapi.objects.SubjectScheduleItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.Serializable
import javax.inject.Inject

class AccountFileRepository @Inject constructor(
    @Transient private val file: File
): Serializable {
    @SerializedName("username")
    private var username: String? = null

    @SerializedName("password")
    private var password: String? = null

    @SerializedName("session_id")
    private var sessionId: String? = null

    @SerializedName("session_id_lastrequest")
    private var sessionIdLastRequest: Long = 0

    @Transient
    private var readyToSave: Boolean = true

    fun isLoggedIn(): Boolean {
        return !isSessionIdExpired() && sessionId != null
    }

    private fun isSessionIdExpired(): Boolean {
        return System.currentTimeMillis() - sessionIdLastRequest >= (1000 * 60 * 30)
    }

    fun setSessionId(sessionId: String) {
        this.sessionId = sessionId

        if (Account.isLoggedIn(sessionId)) {
            sessionIdLastRequest = System.currentTimeMillis()
        }
        else {
            sessionIdLastRequest = 0
            this.sessionId = null
        }
        saveSettings()
    }

    fun getUsername(): String? {
        return username
    }

    fun checkOrGetSessionId(force: Boolean = false): Boolean {
        return try {
            if (!isLoggedIn() || force) {
                val response = Account.getSessionId()
                sessionId = response.sessionId
            }
            saveSettings()
            true
        } catch (ex: Exception) {
            saveSettings()
            false
        }
    }

    fun login(): Boolean {
        return if (username != null && password != null) {
            login(username!!, password!!)
        }
        else false
    }

    fun login(username: String, password: String, remember: Boolean = false): Boolean {
        this.username = username
        if (remember)
            this.password = password

        checkOrGetSessionId()
        Account.login(this.sessionId, this.username, this.password)

        if (!Account.isLoggedIn(this.sessionId)) {
            this.sessionId = null
            this.sessionIdLastRequest = 0
        }
        else this.sessionIdLastRequest = System.currentTimeMillis()

        saveSettings()
        return isLoggedIn()
    }

    fun logout(): Boolean {
        return try {
            if (isLoggedIn()) {
                CoroutineScope(Dispatchers.IO).launch {
                    kotlin.runCatching {
                        Account.logout(sessionId)
                    }
                }
                this.sessionId = null
                this.sessionIdLastRequest = 0
            }
            saveSettings()
            true
        }
        catch (ex: Exception) {
            saveSettings()
            false
        }
    }

    fun getSubjectSchedule(year: Int, semester: Int): ArrayList<SubjectScheduleItem> {
        return if (isLoggedIn()) {
            try {
                val result = Account.getSubjectSchedule(this.sessionId, year, semester)
                saveSettings()
                result
            }
            catch (ex: Exception) {
                saveSettings()
                ArrayList()
            }
        } else {
            saveSettings()
            ArrayList()
        }
    }

    fun getSubjectFee(year: Int, semester: Int): ArrayList<SubjectFeeItem> {
        return if (isLoggedIn()) {
            try {
                val result = Account.getSubjectFee(this.sessionId, year, semester)
                saveSettings()
                result
            }
            catch (ex: Exception) {
                saveSettings()
                ArrayList()
            }
        } else {
            saveSettings()
            ArrayList()
        }
    }

    fun getAccountInformation(): AccountInformation? {
        return if (isLoggedIn()) {
            try {
                val result = Account.getAccountInformation(this.sessionId)
                saveSettings()
                result
            }
            catch (ex: Exception) {
                saveSettings()
                null
            }
        } else {
            saveSettings()
            null
        }
    }

    private fun loadSettings() {
        try {
            Log.d("AccountRead", "Triggered account reading...")
            readyToSave = false

            val buffer: BufferedReader = file.bufferedReader()
            val inputStr = buffer.use { it.readText() }
            buffer.close()

            val itemType = object : TypeToken<AccountFileRepository>() {}.type
            val variableItemTemp = Gson().fromJson<AccountFileRepository>(inputStr, itemType)

            username = variableItemTemp.username
            password = variableItemTemp.password
            sessionId = variableItemTemp.sessionId
            sessionIdLastRequest = variableItemTemp.sessionIdLastRequest
        }
        catch (ex: Exception) {
            ex.printStackTrace()
        }
        finally {
            readyToSave = true
            saveSettings()
        }
    }

    private fun saveSettings() {
        if (readyToSave) {
            Log.d("AccountWrite", "Triggered account writing...")

            try {
                val str = Gson().toJson(this)
                file.writeText(str)
            }
            catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    init {
        loadSettings()
    }
}