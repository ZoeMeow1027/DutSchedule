package io.zoemeow.dutschedule.model.settings

import com.google.gson.annotations.SerializedName
import io.zoemeow.dutschedule.model.account.SchoolYearItem
import java.io.Serializable

data class AppSettings(
    @SerializedName("appsettings.appearance.thememode")
    val themeMode: ThemeMode = ThemeMode.FollowDeviceTheme,

    @SerializedName("appsettings.appearance.dynamiccolor")
    val dynamicColor: Boolean = true,

    @SerializedName("appsettings.appearance.blackbackground")
    val blackBackground: Boolean = false,

    @SerializedName("appsettings.appearance.backgroundimage.option")
    val backgroundImage: BackgroundImageOption = BackgroundImageOption.None,

    @SerializedName("appsettings.appearance.backgroundimage.backgroundopacity")
    val backgroundImageOpacity: Float = 0.7f,

    @SerializedName("appsettings.appearance.backgroundimage.componentopacity")
    val componentOpacity: Float = 0.7f,

    @SerializedName("appsettings.miscellaneous.openlinkinsideapp")
    val openLinkInsideApp: Boolean = true,

    @SerializedName("appsettings.newsbackground.filterlist")
    val newsBackgroundFilterList: ArrayList<SubjectCode> = arrayListOf(),

    @SerializedName("appsettings.newsbackground.duration")
    val newsBackgroundDuration: Int = 0,

    @SerializedName("appsettings.newsbackground.newsglobal.enabled")
    val newsBackgroundGlobalEnabled: Boolean = false,

    @SerializedName("appsettings.newsbackground.newssubject.enabled")
    val newsBackgroundSubjectEnabled: Int = -1,

    @SerializedName("appsettings.newsbackground.parsenewssubject")
    val newsBackgroundParseNewsSubject: Boolean = false,

    @SerializedName("appsettings.globalvariables.schoolyear")
    val currentSchoolYear: SchoolYearItem = SchoolYearItem(),
): Serializable {
    fun clone(
        themeMode: ThemeMode? = null,
        dynamicColor: Boolean? = null,
        blackBackground: Boolean? = null,
        backgroundImage: BackgroundImageOption? = null,
        openLinkInsideApp: Boolean? = null,
        newsFilterList: ArrayList<SubjectCode>? = null,
        backgroundImageOpacity: Float? = null,
        fetchNewsBackgroundDuration: Int? = null,
        newsBackgroundGlobalEnabled: Boolean? = null,
        newsBackgroundSubjectEnabled: Int? = null,
        newsBackgroundParseNewsSubject: Boolean? = null,
        currentSchoolYear: SchoolYearItem? = null
    ): AppSettings {
        return AppSettings(
            themeMode = themeMode ?: this.themeMode,
            dynamicColor = dynamicColor ?: this.dynamicColor,
            blackBackground = blackBackground ?: this.blackBackground,
            backgroundImage = backgroundImage ?: this.backgroundImage,
            openLinkInsideApp = openLinkInsideApp ?: this.openLinkInsideApp,
            newsBackgroundFilterList = newsFilterList ?: this.newsBackgroundFilterList,
            backgroundImageOpacity = backgroundImageOpacity ?: this.backgroundImageOpacity,
            newsBackgroundDuration = when (fetchNewsBackgroundDuration) {
                null -> this.newsBackgroundDuration
                0 -> 0
                else -> if (fetchNewsBackgroundDuration >= 5) fetchNewsBackgroundDuration else 5
            },
            newsBackgroundGlobalEnabled = newsBackgroundGlobalEnabled ?: this.newsBackgroundGlobalEnabled,
            newsBackgroundSubjectEnabled = newsBackgroundSubjectEnabled ?: this.newsBackgroundSubjectEnabled,
            newsBackgroundParseNewsSubject = newsBackgroundParseNewsSubject ?: this.newsBackgroundParseNewsSubject,
            currentSchoolYear = currentSchoolYear ?: this.currentSchoolYear
        )
    }
}
