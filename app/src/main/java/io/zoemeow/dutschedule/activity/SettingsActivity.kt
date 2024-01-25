package io.zoemeow.dutschedule.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import io.zoemeow.dutschedule.BuildConfig
import io.zoemeow.dutschedule.R
import io.zoemeow.dutschedule.model.permissionrequest.PermissionList
import io.zoemeow.dutschedule.model.settings.BackgroundImageOption
import io.zoemeow.dutschedule.model.settings.SubjectCode
import io.zoemeow.dutschedule.model.settings.ThemeMode
import io.zoemeow.dutschedule.ui.component.base.DialogBase
import io.zoemeow.dutschedule.ui.component.base.DividerItem
import io.zoemeow.dutschedule.ui.component.base.OptionItem
import io.zoemeow.dutschedule.ui.component.base.OptionSwitchItem
import io.zoemeow.dutschedule.ui.component.base.SwitchWithTextInSurface
import io.zoemeow.dutschedule.ui.component.settings.ContentRegion
import io.zoemeow.dutschedule.ui.component.settings.dialog.DialogAppBackgroundSettings
import io.zoemeow.dutschedule.ui.component.settings.dialog.DialogAppThemeSettings
import io.zoemeow.dutschedule.ui.component.settings.dialog.DialogFetchNewsInBackgroundSettings
import io.zoemeow.dutschedule.ui.component.settings.dialog.DialogSchoolYearSettings
import io.zoemeow.dutschedule.ui.component.settings.newsfilter.NewsFilterAddInNewsSubject
import io.zoemeow.dutschedule.ui.component.settings.newsfilter.NewsFilterAddManually
import io.zoemeow.dutschedule.ui.component.settings.newsfilter.NewsFilterClearAll
import io.zoemeow.dutschedule.ui.component.settings.newsfilter.NewsFilterCurrentFilter
import io.zoemeow.dutschedule.utils.BackgroundImageUtil
import io.zoemeow.dutschedule.utils.openLink

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
                View_NewsFilterSettings(
                    context = context,
                    snackBarHostState = snackBarHostState,
                    containerColor = containerColor,
                    contentColor = contentColor
                )
            }

            "settings_newssubjectnewparse" -> {
                View_NewParseNotification(
                    context = context,
                    snackBarHostState = snackBarHostState,
                    containerColor = containerColor,
                    contentColor = contentColor
                )
            }

            "settings_experimentsettings" -> {
                View_ExperimentSettings(
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
    private fun View_NewParseNotification(
        context: Context,
        snackBarHostState: SnackbarHostState,
        containerColor: Color,
        contentColor: Color
    ) {
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
                    title = { Text("New parse method on notification") },
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
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .padding(bottom = 5.dp),
                            shape = RoundedCornerShape(30.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(
                                alpha = getControlBackgroundAlpha()
                            ),
                            content = {
                                Column(
                                    modifier = Modifier
                                        .padding(20.dp),
                                    content = {
                                        Text(
                                            "New Making up lesson in a subject",
                                            style = MaterialTheme.typography.titleLarge,
                                            modifier = Modifier.padding(bottom = 5.dp)
                                        )
                                        Text(
                                            when (getMainViewModel().appSettings.value.newsBackgroundParseNewsSubject) {
                                                true -> "Lecturer: ...\nOn ... at lesson(s) ...\nRoom will make up: ..."
                                                false -> "Person messaged: Class will MAKED UP at lesson 1-4, date: dd/MM/yyyy, at room A123"
                                            }
                                        )
                                    }
                                )
                            }
                        )
                        SwitchWithTextInSurface(
                            text = "Use this feature",
                            enabled = true,
                            checked = getMainViewModel().appSettings.value.newsBackgroundParseNewsSubject,
                            onCheckedChange = {
                                getMainViewModel().appSettings.value = getMainViewModel().appSettings.value.clone(
                                    newsBackgroundParseNewsSubject = !getMainViewModel().appSettings.value.newsBackgroundParseNewsSubject
                                )
                            }
                        )
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.Start,
                            modifier = Modifier
                                .wrapContentSize()
                                .padding(horizontal = 20.dp)
                                .padding(top = 20.dp),
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.ic_baseline_info_24),
                                contentDescription = "info_icon",
                                modifier = Modifier.size(24.dp),
                            )
                            Text("Use the new parser for news subject if supported. Turned off or unsupported news subject won't affected.")
                        }
                    }
                )
            }
        )
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun View_NewsFilterSettings(
        context: Context,
        snackBarHostState: SnackbarHostState,
        containerColor: Color,
        contentColor: Color
    ) {
        val tempFilterList = remember {
            mutableStateListOf<SubjectCode>().also {
                it.addAll(getMainViewModel().appSettings.value.newsBackgroundFilterList)
            }
        }
        val modified = remember { mutableStateOf(false) }
        val exitWithoutSavingDialog = remember { mutableStateOf(false) }

        fun saveChanges(exit: Boolean = false) {
            getMainViewModel().appSettings.value = getMainViewModel().appSettings.value.clone(
                newsFilterList = getMainViewModel().appSettings.value.newsBackgroundFilterList.also {
                    it.clear()
                    it.addAll(tempFilterList.toList())
                }
            )
            getMainViewModel().saveSettings()
            modified.value = false

            if (!exit) {
                showSnackBar(
                    text = "Saved changes!",
                    clearPrevious = true
                )
            } else {
                tempFilterList.clear()
                setResult(RESULT_OK)
                finish()
            }
        }

        fun discardChangesAndExit() {
            tempFilterList.clear()
            setResult(RESULT_CANCELED)
            finish()
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
            containerColor = containerColor,
            contentColor = contentColor,
            topBar = {
                TopAppBar(
                    title = { Text("News filter settings") },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                if (!modified.value) {
                                    discardChangesAndExit()
                                } else {
                                    exitWithoutSavingDialog.value = true
                                }
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
                    actions = {
                        IconButton(
                            onClick = {
                                saveChanges()
                            },
                            content = {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_baseline_save_24),
                                    "",
                                    modifier = Modifier.size(25.dp)
                                )
                            }
                        )
                    }
                )
            },
            content = {
                val tabIndex = remember { mutableIntStateOf(1) }

                Column(
                    modifier = Modifier
                        .padding(it)
                        .padding(horizontal = 7.dp)
                        .verticalScroll(rememberScrollState()),
                    content = {
                        NewsFilterCurrentFilter(
                            opacity = getControlBackgroundAlpha(),
                            selectedSubjects = tempFilterList,
                            onRemoveRequested = { subjectCode ->
                                tempFilterList.remove(subjectCode)
                                modified.value = true
                                showSnackBar(
                                    text = "Removed $subjectCode. Save changes to apply your settings.",
                                    clearPrevious = true
                                )
                            }
                        )
                        NewsFilterAddInNewsSubject(
                            opacity = getControlBackgroundAlpha(),
                            expanded = tabIndex.intValue == 1,
                            onExpanded = { tabIndex.intValue = 1 }
                        )
                        NewsFilterAddManually(
                            opacity = getControlBackgroundAlpha(),
                            expanded = tabIndex.intValue == 2,
                            onExpanded = { tabIndex.intValue = 2 },
                            onSubmit = { schoolYearItem, classItem, subjectName ->
                                tempFilterList.add(
                                    SubjectCode(
                                        studentYearId = schoolYearItem,
                                        classId = classItem,
                                        subjectName = subjectName
                                    )
                                )
                                modified.value = true
                                showSnackBar(
                                    text = "Added ${schoolYearItem}.${classItem}. Save changes to apply your settings.",
                                    clearPrevious = true
                                )
                            }
                        )
                        NewsFilterClearAll(
                            opacity = getControlBackgroundAlpha(),
                            expanded = tabIndex.intValue == 3,
                            onExpanded = { tabIndex.intValue = 3 },
                            onSubmit = {
                                if (tempFilterList.isNotEmpty()) {
                                    tempFilterList.clear()
                                    modified.value = true
                                    showSnackBar(
                                        text = "Cleared! Remember to save changes to apply your settings.",
                                        clearPrevious = true
                                    )
                                } else {
                                    showSnackBar(
                                        text = "Nothing to clear!",
                                        clearPrevious = true
                                    )
                                }
                            }
                        )
                    }
                )
            }
        )
        DialogBase(
            modifier = Modifier
                .fillMaxWidth()
                .padding(25.dp),
            canDismiss = false,
            isTitleCentered = true,
            title = "Exit without saving?",
            isVisible = exitWithoutSavingDialog.value,
            content = {
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Top,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("You have modified changes. Save them now?\n\n- Yes: Save changes and exit\n- No: Discard changes and exit\n- Cancel: Just close this dialog.")
                }
            },
            actionButtons = {
                TextButton(
                    onClick = {
                        exitWithoutSavingDialog.value = false
                        saveChanges(exit = true)
                    },
                    content = { Text("Yes") },
                    modifier = Modifier.padding(start = 8.dp),
                )
                TextButton(
                    onClick = {
                        exitWithoutSavingDialog.value = false
                        discardChangesAndExit()
                    },
                    content = { Text("No") },
                    modifier = Modifier.padding(start = 8.dp),
                )
                TextButton(
                    onClick = {
                        exitWithoutSavingDialog.value = false
                    },
                    content = { Text("Cancel") },
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        )
        BackHandler(
            enabled = modified.value,
            onBack = {
                exitWithoutSavingDialog.value = true
            }
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun View_ExperimentSettings(
        context: Context,
        snackBarHostState: SnackbarHostState,
        containerColor: Color,
        contentColor: Color
    ) {
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        val dialogSchoolYear = remember { mutableStateOf(false) }

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
            containerColor = containerColor,
            contentColor = contentColor,
            topBar = {
                LargeTopAppBar(
                    title = { Text("Experiment settings") },
                    colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = Color.Transparent, scrolledContainerColor = Color.Transparent),
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                setResult(RESULT_CANCELED)
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
                            text = "Global variables settings",
                            content = {
                                OptionItem(
                                    title = "Current school year settings",
                                    description = String.format(
                                        "Year: 20%d-20%d, Semester: %s%s",
                                        getMainViewModel().appSettings.value.currentSchoolYear.year,
                                        getMainViewModel().appSettings.value.currentSchoolYear.year + 1,
                                        when (getMainViewModel().appSettings.value.currentSchoolYear.semester) {
                                            1 -> "1"
                                            2 -> "2"
                                            else -> "2"
                                        },
                                        if (getMainViewModel().appSettings.value.currentSchoolYear.semester > 2) " (in summer)" else ""
                                    ),
                                    onClick = {
                                        dialogSchoolYear.value = true
                                    }
                                )
                                OptionItem(
                                    title = "New parse method on notification",
                                    description = when (getMainViewModel().appSettings.value.newsBackgroundParseNewsSubject) {
                                        true -> "Enabled (special notification for news subject)"
                                        false -> "Disabled (regular notification for news subject)"
                                    },
                                    onClick = {
                                        val intent = Intent(context, SettingsActivity::class.java)
                                        intent.action = "settings_newssubjectnewparse"
                                        context.startActivity(intent)
                                    }
                                )
                            }
                        )
                    }
                )
            }
        )
        DialogSchoolYearSettings(
            isVisible = dialogSchoolYear.value,
            dismissRequested = { dialogSchoolYear.value = false },
            currentSchoolYearItem = getMainViewModel().appSettings.value.currentSchoolYear,
            onSubmit = {
                getMainViewModel().appSettings.value = getMainViewModel().appSettings.value.clone(
                    currentSchoolYear = it
                )
                saveSettings()
                dialogSchoolYear.value = false
            }
        )
    }
}