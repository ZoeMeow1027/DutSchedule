package io.zoemeow.dutapp.android.model.appsettings

import java.io.Serializable

data class AccountItem(
    var username: String? = null,
    var password: String? = null
) : Serializable
