package io.zoemeow.dutschedule.model.settings

import com.google.gson.annotations.SerializedName

enum class BackgroundImageOption(val value: Int) {
    @SerializedName("0") None(0),
    @SerializedName("1") YourCurrentWallpaper(1),
    @SerializedName("2") PickFileFromMedia(2)
}