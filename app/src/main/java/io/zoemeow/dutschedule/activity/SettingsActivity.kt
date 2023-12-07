package io.zoemeow.dutschedule.activity

import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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
import io.zoemeow.dutschedule.ui.component.settings.DividerItem
import io.zoemeow.dutschedule.ui.component.settings.OptionHeaderItem
import io.zoemeow.dutschedule.ui.component.settings.OptionItem
import io.zoemeow.dutschedule.ui.component.settings.OptionSwitchItem
import io.zoemeow.dutschedule.ui.component.settings.newsfilter.NewsFilterAddManually
import io.zoemeow.dutschedule.ui.component.settings.newsfilter.NewsFilterClearAll
import io.zoemeow.dutschedule.ui.component.settings.newsfilter.NewsFilterCurrentFilter
import io.zoemeow.dutschedule.util.BackgroundImageUtils
import io.zoemeow.dutschedule.util.OpenLink

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
                BackgroundImageUtils.saveImageToAppData(this, uri)
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
    override fun OnMainView(padding: PaddingValues) {
        when (intent.action) {
            "news_filter" -> {
                NewsFilterSettingsView()
            }

            else -> {
                MainView()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainView() {
        val dialogAppTheme: MutableState<Boolean> = remember { mutableStateOf(false) }
        val dialogBackground: MutableState<Boolean> = remember { mutableStateOf(false) }
        val dialogFetchNews: MutableState<Boolean> = remember { mutableStateOf(false) }
        val context = LocalContext.current

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Settings") },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
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
                )
            },
            content = {
                Column(
                    modifier = Modifier
                        .padding(it)
                        .verticalScroll(rememberScrollState()),
                    content = {
                        OptionHeaderItem(
                            text = "News",
                            padding = PaddingValues(
                                top = 10.dp,
                                start = 20.dp,
                                end = 20.dp
                            ),
                        )
                        OptionItem(
                            title = "Fetch news in background",
                            description = when {
                                (getMainViewModel().appSettings.value.fetchNewsBackgroundDuration > 0) ->
                                    String.format(
                                        "Enabled, every %d minute%s",
                                        getMainViewModel().appSettings.value.fetchNewsBackgroundDuration,
                                        if (getMainViewModel().appSettings.value.fetchNewsBackgroundDuration != 1) "s" else ""
                                    )
                                else -> "Disabled"
                            },
                            padding = PaddingValues(horizontal = 20.dp, vertical = 15.dp),
                            clicked = {
                                dialogFetchNews.value = true
                            }
                        )
                        OptionItem(
                            title = "News filter settings",
                            description = "Make your filter to only receive your subject news.",
                            padding = PaddingValues(horizontal = 20.dp, vertical = 15.dp),
                            clicked = {
                                val intent = Intent(context, SettingsActivity::class.java)
                                intent.action = "news_filter"
                                context.startActivity(intent)
                            }
                        )
                        DividerItem(padding = PaddingValues(top = 5.dp, bottom = 15.dp))
                        OptionHeaderItem(
                            text = "Appearance",
                            padding = PaddingValues(
                                top = 10.dp,
                                start = 20.dp,
                                end = 20.dp
                            ),
                        )
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
                            padding = PaddingValues(horizontal = 20.dp, vertical = 15.dp),
                            clicked = { dialogAppTheme.value = true }
                        )
                        OptionSwitchItem(
                            title = "Black background",
                            description = "Make app background to black color. Only in dark mode and turned off background image.",
                            switchChecked = getMainViewModel().appSettings.value.blackBackground,
                            padding = PaddingValues(horizontal = 20.dp, vertical = 15.dp),
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
                            padding = PaddingValues(horizontal = 20.dp, vertical = 15.dp),
                            clicked = { dialogBackground.value = true }
                        )
                        DividerItem(padding = PaddingValues(top = 5.dp, bottom = 15.dp))
                        OptionHeaderItem(
                            text = "Miscellaneous settings",
                            padding = PaddingValues(horizontal = 20.dp),
                        )
                        OptionItem(
                            title = "Application permissions",
                            description = "Click here for allow and manage app permissions you granted.",
                            padding = PaddingValues(horizontal = 20.dp, vertical = 15.dp),
                            clicked = {
                                context.startActivity(
                                    Intent(
                                        context,
                                        PermissionRequestActivity::class.java
                                    )
                                )
                            }
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            OptionItem(
                                title = "System notification settings",
                                description = "Click here to manage app notifications in Android app settings.",
                                padding = PaddingValues(horizontal = 20.dp, vertical = 15.dp),
                                clicked = {
                                    context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).also { intent ->
                                        intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                                    })
                                }
                            )
                        }
                        OptionSwitchItem(
                            title = "Open link inside app",
                            description = "Open clicked link without leaving this app. Turn off to open link in default browser.",
                            switchChecked = getMainViewModel().appSettings.value.openLinkInsideApp,
                            padding = PaddingValues(horizontal = 20.dp, vertical = 15.dp),
                            onValueChanged = { value ->
                                getMainViewModel().appSettings.value =
                                    getMainViewModel().appSettings.value.clone(
                                        openLinkInsideApp = value
                                    )
                                saveSettings()
                            }
                        )
                        DividerItem(padding = PaddingValues(top = 5.dp, bottom = 15.dp))
                        OptionHeaderItem(
                            text = "About",
                            padding = PaddingValues(horizontal = 20.dp),
                        )
                        OptionItem(
                            title = "Version",
                            description = "Current version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\nClick here to check for update",
                            padding = PaddingValues(horizontal = 20.dp, vertical = 15.dp),
                        )
                        OptionItem(
                            title = "Changelogs",
                            description = "Tap to view app changelog",
                            padding = PaddingValues(horizontal = 20.dp, vertical = 15.dp),
                            clicked = {
                                OpenLink(
                                    url = "https://github.com/ZoeMeow1027/DutSchedule/blob/stable/CHANGELOG.md",
                                    context = context,
                                    customTab = getMainViewModel().appSettings.value.openLinkInsideApp,
                                )
                            }
                        )
                        OptionItem(
                            title = "GitHub (click to open link)",
                            description = "https://github.com/ZoeMeow1027/DutSchedule",
                            padding = PaddingValues(horizontal = 20.dp, vertical = 15.dp),
                            clicked = {
                                OpenLink(
                                    url = "https://github.com/ZoeMeow1027/DutSchedule",
                                    context = context,
                                    customTab = getMainViewModel().appSettings.value.openLinkInsideApp,
                                )
                            }
                        )
                    },
                )
            }
        )
        DialogSettingAppTheme(
            themeModeValue = getMainViewModel().appSettings.value.themeMode,
            dynamicColorEnabled = getMainViewModel().appSettings.value.dynamicColor,
            dismissRequested = {
                dialogAppTheme.value = false
            },
            isVisible = dialogAppTheme.value,
            onValueClicked = { themeMode, dynamicColor ->
                getMainViewModel().appSettings.value = getMainViewModel().appSettings.value.clone(
                    themeMode = themeMode,
                    dynamicColor = dynamicColor
                )
                saveSettings()
            }
        )
        DialogSettingAppBackground(
            backgroundValue = getMainViewModel().appSettings.value.backgroundImage,
            isVisible = dialogBackground.value,
            manageStorageGranted = PermissionRequestActivity.isPermissionGranted(
                PermissionList.PERMISSION_MANAGE_EXTERNAL_STORAGE,
                context = context
            ),
            dismissRequested = {
                dialogBackground.value = false
            },
            onValueClicked = {
                when (it) {
                    BackgroundImageOption.None -> {
                        getMainViewModel().appSettings.value =
                            getMainViewModel().appSettings.value.clone(
                                backgroundImage = it
                            )
                    }

                    BackgroundImageOption.YourCurrentWallpaper -> {
                        // When active
                        if (PermissionRequestActivity.isPermissionGranted(
                                PermissionList.PERMISSION_MANAGE_EXTERNAL_STORAGE,
                                context = context
                            )
                        ) {
                            getMainViewModel().appSettings.value =
                                getMainViewModel().appSettings.value.clone(
                                    backgroundImage = it
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
        DialogFetchNewsInBackground(
            isVisible = dialogFetchNews.value,
            dismissRequested = { dialogFetchNews.value = false },
            baseValue = getMainViewModel().appSettings.value.fetchNewsBackgroundDuration,
            onSubmit = {
                dialogFetchNews.value = false
                getMainViewModel().appSettings.value = getMainViewModel().appSettings.value.clone(
                    fetchNewsBackgroundDuration = it
                )
                getMainViewModel().saveSettings()
                // TODO: Check if news service is scheduled.
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
    private fun NewsFilterSettingsView() {
        val tempFilterList = remember {
            mutableStateListOf<SubjectCode>().also {
                it.addAll(getMainViewModel().appSettings.value.newsFilterList)
            }
        }
        val modified = remember { mutableStateOf(false) }
        val exitWithoutSavingDialog = remember { mutableStateOf(false) }

        fun saveChanges(exit: Boolean = false) {
            getMainViewModel().appSettings.value = getMainViewModel().appSettings.value.clone(
                newsFilterList = getMainViewModel().appSettings.value.newsFilterList.also {
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
            containerColor = Color.Transparent,
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
                        NewsFilterAddManually(
                            expanded = tabIndex.intValue == 1,
                            onExpanded = { tabIndex.intValue = 1 },
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
                            expanded = tabIndex.intValue == 2,
                            onExpanded = { tabIndex.intValue = 2 },
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

    @Composable
    private fun DialogSettingAppTheme(
        isVisible: Boolean = false,
        dismissRequested: (() -> Unit)? = null,
        themeModeValue: ThemeMode,
        dynamicColorEnabled: Boolean,
        onValueClicked: ((ThemeMode, Boolean) -> Unit)? = null
    ) {
        DialogBase(
            modifier = Modifier
                .fillMaxWidth()
                .padding(25.dp),
            title = "App theme",
            isVisible = isVisible,
            canDismiss = false,
            isTitleCentered = true,
            dismissClicked = {
                dismissRequested?.let { it() }
            },
            content = {
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Top,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    DialogRadioButton(
                        title = "Follow device theme",
                        selected = themeModeValue == ThemeMode.FollowDeviceTheme,
                        onClick = {
                            onValueClicked?.let {
                                it(
                                    ThemeMode.FollowDeviceTheme,
                                    dynamicColorEnabled
                                )
                            }
                        }
                    )
                    DialogRadioButton(
                        title = "Light mode",
                        selected = themeModeValue == ThemeMode.LightMode,
                        onClick = {
                            onValueClicked?.let {
                                it(
                                    ThemeMode.LightMode,
                                    dynamicColorEnabled
                                )
                            }
                        }
                    )
                    DialogRadioButton(
                        title = "Dark mode",
                        selected = themeModeValue == ThemeMode.DarkMode,
                        onClick = {
                            onValueClicked?.let {
                                it(
                                    ThemeMode.DarkMode,
                                    dynamicColorEnabled
                                )
                            }
                        }
                    )
                    DialogCheckBoxButton(
                        title = "Dynamic color",
                        checked = dynamicColorEnabled,
                        onClick = { value -> onValueClicked?.let { it(themeModeValue, value) } }
                    )
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(top = 20.dp),
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_baseline_info_24),
                            contentDescription = "info_icon",
                            modifier = Modifier.size(24.dp),
                            // tint = if (mainViewModel.isDarkTheme.value) Color.White else Color.Black
                        )
                        Text(
                            "Your OS needs at least:\n" +
                                    "- Android 9 to follow device theme.\n" +
                                    "- Android 12 to enable dynamic color."
                        )
                    }
                }
            },
            actionButtons = {
                TextButton(
                    onClick = { dismissRequested?.let { it() } },
                    content = { Text("OK") },
                    modifier = Modifier.padding(start = 8.dp),
                )
            },
        )
    }

    @Composable
    private fun DialogSettingAppBackground(
        isVisible: Boolean = false,
        manageStorageGranted: Boolean = false,
        dismissRequested: (() -> Unit)? = null,
        backgroundValue: BackgroundImageOption,
        onValueClicked: ((BackgroundImageOption) -> Unit)? = null
    ) {
        DialogBase(
            modifier = Modifier
                .fillMaxWidth()
                .padding(25.dp),
            title = "App background",
            isVisible = isVisible,
            canDismiss = true,
            isTitleCentered = true,
            dismissClicked = {
                dismissRequested?.let { it() }
            },
            content = {
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Top,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    DialogRadioButton(
                        title = "None",
                        selected = backgroundValue == BackgroundImageOption.None,
                        onClick = { onValueClicked?.let { it(BackgroundImageOption.None) } }
                    )
                    DialogRadioButton(
                        title = "Your current wallpaper${if (!manageStorageGranted) "\n(You might need to grant access all file permission)" else ""}",
                        selected = backgroundValue == BackgroundImageOption.YourCurrentWallpaper,
                        onClick = { onValueClicked?.let { it(BackgroundImageOption.YourCurrentWallpaper) } }
                    )
                    DialogRadioButton(
                        title = "Choose a image from media",
                        selected = backgroundValue == BackgroundImageOption.PickFileFromMedia,
                        onClick = { onValueClicked?.let { it(BackgroundImageOption.PickFileFromMedia) } }
                    )
                }
            },
            actionButtons = {
                TextButton(
                    onClick = { dismissRequested?.let { it() } },
                    content = { Text("Cancel") },
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        )
    }

    @Composable
    private fun DialogRadioButton(
        modifier: Modifier = Modifier,
        title: String,
        selected: Boolean,
        enabled: Boolean = true,
        onClick: (() -> Unit)? = null
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .clickable { onClick?.let { it() } },
            color = Color.Transparent,
            content = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 1.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    content = {
                        RadioButton(
                            selected = selected,
                            enabled = enabled,
                            onClick = { onClick?.let { it() } }
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(title)
                        }
                    }
                )
            }
        )
    }

    @Composable
    private fun DialogCheckBoxButton(
        modifier: Modifier = Modifier,
        title: String,
        checked: Boolean,
        enabled: Boolean = true,
        onClick: ((Boolean) -> Unit)? = null
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .clickable { onClick?.let { it(!checked) } },
            color = Color.Transparent,
            content = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 1.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    content = {
                        Checkbox(
                            checked = checked,
                            enabled = enabled,
                            onCheckedChange = { onClick?.let { it(!checked) } }
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(title)
                        }
                    }
                )
            }
        )
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    private fun DialogFetchNewsInBackground(
        isVisible: Boolean = false,
        baseValue: Int = 0,
        dismissRequested: (() -> Unit)? = null,
        onSubmit: ((Int) -> Unit)? = null
    ) {
        val duration = remember { mutableIntStateOf(0) }

        LaunchedEffect(isVisible) {
            duration.intValue = baseValue
        }

        DialogBase(
            modifier = Modifier
                .fillMaxWidth()
                .padding(25.dp),
            title = "Fetch news in background",
            isVisible = isVisible,
            canDismiss = true,
            isTitleCentered = true,
            dismissClicked = {
                dismissRequested?.let { it() }
            },
            content = {
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Top,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        "Drag slider below to adjust news background duration." +
                                "\n - Drag slider to 0 to disable this function." +
                                "\n - If you set this value below than 5 minutes, this will automatically adjust back to 5 minutes.",
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    Slider(
                        valueRange = 0f..240f,
                        steps = 241,
                        value = duration.intValue.toFloat(),
                        onValueChange = {
                            duration.intValue = it.toInt()
                        }
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        content = {
                            Text("${duration.intValue} minute${if (duration.intValue != 1) "s" else ""}")
                        }
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth()
                            .wrapContentHeight()
                            .padding(top = 7.dp),
                        horizontalArrangement = Arrangement.Center,
                        content = {
                            listOf(0, 5, 15, 30, 60).forEach { min ->
                                SuggestionChip(
                                    modifier = Modifier.padding(horizontal = 5.dp),
                                    onClick = {
                                        duration.intValue = min
                                    },
                                    label = { Text(if (min == 0) "Turn off" else "$min min") }
                                )
                            }
                        }
                    )
                }
            },
            actionButtons = {
                TextButton(
                    onClick = { onSubmit?.let { it(duration.intValue) } },
                    content = { Text("Save") },
                    modifier = Modifier.padding(start = 8.dp),
                )
                TextButton(
                    onClick = { dismissRequested?.let { it() } },
                    content = { Text("Cancel") },
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        )
    }
}