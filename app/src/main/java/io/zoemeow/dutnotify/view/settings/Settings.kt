package io.zoemeow.dutnotify.view.settings

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import io.zoemeow.dutnotify.*
import io.zoemeow.dutnotify.R
import io.zoemeow.dutnotify.model.appsettings.AppSettings
import io.zoemeow.dutnotify.ui.custom.CustomDivider
import io.zoemeow.dutnotify.ui.custom.SettingsOptionHeader
import io.zoemeow.dutnotify.ui.custom.SettingsOptionItemClickable
import io.zoemeow.dutnotify.ui.custom.SettingsOptionItemSwitch
import io.zoemeow.dutnotify.utils.openLink
import io.zoemeow.dutnotify.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(
    mainViewModel: MainViewModel,
) {
    val context = LocalContext.current

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
        contentColor = if (mainViewModel.mainActivityIsDarkTheme.value) Color.White else Color.Black,
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
            ) {
                SettingsOptionHeader(headerText = stringResource(id = R.string.settings_category_loadnewsinbackground))
                SettingsOptionItemSwitch(
                    title = stringResource(id = R.string.settings_loadnewsinbackground_enabled_name),
                    description = stringResource(id = R.string.settings_loadnewsinbackground_enabled_description),
                    value = mainViewModel.appSettings.value.refreshNewsEnabled,
                    onValueChanged = { value ->
                        mainViewModel.appSettings.value =
                            mainViewModel.appSettings.value.modify(
                                optionToModify = AppSettings.NEWSINBACKGROUND_ENABLED,
                                value = value
                            )
                        mainViewModel.requestSaveChanges()

                        val activity = context as MainActivity
                        if (PermissionRequestActivity.checkPermission(activity, Manifest.permission.POST_NOTIFICATIONS)) {
                            activity.onPermissionResult(
                                permission = Manifest.permission.POST_NOTIFICATIONS,
                                granted = true,
                                notifyToUser = true
                            )
                        } else {
                            val requestIntent = Intent(activity, PermissionRequestActivity::class.java)
                            requestIntent.putExtra(
                                "permissions.list",
                                arrayOf(Manifest.permission.POST_NOTIFICATIONS)
                            )
                            activity.startActivity(requestIntent)
                        }
                    }
                )
                if (mainViewModel.appSettings.value.refreshNewsEnabled) {
                    SettingsOptionItemClickable(
                        title = stringResource(id = R.string.settings_loadnewsinbackground_timeactive_name),
                        description = String.format(
                            stringResource(id = R.string.settings_loadnewsinbackground_timeactive_value),
                            mainViewModel.appSettings.value.refreshNewsTimeStart,
                            mainViewModel.appSettings.value.refreshNewsTimeEnd,
                            if (mainViewModel.appSettings.value.refreshNewsTimeEnd < mainViewModel.appSettings.value.refreshNewsTimeStart)
                                stringResource(id = R.string.settings_loadnewsinbackground_timeactive_valueextended) else ""
                        ),
                        clickable = {
                            refreshNewsTimeAdjustEnabled.value = true
                        }
                    )
                    SettingsOptionItemClickable(
                        title = stringResource(id = R.string.settings_loadnewsinbackground_interval_name),
                        description = "Every ${mainViewModel.appSettings.value.refreshNewsIntervalInMinute} minute" +
                                if (mainViewModel.appSettings.value.refreshNewsIntervalInMinute > 1) "s" else "",
                        clickable = {
                            refreshNewsTimeIntervalEnabled.value = true
                        }
                    )
                }
                SettingsOptionItemClickable(
                    title = "News notification filter",
                    description = "Use this if you want to receive notification related with your subject news only.",
                    clickable = {
                        val intent = Intent(context, NewsFilterSettingsActivity::class.java)
                        context.startActivity(intent)
                    }
                )
                CustomDivider()
                SettingsOptionHeader(headerText = stringResource(id = R.string.settings_category_account))
                SettingsOptionItemClickable(
                    title = "School year",
                    description = "School year: " +
                            "20${mainViewModel.appSettings.value.schoolYear.year}-" +
                            "20${mainViewModel.appSettings.value.schoolYear.year + 1}, " +
                            "Semester: ${
                                if (mainViewModel.appSettings.value.schoolYear.semester < 3)
                                    mainViewModel.appSettings.value.schoolYear.semester
                                else
                                    "3 (in summer)"
                            }\n(change this will affect to your subjects schedule)",
                    clickable = {
                        schoolYearSettingsEnabled.value = true
                    }
                )
                CustomDivider()
                SettingsOptionHeader(headerText = stringResource(id = R.string.settings_category_appearance))
                SettingsOptionItemClickable(
                    title = stringResource(id = R.string.settings_apptheme_name),
                    description = (
                            appThemeOptionList[mainViewModel.appSettings.value.appTheme.ordinal] +
                                    " ${
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                            if (mainViewModel.appSettings.value.dynamicColorEnabled)
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
                    value = mainViewModel.appSettings.value.blackThemeEnabled,
                    onValueChanged = {
                        mainViewModel.appSettings.value =
                            mainViewModel.appSettings.value.modify(
                                optionToModify = AppSettings.APPEARANCE_BLACKTHEME_ENABLED,
                                value = !mainViewModel.appSettings.value.blackThemeEnabled
                            )
                        mainViewModel.requestSaveChanges()
                    }
                )
                SettingsOptionItemClickable(
                    title = stringResource(id = R.string.settings_backgroundimage_name),
                    description = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
                        backgroundImageOptionList[mainViewModel.appSettings.value.backgroundImage.option.ordinal]
                    else "This feature is temporary disabled on Android 13",
                    clickable = {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
                            backgroundImageSettingsEnabled.value = true
                    }
                )
                CustomDivider()
                SettingsOptionHeader(headerText = stringResource(id = R.string.settings_category_miscellaneous))
                SettingsOptionItemSwitch(
                    stringResource(id = R.string.settings_openlinkincustomtab_name),
                    description = stringResource(id = R.string.settings_openlinkincustomtab_description),
                    value = mainViewModel.appSettings.value.openLinkInCustomTab,
                    onValueChanged = {
                        mainViewModel.appSettings.value =
                            mainViewModel.appSettings.value.modify(
                                optionToModify = AppSettings.MISCELLANEOUS_OPENLINKINCUSTOMTAB,
                                value = !mainViewModel.appSettings.value.openLinkInCustomTab
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
                            "https://github.com/ZoeMeow5466/DUTNotify/commits/",
                            context,
                            true
                        )
                    }
                )
                SettingsOptionItemClickable(
                    title = stringResource(id = R.string.settings_github_name),
                    description = "https://github.com/ZoeMeow5466/DUTNotify",
                    clickable = {
                        openLink(
                            "https://github.com/ZoeMeow5466/DUTNotify",
                            context,
                            true
                        )
                    }
                )
            }
        }
    )
}