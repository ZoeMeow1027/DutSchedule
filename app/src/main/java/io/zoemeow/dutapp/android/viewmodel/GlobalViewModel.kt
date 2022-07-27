package io.zoemeow.dutapp.android.viewmodel

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import dagger.hilt.android.lifecycle.HiltViewModel
import io.zoemeow.dutapp.android.MainActivity
import io.zoemeow.dutapp.android.model.enums.BackgroundImageType
import io.zoemeow.dutapp.android.repository.SettingsFileRepository
import io.zoemeow.dutapp.android.utils.GetCurrentHomeWallpaper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GlobalViewModel @Inject constructor(
    private val settingsFileRepository: SettingsFileRepository
): ViewModel() {
    companion object {
        private val instance: MutableState<GlobalViewModel?> = mutableStateOf(null)

        fun getInstance(): GlobalViewModel {
            return instance.value!!
        }

        fun setInstance(globalViewModel: GlobalViewModel) {
            this.instance.value = globalViewModel
        }
    }

    // App background image
    val backgroundImage = settingsFileRepository.backgroundImage

    // App dynamic color
    val dynamicColorEnabled = settingsFileRepository.dynamicColorEnabled

    // App mode layout
    val appTheme = settingsFileRepository.appTheme

    // School year settings
    var schoolYear = settingsFileRepository.schoolYear

    // Black theme (for AMOLED display)
    var blackTheme = settingsFileRepository.blackTheme

    fun requestSaveSettings() {
        settingsFileRepository.saveSettings()
    }



    // Just trigger. This doesn't do anything.
    val triggerUpdateVar: MutableState<Boolean> = mutableStateOf(false)
    fun update() {
        triggerUpdateVar.value = !triggerUpdateVar.value
    }



    // Main activity
    private val mainActivity: MutableState<Activity?> = mutableStateOf(null)

    // Set main activity
    fun setActivity(activity: Activity) {
        mainActivity.value = activity
    }

    // Drawable and painter for background image
    val backgroundDrawable: MutableState<Drawable?> = mutableStateOf(null)

    /**
     * Get current drawable for background image. Image loaded will save to backgroundPainter.
     */
    fun loadBackground() {
        // This will get background wallpaper from launcher.
        if (backgroundImage.value.option == BackgroundImageType.FromWallpaper) {
            if (mainActivity.value != null) {
                GetCurrentHomeWallpaper.getCurrentWallpaper(
                    activity = mainActivity.value!!,
                    onResult = { successful, drawable ->
                        if (successful) {
                            backgroundDrawable.value = drawable
                        }
                        else {
                            backgroundImage.value.option = BackgroundImageType.Unset
                            backgroundImage.value.path = null
                            showMessageSnackBar(
                                """Missing permission for getting your device wallpaper! BackgroundImage settings will revert to unset.""")
                        }
                    }
                )
            }
        }
        // Otherwise set to null
        else {
            backgroundDrawable.value = null
        }
    }

    // SnackBar host state for MainActivity Scaffold
    lateinit var snackBarHostState: SnackbarHostState

    /**
     * Show message in snack bar in MainActivity Scaffold.
     *
     * @param msg Message to show
     */
    fun showMessageSnackBar(msg: String) {
        if (!this::snackBarHostState.isInitialized)
            return

        CoroutineScope(Dispatchers.IO).launch {
            snackBarHostState.showSnackbar(msg)
        }
    }
}