package io.zoemeow.subjectnotifier.model.appsettings

import com.google.gson.annotations.SerializedName
import io.zoemeow.subjectnotifier.model.account.SchoolYearItem
import io.zoemeow.subjectnotifier.model.enums.AppTheme
import io.zoemeow.subjectnotifier.model.enums.BackgroundImageType
import java.io.Serializable

data class AppSettings(
    @SerializedName(APPEARANCE_APPTHEME)
    var appTheme: AppTheme = AppTheme.FollowSystem,
    @SerializedName(APPEARANCE_BACKGROUNDIMAGE)
    var backgroundImage: BackgroundImage = BackgroundImage(
        option = BackgroundImageType.Unset,
        path = null
    ),
    @SerializedName(APPEARANCE_BLACKTHEME_ENABLED)
    var blackThemeEnabled: Boolean = false,
    @SerializedName(APPEARANCE_DYNAMICCOLOR_ENABLED)
    var dynamicColorEnabled: Boolean = true,
    @SerializedName(ACCOUNT_SCHOOLYEAR)
    var schoolYear: SchoolYearItem = SchoolYearItem(22, 1),
    @SerializedName(MISCELLANEOUS_OPENLINKINCUSTOMTAB)
    var openLinkInCustomTab: Boolean = true,
    @SerializedName(NEWSINBACKGROUND_TIMESTART)
    var refreshNewsTimeStart: CustomClock = CustomClock(6, 0),
    @SerializedName(NEWSINBACKGROUND_TIMEEND)
    var refreshNewsTimeEnd: CustomClock = CustomClock(23, 0),
    @SerializedName(NEWSINBACKGROUND_INTERVAL)
    var refreshNewsIntervalInMinute: Int = 3,
    @SerializedName(NEWSINBACKGROUND_ENABLED)
    var refreshNewsEnabled: Boolean = false,
    @SerializedName(NEWSFILTER_FILTERLIST)
    var newsFilterList: ArrayList<SubjectCode> = arrayListOf(),
    @SerializedName(SCREEN_WELCOME_VIEWED)
    var welcomeScreenViewed: ArrayList<Int> = arrayListOf(),
) : Serializable {
    @Suppress("SpellCheckingInspection")
    companion object {
        const val APPEARANCE_APPTHEME = "appearance.apptheme"
        const val APPEARANCE_BACKGROUNDIMAGE = "appearance.backgroundimage"
        const val APPEARANCE_BLACKTHEME_ENABLED = "appearance.blackthemeenabled"
        const val APPEARANCE_DYNAMICCOLOR_ENABLED = "appearance.dynamiccolorenabled"
        const val ACCOUNT_SCHOOLYEAR = "account.schoolyear"
        const val MISCELLANEOUS_OPENLINKINCUSTOMTAB = "browser.openlinkincustomtab"
        const val NEWSINBACKGROUND_ENABLED = "news.refreshinbackground.enabled"
        const val NEWSINBACKGROUND_INTERVAL = "news.refreshinbackground.interval"
        const val NEWSINBACKGROUND_TIMESTART = "news.refreshinbackground.timestart"
        const val NEWSINBACKGROUND_TIMEEND = "news.refreshinbackground.timeend"
        const val NEWSFILTER_FILTERLIST = "newsfilter.filterlist"
        const val SCREEN_WELCOME_VIEWED = "screen.welcome.viewed"
    }

    fun clone(): AppSettings {
        return AppSettings(
            appTheme,
            backgroundImage,
            blackThemeEnabled,
            dynamicColorEnabled,
            schoolYear,
            openLinkInCustomTab,
            refreshNewsTimeStart,
            refreshNewsTimeEnd,
            refreshNewsIntervalInMinute,
            refreshNewsEnabled,
            arrayListOf<SubjectCode>().apply {
                addAll(newsFilterList)
            },
            arrayListOf<Int>().apply {
                addAll(welcomeScreenViewed)
            }
        )
    }

    fun modify(
        optionToModify: String,
        value: Any
    ): AppSettings {
        val appSettings = clone()

        when (optionToModify) {
            APPEARANCE_APPTHEME -> {
                appSettings.appTheme = value as AppTheme
            }
            APPEARANCE_BACKGROUNDIMAGE -> {
                appSettings.backgroundImage = value as BackgroundImage
            }
            APPEARANCE_BLACKTHEME_ENABLED -> {
                appSettings.blackThemeEnabled = value as Boolean
            }
            APPEARANCE_DYNAMICCOLOR_ENABLED -> {
                appSettings.dynamicColorEnabled = value as Boolean
            }
            ACCOUNT_SCHOOLYEAR -> {
                appSettings.schoolYear = value as SchoolYearItem
            }
            MISCELLANEOUS_OPENLINKINCUSTOMTAB -> {
                appSettings.openLinkInCustomTab = value as Boolean
            }
            NEWSINBACKGROUND_TIMESTART -> {
                appSettings.refreshNewsTimeStart = value as CustomClock
            }
            NEWSINBACKGROUND_TIMEEND -> {
                appSettings.refreshNewsTimeEnd = value as CustomClock
            }
            NEWSINBACKGROUND_INTERVAL -> {
                appSettings.refreshNewsIntervalInMinute = value as Int
            }
            NEWSINBACKGROUND_ENABLED -> {
                appSettings.refreshNewsEnabled = value as Boolean
            }
            NEWSFILTER_FILTERLIST -> {
                @Suppress("UNCHECKED_CAST")
                appSettings.newsFilterList = value as ArrayList<SubjectCode>
            }
            SCREEN_WELCOME_VIEWED -> {
                @Suppress("UNCHECKED_CAST")
                appSettings.welcomeScreenViewed = value as ArrayList<Int>
            }
            else -> {

            }
        }

        return appSettings
    }
}