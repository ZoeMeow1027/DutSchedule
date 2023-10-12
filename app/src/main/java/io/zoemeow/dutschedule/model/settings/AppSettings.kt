package io.zoemeow.dutschedule.model.settings

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AppSettings(
    @SerializedName("appsettings.thememode")
    val themeMode: ThemeMode = ThemeMode.FollowDeviceTheme,

    @SerializedName("appsettings.dynamiccolor")
    val dynamicColor: Boolean = true,

    @SerializedName("appsettings.blackbackground")
    val blackBackground: Boolean = false,

    @SerializedName("appsettings.backgroundimage")
    val backgroundImage: BackgroundImageOption = BackgroundImageOption.None,

    @SerializedName("appsettings.openlinkinsideapp")
    val openLinkInsideApp: Boolean = true,
): Serializable {
    fun clone(
        themeMode: ThemeMode? = null,
        dynamicColor: Boolean? = null,
        blackBackground: Boolean? = null,
        backgroundImage: BackgroundImageOption? = null,
        openLinkInsideApp: Boolean? = null,
    ): AppSettings {
        return AppSettings(
            themeMode = themeMode ?: this.themeMode,
            dynamicColor = dynamicColor ?: this.dynamicColor,
            blackBackground = blackBackground ?: this.blackBackground,
            backgroundImage = backgroundImage ?: this.backgroundImage,
            openLinkInsideApp = openLinkInsideApp ?: this.openLinkInsideApp
        )
    }
}
