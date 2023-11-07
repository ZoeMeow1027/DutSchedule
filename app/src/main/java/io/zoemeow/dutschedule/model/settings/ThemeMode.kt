package io.zoemeow.dutschedule.model.settings

import com.google.gson.annotations.SerializedName

enum class ThemeMode(val value: Int) {
    @SerializedName("0") FollowDeviceTheme(0),
    @SerializedName("1") DarkMode(1),
    @SerializedName("2") LightMode(2)
}