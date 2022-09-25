package io.zoemeow.subjectnotifier.model.account

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AccountSession(
    @SerializedName("account.username")
    var username: String? = null,

    @SerializedName("account.password")
    var password: String? = null,

    @SerializedName("session.id")
    var sessionId: String? = null,

    @SerializedName("session.lastrequest")
    var sessionIdLastRequest: Long = 0,
) : Serializable
