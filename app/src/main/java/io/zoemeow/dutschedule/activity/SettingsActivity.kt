package io.zoemeow.dutschedule.activity

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
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
import io.zoemeow.dutschedule.R
import io.zoemeow.dutschedule.model.permissionrequest.PermissionList
import io.zoemeow.dutschedule.model.settings.BackgroundImageOption
import io.zoemeow.dutschedule.model.settings.ThemeMode
import io.zoemeow.dutschedule.ui.component.base.DialogBase
import io.zoemeow.dutschedule.ui.component.newsfilter.NewsFilterAddManually
import io.zoemeow.dutschedule.ui.component.newsfilter.NewsFilterClearAll
import io.zoemeow.dutschedule.ui.component.newsfilter.NewsFilterCurrentFilter
import io.zoemeow.dutschedule.ui.component.settings.DividerItem
import io.zoemeow.dutschedule.ui.component.settings.OptionHeaderItem
import io.zoemeow.dutschedule.ui.component.settings.OptionItem
import io.zoemeow.dutschedule.ui.component.settings.OptionSwitchItem
import io.zoemeow.dutschedule.utils.OpenLink

@AndroidEntryPoint
class SettingsActivity : BaseActivity() {
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
                        OptionSwitchItem(
                            title = "Load news in background",
                            description = "Update news list in background.",
                            switchChecked = false,
                            padding = PaddingValues(horizontal = 20.dp, vertical = 15.dp),
                            onValueChanged = { value ->

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
                                BackgroundImageOption.YourWallpaper -> "Your current wallpaper"
                                BackgroundImageOption.ChooseFromFile -> "Pick a file"
                            },
                            padding = PaddingValues(horizontal = 20.dp, vertical = 15.dp),
                            clicked = { dialogBackground.value = true }
                        )
                        DividerItem(padding = PaddingValues(top = 5.dp, bottom = 15.dp))
                        OptionHeaderItem(
                            text = "Behavior",
                            padding = PaddingValues(horizontal = 20.dp),
                        )
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
                            title = "Version (click to check update)",
                            description = "0.1",
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
                        getMainViewModel().appSettings.value = getMainViewModel().appSettings.value.clone(
                            backgroundImage = it
                        )
                    }
                    BackgroundImageOption.YourWallpaper -> {
                        // When active
                        if (PermissionRequestActivity.isPermissionGranted(
                                PermissionList.PERMISSION_MANAGE_EXTERNAL_STORAGE,
                                context = context
                            )) {
                            getMainViewModel().appSettings.value = getMainViewModel().appSettings.value.clone(
                                backgroundImage = it
                            )
                        }
                    }
                    else -> { }
                }
                saveSettings()
            }
        )
        BackHandler(
            enabled = dialogAppTheme.value || dialogBackground.value,
            onBack = {
                dialogAppTheme.value = false
                dialogBackground.value = false
            }
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun NewsFilterSettingsView() {
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
                val tabIndex = remember { mutableIntStateOf(1) }

                Column(
                    modifier = Modifier.padding(it),
                    content = {
                        NewsFilterCurrentFilter()
                        NewsFilterAddManually(
                            expanded = tabIndex.intValue == 1,
                            onExpanded = { tabIndex.intValue = 1 }
                        )
                        NewsFilterClearAll(
                            expanded = tabIndex.intValue == 2,
                            onExpanded = { tabIndex.intValue = 2 }
                        )
                    }
                )
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
            title = "App theme",
            padding = PaddingValues(15.dp),
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
                        onClick = { onValueClicked?.let { it(ThemeMode.FollowDeviceTheme, dynamicColorEnabled) } }
                    )
                    DialogRadioButton(
                        title = "Light mode",
                        selected = themeModeValue == ThemeMode.LightMode,
                        onClick = { onValueClicked?.let { it(ThemeMode.LightMode, dynamicColorEnabled) } }
                    )
                    DialogRadioButton(
                        title = "Dark mode",
                        selected = themeModeValue == ThemeMode.DarkMode,
                        onClick = { onValueClicked?.let { it(ThemeMode.DarkMode, dynamicColorEnabled) } }
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
            title = "App background",
            padding = PaddingValues(15.dp),
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
                        selected = backgroundValue == BackgroundImageOption.YourWallpaper,
                        onClick = { onValueClicked?.let { it(BackgroundImageOption.YourWallpaper) } }
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
}