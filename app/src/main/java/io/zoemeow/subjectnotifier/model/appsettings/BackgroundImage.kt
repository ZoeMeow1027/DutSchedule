package io.zoemeow.subjectnotifier.model.appsettings

import io.zoemeow.subjectnotifier.model.enums.BackgroundImageType
import java.io.Serializable

data class BackgroundImage(
    var option: BackgroundImageType = BackgroundImageType.Unset,
    var path: String? = null
) : Serializable
