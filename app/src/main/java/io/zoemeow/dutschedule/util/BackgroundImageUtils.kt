package io.zoemeow.dutschedule.util

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap

class BackgroundImageUtils {
    companion object {
        fun getCurrentWallpaperBackground(context: Context): Bitmap? {
            try {
                val wallpaperManager = WallpaperManager.getInstance(context)
                return wallpaperManager.drawable?.toBitmap()
            } catch (_: Exception) {
                return null
            }
        }
    }
}