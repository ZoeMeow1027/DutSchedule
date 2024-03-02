package io.zoemeow.dutschedule.model.account

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AccountAuth(
    @SerializedName("account.auth.username")
    val username: String? = null,

    @SerializedName("account.auth.password")
    val password: String? = null,

    @SerializedName("account.auth.rememberlogin")
    val rememberLogin: Boolean = false,
): Serializable {
    fun clone(
        username: String? = null,
        password: String? = null,
        rememberLogin: Boolean? = null,
    ): AccountAuth {
        return AccountAuth(
            username = username ?: this.username,
            password = password ?: this.password,
            rememberLogin = rememberLogin ?: this.rememberLogin,
        )
    }

    fun isValidLogin(): Boolean {
        return this.username != null && this.password != null
    }
}