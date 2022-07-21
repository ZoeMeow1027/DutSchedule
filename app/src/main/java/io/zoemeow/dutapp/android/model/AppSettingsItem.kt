package io.zoemeow.dutapp.android.model

import io.zoemeow.dutapp.android.model.enums.BackgroundImageType
import java.io.Serializable

data class AppSettingsItem(
    // Background image area
    var backgroundImageOption: BackgroundImageType = BackgroundImageType.None,
    var backgroundImagePath: String? = null,

    // Account area
    var username: String? = null,
    var password: String? = null,
    var rememberLogin: Boolean = false,

    /**
     * Gets or sets current school year
     */
    var schoolYear: SchoolYearItem = SchoolYearItem()
): Serializable