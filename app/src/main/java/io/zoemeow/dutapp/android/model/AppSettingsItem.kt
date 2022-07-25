package io.zoemeow.dutapp.android.model

import io.zoemeow.dutapp.android.model.enums.BackgroundImageType
import java.io.Serializable

class AppSettingsItem: Serializable {
    class Personalize: Serializable {
        class BackgroundImage: Serializable {
            var option: BackgroundImageType = BackgroundImageType.None
            var path: String? = null
        }

        var backgroundImage: BackgroundImage = BackgroundImage()
        var appDynamicColors: Boolean = true
    }

    class Account: Serializable {
        var username: String? = null
        var password: String? = null
    }

    var account: Account = Account()
    var personalize: Personalize = Personalize()
    var schoolYear: SchoolYearItem = SchoolYearItem()
}