package io.zoemeow.dutapp.android.model.appsettings

import com.google.gson.annotations.SerializedName
import io.zoemeow.dutapp.android.model.account.SchoolYearItem
import io.zoemeow.dutapp.android.model.enums.AppTheme
import io.zoemeow.dutapp.android.model.enums.OpenLinkType
import java.io.Serializable

data class AppSettings(
    @SerializedName("appearance.apptheme") val appTheme: AppTheme,
    @SerializedName("appearance.backgroundimage") val backgroundImage: BackgroundImage,
    @SerializedName("appearance.blackthemeenabled") val blackThemeEnabled: Boolean,
    @SerializedName("appearance.dynamiccolorenabled") val dynamicColorEnabled: Boolean,
    @SerializedName("account.schoolyear") val schoolYear: SchoolYearItem,
    @SerializedName("builtinbrowser.openlinktype") val builtInBrowserOpenLinkType: OpenLinkType,
) : Serializable
