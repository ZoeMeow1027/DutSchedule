package io.zoemeow.dutnotify.model.appsettings

import io.zoemeow.dutnotify.model.enums.BackgroundImageType
import java.io.Serializable

data class BackgroundImage(
    var option: BackgroundImageType = BackgroundImageType.Unset,
    var path: String? = null
) : Serializable
