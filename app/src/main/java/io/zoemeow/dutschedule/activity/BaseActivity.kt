package io.zoemeow.dutschedule.activity

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import io.zoemeow.dutschedule.model.settings.BackgroundImageOption
import io.zoemeow.dutschedule.model.settings.ThemeMode
import io.zoemeow.dutschedule.ui.theme.DutScheduleTheme
import io.zoemeow.dutschedule.utils.BackgroundImageUtil
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
    private lateinit var snackBarHostState: SnackbarHostState
    private lateinit var snackBarScope: CoroutineScope
    private val loadScriptAtStartup = mutableStateOf(true)
    private var focusManager: FocusManager? = null
    private var keyboardController: SoftwareKeyboardController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // A surface container using the 'background' color from the theme
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            // This app is only ever in dark mode, so hard code detectDarkMode to true.
            SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
                detectDarkMode = { true }
            )
        )

        permitAllPolicy()
        setContent {
            // SnackBar state
            snackBarHostState = remember { SnackbarHostState() }
            snackBarScope = rememberCoroutineScope()

            // Initialize focus manager & software keyboard controller
            focusManager = LocalFocusManager.current
            keyboardController = LocalSoftwareKeyboardController.current

            // Initialize MainViewModel
            if (!isMainViewModelInitialized()) {
                mainViewModel = viewModel()
            }

            DutScheduleTheme(
                darkTheme = when (mainViewModel.appSettings.value.themeMode) {
                    ThemeMode.DarkMode -> true
                    ThemeMode.LightMode -> false
                    ThemeMode.FollowDeviceTheme -> isSystemInDarkTheme()
                },
                dynamicColor = mainViewModel.appSettings.value.dynamicColor,
                translucentStatusBar = getMainViewModel().appSettings.value.backgroundImage != BackgroundImageOption.None,
                content = {
                    val context = LocalContext.current

                    val draw: Bitmap? = when (mainViewModel.appSettings.value.backgroundImage) {
                        BackgroundImageOption.None -> null
                        BackgroundImageOption.YourCurrentWallpaper -> BackgroundImageUtil.getCurrentWallpaperBackground(context)
                        BackgroundImageOption.PickFileFromMedia -> BackgroundImageUtil.getImageFromAppData(context)
                    }
                    if (draw != null) {
                        Image(
                            modifier = Modifier.fillMaxSize(),
                            bitmap = draw.asImageBitmap(),
                            contentDescription = "background_image",
                            contentScale = ContentScale.Crop
                        )
                    }

                    OnMainView(
                        snackBarHostState = snackBarHostState,
                        containerColor = when (mainViewModel.appSettings.value.backgroundImage) {
                            BackgroundImageOption.None -> when (mainViewModel.appSettings.value.blackBackground) {
                                true -> if (isAppInDarkMode()) Color.Black else MaterialTheme.colorScheme.background
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
                        context = context
                    )
                },
            )

            // Run startup script once
            if (loadScriptAtStartup.value) {
                loadScriptAtStartup.value = false
                OnPreloadOnce()
            }
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

    fun clearAllFocusAndHideKeyboard() {
        keyboardController?.hide()
        focusManager?.clearFocus(force = true)
    }

    @Composable
    abstract fun OnPreloadOnce()

    @Composable
    abstract fun OnMainView(
        context: Context,
        snackBarHostState: SnackbarHostState,
        containerColor: Color,
        contentColor: Color
    )

    fun getMainViewModel(): MainViewModel {
        if (!isMainViewModelInitialized()) {
            // Initialize MainViewModel if this isn't initialized before.
            mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        }
        return mainViewModel
    }

    fun getControlBackgroundAlpha(): Float {
        return when (mainViewModel.appSettings.value.backgroundImage != BackgroundImageOption.None) {
            true -> mainViewModel.appSettings.value.componentOpacity
            false -> 1f
            // true -> return mainViewModel.appSettings.value.
        }
    }

    fun showSnackBar(
        text: String,
        clearPrevious: Boolean = false,
        duration: SnackbarDuration = SnackbarDuration.Short,
        actionText: String? = null,
        action: (() -> Unit)? = null,
        onDismiss: (() -> Unit)? = null
    ) {
        snackBarScope.launch {
            if (clearPrevious) {
                snackBarHostState.currentSnackbarData?.dismiss()
            }
            val result = snackBarHostState
                .showSnackbar(
                    message = text,
                    actionLabel = actionText,
                    withDismissAction = false,
                    duration = duration
                )
            when (result) {
                SnackbarResult.ActionPerformed -> {
                    if (actionText != null) action?.let { it() }
                }
                SnackbarResult.Dismissed -> {
                    onDismiss?.let { it() }
                }
                else -> { }
            }
        }
    }

    fun openLink(
        url: String,
        context: Context,
        customTab: Boolean = true
    ) {
        when (customTab) {
            false -> {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }

            true -> {
                val builder = CustomTabsIntent.Builder()
                val defaultColors = CustomTabColorSchemeParams.Builder().build()
                builder.setDefaultColorSchemeParams(defaultColors)

                val customTabsIntent = builder.build()
                customTabsIntent.launchUrl(context, Uri.parse(url))
            }
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