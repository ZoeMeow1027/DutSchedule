package io.zoemeow.subjectnotifier.view.settings

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.zoemeow.subjectnotifier.*
import io.zoemeow.subjectnotifier.R
import io.zoemeow.subjectnotifier.model.appsettings.AppSettings
import io.zoemeow.subjectnotifier.ui.custom.CustomDivider
import io.zoemeow.subjectnotifier.ui.custom.SettingsOptionHeader
import io.zoemeow.subjectnotifier.ui.custom.SettingsOptionItemClickable
import io.zoemeow.subjectnotifier.ui.custom.SettingsOptionItemSwitch
import io.zoemeow.subjectnotifier.utils.AppUtils
import io.zoemeow.subjectnotifier.view.MainActivity
import io.zoemeow.subjectnotifier.view.NewsFilterSettingsActivity
import io.zoemeow.subjectnotifier.view.PermissionRequestActivity
import io.zoemeow.subjectnotifier.viewmodel.MainViewModel

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
        contentColor = if (mainViewModel.isDarkTheme.value) Color.White else Color.Black,
        topBar = {
            TopAppBar(
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
                SettingsOptionHeader(headerText = stringResource(id = R.string.settings_category_news))
                SettingsOptionItemSwitch(
                    title = stringResource(id = R.string.settings_loadnewsinbackground_name),
                    description = stringResource(id = R.string.settings_loadnewsinbackground_description),
                    value = mainViewModel.appSettings.value.refreshNewsEnabled,
                    onValueChanged = { value ->
                        mainViewModel.appSettings.value =
                            mainViewModel.appSettings.value.modify(
                                optionToModify = AppSettings.NEWSINBACKGROUND_ENABLED,
                                value = value
                            )
                        mainViewModel.requestSaveChanges()

                        val activity = context as MainActivity
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                            activity.onPermissionResult(
                                permission = Manifest.permission.POST_NOTIFICATIONS,
                                granted = true,
                                notifyToUser = true
                            )
                        } else if (PermissionRequestActivity.checkPermission(
                                activity,
                                Manifest.permission.POST_NOTIFICATIONS
                            )
                        ) {
                            activity.onPermissionResult(
                                permission = Manifest.permission.POST_NOTIFICATIONS,
                                granted = true,
                                notifyToUser = true
                            )
                        } else {
                            val requestIntent =
                                Intent(activity, PermissionRequestActivity::class.java)
                            requestIntent.putExtra(
                                "permissions.list",
                                arrayOf(Manifest.permission.POST_NOTIFICATIONS)
                            )
                            activity.startActivity(requestIntent)
                        }
                    }
                )
                AnimatedVisibility(
                    visible = mainViewModel.appSettings.value.refreshNewsEnabled
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .clip(RoundedCornerShape(25.dp)),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7F),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.Start,
                        ) {
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
                                description = String.format(
                                    stringResource(
                                        id =
                                        if (mainViewModel.appSettings.value.refreshNewsIntervalInMinute == 1)
                                            R.string.settings_loadnewsinbackground_interval_valuepartial
                                        else R.string.settings_loadnewsinbackground_interval_value
                                    ),
                                    mainViewModel.appSettings.value.refreshNewsIntervalInMinute
                                ),
                                clickable = {
                                    refreshNewsTimeIntervalEnabled.value = true
                                }
                            )
                        }
                    }
                }
                SettingsOptionItemClickable(
                    title = stringResource(id = R.string.settings_subjectnewsfilter_name),
                    description = stringResource(id = R.string.settings_subjectnewsfilter_description),
                    clickable = {
                        val intent = Intent(context, NewsFilterSettingsActivity::class.java)
                        context.startActivity(intent)
                    }
                )
                CustomDivider()
                SettingsOptionHeader(headerText = stringResource(id = R.string.settings_category_account))
                SettingsOptionItemClickable(
                    title = stringResource(id = R.string.settings_schoolyear_name),
                    description = String.format(
                        stringResource(id = R.string.settings_schoolyear_value),
                        mainViewModel.appSettings.value.schoolYear.year,
                        mainViewModel.appSettings.value.schoolYear.year + 1,
                        mainViewModel.appSettings.value.schoolYear.semester
                    ),
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
                    description = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                        stringResource(id = R.string.settings_backgroundimage_disabledandroid13)
                    else backgroundImageOptionList[mainViewModel.appSettings.value.backgroundImage.option.ordinal],
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
                    description = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                )
                SettingsOptionItemClickable(
                    title = stringResource(id = R.string.settings_changelog_name),
                    description = stringResource(id = R.string.settings_changelog_description),
                    clickable = {
                        AppUtils.openLink(
                            "https://github.com/ZoeMeow5466/SubjectNotifier/blob/stable/CHANGELOG.md",
                            context,
                            true
                        )
                    }
                )
                SettingsOptionItemClickable(
                    title = stringResource(id = R.string.settings_githubrepo_name),
                    description = "https://github.com/ZoeMeow5466/SubjectNotifier",
                    clickable = {
                        AppUtils.openLink(
                            "https://github.com/ZoeMeow5466/SubjectNotifier",
                            context,
                            true
                        )
                    }
                )
            }
        }
    )
}