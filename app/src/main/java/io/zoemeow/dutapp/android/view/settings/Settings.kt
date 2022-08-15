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
import androidx.compose.ui.res.stringResource
import io.zoemeow.dutapp.android.BuildConfig
import io.zoemeow.dutapp.android.R
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

    val openLinkInOptionList = listOf(
        stringResource(id = R.string.settings_openlinkin_builtinbrowser),
        stringResource(id = R.string.settings_openlinkin_customtab),
        stringResource(id = R.string.settings_openlinkin_external),
    )
    val backgroundImageOptionList = listOf(
        "Unset",
        "From device wallpaper",
        "Specific a image"
    )
    val appThemeOptionList = listOf(
        stringResource(id = R.string.settings_apptheme_followsystem),
        stringResource(id = R.string.settings_apptheme_dark),
        stringResource(id = R.string.settings_apptheme_light),
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
                    Text(text = stringResource(id = R.string.navbar_settings))
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
                    SettingsOptionItemClickable(
                        title = stringResource(id = R.string.settings_apptheme_name),
                        description = (
                                appThemeOptionList[globalViewModel.appTheme.value.ordinal] +
                                        " ${
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                                if (globalViewModel.dynamicColorEnabled.value)
                                                    stringResource(id = R.string.settings_apptheme_dynamiccolor_enabled)
                                                else ""
                                            } else ""
                                        }"
                                ),
                        clickable = {
                            appThemeSettingsEnabled.value = true
                        }
                    )
                    SettingsOptionItemSwitch(
                        title = stringResource(id = R.string.settings_blacktheme_name),
                        description = stringResource(id = R.string.settings_blacktheme_description),
                        value = globalViewModel.blackTheme.value,
                        onValueChanged = {
                            globalViewModel.blackTheme.value = !globalViewModel.blackTheme.value
                            globalViewModel.requestSaveSettings()
                        }
                    )
                    SettingsOptionItemClickable(
                        title = stringResource(id = R.string.settings_backgroundimage_name),
                        description = backgroundImageOptionList[globalViewModel.backgroundImage.value.option.ordinal],
                        clickable = {
                            backgroundImageSettingsEnabled.value = true
                        }
                    )
                    CustomDivider()
                    SettingsOptionHeader(headerText = stringResource(id = R.string.settings_category_account))
                    SettingsOptionItemClickable(
                        title = "School year",
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
                    SettingsOptionHeader(headerText = stringResource(id = R.string.settings_category_miscellaneous))
                    SettingsOptionItemClickable(
                        title = stringResource(id = R.string.settings_openlinkin_name),
                        description = openLinkInOptionList[globalViewModel.openLinkType.value.ordinal],
                        clickable = {
                            openLinkTypeSettingsEnabled.value = true
                        }
                    )
                    CustomDivider()
                    SettingsOptionHeader(headerText = stringResource(id = R.string.settings_category_aboutapplication))
                    SettingsOptionItemClickable(
                        title = stringResource(id = R.string.settings_version_name),
                        description = BuildConfig.VERSION_NAME,
                    )
                    SettingsOptionItemClickable(
                        title = stringResource(id = R.string.settings_changelog_name),
                        description = stringResource(id = R.string.settings_changelog_description),
                        clickable = {
                            openLink(
                                "https://github.com/ZoeMeow5466/DUTApp.Android",
                                context.value!!,
                                OpenLinkType.InCustomTabs
                            )
                        }
                    )
                    SettingsOptionItemClickable(
                        title = stringResource(id = R.string.settings_github_name),
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