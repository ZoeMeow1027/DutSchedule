package io.zoemeow.dutapp.android.model

import java.io.Serializable

data class AccountSession(
    var username: String? = null,
    var password: String? = null,
    var sessionId: String? = null,
    var sessionIdLastRequest: Long = 0,
): Serializable
