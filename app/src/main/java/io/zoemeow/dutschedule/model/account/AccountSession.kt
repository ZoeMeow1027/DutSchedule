package io.zoemeow.dutschedule.model.account

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AccountSession(
    @SerializedName("account.session.auth")
    var accountAuth: AccountAuth = AccountAuth(),

    @SerializedName("account.session.id")
    var sessionId: String? = null,

    @SerializedName("account.session.lastrequest")
    var sessionLastRequest: Long = 0,
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
}