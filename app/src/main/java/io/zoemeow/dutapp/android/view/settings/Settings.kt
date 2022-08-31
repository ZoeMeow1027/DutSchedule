package io.zoemeow.dutapp.android.view.settings

import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import io.zoemeow.dutapp.android.BuildConfig
import io.zoemeow.dutapp.android.R
import io.zoemeow.dutapp.android.model.enums.AppSettingsCode
import io.zoemeow.dutapp.android.ui.custom.CustomDivider
import io.zoemeow.dutapp.android.ui.custom.SettingsOptionHeader
import io.zoemeow.dutapp.android.ui.custom.SettingsOptionItemClickable
import io.zoemeow.dutapp.android.ui.custom.SettingsOptionItemSwitch
import io.zoemeow.dutapp.android.utils.openLink
import io.zoemeow.dutapp.android.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(
    mainViewModel: MainViewModel,
) {
    val context: MutableState<Context?> = remember { mutableStateOf(null) }
    context.value = LocalContext.current

    val refreshNewsTimeAdjustEnabled = remember { mutableStateOf(false) }
    val refreshNewsTimeIntervalEnabled = remember { mutableStateOf(false) }
    val schoolYearSettingsEnabled = remember { mutableStateOf(false) }
    val appThemeSettingsEnabled = remember { mutableStateOf(false) }
    val backgroundImageSettingsEnabled = remember { mutableStateOf(false) }

    val backgroundImageOptionList = listOf(
        stringResource(id = R.string.settings_backgroundimage_none),
        stringResource(id = R.string.settings_backgroundimage_fromsystem),
        stringResource(id = R.string.settings_backgroundimage_specific),
    )
    val appThemeOptionList = listOf(
        stringResource(id = R.string.settings_apptheme_followsystem),
        stringResource(id = R.string.settings_apptheme_dark),
        stringResource(id = R.string.settings_apptheme_light),
    )

    SettingsSchoolYear(
        enabled = schoolYearSettingsEnabled,
        mainViewModel = mainViewModel,
    )
    SettingsAppTheme(
        enabled = appThemeSettingsEnabled,
        mainViewModel = mainViewModel,
    )
    SettingsBackgroundImage(
        enabled = backgroundImageSettingsEnabled,
        mainViewModel = mainViewModel,
    )
    SettingsRefreshNewsTimeRange(
        enabled = refreshNewsTimeAdjustEnabled,
        mainViewModel = mainViewModel
    )
    SettingsRefreshNewsInterval(
        enabled = refreshNewsTimeIntervalEnabled,
        mainViewModel = mainViewModel
    )
    Scaffold(
        containerColor = Color.Transparent,
        contentColor = if (mainViewModel.uiStatus.mainActivityIsDarkTheme.value) Color.White else Color.Black,
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
                    SettingsOptionHeader(headerText = "News")
                    SettingsOptionItemClickable(
                        title = "Refresh news time range",
                        description = "From ${mainViewModel.settings.value.refreshNewsTimeStart}" +
                                " to ${mainViewModel.settings.value.refreshNewsTimeEnd}" +
                            if (mainViewModel.settings.value.refreshNewsTimeEnd < mainViewModel.settings.value.refreshNewsTimeStart) " (tomorrow)" else "",
                        clickable = {
                            refreshNewsTimeAdjustEnabled.value = true
                        }
                    )
                    SettingsOptionItemClickable(
                        title = "Refresh news time interval",
                        description = "Every ${mainViewModel.settings.value.refreshNewsIntervalInMinute} minute" +
                                if (mainViewModel.settings.value.refreshNewsIntervalInMinute > 1) "s" else "",
                        clickable = {
                            refreshNewsTimeIntervalEnabled.value = true
                        }
                    )
                    CustomDivider()
                    SettingsOptionHeader(headerText = stringResource(id = R.string.settings_category_account))
                    SettingsOptionItemClickable(
                        title = "School year",
                        description = "School year: " +
                                "20${mainViewModel.settings.value.schoolYear.year}-" +
                                "20${mainViewModel.settings.value.schoolYear.year + 1}, " +
                                "Semester: ${
                                    if (mainViewModel.settings.value.schoolYear.semester < 3)
                                        mainViewModel.settings.value.schoolYear.semester
                                    else
                                        "3 (in summer)"
                                }\n(change this will affect to your subjects schedule)",
                        clickable = {
                            schoolYearSettingsEnabled.value = true
                        }
                    )
                    CustomDivider()
                    SettingsOptionHeader(headerText = "Layout")
                    SettingsOptionItemClickable(
                        title = stringResource(id = R.string.settings_apptheme_name),
                        description = (
                            appThemeOptionList[mainViewModel.settings.value.appTheme.ordinal] +
                                    " ${
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                            if (mainViewModel.settings.value.dynamicColorEnabled)
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
                        value = mainViewModel.settings.value.blackThemeEnabled,
                        onValueChanged = {
                            mainViewModel.settings.value = mainViewModel.settings.value.modify(
                                optionToModify = AppSettingsCode.BlackThemeEnabled,
                                value = !mainViewModel.settings.value.blackThemeEnabled
                            )
                            mainViewModel.requestSaveChanges()
                        }
                    )
                    SettingsOptionItemClickable(
                        title = stringResource(id = R.string.settings_backgroundimage_name),
                        description = backgroundImageOptionList[mainViewModel.settings.value.backgroundImage.option.ordinal],
                        clickable = {
                            backgroundImageSettingsEnabled.value = true
                        }
                    )
                    CustomDivider()
                    SettingsOptionHeader(headerText = stringResource(id = R.string.settings_category_miscellaneous))
                    SettingsOptionItemSwitch(
                        stringResource(id = R.string.settings_openlinkincustomtab_name),
                        description = stringResource(id = R.string.settings_openlinkincustomtab_description),
                        value = mainViewModel.settings.value.openLinkInCustomTab,
                        onValueChanged = {
                            mainViewModel.settings.value = mainViewModel.settings.value.modify(
                                optionToModify = AppSettingsCode.OpenLinkInCustomTab,
                                value = !mainViewModel.settings.value.openLinkInCustomTab
                            )
                            mainViewModel.requestSaveChanges()
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
                                true
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
                                true
                            )
                        }
                    )
                }
            )
        }
    )
}