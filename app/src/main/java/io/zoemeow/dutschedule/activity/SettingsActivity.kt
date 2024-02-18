package io.zoemeow.dutschedule.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import io.zoemeow.dutschedule.BuildConfig
import io.zoemeow.dutschedule.model.permissionrequest.PermissionList
import io.zoemeow.dutschedule.model.settings.BackgroundImageOption
import io.zoemeow.dutschedule.model.settings.ThemeMode
import io.zoemeow.dutschedule.ui.component.base.DividerItem
import io.zoemeow.dutschedule.ui.component.base.OptionItem
import io.zoemeow.dutschedule.ui.component.base.OptionSwitchItem
import io.zoemeow.dutschedule.ui.component.settings.ContentRegion
import io.zoemeow.dutschedule.ui.component.settings.dialog.DialogAppBackgroundSettings
import io.zoemeow.dutschedule.ui.component.settings.dialog.DialogAppThemeSettings
import io.zoemeow.dutschedule.ui.component.settings.dialog.DialogFetchNewsInBackgroundSettings
import io.zoemeow.dutschedule.ui.view.settings.ExperimentSettings
import io.zoemeow.dutschedule.ui.view.settings.NewsFilterSettings
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
            "settings_newsfilter" -> {
                NewsFilterSettings(
                    snackBarHostState = snackBarHostState,
                    containerColor = containerColor,
                    contentColor = contentColor
                )
            }

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

            else -> {
                View_Main(
                    context = context,
                    snackBarHostState = snackBarHostState,
                    containerColor = containerColor,
                    contentColor = contentColor
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun View_Main(
        context: Context,
        snackBarHostState: SnackbarHostState,
        containerColor: Color,
        contentColor: Color
    ) {
        val dialogAppTheme: MutableState<Boolean> = remember { mutableStateOf(false) }
        val dialogBackground: MutableState<Boolean> = remember { mutableStateOf(false) }
        val dialogFetchNews: MutableState<Boolean> = remember { mutableStateOf(false) }

        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
            containerColor = containerColor,
            contentColor = contentColor,
            topBar = {
                LargeTopAppBar(
                    title = { Text("Settings") },
                    colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = Color.Transparent, scrolledContainerColor = Color.Transparent),
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                setResult(RESULT_OK)
                                finish()
                            },
                            content = {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    "",
                                    modifier = Modifier.size(25.dp)
                                )
                            }
                        )
                    },
                    scrollBehavior = scrollBehavior
                )
            },
            content = {
                Column(
                    modifier = Modifier
                        .padding(it)
                        .verticalScroll(rememberScrollState()),
                    content = {
                        ContentRegion(
                            modifier = Modifier
                                .padding(horizontal = 20.dp)
                                .padding(top = 10.dp),
                            text = "Notifications",
                            content = {
                                OptionItem(
                                    title = "Fetch news in background",
                                    description = when {
                                        (getMainViewModel().appSettings.value.newsBackgroundDuration > 0) ->
                                            String.format(
                                                "Enabled, every %d minute%s",
                                                getMainViewModel().appSettings.value.newsBackgroundDuration,
                                                if (getMainViewModel().appSettings.value.newsBackgroundDuration != 1) "s" else ""
                                            )
                                        else -> "Disabled"
                                    },
                                    onClick = {
                                        if (PermissionRequestActivity.isPermissionGranted(PermissionList.PERMISSION_SCHEDULE_EXACT_ALARM, context)) {
                                            dialogFetchNews.value = true
                                        } else {
                                            showSnackBar(
                                                text = "You need to enable Alarms & reminders in Android app settings to use this feature.",
                                                clearPrevious = true,
                                                actionText = "Open",
                                                action = {
                                                    context.startActivity(
                                                        PermissionList.PERMISSION_SCHEDULE_EXACT_ALARM.extraAction
                                                    )
                                                }
                                            )
                                        }
                                    }
                                )
                                OptionItem(
                                    title = "News filter settings",
                                    description = "Make your filter to only receive your preferred subject news.",
                                    onClick = {
                                        val intent = Intent(context, SettingsActivity::class.java)
                                        intent.action = "settings_newsfilter"
                                        context.startActivity(intent)
                                    }
                                )
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    OptionItem(
                                        title = "System notification settings",
                                        description = "Click here to manage app notifications in Android app settings.",
                                        onClick = {
                                            context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).also { intent ->
                                                intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                                            })
                                        }
                                    )
                                }
                            }
                        )
                        DividerItem(padding = PaddingValues(top = 5.dp, bottom = 15.dp))
                        ContentRegion(
                            modifier = Modifier
                                .padding(horizontal = 20.dp)
                                .padding(top = 10.dp),
                            text = "Appearance",
                            content = {
                                OptionItem(
                                    title = "App theme",
                                    description = String.format(
                                        "%s%s",
                                        when (getMainViewModel().appSettings.value.themeMode) {
                                            ThemeMode.FollowDeviceTheme -> "Follow device theme"
                                            ThemeMode.DarkMode -> "Dark mode"
                                            ThemeMode.LightMode -> "Light mode"
                                        },
                                        if (getMainViewModel().appSettings.value.dynamicColor) " (dynamic color enabled)" else ""
                                    ),
                                    onClick = { dialogAppTheme.value = true }
                                )
                                OptionSwitchItem(
                                    title = "Black background",
                                    description = "Make app background to black color. Only in dark mode and turned off background image.",
                                    isChecked = getMainViewModel().appSettings.value.blackBackground,
                                    onValueChanged = { value ->
                                        getMainViewModel().appSettings.value =
                                            getMainViewModel().appSettings.value.clone(
                                                blackBackground = value
                                            )
                                        saveSettings()
                                    }
                                )
                                OptionItem(
                                    title = "Background image",
                                    description = when (getMainViewModel().appSettings.value.backgroundImage) {
                                        BackgroundImageOption.None -> "None"
                                        BackgroundImageOption.YourCurrentWallpaper -> "Your current wallpaper"
                                        BackgroundImageOption.PickFileFromMedia -> "Your picked image"
                                    },
                                    onClick = { dialogBackground.value = true }
                                )
                                if (getMainViewModel().appSettings.value.backgroundImage != BackgroundImageOption.None) {
                                    OptionItem(
                                        title = "Background opacity",
                                        description = String.format(
                                            "%2.0f%%",
                                            (getMainViewModel().appSettings.value.backgroundImageOpacity * 100)
                                        ),
                                        onClick = {
                                            showSnackBar("This option is in development. Check back soon.", true)
                                            /* TODO: Implement here: Background opacity */
                                        }
                                    )
                                    OptionItem(
                                        title = "Component opacity",
                                        description = String.format(
                                            "%2.0f%%",
                                            (getMainViewModel().appSettings.value.componentOpacity * 100)
                                        ),
                                        onClick = {
                                            showSnackBar("This option is in development. Check back soon.", true)
                                            /* TODO: Implement here: Component opacity */
                                        }
                                    )
                                }
                            }
                        )
                        DividerItem(padding = PaddingValues(top = 5.dp, bottom = 15.dp))
                        ContentRegion(
                            modifier = Modifier
                                .padding(horizontal = 20.dp)
                                .padding(top = 10.dp),
                            text = "Miscellaneous settings",
                            content = {
                                OptionItem(
                                    title = "Application permissions",
                                    description = "Click here for allow and manage app permissions you granted.",
                                    onClick = {
                                        context.startActivity(
                                            Intent(
                                                context,
                                                PermissionRequestActivity::class.java
                                            )
                                        )
                                    }
                                )
                                OptionSwitchItem(
                                    title = "Open link inside app",
                                    description = "Open clicked link without leaving this app. Turn off to open link in default browser.",
                                    isChecked = getMainViewModel().appSettings.value.openLinkInsideApp,
                                    onValueChanged = { value ->
                                        getMainViewModel().appSettings.value =
                                            getMainViewModel().appSettings.value.clone(
                                                openLinkInsideApp = value
                                            )
                                        saveSettings()
                                    }
                                )
                            }
                        )
                        DividerItem(padding = PaddingValues(top = 5.dp, bottom = 15.dp))
                        ContentRegion(
                            modifier = Modifier
                                .padding(horizontal = 20.dp)
                                .padding(top = 10.dp),
                            text = "About",
                            content = {
                                OptionItem(
                                    title = "Version",
                                    description = "Current version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\nClick here to check for update",
                                    onClick = { }
                                )
                                OptionItem(
                                    title = "Changelogs",
                                    description = "Tap to view app changelog",
                                    onClick = {
                                        openLink(
                                            url = "https://github.com/ZoeMeow1027/DutSchedule/blob/stable/CHANGELOG.md",
                                            context = context,
                                            customTab = getMainViewModel().appSettings.value.openLinkInsideApp,
                                        )
                                    }
                                )
                                OptionItem(
                                    title = "GitHub (click to open link)",
                                    description = "https://github.com/ZoeMeow1027/DutSchedule",
                                    onClick = {
                                        openLink(
                                            url = "https://github.com/ZoeMeow1027/DutSchedule",
                                            context = context,
                                            customTab = getMainViewModel().appSettings.value.openLinkInsideApp,
                                        )
                                    }
                                )
                                OptionItem(
                                    title = "Experiment settings",
                                    description = "Our current experiment settings before public.",
                                    onClick = {
                                        val intent = Intent(context, SettingsActivity::class.java)
                                        intent.action = "settings_experimentsettings"
                                        context.startActivity(intent)
                                    }
                                )
                            }
                        )
                    },
                )
            }
        )
        DialogAppThemeSettings(
            isVisible = dialogAppTheme.value,
            themeModeValue = getMainViewModel().appSettings.value.themeMode,
            dynamicColorEnabled = getMainViewModel().appSettings.value.dynamicColor,
            onDismiss = { dialogAppTheme.value = false },
            onValueChanged = { themeMode, dynamicColor ->
                getMainViewModel().appSettings.value = getMainViewModel().appSettings.value.clone(
                    themeMode = themeMode,
                    dynamicColor = dynamicColor
                )
                saveSettings()
            }
        )
        DialogAppBackgroundSettings(
            context = context,
            value = getMainViewModel().appSettings.value.backgroundImage,
            isVisible = dialogBackground.value,
            onDismiss = { dialogBackground.value = false },
            onValueChanged = { value ->
                when (value) {
                    BackgroundImageOption.None -> {
                        getMainViewModel().appSettings.value =
                            getMainViewModel().appSettings.value.clone(
                                backgroundImage = value
                            )
                    }
                    BackgroundImageOption.YourCurrentWallpaper -> {
                        val compPer = PermissionRequestActivity.isPermissionGranted(
                            PermissionList.PERMISSION_MANAGE_EXTERNAL_STORAGE,
                            context = context
                        )
                        if (compPer) {
                            getMainViewModel().appSettings.value =
                                getMainViewModel().appSettings.value.clone(
                                    backgroundImage = value
                                )
                        } else {
                            showSnackBar(
                                text = "You need to grant All files access in Application permission to use this feature. You can use \"Choose a image from media\" without this permission.",
                                clearPrevious = true,
                                actionText = "Grant",
                                action = {
                                    context.startActivity(
                                        PermissionList.PERMISSION_MANAGE_EXTERNAL_STORAGE.extraAction
                                    )
                                }
                            )
                        }
                    }
                    BackgroundImageOption.PickFileFromMedia -> {
                        // Launch the photo picker and let the user choose only images.
                        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                }

                dialogBackground.value = false
                saveSettings()
            }
        )
        DialogFetchNewsInBackgroundSettings(
            isVisible = dialogFetchNews.value,
            value = getMainViewModel().appSettings.value.newsBackgroundDuration,
            onDismiss = { dialogFetchNews.value = false },
            onValueChanged = { value ->
                dialogFetchNews.value = false
                getMainViewModel().appSettings.value = getMainViewModel().appSettings.value.clone(
                    fetchNewsBackgroundDuration = value
                )
                getMainViewModel().saveSettings()
            }
        )
        BackHandler(
            enabled = dialogAppTheme.value || dialogBackground.value || dialogFetchNews.value,
            onBack = {
                dialogAppTheme.value = false
                dialogBackground.value = false
                dialogFetchNews.value = false
            }
        )
    }
}