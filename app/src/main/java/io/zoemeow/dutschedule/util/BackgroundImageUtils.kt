package io.zoemeow.dutschedule.util

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.graphics.drawable.toBitmap
import java.io.File

class BackgroundImageUtils {
    companion object {
        fun getCurrentWallpaperBackground(context: Context): Bitmap? {
            return try {
                val wallpaperManager = WallpaperManager.getInstance(context)
                wallpaperManager.drawable?.toBitmap()
            } catch (_: Exception) {
                null
            }
        }

        // https://stackoverflow.com/questions/76587418/how-save-an-image-from-gallery-to-internal-memory-using-jetpack-compose
        // https://stackoverflow.com/questions/5963535/java-lang-illegalargumentexception-contains-a-path-separator
        fun saveImageToAppData(context: Context, uri: Uri): Boolean {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val file = File("${context.filesDir.path}/image/background.jpg")
                run {
                    File("${context.filesDir.path}/image").mkdir()
                    file.createNewFile()
                }
                inputStream?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                        output.close()
                    }
                    input.close()
                }

                return true
            } catch (_: Exception) {
                return false
            }
        }

        // https://stackoverflow.com/questions/75172380/kotlin-jetpack-compose-display-bytearray-or-filestream-as-image-in-android
        fun getImageFromAppData(context: Context): Bitmap? {
            return try {
                val file = File("${context.filesDir.path}/image/background.jpg")
                BitmapFactory.decodeByteArray(file.readBytes(), 0, file.readBytes().size)
            } catch (_: Exception) {
                null
            }
        }
    }
}