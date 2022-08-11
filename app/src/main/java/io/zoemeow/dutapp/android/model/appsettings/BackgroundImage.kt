package io.zoemeow.dutapp.android.model.appsettings

import io.zoemeow.dutapp.android.model.enums.BackgroundImageType
import java.io.Serializable

data class BackgroundImage(
    var option: BackgroundImageType = BackgroundImageType.Unset,
    var path: String? = null
) : Serializable
