package io.zoemeow.dutapp.android.model

import io.zoemeow.dutapp.android.model.enums.BackgroundImageType
import java.io.Serializable

data class AppSettingsItem(
    // Background image area
    var backgroundImageOption: BackgroundImageType = BackgroundImageType.FromLauncher,
    var backgroundImagePath: String? = null,

    // Account area
    var username: String? = null,
    var password: String? = null,
    var rememberLogin: Boolean = false,

    // School year settings
    var schoolYear: SchoolYearItem = SchoolYearItem()
): Serializable