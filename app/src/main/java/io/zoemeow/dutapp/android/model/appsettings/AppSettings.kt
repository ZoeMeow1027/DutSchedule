package io.zoemeow.dutapp.android.model.appsettings

import com.google.gson.annotations.SerializedName
import io.zoemeow.dutapp.android.model.account.SchoolYearItem
import io.zoemeow.dutapp.android.model.enums.AppSettingsCode
import io.zoemeow.dutapp.android.model.enums.AppTheme
import io.zoemeow.dutapp.android.model.enums.BackgroundImageType
import java.io.Serializable

data class AppSettings(
    @SerializedName(AppSettingsCode.AppTheme)
    var appTheme: AppTheme = AppTheme.FollowSystem,
    @SerializedName(AppSettingsCode.BackgroundImage)
    var backgroundImage: BackgroundImage = BackgroundImage(
        option = BackgroundImageType.Unset,
        path = null
    ),
    @SerializedName(AppSettingsCode.BlackThemeEnabled)
    var blackThemeEnabled: Boolean = false,
    @SerializedName(AppSettingsCode.DynamicColorEnabled)
    var dynamicColorEnabled: Boolean = true,
    @SerializedName(AppSettingsCode.SchoolYear)
    var schoolYear: SchoolYearItem = SchoolYearItem(22, 1),
    @SerializedName(AppSettingsCode.OpenLinkInCustomTab)
    var openLinkInCustomTab: Boolean = true,
    @SerializedName(AppSettingsCode.RefreshNewsTimeStart)
    var refreshNewsTimeStart: CustomClock = CustomClock(6, 0),
    @SerializedName(AppSettingsCode.RefreshNewsTimeEnd)
    var refreshNewsTimeEnd: CustomClock = CustomClock(23, 0),
    @SerializedName(AppSettingsCode.RefreshNewsInterval)
    var refreshNewsIntervalInMinute: Int = 3,
) : Serializable {
    private fun clone(): AppSettings {
        return AppSettings(
            appTheme,
            backgroundImage,
            blackThemeEnabled,
            dynamicColorEnabled,
            schoolYear,
            openLinkInCustomTab,
            refreshNewsTimeStart,
            refreshNewsTimeEnd,
            refreshNewsIntervalInMinute
        )
    }

    fun modify(
        optionToModify: String,
        value: Any
    ): AppSettings {
        val appSettings = clone()

        when (optionToModify) {
            AppSettingsCode.AppTheme -> {
                appSettings.appTheme = value as AppTheme
            }
            AppSettingsCode.BackgroundImage -> {
                appSettings.backgroundImage = value as BackgroundImage
            }
            AppSettingsCode.BlackThemeEnabled -> {
                appSettings.blackThemeEnabled = value as Boolean
            }
            AppSettingsCode.DynamicColorEnabled -> {
                appSettings.dynamicColorEnabled = value as Boolean
            }
            AppSettingsCode.SchoolYear -> {
                appSettings.schoolYear = value as SchoolYearItem
            }
            AppSettingsCode.OpenLinkInCustomTab -> {
                appSettings.openLinkInCustomTab = value as Boolean
            }
            AppSettingsCode.RefreshNewsTimeStart -> {
                appSettings.refreshNewsTimeStart = value as CustomClock
            }
            AppSettingsCode.RefreshNewsTimeEnd -> {
                appSettings.refreshNewsTimeEnd = value as CustomClock
            }
            AppSettingsCode.RefreshNewsInterval -> {
                appSettings.refreshNewsIntervalInMinute = value as Int
            }
            else -> {

            }
        }

        return appSettings
    }
}