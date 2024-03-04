package io.zoemeow.dutschedule.model.settings

import com.google.gson.annotations.SerializedName

enum class ThemeMode(val value: Int) {
    /**
     * Follow your device theme
     */
    @SerializedName("0") FollowDeviceTheme(0),

    /**
     * Dark mode
     */
    @SerializedName("1") DarkMode(1),

    /**
     * Light mode
     */
    @SerializedName("2") LightMode(2)
}