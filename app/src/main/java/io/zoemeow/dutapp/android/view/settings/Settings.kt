package io.zoemeow.dutapp.android.view.settings

import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import io.zoemeow.dutapp.android.BuildConfig
import io.zoemeow.dutapp.android.model.enums.OpenLinkType
import io.zoemeow.dutapp.android.ui.custom.CustomDivider
import io.zoemeow.dutapp.android.ui.custom.SettingsOptionHeader
import io.zoemeow.dutapp.android.ui.custom.SettingsOptionItemClickable
import io.zoemeow.dutapp.android.ui.custom.SettingsOptionItemSwitch
import io.zoemeow.dutapp.android.utils.openLink
import io.zoemeow.dutapp.android.viewmodel.GlobalViewModel
import io.zoemeow.dutapp.android.viewmodel.UIStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings() {
    val globalViewModel = GlobalViewModel.getInstance()
    val uiStatus = UIStatus.getInstance()
    val context: MutableState<Context?> = remember { mutableStateOf(null) }
    context.value = LocalContext.current

    val schoolYearSettingsEnabled = remember { mutableStateOf(false) }
    val appThemeSettingsEnabled = remember { mutableStateOf(false) }
    val backgroundImageSettingsEnabled = remember { mutableStateOf(false) }
    val openLinkTypeSettingsEnabled = remember { mutableStateOf(false) }

    // Just trigger to recompose, this doesn't do anything special!
    val text1 = remember { mutableStateOf("Layout") }
    LaunchedEffect(uiStatus.triggerUpdateComposeUI.value) {
        text1.value = "Layout"
    }

    val openAppInOptionList = listOf(
        "Built-in browser",
        "Default browser custom tab",
        "External browser"
    )
    val backgroundImageOptionList = listOf(
        "Unset",
        "From device wallpaper",
        "Specific a image"
    )

    SettingsSchoolYear(schoolYearSettingsEnabled, globalViewModel, uiStatus)
    SettingsAppTheme(appThemeSettingsEnabled, globalViewModel, uiStatus)
    SettingsBackgroundImage(backgroundImageSettingsEnabled, globalViewModel, uiStatus)
    SettingsOpenLinkType(openLinkTypeSettingsEnabled, globalViewModel, uiStatus)
    Scaffold(
        containerColor = Color.Transparent,
        contentColor = if (uiStatus.mainActivityIsDarkTheme.value) Color.White else Color.Black,
        topBar = {
            SmallTopAppBar(
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color.Transparent
                ),
                title = {
                    Text(text = "Settings")
                }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
                content = {
                    SettingsOptionHeader(headerText = text1.value)
                    val themeList = listOf("Follow device theme", "Dark mode", "Light mode")
                    SettingsOptionItemClickable(
                        title = "App theme",
                        description = (
                                themeList[globalViewModel.appTheme.value.ordinal] +
                                        " ${
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                                if (globalViewModel.dynamicColorEnabled.value)
                                                    "(dynamic color enabled)"
                                                else ""
                                            } else ""
                                        }"
                                ),
                        clickable = {
                            appThemeSettingsEnabled.value = true
                        }
                    )
                    SettingsOptionItemSwitch(
                        title = "Black background",
                        description = "Useful if your device has AMOLED display. Requires dark mode enabled.",
                        value = globalViewModel.blackTheme.value,
                        onValueChanged = {
                            globalViewModel.blackTheme.value = !globalViewModel.blackTheme.value
                            globalViewModel.requestSaveSettings()
                        }
                    )
                    SettingsOptionItemClickable(
                        title = "Background Image",
                        description = backgroundImageOptionList[globalViewModel.backgroundImage.value.option.ordinal],
                        clickable = {
                            backgroundImageSettingsEnabled.value = true
                        }
                    )
                    CustomDivider()
                    SettingsOptionHeader(headerText = "Accounts")
                    SettingsOptionItemClickable(
                        title = "Change school year",
                        description = "School year: " +
                                "20${globalViewModel.schoolYear.value.year}-" +
                                "20${globalViewModel.schoolYear.value.year + 1}, " +
                                "Semester: ${
                                    if (globalViewModel.schoolYear.value.semester < 3)
                                        globalViewModel.schoolYear.value.semester
                                    else
                                        "3 (in summer)"
                                }\n(change this will affect to your subjects schedule)",
                        clickable = {
                            schoolYearSettingsEnabled.value = true
                        }
                    )
                    CustomDivider()
                    SettingsOptionHeader(headerText = "Miscellaneous")
                    SettingsOptionItemClickable(
                        title = "Open link in",
                        description = openAppInOptionList.get(globalViewModel.openLinkType.value.ordinal),
                        clickable = {
                            openLinkTypeSettingsEnabled.value = true
                        }
                    )
                    CustomDivider()
                    SettingsOptionHeader(headerText = "About Application")
                    SettingsOptionItemClickable(
                        title = "Version",
                        description = BuildConfig.VERSION_NAME,
                    )
                    SettingsOptionItemClickable(
                        title = "Changelog",
                        description = "Click here to view changelog for this application.",
                        clickable = {
                            openLink(
                                "https://github.com/ZoeMeow5466/DUTApp.Android",
                                context.value!!,
                                OpenLinkType.InCustomTabs
                            )
                        }
                    )
                    SettingsOptionItemClickable(
                        title = "GitHub (click to open in browser)",
                        description = "https://github.com/ZoeMeow5466/DUTApp.Android",
                        clickable = {
                            openLink(
                                "https://github.com/ZoeMeow5466/DUTApp.Android",
                                context.value!!,
                                OpenLinkType.InCustomTabs
                            )
                        }
                    )
                }
            )
        }
    )
}