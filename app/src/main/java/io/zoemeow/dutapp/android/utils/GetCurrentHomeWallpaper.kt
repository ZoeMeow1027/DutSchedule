package io.zoemeow.dutapp.android.utils

import android.Manifest
import android.app.Activity
import android.app.WallpaperManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class GetCurrentHomeWallpaper {
    companion object {
        fun checkPermission(context: Context): Boolean {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }

        fun requirePermission(activity: Activity) {
            ActivityCompat.requestPermissions(
                activity,
                listOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ).toTypedArray(),
                0
            )
        }

        fun getCurrentWallpaper(
            activity: Activity,
            onResult: (Boolean, Drawable?) -> Unit
        ) {
            var drawable: Drawable? = null
            var successful = false

            try {
                if (checkPermission(activity)) {
                    val wallpaperManager = WallpaperManager.getInstance(activity)
                    drawable = wallpaperManager.drawable
                    successful = true
                    onResult(successful, drawable)
                }
                else {
                    requirePermission(activity)
                    if (checkPermission(activity)) {
                        getCurrentWallpaper(
                            activity,
                            onResult = { successfulP, drawableP ->
                                drawable = drawableP
                                successful = successfulP
                            }
                        )
                    }
                    onResult(successful, drawable)
                }
            }
            catch (ex: Exception) {
                ex.printStackTrace()
                drawable = null
                successful = false
                onResult(successful, drawable)
            }
        }
    }
}