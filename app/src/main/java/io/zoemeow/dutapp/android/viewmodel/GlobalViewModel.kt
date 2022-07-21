package io.zoemeow.dutapp.android.viewmodel

import android.app.Activity
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.painter.Painter
import androidx.lifecycle.ViewModel
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import io.zoemeow.dutapp.android.model.AppSettingsItem
import io.zoemeow.dutapp.android.model.SchoolYearItem
import io.zoemeow.dutapp.android.model.enums.BackgroundImageType
import io.zoemeow.dutapp.android.utils.GetCurrentHomeWallpaper

class GlobalViewModel: ViewModel() {
    companion object {
        private val instance: MutableState<GlobalViewModel> = mutableStateOf(GlobalViewModel())

        fun getInstance(): GlobalViewModel {
            return instance.value
        }

        fun setInstance(globalViewModel: GlobalViewModel) {
            this.instance.value = globalViewModel
        }
    }

    // App settings here.
    var settings: AppSettingsItem = AppSettingsItem()

    // Main activity
    private val mainActivity: MutableState<Activity?> = mutableStateOf(null)

    // Set main activity
    fun setActivity(activity: Activity) {
        mainActivity.value = activity
    }

    // Drawable and painter for background image
    private val backgroundDrawable: MutableState<Drawable?> = mutableStateOf(null)
    val backgroundPainter: MutableState<Painter?> = mutableStateOf(null)

    // Get current drawable for background image
    @Composable
    fun LoadBackground() {
        // This will get background wallpaper from launcher.
        if (settings.backgroundImageOption == BackgroundImageType.FromLauncher) {
            if (mainActivity.value != null) {
                backgroundDrawable.value = GetCurrentHomeWallpaper.getCurrentWallpaper(mainActivity.value!!)
                backgroundPainter.value = rememberDrawablePainter(drawable = backgroundDrawable.value!!)
            }
        }
        // Otherwise set to null
        else {
            backgroundDrawable.value = null
            backgroundPainter.value = null
        }
    }
}