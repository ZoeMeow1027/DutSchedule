package io.zoemeow.dutschedule.model.account

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AccountSession(
    @SerializedName("account.session.auth")
    val accountAuth: AccountAuth = AccountAuth(),

    @SerializedName("account.session.id")
    val sessionId: String? = null,

    @SerializedName("account.session.lastrequest")
    val sessionLastRequest: Long = 0,
): Serializable {
    fun clone(
        accountAuth: AccountAuth? = null,
        sessionId: String? = null,
        sessionLastRequest: Long? = null
    ): AccountSession {
        return AccountSession(
            accountAuth = accountAuth ?: this.accountAuth.clone(),
            sessionId = sessionId ?: this.sessionId,
            sessionLastRequest = sessionLastRequest ?: this.sessionLastRequest
        )
    }

    fun isValidLogin(): Boolean {
        return accountAuth.isValidLogin()
    }
}