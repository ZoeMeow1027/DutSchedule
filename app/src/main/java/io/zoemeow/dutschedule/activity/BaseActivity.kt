package io.zoemeow.dutschedule.activity

import android.graphics.Bitmap
import android.os.Bundle
import android.os.StrictMode
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import io.zoemeow.dutschedule.model.settings.BackgroundImageOption
import io.zoemeow.dutschedule.model.settings.ThemeMode
import io.zoemeow.dutschedule.ui.theme.DutScheduleTheme
import io.zoemeow.dutschedule.util.BackgroundImageUtils
import io.zoemeow.dutschedule.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

abstract class BaseActivity: ComponentActivity() {
    companion object {
        private lateinit var mainViewModel: MainViewModel

        private fun isMainViewModelInitialized(): Boolean {
            return ::mainViewModel.isInitialized
        }
    }
    private lateinit var snackbarHostState: SnackbarHostState
    private lateinit var snackbarScope: CoroutineScope
    private val loadScriptAtStartup = mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permitAllPolicy()

        setContent {
            snackbarHostState = remember { SnackbarHostState() }
            snackbarScope = rememberCoroutineScope()

            if (!isMainViewModelInitialized()) {
                mainViewModel = viewModel()
            }

            if (loadScriptAtStartup.value) {
                loadScriptAtStartup.value = false
                OnPreloadOnce()
            }

            DutScheduleTheme(
                darkTheme = when (mainViewModel.appSettings.value.themeMode) {
                    ThemeMode.DarkMode -> true
                    ThemeMode.LightMode -> false
                    ThemeMode.FollowDeviceTheme -> isSystemInDarkTheme()
                },
                dynamicColor = mainViewModel.appSettings.value.dynamicColor,
                content = {
                    val context = LocalContext.current

                    var draw: Bitmap? = when (mainViewModel.appSettings.value.backgroundImage) {
                        BackgroundImageOption.None -> null
                        BackgroundImageOption.YourCurrentWallpaper -> BackgroundImageUtils.getCurrentWallpaperBackground(context)
                        BackgroundImageOption.PickFileFromMedia -> BackgroundImageUtils.getImageFromAppData(context)
                    }
                    if (draw != null) {
                        Image(
                            modifier = Modifier.fillMaxSize(),
                            bitmap = draw.asImageBitmap(),
                            contentDescription = "background_image",
                            contentScale = ContentScale.Crop
                        )
                    }
                    Scaffold(
                        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                        containerColor = when (mainViewModel.appSettings.value.backgroundImage) {
                            BackgroundImageOption.None -> when (mainViewModel.appSettings.value.blackBackground) {
                                true -> when (mainViewModel.appSettings.value.backgroundImage) {
                                    BackgroundImageOption.None -> if (isAppInDarkMode()) Color.Black else MaterialTheme.colorScheme.background
                                    else -> MaterialTheme.colorScheme.background
                                }
                                false -> MaterialTheme.colorScheme.background
                            }
                            BackgroundImageOption.YourCurrentWallpaper -> MaterialTheme.colorScheme.background.copy(
                                alpha = getMainViewModel().appSettings.value.backgroundImageOpacity
                            )
                            BackgroundImageOption.PickFileFromMedia -> MaterialTheme.colorScheme.background.copy(
                                alpha = getMainViewModel().appSettings.value.backgroundImageOpacity
                            )
                        },
                        contentColor = if (isAppInDarkMode()) Color.White else Color.Black,
                        content = {
                            OnMainView(it)
                        }
                    )
                },
            )
        }
    }

    @Composable
    fun isAppInDarkMode(
        themeMode: ThemeMode = mainViewModel.appSettings.value.themeMode
    ): Boolean {
        return when (themeMode) {
            ThemeMode.LightMode -> false
            ThemeMode.DarkMode -> true
            ThemeMode.FollowDeviceTheme -> isSystemInDarkTheme()
        }
    }

    @Composable
    abstract fun OnPreloadOnce()

    @Composable
    abstract fun OnMainView(padding: PaddingValues)

    fun getMainViewModel(): MainViewModel {
        return mainViewModel
    }

    fun saveSettings() {
        mainViewModel.saveSettings()
    }

    fun showSnackBar(
        text: String,
        clearPrevious: Boolean = false,
    ) {
        snackbarScope.launch {
            if (clearPrevious) {
                snackbarHostState.currentSnackbarData?.dismiss()
            }
            snackbarHostState
                .showSnackbar(
                    message = text,
                    withDismissAction = false,
                    duration = SnackbarDuration.Short
                )
        }
    }

    /**
     * This will bypass network on main thread exception.
     * Use this at your own risk.
     * Target: OkHttp3
     *
     * Source: https://blog.cpming.top/p/android-os-networkonmainthreadexception
     */
    private fun permitAllPolicy() {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
    }
}