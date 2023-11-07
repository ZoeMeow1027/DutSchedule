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

    @SerializedName("appsettings.backgroundimage.opacity")
    val backgroundImageOpacity: Float = 0.7f,

    @SerializedName("appsettings.openlinkinsideapp")
    val openLinkInsideApp: Boolean = true,

    @SerializedName("appsettings.newsfilterlist")
    val newsFilterList: ArrayList<SubjectCode> = arrayListOf(),

    @SerializedName("appsettings.fetchnewsinbackground.duration")
    val fetchNewsBackgroundDuration: Int = 0
): Serializable {
    fun clone(
        themeMode: ThemeMode? = null,
        dynamicColor: Boolean? = null,
        blackBackground: Boolean? = null,
        backgroundImage: BackgroundImageOption? = null,
        openLinkInsideApp: Boolean? = null,
        newsFilterList: ArrayList<SubjectCode>? = null,
        backgroundImageOpacity: Float? = null,
        fetchNewsBackgroundDuration: Int? = null
    ): AppSettings {
        return AppSettings(
            themeMode = themeMode ?: this.themeMode,
            dynamicColor = dynamicColor ?: this.dynamicColor,
            blackBackground = blackBackground ?: this.blackBackground,
            backgroundImage = backgroundImage ?: this.backgroundImage,
            openLinkInsideApp = openLinkInsideApp ?: this.openLinkInsideApp,
            newsFilterList = newsFilterList ?: this.newsFilterList,
            backgroundImageOpacity = backgroundImageOpacity ?: this.backgroundImageOpacity,
            fetchNewsBackgroundDuration = when (fetchNewsBackgroundDuration) {
                null -> this.fetchNewsBackgroundDuration
                0 -> 0
                else -> if (fetchNewsBackgroundDuration >= 5) fetchNewsBackgroundDuration else 5
            }
        )
    }
}
