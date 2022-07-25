package io.zoemeow.dutapp.android.view.settings

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import io.zoemeow.dutapp.android.BuildConfig
import io.zoemeow.dutapp.android.model.enums.BackgroundImageType
import io.zoemeow.dutapp.android.ui.custom.CustomDivider
import io.zoemeow.dutapp.android.ui.custom.SettingsOptionHeader
import io.zoemeow.dutapp.android.ui.custom.SettingsOptionItem
import io.zoemeow.dutapp.android.utils.openLinkInCustomTab
import io.zoemeow.dutapp.android.viewmodel.GlobalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings() {
    val globalViewModel = GlobalViewModel.getInstance()
    val context: MutableState<Context?> = remember { mutableStateOf(null) }
    context.value = LocalContext.current

    val schoolYearSettingsEnabled = remember { mutableStateOf(false) }

    SettingsSchoolYear(schoolYearSettingsEnabled)
    Scaffold(
        containerColor = Color.Transparent,
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
                modifier = Modifier.padding(padding)
                    .verticalScroll(rememberScrollState()),
                content = {
                    if (schoolYearSettingsEnabled.value)
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    SettingsOptionHeader(headerText = "Layout")
                    SettingsOptionItem(
                        title = "App theme",
                        description = "Following system theme (This feature are under development. Stay tuned!)",
                        clickable = { }
                    )
                    SettingsOptionItem(
                        title = "Black backgrounds in dark theme (for AMOLED)",
                        description = "No (This feature are under development. Stay tuned!)",
                        clickable = {  }
                    )
                    SettingsOptionItem(
                        title = "Background Image",
                        description = when (globalViewModel.settings.personalize.backgroundImage.option) {
                            BackgroundImageType.None -> "Unset"
                            BackgroundImageType.FromWallpaper -> "From phone wallpaper"
                            BackgroundImageType.FromItemYouSpecific -> "Specific a image (" +
                                    "${globalViewModel.settings.personalize.backgroundImage.path})"
                        } + " (This feature are under development. Stay tuned!)",
                        clickable = { }
                    )
                    CustomDivider()
                    SettingsOptionHeader(headerText = "Accounts")
                    SettingsOptionItem(
                        title = "Change school year",
                        description = "School year: " +
                                "20${globalViewModel.settings.schoolYear.year}-" +
                                "20${globalViewModel.settings.schoolYear.year + 1}, " +
                                "Semester: ${
                                    if (globalViewModel.settings.schoolYear.semester < 3)
                                        globalViewModel.settings.schoolYear.semester
                                    else
                                        "3 (in summer)"
                                }\n(change this will affect to your subjects schedule)",
                        clickable = {
                            schoolYearSettingsEnabled.value = true
                        }
                    )
                    CustomDivider()
                    SettingsOptionHeader(headerText = "About Application")
                    SettingsOptionItem(
                        title = "Version",
                        description = BuildConfig.VERSION_NAME,
                    )
                    SettingsOptionItem(
                        title = "Changelog",
                        description = "Click here to view changelog for this application.",
                        clickable = {
                            openLinkInCustomTab(
                                context.value!!,
                                "https://github.com/ZoeMeow5466/DUTApp.Android"
                            )
                        }
                    )
                    SettingsOptionItem(
                        title = "GitHub (click to open in browser)",
                        description = "https://github.com/ZoeMeow5466/DUTApp.Android",
                        clickable = {
                            openLinkInCustomTab(
                                context.value!!,
                                "https://github.com/ZoeMeow5466/DUTApp.Android"
                            )
                        }
                    )
                }
            )
        }
    )
}