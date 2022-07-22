package io.zoemeow.dutapp.android.model.enums

import java.io.Serializable

enum class BackgroundImageType(code: Int): Serializable {
    None(0),
    FromWallpaper(1),
    FromItemYouSpecific(2)
}