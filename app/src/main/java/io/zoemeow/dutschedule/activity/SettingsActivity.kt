package io.zoemeow.dutschedule.activity

import android.content.Context
import android.util.Log
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import dagger.hilt.android.AndroidEntryPoint
import io.zoemeow.dutschedule.model.settings.BackgroundImageOption
import io.zoemeow.dutschedule.ui.view.settings.ExperimentSettings
import io.zoemeow.dutschedule.ui.view.settings.LanguageSettings
import io.zoemeow.dutschedule.ui.view.settings.MainView
import io.zoemeow.dutschedule.ui.view.settings.NewsNotificationSettings
import io.zoemeow.dutschedule.ui.view.settings.ParseNewsSubjectNotification
import io.zoemeow.dutschedule.utils.BackgroundImageUtil

@AndroidEntryPoint
class SettingsActivity : BaseActivity() {
    @Composable
    override fun OnPreloadOnce() { }

    // When active
    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the photo picker.
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: $uri")
                BackgroundImageUtil.saveImageToAppData(this, uri)
                getMainViewModel().appSettings.value = getMainViewModel().appSettings.value.clone(
                    backgroundImage = BackgroundImageOption.PickFileFromMedia
                )
                getMainViewModel().saveSettings()
                Log.d("PhotoPicker", "Copied!")
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

    @Composable
    override fun OnMainView(
        context: Context,
        snackBarHostState: SnackbarHostState,
        containerColor: Color,
        contentColor: Color
    ) {
        when (intent.action) {
            "settings_newssubjectnewparse" -> {
                ParseNewsSubjectNotification(
                    snackBarHostState = snackBarHostState,
                    containerColor = containerColor,
                    contentColor = contentColor
                )
            }

            "settings_experimentsettings" -> {
                ExperimentSettings(
                    context = context,
                    snackBarHostState = snackBarHostState,
                    containerColor = containerColor,
                    contentColor = contentColor
                )
            }

            "settings_languagesettings" -> {
                LanguageSettings(
                    context = context,
                    snackBarHostState = snackBarHostState,
                    containerColor = containerColor,
                    contentColor = contentColor
                )
            }

            "settings_newsnotificaitonsettings" -> {
                NewsNotificationSettings(
                    context = context,
                    snackBarHostState = snackBarHostState,
                    containerColor = containerColor,
                    contentColor = contentColor
                )
            }

            else -> {
                MainView(
                    context = context,
                    snackBarHostState = snackBarHostState,
                    containerColor = containerColor,
                    contentColor = contentColor,
                    mediaRequest = {
                        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                )
            }
        }
    }

}